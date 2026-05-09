package com.tencent.wxcloudrun.controller;

import com.tencent.wxcloudrun.config.ApiResponse;
import com.tencent.wxcloudrun.dto.DcChannelMessageBatchRequest;
import com.tencent.wxcloudrun.dto.DcChannelMessageRequest;
import com.tencent.wxcloudrun.model.DcChannelMessage;
import com.tencent.wxcloudrun.scheduler.BloggerRawPostScheduler;
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
import java.time.format.DateTimeFormatter;
import java.util.List;

@RestController
public class DcChannelMessageController {

    private static final Logger log = LoggerFactory.getLogger(DcChannelMessageController.class);
    private final DcChannelMessageService dcChannelMessageService;
    private final DcChannelMessageScheduler dcChannelMessageScheduler;
    private final BloggerRawPostScheduler bloggerRawPostScheduler;

    @Autowired
    public DcChannelMessageController(DcChannelMessageService dcChannelMessageService,
                                      DcChannelMessageScheduler dcChannelMessageScheduler,
                                      BloggerRawPostScheduler bloggerRawPostScheduler) {
        this.dcChannelMessageService = dcChannelMessageService;
        this.dcChannelMessageScheduler = dcChannelMessageScheduler;
        this.bloggerRawPostScheduler = bloggerRawPostScheduler;
    }

    @PostMapping("/api/dc-channel-message")
    public ApiResponse receiveMessage(@RequestBody DcChannelMessageRequest request) {
        try {
            log.info("Saving message to database, channelId: {}, channelName: {}, user: {}, content:{}",
                    request.getChannelId(), request.getChannelName(), request.getUser(),request.getContent());
            dcChannelMessageService.saveMessage(request);
            log.info("Message saved successfully");
            return ApiResponse.ok();
        } catch (Exception e) {
            log.error("Failed to save message: {}", e.getMessage());
            return ApiResponse.error("保存消息失败: " + e.getMessage());
        }
    }

    @PostMapping("/api/dc-channel-message/batch")
    public ApiResponse receiveBatchMessage(@RequestBody DcChannelMessageBatchRequest request) {
        try {
            if (request.getMessages() == null || request.getMessages().isEmpty()) {
                return ApiResponse.error("消息列表为空");
            }

            int successCount = 0;
            int failCount = 0;

            for (DcChannelMessageRequest message : request.getMessages()) {
                try {
                    log.info("Saving message to database, channelId: {}, channelName: {}, user: {}",
                            message.getChannelId(), message.getChannelName(), message.getUser());
                    dcChannelMessageService.saveMessage(message);
                    successCount++;
                } catch (Exception e) {
                    failCount++;
                    log.error("Failed to save message: {}", e.getMessage());
                }
            }

            log.info("Batch message processing completed. Success: {}, Failed: {}", successCount, failCount);
            return ApiResponse.ok("批量处理完成: 成功 {0} 条, 失败 {1} 条".replace("{0}", String.valueOf(successCount)).replace("{1}", String.valueOf(failCount)));
        } catch (Exception e) {
            log.error("Failed to process batch messages: {}", e.getMessage());
            return ApiResponse.error("批量处理失败: " + e.getMessage());
        }
    }

    @PostMapping("/api/dc-channel-message/trigger")
    public ApiResponse triggerProcessChannelMessages() {
        try {
            LocalDate startDate = LocalDate.of(2026, 1, 1);
            LocalDate endDate = LocalDate.of(2026, 1, 1);

            log.info("Manually triggering processChannelMessages task for backfill, from {} to {}", startDate, endDate);

            int successCount = 0;
            int failCount = 0;

            for (LocalDate date = startDate; !date.isAfter(endDate); date = date.plusDays(1)) {
                LocalDate nextDate = date.plusDays(1);

                Timestamp beginTime = Timestamp.valueOf(date.atStartOfDay());
                Timestamp endTime = Timestamp.valueOf(nextDate.atStartOfDay());

                log.info("Processing date: {}, time range: {} to {}", date, beginTime, endTime);

                try {
                    dcChannelMessageScheduler.processChannelMessages(beginTime, endTime);
                    successCount++;
                } catch (Exception e) {
                    failCount++;
                    log.error("Failed to process date: {}, error: {}", date, e.getMessage());
                }
            }

            log.info("Backfill completed. Success: {}, Failed: {}", successCount, failCount);
            return ApiResponse.ok("回跑完成: 成功 {0} 天, 失败 {1} 天".replace("{0}", String.valueOf(successCount)).replace("{1}", String.valueOf(failCount)));
        } catch (Exception e) {
            log.error("Failed to trigger processChannelMessages task: {}", e.getMessage(), e);
            return ApiResponse.error("触发任务失败: " + e.getMessage());
        }
    }

    @PostMapping("/api/dc-channel-message/trigger-raw-post")
    public ApiResponse triggerRawPostProcessing(@RequestParam(required = false) Integer days) {
        try {
            int daysToProcess = days != null && days > 0 ? days : 120;

            LocalDate today = LocalDate.now();
            int successCount = 0;
            int failCount = 0;

            log.info("Manually triggering raw post processing for {} days, from {} backwards", 
                    daysToProcess, today);

            for (int i = 0; i < daysToProcess; i++) {
                LocalDate currentDate = today.minusDays(i);
                LocalDate nextDate = currentDate.plusDays(1);

                Timestamp beginTime = Timestamp.valueOf(currentDate.atStartOfDay());
                Timestamp endTime = Timestamp.valueOf(nextDate.atStartOfDay());

                log.info("Processing day {} of {}: {}", i + 1, daysToProcess, currentDate);

                try {
                    bloggerRawPostScheduler.processRawPostsByBlogger(beginTime, endTime);
                    successCount++;
                } catch (Exception e) {
                    failCount++;
                    log.error("Failed to process date: {}, error: {}", currentDate, e.getMessage());
                }
            }

            log.info("Raw post processing completed. Success: {}, Failed: {}", successCount, failCount);
            return ApiResponse.ok("原始帖子解析任务触发完成: 成功 {0} 天, 失败 {1} 天"
                    .replace("{0}", String.valueOf(successCount))
                    .replace("{1}", String.valueOf(failCount)));
        } catch (Exception e) {
            log.error("Failed to trigger raw post processing: {}", e.getMessage(), e);
            return ApiResponse.error("触发任务失败: " + e.getMessage());
        }
    }

    @PostMapping("/api/dc-channel-message/trigger-raw-post-by-date")
    public ApiResponse triggerRawPostProcessingByDate(
            @RequestParam String startDate,
            @RequestParam String endDate) {
        try {
            log.info("Manually triggering raw post processing by date, from {} to {}", startDate, endDate);

            LocalDate start = LocalDate.parse(startDate);
            LocalDate end = LocalDate.parse(endDate);

            bloggerRawPostScheduler.processRawPostsForDateRange(start, end);

            log.info("Raw post processing by date triggered successfully");
            return ApiResponse.ok("原始帖子解析任务触发成功，日期范围: {0} 至 {1}".replace("{0}", startDate).replace("{1}", endDate));
        } catch (Exception e) {
            log.error("Failed to trigger raw post processing by date: {}", e.getMessage(), e);
            return ApiResponse.error("触发任务失败: " + e.getMessage());
        }
    }
}
