package com.tencent.wxcloudrun.dto;

import lombok.Data;

@Data
public class DcChannelMessageRequest {
    private String channelId;
    private String channelName;
    private String timestamp;
    private String user;
    private String content;
}
