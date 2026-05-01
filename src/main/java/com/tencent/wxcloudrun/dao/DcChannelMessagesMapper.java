package com.tencent.wxcloudrun.dao;

import com.tencent.wxcloudrun.model.DcChannelMessage;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.sql.Timestamp;
import java.util.List;

@Mapper
public interface DcChannelMessagesMapper {
    void insert(DcChannelMessage message);

    List<DcChannelMessage> selectByChannelIdOrderByTimestamp(@Param("channelId") String channelId);

    List<DcChannelMessage> selectByChannelIdAndTimeRange(
            @Param("channelId") String channelId,
            @Param("beginTime") Timestamp beginTime,
            @Param("endTime") Timestamp endTime);

    List<String> selectDistinctChannelIds();

    List<String> selectDistinctChannelIdsByTimeRange(
            @Param("beginTime") Timestamp beginTime,
            @Param("endTime") Timestamp endTime);

    List<DcChannelMessage> selectLatestMessages(@Param("limit") int limit);
}
