file://<WORKSPACE>/src/main/java/com/tencent/wxcloudrun/controller/ChartController.java
empty definition using pc, found symbol in pc: 
semanticdb not found
empty definition using fallback
non-local guesses:

offset: 1108
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

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@RestController
@RequestMapping("/api/charts")
public class ChartController {

    @Autowired
    private BloggerSentimentService bloggerSentimentService;

    @GetMapping("/markers")
    public ApiResponse getMarkers(@RequestParam String ticker) {
        List<Map<String, Object>> markers = new ArrayList<>();

        // 获取当前日期和过去7天的日期
        LocalDate today@@ = LocalDate.now();
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
            for (BloggerSentiment sentiment : sentiments) {
                // 构建标记内容
                String content = buildMarkerContent(sentiment);
                // 构建时间戳（使用日期 + 固定时间）
                String timestamp = sentiment.getDate() + " 10:00:00";
                // 添加到标记列表
                markers.add(createMarkerMap(timestamp, sentiment.getBlogger(), content));
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

    private Map<String, Object> createMarkerMap(String ts, String user, String content) {
        Map<String, Object> map = new HashMap<>();
        map.put("timestamp", ts);
        map.put("user", user);
        map.put("content", content);
        return map;
    }
}

```


#### Short summary: 

empty definition using pc, found symbol in pc: 