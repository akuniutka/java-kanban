package io.github.akuniutka.kanban.exception;

import java.io.IOException;

public class ManagerSaveException extends ManagerException {
    public ManagerSaveException(String message, IOException cause) {
        super(message, cause);
    }
}
