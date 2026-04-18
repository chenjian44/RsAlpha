package com.tencent.wxcloudrun.scheduler;

import com.alibaba.fastjson.JSONObject;
import com.tencent.wxcloudrun.model.DcChannelMessage;
import com.tencent.wxcloudrun.service.DcChannelMessageService;
import com.tencent.wxcloudrun.utils.YunwuApiUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class DcChannelMessageScheduler {

    private static final Logger log = LoggerFactory.getLogger(DcChannelMessageScheduler.class);

    private final DcChannelMessageService dcChannelMessageService;

    @Autowired
    public DcChannelMessageScheduler(DcChannelMessageService dcChannelMessageService) {
        this.dcChannelMessageService = dcChannelMessageService;
    }

    @Scheduled(cron = "0 0 10 * * ?")
    public void scheduledProcessChannelMessages() {
        log.info("Starting scheduled task: processChannelMessages at 10:00 AM");
        processChannelMessages();
    }

    public void processChannelMessages() {
        try {
            List<String> channelIds = dcChannelMessageService.getAllChannelIds();
            log.info("Found {} distinct channelIds", channelIds.size());

            for (String channelId : channelIds) {
                log.info("Processing channelId: {}", channelId);
                List<DcChannelMessage> messages = dcChannelMessageService.getMessagesByChannelId(channelId);
                log.info("Retrieved {} messages for channelId: {}", messages.size(), channelId);

                // 整合消息内容
                StringBuilder messagesContent = new StringBuilder();
                messagesContent.append("以下是该频道的消息记录（按时间升序排列）：\n\n");
                
                for (DcChannelMessage message : messages) {
                    log.info("Message - channelId: {}, channelName: {}, user: {}, timestamp: {}, content: {}",
                            message.getChannelId(),
                            message.getChannelName(),
                            message.getUser(),
                            message.getTimestamp(),
                            message.getContent());
                    
                    messagesContent.append("时间: ").append(message.getTimestamp()).append("\n");
                    messagesContent.append("用户: ").append(message.getUser()).append("\n");
                    messagesContent.append("内容: ").append(message.getContent()).append("\n\n");
                }

                // 调用LLM接口进行分析
                if (!messages.isEmpty()) {
                    String userMessage = "请分析以下频道的消息记录，提供详细的分析报告，包括：\n"
                            + "1. 消息的主题和主要内容\n"
                            + "2. 参与者的互动模式\n"
                            + "3. 关键信息和趋势\n"
                            + "4. 可能的见解和建议\n\n"
                            + messagesContent.toString();
                    
                    log.info("Calling LLM API for channelId: {}", channelId);
                    JSONObject response = YunwuApiUtils.callYunwuApi(userMessage);
                    String assistantResponse = YunwuApiUtils.getAssistantResponse(response);
                    
                    log.info("LLM analysis result for channelId {}: {}", channelId, assistantResponse);
                }
            }

            log.info("Scheduled task completed successfully");
        } catch (Exception e) {
            log.error("Error in scheduled task: {}", e.getMessage(), e);
        }
    }
}
