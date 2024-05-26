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
    private Task testTask;
    private Task modifiedTask;
    private List<Task> expectedHistory;

    public InMemoryHistoryManagerTest() {
        this.manager = new InMemoryHistoryManager();
        this.testTask = fromTestTask().build();
        this.modifiedTask = fromModifiedTask().build();
        this.expectedHistory = List.of(fromTestTask().build());
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
        testTask = fromTestTask().withId(null).build();

        final Exception exception = assertThrows(NullPointerException.class, () -> manager.add(testTask));
        assertEquals(expectedMessage, exception.getMessage(), WRONG_EXCEPTION_MESSAGE);
    }

    @Test
    public void shouldAddTaskToHistory() {
        manager.add(testTask);
        final List<Task> history = manager.getHistory();

        assertListEquals(expectedHistory, history, "task saved with errors");
    }

    @Test
    public void shouldAddTaskToHistoryWhenFieldsNull() {
        expectedHistory = List.of(fromEmptyTask().withId(TEST_TASK_ID).build());
        testTask = fromEmptyTask().withId(TEST_TASK_ID).build();

        manager.add(testTask);
        final List<Task> history = manager.getHistory();

        assertListEquals(expectedHistory, history, "task saved with errors");
    }

    @Test
    public void shouldAddTaskToHistoryWhenSubclass() {
        expectedHistory = List.of(fromTestSubtask().build());
        final Subtask subtask = fromTestSubtask().build();

        manager.add(subtask);
        final List<Task> history = manager.getHistory();

        assertListEquals(expectedHistory, history, "task saved with errors");
    }

    @Test
    public void shouldUpdateTaskInHistory() {
        expectedHistory = List.of(fromModifiedTask().build());

        manager.add(testTask);
        manager.add(modifiedTask);
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
        final Task taskA = fromTestTask().build();
        final Task taskB = fromModifiedTask().withId(ANOTHER_TEST_ID).build();
        expectedHistory = List.of(taskB, taskA);
        modifiedTask = fromModifiedTask().withId(ANOTHER_TEST_ID).build();

        manager.add(testTask);
        manager.add(modifiedTask);
        manager.add(testTask);
        final List<Task> history = manager.getHistory();

        assertListEquals(expectedHistory, history, "incorrect history returned");
    }

    @Test
    public void shouldNotRemoveTaskFromHistoryWhenNotExist() {
        manager.add(testTask);

        manager.remove(ANOTHER_TEST_ID);
        final List<Task> history = manager.getHistory();

        assertListEquals(expectedHistory, history, "history should not change");
    }

    @Test
    public void shouldRemoveTaskFromHistory() {
        modifiedTask = fromModifiedTask().withId(ANOTHER_TEST_ID).build();
        manager.add(testTask);
        manager.add(modifiedTask);

        manager.remove(ANOTHER_TEST_ID);
        List<Task> history = manager.getHistory();

        assertListEquals(expectedHistory, history, "incorrect history returned");
    }
}
