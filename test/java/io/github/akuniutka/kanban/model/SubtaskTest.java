package io.github.akuniutka.kanban.model;

import io.github.akuniutka.kanban.exception.ManagerException;
import io.github.akuniutka.kanban.exception.ManagerValidationException;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static io.github.akuniutka.kanban.TestModels.*;
import static org.junit.jupiter.api.Assertions.*;

class SubtaskTest {
    private static final String WRONG_EXCEPTION_MESSAGE = "message for exception is wrong";

    @Test
    public void shouldCreateSubtask() {
        final Subtask subtask = new Subtask();
        assertNotNull(subtask, "subtask was not created");
    }

    @Test
    public void shouldHaveIdOfIntegerType() {
        final Subtask subtask = new Subtask();

        subtask.setId(TEST_SUBTASK_ID);
        final long actualId = subtask.getId();

        assertEquals(TEST_SUBTASK_ID, actualId, "subtask has wrong id");
    }

    @Test
    public void shouldHaveCorrectType() {
        final Subtask subtask = new Subtask();

        assertEquals(TaskType.SUBTASK, subtask.getType(), "task has wrong type");
    }

    @Test
    public void shouldHaveEpicId() {
        final Subtask subtask = new Subtask();

        subtask.setEpicId(TEST_EPIC_ID);
        final long actualEpicId = subtask.getEpicId();

        assertEquals(TEST_EPIC_ID, actualEpicId, "subtask has wrong epic id");
    }

    @Test
    public void shouldHaveTitle() {
        final Subtask subtask = new Subtask();

        subtask.setTitle(TEST_TITLE);
        final String actualTitle = subtask.getTitle();

        assertEquals(TEST_TITLE, actualTitle, "subtask has wrong title");
    }

    @Test
    public void shouldHaveDescription() {
        final Subtask subtask = new Subtask();

        subtask.setDescription(TEST_DESCRIPTION);
        final String actualDescription = subtask.getDescription();

        assertEquals(TEST_DESCRIPTION, actualDescription, "subtask has wrong description");
    }

    @Test
    public void shouldHaveDuration() {
        final Subtask subtask = new Subtask();

        subtask.setDuration(TEST_DURATION);
        final Long actualDuration = subtask.getDuration();

        assertEquals(TEST_DURATION, actualDuration, "subtask has wrong duration");
    }

    @Test
    public void shouldAcceptNullDuration() {
        final Subtask subtask = new Subtask();

        subtask.setDuration(null);
        final Long actualDuration = subtask.getDuration();

        assertNull(actualDuration, "subtask has wrong duration");
    }

    @Test
    public void shouldThrowWhenDurationZero() {
        final Subtask subtask = new Subtask();

        final Exception exception = assertThrows(ManagerValidationException.class, () -> subtask.setDuration(0L));
        assertEquals("duration cannot be negative or zero", exception.getMessage(), WRONG_EXCEPTION_MESSAGE);
    }

    @Test
    public void shouldThrowWhenDurationNegative() {
        final Subtask subtask = new Subtask();

        final Exception exception = assertThrows(ManagerException.class,
                () -> subtask.setDuration(-TEST_DURATION));
        assertEquals("duration cannot be negative or zero", exception.getMessage(), "message for exception is wrong");
    }

    @Test
    public void shouldHaveStartTime() {
        final Subtask subtask = new Subtask();

        subtask.setStartTime(TEST_START_TIME);
        final LocalDateTime actualStartTime = subtask.getStartTime();

        assertEquals(TEST_START_TIME, actualStartTime, "subtask has wrong start time");
    }

    @Test
    public void shouldTruncateStartTimeToMinutes() {
        final Subtask subtask = new Subtask();

        subtask.setStartTime(TEST_START_TIME.plusSeconds(25));
        final LocalDateTime actualStartTime = subtask.getStartTime();

        assertEquals(TEST_START_TIME, actualStartTime, "subtask has wrong start time");
    }

    @Test
    public void shouldAcceptNullStartTime() {
        final Subtask subtask = new Subtask();

        subtask.setStartTime(null);
        final LocalDateTime actualStartTime = subtask.getStartTime();

        assertNull(actualStartTime, "subtask start time should be null");
    }

    @Test
    public void shouldReturnNullEndTimeWhenDurationNull() {
        final Subtask subtask = new Subtask();
        subtask.setDuration(null);

        final LocalDateTime actualEndTime = subtask.getEndTime();

        assertNull(actualEndTime, "subtask end time should be null");
    }

    @Test
    public void shouldReturnNullEndTimeWhenStartTimeNull() {
        final Subtask subtask = new Subtask();
        subtask.setStartTime(null);

        final LocalDateTime actualEndTime = subtask.getEndTime();

        assertNull(actualEndTime, "subtask end time should be null");
    }

    @Test
    public void shouldReturnCorrectEndTime() {
        final Subtask subtask = new Subtask();
        subtask.setDuration(TEST_DURATION);
        subtask.setStartTime(TEST_START_TIME);

        final LocalDateTime actualEndTime = subtask.getEndTime();

        assertEquals(TEST_END_TIME, actualEndTime, "subtask has wrong end time");
    }

    @Test
    public void shouldHaveStatus() {
        final Subtask subtask = new Subtask();

        subtask.setStatus(TEST_STATUS);
        final TaskStatus actualStatus = subtask.getStatus();

        assertEquals(TEST_STATUS, actualStatus, "subtask has wrong status");
    }

    @Test
    public void shouldBeEqualWhenEqualIds() {
        final Subtask subtask = fromTestSubtask().build();
        final Subtask anotherSubtask = fromEmptySubtask().withId(TEST_SUBTASK_ID).build();

        assertEquals(subtask, anotherSubtask, "subtasks with same id must be considered equal");
    }

    @Test
    public void shouldNotBeEqualWhenNotEqualIds() {
        final Subtask subtask = fromTestSubtask().build();
        final Subtask anotherSubtask = fromTestSubtask().withId(ANOTHER_TEST_ID).build();

        assertNotEquals(subtask, anotherSubtask, "tasks with different ids may not considered equal");
    }

    @Test
    public void shouldConvertToStringWhenFieldsNull() {
        final String expected = "Subtask{id=null, epicId=null, title=null, description=null, duration=null, "
                + "startTime=null, status=null}";
        final Subtask subtask = fromEmptySubtask().build();

        final String actual = subtask.toString();

        assertEquals(expected, actual, "string representation of subtask is wrong");
    }

    @Test
    public void shouldConvertToStringWhenFieldsNonNull() {
        final String expected = "Subtask{id=3, epicId=2, title=\"Title\", description.length=11, duration=30, "
                + "startTime=2000-05-01T13:30, status=IN_PROGRESS}";
        final Subtask subtask = fromTestSubtask().build();

        final String actual = subtask.toString();

        assertEquals(expected, actual, "string representation of subtask is wrong");
    }
}
