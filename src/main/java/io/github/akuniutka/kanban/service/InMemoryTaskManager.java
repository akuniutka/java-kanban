package io.github.akuniutka.kanban.service;

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
        tasks.values().forEach(this::removeFromPrioritizedTasks);
        tasks.keySet().forEach(historyManager::remove);
        tasks.clear();
    }

    @Override
    public Optional<Task> getTaskById(long id) {
        final Optional<Task> task = Optional.ofNullable(tasks.get(id));
        task.ifPresent(historyManager::add);
        return task;
    }

    @Override
    public long createTask(Task task) {
        Objects.requireNonNull(task, "cannot create null task");
        task.setId(generateId());
        updateTask(task);
        return task.getId();
    }

    @Override
    public void updateTask(Task task) {
        Objects.requireNonNull(task, "cannot apply null update");
        validate(task);
        final Task savedTask = tasks.put(task.getId(), task);
        replaceInPrioritizedTasksIfAppropriate(savedTask, task);
    }

    @Override
    public void deleteTask(long id) {
        final Task savedTask = requireTaskExists(id);
        tasks.remove(id);
        historyManager.remove(id);
        removeFromPrioritizedTasks(savedTask);
    }

    @Override
    public List<Epic> getEpics() {
        return new ArrayList<>(epics.values());
    }

    @Override
    public void deleteEpics() {
        subtasks.values().forEach(this::removeFromPrioritizedTasks);
        subtasks.keySet().forEach(historyManager::remove);
        subtasks.clear();
        epics.keySet().forEach(historyManager::remove);
        epics.clear();
    }

    @Override
    public Optional<Epic> getEpicById(long id) {
        final Optional<Epic> epic = Optional.ofNullable(epics.get(id));
        epic.ifPresent(historyManager::add);
        return epic;
    }

    @Override
    public long createEpic(Epic epic) {
        Objects.requireNonNull(epic, "cannot create null epic");
        epic.setId(generateId());
        updateEpic(epic);
        return epic.getId();
    }

    @Override
    public void updateEpic(Epic epic) {
        Objects.requireNonNull(epic, "cannot apply null update");
        validate(epic);
        epics.put(epic.getId(), epic);
        updateEpic(epic.getId());
    }

    @Override
    public void deleteEpic(long id) {
        final Epic epic = requireEpicExists(id);
        epics.remove(id);
        epic.getSubtaskIds().stream()
                .peek(subtaskId -> removeFromPrioritizedTasks(subtasks.get(subtaskId)))
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
        subtasks.values().forEach(this::removeFromPrioritizedTasks);
        subtasks.keySet().forEach(historyManager::remove);
        subtasks.clear();
    }

    @Override
    public Optional<Subtask> getSubtaskById(long id) {
        final Optional<Subtask> subtask = Optional.ofNullable(subtasks.get(id));
        subtask.ifPresent(historyManager::add);
        return subtask;
    }

    @Override
    public long createSubtask(Subtask subtask) {
        Objects.requireNonNull(subtask, "cannot create null subtask");
        subtask.setId(generateId());
        updateSubtask(subtask);
        return subtask.getId();
    }

    @Override
    public void updateSubtask(Subtask subtask) {
        Objects.requireNonNull(subtask, "cannot apply null update");
        Mode mode = validate(subtask);
        final Subtask savedSubtask = subtasks.put(subtask.getId(), subtask);
        replaceInPrioritizedTasksIfAppropriate(savedSubtask, subtask);
        if (mode == Mode.CREATE) {
            final Epic epic = epics.get(subtask.getEpicId());
            epic.getSubtaskIds().add(subtask.getId());
        }
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
        removeFromPrioritizedTasks(subtask);
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

    protected long generateId() {
        return ++lastUsedId;
    }

    protected Mode validate(Task task) {
        Mode mode = validateId(task);
        validateSubtaskIds(task, mode);
        validateEpicId(task, mode);
        validateDurationAndStartTime(task);
        validateStatus(task);
        return mode;
    }

    protected Mode validateId(Task task) {
        if (task.getId() == null) {
            throw new ManagerValidationException("id cannot be null");
        }
        final TaskType type = getTaskTypeById(task.getId());
        if (type == null) {
            lastUsedId = Long.max(lastUsedId, task.getId());
        } else if (task.getType() != type) {
            throw new ManagerValidationException("wrong task type");
        }
        return type == null ? Mode.CREATE : Mode.UPDATE;
    }

    protected void validateSubtaskIds(Task task, Mode mode) {
        if (task.getType() == TaskType.EPIC) {
            final Epic epic = (Epic) task;
            if (mode == Mode.CREATE) {
                epic.setSubtaskIds(new ArrayList<>());
            } else {
                epic.setSubtaskIds(epics.get(epic.getId()).getSubtaskIds());
            }
        }
    }

    protected void validateEpicId(Task task, Mode mode) {
        if (task.getType() == TaskType.SUBTASK) {
            final Subtask subtask = (Subtask) task;
            if (mode == Mode.UPDATE) {
                subtask.setEpicId(subtasks.get(subtask.getId()).getEpicId());
            } else if (!epics.containsKey(subtask.getEpicId())) {
                throw new ManagerValidationException("wrong epic id");
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

    protected TaskType getTaskTypeById(long id) {
        Task task = tasks.get(id);
        if (task == null) {
            task = epics.get(id);
        }
        if (task == null) {
            task = subtasks.get(id);
        }
        return task == null ? null : task.getType();
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

    protected void replaceInPrioritizedTasksIfAppropriate(Task previousVersion, Task currentVersion) {
        removeFromPrioritizedTasks(previousVersion);
        addToPrioritizedTasksIfAppropriate(currentVersion);
    }

    protected void addToPrioritizedTasksIfAppropriate(Task task) {
        if (task != null && task.getStartTime() != null) {
            prioritizedTasks.add(task);
        }
    }

    protected void removeFromPrioritizedTasks(Task task) {
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
