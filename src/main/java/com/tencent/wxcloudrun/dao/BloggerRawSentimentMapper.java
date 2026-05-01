package com.tencent.wxcloudrun.dao;

import com.tencent.wxcloudrun.model.BloggerRawSentiment;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface BloggerRawSentimentMapper {

    void insert(BloggerRawSentiment sentiment);

    List<BloggerRawSentiment> getByDate(String date);

    List<BloggerRawSentiment> getByTickerAndDate(String ticker, String date);

    List<BloggerRawSentiment> getByTickerAndTimeRange(String ticker, String startTime, String endTime);

    List<BloggerRawSentiment> getByTickerAndTimeRangeWithBloggers(String ticker, String startTime, String endTime, List<String> bloggers);

    List<String> getDistinctBloggersByTicker(String ticker);

    BloggerRawSentiment getByDateAndTickerAndBlogger(String date, String ticker, String blogger);

    void update(BloggerRawSentiment sentiment);

    List<BloggerRawSentiment> getByBloggerAndDateRange(String blogger, String startTime, String endTime);

    int countByDateAndBlogger(String date, String blogger);
}
