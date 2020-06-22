package ru.vkr.vkr.entity;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.List;

@Entity
@Table(name = "t_problem")
public class Problem implements Comparable {
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
    private String nameOfTextProblem;

    @Column(nullable = false)
    private String pathToTextProblem;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "teacher_id")
    private Teacher teacherAuthor;


    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "complexity_id")
    private Complexity complexity;

    @Column(nullable = false)
    private boolean publ;


    @OneToMany(mappedBy = "problem", fetch = FetchType.LAZY)
    private List<ChapterProblem> chapters;

//    @ManyToMany(fetch = FetchType.LAZY)
//    @JoinTable(name="t_tag_problem",
//            joinColumns = @JoinColumn(name = "problem_id"),
//            inverseJoinColumns = @JoinColumn(name = "tag_id"))
//    private Set<HashTag> hashTags;

    @OneToMany(mappedBy = "problem", fetch = FetchType.LAZY)
    private List<TagProblem> tagProblems;

    @OneToMany(mappedBy = "problem", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<PerfectSolution> perfectSolutions;


    /*****
    * Поля для статистики
     * Не из БД*/
    transient private Integer acceptedToTotal = 0;
    transient private Long acceptedSubmit = 0L;
    transient private Long totalSubmit = 0L;
    transient private Integer studentAcceptToTotal = 0;
    transient private Long studentAccSubmit = 0L;
    transient private Long totalStudentSubmit = 0L;


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

    public List<Chapter> getChapters() {
        List<Chapter> chapters_ = new ArrayList<>();
        for (ChapterProblem chapterProblem : chapters)
            chapters_.add(chapterProblem.getChapter());
        return chapters_;
    }

    public List<ChapterProblem> getChapterProblems () {
        return chapters;
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

    public List<TagProblem> getTagProblems() {
        return tagProblems;
    }

    public void setTagProblems(List<TagProblem> tagProblems) {
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

    public List<PerfectSolution> getPerfectSolutions() {
        return perfectSolutions;
    }

    public void setPerfectSolutions(List<PerfectSolution> perfectSolutions) {
        this.perfectSolutions = perfectSolutions;
    }

    public void setHashTags(List<TagProblem> tagProblems) {
        this.tagProblems = tagProblems;
    }

    public Complexity getComplexity() {
        return complexity;
    }

    public void setComplexity(Complexity complexity) {
        this.complexity = complexity;
    }

    public boolean isPubl() {
        return publ;
    }

    public void setPubl(boolean publ) {
        this.publ = publ;
    }

    @Transient
    public Long getTotalSubmit() {
        return totalSubmit;
    }

    @Transient
    public void setTotalSubmit(Long totalSubmit) {
        this.totalSubmit = totalSubmit;
    }

    @Transient
    public Long getTotalStudentSubmit() {
        return totalStudentSubmit;
    }
    @Transient
    public void setTotalStudentSubmit(Long totalStudentSubmit) {
        this.totalStudentSubmit = totalStudentSubmit;
    }
    @Transient
    public Integer getAcceptedToTotal() {
        return acceptedToTotal;
    }
    @Transient
    public void setAcceptedToTotal(Integer acceptedToTotal) {
        this.acceptedToTotal = acceptedToTotal;
    }
    @Transient
    public Integer getStudentAcceptToTotal() {
        return studentAcceptToTotal;
    }
    @Transient
    public void setStudentAcceptToTotal(Integer studentAcceptToTotal) {
        this.studentAcceptToTotal = studentAcceptToTotal;
    }
    @Transient
    public Long getAcceptedSubmit() {
        return acceptedSubmit;
    }
    @Transient
    public void setAcceptedSubmit(Long acceptedSubmit) {
        this.acceptedSubmit = acceptedSubmit;
    }
    @Transient
    public Long getStudentAccSubmit() {
        return studentAccSubmit;
    }
    @Transient
    public void setStudentAccSubmit(Long studentAccSubmit) {
        this.studentAccSubmit = studentAccSubmit;
    }

    @Override
    public int compareTo(Object o) {
        Problem pr = (Problem) o;
        if (id < pr.id)
            return -1;
        else if (id > pr.id)
            return 1;
        return 0;
    }
}
