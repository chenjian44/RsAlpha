package com.tencent.wxcloudrun.service;

import com.tencent.wxcloudrun.model.BloggerSentiment;

import java.util.List;

public interface BloggerSentimentService {

    void saveSentiment(BloggerSentiment sentiment);

    List<BloggerSentiment> getSentimentsByDate(String date);

    List<BloggerSentiment> getSentimentsByTickerAndDate(String ticker, String date);

    BloggerSentiment getSentimentByDateAndTickerAndBlogger(String date, String ticker, String blogger);

    void updateSentiment(BloggerSentiment sentiment);
}
