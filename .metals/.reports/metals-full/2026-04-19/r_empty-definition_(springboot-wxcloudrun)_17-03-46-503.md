error id: file://<WORKSPACE>/src/main/java/com/tencent/wxcloudrun/controller/ChartController.java:_empty_/RequestMapping#
file://<WORKSPACE>/src/main/java/com/tencent/wxcloudrun/controller/ChartController.java
empty definition using pc, found symbol in pc: _empty_/RequestMapping#
found definition using semanticdb; symbol org/springframework/web/bind/annotation/RequestMapping#
empty definition using fallback
non-local guesses:

offset: 562
uri: file://<WORKSPACE>/src/main/java/com/tencent/wxcloudrun/controller/ChartController.java
text:
```scala
package com.tencent.wxcloudrun.controller;

import com.tencent.wxcloudrun.config.ApiResponse;
import com.tencent.wxcloudrun.utils.TigerKlineUtils;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@RestController
@@@RequestMapping("/api/charts")
public class ChartController {

    @GetMapping("/markers")
    public ApiResponse getMarkers(@RequestParam String ticker) {
        List<Map<String, Object>> mockMarkers = new ArrayList<>();

        mockMarkers.add(createMarkerMap("2026-04-10 10:00:00", "猫姐", ticker + " 动能分界线 170.5，站稳看多"));
        mockMarkers.add(createMarkerMap("2026-04-14 22:30:00", "鲍博士", ticker + " 财报前瞻，预期偏向乐观"));
        mockMarkers.add(createMarkerMap("2026-04-16 09:15:00", "猫姐", ticker + " 触及压力位，建议部分止盈"));

        return ApiResponse.ok(mockMarkers);
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

empty definition using pc, found symbol in pc: _empty_/RequestMapping#