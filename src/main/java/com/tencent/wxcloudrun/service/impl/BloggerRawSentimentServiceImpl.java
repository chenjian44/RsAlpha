package com.tencent.wxcloudrun.service.impl;

import com.tencent.wxcloudrun.dao.BloggerRawSentimentMapper;
import com.tencent.wxcloudrun.model.BloggerRawSentiment;
import com.tencent.wxcloudrun.service.BloggerRawSentimentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class BloggerRawSentimentServiceImpl implements BloggerRawSentimentService {

    private final BloggerRawSentimentMapper bloggerRawSentimentMapper;

    @Autowired
    public BloggerRawSentimentServiceImpl(BloggerRawSentimentMapper bloggerRawSentimentMapper) {
        this.bloggerRawSentimentMapper = bloggerRawSentimentMapper;
    }

    @Override
    public void saveSentiment(BloggerRawSentiment sentiment) {
        bloggerRawSentimentMapper.insert(sentiment);
    }

    @Override
    public List<BloggerRawSentiment> getSentimentsByDate(String date) {
        return bloggerRawSentimentMapper.getByDate(date);
    }

    @Override
    public List<BloggerRawSentiment> getSentimentsByTickerAndDate(String ticker, String date) {
        return bloggerRawSentimentMapper.getByTickerAndDate(ticker, date);
    }

    @Override
    public List<BloggerRawSentiment> getSentimentsByTickerAndTimeRange(String ticker, String startTime, String endTime) {
        return bloggerRawSentimentMapper.getByTickerAndTimeRange(ticker, startTime, endTime);
    }

    @Override
    public List<BloggerRawSentiment> getSentimentsByTickerAndTimeRange(String ticker, String startTime, String endTime, List<String> bloggers) {
        return bloggerRawSentimentMapper.getByTickerAndTimeRangeWithBloggers(ticker, startTime, endTime, bloggers);
    }

    @Override
    public List<BloggerRawSentiment> getSentimentsByTickerAndChannelName(String ticker, String startTime, String endTime, String channelName) {
        return bloggerRawSentimentMapper.getByTickerAndChannelName(ticker, startTime, endTime, channelName);
    }

    @Override
    public List<String> getDistinctChannelNamesByTicker(String ticker) {
        return bloggerRawSentimentMapper.getDistinctChannelNamesByTicker(ticker);
    }

    @Override
    public BloggerRawSentiment getSentimentByDateAndTickerAndBlogger(String date, String ticker, String blogger) {
        return bloggerRawSentimentMapper.getByDateAndTickerAndBlogger(date, ticker, blogger);
    }

    @Override
    public void updateSentiment(BloggerRawSentiment sentiment) {
        bloggerRawSentimentMapper.update(sentiment);
    }

    @Override
    public List<BloggerRawSentiment> getSentimentsByBloggerAndDateRange(String blogger, String startTime, String endTime) {
        return bloggerRawSentimentMapper.getByBloggerAndDateRange(blogger, startTime, endTime);
    }

    @Override
    public int countByDateAndBlogger(String date, String blogger) {
        return bloggerRawSentimentMapper.countByDateAndBlogger(date, blogger);
    }
}
