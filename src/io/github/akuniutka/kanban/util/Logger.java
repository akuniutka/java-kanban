package io.github.akuniutka.kanban.util;

public class Logger {
    public void logInfo(String info) {
        System.out.println(info);
    }

    public void logError(String errorMessage) {
        System.out.println("ERROR: " + errorMessage);
    }
}
