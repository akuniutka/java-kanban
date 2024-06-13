package io.github.akuniutka.kanban.exception;

public class TaskOverlapException extends ManagerValidationException {
    public TaskOverlapException(String message) {
        super(message);
    }
}
