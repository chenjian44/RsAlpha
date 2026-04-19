package com.tencent.wxcloudrun.utils;


import com.alibaba.fastjson.JSON;
import com.tigerbrokers.stock.openapi.client.config.ClientConfig;
import com.tigerbrokers.stock.openapi.client.https.client.TigerHttpClient;
import com.tigerbrokers.stock.openapi.client.https.domain.quote.item.KlineItem;
import com.tigerbrokers.stock.openapi.client.https.request.quote.QuoteKlineRequest;
import com.tigerbrokers.stock.openapi.client.https.response.quote.QuoteKlineResponse;
import com.tigerbrokers.stock.openapi.client.struct.enums.KType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalField;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class TigerKlineUtils {

    private static final Logger log = LoggerFactory.getLogger(TigerKlineUtils.class);
    private static final long CACHE_EXPIRE_MILLIS = 24 * 60 * 60 * 1000; // 24小时缓存

    private static final ConcurrentHashMap<String, CacheEntry<List<Map<String, Object>>>> yearlyKlineCache = new ConcurrentHashMap<>();
    private static TigerHttpClient tigerClient;

    private static class CacheEntry<T> {
        final T data;
        final long expireTime;

        CacheEntry(T data, long expireTime) {
            this.data = data;
            this.expireTime = expireTime;
        }

        boolean isExpired() {
            return System.currentTimeMillis() > expireTime;
        }
    }

    public static synchronized TigerHttpClient getTigerClient() {
        if (tigerClient == null) {
            try {
                ClientConfig config = ClientConfig.DEFAULT_CONFIG;
                config.configFilePath = "src/main/resources/";
                tigerClient = TigerHttpClient.getInstance().clientConfig(config);
            } catch (Exception e) {
                log.error("Failed to initialize Tiger API client: {}", e.getMessage(), e);
            }
        }
        return tigerClient;
    }

    public static List<Map<String, Object>> getKlineData(String symbol) {
        return getKlineData(symbol, 1); // 固定查询1年
    }

    public static List<Map<String, Object>> getKlineData(String symbol, int years) {
        if (years != 1) {
            log.warn("Only 1 year of data is supported, using 1 year instead of {}", years);
        }


        String cacheKey = symbol;
        CacheEntry<List<Map<String, Object>>> cachedEntry = yearlyKlineCache.get(cacheKey);

        if (cachedEntry != null && !cachedEntry.isExpired()) {
            log.info("Cache hit for symbol: {}, using cached yearly Kline data", symbol);
            return cachedEntry.data;
        }

        log.info("Cache miss for symbol: {}, fetching yearly Kline data", symbol);
        List<Map<String, Object>> klineList = fetchYearlyKlineFromApi(symbol, 1);

        if (!klineList.isEmpty()) {
            yearlyKlineCache.put(cacheKey, new CacheEntry<>(klineList, System.currentTimeMillis() + CACHE_EXPIRE_MILLIS));
            log.info("Yearly Kline data cached for symbol: {}", symbol);
        }

        return klineList;
    }

    private static List<Map<String, Object>> fetchYearlyKlineFromApi(String symbol, int years) {
        List<Map<String, Object>> klineList = new ArrayList<>();

        try {
            TigerHttpClient client = getTigerClient();
            if (client == null) {
                log.error("Tiger client is not initialized");
                return klineList;
            }

            LocalDate endDate = LocalDate.now();
            LocalDate beginDate = endDate.minusYears(years);
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            String beginDateStr = beginDate.format(formatter);
            String endDateStr = endDate.format(formatter);

            log.info("Fetching yearly Kline data for symbol: {}, from: {} to: {}", 
                    symbol, beginDateStr, endDateStr);

            QuoteKlineRequest request = QuoteKlineRequest.newRequest(Collections.singletonList(symbol), KType.day, beginDate.toString(),endDate.toString());

            QuoteKlineResponse response = client.execute(request);

            log.info("QuoteKlineResponse response :{}" , JSON.toJSONString(response));

            if (response.isSuccess()) {
                List<KlineItem> items = response.getKlineItems();
                if (items != null) {
                    for (KlineItem item : items) {
                        // 遍历每个 KlineItem 中的所有 items 元素
                        if (item.getItems() != null && !item.getItems().isEmpty()) {
                            for (com.tigerbrokers.stock.openapi.client.https.domain.quote.item.KlinePoint point : item.getItems()) {
                                Map<String, Object> klineMap = new HashMap<>();
                                // 将时间从毫秒转换为秒
                                klineMap.put("time", point.getTime() / 1000);
                                klineMap.put("open", point.getOpen());
                                klineMap.put("high", point.getHigh());
                                klineMap.put("low", point.getLow());
                                klineMap.put("close", point.getClose());
                                klineMap.put("volume", point.getVolume());
                                // 添加 amount 字段（如果存在）
                                if (point.getAmount() != null) {
                                    klineMap.put("amount", point.getAmount());
                                }
                                klineList.add(klineMap);
                            }
                        }
                    }
                }
                log.info("Successfully retrieved {} kline records for symbol: {}", klineList.size(), symbol);
            } else {
                log.error("Failed to get kline data, error: {}", response.getMessage());
            }

        } catch (Exception e) {
            log.error("Error getting yearly kline data for symbol: {}, error: {}", symbol, e.getMessage(), e);
        }

        return klineList;
    }

    public static void clearCache() {
        yearlyKlineCache.clear();
        log.info("Yearly Kline cache cleared");
    }

    public static void clearCache(String symbol) {
        yearlyKlineCache.remove(symbol);
        log.info("Yearly Kline cache cleared for symbol: {}", symbol);
    }

    public static void resetClient() {
        tigerClient = null;
        log.info("Tiger API client reset");
    }
}
