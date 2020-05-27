package ru.vkr.vkr.entity;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.Set;

@Table
@Entity
public class Chapter {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @NotNull
    @Column(nullable = false)
    @Size(min=5, max=250, message = "Наименование курса должно содержать более 5 и менее 250 символов")
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

    public Set<Problem> getProblems() {
        return chapterProblems;
    }

    public void setProblems(Set<Problem> problems) {
        this.chapterProblems = problems;
    }
}

