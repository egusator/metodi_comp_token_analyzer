package org.example;

import java.util.List;
import java.util.Stack;

public class Main {

    public static void main(String[] args) throws InterruptedException {
       String sourceCode = "int a; \n" +
               "\n" +
               "while (a < 3) {\n" +
               "if(a < 4) {\n" +
               "a = 2; \n" +
               "} \n" +
               "\n" +
               "}\n" +
               "a = 5;#";

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