package com.tencent.wxcloudrun.service;

import com.tencent.wxcloudrun.dto.DcChannelMessageRequest;
import com.tencent.wxcloudrun.model.DcChannelMessage;

import java.util.List;

public interface DcChannelMessageService {
    void saveMessage(DcChannelMessageRequest request);

    List<DcChannelMessage> getMessagesByChannelId(String channelId);

    List<String> getAllChannelIds();
}
