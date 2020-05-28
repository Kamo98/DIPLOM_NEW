package ru.vkr.vkr.entity;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.Set;

@Entity
@Table(name = "t_problem")
public class Problem {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @NotNull
    @Column(nullable = false)
    private String name;

    @NotNull
    @Column(nullable = false)
    private Integer timeLimit;

    @NotNull
    @Column(nullable = false)
    private Integer memoryLimit;

    @Column(nullable = false)
    private String pathToTextProblem;

    @Column
    private Long numElementId;      //Для адекватного поиска задач в PC2

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "teacher_id")
    private Teacher teacherAuthor;

    @ManyToMany(mappedBy = "chapterProblems", fetch = FetchType.LAZY)
    private Set<Chapter> chapters;


    public Set<Chapter> getChapters() {
        return chapters;
    }

    public void setChapters(Set<Chapter> chapters) {
        this.chapters = chapters;
    }

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name="t_tag_problem",
            joinColumns = @JoinColumn(name = "problem_id"),
            inverseJoinColumns = @JoinColumn(name = "tag_id"))
    private Set<HashTag> hashTags;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }


    public Integer getTimeLimit() {
        return timeLimit;
    }

    public void setTimeLimit(Integer timeLimit) {
        this.timeLimit = timeLimit;
    }

    public Integer getMemoryLimit() {
        return memoryLimit;
    }

    public void setMemoryLimit(Integer memoryLimit) {
        this.memoryLimit = memoryLimit;
    }


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getNumElementId() {
        return numElementId;
    }

    public void setNumElementId(Long numElementId) {
        this.numElementId = numElementId;
    }

    public Teacher getTeacherAuthor() {
        return teacherAuthor;
    }

    public void setTeacherAuthor(Teacher teacherAuthor) {
        this.teacherAuthor = teacherAuthor;
    }

    public Set<HashTag> getHashTags() {
        return hashTags;
    }

    public void setHashTags(Set<HashTag> hashTags) {
        this.hashTags = hashTags;
    }
}
