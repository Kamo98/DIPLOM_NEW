package ru.vkr.vkr.form;

public class CheckerSettingsForm {

    public static final Integer exactMatch = 0;
    public static final Integer floatAbsoluteTolerance = 1;
    public static final Integer floatRelativeTolerance = 2;

    private boolean clicsValidator = true;
    private boolean caseSensitive;
    private boolean spaceSensitive;

    private double absoluteTolerance;

    private double relativeTolerance;

    private Integer typeOfClicsChecker = exactMatch;


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
        return typeOfClicsChecker.equals(floatAbsoluteTolerance);
    }

    public void setFloatAbsoluteTolerance(boolean param) {
        if (param)
            typeOfClicsChecker = floatAbsoluteTolerance;
    }

    public boolean isFloatRelativeTolerance() {
        return typeOfClicsChecker.equals(floatRelativeTolerance);
    }

    public void setFloatRelativeTolerance(boolean param) {
        if (param)
            typeOfClicsChecker = floatRelativeTolerance;
    }

    public boolean isExactMatch() {
        return typeOfClicsChecker.equals(exactMatch);
    }

    public void setExactMatch() {
        typeOfClicsChecker = exactMatch;
    }

    public double getAbsoluteTolerance() {
        return absoluteTolerance;
    }

    public void setAbsoluteTolerance(double absoluteTolerance) {
        this.absoluteTolerance = absoluteTolerance;
    }


    public double getRelativeTolerance() {
        return relativeTolerance;
    }

    public void setRelativeTolerance(double relativeTolerance) {
        this.relativeTolerance = relativeTolerance;
    }

    public Integer getTypeOfClicsChecker() {
        return typeOfClicsChecker;
    }

    public void setTypeOfClicsChecker(Integer typeOfClicsChecker) {
        this.typeOfClicsChecker = typeOfClicsChecker;
    }
}
