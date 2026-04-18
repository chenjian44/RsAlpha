package com.tencent.wxcloudrun.utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class EmojiUtil {

    // 仅仅匹配所有 UTF-16 的代理区字符（即所有超出 BMP 平面、需要 4 字节存储的字符）
    private static final Pattern FOUR_BYTE_PATTERN = Pattern.compile("[\\ud800-\\udfff]");

    /**
     * 过滤字符串中无法被 MySQL utf8 兼容的 4 字节字符（包含所有 Emoji）
     */
    public static String filterEmoji(String source) {
        if (source == null || source.isEmpty()) {
            return source;
        }

        Matcher matcher = FOUR_BYTE_PATTERN.matcher(source);
        // 将所有 4 字节字符替换为空字符串
        return matcher.replaceAll("").trim();
    }

    // 如果你希望将表情替换为某个占位符（比如 "[表情]"），可以使用下面这个方法
    public static String replaceEmoji(String source) {
        if (source == null || source.isEmpty()) {
            return source;
        }
        Matcher matcher = FOUR_BYTE_PATTERN.matcher(source);
        return matcher.replaceAll("[表情]").trim();
    }
}