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
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Component
public class BloggerRawPostScheduler {

    private static final Logger log = LoggerFactory.getLogger(BloggerRawPostScheduler.class);
    private static final int FETCH_BATCH_SIZE = 500;

    // 配置的博主列表：channelId -> channelName
    private static final Map<String, String> TARGET_BLOGGERS = new LinkedHashMap<>();
    static {
        TARGET_BLOGGERS.put("1445405338353533099", "顺哥");
        TARGET_BLOGGERS.put("1445439292607434824", "价值哥");
        TARGET_BLOGGERS.put("1445440791836102897", "slias");
        TARGET_BLOGGERS.put("1471391584485511345", "美投君");
        TARGET_BLOGGERS.put("1445404404001017907", "润智");
        TARGET_BLOGGERS.put("1446028730513231872", "ace");
        TARGET_BLOGGERS.put("1471403783341736128", "serenity");
    }

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

        processRawPostsByBlogger(beginTime, endTime);
    }

    /**
     * 按博主维度循环处理消息
     */
    public void processRawPostsByBlogger(Timestamp beginTime, Timestamp endTime) {
        try {
            log.info("Starting to process raw posts by blogger - begin: {}, end: {}", beginTime, endTime);

            int totalSavedCount = 0;
            int totalProcessedBloggers = 0;

            for (Map.Entry<String, String> entry : TARGET_BLOGGERS.entrySet()) {
                String channelId = entry.getKey();
                String channelName = entry.getValue();

                try {
                    log.info("Processing blogger: {} ({})", channelName, channelId);

                    // 按博主捞取最近一天的消息
                    List<DcChannelMessage> messages = dcChannelMessageService.getMessagesByChannelIdAndTimeRange(
                            channelId, beginTime, endTime);

                    log.info("Fetched {} messages for blogger: {}", messages.size(), channelName);

                    if (messages.isEmpty()) {
                        log.info("No messages found for blogger: {}", channelName);
                        continue;
                    }

                    // 调用LLM解析该博主的消息
                    log.info("Calling LLM to parse {} messages for blogger: {}", messages.size(), channelName);
                    List<BloggerRawSentiment> sentiments = BloggerRawPostParser.parseRawPosts(messages, channelId, channelName);
                    log.info("LLM parsing completed for blogger: {}, parsed {} sentiments", channelName, sentiments.size());

                    // 保存解析结果
                    int savedCount = saveSentiments(sentiments);
                    totalSavedCount += savedCount;
                    totalProcessedBloggers++;

                    log.info("Blogger processing completed: {}, saved {} sentiments", channelName, savedCount);

                } catch (Exception e) {
                    log.error("Error processing blogger: {} ({})", channelName, channelId, e);
                }
            }

            log.info("All bloggers processing completed. Total processed bloggers: {}, Total sentiments saved: {}",
                    totalProcessedBloggers, totalSavedCount);

        } catch (Exception e) {
            log.error("Error in raw post processing by blogger: {}", e.getMessage(), e);
        }
    }


    public void processRawPostsForDate(LocalDate date) {
        Timestamp beginTime = Timestamp.valueOf(date.atStartOfDay());
        Timestamp endTime = Timestamp.valueOf(date.plusDays(1).atStartOfDay());
        processRawPostsByBlogger(beginTime, endTime);
    }

    public void processRawPostsForDateRange(LocalDate startDate, LocalDate endDate) {
        Timestamp beginTime = Timestamp.valueOf(startDate.atStartOfDay());
        Timestamp endTime = Timestamp.valueOf(endDate.plusDays(1).atStartOfDay());
        processRawPostsByBlogger(beginTime, endTime);
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
