package ru.vkr.vkr.entity;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.ArrayList;
import java.util.Set;
import java.util.List;

@Table(name = "t_chapter")
@Entity
public class Chapter {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @NotNull
    @Column(nullable = false)
    @Size(min=5, max=250, message = "Наименование темы должно содержать более 5 и менее 250 символов")
    private String name;

    // Курс темы
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "course_id")
    private Course courseChapters;

    // Задачи темы
    @OneToMany(mappedBy = "chapter")
    private List<ChapterProblem> chapterProblems;

    // Теория темы
    @ManyToMany(cascade = CascadeType.REFRESH)
    @JoinTable(name = "t_chapter_theories",
            joinColumns = @JoinColumn(name = "chapter_id"),
            inverseJoinColumns = @JoinColumn(name = "theory_id"))
    private List<Theory> chapterTheories;

    public List<Theory> getChapterTheories() {
        return chapterTheories;
    }

    public List<Problem> getChapterProblems() {
        List<Problem> problems = new ArrayList<>();
        for (ChapterProblem chapterProblem : chapterProblems)
            problems.add(chapterProblem.getProblem());
        return problems;
    }

    public void addChapterProblem(ChapterProblem chapterProblem){
        chapterProblems.add(chapterProblem);
    }

    public void deleteChapterProblem(ChapterProblem chapterProblem) {
        chapterProblems.remove(chapterProblem);
    }

    public void setChapterTheories(List<Theory> chapterTheories) {
        this.chapterTheories = chapterTheories;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Course getCourseChapters() {
        return courseChapters;
    }

    public void setCourseChapters(Course courseChapters) {
        this.courseChapters = courseChapters;
    }
}

