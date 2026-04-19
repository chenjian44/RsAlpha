error id: file://<WORKSPACE>/src/main/java/com/tencent/wxcloudrun/utils/BloggerSentimentParser.java:_empty_/YunwuApiUtils#callYunwuApiWithModel#
file://<WORKSPACE>/src/main/java/com/tencent/wxcloudrun/utils/BloggerSentimentParser.java
empty definition using pc, found symbol in pc: _empty_/YunwuApiUtils#callYunwuApiWithModel#
empty definition using semanticdb
empty definition using fallback
non-local guesses:

offset: 2124
uri: file://<WORKSPACE>/src/main/java/com/tencent/wxcloudrun/utils/BloggerSentimentParser.java
text:
```scala
package com.tencent.wxcloudrun.utils;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.tencent.wxcloudrun.model.BloggerSentiment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class BloggerSentimentParser {

    private static final Logger log = LoggerFactory.getLogger(BloggerSentimentParser.class);
    private static final int MAX_RETRIES = 3;
    private static final String SPECIFIED_MODEL = "gemini-3.1-flash-lite-preview";

    private static String promptTemplate = null;

    private static String getPromptTemplate() {
        if (promptTemplate == null) {
            try (InputStreamReader reader = new InputStreamReader(
                    BloggerSentimentParser.class.getClassLoader().getResourceAsStream("prompts/daily_summary_elt_prompt.txt"),
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

    public static List<BloggerSentiment> parseReport(String reportContent, String date) {
        List<BloggerSentiment> sentiments = new ArrayList<>();

        if (reportContent == null || reportContent.isEmpty()) {
            log.warn("Report content is empty, skip parsing");
            return sentiments;
        }

        String prompt = getPromptTemplate();
        String userMessage = prompt.replace("[在此处插入你的每日总结 Markdown 文本]", reportContent);

        for (int retry = 1; retry <= MAX_RETRIES; retry++) {
            try {
                log.info("Calling LLM API to parse report, attempt {}/{}", retry, MAX_RETRIES);

                JSONObject response = YunwuApiUtils.@@callYunwuApiWithModel(userMessage, SPECIFIED_MODEL);
                String assistantResponse = YunwuApiUtils.getAssistantResponse(response);

                if (assistantResponse == null || assistantResponse.isEmpty()) {
                    log.warn("Empty response from LLM API, attempt {}/{}", retry, MAX_RETRIES);
                    if (retry < MAX_RETRIES) {
                        continue;
                    }
                    return sentiments;
                }

                log.info("LLM API response: {}", assistantResponse);

                JSONArray jsonArray = parseJsonArray(assistantResponse);
                if (jsonArray == null) {
                    log.warn("Failed to parse JSON array from LLM response, attempt {}/{}", retry, MAX_RETRIES);
                    if (retry < MAX_RETRIES) {
                        continue;
                    }
                    return sentiments;
                }

                for (int i = 0; i < jsonArray.size(); i++) {
                    JSONObject obj = jsonArray.getJSONObject(i);
                    BloggerSentiment sentiment = new BloggerSentiment();
                    sentiment.setDate(getStringValue(obj, "date", date));
                    sentiment.setTicker(getStringValue(obj, "ticker", "").toUpperCase());
                    sentiment.setBlogger(getStringValue(obj, "blogger", ""));
                    sentiment.setSentimentScore(getIntValue(obj, "sentiment_score", 0));
                    sentiment.setHorizon(getStringValue(obj, "horizon", null));
                    sentiment.setStrategy(getStringValue(obj, "strategy", ""));
                    sentiments.add(sentiment);
                }

                log.info("Parsed {} sentiments from report", sentiments.size());
                return sentiments;

            } catch (Exception e) {
                log.error("Error parsing report, attempt {}/{}: {}", retry, MAX_RETRIES, e.getMessage());
                if (retry >= MAX_RETRIES) {
                    return sentiments;
                }
            }
        }

        return sentiments;
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
}

```


#### Short summary: 

empty definition using pc, found symbol in pc: _empty_/YunwuApiUtils#callYunwuApiWithModel#