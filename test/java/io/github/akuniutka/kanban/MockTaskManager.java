package io.github.akuniutka.kanban;

import io.github.akuniutka.kanban.model.Epic;
import io.github.akuniutka.kanban.model.Subtask;
import io.github.akuniutka.kanban.model.Task;
import io.github.akuniutka.kanban.service.TaskManager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.LongConsumer;
import java.util.function.LongFunction;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;

import static org.junit.jupiter.api.Assertions.assertNotNull;

public class MockTaskManager implements TaskManager {
    private final Calls calls;
    private Supplier<List<Task>> getTasks;
    private Runnable deleteTasks;
    private LongFunction<Optional<Task>> getTaskById;
    private UnaryOperator<Task> createTask;
    private UnaryOperator<Task> updateTask;
    private LongConsumer deleteTask;
    private Supplier<List<Epic>> getEpics;
    private Runnable deleteEpics;
    private LongFunction<Optional<Epic>> getEpicById;
    private UnaryOperator<Epic> createEpic;
    private UnaryOperator<Epic> updateEpic;
    private LongConsumer deleteEpic;
    private Supplier<List<Subtask>> getSubtasks;
    private Runnable deleteSubtasks;
    private LongFunction<Optional<Subtask>> getSubtaskById;
    private UnaryOperator<Subtask> createSubtask;
    private UnaryOperator<Subtask> updateSubtask;
    private LongConsumer deleteSubtask;
    private LongFunction<List<Subtask>> getEpicSubtasks;
    private Supplier<List<Task>> getHistory;
    private Supplier<List<Task>> getPrioritizedTasks;

    public MockTaskManager() {
        this.calls = new Calls();
    }

    public MockTaskManager withGetTasks(Supplier<List<Task>> getTasks) {
        this.getTasks = getTasks;
        calls.getTasks = 0;
        return this;
    }

    public MockTaskManager withDeleteTasks(Runnable deleteTasks) {
        this.deleteTasks = deleteTasks;
        calls.deleteTasks = 0;
        return this;
    }

    public MockTaskManager withGetTaskById(LongFunction<Optional<Task>> getTaskById) {
        this.getTaskById = getTaskById;
        calls.getTaskById = new ArrayList<>();
        return this;
    }

    public MockTaskManager withCreateTask(UnaryOperator<Task> createTask) {
        this.createTask = createTask;
        calls.createTask = new ArrayList<>();
        return this;
    }

    public MockTaskManager withUpdateTask(UnaryOperator<Task> updateTask) {
        this.updateTask = updateTask;
        calls.updateTask = new ArrayList<>();
        return this;
    }

    public MockTaskManager withDeleteTask(LongConsumer deleteTask) {
        this.deleteTask = deleteTask;
        calls.deleteTask = new ArrayList<>();
        return this;
    }

    public MockTaskManager withGetEpics(Supplier<List<Epic>> getEpics) {
        this.getEpics = getEpics;
        calls.getEpics = 0;
        return this;
    }

    public MockTaskManager withDeleteEpics(Runnable deleteEpics) {
        this.deleteEpics = deleteEpics;
        calls.deleteEpics = 0;
        return this;
    }

    public MockTaskManager withGetEpicById(LongFunction<Optional<Epic>> getEpicById) {
        this.getEpicById = getEpicById;
        calls.getEpicById = new ArrayList<>();
        return this;
    }

    public MockTaskManager withCreateEpic(UnaryOperator<Epic> createEpic) {
        this.createEpic = createEpic;
        calls.createEpic = new ArrayList<>();
        return this;
    }

    public MockTaskManager withUpdateEpic(UnaryOperator<Epic> updateEpic) {
        this.updateEpic = updateEpic;
        calls.updateEpic = new ArrayList<>();
        return this;
    }

    public MockTaskManager withDeleteEpic(LongConsumer deleteEpic) {
        this.deleteEpic = deleteEpic;
        calls.deleteEpic = new ArrayList<>();
        return this;
    }

    public MockTaskManager withGetSubtasks(Supplier<List<Subtask>> getSubtasks) {
        this.getSubtasks = getSubtasks;
        calls.getSubtasks = 0;
        return this;
    }

    public MockTaskManager withDeleteSubtasks(Runnable deleteSubtasks) {
        this.deleteSubtasks = deleteSubtasks;
        calls.deleteSubtasks = 0;
        return this;
    }

    public MockTaskManager withGetSubtaskById(LongFunction<Optional<Subtask>> getSubtaskById) {
        this.getSubtaskById = getSubtaskById;
        calls.getSubtaskById = new ArrayList<>();
        return this;
    }

    public MockTaskManager withCreateSubtask(UnaryOperator<Subtask> createSubtask) {
        this.createSubtask = createSubtask;
        calls.createSubtask = new ArrayList<>();
        return this;
    }

    public MockTaskManager withUpdateSubtask(UnaryOperator<Subtask> updateSubtask) {
        this.updateSubtask = updateSubtask;
        calls.updateSubtask = new ArrayList<>();
        return this;
    }

    public MockTaskManager withDeleteSubtask(LongConsumer deleteSubtask) {
        this.deleteSubtask = deleteSubtask;
        calls.deleteSubtask = new ArrayList<>();
        return this;
    }

    public MockTaskManager withGetEpicSubtasks(LongFunction<List<Subtask>> getEpicSubtasks) {
        this.getEpicSubtasks = getEpicSubtasks;
        calls.getEpicSubtasks = new ArrayList<>();
        return this;
    }

    public MockTaskManager withGetHistory(Supplier<List<Task>> getHistory) {
        this.getHistory = getHistory;
        calls.getHistory = 0;
        return this;
    }

    public MockTaskManager withGetPrioritizedTasks(Supplier<List<Task>> getPrioritizedTasks) {
        this.getPrioritizedTasks = getPrioritizedTasks;
        calls.getPrioritizedTasks = 0;
        return this;
    }

    @Override
    public List<Task> getTasks() {
        assertNotNull(getTasks, "method getTasks() should not be called");
        calls.getTasks++;
        return getTasks.get();
    }

    @Override
    public void deleteTasks() {
        assertNotNull(deleteTasks, "method deleteTasks() should not be called");
        calls.deleteTasks++;
        deleteTasks.run();
    }

    @Override
    public Optional<Task> getTaskById(long id) {
        assertNotNull(getTaskById, "method getTaskById() should not be called");
        calls.getTaskById.add(id);
        return getTaskById.apply(id);
    }

    @Override
    public Task createTask(Task task) {
        assertNotNull(createTask, "method createTask() should not be called");
        calls.createTask.add(task);
        return createTask.apply(task);
    }

    @Override
    public Task updateTask(Task task) {
        assertNotNull(updateTask, "method updateTask() should not be called");
        calls.updateTask.add(task);
        return updateTask.apply(task);
    }

    @Override
    public void deleteTask(long id) {
        assertNotNull(deleteTask, "method deleteTask() should not be called");
        calls.deleteTask.add(id);
        deleteTask.accept(id);
    }

    @Override
    public List<Epic> getEpics() {
        assertNotNull(getEpics, "method getEpics() should not be called");
        calls.getEpics++;
        return getEpics.get();
    }

    @Override
    public void deleteEpics() {
        assertNotNull(deleteEpics, "method deleteEpics() should not be called");
        calls.deleteEpics++;
        deleteEpics.run();
    }

    @Override
    public Optional<Epic> getEpicById(long id) {
        assertNotNull(getEpicById, "method getEpicById() should not be called");
        calls.getEpicById.add(id);
        return getEpicById.apply(id);
    }

    @Override
    public Epic createEpic(Epic epic) {
        assertNotNull(createEpic, "method createEpic() should not be called");
        calls.createEpic.add(epic);
        return createEpic.apply(epic);
    }

    @Override
    public Epic updateEpic(Epic epic) {
        assertNotNull(updateEpic, "method updateEpic() should not be called");
        calls.updateEpic.add(epic);
        return updateEpic.apply(epic);
    }

    @Override
    public void deleteEpic(long id) {
        assertNotNull(deleteEpic, "method deleteEpic() should not be called");
        calls.deleteEpic.add(id);
        deleteEpic.accept(id);
    }

    @Override
    public List<Subtask> getSubtasks() {
        assertNotNull(getSubtasks, "method getSubtasks() should not be called");
        calls.getSubtasks++;
        return getSubtasks.get();
    }

    @Override
    public void deleteSubtasks() {
        assertNotNull(deleteSubtasks, "method deleteSubtasks() should not be called");
        calls.deleteSubtasks++;
        deleteSubtasks.run();
    }

    @Override
    public Optional<Subtask> getSubtaskById(long id) {
        assertNotNull(getSubtaskById, "method getSubtaskById() should not be called");
        calls.getSubtaskById.add(id);
        return getSubtaskById.apply(id);
    }

    @Override
    public Subtask createSubtask(Subtask subtask) {
        assertNotNull(createSubtask, "method createSubtask() should not be called");
        calls.createSubtask.add(subtask);
        return createSubtask.apply(subtask);
    }

    @Override
    public Subtask updateSubtask(Subtask subtask) {
        assertNotNull(updateSubtask, "method updateSubtask() should not be called");
        calls.updateSubtask.add(subtask);
        return updateSubtask.apply(subtask);
    }

    @Override
    public void deleteSubtask(long id) {
        assertNotNull(deleteSubtask, "method deleteSubtask() should not be called");
        calls.deleteSubtask.add(id);
        deleteSubtask.accept(id);
    }

    @Override
    public List<Subtask> getEpicSubtasks(long epicId) {
        assertNotNull(getEpicSubtasks, "method getEpicSubtasks() should not be called");
        calls.getEpicSubtasks.add(epicId);
        return getEpicSubtasks.apply(epicId);
    }

    @Override
    public List<Task> getHistory() {
        assertNotNull(getHistory, "method getHistory() should not be called");
        calls.getHistory++;
        return getHistory.get();
    }

    @Override
    public List<Task> getPrioritizedTasks() {
        assertNotNull(getPrioritizedTasks, "method getPrioritizedTasks() should not be called");
        calls.getPrioritizedTasks++;
        return getPrioritizedTasks.get();
    }

    public Calls calls() {
        return calls;
    }

    public static class Calls {
        private int getTasks;
        private int deleteTasks;
        private List<Long> getTaskById;
        private List<Task> createTask;
        private List<Task> updateTask;
        private List<Long> deleteTask;
        private int getEpics;
        private int deleteEpics;
        private List<Long> getEpicById;
        private List<Epic> createEpic;
        private List<Epic> updateEpic;
        private List<Long> deleteEpic;
        private int getSubtasks;
        private int deleteSubtasks;
        private List<Long> getSubtaskById;
        private List<Subtask> createSubtask;
        private List<Subtask> updateSubtask;
        private List<Long> deleteSubtask;
        private List<Long> getEpicSubtasks;
        private int getHistory;
        private int getPrioritizedTasks;

        public int getTasks() {
            return getTasks;
        }

        public int deleteTasks() {
            return deleteTasks;
        }

        public List<Long> getTaskById() {
            return getTaskById == null ? Collections.emptyList() : new ArrayList<>(getTaskById);
        }

        public List<Task> createTask() {
            return createTask == null ? Collections.emptyList() : new ArrayList<>(createTask);
        }

        public List<Task> updateTask() {
            return updateTask == null ? Collections.emptyList() : new ArrayList<>(updateTask);
        }

        public List<Long> deleteTask() {
            return deleteTask == null ? Collections.emptyList() : new ArrayList<>(deleteTask);
        }

        public int getEpics() {
            return getEpics;
        }

        public int deleteEpics() {
            return deleteEpics;
        }

        public List<Long> getEpicById() {
            return getEpicById == null ? Collections.emptyList() : new ArrayList<>(getEpicById);
        }

        public List<Epic> createEpic() {
            return createEpic == null ? Collections.emptyList() : new ArrayList<>(createEpic);
        }

        public List<Epic> updateEpic() {
            return updateEpic == null ? Collections.emptyList() : new ArrayList<>(updateEpic);
        }

        public List<Long> deleteEpic() {
            return deleteEpic == null ? Collections.emptyList() : new ArrayList<>(deleteEpic);
        }

        public int getSubtasks() {
            return getSubtasks;
        }

        public int deleteSubtasks() {
            return deleteSubtasks;
        }

        public List<Long> getSubtaskById() {
            return getSubtaskById == null ? Collections.emptyList() : new ArrayList<>(getSubtaskById);
        }

        public List<Subtask> createSubtask() {
            return createSubtask == null ? Collections.emptyList() : new ArrayList<>(createSubtask);
        }

        public List<Subtask> updateSubtask() {
            return updateSubtask == null ? Collections.emptyList() : new ArrayList<>(updateSubtask);
        }

        public List<Long> deleteSubtask() {
            return deleteSubtask == null ? Collections.emptyList() : new ArrayList<>(deleteSubtask);
        }

        public List<Long> getEpicSubtasks() {
            return getEpicSubtasks == null ? Collections.emptyList() : new ArrayList<>(getEpicSubtasks);
        }

        public int getHistory() {
            return getHistory;
        }

        public int getPrioritizedTasks() {
            return getPrioritizedTasks;
        }
    }
}
