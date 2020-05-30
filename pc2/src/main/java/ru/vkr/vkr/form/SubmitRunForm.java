package ru.vkr.vkr.form;

import org.springframework.web.multipart.MultipartFile;

public class SubmitRunForm {
    private int language;

    private int problem;

    private MultipartFile multipartFile;

    public int getProblem() {
        return problem;
    }

    public void setProblem(int problem) {
        this.problem = problem;
    }

    public int getLanguage() {
        return language;
    }

    public void setLanguage(int language) {
        this.language = language;
    }

    public MultipartFile getMultipartFile() {
        return multipartFile;
    }

    public void setMultipartFile(MultipartFile multipartFile) {
        this.multipartFile = multipartFile;
    }
}
