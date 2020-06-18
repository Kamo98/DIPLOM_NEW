package ru.vkr.vkr.domain;

import java.util.HashMap;
import java.util.Map;

public final class Format {
    private static Map<String, String> format;
    static {
        format = new HashMap<>();
        format.put("Java", ".java");
        format.put("GNU C++", ".cpp");
        format.put("C++", ".cpp");
        format.put("Python", ".py");
    }

    public static String getFormat(String language) {
        return format.getOrDefault(language, null);
    }
}
