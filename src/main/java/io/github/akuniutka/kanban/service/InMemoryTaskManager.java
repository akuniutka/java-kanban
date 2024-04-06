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
    private static final int OK = 0;
    private static final int WRONG_ARGUMENT = -1;
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
            historyManager.add(task);
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
    public int updateTask(Task task) {
        if (task == null || !tasks.containsKey(task.getId())) {
            return WRONG_ARGUMENT;
        }
        final long id = task.getId();
        tasks.put(id, task);
        return OK;
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
            historyManager.add(epic);
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
        epic.getSubtaskIds().clear();
        updateEpicStatus(epic);
        epics.put(id, epic);
        return id;
    }

    @Override
    public int updateEpic(Epic epic) {
        if (epic == null) {
            return WRONG_ARGUMENT;
        }
        final Long id = epic.getId();
        final Epic savedEpic = epics.get(id);
        if (savedEpic == null) {
            return WRONG_ARGUMENT;
        }
        final List<Long> subtaskIds = savedEpic.getSubtaskIds();
        final TaskStatus status = savedEpic.getStatus();
        epic.setSubtaskIds(subtaskIds);
        epic.setStatus(status);
        epics.put(id, epic);
        return OK;
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
            epic.getSubtaskIds().clear();
            updateEpicStatus(epic);
        }
        subtasks.clear();
    }

    @Override
    public Subtask getSubtask(long id) {
        Subtask subtask = subtasks.get(id);
        if (subtask != null) {
            historyManager.add(subtask);
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
        subtasks.put(id, subtask);
        epic.getSubtaskIds().add(id);
        updateEpicStatus(epic);
        return id;
    }

    @Override
    public int updateSubtask(Subtask subtask) {
        if (subtask == null) {
            return WRONG_ARGUMENT;
        }
        final Long id = subtask.getId();
        final Subtask savedSubtask = subtasks.get(id);
        if (savedSubtask == null) {
            return WRONG_ARGUMENT;
        }
        final Long epicId = savedSubtask.getEpicId();
        final Epic epic = epics.get(epicId);
        subtask.setEpicId(epicId);
        subtasks.put(id, subtask);
        updateEpicStatus(epic);
        return OK;
    }

    @Override
    public void removeSubtask(long id) {
        final Subtask subtask = subtasks.remove(id);
        if (subtask != null) {
            final long epicId = subtask.getEpicId();
            final Epic epic = epics.get(epicId);
            epic.getSubtaskIds().remove(id);
            updateEpicStatus(epic);
        }
    }

    @Override
    public List<Subtask> getEpicSubtasks(long epicId) {
        final List<Subtask> subtaskList = new ArrayList<>();
        final Epic epic = epics.get(epicId);
        if (epic != null) {
            for (long subtaskId : epic.getSubtaskIds()) {
                subtaskList.add(subtasks.get(subtaskId));
            }
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
}
