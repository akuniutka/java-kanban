package io.github.akuniutka.kanban.model;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static io.github.akuniutka.kanban.TestModels.*;
import static org.junit.jupiter.api.Assertions.*;

public class TaskTest {
    @Test
    public void shouldCreateTask() {
        Task task = new Task();
        assertNotNull(task, "task was not created");
    }

    @Test
    public void shouldHaveIdOfIntegerType() {
        Task task = new Task();

        task.setId(TEST_TASK_ID);
        long actualId = task.getId();

        assertEquals(TEST_TASK_ID, actualId, "task has wrong id");
    }

    @Test
    public void shouldHaveCorrectType() {
        Task task = new Task();

        assertEquals(TaskType.TASK, task.getType(), "task has wrong type");
    }

    @Test
    public void shouldHaveTitle() {
        Task task = new Task();

        task.setTitle(TEST_TITLE);
        String actualTitle = task.getTitle();

        assertEquals(TEST_TITLE, actualTitle, "task has wrong title");
    }

    @Test
    public void shouldHaveDescription() {
        Task task = new Task();

        task.setDescription(TEST_DESCRIPTION);
        String actualDescription = task.getDescription();

        assertEquals(TEST_DESCRIPTION, actualDescription, "task has wrong description");
    }

    @Test
    public void shouldHaveDuration() {
        Task task = new Task();

        task.setDuration(TEST_DURATION);
        long actualDuration = task.getDuration();

        assertEquals(TEST_DURATION, actualDuration, "task has wrong duration");
    }

    @Test
    public void shouldAcceptZeroDuration() {
        Task task = new Task();

        task.setDuration(0L);
        long actualDuration = task.getDuration();

        assertEquals(0L, actualDuration, "task has wrong duration");
    }

    @Test
    public void shouldThrowWhenNegativeDuration() {
        Task task = new Task();

        Exception exception = assertThrows(IllegalArgumentException.class, () -> task.setDuration(-TEST_DURATION));
        assertEquals("duration cannot be negative", exception.getMessage(), "message for exception is wrong");
    }

    @Test
    public void shouldHaveStartTime() {
        Task task = new Task();

        task.setStartTime(TEST_START_TIME);
        LocalDateTime actualStartTime = task.getStartTime();

        assertEquals(TEST_START_TIME, actualStartTime, "task has wrong start time");
    }

    @Test
    public void shouldTruncateStartTimeToMinutes() {
        Task task = new Task();

        task.setStartTime(TEST_START_TIME.plusSeconds(25));
        LocalDateTime actualStartTime = task.getStartTime();

        assertEquals(TEST_START_TIME, actualStartTime, "task has wrong start time");
    }

    @Test
    public void shouldAcceptNullStartTime() {
        Task task = new Task();

        task.setStartTime(null);
        LocalDateTime actualStartTime = task.getStartTime();

        assertNull(actualStartTime, "task start time should be null");
    }

    @Test
    public void shouldReturnNullEndTimeWhenStartTimeNull() {
        Task task = new Task();
        task.setStartTime(null);

        LocalDateTime actualEndTime = task.getEndTime();

        assertNull(actualEndTime, "task end time should be null");
    }

    @Test
    public void shouldReturnStartTimeAsEndTimeWhenDurationZero() {
        Task task = new Task();
        task.setDuration(0L);
        task.setStartTime(TEST_START_TIME);

        LocalDateTime actualEndTime = task.getEndTime();

        assertEquals(TEST_START_TIME, actualEndTime, "task has wrong end time");
    }

    @Test
    public void shouldReturnCorrectEndTime() {
        Task task = new Task();
        task.setDuration(TEST_DURATION);
        task.setStartTime(TEST_START_TIME);

        LocalDateTime actualEndTime = task.getEndTime();

        assertEquals(TEST_END_TIME, actualEndTime, "task has wrong end time");
    }

    @Test
    public void shouldHaveStatus() {
        Task task = new Task();

        task.setStatus(TEST_STATUS);
        TaskStatus actualStatus = task.getStatus();

        assertEquals(TEST_STATUS, actualStatus, "task has wrong status");
    }

    @Test
    public void shouldBeEqualWhenEqualIds() {
        Task task = createTestTask(TEST_TASK_ID, TEST_TITLE, TEST_DESCRIPTION, TEST_DURATION, TEST_START_TIME,
                TEST_STATUS);
        Task anotherTask = createTestTask();
        anotherTask.setId(TEST_TASK_ID);

        assertEquals(task, anotherTask, "tasks with same id must be considered equal");
    }

    @Test
    public void shouldNotBeEqualWhenNotEqualIds() {
        Task task = createTestTask(TEST_TASK_ID, TEST_TITLE, TEST_DESCRIPTION, TEST_DURATION, TEST_START_TIME,
                TEST_STATUS);
        Task anotherTask = createTestTask(ANOTHER_TEST_ID, TEST_TITLE, TEST_DESCRIPTION, TEST_DURATION, TEST_START_TIME,
                TEST_STATUS);

        assertNotEquals(task, anotherTask, "tasks with different ids may not considered equal");
    }

    @Test
    public void shouldConvertToStringWhenFieldsNull() {
        String expectedString = "Task{id=null, title=null, description=null, duration=0, startTime=null, status=null}";
        Task task = createTestTask();

        String actualString = task.toString();

        assertEquals(expectedString, actualString, "string representation of task is wrong");
    }

    @Test
    public void shouldConvertToStringWhenFieldsNonNull() {
        String expectedString = "Task{id=1, title=\"Title\", description.length=11, duration=30, "
                + "startTime=2000-05-01T13:30, status=IN_PROGRESS}";
        Task task = createTestTask(TEST_TASK_ID, TEST_TITLE, TEST_DESCRIPTION, TEST_DURATION, TEST_START_TIME,
                TEST_STATUS);

        String actualString = task.toString();

        assertEquals(expectedString, actualString, "string representation of task is wrong");
    }
}
