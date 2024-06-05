package org.example;

import java.util.*;

public class RPNGenerator {

    private static Stack<RPNStep> generationSteps;

    private static List<RPNStep> resultRPN;

    private static CFGState currentState;

    private static List<Token> input;

    private static Token currentToken;

    private static Stack<MagElement> mag;

    private static Map<CFGState, HashMap<TokenType, Runnable>> generationTable;

    static {
        generationTable = new HashMap<>();
        generationSteps = new Stack<>();
        resultRPN = new ArrayList<>();
        mag = new Stack<>();

        for (CFGState state : CFGState.values())
            generationTable.put(state, new HashMap<TokenType, Runnable>());

        //Правила для A

        generationTable.get(CFGState.A).put(
                TokenType.DOT_COMMA, () -> {
                    generationSteps.add(new RPNStep(RPNElementType.SKIP, null));
                    generationSteps.add(new RPNStep(RPNElementType.PROGRAM, "16"));

                    mag.add(new NonTerminal(CFGState.B));
                    mag.add(new Terminal(TokenType.DOT_COMMA));
                }
        );

        generationTable.get(CFGState.A).put(
                TokenType.LEFT_SQR_BR, () -> {
                    generationSteps.add(new RPNStep(RPNElementType.SKIP, null));
                    generationSteps.add(new RPNStep(RPNElementType.PROGRAM, "16"));

                    mag.add(new NonTerminal(CFGState.E));
                    mag.add(new Terminal(TokenType.LEFT_SQR_BR));
                }
        );

        generationTable.get(CFGState.A).put(
                TokenType.ASSIGNMENT, () -> {
                    generationSteps.add(new RPNStep(RPNElementType.OPERATION, "="));
                    generationSteps.add(new RPNStep(RPNElementType.SKIP, null));
                    generationSteps.add(new RPNStep(RPNElementType.SKIP, null));
                    generationSteps.add(new RPNStep(RPNElementType.PROGRAM, "16"));

                    mag.add(new NonTerminal(CFGState.B));
                    mag.add(new Terminal(TokenType.DOT_COMMA));
                    mag.add(new NonTerminal(CFGState.J));
                    mag.add(new Terminal(TokenType.ASSIGNMENT));
                }
        );

        //Правила для E

        generationTable.get(CFGState.E).put(
                TokenType.CONST, () -> {
                    generationSteps.add(new RPNStep(RPNElementType.SKIP, null));
                    generationSteps.add(new RPNStep(RPNElementType.SKIP, null));
                    generationSteps.add(new RPNStep(RPNElementType.CONST, null));

                    mag.add(new NonTerminal(CFGState.F));
                    mag.add(new Terminal(TokenType.RIGHT_SQR_BR));
                    mag.add(new Terminal(TokenType.CONST));
                }
        );

        generationTable.get(CFGState.E).put(
                TokenType.RIGHT_SQR_BR, () -> {
                    generationSteps.add(new RPNStep(RPNElementType.OPERATION, "="));
                    generationSteps.add(new RPNStep(RPNElementType.SKIP, null));
                    generationSteps.add(new RPNStep(RPNElementType.OPERATION, "i"));
                    generationSteps.add(new RPNStep(RPNElementType.SKIP, null));
                    generationSteps.add(new RPNStep(RPNElementType.SKIP, null));
                    generationSteps.add(new RPNStep(RPNElementType.SKIP, null));
                    generationSteps.add(new RPNStep(RPNElementType.PROGRAM, "14"));
                    generationSteps.add(new RPNStep(RPNElementType.SKIP, null));
                    generationSteps.add(new RPNStep(RPNElementType.SKIP, null));

                    mag.add(new NonTerminal(CFGState.B));
                    mag.add(new Terminal(TokenType.DOT_COMMA));
                    mag.add(new Terminal(TokenType.RIGHT_SQR_BR));
                    mag.add(new NonTerminal(CFGState.J));
                    mag.add(new Terminal(TokenType.LEFT_SQR_BR));
                    mag.add(new NonTerminal(CFGState.G));
                    mag.add(new Terminal(TokenType.NEW));
                    mag.add(new Terminal(TokenType.ASSIGNMENT));
                    mag.add(new Terminal(TokenType.RIGHT_SQR_BR));

                }
        );

        //Правила для F

        generationTable.get(CFGState.F).put(
                TokenType.DOT_COMMA, () -> {
                    generationSteps.add(new RPNStep(RPNElementType.SKIP, null));
                    generationSteps.add(new RPNStep(RPNElementType.SKIP, null));

                    mag.add(new NonTerminal(CFGState.B));
                    mag.add(new Terminal(TokenType.DOT_COMMA));
                }
        );

        generationTable.get(CFGState.F).put(
                TokenType.ASSIGNMENT, () -> {
                    generationSteps.add(new RPNStep(RPNElementType.OPERATION, "="));
                    generationSteps.add(new RPNStep(RPNElementType.SKIP, null));
                    generationSteps.add(new RPNStep(RPNElementType.SKIP, null));
                    generationSteps.add(new RPNStep(RPNElementType.SKIP, null));
                    generationSteps.add(new RPNStep(RPNElementType.PROGRAM, "16"));
                    generationSteps.add(new RPNStep(RPNElementType.SKIP, null));


                    mag.add(new NonTerminal(CFGState.B));
                    mag.add(new Terminal(TokenType.DOT_COMMA));
                    mag.add(new Terminal(TokenType.RIGHT_FIG_BR));
                    mag.add(new NonTerminal(CFGState.J));
                    mag.add(new Terminal(TokenType.LEFT_FIG_BR));

                    mag.add(new Terminal(TokenType.ASSIGNMENT));
                }
        );

        //Правила для B

        generationTable.get(CFGState.B).put(
                TokenType.VARIABLE, () -> {
                    generationSteps.add(new RPNStep(RPNElementType.SKIP, null));

                    generationSteps.add(new RPNStep(RPNElementType.VARIABLE, null));
                    mag.add(new NonTerminal(CFGState.D));

                    mag.add(new Terminal(TokenType.VARIABLE));
                }
        );

        generationTable.get(CFGState.B).put(
                TokenType.INT, () -> {
                    generationSteps.add(new RPNStep(RPNElementType.SKIP, null));
                    generationSteps.add(new RPNStep(RPNElementType.VARIABLE, null));

                    generationSteps.add(new RPNStep(RPNElementType.PROGRAM, "11"));
                    mag.add(new NonTerminal(CFGState.A));
                    mag.add(new Terminal(TokenType.VARIABLE));

                    mag.add(new Terminal(TokenType.INT));
                }
        );

        generationTable.get(CFGState.B).put(
                TokenType.DOUBLE, () -> {
                    generationSteps.add(new RPNStep(RPNElementType.SKIP, null));
                    generationSteps.add(new RPNStep(RPNElementType.VARIABLE, null));
                    generationSteps.add(new RPNStep(RPNElementType.PROGRAM, "12"));

                    mag.add(new NonTerminal(CFGState.A));
                    mag.add(new Terminal(TokenType.VARIABLE));

                    mag.add(new Terminal(TokenType.DOUBLE));
                }
        );

        generationTable.get(CFGState.B).put(
                TokenType.BOOLEAN, () -> {
                    generationSteps.add(new RPNStep(RPNElementType.SKIP, null));
                    generationSteps.add(new RPNStep(RPNElementType.VARIABLE, null));
                    generationSteps.add(new RPNStep(RPNElementType.PROGRAM, "13"));


                    mag.add(new NonTerminal(CFGState.A));
                    mag.add(new Terminal(TokenType.VARIABLE));

                    mag.add(new Terminal(TokenType.BOOLEAN));
                }
        );

        generationTable.get(CFGState.B).put(
                TokenType.IF, () -> {
                    generationSteps.add(new RPNStep(RPNElementType.SKIP, null));
                    generationSteps.add(new RPNStep(RPNElementType.PROGRAM, "3"));
                    generationSteps.add(new RPNStep(RPNElementType.SKIP, null));
                    generationSteps.add(new RPNStep(RPNElementType.SKIP, null));
                    generationSteps.add(new RPNStep(RPNElementType.PROGRAM, "1"));
                    generationSteps.add(new RPNStep(RPNElementType.SKIP, null));
                    generationSteps.add(new RPNStep(RPNElementType.SKIP, null));
                    generationSteps.add(new RPNStep(RPNElementType.SKIP, null));
                    mag.add(new NonTerminal(CFGState.B));
                    mag.add(new Terminal(TokenType.RIGHT_FIG_BR));
                    mag.add(new NonTerminal(CFGState.B));
                    mag.add(new Terminal(TokenType.LEFT_FIG_BR));
                    mag.add(new Terminal(TokenType.RIGHT_ROUND_BR));
                    mag.add(new NonTerminal(CFGState.J));
                    mag.add(new Terminal(TokenType.LEFT_ROUND_BR));

                    mag.add(new Terminal(TokenType.IF));
                }
        );

        generationTable.get(CFGState.B).put(
                TokenType.WHILE, () -> {

                    generationSteps.add(new RPNStep(RPNElementType.SKIP, null));
                    generationSteps.add(new RPNStep(RPNElementType.PROGRAM, "5"));
                    generationSteps.add(new RPNStep(RPNElementType.SKIP, null));
                    generationSteps.add(new RPNStep(RPNElementType.PROGRAM, "1"));
                    generationSteps.add(new RPNStep(RPNElementType.SKIP, null));
                    generationSteps.add(new RPNStep(RPNElementType.SKIP, null));
                    generationSteps.add(new RPNStep(RPNElementType.PROGRAM, "4"));
                    generationSteps.add(new RPNStep(RPNElementType.SKIP, null));

                    mag.add(new NonTerminal(CFGState.B));
                    mag.add(new Terminal(TokenType.RIGHT_FIG_BR));
                    mag.add(new NonTerminal(CFGState.B));
                    mag.add(new Terminal(TokenType.LEFT_FIG_BR));
                    mag.add(new Terminal(TokenType.RIGHT_ROUND_BR));
                    mag.add(new NonTerminal(CFGState.J));
                    mag.add(new Terminal(TokenType.LEFT_ROUND_BR));
                    mag.add(new Terminal(TokenType.WHILE));
                }
        );

        generationTable.get(CFGState.B).put(
                TokenType.ELSE, () -> {
                    generationSteps.add(new RPNStep(RPNElementType.SKIP, null));
                    generationSteps.add(new RPNStep(RPNElementType.PROGRAM, "2"));

                    mag.add(new NonTerminal(CFGState.M));

                    mag.add(new Terminal(TokenType.ELSE));
                }
        );

        //TODO
        generationTable.get(CFGState.B).put(
                TokenType.SCAN, () -> {
                    generationSteps.add(new RPNStep(RPNElementType.SKIP, null));
                    generationSteps.add(new RPNStep(RPNElementType.SKIP, null));
                    generationSteps.add(new RPNStep(RPNElementType.OPERATION, "s"));
                    generationSteps.add(new RPNStep(RPNElementType.VARIABLE, null));
                    generationSteps.add(new RPNStep(RPNElementType.SKIP, null));
                    generationSteps.add(new RPNStep(RPNElementType.SKIP, null));
                    mag.add(new NonTerminal(CFGState.B));
                    mag.add(new Terminal(TokenType.DOT_COMMA));
                    mag.add(new Terminal(TokenType.RIGHT_ROUND_BR));
                    mag.add(new Terminal(TokenType.VARIABLE));
                    mag.add(new Terminal(TokenType.LEFT_ROUND_BR));

                    mag.add(new Terminal(TokenType.SCAN));
                }
        );

        generationTable.get(CFGState.B).put(
                TokenType.PRINT, () -> {
                    generationSteps.add(new RPNStep(RPNElementType.SKIP, null));
                    generationSteps.add(new RPNStep(RPNElementType.SKIP, null));
                    generationSteps.add(new RPNStep(RPNElementType.OPERATION, "p"));
                    generationSteps.add(new RPNStep(RPNElementType.SKIP, null));
                    generationSteps.add(new RPNStep(RPNElementType.SKIP, null));
                    generationSteps.add(new RPNStep(RPNElementType.SKIP, null));
                    mag.add(new NonTerminal(CFGState.B));
                    mag.add(new Terminal(TokenType.DOT_COMMA));
                    mag.add(new Terminal(TokenType.RIGHT_ROUND_BR));
                    mag.add(new NonTerminal(CFGState.J));
                    mag.add(new Terminal(TokenType.LEFT_ROUND_BR));

                    mag.add(new Terminal(TokenType.PRINT));
                }
        );
        //Правила для M

        generationTable.get(CFGState.M).put(
                TokenType.LEFT_FIG_BR, () -> {
                    generationSteps.add(new RPNStep(RPNElementType.SKIP, null));
                    generationSteps.add(new RPNStep(RPNElementType.SKIP, null));
                    generationSteps.add(new RPNStep(RPNElementType.SKIP, null));
                    generationSteps.add(new RPNStep(RPNElementType.SKIP, null));
                    mag.add(new NonTerminal(CFGState.B));
                    mag.add(new Terminal(TokenType.RIGHT_FIG_BR));
                    mag.add(new NonTerminal(CFGState.B));

                    mag.add(new Terminal(TokenType.LEFT_FIG_BR));
                }
        );

        generationTable.get(CFGState.M).put(
                TokenType.IF, () -> {
                    generationSteps.add(new RPNStep(RPNElementType.SKIP, null));
                    generationSteps.add(new RPNStep(RPNElementType.PROGRAM, "3"));
                    generationSteps.add(new RPNStep(RPNElementType.SKIP, null));
                    generationSteps.add(new RPNStep(RPNElementType.SKIP, null));
                    generationSteps.add(new RPNStep(RPNElementType.PROGRAM, "1"));
                    generationSteps.add(new RPNStep(RPNElementType.SKIP, null));
                    generationSteps.add(new RPNStep(RPNElementType.SKIP, null));
                    generationSteps.add(new RPNStep(RPNElementType.SKIP, null));

                    mag.add(new NonTerminal(CFGState.B));
                    mag.add(new Terminal(TokenType.RIGHT_FIG_BR));
                    mag.add(new NonTerminal(CFGState.B));
                    mag.add(new Terminal(TokenType.LEFT_FIG_BR));
                    mag.add(new Terminal(TokenType.RIGHT_ROUND_BR));
                    mag.add(new NonTerminal(CFGState.J));
                    mag.add(new Terminal(TokenType.LEFT_ROUND_BR));

                    mag.add(new Terminal(TokenType.IF));
                }
        );
        //Правила для D

        generationTable.get(CFGState.D).put(
                TokenType.LEFT_SQR_BR, () -> {
                    generationSteps.add(new RPNStep(RPNElementType.SKIP, null));
                    generationSteps.add(new RPNStep(RPNElementType.SKIP, null));
                    generationSteps.add(new RPNStep(RPNElementType.SKIP, null));
                    generationSteps.add(new RPNStep(RPNElementType.SKIP, null));
                    generationSteps.add(new RPNStep(RPNElementType.OPERATION, "i"));

                    generationSteps.add(new RPNStep(RPNElementType.SKIP, null));
                    generationSteps.add(new RPNStep(RPNElementType.SKIP, null));
                    mag.add(new NonTerminal(CFGState.B));
                    mag.add(new Terminal(TokenType.DOT_COMMA));
                    mag.add(new NonTerminal(CFGState.J));
                    mag.add(new NonTerminal(CFGState.K));
                    mag.add(new Terminal(TokenType.RIGHT_SQR_BR));
                    mag.add(new NonTerminal(CFGState.J));

                    mag.add(new Terminal(TokenType.LEFT_SQR_BR));
                }
        );

        generationTable.get(CFGState.D).put(
                TokenType.ASSIGNMENT, () -> {


                    generationSteps.add(new RPNStep(RPNElementType.SKIP, null));
                    generationSteps.add(new RPNStep(RPNElementType.OPERATION, "="));
                    generationSteps.add(new RPNStep(RPNElementType.SKIP, null));
                    generationSteps.add(new RPNStep(RPNElementType.SKIP, null));
                    mag.add(new NonTerminal(CFGState.B));
                    mag.add(new Terminal(TokenType.DOT_COMMA));
                    mag.add(new NonTerminal(CFGState.J));

                    mag.add(new Terminal(TokenType.ASSIGNMENT));
                }
        );

        generationTable.get(CFGState.D).put(
                TokenType.PLUS_EQUALS, () -> {
                    generationSteps.add(new RPNStep(RPNElementType.SKIP, null));
                    generationSteps.add(new RPNStep(RPNElementType.OPERATION, "+="));

                    generationSteps.add(new RPNStep(RPNElementType.SKIP, null));
                    generationSteps.add(new RPNStep(RPNElementType.SKIP, null));
                    mag.add(new NonTerminal(CFGState.B));
                    mag.add(new Terminal(TokenType.DOT_COMMA));
                    mag.add(new NonTerminal(CFGState.J));

                    mag.add(new Terminal(TokenType.PLUS_EQUALS));
                }
        );

        generationTable.get(CFGState.D).put(
                TokenType.MINUS_EQUALS, () -> {
                    generationSteps.add(new RPNStep(RPNElementType.SKIP, null));
                    generationSteps.add(new RPNStep(RPNElementType.OPERATION, "-="));

                    generationSteps.add(new RPNStep(RPNElementType.SKIP, null));
                    generationSteps.add(new RPNStep(RPNElementType.SKIP, null));
                    mag.add(new NonTerminal(CFGState.B));
                    mag.add(new Terminal(TokenType.DOT_COMMA));
                    mag.add(new NonTerminal(CFGState.J));

                    mag.add(new Terminal(TokenType.MINUS_EQUALS));
                }
        );

        generationTable.get(CFGState.D).put(
                TokenType.MULTIPLY_EQUALS, () -> {
                    generationSteps.add(new RPNStep(RPNElementType.SKIP, null));
                    generationSteps.add(new RPNStep(RPNElementType.OPERATION, "*="));

                    generationSteps.add(new RPNStep(RPNElementType.SKIP, null));
                    generationSteps.add(new RPNStep(RPNElementType.SKIP, null));
                    mag.add(new NonTerminal(CFGState.B));
                    mag.add(new Terminal(TokenType.DOT_COMMA));
                    mag.add(new NonTerminal(CFGState.J));

                    mag.add(new Terminal(TokenType.MULTIPLY_EQUALS));
                }
        );

        generationTable.get(CFGState.D).put(
                TokenType.DIV_EQUALS, () -> {
                    generationSteps.add(new RPNStep(RPNElementType.SKIP, null));
                    generationSteps.add(new RPNStep(RPNElementType.OPERATION, "/="));

                    generationSteps.add(new RPNStep(RPNElementType.SKIP, null));
                    generationSteps.add(new RPNStep(RPNElementType.SKIP, null));
                    mag.add(new NonTerminal(CFGState.B));
                    mag.add(new Terminal(TokenType.DOT_COMMA));
                    mag.add(new NonTerminal(CFGState.J));

                    mag.add(new Terminal(TokenType.DIV_EQUALS));
                }
        );
        //Правила для J

        generationTable.get(CFGState.J).put(
                TokenType.VARIABLE, () -> {
                    generationSteps.add(new RPNStep(RPNElementType.VARIABLE, null));
                    generationSteps.add(new RPNStep(RPNElementType.SKIP, null));
                    mag.add(new NonTerminal(CFGState.L));

                    mag.add(new Terminal(TokenType.VARIABLE));
                }
        );

        generationTable.get(CFGState.J).put(
                TokenType.CONST, () -> {
                    generationSteps.add(new RPNStep(RPNElementType.SKIP, null));
                    generationSteps.add(new RPNStep(RPNElementType.SKIP, null));
                    generationSteps.add(new RPNStep(RPNElementType.SKIP, null));
                    generationSteps.add(new RPNStep(RPNElementType.SKIP, null));
                    generationSteps.add(new RPNStep(RPNElementType.SKIP, null));
                    generationSteps.add(new RPNStep(RPNElementType.SKIP, null));
                    generationSteps.add(new RPNStep(RPNElementType.SKIP, null));
                    generationSteps.add(new RPNStep(RPNElementType.CONST, null));

                    mag.add(new NonTerminal(CFGState.N));
                    mag.add(new NonTerminal(CFGState.U));
                    mag.add(new NonTerminal(CFGState.T));
                    mag.add(new NonTerminal(CFGState.V));
                    mag.add(new NonTerminal(CFGState.W));
                    mag.add(new NonTerminal(CFGState.X));
                    mag.add(new NonTerminal(CFGState.Y));

                    mag.add(new Terminal(TokenType.CONST));
                }
        );

        generationTable.get(CFGState.J).put(
                TokenType.MINUS, () -> {
                    generationSteps.add(new RPNStep(RPNElementType.OPERATION, "-'"));

                    generationSteps.add(new RPNStep(RPNElementType.SKIP, null));
                    generationSteps.add(new RPNStep(RPNElementType.SKIP, null));
                    generationSteps.add(new RPNStep(RPNElementType.SKIP, null));
                    generationSteps.add(new RPNStep(RPNElementType.SKIP, null));
                    generationSteps.add(new RPNStep(RPNElementType.SKIP, null));
                    generationSteps.add(new RPNStep(RPNElementType.SKIP, null));
                    generationSteps.add(new RPNStep(RPNElementType.SKIP, null));
                    generationSteps.add(new RPNStep(RPNElementType.SKIP, null));
                    mag.add(new NonTerminal(CFGState.N));
                    mag.add(new NonTerminal(CFGState.U));
                    mag.add(new NonTerminal(CFGState.T));
                    mag.add(new NonTerminal(CFGState.V));
                    mag.add(new NonTerminal(CFGState.W));
                    mag.add(new NonTerminal(CFGState.X));
                    mag.add(new NonTerminal(CFGState.Y));
                    mag.add(new NonTerminal(CFGState.R));

                    mag.add(new Terminal(TokenType.MINUS));
                }
        );

        generationTable.get(CFGState.J).put(
                TokenType.NOT, () -> {
                    generationSteps.add(new RPNStep(RPNElementType.OPERATION, "!'"));

                    generationSteps.add(new RPNStep(RPNElementType.SKIP, null));
                    generationSteps.add(new RPNStep(RPNElementType.SKIP, null));
                    generationSteps.add(new RPNStep(RPNElementType.SKIP, null));
                    generationSteps.add(new RPNStep(RPNElementType.SKIP, null));
                    generationSteps.add(new RPNStep(RPNElementType.SKIP, null));
                    generationSteps.add(new RPNStep(RPNElementType.SKIP, null));
                    generationSteps.add(new RPNStep(RPNElementType.SKIP, null));
                    generationSteps.add(new RPNStep(RPNElementType.SKIP, null));
                    mag.add(new NonTerminal(CFGState.N));
                    mag.add(new NonTerminal(CFGState.U));
                    mag.add(new NonTerminal(CFGState.T));
                    mag.add(new NonTerminal(CFGState.V));
                    mag.add(new NonTerminal(CFGState.W));
                    mag.add(new NonTerminal(CFGState.X));
                    mag.add(new NonTerminal(CFGState.Y));
                    mag.add(new NonTerminal(CFGState.R));

                    mag.add(new Terminal(TokenType.MINUS));
                }
        );
        //Правила для L

        generationTable.get(CFGState.L).put(
                TokenType.PLUS, () -> {
                    generationSteps.add(new RPNStep(RPNElementType.OPERATION, "+"));

                    generationSteps.add(new RPNStep(RPNElementType.SKIP, null));
                    generationSteps.add(new RPNStep(RPNElementType.SKIP, null));
                    generationSteps.add(new RPNStep(RPNElementType.SKIP, null));
                    generationSteps.add(new RPNStep(RPNElementType.SKIP, null));
                    generationSteps.add(new RPNStep(RPNElementType.SKIP, null));
                    generationSteps.add(new RPNStep(RPNElementType.SKIP, null));
                    generationSteps.add(new RPNStep(RPNElementType.SKIP, null));
                    mag.add(new NonTerminal(CFGState.N));
                    mag.add(new NonTerminal(CFGState.U));
                    mag.add(new NonTerminal(CFGState.T));
                    mag.add(new NonTerminal(CFGState.V));
                    mag.add(new NonTerminal(CFGState.W));
                    mag.add(new NonTerminal(CFGState.X));
                    mag.add(new NonTerminal(CFGState.J));

                    mag.add(new Terminal(TokenType.PLUS));
                }
        );

        generationTable.get(CFGState.L).put(
                TokenType.MINUS, () -> {
                    generationSteps.add(new RPNStep(RPNElementType.OPERATION, "-"));

                    generationSteps.add(new RPNStep(RPNElementType.SKIP, null));
                    generationSteps.add(new RPNStep(RPNElementType.SKIP, null));
                    generationSteps.add(new RPNStep(RPNElementType.SKIP, null));
                    generationSteps.add(new RPNStep(RPNElementType.SKIP, null));
                    generationSteps.add(new RPNStep(RPNElementType.SKIP, null));
                    generationSteps.add(new RPNStep(RPNElementType.SKIP, null));
                    generationSteps.add(new RPNStep(RPNElementType.SKIP, null));
                    mag.add(new NonTerminal(CFGState.N));
                    mag.add(new NonTerminal(CFGState.U));
                    mag.add(new NonTerminal(CFGState.T));
                    mag.add(new NonTerminal(CFGState.V));
                    mag.add(new NonTerminal(CFGState.W));
                    mag.add(new NonTerminal(CFGState.X));
                    mag.add(new NonTerminal(CFGState.J));

                    mag.add(new Terminal(TokenType.MINUS));
                }
        );

        generationTable.get(CFGState.L).put(
                TokenType.MULTIPLY, () -> {
                    generationSteps.add(new RPNStep(RPNElementType.OPERATION, "*"));

                    generationSteps.add(new RPNStep(RPNElementType.SKIP, null));
                    generationSteps.add(new RPNStep(RPNElementType.SKIP, null));
                    generationSteps.add(new RPNStep(RPNElementType.SKIP, null));
                    generationSteps.add(new RPNStep(RPNElementType.SKIP, null));
                    generationSteps.add(new RPNStep(RPNElementType.SKIP, null));
                    generationSteps.add(new RPNStep(RPNElementType.SKIP, null));
                    generationSteps.add(new RPNStep(RPNElementType.SKIP, null));
                    generationSteps.add(new RPNStep(RPNElementType.SKIP, null));
                    mag.add(new NonTerminal(CFGState.N));
                    mag.add(new NonTerminal(CFGState.U));
                    mag.add(new NonTerminal(CFGState.T));
                    mag.add(new NonTerminal(CFGState.V));
                    mag.add(new NonTerminal(CFGState.W));
                    mag.add(new NonTerminal(CFGState.X));
                    mag.add(new NonTerminal(CFGState.Y));
                    mag.add(new NonTerminal(CFGState.J));

                    mag.add(new Terminal(TokenType.MULTIPLY));
                }
        );

        generationTable.get(CFGState.L).put(
                TokenType.DIV, () -> {
                    generationSteps.add(new RPNStep(RPNElementType.OPERATION, "/"));

                    generationSteps.add(new RPNStep(RPNElementType.SKIP, null));
                    generationSteps.add(new RPNStep(RPNElementType.SKIP, null));
                    generationSteps.add(new RPNStep(RPNElementType.SKIP, null));
                    generationSteps.add(new RPNStep(RPNElementType.SKIP, null));
                    generationSteps.add(new RPNStep(RPNElementType.SKIP, null));
                    generationSteps.add(new RPNStep(RPNElementType.SKIP, null));
                    generationSteps.add(new RPNStep(RPNElementType.SKIP, null));
                    generationSteps.add(new RPNStep(RPNElementType.SKIP, null));

                    mag.add(new NonTerminal(CFGState.N));
                    mag.add(new NonTerminal(CFGState.U));
                    mag.add(new NonTerminal(CFGState.T));
                    mag.add(new NonTerminal(CFGState.V));
                    mag.add(new NonTerminal(CFGState.W));
                    mag.add(new NonTerminal(CFGState.X));
                    mag.add(new NonTerminal(CFGState.Y));
                    mag.add(new NonTerminal(CFGState.J));
                    mag.add(new Terminal(TokenType.MULTIPLY));
                }
        );

        generationTable.get(CFGState.L).put(
                TokenType.GREATER, () -> {
                    generationSteps.add(new RPNStep(RPNElementType.OPERATION, ">"));

                    generationSteps.add(new RPNStep(RPNElementType.SKIP, null));
                    generationSteps.add(new RPNStep(RPNElementType.SKIP, null));
                    generationSteps.add(new RPNStep(RPNElementType.SKIP, null));
                    generationSteps.add(new RPNStep(RPNElementType.SKIP, null));
                    generationSteps.add(new RPNStep(RPNElementType.SKIP, null));
                    generationSteps.add(new RPNStep(RPNElementType.SKIP, null));
                    mag.add(new NonTerminal(CFGState.N));
                    mag.add(new NonTerminal(CFGState.U));
                    mag.add(new NonTerminal(CFGState.T));
                    mag.add(new NonTerminal(CFGState.V));
                    mag.add(new NonTerminal(CFGState.W));
                    mag.add(new NonTerminal(CFGState.J));

                    mag.add(new Terminal(TokenType.GREATER));
                }
        );

        generationTable.get(CFGState.L).put(
                TokenType.LESS, () -> {
                    generationSteps.add(new RPNStep(RPNElementType.OPERATION, "<"));

                    generationSteps.add(new RPNStep(RPNElementType.SKIP, null));
                    generationSteps.add(new RPNStep(RPNElementType.SKIP, null));
                    generationSteps.add(new RPNStep(RPNElementType.SKIP, null));
                    generationSteps.add(new RPNStep(RPNElementType.SKIP, null));
                    generationSteps.add(new RPNStep(RPNElementType.SKIP, null));
                    generationSteps.add(new RPNStep(RPNElementType.SKIP, null));
                    mag.add(new NonTerminal(CFGState.N));
                    mag.add(new NonTerminal(CFGState.U));
                    mag.add(new NonTerminal(CFGState.T));
                    mag.add(new NonTerminal(CFGState.V));
                    mag.add(new NonTerminal(CFGState.W));
                    mag.add(new NonTerminal(CFGState.J));

                    mag.add(new Terminal(TokenType.LESS));
                }
        );

        generationTable.get(CFGState.L).put(
                TokenType.AND, () -> {
                    generationSteps.add(new RPNStep(RPNElementType.OPERATION, "&"));
                    generationSteps.add(new RPNStep(RPNElementType.SKIP, null));
                    generationSteps.add(new RPNStep(RPNElementType.SKIP, null));
                    generationSteps.add(new RPNStep(RPNElementType.SKIP, null));
                    generationSteps.add(new RPNStep(RPNElementType.SKIP, null));
                    mag.add(new NonTerminal(CFGState.N));
                    mag.add(new NonTerminal(CFGState.U));
                    mag.add(new NonTerminal(CFGState.T));
                    mag.add(new NonTerminal(CFGState.J));

                    mag.add(new Terminal(TokenType.AND));
                }
        );

        generationTable.get(CFGState.L).put(
                TokenType.OR, () -> {
                    generationSteps.add(new RPNStep(RPNElementType.OPERATION, "&"));

                    generationSteps.add(new RPNStep(RPNElementType.SKIP, null));
                    generationSteps.add(new RPNStep(RPNElementType.SKIP, null));
                    generationSteps.add(new RPNStep(RPNElementType.SKIP, null));
                    mag.add(new NonTerminal(CFGState.N));
                    mag.add(new NonTerminal(CFGState.U));
                    mag.add(new NonTerminal(CFGState.T));

                    mag.add(new Terminal(TokenType.OR));
                }
        );

        generationTable.get(CFGState.L).put(
                TokenType.LEFT_SQR_BR, () -> {
                    generationSteps.add(new RPNStep(RPNElementType.SKIP, null));
                    generationSteps.add(new RPNStep(RPNElementType.SKIP, null));
                    generationSteps.add(new RPNStep(RPNElementType.SKIP, null));
                    generationSteps.add(new RPNStep(RPNElementType.SKIP, null));
                    generationSteps.add(new RPNStep(RPNElementType.SKIP, null));
                    generationSteps.add(new RPNStep(RPNElementType.SKIP, null));
                    generationSteps.add(new RPNStep(RPNElementType.SKIP, null));
                    generationSteps.add(new RPNStep(RPNElementType.OPERATION, "i"));
                    generationSteps.add(new RPNStep(RPNElementType.SKIP, null));
                    generationSteps.add(new RPNStep(RPNElementType.SKIP, null));

                    mag.add(new NonTerminal(CFGState.N));
                    mag.add(new NonTerminal(CFGState.U));
                    mag.add(new NonTerminal(CFGState.T));
                    mag.add(new NonTerminal(CFGState.V));
                    mag.add(new NonTerminal(CFGState.W));
                    mag.add(new NonTerminal(CFGState.X));
                    mag.add(new NonTerminal(CFGState.Y));
                    mag.add(new Terminal(TokenType.RIGHT_SQR_BR));
                    mag.add(new NonTerminal(CFGState.J));
                    mag.add(new Terminal(TokenType.LEFT_SQR_BR));

                }
        );

        generationTable.get(CFGState.L).put(
                TokenType.COMMA, () -> {
                    generationSteps.add(new RPNStep(RPNElementType.SKIP, null));
                    generationSteps.add(new RPNStep(RPNElementType.SKIP, null));
                    mag.add(new NonTerminal(CFGState.J));

                    mag.add(new Terminal(TokenType.COMMA));
                }
        );

        generationTable.get(CFGState.L).put(
                TokenType.GREATER_EQUALS, () -> {
                    generationSteps.add(new RPNStep(RPNElementType.OPERATION, ">="));

                    generationSteps.add(new RPNStep(RPNElementType.SKIP, null));
                    generationSteps.add(new RPNStep(RPNElementType.SKIP, null));
                    generationSteps.add(new RPNStep(RPNElementType.SKIP, null));
                    generationSteps.add(new RPNStep(RPNElementType.SKIP, null));
                    generationSteps.add(new RPNStep(RPNElementType.SKIP, null));
                    generationSteps.add(new RPNStep(RPNElementType.SKIP, null));
                    mag.add(new NonTerminal(CFGState.N));
                    mag.add(new NonTerminal(CFGState.U));
                    mag.add(new NonTerminal(CFGState.T));
                    mag.add(new NonTerminal(CFGState.V));
                    mag.add(new NonTerminal(CFGState.W));
                    mag.add(new NonTerminal(CFGState.J));

                    mag.add(new Terminal(TokenType.GREATER_EQUALS));
                }
        );

        generationTable.get(CFGState.L).put(
                TokenType.LESS_EQUALS, () -> {
                    generationSteps.add(new RPNStep(RPNElementType.OPERATION, "<="));

                    generationSteps.add(new RPNStep(RPNElementType.SKIP, null));
                    generationSteps.add(new RPNStep(RPNElementType.SKIP, null));
                    generationSteps.add(new RPNStep(RPNElementType.SKIP, null));
                    generationSteps.add(new RPNStep(RPNElementType.SKIP, null));
                    generationSteps.add(new RPNStep(RPNElementType.SKIP, null));
                    generationSteps.add(new RPNStep(RPNElementType.SKIP, null));

                    mag.add(new NonTerminal(CFGState.N));
                    mag.add(new NonTerminal(CFGState.U));
                    mag.add(new NonTerminal(CFGState.T));
                    mag.add(new NonTerminal(CFGState.V));
                    mag.add(new NonTerminal(CFGState.W));
                    mag.add(new NonTerminal(CFGState.J));
                    mag.add(new Terminal(TokenType.LESS_EQUALS));
                }
        );

        generationTable.get(CFGState.L).put(
                TokenType.EQUALS, () -> {
                    generationSteps.add(new RPNStep(RPNElementType.OPERATION, "=="));

                    generationSteps.add(new RPNStep(RPNElementType.SKIP, null));
                    generationSteps.add(new RPNStep(RPNElementType.SKIP, null));
                    generationSteps.add(new RPNStep(RPNElementType.SKIP, null));
                    generationSteps.add(new RPNStep(RPNElementType.SKIP, null));
                    generationSteps.add(new RPNStep(RPNElementType.SKIP, null));
                    mag.add(new NonTerminal(CFGState.N));
                    mag.add(new NonTerminal(CFGState.U));
                    mag.add(new NonTerminal(CFGState.T));
                    mag.add(new NonTerminal(CFGState.V));
                    mag.add(new NonTerminal(CFGState.J));

                    mag.add(new Terminal(TokenType.EQUALS));
                }
        );

        generationTable.get(CFGState.L).put(
                TokenType.NOT_EQUALS, () -> {
                    generationSteps.add(new RPNStep(RPNElementType.OPERATION, "!="));

                    generationSteps.add(new RPNStep(RPNElementType.SKIP, null));
                    generationSteps.add(new RPNStep(RPNElementType.SKIP, null));
                    generationSteps.add(new RPNStep(RPNElementType.SKIP, null));
                    generationSteps.add(new RPNStep(RPNElementType.SKIP, null));
                    generationSteps.add(new RPNStep(RPNElementType.SKIP, null));
                    mag.add(new NonTerminal(CFGState.N));
                    mag.add(new NonTerminal(CFGState.U));
                    mag.add(new NonTerminal(CFGState.T));
                    mag.add(new NonTerminal(CFGState.V));
                    mag.add(new NonTerminal(CFGState.J));

                    mag.add(new Terminal(TokenType.NOT_EQUALS));
                }
        );

        //Правила для N

        generationTable.get(CFGState.N).put(
                TokenType.COMMA, () -> {
                    generationSteps.add(new RPNStep(RPNElementType.SKIP, null));
                    generationSteps.add(new RPNStep(RPNElementType.SKIP, null));
                    mag.add(new NonTerminal(CFGState.J));

                    mag.add(new Terminal(TokenType.COMMA));
                }
        );

        //Правила для G

        generationTable.get(CFGState.G).put(
                TokenType.INT, () -> {
                    generationSteps.add(new RPNStep(RPNElementType.PROGRAM, "11"));
                    mag.add(new Terminal(TokenType.INT));
                }
        );

        generationTable.get(CFGState.G).put(
                TokenType.DOUBLE, () -> {
                    generationSteps.add(new RPNStep(RPNElementType.PROGRAM, "12"));
                    mag.add(new Terminal(TokenType.DOUBLE));
                }
        );

        generationTable.get(CFGState.G).put(
                TokenType.BOOLEAN, () -> {
                    generationSteps.add(new RPNStep(RPNElementType.PROGRAM, "13"));
                    mag.add(new Terminal(TokenType.BOOLEAN));
                }
        );

        //Правила для K

        generationTable.get(CFGState.K).put(
                TokenType.EQUALS, () -> {
                    generationSteps.add(new RPNStep(RPNElementType.OPERATION, "="));
                    mag.add(new Terminal(TokenType.EQUALS));
                }
        );

        generationTable.get(CFGState.K).put(
                TokenType.PLUS, () -> {
                    generationSteps.add(new RPNStep(RPNElementType.OPERATION, "+"));
                    mag.add(new Terminal(TokenType.EQUALS));
                }
        );

        generationTable.get(CFGState.K).put(
                TokenType.MINUS, () -> {
                    generationSteps.add(new RPNStep(RPNElementType.OPERATION, "-"));
                    mag.add(new Terminal(TokenType.EQUALS));
                }
        );

        generationTable.get(CFGState.K).put(
                TokenType.MULTIPLY, () -> {
                    generationSteps.add(new RPNStep(RPNElementType.OPERATION, "*"));
                    mag.add(new Terminal(TokenType.EQUALS));
                }
        );

        generationTable.get(CFGState.K).put(
                TokenType.DIV, () -> {
                    generationSteps.add(new RPNStep(RPNElementType.OPERATION, "/"));
                    mag.add(new Terminal(TokenType.EQUALS));
                }
        );

        //Правила для U

        generationTable.get(CFGState.U).put(
                TokenType.OR, () -> {
                    generationSteps.add(new RPNStep(RPNElementType.OPERATION, "="));

                    generationSteps.add(new RPNStep(RPNElementType.SKIP, null));
                    generationSteps.add(new RPNStep(RPNElementType.SKIP, null));
                    mag.add(new NonTerminal(CFGState.U));
                    mag.add(new NonTerminal(CFGState.J));

                    mag.add(new Terminal(TokenType.OR));

                }
        );

        //Правила для T

        generationTable.get(CFGState.T).put(
                TokenType.AND, () -> {
                    generationSteps.add(new RPNStep(RPNElementType.OPERATION, "&"));

                    generationSteps.add(new RPNStep(RPNElementType.SKIP, null));
                    generationSteps.add(new RPNStep(RPNElementType.SKIP, null));
                    mag.add(new NonTerminal(CFGState.W));
                    mag.add(new NonTerminal(CFGState.J));

                    mag.add(new Terminal(TokenType.AND));

                }
        );


        //Правила для V

        generationTable.get(CFGState.V).put(
                TokenType.EQUALS, () -> {
                    generationSteps.add(new RPNStep(RPNElementType.OPERATION, "=="));

                    generationSteps.add(new RPNStep(RPNElementType.SKIP, null));
                    generationSteps.add(new RPNStep(RPNElementType.SKIP, null));
                    mag.add(new NonTerminal(CFGState.W));
                    mag.add(new NonTerminal(CFGState.J));

                    mag.add(new Terminal(TokenType.EQUALS));

                }
        );

        generationTable.get(CFGState.V).put(
                TokenType.NOT_EQUALS, () -> {
                    generationSteps.add(new RPNStep(RPNElementType.OPERATION, "!="));

                    generationSteps.add(new RPNStep(RPNElementType.SKIP, null));
                    generationSteps.add(new RPNStep(RPNElementType.SKIP, null));
                    mag.add(new NonTerminal(CFGState.W));
                    mag.add(new NonTerminal(CFGState.J));

                    mag.add(new Terminal(TokenType.NOT_EQUALS));

                }
        );

        //Правила для W

        generationTable.get(CFGState.W).put(
                TokenType.GREATER, () -> {
                    generationSteps.add(new RPNStep(RPNElementType.OPERATION, ">"));

                    generationSteps.add(new RPNStep(RPNElementType.SKIP, null));
                    generationSteps.add(new RPNStep(RPNElementType.SKIP, null));
                    mag.add(new NonTerminal(CFGState.W));
                    mag.add(new NonTerminal(CFGState.J));

                    mag.add(new Terminal(TokenType.GREATER));

                }
        );

        generationTable.get(CFGState.W).put(
                TokenType.LESS, () -> {
                    generationSteps.add(new RPNStep(RPNElementType.OPERATION, "<"));
                    generationSteps.add(new RPNStep(RPNElementType.SKIP, null));
                    generationSteps.add(new RPNStep(RPNElementType.SKIP, null));
                    mag.add(new NonTerminal(CFGState.W));
                    mag.add(new NonTerminal(CFGState.J));

                    mag.add(new Terminal(TokenType.LESS));

                }
        );

        generationTable.get(CFGState.W).put(
                TokenType.GREATER_EQUALS, () -> {
                    generationSteps.add(new RPNStep(RPNElementType.OPERATION, ">="));
                    generationSteps.add(new RPNStep(RPNElementType.SKIP, null));
                    generationSteps.add(new RPNStep(RPNElementType.SKIP, null));
                    mag.add(new NonTerminal(CFGState.W));
                    mag.add(new NonTerminal(CFGState.J));

                    mag.add(new Terminal(TokenType.GREATER_EQUALS));

                }
        );

        generationTable.get(CFGState.W).put(
                TokenType.LESS_EQUALS, () -> {
                    generationSteps.add(new RPNStep(RPNElementType.OPERATION, "<="));
                    generationSteps.add(new RPNStep(RPNElementType.SKIP, null));
                    generationSteps.add(new RPNStep(RPNElementType.SKIP, null));
                    mag.add(new NonTerminal(CFGState.W));
                    mag.add(new NonTerminal(CFGState.J));

                    mag.add(new Terminal(TokenType.LESS_EQUALS));

                }
        );

        //Правила для X

        generationTable.get(CFGState.X).put(
                TokenType.PLUS, () -> {
                    generationSteps.add(new RPNStep(RPNElementType.OPERATION, "+"));
                    generationSteps.add(new RPNStep(RPNElementType.SKIP, null));
                    generationSteps.add(new RPNStep(RPNElementType.SKIP, null));
                    mag.add(new NonTerminal(CFGState.X));
                    mag.add(new NonTerminal(CFGState.J));

                    mag.add(new Terminal(TokenType.GREATER));

                }
        );

        generationTable.get(CFGState.X).put(
                TokenType.MINUS, () -> {
                    generationSteps.add(new RPNStep(RPNElementType.OPERATION, "-"));
                    generationSteps.add(new RPNStep(RPNElementType.SKIP, null));
                    generationSteps.add(new RPNStep(RPNElementType.SKIP, null));
                    mag.add(new NonTerminal(CFGState.X));
                    mag.add(new NonTerminal(CFGState.J));

                    mag.add(new Terminal(TokenType.MINUS));

                }
        );

        //Правила для Y

        generationTable.get(CFGState.Y).put(
                TokenType.MULTIPLY, () -> {
                    generationSteps.add(new RPNStep(RPNElementType.OPERATION, "*"));
                    generationSteps.add(new RPNStep(RPNElementType.SKIP, null));
                    generationSteps.add(new RPNStep(RPNElementType.SKIP, null));
                    mag.add(new NonTerminal(CFGState.X));

                    mag.add(new NonTerminal(CFGState.J));

                    mag.add(new Terminal(TokenType.MINUS));

                }
        );

        generationTable.get(CFGState.Y).put(
                TokenType.DIV, () -> {
                    generationSteps.add(new RPNStep(RPNElementType.OPERATION, "/"));
                    generationSteps.add(new RPNStep(RPNElementType.SKIP, null));
                    generationSteps.add(new RPNStep(RPNElementType.SKIP, null));
                    mag.add(new NonTerminal(CFGState.X));
                    mag.add(new NonTerminal(CFGState.J));

                    mag.add(new Terminal(TokenType.GREATER));

                }
        );

        //Правила для R

        generationTable.get(CFGState.R).put(
                TokenType.VARIABLE, () -> {
                    generationSteps.add(new RPNStep(RPNElementType.SKIP, null));
                    generationSteps.add(new RPNStep(RPNElementType.VARIABLE, null));
                    mag.add(new NonTerminal(CFGState.C));

                    mag.add(new Terminal(TokenType.VARIABLE));

                }
        );

        generationTable.get(CFGState.R).put(
                TokenType.CONST, () -> {
                    generationSteps.add(new RPNStep(RPNElementType.CONST, null));

                    mag.add(new Terminal(TokenType.CONST));
                }
        );

        generationTable.get(CFGState.R).put(
                TokenType.LEFT_ROUND_BR, () -> {
                    generationSteps.add(new RPNStep(RPNElementType.SKIP, null));
                    generationSteps.add(new RPNStep(RPNElementType.SKIP, null));
                    generationSteps.add(new RPNStep(RPNElementType.SKIP, null));
                    mag.add(new Terminal(TokenType.RIGHT_ROUND_BR));
                    mag.add(new NonTerminal(CFGState.J));

                    mag.add(new Terminal(TokenType.LEFT_ROUND_BR));

                }
        );

        generationTable.get(CFGState.C).put(
                TokenType.LEFT_SQR_BR, () -> {
                    generationSteps.add(new RPNStep(RPNElementType.SKIP, null));
                    generationSteps.add(new RPNStep(RPNElementType.SKIP, null));
                    generationSteps.add(new RPNStep(RPNElementType.SKIP, null));
                    mag.add(new Terminal(TokenType.RIGHT_SQR_BR));
                    mag.add(new NonTerminal(CFGState.J));

                    mag.add(new Terminal(TokenType.LEFT_SQR_BR));

                }
        );


    }

    public static List<RPNStep> generateRpnFromTokensList(List<Token> userInput) {

        resultRPN = new ArrayList<>();
        mag = new Stack<>();

        mag.add(new NonTerminal(CFGState.B));

        generationSteps = new Stack<>();
        generationSteps.add(new RPNStep(RPNElementType.SKIP, null));


        input = userInput;

        for (int i = 0; i < input.size() || !generationSteps.empty() || !mag.empty(); ) {
            if (i < input.size())
                currentToken = userInput.get(i);

            System.out.println("mag: " + mag);
            System.out.println("steps: " + generationSteps);
            System.out.println("result: " + resultRPN);
            System.out.println("current token: " + currentToken);
            System.out.println();

            MagElement magElement = mag.pop();

            if (magElement.isTerminal()) {
                Terminal terminal = (Terminal) magElement;

                if (currentToken.getType() == terminal.getValue()) {
                    i++;
                    if(i < input.size() && currentToken.getType() != TokenType.VARIABLE && currentToken.getType() != TokenType.CONST) {
                        currentToken = userInput.get(i);
                    }
                }
            } else {
                NonTerminal nonTerminal = (NonTerminal) magElement;
                Runnable runnable = generationTable.get(nonTerminal.getValue()).get(currentToken.getType());
                if (runnable != null)
                    runnable.run();
            }


            RPNStep step = generationSteps.pop();

            if (step.getType() != RPNElementType.SKIP) {
                if (step.getValue() == null)
                    step.setValue(currentToken.getValue());
                resultRPN.add(step);
            }
        }
        return resultRPN;
    }

}
