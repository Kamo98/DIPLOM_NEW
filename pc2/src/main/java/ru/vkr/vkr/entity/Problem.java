package ru.vkr.vkr.entity;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.HashSet;
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

    @Column
    private String nameOfTextProblem;

    @Column
    private String pathToTextProblem;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "teacher_id")
    private Teacher teacherAuthor;

    @ManyToMany(mappedBy = "chapterProblems", fetch = FetchType.LAZY)
    private Set<Chapter> chapters;

//    @ManyToMany(fetch = FetchType.LAZY)
//    @JoinTable(name="t_tag_problem",
//            joinColumns = @JoinColumn(name = "problem_id"),
//            inverseJoinColumns = @JoinColumn(name = "tag_id"))
//    private Set<HashTag> hashTags;

    @OneToMany(mappedBy = "problem", fetch = FetchType.LAZY)
    private Set<TagProblem> tagProblems;

    /*****
    * Поля для статистики
     * Не из БД*/
    //Отношение кол-ва усепшных сдач к общему (%)
    transient private Long countAccepted = 0L;
    transient private Long totalSubmit = 0L;


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

    public String getNameOfTextProblem() {
        return nameOfTextProblem;
    }

    public void setNameOfTextProblem(String nameOfTextProblem) {
        this.nameOfTextProblem = nameOfTextProblem;
    }

    public String getPathToTextProblem() {
        return pathToTextProblem;
    }

    public void setPathToTextProblem(String pathToTextProblem) {
        this.pathToTextProblem = pathToTextProblem;
    }

    public Set<Chapter> getChapters() {
        return chapters;
    }

    public void setChapters(Set<Chapter> chapters) {
        this.chapters = chapters;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Teacher getTeacherAuthor() {
        return teacherAuthor;
    }

    public void setTeacherAuthor(Teacher teacherAuthor) {
        this.teacherAuthor = teacherAuthor;
    }

    public Set<TagProblem> getTagProblems() {
        return tagProblems;
    }

    public void setTagProblems(Set<TagProblem> tagProblems) {
        this.tagProblems = tagProblems;
    }

    public Set<HashTag> getHashTags() {
        Set<HashTag> hashTags = new HashSet<>();
        for(TagProblem tagProblem : tagProblems)
            hashTags.add(tagProblem.getHashTag());
        return hashTags;
    }

    //Для вывода тегов пользователю
    public Set<HashTag> getHashTagsVisible() {
        Set<HashTag> hashTags = new HashSet<>();
        for(TagProblem tagProblem : tagProblems)
            if (tagProblem.isVisible())
                hashTags.add(tagProblem.getHashTag());
        return hashTags;
    }


    public void setHashTags(Set<TagProblem> tagProblems) {
        this.tagProblems = tagProblems;
    }


    @Transient
    public Long getCountAccepted() {
        return countAccepted;
    }
    @Transient
    public void setCountAccepted(Long countAccepted) {
        this.countAccepted = countAccepted;
    }

    @Transient
    public Long getTotalSubmit() {
        return totalSubmit;
    }

    @Transient
    public void setTotalSubmit(Long totalSubmit) {
        this.totalSubmit = totalSubmit;
    }
}
