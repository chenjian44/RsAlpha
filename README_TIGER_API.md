# 老虎开放平台API集成说明

## 项目结构

本项目集成了老虎开放平台的Java SDK，用于拉取股票的历史行情数据。主要包含以下文件：

1. **pom.xml** - 添加了老虎开放平台Java SDK和Fastjson依赖
2. **src/main/resources/tiger_openapi_config.properties** - 老虎开放平台API配置文件
3. **src/main/java/com/tencent/wxcloudrun/config/TigerConfig.java** - 老虎开放平台配置管理类
4. **src/main/java/com/tencent/wxcloudrun/service/TigerQuoteService.java** - 行情服务类
5. **src/main/java/com/tencent/wxcloudrun/utils/TigerQuoteUtils.java** - 行情工具类

## 使用步骤

### 1. 配置API密钥

编辑 `src/main/resources/tiger_openapi_config.properties` 文件，填写您的老虎开放平台API密钥：

```properties
# 开发者ID
tiger.id=your_tiger_id

# API密钥
private.key=your_private_key
```

### 2. 拉取历史行情数据

使用 `TigerQuoteUtils` 类来拉取股票的历史行情数据，示例代码：

```java
import com.tigerbrokers.stock.openapi.client.https.response.quote.QuoteKlineResponse;
import com.tigerbrokers.stock.openapi.client.struct.enums.KType;

public class Example {
    public static void main(String[] args) {
        // 示例：获取AAPL从2023-01-01到2023-12-31的日线数据
        String symbol = "AAPL";
        KType kType = KType.day;
        String beginTime = "2023-01-01";
        String endTime = "2023-12-31";

        QuoteKlineResponse response = TigerQuoteUtils.getStockHistoryData(symbol, kType, beginTime, endTime);

        if (response.isSuccess()) {
            System.out.println("获取历史行情数据成功！");
            System.out.println("数据条数：" + response.getKlineItems().size());
            // 处理数据...
        } else {
            System.out.println("获取历史行情数据失败：" + response.getMessage());
        }
    }
}
```

### 3. 支持的K线类型

- `KType.min1` - 1分钟K线
- `KType.min5` - 5分钟K线
- `KType.min15` - 15分钟K线
- `KType.min30` - 30分钟K线
- `KType.min60` - 60分钟K线
- `KType.day` - 日线
- `KType.week` - 周线
- `KType.month` - 月线

### 4. 注意事项

1. 您需要在老虎开放平台上注册并获取API密钥
2. 某些行情数据可能需要订阅相应的权限
3. 请遵守老虎开放平台的API使用规范，避免过度调用

## 参考文档

- [老虎开放平台官方文档](https://quant.itigerup.com/openapi/zh/python/operation/subscribe/subscribeList.html)
- [老虎开放平台Java SDK GitHub仓库](https://github.com/tigerfintech/openapi-java-sdk)
