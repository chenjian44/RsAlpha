package com.tencent.wxcloudrun.dto;

import lombok.Data;

import java.util.List;

@Data
public class DcChannelMessageBatchRequest {
    private List<DcChannelMessageRequest> messages;
}
