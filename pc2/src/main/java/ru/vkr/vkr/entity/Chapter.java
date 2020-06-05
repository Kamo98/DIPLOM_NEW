package ru.vkr.vkr.entity;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.Set;

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
    @ManyToMany
    @JoinTable(name = "t_chapter_problems",
            joinColumns = @JoinColumn(name = "chapter_id"),
    inverseJoinColumns = @JoinColumn(name = "problem_id"))
    private Set<Problem> chapterProblems;

    // Теория темы
    @ManyToMany(cascade = CascadeType.REFRESH)
    @JoinTable(name = "t_chapter_theories",
            joinColumns = @JoinColumn(name = "chapter_id"),
            inverseJoinColumns = @JoinColumn(name = "theory_id"))
    private Set<Theory> chapterTheories;

    public Set<Theory> getChapterTheories() {
        return chapterTheories;
    }

    public Set<Problem> getChapterProblems() {
        return chapterProblems;
    }

    public void setChapterProblems(Set<Problem> chapterProblems) {
        this.chapterProblems = chapterProblems;
    }

    public void setChapterTheories(Set<Theory> chapterTheories) {
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

