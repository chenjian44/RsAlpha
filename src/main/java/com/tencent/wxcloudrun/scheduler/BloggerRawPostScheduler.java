package com.tencent.wxcloudrun.scheduler;

import com.tencent.wxcloudrun.model.BloggerRawSentiment;
import com.tencent.wxcloudrun.model.DcChannelMessage;
import com.tencent.wxcloudrun.service.BloggerRawSentimentService;
import com.tencent.wxcloudrun.service.DcChannelMessageService;
import com.tencent.wxcloudrun.utils.BloggerRawPostParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Component
public class BloggerRawPostScheduler {

    private static final Logger log = LoggerFactory.getLogger(BloggerRawPostScheduler.class);

    private final DcChannelMessageService dcChannelMessageService;
    private final BloggerRawSentimentService bloggerRawSentimentService;

    @Autowired
    public BloggerRawPostScheduler(DcChannelMessageService dcChannelMessageService,
                                   BloggerRawSentimentService bloggerRawSentimentService) {
        this.dcChannelMessageService = dcChannelMessageService;
        this.bloggerRawSentimentService = bloggerRawSentimentService;
    }

    @Scheduled(cron = "0 0 9 * * ?", zone = "Asia/Shanghai")
    public void scheduledProcessRawPosts() {
        LocalDate today = LocalDate.now();
        LocalDate yesterday = today.minusDays(1);

        Timestamp beginTime = Timestamp.valueOf(yesterday.atStartOfDay());
        Timestamp endTime = Timestamp.valueOf(LocalDateTime.now());

        processRawPosts(beginTime, endTime);
    }

    public void processRawPosts(Timestamp beginTime, Timestamp endTime) {
        try {
            log.info("Starting to process raw posts - begin: {}, end: {}", beginTime, endTime);

            List<String> channelIds = dcChannelMessageService.getAllChannelIdsByTimeRange(beginTime, endTime);
            log.info("Found {} distinct channelIds in time range", channelIds.size());

            int totalProcessed = 0;
            int totalSaved = 0;

            for (String channelId : channelIds) {
                log.info("Processing channelId: {}", channelId);
                List<DcChannelMessage> messages = dcChannelMessageService.getMessagesByChannelIdAndTimeRange(channelId, beginTime, endTime);
                log.info("Retrieved {} messages for channelId: {}", messages.size(), channelId);

                if (messages.isEmpty()) {
                    continue;
                }

                List<BloggerRawSentiment> sentiments = BloggerRawPostParser.parseRawPosts(messages);
                log.info("Parsed {} sentiments from {} messages", sentiments.size(), messages.size());

                for (BloggerRawSentiment sentiment : sentiments) {
                    try {
                        bloggerRawSentimentService.saveSentiment(sentiment);
                        totalSaved++;
                        log.info("Saved raw sentiment: {} - {} - {} - {}",
                                sentiment.getDate(),
                                sentiment.getTicker(),
                                sentiment.getBlogger(),
                                sentiment.getSentimentScore());
                    } catch (Exception e) {
                        log.error("Failed to save raw sentiment: {}", e.getMessage());
                    }
                }

                totalProcessed += messages.size();
            }

            log.info("Raw post processing completed. Total messages processed: {}, Total sentiments saved: {}",
                    totalProcessed, totalSaved);

        } catch (Exception e) {
            log.error("Error in raw post processing: {}", e.getMessage(), e);
        }
    }

    public void processRawMessages(List<DcChannelMessage> messages) {
        try {
            log.info("Starting to process {} raw messages directly", messages.size());

            if (messages == null || messages.isEmpty()) {
                log.info("No messages to process");
                return;
            }

            List<BloggerRawSentiment> sentiments = BloggerRawPostParser.parseRawPosts(messages);
            log.info("Parsed {} sentiments from {} messages", sentiments.size(), messages.size());

            int totalSaved = 0;
            for (BloggerRawSentiment sentiment : sentiments) {
                try {
                    bloggerRawSentimentService.saveSentiment(sentiment);
                    totalSaved++;
                    log.info("Saved raw sentiment: {} - {} - {} - {}",
                            sentiment.getDate(),
                            sentiment.getTicker(),
                            sentiment.getBlogger(),
                            sentiment.getSentimentScore());
                } catch (Exception e) {
                    log.error("Failed to save raw sentiment: {}", e.getMessage());
                }
            }

            log.info("Direct raw post processing completed. Total sentiments saved: {}", totalSaved);

        } catch (Exception e) {
            log.error("Error in direct raw message processing: {}", e.getMessage(), e);
        }
    }

    public void processRawPostsForDate(LocalDate date) {
        Timestamp beginTime = Timestamp.valueOf(date.atStartOfDay());
        Timestamp endTime = Timestamp.valueOf(date.plusDays(1).atStartOfDay());
        processRawPosts(beginTime, endTime);
    }

    public void processRawPostsForDateRange(LocalDate startDate, LocalDate endDate) {
        Timestamp beginTime = Timestamp.valueOf(startDate.atStartOfDay());
        Timestamp endTime = Timestamp.valueOf(endDate.plusDays(1).atStartOfDay());
        processRawPosts(beginTime, endTime);
    }
}
