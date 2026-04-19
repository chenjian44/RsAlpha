error id: file://<WORKSPACE>/src/main/java/com/tencent/wxcloudrun/utils/TigerKlineUtils.java:com/tigerbrokers/stock/openapi/client/config/ClientConfig#
file://<WORKSPACE>/src/main/java/com/tencent/wxcloudrun/utils/TigerKlineUtils.java
empty definition using pc, found symbol in pc: 
found definition using semanticdb; symbol com/tigerbrokers/stock/openapi/client/config/ClientConfig#
empty definition using fallback
non-local guesses:

offset: 1558
uri: file://<WORKSPACE>/src/main/java/com/tencent/wxcloudrun/utils/TigerKlineUtils.java
text:
```scala
package com.tencent.wxcloudrun.utils;


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
                ClientConfig@@ config = ClientConfig.DEFAULT_CONFIG;
                tigerClient = TigerHttpClient.getInstance().clientConfig(config);
                log.info("Tiger API client initialized successfully using SDK");
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

        // 检查并添加.US后缀
        if (!symbol.contains(".")) {
            symbol = symbol + ".US";
            log.info("Added .US suffix to symbol: {}", symbol);
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

            QuoteKlineRequest request = QuoteKlineRequest.newRequest(Collections.singletonList(symbol), KType.day, beginDateStr,endDateStr);

            QuoteKlineResponse response = client.execute(request);

            if (response.isSuccess()) {
                List<KlineItem> items = response.getKlineItems();
                if (items != null) {
                    for (KlineItem item : items) {
                        Map<String, Object> klineMap = new HashMap<>();
                        klineMap.put("time", item.getItems().get(0).getTime());
                        klineMap.put("open", item.getItems().get(0).getOpen());
                        klineMap.put("high", item.getItems().get(0).getHigh());
                        klineMap.put("low", item.getItems().get(0).getLow());
                        klineMap.put("close", item.getItems().get(0).getClose());
                        klineMap.put("volume", item.getItems().get(0).getVolume());
                        klineList.add(klineMap);
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

```


#### Short summary: 

empty definition using pc, found symbol in pc: 