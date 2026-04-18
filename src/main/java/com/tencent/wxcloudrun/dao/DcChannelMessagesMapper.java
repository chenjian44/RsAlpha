package com.tencent.wxcloudrun.dao;

import com.tencent.wxcloudrun.model.DcChannelMessage;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface DcChannelMessagesMapper {
    void insert(DcChannelMessage message);
}
