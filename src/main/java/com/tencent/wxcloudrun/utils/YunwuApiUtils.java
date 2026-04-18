package com.tencent.wxcloudrun.utils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.tencent.wxcloudrun.config.YunwuConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class YunwuApiUtils {

    private static final Logger log = LoggerFactory.getLogger(YunwuApiUtils.class);

    public static JSONObject callYunwuApi(String userMessage) {
        try {
            // 获取配置
            String apiKey = YunwuConfig.getApiKey();
            String apiUrl = YunwuConfig.getApiUrl();
            String model = YunwuConfig.getModel();
            double temperature = YunwuConfig.getTemperature();
            
            // 打印配置信息（隐藏部分API密钥）
            String maskedApiKey = apiKey.substring(0, 5) + "****" + apiKey.substring(apiKey.length() - 5);
            log.info("API Key: {}", maskedApiKey);
            log.info("API URL: {}", apiUrl);
            log.info("Model: {}", model);
            log.info("Temperature: {}", temperature);
            
            // 构建请求参数
            JSONObject requestBody = new JSONObject();
            requestBody.put("model", model);
            
            JSONArray messages = new JSONArray();
            
            // 只添加用户消息，不添加系统提示词，与curl命令保持一致
            JSONObject userMessageObj = new JSONObject();
            userMessageObj.put("role", "user");
            userMessageObj.put("content", userMessage);
            messages.add(userMessageObj);
            
            requestBody.put("messages", messages);
            requestBody.put("temperature", temperature);
            
            String requestBodyStr = requestBody.toJSONString();
            log.info("Request body: {}", requestBodyStr);
            
            // 发送HTTP请求
            URL url = new URL(apiUrl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            
            // 设置请求属性
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setRequestProperty("Authorization", "Bearer " + apiKey);
            conn.setRequestProperty("Content-Length", String.valueOf(requestBodyStr.getBytes(StandardCharsets.UTF_8).length));
            conn.setDoOutput(true);
            conn.setDoInput(true);
            conn.setUseCaches(false);
            conn.setInstanceFollowRedirects(false);
            
            // 写入请求体
            try (OutputStream os = conn.getOutputStream()) {
                os.write(requestBodyStr.getBytes(StandardCharsets.UTF_8));
                os.flush();
            }
            
            // 读取响应
            int responseCode = conn.getResponseCode();
            log.info("Response code: {}", responseCode);
            
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
            log.info("Response: {}", responseStr);
            
            // 解析响应
            return JSON.parseObject(responseStr);
        } catch (Exception e) {
            log.error("Error calling Yunwu API: {}", e.getMessage(), e);
            JSONObject errorResponse = new JSONObject();
            errorResponse.put("error", e.getMessage());
            return errorResponse;
        }
    }

    public static String getAssistantResponse(JSONObject response) {
        if (response.containsKey("choices")) {
            JSONArray choices = response.getJSONArray("choices");
            if (!choices.isEmpty()) {
                JSONObject choice = choices.getJSONObject(0);
                if (choice.containsKey("message")) {
                    JSONObject message = choice.getJSONObject("message");
                    if (message.containsKey("content")) {
                        return message.getString("content");
                    }
                }
            }
        }
        if (response.containsKey("error")) {
            log.error("Yunwu API error: {}", response.getString("error"));
        }
        return "";
    }
}
