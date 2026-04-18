package com.tencent.wxcloudrun.controller;

import com.tencent.wxcloudrun.config.ApiResponse;
import com.tencent.wxcloudrun.dto.DcChannelMessageRequest;
import com.tencent.wxcloudrun.service.DcChannelMessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class DcChannelMessageController {

    private final DcChannelMessageService dcChannelMessageService;

    @Autowired
    public DcChannelMessageController(DcChannelMessageService dcChannelMessageService) {
        this.dcChannelMessageService = dcChannelMessageService;
    }

    @PostMapping("/api/dc-channel-message")
    public ApiResponse receiveMessage(@RequestBody DcChannelMessageRequest request) {
        try {
            dcChannelMessageService.saveMessage(request);
            return ApiResponse.ok();
        } catch (Exception e) {
            return ApiResponse.error("保存消息失败: " + e.getMessage());
        }
    }
}
