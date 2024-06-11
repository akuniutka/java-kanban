package io.github.akuniutka.kanban.service;

import io.github.akuniutka.kanban.model.Epic;
import io.github.akuniutka.kanban.model.Subtask;
import io.github.akuniutka.kanban.model.Task;

import java.util.List;
import java.util.Optional;

public interface TaskManager {
    List<Task> getTasks();

    void deleteTasks();

    Optional<Task> getTaskById(long id);

    long createTask(Task task);

    void updateTask(Task task);

    void deleteTask(long id);

    List<Epic> getEpics();

    void deleteEpics();

    Optional<Epic> getEpicById(long id);

    long createEpic(Epic epic);

    void updateEpic(Epic epic);

    void deleteEpic(long id);

    List<Subtask> getSubtasks();

    void deleteSubtasks();

    Optional<Subtask> getSubtaskById(long id);

    long createSubtask(Subtask subtask);

    void updateSubtask(Subtask subtask);

    void deleteSubtask(long id);

    List<Subtask> getEpicSubtasks(long epicId);

    List<Task> getHistory();

    List<Task> getPrioritizedTasks();
}
