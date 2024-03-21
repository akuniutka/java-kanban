package io.github.akuniutka.kanban.service;

import io.github.akuniutka.kanban.model.Epic;
import io.github.akuniutka.kanban.model.Subtask;
import io.github.akuniutka.kanban.model.Task;
import io.github.akuniutka.kanban.model.TaskStatus;
import io.github.akuniutka.kanban.util.IdGenerator;
import io.github.akuniutka.kanban.util.Logger;

import java.util.ArrayList;
import java.util.HashMap;

public class TaskManager {
    private static final Logger LOGGER = new Logger();
    private static final IdGenerator ID_GENERATOR = new IdGenerator();
    private final HashMap<Integer, Task> tasks;
    private final HashMap<Integer, Subtask> subtasks;
    private final HashMap<Integer, Epic> epics;

    public TaskManager() {
        this.tasks = new HashMap<>();
        this.subtasks = new HashMap<>();
        this.epics = new HashMap<>();
    }

    public ArrayList<Task> getAllTasks() {
        LOGGER.logInfo("Retrieving all tasks");
        ArrayList<Task> taskList = new ArrayList<>();
        for (Task task : tasks.values()) {
            taskList.add(Task.copyOf(task));
        }
        return taskList;
    }

    public void removeAllTasks() {
        LOGGER.logInfo("Removing all tasks");
        tasks.clear();
    }

    public Task getTaskById(int id) {
        LOGGER.logInfo("Retrieving task by id=" + id);
        return Task.copyOf(tasks.get(id));
    }

    public Task createNewTask(Task task) {
        LOGGER.logInfo("Creating new task: " + task);
        if (task == null) {
            LOGGER.logError("Cannot create null task!");
            return null;
        }
        Task newTask = Task.copyOf(task);
        int newTaskId = ID_GENERATOR.nextId();
        newTask.setId(newTaskId);
        tasks.put(newTaskId, newTask);
        return Task.copyOf(tasks.get(newTaskId));
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
        tasks.put(task.getId(), Task.copyOf(task));
        return Task.copyOf(tasks.get(task.getId()));
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
        ArrayList<Epic> epicList = new ArrayList<>();
        for (Epic epic : epics.values()) {
            epicList.add(Epic.copyOf(epic));
        }
        return epicList;
    }

    public void removeAllEpics() {
        LOGGER.logInfo("Removing all epics");
        subtasks.clear();
        epics.clear();
    }

    public Epic getEpicById(int id) {
        LOGGER.logInfo("Retrieving epic by id=" + id);
        return Epic.copyOf(epics.get(id));
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
        Epic newEpic = Epic.copyOf(epic);
        int epicId = ID_GENERATOR.nextId();
        newEpic.setId(epicId);
        for (int subtaskId : newEpic.getSubtaskIds()) {
            reassignSubtaskToNewEpic(subtasks.get(subtaskId), epicId);
        }
        recalculateEpicStatus(newEpic);
        epics.put(epicId, newEpic);
        return Epic.copyOf(epics.get(epicId));
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
        return Epic.copyOf(epics.get(epic.getId()));
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
        ArrayList<Subtask> subtaskList = new ArrayList<>();
        for (Subtask subtask : subtasks.values()) {
            subtaskList.add(Subtask.copyOf(subtask));
        }
        return subtaskList;
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
        return Subtask.copyOf(subtasks.get(id));
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
        int subtaskId = ID_GENERATOR.nextId();
        subtask.setId(subtaskId);
        epic.addSubtaskId(subtaskId);
        subtasks.put(subtaskId, subtask);
        recalculateEpicStatus(epic);
        return subtasks.get(subtaskId);
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
        subtasks.put(subtask.getId(), Subtask.copyOf(subtask));
        recalculateEpicStatus(epics.get(subtask.getEpicId()));
        return Subtask.copyOf(subtasks.get(subtask.getId()));
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
            subtaskList.add(Subtask.copyOf(subtasks.get(subtaskId)));
        }
        return subtaskList;
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
