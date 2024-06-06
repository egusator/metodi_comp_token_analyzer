package org.example;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Stack;

public class RPNExecutor {

    private static Map<String, Integer> integerPointers;

    private static List<Integer> integerMemory;

    private static Map<String, Integer> doublePointers;

    private static List<Double> doubleMemory;

    private static Map<String, Integer> booleanPointers;

    private static List<Boolean> booleanMemory;

    private static Stack<Operand> operandsStack;

    public static void executeRPN(GeneratorResponseBody generatorResponseBody) {

        integerMemory = new ArrayList<>();
        doubleMemory = new ArrayList<>();
        booleanMemory = new ArrayList<>();

        Stack<RPNStep> steps = generatorResponseBody.getSteps();
        Map<TableType, List<String>> variableTables = generatorResponseBody.getVariableTables();


        while (steps.size() > 0) {
            RPNStep step = steps.pop();
            if (step.getRpnStepType() == RPNStepType.INT_VARIABLE) {
                Integer pointer = integerMemory.size();
                String value = step.getValue();
                integerPointers.put(value, pointer);
                if (variableTables.get(TableType.ARRAY).contains(value)) {
                    Integer length = Integer.valueOf(steps.pop().getValue());
                    for (int i = 0; i < length; i++) {
                        integerMemory.add(null);
                    }
                } else {
                    if (!integerPointers.containsKey(value)) {
                        integerMemory.add(null);
                    }
                }
            } else if (step.getRpnStepType() == RPNStepType.DOUBLE_VARIABLE) {
                Integer pointer = doubleMemory.size();
                String value = step.getValue();
                doublePointers.put(value, pointer);
                if (variableTables.get(TableType.ARRAY).contains(value)) {
                    Integer length = Integer.valueOf(steps.pop().getValue());
                    for (int i = 0; i < length; i++) {
                        doubleMemory.add(null);
                    }
                } else {
                    if (!doublePointers.containsKey(value)) {
                        doubleMemory.add(null);
                    }
                }
            } else if (step.getRpnStepType() == RPNStepType.BOOLEAN_VARIABLE) {
                Integer pointer = booleanPointers.size();
                String value = step.getValue();
                booleanPointers.put(value, pointer);
                if (variableTables.get(TableType.ARRAY).contains(value)) {
                    Integer length = Integer.valueOf(steps.pop().getValue());
                    for (int i = 0; i < length; i++) {
                        booleanMemory.add(null);
                    }
                } else {
                    if (!booleanPointers.containsKey(value)) {
                        booleanMemory.add(null);
                    }
                }

            } else if (step.getRpnStepType() == RPNStepType.CONST) {

            } else if (step.getRpnStepType() == RPNStepType.MARKER) {

            } else if (step.getRpnStepType() == RPNStepType.OPERATION) {

            }
        }
    }
}
