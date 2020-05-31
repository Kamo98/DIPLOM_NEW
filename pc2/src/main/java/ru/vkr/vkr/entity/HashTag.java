package ru.vkr.vkr.entity;

import com.sun.naming.internal.FactoryEnumeration;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;
import java.util.List;

@Entity
@Table(name="t_tag")
public class HashTag {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @NotNull
    @Column(nullable = false)
    private String name;

    //Для подсчёта уровня вложенности при dfs(нет в базе)
    transient private Integer level;

    //Родителский тег
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "parent_id")
    private HashTag parent;

    //Дочерние теги (пока не используются и видимо не будут)
//    @OneToMany(mappedBy = "parent", fetch = FetchType.LAZY)
//    private Set<HashTag> children;

    //Задачи, к которым прикреплён
//    @ManyToMany(mappedBy = "hashTags", fetch = FetchType.LAZY)
//    private List<Problem> problems;
    @OneToMany(mappedBy = "hashTag", fetch = FetchType.LAZY)
    private Set<TagProblem> tagProblems;

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


    public HashTag getParent() {
        return parent;
    }

    public void setParent(HashTag parent) {
        this.parent = parent;
    }

    @Transient
    public Integer getLevel() {
        return level;
    }

    @Transient
    public void setLevel(Integer level) {
        this.level = level;
    }

    public Set<TagProblem> getTagProblems() {
        return tagProblems;
    }

    public void setTagProblems(Set<TagProblem> tagProblems) {
        this.tagProblems = tagProblems;
    }

    public List<Problem> getProblems() {
        List<Problem> problems = new ArrayList<>();
        for(TagProblem tagProblem : tagProblems)
            problems.add(tagProblem.getProblem());
        return  problems;
    }


    //Связь с задачей
}
