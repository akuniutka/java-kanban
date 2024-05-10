package io.github.akuniutka.kanban.exception;

import java.io.IOException;

public class ManagerLoadException extends ManagerException {
    public ManagerLoadException(String message) {
        super(message);
    }

    public ManagerLoadException(String message, IOException cause) {
        super(message, cause);
    }
}
