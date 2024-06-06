package org.example;

public class RPNStep {
    private RPNStepType rpnStepType;
    private String value;

    public RPNStep(RPNStepType rpnStepType, String value) {
        this.rpnStepType = rpnStepType;
        this.value = value;
    }

    @Override
    public String toString() {
        return "{" + rpnStepType +
                ", " + value + "}";
    }

    public RPNStepType getRpnStepType() {
        return rpnStepType;
    }

    public void setRpnStepType(RPNStepType rpnStepType) {
        this.rpnStepType = rpnStepType;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
