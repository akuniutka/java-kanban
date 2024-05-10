package io.github.akuniutka.kanban.exception;

public class CSVParsingException extends RuntimeException {
    private final String shortMessage;
    private final int positionInLine;

    public CSVParsingException(String message, int positionInLine) {
        super(message + " at " + positionInLine);
        this.shortMessage = message;
        this.positionInLine = positionInLine;
    }

    public String getShortMessage() {
        return shortMessage;
    }

    public int getPositionInLine() {
        return positionInLine;
    }
}
