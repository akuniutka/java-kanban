package io.github.akuniutka.kanban.service;

import io.github.akuniutka.kanban.model.Epic;
import io.github.akuniutka.kanban.model.Subtask;
import io.github.akuniutka.kanban.model.Task;
import io.github.akuniutka.kanban.model.TaskStatus;
import io.github.akuniutka.kanban.util.Logger;

import java.util.ArrayList;
import java.util.HashMap;

public class TaskManager {
    private static final Logger LOGGER = new Logger();
    private final HashMap<Long, Task> tasks;
    private final HashMap<Long, Subtask> subtasks;
    private final HashMap<Long, Epic> epics;
    private long lastUsedId;

    public TaskManager() {
        this.tasks = new HashMap<>();
        this.subtasks = new HashMap<>();
        this.epics = new HashMap<>();
        lastUsedId = 0L;
    }

    public ArrayList<Task> getTasks() {
        LOGGER.logInfo("Retrieving all tasks");
        ArrayList<Task> taskList = new ArrayList<>(tasks.values());
        LOGGER.logInfo(taskList.size() + " task(s) retrieved");
        return taskList;
    }

    public void removeTasks() {
        LOGGER.logInfo("Removing all tasks");
        LOGGER.logInfo(tasks.size() + " task(s) removed");
        tasks.clear();
    }

    public Task getTask(long id) {
        LOGGER.logInfo("Retrieving task by id=" + id);
        Task task = tasks.get(id);
        if (task == null) {
            LOGGER.logError("Task not found!");
        } else {
            LOGGER.logInfo(task.toString());
        }
        return task;
    }

    public Task addTask(Task task) {
        LOGGER.logInfo("Creating new task: " + task);
        if (task == null) {
            LOGGER.logError("Cannot create null task!");
            return null;
        }
        long taskId = generateId();
        task.setId(taskId);
        tasks.put(taskId, task);
        LOGGER.logInfo("1 task(s) created");
        return task;
    }

    public Task updateTask(Task task) {
        LOGGER.logInfo("Updating task: " + task);
        if (task == null) {
            LOGGER.logError("Cannot update null task!");
            return null;
        }
        if (!tasks.containsKey(task.getId())) {
            LOGGER.logError("Task not found!");
            return null;
        }
        tasks.put(task.getId(), task);
        LOGGER.logInfo("1 task(s) updated");
        return task;
    }

    public Task removeTask(long id) {
        LOGGER.logInfo("Removing task by id=" + id);
        Task task = tasks.remove(id);
        if (task == null) {
            LOGGER.logError("Task not found!");
        } else {
            LOGGER.logInfo(task.toString());
        }
        return task;
    }

    public ArrayList<Epic> getEpics() {
        LOGGER.logInfo("Retrieving all epics");
        ArrayList<Epic> epicList = new ArrayList<>(epics.values());
        LOGGER.logInfo(epicList.size() + " epic(s) retrieved");
        return epicList;
    }

    public void removeEpics() {
        LOGGER.logInfo("Removing all epics");
        LOGGER.logInfo(epics.size() + " epic(s) (" + subtasks.size() + " subtask(s)) removed");
        subtasks.clear();
        epics.clear();
    }

    public Epic getEpic(long id) {
        LOGGER.logInfo("Retrieving epic by id=" + id);
        Epic epic = epics.get(id);
        if (epic == null) {
            LOGGER.logError("Epic not found!");
        } else {
            LOGGER.logInfo(epic.toString());
        }
        return epic;
    }

    public Epic addEpic(Epic epic) {
        LOGGER.logInfo("Creating new epic: " + epic);
        if (epic == null) {
            LOGGER.logError("Cannot create null epic!");
            return null;
        }
        long epicId = generateId();
        epic.setId(epicId);
        updateEpicStatus(epic);
        epics.put(epicId, epic);
        LOGGER.logInfo("1 epic(s) created");
        return epic;
    }

    public Epic updateEpic(Epic epic) {
        LOGGER.logInfo("Updating epic: " + epic);
        if (epic == null) {
            LOGGER.logError("Cannot update null epic!");
            return null;
        }
        Epic storedEpic = epics.get(epic.getId());
        if (storedEpic == null) {
            LOGGER.logError("Epic not found!");
            return null;
        }
        storedEpic.setTitle(epic.getTitle());
        storedEpic.setDescription(epic.getDescription());
        LOGGER.logInfo("1 epic(s) updated");
        return storedEpic;
    }

    public Epic removeEpic(long id) {
        LOGGER.logInfo("Removing epic by id=" + id);
        Epic epic = epics.remove(id);
        if (epic == null) {
            LOGGER.logError("Epic not found!");
        } else {
            LOGGER.logInfo(epic.toString());
            for (long subtaskId : epic.getSubtaskIds()) {
                subtasks.remove(subtaskId);
            }
        }
        return epic;
    }

    public ArrayList<Subtask> getSubtasks() {
        LOGGER.logInfo("Retrieving all subtasks");
        ArrayList<Subtask> subtaskList = new ArrayList<>(subtasks.values());
        LOGGER.logInfo(subtaskList.size() + " subtask{s} retrieved");
        return subtaskList;
    }

    public void removeSubtasks() {
        LOGGER.logInfo("Removing all subtasks");
        for (Epic epic : epics.values()) {
            ArrayList<Long> subtaskIds = epic.getSubtaskIds();
            for (long subtaskId : subtaskIds) {
                epic.removeSubtaskId(subtaskId);
            }
            updateEpicStatus(epic);
        }
        LOGGER.logInfo(subtasks.size() + " subtask(s) removed");
        subtasks.clear();
    }

    public Subtask getSubtask(long id) {
        LOGGER.logInfo("Retrieving subtask by id=" + id);
        Subtask subtask = subtasks.get(id);
        if (subtask == null) {
            LOGGER.logError("Subtask not found!");
        } else {
            LOGGER.logInfo(subtask.toString());
        }
        return subtask;
    }

    public Subtask addSubtask(Subtask subtask) {
        LOGGER.logInfo("Creating new subtask: " + subtask);
        if (subtask == null) {
            LOGGER.logError("Cannot create null subtask!");
            return null;
        }
        Epic epic = epics.get(subtask.getEpicId());
        if (epic == null) {
            LOGGER.logError("Cannot create subtask of unknown epic!");
            return null;
        }
        long subtaskId = generateId();
        subtask.setId(subtaskId);
        epic.addSubtaskId(subtaskId);
        subtasks.put(subtaskId, subtask);
        updateEpicStatus(epic);
        LOGGER.logInfo("1 subtask(s) created");
        return subtask;
    }

    public Subtask updateSubtask(Subtask subtask) {
        LOGGER.logInfo("Updating subtask: " + subtask);
        if (subtask == null) {
            LOGGER.logError("Cannot update null subtask!");
            return null;
        }
        Subtask storedSubtask = subtasks.get(subtask.getId());
        if (storedSubtask == null) {
            LOGGER.logError("Subtask not found!");
            return null;
        }
        if (!epics.containsKey(subtask.getEpicId())) {
            LOGGER.logError("Cannot update subtask of unknown epic!");
            return null;
        }
        if (storedSubtask.getEpicId() != subtask.getEpicId()) {
            reassignSubtaskToNewEpic(storedSubtask, subtask.getEpicId());
        }
        subtasks.put(subtask.getId(), subtask);
        updateEpicStatus(epics.get(subtask.getEpicId()));
        LOGGER.logInfo("1 subtask(s) updated");
        return subtask;
    }

    public Subtask removeSubtask(long id) {
        LOGGER.logInfo("Removing subtask by id=" + id);
        Subtask subtask = subtasks.remove(id);
        if (subtask == null) {
            LOGGER.logError("Subtask not found!");
        } else {
            LOGGER.logInfo(subtask.toString());
            Epic epic = epics.get(subtask.getEpicId());
            epic.removeSubtaskId(subtask.getId());
            updateEpicStatus(epic);
        }
        return subtask;
    }

    public ArrayList<Subtask> getEpicSubtasks(long epicId) {
        LOGGER.logInfo("Retrieving subtasks of epic id=" + epicId);
        Epic epic = epics.get(epicId);
        if (epic == null) {
            LOGGER.logError("Epic not found!");
            return null;
        }
        ArrayList<Long> subtaskIds = epic.getSubtaskIds();
        ArrayList<Subtask> subtaskList = new ArrayList<>();
        for (long subtaskId : subtaskIds) {
            subtaskList.add(subtasks.get(subtaskId));
        }
        LOGGER.logInfo(subtaskList.size() + " subtask(s) retrieved");
        return subtaskList;
    }

    private long generateId() {
        return ++lastUsedId;
    }

    private void reassignSubtaskToNewEpic(Subtask subtask, long newEpicId) {
        LOGGER.logInfo("Reassigning subtask to epic id=" + newEpicId + ": " + subtask);
        Epic oldEpic = epics.get(subtask.getEpicId());
        oldEpic.removeSubtaskId(subtask.getId());
        updateEpicStatus(oldEpic);
        subtask.setEpicId(newEpicId);
    }

    private void updateEpicStatus(Epic epic) {
        boolean areAllSubtasksNew = true;
        boolean areAllSubtasksDone = true;
        for (long subtaskId : epic.getSubtaskIds()) {
            Subtask subtask = subtasks.get(subtaskId);
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
