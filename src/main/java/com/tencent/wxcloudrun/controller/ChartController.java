package com.tencent.wxcloudrun.controller;

import com.tencent.wxcloudrun.config.ApiResponse;
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
@RequestMapping("/api/charts")
public class ChartController {

  /**
   * Mock 投研标注数据
   * 对应前端：fetch(`/api/charts/markers?ticker=${ticker}`)
   */
  @GetMapping("/markers")
  public ApiResponse getMarkers(@RequestParam String ticker) {
    List<Map<String, Object>> mockMarkers = new ArrayList<>();

    // 模拟猫姐和鲍博士的观点数据
    // 注意：这里的 timestamp 格式要和前端处理逻辑一致
    mockMarkers.add(createMarkerMap("2026-04-10 10:00:00", "猫姐", ticker + " 动能分界线 170.5，站稳看多"));
    mockMarkers.add(createMarkerMap("2026-04-14 22:30:00", "鲍博士", ticker + " 财报前瞻，预期偏向乐观"));
    mockMarkers.add(createMarkerMap("2026-04-16 09:15:00", "猫姐", ticker + " 触及压力位，建议部分止盈"));

    return ApiResponse.ok(mockMarkers);
  }

  /**
   * Mock K 线基础数据
   * 对应前端：fetch(`/api/charts/kline?symbol=${ticker}`)
   */
  @GetMapping("/kline")
  public ApiResponse getKlineData(@RequestParam String symbol) {
    List<Map<String, Object>> mockKlines = new ArrayList<>();

    // 模拟最近 10 天的 K 线走势
    // time 格式：YYYY-MM-DD
    mockKlines.add(createKlineMap("2026-04-08", 165.0, 168.5, 164.2, 167.3));
    mockKlines.add(createKlineMap("2026-04-09", 167.3, 170.2, 166.8, 169.5));
    mockKlines.add(createKlineMap("2026-04-10", 169.5, 172.8, 169.1, 171.2));
    mockKlines.add(createKlineMap("2026-04-11", 171.2, 171.5, 168.0, 168.8));
    mockKlines.add(createKlineMap("2026-04-12", 168.8, 170.5, 168.5, 170.1));
    mockKlines.add(createKlineMap("2026-04-13", 170.1, 173.4, 169.8, 172.9));
    mockKlines.add(createKlineMap("2026-04-14", 172.9, 175.0, 171.5, 174.2));
    mockKlines.add(createKlineMap("2026-04-15", 174.2, 176.8, 173.0, 176.1));
    mockKlines.add(createKlineMap("2026-04-16", 176.1, 180.2, 175.5, 179.4));
    mockKlines.add(createKlineMap("2026-04-17", 179.4, 182.5, 178.1, 181.2));

    return ApiResponse.ok(mockKlines);
  }

  // 辅助方法：创建 Marker 数据结构
  private Map<String, Object> createMarkerMap(String ts, String user, String content) {
    Map<String, Object> map = new HashMap<>();
    map.put("timestamp", ts);
    map.put("user", user);
    map.put("content", content);
    return map;
  }

  // 辅助方法：创建 K 线数据结构
  private Map<String, Object> createKlineMap(String time, double o, double h, double l, double c) {
    Map<String, Object> map = new HashMap<>();
    map.put("time", time);
    map.put("open", o);
    map.put("high", h);
    map.put("low", l);
    map.put("close", c);
    return map;
  }
}

