package com.tencent.wxcloudrun.model;

import lombok.Data;

import java.sql.Timestamp;

@Data
public class DcChannelMessage {
    private Integer id;
    private String channelId;
    private String channelName;
    private Timestamp timestamp;
    private String user;
    private String content;
    private String contentMd5;
    private Timestamp createdAt;
    private Timestamp updatedAt;
}
