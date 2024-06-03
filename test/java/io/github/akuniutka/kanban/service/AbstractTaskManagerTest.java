package io.github.akuniutka.kanban.service;

import io.github.akuniutka.kanban.exception.DuplicateIdException;
import io.github.akuniutka.kanban.exception.ManagerValidationException;
import io.github.akuniutka.kanban.exception.TaskNotFoundException;
import io.github.akuniutka.kanban.model.Epic;
import io.github.akuniutka.kanban.model.Subtask;
import io.github.akuniutka.kanban.model.Task;
import io.github.akuniutka.kanban.model.TaskStatus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.NullSource;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

import static io.github.akuniutka.kanban.TestModels.*;
import static org.junit.jupiter.api.Assertions.*;

abstract class AbstractTaskManagerTest {
    protected static final String WRONG_EXCEPTION_MESSAGE = "message for exception is wrong";
    protected final Task emptyTask;
    protected final Task testTask;
    protected final Task modifiedTask;
    protected final Epic emptyEpic;
    protected final Epic testEpic;
    protected final Epic modifiedEpic;
    protected TaskManager manager;
    protected HistoryManager historyManager;

    protected AbstractTaskManagerTest() {
        this.historyManager = new InMemoryHistoryManager();
        this.emptyTask = fromEmptyTask().withStatus(TaskStatus.NEW).build();
        this.testTask = fromTestTask().withId(null).build();
        this.modifiedTask = fromModifiedTask().withId(null).build();
        this.emptyEpic = fromEmptyEpic().build();
        this.testEpic = fromTestEpic().withId(null).build();
        this.modifiedEpic = fromModifiedEpic().withId(null).build();
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
        final String expectedMessage = "cannot add null";

        final Exception exception = assertThrows(NullPointerException.class, () -> manager.addTask(null));
        assertEquals(expectedMessage, exception.getMessage(), WRONG_EXCEPTION_MESSAGE);
    }

    @Test
    public void shouldNotAddTaskWhenIdExists() {
        final long taskId = manager.addTask(testTask);
        final Task dublicateTask = fromModifiedTask().withId(taskId).build();

        final Exception exception = assertThrows(DuplicateIdException.class, () -> manager.addTask(dublicateTask));
        assertEquals("duplicate id=" + taskId, exception.getMessage(), WRONG_EXCEPTION_MESSAGE);
    }

    @Test
    public void shouldNotAddTaskWhenDurationNullAndStartTimeNotNull() {
        final Task task = fromTestTask().withId(null).withDuration(null).build();

        final Exception exception = assertThrows(ManagerValidationException.class, () -> manager.addTask(task));
        assertEquals("duration and start time must be either both set or both null", exception.getMessage(),
                WRONG_EXCEPTION_MESSAGE);
    }

    @Test
    public void shouldNotAddTaskWhenDurationNotNullAndStartTimeNull() {
        final Task task = fromTestTask().withId(null).withStartTime(null).build();

        final Exception exception = assertThrows(ManagerValidationException.class, () -> manager.addTask(task));
        assertEquals("duration and start time must be either both set or both null", exception.getMessage(),
                WRONG_EXCEPTION_MESSAGE);
    }

    @ParameterizedTest
    @MethodSource("io.github.akuniutka.kanban.TestModels#getOverlappingTimeSlots")
    public void shouldNotAddTaskWhenOverlapAnotherPrioritizedTask(Duration duration, LocalDateTime startTime) {
        final long epicId = manager.addEpic(testEpic);
        final Subtask overlappingSubtask = fromTestSubtask().withId(null).withEpicId(epicId).withDuration(duration)
                .withStartTime(startTime).build();
        manager.addSubtask(overlappingSubtask);

        final Exception exception = assertThrows(ManagerValidationException.class, () -> manager.addTask(testTask));
        assertEquals("conflict with another task for time slot", exception.getMessage(), WRONG_EXCEPTION_MESSAGE);
    }

    @Test
    public void shouldNotAddTaskWhenStatusNull() {
        final Task task = fromTestTask().withId(null).withStatus(null).build();

        final Exception exception = assertThrows(ManagerValidationException.class, () -> manager.addTask(task));
        assertEquals("status cannot be null", exception.getMessage(), WRONG_EXCEPTION_MESSAGE);
    }

    @Test
    public void shouldAddTaskToGetAndTasksAndPrioritizedWhenDurationNotNullAndStartTimeNotNull() {
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
        final Task task = fromTestTask().withId(ANOTHER_TEST_ID).build();
        final Task nextTask = fromModifiedTask().withId(null).build();

        final long taskId = manager.addTask(task);
        final long nextId = manager.addTask(nextTask);
        final Task savedTask = manager.getTask(taskId);
        final List<Task> tasks = manager.getTasks();
        final List<Task> prioritized = manager.getPrioritizedTasks();

        final Task expectedTask = fromTestTask().withId(ANOTHER_TEST_ID).build();
        final Task expectedNextTask = fromModifiedTask().withId(nextId).build();
        final List<Task> expectedTasks = List.of(expectedTask, expectedNextTask);
        assertAll("task saved with errors",
                () -> assertEquals(ANOTHER_TEST_ID, taskId, "task saved with errors"),
                () -> assertEquals(ANOTHER_TEST_ID + 1, nextId, "task saved with errors"),
                () -> assertTaskEquals(expectedTask, savedTask, "task saved with errors"),
                () -> assertListEquals(expectedTasks, tasks, "task saved with errors"),
                () -> assertListEquals(expectedTasks, prioritized, "task saved with errors")
        );
    }

    @Test
    public void shouldAddTaskToGetAndTasksAndPrioritizedWithDurationTruncatedToMinutes() {
        final Task task = fromTestTask().withId(null).withDuration(TEST_DURATION.plusSeconds(25L)).build();
        final long taskId = manager.addTask(task);
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
    public void shouldAddTaskToGetAndTasksAndPrioritizedWithStartTimeTruncatedToMinutes() {
        final Task task = fromTestTask().withId(null).withStartTime(TEST_START_TIME.plusSeconds(25L)).build();
        final long taskId = manager.addTask(task);
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
    public void shouldAddTaskToGetAndTasksNotPrioritizedWhenDurationNullAndStartTimeNull() {
        final Task task = fromTestTask().withId(null).withDuration(null).withStartTime(null).build();

        final long taskId = manager.addTask(task);
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

        final Task expectedTask = fromEmptyTask().withId(taskId).withStatus(TaskStatus.NEW).build();
        final List<Task> expectedTasks = List.of(expectedTask);
        assertAll("task saved with errors",
                () -> assertTaskEquals(expectedTask, savedTask, "task saved with errors"),
                () -> assertListEquals(expectedTasks, tasks, "task saved with errors"),
                () -> assertTrue(prioritized.isEmpty(), "task saved with errors")
        );
    }

    @Test
    public void shouldAddTaskToGetAndTasksAndPrioritizedWhenExactlyBeforeAnotherPrioritizedTask() {
        final long epicId = manager.addEpic(testEpic);
        final Subtask subtask = fromTestSubtask().withId(null).withEpicId(epicId)
                .withStartTime(TEST_START_TIME.plus(TEST_DURATION)).build();
        final long subtaskId = manager.addSubtask(subtask);

        final long taskId = manager.addTask(testTask);
        final Task savedTask = manager.getTask(taskId);
        final List<Task> tasks = manager.getTasks();
        final List<Task> prioritized = manager.getPrioritizedTasks();

        final Subtask expectedSubtask = fromTestSubtask().withId(subtaskId).withEpicId(epicId)
                .withStartTime(TEST_START_TIME.plus(TEST_DURATION)).build();
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
    public void shouldAddTaskToGetAndTasksAndPrioritizedWhenWithDurationTruncatedToMinutesExactlyBeforeAnotherTask() {
        final long epicId = manager.addEpic(testEpic);
        final Subtask subtask = fromTestSubtask().withId(null).withEpicId(epicId)
                .withStartTime(TEST_START_TIME.plus(TEST_DURATION)).build();
        final long subtaskId = manager.addSubtask(subtask);
        final Task task = fromTestTask().withId(null).withDuration(TEST_DURATION.plusSeconds(25L)).build();

        final long taskId = manager.addTask(task);
        final Task savedTask = manager.getTask(taskId);
        final List<Task> tasks = manager.getTasks();
        final List<Task> prioritized = manager.getPrioritizedTasks();

        final Subtask expectedSubtask = fromTestSubtask().withId(subtaskId).withEpicId(epicId)
                .withStartTime(TEST_START_TIME.plus(TEST_DURATION)).build();
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
    public void shouldAddTaskToGetAndTasksAndPrioritizedWhenWithStartTimeTruncatedToMinutesExactlyBeforeAnotherTask() {
        final long epicId = manager.addEpic(testEpic);
        final Subtask subtask = fromTestSubtask().withId(null).withEpicId(epicId)
                .withStartTime(TEST_START_TIME.plus(TEST_DURATION)).build();
        final long subtaskId = manager.addSubtask(subtask);
        final Task task = fromTestTask().withId(null).withStartTime(TEST_START_TIME.plusSeconds(25L)).build();

        final long taskId = manager.addTask(task);
        final Task savedTask = manager.getTask(taskId);
        final List<Task> tasks = manager.getTasks();
        final List<Task> prioritized = manager.getPrioritizedTasks();

        final Subtask expectedSubtask = fromTestSubtask().withId(subtaskId).withEpicId(epicId)
                .withStartTime(TEST_START_TIME.plus(TEST_DURATION)).build();
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
        final long epicId = manager.addEpic(testEpic);
        final Subtask subtask = fromTestSubtask().withId(null).withEpicId(epicId)
                .withStartTime(TEST_START_TIME.minus(TEST_DURATION)).build();
        final long subtaskId = manager.addSubtask(subtask);

        final long taskId = manager.addTask(testTask);
        final Task savedTask = manager.getTask(taskId);
        final List<Task> tasks = manager.getTasks();
        final List<Task> prioritized = manager.getPrioritizedTasks();

        final Subtask expectedSubtask = fromTestSubtask().withId(subtaskId).withEpicId(epicId)
                .withStartTime(TEST_START_TIME.minus(TEST_DURATION)).build();
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
        final String expectedMessage = "cannot apply null update";

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
        final Task update = fromTestTask().withId(taskId).build();

        final Exception exception = assertThrows(TaskNotFoundException.class, () -> manager.updateTask(update));
        assertEquals(expectedMessage, exception.getMessage(), WRONG_EXCEPTION_MESSAGE);
    }

    @Test
    public void shouldNotUpdateTaskWhenDurationNullAndStartTimeNotNull() {
        final long taskId = manager.addTask(testTask);
        final Task update = fromModifiedTask().withId(taskId).withDuration(null).build();

        final Exception exception = assertThrows(ManagerValidationException.class, () -> manager.updateTask(update));
        assertEquals("duration and start time must be either both set or both null", exception.getMessage(),
                WRONG_EXCEPTION_MESSAGE);
    }

    @Test
    public void shouldNotUpdateTaskWhenDurationNotNullAndStartTimeNull() {
        final long taskId = manager.addTask(testTask);
        final Task update = fromTestTask().withId(taskId).withStartTime(null).build();

        final Exception exception = assertThrows(ManagerValidationException.class, () -> manager.updateTask(update));
        assertEquals("duration and start time must be either both set or both null", exception.getMessage(),
                WRONG_EXCEPTION_MESSAGE);
    }

    @ParameterizedTest
    @MethodSource("io.github.akuniutka.kanban.TestModels#getOverlappingTimeSlots")
    public void shouldNotUpdateTaskWhenOverlapAnotherPrioritizedTask(Duration duration, LocalDateTime startTime) {
        final long taskId = manager.addTask(modifiedTask);
        final long epicId = manager.addEpic(testEpic);
        final Subtask overlappingSubtask = fromTestSubtask().withId(null).withEpicId(epicId).withDuration(duration)
                .withStartTime(startTime).build();
        manager.addSubtask(overlappingSubtask);
        final Task update = fromTestTask().withId(taskId).build();

        final Exception exception = assertThrows(ManagerValidationException.class, () -> manager.updateTask(update));
        assertEquals("conflict with another task for time slot", exception.getMessage(), WRONG_EXCEPTION_MESSAGE);
    }

    @Test
    public void shouldNotUpdateTaskWhenStatusNull() {
        final long taskId = manager.addTask(modifiedTask);
        final Task update = fromTestTask().withId(taskId).withStatus(null).build();

        final Exception exception = assertThrows(ManagerValidationException.class, () -> manager.updateTask(update));
        assertEquals("status cannot be null", exception.getMessage(), WRONG_EXCEPTION_MESSAGE);
    }

    @Test
    public void shouldUpdateTaskInGetAndTasksAndPrioritizedWhenDurationNotNullAndStartTimeNotNull() {
        final long taskId = manager.addTask(testTask);
        final Task update = fromModifiedTask().withId(taskId).build();
        final Task expectedTask = fromModifiedTask().withId(taskId).build();
        final List<Task> expectedTasks = List.of(expectedTask);

        manager.updateTask(update);
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
    public void shouldUpdateTaskInGetAndTasksAndPrioritizesWhenDurationTruncatedToMinutes() {
        final long taskId = manager.addTask(testTask);
        final Task update = fromModifiedTask().withId(taskId).withDuration(MODIFIED_DURATION.plusSeconds(25L))
                .build();
        final Task expectedTask = fromModifiedTask().withId(taskId).build();
        final List<Task> expectedTasks = List.of(expectedTask);

        manager.updateTask(update);
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
    public void shouldUpdateTaskInGetAndTasksAndPrioritizesWhenStartTimeTruncatedToMinutes() {
        final long taskId = manager.addTask(testTask);
        final Task update = fromModifiedTask().withId(taskId).withStartTime(MODIFIED_START_TIME.plusSeconds(25L))
                .build();
        final Task expectedTask = fromModifiedTask().withId(taskId).build();
        final List<Task> expectedTasks = List.of(expectedTask);

        manager.updateTask(update);
        final Task savedTask = manager.getTask(taskId);
        final List<Task> tasks = manager.getTasks();
        final List<Task> prioritized = manager.getPrioritizedTasks();

        assertAll("task saved with errors",
                () -> assertTaskEquals(expectedTask, savedTask, "task saved with errors"),
                () -> assertListEquals(expectedTasks, tasks, "task saved with errors"),
                () -> assertListEquals(expectedTasks, prioritized, "task saved with errors")
        );
    }

    @ParameterizedTest
    @MethodSource("io.github.akuniutka.kanban.TestModels#getOverlappingTimeSlots")
    public void shouldUpdateTaskInGetAndTasksAndPrioritizedWhenOverlapPreviousVersion(Duration duration,
            LocalDateTime startTime) {
        final Task oldTask = fromModifiedTask().withId(null).withDuration(duration).withStartTime(startTime).build();
        final long taskId = manager.addTask(oldTask);
        final Task update = fromTestTask().withId(taskId).build();
        final Task expectedTask = fromTestTask().withId(taskId).build();
        final List<Task> expectedTasks = List.of(expectedTask);

        manager.updateTask(update);
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
        final long epicId = manager.addEpic(testEpic);
        final Subtask subtask = fromTestSubtask().withId(null).withEpicId(epicId)
                .withStartTime(TEST_START_TIME.plus(TEST_DURATION)).build();
        final long subtaskId = manager.addSubtask(subtask);
        final long taskId = manager.addTask(modifiedTask);
        final Task update = fromTestTask().withId(taskId).build();
        final Subtask expectedSubtask = fromTestSubtask().withId(subtaskId).withEpicId(epicId)
                .withStartTime(TEST_START_TIME.plus(TEST_DURATION)).build();
        final Task expectedTask = fromTestTask().withId(taskId).build();
        final List<Task> expectedTasks = List.of(expectedTask);
        final List<Task> expectedPrioritized = List.of(expectedTask, expectedSubtask);

        manager.updateTask(update);
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
    public void shouldUpdateTaskInGetAndTasksAndPrioritizedWhenWithDurationTruncatedExactlyBeforeAnotherTask() {
        final long epicId = manager.addEpic(testEpic);
        final Subtask subtask = fromTestSubtask().withId(null).withEpicId(epicId)
                .withStartTime(TEST_START_TIME.plus(TEST_DURATION)).build();
        final long subtaskId = manager.addSubtask(subtask);
        final long taskId = manager.addTask(modifiedTask);
        final Task update = fromTestTask().withId(taskId).withDuration(TEST_DURATION.plusSeconds(25L)).build();
        final Subtask expectedSubtask = fromTestSubtask().withId(subtaskId).withEpicId(epicId)
                .withStartTime(TEST_START_TIME.plus(TEST_DURATION)).build();
        final Task expectedTask = fromTestTask().withId(taskId).build();
        final List<Task> expectedTasks = List.of(expectedTask);
        final List<Task> expectedPrioritized = List.of(expectedTask, expectedSubtask);

        manager.updateTask(update);
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
    public void shouldUpdateTaskInGetAndTasksAndPrioritizedWhenWithStartTimeTruncatedExactlyBeforeAnotherTask() {
        final long epicId = manager.addEpic(testEpic);
        final Subtask subtask = fromTestSubtask().withId(null).withEpicId(epicId)
                .withStartTime(TEST_START_TIME.plus(TEST_DURATION)).build();
        final long subtaskId = manager.addSubtask(subtask);
        final long taskId = manager.addTask(modifiedTask);
        final Task update = fromTestTask().withId(taskId).withStartTime(TEST_START_TIME.plusSeconds(25L)).build();
        final Subtask expectedSubtask = fromTestSubtask().withId(subtaskId).withEpicId(epicId)
                .withStartTime(TEST_START_TIME.plus(TEST_DURATION)).build();
        final Task expectedTask = fromTestTask().withId(taskId).build();
        final List<Task> expectedTasks = List.of(expectedTask);
        final List<Task> expectedPrioritized = List.of(expectedTask, expectedSubtask);

        manager.updateTask(update);
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
        final long epicId = manager.addEpic(testEpic);
        final Subtask subtask = fromTestSubtask().withId(null).withEpicId(epicId)
                .withStartTime(TEST_START_TIME.minus(TEST_DURATION)).build();
        final long subtaskId = manager.addSubtask(subtask);
        final long taskId = manager.addTask(modifiedTask);
        final Task update = fromTestTask().withId(taskId).build();
        final Subtask expectedSubtask = fromTestSubtask().withId(subtaskId).withEpicId(epicId)
                .withStartTime(TEST_START_TIME.minus(TEST_DURATION)).build();
        final Task expectedTask = fromTestTask().withId(taskId).build();
        final List<Task> expectedTasks = List.of(expectedTask);
        final List<Task> expectedPrioritized = List.of(expectedSubtask, expectedTask);

        manager.updateTask(update);
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
    public void shouldUpdateTaskInGetAndTasksAndPrioritizedWhenDurationAndStartTimeBecomeNull() {
        final long taskId = manager.addTask(testTask);
        final Task update = fromModifiedTask().withId(taskId).withDuration(null).withStartTime(null).build();
        final Task expectedTask = fromModifiedTask().withId(taskId).withDuration(null).withStartTime(null).build();
        final List<Task> expectedTasks = List.of(expectedTask);

        manager.updateTask(update);
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
    public void shouldUpdateTaskInGetAndTasksAndPrioritizedWhenDurationAndStartTimeWereNull() {
        final Task oldTask = fromTestTask().withId(null).withDuration(null).withStartTime(null).build();
        final long taskId = manager.addTask(oldTask);
        final Task update = fromModifiedTask().withId(taskId).build();
        final Task expectedTask = fromModifiedTask().withId(taskId).build();
        final List<Task> expectedTasks = List.of(expectedTask);

        manager.updateTask(update);
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
    public void shouldUpdateTaskInGetAndTasksNotPrioritizedWhenDurationAndStartTimeNull() {
        final Task oldTask = fromTestTask().withId(null).withDuration(null).withStartTime(null).build();
        final long taskId = manager.addTask(oldTask);
        final Task update = fromModifiedTask().withId(taskId).withDuration(null).withStartTime(null).build();
        final Task expectedTask = fromModifiedTask().withId(taskId).withDuration(null).withStartTime(null).build();
        final List<Task> expectedTasks = List.of(expectedTask);

        manager.updateTask(update);
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
        final Task update = fromEmptyTask().withId(taskId).withStatus(TaskStatus.NEW).build();
        final Task expectedTask = fromEmptyTask().withId(taskId).withStatus(TaskStatus.NEW).build();
        final List<Task> expectedTasks = List.of(expectedTask);

        manager.updateTask(update);
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
        final Epic expectedEpic = fromTestEpic().withId(epicId).withStatus(TaskStatus.NEW).build();

        final Epic savedEpic = manager.getEpic(epicId);

        assertTaskEquals(expectedEpic, savedEpic, "epic saved with errors");
    }

    @Test
    public void shouldNotAddEpicWhenNull() {
        final String expectedMessage = "cannot add null";

        final Exception exception = assertThrows(NullPointerException.class, () -> manager.addEpic(null));
        assertEquals(expectedMessage, exception.getMessage(), WRONG_EXCEPTION_MESSAGE);
    }

    @Test
    public void shouldNotAddEpicWhenIdExists() {
        final long epicId = manager.addEpic(testEpic);
        final Epic duplicateEpic = fromModifiedEpic().withId(epicId).build();

        final Exception exception = assertThrows(DuplicateIdException.class, () -> manager.addEpic(duplicateEpic));
        assertEquals("duplicate id=" + epicId, exception.getMessage(), WRONG_EXCEPTION_MESSAGE);
    }

    @Test
    public void shouldAddEpicToGetAndEpics() {
        final long epicId = manager.addEpic(testEpic);
        final Epic savedEpic = manager.getEpic(epicId);
        final List<Epic> epics = manager.getEpics();

        final Epic expectedEpic = fromTestEpic().withId(epicId).withStatus(TaskStatus.NEW).build();
        final List<Epic> expectedEpics = List.of(expectedEpic);
        assertAll("epic saved with errors",
                () -> assertTaskEquals(expectedEpic, savedEpic, "epic saved with errors"),
                () -> assertListEquals(expectedEpics, epics, "epic saved with errors")
        );
    }

    @Test
    public void shouldAddEpicToGetAndEpicsWhenIdSetButNotExist() {
        final Epic epic = fromTestEpic().withId(ANOTHER_TEST_ID).build();
        final Epic nextEpic = fromModifiedEpic().withId(null).build();

        final long epicId = manager.addEpic(epic);
        final long nextId = manager.addEpic(nextEpic);
        final Epic savedEpic = manager.getEpic(epicId);
        final List<Epic> epics = manager.getEpics();

        final Epic expectedEpic = fromTestEpic().withId(ANOTHER_TEST_ID).withStatus(TaskStatus.NEW).build();
        final Epic expectedNextEpic = fromModifiedEpic().withId(nextId).withStatus(TaskStatus.NEW).build();
        final List<Epic> expectedEpics = List.of(expectedEpic, expectedNextEpic);
        assertAll("epic saved with errors",
                () -> assertEquals(ANOTHER_TEST_ID, epicId, "epic saved with errors"),
                () -> assertEquals(ANOTHER_TEST_ID + 1, nextId, "epic saved with errors"),
                () -> assertTaskEquals(expectedEpic, savedEpic, "epic saved with errors"),
                () -> assertListEquals(expectedEpics, epics, "epic saved with errors")
        );
    }

    @Test
    public void shouldAddEpicToGetAndEpicsWhenFieldsNull() {
        final long epicId = manager.addEpic(emptyEpic);
        final Epic savedEpic = manager.getEpic(epicId);
        final List<Epic> epics = manager.getEpics();

        final Epic expectedEpic = fromEmptyEpic().withId(epicId).withStatus(TaskStatus.NEW).build();
        final List<Epic> expectedEpics = List.of(expectedEpic);
        assertAll("epic saved with errors",
                () -> assertTaskEquals(expectedEpic, savedEpic, "epic saved with errors"),
                () -> assertListEquals(expectedEpics, epics, "epic saved with errors")
        );
    }

    @Test
    public void shouldNotUpdateEpicWhenNull() {
        final String expectedMessage = "cannot apply null update";

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
        final Epic epic = fromTestEpic().withId(epicId).build();

        final Exception exception = assertThrows(TaskNotFoundException.class, () -> manager.updateEpic(epic));
        assertEquals(expectedMessage, exception.getMessage(), WRONG_EXCEPTION_MESSAGE);
    }

    @Test
    public void shouldUpdateEpicInGetAndEpics() {
        final long epicId = manager.addEpic(testEpic);
        final Epic update = fromModifiedEpic().withId(epicId).build();
        final Epic expectedEpic = fromModifiedEpic().withId(epicId).withStatus(TaskStatus.NEW).build();
        final List<Epic> expectedEpics = List.of(expectedEpic);

        manager.updateEpic(update);
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
        final Epic update = fromEmptyEpic().withId(epicId).build();
        final Epic expectedEpic = fromEmptyEpic().withId(epicId).withStatus(TaskStatus.NEW).build();
        final List<Epic> expectedEpics = List.of(expectedEpic);

        manager.updateEpic(update);
        final Epic savedEpic = manager.getEpic(epicId);
        final List<Epic> epics = manager.getEpics();

        assertAll("epic saved with errors",
                () -> assertTaskEquals(expectedEpic, savedEpic, "epic saved with errors"),
                () -> assertListEquals(expectedEpics, epics, "epic saved with errors")
        );
    }

    @Test
    public void shouldRetainEpicSubtaskIdsWhenUpdate() {
        final long epicId = manager.addEpic(testEpic);
        final Subtask subtask = fromTestSubtask().withId(null).withEpicId(epicId).build();
        final long subtaskId = manager.addSubtask(subtask);
        final Epic update = fromModifiedEpic().withId(epicId).build();
        final Epic expectedEpic = fromModifiedEpic().withId(epicId).withSubtaskIds(List.of(subtaskId))
                .withDuration(TEST_DURATION).withStartTime(TEST_START_TIME).withEndTime(TEST_END_TIME)
                .withStatus(TaskStatus.IN_PROGRESS).build();

        manager.updateEpic(update);
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
        final Subtask subtask = fromTestSubtask().withId(null).withEpicId(epicId).build();
        final long subtaskId = manager.addSubtask(subtask);
        final Subtask expectedSubtask = fromTestSubtask().withId(subtaskId).withEpicId(epicId).build();

        final Subtask savedSubtask = manager.getSubtask(subtaskId);

        assertTaskEquals(expectedSubtask, savedSubtask, "subtask saved with errors");
    }

    @Test
    public void shouldNotAddSubtaskWhenNull() {
        final String expectedMessage = "cannot add null";

        final Exception exception = assertThrows(NullPointerException.class, () -> manager.addSubtask(null));
        assertEquals(expectedMessage, exception.getMessage(), WRONG_EXCEPTION_MESSAGE);
    }

    @Test
    public void shouldNotAddSubtaskWhenIdExists() {
        final long anotherEpicId = manager.addEpic(testEpic);
        final Subtask anotherSubtask = fromTestSubtask().withId(null).withEpicId(anotherEpicId).build();
        final long subtaskId = manager.addSubtask(anotherSubtask);
        final long epicId = manager.addEpic(modifiedEpic);
        final Subtask subtask = fromModifiedSubtask().withId(subtaskId).withEpicId(epicId).build();

        final Exception exception = assertThrows(DuplicateIdException.class, () -> manager.addSubtask(subtask));
        assertEquals("duplicate id=" + subtaskId, exception.getMessage(), WRONG_EXCEPTION_MESSAGE);
    }

    @Test
    public void shouldNotAddSubtaskToNull() {
        final String expectedMessage = "no epic with id=null";
        final Subtask subtask = fromTestSubtask().withId(null).withEpicId(null).build();

        final Exception exception = assertThrows(TaskNotFoundException.class, () -> manager.addSubtask(subtask));
        assertEquals(expectedMessage, exception.getMessage(), WRONG_EXCEPTION_MESSAGE);
    }

    @Test
    public void shouldNotAddSubtaskWhenEpicNotExist() {
        final long epicId = -1L;
        final String expectedMessage = "no epic with id=" + epicId;
        final Subtask subtask = fromTestSubtask().withId(null).withEpicId(epicId).build();

        final Exception exception = assertThrows(TaskNotFoundException.class, () -> manager.addSubtask(subtask));
        assertEquals(expectedMessage, exception.getMessage(), WRONG_EXCEPTION_MESSAGE);
    }

    @Test
    public void shouldNotAddSubtaskToTask() {
        final long taskId = manager.addTask(testTask);
        final String expectedMessage = "no epic with id=" + taskId;
        final Subtask subtask = fromModifiedSubtask().withId(null).withEpicId(taskId).build();

        final Exception exception = assertThrows(TaskNotFoundException.class, () -> manager.addSubtask(subtask));
        assertEquals(expectedMessage, exception.getMessage(), WRONG_EXCEPTION_MESSAGE);
    }

    @Test
    public void shouldNotAddSubtaskToSubtask() {
        final long epicId = manager.addEpic(testEpic);
        final Subtask anotherSubtask = fromTestSubtask().withId(null).withEpicId(epicId).build();
        final long subtaskId = manager.addSubtask(anotherSubtask);
        final String expectedMessage = "no epic with id=" + subtaskId;
        final Subtask subtask = fromModifiedSubtask().withId(null).withEpicId(subtaskId).build();

        final Exception exception = assertThrows(TaskNotFoundException.class, () -> manager.addSubtask(subtask));
        assertEquals(expectedMessage, exception.getMessage(), WRONG_EXCEPTION_MESSAGE);
    }

    @Test
    public void shouldNotAddSubtaskWhenDurationNullAndStartTimeNotNull() {
        final long epicId = manager.addEpic(testEpic);
        final Subtask subtask = fromTestSubtask().withId(null).withEpicId(epicId).withDuration(null).build();

        final Exception exception = assertThrows(ManagerValidationException.class, () -> manager.addSubtask(subtask));
        assertEquals("duration and start time must be either both set or both null", exception.getMessage(),
                WRONG_EXCEPTION_MESSAGE);
    }

    @Test
    public void shouldNotAddSubtaskWhenDurationNotNullAndStartTimeNull() {
        final long epicId = manager.addEpic(testEpic);
        final Subtask subtask = fromTestSubtask().withId(null).withEpicId(epicId).withStartTime(null).build();

        final Exception exception = assertThrows(ManagerValidationException.class, () -> manager.addSubtask(subtask));
        assertEquals("duration and start time must be either both set or both null", exception.getMessage(),
                WRONG_EXCEPTION_MESSAGE);
    }

    @ParameterizedTest
    @MethodSource("io.github.akuniutka.kanban.TestModels#getOverlappingTimeSlots")
    public void shouldNotAddSubtaskWhenOverlapAnotherPrioritizedTask(Duration duration, LocalDateTime startTime) {
        final Task overlappingTask = fromTestTask().withId(null).withDuration(duration).withStartTime(startTime)
                .build();
        manager.addTask(overlappingTask);
        final long epicId = manager.addEpic(testEpic);
        final Subtask subtask = fromTestSubtask().withId(null).withEpicId(epicId).build();

        final Exception exception = assertThrows(ManagerValidationException.class, () -> manager.addSubtask(subtask));
        assertEquals("conflict with another task for time slot", exception.getMessage(), WRONG_EXCEPTION_MESSAGE);
    }

    @Test
    public void shouldNotAddSubtaskWhenStatusNull() {
        final long epicId = manager.addEpic(testEpic);
        final Subtask subtask = fromTestSubtask().withId(null).withEpicId(epicId).withStatus(null).build();

        final Exception exception = assertThrows(ManagerValidationException.class, () -> manager.addSubtask(subtask));
        assertEquals("status cannot be null", exception.getMessage(), WRONG_EXCEPTION_MESSAGE);
    }

    @Test
    public void shouldAddSubtaskToGetAndEpicAndSubtasksAndPrioritizedWhenDurationAndStartTimeNotNull() {
        final long epicId = manager.addEpic(testEpic);
        final Subtask subtask = fromTestSubtask().withId(null).withEpicId(epicId).build();

        final long subtaskId = manager.addSubtask(subtask);
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
        final Subtask subtask = fromTestSubtask().withId(ANOTHER_TEST_ID).withEpicId(epicId).build();
        final Subtask nextSubtask = fromModifiedSubtask().withId(null).withEpicId(epicId).build();

        final long subtaskId = manager.addSubtask(subtask);
        final long nextId = manager.addSubtask(nextSubtask);
        final Subtask savedSubtask = manager.getSubtask(subtaskId);
        final List<Subtask> epicSubtasks = manager.getEpicSubtasks(epicId);
        final List<Subtask> subtasks = manager.getSubtasks();
        final List<Task> prioritized = manager.getPrioritizedTasks();

        final Subtask expectedSubtask = fromTestSubtask().withId(ANOTHER_TEST_ID).withEpicId(epicId).build();
        final Subtask expectedNextSubtask = fromModifiedSubtask().withId(nextId).withEpicId(epicId).build();
        final List<Subtask> expectedSubtasks = List.of(expectedSubtask, expectedNextSubtask);
        assertAll("subtask saved with errors",
                () -> assertEquals(ANOTHER_TEST_ID, subtaskId, "new subtask should have new id"),
                () -> assertEquals(ANOTHER_TEST_ID + 1, nextId, "new subtask should have new id"),
                () -> assertTaskEquals(expectedSubtask, savedSubtask, "subtask saved with errors"),
                () -> assertListEquals(expectedSubtasks, epicSubtasks, "subtask saved with errors"),
                () -> assertListEquals(expectedSubtasks, subtasks, "subtask saved with errors"),
                () -> assertListEquals(expectedSubtasks, prioritized, "subtask saved with errors")
        );
    }

    @Test
    public void shouldAddSubtaskToGetAndEpicAndSubtasksAndPrioritizedWithDurationTruncatedToMinutes() {
        final long epicId = manager.addEpic(testEpic);
        final Subtask subtask = fromTestSubtask().withId(null).withEpicId(epicId)
                .withDuration(TEST_DURATION.plusSeconds(25L)).build();

        final long subtaskId = manager.addSubtask(subtask);
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
    public void shouldAddSubtaskToGetAndEpicAndSubtasksAndPrioritizedWithStartTimeTruncatedToMinutes() {
        final long epicId = manager.addEpic(testEpic);
        final Subtask subtask = fromTestSubtask().withId(null).withEpicId(epicId)
                .withStartTime(TEST_START_TIME.plusSeconds(25L)).build();

        final long subtaskId = manager.addSubtask(subtask);
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
    public void shouldAddSubtaskToGetAndEpicAndSubtasksNotPrioritizedWhenDurationAndStartTimeNull() {
        final long epicId = manager.addEpic(testEpic);
        final Subtask subtask = fromTestSubtask().withId(null).withEpicId(epicId).withDuration(null).withStartTime(null)
                .build();

        final long subtaskId = manager.addSubtask(subtask);
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
        final Subtask subtask = fromEmptySubtask().withEpicId(epicId).withStatus(TaskStatus.NEW).build();

        final long subtaskId = manager.addSubtask(subtask);
        final Subtask savedSubtask = manager.getSubtask(subtaskId);
        final List<Subtask> epicSubtasks = manager.getEpicSubtasks(epicId);
        final List<Subtask> subtasks = manager.getSubtasks();
        final List<Task> prioritized = manager.getPrioritizedTasks();

        final Subtask expectedSubtask = fromEmptySubtask().withId(subtaskId).withEpicId(epicId)
                .withStatus(TaskStatus.NEW).build();
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
        final Task task = fromTestTask().withId(null).withStartTime(TEST_START_TIME.plus(TEST_DURATION)).build();
        final long taskId = manager.addTask(task);
        final long epicId = manager.addEpic(testEpic);
        final Subtask subtask = fromTestSubtask().withId(null).withEpicId(epicId).build();

        final long subtaskId = manager.addSubtask(subtask);
        final Subtask savedSubtask = manager.getSubtask(subtaskId);
        final List<Subtask> epicSubtasks = manager.getEpicSubtasks(epicId);
        final List<Subtask> subtasks = manager.getSubtasks();
        final List<Task> prioritized = manager.getPrioritizedTasks();

        final Task expectedTask = fromTestTask().withId(taskId).withStartTime(TEST_START_TIME.plus(TEST_DURATION))
                .build();
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
    public void shouldAddSubtaskToGetAndEpicAndSubtasksAndPrioritizedWhenWithDurationTruncatedExactlyBeforeTask() {
        final Task task = fromTestTask().withId(null).withStartTime(TEST_START_TIME.plus(TEST_DURATION)).build();
        final long taskId = manager.addTask(task);
        final long epicId = manager.addEpic(testEpic);
        final Subtask subtask = fromTestSubtask().withId(null).withEpicId(epicId)
                .withDuration(TEST_DURATION.plusSeconds(25L)).build();

        final long subtaskId = manager.addSubtask(subtask);
        final Subtask savedSubtask = manager.getSubtask(subtaskId);
        final List<Subtask> epicSubtasks = manager.getEpicSubtasks(epicId);
        final List<Subtask> subtasks = manager.getSubtasks();
        final List<Task> prioritized = manager.getPrioritizedTasks();

        final Task expectedTask = fromTestTask().withId(taskId).withStartTime(TEST_START_TIME.plus(TEST_DURATION))
                .build();
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
    public void shouldAddSubtaskToGetAndEpicAndSubtasksAndPrioritizedWhenWithStartTimeTruncatedExactlyBeforeTask() {
        final Task task = fromTestTask().withId(null).withStartTime(TEST_START_TIME.plus(TEST_DURATION)).build();
        final long taskId = manager.addTask(task);
        final long epicId = manager.addEpic(testEpic);
        final Subtask subtask = fromTestSubtask().withId(null).withEpicId(epicId)
                .withStartTime(TEST_START_TIME.plusSeconds(25L)).build();

        final long subtaskId = manager.addSubtask(subtask);
        final Subtask savedSubtask = manager.getSubtask(subtaskId);
        final List<Subtask> epicSubtasks = manager.getEpicSubtasks(epicId);
        final List<Subtask> subtasks = manager.getSubtasks();
        final List<Task> prioritized = manager.getPrioritizedTasks();

        final Task expectedTask = fromTestTask().withId(taskId).withStartTime(TEST_START_TIME.plus(TEST_DURATION))
                .build();
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
        final Task task = fromTestTask().withId(null).withStartTime(TEST_START_TIME.minus(TEST_DURATION)).build();
        final long taskId = manager.addTask(task);
        final long epicId = manager.addEpic(testEpic);
        final Subtask subtask = fromTestSubtask().withId(null).withEpicId(epicId).build();

        final long subtaskId = manager.addSubtask(subtask);
        final Subtask savedSubtask = manager.getSubtask(subtaskId);
        final List<Subtask> epicSubtasks = manager.getEpicSubtasks(epicId);
        final List<Subtask> subtasks = manager.getSubtasks();
        final List<Task> prioritized = manager.getPrioritizedTasks();

        final Task expectedTask = fromTestTask().withId(taskId).withStartTime(TEST_START_TIME.minus(TEST_DURATION))
                .build();
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
        final String expectedMessage = "cannot apply null update";

        final Exception exception = assertThrows(NullPointerException.class, () -> manager.updateSubtask(null));
        assertEquals(expectedMessage, exception.getMessage(), WRONG_EXCEPTION_MESSAGE);
    }

    @Test
    public void shouldNotUpdateSubtaskWhenIdNull() {
        final String expectedMessage = "no subtask with id=null";
        final long epicId = manager.addEpic(testEpic);
        final Subtask subtask = fromTestSubtask().withId(null).withEpicId(epicId).build();

        final Exception exception = assertThrows(TaskNotFoundException.class, () -> manager.updateSubtask(subtask));
        assertEquals(expectedMessage, exception.getMessage(), WRONG_EXCEPTION_MESSAGE);
    }

    @Test
    public void shouldNotUpdateSubtaskWhenNotExist() {
        final long subtaskId = -1L;
        final String expectedMessage = "no subtask with id=" + subtaskId;
        final long epicId = manager.addEpic(testEpic);
        final Subtask subtask = fromTestSubtask().withId(subtaskId).withEpicId(epicId).build();

        final Exception exception = assertThrows(TaskNotFoundException.class, () -> manager.updateSubtask(subtask));
        assertEquals(expectedMessage, exception.getMessage(), WRONG_EXCEPTION_MESSAGE);
    }

    @Test
    public void shouldNotUpdateSubtaskWhenDurationNullAndStartTimeNotNull() {
        final long epicId = manager.addEpic(testEpic);
        final Subtask oldSubtask = fromTestSubtask().withId(null).withEpicId(epicId).build();
        final long subtaskId = manager.addSubtask(oldSubtask);
        final Subtask update = fromModifiedSubtask().withId(subtaskId).withDuration(null).build();

        final Exception exception = assertThrows(ManagerValidationException.class, () -> manager.updateSubtask(update));
        assertEquals("duration and start time must be either both set or both null", exception.getMessage(),
                WRONG_EXCEPTION_MESSAGE);
    }

    @Test
    public void shouldNotUpdateSubtaskWhenDurationNotNullAndStartTimeNull() {
        final long epicId = manager.addEpic(testEpic);
        final Subtask oldSubtask = fromTestSubtask().withId(null).withEpicId(epicId).build();
        final long subtaskId = manager.addSubtask(oldSubtask);
        final Subtask update = fromModifiedSubtask().withId(subtaskId).withStartTime(null).build();

        final Exception exception = assertThrows(ManagerValidationException.class, () -> manager.updateSubtask(update));
        assertEquals("duration and start time must be either both set or both null", exception.getMessage(),
                WRONG_EXCEPTION_MESSAGE);
    }

    @ParameterizedTest
    @MethodSource("io.github.akuniutka.kanban.TestModels#getOverlappingTimeSlots")
    public void shouldNotUpdateSubtaskWhenOverlapAnotherPrioritizedTask(Duration duration, LocalDateTime startTime) {
        final Task overlappingTask = fromTestTask().withId(null).withDuration(duration).withStartTime(startTime)
                .build();
        manager.addTask(overlappingTask);
        final long epicId = manager.addEpic(testEpic);
        final Subtask oldSubtask = fromModifiedSubtask().withId(null).withEpicId(epicId).build();
        final long subtaskId = manager.addSubtask(oldSubtask);
        final Subtask update = fromTestSubtask().withId(subtaskId).build();

        final Exception exception = assertThrows(ManagerValidationException.class, () -> manager.updateSubtask(update));
        assertEquals("conflict with another task for time slot", exception.getMessage(), WRONG_EXCEPTION_MESSAGE);
    }

    @Test
    public void shouldNotUpdateSubtaskWhenStatusNull() {
        final long epicId = manager.addEpic(testEpic);
        final Subtask oldSubtask = fromTestSubtask().withId(null).withEpicId(epicId).build();
        final long subtaskId = manager.addSubtask(oldSubtask);
        final Subtask update = fromModifiedSubtask().withId(subtaskId).withStatus(null).build();

        final Exception exception = assertThrows(ManagerValidationException.class, () -> manager.updateSubtask(update));
        assertEquals("status cannot be null", exception.getMessage(), WRONG_EXCEPTION_MESSAGE);
    }

    @Test
    public void shouldUpdateSubtaskInGetAndEpicAndSubtasksAndPrioritizedWhenDurationAndStartTimeNotNull() {
        final long epicId = manager.addEpic(testEpic);
        final Subtask oldSubtask = fromTestSubtask().withId(null).withEpicId(epicId).build();
        final long subtaskId = manager.addSubtask(oldSubtask);
        final Subtask update = fromModifiedSubtask().withId(subtaskId).build();
        final Subtask expectedSubtask = fromModifiedSubtask().withId(subtaskId).withEpicId(epicId).build();
        final List<Subtask> expectedSubtasks = List.of(expectedSubtask);

        manager.updateSubtask(update);
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
    public void shouldUpdateSubtaskInGetAndEpicAndSubtasksAndPrioritizedWithDurationTruncatedToMinutes() {
        final long epicId = manager.addEpic(testEpic);
        final Subtask oldSubtask = fromTestSubtask().withId(null).withEpicId(epicId).build();
        final long subtaskId = manager.addSubtask(oldSubtask);
        final Subtask update = fromModifiedSubtask().withId(subtaskId)
                .withDuration(MODIFIED_DURATION.plusSeconds(25L)).build();
        final Subtask expectedSubtask = fromModifiedSubtask().withId(subtaskId).withEpicId(epicId).build();
        final List<Subtask> expectedSubtasks = List.of(expectedSubtask);

        manager.updateSubtask(update);
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
    public void shouldUpdateSubtaskInGetAndEpicAndSubtasksAndPrioritizedWithStartTimeTruncatedToMinutes() {
        final long epicId = manager.addEpic(testEpic);
        final Subtask oldSubtask = fromTestSubtask().withId(null).withEpicId(epicId).build();
        final long subtaskId = manager.addSubtask(oldSubtask);
        final Subtask update = fromModifiedSubtask().withId(subtaskId)
                .withStartTime(MODIFIED_START_TIME.plusSeconds(25L)).build();
        final Subtask expectedSubtask = fromModifiedSubtask().withId(subtaskId).withEpicId(epicId).build();
        final List<Subtask> expectedSubtasks = List.of(expectedSubtask);

        manager.updateSubtask(update);
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

    @ParameterizedTest
    @MethodSource("io.github.akuniutka.kanban.TestModels#getOverlappingTimeSlots")
    public void shouldUpdateSubtaskInGetAndEpicAndSubtasksAndPrioritizedWhenOverlapPreviousVersion(Duration duration,
            LocalDateTime startTime) {
        final long epicId = manager.addEpic(testEpic);
        final Subtask oldSubtask = fromModifiedSubtask().withId(null).withEpicId(epicId).withDuration(duration)
                .withStartTime(startTime).build();
        final long subtaskId = manager.addSubtask(oldSubtask);
        final Subtask update = fromTestSubtask().withId(subtaskId).build();
        final Subtask expectedSubtask = fromTestSubtask().withId(subtaskId).withEpicId(epicId).build();
        final List<Subtask> expectedSubtasks = List.of(expectedSubtask);

        manager.updateSubtask(update);
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
        final Task task = fromTestTask().withId(null).withStartTime(TEST_START_TIME.plus(TEST_DURATION)).build();
        final long taskId = manager.addTask(task);
        final long epicId = manager.addEpic(testEpic);
        final Subtask oldSubtask = fromModifiedSubtask().withId(null).withEpicId(epicId).build();
        final long subtaskId = manager.addSubtask(oldSubtask);
        final Subtask update = fromTestSubtask().withId(subtaskId).build();
        final Task expectedTask = fromTestTask().withId(taskId).withStartTime(TEST_START_TIME.plus(TEST_DURATION))
                .build();
        final Subtask expectedSubtask = fromTestSubtask().withId(subtaskId).withEpicId(epicId).build();
        final List<Subtask> expectedSubtasks = List.of(expectedSubtask);
        final List<Task> expectedPrioritized = List.of(expectedSubtask, expectedTask);

        manager.updateSubtask(update);
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
    public void shouldUpdateSubtaskInGetAndEpicAndSubtasksAndPrioritizedWhenWithDurationTruncatedExactlyBeforeTask() {
        final Task task = fromTestTask().withId(null).withStartTime(TEST_START_TIME.plus(TEST_DURATION)).build();
        final long taskId = manager.addTask(task);
        final long epicId = manager.addEpic(testEpic);
        final Subtask oldSubtask = fromModifiedSubtask().withId(null).withEpicId(epicId).build();
        final long subtaskId = manager.addSubtask(oldSubtask);
        final Subtask update = fromTestSubtask().withId(subtaskId).withDuration(TEST_DURATION.plusSeconds(25L)).build();
        final Task expectedTask = fromTestTask().withId(taskId).withStartTime(TEST_START_TIME.plus(TEST_DURATION))
                .build();
        final Subtask expectedSubtask = fromTestSubtask().withId(subtaskId).withEpicId(epicId).build();
        final List<Subtask> expectedSubtasks = List.of(expectedSubtask);
        final List<Task> expectedPrioritized = List.of(expectedSubtask, expectedTask);

        manager.updateSubtask(update);
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
    public void shouldUpdateSubtaskInGetAndEpicAndSubtasksAndPrioritizedWhenWithStartTimeTruncatedExactlyBeforeTask() {
        final Task task = fromTestTask().withId(null).withStartTime(TEST_START_TIME.plus(TEST_DURATION)).build();
        final long taskId = manager.addTask(task);
        final long epicId = manager.addEpic(testEpic);
        final Subtask oldSubtask = fromModifiedSubtask().withId(null).withEpicId(epicId).build();
        final long subtaskId = manager.addSubtask(oldSubtask);
        final Subtask update = fromTestSubtask().withId(subtaskId).withStartTime(TEST_START_TIME.plusSeconds(25L))
                .build();
        final Task expectedTask = fromTestTask().withId(taskId).withStartTime(TEST_START_TIME.plus(TEST_DURATION))
                .build();
        final Subtask expectedSubtask = fromTestSubtask().withId(subtaskId).withEpicId(epicId).build();
        final List<Subtask> expectedSubtasks = List.of(expectedSubtask);
        final List<Task> expectedPrioritized = List.of(expectedSubtask, expectedTask);

        manager.updateSubtask(update);
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
        final Task task = fromTestTask().withId(null).withStartTime(TEST_START_TIME.minus(TEST_DURATION)).build();
        final long taskId = manager.addTask(task);
        final long epicId = manager.addEpic(testEpic);
        final Subtask oldSubtask = fromModifiedSubtask().withId(null).withEpicId(epicId).build();
        final long subtaskId = manager.addSubtask(oldSubtask);
        final Subtask update = fromTestSubtask().withId(subtaskId).build();
        final Task expectedTask = fromTestTask().withId(taskId).withStartTime(TEST_START_TIME.minus(TEST_DURATION))
                .build();
        final Subtask expectedSubtask = fromTestSubtask().withId(subtaskId).withEpicId(epicId).build();
        final List<Subtask> expectedSubtasks = List.of(expectedSubtask);
        final List<Task> expectedPrioritized = List.of(expectedTask, expectedSubtask);

        manager.updateSubtask(update);
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
    public void shouldUpdateSubtaskInGetAndEpicAndSubtasksAndPrioritizedWhenDurationAndStartTimeBecomeNull() {
        final long epicId = manager.addEpic(testEpic);
        final Subtask oldSubtask = fromTestSubtask().withId(null).withEpicId(epicId).build();
        final long subtaskId = manager.addSubtask(oldSubtask);
        final Subtask update = fromModifiedSubtask().withId(subtaskId).withDuration(null).withStartTime(null).build();
        final Subtask expectedSubtask = fromModifiedSubtask().withId(subtaskId).withEpicId(epicId).withDuration(null)
                .withStartTime(null).build();
        final List<Subtask> expectedSubtasks = List.of(expectedSubtask);

        manager.updateSubtask(update);
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
    public void shouldUpdateSubtaskInGetAndEpicAndSubtasksAndPrioritizedWhenDurationAndStartTimeWereNull() {
        final long epicId = manager.addEpic(testEpic);
        final Subtask oldSubtask = fromTestSubtask().withId(null).withEpicId(epicId).withDuration(null)
                .withStartTime(null).build();
        final long subtaskId = manager.addSubtask(oldSubtask);
        final Subtask update = fromModifiedSubtask().withId(subtaskId).build();
        final Subtask expectedSubtask = fromModifiedSubtask().withId(subtaskId).withEpicId(epicId).build();
        final List<Subtask> expectedSubtasks = List.of(expectedSubtask);

        manager.updateSubtask(update);
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
    public void shouldUpdateSubtaskInGetAndEpicAndSubtasksNotPrioritizedWhenDurationAndStartTimeNull() {
        final long epicId = manager.addEpic(testEpic);
        final Subtask oldSubtask = fromTestSubtask().withId(null).withEpicId(epicId).withDuration(null)
                .withStartTime(null).build();
        final long subtaskId = manager.addSubtask(oldSubtask);
        final Subtask update = fromModifiedSubtask().withId(subtaskId).withDuration(null).withStartTime(null).build();
        final Subtask expectedSubtask = fromModifiedSubtask().withId(subtaskId).withEpicId(epicId).withDuration(null)
                .withStartTime(null).build();
        final List<Subtask> expectedSubtasks = List.of(expectedSubtask);

        manager.updateSubtask(update);
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
        final Subtask oldSubtask = fromTestSubtask().withId(null).withEpicId(epicId).build();
        final long subtaskId = manager.addSubtask(oldSubtask);
        final Subtask update = fromEmptySubtask().withId(subtaskId).withStatus(TaskStatus.NEW).build();
        final Subtask expectedSubtask = fromEmptySubtask().withId(subtaskId).withEpicId(epicId)
                .withStatus(TaskStatus.NEW).build();
        final List<Subtask> expectedSubtasks = List.of(expectedSubtask);

        manager.updateSubtask(update);
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
        final Subtask subtask = fromTestSubtask().withId(null).withEpicId(epicId).build();
        final long subtaskId = manager.addSubtask(subtask);

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
        final Subtask subtask = fromTestSubtask().withId(null).withEpicId(epicId).build();
        final long subtaskId = manager.addSubtask(subtask);

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
    public void shouldSetEpicDurationNullWhenAddEpicAndDurationNull() {
        final long epicId = manager.addEpic(testEpic);
        final Duration duration = manager.getEpic(epicId).getDuration();

        assertNull(duration, "wrong epic duration");
    }

    @Test
    public void shouldSetEpicDurationNullWhenAddEpicAndDurationNotNull() {
        final Epic epic = fromTestEpic().withId(null).withDuration(TEST_DURATION).build();

        final long epicId = manager.addEpic(epic);
        final Duration duration = manager.getEpic(epicId).getDuration();

        assertNull(duration, "wrong epic duration");
    }

    @Test
    public void shouldSetEpicDurationNullWhenAddSubtasksAndSubtasksDurationNull() {
        final long epicId = manager.addEpic(testEpic);
        final Subtask subtaskA = fromEmptySubtask().withId(null).withEpicId(epicId).withStatus(TaskStatus.NEW).build();
        final Subtask subtaskB = fromTestSubtask().withId(null).withEpicId(epicId).withDuration(null)
                .withStartTime(null).build();
        final Subtask subtaskC = fromModifiedSubtask().withId(null).withEpicId(epicId).withDuration(null)
                .withStartTime(null).build();

        manager.addSubtask(subtaskA);
        manager.addSubtask(subtaskB);
        manager.addSubtask(subtaskC);
        final Duration duration = manager.getEpic(epicId).getDuration();

        assertNull(duration, "wrong epic duration");
    }

    @Test
    public void shouldSetEpicDurationNotNullWhenAddSubtasksAndSubtasksDurationNotNull() {
        final long epicId = manager.addEpic(testEpic);
        final Subtask subtaskA = fromEmptySubtask().withId(null).withEpicId(epicId).withStatus(TaskStatus.NEW).build();
        final Subtask subtaskB = fromTestSubtask().withId(null).withEpicId(epicId).build();
        final Subtask subtaskC = fromModifiedSubtask().withId(null).withEpicId(epicId).build();

        manager.addSubtask(subtaskA);
        manager.addSubtask(subtaskB);
        manager.addSubtask(subtaskC);
        final Duration duration = manager.getEpic(epicId).getDuration();

        assertEquals(TEST_DURATION.plus(MODIFIED_DURATION), duration, "wrong epic duration");
    }

    @Test
    public void shouldSetEpicDurationNullWhenUpdateSubtasksAndSubtasksDurationNull() {
        final long epicId = manager.addEpic(testEpic);
        final Subtask subtaskA = fromEmptySubtask().withId(null).withEpicId(epicId).withStatus(TaskStatus.NEW).build();
        final Subtask subtaskB = fromTestSubtask().withId(null).withEpicId(epicId).build();
        final Subtask subtaskC = fromModifiedSubtask().withId(null).withEpicId(epicId).build();
        manager.addSubtask(subtaskA);
        final long subtaskBId = manager.addSubtask(subtaskB);
        final long subtaskCId = manager.addSubtask(subtaskC);
        final Subtask updateB = fromTestSubtask().withId(subtaskBId).withDuration(null).withStartTime(null).build();
        final Subtask updateC = fromModifiedSubtask().withId(subtaskCId).withDuration(null).withStartTime(null).build();

        manager.updateSubtask(updateB);
        manager.updateSubtask(updateC);
        final Duration duration = manager.getEpic(epicId).getDuration();

        assertNull(duration, "wrong epic duration");
    }

    @Test
    public void shouldSetEpicDurationNotNullWhenUpdateSubtasksAndSubtasksDurationNotNull() {
        final long epicId = manager.addEpic(testEpic);
        final Subtask subtaskA = fromEmptySubtask().withId(null).withEpicId(epicId).withStatus(TaskStatus.NEW).build();
        final Subtask subtaskB = fromTestSubtask().withId(null).withEpicId(epicId).withDuration(null)
                .withStartTime(null).build();
        final Subtask subtaskC = fromModifiedSubtask().withId(null).withEpicId(epicId).withDuration(null)
                .withStartTime(null).build();
        manager.addSubtask(subtaskA);
        final long subtaskBId = manager.addSubtask(subtaskB);
        final long subtaskCId = manager.addSubtask(subtaskC);
        final Subtask updateB = fromTestSubtask().withId(subtaskBId).build();
        final Subtask updateC = fromModifiedSubtask().withId(subtaskCId).build();

        manager.updateSubtask(updateB);
        manager.updateSubtask(updateC);
        final Duration duration = manager.getEpic(epicId).getDuration();

        assertEquals(TEST_DURATION.plus(MODIFIED_DURATION), duration, "wrong epic duration");
    }

    @Test
    public void shouldSetEpicDurationNullWhenRemoveSubtaskAndNoSubtasksLeft() {
        final long epicId = manager.addEpic(testEpic);
        final Subtask subtaskA = fromEmptySubtask().withId(null).withEpicId(epicId).withStatus(TaskStatus.NEW).build();
        final Subtask subtaskB = fromTestSubtask().withId(null).withEpicId(epicId).build();
        final Subtask subtaskC = fromModifiedSubtask().withId(null).withEpicId(epicId).build();
        final long subtaskAId = manager.addSubtask(subtaskA);
        final long subtaskBId = manager.addSubtask(subtaskB);
        final long subtaskCId = manager.addSubtask(subtaskC);

        manager.removeSubtask(subtaskAId);
        manager.removeSubtask(subtaskBId);
        manager.removeSubtask(subtaskCId);
        final Duration duration = manager.getEpic(epicId).getDuration();

        assertNull(duration, "wrong epic duration");
    }

    @Test
    public void shouldSetEpicDurationNullWhenRemoveSubtaskAndSubtasksDurationNull() {
        final long epicId = manager.addEpic(testEpic);
        final Subtask subtaskA = fromEmptySubtask().withId(null).withEpicId(epicId).withStatus(TaskStatus.NEW).build();
        final Subtask subtaskB = fromTestSubtask().withId(null).withEpicId(epicId).build();
        final Subtask subtaskC = fromModifiedSubtask().withId(null).withEpicId(epicId).build();
        manager.addSubtask(subtaskA);
        final long subtaskBId = manager.addSubtask(subtaskB);
        final long subtaskCId = manager.addSubtask(subtaskC);

        manager.removeSubtask(subtaskBId);
        manager.removeSubtask(subtaskCId);
        final Duration duration = manager.getEpic(epicId).getDuration();

        assertNull(duration, "wrong epic duration");
    }

    @Test
    public void shouldSetEpicDurationNotNullWhenRemoveSubtaskAndAggregateDurationNotChanged() {
        final long epicId = manager.addEpic(testEpic);
        final Subtask subtaskA = fromEmptySubtask().withId(null).withEpicId(epicId).withStatus(TaskStatus.NEW).build();
        final Subtask subtaskB = fromTestSubtask().withId(null).withEpicId(epicId).build();
        final Subtask subtaskC = fromModifiedSubtask().withId(null).withEpicId(epicId).build();
        final long subtaskAId = manager.addSubtask(subtaskA);
        manager.addSubtask(subtaskB);
        manager.addSubtask(subtaskC);

        manager.removeSubtask(subtaskAId);
        final Duration duration = manager.getEpic(epicId).getDuration();

        assertEquals(TEST_DURATION.plus(MODIFIED_DURATION), duration, "wrong epic duration");
    }

    @Test
    public void shouldSetEpicDurationNotNullWhenRemoveSubtaskAndAggregateDurationChanged() {
        final long epicId = manager.addEpic(testEpic);
        final Subtask subtaskA = fromEmptySubtask().withId(null).withEpicId(epicId).withStatus(TaskStatus.NEW).build();
        final Subtask subtaskB = fromTestSubtask().withId(null).withEpicId(epicId).build();
        final Subtask subtaskC = fromModifiedSubtask().withId(null).withEpicId(epicId).build();
        final long subtaskAId = manager.addSubtask(subtaskA);
        final long subtaskBId = manager.addSubtask(subtaskB);
        manager.addSubtask(subtaskC);

        manager.removeSubtask(subtaskAId);
        manager.removeSubtask(subtaskBId);
        final Duration duration = manager.getEpic(epicId).getDuration();

        assertEquals(MODIFIED_DURATION, duration, "wrong epic duration");
    }

    @Test
    public void shouldRetainEpicDurationWhenUpdateEpicAndDurationNull() {
        final long epicId = manager.addEpic(testEpic);
        final Subtask subtaskA = fromEmptySubtask().withId(null).withEpicId(epicId).withStatus(TaskStatus.NEW).build();
        final Subtask subtaskB = fromTestSubtask().withId(null).withEpicId(epicId).withDuration(null)
                .withStartTime(null).build();
        final Subtask subtaskC = fromModifiedSubtask().withId(null).withEpicId(epicId).withDuration(null)
                .withStartTime(null).build();
        manager.addSubtask(subtaskA);
        manager.addSubtask(subtaskB);
        manager.addSubtask(subtaskC);
        final Epic update = fromModifiedEpic().withId(epicId).withDuration(TEST_DURATION).build();

        manager.updateEpic(update);
        final Duration duration = manager.getEpic(epicId).getDuration();

        assertNull(duration, "wrong epic duration");
    }

    @Test
    public void shouldRetainEpicDurationWhenUpdateEpicAndDurationNotNull() {
        final long epicId = manager.addEpic(testEpic);
        final Subtask subtaskA = fromEmptySubtask().withId(null).withEpicId(epicId).withStatus(TaskStatus.NEW).build();
        final Subtask subtaskB = fromTestSubtask().withId(null).withEpicId(epicId).build();
        final Subtask subtaskC = fromModifiedSubtask().withId(null).withEpicId(epicId).build();
        manager.addSubtask(subtaskA);
        manager.addSubtask(subtaskB);
        manager.addSubtask(subtaskC);
        final Epic update = fromModifiedEpic().withId(epicId).build();

        manager.updateEpic(update);
        final Duration duration = manager.getEpic(epicId).getDuration();

        assertEquals(TEST_DURATION.plus(MODIFIED_DURATION), duration, "wrong epic duration");
    }

    @Test
    public void shouldSetEpicDurationNullWhenRemoveSubtasksAndSubtasksDurationNull() {
        final long epicId = manager.addEpic(testEpic);
        final Subtask subtaskA = fromEmptySubtask().withId(null).withEpicId(epicId).withStatus(TaskStatus.NEW).build();
        final Subtask subtaskB = fromTestSubtask().withId(null).withEpicId(epicId).withDuration(null)
                .withStartTime(null).build();
        final Subtask subtaskC = fromModifiedSubtask().withId(null).withEpicId(epicId).withDuration(null)
                .withStartTime(null).build();
        manager.addSubtask(subtaskA);
        manager.addSubtask(subtaskB);
        manager.addSubtask(subtaskC);

        manager.removeSubtasks();
        final Duration duration = manager.getEpic(epicId).getDuration();

        assertNull(duration, "wrong epic duration");
    }

    @Test
    public void shouldSetEpicDurationNullWhenRemoveSubtasksAndSubtasksDurationNotNull() {
        final long epicId = manager.addEpic(testEpic);
        final Subtask subtaskA = fromEmptySubtask().withId(null).withEpicId(epicId).withStatus(TaskStatus.NEW).build();
        final Subtask subtaskB = fromTestSubtask().withId(null).withEpicId(epicId).build();
        final Subtask subtaskC = fromModifiedSubtask().withId(null).withEpicId(epicId).build();
        manager.addSubtask(subtaskA);
        manager.addSubtask(subtaskB);
        manager.addSubtask(subtaskC);

        manager.removeSubtasks();
        final Duration duration = manager.getEpic(epicId).getDuration();

        assertNull(duration, "wrong epic duration");
    }

    @Test
    public void shouldSetEpicStartTimeNullWhenAddEpicAndStartTimeNull() {
        final long epicId = manager.addEpic(testEpic);
        final LocalDateTime startTime = manager.getEpic(epicId).getStartTime();

        assertNull(startTime, "wrong epic start time");
    }

    @Test
    public void shouldSetEpicStartTimeNullWhenAddEpicAndStartTimeNotNull() {
        final Epic epic = fromTestEpic().withId(null).withStartTime(TEST_START_TIME).build();

        final long epicId = manager.addEpic(epic);
        final LocalDateTime startTime = manager.getEpic(epicId).getStartTime();

        assertNull(startTime, "wrong epic start time");
    }

    @Test
    public void shouldSetEpicStartTimeNullWhenAddSubtasksAndSubtasksStartTimeNull() {
        final long epicId = manager.addEpic(testEpic);
        final Subtask subtaskA = fromEmptySubtask().withId(null).withEpicId(epicId).withStatus(TaskStatus.NEW).build();
        final Subtask subtaskB = fromTestSubtask().withId(null).withEpicId(epicId).withDuration(null)
                .withStartTime(null).build();
        final Subtask subtaskC = fromModifiedSubtask().withId(null).withEpicId(epicId).withDuration(null)
                .withStartTime(null).build();

        manager.addSubtask(subtaskA);
        manager.addSubtask(subtaskB);
        manager.addSubtask(subtaskC);
        final LocalDateTime startTime = manager.getEpic(epicId).getStartTime();

        assertNull(startTime, "wrong epic start time");
    }

    @Test
    public void shouldSetEpicMinStartTimeWhenAddSubtasksAndSubtasksStartTimeNotNull() {
        final long epicId = manager.addEpic(testEpic);
        final Subtask subtaskA = fromEmptySubtask().withId(null).withEpicId(epicId).withStatus(TaskStatus.NEW).build();
        final Subtask subtaskB = fromTestSubtask().withId(null).withEpicId(epicId).build();
        final Subtask subtaskC = fromModifiedSubtask().withId(null).withEpicId(epicId).build();

        manager.addSubtask(subtaskA);
        manager.addSubtask(subtaskB);
        manager.addSubtask(subtaskC);
        final LocalDateTime startTime = manager.getEpic(epicId).getStartTime();

        assertEquals(TEST_START_TIME, startTime, "wrong epic start time");
    }

    @Test
    public void shouldSetEpicMinStartTimeWhenAddSubtasksAndSubtasksStartTimeNotNullAndInOppositeOrder() {
        final long epicId = manager.addEpic(testEpic);
        final Subtask subtaskA = fromEmptySubtask().withId(null).withEpicId(epicId).withStatus(TaskStatus.NEW).build();
        final Subtask subtaskB = fromTestSubtask().withId(null).withEpicId(epicId).build();
        final Subtask subtaskC = fromModifiedSubtask().withId(null).withEpicId(epicId).build();

        manager.addSubtask(subtaskC);
        manager.addSubtask(subtaskB);
        manager.addSubtask(subtaskA);
        final LocalDateTime startTime = manager.getEpic(epicId).getStartTime();

        assertEquals(TEST_START_TIME, startTime, "wrong epic start time");
    }

    @Test
    public void shouldSetEpicStartTimeNullWhenUpdateSubtasksAndSubtasksStartTimeNull() {
        final long epicId = manager.addEpic(testEpic);
        final Subtask subtaskA = fromEmptySubtask().withId(null).withEpicId(epicId).withStatus(TaskStatus.NEW).build();
        final Subtask subtaskB = fromTestSubtask().withId(null).withEpicId(epicId).build();
        final Subtask subtaskC = fromModifiedSubtask().withId(null).withEpicId(epicId).build();
        manager.addSubtask(subtaskA);
        final long subtaskBId = manager.addSubtask(subtaskB);
        final long subtaskCId = manager.addSubtask(subtaskC);
        final Subtask updateB = fromTestSubtask().withId(subtaskBId).withDuration(null).withStartTime(null).build();
        final Subtask updateC = fromModifiedSubtask().withId(subtaskCId).withDuration(null).withStartTime(null).build();

        manager.updateSubtask(updateB);
        manager.updateSubtask(updateC);
        final LocalDateTime startTime = manager.getEpic(epicId).getStartTime();

        assertNull(startTime, "wrong epic start time");
    }

    @Test
    public void shouldSetEpicMinStartTimeWhenUpdateSubtasksAndSubtasksStartTimeNotNull() {
        final long epicId = manager.addEpic(testEpic);
        final Subtask subtaskA = fromEmptySubtask().withId(null).withEpicId(epicId).withStatus(TaskStatus.NEW).build();
        final Subtask subtaskB = fromTestSubtask().withId(null).withEpicId(epicId).withDuration(null)
                .withStartTime(null).build();
        final Subtask subtaskC = fromModifiedSubtask().withId(null).withEpicId(epicId).withDuration(null)
                .withStartTime(null).build();
        manager.addSubtask(subtaskA);
        final long subtaskBId = manager.addSubtask(subtaskB);
        final long subtaskCId = manager.addSubtask(subtaskC);
        final Subtask updateB = fromTestSubtask().withId(subtaskBId).build();
        final Subtask updateC = fromModifiedSubtask().withId(subtaskCId).build();

        manager.updateSubtask(updateB);
        manager.updateSubtask(updateC);
        final LocalDateTime startTime = manager.getEpic(epicId).getStartTime();

        assertEquals(TEST_START_TIME, startTime, "wrong epic start time");
    }

    @Test
    public void shouldSetEpicMinStartTimeWhenUpdateSubtasksAndSubtasksStartTimeNotNullInOppositeOrder() {
        final long epicId = manager.addEpic(testEpic);
        final Subtask subtaskA = fromEmptySubtask().withId(null).withEpicId(epicId).withStatus(TaskStatus.NEW).build();
        final Subtask subtaskB = fromTestSubtask().withId(null).withEpicId(epicId).withDuration(null)
                .withStartTime(null).build();
        final Subtask subtaskC = fromModifiedSubtask().withId(null).withEpicId(epicId).withDuration(null)
                .withStartTime(null).build();
        manager.addSubtask(subtaskA);
        final long subtaskBId = manager.addSubtask(subtaskB);
        final long subtaskCId = manager.addSubtask(subtaskC);
        final Subtask updateB = fromTestSubtask().withId(subtaskBId).build();
        final Subtask updateC = fromModifiedSubtask().withId(subtaskCId).build();

        manager.updateSubtask(updateC);
        manager.updateSubtask(updateB);
        final LocalDateTime startTime = manager.getEpic(epicId).getStartTime();

        assertEquals(TEST_START_TIME, startTime, "wrong epic start time");
    }

    @Test
    public void shouldSetEpicStartTimeNullWhenRemoveSubtaskAndNoSubtasksLeft() {
        final long epicId = manager.addEpic(testEpic);
        final Subtask subtaskA = fromEmptySubtask().withId(null).withEpicId(epicId).withStatus(TaskStatus.NEW).build();
        final Subtask subtaskB = fromTestSubtask().withId(null).withEpicId(epicId).build();
        final Subtask subtaskC = fromModifiedSubtask().withId(null).withEpicId(epicId).build();
        final long subtaskAId = manager.addSubtask(subtaskA);
        final long subtaskBId = manager.addSubtask(subtaskB);
        final long subtaskCId = manager.addSubtask(subtaskC);

        manager.removeSubtask(subtaskAId);
        manager.removeSubtask(subtaskBId);
        manager.removeSubtask(subtaskCId);
        final LocalDateTime startTime = manager.getEpic(epicId).getStartTime();

        assertNull(startTime, "wrong epic start time");
    }

    @Test
    public void shouldSetEpicStartTimeNullWhenRemoveSubtaskAndSubtasksStartTimeNull() {
        final long epicId = manager.addEpic(testEpic);
        final Subtask subtaskA = fromEmptySubtask().withId(null).withEpicId(epicId).withStatus(TaskStatus.NEW).build();
        final Subtask subtaskB = fromTestSubtask().withId(null).withEpicId(epicId).build();
        final Subtask subtaskC = fromModifiedSubtask().withId(null).withEpicId(epicId).build();
        manager.addSubtask(subtaskA);
        final long subtaskBId = manager.addSubtask(subtaskB);
        final long subtaskCId = manager.addSubtask(subtaskC);

        manager.removeSubtask(subtaskBId);
        manager.removeSubtask(subtaskCId);
        final LocalDateTime startTime = manager.getEpic(epicId).getStartTime();

        assertNull(startTime, "wrong epic start time");
    }

    @Test
    public void shouldSetEpicMinStartTimeWhenRemoveSubtaskAndMinStartTimeNotChanged() {
        final long epicId = manager.addEpic(testEpic);
        final Subtask subtaskA = fromEmptySubtask().withId(null).withEpicId(epicId).withStatus(TaskStatus.NEW).build();
        final Subtask subtaskB = fromTestSubtask().withId(null).withEpicId(epicId).build();
        final Subtask subtaskC = fromModifiedSubtask().withId(null).withEpicId(epicId).build();
        final long subtaskAId = manager.addSubtask(subtaskA);
        manager.addSubtask(subtaskB);
        final long subtaskCId = manager.addSubtask(subtaskC);

        manager.removeSubtask(subtaskAId);
        manager.removeSubtask(subtaskCId);
        final LocalDateTime startTime = manager.getEpic(epicId).getStartTime();

        assertEquals(TEST_START_TIME, startTime, "wrong epic start time");
    }

    @Test
    public void shouldSetEpicMinStartTimeWhenRemoveSubtaskAndMinStartTimeChanged() {
        final long epicId = manager.addEpic(testEpic);
        final Subtask subtaskA = fromEmptySubtask().withId(null).withEpicId(epicId).withStatus(TaskStatus.NEW).build();
        final Subtask subtaskB = fromTestSubtask().withId(null).withEpicId(epicId).build();
        final Subtask subtaskC = fromModifiedSubtask().withId(null).withEpicId(epicId).build();
        final long subtaskAId = manager.addSubtask(subtaskA);
        final long subtaskBId = manager.addSubtask(subtaskB);
        manager.addSubtask(subtaskC);

        manager.removeSubtask(subtaskAId);
        manager.removeSubtask(subtaskBId);
        final LocalDateTime startTime = manager.getEpic(epicId).getStartTime();

        assertEquals(MODIFIED_START_TIME, startTime, "wrong epic start time");
    }

    @Test
    public void shouldRetainEpicStartTimeWhenUpdateEpicAndStartTimeNull() {
        final long epicId = manager.addEpic(testEpic);
        final Subtask subtaskA = fromEmptySubtask().withId(null).withEpicId(epicId).withStatus(TaskStatus.NEW).build();
        final Subtask subtaskB = fromTestSubtask().withId(null).withEpicId(epicId).withDuration(null)
                .withStartTime(null).build();
        final Subtask subtaskC = fromModifiedSubtask().withId(null).withEpicId(epicId).withDuration(null)
                .withStartTime(null).build();
        manager.addSubtask(subtaskA);
        manager.addSubtask(subtaskB);
        manager.addSubtask(subtaskC);
        final Epic update = fromModifiedEpic().withId(epicId).withStartTime(TEST_START_TIME).build();

        manager.updateEpic(update);
        final LocalDateTime startTime = manager.getEpic(epicId).getStartTime();

        assertNull(startTime, "wrong epic start time");
    }

    @Test
    public void shouldRetainEpicStartTimeWhenUpdateEpicAndStartTimeNotNull() {
        final long epicId = manager.addEpic(testEpic);
        final Subtask subtaskA = fromEmptySubtask().withId(null).withEpicId(epicId).withStatus(TaskStatus.NEW).build();
        final Subtask subtaskB = fromTestSubtask().withId(null).withEpicId(epicId).build();
        final Subtask subtaskC = fromModifiedSubtask().withId(null).withEpicId(epicId).build();
        manager.addSubtask(subtaskA);
        manager.addSubtask(subtaskB);
        manager.addSubtask(subtaskC);
        final Epic update = fromModifiedEpic().withId(epicId).build();

        manager.updateEpic(update);
        final LocalDateTime startTime = manager.getEpic(epicId).getStartTime();

        assertEquals(TEST_START_TIME, startTime, "wrong epic start time");
    }

    @Test
    public void shouldSetEpicStartTimeNullWhenRemoveSubtasksAndSubtasksStartTimeNull() {
        final long epicId = manager.addEpic(testEpic);
        final Subtask subtaskA = fromEmptySubtask().withId(null).withEpicId(epicId).withStatus(TaskStatus.NEW).build();
        final Subtask subtaskB = fromTestSubtask().withId(null).withEpicId(epicId).withDuration(null)
                .withStartTime(null).build();
        final Subtask subtaskC = fromModifiedSubtask().withId(null).withEpicId(epicId).withDuration(null)
                .withStartTime(null).build();
        manager.addSubtask(subtaskA);
        manager.addSubtask(subtaskB);
        manager.addSubtask(subtaskC);

        manager.removeSubtasks();
        final LocalDateTime startTime = manager.getEpic(epicId).getStartTime();

        assertNull(startTime, "wrong epic start time");
    }

    @Test
    public void shouldSetEpicStartTimeNullWhenRemoveSubtasksAndSubtasksStartTimeNotNull() {
        final long epicId = manager.addEpic(testEpic);
        final Subtask subtaskA = fromEmptySubtask().withId(null).withEpicId(epicId).withStatus(TaskStatus.NEW).build();
        final Subtask subtaskB = fromTestSubtask().withId(null).withEpicId(epicId).build();
        final Subtask subtaskC = fromModifiedSubtask().withId(null).withEpicId(epicId).build();
        manager.addSubtask(subtaskA);
        manager.addSubtask(subtaskB);
        manager.addSubtask(subtaskC);

        manager.removeSubtasks();
        final LocalDateTime startTime = manager.getEpic(epicId).getStartTime();

        assertNull(startTime, "wrong epic start time");
    }

    @Test
    public void shouldSetEpicEndTimeNullWhenAddEpicAndEndTimeNull() {
        final long epicId = manager.addEpic(testEpic);
        final LocalDateTime endTime = manager.getEpic(epicId).getEndTime();

        assertNull(endTime, "wrong epic end time");
    }

    @Test
    public void shouldSetEpicEndTimeNullWhenAddEpicAndEndTimeNotNull() {
        final Epic epic = fromTestEpic().withId(null).withEndTime(TEST_END_TIME).build();

        final long epicId = manager.addEpic(epic);
        final LocalDateTime endTime = manager.getEpic(epicId).getEndTime();

        assertNull(endTime, "wrong epic end time");
    }

    @Test
    public void shouldSetEpicEndTimeNullWhenAddSubtasksAndSubtasksEndTimeNull() {
        final long epicId = manager.addEpic(testEpic);
        final Subtask subtaskA = fromEmptySubtask().withId(null).withEpicId(epicId).withStatus(TaskStatus.NEW).build();
        final Subtask subtaskB = fromTestSubtask().withId(null).withEpicId(epicId).withDuration(null)
                .withStartTime(null).build();
        final Subtask subtaskC = fromModifiedSubtask().withId(null).withEpicId(epicId).withDuration(null)
                .withStartTime(null).build();

        manager.addSubtask(subtaskA);
        manager.addSubtask(subtaskB);
        manager.addSubtask(subtaskC);
        final LocalDateTime endTime = manager.getEpic(epicId).getEndTime();

        assertNull(endTime, "wrong epic end time");
    }

    @Test
    public void shouldSetEpicMaxEndTimeWhenAddSubtasksAndSubtasksEndTimeNotNull() {
        final long epicId = manager.addEpic(testEpic);
        final Subtask subtaskA = fromEmptySubtask().withId(null).withEpicId(epicId).withStatus(TaskStatus.NEW).build();
        final Subtask subtaskB = fromTestSubtask().withId(null).withEpicId(epicId).build();
        final Subtask subtaskC = fromModifiedSubtask().withId(null).withEpicId(epicId).build();

        manager.addSubtask(subtaskA);
        manager.addSubtask(subtaskB);
        manager.addSubtask(subtaskC);
        final LocalDateTime endTime = manager.getEpic(epicId).getEndTime();

        assertEquals(MODIFIED_END_TIME, endTime, "wrong epic end time");
    }

    @Test
    public void shouldSetEpicMaxEndTimeWhenAddSubtasksAndSubtasksEndTimeNotNullAndInOppositeOrder() {
        final long epicId = manager.addEpic(testEpic);
        final Subtask subtaskA = fromEmptySubtask().withId(null).withEpicId(epicId).withStatus(TaskStatus.NEW).build();
        final Subtask subtaskB = fromTestSubtask().withId(null).withEpicId(epicId).build();
        final Subtask subtaskC = fromModifiedSubtask().withId(null).withEpicId(epicId).build();

        manager.addSubtask(subtaskC);
        manager.addSubtask(subtaskB);
        manager.addSubtask(subtaskA);
        final LocalDateTime endTime = manager.getEpic(epicId).getEndTime();

        assertEquals(MODIFIED_END_TIME, endTime, "wrong epic end time");
    }

    @Test
    public void shouldSetEpicEndTimeNullWhenUpdateSubtasksAndSubtasksEndTimeNull() {
        final long epicId = manager.addEpic(testEpic);
        final Subtask subtaskA = fromEmptySubtask().withId(null).withEpicId(epicId).withStatus(TaskStatus.NEW).build();
        final Subtask subtaskB = fromTestSubtask().withId(null).withEpicId(epicId).build();
        final Subtask subtaskC = fromModifiedSubtask().withId(null).withEpicId(epicId).build();
        manager.addSubtask(subtaskA);
        final long subtaskBId = manager.addSubtask(subtaskB);
        final long subtaskCId = manager.addSubtask(subtaskC);
        final Subtask updateB = fromTestSubtask().withId(subtaskBId).withDuration(null).withStartTime(null).build();
        final Subtask updateC = fromModifiedSubtask().withId(subtaskCId).withDuration(null).withStartTime(null).build();

        manager.updateSubtask(updateB);
        manager.updateSubtask(updateC);
        final LocalDateTime endTime = manager.getEpic(epicId).getEndTime();

        assertNull(endTime, "wrong epic end time");
    }

    @Test
    public void shouldSetEpicMaxEndTimeWhenUpdateSubtasksAndSubtasksEndTimeNotNull() {
        final long epicId = manager.addEpic(testEpic);
        final Subtask subtaskA = fromEmptySubtask().withId(null).withEpicId(epicId).withStatus(TaskStatus.NEW).build();
        final Subtask subtaskB = fromTestSubtask().withId(null).withEpicId(epicId).withDuration(null)
                .withStartTime(null).build();
        final Subtask subtaskC = fromModifiedSubtask().withId(null).withEpicId(epicId).withDuration(null)
                .withStartTime(null).build();
        manager.addSubtask(subtaskA);
        final long subtaskBId = manager.addSubtask(subtaskB);
        final long subtaskCId = manager.addSubtask(subtaskC);
        final Subtask updateB = fromTestSubtask().withId(subtaskBId).build();
        final Subtask updateC = fromModifiedSubtask().withId(subtaskCId).build();

        manager.updateSubtask(updateB);
        manager.updateSubtask(updateC);
        final LocalDateTime endTime = manager.getEpic(epicId).getEndTime();

        assertEquals(MODIFIED_END_TIME, endTime, "wrong epic end time");
    }

    @Test
    public void shouldSetEpicMaxEndTimeWhenUpdateSubtasksAndSubtasksEndTimeNotNullInOppositeOrder() {
        final long epicId = manager.addEpic(testEpic);
        final Subtask subtaskA = fromEmptySubtask().withId(null).withEpicId(epicId).withStatus(TaskStatus.NEW).build();
        final Subtask subtaskB = fromTestSubtask().withId(null).withEpicId(epicId).withDuration(null)
                .withStartTime(null).build();
        final Subtask subtaskC = fromModifiedSubtask().withId(null).withEpicId(epicId).withDuration(null)
                .withStartTime(null).build();
        manager.addSubtask(subtaskA);
        final long subtaskBId = manager.addSubtask(subtaskB);
        final long subtaskCId = manager.addSubtask(subtaskC);
        final Subtask updateB = fromTestSubtask().withId(subtaskBId).build();
        final Subtask updateC = fromModifiedSubtask().withId(subtaskCId).build();

        manager.updateSubtask(updateC);
        manager.updateSubtask(updateB);
        final LocalDateTime endTime = manager.getEpic(epicId).getEndTime();

        assertEquals(MODIFIED_END_TIME, endTime, "wrong epic end time");
    }

    @Test
    public void shouldSetEpicEndTimeNullWhenRemoveSubtaskAndNoSubtasksLeft() {
        final long epicId = manager.addEpic(testEpic);
        final Subtask subtaskA = fromEmptySubtask().withId(null).withEpicId(epicId).withStatus(TaskStatus.NEW).build();
        final Subtask subtaskB = fromTestSubtask().withId(null).withEpicId(epicId).build();
        final Subtask subtaskC = fromModifiedSubtask().withId(null).withEpicId(epicId).build();
        final long subtaskAId = manager.addSubtask(subtaskA);
        final long subtaskBId = manager.addSubtask(subtaskB);
        final long subtaskCId = manager.addSubtask(subtaskC);

        manager.removeSubtask(subtaskAId);
        manager.removeSubtask(subtaskBId);
        manager.removeSubtask(subtaskCId);
        final LocalDateTime endTime = manager.getEpic(epicId).getEndTime();

        assertNull(endTime, "wrong epic end time");
    }

    @Test
    public void shouldSetEpicEndTimeNullWhenRemoveSubtaskAndSubtasksEndTimeNull() {
        final long epicId = manager.addEpic(testEpic);
        final Subtask subtaskA = fromEmptySubtask().withId(null).withEpicId(epicId).withStatus(TaskStatus.NEW).build();
        final Subtask subtaskB = fromTestSubtask().withId(null).withEpicId(epicId).build();
        final Subtask subtaskC = fromModifiedSubtask().withId(null).withEpicId(epicId).build();
        manager.addSubtask(subtaskA);
        final long subtaskBId = manager.addSubtask(subtaskB);
        final long subtaskCId = manager.addSubtask(subtaskC);

        manager.removeSubtask(subtaskBId);
        manager.removeSubtask(subtaskCId);
        final LocalDateTime endTime = manager.getEpic(epicId).getEndTime();

        assertNull(endTime, "wrong epic end time");
    }

    @Test
    public void shouldSetEpicMaxEndTimeWhenRemoveSubtaskAndMaxEndTimeNotChanged() {
        final long epicId = manager.addEpic(testEpic);
        final Subtask subtaskA = fromEmptySubtask().withId(null).withEpicId(epicId).withStatus(TaskStatus.NEW).build();
        final Subtask subtaskB = fromTestSubtask().withId(null).withEpicId(epicId).build();
        final Subtask subtaskC = fromModifiedSubtask().withId(null).withEpicId(epicId).build();
        final long subtaskAId = manager.addSubtask(subtaskA);
        final long subtaskBId = manager.addSubtask(subtaskB);
        manager.addSubtask(subtaskC);

        manager.removeSubtask(subtaskAId);
        manager.removeSubtask(subtaskBId);
        final LocalDateTime endTime = manager.getEpic(epicId).getEndTime();

        assertEquals(MODIFIED_END_TIME, endTime, "wrong epic end time");
    }

    @Test
    public void shouldSetEpicMaxEndTimeWhenRemoveSubtaskAndMaxEndTimeChanged() {
        final long epicId = manager.addEpic(testEpic);
        final Subtask subtaskA = fromEmptySubtask().withId(null).withEpicId(epicId).withStatus(TaskStatus.NEW).build();
        final Subtask subtaskB = fromTestSubtask().withId(null).withEpicId(epicId).build();
        final Subtask subtaskC = fromModifiedSubtask().withId(null).withEpicId(epicId).build();
        final long subtaskAId = manager.addSubtask(subtaskA);
        manager.addSubtask(subtaskB);
        final long subtaskCId = manager.addSubtask(subtaskC);

        manager.removeSubtask(subtaskAId);
        manager.removeSubtask(subtaskCId);
        final LocalDateTime endTime = manager.getEpic(epicId).getEndTime();

        assertEquals(TEST_END_TIME, endTime, "wrong epic end time");
    }

    @Test
    public void shouldRetainEpicEndTimeWhenUpdateEpicAndEndTimeNull() {
        final long epicId = manager.addEpic(testEpic);
        final Subtask subtaskA = fromEmptySubtask().withId(null).withEpicId(epicId).withStatus(TaskStatus.NEW).build();
        final Subtask subtaskB = fromTestSubtask().withId(null).withEpicId(epicId).withDuration(null)
                .withStartTime(null).build();
        final Subtask subtaskC = fromModifiedSubtask().withId(null).withEpicId(epicId).withDuration(null)
                .withStartTime(null).build();
        manager.addSubtask(subtaskA);
        manager.addSubtask(subtaskB);
        manager.addSubtask(subtaskC);
        final Epic update = fromModifiedEpic().withId(epicId).withEndTime(MODIFIED_END_TIME).build();

        manager.updateEpic(update);
        final LocalDateTime endTime = manager.getEpic(epicId).getEndTime();

        assertNull(endTime, "wrong epic end time");
    }

    @Test
    public void shouldRetainEpicEndTimeWhenUpdateEpicAndEndTimeNotNull() {
        final long epicId = manager.addEpic(testEpic);
        final Subtask subtaskA = fromEmptySubtask().withId(null).withEpicId(epicId).withStatus(TaskStatus.NEW).build();
        final Subtask subtaskB = fromTestSubtask().withId(null).withEpicId(epicId).build();
        final Subtask subtaskC = fromModifiedSubtask().withId(null).withEpicId(epicId).build();
        manager.addSubtask(subtaskA);
        manager.addSubtask(subtaskB);
        manager.addSubtask(subtaskC);
        final Epic update = fromModifiedEpic().withId(epicId).build();

        manager.updateEpic(update);
        final LocalDateTime endTime = manager.getEpic(epicId).getEndTime();

        assertEquals(MODIFIED_END_TIME, endTime, "wrong epic end time");
    }

    @Test
    public void shouldSetEpicEndTimeNullWhenRemoveSubtasksAndSubtasksEndTimeNull() {
        final long epicId = manager.addEpic(testEpic);
        final Subtask subtaskA = fromEmptySubtask().withId(null).withEpicId(epicId).withStatus(TaskStatus.NEW).build();
        final Subtask subtaskB = fromTestSubtask().withId(null).withEpicId(epicId).withDuration(null)
                .withStartTime(null).build();
        final Subtask subtaskC = fromModifiedSubtask().withId(null).withEpicId(epicId).withDuration(null)
                .withStartTime(null).build();
        manager.addSubtask(subtaskA);
        manager.addSubtask(subtaskB);
        manager.addSubtask(subtaskC);

        manager.removeSubtasks();
        final LocalDateTime endTime = manager.getEpic(epicId).getEndTime();

        assertNull(endTime, "wrong epic end time");
    }

    @Test
    public void shouldSetEpicEndTimeNullWhenRemoveSubtasksAndSubtasksEndTimeNotNull() {
        final long epicId = manager.addEpic(testEpic);
        final Subtask subtaskA = fromEmptySubtask().withId(null).withEpicId(epicId).withStatus(TaskStatus.NEW).build();
        final Subtask subtaskB = fromTestSubtask().withId(null).withEpicId(epicId).build();
        final Subtask subtaskC = fromModifiedSubtask().withId(null).withEpicId(epicId).build();
        manager.addSubtask(subtaskA);
        manager.addSubtask(subtaskB);
        manager.addSubtask(subtaskC);

        manager.removeSubtasks();
        final LocalDateTime endTime = manager.getEpic(epicId).getEndTime();

        assertNull(endTime, "wrong epic end time");
    }

    @ParameterizedTest
    @NullSource
    @EnumSource(TaskStatus.class)
    public void shouldSetEpicStatusNewWhenAddEpic(TaskStatus status) {
        final Epic epic = fromTestEpic().withId(null).withStatus(status).build();

        final long epicId = manager.addEpic(epic);
        final TaskStatus actualStatus = manager.getEpic(epicId).getStatus();

        assertEquals(TaskStatus.NEW, actualStatus, "wrong epic status");
    }

    @ParameterizedTest
    @CsvSource({"NEW,NEW", "NEW,IN_PROGRESS", "NEW,DONE", "IN_PROGRESS,NEW", "IN_PROGRESS,IN_PROGRESS",
            "IN_PROGRESS,DONE", "DONE,NEW", "DONE,IN_PROGRESS", "DONE,DONE"})
    public void shouldSetEpicStatusWhenAddSubtask(TaskStatus statusA, TaskStatus statusB) {
        final long epicId = manager.addEpic(testEpic);
        final Subtask subtaskA = fromTestSubtask().withId(null).withEpicId(epicId).withStatus(statusA).build();
        final Subtask subtaskB = fromModifiedSubtask().withId(null).withEpicId(epicId).withStatus(statusB).build();
        final TaskStatus expectedStatus = statusA == statusB ? statusA : TaskStatus.IN_PROGRESS;

        manager.addSubtask(subtaskA);
        manager.addSubtask(subtaskB);
        final TaskStatus actualStatus = manager.getEpic(epicId).getStatus();

        assertEquals(expectedStatus, actualStatus, "wrong epic status");
    }

    @Test
    public void shouldSetEpicStatusInProgressWhenAddSubtaskAndAllStatuses() {
        final long epicId = manager.addEpic(testEpic);
        final Subtask subtaskA = fromEmptySubtask().withEpicId(epicId).withStatus(TaskStatus.NEW).build();
        final Subtask subtaskB = fromTestSubtask().withId(null).withEpicId(epicId).build();
        final Subtask subtaskC = fromModifiedSubtask().withId(null).withEpicId(epicId).build();

        manager.addSubtask(subtaskA);
        manager.addSubtask(subtaskB);
        manager.addSubtask(subtaskC);
        final TaskStatus actualStatus = manager.getEpic(epicId).getStatus();

        assertEquals(TaskStatus.IN_PROGRESS, actualStatus, "wrong epic status");
    }

    @ParameterizedTest
    @CsvSource({"NEW,NEW", "NEW,IN_PROGRESS", "NEW,DONE", "IN_PROGRESS,NEW", "IN_PROGRESS,IN_PROGRESS",
            "IN_PROGRESS,DONE", "DONE,NEW", "DONE,IN_PROGRESS", "DONE,DONE"})
    public void shouldSetEpicStatusWhenUpdateSubtask(TaskStatus statusA, TaskStatus statusB) {
        final long epicId = manager.addEpic(testEpic);
        final Subtask subtaskA = fromTestSubtask().withId(null).withEpicId(epicId).withStatus(statusA).build();
        final Subtask subtaskB = fromModifiedSubtask().withId(null).withEpicId(epicId).withStatus(statusA).build();
        manager.addSubtask(subtaskA);
        final long subtaskBId = manager.addSubtask(subtaskB);
        final Subtask update = fromModifiedSubtask().withId(subtaskBId).withEpicId(epicId).withStatus(statusB).build();
        final TaskStatus expectedStatus = statusA == statusB ? statusA : TaskStatus.IN_PROGRESS;

        manager.updateSubtask(update);
        final TaskStatus actualStatus = manager.getEpic(epicId).getStatus();

        assertEquals(expectedStatus, actualStatus, "wrong epic status");
    }

    @Test
    public void shouldSetEpicStatusInProgressWhenUpdateSubtaskAndAllStatuses() {
        final long epicId = manager.addEpic(testEpic);
        final Subtask subtaskA = fromEmptySubtask().withEpicId(epicId).withStatus(TaskStatus.NEW).build();
        final Subtask subtaskB = fromTestSubtask().withId(null).withEpicId(epicId).withStatus(TaskStatus.NEW).build();
        final Subtask subtaskC = fromModifiedSubtask().withId(null).withEpicId(epicId).withStatus(TaskStatus.NEW)
                .build();
        manager.addSubtask(subtaskA);
        final long subtaskBId = manager.addSubtask(subtaskB);
        final long subtaskCId = manager.addSubtask(subtaskC);
        final Subtask updateB = fromTestSubtask().withId(subtaskBId).build();
        final Subtask updateC = fromModifiedSubtask().withId(subtaskCId).build();

        manager.updateSubtask(updateB);
        manager.updateSubtask(updateC);
        final TaskStatus actualStatus = manager.getEpic(epicId).getStatus();

        assertEquals(TaskStatus.IN_PROGRESS, actualStatus, "wrong epic status");
    }

    @ParameterizedTest
    @CsvSource({"NEW,NEW", "NEW,IN_PROGRESS", "NEW,DONE", "IN_PROGRESS,NEW", "IN_PROGRESS,IN_PROGRESS",
            "IN_PROGRESS,DONE", "DONE,NEW", "DONE,IN_PROGRESS", "DONE,DONE"})
    public void shouldSetEpicStatusWhenRemoveSubtask(TaskStatus statusA, TaskStatus statusB) {
        final long epicId = manager.addEpic(testEpic);
        final Subtask subtaskA = fromEmptySubtask().withEpicId(epicId).withStatus(TaskStatus.NEW).build();
        final Subtask subtaskB = fromTestSubtask().withId(null).withEpicId(epicId).withStatus(statusA).build();
        final Subtask subtaskC = fromModifiedSubtask().withId(null).withEpicId(epicId).withStatus(statusB).build();
        final long subtaskAId = manager.addSubtask(subtaskA);
        manager.addSubtask(subtaskB);
        manager.addSubtask(subtaskC);
        final TaskStatus expectedStatus = statusA == statusB ? statusA : TaskStatus.IN_PROGRESS;

        manager.removeSubtask(subtaskAId);
        final TaskStatus actualStatus = manager.getEpic(epicId).getStatus();

        assertEquals(expectedStatus, actualStatus, "wrong epic status");
    }

    @Test
    public void shouldSetEpicStatusInProgressWhenRemoveSubtaskAndAllStatuses() {
        final long epicId = manager.addEpic(testEpic);
        final Subtask subtaskA = fromEmptySubtask().withEpicId(epicId).withStatus(TaskStatus.NEW).build();
        final Subtask subtaskB = fromTestSubtask().withId(null).withEpicId(epicId).build();
        final Subtask subtaskC = fromModifiedSubtask().withId(null).withEpicId(epicId).build();
        final Subtask subtaskD = fromEmptySubtask().withEpicId(epicId).withStatus(TaskStatus.NEW).build();
        manager.addSubtask(subtaskA);
        manager.addSubtask(subtaskB);
        manager.addSubtask(subtaskC);
        final long subtaskDId = manager.addSubtask(subtaskD);

        manager.removeSubtask(subtaskDId);
        final TaskStatus actualStatus = manager.getEpic(epicId).getStatus();

        assertEquals(TaskStatus.IN_PROGRESS, actualStatus, "wrong epic status");
    }

    @ParameterizedTest
    @EnumSource(TaskStatus.class)
    public void shouldRetainEpicStatusWhenUpdateEpicAndStatusNull(TaskStatus status) {
        final long epicId = manager.addEpic(testEpic);
        final Subtask subtask = fromTestSubtask().withId(null).withEpicId(epicId).withStatus(status).build();
        manager.addSubtask(subtask);
        final Epic update = fromModifiedEpic().withId(epicId).build();

        manager.updateEpic(update);
        final TaskStatus actualStatus = manager.getEpic(epicId).getStatus();

        assertEquals(status, actualStatus, "wrong epic status");
    }

    @ParameterizedTest
    @CsvSource({"NEW,NEW", "NEW,IN_PROGRESS", "NEW,DONE", "IN_PROGRESS,NEW", "IN_PROGRESS,IN_PROGRESS",
            "IN_PROGRESS,DONE", "DONE,NEW", "DONE,IN_PROGRESS", "DONE,DONE"})
    public void shouldRetainEpicStatusWhenUpdateEpicAndStatusNotNull(TaskStatus statusA, TaskStatus statusB) {
        final long epicId = manager.addEpic(testEpic);
        final Subtask subtask = fromTestSubtask().withId(null).withEpicId(epicId).withStatus(statusA).build();
        manager.addSubtask(subtask);
        final Epic update = fromModifiedEpic().withId(epicId).withStatus(statusB).build();

        manager.updateEpic(update);
        final TaskStatus actualStatus = manager.getEpic(epicId).getStatus();

        assertEquals(statusA, actualStatus, "wrong epic status");
    }

    @ParameterizedTest
    @EnumSource(TaskStatus.class)
    public void shouldSetEpicStatusNewWhenRemoveSubtasks(TaskStatus status) {
        final long epicId = manager.addEpic(testEpic);
        final Subtask subtask = fromTestSubtask().withId(null).withEpicId(epicId).withStatus(status).build();
        manager.addSubtask(subtask);

        manager.removeSubtasks();
        final TaskStatus actualStatus = manager.getEpic(epicId).getStatus();

        assertEquals(TaskStatus.NEW, actualStatus, "wrong epic status");
    }

    @Test
    public void shouldNotGetEpicSubtasksWhenEpicNotExist() {
        final long epicId = -1L;
        final String expectedMessage = "no epic with id=" + epicId;

        final Exception exception = assertThrows(TaskNotFoundException.class, () -> manager.getEpicSubtasks(epicId));
        assertEquals(expectedMessage, exception.getMessage(), WRONG_EXCEPTION_MESSAGE);
    }

    @Test
    public void shouldGetEpicSubtasksWhenSeveralSubtasks() {
        final long epicId = manager.addEpic(testEpic);
        final long anotherEpicId = manager.addEpic(modifiedEpic);
        final Subtask subtaskA = fromEmptySubtask().withEpicId(epicId).withStatus(TaskStatus.NEW).build();
        final Subtask subtaskB = fromTestSubtask().withId(null).withEpicId(epicId).build();
        final Subtask subtaskC = fromModifiedSubtask().withId(null).withEpicId(epicId).build();
        final long subtaskAId = manager.addSubtask(subtaskA);
        final long subtaskBId = manager.addSubtask(subtaskB);
        final long subtaskCId = manager.addSubtask(subtaskC);
        final Subtask subtaskD = fromEmptySubtask().withEpicId(anotherEpicId).withStatus(TaskStatus.NEW).build();
        manager.addSubtask(subtaskD);
        manager.removeSubtask(subtaskBId);
        final Subtask expectedSubtaskA = fromEmptySubtask().withId(subtaskAId).withEpicId(epicId)
                .withStatus(TaskStatus.NEW).build();
        final Subtask expectedSubtaskB = fromModifiedSubtask().withId(subtaskCId).withEpicId(epicId).build();
        final List<Subtask> expectedSubtasks = List.of(expectedSubtaskA, expectedSubtaskB);

        final List<Subtask> actualSubtasks = manager.getEpicSubtasks(epicId);

        assertListEquals(expectedSubtasks, actualSubtasks, "incorrect list of subtasks returned");
    }

    @Test
    public void shouldGetTasksWhenSeveralTasks() {
        final long taskAId = manager.addTask(emptyTask);
        final long taskBId = manager.addTask(testTask);
        final long taskCId = manager.addTask(modifiedTask);
        final Task taskA = fromEmptyTask().withId(taskAId).withStatus(TaskStatus.NEW).build();
        final Task taskC = fromModifiedTask().withId(taskCId).build();
        final List<Task> expectedTasks = List.of(taskA, taskC);
        manager.removeTask(taskBId);

        final List<Task> actualTasks = manager.getTasks();

        assertListEquals(expectedTasks, actualTasks, "incorrect list of tasks returned");
    }

    @Test
    public void shouldRemoveTasksFromGetAndTasksAndPrioritized() {
        final long taskAId = manager.addTask(testTask);
        final long taskBId = manager.addTask(modifiedTask);

        manager.removeTasks();
        final List<Task> tasks = manager.getTasks();
        final List<Task> prioritized = manager.getPrioritizedTasks();

        assertAll("tasks removed with errors",
                () -> assertThrows(TaskNotFoundException.class, () -> manager.getTask(taskAId),
                        "tasks removed with errors"),
                () -> assertThrows(TaskNotFoundException.class, () -> manager.getTask(taskBId),
                        "tasks removed with errors"),
                () -> assertTrue(tasks.isEmpty(), "tasks removed with errors"),
                () -> assertTrue(prioritized.isEmpty(), "tasks removed with errors")
        );
    }

    @Test
    public void shouldGetEpicsWhenSeveralEpics() {
        final long epicAId = manager.addEpic(emptyEpic);
        final long epicBId = manager.addEpic(testEpic);
        final long epicCId = manager.addEpic(modifiedEpic);
        final Epic epicA = fromEmptyEpic().withId(epicAId).withStatus(TaskStatus.NEW).build();
        final Epic epicC = fromModifiedEpic().withId(epicCId).withStatus(TaskStatus.NEW).build();
        final List<Epic> expectedEpics = List.of(epicA, epicC);
        manager.removeEpic(epicBId);

        final List<Epic> actualEpics = manager.getEpics();

        assertListEquals(expectedEpics, actualEpics, "incorrect list of epics returned");
    }

    @Test
    public void shouldRemoveEpicsFromGetAndEpics() {
        final long epicAId = manager.addEpic(testEpic);
        final long epicBId = manager.addEpic(modifiedEpic);

        manager.removeEpics();
        final List<Epic> epics = manager.getEpics();

        assertAll("epics removed with errors",
                () -> assertThrows(TaskNotFoundException.class, () -> manager.getEpic(epicAId),
                        "epics removed with errors"),
                () -> assertThrows(TaskNotFoundException.class, () -> manager.getEpic(epicBId),
                        "epics removed with errors"),
                () -> assertTrue(epics.isEmpty(), "epics removed with errors")
        );
    }

    @Test
    public void shouldGetSubtasksWhenSeveralSubtasks() {
        final long epicId = manager.addEpic(testEpic);
        final long anotherEpicId = manager.addEpic(modifiedEpic);
        final Subtask subtaskA = fromEmptySubtask().withEpicId(epicId).withStatus(TaskStatus.NEW).build();
        final Subtask subtaskB = fromTestSubtask().withId(null).withEpicId(epicId).build();
        final Subtask subtaskC = fromModifiedSubtask().withId(null).withEpicId(epicId).build();
        final Subtask subtaskD = fromEmptySubtask().withEpicId(anotherEpicId).withStatus(TaskStatus.NEW).build();
        final long subtaskAId = manager.addSubtask(subtaskA);
        final long subtaskBId = manager.addSubtask(subtaskB);
        final long subtaskCId = manager.addSubtask(subtaskC);
        final long subtaskDId = manager.addSubtask(subtaskD);
        manager.removeSubtask(subtaskBId);
        final Subtask expectedSubtaskA = fromEmptySubtask().withId(subtaskAId).withEpicId(epicId)
                .withStatus(TaskStatus.NEW).build();
        final Subtask expectedSubtaskC = fromModifiedSubtask().withId(subtaskCId).withEpicId(epicId).build();
        final Subtask expectedSubtaskD = fromEmptySubtask().withId(subtaskDId).withEpicId(anotherEpicId)
                .withStatus(TaskStatus.NEW).build();
        final List<Subtask> expectedSubtasks = List.of(expectedSubtaskA, expectedSubtaskC, expectedSubtaskD);

        final List<Subtask> actualSubtasks = manager.getSubtasks();

        assertListEquals(expectedSubtasks, actualSubtasks, "incorrect list of subtasks returned");
    }

    @Test
    public void shouldRemoveSubtasksFromGetAndEpicAndSubtasksAndPrioritized() {
        final long epicId = manager.addEpic(testEpic);
        final long anotherEpicId = manager.addEpic(modifiedEpic);
        final Subtask subtaskA = fromTestSubtask().withId(null).withEpicId(epicId).build();
        final Subtask subtaskB = fromModifiedSubtask().withId(null).withEpicId(anotherEpicId).build();
        final long subtaskAId = manager.addSubtask(subtaskA);
        final long subtaskBId = manager.addSubtask(subtaskB);

        manager.removeSubtasks();
        final List<Subtask> epicSubtasks = manager.getEpicSubtasks(epicId);
        final List<Subtask> anotherEpicSubtasks = manager.getEpicSubtasks(anotherEpicId);
        final List<Subtask> subtasks = manager.getSubtasks();
        final List<Task> prioritized = manager.getPrioritizedTasks();

        assertAll("subtasks removed with errors",
                () -> assertThrows(TaskNotFoundException.class, () -> manager.getSubtask(subtaskAId),
                        "subtasks removed with errors"),
                () -> assertThrows(TaskNotFoundException.class, () -> manager.getSubtask(subtaskBId),
                        "subtasks removed with errors"),
                () -> assertTrue(epicSubtasks.isEmpty(), "subtasks removed with errors"),
                () -> assertTrue(anotherEpicSubtasks.isEmpty(), "subtasks removed with errors"),
                () -> assertTrue(subtasks.isEmpty(), "subtasks removed with errors"),
                () -> assertTrue(prioritized.isEmpty(), "subtasks removed with errors")
        );
    }

    @Test
    public void shouldRemoveSubtasksFromGetAndSubtasksAndPrioritizeWhenRemoveEpics() {
        final long epicId = manager.addEpic(testEpic);
        final long anotherEpicId = manager.addEpic(modifiedEpic);
        final Subtask subtaskA = fromTestSubtask().withId(null).withEpicId(epicId).build();
        final Subtask subtaskB = fromModifiedSubtask().withId(null).withEpicId(anotherEpicId).build();
        final long subtaskAId = manager.addSubtask(subtaskA);
        final long subtaskBId = manager.addSubtask(subtaskB);

        manager.removeEpics();
        final List<Subtask> subtasks = manager.getSubtasks();
        final List<Task> prioritized = manager.getPrioritizedTasks();

        assertAll("subtasks removed with errors",
                () -> assertThrows(TaskNotFoundException.class, () -> manager.getSubtask(subtaskAId),
                        "subtasks removed with errors"),
                () -> assertThrows(TaskNotFoundException.class, () -> manager.getSubtask(subtaskBId),
                        "subtasks removed with errors"),
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
        final Epic expectedEpic = fromTestEpic().withId(epicId).withStatus(TaskStatus.NEW).build();
        final List<Task> expectedTasks = List.of(expectedEpic);

        manager.getEpic(epicId);
        final List<Task> tasks = historyManager.getHistory();

        assertListEquals(expectedTasks, tasks, "history saved with errors");
    }

    @Test
    public void shouldPassSubtaskToHistoryManagerWhenGetSubtask() {
        final long epicId = manager.addEpic(testEpic);
        final Subtask subtask = fromTestSubtask().withId(null).withEpicId(epicId).build();
        final long subtaskId = manager.addSubtask(subtask);
        final Subtask expectedSubtask = fromTestSubtask().withId(subtaskId).withEpicId(epicId).build();
        final List<Task> expectedTasks = List.of(expectedSubtask);

        manager.getSubtask(subtaskId);
        final List<Task> tasks = historyManager.getHistory();

        assertListEquals(expectedTasks, tasks, "history saved with errors");
    }

    @Test
    public void shouldPassTasksEpicsSubtasksFromHistoryManagerWhenGetHistory() {
        final Task task = fromTestTask().build();
        final Epic epic = fromTestEpic().build();
        final Subtask subtask = fromTestSubtask().build();
        historyManager.add(task);
        historyManager.add(epic);
        historyManager.add(subtask);
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
        final Subtask subtask = fromTestSubtask().withId(null).withEpicId(epicId).build();
        final long subtaskId = manager.addSubtask(subtask);
        manager.getSubtask(subtaskId);

        manager.removeSubtask(subtaskId);
        final List<Task> tasks = historyManager.getHistory();

        assertTrue(tasks.isEmpty(), "subtask should be removed from history");
    }

    @Test
    public void shouldRemoveSubtaskFromHistoryManagerWhenRemoveEpic() {
        final long epicId = manager.addEpic(testEpic);
        final long anotherEpicId = manager.addEpic(modifiedEpic);
        final Subtask subtaskA = fromTestSubtask().withId(null).withEpicId(epicId).build();
        final Subtask subtaskB = fromModifiedSubtask().withId(null).withEpicId(anotherEpicId).build();
        final long subtaskAId = manager.addSubtask(subtaskA);
        final long subtaskBId = manager.addSubtask(subtaskB);
        manager.getSubtask(subtaskAId);
        manager.getSubtask(subtaskBId);
        final Subtask expectedSubtask = fromModifiedSubtask().withId(subtaskBId).withEpicId(anotherEpicId)
                .build();
        final List<Task> expectedTasks = List.of(expectedSubtask);

        manager.removeEpic(epicId);
        final List<Task> tasks = historyManager.getHistory();

        assertListEquals(expectedTasks, tasks, "incorrect history returned");
    }

    @Test
    public void shouldRemoveTasksFromHistoryManagerWhenRemoveTasks() {
        final long taskAId = manager.addTask(testTask);
        final long taskBId = manager.addTask(modifiedTask);
        manager.getTask(taskAId);
        manager.getTask(taskBId);

        manager.removeTasks();
        final List<Task> tasks = historyManager.getHistory();

        assertTrue(tasks.isEmpty(), "tasks should be removed from history");
    }

    @Test
    public void shouldRemoveEpicsFromHistoryManagerWhenRemoveEpics() {
        final long epicAId = manager.addEpic(testEpic);
        final long epicBId = manager.addEpic(modifiedEpic);
        manager.getEpic(epicAId);
        manager.getEpic(epicBId);

        manager.removeEpics();
        final List<Task> tasks = historyManager.getHistory();

        assertTrue(tasks.isEmpty(), "epics should be removed from history");
    }

    @Test
    public void shouldRemoveSubtasksFromHistoryManagerWhenRemoveSubtasks() {
        final long epicId = manager.addEpic(testEpic);
        final long anotherEpicId = manager.addEpic(modifiedEpic);
        final Subtask subtaskA = fromTestSubtask().withId(null).withEpicId(epicId).build();
        final Subtask subtaskB = fromModifiedSubtask().withId(null).withEpicId(anotherEpicId).build();
        final long subtaskAId = manager.addSubtask(subtaskA);
        final long subtaskBId = manager.addSubtask(subtaskB);
        manager.getSubtask(subtaskAId);
        manager.getSubtask(subtaskBId);

        manager.removeSubtasks();
        final List<Task> tasks = historyManager.getHistory();

        assertTrue(tasks.isEmpty(), "subtasks should be removed from history");
    }

    @Test
    public void shouldRemoveSubtasksFromHistoryManagerWhenRemovedEpics() {
        final long epicId = manager.addEpic(testEpic);
        final long anotherEpicId = manager.addEpic(modifiedEpic);
        final Subtask subtaskA = fromTestSubtask().withId(null).withEpicId(epicId).build();
        final Subtask subtaskB = fromModifiedSubtask().withId(null).withEpicId(anotherEpicId).build();
        final long subtaskAId = manager.addSubtask(subtaskA);
        final long subtaskBId = manager.addSubtask(subtaskB);
        manager.getSubtask(subtaskAId);
        manager.getSubtask(subtaskBId);

        manager.removeEpics();
        final List<Task> tasks = historyManager.getHistory();

        assertTrue(tasks.isEmpty(), "subtasks should be removed from history");
    }
}
