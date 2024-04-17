package org.example;

import java.util.List;

public class Main {

    public static void main(String[] args) {
        String sourceCode =
                """
                        if (a == 5 & c == 7.2) {
                            b = a + c;
                            print(b);
                        } else {
                            print(a);
                        }#""";
        List<Token> tokens = TokenAnalyzer.parseSourceCodeToTokens(sourceCode);

        System.out.println(tokens.toString());
    }
}