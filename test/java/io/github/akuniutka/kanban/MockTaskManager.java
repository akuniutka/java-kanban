package io.github.akuniutka.kanban;

import io.github.akuniutka.kanban.model.Epic;
import io.github.akuniutka.kanban.model.Subtask;
import io.github.akuniutka.kanban.model.Task;
import io.github.akuniutka.kanban.service.TaskManager;

import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.assertNotNull;

public class MockTaskManager implements TaskManager {
    private Supplier<List<Task>> getTasks;
    private Runnable deleteTasks;
    private Function<Long, Optional<Task>> getTaskById;
    private Function<Task, Long> createTask;
    private Consumer<Task> updateTask;
    private Consumer<Long> deleteTask;
    private Supplier<List<Epic>> getEpics;
    private Runnable deleteEpics;
    private Function<Long, Optional<Epic>> getEpicById;
    private Function<Epic, Long> createEpic;
    private Consumer<Epic> updateEpic;
    private Consumer<Long> deleteEpic;
    private Supplier<List<Subtask>> getSubtasks;
    private Runnable deleteSubtasks;
    private Function<Long, Optional<Subtask>> getSubtaskById;
    private Function<Subtask, Long> createSubtask;
    private Consumer<Subtask> updateSubtask;
    private Consumer<Long> deleteSubtask;
    private Supplier<List<Subtask>> getEpicSubtasks;
    private Supplier<List<Task>> getHistory;
    private Supplier<List<Task>> getPrioritizedTasks;

    public MockTaskManager withGetTasks(Supplier<List<Task>> getTasks) {
        this.getTasks = getTasks;
        return this;
    }

    public MockTaskManager withDeleteTasks(Runnable deleteTasks) {
        this.deleteTasks = deleteTasks;
        return this;
    }

    public MockTaskManager withGetTaskById(Function<Long, Optional<Task>> getTaskById) {
        this.getTaskById = getTaskById;
        return this;
    }

    public MockTaskManager withCreateTask(Function<Task, Long> createTask) {
        this.createTask = createTask;
        return this;
    }

    public MockTaskManager withUpdateTask(Consumer<Task> updateTask) {
        this.updateTask = updateTask;
        return this;
    }

    public MockTaskManager withDeleteTask(Consumer<Long> deleteTask) {
        this.deleteTask = deleteTask;
        return this;
    }

    public MockTaskManager withGetEpics(Supplier<List<Epic>> getEpics) {
        this.getEpics = getEpics;
        return this;
    }

    public MockTaskManager withDeleteEpics(Runnable deleteEpics) {
        this.deleteEpics = deleteEpics;
        return this;
    }

    public MockTaskManager withGetEpicById(Function<Long, Optional<Epic>> getEpicById) {
        this.getEpicById = getEpicById;
        return this;
    }

    public MockTaskManager withCreateEpic(Function<Epic, Long> createEpic) {
        this.createEpic = createEpic;
        return this;
    }

    public MockTaskManager withUpdateEpic(Consumer<Epic> updateEpic) {
        this.updateEpic = updateEpic;
        return this;
    }

    public MockTaskManager withDeleteEpic(Consumer<Long> deleteEpic) {
        this.deleteEpic = deleteEpic;
        return this;
    }

    public MockTaskManager withGetSubtasks(Supplier<List<Subtask>> getSubtasks) {
        this.getSubtasks = getSubtasks;
        return this;
    }

    public MockTaskManager withDeleteSubtasks(Runnable deleteSubtasks) {
        this.deleteSubtasks = deleteSubtasks;
        return this;
    }

    public MockTaskManager withGetSubtaskById(Function<Long, Optional<Subtask>> getSubtaskById) {
        this.getSubtaskById = getSubtaskById;
        return this;
    }

    public MockTaskManager withCreateSubtask(Function<Subtask, Long> createSubtask) {
        this.createSubtask = createSubtask;
        return this;
    }

    public MockTaskManager withUpdateSubtask(Consumer<Subtask> updateSubtask) {
        this.updateSubtask = updateSubtask;
        return this;
    }

    public MockTaskManager withDeleteSubtask(Consumer<Long> deleteSubtask) {
        this.deleteSubtask = deleteSubtask;
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
    public void deleteTasks() {
        assertNotNull(deleteTasks, "method deleteTasks() should not be called");
        deleteTasks.run();
    }

    @Override
    public Optional<Task> getTaskById(long id) {
        assertNotNull(getTaskById, "method getTaskById() should not be called");
        return getTaskById.apply(id);
    }

    @Override
    public long createTask(Task task) {
        assertNotNull(createTask, "method createTask() should not be called");
        return createTask.apply(task);
    }

    @Override
    public void updateTask(Task task) {
        assertNotNull(updateTask, "method updateTask() should not be called");
        updateTask.accept(task);
    }

    @Override
    public void deleteTask(long id) {
        assertNotNull(deleteTask, "method deleteTask() should not be called");
        deleteTask.accept(id);
    }

    @Override
    public List<Epic> getEpics() {
        assertNotNull(getEpics, "method getEpics() should not be called");
        return getEpics.get();
    }

    @Override
    public void deleteEpics() {
        assertNotNull(deleteEpics, "method deleteEpics() should not be called");
        deleteEpics.run();
    }

    @Override
    public Optional<Epic> getEpicById(long id) {
        assertNotNull(getEpicById, "method getEpicById() should not be called");
        return getEpicById.apply(id);
    }

    @Override
    public long createEpic(Epic epic) {
        assertNotNull(createEpic, "method createEpic() should not be called");
        return createEpic.apply(epic);
    }

    @Override
    public void updateEpic(Epic epic) {
        assertNotNull(updateEpic, "method updateEpic() should not be called");
        updateEpic.accept(epic);
    }

    @Override
    public void deleteEpic(long id) {
        assertNotNull(deleteEpic, "method deleteEpic() should not be called");
        deleteEpic.accept(id);
    }

    @Override
    public List<Subtask> getSubtasks() {
        assertNotNull(getSubtasks, "method getSubtasks() should not be called");
        return getSubtasks.get();
    }

    @Override
    public void deleteSubtasks() {
        assertNotNull(deleteSubtasks, "method deleteSubtasks() should not be called");
        deleteSubtasks.run();
    }

    @Override
    public Optional<Subtask> getSubtaskById(long id) {
        assertNotNull(getSubtaskById, "method getSubtaskById() should not be called");
        return getSubtaskById.apply(id);
    }

    @Override
    public long createSubtask(Subtask subtask) {
        assertNotNull(createSubtask, "method createSubtask() should not be called");
        return createSubtask.apply(subtask);
    }

    @Override
    public void updateSubtask(Subtask subtask) {
        assertNotNull(updateSubtask, "method updateSubtask() should not be called");
        updateSubtask.accept(subtask);
    }

    @Override
    public void deleteSubtask(long id) {
        assertNotNull(deleteSubtask, "method deleteSubtask() should not be called");
        deleteSubtask.accept(id);
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
