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
        String model = YunwuConfig.getModel();
        return callYunwuApiWithModel(userMessage, model);
    }

    public static JSONObject callYunwuApiWithModel(String userMessage, String model) {
        try {
            String apiKey = YunwuConfig.getApiKey();
            String apiUrl = YunwuConfig.getApiUrl();
            double temperature = YunwuConfig.getTemperature();

            String maskedApiKey = apiKey.substring(0, 5) + "****" + apiKey.substring(apiKey.length() - 5);
            log.info("API Key: {}", maskedApiKey);
            log.info("API URL: {}", apiUrl);
            log.info("Model: {}", model);
            log.info("Temperature: {}", temperature);

            JSONObject requestBody = new JSONObject();
            requestBody.put("model", model);

            JSONArray messages = new JSONArray();

            JSONObject userMessageObj = new JSONObject();
            userMessageObj.put("role", "user");
            userMessageObj.put("content", userMessage);
            messages.add(userMessageObj);

            requestBody.put("messages", messages);
            requestBody.put("temperature", temperature);

            String requestBodyStr = requestBody.toJSONString();
            log.info("Request body: {}", requestBodyStr);

            URL url = new URL(apiUrl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();

            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setRequestProperty("Authorization", "Bearer " + apiKey);
            conn.setRequestProperty("Content-Length", String.valueOf(requestBodyStr.getBytes(StandardCharsets.UTF_8).length));
            conn.setDoOutput(true);
            conn.setDoInput(true);
            conn.setUseCaches(false);
            conn.setInstanceFollowRedirects(false);

            try (OutputStream os = conn.getOutputStream()) {
                os.write(requestBodyStr.getBytes(StandardCharsets.UTF_8));
                os.flush();
            }

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
