package io.github.akuniutka.kanban.service;

import io.github.akuniutka.kanban.model.Epic;
import io.github.akuniutka.kanban.model.Subtask;
import io.github.akuniutka.kanban.model.Task;
import io.github.akuniutka.kanban.model.TaskStatus;

import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import java.util.HashMap;

public class TaskManager {
    private final Map<Long, Task> tasks;
    private final Map<Long, Subtask> subtasks;
    private final Map<Long, Epic> epics;
    private long lastUsedId;

    public TaskManager() {
        this.tasks = new HashMap<>();
        this.subtasks = new HashMap<>();
        this.epics = new HashMap<>();
        lastUsedId = 0L;
    }

    public List<Task> getTasks() {
        return new ArrayList<>(tasks.values());
    }

    public void removeTasks() {
        tasks.clear();
    }

    public Task getTask(long id) {
        return tasks.get(id);
    }

    public Task addTask(Task task) {
        if (task == null) {
            return null;
        }
        final long id = generateId();
        task.setId(id);
        tasks.put(id, task);
        return task;
    }

    public void updateTask(Task task) {
        if (task != null && tasks.containsKey(task.getId())) {
            final long id = task.getId();
            tasks.put(id, task);
        }
    }

    public void removeTask(long id) {
        tasks.remove(id);
    }

    public List<Epic> getEpics() {
        return new ArrayList<>(epics.values());
    }

    public void removeEpics() {
        subtasks.clear();
        epics.clear();
    }

    public Epic getEpic(long id) {
        return epics.get(id);
    }

    public Epic addEpic(Epic epic) {
        if (epic == null) {
            return null;
        }
        final long id = generateId();
        epic.setId(id);
        updateEpicStatus(epic);
        epics.put(id, epic);
        return epic;
    }

    public void updateEpic(Epic epic) {
        if (epic == null) {
            return;
        }
        final long id = epic.getId();;
        final String title = epic.getTitle();
        final String description = epic.getDescription();
        final Epic savedEpic = epics.get(id);
        if (savedEpic == null) {
            return;
        }
        savedEpic.setTitle(title);
        savedEpic.setDescription(description);
    }

    public void removeEpic(long id) {
        final Epic epic = epics.remove(id);
        if (epic != null) {
            for (long subtaskId : epic.getSubtaskIds()) {
                subtasks.remove(subtaskId);
            }
        }
    }

    public List<Subtask> getSubtasks() {
        return new ArrayList<>(subtasks.values());
    }

    public void removeSubtasks() {
        for (Epic epic : epics.values()) {
            final List<Long> subtaskIds = epic.getSubtaskIds();
            for (long subtaskId : subtaskIds) {
                epic.removeSubtaskId(subtaskId);
            }
            updateEpicStatus(epic);
        }
        subtasks.clear();
    }

    public Subtask getSubtask(long id) {
        return subtasks.get(id);
    }

    public Subtask addSubtask(Subtask subtask) {
        if (subtask == null) {
            return null;
        }
        final long epicId = subtask.getEpicId();
        final Epic epic = epics.get(epicId);
        if (epic == null) {
            return null;
        }
        final long id = generateId();
        subtask.setId(id);
        epic.addSubtaskId(id);
        subtasks.put(id, subtask);
        updateEpicStatus(epic);
        return subtask;
    }

    public void updateSubtask(Subtask subtask) {
        if (subtask == null) {
            return;
        }
        final long id = subtask.getId();
        final long epicId = subtask.getEpicId();
        final Subtask storedSubtask = subtasks.get(id);
        if (storedSubtask == null) {
            return;
        }
        final Epic epic = epics.get(epicId);
        if (epic == null) {
            return;
        }
        subtasks.put(id, subtask);
        updateEpicStatus(epic);
    }

    public void removeSubtask(long id) {
        final Subtask subtask = subtasks.remove(id);
        if (subtask != null) {
            final long epicId = subtask.getEpicId();
            final Epic epic = epics.get(epicId);
            epic.removeSubtaskId(id);
            updateEpicStatus(epic);
        }
    }

    public List<Subtask> getEpicSubtasks(long epicId) {
        final Epic epic = epics.get(epicId);
        if (epic == null) {
            return null;
        }
        final List<Long> subtaskIds = epic.getSubtaskIds();
        final List<Subtask> subtaskList = new ArrayList<>();
        for (long subtaskId : subtaskIds) {
            subtaskList.add(subtasks.get(subtaskId));
        }
        return subtaskList;
    }

    private long generateId() {
        return ++lastUsedId;
    }

    private void updateEpicStatus(Epic epic) {
        boolean areAllSubtasksNew = true;
        boolean areAllSubtasksDone = true;
        for (long subtaskId : epic.getSubtaskIds()) {
            final Subtask subtask = subtasks.get(subtaskId);
            if (subtask.getStatus() != TaskStatus.NEW) {
                areAllSubtasksNew = false;
            }
            if (subtask.getStatus() != TaskStatus.DONE) {
                areAllSubtasksDone = false;
            }
        }
        if (areAllSubtasksNew) {
            epic.setStatus(TaskStatus.NEW);
        } else if (areAllSubtasksDone) {
            epic.setStatus(TaskStatus.DONE);
        } else {
            epic.setStatus(TaskStatus.IN_PROGRESS);
        }
    }
}
