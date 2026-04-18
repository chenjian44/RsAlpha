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
        return getProperty("feishu.webhook.url", "");
    }

    public static boolean isEnabled() {
        return Boolean.parseBoolean(getProperty("feishu.enabled", "false"));
    }

    private static String getProperty(String key, String defaultValue) {
        String value = properties.getProperty(key);
        if (value == null) {
            return defaultValue;
        }
        // 解析环境变量占位符 ${ENV_VAR:default}
        if (value.startsWith("${") && value.endsWith("}")) {
            String envVar = value.substring(2, value.length() - 1);
            String[] parts = envVar.split(":");
            String envKey = parts[0];
            String envDefault = parts.length > 1 ? parts[1] : null;
            
            String envValue = System.getenv(envKey);
            if (envValue != null) {
                return envValue;
            }
            return envDefault != null ? envDefault : defaultValue;
        }
        return value;
    }
}
