package com.tencent.wxcloudrun.service;

import com.tencent.wxcloudrun.model.DailySummary;

public interface DailySummaryService {
    void saveSummary(DailySummary dailySummary);
    DailySummary getSummaryByDate(String date);
}
