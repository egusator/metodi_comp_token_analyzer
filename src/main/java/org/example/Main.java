package org.example;

import java.util.List;

public class Main {

    public static void main(String[] args) {
        String sourceCode =
                """
                        int a;
                        a=0;
                        while (a >= 0) {
                            if(a > 5) {
                                a+=1;
                            } else {
                                a -= 7;
                            }
                        }#""";
        //

        List<Token> tokens = TokenAnalyzer.parseSourceCodeToTokens(sourceCode);
        List<RPNStep> rpnSteps = RPNGenerator.generateRpnFromTokensList(tokens);

        //System.out.println(tokens.toString());
        String res ="";
        for(RPNStep step: rpnSteps) {
            res += step.getValue() + " ";
        }

        System.out.println(res);
    }
}