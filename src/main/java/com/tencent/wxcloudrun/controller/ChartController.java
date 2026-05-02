package com.tencent.wxcloudrun.controller;

import com.tencent.wxcloudrun.config.ApiResponse;
import com.tencent.wxcloudrun.model.BloggerRawSentiment;
import com.tencent.wxcloudrun.service.BloggerRawSentimentService;
import com.tencent.wxcloudrun.utils.TigerKlineUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.*;


@RestController
@RequestMapping("/api/charts")
public class ChartController {

    @Autowired
    private BloggerRawSentimentService bloggerRawSentimentService;

    @GetMapping("/markers")
    public ApiResponse getMarkers(@RequestParam String ticker, @RequestParam(required = false) List<String> bloggers) {
        List<Map<String, Object>> markers = new ArrayList<>();

        LocalDate today = LocalDate.now();
        LocalDate startDate = LocalDate.of(2026, 1, 1);
        String startTime = startDate.toString();
        String endTime = today.toString();

        List<BloggerRawSentiment> sentiments = bloggerRawSentimentService.getSentimentsByTickerAndTimeRange(ticker, startTime, endTime, bloggers);

        if (sentiments != null && !sentiments.isEmpty()) {
            Set<String> uniqueMarkerKeys = new HashSet<>();
            Map<String, Integer> dateMinuteCounter = new HashMap<>();

            for (BloggerRawSentiment sentiment : sentiments) {
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

                if (!uniqueMarkerKeys.contains(uniqueKey)) {
                    uniqueMarkerKeys.add(uniqueKey);
                    String content = buildMarkerContent(sentiment);
                    String rawDateStr = sentiment.getDate();
                    LocalDate date = LocalDate.parse(rawDateStr);
                    date = date.minusDays(1);
                    DayOfWeek dayOfWeek = date.getDayOfWeek();
                    if (dayOfWeek == DayOfWeek.SATURDAY) {
                        date = date.minusDays(1);
                    } else if (dayOfWeek == DayOfWeek.SUNDAY) {
                        date = date.minusDays(2);
                    }

                    String timestamp = date.toString() + " 10:00:00";
                    markers.add(createMarkerMap(timestamp, sentiment.getBlogger(), content, sentiment.getHorizon()));
                }
            }
        }

        return ApiResponse.ok(markers);
    }

    private String buildMarkerContent(BloggerRawSentiment sentiment) {
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

    @GetMapping("/bloggers")
    public ApiResponse getBloggers(@RequestParam String ticker) {
        try {
            List<String> bloggers = bloggerRawSentimentService.getDistinctBloggersByTicker(ticker);
            return ApiResponse.ok(bloggers);
        } catch (Exception e) {
            return ApiResponse.error("获取博主列表失败: " + e.getMessage());
        }
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

    @GetMapping("/blogger-review")
    public ApiResponse getBloggerReviewData(
            @RequestParam String ticker,
            @RequestParam(required = false) String blogger,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {
        try {
            LocalDate today = LocalDate.now();
            LocalDate start = startDate != null ? LocalDate.parse(startDate) : today.minusMonths(3);
            LocalDate end = endDate != null ? LocalDate.parse(endDate) : today;

            List<BloggerRawSentiment> sentiments;
            if (blogger != null && !blogger.isEmpty()) {
                sentiments = bloggerRawSentimentService.getSentimentsByTickerAndTimeRange(ticker, start.toString(), end.toString(), Arrays.asList(blogger));
            } else {
                sentiments = bloggerRawSentimentService.getSentimentsByTickerAndTimeRange(ticker, start.toString(), end.toString());
            }

            List<Map<String, Object>> klineData = TigerKlineUtils.getKlineData(ticker);
            Map<String, Double> priceMap = new HashMap<>();
            for (Map<String, Object> k : klineData) {
                Object timeObj = k.get("time");
                if (timeObj instanceof Number) {
                    long timestamp = ((Number) timeObj).longValue();
                    LocalDate date = LocalDate.ofEpochDay(timestamp / 86400);
                    Object closeObj = k.get("close");
                    if (closeObj instanceof Number) {
                        priceMap.put(date.toString(), ((Number) closeObj).doubleValue());
                    }
                }
            }

            List<Map<String, Object>> reviewRecords = new ArrayList<>();
            for (BloggerRawSentiment s : sentiments) {
                String opinionDate = s.getDate();
                Double opinionPrice = priceMap.get(opinionDate);

                if (opinionPrice == null) {
                    continue;
                }

                int horizonDays = parseHorizonToDays(s.getHorizon());
                LocalDate targetDate = LocalDate.parse(opinionDate).plusDays(horizonDays);
                Double targetPrice = priceMap.get(targetDate.toString());

                if (targetPrice == null) {
                    LocalDate nearestTradingDay = findNearestTradingDay(targetDate, priceMap.keySet());
                    if (nearestTradingDay != null) {
                        targetPrice = priceMap.get(nearestTradingDay.toString());
                    }
                }

                if (targetPrice != null) {
                    double priceChange = ((targetPrice - opinionPrice) / opinionPrice) * 100;
                    String sentimentDirection = getSentimentDirection(s.getSentimentScore());
                    String verificationResult = getVerificationResult(s.getSentimentScore(), priceChange);

                    Map<String, Object> record = new HashMap<>();
                    record.put("id", s.getId());
                    record.put("date", opinionDate);
                    record.put("ticker", s.getTicker());
                    record.put("blogger", s.getBlogger());
                    record.put("sentimentScore", s.getSentimentScore());
                    record.put("sentimentDirection", sentimentDirection);
                    record.put("horizon", s.getHorizon());
                    record.put("strategy", s.getStrategy());
                    record.put("opinionPrice", roundToTwoDecimals(opinionPrice));
                    record.put("targetDate", targetDate.toString());
                    record.put("targetPrice", roundToTwoDecimals(targetPrice));
                    record.put("priceChange", roundToTwoDecimals(priceChange));
                    record.put("verificationResult", verificationResult);
                    record.put("rawContent", s.getRawContent());
                    reviewRecords.add(record);
                }
            }

            reviewRecords.sort((a, b) -> ((String) b.get("date")).compareTo((String) a.get("date")));

            Map<String, Object> summary = calculateSummary(reviewRecords);

            Map<String, Object> result = new HashMap<>();
            result.put("records", reviewRecords);
            result.put("summary", summary);
            result.put("total", reviewRecords.size());

            return ApiResponse.ok(result);
        } catch (Exception e) {
            return ApiResponse.error("获取复盘数据失败: " + e.getMessage());
        }
    }

    @GetMapping("/blogger-review/by-blogger")
    public ApiResponse getBloggerReviewSummary(@RequestParam String ticker) {
        try {
            LocalDate today = LocalDate.now();
            LocalDate start = today.minusYears(1);

            List<BloggerRawSentiment> allSentiments = bloggerRawSentimentService.getSentimentsByTickerAndTimeRange(ticker, start.toString(), today.toString());

            List<Map<String, Object>> klineData = TigerKlineUtils.getKlineData(ticker);
            Map<String, Double> priceMap = new HashMap<>();
            for (Map<String, Object> k : klineData) {
                Object timeObj = k.get("time");
                if (timeObj instanceof Number) {
                    long timestamp = ((Number) timeObj).longValue();
                    LocalDate date = LocalDate.ofEpochDay(timestamp / 86400);
                    Object closeObj = k.get("close");
                    if (closeObj instanceof Number) {
                        priceMap.put(date.toString(), ((Number) closeObj).doubleValue());
                    }
                }
            }

            Map<String, List<Map<String, Object>>> bloggerRecords = new HashMap<>();
            for (BloggerRawSentiment s : allSentiments) {
                String opinionDate = s.getDate();
                Double opinionPrice = priceMap.get(opinionDate);

                if (opinionPrice == null) {
                    continue;
                }

                int horizonDays = parseHorizonToDays(s.getHorizon());
                LocalDate targetDate = LocalDate.parse(opinionDate).plusDays(horizonDays);
                Double targetPrice = priceMap.get(targetDate.toString());

                if (targetPrice == null) {
                    LocalDate nearestTradingDay = findNearestTradingDay(targetDate, priceMap.keySet());
                    if (nearestTradingDay != null) {
                        targetPrice = priceMap.get(nearestTradingDay.toString());
                    }
                }

                if (targetPrice != null) {
                    double priceChange = ((targetPrice - opinionPrice) / opinionPrice) * 100;
                    String verificationResult = getVerificationResult(s.getSentimentScore(), priceChange);

                    Map<String, Object> record = new HashMap<>();
                    record.put("date", opinionDate);
                    record.put("ticker", s.getTicker());
                    record.put("sentimentScore", s.getSentimentScore());
                    record.put("sentimentDirection", getSentimentDirection(s.getSentimentScore()));
                    record.put("horizon", s.getHorizon());
                    record.put("strategy", s.getStrategy());
                    record.put("opinionPrice", roundToTwoDecimals(opinionPrice));
                    record.put("targetDate", targetDate.toString());
                    record.put("targetPrice", roundToTwoDecimals(targetPrice));
                    record.put("priceChange", roundToTwoDecimals(priceChange));
                    record.put("verificationResult", verificationResult);

                    bloggerRecords.computeIfAbsent(s.getBlogger(), k -> new ArrayList<>()).add(record);
                }
            }

            List<Map<String, Object>> bloggerSummaries = new ArrayList<>();
            bloggerRecords.forEach((blogger, records) -> {
                Map<String, Object> summary = calculateSummary(records);
                summary.put("blogger", blogger);
                summary.put("totalOpinions", records.size());
                bloggerSummaries.add(summary);
            });

            bloggerSummaries.sort((a, b) -> {
                double aRate = (double) a.get("accuracyRate");
                double bRate = (double) b.get("accuracyRate");
                return Double.compare(bRate, aRate);
            });

            return ApiResponse.ok(bloggerSummaries);
        } catch (Exception e) {
            return ApiResponse.error("获取博主统计失败: " + e.getMessage());
        }
    }

    private int parseHorizonToDays(String horizon) {
        if (horizon == null || horizon.isEmpty()) {
            return 5;
        }
        String h = horizon.toLowerCase();
        if (h.contains("日内") || h.contains("当天") || h.contains("日内交易")) {
            return 1;
        } else if (h.contains("短期") || h.contains("一周") || h.contains("周")) {
            return 5;
        } else if (h.contains("中期") || h.contains("一月") || h.contains("月")) {
            return 30;
        } else if (h.contains("长期") || h.contains("季度") || h.contains("年")) {
            return 90;
        }
        try {
            return Integer.parseInt(horizon.replaceAll("[^0-9]", ""));
        } catch (NumberFormatException e) {
            return 5;
        }
    }

    private LocalDate findNearestTradingDay(LocalDate target, Set<String> tradingDays) {
        if (tradingDays.contains(target.toString())) {
            return target;
        }
        LocalDate before = target.minusDays(1);
        LocalDate after = target.plusDays(1);
        LocalDate foundBefore = null;
        LocalDate foundAfter = null;

        for (int i = 0; i < 7; i++) {
            if (tradingDays.contains(before.toString())) {
                foundBefore = before;
                break;
            }
            before = before.minusDays(1);
        }

        for (int i = 0; i < 7; i++) {
            if (tradingDays.contains(after.toString())) {
                foundAfter = after;
                break;
            }
            after = after.plusDays(1);
        }

        if (foundBefore != null && foundAfter != null) {
            return target.toEpochDay() - foundBefore.toEpochDay() < foundAfter.toEpochDay() - target.toEpochDay() ? foundBefore : foundAfter;
        } else if (foundBefore != null) {
            return foundBefore;
        } else if (foundAfter != null) {
            return foundAfter;
        }
        return target;
    }

    private String getSentimentDirection(Integer score) {
        if (score == null) return "中性";
        if (score > 0) return "看涨";
        if (score < 0) return "看跌";
        return "中性";
    }

    private String getVerificationResult(Integer sentimentScore, double priceChange) {
        if (sentimentScore == null) {
            return "无法判断";
        }

        boolean bullishCorrect = sentimentScore > 0 && priceChange > 0;
        boolean bearishCorrect = sentimentScore < 0 && priceChange < 0;
        boolean neutralCorrect = sentimentScore == 0 && Math.abs(priceChange) < 2;

        if (bullishCorrect || bearishCorrect) {
            return "✓ 正确";
        } else if (neutralCorrect) {
            return "✓ 正确";
        } else {
            return "✗ 错误";
        }
    }

    private Map<String, Object> calculateSummary(List<Map<String, Object>> records) {
        int total = records.size();
        if (total == 0) {
            Map<String, Object> empty = new HashMap<>();
            empty.put("total", 0);
            empty.put("accuracyRate", 0.0);
            empty.put("correctCount", 0);
            empty.put("wrongCount", 0);
            empty.put("avgPriceChange", 0.0);
            empty.put("maxGain", 0.0);
            empty.put("maxLoss", 0.0);
            empty.put("bullishAccuracy", 0.0);
            empty.put("bearishAccuracy", 0.0);
            return empty;
        }

        int correctCount = 0;
        int wrongCount = 0;
        double totalChange = 0;
        double maxGain = Double.MIN_VALUE;
        double maxLoss = Double.MAX_VALUE;
        int bullishTotal = 0;
        int bullishCorrect = 0;
        int bearishTotal = 0;
        int bearishCorrect = 0;

        for (Map<String, Object> r : records) {
            String result = (String) r.get("verificationResult");
            if (result != null && result.contains("正确")) {
                correctCount++;
            } else if (result != null && result.contains("错误")) {
                wrongCount++;
            }

            Object changeObj = r.get("priceChange");
            if (changeObj instanceof Number) {
                double change = ((Number) changeObj).doubleValue();
                totalChange += change;
                maxGain = Math.max(maxGain, change);
                maxLoss = Math.min(maxLoss, change);
            }

            Integer sentiment = (Integer) r.get("sentimentScore");
            if (sentiment != null) {
                if (sentiment > 0) {
                    bullishTotal++;
                    if (result != null && result.contains("正确")) bullishCorrect++;
                } else if (sentiment < 0) {
                    bearishTotal++;
                    if (result != null && result.contains("正确")) bearishCorrect++;
                }
            }
        }

        Map<String, Object> summary = new HashMap<>();
        summary.put("total", total);
        summary.put("correctCount", correctCount);
        summary.put("wrongCount", wrongCount);
        summary.put("accuracyRate", roundToTwoDecimals((double) correctCount / total * 100));
        summary.put("avgPriceChange", roundToTwoDecimals(totalChange / total));
        summary.put("maxGain", roundToTwoDecimals(maxGain == Double.MIN_VALUE ? 0 : maxGain));
        summary.put("maxLoss", roundToTwoDecimals(maxLoss == Double.MAX_VALUE ? 0 : maxLoss));
        summary.put("bullishAccuracy", bullishTotal > 0 ? roundToTwoDecimals((double) bullishCorrect / bullishTotal * 100) : 0.0);
        summary.put("bearishAccuracy", bearishTotal > 0 ? roundToTwoDecimals((double) bearishCorrect / bearishTotal * 100) : 0.0);

        return summary;
    }

    private double roundToTwoDecimals(double value) {
        return BigDecimal.valueOf(value).setScale(2, RoundingMode.HALF_UP).doubleValue();
    }
}
