package org.example;

public abstract class MagElement<T> {
    private T value;

    public T getValue() {
        return this.value;
    }

    public boolean isTerminal() {
        return isTerminal;
    }

    public void setTerminal(boolean terminal) {
        isTerminal = terminal;
    }

    @Override
    public String toString() {
        return value.toString();
    }

    private boolean isTerminal;

    public MagElement(boolean isTerminal, T value) {
        this.isTerminal = isTerminal;
        this.value = value;
    }
}
