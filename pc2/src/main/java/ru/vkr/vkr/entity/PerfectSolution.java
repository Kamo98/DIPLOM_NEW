package ru.vkr.vkr.entity;


import javax.persistence.*;

@Entity
@Table(name = "t_perfect_solution")
public class PerfectSolution {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;

    @Column(nullable = false)
    private String source;

    @Column(nullable = false)
    private String language;

    @ManyToOne
    private Problem problem;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public Problem getProblem() {
        return problem;
    }

    public void setProblem(Problem problem) {
        this.problem = problem;
    }
}
