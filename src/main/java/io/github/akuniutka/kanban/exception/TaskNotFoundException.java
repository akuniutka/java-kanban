package io.github.akuniutka.kanban.exception;

public class TaskNotFoundException extends ManagerException {
    public TaskNotFoundException(String message) {
        super(message);
    }
}
