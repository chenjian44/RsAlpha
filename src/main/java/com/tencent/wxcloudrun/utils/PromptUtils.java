package com.tencent.wxcloudrun.utils;

import java.io.IOException;
import java.io.InputStream;
import java.util.Scanner;

public class PromptUtils {

    public static String readSystemPrompt() {
        try (InputStream input = PromptUtils.class.getClassLoader().getResourceAsStream("prompts/system_prompt.txt")) {
            if (input != null) {
                Scanner scanner = new Scanner(input).useDelimiter("\\A");
                return scanner.hasNext() ? scanner.next() : "";
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "";
    }
}
