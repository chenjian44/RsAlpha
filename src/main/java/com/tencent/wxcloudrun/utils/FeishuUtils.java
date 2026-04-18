package com.tencent.wxcloudrun.utils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.tencent.wxcloudrun.config.FeishuConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class FeishuUtils {

    private static final Logger log = LoggerFactory.getLogger(FeishuUtils.class);

    public static boolean sendMessage(String content) {
        if (!FeishuConfig.isEnabled()) {
            log.warn("Feishu push is disabled");
            return false;
        }

        try {
            String webhookUrl = FeishuConfig.getWebhookUrl();
            if (webhookUrl.isEmpty()) {
                log.error("Feishu webhook URL is not configured");
                return false;
            }

            if (content == null || content.isEmpty()) {
                log.error("Message content is empty");
                return false;
            }

            // 构建飞书消息格式
            JSONObject messageBody = new JSONObject();
            messageBody.put("msg_type", "text");
            
            JSONObject contentObj = new JSONObject();
            contentObj.put("text", content);
            messageBody.put("content", contentObj);

            String requestBody = messageBody.toJSONString();
            log.info("Sending Feishu message: {}", requestBody);

            // 发送HTTP请求
            URL url = new URL(webhookUrl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setDoOutput(true);
            conn.setDoInput(true);
            conn.setUseCaches(false);
            
            // 写入请求体
            try (OutputStream os = conn.getOutputStream()) {
                os.write(requestBody.getBytes(StandardCharsets.UTF_8));
                os.flush();
            }
            
            // 读取响应
            int responseCode = conn.getResponseCode();
            log.info("Feishu API response code: {}", responseCode);
            
            BufferedReader reader = new BufferedReader(new InputStreamReader(
                    responseCode == 200 ? conn.getInputStream() : conn.getErrorStream(), StandardCharsets.UTF_8));
            
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
            reader.close();
            conn.disconnect();
            
            String responseStr = response.toString();
            log.info("Feishu API response: {}", responseStr);
            
            // 解析响应
            JSONObject responseJson = JSON.parseObject(responseStr);
            if (responseJson.containsKey("code") && responseJson.getInteger("code") == 0) {
                log.info("Feishu message sent successfully");
                return true;
            } else {
                log.error("Feishu message send failed: {}", responseStr);
                return false;
            }
        } catch (Exception e) {
            log.error("Error sending Feishu message: {}", e.getMessage(), e);
            return false;
        }
    }

    public static boolean sendRichTextMessage(String title, String content) {
        if (!FeishuConfig.isEnabled()) {
            log.warn("Feishu push is disabled");
            return false;
        }

        try {
            String webhookUrl = FeishuConfig.getWebhookUrl();
            if (webhookUrl.isEmpty()) {
                log.error("Feishu webhook URL is not configured");
                return false;
            }

            if (content == null || content.isEmpty()) {
                log.error("Message content is empty");
                return false;
            }

            // 构建飞书富文本消息格式
            JSONObject messageBody = new JSONObject();
            messageBody.put("msg_type", "post");
            
            JSONObject contentObj = new JSONObject();
            JSONObject postObj = new JSONObject();
            JSONObject zhCnObj = new JSONObject();
            
            // 设置标题
            zhCnObj.put("title", title);
            
            // 构建富文本内容
            JSONArray contentArray = new JSONArray();
            
            // 分割内容为行并处理
            String[] lines = content.split("\n");
            for (String line : lines) {
                JSONArray lineArray = new JSONArray();
                
                line = line.trim();
                if (line.isEmpty()) {
                    // 空行
                    JSONObject emptyText = new JSONObject();
                    emptyText.put("tag", "text");
                    emptyText.put("text", " ");
                    lineArray.add(emptyText);
                } else if (line.startsWith("### ")) {
                    // 三级标题
                    JSONObject titleText = new JSONObject();
                    titleText.put("tag", "text");
                    titleText.put("text", "  " + line.substring(4));
                    titleText.put("bold", true);
                    titleText.put("text_size", 16);
                    titleText.put("text_color", "blue");
                    lineArray.add(titleText);
                } else if (line.startsWith("## ")) {
                    // 二级标题
                    JSONObject titleText = new JSONObject();
                    titleText.put("tag", "text");
                    titleText.put("text", "  " + line.substring(3));
                    titleText.put("bold", true);
                    titleText.put("text_size", 18);
                    titleText.put("text_color", "red");
                    lineArray.add(titleText);
                } else if (line.startsWith("# ")) {
                    // 一级标题
                    JSONObject titleText = new JSONObject();
                    titleText.put("tag", "text");
                    titleText.put("text", line.substring(2));
                    titleText.put("bold", true);
                    titleText.put("text_size", 20);
                    titleText.put("text_color", "red");
                    lineArray.add(titleText);
                } else if (line.startsWith("- **")) {
                    // 无序列表项（加粗）
                    int endIndex = line.indexOf("**", 4);
                    if (endIndex > 4) {
                        String boldPart = line.substring(4, endIndex);
                        String restPart = line.substring(endIndex + 2);
                        
                        JSONObject bulletText = new JSONObject();
                        bulletText.put("tag", "text");
                        bulletText.put("text", "• ");
                        lineArray.add(bulletText);
                        
                        JSONObject boldText = new JSONObject();
                        boldText.put("tag", "text");
                        boldText.put("text", boldPart);
                        boldText.put("bold", true);
                        lineArray.add(boldText);
                        
                        if (!restPart.isEmpty()) {
                            JSONObject normalText = new JSONObject();
                            normalText.put("tag", "text");
                            normalText.put("text", restPart);
                            lineArray.add(normalText);
                        }
                    }
                } else if (line.startsWith("- ")) {
                    // 无序列表项
                    JSONObject bulletText = new JSONObject();
                    bulletText.put("tag", "text");
                    bulletText.put("text", "• " + line.substring(2));
                    lineArray.add(bulletText);
                } else if (line.matches("^\\d+\\.\\s.*")) {
                    // 有序列表项
                    int dotIndex = line.indexOf(". ");
                    if (dotIndex > 0) {
                        String numberPart = line.substring(0, dotIndex + 2);
                        String restPart = line.substring(dotIndex + 2);
                        
                        JSONObject numText = new JSONObject();
                        numText.put("tag", "text");
                        numText.put("text", numberPart);
                        lineArray.add(numText);
                        
                        if (!restPart.isEmpty()) {
                            JSONObject restText = new JSONObject();
                            restText.put("tag", "text");
                            restText.put("text", restPart);
                            lineArray.add(restText);
                        }
                    }
                } else if (line.contains("**")) {
                    // 行内加粗
                    String[] parts = line.split("\\*\\*");
                    for (int i = 0; i < parts.length; i++) {
                        JSONObject textObj = new JSONObject();
                        textObj.put("tag", "text");
                        if (i % 2 == 1) {
                            // 奇数部分是加粗的
                            textObj.put("text", parts[i]);
                            textObj.put("bold", true);
                        } else {
                            textObj.put("text", parts[i]);
                        }
                        lineArray.add(textObj);
                    }
                } else {
                    // 普通文本
                    JSONObject normalText = new JSONObject();
                    normalText.put("tag", "text");
                    normalText.put("text", "  " + line);
                    lineArray.add(normalText);
                }
                
                contentArray.add(lineArray);
            }
            
            zhCnObj.put("content", contentArray);
            postObj.put("zh_cn", zhCnObj);
            contentObj.put("post", postObj);
            messageBody.put("content", contentObj);

            String requestBody = messageBody.toJSONString();
            log.info("Sending Feishu rich text message: {}", requestBody);

            // 发送HTTP请求
            URL url = new URL(webhookUrl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setDoOutput(true);
            conn.setDoInput(true);
            conn.setUseCaches(false);
            
            // 写入请求体
            try (OutputStream os = conn.getOutputStream()) {
                os.write(requestBody.getBytes(StandardCharsets.UTF_8));
                os.flush();
            }
            
            // 读取响应
            int responseCode = conn.getResponseCode();
            log.info("Feishu API response code: {}", responseCode);
            
            BufferedReader reader = new BufferedReader(new InputStreamReader(
                    responseCode == 200 ? conn.getInputStream() : conn.getErrorStream(), StandardCharsets.UTF_8));
            
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
            reader.close();
            conn.disconnect();
            
            String responseStr = response.toString();
            log.info("Feishu API response: {}", responseStr);
            
            // 解析响应
            JSONObject responseJson = JSON.parseObject(responseStr);
            if (responseJson.containsKey("code") && responseJson.getInteger("code") == 0) {
                log.info("Feishu rich text message sent successfully");
                return true;
            } else {
                log.error("Feishu rich text message send failed: {}", responseStr);
                return false;
            }
        } catch (Exception e) {
            log.error("Error sending Feishu rich text message: {}", e.getMessage(), e);
            return false;
        }
    }
}
