package io.github.akuniutka.kanban.service;

import io.github.akuniutka.kanban.exception.ManagerNoSuchEpicException;
import io.github.akuniutka.kanban.exception.ManagerNoSuchSubtaskException;
import io.github.akuniutka.kanban.exception.ManagerNoSuchTaskException;
import io.github.akuniutka.kanban.model.Epic;
import io.github.akuniutka.kanban.model.Subtask;
import io.github.akuniutka.kanban.model.Task;
import io.github.akuniutka.kanban.model.TaskStatus;

import java.util.*;

public class InMemoryTaskManager implements TaskManager {
    protected final Map<Long, Task> tasks;
    protected final Map<Long, Subtask> subtasks;
    protected final Map<Long, Epic> epics;
    protected final HistoryManager historyManager;
    protected long lastUsedId;

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
        epic.getSubtaskIds().clear();
        updateEpicStatus(epic);
        epics.put(id, epic);
        return id;
    }

    @Override
    public void updateEpic(Epic epic) {
        Objects.requireNonNull(epic, "cannot apply null update to epic");
        final Long id = epic.getId();
        final Epic savedEpic = requireEpicExists(id);
        final List<Long> subtaskIds = savedEpic.getSubtaskIds();
        final TaskStatus status = savedEpic.getStatus();
        epic.setSubtaskIds(subtaskIds);
        epic.setStatus(status);
        epics.put(id, epic);
    }

    @Override
    public void removeEpic(long id) {
        final Epic epic = requireEpicExists(id);
        epics.remove(id);
        for (long subtaskId : epic.getSubtaskIds()) {
            historyManager.remove(subtaskId);
            subtasks.remove(subtaskId);
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
            epic.getSubtaskIds().clear();
            updateEpicStatus(epic);
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
        subtask.setId(generateId());
        saveSubtaskAndLinkToEpic(subtask);
        return subtask.getId();
    }

    @Override
    public void updateSubtask(Subtask subtask) {
        Objects.requireNonNull(subtask, "cannot apply null update to subtask");
        final Long id = subtask.getId();
        final Subtask savedSubtask = requireSubtaskExists(id);
        final Long epicId = savedSubtask.getEpicId();
        final Epic epic = epics.get(epicId);
        subtask.setEpicId(epicId);
        subtasks.put(id, subtask);
        updateEpicStatus(epic);
    }

    @Override
    public void removeSubtask(long id) {
        final Subtask subtask = requireSubtaskExists(id);
        subtasks.remove(id);
        final long epicId = subtask.getEpicId();
        final Epic epic = epics.get(epicId);
        epic.getSubtaskIds().remove(id);
        updateEpicStatus(epic);
        historyManager.remove(id);
    }

    @Override
    public List<Subtask> getEpicSubtasks(long epicId) {
        final Epic epic = requireEpicExists(epicId);
        final List<Subtask> subtaskList = new ArrayList<>();
        for (long subtaskId : epic.getSubtaskIds()) {
            subtaskList.add(subtasks.get(subtaskId));
        }
        return subtaskList;
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
        epic.getSubtaskIds().add(subtask.getId());
        updateEpicStatus(epic);
    }

    protected void updateEpicStatus(Epic epic) {
        Objects.requireNonNull(epic, "cannot update status of null epic");
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

    protected Task requireTaskExists(Long id) {
        final Task task = tasks.get(id);
        if (task == null) {
            throw new ManagerNoSuchTaskException("no task with id=" + id);
        }
        return task;
    }

    protected Epic requireEpicExists(Long id) {
        final Epic epic = epics.get(id);
        if (epic == null) {
            throw new ManagerNoSuchEpicException("no epic with id=" + id);
        }
        return epic;
    }

    protected Subtask requireSubtaskExists(Long id) {
        final Subtask subtask = subtasks.get(id);
        if (subtask == null) {
            throw new ManagerNoSuchSubtaskException("no subtask with id=" + id);
        }
        return subtask;
    }
}
