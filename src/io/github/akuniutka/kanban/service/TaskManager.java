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
    private final HashMap<Integer, Task> tasks;
    private final HashMap<Integer, Subtask> subtasks;
    private final HashMap<Integer, Epic> epics;
    private int lastUsedId = 0;

    public TaskManager() {
        this.tasks = new HashMap<>();
        this.subtasks = new HashMap<>();
        this.epics = new HashMap<>();
    }

    public ArrayList<Task> getAllTasks() {
        LOGGER.logInfo("Retrieving all tasks");
        return new ArrayList<>(tasks.values());
    }

    public void removeAllTasks() {
        LOGGER.logInfo("Removing all tasks");
        tasks.clear();
    }

    public Task getTaskById(int id) {
        LOGGER.logInfo("Retrieving task by id=" + id);
        return tasks.get(id);
    }

    public Task createNewTask(Task task) {
        LOGGER.logInfo("Creating new task: " + task);
        if (task == null) {
            LOGGER.logError("Cannot create null task!");
            return null;
        }
        int taskId = generateId();
        task.setId(taskId);
        tasks.put(taskId, task);
        return task;
    }

    public Task updateTask(Task task) {
        LOGGER.logInfo("Updating task: " + task);
        if (task == null) {
            LOGGER.logError("Cannot update null task!");
            return null;
        }
        if (!tasks.containsKey(task.getId())) {
            LOGGER.logError("Cannot update unknown task!");
            return null;
        }
        tasks.put(task.getId(), task);
        return task;
    }

    public Task removeTask(int id) {
        LOGGER.logInfo("Removing task by id=" + id);
        if (!tasks.containsKey(id)) {
            LOGGER.logError("Cannot remove unknown task!");
            return null;
        }
        return tasks.remove(id);
    }

    public ArrayList<Epic> getAllEpics() {
        LOGGER.logInfo("Retrieving all epics");
        return new ArrayList<>(epics.values());
    }

    public void removeAllEpics() {
        LOGGER.logInfo("Removing all epics");
        subtasks.clear();
        epics.clear();
    }

    public Epic getEpicById(int id) {
        LOGGER.logInfo("Retrieving epic by id=" + id);
        return epics.get(id);
    }

    public Epic createNewEpic(Epic epic) {
        LOGGER.logInfo("Creating new epic: " + epic);
        if (epic == null) {
            LOGGER.logError("Cannot create null epic!");
            return null;
        }
        if (containsUnknownSubtasks(epic)) {
            LOGGER.logError("Cannot create apic with unknown subtasks!");
            return null;
        }
        int epicId = generateId();
        epic.setId(epicId);
        for (int subtaskId : epic.getSubtaskIds()) {
            reassignSubtaskToNewEpic(subtasks.get(subtaskId), epicId);
        }
        recalculateEpicStatus(epic);
        epics.put(epicId, epic);
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
            LOGGER.logError("Cannot update unknown epic!");
            return null;
        }
        if (containsUnknownSubtasks(epic)) {
            LOGGER.logError("Cannot update apic with unknown subtasks!");
            return null;
        }
        storedEpic.setTitle(epic.getTitle());
        storedEpic.setDescription(epic.getDescription());
        for (int subtaskId : epic.getSubtaskIds()) {
            Subtask subtask = subtasks.get(subtaskId);
            if (subtask.getEpicId() != storedEpic.getId()) {
                storedEpic.addSubtaskId(subtaskId);
                reassignSubtaskToNewEpic(subtask, storedEpic.getId());
            }
        }
        ArrayList<Integer> removedSubtaskIds = new ArrayList<>();
        for (int subtaskId : storedEpic.getSubtaskIds()) {
            if (!epic.containsSubtask(subtaskId)) {
                removedSubtaskIds.add(subtaskId);
            }
        }
        for (int subtaskId : removedSubtaskIds) {
            storedEpic.removeSubtaskId(subtaskId);
            subtasks.remove(subtaskId);
        }
        recalculateEpicStatus(storedEpic);
        return storedEpic;
    }

    public Epic removeEpic(int id) {
        LOGGER.logInfo("Removing epic by id=" + id);
        Epic epic = epics.remove(id);
        if (epic == null) {
            LOGGER.logError("Cannot remove unknown epic!");
            return null;
        }
        for (int subtaskId : epic.getSubtaskIds()) {
            LOGGER.logInfo("Removing subtask: " + subtasks.get(subtaskId));
            subtasks.remove(subtaskId);
        }
        return epic;
    }

    public ArrayList<Subtask> getAllSubtasks() {
        LOGGER.logInfo("Retrieving all subtasks");
        return new ArrayList<>(subtasks.values());
    }

    public void removeAllSubtasks() {
        LOGGER.logInfo("Removing all subtasks");
        for (Epic epic : epics.values()) {
            ArrayList<Integer> subtaskIds = epic.getSubtaskIds();
            for (int subtaskId : subtaskIds) {
                epic.removeSubtaskId(subtaskId);
            }
            recalculateEpicStatus(epic);
        }
        subtasks.clear();
    }

    public Subtask getSubtaskById(int id) {
        LOGGER.logInfo("Retrieving subtask by id=" + id);
        return subtasks.get(id);
    }

    public Subtask createNewSubtask(Subtask subtask) {
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
        int subtaskId = generateId();
        subtask.setId(subtaskId);
        epic.addSubtaskId(subtaskId);
        subtasks.put(subtaskId, subtask);
        recalculateEpicStatus(epic);
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
            LOGGER.logError("Cannot update unknown subtask!");
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
        recalculateEpicStatus(epics.get(subtask.getEpicId()));
        return subtask;
    }

    public Subtask removeSubtask(int id) {
        LOGGER.logInfo("Removing subtask by id=" + id);
        Subtask subtask = subtasks.remove(id);
        if (subtask == null) {
            LOGGER.logError("Cannot remove unknown subtask!");
            return null;
        }
        Epic epic = epics.get(subtask.getEpicId());
        epic.removeSubtaskId(subtask.getId());
        recalculateEpicStatus(epic);
        return subtask;
    }

    public ArrayList<Subtask> getEpicSubtasks(int epicId) {
        LOGGER.logInfo("Retrieving all subtasks of epic id=" + epicId);
        Epic epic = epics.get(epicId);
        if (epic == null) {
            LOGGER.logError("Cannot retrieve subtasks of unknown epic!");
            return null;
        }
        ArrayList<Integer> subtaskIds = epic.getSubtaskIds();
        ArrayList<Subtask> subtaskList = new ArrayList<>();
        for (int subtaskId : subtaskIds) {
            subtaskList.add(subtasks.get(subtaskId));
        }
        return subtaskList;
    }

    private int generateId() {
        return ++lastUsedId;
    }

    private boolean containsUnknownSubtasks(Epic epic) {
        for (int subtaskId : epic.getSubtaskIds()) {
            if (!subtasks.containsKey(subtaskId)) {
                return true;
            }
        }
        return false;
    }

    private void reassignSubtaskToNewEpic(Subtask subtask, int newEpicId) {
        LOGGER.logInfo("Reassigning subtask to epic id=" + newEpicId + ": " + subtask);
        Epic oldEpic = epics.get(subtask.getEpicId());
        oldEpic.removeSubtaskId(subtask.getId());
        recalculateEpicStatus(oldEpic);
        subtask.setEpicId(newEpicId);
    }

    private void recalculateEpicStatus(Epic epic) {
        boolean areAllSubtasksNew = true;
        boolean areAllSubtasksDone = true;
        for (int subtaskId : epic.getSubtaskIds()) {
            Subtask subtask = subtasks.get(subtaskId);
            if (subtask.getStatus() != TaskStatus.NEW) {
                areAllSubtasksNew = false;
            }
            if (subtask.getStatus() != TaskStatus.DONE) {
                areAllSubtasksDone = false;
            }
        }
        if (!areAllSubtasksNew) {
            if (areAllSubtasksDone) {
                epic.setStatus(TaskStatus.DONE);
            } else {
                epic.setStatus(TaskStatus.IN_PROGRESS);
            }
        } else {
            epic.setStatus(TaskStatus.NEW);
        }
    }
}
