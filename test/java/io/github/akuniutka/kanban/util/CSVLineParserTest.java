package io.github.akuniutka.kanban.util;

import io.github.akuniutka.kanban.exception.CSVParsingException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CSVLineParserTest {
    private static final String WRONG_EXCEPTION_MESSAGE = "message for exception is wrong";

    @Test
    public void shouldInstantiateCSVLineParser() {
        new CSVLineParser("");
    }

    @Test
    public void shouldThrowWhenNullStringPassed() {
        Exception exception = assertThrows(NullPointerException.class, () -> new CSVLineParser(null));
        assertEquals("cannot parse null string", exception.getMessage(), WRONG_EXCEPTION_MESSAGE);
    }

    @Test
    public void shouldReturnTokenWhenEmptyLine() {
        CSVLineParser parser = new CSVLineParser("");
        String token = parser.next();

        assertEquals("", token, "wrong value of token");
    }

    @Test
    public void shouldReturnEmptyTokenInStartOfLine() {
        CSVLineParser parser = new CSVLineParser(",,");
        String token = parser.next();

        assertEquals("", token, "wrong value of token");
    }

    @Test
    public void shouldReturnEmptyTokenInMiddleOfLine() {
        CSVLineParser parser = new CSVLineParser(",,");
        parser.next();
        String token = parser.next();

        assertEquals("", token, "wrong value of token");
    }

    @Test
    public void shouldReturnEmptyTokenInEndOfLine() {
        CSVLineParser parser = new CSVLineParser(",,");
        parser.next();
        parser.next();
        String token = parser.next();

        assertEquals("", token, "wrong value of token");
    }

    @Test
    public void shouldReturnNonEmptyTokenInStartOfLine() {
        CSVLineParser parser = new CSVLineParser("tokenA,tokenB,tokenC");
        String token = parser.next();

        assertEquals("tokenA", token, "wrong value of token");
    }

    @Test
    public void shouldReturnNonEmptyTokenInMiddleOfLine() {
        CSVLineParser parser = new CSVLineParser("tokenA,tokenB,tokenC");
        parser.next();
        String token = parser.next();

        assertEquals("tokenB", token, "wrong value of token");
    }

    @Test
    public void shouldReturnNonEmptyTokenInEndOfLine() {
        CSVLineParser parser = new CSVLineParser("tokenA,tokenB,tokenC");
        parser.next();
        parser.next();
        String token = parser.next();

        assertEquals("tokenC", token, "wrong value of token");
    }

    @Test
    public void shouldReturnQuotedTokenInStartOfLine() {
        CSVLineParser parser = new CSVLineParser("\"tokenA\",tokenB,tokenC");
        String token = parser.next();

        assertEquals("\"tokenA\"", token, "wrong value of token");
    }

    @Test
    public void shouldReturnQuotedTokenInMiddleOfLine() {
        CSVLineParser parser = new CSVLineParser("tokenA,\"tokenB\",tokenC");
        parser.next();
        String token = parser.next();

        assertEquals("\"tokenB\"", token, "wrong value of token");
    }

    @Test
    public void shouldReturnQuotedTokenInEndOfLine() {
        CSVLineParser parser = new CSVLineParser("tokenA,tokenB,\"tokenC\"");
        parser.next();
        parser.next();
        String token = parser.next();

        assertEquals("\"tokenC\"", token, "wrong value of token");
    }

    @Test
    public void shouldHaveNextWhenLineEmpty() {
        CSVLineParser parser = new CSVLineParser("");

        assertTrue(parser.hasNext(), "empty line should have one token");
    }

    @Test
    public void shouldHaveNextWhenLineNotEmpty() {
        CSVLineParser parser = new CSVLineParser("token");

        assertTrue(parser.hasNext(), "nonempty line should have one token");
    }

    @Test
    public void shouldHaveNextWhenSeveralTokensInLine() {
        CSVLineParser parser = new CSVLineParser("tokenA,tokenB,tokenC");

        assertTrue(parser.hasNext(), "nonempty line should have one token");
    }

    @Test
    public void shouldHaveNextWhenNotLastTokenRetrieved() {
        CSVLineParser parser = new CSVLineParser("tokenA,tokenB");
        parser.next();

        assertTrue(parser.hasNext(), "should be another token available");
    }

    @Test
    public void shouldHaveNoNextTokenWhenTokenRetrievedFromEmptyLine() {
        CSVLineParser parser = new CSVLineParser("");
        parser.next();

        assertFalse(parser.hasNext(), "should have no more tokens");
    }

    @Test
    public void shouldHaveNoNextTokenWhenTheOnlyTokenRetrieved() {
        CSVLineParser parser = new CSVLineParser("token");
        parser.next();

        assertFalse(parser.hasNext(), "should have no more tokens");
    }

    @Test
    public void shouldHaveNoNextTokenWhenTheOnlyQuotedTokenRetrieved() {
        CSVLineParser parser = new CSVLineParser("\"token\"");
        parser.next();

        assertFalse(parser.hasNext(), "should have no more tokens");
    }

    @Test
    public void shouldHaveNoNextTokenWhenLastTokenRetrieved() {
        CSVLineParser parser = new CSVLineParser("tokenA,tokenB");
        parser.next();
        parser.next();

        assertFalse(parser.hasNext(), "should have no more tokens");
    }

    @Test
    public void shouldHaveNoNextTokenWhenLastQuotedTokenRetrieved() {
        CSVLineParser parser = new CSVLineParser("tokenA,\"tokenB\"");
        parser.next();
        parser.next();

        assertFalse(parser.hasNext(), "should have no more tokens");
    }

    @Test
    public void shouldThrowWhenRetrievingAfterTokenRetrievedFromEmptyLine() {
        CSVLineParser parser = new CSVLineParser("");
        parser.next();

        Exception exception = assertThrows(CSVParsingException.class, parser::next);
        assertEquals("unexpected end of line", exception.getMessage(), WRONG_EXCEPTION_MESSAGE);
    }

    @Test
    public void shouldThrowWhenRetrievingAfterTheOnlyTokenRetrieved() {
        CSVLineParser parser = new CSVLineParser("token");
        parser.next();

        Exception exception = assertThrows(CSVParsingException.class, parser::next);
        assertEquals("unexpected end of line", exception.getMessage(), WRONG_EXCEPTION_MESSAGE);
    }

    @Test
    public void shouldThrowWhenRetrievingAfterTheOnlyQuotedTokenRetrieved() {
        CSVLineParser parser = new CSVLineParser("\"token\"");
        parser.next();

        Exception exception = assertThrows(CSVParsingException.class, parser::next);
        assertEquals("unexpected end of line", exception.getMessage(), WRONG_EXCEPTION_MESSAGE);
    }

    @Test
    public void shouldThrowWhenRetrievingAfterLastTokenRetrieved() {
        CSVLineParser parser = new CSVLineParser("tokenA,tokenB");
        parser.next();
        parser.next();

        Exception exception = assertThrows(CSVParsingException.class, parser::next);
        assertEquals("unexpected end of line", exception.getMessage(), WRONG_EXCEPTION_MESSAGE);
    }

    @Test
    public void shouldThrowWhenRetrievingAfterLastQuotedTokenRetrieved() {
        CSVLineParser parser = new CSVLineParser("tokenA,\"tokenB\"");
        parser.next();
        parser.next();

        Exception exception = assertThrows(CSVParsingException.class, parser::next);
        assertEquals("unexpected end of line", exception.getMessage(), WRONG_EXCEPTION_MESSAGE);
    }

    @Test
    public void shouldThrowWhenQuoteAfterNonComma() {
        CSVLineParser parser = new CSVLineParser("tokenA\"tokenB\"tokenC");

        Exception exception = assertThrows(CSVParsingException.class, parser::next);
        assertEquals("no comma before opening double quote", exception.getMessage(), WRONG_EXCEPTION_MESSAGE);
    }

    @Test
    public void shouldThrowWhenNoClosingQuote() {
        CSVLineParser parser = new CSVLineParser("\"tokenA.tokenB,tokenC");

        Exception exception = assertThrows(CSVParsingException.class, parser::next);
        assertEquals("no closing double quote", exception.getMessage(), WRONG_EXCEPTION_MESSAGE);
    }

    @Test
    public void shouldThrowWhenNonCommaAfterQuote() {
        CSVLineParser parser = new CSVLineParser("\"tokenA\"tokenB,tokenC");

        Exception exception = assertThrows(CSVParsingException.class, parser::next);
        assertEquals("no comma after closing double quote", exception.getMessage(), WRONG_EXCEPTION_MESSAGE);
    }
}