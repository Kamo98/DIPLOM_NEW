package ru.vkr.vkr.entity;

import javax.persistence.*;
import javax.validation.constraints.NotNull;


@Entity
@Table(name = "t_chapter_problems")
public class ChapterProblem {
    @EmbeddedId
    private ChapterProblemKey id;

    @Column(name = "perfect_solution_available")
    private boolean perfectSolutionAvailable;

    @ManyToOne
    @MapsId("chapter_id")
    @JoinColumn(name = "chapter_id")
    private Chapter chapter;

    @ManyToOne
    @MapsId("problem_id")
    @JoinColumn(name = "problem_id")
    private Problem problem;



    public Problem getProblem() {
        return problem;
    }

    public void setProblem(Problem problem) {
        this.problem = problem;
    }


    public boolean isPerfectSolutionAvailable() {
        return perfectSolutionAvailable;
    }

    public void setPerfectSolutionAvailable(boolean perfectSolutionAvailable) {
        this.perfectSolutionAvailable = perfectSolutionAvailable;
    }

    public Chapter getChapter() {
        return chapter;
    }

    public void setChapter(Chapter chapter) {
        this.chapter = chapter;
    }

    public ChapterProblemKey getId() {
        return id;
    }

    public void setId(ChapterProblemKey id) {
        this.id = id;
    }
}
