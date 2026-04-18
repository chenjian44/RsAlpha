package com.tencent.wxcloudrun.config;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class YunwuConfig {
    private static final Properties properties = new Properties();

    static {
        try (InputStream input = YunwuConfig.class.getClassLoader().getResourceAsStream("yunwu_api_config.properties")) {
            if (input != null) {
                properties.load(input);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static String getApiKey() {
        return getProperty("yunwu.api.key", "");
    }

    public static String getApiUrl() {
        return getProperty("yunwu.api.url", "https://yunwu.ai/v1/chat/completions");
    }

    public static String getModel() {
        return getProperty("yunwu.api.model", "gemini-3.1-flash-lite-preview");
    }

    public static double getTemperature() {
        return Double.parseDouble(getProperty("yunwu.api.temperature", "0.0"));
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
