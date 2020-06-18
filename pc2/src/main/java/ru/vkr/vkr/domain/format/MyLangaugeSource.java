package ru.vkr.vkr.domain.format;

public interface MyLangaugeSource {
    default String getFileName(String source) {
        return "source";
    }

    String getFormat();
}
