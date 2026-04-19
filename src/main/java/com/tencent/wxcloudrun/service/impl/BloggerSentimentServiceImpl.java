package com.tencent.wxcloudrun.service.impl;

import com.tencent.wxcloudrun.dao.BloggerSentimentMapper;
import com.tencent.wxcloudrun.model.BloggerSentiment;
import com.tencent.wxcloudrun.service.BloggerSentimentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class BloggerSentimentServiceImpl implements BloggerSentimentService {

    private final BloggerSentimentMapper bloggerSentimentMapper;

    @Autowired
    public BloggerSentimentServiceImpl(BloggerSentimentMapper bloggerSentimentMapper) {
        this.bloggerSentimentMapper = bloggerSentimentMapper;
    }

    @Override
    public void saveSentiment(BloggerSentiment sentiment) {
        bloggerSentimentMapper.insert(sentiment);
    }

    @Override
    public List<BloggerSentiment> getSentimentsByDate(String date) {
        return bloggerSentimentMapper.getByDate(date);
    }

    @Override
    public List<BloggerSentiment> getSentimentsByTickerAndDate(String ticker, String date) {
        return bloggerSentimentMapper.getByTickerAndDate(ticker, date);
    }

    @Override
    public BloggerSentiment getSentimentByDateAndTickerAndBlogger(String date, String ticker, String blogger) {
        return bloggerSentimentMapper.getByDateAndTickerAndBlogger(date, ticker, blogger);
    }

    @Override
    public void updateSentiment(BloggerSentiment sentiment) {
        bloggerSentimentMapper.update(sentiment);
    }
}
