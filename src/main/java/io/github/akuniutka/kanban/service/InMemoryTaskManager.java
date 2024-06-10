package io.github.akuniutka.kanban.service;

import io.github.akuniutka.kanban.exception.DuplicateIdException;
import io.github.akuniutka.kanban.exception.ManagerValidationException;
import io.github.akuniutka.kanban.exception.TaskNotFoundException;
import io.github.akuniutka.kanban.model.*;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

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
    public void deleteTasks() {
        tasks.values().forEach(this::removeFromPrioritizedIfPresent);
        tasks.keySet().forEach(historyManager::remove);
        tasks.clear();
    }

    @Override
    public Task getTaskById(long id) {
        final Task task = requireTaskExists(id);
        historyManager.add(task);
        return task;
    }

    @Override
    public long createTask(Task task) {
        validate(task, Mode.CREATE);
        tasks.put(task.getId(), task);
        addToPrioritizedIfStartTimeNotNull(task);
        return task.getId();
    }

    @Override
    public void updateTask(Task task) {
        validate(task, Mode.UPDATE);
        final Task savedTask = tasks.get(task.getId());
        removeFromPrioritizedIfPresent(savedTask);
        tasks.put(task.getId(), task);
        addToPrioritizedIfStartTimeNotNull(task);
    }

    @Override
    public void deleteTask(long id) {
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
    public void deleteEpics() {
        subtasks.values().forEach(this::removeFromPrioritizedIfPresent);
        subtasks.keySet().forEach(historyManager::remove);
        subtasks.clear();
        epics.keySet().forEach(historyManager::remove);
        epics.clear();
    }

    @Override
    public Epic getEpicById(long id) {
        final Epic epic = requireEpicExists(id);
        historyManager.add(epic);
        return epic;
    }

    @Override
    public long createEpic(Epic epic) {
        validate(epic, Mode.CREATE);
        final long epicId = epic.getId();
        epics.put(epicId, epic);
        updateEpic(epicId);
        return epicId;
    }

    @Override
    public void updateEpic(Epic epic) {
        validate(epic, Mode.UPDATE);
        epics.put(epic.getId(), epic);
        updateEpic(epic.getId());
    }

    @Override
    public void deleteEpic(long id) {
        final Epic epic = requireEpicExists(id);
        epics.remove(id);
        epic.getSubtaskIds().stream()
                .peek(subtaskId -> removeFromPrioritizedIfPresent(subtasks.get(subtaskId)))
                .peek(historyManager::remove)
                .forEach(subtasks::remove);
        historyManager.remove(id);
    }

    @Override
    public List<Subtask> getSubtasks() {
        return new ArrayList<>(subtasks.values());
    }

    @Override
    public void deleteSubtasks() {
        epics.values().forEach(epic -> epic.setSubtaskIds(new ArrayList<>()));
        epics.keySet().forEach(this::updateEpic);
        subtasks.values().forEach(this::removeFromPrioritizedIfPresent);
        subtasks.keySet().forEach(historyManager::remove);
        subtasks.clear();
    }

    @Override
    public Subtask getSubtaskById(long id) {
        final Subtask subtask = requireSubtaskExists(id);
        historyManager.add(subtask);
        return subtask;
    }

    @Override
    public long createSubtask(Subtask subtask) {
        validate(subtask, Mode.CREATE);
        final long subtaskId = subtask.getId();
        final long epicId = subtask.getEpicId();
        final Epic epic = epics.get(epicId);
        subtasks.put(subtaskId, subtask);
        epic.getSubtaskIds().add(subtaskId);
        updateEpic(epicId);
        addToPrioritizedIfStartTimeNotNull(subtask);
        return subtaskId;
    }

    @Override
    public void updateSubtask(Subtask subtask) {
        validate(subtask, Mode.UPDATE);
        final Subtask savedSubtask = subtasks.get(subtask.getId());
        removeFromPrioritizedIfPresent(savedSubtask);
        subtasks.put(subtask.getId(), subtask);
        addToPrioritizedIfStartTimeNotNull(subtask);
        updateEpic(subtask.getEpicId());
    }

    @Override
    public void deleteSubtask(long id) {
        final Subtask subtask = requireSubtaskExists(id);
        subtasks.remove(id);
        final long epicId = subtask.getEpicId();
        final Epic epic = epics.get(epicId);
        epic.getSubtaskIds().remove(id);
        updateEpic(epicId);
        historyManager.remove(id);
        removeFromPrioritizedIfPresent(subtask);
    }

    @Override
    public List<Subtask> getEpicSubtasks(long epicId) {
        final Epic epic = requireEpicExists(epicId);
        return epic.getSubtaskIds().stream()
                .map(subtasks::get)
                .toList();
    }

    @Override
    public List<Task> getHistory() {
        return historyManager.getHistory();
    }

    @Override
    public List<Task> getPrioritizedTasks() {
        return new ArrayList<>(prioritizedTasks);
    }

    protected void validate(Task task, Mode mode) {
        Objects.requireNonNull(task, mode == Mode.CREATE ? "cannot create from null" : "cannot apply null update");
        validateId(task, mode);
        validateSubtaskIds(task, mode);
        validateEpicId(task, mode);
        validateDurationAndStartTime(task);
        validateStatus(task);
    }

    protected void validateId(Task task, Mode mode) {
        if (mode == Mode.CREATE) {
            if (task.getId() == null) {
                task.setId(++lastUsedId);
            } else {
                requireIdNotExist(task.getId());
                lastUsedId = Long.max(lastUsedId, task.getId());
            }
        } else if (task.getType() == TaskType.TASK && !tasks.containsKey(task.getId())) {
            throw new TaskNotFoundException("no task with id=" + task.getId());
        } else if (task.getType() == TaskType.EPIC && !epics.containsKey(task.getId())) {
            throw new TaskNotFoundException("no epic with id=" + task.getId());
        } else if (task.getType() == TaskType.SUBTASK && !subtasks.containsKey(task.getId())) {
            throw new TaskNotFoundException("no subtask with id=" + task.getId());
        }
    }

    protected void validateSubtaskIds(Task task, Mode mode) {
        if (task.getType() == TaskType.EPIC) {
            Epic epic = (Epic) task;
            if (mode == Mode.CREATE) {
                epic.setSubtaskIds(new ArrayList<>());
            } else {
                epic.setSubtaskIds(epics.get(epic.getId()).getSubtaskIds());
            }
        }
    }

    protected void validateEpicId(Task task, Mode mode) {
        if (task.getType() == TaskType.SUBTASK) {
            Subtask subtask = (Subtask) task;
            if (mode == Mode.UPDATE) {
                subtask.setEpicId(subtasks.get(subtask.getId()).getEpicId());
            } else if (!epics.containsKey(subtask.getEpicId())) {
                throw new TaskNotFoundException("no epic with id=" + subtask.getEpicId());
            }
        }
    }

    protected void validateDurationAndStartTime(Task task) {
        if (task.getType() == TaskType.EPIC || (task.getDuration() == null && task.getStartTime() == null)) {
            return;
        }
        if (task.getDuration() == null || task.getStartTime() == null) {
            throw new ManagerValidationException("duration and start time must be either both set or both null");
        }
        task.setDuration(task.getDuration().truncatedTo(ChronoUnit.MINUTES));
        if (!task.getDuration().isPositive()) {
            throw new ManagerValidationException("duration cannot be negative or zero");
        }
        task.setStartTime(task.getStartTime().truncatedTo(ChronoUnit.MINUTES));
        requireDoesNotOverlapOtherTasks(task);
    }

    protected void validateStatus(Task task) {
        if (task.getType() != TaskType.EPIC && task.getStatus() == null) {
            throw new ManagerValidationException("status cannot be null");
        }
    }

    protected void requireIdNotExist(Long id) {
        if (tasks.containsKey(id) || epics.containsKey(id) || subtasks.containsKey(id)) {
            throw new DuplicateIdException("duplicate id=" + id);
        }
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
        final Task taskBefore = prioritizedTasks.floor(task);
        if (taskBefore != null && !task.equals(taskBefore) && task.getStartTime().isBefore(taskBefore.getEndTime())) {
            throw new ManagerValidationException("conflict with another task for time slot");
        }
        Task taskAfter = prioritizedTasks.higher(task);
        if (task.equals(taskAfter)) {
            taskAfter = prioritizedTasks.higher(taskAfter);
        }
        if (taskAfter != null && task.getEndTime().isAfter(taskAfter.getStartTime())) {
            throw new ManagerValidationException("conflict with another task for time slot");
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

    protected void updateEpic(long epicId) {
        updateEpicDuration(epicId);
        updateEpicStartTime(epicId);
        updateEpicEndTime(epicId);
        updateEpicStatus(epicId);
    }

    protected void updateEpicDuration(long epicId) {
        Epic epic = epics.get(epicId);
        Duration duration = epic.getSubtaskIds().stream()
                .map(subtasks::get)
                .map(Subtask::getDuration)
                .filter(Objects::nonNull)
                .reduce(Duration::plus)
                .orElse(null);
        epic.setDuration(duration);
    }

    protected void updateEpicStartTime(long epicId) {
        Epic epic = epics.get(epicId);
        LocalDateTime startTime = epic.getSubtaskIds().stream()
                .map(subtasks::get)
                .map(Subtask::getStartTime)
                .filter(Objects::nonNull)
                .min(Comparator.naturalOrder())
                .orElse(null);
        epic.setStartTime(startTime);
    }

    protected void updateEpicEndTime(long epicId) {
        Epic epic = epics.get(epicId);
        LocalDateTime endTime = epic.getSubtaskIds().stream()
                .map(subtasks::get)
                .map(Subtask::getEndTime)
                .filter(Objects::nonNull)
                .max(Comparator.naturalOrder())
                .orElse(null);
        epic.setEndTime(endTime);
    }

    protected void updateEpicStatus(long epicId) {
        Epic epic = epics.get(epicId);
        Set<TaskStatus> statuses = epic.getSubtaskIds().stream()
                .map(subtasks::get)
                .map(Subtask::getStatus)
                .collect(Collectors.toCollection(HashSet::new));
        final TaskStatus status = switch (statuses.size()) {
            case 0 -> TaskStatus.NEW;
            case 1 -> statuses.iterator().next();
            default -> TaskStatus.IN_PROGRESS;
        };
        epic.setStatus(status);
    }

    protected enum Mode {
        CREATE,
        UPDATE
    }
}
