package com.tencent.wxcloudrun.scheduler;

import com.alibaba.fastjson.JSONObject;
import com.tencent.wxcloudrun.model.DailySummary;
import com.tencent.wxcloudrun.model.DcChannelMessage;
import com.tencent.wxcloudrun.service.DailySummaryService;
import com.tencent.wxcloudrun.service.DcChannelMessageService;
import com.tencent.wxcloudrun.utils.FeishuUtils;
import com.tencent.wxcloudrun.utils.PromptUtils;
import com.tencent.wxcloudrun.utils.YunwuApiUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Component
public class DcChannelMessageScheduler {

    private static final Logger log = LoggerFactory.getLogger(DcChannelMessageScheduler.class);

    private final DcChannelMessageService dcChannelMessageService;
    private final DailySummaryService dailySummaryService;

    @Autowired
    public DcChannelMessageScheduler(DcChannelMessageService dcChannelMessageService, DailySummaryService dailySummaryService) {
        this.dcChannelMessageService = dcChannelMessageService;
        this.dailySummaryService = dailySummaryService;
    }

    @Scheduled(cron = "0 10 12 * * ?", zone = "Asia/Shanghai")
    public void scheduledProcessChannelMessages() {
        log.info("Starting scheduled task: processChannelMessages at 11:40 北京时间");

        LocalDate today = LocalDate.now();
        LocalDate yesterday = today.minusDays(1);

        Timestamp beginTime = Timestamp.valueOf(yesterday.atStartOfDay());
        Timestamp endTime = Timestamp.valueOf(LocalDateTime.now());

        processChannelMessages(beginTime, endTime);
    }

    public void processChannelMessages(Timestamp beginTime, Timestamp endTime) {
        processChannelMessages(beginTime, endTime, true);
    }

    public void processChannelMessages(Timestamp beginTime, Timestamp endTime, boolean saveToDatabase) {
        try {
            log.info("Query time range - begin: {}, end: {}", beginTime, endTime);

            List<String> channelIds = dcChannelMessageService.getAllChannelIdsByTimeRange(beginTime, endTime);
            log.info("Found {} distinct channelIds in time range", channelIds.size());

            StringBuilder allSummaries = new StringBuilder();
            LocalDate today = LocalDate.now();
            String dateStr = today.format(DateTimeFormatter.ISO_DATE);
            StringBuilder messagesContent = new StringBuilder();

            for (String channelId : channelIds) {
                log.info("Processing channelId: {}", channelId);
                List<DcChannelMessage> messages = dcChannelMessageService.getMessagesByChannelIdAndTimeRange(channelId, beginTime, endTime);
                log.info("Retrieved {} messages for channelId: {}", messages.size(), channelId);

                if (messages.isEmpty()) {
                    continue;
                }

                messagesContent.append("以下是该频道的消息记录（按时间升序排列）：\n\n");

                for (DcChannelMessage message : messages) {
                    log.info("Message - channelId: {}, channelName: {}, user: {}, timestamp: {}, content: {}",
                            message.getChannelId(),
                            message.getChannelName(),
                            message.getUser(),
                            message.getTimestamp(),
                            message.getContent());

                    messagesContent.append("时间: ").append(message.getTimestamp()).append("\n");
                    messagesContent.append("频道: ").append(message.getChannelName()).append("\n");
                    messagesContent.append("内容: ").append(message.getContent()).append("\n\n");
                }

            }

            if (messagesContent.length() == 0) {
                log.info("No messages found in time range, skip processing");
                return;
            }

            String prompt = PromptUtils.readSystemPrompt();
            String userMessage = prompt +"<Post>\n" + messagesContent + "\n</Post>";

            JSONObject response = YunwuApiUtils.callYunwuApi(userMessage);
            String assistantResponse = YunwuApiUtils.getAssistantResponse(response);

            if (!assistantResponse.isEmpty()) {
                String title = String.format("%s 频道分析报告", today);
                boolean pushSuccess = FeishuUtils.sendRichTextMessage(title, assistantResponse);
                if (pushSuccess) {
                    log.info("Feishu push successful");
                } else {
                    log.error("Feishu push failed for : {}", title);
                }

                allSummaries.append(today).append(" 频道分析\n").append(assistantResponse);
            }

            if (saveToDatabase && allSummaries.length() > 0) {
                DailySummary dailySummary = new DailySummary();
                dailySummary.setDate(dateStr);
                dailySummary.setContent(allSummaries.toString());

                dailySummaryService.saveSummary(dailySummary);
                log.info("Daily summary saved successfully");
            }
            log.info("Scheduled task completed successfully");
        } catch (Exception e) {
            log.error("Error in scheduled task: {}", e.getMessage(), e);
        }
    }
}
