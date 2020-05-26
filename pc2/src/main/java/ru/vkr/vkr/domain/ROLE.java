package ru.vkr.vkr.domain;

public enum ROLE {
    ROLE_ADMIN(1L),
    ROLE_TEACHER(2L, "ADMINISTRATOR"),
    ROLE_STUDENT(3L, "TEAM");

    private Long id;
    private String rolePc2;

    private ROLE(Long id){
        this.id = id;
    }

    private ROLE(Long id, String rolePc2) {
        this.id = id;
        this.rolePc2 = rolePc2;
    }

    public Long getId() {return id;}

    public String getRolePc2() {
        return rolePc2;
    }
};
