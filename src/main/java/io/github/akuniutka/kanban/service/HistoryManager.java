package io.github.akuniutka.kanban.service;

import io.github.akuniutka.kanban.model.Task;

import java.util.List;

public interface HistoryManager {
    void add(Task task);

    void remove(long id);

    List<Task> getHistory();
}
