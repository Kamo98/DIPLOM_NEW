package ru.vkr.vkr.form;


import org.springframework.web.multipart.MultipartFile;

public class LoadTestsForm {
    private String extensionIn;
    private String extensionAns;
    private MultipartFile[] dirTests;

    public MultipartFile[] getDirTests() {
        return dirTests;
    }

    public void setDirTests(MultipartFile[] dirTests) {
        this.dirTests = dirTests;
    }

    public String getExtensionIn() {
        return extensionIn;
    }

    public void setExtensionIn(String extensionIn) {
        this.extensionIn = extensionIn;
    }

    public String getExtensionAns() {
        return extensionAns;
    }

    public void setExtensionAns(String extensionAns) {
        this.extensionAns = extensionAns;
    }
}
