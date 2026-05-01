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
    private static final int BATCH_SIZE = 100;

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

        processRawPostsInBatches(beginTime, endTime);
    }

    public void processRawPosts(Timestamp beginTime, Timestamp endTime) {
        processRawPostsInBatches(beginTime, endTime);
    }

    private void processRawPostsInBatches(Timestamp beginTime, Timestamp endTime) {
        try {
            log.info("Starting to process raw posts in batches - begin: {}, end: {}", beginTime, endTime);

            int offset = 0;
            int totalProcessedMessages = 0;
            int totalSavedSentiments = 0;

            while (true) {
                List<DcChannelMessage> messages = dcChannelMessageService.getMessagesByTimeRangeWithLimit(beginTime, endTime, offset, BATCH_SIZE);
                log.info("Retrieved {} messages at offset {}", messages.size(), offset);

                if (messages.isEmpty()) {
                    break;
                }

                List<BloggerRawSentiment> sentiments = BloggerRawPostParser.parseRawPosts(messages);
                log.info("Parsed {} sentiments from {} messages", sentiments.size(), messages.size());

                int savedCount = saveSentiments(sentiments);
                totalSavedSentiments += savedCount;
                totalProcessedMessages += messages.size();

                offset += BATCH_SIZE;

                if (messages.size() < BATCH_SIZE) {
                    break;
                }
            }

            log.info("Raw post processing completed. Total messages processed: {}, Total sentiments saved: {}",
                    totalProcessedMessages, totalSavedSentiments);

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

            int totalSaved = saveSentiments(sentiments);

            log.info("Direct raw post processing completed. Total sentiments saved: {}", totalSaved);

        } catch (Exception e) {
            log.error("Error in direct raw message processing: {}", e.getMessage(), e);
        }
    }

    public void processRawPostsForDate(LocalDate date) {
        Timestamp beginTime = Timestamp.valueOf(date.atStartOfDay());
        Timestamp endTime = Timestamp.valueOf(date.plusDays(1).atStartOfDay());
        processRawPostsInBatches(beginTime, endTime);
    }

    public void processRawPostsForDateRange(LocalDate startDate, LocalDate endDate) {
        Timestamp beginTime = Timestamp.valueOf(startDate.atStartOfDay());
        Timestamp endTime = Timestamp.valueOf(endDate.plusDays(1).atStartOfDay());
        processRawPostsInBatches(beginTime, endTime);
    }

    private int saveSentiments(List<BloggerRawSentiment> sentiments) {
        int savedCount = 0;
        for (BloggerRawSentiment sentiment : sentiments) {
            try {
                bloggerRawSentimentService.saveSentiment(sentiment);
                savedCount++;
                log.debug("Saved raw sentiment: {} - {} - {} - {}",
                        sentiment.getDate(),
                        sentiment.getTicker(),
                        sentiment.getBlogger(),
                        sentiment.getSentimentScore());
            } catch (Exception e) {
                log.error("Failed to save raw sentiment: {}", e.getMessage());
            }
        }
        return savedCount;
    }
}
