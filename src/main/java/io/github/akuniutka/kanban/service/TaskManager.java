package io.github.akuniutka.kanban.service;

import io.github.akuniutka.kanban.model.Epic;
import io.github.akuniutka.kanban.model.Subtask;
import io.github.akuniutka.kanban.model.Task;

import java.util.List;

public interface TaskManager {
    List<Task> getTasks();

    void removeTasks();

    Task getTask(long id);

    long addTask(Task task);

    void updateTask(Task task);

    void removeTask(long id);

    List<Epic> getEpics();

    void removeEpics();

    Epic getEpic(long id);

    long addEpic(Epic epic);

    void updateEpic(Epic epic);

    void removeEpic(long id);

    List<Subtask> getSubtasks();

    void removeSubtasks();

    Subtask getSubtask(long id);

    long addSubtask(Subtask subtask);

    void updateSubtask(Subtask subtask);

    void removeSubtask(long id);

    List<Subtask> getEpicSubtasks(long epicId);
}
