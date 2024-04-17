package org.example;

public class Token {

    private int number;

    private String value;

    private String type;

    @Override
    public String toString() {
        return "" + value + '\n';

    }

    public Token(int number, String value, String type) {
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

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

}
