package io.github.akuniutka.kanban.service;

import io.github.akuniutka.kanban.exception.ManagerException;
import io.github.akuniutka.kanban.exception.TaskNotFoundException;
import io.github.akuniutka.kanban.model.Epic;
import io.github.akuniutka.kanban.model.Subtask;
import io.github.akuniutka.kanban.model.Task;

import java.util.*;

public class InMemoryTaskManager implements TaskManager {
    protected final Map<Long, Task> tasks;
    protected final Map<Long, Subtask> subtasks;
    protected final Map<Long, Epic> epics;
    protected final HistoryManager historyManager;
    protected long lastUsedId;

    public InMemoryTaskManager(HistoryManager historyManager) {
        Objects.requireNonNull(historyManager, "cannot start: history manager is null");
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
        for (Task task : tasks.values()) {
            historyManager.remove(task.getId());
        }
        tasks.clear();
    }

    @Override
    public Task getTask(long id) {
        final Task task = requireTaskExists(id);
        historyManager.add(task);
        return task;
    }

    @Override
    public long addTask(Task task) {
        Objects.requireNonNull(task, "cannot add null to list of tasks");
        checkDurationAndStartTimeConsistency(task);
        final long id = generateId();
        task.setId(id);
        tasks.put(id, task);
        return id;
    }

    @Override
    public void updateTask(Task task) {
        Objects.requireNonNull(task, "cannot apply null update to task");
        final Long id = task.getId();
        requireTaskExists(id);
        checkDurationAndStartTimeConsistency(task);
        tasks.put(id, task);
    }

    @Override
    public void removeTask(long id) {
        requireTaskExists(id);
        tasks.remove(id);
        historyManager.remove(id);
    }

    @Override
    public List<Epic> getEpics() {
        return new ArrayList<>(epics.values());
    }

    @Override
    public void removeEpics() {
        for (Subtask subtask : subtasks.values()) {
            historyManager.remove(subtask.getId());
        }
        subtasks.clear();
        for (Epic epic : epics.values()) {
            historyManager.remove(epic.getId());
        }
        epics.clear();
    }

    @Override
    public Epic getEpic(long id) {
        final Epic epic = requireEpicExists(id);
        historyManager.add(epic);
        return epic;
    }

    @Override
    public long addEpic(Epic epic) {
        Objects.requireNonNull(epic, "cannot add null to list of epics");
        final long id = generateId();
        epic.setId(id);
        epic.getSubtasks().clear();
        epics.put(id, epic);
        return id;
    }

    @Override
    public void updateEpic(Epic epic) {
        Objects.requireNonNull(epic, "cannot apply null update to epic");
        final Long id = epic.getId();
        final Epic savedEpic = requireEpicExists(id);
        final List<Subtask> epicSubtasks = savedEpic.getSubtasks();
        epic.setSubtasks(epicSubtasks);
        epics.put(id, epic);
    }

    @Override
    public void removeEpic(long id) {
        final Epic epic = requireEpicExists(id);
        epics.remove(id);
        for (Subtask subtask : epic.getSubtasks()) {
            historyManager.remove(subtask.getId());
            subtasks.remove(subtask.getId());
        }
        historyManager.remove(id);
    }

    @Override
    public List<Subtask> getSubtasks() {
        return new ArrayList<>(subtasks.values());
    }

    @Override
    public void removeSubtasks() {
        for (Epic epic : epics.values()) {
            epic.getSubtasks().clear();
        }
        for (Subtask subtask : subtasks.values()) {
            historyManager.remove(subtask.getId());
        }
        subtasks.clear();
    }

    @Override
    public Subtask getSubtask(long id) {
        final Subtask subtask = requireSubtaskExists(id);
        historyManager.add(subtask);
        return subtask;
    }

    @Override
    public long addSubtask(Subtask subtask) {
        Objects.requireNonNull(subtask, "cannot add null to list of subtasks");
        checkDurationAndStartTimeConsistency(subtask);
        subtask.setId(generateId());
        saveSubtaskAndLinkToEpic(subtask);
        return subtask.getId();
    }

    @Override
    public void updateSubtask(Subtask subtask) {
        Objects.requireNonNull(subtask, "cannot apply null update to subtask");
        final Long id = subtask.getId();
        final Subtask savedSubtask = requireSubtaskExists(id);
        checkDurationAndStartTimeConsistency(subtask);
        final Long epicId = savedSubtask.getEpicId();
        final Epic epic = epics.get(epicId);
        subtask.setEpicId(epicId);
        subtasks.put(id, subtask);
        final int index = epic.getSubtasks().indexOf(subtask);
        epic.getSubtasks().set(index, subtask);
    }

    @Override
    public void removeSubtask(long id) {
        final Subtask subtask = requireSubtaskExists(id);
        subtasks.remove(id);
        final long epicId = subtask.getEpicId();
        final Epic epic = epics.get(epicId);
        epic.getSubtasks().remove(subtask);
        historyManager.remove(id);
    }

    @Override
    public List<Subtask> getEpicSubtasks(long epicId) {
        final Epic epic = requireEpicExists(epicId);
        return epic.getSubtasks();
    }

    @Override
    public List<Task> getHistory() {
        return historyManager.getHistory();
    }

    protected long generateId() {
        return ++lastUsedId;
    }

    protected void saveSubtaskAndLinkToEpic(Subtask subtask) {
        Objects.requireNonNull(subtask, "cannot add null to list of subtasks");
        final Epic epic = requireEpicExists(subtask.getEpicId());
        subtasks.put(subtask.getId(), subtask);
        epic.getSubtasks().add(subtask);
    }

    protected Task requireTaskExists(Long id) {
        final Task task = tasks.get(id);
        if (task == null) {
            throw new TaskNotFoundException("no task with id=" + id);
        }
        return task;
    }

    protected Epic requireEpicExists(Long id) {
        final Epic epic = epics.get(id);
        if (epic == null) {
            throw new TaskNotFoundException("no epic with id=" + id);
        }
        return epic;
    }

    protected Subtask requireSubtaskExists(Long id) {
        final Subtask subtask = subtasks.get(id);
        if (subtask == null) {
            throw new TaskNotFoundException("no subtask with id=" + id);
        }
        return subtask;
    }

    protected void checkDurationAndStartTimeConsistency(Task task) {
        if (Objects.isNull(task.getDuration()) != Objects.isNull(task.getStartTime())) {
            throw new ManagerException("duration and start time must be either both set or both null");
        }
    }
}
