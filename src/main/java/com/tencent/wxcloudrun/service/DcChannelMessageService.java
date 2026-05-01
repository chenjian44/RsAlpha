package com.tencent.wxcloudrun.service;

import com.tencent.wxcloudrun.dto.DcChannelMessageRequest;
import com.tencent.wxcloudrun.model.DcChannelMessage;

import java.sql.Timestamp;
import java.util.List;

public interface DcChannelMessageService {
    void saveMessage(DcChannelMessageRequest request);

    List<DcChannelMessage> getMessagesByChannelId(String channelId);

    List<DcChannelMessage> getMessagesByChannelIdAndTimeRange(String channelId, Timestamp beginTime, Timestamp endTime);

    List<String> getAllChannelIds();

    List<String> getAllChannelIdsByTimeRange(Timestamp beginTime, Timestamp endTime);

    List<DcChannelMessage> getLatestMessages(int limit);
}
