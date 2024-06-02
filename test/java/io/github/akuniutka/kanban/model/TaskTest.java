package io.github.akuniutka.kanban.model;

import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.LocalDateTime;

import static io.github.akuniutka.kanban.TestModels.*;
import static org.junit.jupiter.api.Assertions.*;

public class TaskTest {
    @Test
    public void shouldCreateTask() {
        final Task task = new Task();
        assertNotNull(task, "task was not created");
    }

    @Test
    public void shouldHaveIdOfIntegerType() {
        final Task task = new Task();

        task.setId(TEST_TASK_ID);
        final long actualId = task.getId();

        assertEquals(TEST_TASK_ID, actualId, "task has wrong id");
    }

    @Test
    public void shouldHaveCorrectType() {
        final Task task = new Task();

        assertEquals(TaskType.TASK, task.getType(), "task has wrong type");
    }

    @Test
    public void shouldHaveTitle() {
        final Task task = new Task();

        task.setTitle(TEST_TITLE);
        final String actualTitle = task.getTitle();

        assertEquals(TEST_TITLE, actualTitle, "task has wrong title");
    }

    @Test
    public void shouldHaveDescription() {
        final Task task = new Task();

        task.setDescription(TEST_DESCRIPTION);
        final String actualDescription = task.getDescription();

        assertEquals(TEST_DESCRIPTION, actualDescription, "task has wrong description");
    }

    @Test
    public void shouldHaveDuration() {
        final Task task = new Task();

        task.setDuration(TEST_DURATION);
        final Duration actualDuration = task.getDuration();

        assertEquals(TEST_DURATION, actualDuration, "task has wrong duration");
    }

    @Test
    public void shouldAcceptNullDuration() {
        final Task task = new Task();

        task.setDuration(null);
        final Duration actualDuration = task.getDuration();

        assertNull(actualDuration, "task has wrong duration");
    }

    @Test
    public void shouldHaveStartTime() {
        final Task task = new Task();

        task.setStartTime(TEST_START_TIME);
        final LocalDateTime actualStartTime = task.getStartTime();

        assertEquals(TEST_START_TIME, actualStartTime, "task has wrong start time");
    }

    @Test
    public void shouldAcceptNullStartTime() {
        final Task task = new Task();

        task.setStartTime(null);
        final LocalDateTime actualStartTime = task.getStartTime();

        assertNull(actualStartTime, "task start time should be null");
    }

    @Test
    public void shouldReturnNullEndTimeWhenDurationNull() {
        final Task task = new Task();
        task.setDuration(null);

        final LocalDateTime actualEndTime = task.getEndTime();

        assertNull(actualEndTime, "task end time should be null");
    }

    @Test
    public void shouldReturnNullEndTimeWhenStartTimeNull() {
        final Task task = new Task();
        task.setStartTime(null);

        final LocalDateTime actualEndTime = task.getEndTime();

        assertNull(actualEndTime, "task end time should be null");
    }

    @Test
    public void shouldReturnCorrectEndTime() {
        final Task task = new Task();
        task.setDuration(TEST_DURATION);
        task.setStartTime(TEST_START_TIME);

        final LocalDateTime actualEndTime = task.getEndTime();

        assertEquals(TEST_END_TIME, actualEndTime, "task has wrong end time");
    }

    @Test
    public void shouldHaveStatus() {
        final Task task = new Task();

        task.setStatus(TEST_STATUS);
        final TaskStatus actualStatus = task.getStatus();

        assertEquals(TEST_STATUS, actualStatus, "task has wrong status");
    }

    @Test
    public void shouldBeEqualWhenEqualIds() {
        final Task task = fromTestTask().build();
        final Task anotherTask = fromEmptyTask().withId(TEST_TASK_ID).build();

        assertEquals(task, anotherTask, "tasks with same id must be considered equal");
    }

    @Test
    public void shouldNotBeEqualWhenNotEqualIds() {
        final Task task = fromTestTask().build();
        final Task anotherTask = fromTestTask().withId(ANOTHER_TEST_ID).build();

        assertNotEquals(task, anotherTask, "tasks with different ids may not considered equal");
    }

    @Test
    public void shouldConvertToStringWhenFieldsNull() {
        final String expected = """
                Task{id=null, type=TASK, title=null, description=null, duration=null, startTime=null, endTime=null, \
                status=null}\
                """;
        final Task task = fromEmptyTask().build();

        final String actual = task.toString();

        assertEquals(expected, actual, "string representation of task is wrong");
    }

    @Test
    public void shouldConvertToStringWhenFieldsNonNull() {
        final String expected = """
                Task{id=1, type=TASK, title="Title", description.length=11, duration=PT30M, \
                startTime=2000-05-01T13:30, endTime=2000-05-01T14:00, status=IN_PROGRESS}\
                """;
        final Task task = fromTestTask().build();

        final String actual = task.toString();

        assertEquals(expected, actual, "string representation of task is wrong");
    }
}
