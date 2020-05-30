package ru.vkr.vkr.form;

import java.util.ArrayList;
import java.util.List;

public class SearchProblemForm {
    private List<Boolean> tagList = new ArrayList<>(0);

    public SearchProblemForm(){

    }

    public SearchProblemForm (int countTags) {
        for (int i = 0; i < countTags; i++)
            tagList.add(false);
    }

    public SearchProblemForm (int countTags, SearchProblemForm searchProblemForm) {
        for (int i = 0; i < countTags; i++) {
            tagList.add(false);
            if (i < searchProblemForm.getTagList().size() && searchProblemForm.getTagList().get(i) != null)
                tagList.set(i, searchProblemForm.getTagList().get(i));
        }
    }

    public List<Boolean> getTagList() {
        return tagList;
    }

    public void setTagList(List<Boolean> tagList) {
        this.tagList = tagList;
    }
}
