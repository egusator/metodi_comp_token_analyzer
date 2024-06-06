package org.example;

import java.util.*;

public class RPNGenerator {

    private static Stack<RPNGenerationStep> generationSteps;

    private static int markerNumber = 0;

    private static int whileLastMarkerNumber;

    private static Map<TableType, List<String>> variableTables;

    private static Map<String, Runnable> programs;
    private static TableType CURRENT_TABLE_TYPE;

    private static List<RPNStep> resultRPN;

    private static List<Token> input;

    private static Token currentToken;

    private static Stack<MagElement> mag;

    private static HashMap<String, Integer> markers;

    private static Map<CFGState, HashMap<TokenType, Runnable>> generationTable;

    static {
        generationSteps = new Stack<>();
        resultRPN = new ArrayList<>();
        mag = new Stack<>();
        initGenerationTable();
        initPrograms();
    }

    public static List<RPNStep> generateRpnFromTokensList(List<Token> userInput) {

        resultRPN = new ArrayList<>();
        mag = new Stack<>();

        mag.add(new NonTerminal(CFGState.B));

        generationSteps = new Stack<>();

        Boolean flag = false;
        input = userInput;

        for (int i = 0; i < input.size() || !generationSteps.empty() || !mag.empty(); ) {
            if (i < input.size())
                currentToken = userInput.get(i);

            MagElement magElement = mag.pop();

            if (magElement.isTerminal()) {
                Terminal terminal = (Terminal) magElement;

                if (currentToken.getType() == terminal.getValue()) {
                    i++;
                }
            } else {
                NonTerminal nonTerminal = (NonTerminal) magElement;
                Runnable runnable = generationTable.get(nonTerminal.getValue()).get(currentToken.getType());
                if (runnable != null)
                    runnable.run();
            }
            RPNGenerationStep step = null;
            if (flag) {
                step = generationSteps.pop();
            } else {
                flag = true;
            }

            System.out.println("mag: " + mag);
            System.out.println("steps: " + generationSteps);
            System.out.println("result: " + resultRPN);
            System.out.println("current token: " + currentToken);
            System.out.println();

            if (step != null && step.getType() != RPNGenerationStepType.SKIP) {
                performSemanticForGen(step);
            }
        }
        System.out.println(markers);
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
                    resultRPN.add(new RPNStep(RPNStepType.INT_VARIABLE, currentToken.getValue()));
                }
                case DOUBLE -> {
                    resultRPN.add(new RPNStep(RPNStepType.DOUBLE_VARIABLE, currentToken.getValue()));
                }
                case BOOLEAN -> {
                    resultRPN.add(new RPNStep(RPNStepType.BOOLEAN_VARIABLE, currentToken.getValue()));
                }
            }
        } else if (step.getType() == RPNGenerationStepType.OPERATION) {
            resultRPN.add(new RPNStep(RPNStepType.OPERATION, step.getValue()));
        }
    }


    private static void initPrograms() {

        variableTables = new HashMap<>();
        markers = new HashMap<>();

        for (TableType type : TableType.values()) {
            variableTables.put(type, new ArrayList<>());
        }

        programs = new HashMap<>();

        programs.put("1", () -> {
            markerNumber++;
            String markerName = "m" + markerNumber;
            resultRPN.add(new RPNStep(RPNStepType.MARKER, markerName));
            markers.put(markerName, null);
            resultRPN.add(new RPNStep(RPNStepType.OPERATION, "jf"));
        });

        programs.put("2", () -> {
            String markerName = "m" + markerNumber;
            markers.put(markerName, resultRPN.size() - 1);
        });

        programs.put("3", () -> {
            Integer markerIndex = resultRPN.size() + 1;
            String markerName = "m" + markerNumber;
            markers.put(markerName, markerIndex);
            markerNumber++;
            String markerName2 = "m" + markerNumber;
            resultRPN.add(new RPNStep(RPNStepType.MARKER, markerName2));
            markers.put(markerName2, markerIndex);
            resultRPN.add(new RPNStep(RPNStepType.OPERATION, "j"));
        });

        programs.put("4", () -> {
            Integer markerIndex = resultRPN.size() - 1;
            String markerName = "m" + markerNumber;
            markers.put(markerName, markerIndex);
            whileLastMarkerNumber = markerNumber + 1;
        });

        programs.put("5", () -> {
            String markerName = "m" + whileLastMarkerNumber;
            Integer markerIndex = resultRPN.size() + 1;
            markers.put(markerName, markerIndex);
            String m0 = "m" + (whileLastMarkerNumber - 1);
            resultRPN.add(new RPNStep(RPNStepType.MARKER, m0));
            resultRPN.add(new RPNStep(RPNStepType.OPERATION, "j"));
        });

        programs.put("11", () -> {
            CURRENT_TABLE_TYPE = TableType.INT;
        });

        programs.put("12", () -> {
            CURRENT_TABLE_TYPE = TableType.DOUBLE;
        });

        programs.put("13", () -> {
            CURRENT_TABLE_TYPE = TableType.BOOLEAN;
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
                    generationSteps.add(new RPNGenerationStep(RPNGenerationStepType.SKIP, null));
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
                    generationSteps.add(new RPNGenerationStep(RPNGenerationStepType.SKIP, null));

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
                    generationSteps.add(new RPNGenerationStep(RPNGenerationStepType.SKIP, null));
                    generationSteps.add(new RPNGenerationStep(RPNGenerationStepType.OPERATION, "i"));

                    generationSteps.add(new RPNGenerationStep(RPNGenerationStepType.SKIP, null));
                    generationSteps.add(new RPNGenerationStep(RPNGenerationStepType.SKIP, null));
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
                    generationSteps.add(new RPNGenerationStep(RPNGenerationStepType.VARIABLE, null));
                    generationSteps.add(new RPNGenerationStep(RPNGenerationStepType.SKIP, null));
                    mag.add(new NonTerminal(CFGState.L));

                    mag.add(new Terminal(TokenType.VARIABLE));
                }
        );

        generationTable.get(CFGState.J).put(
                TokenType.CONST, () -> {
                    generationSteps.add(new RPNGenerationStep(RPNGenerationStepType.SKIP, null));
                    generationSteps.add(new RPNGenerationStep(RPNGenerationStepType.SKIP, null));
                    generationSteps.add(new RPNGenerationStep(RPNGenerationStepType.SKIP, null));
                    generationSteps.add(new RPNGenerationStep(RPNGenerationStepType.SKIP, null));
                    generationSteps.add(new RPNGenerationStep(RPNGenerationStepType.SKIP, null));
                    generationSteps.add(new RPNGenerationStep(RPNGenerationStepType.SKIP, null));
                    generationSteps.add(new RPNGenerationStep(RPNGenerationStepType.SKIP, null));
                    generationSteps.add(new RPNGenerationStep(RPNGenerationStepType.CONST, null));

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
                    generationSteps.add(new RPNGenerationStep(RPNGenerationStepType.OPERATION, "-'"));

                    generationSteps.add(new RPNGenerationStep(RPNGenerationStepType.SKIP, null));
                    generationSteps.add(new RPNGenerationStep(RPNGenerationStepType.SKIP, null));
                    generationSteps.add(new RPNGenerationStep(RPNGenerationStepType.SKIP, null));
                    generationSteps.add(new RPNGenerationStep(RPNGenerationStepType.SKIP, null));
                    generationSteps.add(new RPNGenerationStep(RPNGenerationStepType.SKIP, null));
                    generationSteps.add(new RPNGenerationStep(RPNGenerationStepType.SKIP, null));
                    generationSteps.add(new RPNGenerationStep(RPNGenerationStepType.SKIP, null));
                    generationSteps.add(new RPNGenerationStep(RPNGenerationStepType.SKIP, null));
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
                    generationSteps.add(new RPNGenerationStep(RPNGenerationStepType.OPERATION, "!'"));

                    generationSteps.add(new RPNGenerationStep(RPNGenerationStepType.SKIP, null));
                    generationSteps.add(new RPNGenerationStep(RPNGenerationStepType.SKIP, null));
                    generationSteps.add(new RPNGenerationStep(RPNGenerationStepType.SKIP, null));
                    generationSteps.add(new RPNGenerationStep(RPNGenerationStepType.SKIP, null));
                    generationSteps.add(new RPNGenerationStep(RPNGenerationStepType.SKIP, null));
                    generationSteps.add(new RPNGenerationStep(RPNGenerationStepType.SKIP, null));
                    generationSteps.add(new RPNGenerationStep(RPNGenerationStepType.SKIP, null));
                    generationSteps.add(new RPNGenerationStep(RPNGenerationStepType.SKIP, null));
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
                    generationSteps.add(new RPNGenerationStep(RPNGenerationStepType.OPERATION, "+"));

                    generationSteps.add(new RPNGenerationStep(RPNGenerationStepType.SKIP, null));
                    generationSteps.add(new RPNGenerationStep(RPNGenerationStepType.SKIP, null));
                    generationSteps.add(new RPNGenerationStep(RPNGenerationStepType.SKIP, null));
                    generationSteps.add(new RPNGenerationStep(RPNGenerationStepType.SKIP, null));
                    generationSteps.add(new RPNGenerationStep(RPNGenerationStepType.SKIP, null));
                    generationSteps.add(new RPNGenerationStep(RPNGenerationStepType.SKIP, null));
                    generationSteps.add(new RPNGenerationStep(RPNGenerationStepType.SKIP, null));
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
                    generationSteps.add(new RPNGenerationStep(RPNGenerationStepType.OPERATION, "-"));

                    generationSteps.add(new RPNGenerationStep(RPNGenerationStepType.SKIP, null));
                    generationSteps.add(new RPNGenerationStep(RPNGenerationStepType.SKIP, null));
                    generationSteps.add(new RPNGenerationStep(RPNGenerationStepType.SKIP, null));
                    generationSteps.add(new RPNGenerationStep(RPNGenerationStepType.SKIP, null));
                    generationSteps.add(new RPNGenerationStep(RPNGenerationStepType.SKIP, null));
                    generationSteps.add(new RPNGenerationStep(RPNGenerationStepType.SKIP, null));
                    generationSteps.add(new RPNGenerationStep(RPNGenerationStepType.SKIP, null));
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
                    generationSteps.add(new RPNGenerationStep(RPNGenerationStepType.OPERATION, "*"));

                    generationSteps.add(new RPNGenerationStep(RPNGenerationStepType.SKIP, null));
                    generationSteps.add(new RPNGenerationStep(RPNGenerationStepType.SKIP, null));
                    generationSteps.add(new RPNGenerationStep(RPNGenerationStepType.SKIP, null));
                    generationSteps.add(new RPNGenerationStep(RPNGenerationStepType.SKIP, null));
                    generationSteps.add(new RPNGenerationStep(RPNGenerationStepType.SKIP, null));
                    generationSteps.add(new RPNGenerationStep(RPNGenerationStepType.SKIP, null));
                    generationSteps.add(new RPNGenerationStep(RPNGenerationStepType.SKIP, null));
                    generationSteps.add(new RPNGenerationStep(RPNGenerationStepType.SKIP, null));
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
                    generationSteps.add(new RPNGenerationStep(RPNGenerationStepType.OPERATION, "/"));

                    generationSteps.add(new RPNGenerationStep(RPNGenerationStepType.SKIP, null));
                    generationSteps.add(new RPNGenerationStep(RPNGenerationStepType.SKIP, null));
                    generationSteps.add(new RPNGenerationStep(RPNGenerationStepType.SKIP, null));
                    generationSteps.add(new RPNGenerationStep(RPNGenerationStepType.SKIP, null));
                    generationSteps.add(new RPNGenerationStep(RPNGenerationStepType.SKIP, null));
                    generationSteps.add(new RPNGenerationStep(RPNGenerationStepType.SKIP, null));
                    generationSteps.add(new RPNGenerationStep(RPNGenerationStepType.SKIP, null));
                    generationSteps.add(new RPNGenerationStep(RPNGenerationStepType.SKIP, null));

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
                    generationSteps.add(new RPNGenerationStep(RPNGenerationStepType.OPERATION, ">"));

                    generationSteps.add(new RPNGenerationStep(RPNGenerationStepType.SKIP, null));
                    generationSteps.add(new RPNGenerationStep(RPNGenerationStepType.SKIP, null));
                    generationSteps.add(new RPNGenerationStep(RPNGenerationStepType.SKIP, null));
                    generationSteps.add(new RPNGenerationStep(RPNGenerationStepType.SKIP, null));
                    generationSteps.add(new RPNGenerationStep(RPNGenerationStepType.SKIP, null));
                    generationSteps.add(new RPNGenerationStep(RPNGenerationStepType.SKIP, null));
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
                    generationSteps.add(new RPNGenerationStep(RPNGenerationStepType.OPERATION, "<"));

                    generationSteps.add(new RPNGenerationStep(RPNGenerationStepType.SKIP, null));
                    generationSteps.add(new RPNGenerationStep(RPNGenerationStepType.SKIP, null));
                    generationSteps.add(new RPNGenerationStep(RPNGenerationStepType.SKIP, null));
                    generationSteps.add(new RPNGenerationStep(RPNGenerationStepType.SKIP, null));
                    generationSteps.add(new RPNGenerationStep(RPNGenerationStepType.SKIP, null));
                    generationSteps.add(new RPNGenerationStep(RPNGenerationStepType.SKIP, null));
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
                    generationSteps.add(new RPNGenerationStep(RPNGenerationStepType.OPERATION, "&"));
                    generationSteps.add(new RPNGenerationStep(RPNGenerationStepType.SKIP, null));
                    generationSteps.add(new RPNGenerationStep(RPNGenerationStepType.SKIP, null));
                    generationSteps.add(new RPNGenerationStep(RPNGenerationStepType.SKIP, null));
                    generationSteps.add(new RPNGenerationStep(RPNGenerationStepType.SKIP, null));
                    mag.add(new NonTerminal(CFGState.N));
                    mag.add(new NonTerminal(CFGState.U));
                    mag.add(new NonTerminal(CFGState.T));
                    mag.add(new NonTerminal(CFGState.J));

                    mag.add(new Terminal(TokenType.AND));
                }
        );

        generationTable.get(CFGState.L).put(
                TokenType.OR, () -> {
                    generationSteps.add(new RPNGenerationStep(RPNGenerationStepType.OPERATION, "&"));

                    generationSteps.add(new RPNGenerationStep(RPNGenerationStepType.SKIP, null));
                    generationSteps.add(new RPNGenerationStep(RPNGenerationStepType.SKIP, null));
                    generationSteps.add(new RPNGenerationStep(RPNGenerationStepType.SKIP, null));
                    mag.add(new NonTerminal(CFGState.N));
                    mag.add(new NonTerminal(CFGState.U));
                    mag.add(new NonTerminal(CFGState.T));

                    mag.add(new Terminal(TokenType.OR));
                }
        );

        generationTable.get(CFGState.L).put(
                TokenType.LEFT_SQR_BR, () -> {
                    generationSteps.add(new RPNGenerationStep(RPNGenerationStepType.SKIP, null));
                    generationSteps.add(new RPNGenerationStep(RPNGenerationStepType.SKIP, null));
                    generationSteps.add(new RPNGenerationStep(RPNGenerationStepType.SKIP, null));
                    generationSteps.add(new RPNGenerationStep(RPNGenerationStepType.SKIP, null));
                    generationSteps.add(new RPNGenerationStep(RPNGenerationStepType.SKIP, null));
                    generationSteps.add(new RPNGenerationStep(RPNGenerationStepType.SKIP, null));
                    generationSteps.add(new RPNGenerationStep(RPNGenerationStepType.SKIP, null));
                    generationSteps.add(new RPNGenerationStep(RPNGenerationStepType.OPERATION, "i"));
                    generationSteps.add(new RPNGenerationStep(RPNGenerationStepType.SKIP, null));
                    generationSteps.add(new RPNGenerationStep(RPNGenerationStepType.SKIP, null));

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
                    generationSteps.add(new RPNGenerationStep(RPNGenerationStepType.SKIP, null));
                    generationSteps.add(new RPNGenerationStep(RPNGenerationStepType.SKIP, null));
                    mag.add(new NonTerminal(CFGState.J));

                    mag.add(new Terminal(TokenType.COMMA));
                }
        );

        generationTable.get(CFGState.L).put(
                TokenType.GREATER_EQUALS, () -> {
                    generationSteps.add(new RPNGenerationStep(RPNGenerationStepType.OPERATION, ">="));

                    generationSteps.add(new RPNGenerationStep(RPNGenerationStepType.SKIP, null));
                    generationSteps.add(new RPNGenerationStep(RPNGenerationStepType.SKIP, null));
                    generationSteps.add(new RPNGenerationStep(RPNGenerationStepType.SKIP, null));
                    generationSteps.add(new RPNGenerationStep(RPNGenerationStepType.SKIP, null));
                    generationSteps.add(new RPNGenerationStep(RPNGenerationStepType.SKIP, null));
                    generationSteps.add(new RPNGenerationStep(RPNGenerationStepType.SKIP, null));
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
                    generationSteps.add(new RPNGenerationStep(RPNGenerationStepType.OPERATION, "<="));

                    generationSteps.add(new RPNGenerationStep(RPNGenerationStepType.SKIP, null));
                    generationSteps.add(new RPNGenerationStep(RPNGenerationStepType.SKIP, null));
                    generationSteps.add(new RPNGenerationStep(RPNGenerationStepType.SKIP, null));
                    generationSteps.add(new RPNGenerationStep(RPNGenerationStepType.SKIP, null));
                    generationSteps.add(new RPNGenerationStep(RPNGenerationStepType.SKIP, null));
                    generationSteps.add(new RPNGenerationStep(RPNGenerationStepType.SKIP, null));

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
                    generationSteps.add(new RPNGenerationStep(RPNGenerationStepType.OPERATION, "=="));

                    generationSteps.add(new RPNGenerationStep(RPNGenerationStepType.SKIP, null));
                    generationSteps.add(new RPNGenerationStep(RPNGenerationStepType.SKIP, null));
                    generationSteps.add(new RPNGenerationStep(RPNGenerationStepType.SKIP, null));
                    generationSteps.add(new RPNGenerationStep(RPNGenerationStepType.SKIP, null));
                    generationSteps.add(new RPNGenerationStep(RPNGenerationStepType.SKIP, null));
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
                    generationSteps.add(new RPNGenerationStep(RPNGenerationStepType.OPERATION, "!="));

                    generationSteps.add(new RPNGenerationStep(RPNGenerationStepType.SKIP, null));
                    generationSteps.add(new RPNGenerationStep(RPNGenerationStepType.SKIP, null));
                    generationSteps.add(new RPNGenerationStep(RPNGenerationStepType.SKIP, null));
                    generationSteps.add(new RPNGenerationStep(RPNGenerationStepType.SKIP, null));
                    generationSteps.add(new RPNGenerationStep(RPNGenerationStepType.SKIP, null));
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
                TokenType.EQUALS, () -> {
                    generationSteps.add(new RPNGenerationStep(RPNGenerationStepType.OPERATION, "="));
                    mag.add(new Terminal(TokenType.EQUALS));
                }
        );

        generationTable.get(CFGState.K).put(
                TokenType.PLUS, () -> {
                    generationSteps.add(new RPNGenerationStep(RPNGenerationStepType.OPERATION, "+"));
                    mag.add(new Terminal(TokenType.EQUALS));
                }
        );

        generationTable.get(CFGState.K).put(
                TokenType.MINUS, () -> {
                    generationSteps.add(new RPNGenerationStep(RPNGenerationStepType.OPERATION, "-"));
                    mag.add(new Terminal(TokenType.EQUALS));
                }
        );

        generationTable.get(CFGState.K).put(
                TokenType.MULTIPLY, () -> {
                    generationSteps.add(new RPNGenerationStep(RPNGenerationStepType.OPERATION, "*"));
                    mag.add(new Terminal(TokenType.EQUALS));
                }
        );

        generationTable.get(CFGState.K).put(
                TokenType.DIV, () -> {
                    generationSteps.add(new RPNGenerationStep(RPNGenerationStepType.OPERATION, "/"));
                    mag.add(new Terminal(TokenType.EQUALS));
                }
        );

        //Правила для U

        generationTable.get(CFGState.U).put(
                TokenType.OR, () -> {
                    generationSteps.add(new RPNGenerationStep(RPNGenerationStepType.OPERATION, "="));

                    generationSteps.add(new RPNGenerationStep(RPNGenerationStepType.SKIP, null));
                    generationSteps.add(new RPNGenerationStep(RPNGenerationStepType.SKIP, null));
                    mag.add(new NonTerminal(CFGState.U));
                    mag.add(new NonTerminal(CFGState.J));

                    mag.add(new Terminal(TokenType.OR));

                }
        );

        //Правила для T

        generationTable.get(CFGState.T).put(
                TokenType.AND, () -> {
                    generationSteps.add(new RPNGenerationStep(RPNGenerationStepType.OPERATION, "&"));

                    generationSteps.add(new RPNGenerationStep(RPNGenerationStepType.SKIP, null));
                    generationSteps.add(new RPNGenerationStep(RPNGenerationStepType.SKIP, null));
                    mag.add(new NonTerminal(CFGState.W));
                    mag.add(new NonTerminal(CFGState.J));

                    mag.add(new Terminal(TokenType.AND));

                }
        );


        //Правила для V

        generationTable.get(CFGState.V).put(
                TokenType.EQUALS, () -> {
                    generationSteps.add(new RPNGenerationStep(RPNGenerationStepType.OPERATION, "=="));

                    generationSteps.add(new RPNGenerationStep(RPNGenerationStepType.SKIP, null));
                    generationSteps.add(new RPNGenerationStep(RPNGenerationStepType.SKIP, null));
                    mag.add(new NonTerminal(CFGState.W));
                    mag.add(new NonTerminal(CFGState.J));

                    mag.add(new Terminal(TokenType.EQUALS));

                }
        );

        generationTable.get(CFGState.V).put(
                TokenType.NOT_EQUALS, () -> {
                    generationSteps.add(new RPNGenerationStep(RPNGenerationStepType.OPERATION, "!="));

                    generationSteps.add(new RPNGenerationStep(RPNGenerationStepType.SKIP, null));
                    generationSteps.add(new RPNGenerationStep(RPNGenerationStepType.SKIP, null));
                    mag.add(new NonTerminal(CFGState.W));
                    mag.add(new NonTerminal(CFGState.J));

                    mag.add(new Terminal(TokenType.NOT_EQUALS));

                }
        );

        //Правила для W

        generationTable.get(CFGState.W).put(
                TokenType.GREATER, () -> {
                    generationSteps.add(new RPNGenerationStep(RPNGenerationStepType.OPERATION, ">"));

                    generationSteps.add(new RPNGenerationStep(RPNGenerationStepType.SKIP, null));
                    generationSteps.add(new RPNGenerationStep(RPNGenerationStepType.SKIP, null));
                    mag.add(new NonTerminal(CFGState.W));
                    mag.add(new NonTerminal(CFGState.J));

                    mag.add(new Terminal(TokenType.GREATER));

                }
        );

        generationTable.get(CFGState.W).put(
                TokenType.LESS, () -> {
                    generationSteps.add(new RPNGenerationStep(RPNGenerationStepType.OPERATION, "<"));
                    generationSteps.add(new RPNGenerationStep(RPNGenerationStepType.SKIP, null));
                    generationSteps.add(new RPNGenerationStep(RPNGenerationStepType.SKIP, null));
                    mag.add(new NonTerminal(CFGState.W));
                    mag.add(new NonTerminal(CFGState.J));

                    mag.add(new Terminal(TokenType.LESS));

                }
        );

        generationTable.get(CFGState.W).put(
                TokenType.GREATER_EQUALS, () -> {
                    generationSteps.add(new RPNGenerationStep(RPNGenerationStepType.OPERATION, ">="));
                    generationSteps.add(new RPNGenerationStep(RPNGenerationStepType.SKIP, null));
                    generationSteps.add(new RPNGenerationStep(RPNGenerationStepType.SKIP, null));
                    mag.add(new NonTerminal(CFGState.W));
                    mag.add(new NonTerminal(CFGState.J));

                    mag.add(new Terminal(TokenType.GREATER_EQUALS));

                }
        );

        generationTable.get(CFGState.W).put(
                TokenType.LESS_EQUALS, () -> {
                    generationSteps.add(new RPNGenerationStep(RPNGenerationStepType.OPERATION, "<="));
                    generationSteps.add(new RPNGenerationStep(RPNGenerationStepType.SKIP, null));
                    generationSteps.add(new RPNGenerationStep(RPNGenerationStepType.SKIP, null));
                    mag.add(new NonTerminal(CFGState.W));
                    mag.add(new NonTerminal(CFGState.J));

                    mag.add(new Terminal(TokenType.LESS_EQUALS));

                }
        );

        //Правила для X

        generationTable.get(CFGState.X).put(
                TokenType.PLUS, () -> {
                    generationSteps.add(new RPNGenerationStep(RPNGenerationStepType.OPERATION, "+"));
                    generationSteps.add(new RPNGenerationStep(RPNGenerationStepType.SKIP, null));
                    generationSteps.add(new RPNGenerationStep(RPNGenerationStepType.SKIP, null));
                    mag.add(new NonTerminal(CFGState.X));
                    mag.add(new NonTerminal(CFGState.J));

                    mag.add(new Terminal(TokenType.GREATER));

                }
        );

        generationTable.get(CFGState.X).put(
                TokenType.MINUS, () -> {
                    generationSteps.add(new RPNGenerationStep(RPNGenerationStepType.OPERATION, "-"));
                    generationSteps.add(new RPNGenerationStep(RPNGenerationStepType.SKIP, null));
                    generationSteps.add(new RPNGenerationStep(RPNGenerationStepType.SKIP, null));
                    mag.add(new NonTerminal(CFGState.X));
                    mag.add(new NonTerminal(CFGState.J));

                    mag.add(new Terminal(TokenType.MINUS));

                }
        );

        //Правила для Y

        generationTable.get(CFGState.Y).put(
                TokenType.MULTIPLY, () -> {
                    generationSteps.add(new RPNGenerationStep(RPNGenerationStepType.OPERATION, "*"));
                    generationSteps.add(new RPNGenerationStep(RPNGenerationStepType.SKIP, null));
                    generationSteps.add(new RPNGenerationStep(RPNGenerationStepType.SKIP, null));
                    mag.add(new NonTerminal(CFGState.X));

                    mag.add(new NonTerminal(CFGState.J));

                    mag.add(new Terminal(TokenType.MINUS));

                }
        );

        generationTable.get(CFGState.Y).put(
                TokenType.DIV, () -> {
                    generationSteps.add(new RPNGenerationStep(RPNGenerationStepType.OPERATION, "/"));
                    generationSteps.add(new RPNGenerationStep(RPNGenerationStepType.SKIP, null));
                    generationSteps.add(new RPNGenerationStep(RPNGenerationStepType.SKIP, null));
                    mag.add(new NonTerminal(CFGState.X));
                    mag.add(new NonTerminal(CFGState.J));

                    mag.add(new Terminal(TokenType.GREATER));

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

        generationTable.get(CFGState.C).put(
                TokenType.LEFT_SQR_BR, () -> {
                    generationSteps.add(new RPNGenerationStep(RPNGenerationStepType.SKIP, null));
                    generationSteps.add(new RPNGenerationStep(RPNGenerationStepType.SKIP, null));
                    generationSteps.add(new RPNGenerationStep(RPNGenerationStepType.SKIP, null));
                    mag.add(new Terminal(TokenType.RIGHT_SQR_BR));
                    mag.add(new NonTerminal(CFGState.J));

                    mag.add(new Terminal(TokenType.LEFT_SQR_BR));

                }
        );
    }

}
