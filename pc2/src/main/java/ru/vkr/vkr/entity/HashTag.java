package ru.vkr.vkr.entity;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.util.HashMap;
import java.util.Set;

@Entity
@Table(name="t_tag")
public class HashTag {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @NotNull
    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private Integer level;

    //Родителский тег
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "parent_id")
    private HashTag parent;

    //Дочерние теги (пока не используются и видимо не будут)
//    @OneToMany(mappedBy = "parent", fetch = FetchType.LAZY)
//    private Set<HashTag> children;

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

    public Integer getLevel() {
        return level;
    }

    public void setLevel(Integer level) {
        this.level = level;
    }

    public HashTag getParent() {
        return parent;
    }

    public void setParent(HashTag parent) {
        this.parent = parent;
    }

    //Связь с задачей
}
