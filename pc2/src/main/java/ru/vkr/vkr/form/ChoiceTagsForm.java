package ru.vkr.vkr.form;

import ru.vkr.vkr.entity.HashTag;

import java.util.Set;

public class ChoiceTagsForm {
    private Set<HashTag> tagList;

    public Set<HashTag> getTagList() {
        return tagList;
    }

    public void setTagList(Set<HashTag> tagList) {
        this.tagList = tagList;
    }
}
