package ru.vkr.vkr.domain.format;

import java.util.HashMap;
import java.util.Map;

public final class Format {
    private static Map<String, MyLangaugeSource> format;
    static {
        format = new HashMap<>();
        format.put("Java", new MyLangaugeSourceJava());
        format.put("C++", new MyLangaugeSourceCplus());
        format.put("GNU C++", new MyLangaugeSourceCplus());
        format.put("Python", new MyLangaugeSourcePython());
    }

    public static String getName(String language, String source) {
        return format.containsKey(language) ? format.get(language).getFileName(source) : "";
    }

    public static String getFormat(String language) {
        return format.containsKey(language) ? format.get(language).getFormat() : "";
    }
}
