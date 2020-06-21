package ru.vkr.vkr.entity;

import ru.vkr.vkr.entity.api.PersonRegisterData;

import javax.persistence.*;
import java.util.Set;
import java.util.List;

@Entity
@Table(name = "t_teacher")
public class Teacher implements PersonRegisterData {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(nullable = false)
    private String surname;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String middleName;

    @OneToOne(fetch = FetchType.LAZY)
    private User user;

    // курсы, созданные преподавателем
    @OneToMany(mappedBy = "teacherAuthor", fetch = FetchType.LAZY)
    private Set<Course> courses;

    // задачи, созданные преподавателем
    @OneToMany(mappedBy = "teacherAuthor", fetch = FetchType.LAZY)
    private List<Problem> problems;


    public void setId(Long id) {
        this.id = id;
    }

    public void setSurname(String surname) {
        this.surname = surname;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setMiddleName(String middleName) {
        this.middleName = middleName;
    }

    public Long getId() {
        return id;
    }

    public String getSurname() {
        return surname;
    }

    public String getName() {
        return name;
    }

    public String getMiddleName() {
        return middleName;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Set<Course> getCourses() {
        return courses;
    }

    public void setCourses(Set<Course> courses) {
        this.courses = courses;
    }

    public List<Problem> getProblems() {
        return problems;
    }

    public void setProblems(List<Problem> problems) {
        this.problems = problems;
    }
}
