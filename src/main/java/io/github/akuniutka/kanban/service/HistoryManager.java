package io.github.akuniutka.kanban.service;

import java.util.List;
import io.github.akuniutka.kanban.model.Task;

public interface HistoryManager {
    void add(Task task);

    List<Task> getHistory();
}
