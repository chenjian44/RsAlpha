package com.tencent.wxcloudrun.service;

import com.tencent.wxcloudrun.dto.DcChannelMessageRequest;

public interface DcChannelMessageService {
    void saveMessage(DcChannelMessageRequest request);
}
