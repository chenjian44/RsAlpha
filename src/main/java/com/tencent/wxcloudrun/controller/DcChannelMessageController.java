package com.tencent.wxcloudrun.controller;

import com.tencent.wxcloudrun.config.ApiResponse;
import com.tencent.wxcloudrun.dto.DcChannelMessageRequest;
import com.tencent.wxcloudrun.scheduler.DcChannelMessageScheduler;
import com.tencent.wxcloudrun.service.DcChannelMessageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

@RestController
public class DcChannelMessageController {

    private static final Logger log = LoggerFactory.getLogger(DcChannelMessageController.class);
    private final DcChannelMessageService dcChannelMessageService;
    private final DcChannelMessageScheduler dcChannelMessageScheduler;

    @Autowired
    public DcChannelMessageController(DcChannelMessageService dcChannelMessageService,
                                      DcChannelMessageScheduler dcChannelMessageScheduler) {
        this.dcChannelMessageService = dcChannelMessageService;
        this.dcChannelMessageScheduler = dcChannelMessageScheduler;
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

    @PostMapping("/api/dc-channel-message/trigger")
    public ApiResponse triggerProcessChannelMessages(
            @RequestParam(required = false) String beginDate,
            @RequestParam(required = false) String endDate) {
        try {
            log.info("Manually triggering processChannelMessages task with beginDate: {}, endDate: {}", beginDate, endDate);
            
            Timestamp beginTime;
            Timestamp endTime;
            
            if (beginDate != null && !beginDate.isEmpty()) {
                LocalDate beginLocalDate = LocalDate.parse(beginDate, DateTimeFormatter.ISO_DATE);
                beginTime = Timestamp.valueOf(beginLocalDate.atStartOfDay());
            } else {
                LocalDate yesterday = LocalDate.now().minusDays(1);
                beginTime = Timestamp.valueOf(yesterday.atStartOfDay());
            }
            
            if (endDate != null && !endDate.isEmpty()) {
                LocalDate endLocalDate = LocalDate.parse(endDate, DateTimeFormatter.ISO_DATE);
                endTime = Timestamp.valueOf(endLocalDate.atTime(LocalTime.MAX));
            } else {
                endTime = Timestamp.valueOf(LocalDateTime.now());
            }
            
            log.info("Time range - beginTime: {}, endTime: {}", beginTime, endTime);
            
            dcChannelMessageScheduler.processChannelMessages(beginTime, endTime);
            log.info("processChannelMessages task triggered successfully");
            return ApiResponse.ok();
        } catch (Exception e) {
            log.error("Failed to trigger processChannelMessages task: {}", e.getMessage(), e);
            return ApiResponse.error("触发任务失败: " + e.getMessage());
        }
    }
}
