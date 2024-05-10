package io.github.akuniutka.kanban.exception;

public class ManagerException extends RuntimeException {
    public ManagerException(String message) {
        super(message);
    }

    public ManagerException(String message, Throwable cause) {
        super(message, cause);
    }
}
