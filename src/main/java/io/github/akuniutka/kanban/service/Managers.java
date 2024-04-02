package io.github.akuniutka.kanban.service;

public class Managers {
    private Managers() {}

    public static TaskManager getDefault() {
        return getInMemoryTaskManager(getDefaultHistory());
    }

    public static HistoryManager getDefaultHistory() {
        return getInMemoryHistoryManager();
    }

    public static TaskManager getInMemoryTaskManager(HistoryManager historyManager) {
        return new InMemoryTaskManager(historyManager);
    }

    public static HistoryManager getInMemoryHistoryManager() {
        return new InMemoryHistoryManager();
    }
}
