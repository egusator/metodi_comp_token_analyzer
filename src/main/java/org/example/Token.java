package org.example;

public class Token {

    private int number;

    private String value;

    private TokenType type;

    @Override
    public String toString() {
        return "{" + type + ", " + value + '}';

    }

    public Token(int number, String value, TokenType type) {
        this.number = number;
        this.value = value;
        this.type = type;
    }

    public int getNumber() {
        return number;
    }

    public void setNumber(int number) {
        this.number = number;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public TokenType getType() {
        return type;
    }

    public void setType(TokenType type) {
        this.type = type;
    }

}
