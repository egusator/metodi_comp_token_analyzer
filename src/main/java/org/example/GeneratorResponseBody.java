package org.example;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

public class GeneratorResponseBody {
    private Stack<RPNStep> steps;
    private Map<TableType, List<String>> variableTables;

    public Stack<RPNStep> getSteps() {
        return steps;
    }

    public void setSteps(Stack<RPNStep> steps) {
        this.steps = steps;
    }

    public Map<TableType, List<String>> getVariableTables() {
        return variableTables;
    }

    public void setVariableTables(Map<TableType, List<String>> variableTables) {
        this.variableTables = variableTables;
    }

    public HashMap<String, Integer> getMarkerValues() {
        return markerValues;
    }

    public void setMarkerValues(HashMap<String, Integer> markerValues) {
        this.markerValues = markerValues;
    }

    public GeneratorResponseBody(Stack<RPNStep> steps, Map<TableType, List<String>> variableTables, HashMap<String, Integer> markerValues) {
        this.steps = steps;
        this.variableTables = variableTables;
        this.markerValues = markerValues;
    }

    private HashMap<String, Integer> markerValues;

}
