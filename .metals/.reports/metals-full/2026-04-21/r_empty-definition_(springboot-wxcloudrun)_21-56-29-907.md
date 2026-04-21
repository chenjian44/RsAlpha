error id: file://<WORKSPACE>/src/main/java/com/tencent/wxcloudrun/controller/ChartController.java:org/springframework/web/bind/annotation/RequestParam#
file://<WORKSPACE>/src/main/java/com/tencent/wxcloudrun/controller/ChartController.java
empty definition using pc, found symbol in pc: org/springframework/web/bind/annotation/RequestParam#
semanticdb not found
empty definition using fallback
non-local guesses:

offset: 1041
uri: file://<WORKSPACE>/src/main/java/com/tencent/wxcloudrun/controller/ChartController.java
text:
```scala
package com.tencent.wxcloudrun.controller;

import com.tencent.wxcloudrun.config.ApiResponse;
import com.tencent.wxcloudrun.model.BloggerSentiment;
import com.tencent.wxcloudrun.service.BloggerSentimentService;
import com.tencent.wxcloudrun.utils.TigerKlineUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;


@RestController
@RequestMapping("/api/charts")
public class ChartController {

    @Autowired
    private BloggerSentimentService bloggerSentimentService;

    @GetMapping("/markers")
    public ApiResponse getMarkers(@@@RequestParam String ticker) {
        List<Map<String, Object>> markers = new ArrayList<>();

        // 获取当前日期和过去7天的日期
        LocalDate today = LocalDate.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        List<String> dates = new ArrayList<>();

        // 收集过去7天的日期
        for (int i = 0; i < 30; i++) {
            LocalDate date = today.minusDays(i);
            dates.add(date.format(formatter));
        }

        // 批量查询过去30天的情感数据
        List<BloggerSentiment> sentiments = bloggerSentimentService.getSentimentsByTickerAndDates(ticker, dates);

        // 处理查询结果
        if (sentiments != null && !sentiments.isEmpty()) {
            // 使用 Set 来存储唯一的标记键，实现去重
            Set<String> uniqueMarkerKeys = new HashSet<>();
            // 使用 Map 来跟踪每个日期的分钟计数，确保当天的时间按分钟递增
            Map<String, Integer> dateMinuteCounter = new HashMap<>();
            
            for (BloggerSentiment sentiment : sentiments) {
                // 生成唯一键：日期-博主-策略-看多看空标记-标的
                String sentimentDirection = "中性";
                if (sentiment.getSentimentScore() != null) {
                    if (sentiment.getSentimentScore() > 0) {
                        sentimentDirection = "看涨";
                    } else if (sentiment.getSentimentScore() < 0) {
                        sentimentDirection = "看跌";
                    }
                }
                
                String uniqueKey = String.format("%s-%s-%s-%s-%s", 
                    sentiment.getDate(), 
                    sentiment.getBlogger(), 
                    sentiment.getStrategy() != null ? sentiment.getStrategy() : "", 
                    sentimentDirection, 
                    sentiment.getTicker());
                
                // 只有当键不存在时，才添加标记
                if (!uniqueMarkerKeys.contains(uniqueKey)) {
                    uniqueMarkerKeys.add(uniqueKey);
                    // 构建标记内容
                    String content = buildMarkerContent(sentiment);
                    // 1. 获取原始日期字符串 (假设格式为 "YYYY-MM-DD")
                    String rawDateStr = sentiment.getDate();
                    LocalDate date = LocalDate.parse(rawDateStr);
                    date = date.minusDays(1);
                    // 2. 判断并平移周末到周五
                    DayOfWeek dayOfWeek = date.getDayOfWeek();
                    if (dayOfWeek == DayOfWeek.SATURDAY) {
                        // 如果是周六，往前推1天
                        date = date.minusDays(1);
                    } else if (dayOfWeek == DayOfWeek.SUNDAY) {
                        // 如果是周日，往前推2天
                        date = date.minusDays(2);
                    }

                    // 3. 构建最终的时间戳（使用清洗后的日期 + 固定时间）
                    // date.toString() 会默认输出 "YYYY-MM-DD" 格式
                    String timestamp = date.toString() + " 10:00:00";
                    // 添加到标记列表
                    markers.add(createMarkerMap(timestamp, sentiment.getBlogger(), content, sentiment.getHorizon()));
                }
            }
        }

        return ApiResponse.ok(markers);
    }

    private String buildMarkerContent(BloggerSentiment sentiment) {
        StringBuilder content = new StringBuilder();
        content.append(sentiment.getTicker());
        
        if (sentiment.getSentimentScore() != null) {
            if (sentiment.getSentimentScore() > 0) {
                content.append(" 看涨，");
            } else if (sentiment.getSentimentScore() < 0) {
                content.append(" 看跌，");
            } else {
                content.append(" 中性，");
            }
        }
        
        if (sentiment.getHorizon() != null) {
            content.append("时间范围：").append(sentiment.getHorizon()).append("，");
        }
        
        if (sentiment.getStrategy() != null) {
            content.append("策略：").append(sentiment.getStrategy());
        }
        
        return content.toString();
    }

    @GetMapping("/kline")
    public ApiResponse getKlineData(@RequestParam String symbol) {
        try {
            List<Map<String, Object>> klineData = TigerKlineUtils.getKlineData(symbol, 1);
            return ApiResponse.ok(klineData);
        } catch (Exception e) {
            return ApiResponse.error("获取K线数据失败: " + e.getMessage());
        }
    }

    private Map<String, Object> createMarkerMap(String ts, String user, String content, String horizon) {
        Map<String, Object> map = new HashMap<>();
        map.put("timestamp", ts);
        map.put("user", user);
        map.put("content", content);
        map.put("horizon", horizon);
        return map;
    }
}

```


#### Short summary: 

empty definition using pc, found symbol in pc: org/springframework/web/bind/annotation/RequestParam#