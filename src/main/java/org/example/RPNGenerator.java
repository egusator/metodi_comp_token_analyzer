package org.example;

import java.util.*;


public class RPNGenerator {

    private static Stack<RPNGenerationStep> generationSteps;

    private static int markerNumber = 0;

    private static int whileLastMarkerNumber;

    private static Map<TableType, List<String>> variableTables;

    private static Map<String, Runnable> programs;
    private static TableType CURRENT_TABLE_TYPE;

    private static TableType previousStepTypeForArr;

    private static List<RPNStep> resultRPN;

    private static Token currentToken;

    private static Token prevVariable;

    private static Stack<MagElement> mag;

    private static HashMap<String, Integer> markerValues;

    private static Stack<String> markers;

    private static Stack<String> markersForElse;


    private static Stack<String> markerNamesForIf;

    private static Stack<String> markerNamesForWhile;

    private static Map<CFGState, HashMap<TokenType, Runnable>> generationTable;

    static {
        generationSteps = new Stack<>();
        resultRPN = new ArrayList<>();
        mag = new Stack<>();
        markers = new Stack<>();
        markersForElse = new Stack<>();
        initGenerationTable();
        initPrograms();
    }

    public static List<RPNStep> generateRpnFromTokensList(Stack<Token> userInput) throws InterruptedException {

        resultRPN = new ArrayList<>();

        mag = new Stack<>();

        mag.add(new NonTerminal(CFGState.B));

        generationSteps = new Stack<>();

        currentToken = userInput.pop();
        for (int i = 0; !mag.empty(); ) {
            System.out.println();
            System.out.println("input:" + userInput);
            System.out.println("mag: " + mag);
            System.out.println("steps: " + generationSteps);
            System.out.println("result: " + resultRPN);
            System.out.println("current token: " + currentToken);
            System.out.println("prev variable: " + prevVariable);


            MagElement magElement = mag.pop();

            if (magElement.isTerminal()) {
                Terminal terminal = (Terminal) magElement;

                if (!userInput.isEmpty() && currentToken.getType() == terminal.getValue()) {
                    currentToken = userInput.pop();
                    if (currentToken.getType() == TokenType.VARIABLE)
                        prevVariable = currentToken;
                }
            } else {
                NonTerminal nonTerminal = (NonTerminal) magElement;
                System.out.println("это было с " + nonTerminal.getValue());
                Runnable runnable = generationTable.get(nonTerminal.getValue()).get(currentToken.getType());
                if (runnable != null)
                    runnable.run();
            }

            RPNGenerationStep step = null;
            if (!generationSteps.isEmpty())
                step = generationSteps.pop();


            if (step != null && step.getType() != RPNGenerationStepType.SKIP) {
                performSemanticForGen(step);
            }
        }
        System.out.println(markerValues);
        return resultRPN;
    }

    private static void performSemanticForGen(RPNGenerationStep step) {
        if (step.getType() == RPNGenerationStepType.PROGRAM) {
            Runnable program = programs.get(step.getValue());
            program.run();
        } else if (step.getType() == RPNGenerationStepType.CONST) {
            resultRPN.add(new RPNStep(RPNStepType.CONST, currentToken.getValue()));
        } else if (step.getType() == RPNGenerationStepType.VARIABLE) {
            switch (CURRENT_TABLE_TYPE) {
                case INT -> {
                    resultRPN.add(new RPNStep(RPNStepType.INT_VARIABLE, prevVariable.getValue()));
                }
                case DOUBLE -> {
                    resultRPN.add(new RPNStep(RPNStepType.DOUBLE_VARIABLE, prevVariable.getValue()));
                }
                case BOOLEAN -> {
                    resultRPN.add(new RPNStep(RPNStepType.BOOLEAN_VARIABLE, prevVariable.getValue()));
                }
                case ARRAY -> {
                    switch (previousStepTypeForArr) {
                        case INT -> {
                            resultRPN.add(new RPNStep(RPNStepType.INT_VARIABLE, prevVariable.getValue()));
                        }
                        case DOUBLE -> {
                            resultRPN.add(new RPNStep(RPNStepType.DOUBLE_VARIABLE, prevVariable.getValue()));
                        }
                        case BOOLEAN -> {
                            resultRPN.add(new RPNStep(RPNStepType.BOOLEAN_VARIABLE, prevVariable.getValue()));
                        }
                    }
                }
            }
        } else if (step.getType() == RPNGenerationStepType.OPERATION) {
            resultRPN.add(new RPNStep(RPNStepType.OPERATION, step.getValue()));
        }
    }


    private static void initPrograms() {

        variableTables = new HashMap<>();
        markerValues = new HashMap<>();

        for (TableType type : TableType.values()) {
            variableTables.put(type, new ArrayList<>());
        }

        programs = new HashMap<>();

        programs.put("1", () -> {
            markerNumber++;
            String markerName = "m" + markerNumber;

            markerValues.put(markerName, null);

            markers.add(markerName);

            resultRPN.add(new RPNStep(RPNStepType.MARKER, markerName));

            resultRPN.add(new RPNStep(RPNStepType.OPERATION, "jf"));
        });


        programs.put("2", () -> {
            String marker = markersForElse.pop();
            markerValues.put(marker, resultRPN.size() - 1);
        });

        programs.put("3", () -> {
            Integer ind = resultRPN.size() + 1;
            String marker = markers.pop();
            markerValues.put(marker, ind);

            markerNumber++;

            String markerName = "m" + markerNumber;

            markerValues.put(markerName, ind);

            markersForElse.add(markerName);

            resultRPN.add(new RPNStep(RPNStepType.MARKER, markerName));

            resultRPN.add(new RPNStep(RPNStepType.OPERATION, "j"));
        });

        programs.put("4", () -> {
            markerNumber++;
            String markerName = "m" + markerNumber;
            markers.add(markerName);
            markerValues.put(markerName, resultRPN.size() - 1);
        });

        programs.put("5", () -> {
            String markerName = markers.pop();
            markerValues.put(markerName, resultRPN.size() + 1);

            String markerName2 = markers.pop();
            resultRPN.add(new RPNStep(RPNStepType.MARKER, markerName2));
            resultRPN.add(new RPNStep(RPNStepType.OPERATION, "j"));
        });

        programs.put("11", () -> {
            CURRENT_TABLE_TYPE = TableType.INT;
            previousStepTypeForArr = CURRENT_TABLE_TYPE;
        });

        programs.put("12", () -> {
            CURRENT_TABLE_TYPE = TableType.DOUBLE;
            previousStepTypeForArr = CURRENT_TABLE_TYPE;

        });

        programs.put("13", () -> {
            CURRENT_TABLE_TYPE = TableType.BOOLEAN;
            previousStepTypeForArr = CURRENT_TABLE_TYPE;

        });

        programs.put("15", () -> {
            CURRENT_TABLE_TYPE = TableType.ARRAY;
        });

        programs.put("16", () -> {
            List<String> table = variableTables.get(CURRENT_TABLE_TYPE);
            RPNStep prevVariable = resultRPN.get(resultRPN.size() - 1);
            if (table.contains(prevVariable.getValue())) {
                throw new RuntimeException("This variable already defined");
            } else {
                table.add(prevVariable.getValue());
            }
        });
    }


    private static void initGenerationTable() {
        generationTable = new HashMap<>();


        for (CFGState state : CFGState.values())
            generationTable.put(state, new HashMap<TokenType, Runnable>());

        //Правила для A

        generationTable.get(CFGState.A).put(
                TokenType.DOT_COMMA, () -> {
                    generationSteps.add(new RPNGenerationStep(RPNGenerationStepType.SKIP, null));
                    generationSteps.add(new RPNGenerationStep(RPNGenerationStepType.PROGRAM, "16"));

                    mag.add(new NonTerminal(CFGState.B));
                    mag.add(new Terminal(TokenType.DOT_COMMA));
                }
        );

        generationTable.get(CFGState.A).put(
                TokenType.LEFT_SQR_BR, () -> {
                    generationSteps.add(new RPNGenerationStep(RPNGenerationStepType.SKIP, null));
                    generationSteps.add(new RPNGenerationStep(RPNGenerationStepType.PROGRAM, "16"));

                    mag.add(new NonTerminal(CFGState.E));
                    mag.add(new Terminal(TokenType.LEFT_SQR_BR));
                }
        );

        generationTable.get(CFGState.A).put(
                TokenType.ASSIGNMENT, () -> {
                    generationSteps.add(new RPNGenerationStep(RPNGenerationStepType.SKIP, null));
                    generationSteps.add(new RPNGenerationStep(RPNGenerationStepType.OPERATION, "="));
                    generationSteps.add(new RPNGenerationStep(RPNGenerationStepType.SKIP, null));
                    generationSteps.add(new RPNGenerationStep(RPNGenerationStepType.PROGRAM, "16"));

                    mag.add(new NonTerminal(CFGState.B));
                    mag.add(new Terminal(TokenType.DOT_COMMA));
                    mag.add(new NonTerminal(CFGState.J));
                    mag.add(new Terminal(TokenType.ASSIGNMENT));
                }
        );

        //Правила для E

        generationTable.get(CFGState.E).put(
                TokenType.CONST, () -> {
                    generationSteps.add(new RPNGenerationStep(RPNGenerationStepType.SKIP, null));
                    generationSteps.add(new RPNGenerationStep(RPNGenerationStepType.PROGRAM, "15"));
                    generationSteps.add(new RPNGenerationStep(RPNGenerationStepType.CONST, null));

                    mag.add(new NonTerminal(CFGState.F));
                    mag.add(new Terminal(TokenType.RIGHT_SQR_BR));
                    mag.add(new Terminal(TokenType.CONST));
                }
        );

        generationTable.get(CFGState.E).put(
                TokenType.RIGHT_SQR_BR, () -> {
                    generationSteps.add(new RPNGenerationStep(RPNGenerationStepType.OPERATION, "="));
                    generationSteps.add(new RPNGenerationStep(RPNGenerationStepType.SKIP, null));
                    generationSteps.add(new RPNGenerationStep(RPNGenerationStepType.OPERATION, "i"));
                    generationSteps.add(new RPNGenerationStep(RPNGenerationStepType.SKIP, null));
                    generationSteps.add(new RPNGenerationStep(RPNGenerationStepType.SKIP, null));
                    generationSteps.add(new RPNGenerationStep(RPNGenerationStepType.SKIP, null));
                    generationSteps.add(new RPNGenerationStep(RPNGenerationStepType.PROGRAM, "14"));
                    generationSteps.add(new RPNGenerationStep(RPNGenerationStepType.SKIP, null));
                    generationSteps.add(new RPNGenerationStep(RPNGenerationStepType.SKIP, null));

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
                    generationSteps.add(new RPNGenerationStep(RPNGenerationStepType.SKIP, null));
                    generationSteps.add(new RPNGenerationStep(RPNGenerationStepType.PROGRAM, "16"));

                    mag.add(new NonTerminal(CFGState.B));
                    mag.add(new Terminal(TokenType.DOT_COMMA));
                }
        );

        generationTable.get(CFGState.F).put(
                TokenType.ASSIGNMENT, () -> {
                    generationSteps.add(new RPNGenerationStep(RPNGenerationStepType.SKIP, null));
                    generationSteps.add(new RPNGenerationStep(RPNGenerationStepType.OPERATION, "="));
                    generationSteps.add(new RPNGenerationStep(RPNGenerationStepType.SKIP, null));
                    generationSteps.add(new RPNGenerationStep(RPNGenerationStepType.SKIP, null));
                    generationSteps.add(new RPNGenerationStep(RPNGenerationStepType.PROGRAM, "15"));
                    generationSteps.add(new RPNGenerationStep(RPNGenerationStepType.SKIP, null));


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
                    generationSteps.add(new RPNGenerationStep(RPNGenerationStepType.SKIP, null));
                    generationSteps.add(new RPNGenerationStep(RPNGenerationStepType.VARIABLE, null));

                    mag.add(new NonTerminal(CFGState.D));
                    mag.add(new Terminal(TokenType.VARIABLE));
                }
        );

        generationTable.get(CFGState.B).put(
                TokenType.INT, () -> {
                    generationSteps.add(new RPNGenerationStep(RPNGenerationStepType.SKIP, null));
                    generationSteps.add(new RPNGenerationStep(RPNGenerationStepType.VARIABLE, null));
                    generationSteps.add(new RPNGenerationStep(RPNGenerationStepType.PROGRAM, "11"));

                    mag.add(new NonTerminal(CFGState.A));
                    mag.add(new Terminal(TokenType.VARIABLE));
                    mag.add(new Terminal(TokenType.INT));
                }
        );

        generationTable.get(CFGState.B).put(
                TokenType.DOUBLE, () -> {
                    generationSteps.add(new RPNGenerationStep(RPNGenerationStepType.SKIP, null));
                    generationSteps.add(new RPNGenerationStep(RPNGenerationStepType.VARIABLE, null));
                    generationSteps.add(new RPNGenerationStep(RPNGenerationStepType.PROGRAM, "12"));

                    mag.add(new NonTerminal(CFGState.A));
                    mag.add(new Terminal(TokenType.VARIABLE));

                    mag.add(new Terminal(TokenType.DOUBLE));
                }
        );

        generationTable.get(CFGState.B).put(
                TokenType.BOOLEAN, () -> {
                    generationSteps.add(new RPNGenerationStep(RPNGenerationStepType.SKIP, null));
                    generationSteps.add(new RPNGenerationStep(RPNGenerationStepType.VARIABLE, null));
                    generationSteps.add(new RPNGenerationStep(RPNGenerationStepType.PROGRAM, "13"));


                    mag.add(new NonTerminal(CFGState.A));
                    mag.add(new Terminal(TokenType.VARIABLE));

                    mag.add(new Terminal(TokenType.BOOLEAN));
                }
        );

        generationTable.get(CFGState.B).put(
                TokenType.IF, () -> {
                    generationSteps.add(new RPNGenerationStep(RPNGenerationStepType.SKIP, null));
                    generationSteps.add(new RPNGenerationStep(RPNGenerationStepType.PROGRAM, "3"));
                    generationSteps.add(new RPNGenerationStep(RPNGenerationStepType.SKIP, null));
                    generationSteps.add(new RPNGenerationStep(RPNGenerationStepType.SKIP, null));
                    generationSteps.add(new RPNGenerationStep(RPNGenerationStepType.PROGRAM, "1"));
                    generationSteps.add(new RPNGenerationStep(RPNGenerationStepType.SKIP, null));
                    generationSteps.add(new RPNGenerationStep(RPNGenerationStepType.SKIP, null));
                    generationSteps.add(new RPNGenerationStep(RPNGenerationStepType.SKIP, null));
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

                    generationSteps.add(new RPNGenerationStep(RPNGenerationStepType.SKIP, null));
                    generationSteps.add(new RPNGenerationStep(RPNGenerationStepType.PROGRAM, "5"));
                    generationSteps.add(new RPNGenerationStep(RPNGenerationStepType.SKIP, null));
                    generationSteps.add(new RPNGenerationStep(RPNGenerationStepType.PROGRAM, "1"));
                    generationSteps.add(new RPNGenerationStep(RPNGenerationStepType.SKIP, null));
                    generationSteps.add(new RPNGenerationStep(RPNGenerationStepType.SKIP, null));
                    generationSteps.add(new RPNGenerationStep(RPNGenerationStepType.PROGRAM, "4"));
                    generationSteps.add(new RPNGenerationStep(RPNGenerationStepType.SKIP, null));

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
                    generationSteps.add(new RPNGenerationStep(RPNGenerationStepType.SKIP, null));
                    generationSteps.add(new RPNGenerationStep(RPNGenerationStepType.SKIP, null));

                    mag.add(new NonTerminal(CFGState.M));
                    mag.add(new Terminal(TokenType.ELSE));
                }
        );

        //TODO
        generationTable.get(CFGState.B).put(
                TokenType.SCAN, () -> {
                    generationSteps.add(new RPNGenerationStep(RPNGenerationStepType.SKIP, null));
                    generationSteps.add(new RPNGenerationStep(RPNGenerationStepType.SKIP, null));
                    generationSteps.add(new RPNGenerationStep(RPNGenerationStepType.OPERATION, "s"));
                    generationSteps.add(new RPNGenerationStep(RPNGenerationStepType.VARIABLE, null));
                    generationSteps.add(new RPNGenerationStep(RPNGenerationStepType.SKIP, null));
                    generationSteps.add(new RPNGenerationStep(RPNGenerationStepType.SKIP, null));
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
                    generationSteps.add(new RPNGenerationStep(RPNGenerationStepType.SKIP, null));
                    generationSteps.add(new RPNGenerationStep(RPNGenerationStepType.SKIP, null));
                    generationSteps.add(new RPNGenerationStep(RPNGenerationStepType.OPERATION, "p"));
                    generationSteps.add(new RPNGenerationStep(RPNGenerationStepType.SKIP, null));
                    generationSteps.add(new RPNGenerationStep(RPNGenerationStepType.SKIP, null));
                    generationSteps.add(new RPNGenerationStep(RPNGenerationStepType.SKIP, null));
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
                    generationSteps.add(new RPNGenerationStep(RPNGenerationStepType.SKIP, null));
                    generationSteps.add(new RPNGenerationStep(RPNGenerationStepType.PROGRAM, "2"));
                    generationSteps.add(new RPNGenerationStep(RPNGenerationStepType.SKIP, null));
                    generationSteps.add(new RPNGenerationStep(RPNGenerationStepType.SKIP, null));

                    mag.add(new NonTerminal(CFGState.B));
                    mag.add(new Terminal(TokenType.RIGHT_FIG_BR));
                    mag.add(new NonTerminal(CFGState.B));
                    mag.add(new Terminal(TokenType.LEFT_FIG_BR));
                }
        );

        generationTable.get(CFGState.M).put(
                TokenType.IF, () -> {
                    generationSteps.add(new RPNGenerationStep(RPNGenerationStepType.SKIP, null));
                    generationSteps.add(new RPNGenerationStep(RPNGenerationStepType.PROGRAM, "3"));
                    generationSteps.add(new RPNGenerationStep(RPNGenerationStepType.SKIP, null));
                    generationSteps.add(new RPNGenerationStep(RPNGenerationStepType.SKIP, null));
                    generationSteps.add(new RPNGenerationStep(RPNGenerationStepType.PROGRAM, "1"));
                    generationSteps.add(new RPNGenerationStep(RPNGenerationStepType.SKIP, null));
                    generationSteps.add(new RPNGenerationStep(RPNGenerationStepType.SKIP, null));
                    generationSteps.add(new RPNGenerationStep(RPNGenerationStepType.SKIP, null));

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
                    generationSteps.add(new RPNGenerationStep(RPNGenerationStepType.SKIP, null));
                    generationSteps.add(new RPNGenerationStep(RPNGenerationStepType.SKIP, null));
                    generationSteps.add(new RPNGenerationStep(RPNGenerationStepType.SKIP, null));
                    generationSteps.add(new RPNGenerationStep(RPNGenerationStepType.OPERATION, "i"));
                    generationSteps.add(new RPNGenerationStep(RPNGenerationStepType.SKIP, null));
                    generationSteps.add(new RPNGenerationStep(RPNGenerationStepType.SKIP, null));

                    mag.add(new NonTerminal(CFGState.B));
                    mag.add(new Terminal(TokenType.DOT_COMMA));
                    mag.add(new NonTerminal(CFGState.K));
                    mag.add(new Terminal(TokenType.RIGHT_SQR_BR));
                    mag.add(new NonTerminal(CFGState.J));
                    mag.add(new Terminal(TokenType.LEFT_SQR_BR));
                }
        );

        generationTable.get(CFGState.D).put(
                TokenType.ASSIGNMENT, () -> {
                    generationSteps.add(new RPNGenerationStep(RPNGenerationStepType.SKIP, null));
                    generationSteps.add(new RPNGenerationStep(RPNGenerationStepType.OPERATION, "="));
                    generationSteps.add(new RPNGenerationStep(RPNGenerationStepType.SKIP, null));
                    generationSteps.add(new RPNGenerationStep(RPNGenerationStepType.SKIP, null));

                    mag.add(new NonTerminal(CFGState.B));
                    mag.add(new Terminal(TokenType.DOT_COMMA));
                    mag.add(new NonTerminal(CFGState.J));
                    mag.add(new Terminal(TokenType.ASSIGNMENT));
                }
        );

        generationTable.get(CFGState.D).put(
                TokenType.PLUS_EQUALS, () -> {
                    generationSteps.add(new RPNGenerationStep(RPNGenerationStepType.SKIP, null));
                    generationSteps.add(new RPNGenerationStep(RPNGenerationStepType.OPERATION, "+="));
                    generationSteps.add(new RPNGenerationStep(RPNGenerationStepType.SKIP, null));
                    generationSteps.add(new RPNGenerationStep(RPNGenerationStepType.SKIP, null));

                    mag.add(new NonTerminal(CFGState.B));
                    mag.add(new Terminal(TokenType.DOT_COMMA));
                    mag.add(new NonTerminal(CFGState.J));
                    mag.add(new Terminal(TokenType.PLUS_EQUALS));
                }
        );

        generationTable.get(CFGState.D).put(
                TokenType.MINUS_EQUALS, () -> {
                    generationSteps.add(new RPNGenerationStep(RPNGenerationStepType.SKIP, null));
                    generationSteps.add(new RPNGenerationStep(RPNGenerationStepType.OPERATION, "-="));
                    generationSteps.add(new RPNGenerationStep(RPNGenerationStepType.SKIP, null));
                    generationSteps.add(new RPNGenerationStep(RPNGenerationStepType.SKIP, null));

                    mag.add(new NonTerminal(CFGState.B));
                    mag.add(new Terminal(TokenType.DOT_COMMA));
                    mag.add(new NonTerminal(CFGState.J));
                    mag.add(new Terminal(TokenType.MINUS_EQUALS));
                }
        );

        generationTable.get(CFGState.D).put(
                TokenType.MULTIPLY_EQUALS, () -> {
                    generationSteps.add(new RPNGenerationStep(RPNGenerationStepType.SKIP, null));
                    generationSteps.add(new RPNGenerationStep(RPNGenerationStepType.OPERATION, "*="));
                    generationSteps.add(new RPNGenerationStep(RPNGenerationStepType.SKIP, null));
                    generationSteps.add(new RPNGenerationStep(RPNGenerationStepType.SKIP, null));

                    mag.add(new NonTerminal(CFGState.B));
                    mag.add(new Terminal(TokenType.DOT_COMMA));
                    mag.add(new NonTerminal(CFGState.J));
                    mag.add(new Terminal(TokenType.MULTIPLY_EQUALS));
                }
        );

        generationTable.get(CFGState.D).put(
                TokenType.DIV_EQUALS, () -> {
                    generationSteps.add(new RPNGenerationStep(RPNGenerationStepType.SKIP, null));
                    generationSteps.add(new RPNGenerationStep(RPNGenerationStepType.OPERATION, "/="));
                    generationSteps.add(new RPNGenerationStep(RPNGenerationStepType.SKIP, null));
                    generationSteps.add(new RPNGenerationStep(RPNGenerationStepType.SKIP, null));

                    mag.add(new NonTerminal(CFGState.B));
                    mag.add(new Terminal(TokenType.DOT_COMMA));
                    mag.add(new NonTerminal(CFGState.J));
                    mag.add(new Terminal(TokenType.DIV_EQUALS));
                }
        );

        //Правила для J

        generationTable.get(CFGState.J).put(
                TokenType.VARIABLE, () -> {
                    generationSteps.add(new RPNGenerationStep(RPNGenerationStepType.SKIP, null));
                    generationSteps.add(new RPNGenerationStep(RPNGenerationStepType.SKIP, null));
                    generationSteps.add(new RPNGenerationStep(RPNGenerationStepType.SKIP, null));
                    generationSteps.add(new RPNGenerationStep(RPNGenerationStepType.SKIP, null));
                    generationSteps.add(new RPNGenerationStep(RPNGenerationStepType.SKIP, null));
                    generationSteps.add(new RPNGenerationStep(RPNGenerationStepType.SKIP, null));
                    generationSteps.add(new RPNGenerationStep(RPNGenerationStepType.VARIABLE, null));

                    mag.add(new NonTerminal(CFGState.U));
                    mag.add(new NonTerminal(CFGState.T));
                    mag.add(new NonTerminal(CFGState.W));
                    mag.add(new NonTerminal(CFGState.X));
                    mag.add(new NonTerminal(CFGState.Y));
                    mag.add(new NonTerminal(CFGState.C));
                    mag.add(new Terminal(TokenType.VARIABLE));
                }
        );

        generationTable.get(CFGState.J).put(
                TokenType.LEFT_ROUND_BR, () -> {
                    generationSteps.add(new RPNGenerationStep(RPNGenerationStepType.SKIP, null));
                    generationSteps.add(new RPNGenerationStep(RPNGenerationStepType.SKIP, null));
                    generationSteps.add(new RPNGenerationStep(RPNGenerationStepType.SKIP, null));
                    generationSteps.add(new RPNGenerationStep(RPNGenerationStepType.SKIP, null));
                    generationSteps.add(new RPNGenerationStep(RPNGenerationStepType.SKIP, null));
                    generationSteps.add(new RPNGenerationStep(RPNGenerationStepType.SKIP, null));
                    generationSteps.add(new RPNGenerationStep(RPNGenerationStepType.SKIP, null));
                    generationSteps.add(new RPNGenerationStep(RPNGenerationStepType.SKIP, null));

                    mag.add(new NonTerminal(CFGState.U));
                    mag.add(new NonTerminal(CFGState.T));
                    mag.add(new NonTerminal(CFGState.W));
                    mag.add(new NonTerminal(CFGState.X));
                    mag.add(new NonTerminal(CFGState.Y));
                    mag.add(new Terminal(TokenType.RIGHT_ROUND_BR));
                    mag.add(new NonTerminal(CFGState.J));
                    mag.add(new Terminal(TokenType.LEFT_ROUND_BR));

                }
        );

        generationTable.get(CFGState.J).put(
                TokenType.CONST, () -> {
                    generationSteps.add(new RPNGenerationStep(RPNGenerationStepType.SKIP, null));
                    generationSteps.add(new RPNGenerationStep(RPNGenerationStepType.SKIP, null));
                    generationSteps.add(new RPNGenerationStep(RPNGenerationStepType.SKIP, null));
                    generationSteps.add(new RPNGenerationStep(RPNGenerationStepType.SKIP, null));
                    generationSteps.add(new RPNGenerationStep(RPNGenerationStepType.SKIP, null));
                    generationSteps.add(new RPNGenerationStep(RPNGenerationStepType.CONST, null));

                    mag.add(new NonTerminal(CFGState.U));
                    mag.add(new NonTerminal(CFGState.T));
                    mag.add(new NonTerminal(CFGState.W));
                    mag.add(new NonTerminal(CFGState.X));
                    mag.add(new NonTerminal(CFGState.Y));
                    mag.add(new Terminal(TokenType.CONST));
                }
        );

        generationTable.get(CFGState.J).put(
                TokenType.MINUS, () -> {
                    generationSteps.add(new RPNGenerationStep(RPNGenerationStepType.OPERATION, "-'"));
                    generationSteps.add(new RPNGenerationStep(RPNGenerationStepType.SKIP, null));
                    generationSteps.add(new RPNGenerationStep(RPNGenerationStepType.SKIP, null));
                    generationSteps.add(new RPNGenerationStep(RPNGenerationStepType.SKIP, null));
                    generationSteps.add(new RPNGenerationStep(RPNGenerationStepType.SKIP, null));
                    generationSteps.add(new RPNGenerationStep(RPNGenerationStepType.SKIP, null));
                    generationSteps.add(new RPNGenerationStep(RPNGenerationStepType.SKIP, null));

                    mag.add(new NonTerminal(CFGState.U));
                    mag.add(new NonTerminal(CFGState.T));
                    mag.add(new NonTerminal(CFGState.W));
                    mag.add(new NonTerminal(CFGState.X));
                    mag.add(new NonTerminal(CFGState.Y));
                    mag.add(new NonTerminal(CFGState.R));
                    mag.add(new Terminal(TokenType.MINUS));
                }
        );

        generationTable.get(CFGState.J).put(
                TokenType.NOT, () -> {
                    generationSteps.add(new RPNGenerationStep(RPNGenerationStepType.OPERATION, "!'"));
                    generationSteps.add(new RPNGenerationStep(RPNGenerationStepType.SKIP, null));
                    generationSteps.add(new RPNGenerationStep(RPNGenerationStepType.SKIP, null));
                    generationSteps.add(new RPNGenerationStep(RPNGenerationStepType.SKIP, null));
                    generationSteps.add(new RPNGenerationStep(RPNGenerationStepType.SKIP, null));
                    generationSteps.add(new RPNGenerationStep(RPNGenerationStepType.SKIP, null));
                    generationSteps.add(new RPNGenerationStep(RPNGenerationStepType.SKIP, null));

                    mag.add(new NonTerminal(CFGState.U));
                    mag.add(new NonTerminal(CFGState.T));
                    mag.add(new NonTerminal(CFGState.W));
                    mag.add(new NonTerminal(CFGState.X));
                    mag.add(new NonTerminal(CFGState.Y));
                    mag.add(new NonTerminal(CFGState.R));
                    mag.add(new Terminal(TokenType.NOT));
                }
        );

        //Правила для V

        generationTable.get(CFGState.V).put(
                TokenType.VARIABLE, () -> {
                    generationSteps.add(new RPNGenerationStep(RPNGenerationStepType.SKIP, null));
                    generationSteps.add(new RPNGenerationStep(RPNGenerationStepType.VARIABLE, null));

                    mag.add(new NonTerminal(CFGState.C));
                    mag.add(new Terminal(TokenType.VARIABLE));
                }
        );

        generationTable.get(CFGState.V).put(
                TokenType.CONST, () -> {
                    generationSteps.add(new RPNGenerationStep(RPNGenerationStepType.CONST, null));

                    mag.add(new Terminal(TokenType.CONST));
                }
        );

        generationTable.get(CFGState.V).put(
                TokenType.MINUS, () -> {
                    generationSteps.add(new RPNGenerationStep(RPNGenerationStepType.OPERATION, "-'"));
                    generationSteps.add(new RPNGenerationStep(RPNGenerationStepType.SKIP, null));
                    generationSteps.add(new RPNGenerationStep(RPNGenerationStepType.SKIP, null));

                    mag.add(new NonTerminal(CFGState.Z));
                    mag.add(new NonTerminal(CFGState.R));
                    mag.add(new Terminal(TokenType.MINUS));
                }
        );

        generationTable.get(CFGState.V).put(
                TokenType.LEFT_ROUND_BR, () -> {
                    generationSteps.add(new RPNGenerationStep(RPNGenerationStepType.SKIP, null));
                    generationSteps.add(new RPNGenerationStep(RPNGenerationStepType.SKIP, null));
                    generationSteps.add(new RPNGenerationStep(RPNGenerationStepType.SKIP, null));

                    mag.add(new Terminal(TokenType.RIGHT_ROUND_BR));
                    mag.add(new NonTerminal(CFGState.J));
                    mag.add(new Terminal(TokenType.LEFT_ROUND_BR));
                }
        );

        //Правила для L

        generationTable.get(CFGState.L).put(
                TokenType.VARIABLE, () -> {
                    generationSteps.add(new RPNGenerationStep(RPNGenerationStepType.SKIP, null));
                    generationSteps.add(new RPNGenerationStep(RPNGenerationStepType.SKIP, null));
                    generationSteps.add(new RPNGenerationStep(RPNGenerationStepType.VARIABLE, null));

                    mag.add(new NonTerminal(CFGState.Y));
                    mag.add(new NonTerminal(CFGState.C));
                    mag.add(new Terminal(TokenType.VARIABLE));
                }
        );

        generationTable.get(CFGState.L).put(
                TokenType.CONST, () -> {
                    generationSteps.add(new RPNGenerationStep(RPNGenerationStepType.SKIP, null));
                    generationSteps.add(new RPNGenerationStep(RPNGenerationStepType.CONST, null));

                    mag.add(new NonTerminal(CFGState.Y));
                    mag.add(new Terminal(TokenType.CONST));
                }
        );


        generationTable.get(CFGState.L).put(
                TokenType.MINUS, () -> {
                    generationSteps.add(new RPNGenerationStep(RPNGenerationStepType.OPERATION, "-'"));
                    generationSteps.add(new RPNGenerationStep(RPNGenerationStepType.SKIP, null));
                    generationSteps.add(new RPNGenerationStep(RPNGenerationStepType.SKIP, null));

                    mag.add(new NonTerminal(CFGState.Y));
                    mag.add(new NonTerminal(CFGState.R));
                    mag.add(new Terminal(TokenType.MINUS));
                }
        );

        generationTable.get(CFGState.L).put(
                TokenType.LEFT_ROUND_BR, () -> {
                    generationSteps.add(new RPNGenerationStep(RPNGenerationStepType.SKIP, null));
                    generationSteps.add(new RPNGenerationStep(RPNGenerationStepType.SKIP, null));
                    generationSteps.add(new RPNGenerationStep(RPNGenerationStepType.SKIP, null));
                    generationSteps.add(new RPNGenerationStep(RPNGenerationStepType.SKIP, null));

                    mag.add(new NonTerminal(CFGState.Y));
                    mag.add(new Terminal(TokenType.RIGHT_ROUND_BR));
                    mag.add(new NonTerminal(CFGState.J));
                    mag.add(new Terminal(TokenType.LEFT_ROUND_BR));
                }
        );

        //Правила для H

        generationTable.get(CFGState.H).put(
                TokenType.VARIABLE, () -> {
                    generationSteps.add(new RPNGenerationStep(RPNGenerationStepType.SKIP, null));
                    generationSteps.add(new RPNGenerationStep(RPNGenerationStepType.SKIP, null));
                    generationSteps.add(new RPNGenerationStep(RPNGenerationStepType.SKIP, null));
                    generationSteps.add(new RPNGenerationStep(RPNGenerationStepType.VARIABLE, null));

                    mag.add(new NonTerminal(CFGState.X));
                    mag.add(new NonTerminal(CFGState.Y));
                    mag.add(new NonTerminal(CFGState.C));
                    mag.add(new Terminal(TokenType.VARIABLE));
                }
        );

        generationTable.get(CFGState.H).put(
                TokenType.CONST, () -> {
                    generationSteps.add(new RPNGenerationStep(RPNGenerationStepType.SKIP, null));
                    generationSteps.add(new RPNGenerationStep(RPNGenerationStepType.SKIP, null));
                    generationSteps.add(new RPNGenerationStep(RPNGenerationStepType.CONST, null));

                    mag.add(new NonTerminal(CFGState.X));
                    mag.add(new NonTerminal(CFGState.Y));
                    mag.add(new Terminal(TokenType.CONST));
                }
        );


        generationTable.get(CFGState.H).put(
                TokenType.MINUS, () -> {
                    generationSteps.add(new RPNGenerationStep(RPNGenerationStepType.OPERATION, "-'"));
                    generationSteps.add(new RPNGenerationStep(RPNGenerationStepType.SKIP, null));
                    generationSteps.add(new RPNGenerationStep(RPNGenerationStepType.SKIP, null));
                    generationSteps.add(new RPNGenerationStep(RPNGenerationStepType.SKIP, null));

                    mag.add(new NonTerminal(CFGState.X));
                    mag.add(new NonTerminal(CFGState.Y));
                    mag.add(new NonTerminal(CFGState.R));
                    mag.add(new Terminal(TokenType.MINUS));
                }
        );

        generationTable.get(CFGState.H).put(
                TokenType.LEFT_ROUND_BR, () -> {
                    generationSteps.add(new RPNGenerationStep(RPNGenerationStepType.SKIP, null));
                    generationSteps.add(new RPNGenerationStep(RPNGenerationStepType.SKIP, null));
                    generationSteps.add(new RPNGenerationStep(RPNGenerationStepType.SKIP, null));
                    generationSteps.add(new RPNGenerationStep(RPNGenerationStepType.SKIP, null));
                    generationSteps.add(new RPNGenerationStep(RPNGenerationStepType.SKIP, null));

                    mag.add(new NonTerminal(CFGState.X));
                    mag.add(new NonTerminal(CFGState.Y));
                    mag.add(new Terminal(TokenType.RIGHT_ROUND_BR));
                    mag.add(new NonTerminal(CFGState.J));
                    mag.add(new Terminal(TokenType.LEFT_ROUND_BR));
                }
        );

        //Правила для N

        generationTable.get(CFGState.N).put(
                TokenType.VARIABLE, () -> {
                    generationSteps.add(new RPNGenerationStep(RPNGenerationStepType.SKIP, null));
                    generationSteps.add(new RPNGenerationStep(RPNGenerationStepType.SKIP, null));
                    generationSteps.add(new RPNGenerationStep(RPNGenerationStepType.SKIP, null));
                    generationSteps.add(new RPNGenerationStep(RPNGenerationStepType.SKIP, null));
                    generationSteps.add(new RPNGenerationStep(RPNGenerationStepType.SKIP, null));
                    generationSteps.add(new RPNGenerationStep(RPNGenerationStepType.VARIABLE, null));

                    mag.add(new NonTerminal(CFGState.T));
                    mag.add(new NonTerminal(CFGState.W));
                    mag.add(new NonTerminal(CFGState.X));
                    mag.add(new NonTerminal(CFGState.Y));
                    mag.add(new NonTerminal(CFGState.C));
                    mag.add(new Terminal(TokenType.VARIABLE));
                }
        );

        generationTable.get(CFGState.N).put(
                TokenType.CONST, () -> {
                    generationSteps.add(new RPNGenerationStep(RPNGenerationStepType.SKIP, null));
                    generationSteps.add(new RPNGenerationStep(RPNGenerationStepType.SKIP, null));
                    generationSteps.add(new RPNGenerationStep(RPNGenerationStepType.SKIP, null));
                    generationSteps.add(new RPNGenerationStep(RPNGenerationStepType.SKIP, null));
                    generationSteps.add(new RPNGenerationStep(RPNGenerationStepType.CONST, null));

                    mag.add(new NonTerminal(CFGState.T));
                    mag.add(new NonTerminal(CFGState.W));
                    mag.add(new NonTerminal(CFGState.X));
                    mag.add(new NonTerminal(CFGState.Y));
                    mag.add(new Terminal(TokenType.CONST));
                }
        );


        generationTable.get(CFGState.N).put(
                TokenType.MINUS, () -> {
                    generationSteps.add(new RPNGenerationStep(RPNGenerationStepType.OPERATION, "-'"));
                    generationSteps.add(new RPNGenerationStep(RPNGenerationStepType.SKIP, null));
                    generationSteps.add(new RPNGenerationStep(RPNGenerationStepType.SKIP, null));
                    generationSteps.add(new RPNGenerationStep(RPNGenerationStepType.SKIP, null));
                    generationSteps.add(new RPNGenerationStep(RPNGenerationStepType.SKIP, null));
                    generationSteps.add(new RPNGenerationStep(RPNGenerationStepType.SKIP, null));

                    mag.add(new NonTerminal(CFGState.T));
                    mag.add(new NonTerminal(CFGState.W));
                    mag.add(new NonTerminal(CFGState.X));
                    mag.add(new NonTerminal(CFGState.Y));
                    mag.add(new NonTerminal(CFGState.R));
                    mag.add(new Terminal(TokenType.MINUS));
                }
        );

        generationTable.get(CFGState.N).put(
                TokenType.LEFT_ROUND_BR, () -> {
                    generationSteps.add(new RPNGenerationStep(RPNGenerationStepType.SKIP, null));
                    generationSteps.add(new RPNGenerationStep(RPNGenerationStepType.SKIP, null));
                    generationSteps.add(new RPNGenerationStep(RPNGenerationStepType.SKIP, null));
                    generationSteps.add(new RPNGenerationStep(RPNGenerationStepType.SKIP, null));
                    generationSteps.add(new RPNGenerationStep(RPNGenerationStepType.SKIP, null));
                    generationSteps.add(new RPNGenerationStep(RPNGenerationStepType.SKIP, null));
                    generationSteps.add(new RPNGenerationStep(RPNGenerationStepType.SKIP, null));

                    mag.add(new NonTerminal(CFGState.T));
                    mag.add(new NonTerminal(CFGState.W));
                    mag.add(new NonTerminal(CFGState.X));
                    mag.add(new NonTerminal(CFGState.Y));
                    mag.add(new Terminal(TokenType.RIGHT_ROUND_BR));
                    mag.add(new NonTerminal(CFGState.J));
                    mag.add(new Terminal(TokenType.LEFT_ROUND_BR));
                }
        );

        //Правила для O

        generationTable.get(CFGState.O).put(
                TokenType.VARIABLE, () -> {
                    generationSteps.add(new RPNGenerationStep(RPNGenerationStepType.SKIP, null));
                    generationSteps.add(new RPNGenerationStep(RPNGenerationStepType.SKIP, null));
                    generationSteps.add(new RPNGenerationStep(RPNGenerationStepType.SKIP, null));
                    generationSteps.add(new RPNGenerationStep(RPNGenerationStepType.SKIP, null));
                    generationSteps.add(new RPNGenerationStep(RPNGenerationStepType.VARIABLE, null));

                    mag.add(new NonTerminal(CFGState.W));
                    mag.add(new NonTerminal(CFGState.X));
                    mag.add(new NonTerminal(CFGState.Y));
                    mag.add(new NonTerminal(CFGState.C));
                    mag.add(new Terminal(TokenType.VARIABLE));
                }
        );

        generationTable.get(CFGState.O).put(
                TokenType.CONST, () -> {
                    generationSteps.add(new RPNGenerationStep(RPNGenerationStepType.SKIP, null));
                    generationSteps.add(new RPNGenerationStep(RPNGenerationStepType.SKIP, null));
                    generationSteps.add(new RPNGenerationStep(RPNGenerationStepType.SKIP, null));
                    generationSteps.add(new RPNGenerationStep(RPNGenerationStepType.CONST, null));

                    mag.add(new NonTerminal(CFGState.W));
                    mag.add(new NonTerminal(CFGState.X));
                    mag.add(new NonTerminal(CFGState.Y));
                    mag.add(new Terminal(TokenType.CONST));
                }
        );


        generationTable.get(CFGState.O).put(
                TokenType.MINUS, () -> {
                    generationSteps.add(new RPNGenerationStep(RPNGenerationStepType.OPERATION, "-'"));
                    generationSteps.add(new RPNGenerationStep(RPNGenerationStepType.SKIP, null));
                    generationSteps.add(new RPNGenerationStep(RPNGenerationStepType.SKIP, null));
                    generationSteps.add(new RPNGenerationStep(RPNGenerationStepType.SKIP, null));
                    generationSteps.add(new RPNGenerationStep(RPNGenerationStepType.SKIP, null));

                    mag.add(new NonTerminal(CFGState.W));
                    mag.add(new NonTerminal(CFGState.X));
                    mag.add(new NonTerminal(CFGState.Y));
                    mag.add(new NonTerminal(CFGState.R));
                    mag.add(new Terminal(TokenType.MINUS));
                }
        );

        generationTable.get(CFGState.O).put(
                TokenType.LEFT_ROUND_BR, () -> {
                    generationSteps.add(new RPNGenerationStep(RPNGenerationStepType.SKIP, null));
                    generationSteps.add(new RPNGenerationStep(RPNGenerationStepType.SKIP, null));
                    generationSteps.add(new RPNGenerationStep(RPNGenerationStepType.SKIP, null));
                    generationSteps.add(new RPNGenerationStep(RPNGenerationStepType.SKIP, null));
                    generationSteps.add(new RPNGenerationStep(RPNGenerationStepType.SKIP, null));
                    generationSteps.add(new RPNGenerationStep(RPNGenerationStepType.SKIP, null));

                    mag.add(new NonTerminal(CFGState.W));
                    mag.add(new NonTerminal(CFGState.X));
                    mag.add(new NonTerminal(CFGState.Y));
                    mag.add(new Terminal(TokenType.RIGHT_ROUND_BR));
                    mag.add(new NonTerminal(CFGState.J));
                    mag.add(new Terminal(TokenType.LEFT_ROUND_BR));
                }
        );


        //Правила для N

        generationTable.get(CFGState.N).put(
                TokenType.COMMA, () -> {
                    generationSteps.add(new RPNGenerationStep(RPNGenerationStepType.SKIP, null));
                    generationSteps.add(new RPNGenerationStep(RPNGenerationStepType.SKIP, null));
                    mag.add(new NonTerminal(CFGState.J));

                    mag.add(new Terminal(TokenType.COMMA));
                }
        );

        //Правила для G

        generationTable.get(CFGState.G).put(
                TokenType.INT, () -> {
                    generationSteps.add(new RPNGenerationStep(RPNGenerationStepType.PROGRAM, "11"));
                    mag.add(new Terminal(TokenType.INT));
                }
        );

        generationTable.get(CFGState.G).put(
                TokenType.DOUBLE, () -> {
                    generationSteps.add(new RPNGenerationStep(RPNGenerationStepType.PROGRAM, "12"));
                    mag.add(new Terminal(TokenType.DOUBLE));
                }
        );

        generationTable.get(CFGState.G).put(
                TokenType.BOOLEAN, () -> {
                    generationSteps.add(new RPNGenerationStep(RPNGenerationStepType.PROGRAM, "13"));
                    mag.add(new Terminal(TokenType.BOOLEAN));
                }
        );

        //Правила для K

        generationTable.get(CFGState.K).put(
                TokenType.ASSIGNMENT, () -> {
                    generationSteps.add(new RPNGenerationStep(RPNGenerationStepType.OPERATION, "="));
                    generationSteps.add(new RPNGenerationStep(RPNGenerationStepType.SKIP, null));
                    generationSteps.add(new RPNGenerationStep(RPNGenerationStepType.SKIP, null));
                    mag.add(new NonTerminal(CFGState.J));
                    mag.add(new Terminal(TokenType.ASSIGNMENT));
                }
        );

        generationTable.get(CFGState.K).put(
                TokenType.PLUS_EQUALS, () -> {
                    generationSteps.add(new RPNGenerationStep(RPNGenerationStepType.OPERATION, "+="));
                    generationSteps.add(new RPNGenerationStep(RPNGenerationStepType.SKIP, null));
                    generationSteps.add(new RPNGenerationStep(RPNGenerationStepType.SKIP, null));
                    mag.add(new NonTerminal(CFGState.J));
                    mag.add(new Terminal(TokenType.PLUS_EQUALS));
                }
        );

        generationTable.get(CFGState.K).put(
                TokenType.MINUS_EQUALS, () -> {
                    generationSteps.add(new RPNGenerationStep(RPNGenerationStepType.OPERATION, "-="));
                    generationSteps.add(new RPNGenerationStep(RPNGenerationStepType.SKIP, null));
                    generationSteps.add(new RPNGenerationStep(RPNGenerationStepType.SKIP, null));
                    mag.add(new NonTerminal(CFGState.J));
                    mag.add(new Terminal(TokenType.MINUS_EQUALS));
                }
        );

        generationTable.get(CFGState.K).put(
                TokenType.MULTIPLY_EQUALS, () -> {
                    generationSteps.add(new RPNGenerationStep(RPNGenerationStepType.OPERATION, "*="));
                    generationSteps.add(new RPNGenerationStep(RPNGenerationStepType.SKIP, null));
                    generationSteps.add(new RPNGenerationStep(RPNGenerationStepType.SKIP, null));
                    mag.add(new NonTerminal(CFGState.J));
                    mag.add(new Terminal(TokenType.MULTIPLY_EQUALS));
                }
        );

        generationTable.get(CFGState.K).put(
                TokenType.DIV_EQUALS, () -> {
                    generationSteps.add(new RPNGenerationStep(RPNGenerationStepType.OPERATION, "/="));
                    generationSteps.add(new RPNGenerationStep(RPNGenerationStepType.SKIP, null));
                    generationSteps.add(new RPNGenerationStep(RPNGenerationStepType.SKIP, null));
                    mag.add(new NonTerminal(CFGState.J));
                    mag.add(new Terminal(TokenType.DIV_EQUALS));
                }
        );

        //Правила для U

        generationTable.get(CFGState.U).put(
                TokenType.OR, () -> {
                    generationSteps.add(new RPNGenerationStep(RPNGenerationStepType.OPERATION, "|"));
                    generationSteps.add(new RPNGenerationStep(RPNGenerationStepType.SKIP, null));
                    generationSteps.add(new RPNGenerationStep(RPNGenerationStepType.SKIP, null));

                    mag.add(new NonTerminal(CFGState.U));
                    mag.add(new NonTerminal(CFGState.N));
                    mag.add(new Terminal(TokenType.OR));

                }
        );

        //Правила для T

        generationTable.get(CFGState.T).put(
                TokenType.AND, () -> {
                    generationSteps.add(new RPNGenerationStep(RPNGenerationStepType.OPERATION, "&"));
                    generationSteps.add(new RPNGenerationStep(RPNGenerationStepType.SKIP, null));
                    generationSteps.add(new RPNGenerationStep(RPNGenerationStepType.SKIP, null));

                    mag.add(new NonTerminal(CFGState.T));
                    mag.add(new NonTerminal(CFGState.O));
                    mag.add(new Terminal(TokenType.AND));

                }
        );

        //Правила для W

        generationTable.get(CFGState.W).put(
                TokenType.GREATER, () -> {
                    generationSteps.add(new RPNGenerationStep(RPNGenerationStepType.OPERATION, ">"));
                    generationSteps.add(new RPNGenerationStep(RPNGenerationStepType.SKIP, null));
                    generationSteps.add(new RPNGenerationStep(RPNGenerationStepType.SKIP, null));

                    mag.add(new NonTerminal(CFGState.W));
                    mag.add(new NonTerminal(CFGState.H));
                    mag.add(new Terminal(TokenType.GREATER));

                }
        );

        generationTable.get(CFGState.W).put(
                TokenType.LESS, () -> {
                    generationSteps.add(new RPNGenerationStep(RPNGenerationStepType.OPERATION, "<"));
                    generationSteps.add(new RPNGenerationStep(RPNGenerationStepType.SKIP, null));
                    generationSteps.add(new RPNGenerationStep(RPNGenerationStepType.SKIP, null));

                    mag.add(new NonTerminal(CFGState.W));
                    mag.add(new NonTerminal(CFGState.H));
                    mag.add(new Terminal(TokenType.LESS));

                }
        );

        generationTable.get(CFGState.W).put(
                TokenType.GREATER_EQUALS, () -> {
                    generationSteps.add(new RPNGenerationStep(RPNGenerationStepType.OPERATION, ">="));
                    generationSteps.add(new RPNGenerationStep(RPNGenerationStepType.SKIP, null));
                    generationSteps.add(new RPNGenerationStep(RPNGenerationStepType.SKIP, null));

                    mag.add(new NonTerminal(CFGState.W));
                    mag.add(new NonTerminal(CFGState.H));
                    mag.add(new Terminal(TokenType.GREATER_EQUALS));

                }
        );

        generationTable.get(CFGState.W).put(
                TokenType.LESS_EQUALS, () -> {
                    generationSteps.add(new RPNGenerationStep(RPNGenerationStepType.OPERATION, "<="));
                    generationSteps.add(new RPNGenerationStep(RPNGenerationStepType.SKIP, null));
                    generationSteps.add(new RPNGenerationStep(RPNGenerationStepType.SKIP, null));


                    mag.add(new NonTerminal(CFGState.W));
                    mag.add(new NonTerminal(CFGState.H));
                    mag.add(new Terminal(TokenType.LESS_EQUALS));
                }
        );
        generationTable.get(CFGState.W).put(
                TokenType.EQUALS, () -> {
                    generationSteps.add(new RPNGenerationStep(RPNGenerationStepType.OPERATION, "=="));
                    generationSteps.add(new RPNGenerationStep(RPNGenerationStepType.SKIP, null));
                    generationSteps.add(new RPNGenerationStep(RPNGenerationStepType.SKIP, null));


                    mag.add(new NonTerminal(CFGState.W));
                    mag.add(new NonTerminal(CFGState.H));
                    mag.add(new Terminal(TokenType.EQUALS));
                }
        );
        generationTable.get(CFGState.W).put(
                TokenType.NOT_EQUALS, () -> {
                    generationSteps.add(new RPNGenerationStep(RPNGenerationStepType.OPERATION, "!="));
                    generationSteps.add(new RPNGenerationStep(RPNGenerationStepType.SKIP, null));
                    generationSteps.add(new RPNGenerationStep(RPNGenerationStepType.SKIP, null));


                    mag.add(new NonTerminal(CFGState.W));
                    mag.add(new NonTerminal(CFGState.H));
                    mag.add(new Terminal(TokenType.NOT_EQUALS));
                }
        );

        //Правила для X

        generationTable.get(CFGState.X).put(
                TokenType.PLUS, () -> {
                    generationSteps.add(new RPNGenerationStep(RPNGenerationStepType.OPERATION, "+"));
                    generationSteps.add(new RPNGenerationStep(RPNGenerationStepType.SKIP, null));
                    generationSteps.add(new RPNGenerationStep(RPNGenerationStepType.SKIP, null));


                    mag.add(new NonTerminal(CFGState.X));
                    mag.add(new NonTerminal(CFGState.L));
                    mag.add(new Terminal(TokenType.PLUS));

                }
        );

        generationTable.get(CFGState.X).put(
                TokenType.MINUS, () -> {
                    generationSteps.add(new RPNGenerationStep(RPNGenerationStepType.OPERATION, "-"));
                    generationSteps.add(new RPNGenerationStep(RPNGenerationStepType.SKIP, null));
                    generationSteps.add(new RPNGenerationStep(RPNGenerationStepType.SKIP, null));


                    mag.add(new NonTerminal(CFGState.X));
                    mag.add(new NonTerminal(CFGState.L));
                    mag.add(new Terminal(TokenType.MINUS));

                }
        );

        //Правила для Y

        generationTable.get(CFGState.Y).put(
                TokenType.MULTIPLY, () -> {
                    generationSteps.add(new RPNGenerationStep(RPNGenerationStepType.OPERATION, "*"));
                    generationSteps.add(new RPNGenerationStep(RPNGenerationStepType.SKIP, null));
                    generationSteps.add(new RPNGenerationStep(RPNGenerationStepType.SKIP, null));

                    mag.add(new NonTerminal(CFGState.Y));
                    mag.add(new NonTerminal(CFGState.V));
                    mag.add(new Terminal(TokenType.MULTIPLY));

                }
        );

        generationTable.get(CFGState.Y).put(
                TokenType.DIV, () -> {
                    generationSteps.add(new RPNGenerationStep(RPNGenerationStepType.OPERATION, "/"));
                    generationSteps.add(new RPNGenerationStep(RPNGenerationStepType.SKIP, null));
                    generationSteps.add(new RPNGenerationStep(RPNGenerationStepType.SKIP, null));

                    mag.add(new NonTerminal(CFGState.Y));
                    mag.add(new NonTerminal(CFGState.V));
                    mag.add(new Terminal(TokenType.DIV));

                }
        );

        //Правила для R

        generationTable.get(CFGState.R).put(
                TokenType.VARIABLE, () -> {
                    generationSteps.add(new RPNGenerationStep(RPNGenerationStepType.SKIP, null));
                    generationSteps.add(new RPNGenerationStep(RPNGenerationStepType.VARIABLE, null));

                    mag.add(new NonTerminal(CFGState.C));
                    mag.add(new Terminal(TokenType.VARIABLE));

                }
        );

        generationTable.get(CFGState.R).put(
                TokenType.CONST, () -> {
                    generationSteps.add(new RPNGenerationStep(RPNGenerationStepType.CONST, null));

                    mag.add(new Terminal(TokenType.CONST));
                }
        );

        generationTable.get(CFGState.R).put(
                TokenType.LEFT_ROUND_BR, () -> {
                    generationSteps.add(new RPNGenerationStep(RPNGenerationStepType.SKIP, null));
                    generationSteps.add(new RPNGenerationStep(RPNGenerationStepType.SKIP, null));
                    generationSteps.add(new RPNGenerationStep(RPNGenerationStepType.SKIP, null));

                    mag.add(new Terminal(TokenType.RIGHT_ROUND_BR));
                    mag.add(new NonTerminal(CFGState.J));
                    mag.add(new Terminal(TokenType.LEFT_ROUND_BR));

                }
        );

        // Правила для C

        generationTable.get(CFGState.C).put(
                TokenType.LEFT_SQR_BR, () -> {
                    generationSteps.add(new RPNGenerationStep(RPNGenerationStepType.OPERATION, "i"));
                    generationSteps.add(new RPNGenerationStep(RPNGenerationStepType.SKIP, null));
                    generationSteps.add(new RPNGenerationStep(RPNGenerationStepType.SKIP, null));

                    mag.add(new Terminal(TokenType.RIGHT_SQR_BR));
                    mag.add(new NonTerminal(CFGState.J));
                    mag.add(new Terminal(TokenType.LEFT_SQR_BR));

                }
        );
    }

}
