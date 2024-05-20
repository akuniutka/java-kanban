package io.github.akuniutka.kanban.util;

import io.github.akuniutka.kanban.exception.CSVParsingException;

import java.util.Objects;

public class CSVLineParser {
    private final String line;
    private int prevDelimiterAt;

    public CSVLineParser(String line) {
        Objects.requireNonNull(line, "cannot parse null string");
        this.line = line;
        this.prevDelimiterAt = -1;
    }

    public boolean hasNext() {
        return prevDelimiterAt != line.length();
    }

    public CSVToken next() {
        if (!hasNext()) {
            throw new CSVParsingException("unexpected end of line", prevDelimiterAt + 1);
        }
        int startIndex = prevDelimiterAt + 1;
        boolean isQuoted = false;
        if (line.length() > startIndex && line.charAt(startIndex) == '"') {
            isQuoted = true;
            prevDelimiterAt = line.indexOf('"', startIndex + 1);
            if (prevDelimiterAt == -1) {
                throw new CSVParsingException("double quote expected", line.length() + 1);
            }
            prevDelimiterAt++;
            if (prevDelimiterAt != line.length() && line.charAt(prevDelimiterAt) != ',') {
                throw new CSVParsingException("comma expected", prevDelimiterAt + 1);
            }
        } else {
            prevDelimiterAt = line.indexOf(',', startIndex);
            if (prevDelimiterAt == -1) {
                prevDelimiterAt = line.length();
            }
            int doubleQuoteIndex = line.indexOf('"', startIndex);
            if (doubleQuoteIndex != -1 && doubleQuoteIndex < prevDelimiterAt) {
                throw new CSVParsingException("unexpected double quote", doubleQuoteIndex + 1);
            }
        }
        return new CSVToken(startIndex, line.substring(startIndex, prevDelimiterAt), isQuoted);
    }
}
