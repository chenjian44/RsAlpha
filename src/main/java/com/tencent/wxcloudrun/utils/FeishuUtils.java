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

            // 构建飞书消息格式 - 修正text字段格式
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

            // 构建飞书富文本消息格式 - 修正post字段格式
            JSONObject messageBody = new JSONObject();
            messageBody.put("msg_type", "post");
            
            JSONObject contentObj = new JSONObject();
            JSONObject postObj = new JSONObject();
            JSONObject zhCnObj = new JSONObject();
            
            zhCnObj.put("title", title != null ? title : "");
            
            // 构建富文本内容
            JSONArray contentArray = new JSONArray();
            JSONArray lineArray = new JSONArray();
            
            JSONObject textObj = new JSONObject();
            textObj.put("tag", "text");
            textObj.put("text", content);
            
            lineArray.add(textObj);
            contentArray.add(lineArray);
            
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
