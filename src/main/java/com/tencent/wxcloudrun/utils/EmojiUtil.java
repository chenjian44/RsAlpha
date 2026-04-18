package com.tencent.wxcloudrun.utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class EmojiUtil {

    // 正则匹配所有Emoji和4字节特殊字符
    private static final Pattern EMOJI_PATTERN = Pattern.compile(
            "[\\uD83C\\uDF00-\\uD83D\\uDDFF]|[\\uD83E\\uDD00-\\uD83E\\uDDFF]|[\\uD83D\\uDE00-\\uD83D\\uDE4F]|[\\uD83D\\uDE80-\\uD83D\\uDEFF]|[\\u2600-\\u26FF]|[\\u2700-\\u27BF]|[\\u1F600-\\u1F64F]|[\\u1F680-\\u1F6FF]|[\\u1F1E0-\\u1F1FF]|[\\u1F900-\\u1F9FF]|[\\u200D]|[\\u2640-\\u2642]|[\\u260E]|[\\u2708]|[\\u2614-\\u2615]|[\\u26F9]|[\\u267F]|[\\u2693]|[\\u23F0]|[\\u23F3]|[\\u2B50]|[\\u2B55]|[\\u3030]|[\\u303D]|[\\u3297]|[\\u3299]",
            Pattern.UNICODE_CASE | Pattern.CASE_INSENSITIVE
    );

    /**
     * 过滤字符串中的Emoji和4字节字符
     */
    public static String filterEmoji(String source) {
        if (source == null || source.isEmpty()) {
            return source;
        }
        Matcher matcher = EMOJI_PATTERN.matcher(source);
        return matcher.replaceAll("").trim();
    }
}
