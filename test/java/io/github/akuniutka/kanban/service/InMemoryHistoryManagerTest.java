package io.github.akuniutka.kanban.service;

import io.github.akuniutka.kanban.model.Subtask;
import io.github.akuniutka.kanban.model.Task;
import org.junit.jupiter.api.Test;

import java.util.List;

import static io.github.akuniutka.kanban.TestModels.*;
import static org.junit.jupiter.api.Assertions.*;

class InMemoryHistoryManagerTest {
    private static final String WRONG_EXCEPTION_MESSAGE = "message for exception is wrong";
    private final HistoryManager manager;
    private final Task testTask;

    public InMemoryHistoryManagerTest() {
        this.manager = new InMemoryHistoryManager();
        this.testTask = fromTestTask().build();
    }

    @Test
    public void shouldCreateInMemoryHistoryManagerOfInterfaceType() {
        assertNotNull(manager, "history manager was not created");
    }

    @Test
    public void shouldNotAddTaskWhenNull() {
        final String expectedMessage = "cannot add null to visited tasks history";

        final Exception exception = assertThrows(NullPointerException.class, () -> manager.add(null));
        assertEquals(expectedMessage, exception.getMessage(), WRONG_EXCEPTION_MESSAGE);
    }

    @Test
    public void shouldNotAddTaskWhenIdNull() {
        final String expectedMessage = "cannot add task with null id to visited tasks history";
        final Task task = fromTestTask().withId(null).build();

        final Exception exception = assertThrows(NullPointerException.class, () -> manager.add(task));
        assertEquals(expectedMessage, exception.getMessage(), WRONG_EXCEPTION_MESSAGE);
    }

    @Test
    public void shouldAddTaskToHistory() {
        final List<Task> expectedHistory = List.of(fromTestTask().build());
        manager.add(testTask);
        final List<Task> history = manager.getHistory();

        assertListEquals(expectedHistory, history, "task saved with errors");
    }

    @Test
    public void shouldAddTaskToHistoryWhenFieldsNull() {
        final List<Task> expectedHistory = List.of(fromEmptyTask().withId(TEST_TASK_ID).build());
        final Task task = fromEmptyTask().withId(TEST_TASK_ID).build();

        manager.add(task);
        final List<Task> history = manager.getHistory();

        assertListEquals(expectedHistory, history, "task saved with errors");
    }

    @Test
    public void shouldAddTaskToHistoryWhenSubclass() {
        final List<Task> expectedHistory = List.of(fromTestSubtask().build());
        final Subtask subtask = fromTestSubtask().build();

        manager.add(subtask);
        final List<Task> history = manager.getHistory();

        assertListEquals(expectedHistory, history, "task saved with errors");
    }

    @Test
    public void shouldUpdateTaskInHistory() {
        final List<Task> expectedHistory = List.of(fromModifiedTask().build());
        final Task anotherTask = fromModifiedTask().build();

        manager.add(testTask);
        manager.add(anotherTask);
        final List<Task> history = manager.getHistory();

        assertListEquals(expectedHistory, history, "task saved with errors");
    }

    @Test
    public void shouldRetainOriginalTaskData() {
        final Task expectedTask = fromTestTask().build();

        manager.add(testTask);

        assertTaskEquals(expectedTask, testTask, "original task should not change");
    }

    @Test
    public void shouldKeepTasksInOrderTheyLastTimeVisited() {
        final List<Task> expectedHistory = List.of(
                fromModifiedTask().withId(ANOTHER_TEST_ID).build(),
                fromTestTask().build()
        );
        final Task anotherTask = fromModifiedTask().withId(ANOTHER_TEST_ID).build();

        manager.add(testTask);
        manager.add(anotherTask);
        manager.add(testTask);
        final List<Task> history = manager.getHistory();

        assertListEquals(expectedHistory, history, "incorrect history returned");
    }

    @Test
    public void shouldNotRemoveTaskFromHistoryWhenNotExist() {
        final List<Task> expectedHistory = List.of(fromTestTask().build());
        manager.add(testTask);

        manager.remove(ANOTHER_TEST_ID);
        final List<Task> history = manager.getHistory();

        assertListEquals(expectedHistory, history, "history should not change");
    }

    @Test
    public void shouldRemoveTaskFromHistory() {
        final List<Task> expectedHistory = List.of(fromTestTask().build());
        final Task anotherTask = fromModifiedTask().withId(ANOTHER_TEST_ID).build();
        manager.add(testTask);
        manager.add(anotherTask);

        manager.remove(ANOTHER_TEST_ID);
        List<Task> history = manager.getHistory();

        assertListEquals(expectedHistory, history, "incorrect history returned");
    }
}
