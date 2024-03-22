package io.github.akuniutka.kanban.service;

import io.github.akuniutka.kanban.model.Epic;
import io.github.akuniutka.kanban.model.Subtask;
import io.github.akuniutka.kanban.model.Task;
import io.github.akuniutka.kanban.model.TaskStatus;

import java.util.ArrayList;
import java.util.HashMap;

public class TaskManager {
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
        return new ArrayList<>(tasks.values());
    }

    public void removeTasks() {
        tasks.clear();
    }

    public Task getTask(long id) {
        return tasks.get(id);
    }

    public Task addTask(Task task) {
        if (task == null) {
            return null;
        }
        long taskId = generateId();
        task.setId(taskId);
        tasks.put(taskId, task);
        return task;
    }

    public Task updateTask(Task task) {
        if (task == null || !tasks.containsKey(task.getId())) {
            return null;
        }
        tasks.put(task.getId(), task);
        return task;
    }

    public Task removeTask(long id) {
        return tasks.remove(id);
    }

    public ArrayList<Epic> getEpics() {
        return new ArrayList<>(epics.values());
    }

    public void removeEpics() {
        subtasks.clear();
        epics.clear();
    }

    public Epic getEpic(long id) {
        return epics.get(id);
    }

    public Epic addEpic(Epic epic) {
        if (epic == null) {
            return null;
        }
        long epicId = generateId();
        epic.setId(epicId);
        updateEpicStatus(epic);
        epics.put(epicId, epic);
        return epic;
    }

    public Epic updateEpic(Epic epic) {
        if (epic == null) {
            return null;
        }
        Epic storedEpic = epics.get(epic.getId());
        if (storedEpic == null) {
            return null;
        }
        storedEpic.setTitle(epic.getTitle());
        storedEpic.setDescription(epic.getDescription());
        return storedEpic;
    }

    public Epic removeEpic(long id) {
        Epic epic = epics.remove(id);
        if (epic != null) {
            for (long subtaskId : epic.getSubtaskIds()) {
                subtasks.remove(subtaskId);
            }
        }
        return epic;
    }

    public ArrayList<Subtask> getSubtasks() {
        return new ArrayList<>(subtasks.values());
    }

    public void removeSubtasks() {
        for (Epic epic : epics.values()) {
            ArrayList<Long> subtaskIds = epic.getSubtaskIds();
            for (long subtaskId : subtaskIds) {
                epic.removeSubtaskId(subtaskId);
            }
            updateEpicStatus(epic);
        }
        subtasks.clear();
    }

    public Subtask getSubtask(long id) {
        return subtasks.get(id);
    }

    public Subtask addSubtask(Subtask subtask) {
        if (subtask == null) {
            return null;
        }
        Epic epic = epics.get(subtask.getEpicId());
        if (epic == null) {
            return null;
        }
        long subtaskId = generateId();
        subtask.setId(subtaskId);
        epic.addSubtaskId(subtaskId);
        subtasks.put(subtaskId, subtask);
        updateEpicStatus(epic);
        return subtask;
    }

    public Subtask updateSubtask(Subtask subtask) {
        if (subtask == null) {
            return null;
        }
        Subtask storedSubtask = subtasks.get(subtask.getId());
        if (storedSubtask == null) {
            return null;
        }
        storedSubtask.setTitle(subtask.getTitle());
        storedSubtask.setDescription(subtask.getDescription());
        storedSubtask.setStatus(subtask.getStatus());
        updateEpicStatus(epics.get(storedSubtask.getEpicId()));
        return storedSubtask;
    }

    public Subtask removeSubtask(long id) {
        Subtask subtask = subtasks.remove(id);
        if (subtask != null) {
            Epic epic = epics.get(subtask.getEpicId());
            epic.removeSubtaskId(subtask.getId());
            updateEpicStatus(epic);
        }
        return subtask;
    }

    public ArrayList<Subtask> getEpicSubtasks(long epicId) {
        Epic epic = epics.get(epicId);
        if (epic == null) {
            return null;
        }
        ArrayList<Long> subtaskIds = epic.getSubtaskIds();
        ArrayList<Subtask> subtaskList = new ArrayList<>();
        for (long subtaskId : subtaskIds) {
            subtaskList.add(subtasks.get(subtaskId));
        }
        return subtaskList;
    }

    private long generateId() {
        return ++lastUsedId;
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
