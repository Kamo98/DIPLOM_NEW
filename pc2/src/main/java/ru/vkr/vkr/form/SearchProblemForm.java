package ru.vkr.vkr.form;

import java.util.ArrayList;
import java.util.List;

public class SearchProblemForm {
    private List<Boolean> tagList = new ArrayList<>(0);

    public SearchProblemForm (int countTags) {
        for (int i = 0; i < countTags; i++)
            getTagList().add(false);
    }

    public List<Boolean> getTagList() {
        return tagList;
    }

    public void setTagList(List<Boolean> tagList) {
        this.tagList = tagList;
    }
}
