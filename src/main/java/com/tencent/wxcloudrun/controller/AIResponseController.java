package com.tencent.wxcloudrun.controller;

import com.alibaba.fastjson.JSONObject;
import com.tencent.wxcloudrun.config.ApiResponse;
import com.tencent.wxcloudrun.utils.YunwuApiUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AIResponseController {

    private static final Logger log = LoggerFactory.getLogger(AIResponseController.class);

    @PostMapping("/api/ai/chat")
    public ApiResponse chatWithAI(@RequestBody JSONObject request) {
        try {
            String userMessage = request.getString("message");
            log.info("Received AI chat request: {}", userMessage);
            
            JSONObject response = YunwuApiUtils.callYunwuApi(userMessage);
            log.info("Yunwu API response: {}", response);
            
            if (response.containsKey("error")) {
                return ApiResponse.error("AI API error: " + response.getString("error"));
            }
            
            String assistantResponse = YunwuApiUtils.getAssistantResponse(response);
            if (assistantResponse.isEmpty()) {
                return ApiResponse.error("No response from AI");
            }
            
            JSONObject result = new JSONObject();
            result.put("response", assistantResponse);
            return ApiResponse.ok(result);
        } catch (Exception e) {
            log.error("Error in AI chat: {}", e.getMessage());
            return ApiResponse.error("Failed to chat with AI: " + e.getMessage());
        }
    }
}
