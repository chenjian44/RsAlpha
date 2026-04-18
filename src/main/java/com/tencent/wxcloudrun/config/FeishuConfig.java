package com.tencent.wxcloudrun.config;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class FeishuConfig {

    private static final Properties properties = new Properties();

    static {
        try (InputStream input = FeishuConfig.class.getClassLoader().getResourceAsStream("feishu_config.properties")) {
            if (input != null) {
                properties.load(input);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static String getWebhookUrl() {
        return properties.getProperty("feishu.webhook.url", "");
    }

    public static boolean isEnabled() {
        return Boolean.parseBoolean(properties.getProperty("feishu.enabled", "false"));
    }
}
