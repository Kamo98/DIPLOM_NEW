package ru.vkr.vkr.form;

import java.util.ArrayList;
import java.util.List;

public class SearchProblemForm {
    private List<Boolean> tagList = new ArrayList<>(0);
    private List<Boolean> complexityList = new ArrayList<>(0);
    private Integer  solvabilityStudentFrom = 0;
    private Integer  solvabilityStudentTo = 100;
    private Integer  solvabilitySubmitFrom = 0;
    private Integer  solvabilitySubmitTo = 100;

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

        solvabilityStudentFrom = searchProblemForm.getSolvabilityStudentFrom();
        solvabilityStudentTo = searchProblemForm.getSolvabilityStudentTo();
        solvabilitySubmitFrom = searchProblemForm.getSolvabilitySubmitFrom();
        solvabilitySubmitTo = searchProblemForm.getSolvabilitySubmitTo();
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


    public Integer getSolvabilityStudentFrom() {
        return solvabilityStudentFrom;
    }

    public void setSolvabilityStudentFrom(Integer solvabilityStudentFrom) {
        this.solvabilityStudentFrom = solvabilityStudentFrom;
    }

    public Integer getSolvabilityStudentTo() {
        return solvabilityStudentTo;
    }

    public void setSolvabilityStudentTo(Integer solvabilityStudentTo) {
        this.solvabilityStudentTo = solvabilityStudentTo;
    }

    public Integer getSolvabilitySubmitFrom() {
        return solvabilitySubmitFrom;
    }

    public void setSolvabilitySubmitFrom(Integer solvabilitySubmitFrom) {
        this.solvabilitySubmitFrom = solvabilitySubmitFrom;
    }

    public Integer getSolvabilitySubmitTo() {
        return solvabilitySubmitTo;
    }

    public void setSolvabilitySubmitTo(Integer solvabilitySubmitTo) {
        this.solvabilitySubmitTo = solvabilitySubmitTo;
    }
}
