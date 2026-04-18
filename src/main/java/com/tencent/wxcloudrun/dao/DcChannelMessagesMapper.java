package com.tencent.wxcloudrun.dao;

import com.tencent.wxcloudrun.model.DcChannelMessage;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface DcChannelMessagesMapper {
    void insert(DcChannelMessage message);

    List<DcChannelMessage> selectByChannelIdOrderByTimestamp(@Param("channelId") String channelId);

    List<String> selectDistinctChannelIds();
}
