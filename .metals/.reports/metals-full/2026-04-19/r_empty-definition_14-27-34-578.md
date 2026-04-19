error id: file://<WORKSPACE>/src/main/java/com/tencent/wxcloudrun/controller/DcChannelMessageController.java:_empty_/ApiResponse#ok#
file://<WORKSPACE>/src/main/java/com/tencent/wxcloudrun/controller/DcChannelMessageController.java
empty definition using pc, found symbol in pc: _empty_/ApiResponse#ok#
empty definition using semanticdb
empty definition using fallback
non-local guesses:

offset: 3292
uri: file://<WORKSPACE>/src/main/java/com/tencent/wxcloudrun/controller/DcChannelMessageController.java
text:
```scala
package com.tencent.wxcloudrun.controller;

import com.tencent.wxcloudrun.config.ApiResponse;
import com.tencent.wxcloudrun.dto.DcChannelMessageBatchRequest;
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
            return ApiResponse.@@ok("批量处理完成: 成功 {0} 条, 失败 {1} 条".replace("{0}", String.valueOf(successCount)).replace("{1}", String.valueOf(failCount)));
        } catch (Exception e) {
            log.error("Failed to process batch messages: {}", e.getMessage());
            return ApiResponse.error("批量处理失败: " + e.getMessage());
        }
    }

    @PostMapping("/api/dc-channel-message/trigger")
    public ApiResponse triggerProcessChannelMessages() {
        try {
            LocalDate today = LocalDate.now();
            LocalDate oneMonthAgo = today.minusDays(30);
            LocalDate yesterday = today.minusDays(1);

            log.info("Manually triggering processChannelMessages task for backfill, from {} to {}", oneMonthAgo, yesterday);

            int successCount = 0;
            int failCount = 0;

            for (LocalDate date = oneMonthAgo; !date.isAfter(yesterday); date = date.plusDays(1)) {
                LocalDate nextDate = date.plusDays(1);

                Timestamp beginTime = Timestamp.valueOf(date.atStartOfDay());
                Timestamp endTime = Timestamp.valueOf(nextDate.atTime(LocalTime.of(9, 0)));

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
}

```


#### Short summary: 

empty definition using pc, found symbol in pc: _empty_/ApiResponse#ok#