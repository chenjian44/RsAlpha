package com.tencent.wxcloudrun.controller;

import com.tencent.wxcloudrun.config.ApiResponse;
import com.tencent.wxcloudrun.dto.DcChannelMessageRequest;
import com.tencent.wxcloudrun.service.DcChannelMessageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class DcChannelMessageController {

    private static final Logger log = LoggerFactory.getLogger(DcChannelMessageController.class);
    private final DcChannelMessageService dcChannelMessageService;

    @Autowired
    public DcChannelMessageController(DcChannelMessageService dcChannelMessageService) {
        this.dcChannelMessageService = dcChannelMessageService;
    }

    @PostMapping("/api/dc-channel-message")
    public ApiResponse receiveMessage(@RequestBody DcChannelMessageRequest request) {
        try {
            log.info("Saving message to database, channelId: {}, channelName: {}, user: {}",
                    request.getChannelId(), request.getChannelName(), request.getUser());
            dcChannelMessageService.saveMessage(request);
            log.info("Message saved successfully");
            return ApiResponse.ok();
        } catch (Exception e) {
            log.error("Failed to save message: {}", e.getMessage());
            return ApiResponse.error("保存消息失败: " + e.getMessage());
        }
    }
}
