package org.example;

import java.util.List;
import java.util.Stack;

public class Main {

    public static void main(String[] args) throws InterruptedException {
       String sourceCode = "int a[5];\n" +
               "a[1] = 4;\n" +
               "int i = 0;\n" +
               "int j = 0;\n" +
               "int min;\n" +
               "int c;\n" +
               "while (i < 5) {\n" +
               "    min = 0;\n" +
               "    j = 0;\n" +
               "    while (j < 5) {\n" +
               "        if (a[j] < a[min]) {\n" +
               "            min = j;\n" +
               "        }\n" +
               "        j += 1;\n" +
               "    }\n" +
               "    c = a[i];\n" +
               "    a[i] = a[min];\n" +
               "    a[min] = c;\n" +
               "    i+=1; \n" +
               "}#";

        List<Token> tokens = TokenAnalyzer.parseSourceCodeToTokens(sourceCode);

        Stack<Token> tokenStack = new Stack<>();

        for(int i = tokens.size() - 1; i >= 0; i--) {
            tokenStack.add(tokens.get(i));
        }

        List<RPNStep> rpnSteps = RPNGenerator.generateRpnFromTokensList(tokenStack);

        //System.out.println(tokens.toString());
        String res ="";
        Integer i = 0;
        System.out.println();
        for(RPNStep step: rpnSteps) {

            res += step.getValue() + " ";
        }
        System.out.println();
        System.out.println();
        for(RPNStep step: rpnSteps) {

            res += "{" + i + "," + step.getValue() + "}\n";
            i++;
        }

        System.out.println(res);
    }
}