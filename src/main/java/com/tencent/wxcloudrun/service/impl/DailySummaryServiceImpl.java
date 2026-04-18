package com.tencent.wxcloudrun.service.impl;

import com.tencent.wxcloudrun.dao.DailySummaryMapper;
import com.tencent.wxcloudrun.model.DailySummary;
import com.tencent.wxcloudrun.service.DailySummaryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class DailySummaryServiceImpl implements DailySummaryService {
    
    private final DailySummaryMapper dailySummaryMapper;
    
    @Autowired
    public DailySummaryServiceImpl(DailySummaryMapper dailySummaryMapper) {
        this.dailySummaryMapper = dailySummaryMapper;
    }
    
    @Override
    public void saveSummary(DailySummary dailySummary) {
        dailySummaryMapper.insert(dailySummary);
    }
    
    @Override
    public DailySummary getSummaryByDate(String date) {
        return dailySummaryMapper.findByDate(date);
    }
}
