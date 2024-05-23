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

    public String next() {
        if (!hasNext()) {
            throw new CSVParsingException("unexpected end of line");
        }
        int startIndex = prevDelimiterAt + 1;
        if (line.length() > startIndex && line.charAt(startIndex) == '"') {
            prevDelimiterAt = line.indexOf('"', startIndex + 1);
            if (prevDelimiterAt == -1) {
                throw new CSVParsingException("no closing double quote");
            }
            prevDelimiterAt++;
            if (prevDelimiterAt != line.length() && line.charAt(prevDelimiterAt) != ',') {
                throw new CSVParsingException("no comma after closing double quote");
            }
        } else {
            prevDelimiterAt = line.indexOf(',', startIndex);
            if (prevDelimiterAt == -1) {
                prevDelimiterAt = line.length();
            }
            int doubleQuoteIndex = line.indexOf('"', startIndex);
            if (doubleQuoteIndex != -1 && doubleQuoteIndex < prevDelimiterAt) {
                throw new CSVParsingException("no comma before opening double quote");
            }
        }
        return line.substring(startIndex, prevDelimiterAt);
    }
}
