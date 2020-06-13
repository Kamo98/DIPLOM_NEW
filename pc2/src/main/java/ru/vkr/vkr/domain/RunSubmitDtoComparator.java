package ru.vkr.vkr.domain;

import java.util.Comparator;

public class RunSubmitDtoComparator implements Comparator<RunSubmitDto> {
    @Override
    public int compare(RunSubmitDto first, RunSubmitDto second) {
        return -Integer.compare(first.getNumber(), second.getNumber());
    }
}
