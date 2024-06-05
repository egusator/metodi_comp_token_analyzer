package org.example;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class TokenAnalyzer {

    private static State currentState;

    private static Integer tokenNumber;

    private static Integer indexInCode;

    private static String stringBuffer;

    private static Integer numberBuffer;

    private static Double doubleBuffer;

    private static Double power;

    private static String sourceCode;

    private static List<Token> tokens;

    private static HashMap<String, String> tokenType;

    static {
        tokens = new ArrayList<Token>();
        tokenType = new HashMap<String, String>();
        tokenType.put("=", "присваивание");
        tokenType.put("-", "вычитание");
        tokenType.put("+", "сложение");
        tokenType.put("*", "умножение");
        tokenType.put("/", "деление");
        tokenType.put(">", "больше");
        tokenType.put("<", "меньше");
        tokenType.put("!", "отрицание");
        tokenType.put("==", "сравнение");
        tokenType.put("-=", "минусравно");
        tokenType.put("+=", "плюсравно");
        tokenType.put("*=", "умножравно");
        tokenType.put("/=", "делравно");
        tokenType.put(">=", "большеравно");
        tokenType.put("<=", "меньшеравно");
        tokenType.put("!=", "не равно");
        tokenType.put("if", "if");
        tokenType.put("else", "else");
        tokenType.put("while", "while");
        tokenType.put("scan", "scan");
        tokenType.put("print", "print");
        tokenType.put("int", "int");
        tokenType.put("double", "double");
        tokenType.put("boolean", "boolean");
        tokenType.put("new", "new");
        tokenType.put("true", "true");
        tokenType.put("false", "false");
    }


    public static List<Token> parseSourceCodeToTokens(String code) {

        currentState = State.S;
        tokenNumber = 0;
        tokens.clear();

        sourceCode = code;

        for (indexInCode = 0; indexInCode < code.length(); ) {
            Character c = code.charAt(indexInCode);

            if (currentState == State.Z)
                currentState = State.S;

            //если буква
            if (Character.isLetter(c) || c.equals('_')) {
                switch (currentState) {
                    case S -> {
                        currentState = State.STRING;
                        prog1();
                    }
                    case STRING -> {
                        currentState = State.STRING;
                        prog13();
                    }
                    case NUMBER -> {
                        currentState = State.ERROR;
                        return null;
                    }
                    case DOUBLE -> {
                        currentState = State.ERROR;
                        return null;
                    }
                    case BIN_OP -> {
                        currentState = State.Z;
                        prog20();
                        indexInCode--;
                    }
                }
            }

            //если цифра
            if (Character.isDigit(c)) {
                switch (currentState) {
                    case S -> {
                        currentState = State.NUMBER;
                        prog2();
                    }
                    case STRING -> {
                        currentState = State.STRING;
                        prog13();
                    }
                    case NUMBER -> {
                        currentState = State.NUMBER;
                        prog15();
                    }
                    case DOUBLE -> {
                        currentState = State.DOUBLE;
                        prog18();
                    }
                    case BIN_OP -> {
                        currentState = State.Z;
                        prog20();
                        indexInCode--;
                    }
                }
            }

            //если =
            if (c.equals('=')) {
                switch (currentState) {
                    case S -> {
                        currentState = State.BIN_OP;
                        prog1();
                    }
                    case STRING -> {
                        currentState = State.Z;
                        prog14();
                        indexInCode--;
                    }
                    case NUMBER -> {
                        currentState = State.Z;
                        prog16();
                        indexInCode--;
                    }
                    case DOUBLE -> {
                        currentState = State.Z;
                        prog19();
                        indexInCode--;
                    }
                    case BIN_OP -> {
                        currentState = State.Z;
                        prog21();
                    }
                }
            }

            //если -
            if (c.equals('-')) {
                switch (currentState) {
                    case S -> {
                        currentState = State.BIN_OP;
                        prog1();
                    }
                    case STRING -> {
                        currentState = State.Z;
                        prog14();
                        indexInCode--;
                    }
                    case NUMBER -> {
                        currentState = State.Z;
                        prog16();
                        indexInCode--;
                    }
                    case DOUBLE -> {
                        currentState = State.Z;
                        prog19();
                        indexInCode--;
                    }
                    case BIN_OP -> {
                        currentState = State.ERROR;
                        return null;
                    }
                }
            }

            //если +
            if (c.equals('+')) {
                switch (currentState) {
                    case S -> {
                        currentState = State.BIN_OP;
                        prog1();
                    }
                    case STRING -> {
                        currentState = State.Z;
                        prog14();
                        indexInCode--;
                    }
                    case NUMBER -> {
                        currentState = State.Z;
                        prog16();
                        indexInCode--;
                    }
                    case DOUBLE -> {
                        currentState = State.Z;
                        prog19();
                        indexInCode--;
                    }
                    case BIN_OP -> {
                        currentState = State.ERROR;
                        return null;
                    }
                }
            }

            //если *
            if (c.equals('*')) {
                switch (currentState) {
                    case S -> {
                        currentState = State.BIN_OP;
                        prog1();
                    }
                    case STRING -> {
                        currentState = State.Z;
                        prog14();
                        indexInCode--;
                    }
                    case NUMBER -> {
                        currentState = State.Z;
                        prog16();
                        indexInCode--;
                    }
                    case DOUBLE -> {
                        currentState = State.Z;
                        prog19();
                        indexInCode--;
                    }
                    case BIN_OP -> {
                        currentState = State.ERROR;
                        return null;
                    }
                }
            }

            //если /
            if (c.equals('/')) {
                switch (currentState) {
                    case S -> {
                        currentState = State.BIN_OP;
                        prog1();
                    }
                    case STRING -> {
                        currentState = State.Z;
                        prog14();
                        indexInCode--;
                    }
                    case NUMBER -> {
                        currentState = State.Z;
                        prog16();
                        indexInCode--;
                    }
                    case DOUBLE -> {
                        currentState = State.Z;
                        prog19();
                        indexInCode--;
                    }
                    case BIN_OP -> {
                        currentState = State.ERROR;
                        return null;
                    }
                }
            }

            //если (
            if (c.equals('(')) {
                switch (currentState) {
                    case S -> {
                        currentState = State.Z;
                        prog3();
                    }
                    case STRING -> {
                        currentState = State.Z;
                        prog14();
                        indexInCode--;
                    }
                    case NUMBER -> {
                        currentState = State.Z;
                        prog16();
                        indexInCode--;
                    }
                    case DOUBLE -> {
                        currentState = State.Z;
                        prog19();
                        indexInCode--;
                    }
                    case BIN_OP -> {
                        currentState = State.Z;
                        prog20();
                        indexInCode--;
                    }
                }
            }

            //если )
            if (c.equals(')')) {
                switch (currentState) {
                    case S -> {
                        currentState = State.Z;
                        prog4();
                    }
                    case STRING -> {
                        currentState = State.Z;
                        prog14();
                        indexInCode--;
                    }
                    case NUMBER -> {
                        currentState = State.Z;
                        prog16();
                        indexInCode--;
                    }
                    case DOUBLE -> {
                        currentState = State.Z;
                        prog19();
                        indexInCode--;
                    }
                    case BIN_OP -> {
                        currentState = State.Z;
                        prog20();
                        indexInCode--;
                    }
                }
            }

            //если >
            if (c.equals('>')) {
                switch (currentState) {
                    case S -> {
                        currentState = State.BIN_OP;
                        prog1();
                    }
                    case STRING -> {
                        currentState = State.Z;
                        prog14();
                        indexInCode--;
                    }
                    case NUMBER -> {
                        currentState = State.Z;
                        prog16();
                        indexInCode--;
                    }
                    case DOUBLE -> {
                        currentState = State.Z;
                        prog19();
                        indexInCode--;
                    }
                    case BIN_OP -> {
                        currentState = State.ERROR;
                        return null;
                    }
                }
            }

            //если <
            if (c.equals('<')) {
                switch (currentState) {
                    case S -> {
                        currentState = State.BIN_OP;
                        prog1();
                    }
                    case STRING -> {
                        currentState = State.Z;
                        prog14();
                        indexInCode--;
                    }
                    case NUMBER -> {
                        currentState = State.Z;
                        prog16();
                        indexInCode--;
                    }
                    case DOUBLE -> {
                        currentState = State.Z;
                        prog19();
                        indexInCode--;
                    }
                    case BIN_OP -> {
                        currentState = State.ERROR;
                        return null;
                    }
                }
            }

            //если !
            if (c.equals('!')) {
                switch (currentState) {
                    case S -> {
                        currentState = State.BIN_OP;
                        prog1();
                    }
                    case STRING -> {
                        currentState = State.Z;
                        prog14();
                        indexInCode--;
                    }
                    case NUMBER -> {
                        currentState = State.Z;
                        prog16();
                        indexInCode--;
                    }
                    case DOUBLE -> {
                        currentState = State.Z;
                        prog19();
                        indexInCode--;
                    }
                    case BIN_OP -> {
                        currentState = State.ERROR;
                        return null;
                    }
                }
            }

            //если &
            if (c.equals('&')) {
                switch (currentState) {
                    case S -> {
                        currentState = State.Z;
                        prog5();
                    }
                    case STRING -> {
                        currentState = State.Z;
                        prog14();
                        indexInCode--;
                    }
                    case NUMBER -> {
                        currentState = State.Z;
                        prog16();
                        indexInCode--;
                    }
                    case DOUBLE -> {
                        currentState = State.Z;
                        prog19();
                        indexInCode--;
                    }
                    case BIN_OP -> {
                        currentState = State.ERROR;
                        return null;
                    }
                }
            }

            //если |
            if (c.equals('|')) {
                switch (currentState) {
                    case S -> {
                        currentState = State.Z;
                        prog6();
                    }
                    case STRING -> {
                        currentState = State.Z;
                        prog14();
                        indexInCode--;
                    }
                    case NUMBER -> {
                        currentState = State.Z;
                        prog16();
                        indexInCode--;
                    }
                    case DOUBLE -> {
                        currentState = State.Z;
                        prog19();
                        indexInCode--;
                    }
                    case BIN_OP -> {
                        currentState = State.ERROR;
                        return null;
                    }
                }
            }

            //если ;
            if (c.equals(';')) {
                switch (currentState) {
                    case S -> {
                        currentState = State.Z;
                        prog7();
                    }
                    case STRING -> {
                        currentState = State.Z;
                        prog14();
                        indexInCode--;
                    }
                    case NUMBER -> {
                        currentState = State.Z;
                        prog16();
                        indexInCode--;
                    }
                    case DOUBLE -> {
                        currentState = State.Z;
                        prog19();
                        indexInCode--;
                    }
                    case BIN_OP -> {
                        currentState = State.ERROR;
                        return null;
                    }
                }
            }

            //если {
            if (c.equals('{')) {
                switch (currentState) {
                    case S -> {
                        currentState = State.Z;
                        prog8();
                    }
                    case STRING -> {
                        currentState = State.Z;
                        prog14();
                        indexInCode--;
                    }
                    case NUMBER -> {
                        currentState = State.Z;
                        prog16();
                        indexInCode--;
                    }
                    case DOUBLE -> {
                        currentState = State.Z;
                        prog19();
                        indexInCode--;
                    }
                    case BIN_OP -> {
                        currentState = State.ERROR;
                        return null;
                    }
                }
            }

            //если }
            if (c.equals('}')) {
                switch (currentState) {
                    case S -> {
                        currentState = State.Z;
                        prog9();
                    }
                    case STRING -> {
                        currentState = State.Z;
                        prog14();
                        indexInCode--;
                    }
                    case NUMBER -> {
                        currentState = State.Z;
                        prog16();
                        indexInCode--;
                    }
                    case DOUBLE -> {
                        currentState = State.Z;
                        prog19();
                        indexInCode--;
                    }
                    case BIN_OP -> {
                        currentState = State.ERROR;
                        return null;
                    }
                }
            }

            //если [
            if (c.equals('[')) {
                switch (currentState) {
                    case S -> {
                        currentState = State.Z;
                        prog10();
                    }
                    case STRING -> {
                        currentState = State.Z;
                        prog14();
                        indexInCode--;
                    }
                    case NUMBER -> {
                        currentState = State.Z;
                        prog16();
                        indexInCode--;
                    }
                    case DOUBLE -> {
                        currentState = State.Z;
                        prog19();
                        indexInCode--;
                    }
                    case BIN_OP -> {
                        currentState = State.ERROR;
                        return null;
                    }
                }
            }

            //если ]
            if (c.equals(']')) {
                switch (currentState) {
                    case S -> {
                        currentState = State.Z;
                        prog11();
                    }
                    case STRING -> {
                        currentState = State.Z;
                        prog14();
                        indexInCode--;
                    }
                    case NUMBER -> {
                        currentState = State.Z;
                        prog16();
                        indexInCode--;
                    }
                    case DOUBLE -> {
                        currentState = State.Z;
                        prog19();
                        indexInCode--;
                    }
                    case BIN_OP -> {
                        currentState = State.ERROR;
                        return null;
                    }
                }
            }

            //если .
            if (c.equals('.')) {
                switch (currentState) {
                    case S -> {
                        currentState = State.ERROR;
                        return null;
                    }
                    case STRING -> {
                        currentState = State.ERROR;
                        return null;
                    }
                    case NUMBER -> {
                        currentState = State.DOUBLE;
                        prog17();
                    }
                    case DOUBLE -> {
                        currentState = State.ERROR;
                        return null;
                    }
                    case BIN_OP -> {
                        currentState = State.ERROR;
                        return null;
                    }
                }
            }

            //если ,
            if (c.equals(',')) {
                switch (currentState) {
                    case S -> {
                        currentState = State.Z;
                        prog22();
                    }
                    case STRING -> {
                        currentState = State.Z;
                        prog14();
                        indexInCode--;
                    }
                    case NUMBER -> {
                        currentState = State.Z;
                        prog14();
                        indexInCode--;
                    }
                    case DOUBLE -> {
                        currentState = State.Z;
                        prog14();
                        indexInCode--;
                    }
                    case BIN_OP -> {
                        currentState = State.ERROR;
                        return null;
                    }
                }
            }

            //если пробел
            if (c.equals(' ') || c.equals('\n')) {
                switch (currentState) {
                    case S -> {
                        currentState = State.S;
                        prog12();
                    }
                    case STRING -> {
                        currentState = State.Z;
                        prog14();
                    }
                    case NUMBER -> {
                        currentState = State.Z;
                        prog16();
                    }
                    case DOUBLE -> {
                        currentState = State.Z;
                        prog19();
                    }
                    case BIN_OP -> {
                        currentState = State.Z;
                        prog20();
                    }
                }
            }

            if (c.equals('#')) {
                return new ArrayList<>(tokens);
            }


        }
        return null;
    }

    private static void prog1() {
        stringBuffer = String.valueOf(sourceCode.charAt(indexInCode));
        indexInCode++;
    }

    private static void prog2() {
        numberBuffer = Character.getNumericValue(sourceCode.charAt(indexInCode));
        indexInCode++;
    }

    private static void prog3() {
        tokens.add(new Token(tokenNumber++, "(", TokenType.LEFT_ROUND_BR));
        indexInCode++;
    }

    private static void prog4() {
        tokens.add(new Token(tokenNumber++, ")", TokenType.RIGHT_ROUND_BR));
        indexInCode++;
    }

    private static void prog5() {
        tokens.add(new Token(tokenNumber++, "&", TokenType.AND));
        indexInCode++;
    }

    private static void prog6() {
        tokens.add(new Token(tokenNumber++, "|", TokenType.OR));
        indexInCode++;
    }

    private static void prog7() {
        tokens.add(new Token(tokenNumber++, ";", TokenType.DOT_COMMA));
        indexInCode++;
    }

    private static void prog8() {
        tokens.add(new Token(tokenNumber++, "{", TokenType.LEFT_FIG_BR));
        indexInCode++;
    }

    private static void prog9() {
        tokens.add(new Token(tokenNumber++, "}", TokenType.RIGHT_FIG_BR));
        indexInCode++;
    }

    private static void prog10() {
        tokens.add(new Token(tokenNumber++, "[", TokenType.LEFT_SQR_BR));
        indexInCode++;
    }

    private static void prog11() {
        tokens.add(new Token(tokenNumber++, "]", TokenType.RIGHT_SQR_BR));
        indexInCode++;
    }

    private static void prog12() {
        indexInCode++;
    }

    private static void prog13() {
        stringBuffer += sourceCode.charAt(indexInCode);
        indexInCode++;
    }

    private static void prog14() {
        TokenType type = null;
        if (stringBuffer.equals("if")) {
            type = TokenType.IF;
        } else if (stringBuffer.equals("boolean")) {
            type = TokenType.BOOLEAN;
        } else if (stringBuffer.equals("while")) {
            type = TokenType.WHILE;
        } else if (stringBuffer.equals("else")) {
            type = TokenType.ELSE;
        } else if (stringBuffer.equals("scan")) {
            type = TokenType.SCAN;
        } else if (stringBuffer.equals("print")) {
            type = TokenType.PRINT;
        } else if (stringBuffer.equals("true")) {
            type = TokenType.TRUE;
        } else if (stringBuffer.equals("false")) {
            type = TokenType.FALSE;
        } else if (stringBuffer.equals("new")) {
            type = TokenType.NEW;
        } else if (stringBuffer.equals("int")) {
            type = TokenType.INT;
        } else if (stringBuffer.equals("double")) {
            type = TokenType.DOUBLE;
        } else {
            type = TokenType.VARIABLE;
        }
        tokens.add(new Token(tokenNumber++, stringBuffer, type));
        indexInCode++;
    }

    private static void prog15() {
        numberBuffer += numberBuffer * 10 + Character.getNumericValue(sourceCode.charAt(indexInCode));
        indexInCode++;
    }

    private static void prog16() {
        tokens.add(new Token(tokenNumber++, String.valueOf(numberBuffer), TokenType.CONST));
        indexInCode++;
    }

    private static void prog17() {
        doubleBuffer = (double) numberBuffer;
        power = 1.0;
        indexInCode++;
    }

    private static void prog18() {
        power /= 10;
        doubleBuffer += power * Character.getNumericValue(sourceCode.charAt(indexInCode));
        indexInCode++;
    }

    private static void prog19() {
        tokens.add(new Token(tokenNumber++, String.valueOf(doubleBuffer), TokenType.CONST));
        indexInCode++;
    }

    private static void prog20() {
        TokenType type = null;
        if (stringBuffer.equals("+")) {
            type = TokenType.PLUS;
        } else if (stringBuffer.equals("-")) {
            type = TokenType.MINUS;
        } else if (stringBuffer.equals("*")) {
            type = TokenType.MULTIPLY;
        } else if (stringBuffer.equals("/")) {
            type = TokenType.DIV;
        } else if (stringBuffer.equals(">")) {
            type = TokenType.GREATER;
        } else if (stringBuffer.equals("<")) {
            type = TokenType.LESS;
        } else if (stringBuffer.equals("!")) {
            type = TokenType.NOT;
        } else if (stringBuffer.equals("=")) {
            type = TokenType.ASSIGNMENT;
        }
        tokens.add(new Token(tokenNumber++, stringBuffer, type));
        indexInCode++;
    }

    private static void prog21() {
        stringBuffer += String.valueOf(sourceCode.charAt(indexInCode));
        TokenType type = null;
        if (stringBuffer.equals("+=")) {
            type = TokenType.PLUS_EQUALS;
        } else if (stringBuffer.equals("-=")) {
            type = TokenType.MINUS_EQUALS;
        } else if (stringBuffer.equals("*=")) {
            type = TokenType.MULTIPLY_EQUALS;
        } else if (stringBuffer.equals("/=")) {
            type = TokenType.DIV_EQUALS;
        } else if (stringBuffer.equals(">=")) {
            type = TokenType.GREATER_EQUALS;
        } else if (stringBuffer.equals("<=")) {
            type = TokenType.LESS_EQUALS;
        } else if (stringBuffer.equals("!=")) {
            type = TokenType.NOT_EQUALS;
        } else if (stringBuffer.equals("==")) {
            type = TokenType.EQUALS;
        }
        tokens.add(new Token(tokenNumber++, stringBuffer, type));
        indexInCode++;
    }

    private static void prog22() {
        tokens.add(new Token(tokenNumber++, ",", TokenType.COMMA));
        indexInCode++;
    }
}
