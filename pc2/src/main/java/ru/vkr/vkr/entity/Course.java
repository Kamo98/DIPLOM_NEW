package ru.vkr.vkr.entity;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.Collection;
import java.util.Set;
import java.util.List;

@Entity
@Table(name="t_course")
public class Course {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @NotNull
    @Column(nullable = false)
    @Size(min=5, max=250, message = "Наименование курса должно содержать более 5 и менее 250 символов")
    private String name;

    // Преподаватель курса
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "teacher_id")
    private Teacher teacherAuthor;

    // Подписчики(группы) курса
    @OneToMany(mappedBy = "courseSubscriptions", fetch = FetchType.LAZY)
    private List<Group> subscribers;

    // Подписчики(группы) курса
    @OneToMany(mappedBy = "courseChapters",
            fetch = FetchType.LAZY,
            cascade = CascadeType.ALL)
    private List<Chapter> chapters;

    public List<Chapter> getChapters() {
        return chapters;
    }

    public void setChapters(List<Chapter> chapters) {
        this.chapters = chapters;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Teacher getTeacherAuthor() {
        return teacherAuthor;
    }

    public void setTeacherAuthor(Teacher teacherAuthor) {
        this.teacherAuthor = teacherAuthor;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    @Override
    public String toString() {
        return id + ": " + name + " by " + teacherAuthor.getSurname();
    }


    public List<Group> getSubscribers() {
        return subscribers;
    }

    public void setSubscribers(List<Group> subscribers) {
        this.subscribers = subscribers;
    }
}
