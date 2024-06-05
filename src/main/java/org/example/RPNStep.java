package org.example;

public class RPNStep {
    private RPNElementType type;
    private String value;

    @Override
    public String toString() {
        return "{" + type +
                ", " + value + "}";
    }

    public RPNElementType getType() {
        return type;
    }

    public void setType(RPNElementType type) {
        this.type = type;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public RPNStep(RPNElementType type, String value) {
        this.type = type;
        this.value = value;
    }
}
