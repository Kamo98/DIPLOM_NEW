package ru.vkr.vkr.entity;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import java.io.Serializable;

@Embeddable
public class ChapterProblemKey implements Serializable {


    @Column(name = "chapter_id")
    private Long chapterId;

    @Column(name = "problem_id")
    private Long problemId;


    public Long getChapterId() {
        return chapterId;
    }

    public void setChapterId(Long chapterId) {
        this.chapterId = chapterId;
    }

    public Long getProblemId() {
        return problemId;
    }

    public void setProblemId(Long problemId) {
        this.problemId = problemId;
    }
}
