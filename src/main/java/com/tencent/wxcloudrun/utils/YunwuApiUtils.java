package com.tencent.wxcloudrun.utils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.tencent.wxcloudrun.config.YunwuConfig;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class YunwuApiUtils {

    public static JSONObject callYunwuApi(String userMessage) {
        try {
            // 构建请求参数
            JSONObject requestBody = new JSONObject();
            requestBody.put("model", YunwuConfig.getModel());
            
            JSONArray messages = new JSONArray();
            
            // 添加系统提示词
            String systemPrompt = PromptUtils.readSystemPrompt();
            if (!systemPrompt.isEmpty()) {
                JSONObject systemMessage = new JSONObject();
                systemMessage.put("role", "system");
                systemMessage.put("content", systemPrompt);
                messages.add(systemMessage);
            }
            
            // 添加用户消息
            JSONObject userMessageObj = new JSONObject();
            userMessageObj.put("role", "user");
            userMessageObj.put("content", userMessage);
            messages.add(userMessageObj);
            
            requestBody.put("messages", messages);
            requestBody.put("temperature", YunwuConfig.getTemperature());
            
            // 发送HTTP请求
            URL url = new URL(YunwuConfig.getApiUrl());
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setRequestProperty("Authorization", "Bearer " + YunwuConfig.getApiKey());
            conn.setDoOutput(true);
            
            // 写入请求体
            try (OutputStream os = conn.getOutputStream()) {
                os.write(requestBody.toJSONString().getBytes());
                os.flush();
            }
            
            // 读取响应
            int responseCode = conn.getResponseCode();
            BufferedReader reader = new BufferedReader(new InputStreamReader(
                    responseCode == 200 ? conn.getInputStream() : conn.getErrorStream()));
            
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
            reader.close();
            conn.disconnect();
            
            // 解析响应
            return JSON.parseObject(response.toString());
        } catch (Exception e) {
            e.printStackTrace();
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
                    return message.getString("content");
                }
            }
        }
        return "";
    }
}
