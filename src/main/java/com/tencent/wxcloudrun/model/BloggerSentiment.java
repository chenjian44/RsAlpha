package com.tencent.wxcloudrun.model;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class BloggerSentiment {
    private Long id;
    private String date;
    private String ticker;
    private String blogger;
    private Integer sentimentScore;
    private String horizon;
    private String strategy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
