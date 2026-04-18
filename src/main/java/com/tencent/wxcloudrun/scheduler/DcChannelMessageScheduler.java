package com.tencent.wxcloudrun.scheduler;

import com.tencent.wxcloudrun.model.DcChannelMessage;
import com.tencent.wxcloudrun.service.DcChannelMessageService;
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

                for (DcChannelMessage message : messages) {
                    log.info("Message - channelId: {}, channelName: {}, user: {}, timestamp: {}, content: {}",
                            message.getChannelId(),
                            message.getChannelName(),
                            message.getUser(),
                            message.getTimestamp(),
                            message.getContent());
                }
            }

            log.info("Scheduled task completed successfully");
        } catch (Exception e) {
            log.error("Error in scheduled task: {}", e.getMessage(), e);
        }
    }
}
