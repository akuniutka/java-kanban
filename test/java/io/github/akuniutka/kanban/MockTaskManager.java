package io.github.akuniutka.kanban;

import io.github.akuniutka.kanban.model.Epic;
import io.github.akuniutka.kanban.model.Subtask;
import io.github.akuniutka.kanban.model.Task;
import io.github.akuniutka.kanban.service.TaskManager;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.assertNotNull;

public class MockTaskManager implements TaskManager {
    private Supplier<List<Task>> getTasks;
    private Runnable removeTasks;
    private Function<Long, Task> getTask;
    private Function<Task, Long> addTask;
    private Consumer<Task> updateTask;
    private Consumer<Long> removeTask;
    private Supplier<List<Epic>> getEpics;
    private Runnable removeEpics;
    private Function<Long, Epic> getEpic;
    private Function<Epic, Long> addEpic;
    private Consumer<Epic> updateEpic;
    private Consumer<Long> removeEpic;
    private Supplier<List<Subtask>> getSubtasks;
    private Runnable removeSubtasks;
    private Function<Long, Subtask> getSubtask;
    private Function<Subtask, Long> addSubtask;
    private Consumer<Subtask> updateSubtask;
    private Consumer<Long> removeSubtask;
    private Supplier<List<Subtask>> getEpicSubtasks;
    private Supplier<List<Task>> getHistory;
    private Supplier<List<Task>> getPrioritizedTasks;

    public MockTaskManager withGetTasks(Supplier<List<Task>> getTasks) {
        this.getTasks = getTasks;
        return this;
    }

    public MockTaskManager withRemoveTasks(Runnable removeTasks) {
        this.removeTasks = removeTasks;
        return this;
    }

    public MockTaskManager withGetTask(Function<Long, Task> getTask) {
        this.getTask = getTask;
        return this;
    }

    public MockTaskManager withAddTask(Function<Task, Long> addTask) {
        this.addTask = addTask;
        return this;
    }

    public MockTaskManager withUpdateTask(Consumer<Task> updateTask) {
        this.updateTask = updateTask;
        return this;
    }

    public MockTaskManager withRemoveTask(Consumer<Long> removeTask) {
        this.removeTask = removeTask;
        return this;
    }

    public MockTaskManager withGetEpics(Supplier<List<Epic>> getEpics) {
        this.getEpics = getEpics;
        return this;
    }

    public MockTaskManager withRemoveEpics(Runnable removeEpics) {
        this.removeEpics = removeEpics;
        return this;
    }

    public MockTaskManager withGetEpic(Function<Long, Epic> getEpic) {
        this.getEpic = getEpic;
        return this;
    }

    public MockTaskManager withAddEpic(Function<Epic, Long> addEpic) {
        this.addEpic = addEpic;
        return this;
    }

    public MockTaskManager withUpdateEpic(Consumer<Epic> updateEpic) {
        this.updateEpic = updateEpic;
        return this;
    }

    public MockTaskManager withRemoveEpic(Consumer<Long> removeEpic) {
        this.removeEpic = removeEpic;
        return this;
    }

    public MockTaskManager withGetSubtasks(Supplier<List<Subtask>> getSubtasks) {
        this.getSubtasks = getSubtasks;
        return this;
    }

    public MockTaskManager withRemoveSubtasks(Runnable removeSubtasks) {
        this.removeSubtasks = removeSubtasks;
        return this;
    }

    public MockTaskManager withGetSubtask(Function<Long, Subtask> getSubtask) {
        this.getSubtask = getSubtask;
        return this;
    }

    public MockTaskManager withAddSubtask(Function<Subtask, Long> addSubtask) {
        this.addSubtask = addSubtask;
        return this;
    }

    public MockTaskManager withUpdateSubtask(Consumer<Subtask> updateSubtask) {
        this.updateSubtask = updateSubtask;
        return this;
    }

    public MockTaskManager withRemoveSubtask(Consumer<Long> removeSubtask) {
        this.removeSubtask = removeSubtask;
        return this;
    }

    public MockTaskManager withGetEpicSubtasks(Supplier<List<Subtask>> getEpicSubtasks) {
        this.getEpicSubtasks = getEpicSubtasks;
        return this;
    }

    public MockTaskManager withGetHistory(Supplier<List<Task>> getHistory) {
        this.getHistory = getHistory;
        return this;
    }

    public MockTaskManager withGetPrioritizedTasks(Supplier<List<Task>> getPrioritizedTasks) {
        this.getPrioritizedTasks = getPrioritizedTasks;
        return this;
    }

    @Override
    public List<Task> getTasks() {
        assertNotNull(getTasks, "method getTasks() should not be called");
        return getTasks.get();
    }

    @Override
    public void removeTasks() {
        assertNotNull(removeTasks, "method removeTasks() should not be called");
        removeTasks.run();
    }

    @Override
    public Task getTask(long id) {
        assertNotNull(getTask, "method getTask() should not be called");
        return getTask.apply(id);
    }

    @Override
    public long addTask(Task task) {
        assertNotNull(addTask, "method addTask() should not be called");
        return addTask.apply(task);
    }

    @Override
    public void updateTask(Task task) {
        assertNotNull(updateTask, "method updateTask() should not be called");
        updateTask.accept(task);
    }

    @Override
    public void removeTask(long id) {
        assertNotNull(removeTask, "method removeTask() should not be called");
        removeTask.accept(id);
    }

    @Override
    public List<Epic> getEpics() {
        assertNotNull(getEpics, "method getEpics() should not be called");
        return getEpics.get();
    }

    @Override
    public void removeEpics() {
        assertNotNull(removeEpics, "method removeEpics() should not be called");
        removeEpics.run();
    }

    @Override
    public Epic getEpic(long id) {
        assertNotNull(getEpic, "method getEpic() should not be called");
        return getEpic.apply(id);
    }

    @Override
    public long addEpic(Epic epic) {
        assertNotNull(addEpic, "method addEpic() should not be called");
        return addEpic.apply(epic);
    }

    @Override
    public void updateEpic(Epic epic) {
        assertNotNull(updateEpic, "method updateEpic() should not be called");
        updateEpic.accept(epic);
    }

    @Override
    public void removeEpic(long id) {
        assertNotNull(removeEpic, "method removeEpic() should not be called");
        removeEpic.accept(id);
    }

    @Override
    public List<Subtask> getSubtasks() {
        assertNotNull(getSubtasks, "method getSubtasks() should not be called");
        return getSubtasks.get();
    }

    @Override
    public void removeSubtasks() {
        assertNotNull(removeSubtasks, "method removeSubtasks() should not be called");
        removeSubtasks.run();
    }

    @Override
    public Subtask getSubtask(long id) {
        assertNotNull(getSubtask, "method getSubtask() should not be called");
        return getSubtask.apply(id);
    }

    @Override
    public long addSubtask(Subtask subtask) {
        assertNotNull(addSubtask, "method addSubtask() should not be called");
        return addSubtask.apply(subtask);
    }

    @Override
    public void updateSubtask(Subtask subtask) {
        assertNotNull(updateSubtask, "method updateSubtask() should not be called");
        updateSubtask.accept(subtask);
    }

    @Override
    public void removeSubtask(long id) {
        assertNotNull(removeSubtask, "method removeSubtask() should not be called");
        removeSubtask.accept(id);
    }

    @Override
    public List<Subtask> getEpicSubtasks(long epicId) {
        assertNotNull(getEpicSubtasks, "method getEpicSubtasks() should not be called");
        return getEpicSubtasks.get();
    }

    @Override
    public List<Task> getHistory() {
        assertNotNull(getHistory, "method getHistory() should not be called");
        return List.of();
    }

    @Override
    public List<Task> getPrioritizedTasks() {
        assertNotNull(getPrioritizedTasks, "method getPrioritizedTasks() should not be called");
        return getPrioritizedTasks.get();
    }
}
