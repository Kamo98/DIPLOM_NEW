package ru.vkr.vkr.domain.format;

public class MyLangaugeSourceJava implements MyLangaugeSource {

    @Override
    public String getFileName(String source) {
        String[] sourceArray = source.split("\\{+| +|\n+|\r+");
        for (int i = 0; i < sourceArray.length; ++i) {
            if (sourceArray[i].equals("class") && i < sourceArray.length - 1) {
                return sourceArray[i + 1];
            }
        }
        return "source";
    }

    @Override
    public String getFormat() {
        return ".java";
    }
}
