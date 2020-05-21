package ru.vkr.vkr.entity;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.Collection;
import java.util.Set;

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

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "teacher_id")
    private Teacher teacherOwner;


    //Подписки на курсы
    @ManyToMany(mappedBy = "subscribers")
    private Set<Course> courseSubscriptions;

    //todo: сейчас при удалении группы удаляются все студенты вместе с учётками
    @OneToMany(mappedBy = "group", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private Set<Student> students;

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

    public Set<Course> getCourseSubscriptions() {
        return courseSubscriptions;
    }

    public void setCourseSubscriptions(Set<Course> courseSubscriptions) {
        this.courseSubscriptions = courseSubscriptions;
    }

    public Set<Student> getStudents() {
        return students;
    }

    public void setStudents(Set<Student> students) {
        this.students = students;
    }
}
