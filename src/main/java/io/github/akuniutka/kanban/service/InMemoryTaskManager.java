package io.github.akuniutka.kanban.service;

import io.github.akuniutka.kanban.model.Epic;
import io.github.akuniutka.kanban.model.Subtask;
import io.github.akuniutka.kanban.model.Task;
import io.github.akuniutka.kanban.model.TaskStatus;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class InMemoryTaskManager implements TaskManager {
    private static final long WRONG_ARGUMENT = -1L;
    private final Map<Long, Task> tasks;
    private final Map<Long, Subtask> subtasks;
    private final Map<Long, Epic> epics;
    private final HistoryManager historyManager;
    private long lastUsedId;

    public InMemoryTaskManager(HistoryManager historyManager) {
        this.tasks = new HashMap<>();
        this.subtasks = new HashMap<>();
        this.epics = new HashMap<>();
        this.historyManager = historyManager;
        this.lastUsedId = -1L;
    }

    @Override
    public List<Task> getTasks() {
        return new ArrayList<>(tasks.values());
    }

    @Override
    public void removeTasks() {
        tasks.clear();
    }

    @Override
    public Task getTask(long id) {
        Task task = tasks.get(id);
        if (task != null) {
            historyManager.add(copyOf(task));
        }
        return task;
    }

    @Override
    public long addTask(Task task) {
        if (task == null) {
            return WRONG_ARGUMENT;
        }
        final long id = generateId();
        task.setId(id);
        tasks.put(id, task);
        return id;
    }

    @Override
    public void updateTask(Task task) {
        if (task != null && tasks.containsKey(task.getId())) {
            final long id = task.getId();
            tasks.put(id, task);
        }
    }

    @Override
    public void removeTask(long id) {
        tasks.remove(id);
    }

    @Override
    public List<Epic> getEpics() {
        return new ArrayList<>(epics.values());
    }

    @Override
    public void removeEpics() {
        subtasks.clear();
        epics.clear();
    }

    @Override
    public Epic getEpic(long id) {
        Epic epic = epics.get(id);
        if (epic != null) {
            historyManager.add(copyOf(epic));
        }
        return epic;
    }

    @Override
    public long addEpic(Epic epic) {
        if (epic == null) {
            return WRONG_ARGUMENT;
        }
        final long id = generateId();
        epic.setId(id);
        updateEpicStatus(epic);
        epics.put(id, epic);
        return id;
    }

    @Override
    public void updateEpic(Epic epic) {
        if (epic == null) {
            return;
        }
        final Long id = epic.getId();
        final String title = epic.getTitle();
        final String description = epic.getDescription();
        final Epic savedEpic = epics.get(id);
        if (savedEpic == null) {
            return;
        }
        savedEpic.setTitle(title);
        savedEpic.setDescription(description);
    }

    @Override
    public void removeEpic(long id) {
        final Epic epic = epics.remove(id);
        if (epic != null) {
            for (long subtaskId : epic.getSubtaskIds()) {
                subtasks.remove(subtaskId);
            }
        }
    }

    @Override
    public List<Subtask> getSubtasks() {
        return new ArrayList<>(subtasks.values());
    }

    @Override
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

    @Override
    public Subtask getSubtask(long id) {
        Subtask subtask = subtasks.get(id);
        if (subtask != null) {
            historyManager.add(copyOf(subtask));
        }
        return subtask;
    }

    @Override
    public long addSubtask(Subtask subtask) {
        if (subtask == null) {
            return WRONG_ARGUMENT;
        }
        final Long epicId = subtask.getEpicId();
        final Epic epic = epics.get(epicId);
        if (epic == null) {
            return WRONG_ARGUMENT;
        }
        final long id = generateId();
        subtask.setId(id);
        epic.addSubtaskId(id);
        subtasks.put(id, subtask);
        updateEpicStatus(epic);
        return id;
    }

    @Override
    public void updateSubtask(Subtask subtask) {
        if (subtask == null) {
            return;
        }
        final Long id = subtask.getId();
        final Long epicId = subtask.getEpicId();
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

    @Override
    public void removeSubtask(long id) {
        final Subtask subtask = subtasks.remove(id);
        if (subtask != null) {
            final long epicId = subtask.getEpicId();
            final Epic epic = epics.get(epicId);
            epic.removeSubtaskId(id);
            updateEpicStatus(epic);
        }
    }

    @Override
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

    @Override
    public List<Task> getHistory() {
        return historyManager.getHistory();
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

    private Task copyOf(Task task) {
        Task copy = new Task();
        copy.setId(task.getId());
        copy.setTitle(task.getTitle());
        copy.setDescription(task.getDescription());
        copy.setStatus(task.getStatus());
        return copy;
    }

    private Epic copyOf(Epic epic) {
        Epic copy = new Epic();
        copy.setId(epic.getId());
        copy.setTitle(epic.getTitle());
        copy.setDescription(epic.getDescription());
        for (Long subtaskId : epic.getSubtaskIds()) {
            copy.addSubtaskId(subtaskId);
        }
        copy.setStatus(epic.getStatus());
        return copy;
    }

    private Subtask copyOf(Subtask subtask) {
        Subtask copy = new Subtask();
        copy.setId(subtask.getId());
        copy.setEpicId(subtask.getEpicId());
        copy.setTitle(subtask.getTitle());
        copy.setDescription(subtask.getDescription());
        copy.setStatus(subtask.getStatus());
        return copy;
    }
}
