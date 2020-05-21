package ru.vkr.vkr.domain;

public enum ROLE {
    ROLE_ADMIN(1L),
    ROLE_TEACHER(2L),
    ROLE_STUDENT(3L);

    private Long id;
    private ROLE(Long id){
        this.id = id;
    }

    public Long getId() {return id;}
};
