package ru.vkr.vkr.form;

public class CheckerSettingsForm {
    private boolean clicsValidator;
    private boolean caseSensitive;
    private boolean spaceSensitive;

    private boolean floatAbsoluteTolerance;
    private double absoluteTolerance;


    public boolean isCaseSensitive() {
        return caseSensitive;
    }

    public void setCaseSensitive(boolean caseSensitive) {
        this.caseSensitive = caseSensitive;
    }

    public boolean isSpaceSensitive() {
        return spaceSensitive;
    }

    public void setSpaceSensitive(boolean spaceSensitive) {
        this.spaceSensitive = spaceSensitive;
    }

    public boolean isClicsValidator() {
        return clicsValidator;
    }

    public void setClicsValidator(boolean clicsValidator) {
        this.clicsValidator = clicsValidator;
    }


    public boolean isFloatAbsoluteTolerance() {
        return floatAbsoluteTolerance;
    }

    public void setFloatAbsoluteTolerance(boolean floatAbsoluteTolerance) {
        this.floatAbsoluteTolerance = floatAbsoluteTolerance;
    }

    public double getAbsoluteTolerance() {
        return absoluteTolerance;
    }

    public void setAbsoluteTolerance(double absoluteTolerance) {
        this.absoluteTolerance = absoluteTolerance;
    }

}
