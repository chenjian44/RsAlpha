package com.tencent.wxcloudrun.model;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class BloggerRawSentiment {
    private Long id;
    private String date;
    private String ticker;
    private String blogger;
    private Integer sentimentScore;
    private String horizon;
    private String strategy;
    private String rawContent;
    private String channelId;
    private String channelName;
    private LocalDateTime messageTime;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
