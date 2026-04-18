package com.tencent.wxcloudrun.dao;

import com.tencent.wxcloudrun.model.DailySummary;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface DailySummaryMapper {
    void insert(DailySummary dailySummary);
    DailySummary findByDate(@Param("date") String date);
}
