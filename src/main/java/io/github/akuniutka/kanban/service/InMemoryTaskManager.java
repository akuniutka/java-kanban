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
    protected final TreeSet<Task> prioritizedTasks;
    protected long lastUsedId;

    public InMemoryTaskManager(HistoryManager historyManager) {
        Objects.requireNonNull(historyManager, "cannot start: history manager is null");
        this.tasks = new HashMap<>();
        this.subtasks = new HashMap<>();
        this.epics = new HashMap<>();
        this.historyManager = historyManager;
        this.prioritizedTasks = new TreeSet<>(Comparator.comparing(Task::getStartTime));
        this.lastUsedId = -1L;
    }

    @Override
    public List<Task> getTasks() {
        return new ArrayList<>(tasks.values());
    }

    @Override
    public void removeTasks() {
        tasks.values().forEach(this::removeFromPrioritizedIfPresent);
        tasks.values().forEach(task -> historyManager.remove(task.getId()));
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
        requireIdNotExist(task.getId());
        requireDoesNotOverlapOtherTasks(task);
        final long id = generateId();
        task.setId(id);
        tasks.put(id, task);
        addToPrioritizedIfStartTimeNotNull(task);
        return id;
    }

    @Override
    public void updateTask(Task task) {
        Objects.requireNonNull(task, "cannot apply null update to task");
        final Long id = task.getId();
        final Task savedTask = requireTaskExists(id);
        requireDoesNotOverlapOtherTasks(task);
        removeFromPrioritizedIfPresent(savedTask);
        tasks.put(id, task);
        addToPrioritizedIfStartTimeNotNull(task);
    }

    @Override
    public void removeTask(long id) {
        final Task savedTask = requireTaskExists(id);
        tasks.remove(id);
        historyManager.remove(id);
        removeFromPrioritizedIfPresent(savedTask);
    }

    @Override
    public List<Epic> getEpics() {
        return new ArrayList<>(epics.values());
    }

    @Override
    public void removeEpics() {
        subtasks.values().forEach(this::removeFromPrioritizedIfPresent);
        subtasks.values().forEach(subtask -> historyManager.remove(subtask.getId()));
        subtasks.clear();
        epics.values().forEach(epic -> historyManager.remove(epic.getId()));
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
        requireIdNotExist(epic.getId());
        final long id = generateId();
        epic.setId(id);
        epic.setSubtasks(new ArrayList<>());
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
        epic.getSubtasks().stream()
                .peek(this::removeFromPrioritizedIfPresent)
                .map(Subtask::getId)
                .peek(historyManager::remove)
                .forEach(subtasks::remove);
        historyManager.remove(id);
    }

    @Override
    public List<Subtask> getSubtasks() {
        return new ArrayList<>(subtasks.values());
    }

    @Override
    public void removeSubtasks() {
        epics.values().forEach(epic -> epic.setSubtasks(new ArrayList<>()));
        subtasks.values().forEach(this::removeFromPrioritizedIfPresent);
        subtasks.values().forEach(subtask -> historyManager.remove(subtask.getId()));
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
        requireIdNotExist(subtask.getId());
        requireDoesNotOverlapOtherTasks(subtask);
        subtask.setId(generateId());
        saveSubtaskAndLinkToEpic(subtask);
        addToPrioritizedIfStartTimeNotNull(subtask);
        return subtask.getId();
    }

    @Override
    public void updateSubtask(Subtask subtask) {
        Objects.requireNonNull(subtask, "cannot apply null update to subtask");
        final Long id = subtask.getId();
        final Subtask savedSubtask = requireSubtaskExists(id);
        requireDoesNotOverlapOtherTasks(subtask);
        final Long epicId = savedSubtask.getEpicId();
        final Epic epic = epics.get(epicId);
        subtask.setEpicId(epicId);
        subtasks.put(id, subtask);
        removeFromPrioritizedIfPresent(savedSubtask);
        addToPrioritizedIfStartTimeNotNull(subtask);
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
        removeFromPrioritizedIfPresent(subtask);
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

    @Override
    public List<Task> getPrioritizedTasks() {
        return new ArrayList<>(prioritizedTasks);
    }

    protected void requireIdNotExist(Long id) {
        if (tasks.containsKey(id) || epics.containsKey(id) || subtasks.containsKey(id)) {
            throw new ManagerException("duplicate id=" + id);
        }
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

    protected void requireDoesNotOverlapOtherTasks(Task task) {
        Objects.requireNonNull(task, "cannot check time slot for null task");
        if (task.getDuration() == null && task.getStartTime() == null) {
            return;
        } else if (task.getDuration() == null || task.getStartTime() == null) {
            throw new ManagerException("duration and start time must be either both set or both null");
        }
        final Task taskBefore = prioritizedTasks.floor(task);
        if (taskBefore != null && !task.equals(taskBefore) && task.getStartTime().isBefore(taskBefore.getEndTime())) {
            throw new ManagerException("conflict with another task for time slot");
        }
        Task taskAfter = prioritizedTasks.higher(task);
        if (task.equals(taskAfter)) {
            taskAfter = prioritizedTasks.higher(taskAfter);
        }
        if (taskAfter != null && task.getEndTime().isAfter(taskAfter.getStartTime())) {
            throw new ManagerException("conflict with another task for time slot");
        }
    }

    protected void addToPrioritizedIfStartTimeNotNull(Task task) {
        Objects.requireNonNull(task, "cannot add null to list of prioritized tasks");
        if (task.getStartTime() != null) {
            prioritizedTasks.add(task);
        }
    }

    protected void removeFromPrioritizedIfPresent(Task task) {
        if (task != null && task.getStartTime() != null) {
            prioritizedTasks.remove(task);
        }
    }
}
