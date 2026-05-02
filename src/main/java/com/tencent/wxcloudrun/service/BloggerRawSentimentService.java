package com.tencent.wxcloudrun.service;

import com.tencent.wxcloudrun.model.BloggerRawSentiment;

import java.util.List;

public interface BloggerRawSentimentService {

    void saveSentiment(BloggerRawSentiment sentiment);

    List<BloggerRawSentiment> getSentimentsByDate(String date);

    List<BloggerRawSentiment> getSentimentsByTickerAndDate(String ticker, String date);

    List<BloggerRawSentiment> getSentimentsByTickerAndTimeRange(String ticker, String startTime, String endTime);

    List<BloggerRawSentiment> getSentimentsByTickerAndTimeRange(String ticker, String startTime, String endTime, List<String> bloggers);

    List<BloggerRawSentiment> getSentimentsByTickerAndChannelName(String ticker, String startTime, String endTime, String channelName);

    List<String> getDistinctChannelNamesByTicker(String ticker);

    BloggerRawSentiment getSentimentByDateAndTickerAndBlogger(String date, String ticker, String blogger);

    void updateSentiment(BloggerRawSentiment sentiment);

    List<BloggerRawSentiment> getSentimentsByBloggerAndDateRange(String blogger, String startTime, String endTime);

    int countByDateAndBlogger(String date, String blogger);
}
