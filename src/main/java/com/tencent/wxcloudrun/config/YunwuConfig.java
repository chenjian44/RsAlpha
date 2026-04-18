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
        return properties.getProperty("yunwu.api.key", "sk-5Oy2J9SEKRky76NIZeihwM3LsqJ9Q1jHWS5HkQUGIfjpeadN");
    }

    public static String getApiUrl() {
        return properties.getProperty("yunwu.api.url", "https://yunwu.ai/v1/chat/completions");
    }

    public static String getModel() {
        return properties.getProperty("yunwu.api.model", "gemini-3.1-flash-lite-preview");
    }

    public static double getTemperature() {
        return Double.parseDouble(properties.getProperty("yunwu.api.temperature", "0.7"));
    }
}
