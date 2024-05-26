package io.github.akuniutka.kanban.service;

import io.github.akuniutka.kanban.exception.ManagerException;
import io.github.akuniutka.kanban.exception.TaskNotFoundException;
import io.github.akuniutka.kanban.model.Epic;
import io.github.akuniutka.kanban.model.Subtask;
import io.github.akuniutka.kanban.model.Task;
import org.junit.jupiter.api.Test;

import java.util.List;

import static io.github.akuniutka.kanban.TestModels.*;
import static org.junit.jupiter.api.Assertions.*;

abstract class AbstractTaskManagerTest {
    private static final String WRONG_EXCEPTION_MESSAGE = "message for exception is wrong";
    protected TaskManager manager;
    protected HistoryManager historyManager;
    private Task emptyTask;
    private Task testTask;
    private Task modifiedTask;
    private Epic emptyEpic;
    private Epic testEpic;
    private Epic modifiedEpic;
    private Subtask emptySubtask;
    private Subtask testSubtask;
    private Subtask modifiedSubtask;

    protected AbstractTaskManagerTest() {
        this.historyManager = new InMemoryHistoryManager();
        this.emptyTask = fromEmptyTask().build();
        this.testTask = fromTestTask().withId(null).build();
        this.modifiedTask = fromModifiedTask().withId(null).build();
        this.emptyEpic = fromEmptyEpic().build();
        this.testEpic = fromTestEpic().withId(null).build();
        this.modifiedEpic = fromModifiedEpic().withId(null).build();
    }

    @Test
    public void shouldCreateInMemoryTaskManagerOfInterfaceType() {
        assertNotNull(manager, "task manager was not created");
    }

    @Test
    public void shouldThrowWhenHistoryManagerIsNull() {
        final Exception exception = assertThrows(NullPointerException.class, () -> new InMemoryTaskManager(null));
        assertEquals("cannot start: history manager is null", exception.getMessage(), WRONG_EXCEPTION_MESSAGE);
    }

    @Test
    public void shouldNotGetTaskWhenNotExist() {
        final long taskId = -1L;
        final String expectedMessage = "no task with id=" + taskId;

        final Exception exception = assertThrows(TaskNotFoundException.class, () -> manager.getTask(taskId));
        assertEquals(expectedMessage, exception.getMessage(), WRONG_EXCEPTION_MESSAGE);
    }

    @Test
    public void shouldGetTask() {
        final long taskId = manager.addTask(testTask);
        final Task expectedTask = fromTestTask().withId(taskId).build();

        final Task savedTask = manager.getTask(taskId);

        assertTaskEquals(expectedTask, savedTask, "task saved with errors");
    }

    @Test
    public void shouldNotAddTaskWhenNull() {
        final String expectedMessage = "cannot add null to list of tasks";

        final Exception exception = assertThrows(NullPointerException.class, () -> manager.addTask(null));
        assertEquals(expectedMessage, exception.getMessage(), WRONG_EXCEPTION_MESSAGE);
    }

    @Test
    public void shouldNotAddTaskWhenIdExists() {
        final long taskId = manager.addTask(testTask);
        modifiedTask = fromModifiedTask().withId(taskId).build();

        final Exception exception = assertThrows(ManagerException.class, () -> manager.addTask(modifiedTask));
        assertEquals("duplicate id=" + taskId, exception.getMessage(), WRONG_EXCEPTION_MESSAGE);
    }

    @Test
    public void shouldNotAddTaskWhenDurationNullAndStartTimeNotNull() {
        testTask = fromTestTask().withDuration(null).build();

        final Exception exception = assertThrows(ManagerException.class, () -> manager.addTask(testTask));
        assertEquals("duration and start time must be either both set or both null", exception.getMessage(),
                WRONG_EXCEPTION_MESSAGE);
    }

    @Test
    public void shouldNotAddTaskWhenDurationNotNullAndStartTimeNull() {
        testTask = fromTestTask().withStartTime(null).build();

        final Exception exception = assertThrows(ManagerException.class, () -> manager.addTask(testTask));
        assertEquals("duration and start time must be either both set or both null", exception.getMessage(),
                WRONG_EXCEPTION_MESSAGE);
    }

    @Test
    public void shouldNotAddTaskWhenAnotherPrioritizedTaskCoversStartTime() {
        final long epicId = manager.addEpic(emptyEpic);
        testSubtask = fromTestSubtask().withId(null).withEpicId(epicId)
                .withStartTime(TEST_START_TIME.minusMinutes(15L)).build();
        manager.addSubtask(testSubtask);

        final Exception exception = assertThrows(ManagerException.class, () -> manager.addTask(testTask));
        assertEquals("conflict with another task for time slot", exception.getMessage(), WRONG_EXCEPTION_MESSAGE);
    }

    @Test
    public void shouldNotAddTaskWhenAnotherPrioritizedTaskCoversEndTime() {
        final long epicId = manager.addEpic(emptyEpic);
        testSubtask = fromTestSubtask().withId(null).withEpicId(epicId)
                .withStartTime(TEST_START_TIME.plusMinutes(15L)).build();
        manager.addSubtask(testSubtask);

        final Exception exception = assertThrows(ManagerException.class, () -> manager.addTask(testTask));
        assertEquals("conflict with another task for time slot", exception.getMessage(), WRONG_EXCEPTION_MESSAGE);
    }

    @Test
    public void shouldNotAddTaskWhenAnotherPrioritizedTaskCoversWholeInterval() {
        final long epicId = manager.addEpic(emptyEpic);
        testSubtask = fromTestSubtask().withId(null).withEpicId(epicId).withDuration(60L)
                .withStartTime(TEST_START_TIME.minusMinutes(15L)).build();
        manager.addSubtask(testSubtask);

        final Exception exception = assertThrows(ManagerException.class, () -> manager.addTask(testTask));
        assertEquals("conflict with another task for time slot", exception.getMessage(), WRONG_EXCEPTION_MESSAGE);
    }

    @Test
    public void shouldNotAddTaskWhenAnotherPrioritizedTaskWithinInterval() {
        final long epicId = manager.addEpic(emptyEpic);
        testSubtask = fromTestSubtask().withId(null).withEpicId(epicId).withDuration(20L)
                .withStartTime(TEST_START_TIME.plusMinutes(5L)).build();
        manager.addSubtask(testSubtask);

        final Exception exception = assertThrows(ManagerException.class, () -> manager.addTask(testTask));
        assertEquals("conflict with another task for time slot", exception.getMessage(), WRONG_EXCEPTION_MESSAGE);
    }

    @Test
    public void shouldNotAddTaskWhenAnotherPrioritizedTaskWithinIntervalLeftAligned() {
        final long epicId = manager.addEpic(emptyEpic);
        testSubtask = fromTestSubtask().withId(null).withEpicId(epicId).withDuration(20L).build();
        manager.addSubtask(testSubtask);

        final Exception exception = assertThrows(ManagerException.class, () -> manager.addTask(testTask));
        assertEquals("conflict with another task for time slot", exception.getMessage(), WRONG_EXCEPTION_MESSAGE);
    }

    @Test
    public void shouldNotAddTaskWhenAnotherPrioritizedTaskWithinIntervalRightAligned() {
        final long epicId = manager.addEpic(emptyEpic);
        testSubtask = fromTestSubtask().withId(null).withEpicId(epicId).withDuration(20L)
                .withStartTime(TEST_START_TIME.plusMinutes(10L)).build();
        manager.addSubtask(testSubtask);

        final Exception exception = assertThrows(ManagerException.class, () -> manager.addTask(testTask));
        assertEquals("conflict with another task for time slot", exception.getMessage(), WRONG_EXCEPTION_MESSAGE);
    }

    @Test
    public void shouldNotAddTaskWhenAnotherPrioritizedTaskWithSameInterval() {
        final long epicId = manager.addEpic(emptyEpic);
        testSubtask = fromTestSubtask().withId(null).withEpicId(epicId).build();
        manager.addSubtask(testSubtask);

        final Exception exception = assertThrows(ManagerException.class, () -> manager.addTask(testTask));
        assertEquals("conflict with another task for time slot", exception.getMessage(), WRONG_EXCEPTION_MESSAGE);
    }

    @Test
    public void shouldAddTaskToGetAndTasksAndPrioritizedWhenStartTimeNotNull() {
        final long taskId = manager.addTask(testTask);
        final Task savedTask = manager.getTask(taskId);
        final List<Task> tasks = manager.getTasks();
        final List<Task> prioritized = manager.getPrioritizedTasks();

        final Task expectedTask = fromTestTask().withId(taskId).build();
        final List<Task> expectedTasks = List.of(expectedTask);
        assertAll("task saved with errors",
                () -> assertTaskEquals(expectedTask, savedTask, "task saved with errors"),
                () -> assertListEquals(expectedTasks, tasks, "task saved with errors"),
                () -> assertListEquals(expectedTasks, prioritized, "task saved with errors")
        );
    }

    @Test
    public void shouldAddTaskToGetAndTasksAndPrioritizedWhenIdSetButNotExist() {
        testTask = fromTestTask().withId(ANOTHER_TEST_ID).build();

        final long taskId = manager.addTask(testTask);
        final Task savedTask = manager.getTask(taskId);
        final List<Task> tasks = manager.getTasks();
        final List<Task> prioritized = manager.getPrioritizedTasks();

        final Task expectedTask = fromTestTask().withId(taskId).build();
        final List<Task> expectedTasks = List.of(expectedTask);
        assertAll("task saved with errors",
                () -> assertNotEquals(ANOTHER_TEST_ID, taskId, "new task should have new id"),
                () -> assertTaskEquals(expectedTask, savedTask, "task saved with errors"),
                () -> assertListEquals(expectedTasks, tasks, "task saved with errors"),
                () -> assertListEquals(expectedTasks, prioritized, "task saved with errors")
        );
    }

    @Test
    public void shouldAddTaskToGetAndTasksNotPrioritizedWhenStartTimeNull() {
        testTask = fromTestTask().withId(null).withDuration(null).withStartTime(null).build();

        final long taskId = manager.addTask(testTask);
        final Task savedTask = manager.getTask(taskId);
        final List<Task> tasks = manager.getTasks();
        final List<Task> prioritized = manager.getPrioritizedTasks();

        final Task expectedTask = fromTestTask().withId(taskId).withDuration(null).withStartTime(null).build();
        final List<Task> expectedTasks = List.of(expectedTask);
        assertAll("task saved with errors",
                () -> assertTaskEquals(expectedTask, savedTask, "task saved with errors"),
                () -> assertListEquals(expectedTasks, tasks, "task saved with errors"),
                () -> assertTrue(prioritized.isEmpty(), "task saved with errors")
        );
    }

    @Test
    public void shouldAddTaskToGetAndTasksNotPrioritizedWhenFieldsNull() {
        final long taskId = manager.addTask(emptyTask);
        final Task savedTask = manager.getTask(taskId);
        final List<Task> tasks = manager.getTasks();
        final List<Task> prioritized = manager.getPrioritizedTasks();

        final Task expectedTask = fromEmptyTask().withId(taskId).build();
        final List<Task> expectedTasks = List.of(expectedTask);
        assertAll("task saved with errors",
                () -> assertTaskEquals(expectedTask, savedTask, "task saved with errors"),
                () -> assertListEquals(expectedTasks, tasks, "task saved with errors"),
                () -> assertTrue(prioritized.isEmpty(), "task saved with errors")
        );
    }

    @Test
    public void shouldAddTaskToGetAndTasksAndPrioritizedWhenExactlyBeforeAnotherPrioritizedTask() {
        final long epicId = manager.addEpic(emptyEpic);
        testSubtask = fromTestSubtask().withId(null).withEpicId(epicId)
                .withStartTime(TEST_START_TIME.plusMinutes(TEST_DURATION)).build();
        final long subtaskId = manager.addSubtask(testSubtask);

        final long taskId = manager.addTask(testTask);
        final Task savedTask = manager.getTask(taskId);
        final List<Task> tasks = manager.getTasks();
        final List<Task> prioritized = manager.getPrioritizedTasks();

        final Subtask expectedSubtask = fromTestSubtask().withId(subtaskId).withEpicId(epicId)
                .withStartTime(TEST_START_TIME.plusMinutes(TEST_DURATION)).build();
        final Task expectedTask = fromTestTask().withId(taskId).build();
        final List<Task> expectedTasks = List.of(expectedTask);
        final List<Task> expectedPrioritized = List.of(expectedTask, expectedSubtask);
        assertAll("task saved with errors",
                () -> assertTaskEquals(expectedTask, savedTask, "task saved with errors"),
                () -> assertListEquals(expectedTasks, tasks, "task saved with errors"),
                () -> assertListEquals(expectedPrioritized, prioritized, "task saved with errors")
        );
    }

    @Test
    public void shouldAddTaskToGetAndTasksAndPrioritizedWhenExactlyAfterAnotherPrioritizedTask() {
        final long epicId = manager.addEpic(emptyEpic);
        testSubtask = fromTestSubtask().withId(null).withEpicId(epicId)
                .withStartTime(TEST_START_TIME.minusMinutes(TEST_DURATION)).build();
        final long subtaskId = manager.addSubtask(testSubtask);

        final long taskId = manager.addTask(testTask);
        final Task savedTask = manager.getTask(taskId);
        final List<Task> tasks = manager.getTasks();
        final List<Task> prioritized = manager.getPrioritizedTasks();

        final Subtask expectedSubtask = fromTestSubtask().withId(subtaskId).withEpicId(epicId)
                .withStartTime(TEST_START_TIME.minusMinutes(TEST_DURATION)).build();
        final Task expectedTask = fromTestTask().withId(taskId).build();
        final List<Task> expectedTasks = List.of(expectedTask);
        final List<Task> expectedPrioritized = List.of(expectedSubtask, expectedTask);
        assertAll("task saved with errors",
                () -> assertTaskEquals(expectedTask, savedTask, "task saved with errors"),
                () -> assertListEquals(expectedTasks, tasks, "task saved with errors"),
                () -> assertListEquals(expectedPrioritized, prioritized, "task saved with errors")
        );
    }

    @Test
    public void shouldNotUpdateTaskWhenNull() {
        final String expectedMessage = "cannot apply null update to task";

        final Exception exception = assertThrows(NullPointerException.class, () -> manager.updateTask(null));
        assertEquals(expectedMessage, exception.getMessage(), WRONG_EXCEPTION_MESSAGE);
    }

    @Test
    public void shouldNotUpdateTaskWhenIdNull() {
        final String expectedMessage = "no task with id=null";

        final Exception exception = assertThrows(TaskNotFoundException.class, () -> manager.updateTask(testTask));
        assertEquals(expectedMessage, exception.getMessage(), WRONG_EXCEPTION_MESSAGE);
    }

    @Test
    public void shouldNotUpdateTaskWhenNotExist() {
        final long taskId = -1L;
        final String expectedMessage = "no task with id=" + taskId;
        testTask = fromTestTask().withId(taskId).build();

        final Exception exception = assertThrows(TaskNotFoundException.class, () -> manager.updateTask(testTask));
        assertEquals(expectedMessage, exception.getMessage(), WRONG_EXCEPTION_MESSAGE);
    }

    @Test
    public void shouldNotUpdateTaskWhenDurationNullAndStartTimeNotNull() {
        final long taskId = manager.addTask(testTask);
        modifiedTask = fromModifiedTask().withId(taskId).withDuration(null).build();

        final Exception exception = assertThrows(ManagerException.class, () -> manager.updateTask(modifiedTask));
        assertEquals("duration and start time must be either both set or both null", exception.getMessage(),
                WRONG_EXCEPTION_MESSAGE);
    }

    @Test
    public void shouldNotUpdateTaskWhenDurationNotNullAndStartTimeNull() {
        final long taskId = manager.addTask(testTask);
        modifiedTask = fromTestTask().withId(taskId).withStartTime(null).build();

        final Exception exception = assertThrows(ManagerException.class, () -> manager.updateTask(modifiedTask));
        assertEquals("duration and start time must be either both set or both null", exception.getMessage(),
                WRONG_EXCEPTION_MESSAGE);
    }

    @Test
    public void shouldNotUpdateTaskWhenAnotherPrioritizedTaskCoversStartTime() {
        final long taskId = manager.addTask(emptyTask);
        final long epicId = manager.addEpic(emptyEpic);
        testSubtask = fromTestSubtask().withId(null).withEpicId(epicId)
                .withStartTime(TEST_START_TIME.minusMinutes(15L)).build();
        manager.addSubtask(testSubtask);
        testTask = fromTestTask().withId(taskId).build();

        final Exception exception = assertThrows(ManagerException.class, () -> manager.updateTask(testTask));
        assertEquals("conflict with another task for time slot", exception.getMessage(), WRONG_EXCEPTION_MESSAGE);
    }

    @Test
    public void shouldNotUpdateTaskWhenAnotherPrioritizedTaskCoversEndTime() {
        final long taskId = manager.addTask(emptyTask);
        final long epicId = manager.addEpic(emptyEpic);
        testSubtask = fromTestSubtask().withId(null).withEpicId(epicId)
                .withStartTime(TEST_START_TIME.plusMinutes(15L)).build();
        manager.addSubtask(testSubtask);
        testTask = fromTestTask().withId(taskId).build();

        final Exception exception = assertThrows(ManagerException.class, () -> manager.updateTask(testTask));
        assertEquals("conflict with another task for time slot", exception.getMessage(), WRONG_EXCEPTION_MESSAGE);
    }

    @Test
    public void shouldNotUpdateTaskWhenAnotherPrioritizedTaskCoversWholeInterval() {
        final long taskId = manager.addTask(emptyTask);
        final long epicId = manager.addEpic(emptyEpic);
        testSubtask = fromTestSubtask().withId(null).withEpicId(epicId).withDuration(60L)
                .withStartTime(TEST_START_TIME.minusMinutes(15L)).build();
        manager.addSubtask(testSubtask);
        testTask = fromTestTask().withId(taskId).build();

        final Exception exception = assertThrows(ManagerException.class, () -> manager.updateTask(testTask));
        assertEquals("conflict with another task for time slot", exception.getMessage(), WRONG_EXCEPTION_MESSAGE);
    }

    @Test
    public void shouldNotUpdateTaskWhenAnotherPrioritizedTaskWithinInterval() {
        final long taskId = manager.addTask(emptyTask);
        final long epicId = manager.addEpic(emptyEpic);
        testSubtask = fromTestSubtask().withId(null).withEpicId(epicId).withDuration(20L)
                .withStartTime(TEST_START_TIME.plusMinutes(5L)).build();
        manager.addSubtask(testSubtask);
        testTask = fromTestTask().withId(taskId).build();

        final Exception exception = assertThrows(ManagerException.class, () -> manager.updateTask(testTask));
        assertEquals("conflict with another task for time slot", exception.getMessage(), WRONG_EXCEPTION_MESSAGE);
    }

    @Test
    public void shouldNotUpdateTaskWhenAnotherPrioritizedTaskWithinIntervalLeftAligned() {
        final long taskId = manager.addTask(emptyTask);
        final long epicId = manager.addEpic(emptyEpic);
        testSubtask = fromTestSubtask().withId(null).withEpicId(epicId).withDuration(20L).build();
        manager.addSubtask(testSubtask);
        testTask = fromTestTask().withId(taskId).build();

        final Exception exception = assertThrows(ManagerException.class, () -> manager.updateTask(testTask));
        assertEquals("conflict with another task for time slot", exception.getMessage(), WRONG_EXCEPTION_MESSAGE);
    }

    @Test
    public void shouldNotUpdateTaskWhenAnotherPrioritizedTaskWithinIntervalRightAligned() {
        final long taskId = manager.addTask(emptyTask);
        final long epicId = manager.addEpic(emptyEpic);
        testSubtask = fromTestSubtask().withId(null).withEpicId(epicId).withDuration(20L)
                .withStartTime(TEST_START_TIME.plusMinutes(10L)).build();
        manager.addSubtask(testSubtask);
        testTask = fromTestTask().withId(taskId).build();

        final Exception exception = assertThrows(ManagerException.class, () -> manager.updateTask(testTask));
        assertEquals("conflict with another task for time slot", exception.getMessage(), WRONG_EXCEPTION_MESSAGE);
    }

    @Test
    public void shouldNotUpdateTaskWhenAnotherPrioritizedTaskWithSameInterval() {
        final long taskId = manager.addTask(emptyTask);
        final long epicId = manager.addEpic(emptyEpic);
        testSubtask = fromTestSubtask().withId(null).withEpicId(epicId).build();
        manager.addSubtask(testSubtask);
        testTask = fromTestTask().withId(taskId).build();

        final Exception exception = assertThrows(ManagerException.class, () -> manager.updateTask(testTask));
        assertEquals("conflict with another task for time slot", exception.getMessage(), WRONG_EXCEPTION_MESSAGE);
    }

    @Test
    public void shouldUpdateTaskInGetAndTasksAndPrioritizedWhenStartTimeNotNull() {
        final long taskId = manager.addTask(testTask);
        modifiedTask = fromModifiedTask().withId(taskId).build();
        final Task expectedTask = fromModifiedTask().withId(taskId).build();
        final List<Task> expectedTasks = List.of(expectedTask);

        manager.updateTask(modifiedTask);
        final Task savedTask = manager.getTask(taskId);
        final List<Task> tasks = manager.getTasks();
        final List<Task> prioritized = manager.getPrioritizedTasks();

        assertAll("task saved with errors",
                () -> assertTaskEquals(expectedTask, savedTask, "task saved with errors"),
                () -> assertListEquals(expectedTasks, tasks, "task saved with errors"),
                () -> assertListEquals(expectedTasks, prioritized, "task saved with errors")
        );
    }

    @Test
    public void shouldUpdateTaskInGetAndTasksAndPrioritizedWhenPreviousVersionCoversStartTime() {
        emptyTask = fromEmptyTask().withDuration(TEST_DURATION).withStartTime(TEST_START_TIME.minusMinutes(15L))
                .build();
        final long taskId = manager.addTask(emptyTask);
        testTask = fromTestTask().withId(taskId).build();
        final Task expectedTask = fromTestTask().withId(taskId).build();
        final List<Task> expectedTasks = List.of(expectedTask);

        manager.updateTask(testTask);
        final Task savedTask = manager.getTask(taskId);
        final List<Task> tasks = manager.getTasks();
        final List<Task> prioritized = manager.getPrioritizedTasks();

        assertAll("task saved with errors",
                () -> assertTaskEquals(expectedTask, savedTask, "task saved with errors"),
                () -> assertListEquals(expectedTasks, tasks, "task saved with errors"),
                () -> assertListEquals(expectedTasks, prioritized, "task saved with errors")
        );
    }

    @Test
    public void shouldUpdateTaskInGetAndTasksAndPrioritizedWhenPreviousVersionCoversEndTime() {
        emptyTask = fromEmptyTask().withDuration(TEST_DURATION).withStartTime(TEST_START_TIME.plusMinutes(15L))
                .build();
        final long taskId = manager.addTask(emptyTask);
        testTask = fromTestTask().withId(taskId).build();
        final Task expectedTask = fromTestTask().withId(taskId).build();
        final List<Task> expectedTasks = List.of(expectedTask);

        manager.updateTask(testTask);
        final Task savedTask = manager.getTask(taskId);
        final List<Task> tasks = manager.getTasks();
        final List<Task> prioritized = manager.getPrioritizedTasks();

        assertAll("task saved with errors",
                () -> assertTaskEquals(expectedTask, savedTask, "task saved with errors"),
                () -> assertListEquals(expectedTasks, tasks, "task saved with errors"),
                () -> assertListEquals(expectedTasks, prioritized, "task saved with errors")
        );
    }

    @Test
    public void shouldUpdateTaskInGetAndTasksAndPrioritizedWhenPreviousVersionCoversWholeInterval() {
        emptyTask = fromEmptyTask().withDuration(60L).withStartTime(TEST_START_TIME.minusMinutes(15L)).build();
        final long taskId = manager.addTask(emptyTask);
        testTask = fromTestTask().withId(taskId).build();
        final Task expectedTask = fromTestTask().withId(taskId).build();
        final List<Task> expectedTasks = List.of(expectedTask);

        manager.updateTask(testTask);
        final Task savedTask = manager.getTask(taskId);
        final List<Task> tasks = manager.getTasks();
        final List<Task> prioritized = manager.getPrioritizedTasks();

        assertAll("task saved with errors",
                () -> assertTaskEquals(expectedTask, savedTask, "task saved with errors"),
                () -> assertListEquals(expectedTasks, tasks, "task saved with errors"),
                () -> assertListEquals(expectedTasks, prioritized, "task saved with errors")
        );
    }

    @Test
    public void shouldUpdateTaskInGetAndTasksAndPrioritizedWhenPreviousVersionWithinInterval() {
        emptyTask = fromEmptyTask().withDuration(20L).withStartTime(TEST_START_TIME.plusMinutes(5L)).build();
        final long taskId = manager.addTask(emptyTask);
        testTask = fromTestTask().withId(taskId).build();
        final Task expectedTask = fromTestTask().withId(taskId).build();
        final List<Task> expectedTasks = List.of(expectedTask);

        manager.updateTask(testTask);
        final Task savedTask = manager.getTask(taskId);
        final List<Task> tasks = manager.getTasks();
        final List<Task> prioritized = manager.getPrioritizedTasks();

        assertAll("task saved with errors",
                () -> assertTaskEquals(expectedTask, savedTask, "task saved with errors"),
                () -> assertListEquals(expectedTasks, tasks, "task saved with errors"),
                () -> assertListEquals(expectedTasks, prioritized, "task saved with errors")
        );
    }

    @Test
    public void shouldUpdateTaskInGetAndTasksAndPrioritizedWhenPreviousVersionWithinIntervalLeftAligned() {
        emptyTask = fromEmptyTask().withDuration(20L).withStartTime(TEST_START_TIME).build();
        final long taskId = manager.addTask(emptyTask);
        testTask = fromTestTask().withId(taskId).build();
        final Task expectedTask = fromTestTask().withId(taskId).build();
        final List<Task> expectedTasks = List.of(expectedTask);

        manager.updateTask(testTask);
        final Task savedTask = manager.getTask(taskId);
        final List<Task> tasks = manager.getTasks();
        final List<Task> prioritized = manager.getPrioritizedTasks();

        assertAll("task saved with errors",
                () -> assertTaskEquals(expectedTask, savedTask, "task saved with errors"),
                () -> assertListEquals(expectedTasks, tasks, "task saved with errors"),
                () -> assertListEquals(expectedTasks, prioritized, "task saved with errors")
        );
    }

    @Test
    public void shouldUpdateTaskInGetAndTasksAndPrioritizedWhenPreviousVersionWithinIntervalRightAligned() {
        emptyTask = fromEmptyTask().withDuration(20L).withStartTime(TEST_START_TIME.plusMinutes(10L)).build();
        final long taskId = manager.addTask(emptyTask);
        testTask = fromTestTask().withId(taskId).build();
        final Task expectedTask = fromTestTask().withId(taskId).build();
        final List<Task> expectedTasks = List.of(expectedTask);

        manager.updateTask(testTask);
        final Task savedTask = manager.getTask(taskId);
        final List<Task> tasks = manager.getTasks();
        final List<Task> prioritized = manager.getPrioritizedTasks();

        assertAll("task saved with errors",
                () -> assertTaskEquals(expectedTask, savedTask, "task saved with errors"),
                () -> assertListEquals(expectedTasks, tasks, "task saved with errors"),
                () -> assertListEquals(expectedTasks, prioritized, "task saved with errors")
        );
    }

    @Test
    public void shouldUpdateTaskInGetAndTasksAndPrioritizedWhenPreviousVersionWithSameInterval() {
        emptyTask = fromEmptyTask().withDuration(TEST_DURATION).withStartTime(TEST_START_TIME).build();
        final long taskId = manager.addTask(emptyTask);
        testTask = fromTestTask().withId(taskId).build();
        final Task expectedTask = fromTestTask().withId(taskId).build();
        final List<Task> expectedTasks = List.of(expectedTask);

        manager.updateTask(testTask);
        final Task savedTask = manager.getTask(taskId);
        final List<Task> tasks = manager.getTasks();
        final List<Task> prioritized = manager.getPrioritizedTasks();

        assertAll("task saved with errors",
                () -> assertTaskEquals(expectedTask, savedTask, "task saved with errors"),
                () -> assertListEquals(expectedTasks, tasks, "task saved with errors"),
                () -> assertListEquals(expectedTasks, prioritized, "task saved with errors")
        );
    }

    @Test
    public void shouldUpdateTaskInGetAndTasksAndPrioritizedWhenExactlyBeforeAnotherPrioritizedTask() {
        final long epicId = manager.addEpic(emptyEpic);
        testSubtask = fromTestSubtask().withId(null).withEpicId(epicId)
                .withStartTime(TEST_START_TIME.plusMinutes(TEST_DURATION)).build();
        final long subtaskId = manager.addSubtask(testSubtask);
        final long taskId = manager.addTask(emptyTask);
        testTask = fromTestTask().withId(taskId).build();
        final Subtask expectedSubtask = fromTestSubtask().withId(subtaskId).withEpicId(epicId)
                .withStartTime(TEST_START_TIME.plusMinutes(TEST_DURATION)).build();
        final Task expectedTask = fromTestTask().withId(taskId).build();
        final List<Task> expectedTasks = List.of(expectedTask);
        final List<Task> expectedPrioritized = List.of(expectedTask, expectedSubtask);

        manager.updateTask(testTask);
        final Task savedTask = manager.getTask(taskId);
        final List<Task> tasks = manager.getTasks();
        final List<Task> prioritized = manager.getPrioritizedTasks();

        assertAll("task saved with errors",
                () -> assertTaskEquals(expectedTask, savedTask, "task saved with errors"),
                () -> assertListEquals(expectedTasks, tasks, "task saved with errors"),
                () -> assertListEquals(expectedPrioritized, prioritized, "task saved with errors")
        );
    }

    @Test
    public void shouldUpdateTaskInGetAndTasksAndPrioritizedWhenExactlyAfterAnotherPrioritizedTask() {
        final long epicId = manager.addEpic(emptyEpic);
        testSubtask = fromTestSubtask().withId(null).withEpicId(epicId)
                .withStartTime(TEST_START_TIME.minusMinutes(TEST_DURATION)).build();
        final long subtaskId = manager.addSubtask(testSubtask);
        final long taskId = manager.addTask(emptyTask);
        testTask = fromTestTask().withId(taskId).build();
        final Subtask expectedSubtask = fromTestSubtask().withId(subtaskId).withEpicId(epicId)
                .withStartTime(TEST_START_TIME.minusMinutes(TEST_DURATION)).build();
        final Task expectedTask = fromTestTask().withId(taskId).build();
        final List<Task> expectedTasks = List.of(expectedTask);
        final List<Task> expectedPrioritized = List.of(expectedSubtask, expectedTask);

        manager.updateTask(testTask);
        final Task savedTask = manager.getTask(taskId);
        final List<Task> tasks = manager.getTasks();
        final List<Task> prioritized = manager.getPrioritizedTasks();

        assertAll("task saved with errors",
                () -> assertTaskEquals(expectedTask, savedTask, "task saved with errors"),
                () -> assertListEquals(expectedTasks, tasks, "task saved with errors"),
                () -> assertListEquals(expectedPrioritized, prioritized, "task saved with errors")
        );
    }

    @Test
    public void shouldUpdateTaskInGetAndTasksAndPrioritizedWhenStartTimeBecomesNull() {
        final long taskId = manager.addTask(testTask);
        modifiedTask = fromModifiedTask().withId(taskId).withDuration(null).withStartTime(null).build();
        final Task expectedTask = fromModifiedTask().withId(taskId).withDuration(null).withStartTime(null).build();
        final List<Task> expectedTasks = List.of(expectedTask);

        manager.updateTask(modifiedTask);
        final Task savedTask = manager.getTask(taskId);
        final List<Task> tasks = manager.getTasks();
        final List<Task> prioritized = manager.getPrioritizedTasks();

        assertAll("task saved with errors",
                () -> assertTaskEquals(expectedTask, savedTask, "task saved with errors"),
                () -> assertListEquals(expectedTasks, tasks, "task saved with errors"),
                () -> assertTrue(prioritized.isEmpty(), "task saved with errors")
        );
    }

    @Test
    public void shouldUpdateTaskInGetAndTasksAndPrioritizedWhenStartTimeWasNull() {
        testTask = fromTestTask().withId(null).withDuration(null).withStartTime(null).build();
        final long taskId = manager.addTask(testTask);
        modifiedTask = fromModifiedTask().withId(taskId).build();
        final Task expectedTask = fromModifiedTask().withId(taskId).build();
        final List<Task> expectedTasks = List.of(expectedTask);

        manager.updateTask(modifiedTask);
        final Task savedTask = manager.getTask(taskId);
        final List<Task> tasks = manager.getTasks();
        final List<Task> prioritized = manager.getPrioritizedTasks();

        assertAll("task saved with errors",
                () -> assertTaskEquals(expectedTask, savedTask, "task saved with errors"),
                () -> assertListEquals(expectedTasks, tasks, "task saved with errors"),
                () -> assertListEquals(expectedTasks, prioritized, "task saved with errors")
        );
    }

    @Test
    public void shouldUpdateTaskInGetAndTasksNotPrioritizedWhenStartTimeNull() {
        testTask = fromTestTask().withId(null).withDuration(null).withStartTime(null).build();
        final long taskId = manager.addTask(testTask);
        modifiedTask = fromModifiedTask().withId(taskId).withDuration(null).withStartTime(null).build();
        final Task expectedTask = fromModifiedTask().withId(taskId).withDuration(null).withStartTime(null).build();
        final List<Task> expectedTasks = List.of(expectedTask);

        manager.updateTask(modifiedTask);
        final Task savedTask = manager.getTask(taskId);
        final List<Task> tasks = manager.getTasks();
        final List<Task> prioritized = manager.getPrioritizedTasks();

        assertAll("task saved with errors",
                () -> assertTaskEquals(expectedTask, savedTask, "task saved with errors"),
                () -> assertListEquals(expectedTasks, tasks, "task saved with errors"),
                () -> assertTrue(prioritized.isEmpty(), "task saved with errors")
        );
    }

    @Test
    public void shouldUpdateTaskInGetAndTasksAndPrioritizedWhenFieldsBecomeNull() {
        final long taskId = manager.addTask(testTask);
        emptyTask = fromEmptyTask().withId(taskId).build();
        final Task expectedTask = fromEmptyTask().withId(taskId).build();
        final List<Task> expectedTasks = List.of(expectedTask);

        manager.updateTask(emptyTask);
        final Task savedTask = manager.getTask(taskId);
        final List<Task> tasks = manager.getTasks();
        final List<Task> prioritized = manager.getPrioritizedTasks();

        assertAll("task saved with errors",
                () -> assertTaskEquals(expectedTask, savedTask, "task saved with errors"),
                () -> assertListEquals(expectedTasks, tasks, "task saved with errors"),
                () -> assertTrue(prioritized.isEmpty(), "task saved with errors")
        );
    }

    @Test
    public void shouldNotRemoveTaskWhenNotExist() {
        final long taskId = -1L;
        final String expectedMessage = "no task with id=" + taskId;

        final Exception exception = assertThrows(TaskNotFoundException.class, () -> manager.removeTask(taskId));
        assertEquals(expectedMessage, exception.getMessage(), WRONG_EXCEPTION_MESSAGE);
    }

    @Test
    public void shouldRemoveTaskFromGetAndTasksAndPrioritized() {
        final long taskId = manager.addTask(testTask);

        manager.removeTask(taskId);
        final List<Task> tasks = manager.getTasks();
        final List<Task> prioritized = manager.getPrioritizedTasks();

        assertAll("task removed with errors",
                () -> assertThrows(TaskNotFoundException.class, () -> manager.getTask(taskId),
                        "task removed with errors"),
                () -> assertTrue(tasks.isEmpty(), "task removed with errors"),
                () -> assertTrue(prioritized.isEmpty(), "task removed with errors")
        );
    }

    @Test
    public void shouldNotGetEpicWhenNotExist() {
        final long epicId = -1L;
        final String expectedMessage = "no epic with id=" + epicId;

        final Exception exception = assertThrows(TaskNotFoundException.class, () -> manager.getEpic(epicId));
        assertEquals(expectedMessage, exception.getMessage(), WRONG_EXCEPTION_MESSAGE);
    }

    @Test
    public void shouldGetEpic() {
        final long epicId = manager.addEpic(testEpic);
        final Epic expectedEpic = fromTestEpic().withId(epicId).build();

        final Epic savedEpic = manager.getEpic(epicId);

        assertTaskEquals(expectedEpic, savedEpic, "epic saved with errors");
    }

    @Test
    public void shouldNotAddEpicWhenNull() {
        final String expectedMessage = "cannot add null to list of epics";

        final Exception exception = assertThrows(NullPointerException.class, () -> manager.addEpic(null));
        assertEquals(expectedMessage, exception.getMessage(), WRONG_EXCEPTION_MESSAGE);
    }

    @Test
    public void shouldNotAddEpicWhenIdExists() {
        final long epicId = manager.addEpic(testEpic);
        modifiedEpic = fromModifiedEpic().withId(epicId).build();

        final Exception exception = assertThrows(ManagerException.class, () -> manager.addEpic(modifiedEpic));
        assertEquals("duplicate id=" + epicId, exception.getMessage(), WRONG_EXCEPTION_MESSAGE);
    }

    @Test
    public void shouldAddEpicToGetAndEpics() {
        final long epicId = manager.addEpic(testEpic);
        final Epic savedEpic = manager.getEpic(epicId);
        final List<Epic> epics = manager.getEpics();

        final Epic expectedEpic = fromTestEpic().withId(epicId).build();
        final List<Epic> expectedEpics = List.of(expectedEpic);
        assertAll("epic saved with errors",
                () -> assertTaskEquals(expectedEpic, savedEpic, "epic saved with errors"),
                () -> assertListEquals(expectedEpics, epics, "epic saved with errors")
        );
    }

    @Test
    public void shouldAddEpicToGetAndEpicsWhenIdSetButNotExist() {
        testEpic = fromTestEpic().withId(ANOTHER_TEST_ID).build();

        final long epicId = manager.addEpic(testEpic);
        final Epic savedEpic = manager.getEpic(epicId);
        final List<Epic> epics = manager.getEpics();

        final Epic expectedEpic = fromTestEpic().withId(epicId).build();
        final List<Epic> expectedEpics = List.of(expectedEpic);
        assertAll("epic saved with errors",
                () -> assertNotEquals(ANOTHER_TEST_ID, epicId, "new epic should have new id"),
                () -> assertTaskEquals(expectedEpic, savedEpic, "epic saved with errors"),
                () -> assertListEquals(expectedEpics, epics, "epic saved with errors")
        );
    }

    @Test
    public void shouldAddEpicToGetAndEpicsWhenFieldsNull() {
        final long epicId = manager.addEpic(emptyEpic);
        final Epic savedEpic = manager.getEpic(epicId);
        final List<Epic> epics = manager.getEpics();

        final Epic expectedEpic = fromEmptyEpic().withId(epicId).build();
        final List<Epic> expectedEpics = List.of(expectedEpic);
        assertAll("epic saved with errors",
                () -> assertTaskEquals(expectedEpic, savedEpic, "epic saved with errors"),
                () -> assertListEquals(expectedEpics, epics, "epic saved with errors")
        );
    }

    @Test
    public void shouldNotUpdateEpicWhenNull() {
        final String expectedMessage = "cannot apply null update to epic";

        final Exception exception = assertThrows(NullPointerException.class, () -> manager.updateEpic(null));
        assertEquals(expectedMessage, exception.getMessage(), WRONG_EXCEPTION_MESSAGE);
    }

    @Test
    public void shouldNotUpdateEpicWhenIdNull() {
        final String expectedMessage = "no epic with id=null";

        final Exception exception = assertThrows(TaskNotFoundException.class, () -> manager.updateEpic(testEpic));
        assertEquals(expectedMessage, exception.getMessage(), WRONG_EXCEPTION_MESSAGE);
    }

    @Test
    public void shouldNotUpdateEpicWhenNotExist() {
        final long epicId = -1L;
        final String expectedMessage = "no epic with id=" + epicId;
        testEpic = fromTestEpic().withId(epicId).build();

        final Exception exception = assertThrows(TaskNotFoundException.class, () -> manager.updateEpic(testEpic));
        assertEquals(expectedMessage, exception.getMessage(), WRONG_EXCEPTION_MESSAGE);
    }

    @Test
    public void shouldUpdateEpicInGetAndEpics() {
        final long epicId = manager.addEpic(testEpic);
        modifiedEpic = fromModifiedEpic().withId(epicId).build();
        final Epic expectedEpic = fromModifiedEpic().withId(epicId).build();
        final List<Epic> expectedEpics = List.of(expectedEpic);

        manager.updateEpic(modifiedEpic);
        final Epic savedEpic = manager.getEpic(epicId);
        final List<Epic> epics = manager.getEpics();

        assertAll("epic saved with errors",
                () -> assertTaskEquals(expectedEpic, savedEpic, "epic saved with errors"),
                () -> assertListEquals(expectedEpics, epics, "epic saved with errors")
        );
    }

    @Test
    public void shouldUpdateEpicInGetAndEpicsWhenFieldsBecomeNull() {
        final long epicId = manager.addEpic(testEpic);
        emptyEpic = fromEmptyEpic().withId(epicId).build();
        final Epic expectedEpic = fromEmptyEpic().withId(epicId).build();
        final List<Epic> expectedEpics = List.of(expectedEpic);

        manager.updateEpic(emptyEpic);
        final Epic savedEpic = manager.getEpic(epicId);
        final List<Epic> epics = manager.getEpics();

        assertAll("epic saved with errors",
                () -> assertTaskEquals(expectedEpic, savedEpic, "epic saved with errors"),
                () -> assertListEquals(expectedEpics, epics, "epic saved with errors")
        );
    }

    @Test
    public void shouldRetainEpicSubtasksWhenUpdate() {
        final long epicId = manager.addEpic(testEpic);
        testSubtask = fromTestSubtask().withId(null).withEpicId(epicId).build();
        final long subtaskId = manager.addSubtask(testSubtask);
        testSubtask = fromTestSubtask().withId(subtaskId).withEpicId(epicId).build();
        modifiedEpic = fromModifiedEpic().withId(epicId).build();
        final Epic expectedEpic = fromModifiedEpic().withId(epicId)
                .withSubtasks(List.of(testSubtask)).build();

        manager.updateEpic(modifiedEpic);
        final Epic savedEpic = manager.getEpic(epicId);

        assertTaskEquals(expectedEpic, savedEpic, "epic saved with errors");
    }

    @Test
    public void shouldNotRemoveEpicWhenNotExist() {
        final long epicId = -1L;
        final String expectedMessage = "no epic with id=" + epicId;

        final Exception exception = assertThrows(TaskNotFoundException.class, () -> manager.removeEpic(epicId));
        assertEquals(expectedMessage, exception.getMessage(), WRONG_EXCEPTION_MESSAGE);
    }

    @Test
    public void shouldRemoveEpicFromGetAndEpics() {
        final long epicId = manager.addEpic(testEpic);

        manager.removeEpic(epicId);
        final List<Epic> epics = manager.getEpics();

        assertAll("epic removed with errors",
                () -> assertThrows(TaskNotFoundException.class, () -> manager.getEpic(epicId),
                        "epic removed with errors"),
                () -> assertTrue(epics.isEmpty(), "epic removed with errors")
        );
    }

    @Test
    public void shouldNotGetSubtaskWhenNotExist() {
        final long subtaskId = -1L;
        final String expectedMessage = "no subtask with id=" + subtaskId;

        final Exception exception = assertThrows(TaskNotFoundException.class, () -> manager.getSubtask(subtaskId));
        assertEquals(expectedMessage, exception.getMessage(), WRONG_EXCEPTION_MESSAGE);
    }

    @Test
    public void shouldGetSubtask() {
        final long epicId = manager.addEpic(testEpic);
        testSubtask = fromTestSubtask().withId(null).withEpicId(epicId).build();
        final long subtaskId = manager.addSubtask(testSubtask);
        final Subtask expectedSubtask = fromTestSubtask().withId(subtaskId).withEpicId(epicId).build();

        final Subtask savedSubtask = manager.getSubtask(subtaskId);

        assertTaskEquals(expectedSubtask, savedSubtask, "subtask saved with errors");
    }

    @Test
    public void shouldNotAddSubtaskWhenNull() {
        final String expectedMessage = "cannot add null to list of subtasks";

        final Exception exception = assertThrows(NullPointerException.class, () -> manager.addSubtask(null));
        assertEquals(expectedMessage, exception.getMessage(), WRONG_EXCEPTION_MESSAGE);
    }

    @Test
    public void shouldNotAddSubtaskWhenIdExists() {
        final long epicId = manager.addEpic(testEpic);
        testSubtask = fromTestSubtask().withId(null).withEpicId(epicId).build();
        final long subtaskId = manager.addSubtask(testSubtask);
        final long anotherEpicId = manager.addEpic(modifiedEpic);
        modifiedSubtask = fromModifiedSubtask().withId(subtaskId).withEpicId(anotherEpicId).build();

        final Exception exception = assertThrows(ManagerException.class, () -> manager.addSubtask(modifiedSubtask));
        assertEquals("duplicate id=" + subtaskId, exception.getMessage(), WRONG_EXCEPTION_MESSAGE);
    }

    @Test
    public void shouldNotAddSubtaskToNull() {
        final String expectedMessage = "no epic with id=null";
        testSubtask = fromTestSubtask().withId(null).withEpicId(null).build();

        final Exception exception = assertThrows(TaskNotFoundException.class, () -> manager.addSubtask(testSubtask));
        assertEquals(expectedMessage, exception.getMessage(), WRONG_EXCEPTION_MESSAGE);
    }

    @Test
    public void shouldNotAddSubtaskToNotExistingEpic() {
        final long epicId = -1L;
        final String expectedMessage = "no epic with id=" + epicId;
        testSubtask = fromTestSubtask().withId(null).withEpicId(epicId).build();

        final Exception exception = assertThrows(TaskNotFoundException.class, () -> manager.addSubtask(testSubtask));
        assertEquals(expectedMessage, exception.getMessage(), WRONG_EXCEPTION_MESSAGE);
    }

    @Test
    public void shouldNotAddSubtaskToTask() {
        final long taskId = manager.addTask(testTask);
        final String expectedMessage = "no epic with id=" + taskId;
        modifiedSubtask = fromModifiedSubtask().withId(null).withEpicId(taskId).build();

        final Exception exception = assertThrows(TaskNotFoundException.class,
                () -> manager.addSubtask(modifiedSubtask));
        assertEquals(expectedMessage, exception.getMessage(), WRONG_EXCEPTION_MESSAGE);
    }

    @Test
    public void shouldNotAddSubtaskToSubtask() {
        final long epicId = manager.addEpic(testEpic);
        testSubtask = fromTestSubtask().withId(null).withEpicId(epicId).build();
        final long subtaskId = manager.addSubtask(testSubtask);
        final String expectedMessage = "no epic with id=" + subtaskId;
        modifiedSubtask = fromModifiedSubtask().withId(null).withEpicId(subtaskId).build();

        final Exception exception = assertThrows(TaskNotFoundException.class,
                () -> manager.addSubtask(modifiedSubtask));
        assertEquals(expectedMessage, exception.getMessage(), WRONG_EXCEPTION_MESSAGE);
    }

    @Test
    public void shouldNotAddSubtaskWhenDurationNullAndStartTimeNotNull() {
        final long epicId = manager.addEpic(testEpic);
        testSubtask = fromTestSubtask().withId(null).withEpicId(epicId).withDuration(null).build();

        final Exception exception = assertThrows(ManagerException.class, () -> manager.addSubtask(testSubtask));
        assertEquals("duration and start time must be either both set or both null", exception.getMessage(),
                WRONG_EXCEPTION_MESSAGE);
    }

    @Test
    public void shouldNotAddSubtaskWhenDurationNotNullAndStartTimeNull() {
        final long epicId = manager.addEpic(testEpic);
        testSubtask = fromTestSubtask().withId(null).withEpicId(epicId).withStartTime(null).build();

        final Exception exception = assertThrows(ManagerException.class, () -> manager.addSubtask(testSubtask));
        assertEquals("duration and start time must be either both set or both null", exception.getMessage(),
                WRONG_EXCEPTION_MESSAGE);
    }

    @Test
    public void shouldNotAddSubtaskWhenAnotherPrioritizedTaskCoversStartTime() {
        testTask = fromTestTask().withId(null).withStartTime(TEST_START_TIME.minusMinutes(15L)).build();
        manager.addTask(testTask);
        final long epicId = manager.addEpic(emptyEpic);
        testSubtask = fromTestSubtask().withId(null).withEpicId(epicId).build();

        final Exception exception = assertThrows(ManagerException.class, () -> manager.addSubtask(testSubtask));
        assertEquals("conflict with another task for time slot", exception.getMessage(), WRONG_EXCEPTION_MESSAGE);
    }

    @Test
    public void shouldNotAddSubtaskWhenAnotherPrioritizedTaskCoversEndTime() {
        testTask = fromTestTask().withId(null).withStartTime(TEST_START_TIME.plusMinutes(15L)).build();
        manager.addTask(testTask);
        final long epicId = manager.addEpic(emptyEpic);
        testSubtask = fromTestSubtask().withId(null).withEpicId(epicId).build();

        final Exception exception = assertThrows(ManagerException.class, () -> manager.addSubtask(testSubtask));
        assertEquals("conflict with another task for time slot", exception.getMessage(), WRONG_EXCEPTION_MESSAGE);
    }

    @Test
    public void shouldNotAddSubtaskWhenAnotherPrioritizedTaskCoversWholeInterval() {
        testTask = fromTestTask().withId(null).withDuration(60L)
                .withStartTime(TEST_START_TIME.minusMinutes(15L)).build();
        manager.addTask(testTask);
        final long epicId = manager.addEpic(emptyEpic);
        testSubtask = fromTestSubtask().withId(null).withEpicId(epicId).build();

        final Exception exception = assertThrows(ManagerException.class, () -> manager.addSubtask(testSubtask));
        assertEquals("conflict with another task for time slot", exception.getMessage(), WRONG_EXCEPTION_MESSAGE);
    }

    @Test
    public void shouldNotAddSubtaskWhenAnotherPrioritizedTaskWithinInterval() {
        testTask = fromTestTask().withId(null).withDuration(20L).withStartTime(TEST_START_TIME.plusMinutes(5L))
                .build();
        manager.addTask(testTask);
        final long epicId = manager.addEpic(emptyEpic);
        testSubtask = fromTestSubtask().withId(null).withEpicId(epicId).build();

        final Exception exception = assertThrows(ManagerException.class, () -> manager.addSubtask(testSubtask));
        assertEquals("conflict with another task for time slot", exception.getMessage(), WRONG_EXCEPTION_MESSAGE);
    }

    @Test
    public void shouldNotAddSubtaskWhenAnotherPrioritizedTaskWithinIntervalLeftAligned() {
        testTask = fromTestTask().withId(null).withDuration(20L).build();
        manager.addTask(testTask);
        final long epicId = manager.addEpic(emptyEpic);
        testSubtask = fromTestSubtask().withId(null).withEpicId(epicId).build();

        final Exception exception = assertThrows(ManagerException.class, () -> manager.addSubtask(testSubtask));
        assertEquals("conflict with another task for time slot", exception.getMessage(), WRONG_EXCEPTION_MESSAGE);
    }

    @Test
    public void shouldNotAddSubtaskWhenAnotherPrioritizedTaskWithinIntervalRightAligned() {
        testTask = fromTestTask().withId(null).withDuration(20L).withStartTime(TEST_START_TIME.plusMinutes(10L))
                .build();
        manager.addTask(testTask);
        final long epicId = manager.addEpic(emptyEpic);
        testSubtask = fromTestSubtask().withId(null).withEpicId(epicId).build();

        final Exception exception = assertThrows(ManagerException.class, () -> manager.addSubtask(testSubtask));
        assertEquals("conflict with another task for time slot", exception.getMessage(), WRONG_EXCEPTION_MESSAGE);
    }

    @Test
    public void shouldNotAddSubtaskWhenAnotherPrioritizedTaskWithSameInterval() {
        manager.addTask(testTask);
        final long epicId = manager.addEpic(emptyEpic);
        testSubtask = fromTestSubtask().withId(null).withEpicId(epicId).build();

        final Exception exception = assertThrows(ManagerException.class, () -> manager.addSubtask(testSubtask));
        assertEquals("conflict with another task for time slot", exception.getMessage(), WRONG_EXCEPTION_MESSAGE);
    }

    @Test
    public void shouldAddSubtaskToGetAndEpicAndSubtasksAndPrioritizedWhenStartTimeNotNull() {
        final long epicId = manager.addEpic(testEpic);
        testSubtask = fromTestSubtask().withId(null).withEpicId(epicId).build();

        final long subtaskId = manager.addSubtask(testSubtask);
        final Subtask savedSubtask = manager.getSubtask(subtaskId);
        final List<Subtask> epicSubtasks = manager.getEpicSubtasks(epicId);
        final List<Subtask> subtasks = manager.getSubtasks();
        final List<Task> prioritized = manager.getPrioritizedTasks();

        final Subtask expectedSubtask = fromTestSubtask().withId(subtaskId).withEpicId(epicId).build();
        final List<Subtask> expectedSubtasks = List.of(expectedSubtask);
        assertAll("subtask saved with errors",
                () -> assertTaskEquals(expectedSubtask, savedSubtask, "subtask saved with errors"),
                () -> assertListEquals(expectedSubtasks, epicSubtasks, "subtask saved with errors"),
                () -> assertListEquals(expectedSubtasks, subtasks, "subtask saved with errors"),
                () -> assertListEquals(expectedSubtasks, prioritized, "subtask saved with errors")
        );
    }

    @Test
    public void shouldAddSubtaskToGetAndEpicAndSubtasksAndPrioritizedWhenIdSetButNotExist() {
        final long epicId = manager.addEpic(testEpic);
        testSubtask = fromTestSubtask().withId(ANOTHER_TEST_ID).withEpicId(epicId).build();

        final long subtaskId = manager.addSubtask(testSubtask);
        final Subtask savedSubtask = manager.getSubtask(subtaskId);
        final List<Subtask> epicSubtasks = manager.getEpicSubtasks(epicId);
        final List<Subtask> subtasks = manager.getSubtasks();
        final List<Task> prioritized = manager.getPrioritizedTasks();

        final Subtask expectedSubtask = fromTestSubtask().withId(subtaskId).withEpicId(epicId).build();
        final List<Subtask> expectedSubtasks = List.of(expectedSubtask);
        assertAll("subtask saved with errors",
                () -> assertNotEquals(ANOTHER_TEST_ID, subtaskId, "new subtask should have new id"),
                () -> assertTaskEquals(expectedSubtask, savedSubtask, "subtask saved with errors"),
                () -> assertListEquals(expectedSubtasks, epicSubtasks, "subtask saved with errors"),
                () -> assertListEquals(expectedSubtasks, subtasks, "subtask saved with errors"),
                () -> assertListEquals(expectedSubtasks, prioritized, "subtask saved with errors")
        );
    }

    @Test
    public void shouldAddSubtaskToGetAndEpicAndSubtasksNotPrioritizedWhenStartTimeNull() {
        final long epicId = manager.addEpic(testEpic);
        testSubtask = fromTestSubtask().withId(null).withEpicId(epicId).withDuration(null).withStartTime(null).build();

        final long subtaskId = manager.addSubtask(testSubtask);
        final Subtask savedSubtask = manager.getSubtask(subtaskId);
        final List<Subtask> epicSubtasks = manager.getEpicSubtasks(epicId);
        final List<Subtask> subtasks = manager.getSubtasks();
        final List<Task> prioritized = manager.getPrioritizedTasks();

        final Subtask expectedSubtask = fromTestSubtask().withId(subtaskId).withEpicId(epicId).withDuration(null)
                .withStartTime(null).build();
        final List<Subtask> expectedSubtasks = List.of(expectedSubtask);
        assertAll("subtask saved with errors",
                () -> assertTaskEquals(expectedSubtask, savedSubtask, "subtask saved with errors"),
                () -> assertListEquals(expectedSubtasks, epicSubtasks, "subtask saved with errors"),
                () -> assertListEquals(expectedSubtasks, subtasks, "subtask saved with errors"),
                () -> assertTrue(prioritized.isEmpty(), "subtask saved with errors")
        );
    }

    @Test
    public void shouldAddSubtaskToGetAndEpicAndSubtasksNotPrioritizedWhenFieldsNull() {
        final long epicId = manager.addEpic(testEpic);
        emptySubtask = fromEmptySubtask().withId(null).withEpicId(epicId).build();

        final long subtaskId = manager.addSubtask(emptySubtask);
        final Subtask savedSubtask = manager.getSubtask(subtaskId);
        final List<Subtask> epicSubtasks = manager.getEpicSubtasks(epicId);
        final List<Subtask> subtasks = manager.getSubtasks();
        final List<Task> prioritized = manager.getPrioritizedTasks();

        final Subtask expectedSubtask = fromEmptySubtask().withId(subtaskId).withEpicId(epicId).build();
        final List<Subtask> expectedSubtasks = List.of(expectedSubtask);
        assertAll("subtask saved with errors",
                () -> assertTaskEquals(expectedSubtask, savedSubtask, "subtask saved with errors"),
                () -> assertListEquals(expectedSubtasks, epicSubtasks, "subtask saved with errors"),
                () -> assertListEquals(expectedSubtasks, subtasks, "subtask saved with errors"),
                () -> assertTrue(prioritized.isEmpty(), "subtask saved with errors")
        );
    }

    @Test
    public void shouldAddSubtaskToGetAndEpicAndSubtasksAndPrioritizedWhenExactlyBeforeAnotherPrioritizedTask() {
        testTask = fromTestTask().withId(null).withStartTime(TEST_START_TIME.plusMinutes(TEST_DURATION)).build();
        final long taskId = manager.addTask(testTask);
        final long epicId = manager.addEpic(emptyEpic);
        testSubtask = fromTestSubtask().withId(null).withEpicId(epicId).build();

        final long subtaskId = manager.addSubtask(testSubtask);
        final Subtask savedSubtask = manager.getSubtask(subtaskId);
        final List<Subtask> epicSubtasks = manager.getEpicSubtasks(epicId);
        final List<Subtask> subtasks = manager.getSubtasks();
        final List<Task> prioritized = manager.getPrioritizedTasks();

        final Task expectedTask = fromTestTask().withId(taskId)
                .withStartTime(TEST_START_TIME.plusMinutes(TEST_DURATION)).build();
        final Subtask expectedSubtask = fromTestSubtask().withId(subtaskId).withEpicId(epicId).build();
        final List<Subtask> expectedSubtasks = List.of(expectedSubtask);
        final List<Task> expectedPrioritized = List.of(expectedSubtask, expectedTask);
        assertAll("subtask saved with errors",
                () -> assertTaskEquals(expectedSubtask, savedSubtask, "subtask saved with errors"),
                () -> assertListEquals(expectedSubtasks, epicSubtasks, "subtask saved with errors"),
                () -> assertListEquals(expectedSubtasks, subtasks, "subtask saved with errors"),
                () -> assertListEquals(expectedPrioritized, prioritized, "subtask saved with errors")
        );
    }

    @Test
    public void shouldAddSubtaskToGetAndEpicAndSubtasksAndPrioritizedWhenExactlyAfterAnotherPrioritizedTask() {
        testTask = fromTestTask().withId(null).withStartTime(TEST_START_TIME.minusMinutes(TEST_DURATION))
                .build();
        final long taskId = manager.addTask(testTask);
        final long epicId = manager.addEpic(emptyEpic);
        testSubtask = fromTestSubtask().withId(null).withEpicId(epicId).build();

        final long subtaskId = manager.addSubtask(testSubtask);
        final Subtask savedSubtask = manager.getSubtask(subtaskId);
        final List<Subtask> epicSubtasks = manager.getEpicSubtasks(epicId);
        final List<Subtask> subtasks = manager.getSubtasks();
        final List<Task> prioritized = manager.getPrioritizedTasks();

        final Task expectedTask = fromTestTask().withId(taskId)
                .withStartTime(TEST_START_TIME.minusMinutes(TEST_DURATION)).build();
        final Subtask expectedSubtask = fromTestSubtask().withId(subtaskId).withEpicId(epicId).build();
        final List<Subtask> expectedSubtasks = List.of(expectedSubtask);
        final List<Task> expectedPrioritized = List.of(expectedTask, expectedSubtask);
        assertAll("subtask saved with errors",
                () -> assertTaskEquals(expectedSubtask, savedSubtask, "subtask saved with errors"),
                () -> assertListEquals(expectedSubtasks, epicSubtasks, "subtask saved with errors"),
                () -> assertListEquals(expectedSubtasks, subtasks, "subtask saved with errors"),
                () -> assertListEquals(expectedPrioritized, prioritized, "subtask saved with errors")
        );
    }

    @Test
    public void shouldNotUpdateSubtaskWhenNull() {
        final String expectedMessage = "cannot apply null update to subtask";

        final Exception exception = assertThrows(NullPointerException.class, () -> manager.updateSubtask(null));
        assertEquals(expectedMessage, exception.getMessage(), WRONG_EXCEPTION_MESSAGE);
    }

    @Test
    public void shouldNotUpdateSubtaskWhenIdNull() {
        final String expectedMessage = "no subtask with id=null";
        final long epicId = manager.addEpic(testEpic);
        testSubtask = fromTestSubtask().withId(null).withEpicId(epicId).build();

        final Exception exception = assertThrows(TaskNotFoundException.class, () -> manager.updateSubtask(testSubtask));
        assertEquals(expectedMessage, exception.getMessage(), WRONG_EXCEPTION_MESSAGE);
    }

    @Test
    public void shouldNotUpdateSubtaskWhenNotExist() {
        final long subtaskId = -1L;
        final String expectedMessage = "no subtask with id=" + subtaskId;
        final long epicId = manager.addEpic(testEpic);
        testSubtask = fromTestSubtask().withId(subtaskId).withEpicId(epicId).build();

        final Exception exception = assertThrows(TaskNotFoundException.class, () -> manager.updateSubtask(testSubtask));
        assertEquals(expectedMessage, exception.getMessage(), WRONG_EXCEPTION_MESSAGE);
    }

    @Test
    public void shouldNotUpdateSubtaskWhenDurationNullAndStartTimeNotNull() {
        final long epicId = manager.addEpic(testEpic);
        testSubtask = fromTestSubtask().withId(null).withEpicId(epicId).build();
        final long subtaskId = manager.addSubtask(testSubtask);
        modifiedSubtask = fromModifiedSubtask().withId(subtaskId).withDuration(null).build();

        final Exception exception = assertThrows(ManagerException.class,
                () -> manager.updateSubtask(modifiedSubtask));
        assertEquals("duration and start time must be either both set or both null", exception.getMessage(),
                WRONG_EXCEPTION_MESSAGE);
    }

    @Test
    public void shouldNotUpdateSubtaskWhenDurationNotNullAndStartTimeNull() {
        final long epicId = manager.addEpic(testEpic);
        testSubtask = fromTestSubtask().withId(null).withEpicId(epicId).build();
        final long subtaskId = manager.addSubtask(testSubtask);
        modifiedSubtask = fromModifiedSubtask().withId(subtaskId).withStartTime(null).build();

        final Exception exception = assertThrows(ManagerException.class,
                () -> manager.updateSubtask(modifiedSubtask));
        assertEquals("duration and start time must be either both set or both null", exception.getMessage(),
                WRONG_EXCEPTION_MESSAGE);
    }

    @Test
    public void shouldNotUpdateSubtaskWhenAnotherPrioritizedTaskCoversStartTime() {
        testTask = fromTestTask().withId(null).withStartTime(TEST_START_TIME.minusMinutes(15L)).build();
        manager.addTask(testTask);
        final long epicId = manager.addEpic(emptyEpic);
        emptySubtask = fromEmptySubtask().withEpicId(epicId).build();
        final long subtaskId = manager.addSubtask(emptySubtask);
        testSubtask = fromTestSubtask().withId(subtaskId).build();

        final Exception exception = assertThrows(ManagerException.class, () -> manager.updateSubtask(testSubtask));
        assertEquals("conflict with another task for time slot", exception.getMessage(), WRONG_EXCEPTION_MESSAGE);
    }

    @Test
    public void shouldNotUpdateSubtaskWhenAnotherPrioritizedTaskCoversEndTime() {
        testTask = fromTestTask().withId(null).withStartTime(TEST_START_TIME.plusMinutes(15L)).build();
        manager.addTask(testTask);
        final long epicId = manager.addEpic(emptyEpic);
        emptySubtask = fromEmptySubtask().withEpicId(epicId).build();
        final long subtaskId = manager.addSubtask(emptySubtask);
        testSubtask = fromTestSubtask().withId(subtaskId).build();

        final Exception exception = assertThrows(ManagerException.class, () -> manager.updateSubtask(testSubtask));
        assertEquals("conflict with another task for time slot", exception.getMessage(), WRONG_EXCEPTION_MESSAGE);
    }

    @Test
    public void shouldNotUpdateSubtaskWhenAnotherPrioritizedTaskCoversWholeInterval() {
        testTask = fromTestTask().withId(null).withDuration(60L).withStartTime(TEST_START_TIME.minusMinutes(15L))
                .build();
        manager.addTask(testTask);
        final long epicId = manager.addEpic(emptyEpic);
        emptySubtask = fromEmptySubtask().withEpicId(epicId).build();
        final long subtaskId = manager.addSubtask(emptySubtask);
        testSubtask = fromTestSubtask().withId(subtaskId).build();

        final Exception exception = assertThrows(ManagerException.class, () -> manager.updateSubtask(testSubtask));
        assertEquals("conflict with another task for time slot", exception.getMessage(), WRONG_EXCEPTION_MESSAGE);
    }

    @Test
    public void shouldNotUpdateSubtaskWhenAnotherPrioritizedTaskWithinInterval() {
        testTask = fromTestTask().withId(null).withDuration(20L).withStartTime(TEST_START_TIME.plusMinutes(5L))
                .build();
        manager.addTask(testTask);
        final long epicId = manager.addEpic(emptyEpic);
        emptySubtask = fromEmptySubtask().withEpicId(epicId).build();
        final long subtaskId = manager.addSubtask(emptySubtask);
        testSubtask = fromTestSubtask().withId(subtaskId).build();

        final Exception exception = assertThrows(ManagerException.class, () -> manager.updateSubtask(testSubtask));
        assertEquals("conflict with another task for time slot", exception.getMessage(), WRONG_EXCEPTION_MESSAGE);
    }

    @Test
    public void shouldNotUpdateSubtaskWhenAnotherPrioritizedTaskWithinIntervalLeftAligned() {
        testTask = fromTestTask().withId(null).withDuration(20L).build();
        manager.addTask(testTask);
        final long epicId = manager.addEpic(emptyEpic);
        emptySubtask = fromEmptySubtask().withEpicId(epicId).build();
        final long subtaskId = manager.addSubtask(emptySubtask);
        testSubtask = fromTestSubtask().withId(subtaskId).build();

        final Exception exception = assertThrows(ManagerException.class, () -> manager.updateSubtask(testSubtask));
        assertEquals("conflict with another task for time slot", exception.getMessage(), WRONG_EXCEPTION_MESSAGE);
    }

    @Test
    public void shouldNotUpdateSubtaskWhenAnotherPrioritizedTaskWithinIntervalRightAligned() {
        testTask = fromTestTask().withId(null).withDuration(20L).withStartTime(TEST_START_TIME.plusMinutes(10L))
                .build();
        manager.addTask(testTask);
        final long epicId = manager.addEpic(emptyEpic);
        emptySubtask = fromEmptySubtask().withEpicId(epicId).build();
        final long subtaskId = manager.addSubtask(emptySubtask);
        testSubtask = fromTestSubtask().withId(subtaskId).build();

        final Exception exception = assertThrows(ManagerException.class, () -> manager.updateSubtask(testSubtask));
        assertEquals("conflict with another task for time slot", exception.getMessage(), WRONG_EXCEPTION_MESSAGE);
    }

    @Test
    public void shouldNotUpdateSubtaskWhenAnotherPrioritizedTaskWithSameInterval() {
        manager.addTask(testTask);
        final long epicId = manager.addEpic(emptyEpic);
        emptySubtask = fromEmptySubtask().withEpicId(epicId).build();
        final long subtaskId = manager.addSubtask(emptySubtask);
        testSubtask = fromTestSubtask().withId(subtaskId).build();

        final Exception exception = assertThrows(ManagerException.class, () -> manager.updateSubtask(testSubtask));
        assertEquals("conflict with another task for time slot", exception.getMessage(), WRONG_EXCEPTION_MESSAGE);
    }

    @Test
    public void shouldUpdateSubtaskInGetAndEpicAndSubtasksAndPrioritizedWhenStartTimeNotNull() {
        final long epicId = manager.addEpic(testEpic);
        testSubtask = fromTestSubtask().withId(null).withEpicId(epicId).build();
        final long subtaskId = manager.addSubtask(testSubtask);
        modifiedSubtask = fromModifiedSubtask().withId(subtaskId).build();
        final Subtask expectedSubtask = fromModifiedSubtask().withId(subtaskId).withEpicId(epicId).build();
        final List<Subtask> expectedSubtasks = List.of(expectedSubtask);

        manager.updateSubtask(modifiedSubtask);
        final Subtask savedSubtask = manager.getSubtask(subtaskId);
        final List<Subtask> epicSubtasks = manager.getEpicSubtasks(epicId);
        final List<Subtask> subtasks = manager.getSubtasks();
        final List<Task> prioritized = manager.getPrioritizedTasks();

        assertAll("subtask saved with errors",
                () -> assertTaskEquals(expectedSubtask, savedSubtask, "subtask saved with errors"),
                () -> assertListEquals(expectedSubtasks, epicSubtasks, "subtask saved with errors"),
                () -> assertListEquals(expectedSubtasks, subtasks, "subtask saved with errors"),
                () -> assertListEquals(expectedSubtasks, prioritized, "subtask saved with errors")
        );
    }

    @Test
    public void shouldUpdateSubtaskInGetAndEpicAndSubtasksAndPrioritizedWhenPreviousVersionCoversStartTime() {
        final long epicId = manager.addEpic(emptyEpic);
        emptySubtask = fromEmptySubtask().withEpicId(epicId).withDuration(TEST_DURATION)
                .withStartTime(TEST_START_TIME.minusMinutes(15L)).build();
        final long subtaskId = manager.addSubtask(emptySubtask);
        testSubtask = fromTestSubtask().withId(subtaskId).build();
        final Subtask expectedSubtask = fromTestSubtask().withId(subtaskId).withEpicId(epicId).build();
        final List<Subtask> expectedSubtasks = List.of(expectedSubtask);

        manager.updateSubtask(testSubtask);
        final Subtask savedSubtask = manager.getSubtask(subtaskId);
        final List<Subtask> epicSubtasks = manager.getEpicSubtasks(epicId);
        final List<Subtask> subtasks = manager.getSubtasks();
        final List<Task> prioritized = manager.getPrioritizedTasks();

        assertAll("subtask saved with errors",
                () -> assertTaskEquals(expectedSubtask, savedSubtask, "subtask saved with errors"),
                () -> assertListEquals(expectedSubtasks, epicSubtasks, "subtask saved with errors"),
                () -> assertListEquals(expectedSubtasks, subtasks, "subtask saved with errors"),
                () -> assertListEquals(expectedSubtasks, prioritized, "subtask saved with errors")
        );
    }

    @Test
    public void shouldUpdateSubtaskInGetAndEpicAndSubtasksAndPrioritizedWhenPreviousVersionCoversEndTime() {
        final long epicId = manager.addEpic(emptyEpic);
        emptySubtask = fromEmptySubtask().withEpicId(epicId).withDuration(TEST_DURATION)
                .withStartTime(TEST_START_TIME.plusMinutes(15L)).build();
        final long subtaskId = manager.addSubtask(emptySubtask);
        testSubtask = fromTestSubtask().withId(subtaskId).build();
        final Subtask expectedSubtask = fromTestSubtask().withId(subtaskId).withEpicId(epicId).build();
        final List<Subtask> expectedSubtasks = List.of(expectedSubtask);

        manager.updateSubtask(testSubtask);
        final Subtask savedSubtask = manager.getSubtask(subtaskId);
        final List<Subtask> epicSubtasks = manager.getEpicSubtasks(epicId);
        final List<Subtask> subtasks = manager.getSubtasks();
        final List<Task> prioritized = manager.getPrioritizedTasks();

        assertAll("subtask saved with errors",
                () -> assertTaskEquals(expectedSubtask, savedSubtask, "subtask saved with errors"),
                () -> assertListEquals(expectedSubtasks, epicSubtasks, "subtask saved with errors"),
                () -> assertListEquals(expectedSubtasks, subtasks, "subtask saved with errors"),
                () -> assertListEquals(expectedSubtasks, prioritized, "subtask saved with errors")
        );
    }

    @Test
    public void shouldUpdateSubtaskInGetAndEpicAndSubtasksAndPrioritizedWhenPreviousVersionCoversWholeInterval() {
        final long epicId = manager.addEpic(emptyEpic);
        emptySubtask = fromEmptySubtask().withEpicId(epicId).withDuration(60L)
                .withStartTime(TEST_START_TIME.minusMinutes(15L)).build();
        final long subtaskId = manager.addSubtask(emptySubtask);
        testSubtask = fromTestSubtask().withId(subtaskId).build();
        final Subtask expectedSubtask = fromTestSubtask().withId(subtaskId).withEpicId(epicId).build();
        final List<Subtask> expectedSubtasks = List.of(expectedSubtask);

        manager.updateSubtask(testSubtask);
        final Subtask savedSubtask = manager.getSubtask(subtaskId);
        final List<Subtask> epicSubtasks = manager.getEpicSubtasks(epicId);
        final List<Subtask> subtasks = manager.getSubtasks();
        final List<Task> prioritized = manager.getPrioritizedTasks();

        assertAll("subtask saved with errors",
                () -> assertTaskEquals(expectedSubtask, savedSubtask, "subtask saved with errors"),
                () -> assertListEquals(expectedSubtasks, epicSubtasks, "subtask saved with errors"),
                () -> assertListEquals(expectedSubtasks, subtasks, "subtask saved with errors"),
                () -> assertListEquals(expectedSubtasks, prioritized, "subtask saved with errors")
        );
    }

    @Test
    public void shouldUpdateSubtaskInGetAndEpicAndSubtasksAndPrioritizedWhenPreviousVersionWithinInterval() {
        final long epicId = manager.addEpic(emptyEpic);
        emptySubtask = fromEmptySubtask().withEpicId(epicId).withDuration(20L)
                .withStartTime(TEST_START_TIME.plusMinutes(5L)).build();
        final long subtaskId = manager.addSubtask(emptySubtask);
        testSubtask = fromTestSubtask().withId(subtaskId).build();
        final Subtask expectedSubtask = fromTestSubtask().withId(subtaskId).withEpicId(epicId).build();
        final List<Subtask> expectedSubtasks = List.of(expectedSubtask);

        manager.updateSubtask(testSubtask);
        final Subtask savedSubtask = manager.getSubtask(subtaskId);
        final List<Subtask> epicSubtasks = manager.getEpicSubtasks(epicId);
        final List<Subtask> subtasks = manager.getSubtasks();
        final List<Task> prioritized = manager.getPrioritizedTasks();

        assertAll("subtask saved with errors",
                () -> assertTaskEquals(expectedSubtask, savedSubtask, "subtask saved with errors"),
                () -> assertListEquals(expectedSubtasks, epicSubtasks, "subtask saved with errors"),
                () -> assertListEquals(expectedSubtasks, subtasks, "subtask saved with errors"),
                () -> assertListEquals(expectedSubtasks, prioritized, "subtask saved with errors")
        );
    }

    @Test
    public void shouldUpdateSubtaskInGetAndEpicAndSubtasksAndPrioritizedWhenPreviousVersionWithinIntervalLeftAligned() {
        final long epicId = manager.addEpic(emptyEpic);
        emptySubtask = fromEmptySubtask().withEpicId(epicId).withDuration(20L).withStartTime(TEST_START_TIME)
                .build();
        final long subtaskId = manager.addSubtask(emptySubtask);
        testSubtask = fromTestSubtask().withId(subtaskId).build();
        final Subtask expectedSubtask = fromTestSubtask().withId(subtaskId).withEpicId(epicId).build();
        final List<Subtask> expectedSubtasks = List.of(expectedSubtask);

        manager.updateSubtask(testSubtask);
        final Subtask savedSubtask = manager.getSubtask(subtaskId);
        final List<Subtask> epicSubtasks = manager.getEpicSubtasks(epicId);
        final List<Subtask> subtasks = manager.getSubtasks();
        final List<Task> prioritized = manager.getPrioritizedTasks();

        assertAll("subtask saved with errors",
                () -> assertTaskEquals(expectedSubtask, savedSubtask, "subtask saved with errors"),
                () -> assertListEquals(expectedSubtasks, epicSubtasks, "subtask saved with errors"),
                () -> assertListEquals(expectedSubtasks, subtasks, "subtask saved with errors"),
                () -> assertListEquals(expectedSubtasks, prioritized, "subtask saved with errors")
        );
    }

    @Test
    public void shouldUpdateSubtaskInGetAndEpicAndSubtasksAndPrioritizedWhenPreviousVersionWithinIntervalRAligned() {
        final long epicId = manager.addEpic(emptyEpic);
        emptySubtask = fromEmptySubtask().withEpicId(epicId).withDuration(20L)
                .withStartTime(TEST_START_TIME.plusMinutes(10L)).build();
        final long subtaskId = manager.addSubtask(emptySubtask);
        testSubtask = fromTestSubtask().withId(subtaskId).build();
        final Subtask expectedSubtask = fromTestSubtask().withId(subtaskId).withEpicId(epicId).build();
        final List<Subtask> expectedSubtasks = List.of(expectedSubtask);

        manager.updateSubtask(testSubtask);
        final Subtask savedSubtask = manager.getSubtask(subtaskId);
        final List<Subtask> epicSubtasks = manager.getEpicSubtasks(epicId);
        final List<Subtask> subtasks = manager.getSubtasks();
        final List<Task> prioritized = manager.getPrioritizedTasks();

        assertAll("subtask saved with errors",
                () -> assertTaskEquals(expectedSubtask, savedSubtask, "subtask saved with errors"),
                () -> assertListEquals(expectedSubtasks, epicSubtasks, "subtask saved with errors"),
                () -> assertListEquals(expectedSubtasks, subtasks, "subtask saved with errors"),
                () -> assertListEquals(expectedSubtasks, prioritized, "subtask saved with errors")
        );
    }

    @Test
    public void shouldUpdateSubtaskInGetAndEpicAndSubtasksAndPrioritizedWhenPreviousVersionWithSameInterval() {
        final long epicId = manager.addEpic(emptyEpic);
        emptySubtask = fromEmptySubtask().withEpicId(epicId).withDuration(TEST_DURATION)
                .withStartTime(TEST_START_TIME).build();
        final long subtaskId = manager.addSubtask(emptySubtask);
        testSubtask = fromTestSubtask().withId(subtaskId).build();
        final Subtask expectedSubtask = fromTestSubtask().withId(subtaskId).withEpicId(epicId).build();
        final List<Subtask> expectedSubtasks = List.of(expectedSubtask);

        manager.updateSubtask(testSubtask);
        final Subtask savedSubtask = manager.getSubtask(subtaskId);
        final List<Subtask> epicSubtasks = manager.getEpicSubtasks(epicId);
        final List<Subtask> subtasks = manager.getSubtasks();
        final List<Task> prioritized = manager.getPrioritizedTasks();

        assertAll("subtask saved with errors",
                () -> assertTaskEquals(expectedSubtask, savedSubtask, "subtask saved with errors"),
                () -> assertListEquals(expectedSubtasks, epicSubtasks, "subtask saved with errors"),
                () -> assertListEquals(expectedSubtasks, subtasks, "subtask saved with errors"),
                () -> assertListEquals(expectedSubtasks, prioritized, "subtask saved with errors")
        );
    }

    @Test
    public void shouldUpdateSubtaskInGetAndEpicAndSubtasksAndPrioritizedWhenExactlyBeforeAnotherPrioritizedTask() {
        testTask = fromTestTask().withId(null).withStartTime(TEST_START_TIME.plusMinutes(TEST_DURATION)).build();
        final long taskId = manager.addTask(testTask);
        final long epicId = manager.addEpic(emptyEpic);
        emptySubtask = fromEmptySubtask().withEpicId(epicId).build();
        final long subtaskId = manager.addSubtask(emptySubtask);
        testSubtask = fromTestSubtask().withId(subtaskId).build();
        final Task expectedTask = fromTestTask().withId(taskId)
                .withStartTime(TEST_START_TIME.plusMinutes(TEST_DURATION)).build();
        final Subtask expectedSubtask = fromTestSubtask().withId(subtaskId).withEpicId(epicId).build();
        final List<Subtask> expectedSubtasks = List.of(expectedSubtask);
        final List<Task> expectedPrioritized = List.of(expectedSubtask, expectedTask);

        manager.updateSubtask(testSubtask);
        final Subtask savedSubtask = manager.getSubtask(subtaskId);
        final List<Subtask> epicSubtasks = manager.getEpicSubtasks(epicId);
        final List<Subtask> subtasks = manager.getSubtasks();
        final List<Task> prioritized = manager.getPrioritizedTasks();

        assertAll("subtask saved with errors",
                () -> assertTaskEquals(expectedSubtask, savedSubtask, "subtask saved with errors"),
                () -> assertListEquals(expectedSubtasks, epicSubtasks, "subtask saved with errors"),
                () -> assertListEquals(expectedSubtasks, subtasks, "subtask saved with errors"),
                () -> assertListEquals(expectedPrioritized, prioritized, "subtask saved with errors")
        );
    }

    @Test
    public void shouldUpdateSubtaskInGetAndEpicAndSubtasksAndPrioritizedWhenExactlyAfterAnotherPrioritizedTask() {
        testTask = fromTestTask().withId(null).withStartTime(TEST_START_TIME.minusMinutes(TEST_DURATION))
                .build();
        final long taskId = manager.addTask(testTask);
        final long epicId = manager.addEpic(emptyEpic);
        emptySubtask = fromEmptySubtask().withEpicId(epicId).build();
        final long subtaskId = manager.addSubtask(emptySubtask);
        testSubtask = fromTestSubtask().withId(subtaskId).build();
        final Task expectedTask = fromTestTask().withId(taskId)
                .withStartTime(TEST_START_TIME.minusMinutes(TEST_DURATION)).build();
        final Subtask expectedSubtask = fromTestSubtask().withId(subtaskId).withEpicId(epicId).build();
        final List<Subtask> expectedSubtasks = List.of(expectedSubtask);
        final List<Task> expectedPrioritized = List.of(expectedTask, expectedSubtask);

        manager.updateSubtask(testSubtask);
        final Subtask savedSubtask = manager.getSubtask(subtaskId);
        final List<Subtask> epicSubtasks = manager.getEpicSubtasks(epicId);
        final List<Subtask> subtasks = manager.getSubtasks();
        final List<Task> prioritized = manager.getPrioritizedTasks();

        assertAll("subtask saved with errors",
                () -> assertTaskEquals(expectedSubtask, savedSubtask, "subtask saved with errors"),
                () -> assertListEquals(expectedSubtasks, epicSubtasks, "subtask saved with errors"),
                () -> assertListEquals(expectedSubtasks, subtasks, "subtask saved with errors"),
                () -> assertListEquals(expectedPrioritized, prioritized, "subtask saved with errors")
        );
    }

    @Test
    public void shouldUpdateSubtaskInGetAndEpicAndSubtasksAndPrioritizedWhenStartTimeBecomesNull() {
        final long epicId = manager.addEpic(testEpic);
        testSubtask = fromTestSubtask().withId(null).withEpicId(epicId).build();
        final long subtaskId = manager.addSubtask(testSubtask);
        modifiedSubtask = fromModifiedSubtask().withId(subtaskId).withDuration(null).withStartTime(null).build();
        final Subtask expectedSubtask = fromModifiedSubtask().withId(subtaskId).withEpicId(epicId).withDuration(null)
                .withStartTime(null).build();
        final List<Subtask> expectedSubtasks = List.of(expectedSubtask);

        manager.updateSubtask(modifiedSubtask);
        final Subtask savedSubtask = manager.getSubtask(subtaskId);
        final List<Subtask> epicSubtasks = manager.getEpicSubtasks(epicId);
        final List<Subtask> subtasks = manager.getSubtasks();
        final List<Task> prioritized = manager.getPrioritizedTasks();

        assertAll("subtask saved with errors",
                () -> assertTaskEquals(expectedSubtask, savedSubtask, "subtask saved with errors"),
                () -> assertListEquals(expectedSubtasks, epicSubtasks, "subtask saved with errors"),
                () -> assertListEquals(expectedSubtasks, subtasks, "subtask saved with errors"),
                () -> assertTrue(prioritized.isEmpty(), "subtask saved with errors")
        );
    }

    @Test
    public void shouldUpdateSubtaskInGetAndEpicAndSubtasksAndPrioritizedWhenStartTimeWasNull() {
        final long epicId = manager.addEpic(testEpic);
        testSubtask = fromTestSubtask().withId(null).withEpicId(epicId).withDuration(null).withStartTime(null).build();
        final long subtaskId = manager.addSubtask(testSubtask);
        modifiedSubtask = fromModifiedSubtask().withId(subtaskId).build();
        final Subtask expectedSubtask = fromModifiedSubtask().withId(subtaskId).withEpicId(epicId).build();
        final List<Subtask> expectedSubtasks = List.of(expectedSubtask);

        manager.updateSubtask(modifiedSubtask);
        final Subtask savedSubtask = manager.getSubtask(subtaskId);
        final List<Subtask> epicSubtasks = manager.getEpicSubtasks(epicId);
        final List<Subtask> subtasks = manager.getSubtasks();
        final List<Task> prioritized = manager.getPrioritizedTasks();

        assertAll("subtask saved with errors",
                () -> assertTaskEquals(expectedSubtask, savedSubtask, "subtask saved with errors"),
                () -> assertListEquals(expectedSubtasks, epicSubtasks, "subtask saved with errors"),
                () -> assertListEquals(expectedSubtasks, subtasks, "subtask saved with errors"),
                () -> assertListEquals(expectedSubtasks, prioritized, "subtask saved with errors")
        );
    }

    @Test
    public void shouldUpdateSubtaskInGetAndEpicAndSubtasksNotPrioritizedWhenStartTimeNull() {
        final long epicId = manager.addEpic(testEpic);
        testSubtask = fromTestSubtask().withId(null).withEpicId(epicId).withDuration(null).withStartTime(null).build();
        final long subtaskId = manager.addSubtask(testSubtask);
        modifiedSubtask = fromModifiedSubtask().withId(subtaskId).withDuration(null).withStartTime(null).build();
        final Subtask expectedSubtask = fromModifiedSubtask().withId(subtaskId).withEpicId(epicId).withDuration(null)
                .withStartTime(null).build();
        final List<Subtask> expectedSubtasks = List.of(expectedSubtask);

        manager.updateSubtask(modifiedSubtask);
        final Subtask savedSubtask = manager.getSubtask(subtaskId);
        final List<Subtask> epicSubtasks = manager.getEpicSubtasks(epicId);
        final List<Subtask> subtasks = manager.getSubtasks();
        final List<Task> prioritized = manager.getPrioritizedTasks();

        assertAll("subtask saved with errors",
                () -> assertTaskEquals(expectedSubtask, savedSubtask, "subtask saved with errors"),
                () -> assertListEquals(expectedSubtasks, epicSubtasks, "subtask saved with errors"),
                () -> assertListEquals(expectedSubtasks, subtasks, "subtask saved with errors"),
                () -> assertTrue(prioritized.isEmpty(), "subtask saved with errors")
        );
    }

    @Test
    public void shouldUpdateSubtaskInGetAndEpicAndSubtasksAndPrioritizedWhenFieldsBecomeNull() {
        final long epicId = manager.addEpic(testEpic);
        testSubtask = fromTestSubtask().withId(null).withEpicId(epicId).build();
        final long subtaskId = manager.addSubtask(testSubtask);
        emptySubtask = fromEmptySubtask().withId(subtaskId).build();
        final Subtask expectedSubtask = fromEmptySubtask().withId(subtaskId).withEpicId(epicId).build();
        final List<Subtask> expectedSubtasks = List.of(expectedSubtask);

        manager.updateSubtask(emptySubtask);
        final Subtask savedSubtask = manager.getSubtask(subtaskId);
        final List<Subtask> epicSubtasks = manager.getEpicSubtasks(epicId);
        final List<Subtask> subtasks = manager.getSubtasks();
        final List<Task> prioritized = manager.getPrioritizedTasks();

        assertAll("subtask saved with errors",
                () -> assertTaskEquals(expectedSubtask, savedSubtask, "subtask saved with errors"),
                () -> assertListEquals(expectedSubtasks, epicSubtasks, "subtask saved with errors"),
                () -> assertListEquals(expectedSubtasks, subtasks, "subtask saved with errors"),
                () -> assertTrue(prioritized.isEmpty(), "subtask saved with errors")
        );
    }

    @Test
    public void shouldNotRemoveSubtaskWhenNotExist() {
        final long subtaskId = -1L;
        final String expectedMessage = "no subtask with id=" + subtaskId;

        final Exception exception = assertThrows(TaskNotFoundException.class, () -> manager.removeSubtask(subtaskId));
        assertEquals(expectedMessage, exception.getMessage(), WRONG_EXCEPTION_MESSAGE);
    }

    @Test
    public void shouldRemoveSubtaskFromGetAndEpicAndSubtasksAndPrioritized() {
        final long epicId = manager.addEpic(testEpic);
        testSubtask = fromTestSubtask().withId(null).withEpicId(epicId).build();
        final long subtaskId = manager.addSubtask(testSubtask);

        manager.removeSubtask(subtaskId);
        final List<Subtask> epicSubtasks = manager.getEpicSubtasks(epicId);
        final List<Subtask> subtasks = manager.getSubtasks();
        final List<Task> prioritized = manager.getPrioritizedTasks();

        assertAll("subtask removed with errors",
                () -> assertThrows(TaskNotFoundException.class, () -> manager.getSubtask(subtaskId),
                        "subtask removed with errors"),
                () -> assertTrue(epicSubtasks.isEmpty(), "subtask removed with errors"),
                () -> assertTrue(subtasks.isEmpty(), "subtask removed with errors"),
                () -> assertTrue(prioritized.isEmpty(), "subtask removed with errors")
        );
    }

    @Test
    public void shouldRemoveSubtaskFromGetAndSubtasksAndPrioritizedWhenRemoveEpic() {
        final long epicId = manager.addEpic(testEpic);
        testSubtask = fromTestSubtask().withId(null).withEpicId(epicId).build();
        final long subtaskId = manager.addSubtask(testSubtask);

        manager.removeEpic(epicId);
        final List<Subtask> subtasks = manager.getSubtasks();
        final List<Task> prioritized = manager.getPrioritizedTasks();

        assertAll("subtask removed with errors",
                () -> assertThrows(TaskNotFoundException.class, () -> manager.getSubtask(subtaskId),
                        "subtask removed with errors"),
                () -> assertTrue(subtasks.isEmpty(), "subtask removed with errors"),
                () -> assertTrue(prioritized.isEmpty(), "subtask removed with errors")
        );
    }

    @Test
    public void shouldNotGetEpicSubtasksWhenEpicNotExist() {
        final long epicId = -1L;
        final String expectedMessage = "no epic with id=" + epicId;

        final Exception exception = assertThrows(TaskNotFoundException.class, () -> manager.getEpicSubtasks(epicId));
        assertEquals(expectedMessage, exception.getMessage(), WRONG_EXCEPTION_MESSAGE);
    }

    @Test
    public void shouldGetEpicSubtasks() {
        final long epicId = manager.addEpic(testEpic);
        final long anotherEpicId = manager.addEpic(modifiedEpic);
        emptySubtask = fromEmptySubtask().withEpicId(epicId).build();
        testSubtask = fromTestSubtask().withId(null).withEpicId(epicId).build();
        modifiedSubtask = fromModifiedSubtask().withId(null).withEpicId(epicId).build();
        final long emptySubtaskId = manager.addSubtask(emptySubtask);
        final long testSubtaskId = manager.addSubtask(testSubtask);
        final long modifiedSubtaskId = manager.addSubtask(modifiedSubtask);
        emptySubtask = fromEmptySubtask().withEpicId(anotherEpicId).build();
        manager.addSubtask(emptySubtask);
        manager.removeSubtask(testSubtaskId);
        emptySubtask = fromEmptySubtask().withId(emptySubtaskId).withEpicId(epicId).build();
        modifiedSubtask = fromModifiedSubtask().withId(modifiedSubtaskId).withEpicId(epicId).build();
        final List<Subtask> expectedSubtasks = List.of(emptySubtask, modifiedSubtask);

        final List<Subtask> actualSubtasks = manager.getEpicSubtasks(epicId);

        assertListEquals(expectedSubtasks, actualSubtasks, "incorrect list of subtasks returned");
    }

    @Test
    public void shouldGetTasks() {
        final long emptyTaskId = manager.addTask(emptyTask);
        final long testTaskId = manager.addTask(testTask);
        final long modifiedTaskId = manager.addTask(modifiedTask);
        emptyTask = fromEmptyTask().withId(emptyTaskId).build();
        modifiedTask = fromModifiedTask().withId(modifiedTaskId).build();
        final List<Task> expectedTasks = List.of(emptyTask, modifiedTask);
        manager.removeTask(testTaskId);

        final List<Task> actualTasks = manager.getTasks();

        assertListEquals(expectedTasks, actualTasks, "incorrect list of tasks returned");
    }

    @Test
    public void shouldRemoveTasks() {
        manager.addTask(testTask);
        manager.addTask(modifiedTask);

        manager.removeTasks();
        final List<Task> tasks = manager.getTasks();
        final List<Task> prioritized = manager.getPrioritizedTasks();

        assertAll("tasks removed with errors",
                () -> assertTrue(tasks.isEmpty(), "tasks removed with errors"),
                () -> assertTrue(prioritized.isEmpty(), "tasks removed with errors")
        );
    }

    @Test
    public void shouldGetEpics() {
        final long emptyEpicId = manager.addEpic(emptyEpic);
        final long testEpicId = manager.addEpic(testEpic);
        final long modifiedEpicId = manager.addEpic(modifiedEpic);
        emptyEpic = fromEmptyEpic().withId(emptyEpicId).build();
        modifiedEpic = fromModifiedEpic().withId(modifiedEpicId).build();
        final List<Epic> expectedEpics = List.of(emptyEpic, modifiedEpic);
        manager.removeEpic(testEpicId);

        final List<Epic> actualEpics = manager.getEpics();

        assertListEquals(expectedEpics, actualEpics, "incorrect list of epics returned");
    }

    @Test
    public void shouldRemoveEpics() {
        manager.addEpic(testEpic);
        manager.addEpic(modifiedEpic);

        manager.removeEpics();
        final List<Epic> epics = manager.getEpics();

        assertTrue(epics.isEmpty(), "list of epics should be empty");
    }

    @Test
    public void shouldGetSubtasks() {
        final long epicId = manager.addEpic(testEpic);
        final long anotherEpicId = manager.addEpic(modifiedEpic);
        emptySubtask = fromEmptySubtask().withEpicId(epicId).build();
        testSubtask = fromTestSubtask().withId(null).withEpicId(epicId).build();
        modifiedSubtask = fromModifiedSubtask().withId(null).withEpicId(epicId).build();
        final long emptySubtaskId = manager.addSubtask(emptySubtask);
        final long testSubtaskId = manager.addSubtask(testSubtask);
        final long modifiedSubtaskId = manager.addSubtask(modifiedSubtask);
        emptySubtask = fromEmptySubtask().withEpicId(anotherEpicId).build();
        final long anotherEmptySubtaskId = manager.addSubtask(emptySubtask);
        manager.removeSubtask(testSubtaskId);
        emptySubtask = fromEmptySubtask().withId(emptySubtaskId).withEpicId(epicId).build();
        modifiedSubtask = fromModifiedSubtask().withId(modifiedSubtaskId).withEpicId(epicId).build();
        testSubtask = fromEmptySubtask().withId(anotherEmptySubtaskId).withEpicId(anotherEpicId).build();
        final List<Subtask> expectedSubtasks = List.of(emptySubtask, modifiedSubtask, testSubtask);

        final List<Subtask> actualSubtasks = manager.getSubtasks();

        assertListEquals(expectedSubtasks, actualSubtasks, "incorrect list of subtasks returned");
    }

    @Test
    public void shouldRemoveSubtasks() {
        final long epicId = manager.addEpic(testEpic);
        testSubtask = fromTestSubtask().withId(null).withEpicId(epicId).build();
        manager.addSubtask(testSubtask);
        final long anotherEpicId = manager.addEpic(modifiedEpic);
        modifiedSubtask = fromModifiedSubtask().withId(null).withEpicId(anotherEpicId).build();
        manager.addSubtask(modifiedSubtask);

        manager.removeSubtasks();
        final List<Subtask> testEpicSubtasks = manager.getEpicSubtasks(epicId);
        final List<Subtask> modifiedEpicSubtasks = manager.getEpicSubtasks(anotherEpicId);
        final List<Subtask> subtasks = manager.getSubtasks();
        final List<Task> prioritized = manager.getPrioritizedTasks();

        assertAll("subtasks removed with errors",
                () -> assertTrue(testEpicSubtasks.isEmpty(), "subtasks removed with errors"),
                () -> assertTrue(modifiedEpicSubtasks.isEmpty(), "subtasks removed with errors"),
                () -> assertTrue(subtasks.isEmpty(), "subtasks removed with errors"),
                () -> assertTrue(prioritized.isEmpty(), "subtasks removed with errors")
        );
    }

    @Test
    public void shouldRemoveSubtasksWhenRemoveEpics() {
        final long epicId = manager.addEpic(testEpic);
        testSubtask = fromTestSubtask().withId(null).withEpicId(epicId).build();
        manager.addSubtask(testSubtask);
        final long anotherEpicId = manager.addEpic(modifiedEpic);
        modifiedSubtask = fromModifiedSubtask().withId(null).withEpicId(anotherEpicId).build();
        manager.addSubtask(modifiedSubtask);

        manager.removeEpics();
        final List<Subtask> subtasks = manager.getSubtasks();
        final List<Task> prioritized = manager.getPrioritizedTasks();

        assertAll("subtasks removed with errors",
                () -> assertTrue(subtasks.isEmpty(), "subtasks removed with errors"),
                () -> assertTrue(prioritized.isEmpty(), "subtasks removed with errors")
        );
    }

    @Test
    public void shouldPassTaskToHistoryManagerWhenGetTask() {
        final long taskId = manager.addTask(testTask);
        final Task expectedTask = fromTestTask().withId(taskId).build();
        final List<Task> expectedTasks = List.of(expectedTask);

        manager.getTask(taskId);
        final List<Task> tasks = historyManager.getHistory();

        assertListEquals(expectedTasks, tasks, "history saved with errors");
    }

    @Test
    public void shouldPassEpicToHistoryManagerWhenGetEpic() {
        final long epicId = manager.addEpic(testEpic);
        final Epic expectedEpic = fromTestEpic().withId(epicId).build();
        final List<Task> expectedTasks = List.of(expectedEpic);

        manager.getEpic(epicId);
        final List<Task> tasks = historyManager.getHistory();

        assertListEquals(expectedTasks, tasks, "history saved with errors");
    }

    @Test
    public void shouldPassSubtaskToHistoryManagerWhenGetSubtask() {
        final long epicId = manager.addEpic(testEpic);
        testSubtask = fromTestSubtask().withId(null).withEpicId(epicId).build();
        final long subtaskId = manager.addSubtask(testSubtask);
        final Subtask expectedSubtask = fromTestSubtask().withId(subtaskId).withEpicId(epicId).build();
        final List<Task> expectedTasks = List.of(expectedSubtask);

        manager.getSubtask(subtaskId);
        final List<Task> tasks = historyManager.getHistory();

        assertListEquals(expectedTasks, tasks, "history saved with errors");
    }

    @Test
    public void shouldPassTasksEpicsSubtasksFromHistoryManagerWhenGetHistory() {
        testTask = fromTestTask().build();
        testEpic = fromTestEpic().build();
        testSubtask = fromTestSubtask().build();
        historyManager.add(testTask);
        historyManager.add(testEpic);
        historyManager.add(testSubtask);
        final Task expectedTask = fromTestTask().build();
        final Epic expectedEpic = fromTestEpic().build();
        final Subtask expectedSubtask = fromTestSubtask().build();
        final List<Task> expectedTasks = List.of(expectedTask, expectedEpic, expectedSubtask);

        final List<Task> tasks = manager.getHistory();

        assertListEquals(expectedTasks, tasks, "incorrect history returned");
    }

    @Test
    public void shouldRemoveTaskFromHistoryManagerWhenRemoveTask() {
        final long taskId = manager.addTask(testTask);
        manager.getTask(taskId);

        manager.removeTask(taskId);
        final List<Task> tasks = historyManager.getHistory();

        assertTrue(tasks.isEmpty(), "task should be removed from history");
    }

    @Test
    public void shouldRemoveEpicFromHistoryManagerWhenRemoveEpic() {
        final long epicId = manager.addEpic(testEpic);
        manager.getEpic(epicId);

        manager.removeEpic(epicId);
        final List<Task> tasks = historyManager.getHistory();

        assertTrue(tasks.isEmpty(), "epic should be removed from history");
    }

    @Test
    public void shouldRemoveSubtaskFromHistoryManagerWhenRemoveSubtask() {
        final long epicId = manager.addEpic(testEpic);
        testSubtask = fromTestSubtask().withId(null).withEpicId(epicId).build();
        final long subtaskId = manager.addSubtask(testSubtask);
        manager.getSubtask(subtaskId);

        manager.removeSubtask(subtaskId);
        final List<Task> tasks = historyManager.getHistory();

        assertTrue(tasks.isEmpty(), "subtask should be removed from history");
    }

    @Test
    public void shouldRemoveSubtaskFromHistoryManagerWhenRemoveEpic() {
        final long epicId = manager.addEpic(testEpic);
        testSubtask = fromTestSubtask().withId(null).withEpicId(epicId).build();
        final long testSubtaskId = manager.addSubtask(testSubtask);
        final long anotherEpicId = manager.addEpic(modifiedEpic);
        modifiedSubtask = fromModifiedSubtask().withId(null).withEpicId(anotherEpicId).build();
        final long modifiedSubtaskId = manager.addSubtask(modifiedSubtask);
        manager.getSubtask(testSubtaskId);
        manager.getSubtask(modifiedSubtaskId);
        final Subtask expectedSubtask = fromModifiedSubtask().withId(modifiedSubtaskId).withEpicId(anotherEpicId)
                .build();
        final List<Task> expectedTasks = List.of(expectedSubtask);

        manager.removeEpic(epicId);
        final List<Task> tasks = historyManager.getHistory();

        assertListEquals(expectedTasks, tasks, "incorrect history returned");
    }

    @Test
    public void shouldRemoveTasksFromHistoryManagerWhenRemoveTasks() {
        final long testTaskId = manager.addTask(testTask);
        final long modifiedTaskId = manager.addTask(modifiedTask);
        manager.getTask(testTaskId);
        manager.getTask(modifiedTaskId);

        manager.removeTasks();
        final List<Task> tasks = historyManager.getHistory();

        assertTrue(tasks.isEmpty(), "tasks should be removed from history");
    }

    @Test
    public void shouldRemoveEpicsFromHistoryManagerWhenRemoveEpics() {
        final long testEpicId = manager.addEpic(testEpic);
        final long modifiedEpicId = manager.addEpic(modifiedEpic);
        manager.getEpic(testEpicId);
        manager.getEpic(modifiedEpicId);

        manager.removeEpics();
        final List<Task> tasks = historyManager.getHistory();

        assertTrue(tasks.isEmpty(), "epics should be removed from history");
    }

    @Test
    public void shouldRemoveSubtasksFromHistoryManagerWhenRemoveSubtasks() {
        final long epicId = manager.addEpic(testEpic);
        testSubtask = fromTestSubtask().withId(null).withEpicId(epicId).build();
        final long testSubtaskId = manager.addSubtask(testSubtask);
        final long anotherEpicId = manager.addEpic(modifiedEpic);
        modifiedSubtask = fromModifiedSubtask().withId(null).withEpicId(anotherEpicId).build();
        final long modifiedSubtaskId = manager.addSubtask(modifiedSubtask);
        manager.getSubtask(testSubtaskId);
        manager.getSubtask(modifiedSubtaskId);

        manager.removeSubtasks();
        final List<Task> tasks = historyManager.getHistory();

        assertTrue(tasks.isEmpty(), "subtasks should be removed from history");
    }

    @Test
    public void shouldRemoveSubtasksFromHistoryManagerWhenRemovedEpics() {
        final long epicId = manager.addEpic(testEpic);
        testSubtask = fromTestSubtask().withId(null).withEpicId(epicId).build();
        final long testSubtaskId = manager.addSubtask(testSubtask);
        final long anotherEpicId = manager.addEpic(modifiedEpic);
        modifiedSubtask = fromModifiedSubtask().withId(null).withEpicId(anotherEpicId).build();
        final long modifiedSubtaskId = manager.addSubtask(modifiedSubtask);
        manager.getSubtask(testSubtaskId);
        manager.getSubtask(modifiedSubtaskId);

        manager.removeEpics();
        final List<Task> tasks = historyManager.getHistory();

        assertTrue(tasks.isEmpty(), "subtasks should be removed from history");
    }
}
