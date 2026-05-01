package com.tencent.wxcloudrun.utils;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.tencent.wxcloudrun.model.BloggerRawSentiment;
import com.tencent.wxcloudrun.model.DcChannelMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.regex.Matcher;
import java.util.stream.Collectors;

public class BloggerRawPostParser {

    private static final Logger log = LoggerFactory.getLogger(BloggerRawPostParser.class);
    private static final int MAX_RETRIES = 3;
    private static final String SPECIFIED_MODEL = "gemini-3.1-pro-preview";

    private static String promptTemplate = null;

    private static final String IMAGE_URL_PATTERN = "https?://[^\\s\\[\\]\\\"'<>]+(?:\\.jpg|\\.jpeg|\\.png|\\.gif|\\.webp|\\.svg)";

    private static String getPromptTemplate() {
        if (promptTemplate == null) {
            try (InputStreamReader reader = new InputStreamReader(
                    BloggerRawPostParser.class.getClassLoader().getResourceAsStream("prompts/raw_post_elt_prompt.txt"),
                    StandardCharsets.UTF_8);
                 BufferedReader bufferedReader = new BufferedReader(reader)) {
                promptTemplate = bufferedReader.lines().collect(Collectors.joining("\n"));
            } catch (Exception e) {
                log.error("Failed to load prompt template: {}", e.getMessage());
                return "";
            }
        }
        return promptTemplate;
    }

    public static List<BloggerRawSentiment> parseRawPosts(List<DcChannelMessage> messages) {
        List<BloggerRawSentiment> allSentiments = new ArrayList<>();

        if (messages == null || messages.isEmpty()) {
            log.warn("No messages to parse");
            return allSentiments;
        }

        List<MessageWithImages> messagesWithImages = new ArrayList<>();
        for (DcChannelMessage msg : messages) {
            MessageWithImages mwi = new MessageWithImages();
            mwi.message = msg;
            mwi.imageUrls = extractImageUrls(msg.getContent());
            mwi.contentWithoutImages = removeImageUrls(msg.getContent());
            messagesWithImages.add(mwi);
        }

        boolean hasAnyImages = messagesWithImages.stream().anyMatch(m -> m.imageUrls != null && !m.imageUrls.isEmpty());
        if (hasAnyImages) {
            log.info("Found messages with images, using multimodal parsing");
            return parseWithMultimodal(messagesWithImages);
        } else {
            log.info("No images found, using text-only parsing");
            return parseTextOnly(messagesWithImages);
        }
    }

    private static List<BloggerRawSentiment> parseWithMultimodal(List<MessageWithImages> messagesWithImages) {
        List<BloggerRawSentiment> allSentiments = new ArrayList<>();

        for (MessageWithImages mwi : messagesWithImages) {
            DcChannelMessage msg = mwi.message;
            List<String> imageUrls = mwi.imageUrls;

            JSONArray contentParts = new JSONArray();

            JSONObject textPart = new JSONObject();
            textPart.put("type", "text");
            StringBuilder textContent = new StringBuilder();
            textContent.append("时间: ").append(msg.getTimestamp()).append("\n");
            textContent.append("频道: ").append(msg.getChannelName()).append("\n");
            textContent.append("内容: ").append(mwi.contentWithoutImages).append("\n");
            textPart.put("text", textContent.toString());
            contentParts.add(textPart);

            if (imageUrls != null && !imageUrls.isEmpty()) {
                for (String imageUrl : imageUrls) {
                    try {
                        byte[] imageBytes = YunwuApiUtils.downloadImageAsBytes(imageUrl);
                        if (imageBytes != null) {
                            String base64Image = Base64.getEncoder().encodeToString(imageBytes);

                            JSONObject imagePart = new JSONObject();
                            imagePart.put("type", "image_url");

                            JSONObject imageUrlObj = new JSONObject();
                            imageUrlObj.put("url", "data:image/jpeg;base64," + base64Image);
                            imagePart.put("image_url", imageUrlObj);

                            contentParts.add(imagePart);
                            log.info("Successfully converted image {} to base64 ({} bytes)", imageUrl, imageBytes.length);
                        } else {
                            log.warn("Failed to download image: {}", imageUrl);
                        }
                    } catch (Exception e) {
                        log.error("Error processing image {}: {}", imageUrl, e.getMessage());
                    }
                }
            }

            String prompt = getPromptTemplate();
            JSONObject textPrompt = new JSONObject();
            textPrompt.put("type", "text");
            textPrompt.put("text", prompt.replace("[在此处插入原始帖子内容]", "以下是原始帖子内容，请分析：\n" + mwi.contentWithoutImages));
            JSONArray newContentParts = new JSONArray();
            newContentParts.add(textPrompt);
            for (Object part : contentParts) {
                if (part instanceof JSONObject && "image_url".equals(((JSONObject) part).getString("type"))) {
                    newContentParts.add(part);
                }
            }

            for (int retry = 1; retry <= MAX_RETRIES; retry++) {
                try {
                    log.info("Calling LLM API for multimodal parsing, attempt {}/{}", retry, MAX_RETRIES);

                    JSONObject response = YunwuApiUtils.callYunwuApiWithMultimodalContent(newContentParts, SPECIFIED_MODEL);
                    String assistantResponse = YunwuApiUtils.getAssistantResponse(response);

                    if (assistantResponse == null || assistantResponse.isEmpty()) {
                        log.warn("Empty response from LLM API, attempt {}/{}", retry, MAX_RETRIES);
                        if (retry < MAX_RETRIES) {
                            continue;
                        }
                        break;
                    }

                    log.info("LLM API response: {}", assistantResponse);

                    JSONArray jsonArray = parseJsonArray(assistantResponse);
                    if (jsonArray == null) {
                        log.warn("Failed to parse JSON array from LLM response, attempt {}/{}", retry, MAX_RETRIES);
                        if (retry < MAX_RETRIES) {
                            continue;
                        }
                        break;
                    }

                    for (int i = 0; i < jsonArray.size(); i++) {
                        JSONObject obj = jsonArray.getJSONObject(i);
                        BloggerRawSentiment sentiment = new BloggerRawSentiment();
                        sentiment.setTicker(getStringValue(obj, "ticker", "").toUpperCase());
                        sentiment.setBlogger(getStringValue(obj, "blogger", msg.getUser()));
                        sentiment.setSentimentScore(getIntValue(obj, "sentiment_score", 0));
                        sentiment.setHorizon(getStringValue(obj, "horizon", null));
                        sentiment.setStrategy(getStringValue(obj, "strategy", ""));
                        sentiment.setRawContent(msg.getContent());
                        sentiment.setChannelId(msg.getChannelId());
                        sentiment.setChannelName(msg.getChannelName());

                        if (msg.getTimestamp() != null) {
                            sentiment.setMessageTime(msg.getTimestamp().toLocalDateTime());
                            sentiment.setDate(msg.getTimestamp().toLocalDateTime().toLocalDate().toString());
                        }

                        allSentiments.add(sentiment);
                        log.info("Parsed sentiment from multimodal: {} - {} - {}", sentiment.getTicker(), sentiment.getBlogger(), sentiment.getSentimentScore());
                    }

                    break;

                } catch (Exception e) {
                    log.error("Error in multimodal parsing, attempt {}/{}: {}", retry, MAX_RETRIES, e.getMessage());
                    if (retry >= MAX_RETRIES) {
                        break;
                    }
                }
            }
        }

        return allSentiments;
    }

    private static List<BloggerRawSentiment> parseTextOnly(List<MessageWithImages> messagesWithImages) {
        List<BloggerRawSentiment> allSentiments = new ArrayList<>();

        StringBuilder postsContent = new StringBuilder();
        for (MessageWithImages mwi : messagesWithImages) {
            DcChannelMessage msg = mwi.message;
            postsContent.append("---\n");
            postsContent.append("时间: ").append(msg.getTimestamp()).append("\n");
            postsContent.append("频道: ").append(msg.getChannelName()).append("\n");
            postsContent.append("频道ID: ").append(msg.getChannelId()).append("\n");
            postsContent.append("user: ").append(msg.getUser()).append("\n");
            postsContent.append("内容: ").append(mwi.contentWithoutImages).append("\n");
        }

        String prompt = getPromptTemplate();
        String userMessage = prompt.replace("[在此处插入原始帖子内容]", postsContent.toString());

        for (int retry = 1; retry <= MAX_RETRIES; retry++) {
            try {
                log.info("Calling LLM API to parse raw posts (text only), attempt {}/{}", retry, MAX_RETRIES);

                JSONObject response = YunwuApiUtils.callYunwuApiWithModel(userMessage, SPECIFIED_MODEL);
                String assistantResponse = YunwuApiUtils.getAssistantResponse(response);

                if (assistantResponse == null || assistantResponse.isEmpty()) {
                    log.warn("Empty response from LLM API, attempt {}/{}", retry, MAX_RETRIES);
                    if (retry < MAX_RETRIES) {
                        continue;
                    }
                    return allSentiments;
                }

                log.info("LLM API response: {}", assistantResponse);

                JSONArray jsonArray = parseJsonArray(assistantResponse);
                if (jsonArray == null) {
                    log.warn("Failed to parse JSON array from LLM response, attempt {}/{}", retry, MAX_RETRIES);
                    if (retry < MAX_RETRIES) {
                        continue;
                    }
                    return allSentiments;
                }

                for (int i = 0; i < jsonArray.size(); i++) {
                    JSONObject obj = jsonArray.getJSONObject(i);
                    String blogger = getStringValue(obj, "blogger", "");

                    DcChannelMessage matchedMsg = findMatchingMessage(messagesWithImages, blogger);
                    if (matchedMsg == null) {
                        log.warn("Cannot find matching message for blogger: {}", blogger);
                        continue;
                    }

                    BloggerRawSentiment sentiment = new BloggerRawSentiment();
                    sentiment.setTicker(getStringValue(obj, "ticker", "").toUpperCase());
                    sentiment.setBlogger(blogger);
                    sentiment.setSentimentScore(getIntValue(obj, "sentiment_score", 0));
                    sentiment.setHorizon(getStringValue(obj, "horizon", null));
                    sentiment.setStrategy(getStringValue(obj, "strategy", ""));
                    sentiment.setRawContent(matchedMsg.getContent());
                    sentiment.setChannelId(matchedMsg.getChannelId());
                    sentiment.setChannelName(matchedMsg.getChannelName());

                    if (matchedMsg.getTimestamp() != null) {
                        sentiment.setMessageTime(matchedMsg.getTimestamp().toLocalDateTime());
                        sentiment.setDate(matchedMsg.getTimestamp().toLocalDateTime().toLocalDate().toString());
                    }

                    allSentiments.add(sentiment);
                }

                log.info("Parsed {} sentiments from raw posts (text only)", allSentiments.size());
                return allSentiments;

            } catch (Exception e) {
                log.error("Error parsing raw posts, attempt {}/{}: {}", retry, MAX_RETRIES, e.getMessage());
                if (retry >= MAX_RETRIES) {
                    return allSentiments;
                }
            }
        }

        return allSentiments;
    }

    private static List<String> extractImageUrls(String content) {
        List<String> imageUrls = new ArrayList<>();
        if (content == null || content.isEmpty()) {
            return imageUrls;
        }

        try {
            java.util.regex.Pattern pattern = java.util.regex.Pattern.compile(IMAGE_URL_PATTERN, java.util.regex.Pattern.CASE_INSENSITIVE);
            Matcher matcher = pattern.matcher(content);
            while (matcher.find()) {
                imageUrls.add(matcher.group());
            }
        } catch (Exception e) {
            log.error("Error extracting image URLs: {}", e.getMessage());
        }

        return imageUrls;
    }

    private static String removeImageUrls(String content) {
        if (content == null || content.isEmpty()) {
            return content;
        }
        try {
            java.util.regex.Pattern pattern = java.util.regex.Pattern.compile("\\[图片：\\s*" + IMAGE_URL_PATTERN + "\\]", java.util.regex.Pattern.CASE_INSENSITIVE);
            return pattern.matcher(content).replaceAll("");
        } catch (Exception e) {
            log.error("Error removing image URLs: {}", e.getMessage());
            return content;
        }
    }

    private static DcChannelMessage findMatchingMessage(List<MessageWithImages> messages, String blogger) {
        for (MessageWithImages mwi : messages) {
            if (mwi.message.getUser() != null && mwi.message.getUser().equals(blogger)) {
                return mwi.message;
            }
        }
        return null;
    }

    private static JSONArray parseJsonArray(String response) {
        if (response == null || response.isEmpty()) {
            return null;
        }

        try {
            return JSONArray.parseArray(response);
        } catch (Exception e) {
            log.warn("Response is not a valid JSON array: {}", e.getMessage());
            return null;
        }
    }

    private static String getStringValue(JSONObject obj, String key, String defaultValue) {
        if (obj == null || !obj.containsKey(key)) {
            return defaultValue;
        }
        Object value = obj.get(key);
        return value != null ? value.toString() : defaultValue;
    }

    private static Integer getIntValue(JSONObject obj, String key, Integer defaultValue) {
        if (obj == null || !obj.containsKey(key)) {
            return defaultValue;
        }
        Object value = obj.get(key);
        if (value == null) {
            return defaultValue;
        }
        if (value instanceof Number) {
            return ((Number) value).intValue();
        }
        try {
            return Integer.parseInt(value.toString());
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    private static class MessageWithImages {
        DcChannelMessage message;
        List<String> imageUrls;
        String contentWithoutImages;
    }
}
