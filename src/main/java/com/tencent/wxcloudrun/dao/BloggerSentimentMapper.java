package com.tencent.wxcloudrun.dao;

import com.tencent.wxcloudrun.model.BloggerSentiment;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface BloggerSentimentMapper {

    void insert(BloggerSentiment sentiment);

    List<BloggerSentiment> getByDate(String date);

    List<BloggerSentiment> getByTickerAndDate(String ticker, String date);

    List<BloggerSentiment> getByTickerAndDates(String ticker, List<String> dates);

    List<BloggerSentiment> getByTickerAndTimeRange(String ticker, String startTime, String endTime);

    BloggerSentiment getByDateAndTickerAndBlogger(String date, String ticker, String blogger);

    void update(BloggerSentiment sentiment);
}
