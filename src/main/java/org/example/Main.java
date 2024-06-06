package org.example;

import java.util.List;
import java.util.Stack;

public class Main {

    public static void main(String[] args) throws InterruptedException {
       String sourceCode = "int a[5];#";

        List<Token> tokens = TokenAnalyzer.parseSourceCodeToTokens(sourceCode);

        Stack<Token> tokenStack = new Stack<>();

        for(int i = tokens.size() - 1; i >= 0; i--) {
            tokenStack.add(tokens.get(i));
        }

        GeneratorResponseBody generatorResponseBody = RPNGenerator.generateRpnFromTokensList(tokenStack);

        //System.out.println(tokens.toString());
        String res ="";
        Integer i = 0;
        System.out.println();
//        for(RPNStep step: generatorResponseBody) {
//
//            res += step.getValue() + " ";
//        }
//        System.out.println();
//        System.out.println();
//        for(RPNStep step: generatorResponseBody) {
//
//            res += "{" + i + "," + step.getValue() + "}\n";
//            i++;
//        }

        RPNExecutor.executeRPN(generatorResponseBody);
    }
}