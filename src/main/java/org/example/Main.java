package org.example;

import java.util.List;

public class Main {

    public static void main(String[] args) {
        String sourceCode =
                """
                        int a; a=3; if (a > 0) { b+=a;} else { b += 1; } print(b);#""";
        List<Token> tokens = TokenAnalyzer.parseSourceCodeToTokens(sourceCode);
        List<RPNStep> rpnSteps = RPNGenerator.generateRpnFromTokensList(tokens);

        System.out.println(tokens.toString());
        String res ="";
        for(RPNStep step: rpnSteps) {
            res += step.getValue() + " ";
        }

        System.out.println(res);
    }
}