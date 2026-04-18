package com.tencent.wxcloudrun.model;

public class DailySummary {
    private Long id;
    private String date; // 格式为yyyy-mm-dd的字符串
    private String content;
    
    // getters and setters
    public Long getId() {
        return id;
    }
    public void setId(Long id) {
        this.id = id;
    }
    public String getDate() {
        return date;
    }
    public void setDate(String date) {
        this.date = date;
    }
    public String getContent() {
        return content;
    }
    public void setContent(String content) {
        this.content = content;
    }
}
