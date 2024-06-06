package org.example;

import java.util.List;
import java.util.Stack;

public class Main {

    public static void main(String[] args) throws InterruptedException {
        String sortirovka =
                """
int a[5];
a[1] = 4;
a[2] = 3;
a[3] = 5;
a[4] = 6;
a[5] = 2;
int i = 0;
int j = 0;
int min;
int c;
while (i < 5) {
    min = 0;
    j = 0;
    while (j < 5) {
        if (a[j] < a[min]) {
            min = j;
        }
        j += 1;
    }
    c = a[i];
    a[i] = a[min];
    a[min] = c;
    i++; 
}
        """;

        String sourceCode = """
                int a; a = 1 * 2 + 3;#
                """;

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