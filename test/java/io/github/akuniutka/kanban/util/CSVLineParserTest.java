package io.github.akuniutka.kanban.util;

import io.github.akuniutka.kanban.exception.CSVParsingException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CSVLineParserTest {
    private static final String WRONG_EXCEPTION_MESSAGE = "message for exception is wrong";
    private static final String WRONG_POSITION = "wrong position in string";

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
        CSVToken token = parser.next();

        assertNotNull(token, "should return nonnull token");
        assertEquals(0, token.position(), "wrong starting position of token");
        assertEquals("", token.value(), "wrong value of token");
        assertFalse(token.isQuoted(), "token should be reported as not quoted");
    }

    @Test
    public void shouldReturnEmptyTokenInStartOfLine() {
        CSVLineParser parser = new CSVLineParser(",,");
        CSVToken token = parser.next();

        assertNotNull(token, "should return nonnull token");
        assertEquals(0, token.position(), "wrong starting position of token");
        assertEquals("", token.value(), "wrong value of token");
        assertFalse(token.isQuoted(), "token should be reported as not quoted");
    }

    @Test
    public void shouldReturnEmptyTokenInMiddleOfLine() {
        CSVLineParser parser = new CSVLineParser(",,");
        parser.next();
        CSVToken token = parser.next();

        assertNotNull(token, "should return nonnull token");
        assertEquals(1, token.position(), "wrong starting position of token");
        assertEquals("", token.value(), "wrong value of token");
        assertFalse(token.isQuoted(), "token should be reported as not quoted");
    }

    @Test
    public void shouldReturnEmptyTokenInEndOfLine() {
        CSVLineParser parser = new CSVLineParser(",,");
        parser.next();
        parser.next();
        CSVToken token = parser.next();

        assertNotNull(token, "should return nonnull token");
        assertEquals(2, token.position(), "wrong starting position of token");
        assertEquals("", token.value(), "wrong value of token");
        assertFalse(token.isQuoted(), "token should be reported as not quoted");
    }

    @Test
    public void shouldReturnNonEmptyTokenInStartOfLine() {
        CSVLineParser parser = new CSVLineParser("tokenA,tokenB,tokenC");
        CSVToken token = parser.next();

        assertNotNull(token, "should return nonnull token");
        assertEquals(0, token.position(), "wrong starting position of token");
        assertEquals("tokenA", token.value(), "wrong value of token");
        assertFalse(token.isQuoted(), "token should be reported as not quoted");
    }

    @Test
    public void shouldReturnNonEmptyTokenInMiddleOfLine() {
        CSVLineParser parser = new CSVLineParser("tokenA,tokenB,tokenC");
        parser.next();
        CSVToken token = parser.next();

        assertNotNull(token, "should return nonnull token");
        assertEquals(7, token.position(), "wrong starting position of token");
        assertEquals("tokenB", token.value(), "wrong value of token");
        assertFalse(token.isQuoted(), "token should be reported as not quoted");
    }

    @Test
    public void shouldReturnNonEmptyTokenInEndOfLine() {
        CSVLineParser parser = new CSVLineParser("tokenA,tokenB,tokenC");
        parser.next();
        parser.next();
        CSVToken token = parser.next();

        assertNotNull(token, "should return nonnull token");
        assertEquals(14, token.position(), "wrong starting position of token");
        assertEquals("tokenC", token.value(), "wrong value of token");
        assertFalse(token.isQuoted(), "token should be reported as not quoted");
    }

    @Test
    public void shouldReturnQuotedTokenInStartOfLine() {
        CSVLineParser parser = new CSVLineParser("\"tokenA\",tokenB,tokenC");
        CSVToken token = parser.next();

        assertNotNull(token, "should return nonnull token");
        assertEquals(0, token.position(), "wrong starting position of token");
        assertEquals("\"tokenA\"", token.value(), "wrong value of token");
        assertTrue(token.isQuoted(), "token should be reported as quoted");
    }

    @Test
    public void shouldReturnQuotedTokenInMiddleOfLine() {
        CSVLineParser parser = new CSVLineParser("tokenA,\"tokenB\",tokenC");
        parser.next();
        CSVToken token = parser.next();

        assertNotNull(token, "should return nonnull token");
        assertEquals(7, token.position(), "wrong starting position of token");
        assertEquals("\"tokenB\"", token.value(), "wrong value of token");
        assertTrue(token.isQuoted(), "token should be reported as quoted");
    }

    @Test
    public void shouldReturnQuotedTokenInEndOfLine() {
        CSVLineParser parser = new CSVLineParser("tokenA,tokenB,\"tokenC\"");
        parser.next();
        parser.next();
        CSVToken token = parser.next();

        assertNotNull(token, "should return nonnull token");
        assertEquals(14, token.position(), "wrong starting position of token");
        assertEquals("\"tokenC\"", token.value(), "wrong value of token");
        assertTrue(token.isQuoted(), "token should be reported as quoted");
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
        String expectedMessage = "unexpected end of line";
        int expectedPosition = 1;
        parser.next();

        CSVParsingException exception = assertThrows(CSVParsingException.class, parser::next);
        assertEquals(expectedMessage, exception.getShortMessage(), WRONG_EXCEPTION_MESSAGE);
        assertEquals(expectedPosition, exception.getPositionInLine(), WRONG_POSITION);
        assertEquals(expectedMessage + " at " + expectedPosition, exception.getMessage(), WRONG_EXCEPTION_MESSAGE);
    }

    @Test
    public void shouldThrowWhenRetrievingAfterTheOnlyTokenRetrieved() {
        CSVLineParser parser = new CSVLineParser("token");
        String expectedMessage = "unexpected end of line";
        int expectedPosition = 6;
        parser.next();

        CSVParsingException exception = assertThrows(CSVParsingException.class, parser::next);
        assertEquals(expectedMessage, exception.getShortMessage(), WRONG_EXCEPTION_MESSAGE);
        assertEquals(expectedPosition, exception.getPositionInLine(), WRONG_POSITION);
        assertEquals(expectedMessage + " at " + expectedPosition, exception.getMessage(), WRONG_EXCEPTION_MESSAGE);
    }

    @Test
    public void shouldThrowWhenRetrievingAfterTheOnlyQuotedTokenRetrieved() {
        CSVLineParser parser = new CSVLineParser("\"token\"");
        String expectedMessage = "unexpected end of line";
        int expectedPosition = 8;
        parser.next();

        CSVParsingException exception = assertThrows(CSVParsingException.class, parser::next);
        assertEquals(expectedMessage, exception.getShortMessage(), WRONG_EXCEPTION_MESSAGE);
        assertEquals(expectedPosition, exception.getPositionInLine(), WRONG_POSITION);
        assertEquals(expectedMessage + " at " + expectedPosition, exception.getMessage(), WRONG_EXCEPTION_MESSAGE);
    }

    @Test
    public void shouldThrowWhenRetrievingAfterLastTokenRetrieved() {
        CSVLineParser parser = new CSVLineParser("tokenA,tokenB");
        String expectedMessage = "unexpected end of line";
        int expectedPosition = 14;
        parser.next();
        parser.next();

        CSVParsingException exception = assertThrows(CSVParsingException.class, parser::next);
        assertEquals(expectedMessage, exception.getShortMessage(), WRONG_EXCEPTION_MESSAGE);
        assertEquals(expectedPosition, exception.getPositionInLine(), WRONG_POSITION);
        assertEquals(expectedMessage + " at " + expectedPosition, exception.getMessage(), WRONG_EXCEPTION_MESSAGE);
    }

    @Test
    public void shouldThrowWhenRetrievingAfterLastQuotedTokenRetrieved() {
        CSVLineParser parser = new CSVLineParser("tokenA,\"tokenB\"");
        String expectedMessage = "unexpected end of line";
        int expectedPosition = 16;
        parser.next();
        parser.next();

        CSVParsingException exception = assertThrows(CSVParsingException.class, parser::next);
        assertEquals(expectedMessage, exception.getShortMessage(), WRONG_EXCEPTION_MESSAGE);
        assertEquals(expectedPosition, exception.getPositionInLine(), WRONG_POSITION);
        assertEquals(expectedMessage + " at " + expectedPosition, exception.getMessage(), WRONG_EXCEPTION_MESSAGE);
    }

    @Test
    public void shouldThrowWhenQuoteAfterNonComma() {
        CSVLineParser parser = new CSVLineParser("tokenA\"tokenB\"tokenC");
        String expectedMessage = "unexpected double quote";
        int expectedPosition = 7;

        CSVParsingException exception = assertThrows(CSVParsingException.class, parser::next);
        assertEquals(expectedMessage, exception.getShortMessage(), WRONG_EXCEPTION_MESSAGE);
        assertEquals(expectedPosition, exception.getPositionInLine(), WRONG_POSITION);
        assertEquals(expectedMessage + " at " + expectedPosition, exception.getMessage(), WRONG_EXCEPTION_MESSAGE);
    }

    @Test
    public void shouldThrowWhenNoClosingQuote() {
        CSVLineParser parser = new CSVLineParser("\"tokenA.tokenB,tokenC");
        String expectedMessage = "double quote expected";
        int expectedPosition = 22;

        CSVParsingException exception = assertThrows(CSVParsingException.class, parser::next);
        assertEquals(expectedMessage, exception.getShortMessage(), WRONG_EXCEPTION_MESSAGE);
        assertEquals(expectedPosition, exception.getPositionInLine(), WRONG_POSITION);
        assertEquals(expectedMessage + " at " + expectedPosition, exception.getMessage(), WRONG_EXCEPTION_MESSAGE);
    }

    @Test
    public void shouldThrowWhenNonCommaAfterQuote() {
        CSVLineParser parser = new CSVLineParser("\"tokenA\"tokenB,tokenC");
        String expectedMessage = "comma expected";
        int expectedPosition = 9;

        CSVParsingException exception = assertThrows(CSVParsingException.class, parser::next);
        assertEquals(expectedMessage, exception.getShortMessage(), WRONG_EXCEPTION_MESSAGE);
        assertEquals(expectedPosition, exception.getPositionInLine(), WRONG_POSITION);
        assertEquals(expectedMessage + " at " + expectedPosition, exception.getMessage(), WRONG_EXCEPTION_MESSAGE);
    }
}