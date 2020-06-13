package ru.vkr.vkr.domain;

public class RunSubmitDto {
    private int hiddenNumber;
    private int number;
    private String displayName;
    private Long idProblem;
    private long time;
    private String langauge;
    private String status;


    public RunSubmitDto(int hiddenNumber, int number, String displayName, long time, String langauge, String status, Long idProblem) {
        this.hiddenNumber = hiddenNumber;
        this.number = number;
        this.displayName = displayName;
        this.time = time;
        this.langauge = langauge;
        this.status = status;
        this.idProblem = idProblem;
    }

    public int getNumber() {
        return number;
    }

    public void setNumber(int number) {
        this.number = number;
    }

    public RunSubmitDto(String displayName, int time, String langauge, String status) {
        this.displayName = displayName;
        this.time = time;
        this.langauge = langauge;
        this.status = status;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public String getLangauge() {
        return langauge;
    }

    public void setLangauge(String langauge) {
        this.langauge = langauge;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public int getHiddenNumber() {
        return hiddenNumber;
    }

    public void setHiddenNumber(int hiddenNumber) {
        this.hiddenNumber = hiddenNumber;
    }

    public Long getIdProblem() {
        return idProblem;
    }

    public void setIdProblem(Long idProblem) {
        this.idProblem = idProblem;
    }
}
