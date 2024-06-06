package org.example;

public class RPNGenerationStep {
    private RPNGenerationStepType type;
    private String value;

    @Override
    public String toString() {
        if (type == RPNGenerationStepType.SKIP) {
            return "skip";
        } else if (type == RPNGenerationStepType.VARIABLE) {
            return "a";
        } else if (type == RPNGenerationStepType.CONST) {
            return "k";
        } else return value;
//        return "{" + type +
//                ", " + value + "}";
    }

    public RPNGenerationStepType getType() {
        return type;
    }

    public void setType(RPNGenerationStepType type) {
        this.type = type;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public RPNGenerationStep(RPNGenerationStepType type, String value) {
        this.type = type;
        this.value = value;
    }
}
