package ru.vkr.vkr.form;

public class CheckerSettingsForm {

    private boolean exactMatch = true;
    private boolean floatAbsoluteTolerance = false;
    private boolean floatRelativeTolerance = false;

    private boolean caseSensitive;
    private boolean spaceSensitive;

    private double absoluteTolerance;
    private double relativeTolerance;


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

    public boolean isExactMatch() {
        return exactMatch;
    }

    public void setExactMatch(boolean exactMatch) {
        this.exactMatch = exactMatch;
    }

    public boolean isFloatAbsoluteTolerance() {
        return floatAbsoluteTolerance;
    }

    public void setFloatAbsoluteTolerance(boolean floatAbsoluteTolerance) {
        this.floatAbsoluteTolerance = floatAbsoluteTolerance;
    }

    public boolean isFloatRelativeTolerance() {
        return floatRelativeTolerance;
    }

    public void setFloatRelativeTolerance(boolean floatRelativeTolerance) {
        this.floatRelativeTolerance = floatRelativeTolerance;
    }
}
