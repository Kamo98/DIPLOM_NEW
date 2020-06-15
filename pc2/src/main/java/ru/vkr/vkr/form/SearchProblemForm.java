package ru.vkr.vkr.form;

import java.util.ArrayList;
import java.util.List;

public class SearchProblemForm {
    private List<Boolean> tagList = new ArrayList<>(0);
    private List<Boolean> complexityList = new ArrayList<>(0);

    public SearchProblemForm(){

    }

    public SearchProblemForm (int countTags, int countComplexities) {
        for (int i = 0; i < countTags; i++)
            tagList.add(false);
        for (int i = 0; i < countComplexities; i++)
            complexityList.add(false);
    }

    public SearchProblemForm (int countTags, int countComplexities, SearchProblemForm searchProblemForm) {
        for (int i = 0; i < countTags; i++) {
            tagList.add(false);
            if (i < searchProblemForm.getTagList().size() && searchProblemForm.getTagList().get(i) != null)
                tagList.set(i, searchProblemForm.getTagList().get(i));
        }

        for (int i = 0; i < countComplexities; i++) {
            complexityList.add(false);
            if (i < searchProblemForm.getComplexityList().size() && searchProblemForm.getComplexityList().get(i) != null)
                complexityList.set(i, searchProblemForm.getComplexityList().get(i));
        }
    }

    public List<Boolean> getTagList() {
        return tagList;
    }

    public List<Boolean> getComplexityList() {
        return complexityList;
    }

    public void setTagList(List<Boolean> tagList) {
        this.tagList = tagList;
    }
}
