package io.github.akuniutka.kanban.service;

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
    public void shouldNotGetTaskByIdWhenNotExist() {
        final long taskId = -1L;
        final String expectedMessage = "no task with id=" + taskId;

        final Exception exception = assertThrows(TaskNotFoundException.class, () -> manager.getTaskById(taskId));
        assertEquals(expectedMessage, exception.getMessage(), WRONG_EXCEPTION_MESSAGE);
    }

    @Test
    public void shouldGetTaskById() {
        final long taskId = manager.createTask(testTask);
        final Task expectedTask = fromTestTask().withId(taskId).build();

        final Task savedTask = manager.getTaskById(taskId);

        assertTaskEquals(expectedTask, savedTask, "task saved with errors");
    }

    @Test
    public void shouldNotCreateTaskWhenNull() {
        final String expectedMessage = "cannot create null task";

        final Exception exception = assertThrows(NullPointerException.class, () -> manager.createTask(null));
        assertEquals(expectedMessage, exception.getMessage(), WRONG_EXCEPTION_MESSAGE);
    }

    @Test
    public void shouldNotCreateTaskWhenDurationNullAndStartTimeNotNull() {
        final Task task = fromTestTask().withId(null).withDuration(null).build();

        final Exception exception = assertThrows(ManagerValidationException.class, () -> manager.createTask(task));
        assertEquals("duration and start time must be either both set or both null", exception.getMessage(),
                WRONG_EXCEPTION_MESSAGE);
    }

    @Test
    public void shouldNotCreateTaskWhenDurationNotNullAndStartTimeNull() {
        final Task task = fromTestTask().withId(null).withStartTime(null).build();

        final Exception exception = assertThrows(ManagerValidationException.class, () -> manager.createTask(task));
        assertEquals("duration and start time must be either both set or both null", exception.getMessage(),
                WRONG_EXCEPTION_MESSAGE);
    }

    @ParameterizedTest
    @MethodSource("io.github.akuniutka.kanban.TestModels#getOverlappingTimeSlots")
    public void shouldNotCreateTaskWhenOverlapAnotherPrioritizedTask(Duration duration, LocalDateTime startTime) {
        final long epicId = manager.createEpic(testEpic);
        final Subtask overlappingSubtask = fromTestSubtask().withId(null).withEpicId(epicId).withDuration(duration)
                .withStartTime(startTime).build();
        manager.createSubtask(overlappingSubtask);

        final Exception exception = assertThrows(ManagerValidationException.class, () -> manager.createTask(testTask));
        assertEquals("conflict with another task for time slot", exception.getMessage(), WRONG_EXCEPTION_MESSAGE);
    }

    @Test
    public void shouldNotCreateTaskWhenStatusNull() {
        final Task task = fromTestTask().withId(null).withStatus(null).build();

        final Exception exception = assertThrows(ManagerValidationException.class, () -> manager.createTask(task));
        assertEquals("status cannot be null", exception.getMessage(), WRONG_EXCEPTION_MESSAGE);
    }

    @Test
    public void shouldCreateTaskWhenDurationNotNullAndStartTimeNotNull() {
        final long taskId = manager.createTask(testTask);
        final Task savedTask = manager.getTaskById(taskId);
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
    public void shouldCreateTaskWithDurationTruncatedToMinutes() {
        final Task task = fromTestTask().withId(null).withDuration(TEST_DURATION.plusSeconds(25L)).build();
        final long taskId = manager.createTask(task);
        final Task savedTask = manager.getTaskById(taskId);
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
    public void shouldCreateTaskWithStartTimeTruncatedToMinutes() {
        final Task task = fromTestTask().withId(null).withStartTime(TEST_START_TIME.plusSeconds(25L)).build();
        final long taskId = manager.createTask(task);
        final Task savedTask = manager.getTaskById(taskId);
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
    public void shouldCreateTaskWhenDurationNullAndStartTimeNull() {
        final Task task = fromTestTask().withId(null).withDuration(null).withStartTime(null).build();

        final long taskId = manager.createTask(task);
        final Task savedTask = manager.getTaskById(taskId);
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
    public void shouldCreateTaskWhenFieldsNull() {
        final long taskId = manager.createTask(emptyTask);
        final Task savedTask = manager.getTaskById(taskId);
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
    public void shouldCreateTaskWhenExactlyBeforeAnotherPrioritizedTask() {
        final long epicId = manager.createEpic(testEpic);
        final Subtask subtask = fromTestSubtask().withId(null).withEpicId(epicId)
                .withStartTime(TEST_START_TIME.plus(TEST_DURATION)).build();
        final long subtaskId = manager.createSubtask(subtask);

        final long taskId = manager.createTask(testTask);
        final Task savedTask = manager.getTaskById(taskId);
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
    public void shouldCreateTaskWhenWithDurationTruncatedToMinutesExactlyBeforeAnotherTask() {
        final long epicId = manager.createEpic(testEpic);
        final Subtask subtask = fromTestSubtask().withId(null).withEpicId(epicId)
                .withStartTime(TEST_START_TIME.plus(TEST_DURATION)).build();
        final long subtaskId = manager.createSubtask(subtask);
        final Task task = fromTestTask().withId(null).withDuration(TEST_DURATION.plusSeconds(25L)).build();

        final long taskId = manager.createTask(task);
        final Task savedTask = manager.getTaskById(taskId);
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
    public void shouldCreateTaskWhenWithStartTimeTruncatedToMinutesExactlyBeforeAnotherTask() {
        final long epicId = manager.createEpic(testEpic);
        final Subtask subtask = fromTestSubtask().withId(null).withEpicId(epicId)
                .withStartTime(TEST_START_TIME.plus(TEST_DURATION)).build();
        final long subtaskId = manager.createSubtask(subtask);
        final Task task = fromTestTask().withId(null).withStartTime(TEST_START_TIME.plusSeconds(25L)).build();

        final long taskId = manager.createTask(task);
        final Task savedTask = manager.getTaskById(taskId);
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
    public void shouldCreateTaskWhenExactlyAfterAnotherPrioritizedTask() {
        final long epicId = manager.createEpic(testEpic);
        final Subtask subtask = fromTestSubtask().withId(null).withEpicId(epicId)
                .withStartTime(TEST_START_TIME.minus(TEST_DURATION)).build();
        final long subtaskId = manager.createSubtask(subtask);

        final long taskId = manager.createTask(testTask);
        final Task savedTask = manager.getTaskById(taskId);
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
        final String expectedMessage = "id cannot be null";

        final Exception exception = assertThrows(ManagerValidationException.class, () -> manager.updateTask(testTask));
        assertEquals(expectedMessage, exception.getMessage(), WRONG_EXCEPTION_MESSAGE);
    }

    @Test
    public void shouldNotUpdateTaskWhenEpicIdAsId() {
        final long epicId = manager.createEpic(testEpic);
        final String expectedMessage = "wrong task type";
        final Task update = fromTestTask().withId(epicId).build();

        final Exception exception = assertThrows(ManagerValidationException.class, () -> manager.updateTask(update));
        assertEquals(expectedMessage, exception.getMessage(), WRONG_EXCEPTION_MESSAGE);
    }

    @Test
    public void shouldNotUpdateTaskWhenSubtaskIdAsId() {
        final long epicId = manager.createEpic(testEpic);
        final Subtask subtask = fromModifiedSubtask().withId(null).withEpicId(epicId).build();
        final long subtaskId = manager.createSubtask(subtask);
        final String expectedMessage = "wrong task type";
        final Task update = fromTestTask().withId(subtaskId).build();

        final Exception exception = assertThrows(ManagerValidationException.class, () -> manager.updateTask(update));
        assertEquals(expectedMessage, exception.getMessage(), WRONG_EXCEPTION_MESSAGE);
    }

    @Test
    public void shouldNotUpdateTaskWhenDurationNullAndStartTimeNotNull() {
        final long taskId = manager.createTask(testTask);
        final Task update = fromModifiedTask().withId(taskId).withDuration(null).build();

        final Exception exception = assertThrows(ManagerValidationException.class, () -> manager.updateTask(update));
        assertEquals("duration and start time must be either both set or both null", exception.getMessage(),
                WRONG_EXCEPTION_MESSAGE);
    }

    @Test
    public void shouldNotUpdateTaskWhenDurationNotNullAndStartTimeNull() {
        final long taskId = manager.createTask(testTask);
        final Task update = fromTestTask().withId(taskId).withStartTime(null).build();

        final Exception exception = assertThrows(ManagerValidationException.class, () -> manager.updateTask(update));
        assertEquals("duration and start time must be either both set or both null", exception.getMessage(),
                WRONG_EXCEPTION_MESSAGE);
    }

    @ParameterizedTest
    @MethodSource("io.github.akuniutka.kanban.TestModels#getOverlappingTimeSlots")
    public void shouldNotUpdateTaskWhenOverlapAnotherPrioritizedTask(Duration duration, LocalDateTime startTime) {
        final long taskId = manager.createTask(modifiedTask);
        final long epicId = manager.createEpic(testEpic);
        final Subtask overlappingSubtask = fromTestSubtask().withId(null).withEpicId(epicId).withDuration(duration)
                .withStartTime(startTime).build();
        manager.createSubtask(overlappingSubtask);
        final Task update = fromTestTask().withId(taskId).build();

        final Exception exception = assertThrows(ManagerValidationException.class, () -> manager.updateTask(update));
        assertEquals("conflict with another task for time slot", exception.getMessage(), WRONG_EXCEPTION_MESSAGE);
    }

    @Test
    public void shouldNotUpdateTaskWhenStatusNull() {
        final long taskId = manager.createTask(modifiedTask);
        final Task update = fromTestTask().withId(taskId).withStatus(null).build();

        final Exception exception = assertThrows(ManagerValidationException.class, () -> manager.updateTask(update));
        assertEquals("status cannot be null", exception.getMessage(), WRONG_EXCEPTION_MESSAGE);
    }

    @Test
    public void shouldUpdateTaskWhenDurationNotNullAndStartTimeNotNull() {
        final long taskId = manager.createTask(testTask);
        final Task update = fromModifiedTask().withId(taskId).build();
        final Task expectedTask = fromModifiedTask().withId(taskId).build();
        final List<Task> expectedTasks = List.of(expectedTask);

        manager.updateTask(update);
        final Task savedTask = manager.getTaskById(taskId);
        final List<Task> tasks = manager.getTasks();
        final List<Task> prioritized = manager.getPrioritizedTasks();

        assertAll("task saved with errors",
                () -> assertTaskEquals(expectedTask, savedTask, "task saved with errors"),
                () -> assertListEquals(expectedTasks, tasks, "task saved with errors"),
                () -> assertListEquals(expectedTasks, prioritized, "task saved with errors")
        );
    }

    @Test
    public void shouldUpdateTaskWhenIdNotExist() {
        final Task update = fromTestTask().withId(ANOTHER_TEST_ID).build();
        final Task expectedTask = fromTestTask().withId(ANOTHER_TEST_ID).build();
        final Task expectedNextTask = fromModifiedTask().withId(ANOTHER_TEST_ID + 1).build();
        final List<Task> expectedTasks = List.of(expectedTask, expectedNextTask);

        manager.updateTask(update);
        final long nextId = manager.createTask(modifiedTask);
        final Task savedTask = manager.getTaskById(ANOTHER_TEST_ID);
        final Task nextTask = manager.getTaskById(nextId);
        final List<Task> tasks = manager.getTasks();
        final List<Task> prioritized = manager.getPrioritizedTasks();

        assertAll("task saved with errors",
                () -> assertEquals(ANOTHER_TEST_ID + 1, nextId, "task saved with errors"),
                () -> assertTaskEquals(expectedTask, savedTask, "task saved with errors"),
                () -> assertTaskEquals(expectedNextTask, nextTask, "task saved with errors"),
                () -> assertListEquals(expectedTasks, tasks, "task saved with errors"),
                () -> assertListEquals(expectedTasks, prioritized, "task saved with errors")
        );
    }

    @Test
    public void shouldUpdateTaskWhenDurationTruncatedToMinutes() {
        final long taskId = manager.createTask(testTask);
        final Task update = fromModifiedTask().withId(taskId).withDuration(MODIFIED_DURATION.plusSeconds(25L))
                .build();
        final Task expectedTask = fromModifiedTask().withId(taskId).build();
        final List<Task> expectedTasks = List.of(expectedTask);

        manager.updateTask(update);
        final Task savedTask = manager.getTaskById(taskId);
        final List<Task> tasks = manager.getTasks();
        final List<Task> prioritized = manager.getPrioritizedTasks();

        assertAll("task saved with errors",
                () -> assertTaskEquals(expectedTask, savedTask, "task saved with errors"),
                () -> assertListEquals(expectedTasks, tasks, "task saved with errors"),
                () -> assertListEquals(expectedTasks, prioritized, "task saved with errors")
        );
    }

    @Test
    public void shouldUpdateTaskWhenStartTimeTruncatedToMinutes() {
        final long taskId = manager.createTask(testTask);
        final Task update = fromModifiedTask().withId(taskId).withStartTime(MODIFIED_START_TIME.plusSeconds(25L))
                .build();
        final Task expectedTask = fromModifiedTask().withId(taskId).build();
        final List<Task> expectedTasks = List.of(expectedTask);

        manager.updateTask(update);
        final Task savedTask = manager.getTaskById(taskId);
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
    public void shouldUpdateTaskWhenOverlapPreviousVersion(Duration duration, LocalDateTime startTime) {
        final Task oldTask = fromModifiedTask().withId(null).withDuration(duration).withStartTime(startTime).build();
        final long taskId = manager.createTask(oldTask);
        final Task update = fromTestTask().withId(taskId).build();
        final Task expectedTask = fromTestTask().withId(taskId).build();
        final List<Task> expectedTasks = List.of(expectedTask);

        manager.updateTask(update);
        final Task savedTask = manager.getTaskById(taskId);
        final List<Task> tasks = manager.getTasks();
        final List<Task> prioritized = manager.getPrioritizedTasks();

        assertAll("task saved with errors",
                () -> assertTaskEquals(expectedTask, savedTask, "task saved with errors"),
                () -> assertListEquals(expectedTasks, tasks, "task saved with errors"),
                () -> assertListEquals(expectedTasks, prioritized, "task saved with errors")
        );
    }

    @Test
    public void shouldUpdateTaskWhenExactlyBeforeAnotherPrioritizedTask() {
        final long epicId = manager.createEpic(testEpic);
        final Subtask subtask = fromTestSubtask().withId(null).withEpicId(epicId)
                .withStartTime(TEST_START_TIME.plus(TEST_DURATION)).build();
        final long subtaskId = manager.createSubtask(subtask);
        final long taskId = manager.createTask(modifiedTask);
        final Task update = fromTestTask().withId(taskId).build();
        final Subtask expectedSubtask = fromTestSubtask().withId(subtaskId).withEpicId(epicId)
                .withStartTime(TEST_START_TIME.plus(TEST_DURATION)).build();
        final Task expectedTask = fromTestTask().withId(taskId).build();
        final List<Task> expectedTasks = List.of(expectedTask);
        final List<Task> expectedPrioritized = List.of(expectedTask, expectedSubtask);

        manager.updateTask(update);
        final Task savedTask = manager.getTaskById(taskId);
        final List<Task> tasks = manager.getTasks();
        final List<Task> prioritized = manager.getPrioritizedTasks();

        assertAll("task saved with errors",
                () -> assertTaskEquals(expectedTask, savedTask, "task saved with errors"),
                () -> assertListEquals(expectedTasks, tasks, "task saved with errors"),
                () -> assertListEquals(expectedPrioritized, prioritized, "task saved with errors")
        );
    }

    @Test
    public void shouldUpdateTaskWhenWithDurationTruncatedExactlyBeforeAnotherPrioritizedTask() {
        final long epicId = manager.createEpic(testEpic);
        final Subtask subtask = fromTestSubtask().withId(null).withEpicId(epicId)
                .withStartTime(TEST_START_TIME.plus(TEST_DURATION)).build();
        final long subtaskId = manager.createSubtask(subtask);
        final long taskId = manager.createTask(modifiedTask);
        final Task update = fromTestTask().withId(taskId).withDuration(TEST_DURATION.plusSeconds(25L)).build();
        final Subtask expectedSubtask = fromTestSubtask().withId(subtaskId).withEpicId(epicId)
                .withStartTime(TEST_START_TIME.plus(TEST_DURATION)).build();
        final Task expectedTask = fromTestTask().withId(taskId).build();
        final List<Task> expectedTasks = List.of(expectedTask);
        final List<Task> expectedPrioritized = List.of(expectedTask, expectedSubtask);

        manager.updateTask(update);
        final Task savedTask = manager.getTaskById(taskId);
        final List<Task> tasks = manager.getTasks();
        final List<Task> prioritized = manager.getPrioritizedTasks();

        assertAll("task saved with errors",
                () -> assertTaskEquals(expectedTask, savedTask, "task saved with errors"),
                () -> assertListEquals(expectedTasks, tasks, "task saved with errors"),
                () -> assertListEquals(expectedPrioritized, prioritized, "task saved with errors")
        );
    }

    @Test
    public void shouldUpdateTaskWhenWithStartTimeTruncatedExactlyBeforeAnotherPrioritizedTask() {
        final long epicId = manager.createEpic(testEpic);
        final Subtask subtask = fromTestSubtask().withId(null).withEpicId(epicId)
                .withStartTime(TEST_START_TIME.plus(TEST_DURATION)).build();
        final long subtaskId = manager.createSubtask(subtask);
        final long taskId = manager.createTask(modifiedTask);
        final Task update = fromTestTask().withId(taskId).withStartTime(TEST_START_TIME.plusSeconds(25L)).build();
        final Subtask expectedSubtask = fromTestSubtask().withId(subtaskId).withEpicId(epicId)
                .withStartTime(TEST_START_TIME.plus(TEST_DURATION)).build();
        final Task expectedTask = fromTestTask().withId(taskId).build();
        final List<Task> expectedTasks = List.of(expectedTask);
        final List<Task> expectedPrioritized = List.of(expectedTask, expectedSubtask);

        manager.updateTask(update);
        final Task savedTask = manager.getTaskById(taskId);
        final List<Task> tasks = manager.getTasks();
        final List<Task> prioritized = manager.getPrioritizedTasks();

        assertAll("task saved with errors",
                () -> assertTaskEquals(expectedTask, savedTask, "task saved with errors"),
                () -> assertListEquals(expectedTasks, tasks, "task saved with errors"),
                () -> assertListEquals(expectedPrioritized, prioritized, "task saved with errors")
        );
    }

    @Test
    public void shouldUpdateTaskWhenExactlyAfterAnotherPrioritizedTask() {
        final long epicId = manager.createEpic(testEpic);
        final Subtask subtask = fromTestSubtask().withId(null).withEpicId(epicId)
                .withStartTime(TEST_START_TIME.minus(TEST_DURATION)).build();
        final long subtaskId = manager.createSubtask(subtask);
        final long taskId = manager.createTask(modifiedTask);
        final Task update = fromTestTask().withId(taskId).build();
        final Subtask expectedSubtask = fromTestSubtask().withId(subtaskId).withEpicId(epicId)
                .withStartTime(TEST_START_TIME.minus(TEST_DURATION)).build();
        final Task expectedTask = fromTestTask().withId(taskId).build();
        final List<Task> expectedTasks = List.of(expectedTask);
        final List<Task> expectedPrioritized = List.of(expectedSubtask, expectedTask);

        manager.updateTask(update);
        final Task savedTask = manager.getTaskById(taskId);
        final List<Task> tasks = manager.getTasks();
        final List<Task> prioritized = manager.getPrioritizedTasks();

        assertAll("task saved with errors",
                () -> assertTaskEquals(expectedTask, savedTask, "task saved with errors"),
                () -> assertListEquals(expectedTasks, tasks, "task saved with errors"),
                () -> assertListEquals(expectedPrioritized, prioritized, "task saved with errors")
        );
    }

    @Test
    public void shouldUpdateTaskWhenDurationAndStartTimeBecomeNull() {
        final long taskId = manager.createTask(testTask);
        final Task update = fromModifiedTask().withId(taskId).withDuration(null).withStartTime(null).build();
        final Task expectedTask = fromModifiedTask().withId(taskId).withDuration(null).withStartTime(null).build();
        final List<Task> expectedTasks = List.of(expectedTask);

        manager.updateTask(update);
        final Task savedTask = manager.getTaskById(taskId);
        final List<Task> tasks = manager.getTasks();
        final List<Task> prioritized = manager.getPrioritizedTasks();

        assertAll("task saved with errors",
                () -> assertTaskEquals(expectedTask, savedTask, "task saved with errors"),
                () -> assertListEquals(expectedTasks, tasks, "task saved with errors"),
                () -> assertTrue(prioritized.isEmpty(), "task saved with errors")
        );
    }

    @Test
    public void shouldUpdateTaskWhenDurationAndStartTimeWereNull() {
        final Task oldTask = fromTestTask().withId(null).withDuration(null).withStartTime(null).build();
        final long taskId = manager.createTask(oldTask);
        final Task update = fromModifiedTask().withId(taskId).build();
        final Task expectedTask = fromModifiedTask().withId(taskId).build();
        final List<Task> expectedTasks = List.of(expectedTask);

        manager.updateTask(update);
        final Task savedTask = manager.getTaskById(taskId);
        final List<Task> tasks = manager.getTasks();
        final List<Task> prioritized = manager.getPrioritizedTasks();

        assertAll("task saved with errors",
                () -> assertTaskEquals(expectedTask, savedTask, "task saved with errors"),
                () -> assertListEquals(expectedTasks, tasks, "task saved with errors"),
                () -> assertListEquals(expectedTasks, prioritized, "task saved with errors")
        );
    }

    @Test
    public void shouldUpdateTaskWhenDurationAndStartTimeNull() {
        final Task oldTask = fromTestTask().withId(null).withDuration(null).withStartTime(null).build();
        final long taskId = manager.createTask(oldTask);
        final Task update = fromModifiedTask().withId(taskId).withDuration(null).withStartTime(null).build();
        final Task expectedTask = fromModifiedTask().withId(taskId).withDuration(null).withStartTime(null).build();
        final List<Task> expectedTasks = List.of(expectedTask);

        manager.updateTask(update);
        final Task savedTask = manager.getTaskById(taskId);
        final List<Task> tasks = manager.getTasks();
        final List<Task> prioritized = manager.getPrioritizedTasks();

        assertAll("task saved with errors",
                () -> assertTaskEquals(expectedTask, savedTask, "task saved with errors"),
                () -> assertListEquals(expectedTasks, tasks, "task saved with errors"),
                () -> assertTrue(prioritized.isEmpty(), "task saved with errors")
        );
    }

    @Test
    public void shouldUpdateTaskWhenFieldsBecomeNull() {
        final long taskId = manager.createTask(testTask);
        final Task update = fromEmptyTask().withId(taskId).withStatus(TaskStatus.NEW).build();
        final Task expectedTask = fromEmptyTask().withId(taskId).withStatus(TaskStatus.NEW).build();
        final List<Task> expectedTasks = List.of(expectedTask);

        manager.updateTask(update);
        final Task savedTask = manager.getTaskById(taskId);
        final List<Task> tasks = manager.getTasks();
        final List<Task> prioritized = manager.getPrioritizedTasks();

        assertAll("task saved with errors",
                () -> assertTaskEquals(expectedTask, savedTask, "task saved with errors"),
                () -> assertListEquals(expectedTasks, tasks, "task saved with errors"),
                () -> assertTrue(prioritized.isEmpty(), "task saved with errors")
        );
    }

    @Test
    public void shouldNotDeleteTaskWhenNotExist() {
        final long taskId = -1L;
        final String expectedMessage = "no task with id=" + taskId;

        final Exception exception = assertThrows(TaskNotFoundException.class, () -> manager.deleteTask(taskId));
        assertEquals(expectedMessage, exception.getMessage(), WRONG_EXCEPTION_MESSAGE);
    }

    @Test
    public void shouldDeleteTask() {
        final long taskId = manager.createTask(testTask);

        manager.deleteTask(taskId);
        final List<Task> tasks = manager.getTasks();
        final List<Task> prioritized = manager.getPrioritizedTasks();

        assertAll("task removed with errors",
                () -> assertThrows(TaskNotFoundException.class, () -> manager.getTaskById(taskId),
                        "task removed with errors"),
                () -> assertTrue(tasks.isEmpty(), "task removed with errors"),
                () -> assertTrue(prioritized.isEmpty(), "task removed with errors")
        );
    }

    @Test
    public void shouldNotGetEpicByIdWhenNotExist() {
        final long epicId = -1L;
        final String expectedMessage = "no epic with id=" + epicId;

        final Exception exception = assertThrows(TaskNotFoundException.class, () -> manager.getEpicById(epicId));
        assertEquals(expectedMessage, exception.getMessage(), WRONG_EXCEPTION_MESSAGE);
    }

    @Test
    public void shouldGetEpicById() {
        final long epicId = manager.createEpic(testEpic);
        final Epic expectedEpic = fromTestEpic().withId(epicId).withStatus(TaskStatus.NEW).build();

        final Epic savedEpic = manager.getEpicById(epicId);

        assertTaskEquals(expectedEpic, savedEpic, "epic saved with errors");
    }

    @Test
    public void shouldNotCreateEpicWhenNull() {
        final String expectedMessage = "cannot create null epic";

        final Exception exception = assertThrows(NullPointerException.class, () -> manager.createEpic(null));
        assertEquals(expectedMessage, exception.getMessage(), WRONG_EXCEPTION_MESSAGE);
    }

    @Test
    public void shouldCreateEpic() {
        final long epicId = manager.createEpic(testEpic);
        final Epic savedEpic = manager.getEpicById(epicId);
        final List<Epic> epics = manager.getEpics();

        final Epic expectedEpic = fromTestEpic().withId(epicId).withStatus(TaskStatus.NEW).build();
        final List<Epic> expectedEpics = List.of(expectedEpic);
        assertAll("epic saved with errors",
                () -> assertTaskEquals(expectedEpic, savedEpic, "epic saved with errors"),
                () -> assertListEquals(expectedEpics, epics, "epic saved with errors")
        );
    }

    @Test
    public void shouldCreateEpicWhenFieldsNull() {
        final long epicId = manager.createEpic(emptyEpic);
        final Epic savedEpic = manager.getEpicById(epicId);
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
        final String expectedMessage = "id cannot be null";

        final Exception exception = assertThrows(ManagerValidationException.class, () -> manager.updateEpic(testEpic));
        assertEquals(expectedMessage, exception.getMessage(), WRONG_EXCEPTION_MESSAGE);
    }

    @Test
    public void shouldNotUpdateEpicWhenTaskIdAsId() {
        final long taskId = manager.createTask(testTask);
        final String expectedMessage = "wrong task type";
        final Epic epic = fromModifiedEpic().withId(taskId).build();

        final Exception exception = assertThrows(ManagerValidationException.class, () -> manager.updateEpic(epic));
        assertEquals(expectedMessage, exception.getMessage(), WRONG_EXCEPTION_MESSAGE);
    }

    @Test
    public void shouldNotUpdateEpicWhenSubtaskIdAsId() {
        final long anotherEpicId = manager.createEpic(testEpic);
        final Subtask subtask = fromTestSubtask().withId(null).withEpicId(anotherEpicId).build();
        final long subtaskId = manager.createSubtask(subtask);
        final String expectedMessage = "wrong task type";
        final Epic epic = fromModifiedEpic().withId(subtaskId).build();

        final Exception exception = assertThrows(ManagerValidationException.class, () -> manager.updateEpic(epic));
        assertEquals(expectedMessage, exception.getMessage(), WRONG_EXCEPTION_MESSAGE);
    }

    @Test
    public void shouldUpdateEpic() {
        final long epicId = manager.createEpic(testEpic);
        final Epic update = fromModifiedEpic().withId(epicId).build();
        final Epic expectedEpic = fromModifiedEpic().withId(epicId).withStatus(TaskStatus.NEW).build();
        final List<Epic> expectedEpics = List.of(expectedEpic);

        manager.updateEpic(update);
        final Epic savedEpic = manager.getEpicById(epicId);
        final List<Epic> epics = manager.getEpics();

        assertAll("epic saved with errors",
                () -> assertTaskEquals(expectedEpic, savedEpic, "epic saved with errors"),
                () -> assertListEquals(expectedEpics, epics, "epic saved with errors")
        );
    }

    @Test
    public void shouldUpdateEpicWhenIdNotExist() {
        final Epic update = fromTestEpic().withId(ANOTHER_TEST_ID).build();
        final Epic expectedEpic = fromTestEpic().withId(ANOTHER_TEST_ID).withStatus(TaskStatus.NEW).build();
        final Epic expectedNextEpic = fromModifiedEpic().withId(ANOTHER_TEST_ID + 1).withStatus(TaskStatus.NEW).build();
        final List<Epic> expectedEpics = List.of(expectedEpic, expectedNextEpic);

        manager.updateEpic(update);
        final long nextId = manager.createEpic(modifiedEpic);
        final Epic savedEpic = manager.getEpicById(ANOTHER_TEST_ID);
        final Epic nextEpic = manager.getEpicById(nextId);
        final List<Epic> epics = manager.getEpics();

        assertAll("epic saved with errors",
                () -> assertEquals(ANOTHER_TEST_ID + 1, nextId, "epic saved with errors"),
                () -> assertTaskEquals(expectedEpic, savedEpic, "epic saved with errors"),
                () -> assertTaskEquals(expectedNextEpic, nextEpic, "epic saved with errors"),
                () -> assertListEquals(expectedEpics, epics, "epic saved with errors")
        );
    }

    @Test
    public void shouldUpdateEpicWhenFieldsBecomeNull() {
        final long epicId = manager.createEpic(testEpic);
        final Epic update = fromEmptyEpic().withId(epicId).build();
        final Epic expectedEpic = fromEmptyEpic().withId(epicId).withStatus(TaskStatus.NEW).build();
        final List<Epic> expectedEpics = List.of(expectedEpic);

        manager.updateEpic(update);
        final Epic savedEpic = manager.getEpicById(epicId);
        final List<Epic> epics = manager.getEpics();

        assertAll("epic saved with errors",
                () -> assertTaskEquals(expectedEpic, savedEpic, "epic saved with errors"),
                () -> assertListEquals(expectedEpics, epics, "epic saved with errors")
        );
    }

    @Test
    public void shouldRetainEpicSubtaskIdsWhenUpdate() {
        final long epicId = manager.createEpic(testEpic);
        final Subtask subtask = fromTestSubtask().withId(null).withEpicId(epicId).build();
        final long subtaskId = manager.createSubtask(subtask);
        final Epic update = fromModifiedEpic().withId(epicId).build();
        final Epic expectedEpic = fromModifiedEpic().withId(epicId).withSubtaskIds(List.of(subtaskId))
                .withDuration(TEST_DURATION).withStartTime(TEST_START_TIME).withEndTime(TEST_END_TIME)
                .withStatus(TaskStatus.IN_PROGRESS).build();

        manager.updateEpic(update);
        final Epic savedEpic = manager.getEpicById(epicId);

        assertTaskEquals(expectedEpic, savedEpic, "epic saved with errors");
    }

    @Test
    public void shouldNotDeleteEpicWhenNotExist() {
        final long epicId = -1L;
        final String expectedMessage = "no epic with id=" + epicId;

        final Exception exception = assertThrows(TaskNotFoundException.class, () -> manager.deleteEpic(epicId));
        assertEquals(expectedMessage, exception.getMessage(), WRONG_EXCEPTION_MESSAGE);
    }

    @Test
    public void shouldDeleteEpic() {
        final long epicId = manager.createEpic(testEpic);

        manager.deleteEpic(epicId);
        final List<Epic> epics = manager.getEpics();

        assertAll("epic removed with errors",
                () -> assertThrows(TaskNotFoundException.class, () -> manager.getEpicById(epicId),
                        "epic removed with errors"),
                () -> assertTrue(epics.isEmpty(), "epic removed with errors")
        );
    }

    @Test
    public void shouldNotGetSubtaskByIdWhenNotExist() {
        final long subtaskId = -1L;
        final String expectedMessage = "no subtask with id=" + subtaskId;

        final Exception exception = assertThrows(TaskNotFoundException.class, () -> manager.getSubtaskById(subtaskId));
        assertEquals(expectedMessage, exception.getMessage(), WRONG_EXCEPTION_MESSAGE);
    }

    @Test
    public void shouldGetSubtaskById() {
        final long epicId = manager.createEpic(testEpic);
        final Subtask subtask = fromTestSubtask().withId(null).withEpicId(epicId).build();
        final long subtaskId = manager.createSubtask(subtask);
        final Subtask expectedSubtask = fromTestSubtask().withId(subtaskId).withEpicId(epicId).build();

        final Subtask savedSubtask = manager.getSubtaskById(subtaskId);

        assertTaskEquals(expectedSubtask, savedSubtask, "subtask saved with errors");
    }

    @Test
    public void shouldNotCreateSubtaskWhenNull() {
        final String expectedMessage = "cannot create null subtask";

        final Exception exception = assertThrows(NullPointerException.class, () -> manager.createSubtask(null));
        assertEquals(expectedMessage, exception.getMessage(), WRONG_EXCEPTION_MESSAGE);
    }

    @Test
    public void shouldNotCreateSubtaskWhenEpicIdNull() {
        final String expectedMessage = "wrong epic id";
        final Subtask subtask = fromTestSubtask().withId(null).withEpicId(null).build();

        final Exception exception = assertThrows(ManagerValidationException.class,
                () -> manager.createSubtask(subtask));
        assertEquals(expectedMessage, exception.getMessage(), WRONG_EXCEPTION_MESSAGE);
    }

    @Test
    public void shouldNotCreateSubtaskWhenEpicNotExist() {
        final long epicId = -1L;
        final String expectedMessage = "wrong epic id";
        final Subtask subtask = fromTestSubtask().withId(null).withEpicId(epicId).build();

        final Exception exception = assertThrows(ManagerValidationException.class,
                () -> manager.createSubtask(subtask));
        assertEquals(expectedMessage, exception.getMessage(), WRONG_EXCEPTION_MESSAGE);
    }

    @Test
    public void shouldNotCreateSubtaskWhenTaskIdAsEpicId() {
        final long taskId = manager.createTask(testTask);
        final String expectedMessage = "wrong epic id";
        final Subtask subtask = fromModifiedSubtask().withId(null).withEpicId(taskId).build();

        final Exception exception = assertThrows(ManagerValidationException.class,
                () -> manager.createSubtask(subtask));
        assertEquals(expectedMessage, exception.getMessage(), WRONG_EXCEPTION_MESSAGE);
    }

    @Test
    public void shouldNotCreateSubtaskWhenSubtaskIdAsEpicId() {
        final long epicId = manager.createEpic(testEpic);
        final Subtask anotherSubtask = fromTestSubtask().withId(null).withEpicId(epicId).build();
        final long subtaskId = manager.createSubtask(anotherSubtask);
        final String expectedMessage = "wrong epic id";
        final Subtask subtask = fromModifiedSubtask().withId(null).withEpicId(subtaskId).build();

        final Exception exception = assertThrows(ManagerValidationException.class,
                () -> manager.createSubtask(subtask));
        assertEquals(expectedMessage, exception.getMessage(), WRONG_EXCEPTION_MESSAGE);
    }

    @Test
    public void shouldNotCreateSubtaskWhenDurationNullAndStartTimeNotNull() {
        final long epicId = manager.createEpic(testEpic);
        final Subtask subtask = fromTestSubtask().withId(null).withEpicId(epicId).withDuration(null).build();

        final Exception exception = assertThrows(ManagerValidationException.class,
                () -> manager.createSubtask(subtask));
        assertEquals("duration and start time must be either both set or both null", exception.getMessage(),
                WRONG_EXCEPTION_MESSAGE);
    }

    @Test
    public void shouldNotCreateSubtaskWhenDurationNotNullAndStartTimeNull() {
        final long epicId = manager.createEpic(testEpic);
        final Subtask subtask = fromTestSubtask().withId(null).withEpicId(epicId).withStartTime(null).build();

        final Exception exception = assertThrows(ManagerValidationException.class,
                () -> manager.createSubtask(subtask));
        assertEquals("duration and start time must be either both set or both null", exception.getMessage(),
                WRONG_EXCEPTION_MESSAGE);
    }

    @ParameterizedTest
    @MethodSource("io.github.akuniutka.kanban.TestModels#getOverlappingTimeSlots")
    public void shouldNotCreateSubtaskWhenOverlapAnotherPrioritizedTask(Duration duration, LocalDateTime startTime) {
        final Task overlappingTask = fromTestTask().withId(null).withDuration(duration).withStartTime(startTime)
                .build();
        manager.createTask(overlappingTask);
        final long epicId = manager.createEpic(testEpic);
        final Subtask subtask = fromTestSubtask().withId(null).withEpicId(epicId).build();

        final Exception exception = assertThrows(ManagerValidationException.class,
                () -> manager.createSubtask(subtask));
        assertEquals("conflict with another task for time slot", exception.getMessage(), WRONG_EXCEPTION_MESSAGE);
    }

    @Test
    public void shouldNotCreateSubtaskWhenStatusNull() {
        final long epicId = manager.createEpic(testEpic);
        final Subtask subtask = fromTestSubtask().withId(null).withEpicId(epicId).withStatus(null).build();

        final Exception exception = assertThrows(ManagerValidationException.class,
                () -> manager.createSubtask(subtask));
        assertEquals("status cannot be null", exception.getMessage(), WRONG_EXCEPTION_MESSAGE);
    }

    @Test
    public void shouldCreateSubtaskWhenDurationAndStartTimeNotNull() {
        final long epicId = manager.createEpic(testEpic);
        final Subtask subtask = fromTestSubtask().withId(null).withEpicId(epicId).build();

        final long subtaskId = manager.createSubtask(subtask);
        final Subtask savedSubtask = manager.getSubtaskById(subtaskId);
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
    public void shouldCreateSubtaskWithDurationTruncatedToMinutes() {
        final long epicId = manager.createEpic(testEpic);
        final Subtask subtask = fromTestSubtask().withId(null).withEpicId(epicId)
                .withDuration(TEST_DURATION.plusSeconds(25L)).build();

        final long subtaskId = manager.createSubtask(subtask);
        final Subtask savedSubtask = manager.getSubtaskById(subtaskId);
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
    public void shouldCreateSubtaskWithStartTimeTruncatedToMinutes() {
        final long epicId = manager.createEpic(testEpic);
        final Subtask subtask = fromTestSubtask().withId(null).withEpicId(epicId)
                .withStartTime(TEST_START_TIME.plusSeconds(25L)).build();

        final long subtaskId = manager.createSubtask(subtask);
        final Subtask savedSubtask = manager.getSubtaskById(subtaskId);
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
    public void shouldCreateSubtaskWhenDurationAndStartTimeNull() {
        final long epicId = manager.createEpic(testEpic);
        final Subtask subtask = fromTestSubtask().withId(null).withEpicId(epicId).withDuration(null).withStartTime(null)
                .build();

        final long subtaskId = manager.createSubtask(subtask);
        final Subtask savedSubtask = manager.getSubtaskById(subtaskId);
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
    public void shouldCreateSubtaskWhenFieldsNull() {
        final long epicId = manager.createEpic(testEpic);
        final Subtask subtask = fromEmptySubtask().withEpicId(epicId).withStatus(TaskStatus.NEW).build();

        final long subtaskId = manager.createSubtask(subtask);
        final Subtask savedSubtask = manager.getSubtaskById(subtaskId);
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
    public void shouldCreateSubtaskWhenExactlyBeforeAnotherPrioritizedTask() {
        final Task task = fromTestTask().withId(null).withStartTime(TEST_START_TIME.plus(TEST_DURATION)).build();
        final long taskId = manager.createTask(task);
        final long epicId = manager.createEpic(testEpic);
        final Subtask subtask = fromTestSubtask().withId(null).withEpicId(epicId).build();

        final long subtaskId = manager.createSubtask(subtask);
        final Subtask savedSubtask = manager.getSubtaskById(subtaskId);
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
    public void shouldCreateSubtaskWhenWithDurationTruncatedExactlyBeforeTask() {
        final Task task = fromTestTask().withId(null).withStartTime(TEST_START_TIME.plus(TEST_DURATION)).build();
        final long taskId = manager.createTask(task);
        final long epicId = manager.createEpic(testEpic);
        final Subtask subtask = fromTestSubtask().withId(null).withEpicId(epicId)
                .withDuration(TEST_DURATION.plusSeconds(25L)).build();

        final long subtaskId = manager.createSubtask(subtask);
        final Subtask savedSubtask = manager.getSubtaskById(subtaskId);
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
    public void shouldCreateSubtaskWhenWithStartTimeTruncatedExactlyBeforeTask() {
        final Task task = fromTestTask().withId(null).withStartTime(TEST_START_TIME.plus(TEST_DURATION)).build();
        final long taskId = manager.createTask(task);
        final long epicId = manager.createEpic(testEpic);
        final Subtask subtask = fromTestSubtask().withId(null).withEpicId(epicId)
                .withStartTime(TEST_START_TIME.plusSeconds(25L)).build();

        final long subtaskId = manager.createSubtask(subtask);
        final Subtask savedSubtask = manager.getSubtaskById(subtaskId);
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
    public void shouldCreateSubtaskWhenExactlyAfterAnotherPrioritizedTask() {
        final Task task = fromTestTask().withId(null).withStartTime(TEST_START_TIME.minus(TEST_DURATION)).build();
        final long taskId = manager.createTask(task);
        final long epicId = manager.createEpic(testEpic);
        final Subtask subtask = fromTestSubtask().withId(null).withEpicId(epicId).build();

        final long subtaskId = manager.createSubtask(subtask);
        final Subtask savedSubtask = manager.getSubtaskById(subtaskId);
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
        final String expectedMessage = "id cannot be null";
        final long epicId = manager.createEpic(testEpic);
        final Subtask subtask = fromTestSubtask().withId(null).withEpicId(epicId).build();

        final Exception exception = assertThrows(ManagerValidationException.class,
                () -> manager.updateSubtask(subtask));
        assertEquals(expectedMessage, exception.getMessage(), WRONG_EXCEPTION_MESSAGE);
    }

    @Test
    public void shouldNotUpdateSubtaskWhenTaskIdAsId() {
        final long taskId = manager.createTask(modifiedTask);
        final String expectedMessage = "wrong task type";
        final long epicId = manager.createEpic(testEpic);
        final Subtask update = fromTestSubtask().withId(taskId).withEpicId(epicId).build();

        final Exception exception = assertThrows(ManagerValidationException.class, () -> manager.updateSubtask(update));
        assertEquals(expectedMessage, exception.getMessage(), WRONG_EXCEPTION_MESSAGE);
    }

    @Test
    public void shouldNotUpdateSubtaskWhenEpicIdAsId() {
        final String expectedMessage = "wrong task type";
        final long epicId = manager.createEpic(testEpic);
        final Subtask update = fromTestSubtask().withId(epicId).withEpicId(epicId).build();

        final Exception exception = assertThrows(ManagerValidationException.class, () -> manager.updateSubtask(update));
        assertEquals(expectedMessage, exception.getMessage(), WRONG_EXCEPTION_MESSAGE);
    }

    @Test
    public void shouldNotUpdateSubtaskWhenIdNotExistAndEpicIdNull() {
        final String expectedMessage = "wrong epic id";
        final Subtask update = fromTestSubtask().withId(ANOTHER_TEST_ID).withEpicId(null).build();

        final Exception exception = assertThrows(ManagerValidationException.class, () -> manager.updateSubtask(update));
        assertEquals(expectedMessage, exception.getMessage(), WRONG_EXCEPTION_MESSAGE);
    }

    @Test
    public void shouldNotUpdateSubtaskWhenIdNotExistAndEpicNotExist() {
        final String expectedMessage = "wrong epic id";
        final Subtask update = fromTestSubtask().withId(ANOTHER_TEST_ID).withEpicId(ANOTHER_TEST_ID).build();

        final Exception exception = assertThrows(ManagerValidationException.class, () -> manager.updateSubtask(update));
        assertEquals(expectedMessage, exception.getMessage(), WRONG_EXCEPTION_MESSAGE);
    }

    @Test
    public void shouldNotUpdateSubtaskWhenIdNotExistAndTaskIdAsEpicId() {
        final long taskId = manager.createTask(modifiedTask);
        final String expectedMessage = "wrong epic id";
        final Subtask update = fromTestSubtask().withId(ANOTHER_TEST_ID).withEpicId(taskId).build();

        final Exception exception = assertThrows(ManagerValidationException.class, () -> manager.updateSubtask(update));
        assertEquals(expectedMessage, exception.getMessage(), WRONG_EXCEPTION_MESSAGE);
    }

    @Test
    public void shouldNotUpdateSubtaskWhenIdNotExistAndSubtaskIdAsEpicId() {
        final long epicId = manager.createEpic(testEpic);
        final Subtask anotherSubtask = fromTestSubtask().withId(null).withEpicId(epicId).build();
        final long subtaskId = manager.createSubtask(anotherSubtask);
        final String expectedMessage = "wrong epic id";
        final Subtask update = fromModifiedSubtask().withId(ANOTHER_TEST_ID).withEpicId(subtaskId).build();

        final Exception exception = assertThrows(ManagerValidationException.class, () -> manager.updateSubtask(update));
        assertEquals(expectedMessage, exception.getMessage(), WRONG_EXCEPTION_MESSAGE);
    }

    @Test
    public void shouldNotUpdateSubtaskWhenDurationNullAndStartTimeNotNull() {
        final long epicId = manager.createEpic(testEpic);
        final Subtask oldSubtask = fromTestSubtask().withId(null).withEpicId(epicId).build();
        final long subtaskId = manager.createSubtask(oldSubtask);
        final Subtask update = fromModifiedSubtask().withId(subtaskId).withDuration(null).build();

        final Exception exception = assertThrows(ManagerValidationException.class, () -> manager.updateSubtask(update));
        assertEquals("duration and start time must be either both set or both null", exception.getMessage(),
                WRONG_EXCEPTION_MESSAGE);
    }

    @Test
    public void shouldNotUpdateSubtaskWhenDurationNotNullAndStartTimeNull() {
        final long epicId = manager.createEpic(testEpic);
        final Subtask oldSubtask = fromTestSubtask().withId(null).withEpicId(epicId).build();
        final long subtaskId = manager.createSubtask(oldSubtask);
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
        manager.createTask(overlappingTask);
        final long epicId = manager.createEpic(testEpic);
        final Subtask oldSubtask = fromModifiedSubtask().withId(null).withEpicId(epicId).build();
        final long subtaskId = manager.createSubtask(oldSubtask);
        final Subtask update = fromTestSubtask().withId(subtaskId).build();

        final Exception exception = assertThrows(ManagerValidationException.class, () -> manager.updateSubtask(update));
        assertEquals("conflict with another task for time slot", exception.getMessage(), WRONG_EXCEPTION_MESSAGE);
    }

    @Test
    public void shouldNotUpdateSubtaskWhenStatusNull() {
        final long epicId = manager.createEpic(testEpic);
        final Subtask oldSubtask = fromTestSubtask().withId(null).withEpicId(epicId).build();
        final long subtaskId = manager.createSubtask(oldSubtask);
        final Subtask update = fromModifiedSubtask().withId(subtaskId).withStatus(null).build();

        final Exception exception = assertThrows(ManagerValidationException.class, () -> manager.updateSubtask(update));
        assertEquals("status cannot be null", exception.getMessage(), WRONG_EXCEPTION_MESSAGE);
    }

    @Test
    public void shouldUpdateSubtaskWhenDurationAndStartTimeNotNull() {
        final long epicId = manager.createEpic(testEpic);
        final Subtask oldSubtask = fromTestSubtask().withId(null).withEpicId(epicId).build();
        final long subtaskId = manager.createSubtask(oldSubtask);
        final Subtask update = fromModifiedSubtask().withId(subtaskId).build();
        final Subtask expectedSubtask = fromModifiedSubtask().withId(subtaskId).withEpicId(epicId).build();
        final List<Subtask> expectedSubtasks = List.of(expectedSubtask);

        manager.updateSubtask(update);
        final Subtask savedSubtask = manager.getSubtaskById(subtaskId);
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
    public void shouldUpdateSubtaskWhenIdNotExist() {
        final long epicId = manager.createEpic(testEpic);
        final Subtask update = fromTestSubtask().withId(ANOTHER_TEST_ID).withEpicId(epicId).build();
        final Subtask nextSubtask = fromModifiedSubtask().withId(null).withEpicId(epicId).build();

        manager.updateSubtask(update);
        final long nextId = manager.createSubtask(nextSubtask);
        final Subtask savedSubtask = manager.getSubtaskById(ANOTHER_TEST_ID);
        final Subtask savedNextSubtask = manager.getSubtaskById(nextId);
        final List<Subtask> epicSubtasks = manager.getEpicSubtasks(epicId);
        final List<Subtask> subtasks = manager.getSubtasks();
        final List<Task> prioritized = manager.getPrioritizedTasks();

        final Subtask expectedSubtask = fromTestSubtask().withId(ANOTHER_TEST_ID).withEpicId(epicId).build();
        final Subtask expectedNextSubtask = fromModifiedSubtask().withId(nextId).withEpicId(epicId).build();
        final List<Subtask> expectedSubtasks = List.of(expectedSubtask, expectedNextSubtask);
        assertAll("subtask saved with errors",
                () -> assertEquals(ANOTHER_TEST_ID + 1, nextId, "subtask saved with errors"),
                () -> assertTaskEquals(expectedSubtask, savedSubtask, "subtask saved with errors"),
                () -> assertTaskEquals(expectedNextSubtask, savedNextSubtask, "subtask saved with errors"),
                () -> assertListEquals(expectedSubtasks, epicSubtasks, "subtask saved with errors"),
                () -> assertListEquals(expectedSubtasks, subtasks, "subtask saved with errors"),
                () -> assertListEquals(expectedSubtasks, prioritized, "subtask saved with errors")
        );
    }

    @Test
    public void shouldUpdateSubtaskWithDurationTruncatedToMinutes() {
        final long epicId = manager.createEpic(testEpic);
        final Subtask oldSubtask = fromTestSubtask().withId(null).withEpicId(epicId).build();
        final long subtaskId = manager.createSubtask(oldSubtask);
        final Subtask update = fromModifiedSubtask().withId(subtaskId)
                .withDuration(MODIFIED_DURATION.plusSeconds(25L)).build();
        final Subtask expectedSubtask = fromModifiedSubtask().withId(subtaskId).withEpicId(epicId).build();
        final List<Subtask> expectedSubtasks = List.of(expectedSubtask);

        manager.updateSubtask(update);
        final Subtask savedSubtask = manager.getSubtaskById(subtaskId);
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
    public void shouldUpdateSubtaskWithStartTimeTruncatedToMinutes() {
        final long epicId = manager.createEpic(testEpic);
        final Subtask oldSubtask = fromTestSubtask().withId(null).withEpicId(epicId).build();
        final long subtaskId = manager.createSubtask(oldSubtask);
        final Subtask update = fromModifiedSubtask().withId(subtaskId)
                .withStartTime(MODIFIED_START_TIME.plusSeconds(25L)).build();
        final Subtask expectedSubtask = fromModifiedSubtask().withId(subtaskId).withEpicId(epicId).build();
        final List<Subtask> expectedSubtasks = List.of(expectedSubtask);

        manager.updateSubtask(update);
        final Subtask savedSubtask = manager.getSubtaskById(subtaskId);
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
    public void shouldUpdateSubtaskWhenOverlapPreviousVersion(Duration duration, LocalDateTime startTime) {
        final long epicId = manager.createEpic(testEpic);
        final Subtask oldSubtask = fromModifiedSubtask().withId(null).withEpicId(epicId).withDuration(duration)
                .withStartTime(startTime).build();
        final long subtaskId = manager.createSubtask(oldSubtask);
        final Subtask update = fromTestSubtask().withId(subtaskId).build();
        final Subtask expectedSubtask = fromTestSubtask().withId(subtaskId).withEpicId(epicId).build();
        final List<Subtask> expectedSubtasks = List.of(expectedSubtask);

        manager.updateSubtask(update);
        final Subtask savedSubtask = manager.getSubtaskById(subtaskId);
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
    public void shouldUpdateSubtaskWhenExactlyBeforeAnotherPrioritizedTask() {
        final Task task = fromTestTask().withId(null).withStartTime(TEST_START_TIME.plus(TEST_DURATION)).build();
        final long taskId = manager.createTask(task);
        final long epicId = manager.createEpic(testEpic);
        final Subtask oldSubtask = fromModifiedSubtask().withId(null).withEpicId(epicId).build();
        final long subtaskId = manager.createSubtask(oldSubtask);
        final Subtask update = fromTestSubtask().withId(subtaskId).build();
        final Task expectedTask = fromTestTask().withId(taskId).withStartTime(TEST_START_TIME.plus(TEST_DURATION))
                .build();
        final Subtask expectedSubtask = fromTestSubtask().withId(subtaskId).withEpicId(epicId).build();
        final List<Subtask> expectedSubtasks = List.of(expectedSubtask);
        final List<Task> expectedPrioritized = List.of(expectedSubtask, expectedTask);

        manager.updateSubtask(update);
        final Subtask savedSubtask = manager.getSubtaskById(subtaskId);
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
    public void shouldUpdateSubtaskWhenWithDurationTruncatedExactlyBeforeAnotherPrioritizedTask() {
        final Task task = fromTestTask().withId(null).withStartTime(TEST_START_TIME.plus(TEST_DURATION)).build();
        final long taskId = manager.createTask(task);
        final long epicId = manager.createEpic(testEpic);
        final Subtask oldSubtask = fromModifiedSubtask().withId(null).withEpicId(epicId).build();
        final long subtaskId = manager.createSubtask(oldSubtask);
        final Subtask update = fromTestSubtask().withId(subtaskId).withDuration(TEST_DURATION.plusSeconds(25L)).build();
        final Task expectedTask = fromTestTask().withId(taskId).withStartTime(TEST_START_TIME.plus(TEST_DURATION))
                .build();
        final Subtask expectedSubtask = fromTestSubtask().withId(subtaskId).withEpicId(epicId).build();
        final List<Subtask> expectedSubtasks = List.of(expectedSubtask);
        final List<Task> expectedPrioritized = List.of(expectedSubtask, expectedTask);

        manager.updateSubtask(update);
        final Subtask savedSubtask = manager.getSubtaskById(subtaskId);
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
    public void shouldUpdateSubtaskWhenWithStartTimeTruncatedExactlyBeforeAnotherPrioritizedTask() {
        final Task task = fromTestTask().withId(null).withStartTime(TEST_START_TIME.plus(TEST_DURATION)).build();
        final long taskId = manager.createTask(task);
        final long epicId = manager.createEpic(testEpic);
        final Subtask oldSubtask = fromModifiedSubtask().withId(null).withEpicId(epicId).build();
        final long subtaskId = manager.createSubtask(oldSubtask);
        final Subtask update = fromTestSubtask().withId(subtaskId).withStartTime(TEST_START_TIME.plusSeconds(25L))
                .build();
        final Task expectedTask = fromTestTask().withId(taskId).withStartTime(TEST_START_TIME.plus(TEST_DURATION))
                .build();
        final Subtask expectedSubtask = fromTestSubtask().withId(subtaskId).withEpicId(epicId).build();
        final List<Subtask> expectedSubtasks = List.of(expectedSubtask);
        final List<Task> expectedPrioritized = List.of(expectedSubtask, expectedTask);

        manager.updateSubtask(update);
        final Subtask savedSubtask = manager.getSubtaskById(subtaskId);
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
    public void shouldUpdateSubtaskWhenExactlyAfterAnotherPrioritizedTask() {
        final Task task = fromTestTask().withId(null).withStartTime(TEST_START_TIME.minus(TEST_DURATION)).build();
        final long taskId = manager.createTask(task);
        final long epicId = manager.createEpic(testEpic);
        final Subtask oldSubtask = fromModifiedSubtask().withId(null).withEpicId(epicId).build();
        final long subtaskId = manager.createSubtask(oldSubtask);
        final Subtask update = fromTestSubtask().withId(subtaskId).build();
        final Task expectedTask = fromTestTask().withId(taskId).withStartTime(TEST_START_TIME.minus(TEST_DURATION))
                .build();
        final Subtask expectedSubtask = fromTestSubtask().withId(subtaskId).withEpicId(epicId).build();
        final List<Subtask> expectedSubtasks = List.of(expectedSubtask);
        final List<Task> expectedPrioritized = List.of(expectedTask, expectedSubtask);

        manager.updateSubtask(update);
        final Subtask savedSubtask = manager.getSubtaskById(subtaskId);
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
    public void shouldUpdateSubtaskWhenDurationAndStartTimeBecomeNull() {
        final long epicId = manager.createEpic(testEpic);
        final Subtask oldSubtask = fromTestSubtask().withId(null).withEpicId(epicId).build();
        final long subtaskId = manager.createSubtask(oldSubtask);
        final Subtask update = fromModifiedSubtask().withId(subtaskId).withDuration(null).withStartTime(null).build();
        final Subtask expectedSubtask = fromModifiedSubtask().withId(subtaskId).withEpicId(epicId).withDuration(null)
                .withStartTime(null).build();
        final List<Subtask> expectedSubtasks = List.of(expectedSubtask);

        manager.updateSubtask(update);
        final Subtask savedSubtask = manager.getSubtaskById(subtaskId);
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
    public void shouldUpdateSubtaskWhenDurationAndStartTimeWereNull() {
        final long epicId = manager.createEpic(testEpic);
        final Subtask oldSubtask = fromTestSubtask().withId(null).withEpicId(epicId).withDuration(null)
                .withStartTime(null).build();
        final long subtaskId = manager.createSubtask(oldSubtask);
        final Subtask update = fromModifiedSubtask().withId(subtaskId).build();
        final Subtask expectedSubtask = fromModifiedSubtask().withId(subtaskId).withEpicId(epicId).build();
        final List<Subtask> expectedSubtasks = List.of(expectedSubtask);

        manager.updateSubtask(update);
        final Subtask savedSubtask = manager.getSubtaskById(subtaskId);
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
    public void shouldUpdateSubtaskWhenDurationAndStartTimeNull() {
        final long epicId = manager.createEpic(testEpic);
        final Subtask oldSubtask = fromTestSubtask().withId(null).withEpicId(epicId).withDuration(null)
                .withStartTime(null).build();
        final long subtaskId = manager.createSubtask(oldSubtask);
        final Subtask update = fromModifiedSubtask().withId(subtaskId).withDuration(null).withStartTime(null).build();
        final Subtask expectedSubtask = fromModifiedSubtask().withId(subtaskId).withEpicId(epicId).withDuration(null)
                .withStartTime(null).build();
        final List<Subtask> expectedSubtasks = List.of(expectedSubtask);

        manager.updateSubtask(update);
        final Subtask savedSubtask = manager.getSubtaskById(subtaskId);
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
    public void shouldUpdateSubtaskWhenFieldsBecomeNull() {
        final long epicId = manager.createEpic(testEpic);
        final Subtask oldSubtask = fromTestSubtask().withId(null).withEpicId(epicId).build();
        final long subtaskId = manager.createSubtask(oldSubtask);
        final Subtask update = fromEmptySubtask().withId(subtaskId).withStatus(TaskStatus.NEW).build();
        final Subtask expectedSubtask = fromEmptySubtask().withId(subtaskId).withEpicId(epicId)
                .withStatus(TaskStatus.NEW).build();
        final List<Subtask> expectedSubtasks = List.of(expectedSubtask);

        manager.updateSubtask(update);
        final Subtask savedSubtask = manager.getSubtaskById(subtaskId);
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
    public void shouldNotDeleteSubtaskWhenNotExist() {
        final long subtaskId = -1L;
        final String expectedMessage = "no subtask with id=" + subtaskId;

        final Exception exception = assertThrows(TaskNotFoundException.class, () -> manager.deleteSubtask(subtaskId));
        assertEquals(expectedMessage, exception.getMessage(), WRONG_EXCEPTION_MESSAGE);
    }

    @Test
    public void shouldDeleteSubtask() {
        final long epicId = manager.createEpic(testEpic);
        final Subtask subtask = fromTestSubtask().withId(null).withEpicId(epicId).build();
        final long subtaskId = manager.createSubtask(subtask);

        manager.deleteSubtask(subtaskId);
        final List<Subtask> epicSubtasks = manager.getEpicSubtasks(epicId);
        final List<Subtask> subtasks = manager.getSubtasks();
        final List<Task> prioritized = manager.getPrioritizedTasks();

        assertAll("subtask removed with errors",
                () -> assertThrows(TaskNotFoundException.class, () -> manager.getSubtaskById(subtaskId),
                        "subtask removed with errors"),
                () -> assertTrue(epicSubtasks.isEmpty(), "subtask removed with errors"),
                () -> assertTrue(subtasks.isEmpty(), "subtask removed with errors"),
                () -> assertTrue(prioritized.isEmpty(), "subtask removed with errors")
        );
    }

    @Test
    public void shouldDeleteSubtaskWhenDeleteEpic() {
        final long epicId = manager.createEpic(testEpic);
        final Subtask subtask = fromTestSubtask().withId(null).withEpicId(epicId).build();
        final long subtaskId = manager.createSubtask(subtask);

        manager.deleteEpic(epicId);
        final List<Subtask> subtasks = manager.getSubtasks();
        final List<Task> prioritized = manager.getPrioritizedTasks();

        assertAll("subtask removed with errors",
                () -> assertThrows(TaskNotFoundException.class, () -> manager.getSubtaskById(subtaskId),
                        "subtask removed with errors"),
                () -> assertTrue(subtasks.isEmpty(), "subtask removed with errors"),
                () -> assertTrue(prioritized.isEmpty(), "subtask removed with errors")
        );
    }

    @Test
    public void shouldAssignTaskNewIdWhenCreateTaskAndIdNotNullAndIdNotExist() {
        final Task task = fromTestTask().withId(ANOTHER_TEST_ID).build();

        final long taskId = manager.createTask(task);
        final Task savedTask = manager.getTaskById(taskId);
        final List<Task> tasks = manager.getTasks();
        final List<Task> prioritized = manager.getPrioritizedTasks();

        final Task expectedTask = fromTestTask().withId(taskId).build();
        final List<Task> expectedTasks = List.of(expectedTask);
        assertAll("task saved with errors",
                () -> assertNotEquals(ANOTHER_TEST_ID, taskId, "task saved with errors"),
                () -> assertThrows(TaskNotFoundException.class, () -> manager.getTaskById(ANOTHER_TEST_ID),
                        "task saved with errors"),
                () -> assertTaskEquals(expectedTask, savedTask, "task saved with errors"),
                () -> assertListEquals(expectedTasks, tasks, "task saved with errors"),
                () -> assertListEquals(expectedTasks, prioritized, "task saved with errors")
        );
    }

    @Test
    public void shouldAssignTaskNewIdWhenCreateTaskAndIdNotNullAndIdExist() {
        final long oldTaskId = manager.createTask(testTask);
        final Task task = fromModifiedTask().withId(oldTaskId).build();

        final long newTaskId = manager.createTask(task);
        final Task oldTask = manager.getTaskById(oldTaskId);
        final Task newTask = manager.getTaskById(newTaskId);
        final List<Task> tasks = manager.getTasks();
        final List<Task> prioritized = manager.getPrioritizedTasks();

        final Task expectedOldTask = fromTestTask().withId(oldTaskId).build();
        final Task expectedNewtask = fromModifiedTask().withId(newTaskId).build();
        final List<Task> expectedTasks = List.of(expectedOldTask, expectedNewtask);
        assertAll("task saved with errors",
                () -> assertNotEquals(oldTaskId, newTaskId, "task saved with errors"),
                () -> assertTaskEquals(expectedOldTask, oldTask, "task saved with errors"),
                () -> assertTaskEquals(expectedNewtask, newTask, "task saved with errors"),
                () -> assertListEquals(expectedTasks, tasks, "task saved with errors"),
                () -> assertListEquals(expectedTasks, prioritized, "task saved with errors")
        );
    }

    @Test
    public void shouldAssignEpicNewIdWhenCreateEpicAndIdNotNullAndIdNotExist() {
        final Epic epic = fromTestEpic().withId(ANOTHER_TEST_ID).build();

        final long epicId = manager.createEpic(epic);
        final Epic savedEpic = manager.getEpicById(epicId);
        final List<Epic> epics = manager.getEpics();

        final Epic expectedEpic = fromTestEpic().withId(epicId).withStatus(TaskStatus.NEW).build();
        final List<Epic> expectedEpics = List.of(expectedEpic);
        assertAll("epic saved with errors",
                () -> assertNotEquals(ANOTHER_TEST_ID, epicId, "epic saved with errors"),
                () -> assertThrows(TaskNotFoundException.class, () -> manager.getEpicById(ANOTHER_TEST_ID),
                        "epic saved with errors"),
                () -> assertTaskEquals(expectedEpic, savedEpic, "epic saved with errors"),
                () -> assertListEquals(expectedEpics, epics, "epic saved with errors")
        );
    }

    @Test
    public void shouldAssignEpicNewIdWhenCreateEpicAndIdNotNullAndIdExist() {
        final long oldEpicId = manager.createEpic(testEpic);
        final Epic epic = fromModifiedEpic().withId(oldEpicId).build();

        final long newEpicId = manager.createEpic(epic);
        final Epic oldEpic = manager.getEpicById(oldEpicId);
        final Epic newEpic = manager.getEpicById(newEpicId);
        final List<Epic> epics = manager.getEpics();

        final Epic expectedOldEpic = fromTestEpic().withId(oldEpicId).withStatus(TaskStatus.NEW).build();
        final Epic expecedNewEpic = fromModifiedEpic().withId(newEpicId).withStatus(TaskStatus.NEW).build();
        final List<Epic> expectedEpics = List.of(expectedOldEpic, expecedNewEpic);
        assertAll("epic saved with errors",
                () -> assertNotEquals(oldEpicId, newEpicId, "epic saved with errors"),
                () -> assertTaskEquals(expectedOldEpic, oldEpic, "epic saved with errors"),
                () -> assertTaskEquals(expecedNewEpic, newEpic, "epic saved with errors"),
                () -> assertListEquals(expectedEpics, epics, "epic saved with errors")
        );
    }

    @Test
    public void shouldAssignSubtaskNewIdWhenCreateSubtaskAndIdNotNullAndIdNotExist() {
        final long epicId = manager.createEpic(testEpic);
        final Subtask subtask = fromTestSubtask().withId(ANOTHER_TEST_ID).withEpicId(epicId).build();

        final long subtaskId = manager.createSubtask(subtask);
        final Subtask savedSubtask = manager.getSubtaskById(subtaskId);
        final List<Subtask> epicSubtasks = manager.getEpicSubtasks(epicId);
        final List<Subtask> subtasks = manager.getSubtasks();
        final List<Task> prioritized = manager.getPrioritizedTasks();

        final Subtask expectedSubtask = fromTestSubtask().withId(subtaskId).withEpicId(epicId).build();
        final List<Subtask> expectedSubtasks = List.of(expectedSubtask);
        assertAll("subtask saved with errors",
                () -> assertNotEquals(ANOTHER_TEST_ID, subtaskId, "subtask saved with errors"),
                () -> assertThrows(TaskNotFoundException.class, () -> manager.getSubtaskById(ANOTHER_TEST_ID),
                        "subtask saved with errors"),
                () -> assertTaskEquals(expectedSubtask, savedSubtask, "subtask saved with errors"),
                () -> assertListEquals(expectedSubtasks, epicSubtasks, "subtask saved with errors"),
                () -> assertListEquals(expectedSubtasks, subtasks, "subtask saved with errors"),
                () -> assertListEquals(expectedSubtasks, prioritized, "subtask saved with errors")
        );
    }

    @Test
    public void shouldAssignSubtaskNewIdWhenCreateSubtaskAndIdNotNullAndIdExist() {
        final long anotherEpicId = manager.createEpic(testEpic);
        final Subtask anotherSubtask = fromTestSubtask().withId(null).withEpicId(anotherEpicId).build();
        final long oldSubtaskId = manager.createSubtask(anotherSubtask);
        final long epicId = manager.createEpic(modifiedEpic);
        final Subtask subtask = fromModifiedSubtask().withId(oldSubtaskId).withEpicId(epicId).build();

        final long newSubtaskId = manager.createSubtask(subtask);
        final Subtask oldSubtask = manager.getSubtaskById(oldSubtaskId);
        final Subtask newSubtask = manager.getSubtaskById(newSubtaskId);
        final List<Subtask> anotherEpicSubtasks = manager.getEpicSubtasks(anotherEpicId);
        final List<Subtask> epicSubtasks = manager.getEpicSubtasks(epicId);
        final List<Subtask> subtasks = manager.getSubtasks();
        final List<Task> prioritized = manager.getPrioritizedTasks();

        final Subtask expectedOldSubtask = fromTestSubtask().withId(oldSubtaskId).withEpicId(anotherEpicId).build();
        final Subtask expectedNewSubtask = fromModifiedSubtask().withId(newSubtaskId).withEpicId(epicId).build();
        final List<Subtask> expectedAnotherEpicSubtasks = List.of(expectedOldSubtask);
        final List<Subtask> expectedEpicSubtasks = List.of(expectedNewSubtask);
        final List<Subtask> expectedSubtasks = List.of(expectedOldSubtask, expectedNewSubtask);
        assertAll("subtask saved with errors",
                () -> assertNotEquals(oldSubtaskId, newSubtaskId, "subtask saved with errors"),
                () -> assertTaskEquals(expectedOldSubtask, oldSubtask, "subtask saved with errors"),
                () -> assertTaskEquals(expectedNewSubtask, newSubtask, "subtask saved with errors"),
                () -> assertListEquals(expectedAnotherEpicSubtasks, anotherEpicSubtasks, "subtask saved with errors"),
                () -> assertListEquals(expectedEpicSubtasks, epicSubtasks, "subtask saved with errors"),
                () -> assertListEquals(expectedSubtasks, subtasks, "subtask saved with errors"),
                () -> assertListEquals(expectedSubtasks, prioritized, "subtask saved with errors")
        );
    }

    @Test
    public void shouldSetEpicDurationNullWhenCreateEpicAndDurationNull() {
        final long epicId = manager.createEpic(testEpic);
        final Duration duration = manager.getEpicById(epicId).getDuration();

        assertNull(duration, "wrong epic duration");
    }

    @Test
    public void shouldSetEpicDurationNullWhenCreateEpicAndDurationNotNull() {
        final Epic epic = fromTestEpic().withId(null).withDuration(TEST_DURATION).build();

        final long epicId = manager.createEpic(epic);
        final Duration duration = manager.getEpicById(epicId).getDuration();

        assertNull(duration, "wrong epic duration");
    }

    @Test
    public void shouldSetEpicDurationNullWhenCreateSubtaskAndSubtaskDurationNull() {
        final long epicId = manager.createEpic(testEpic);
        final Subtask subtaskA = fromEmptySubtask().withId(null).withEpicId(epicId).withStatus(TaskStatus.NEW).build();
        final Subtask subtaskB = fromTestSubtask().withId(null).withEpicId(epicId).withDuration(null)
                .withStartTime(null).build();
        final Subtask subtaskC = fromModifiedSubtask().withId(null).withEpicId(epicId).withDuration(null)
                .withStartTime(null).build();

        manager.createSubtask(subtaskA);
        manager.createSubtask(subtaskB);
        manager.createSubtask(subtaskC);
        final Duration duration = manager.getEpicById(epicId).getDuration();

        assertNull(duration, "wrong epic duration");
    }

    @Test
    public void shouldSetEpicDurationNotNullWhenCreateSubtaskAndSubtaskDurationNotNull() {
        final long epicId = manager.createEpic(testEpic);
        final Subtask subtaskA = fromEmptySubtask().withId(null).withEpicId(epicId).withStatus(TaskStatus.NEW).build();
        final Subtask subtaskB = fromTestSubtask().withId(null).withEpicId(epicId).build();
        final Subtask subtaskC = fromModifiedSubtask().withId(null).withEpicId(epicId).build();

        manager.createSubtask(subtaskA);
        manager.createSubtask(subtaskB);
        manager.createSubtask(subtaskC);
        final Duration duration = manager.getEpicById(epicId).getDuration();

        assertEquals(TEST_DURATION.plus(MODIFIED_DURATION), duration, "wrong epic duration");
    }

    @Test
    public void shouldSetEpicDurationNullWhenUpdateSubtaskAndSubtaskDurationNull() {
        final long epicId = manager.createEpic(testEpic);
        final Subtask subtaskA = fromEmptySubtask().withId(null).withEpicId(epicId).withStatus(TaskStatus.NEW).build();
        final Subtask subtaskB = fromTestSubtask().withId(null).withEpicId(epicId).build();
        final Subtask subtaskC = fromModifiedSubtask().withId(null).withEpicId(epicId).build();
        manager.createSubtask(subtaskA);
        final long subtaskBId = manager.createSubtask(subtaskB);
        final long subtaskCId = manager.createSubtask(subtaskC);
        final Subtask updateB = fromTestSubtask().withId(subtaskBId).withDuration(null).withStartTime(null).build();
        final Subtask updateC = fromModifiedSubtask().withId(subtaskCId).withDuration(null).withStartTime(null).build();

        manager.updateSubtask(updateB);
        manager.updateSubtask(updateC);
        final Duration duration = manager.getEpicById(epicId).getDuration();

        assertNull(duration, "wrong epic duration");
    }

    @Test
    public void shouldSetEpicDurationNotNullWhenUpdateSubtaskAndSubtaskDurationNotNull() {
        final long epicId = manager.createEpic(testEpic);
        final Subtask subtaskA = fromEmptySubtask().withId(null).withEpicId(epicId).withStatus(TaskStatus.NEW).build();
        final Subtask subtaskB = fromTestSubtask().withId(null).withEpicId(epicId).withDuration(null)
                .withStartTime(null).build();
        final Subtask subtaskC = fromModifiedSubtask().withId(null).withEpicId(epicId).withDuration(null)
                .withStartTime(null).build();
        manager.createSubtask(subtaskA);
        final long subtaskBId = manager.createSubtask(subtaskB);
        final long subtaskCId = manager.createSubtask(subtaskC);
        final Subtask updateB = fromTestSubtask().withId(subtaskBId).build();
        final Subtask updateC = fromModifiedSubtask().withId(subtaskCId).build();

        manager.updateSubtask(updateB);
        manager.updateSubtask(updateC);
        final Duration duration = manager.getEpicById(epicId).getDuration();

        assertEquals(TEST_DURATION.plus(MODIFIED_DURATION), duration, "wrong epic duration");
    }

    @Test
    public void shouldSetEpicDurationNullWhenDeleteSubtaskAndNoSubtaskLeft() {
        final long epicId = manager.createEpic(testEpic);
        final Subtask subtaskA = fromEmptySubtask().withId(null).withEpicId(epicId).withStatus(TaskStatus.NEW).build();
        final Subtask subtaskB = fromTestSubtask().withId(null).withEpicId(epicId).build();
        final Subtask subtaskC = fromModifiedSubtask().withId(null).withEpicId(epicId).build();
        final long subtaskAId = manager.createSubtask(subtaskA);
        final long subtaskBId = manager.createSubtask(subtaskB);
        final long subtaskCId = manager.createSubtask(subtaskC);

        manager.deleteSubtask(subtaskAId);
        manager.deleteSubtask(subtaskBId);
        manager.deleteSubtask(subtaskCId);
        final Duration duration = manager.getEpicById(epicId).getDuration();

        assertNull(duration, "wrong epic duration");
    }

    @Test
    public void shouldSetEpicDurationNullWhenDeleteSubtaskAndSubtaskDurationNull() {
        final long epicId = manager.createEpic(testEpic);
        final Subtask subtaskA = fromEmptySubtask().withId(null).withEpicId(epicId).withStatus(TaskStatus.NEW).build();
        final Subtask subtaskB = fromTestSubtask().withId(null).withEpicId(epicId).build();
        final Subtask subtaskC = fromModifiedSubtask().withId(null).withEpicId(epicId).build();
        manager.createSubtask(subtaskA);
        final long subtaskBId = manager.createSubtask(subtaskB);
        final long subtaskCId = manager.createSubtask(subtaskC);

        manager.deleteSubtask(subtaskBId);
        manager.deleteSubtask(subtaskCId);
        final Duration duration = manager.getEpicById(epicId).getDuration();

        assertNull(duration, "wrong epic duration");
    }

    @Test
    public void shouldSetEpicDurationNotNullWhenDeleteSubtaskAndAggregateDurationNotChanged() {
        final long epicId = manager.createEpic(testEpic);
        final Subtask subtaskA = fromEmptySubtask().withId(null).withEpicId(epicId).withStatus(TaskStatus.NEW).build();
        final Subtask subtaskB = fromTestSubtask().withId(null).withEpicId(epicId).build();
        final Subtask subtaskC = fromModifiedSubtask().withId(null).withEpicId(epicId).build();
        final long subtaskAId = manager.createSubtask(subtaskA);
        manager.createSubtask(subtaskB);
        manager.createSubtask(subtaskC);

        manager.deleteSubtask(subtaskAId);
        final Duration duration = manager.getEpicById(epicId).getDuration();

        assertEquals(TEST_DURATION.plus(MODIFIED_DURATION), duration, "wrong epic duration");
    }

    @Test
    public void shouldSetEpicDurationNotNullWhenDeleteSubtaskAndAggregateDurationChanged() {
        final long epicId = manager.createEpic(testEpic);
        final Subtask subtaskA = fromEmptySubtask().withId(null).withEpicId(epicId).withStatus(TaskStatus.NEW).build();
        final Subtask subtaskB = fromTestSubtask().withId(null).withEpicId(epicId).build();
        final Subtask subtaskC = fromModifiedSubtask().withId(null).withEpicId(epicId).build();
        final long subtaskAId = manager.createSubtask(subtaskA);
        final long subtaskBId = manager.createSubtask(subtaskB);
        manager.createSubtask(subtaskC);

        manager.deleteSubtask(subtaskAId);
        manager.deleteSubtask(subtaskBId);
        final Duration duration = manager.getEpicById(epicId).getDuration();

        assertEquals(MODIFIED_DURATION, duration, "wrong epic duration");
    }

    @Test
    public void shouldRetainEpicDurationWhenUpdateEpicAndDurationNull() {
        final long epicId = manager.createEpic(testEpic);
        final Subtask subtaskA = fromEmptySubtask().withId(null).withEpicId(epicId).withStatus(TaskStatus.NEW).build();
        final Subtask subtaskB = fromTestSubtask().withId(null).withEpicId(epicId).withDuration(null)
                .withStartTime(null).build();
        final Subtask subtaskC = fromModifiedSubtask().withId(null).withEpicId(epicId).withDuration(null)
                .withStartTime(null).build();
        manager.createSubtask(subtaskA);
        manager.createSubtask(subtaskB);
        manager.createSubtask(subtaskC);
        final Epic update = fromModifiedEpic().withId(epicId).withDuration(TEST_DURATION).build();

        manager.updateEpic(update);
        final Duration duration = manager.getEpicById(epicId).getDuration();

        assertNull(duration, "wrong epic duration");
    }

    @Test
    public void shouldRetainEpicDurationWhenUpdateEpicAndDurationNotNull() {
        final long epicId = manager.createEpic(testEpic);
        final Subtask subtaskA = fromEmptySubtask().withId(null).withEpicId(epicId).withStatus(TaskStatus.NEW).build();
        final Subtask subtaskB = fromTestSubtask().withId(null).withEpicId(epicId).build();
        final Subtask subtaskC = fromModifiedSubtask().withId(null).withEpicId(epicId).build();
        manager.createSubtask(subtaskA);
        manager.createSubtask(subtaskB);
        manager.createSubtask(subtaskC);
        final Epic update = fromModifiedEpic().withId(epicId).build();

        manager.updateEpic(update);
        final Duration duration = manager.getEpicById(epicId).getDuration();

        assertEquals(TEST_DURATION.plus(MODIFIED_DURATION), duration, "wrong epic duration");
    }

    @Test
    public void shouldSetEpicDurationNullWhenDeleteSubtasksAndSubtaskDurationNull() {
        final long epicId = manager.createEpic(testEpic);
        final Subtask subtaskA = fromEmptySubtask().withId(null).withEpicId(epicId).withStatus(TaskStatus.NEW).build();
        final Subtask subtaskB = fromTestSubtask().withId(null).withEpicId(epicId).withDuration(null)
                .withStartTime(null).build();
        final Subtask subtaskC = fromModifiedSubtask().withId(null).withEpicId(epicId).withDuration(null)
                .withStartTime(null).build();
        manager.createSubtask(subtaskA);
        manager.createSubtask(subtaskB);
        manager.createSubtask(subtaskC);

        manager.deleteSubtasks();
        final Duration duration = manager.getEpicById(epicId).getDuration();

        assertNull(duration, "wrong epic duration");
    }

    @Test
    public void shouldSetEpicDurationNullWhenDeleteSubtasksAndSubtaskDurationNotNull() {
        final long epicId = manager.createEpic(testEpic);
        final Subtask subtaskA = fromEmptySubtask().withId(null).withEpicId(epicId).withStatus(TaskStatus.NEW).build();
        final Subtask subtaskB = fromTestSubtask().withId(null).withEpicId(epicId).build();
        final Subtask subtaskC = fromModifiedSubtask().withId(null).withEpicId(epicId).build();
        manager.createSubtask(subtaskA);
        manager.createSubtask(subtaskB);
        manager.createSubtask(subtaskC);

        manager.deleteSubtasks();
        final Duration duration = manager.getEpicById(epicId).getDuration();

        assertNull(duration, "wrong epic duration");
    }

    @Test
    public void shouldSetEpicStartTimeNullWhenCreateEpicAndStartTimeNull() {
        final long epicId = manager.createEpic(testEpic);
        final LocalDateTime startTime = manager.getEpicById(epicId).getStartTime();

        assertNull(startTime, "wrong epic start time");
    }

    @Test
    public void shouldSetEpicStartTimeNullWhenCreateEpicAndStartTimeNotNull() {
        final Epic epic = fromTestEpic().withId(null).withStartTime(TEST_START_TIME).build();

        final long epicId = manager.createEpic(epic);
        final LocalDateTime startTime = manager.getEpicById(epicId).getStartTime();

        assertNull(startTime, "wrong epic start time");
    }

    @Test
    public void shouldSetEpicStartTimeNullWhenCreateSubtaskAndSubtaskStartTimeNull() {
        final long epicId = manager.createEpic(testEpic);
        final Subtask subtaskA = fromEmptySubtask().withId(null).withEpicId(epicId).withStatus(TaskStatus.NEW).build();
        final Subtask subtaskB = fromTestSubtask().withId(null).withEpicId(epicId).withDuration(null)
                .withStartTime(null).build();
        final Subtask subtaskC = fromModifiedSubtask().withId(null).withEpicId(epicId).withDuration(null)
                .withStartTime(null).build();

        manager.createSubtask(subtaskA);
        manager.createSubtask(subtaskB);
        manager.createSubtask(subtaskC);
        final LocalDateTime startTime = manager.getEpicById(epicId).getStartTime();

        assertNull(startTime, "wrong epic start time");
    }

    @Test
    public void shouldSetEpicMinStartTimeWhenCreateSubtaskAndSubtaskStartTimeNotNull() {
        final long epicId = manager.createEpic(testEpic);
        final Subtask subtaskA = fromEmptySubtask().withId(null).withEpicId(epicId).withStatus(TaskStatus.NEW).build();
        final Subtask subtaskB = fromTestSubtask().withId(null).withEpicId(epicId).build();
        final Subtask subtaskC = fromModifiedSubtask().withId(null).withEpicId(epicId).build();

        manager.createSubtask(subtaskA);
        manager.createSubtask(subtaskB);
        manager.createSubtask(subtaskC);
        final LocalDateTime startTime = manager.getEpicById(epicId).getStartTime();

        assertEquals(TEST_START_TIME, startTime, "wrong epic start time");
    }

    @Test
    public void shouldSetEpicMinStartTimeWhenCreateSubtaskAndSubtaskStartTimeNotNullAndInOppositeOrder() {
        final long epicId = manager.createEpic(testEpic);
        final Subtask subtaskA = fromEmptySubtask().withId(null).withEpicId(epicId).withStatus(TaskStatus.NEW).build();
        final Subtask subtaskB = fromTestSubtask().withId(null).withEpicId(epicId).build();
        final Subtask subtaskC = fromModifiedSubtask().withId(null).withEpicId(epicId).build();

        manager.createSubtask(subtaskC);
        manager.createSubtask(subtaskB);
        manager.createSubtask(subtaskA);
        final LocalDateTime startTime = manager.getEpicById(epicId).getStartTime();

        assertEquals(TEST_START_TIME, startTime, "wrong epic start time");
    }

    @Test
    public void shouldSetEpicStartTimeNullWhenUpdateSubtaskAndSubtaskStartTimeNull() {
        final long epicId = manager.createEpic(testEpic);
        final Subtask subtaskA = fromEmptySubtask().withId(null).withEpicId(epicId).withStatus(TaskStatus.NEW).build();
        final Subtask subtaskB = fromTestSubtask().withId(null).withEpicId(epicId).build();
        final Subtask subtaskC = fromModifiedSubtask().withId(null).withEpicId(epicId).build();
        manager.createSubtask(subtaskA);
        final long subtaskBId = manager.createSubtask(subtaskB);
        final long subtaskCId = manager.createSubtask(subtaskC);
        final Subtask updateB = fromTestSubtask().withId(subtaskBId).withDuration(null).withStartTime(null).build();
        final Subtask updateC = fromModifiedSubtask().withId(subtaskCId).withDuration(null).withStartTime(null).build();

        manager.updateSubtask(updateB);
        manager.updateSubtask(updateC);
        final LocalDateTime startTime = manager.getEpicById(epicId).getStartTime();

        assertNull(startTime, "wrong epic start time");
    }

    @Test
    public void shouldSetEpicMinStartTimeWhenUpdateSubtaskAndSubtaskStartTimeNotNull() {
        final long epicId = manager.createEpic(testEpic);
        final Subtask subtaskA = fromEmptySubtask().withId(null).withEpicId(epicId).withStatus(TaskStatus.NEW).build();
        final Subtask subtaskB = fromTestSubtask().withId(null).withEpicId(epicId).withDuration(null)
                .withStartTime(null).build();
        final Subtask subtaskC = fromModifiedSubtask().withId(null).withEpicId(epicId).withDuration(null)
                .withStartTime(null).build();
        manager.createSubtask(subtaskA);
        final long subtaskBId = manager.createSubtask(subtaskB);
        final long subtaskCId = manager.createSubtask(subtaskC);
        final Subtask updateB = fromTestSubtask().withId(subtaskBId).build();
        final Subtask updateC = fromModifiedSubtask().withId(subtaskCId).build();

        manager.updateSubtask(updateB);
        manager.updateSubtask(updateC);
        final LocalDateTime startTime = manager.getEpicById(epicId).getStartTime();

        assertEquals(TEST_START_TIME, startTime, "wrong epic start time");
    }

    @Test
    public void shouldSetEpicMinStartTimeWhenUpdateSubtaskAndSubtaskStartTimeNotNullAndInOppositeOrder() {
        final long epicId = manager.createEpic(testEpic);
        final Subtask subtaskA = fromEmptySubtask().withId(null).withEpicId(epicId).withStatus(TaskStatus.NEW).build();
        final Subtask subtaskB = fromTestSubtask().withId(null).withEpicId(epicId).withDuration(null)
                .withStartTime(null).build();
        final Subtask subtaskC = fromModifiedSubtask().withId(null).withEpicId(epicId).withDuration(null)
                .withStartTime(null).build();
        manager.createSubtask(subtaskA);
        final long subtaskBId = manager.createSubtask(subtaskB);
        final long subtaskCId = manager.createSubtask(subtaskC);
        final Subtask updateB = fromTestSubtask().withId(subtaskBId).build();
        final Subtask updateC = fromModifiedSubtask().withId(subtaskCId).build();

        manager.updateSubtask(updateC);
        manager.updateSubtask(updateB);
        final LocalDateTime startTime = manager.getEpicById(epicId).getStartTime();

        assertEquals(TEST_START_TIME, startTime, "wrong epic start time");
    }

    @Test
    public void shouldSetEpicStartTimeNullWhenDeleteSubtaskAndNoSubtaskLeft() {
        final long epicId = manager.createEpic(testEpic);
        final Subtask subtaskA = fromEmptySubtask().withId(null).withEpicId(epicId).withStatus(TaskStatus.NEW).build();
        final Subtask subtaskB = fromTestSubtask().withId(null).withEpicId(epicId).build();
        final Subtask subtaskC = fromModifiedSubtask().withId(null).withEpicId(epicId).build();
        final long subtaskAId = manager.createSubtask(subtaskA);
        final long subtaskBId = manager.createSubtask(subtaskB);
        final long subtaskCId = manager.createSubtask(subtaskC);

        manager.deleteSubtask(subtaskAId);
        manager.deleteSubtask(subtaskBId);
        manager.deleteSubtask(subtaskCId);
        final LocalDateTime startTime = manager.getEpicById(epicId).getStartTime();

        assertNull(startTime, "wrong epic start time");
    }

    @Test
    public void shouldSetEpicStartTimeNullWhenDeleteSubtaskAndSubtaskStartTimeNull() {
        final long epicId = manager.createEpic(testEpic);
        final Subtask subtaskA = fromEmptySubtask().withId(null).withEpicId(epicId).withStatus(TaskStatus.NEW).build();
        final Subtask subtaskB = fromTestSubtask().withId(null).withEpicId(epicId).build();
        final Subtask subtaskC = fromModifiedSubtask().withId(null).withEpicId(epicId).build();
        manager.createSubtask(subtaskA);
        final long subtaskBId = manager.createSubtask(subtaskB);
        final long subtaskCId = manager.createSubtask(subtaskC);

        manager.deleteSubtask(subtaskBId);
        manager.deleteSubtask(subtaskCId);
        final LocalDateTime startTime = manager.getEpicById(epicId).getStartTime();

        assertNull(startTime, "wrong epic start time");
    }

    @Test
    public void shouldSetEpicMinStartTimeWhenDeleteSubtaskAndMinStartTimeNotChanged() {
        final long epicId = manager.createEpic(testEpic);
        final Subtask subtaskA = fromEmptySubtask().withId(null).withEpicId(epicId).withStatus(TaskStatus.NEW).build();
        final Subtask subtaskB = fromTestSubtask().withId(null).withEpicId(epicId).build();
        final Subtask subtaskC = fromModifiedSubtask().withId(null).withEpicId(epicId).build();
        final long subtaskAId = manager.createSubtask(subtaskA);
        manager.createSubtask(subtaskB);
        final long subtaskCId = manager.createSubtask(subtaskC);

        manager.deleteSubtask(subtaskAId);
        manager.deleteSubtask(subtaskCId);
        final LocalDateTime startTime = manager.getEpicById(epicId).getStartTime();

        assertEquals(TEST_START_TIME, startTime, "wrong epic start time");
    }

    @Test
    public void shouldSetEpicMinStartTimeWhenDeleteSubtaskAndMinStartTimeChanged() {
        final long epicId = manager.createEpic(testEpic);
        final Subtask subtaskA = fromEmptySubtask().withId(null).withEpicId(epicId).withStatus(TaskStatus.NEW).build();
        final Subtask subtaskB = fromTestSubtask().withId(null).withEpicId(epicId).build();
        final Subtask subtaskC = fromModifiedSubtask().withId(null).withEpicId(epicId).build();
        final long subtaskAId = manager.createSubtask(subtaskA);
        final long subtaskBId = manager.createSubtask(subtaskB);
        manager.createSubtask(subtaskC);

        manager.deleteSubtask(subtaskAId);
        manager.deleteSubtask(subtaskBId);
        final LocalDateTime startTime = manager.getEpicById(epicId).getStartTime();

        assertEquals(MODIFIED_START_TIME, startTime, "wrong epic start time");
    }

    @Test
    public void shouldRetainEpicStartTimeWhenUpdateEpicAndStartTimeNull() {
        final long epicId = manager.createEpic(testEpic);
        final Subtask subtaskA = fromEmptySubtask().withId(null).withEpicId(epicId).withStatus(TaskStatus.NEW).build();
        final Subtask subtaskB = fromTestSubtask().withId(null).withEpicId(epicId).withDuration(null)
                .withStartTime(null).build();
        final Subtask subtaskC = fromModifiedSubtask().withId(null).withEpicId(epicId).withDuration(null)
                .withStartTime(null).build();
        manager.createSubtask(subtaskA);
        manager.createSubtask(subtaskB);
        manager.createSubtask(subtaskC);
        final Epic update = fromModifiedEpic().withId(epicId).withStartTime(TEST_START_TIME).build();

        manager.updateEpic(update);
        final LocalDateTime startTime = manager.getEpicById(epicId).getStartTime();

        assertNull(startTime, "wrong epic start time");
    }

    @Test
    public void shouldRetainEpicStartTimeWhenUpdateEpicAndStartTimeNotNull() {
        final long epicId = manager.createEpic(testEpic);
        final Subtask subtaskA = fromEmptySubtask().withId(null).withEpicId(epicId).withStatus(TaskStatus.NEW).build();
        final Subtask subtaskB = fromTestSubtask().withId(null).withEpicId(epicId).build();
        final Subtask subtaskC = fromModifiedSubtask().withId(null).withEpicId(epicId).build();
        manager.createSubtask(subtaskA);
        manager.createSubtask(subtaskB);
        manager.createSubtask(subtaskC);
        final Epic update = fromModifiedEpic().withId(epicId).build();

        manager.updateEpic(update);
        final LocalDateTime startTime = manager.getEpicById(epicId).getStartTime();

        assertEquals(TEST_START_TIME, startTime, "wrong epic start time");
    }

    @Test
    public void shouldSetEpicStartTimeNullWhenDeleteSubtasksAndSubtaskStartTimeNull() {
        final long epicId = manager.createEpic(testEpic);
        final Subtask subtaskA = fromEmptySubtask().withId(null).withEpicId(epicId).withStatus(TaskStatus.NEW).build();
        final Subtask subtaskB = fromTestSubtask().withId(null).withEpicId(epicId).withDuration(null)
                .withStartTime(null).build();
        final Subtask subtaskC = fromModifiedSubtask().withId(null).withEpicId(epicId).withDuration(null)
                .withStartTime(null).build();
        manager.createSubtask(subtaskA);
        manager.createSubtask(subtaskB);
        manager.createSubtask(subtaskC);

        manager.deleteSubtasks();
        final LocalDateTime startTime = manager.getEpicById(epicId).getStartTime();

        assertNull(startTime, "wrong epic start time");
    }

    @Test
    public void shouldSetEpicStartTimeNullWhenDeleteSubtasksAndSubtaskStartTimeNotNull() {
        final long epicId = manager.createEpic(testEpic);
        final Subtask subtaskA = fromEmptySubtask().withId(null).withEpicId(epicId).withStatus(TaskStatus.NEW).build();
        final Subtask subtaskB = fromTestSubtask().withId(null).withEpicId(epicId).build();
        final Subtask subtaskC = fromModifiedSubtask().withId(null).withEpicId(epicId).build();
        manager.createSubtask(subtaskA);
        manager.createSubtask(subtaskB);
        manager.createSubtask(subtaskC);

        manager.deleteSubtasks();
        final LocalDateTime startTime = manager.getEpicById(epicId).getStartTime();

        assertNull(startTime, "wrong epic start time");
    }

    @Test
    public void shouldSetEpicEndTimeNullWhenCreateEpicAndEndTimeNull() {
        final long epicId = manager.createEpic(testEpic);
        final LocalDateTime endTime = manager.getEpicById(epicId).getEndTime();

        assertNull(endTime, "wrong epic end time");
    }

    @Test
    public void shouldSetEpicEndTimeNullWhenCreateEpicAndEndTimeNotNull() {
        final Epic epic = fromTestEpic().withId(null).withEndTime(TEST_END_TIME).build();

        final long epicId = manager.createEpic(epic);
        final LocalDateTime endTime = manager.getEpicById(epicId).getEndTime();

        assertNull(endTime, "wrong epic end time");
    }

    @Test
    public void shouldSetEpicEndTimeNullWhenCreateSubtaskAndSubtaskEndTimeNull() {
        final long epicId = manager.createEpic(testEpic);
        final Subtask subtaskA = fromEmptySubtask().withId(null).withEpicId(epicId).withStatus(TaskStatus.NEW).build();
        final Subtask subtaskB = fromTestSubtask().withId(null).withEpicId(epicId).withDuration(null)
                .withStartTime(null).build();
        final Subtask subtaskC = fromModifiedSubtask().withId(null).withEpicId(epicId).withDuration(null)
                .withStartTime(null).build();

        manager.createSubtask(subtaskA);
        manager.createSubtask(subtaskB);
        manager.createSubtask(subtaskC);
        final LocalDateTime endTime = manager.getEpicById(epicId).getEndTime();

        assertNull(endTime, "wrong epic end time");
    }

    @Test
    public void shouldSetEpicMaxEndTimeWhenCreateSubtaskAndSubtaskEndTimeNotNull() {
        final long epicId = manager.createEpic(testEpic);
        final Subtask subtaskA = fromEmptySubtask().withId(null).withEpicId(epicId).withStatus(TaskStatus.NEW).build();
        final Subtask subtaskB = fromTestSubtask().withId(null).withEpicId(epicId).build();
        final Subtask subtaskC = fromModifiedSubtask().withId(null).withEpicId(epicId).build();

        manager.createSubtask(subtaskA);
        manager.createSubtask(subtaskB);
        manager.createSubtask(subtaskC);
        final LocalDateTime endTime = manager.getEpicById(epicId).getEndTime();

        assertEquals(MODIFIED_END_TIME, endTime, "wrong epic end time");
    }

    @Test
    public void shouldSetEpicMaxEndTimeWhenCreateSubtaskAndSubtaskEndTimeNotNullAndInOppositeOrder() {
        final long epicId = manager.createEpic(testEpic);
        final Subtask subtaskA = fromEmptySubtask().withId(null).withEpicId(epicId).withStatus(TaskStatus.NEW).build();
        final Subtask subtaskB = fromTestSubtask().withId(null).withEpicId(epicId).build();
        final Subtask subtaskC = fromModifiedSubtask().withId(null).withEpicId(epicId).build();

        manager.createSubtask(subtaskC);
        manager.createSubtask(subtaskB);
        manager.createSubtask(subtaskA);
        final LocalDateTime endTime = manager.getEpicById(epicId).getEndTime();

        assertEquals(MODIFIED_END_TIME, endTime, "wrong epic end time");
    }

    @Test
    public void shouldSetEpicEndTimeNullWhenUpdateSubtaskAndSubtaskEndTimeNull() {
        final long epicId = manager.createEpic(testEpic);
        final Subtask subtaskA = fromEmptySubtask().withId(null).withEpicId(epicId).withStatus(TaskStatus.NEW).build();
        final Subtask subtaskB = fromTestSubtask().withId(null).withEpicId(epicId).build();
        final Subtask subtaskC = fromModifiedSubtask().withId(null).withEpicId(epicId).build();
        manager.createSubtask(subtaskA);
        final long subtaskBId = manager.createSubtask(subtaskB);
        final long subtaskCId = manager.createSubtask(subtaskC);
        final Subtask updateB = fromTestSubtask().withId(subtaskBId).withDuration(null).withStartTime(null).build();
        final Subtask updateC = fromModifiedSubtask().withId(subtaskCId).withDuration(null).withStartTime(null).build();

        manager.updateSubtask(updateB);
        manager.updateSubtask(updateC);
        final LocalDateTime endTime = manager.getEpicById(epicId).getEndTime();

        assertNull(endTime, "wrong epic end time");
    }

    @Test
    public void shouldSetEpicMaxEndTimeWhenUpdateSubtaskAndSubtaskEndTimeNotNull() {
        final long epicId = manager.createEpic(testEpic);
        final Subtask subtaskA = fromEmptySubtask().withId(null).withEpicId(epicId).withStatus(TaskStatus.NEW).build();
        final Subtask subtaskB = fromTestSubtask().withId(null).withEpicId(epicId).withDuration(null)
                .withStartTime(null).build();
        final Subtask subtaskC = fromModifiedSubtask().withId(null).withEpicId(epicId).withDuration(null)
                .withStartTime(null).build();
        manager.createSubtask(subtaskA);
        final long subtaskBId = manager.createSubtask(subtaskB);
        final long subtaskCId = manager.createSubtask(subtaskC);
        final Subtask updateB = fromTestSubtask().withId(subtaskBId).build();
        final Subtask updateC = fromModifiedSubtask().withId(subtaskCId).build();

        manager.updateSubtask(updateB);
        manager.updateSubtask(updateC);
        final LocalDateTime endTime = manager.getEpicById(epicId).getEndTime();

        assertEquals(MODIFIED_END_TIME, endTime, "wrong epic end time");
    }

    @Test
    public void shouldSetEpicMaxEndTimeWhenUpdateSubtaskAndSubtaskEndTimeNotNullAndInOppositeOrder() {
        final long epicId = manager.createEpic(testEpic);
        final Subtask subtaskA = fromEmptySubtask().withId(null).withEpicId(epicId).withStatus(TaskStatus.NEW).build();
        final Subtask subtaskB = fromTestSubtask().withId(null).withEpicId(epicId).withDuration(null)
                .withStartTime(null).build();
        final Subtask subtaskC = fromModifiedSubtask().withId(null).withEpicId(epicId).withDuration(null)
                .withStartTime(null).build();
        manager.createSubtask(subtaskA);
        final long subtaskBId = manager.createSubtask(subtaskB);
        final long subtaskCId = manager.createSubtask(subtaskC);
        final Subtask updateB = fromTestSubtask().withId(subtaskBId).build();
        final Subtask updateC = fromModifiedSubtask().withId(subtaskCId).build();

        manager.updateSubtask(updateC);
        manager.updateSubtask(updateB);
        final LocalDateTime endTime = manager.getEpicById(epicId).getEndTime();

        assertEquals(MODIFIED_END_TIME, endTime, "wrong epic end time");
    }

    @Test
    public void shouldSetEpicEndTimeNullWhenDeleteSubtaskAndNoSubtaskLeft() {
        final long epicId = manager.createEpic(testEpic);
        final Subtask subtaskA = fromEmptySubtask().withId(null).withEpicId(epicId).withStatus(TaskStatus.NEW).build();
        final Subtask subtaskB = fromTestSubtask().withId(null).withEpicId(epicId).build();
        final Subtask subtaskC = fromModifiedSubtask().withId(null).withEpicId(epicId).build();
        final long subtaskAId = manager.createSubtask(subtaskA);
        final long subtaskBId = manager.createSubtask(subtaskB);
        final long subtaskCId = manager.createSubtask(subtaskC);

        manager.deleteSubtask(subtaskAId);
        manager.deleteSubtask(subtaskBId);
        manager.deleteSubtask(subtaskCId);
        final LocalDateTime endTime = manager.getEpicById(epicId).getEndTime();

        assertNull(endTime, "wrong epic end time");
    }

    @Test
    public void shouldSetEpicEndTimeNullWhenDeleteSubtaskAndSubtaskEndTimeNull() {
        final long epicId = manager.createEpic(testEpic);
        final Subtask subtaskA = fromEmptySubtask().withId(null).withEpicId(epicId).withStatus(TaskStatus.NEW).build();
        final Subtask subtaskB = fromTestSubtask().withId(null).withEpicId(epicId).build();
        final Subtask subtaskC = fromModifiedSubtask().withId(null).withEpicId(epicId).build();
        manager.createSubtask(subtaskA);
        final long subtaskBId = manager.createSubtask(subtaskB);
        final long subtaskCId = manager.createSubtask(subtaskC);

        manager.deleteSubtask(subtaskBId);
        manager.deleteSubtask(subtaskCId);
        final LocalDateTime endTime = manager.getEpicById(epicId).getEndTime();

        assertNull(endTime, "wrong epic end time");
    }

    @Test
    public void shouldSetEpicMaxEndTimeWhenDeleteSubtaskAndMaxEndTimeNotChanged() {
        final long epicId = manager.createEpic(testEpic);
        final Subtask subtaskA = fromEmptySubtask().withId(null).withEpicId(epicId).withStatus(TaskStatus.NEW).build();
        final Subtask subtaskB = fromTestSubtask().withId(null).withEpicId(epicId).build();
        final Subtask subtaskC = fromModifiedSubtask().withId(null).withEpicId(epicId).build();
        final long subtaskAId = manager.createSubtask(subtaskA);
        final long subtaskBId = manager.createSubtask(subtaskB);
        manager.createSubtask(subtaskC);

        manager.deleteSubtask(subtaskAId);
        manager.deleteSubtask(subtaskBId);
        final LocalDateTime endTime = manager.getEpicById(epicId).getEndTime();

        assertEquals(MODIFIED_END_TIME, endTime, "wrong epic end time");
    }

    @Test
    public void shouldSetEpicMaxEndTimeWhenDeleteSubtaskAndMaxEndTimeChanged() {
        final long epicId = manager.createEpic(testEpic);
        final Subtask subtaskA = fromEmptySubtask().withId(null).withEpicId(epicId).withStatus(TaskStatus.NEW).build();
        final Subtask subtaskB = fromTestSubtask().withId(null).withEpicId(epicId).build();
        final Subtask subtaskC = fromModifiedSubtask().withId(null).withEpicId(epicId).build();
        final long subtaskAId = manager.createSubtask(subtaskA);
        manager.createSubtask(subtaskB);
        final long subtaskCId = manager.createSubtask(subtaskC);

        manager.deleteSubtask(subtaskAId);
        manager.deleteSubtask(subtaskCId);
        final LocalDateTime endTime = manager.getEpicById(epicId).getEndTime();

        assertEquals(TEST_END_TIME, endTime, "wrong epic end time");
    }

    @Test
    public void shouldRetainEpicEndTimeWhenUpdateEpicAndEndTimeNull() {
        final long epicId = manager.createEpic(testEpic);
        final Subtask subtaskA = fromEmptySubtask().withId(null).withEpicId(epicId).withStatus(TaskStatus.NEW).build();
        final Subtask subtaskB = fromTestSubtask().withId(null).withEpicId(epicId).withDuration(null)
                .withStartTime(null).build();
        final Subtask subtaskC = fromModifiedSubtask().withId(null).withEpicId(epicId).withDuration(null)
                .withStartTime(null).build();
        manager.createSubtask(subtaskA);
        manager.createSubtask(subtaskB);
        manager.createSubtask(subtaskC);
        final Epic update = fromModifiedEpic().withId(epicId).withEndTime(MODIFIED_END_TIME).build();

        manager.updateEpic(update);
        final LocalDateTime endTime = manager.getEpicById(epicId).getEndTime();

        assertNull(endTime, "wrong epic end time");
    }

    @Test
    public void shouldRetainEpicEndTimeWhenUpdateEpicAndEndTimeNotNull() {
        final long epicId = manager.createEpic(testEpic);
        final Subtask subtaskA = fromEmptySubtask().withId(null).withEpicId(epicId).withStatus(TaskStatus.NEW).build();
        final Subtask subtaskB = fromTestSubtask().withId(null).withEpicId(epicId).build();
        final Subtask subtaskC = fromModifiedSubtask().withId(null).withEpicId(epicId).build();
        manager.createSubtask(subtaskA);
        manager.createSubtask(subtaskB);
        manager.createSubtask(subtaskC);
        final Epic update = fromModifiedEpic().withId(epicId).build();

        manager.updateEpic(update);
        final LocalDateTime endTime = manager.getEpicById(epicId).getEndTime();

        assertEquals(MODIFIED_END_TIME, endTime, "wrong epic end time");
    }

    @Test
    public void shouldSetEpicEndTimeNullWhenDeleteSubtasksAndSubtaskEndTimeNull() {
        final long epicId = manager.createEpic(testEpic);
        final Subtask subtaskA = fromEmptySubtask().withId(null).withEpicId(epicId).withStatus(TaskStatus.NEW).build();
        final Subtask subtaskB = fromTestSubtask().withId(null).withEpicId(epicId).withDuration(null)
                .withStartTime(null).build();
        final Subtask subtaskC = fromModifiedSubtask().withId(null).withEpicId(epicId).withDuration(null)
                .withStartTime(null).build();
        manager.createSubtask(subtaskA);
        manager.createSubtask(subtaskB);
        manager.createSubtask(subtaskC);

        manager.deleteSubtasks();
        final LocalDateTime endTime = manager.getEpicById(epicId).getEndTime();

        assertNull(endTime, "wrong epic end time");
    }

    @Test
    public void shouldSetEpicEndTimeNullWhenDeleteSubtasksAndSubtaskEndTimeNotNull() {
        final long epicId = manager.createEpic(testEpic);
        final Subtask subtaskA = fromEmptySubtask().withId(null).withEpicId(epicId).withStatus(TaskStatus.NEW).build();
        final Subtask subtaskB = fromTestSubtask().withId(null).withEpicId(epicId).build();
        final Subtask subtaskC = fromModifiedSubtask().withId(null).withEpicId(epicId).build();
        manager.createSubtask(subtaskA);
        manager.createSubtask(subtaskB);
        manager.createSubtask(subtaskC);

        manager.deleteSubtasks();
        final LocalDateTime endTime = manager.getEpicById(epicId).getEndTime();

        assertNull(endTime, "wrong epic end time");
    }

    @ParameterizedTest
    @NullSource
    @EnumSource(TaskStatus.class)
    public void shouldSetEpicStatusNewWhenCreateEpic(TaskStatus status) {
        final Epic epic = fromTestEpic().withId(null).withStatus(status).build();

        final long epicId = manager.createEpic(epic);
        final TaskStatus actualStatus = manager.getEpicById(epicId).getStatus();

        assertEquals(TaskStatus.NEW, actualStatus, "wrong epic status");
    }

    @ParameterizedTest
    @CsvSource({"NEW,NEW", "NEW,IN_PROGRESS", "NEW,DONE", "IN_PROGRESS,NEW", "IN_PROGRESS,IN_PROGRESS",
            "IN_PROGRESS,DONE", "DONE,NEW", "DONE,IN_PROGRESS", "DONE,DONE"})
    public void shouldSetEpicStatusWhenCreateSubtask(TaskStatus statusA, TaskStatus statusB) {
        final long epicId = manager.createEpic(testEpic);
        final Subtask subtaskA = fromTestSubtask().withId(null).withEpicId(epicId).withStatus(statusA).build();
        final Subtask subtaskB = fromModifiedSubtask().withId(null).withEpicId(epicId).withStatus(statusB).build();
        final TaskStatus expectedStatus = statusA == statusB ? statusA : TaskStatus.IN_PROGRESS;

        manager.createSubtask(subtaskA);
        manager.createSubtask(subtaskB);
        final TaskStatus actualStatus = manager.getEpicById(epicId).getStatus();

        assertEquals(expectedStatus, actualStatus, "wrong epic status");
    }

    @Test
    public void shouldSetEpicStatusInProgressWhenCreateSubtaskAndAllStatuses() {
        final long epicId = manager.createEpic(testEpic);
        final Subtask subtaskA = fromEmptySubtask().withEpicId(epicId).withStatus(TaskStatus.NEW).build();
        final Subtask subtaskB = fromTestSubtask().withId(null).withEpicId(epicId).build();
        final Subtask subtaskC = fromModifiedSubtask().withId(null).withEpicId(epicId).build();

        manager.createSubtask(subtaskA);
        manager.createSubtask(subtaskB);
        manager.createSubtask(subtaskC);
        final TaskStatus actualStatus = manager.getEpicById(epicId).getStatus();

        assertEquals(TaskStatus.IN_PROGRESS, actualStatus, "wrong epic status");
    }

    @ParameterizedTest
    @CsvSource({"NEW,NEW", "NEW,IN_PROGRESS", "NEW,DONE", "IN_PROGRESS,NEW", "IN_PROGRESS,IN_PROGRESS",
            "IN_PROGRESS,DONE", "DONE,NEW", "DONE,IN_PROGRESS", "DONE,DONE"})
    public void shouldSetEpicStatusWhenUpdateSubtask(TaskStatus statusA, TaskStatus statusB) {
        final long epicId = manager.createEpic(testEpic);
        final Subtask subtaskA = fromTestSubtask().withId(null).withEpicId(epicId).withStatus(statusA).build();
        final Subtask subtaskB = fromModifiedSubtask().withId(null).withEpicId(epicId).withStatus(statusA).build();
        manager.createSubtask(subtaskA);
        final long subtaskBId = manager.createSubtask(subtaskB);
        final Subtask update = fromModifiedSubtask().withId(subtaskBId).withEpicId(epicId).withStatus(statusB).build();
        final TaskStatus expectedStatus = statusA == statusB ? statusA : TaskStatus.IN_PROGRESS;

        manager.updateSubtask(update);
        final TaskStatus actualStatus = manager.getEpicById(epicId).getStatus();

        assertEquals(expectedStatus, actualStatus, "wrong epic status");
    }

    @Test
    public void shouldSetEpicStatusInProgressWhenUpdateSubtaskAndAllStatuses() {
        final long epicId = manager.createEpic(testEpic);
        final Subtask subtaskA = fromEmptySubtask().withEpicId(epicId).withStatus(TaskStatus.NEW).build();
        final Subtask subtaskB = fromTestSubtask().withId(null).withEpicId(epicId).withStatus(TaskStatus.NEW).build();
        final Subtask subtaskC = fromModifiedSubtask().withId(null).withEpicId(epicId).withStatus(TaskStatus.NEW)
                .build();
        manager.createSubtask(subtaskA);
        final long subtaskBId = manager.createSubtask(subtaskB);
        final long subtaskCId = manager.createSubtask(subtaskC);
        final Subtask updateB = fromTestSubtask().withId(subtaskBId).build();
        final Subtask updateC = fromModifiedSubtask().withId(subtaskCId).build();

        manager.updateSubtask(updateB);
        manager.updateSubtask(updateC);
        final TaskStatus actualStatus = manager.getEpicById(epicId).getStatus();

        assertEquals(TaskStatus.IN_PROGRESS, actualStatus, "wrong epic status");
    }

    @ParameterizedTest
    @CsvSource({"NEW,NEW", "NEW,IN_PROGRESS", "NEW,DONE", "IN_PROGRESS,NEW", "IN_PROGRESS,IN_PROGRESS",
            "IN_PROGRESS,DONE", "DONE,NEW", "DONE,IN_PROGRESS", "DONE,DONE"})
    public void shouldSetEpicStatusWhenDeleteSubtask(TaskStatus statusA, TaskStatus statusB) {
        final long epicId = manager.createEpic(testEpic);
        final Subtask subtaskA = fromEmptySubtask().withEpicId(epicId).withStatus(TaskStatus.NEW).build();
        final Subtask subtaskB = fromTestSubtask().withId(null).withEpicId(epicId).withStatus(statusA).build();
        final Subtask subtaskC = fromModifiedSubtask().withId(null).withEpicId(epicId).withStatus(statusB).build();
        final long subtaskAId = manager.createSubtask(subtaskA);
        manager.createSubtask(subtaskB);
        manager.createSubtask(subtaskC);
        final TaskStatus expectedStatus = statusA == statusB ? statusA : TaskStatus.IN_PROGRESS;

        manager.deleteSubtask(subtaskAId);
        final TaskStatus actualStatus = manager.getEpicById(epicId).getStatus();

        assertEquals(expectedStatus, actualStatus, "wrong epic status");
    }

    @Test
    public void shouldSetEpicStatusInProgressWhenDeleteSubtaskAndAllStatuses() {
        final long epicId = manager.createEpic(testEpic);
        final Subtask subtaskA = fromEmptySubtask().withEpicId(epicId).withStatus(TaskStatus.NEW).build();
        final Subtask subtaskB = fromTestSubtask().withId(null).withEpicId(epicId).build();
        final Subtask subtaskC = fromModifiedSubtask().withId(null).withEpicId(epicId).build();
        final Subtask subtaskD = fromEmptySubtask().withEpicId(epicId).withStatus(TaskStatus.NEW).build();
        manager.createSubtask(subtaskA);
        manager.createSubtask(subtaskB);
        manager.createSubtask(subtaskC);
        final long subtaskDId = manager.createSubtask(subtaskD);

        manager.deleteSubtask(subtaskDId);
        final TaskStatus actualStatus = manager.getEpicById(epicId).getStatus();

        assertEquals(TaskStatus.IN_PROGRESS, actualStatus, "wrong epic status");
    }

    @ParameterizedTest
    @EnumSource(TaskStatus.class)
    public void shouldRetainEpicStatusWhenUpdateEpicAndStatusNull(TaskStatus status) {
        final long epicId = manager.createEpic(testEpic);
        final Subtask subtask = fromTestSubtask().withId(null).withEpicId(epicId).withStatus(status).build();
        manager.createSubtask(subtask);
        final Epic update = fromModifiedEpic().withId(epicId).build();

        manager.updateEpic(update);
        final TaskStatus actualStatus = manager.getEpicById(epicId).getStatus();

        assertEquals(status, actualStatus, "wrong epic status");
    }

    @ParameterizedTest
    @CsvSource({"NEW,NEW", "NEW,IN_PROGRESS", "NEW,DONE", "IN_PROGRESS,NEW", "IN_PROGRESS,IN_PROGRESS",
            "IN_PROGRESS,DONE", "DONE,NEW", "DONE,IN_PROGRESS", "DONE,DONE"})
    public void shouldRetainEpicStatusWhenUpdateEpicAndStatusNotNull(TaskStatus statusA, TaskStatus statusB) {
        final long epicId = manager.createEpic(testEpic);
        final Subtask subtask = fromTestSubtask().withId(null).withEpicId(epicId).withStatus(statusA).build();
        manager.createSubtask(subtask);
        final Epic update = fromModifiedEpic().withId(epicId).withStatus(statusB).build();

        manager.updateEpic(update);
        final TaskStatus actualStatus = manager.getEpicById(epicId).getStatus();

        assertEquals(statusA, actualStatus, "wrong epic status");
    }

    @ParameterizedTest
    @EnumSource(TaskStatus.class)
    public void shouldSetEpicStatusNewWhenDeleteSubtasks(TaskStatus status) {
        final long epicId = manager.createEpic(testEpic);
        final Subtask subtask = fromTestSubtask().withId(null).withEpicId(epicId).withStatus(status).build();
        manager.createSubtask(subtask);

        manager.deleteSubtasks();
        final TaskStatus actualStatus = manager.getEpicById(epicId).getStatus();

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
        final long epicId = manager.createEpic(testEpic);
        final long anotherEpicId = manager.createEpic(modifiedEpic);
        final Subtask subtaskA = fromEmptySubtask().withEpicId(epicId).withStatus(TaskStatus.NEW).build();
        final Subtask subtaskB = fromTestSubtask().withId(null).withEpicId(epicId).build();
        final Subtask subtaskC = fromModifiedSubtask().withId(null).withEpicId(epicId).build();
        final long subtaskAId = manager.createSubtask(subtaskA);
        final long subtaskBId = manager.createSubtask(subtaskB);
        final long subtaskCId = manager.createSubtask(subtaskC);
        final Subtask subtaskD = fromEmptySubtask().withEpicId(anotherEpicId).withStatus(TaskStatus.NEW).build();
        manager.createSubtask(subtaskD);
        manager.deleteSubtask(subtaskBId);
        final Subtask expectedSubtaskA = fromEmptySubtask().withId(subtaskAId).withEpicId(epicId)
                .withStatus(TaskStatus.NEW).build();
        final Subtask expectedSubtaskB = fromModifiedSubtask().withId(subtaskCId).withEpicId(epicId).build();
        final List<Subtask> expectedSubtasks = List.of(expectedSubtaskA, expectedSubtaskB);

        final List<Subtask> actualSubtasks = manager.getEpicSubtasks(epicId);

        assertListEquals(expectedSubtasks, actualSubtasks, "incorrect list of subtasks returned");
    }

    @Test
    public void shouldGetTasksWhenSeveralTasks() {
        final long taskAId = manager.createTask(emptyTask);
        final long taskBId = manager.createTask(testTask);
        final long taskCId = manager.createTask(modifiedTask);
        final Task taskA = fromEmptyTask().withId(taskAId).withStatus(TaskStatus.NEW).build();
        final Task taskC = fromModifiedTask().withId(taskCId).build();
        final List<Task> expectedTasks = List.of(taskA, taskC);
        manager.deleteTask(taskBId);

        final List<Task> actualTasks = manager.getTasks();

        assertListEquals(expectedTasks, actualTasks, "incorrect list of tasks returned");
    }

    @Test
    public void shouldDeleteTasks() {
        final long taskAId = manager.createTask(testTask);
        final long taskBId = manager.createTask(modifiedTask);

        manager.deleteTasks();
        final List<Task> tasks = manager.getTasks();
        final List<Task> prioritized = manager.getPrioritizedTasks();

        assertAll("tasks removed with errors",
                () -> assertThrows(TaskNotFoundException.class, () -> manager.getTaskById(taskAId),
                        "tasks removed with errors"),
                () -> assertThrows(TaskNotFoundException.class, () -> manager.getTaskById(taskBId),
                        "tasks removed with errors"),
                () -> assertTrue(tasks.isEmpty(), "tasks removed with errors"),
                () -> assertTrue(prioritized.isEmpty(), "tasks removed with errors")
        );
    }

    @Test
    public void shouldGetEpicsWhenSeveralEpics() {
        final long epicAId = manager.createEpic(emptyEpic);
        final long epicBId = manager.createEpic(testEpic);
        final long epicCId = manager.createEpic(modifiedEpic);
        final Epic epicA = fromEmptyEpic().withId(epicAId).withStatus(TaskStatus.NEW).build();
        final Epic epicC = fromModifiedEpic().withId(epicCId).withStatus(TaskStatus.NEW).build();
        final List<Epic> expectedEpics = List.of(epicA, epicC);
        manager.deleteEpic(epicBId);

        final List<Epic> actualEpics = manager.getEpics();

        assertListEquals(expectedEpics, actualEpics, "incorrect list of epics returned");
    }

    @Test
    public void shouldDeleteEpics() {
        final long epicAId = manager.createEpic(testEpic);
        final long epicBId = manager.createEpic(modifiedEpic);

        manager.deleteEpics();
        final List<Epic> epics = manager.getEpics();

        assertAll("epics removed with errors",
                () -> assertThrows(TaskNotFoundException.class, () -> manager.getEpicById(epicAId),
                        "epics removed with errors"),
                () -> assertThrows(TaskNotFoundException.class, () -> manager.getEpicById(epicBId),
                        "epics removed with errors"),
                () -> assertTrue(epics.isEmpty(), "epics removed with errors")
        );
    }

    @Test
    public void shouldGetSubtasksWhenSeveralSubtasks() {
        final long epicId = manager.createEpic(testEpic);
        final long anotherEpicId = manager.createEpic(modifiedEpic);
        final Subtask subtaskA = fromEmptySubtask().withEpicId(epicId).withStatus(TaskStatus.NEW).build();
        final Subtask subtaskB = fromTestSubtask().withId(null).withEpicId(epicId).build();
        final Subtask subtaskC = fromModifiedSubtask().withId(null).withEpicId(epicId).build();
        final Subtask subtaskD = fromEmptySubtask().withEpicId(anotherEpicId).withStatus(TaskStatus.NEW).build();
        final long subtaskAId = manager.createSubtask(subtaskA);
        final long subtaskBId = manager.createSubtask(subtaskB);
        final long subtaskCId = manager.createSubtask(subtaskC);
        final long subtaskDId = manager.createSubtask(subtaskD);
        manager.deleteSubtask(subtaskBId);
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
    public void shouldDeleteSubtasks() {
        final long epicId = manager.createEpic(testEpic);
        final long anotherEpicId = manager.createEpic(modifiedEpic);
        final Subtask subtaskA = fromTestSubtask().withId(null).withEpicId(epicId).build();
        final Subtask subtaskB = fromModifiedSubtask().withId(null).withEpicId(anotherEpicId).build();
        final long subtaskAId = manager.createSubtask(subtaskA);
        final long subtaskBId = manager.createSubtask(subtaskB);

        manager.deleteSubtasks();
        final List<Subtask> epicSubtasks = manager.getEpicSubtasks(epicId);
        final List<Subtask> anotherEpicSubtasks = manager.getEpicSubtasks(anotherEpicId);
        final List<Subtask> subtasks = manager.getSubtasks();
        final List<Task> prioritized = manager.getPrioritizedTasks();

        assertAll("subtasks removed with errors",
                () -> assertThrows(TaskNotFoundException.class, () -> manager.getSubtaskById(subtaskAId),
                        "subtasks removed with errors"),
                () -> assertThrows(TaskNotFoundException.class, () -> manager.getSubtaskById(subtaskBId),
                        "subtasks removed with errors"),
                () -> assertTrue(epicSubtasks.isEmpty(), "subtasks removed with errors"),
                () -> assertTrue(anotherEpicSubtasks.isEmpty(), "subtasks removed with errors"),
                () -> assertTrue(subtasks.isEmpty(), "subtasks removed with errors"),
                () -> assertTrue(prioritized.isEmpty(), "subtasks removed with errors")
        );
    }

    @Test
    public void shouldDeleteSubtasksWhenDeleteEpics() {
        final long epicId = manager.createEpic(testEpic);
        final long anotherEpicId = manager.createEpic(modifiedEpic);
        final Subtask subtaskA = fromTestSubtask().withId(null).withEpicId(epicId).build();
        final Subtask subtaskB = fromModifiedSubtask().withId(null).withEpicId(anotherEpicId).build();
        final long subtaskAId = manager.createSubtask(subtaskA);
        final long subtaskBId = manager.createSubtask(subtaskB);

        manager.deleteEpics();
        final List<Subtask> subtasks = manager.getSubtasks();
        final List<Task> prioritized = manager.getPrioritizedTasks();

        assertAll("subtasks removed with errors",
                () -> assertThrows(TaskNotFoundException.class, () -> manager.getSubtaskById(subtaskAId),
                        "subtasks removed with errors"),
                () -> assertThrows(TaskNotFoundException.class, () -> manager.getSubtaskById(subtaskBId),
                        "subtasks removed with errors"),
                () -> assertTrue(subtasks.isEmpty(), "subtasks removed with errors"),
                () -> assertTrue(prioritized.isEmpty(), "subtasks removed with errors")
        );
    }

    @Test
    public void shouldPassTaskToHistoryManagerWhenGetTaskById() {
        final long taskId = manager.createTask(testTask);
        final Task expectedTask = fromTestTask().withId(taskId).build();
        final List<Task> expectedTasks = List.of(expectedTask);

        manager.getTaskById(taskId);
        final List<Task> tasks = historyManager.getHistory();

        assertListEquals(expectedTasks, tasks, "history saved with errors");
    }

    @Test
    public void shouldPassEpicToHistoryManagerWhenGetEpicById() {
        final long epicId = manager.createEpic(testEpic);
        final Epic expectedEpic = fromTestEpic().withId(epicId).withStatus(TaskStatus.NEW).build();
        final List<Task> expectedTasks = List.of(expectedEpic);

        manager.getEpicById(epicId);
        final List<Task> tasks = historyManager.getHistory();

        assertListEquals(expectedTasks, tasks, "history saved with errors");
    }

    @Test
    public void shouldPassSubtaskToHistoryManagerWhenGetSubtaskById() {
        final long epicId = manager.createEpic(testEpic);
        final Subtask subtask = fromTestSubtask().withId(null).withEpicId(epicId).build();
        final long subtaskId = manager.createSubtask(subtask);
        final Subtask expectedSubtask = fromTestSubtask().withId(subtaskId).withEpicId(epicId).build();
        final List<Task> expectedTasks = List.of(expectedSubtask);

        manager.getSubtaskById(subtaskId);
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
    public void shouldRemoveTaskFromHistoryManagerWhenDeleteTask() {
        final long taskId = manager.createTask(testTask);
        manager.getTaskById(taskId);

        manager.deleteTask(taskId);
        final List<Task> tasks = historyManager.getHistory();

        assertTrue(tasks.isEmpty(), "task should be removed from history");
    }

    @Test
    public void shouldRemoveEpicFromHistoryManagerWhenDeleteEpic() {
        final long epicId = manager.createEpic(testEpic);
        manager.getEpicById(epicId);

        manager.deleteEpic(epicId);
        final List<Task> tasks = historyManager.getHistory();

        assertTrue(tasks.isEmpty(), "epic should be removed from history");
    }

    @Test
    public void shouldRemoveSubtaskFromHistoryManagerWhenDeleteSubtask() {
        final long epicId = manager.createEpic(testEpic);
        final Subtask subtask = fromTestSubtask().withId(null).withEpicId(epicId).build();
        final long subtaskId = manager.createSubtask(subtask);
        manager.getSubtaskById(subtaskId);

        manager.deleteSubtask(subtaskId);
        final List<Task> tasks = historyManager.getHistory();

        assertTrue(tasks.isEmpty(), "subtask should be removed from history");
    }

    @Test
    public void shouldRemoveSubtaskFromHistoryManagerWhenDeleteEpic() {
        final long epicId = manager.createEpic(testEpic);
        final long anotherEpicId = manager.createEpic(modifiedEpic);
        final Subtask subtaskA = fromTestSubtask().withId(null).withEpicId(epicId).build();
        final Subtask subtaskB = fromModifiedSubtask().withId(null).withEpicId(anotherEpicId).build();
        final long subtaskAId = manager.createSubtask(subtaskA);
        final long subtaskBId = manager.createSubtask(subtaskB);
        manager.getSubtaskById(subtaskAId);
        manager.getSubtaskById(subtaskBId);
        final Subtask expectedSubtask = fromModifiedSubtask().withId(subtaskBId).withEpicId(anotherEpicId)
                .build();
        final List<Task> expectedTasks = List.of(expectedSubtask);

        manager.deleteEpic(epicId);
        final List<Task> tasks = historyManager.getHistory();

        assertListEquals(expectedTasks, tasks, "incorrect history returned");
    }

    @Test
    public void shouldRemoveTasksFromHistoryManagerWhenDeleteTasks() {
        final long taskAId = manager.createTask(testTask);
        final long taskBId = manager.createTask(modifiedTask);
        manager.getTaskById(taskAId);
        manager.getTaskById(taskBId);

        manager.deleteTasks();
        final List<Task> tasks = historyManager.getHistory();

        assertTrue(tasks.isEmpty(), "tasks should be removed from history");
    }

    @Test
    public void shouldRemoveEpicsFromHistoryManagerWhenDeleteEpics() {
        final long epicAId = manager.createEpic(testEpic);
        final long epicBId = manager.createEpic(modifiedEpic);
        manager.getEpicById(epicAId);
        manager.getEpicById(epicBId);

        manager.deleteEpics();
        final List<Task> tasks = historyManager.getHistory();

        assertTrue(tasks.isEmpty(), "epics should be removed from history");
    }

    @Test
    public void shouldRemoveSubtasksFromHistoryManagerWhenDeleteSubtasks() {
        final long epicId = manager.createEpic(testEpic);
        final long anotherEpicId = manager.createEpic(modifiedEpic);
        final Subtask subtaskA = fromTestSubtask().withId(null).withEpicId(epicId).build();
        final Subtask subtaskB = fromModifiedSubtask().withId(null).withEpicId(anotherEpicId).build();
        final long subtaskAId = manager.createSubtask(subtaskA);
        final long subtaskBId = manager.createSubtask(subtaskB);
        manager.getSubtaskById(subtaskAId);
        manager.getSubtaskById(subtaskBId);

        manager.deleteSubtasks();
        final List<Task> tasks = historyManager.getHistory();

        assertTrue(tasks.isEmpty(), "subtasks should be removed from history");
    }

    @Test
    public void shouldRemoveSubtasksFromHistoryManagerWhenDeleteEpics() {
        final long epicId = manager.createEpic(testEpic);
        final long anotherEpicId = manager.createEpic(modifiedEpic);
        final Subtask subtaskA = fromTestSubtask().withId(null).withEpicId(epicId).build();
        final Subtask subtaskB = fromModifiedSubtask().withId(null).withEpicId(anotherEpicId).build();
        final long subtaskAId = manager.createSubtask(subtaskA);
        final long subtaskBId = manager.createSubtask(subtaskB);
        manager.getSubtaskById(subtaskAId);
        manager.getSubtaskById(subtaskBId);

        manager.deleteEpics();
        final List<Task> tasks = historyManager.getHistory();

        assertTrue(tasks.isEmpty(), "subtasks should be removed from history");
    }
}
