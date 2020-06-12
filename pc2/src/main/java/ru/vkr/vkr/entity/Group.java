package ru.vkr.vkr.entity;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.Collection;
import java.util.Set;
import java.util.List;

@Entity
@Table(name="t_group")
public class Group {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @NotNull
    @Column(nullable = false)
    @Size(min=5, max=250, message = "Наименование группы должно содержать более 5 и менее 250 символов")
    private String name;

    // Преподаватель группы
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "teacher_id")
    private Teacher teacherOwner;

    // Подписки на курсы
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "course_id")
    private Course courseSubscriptions;

    // Студенты группы
    @OneToMany(mappedBy = "group", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<Student> students;

    public Group() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Teacher getTeacherOwner() {
        return teacherOwner;
    }

    public void setTeacherOwner(Teacher teacherOwner) {
        this.teacherOwner = teacherOwner;
    }

    @Override
    public String toString() {
        return "id = " + id + "  name = " + name;
    }

    public Course getCourseSubscriptions() {
        return courseSubscriptions;
    }

    public void setCourseSubscriptions(Course courseSubscriptions) {
        this.courseSubscriptions = courseSubscriptions;
    }

    public List<Student> getStudents() {
        return students;
    }

    public void setStudents(List<Student> students) {
        this.students = students;
    }
}
