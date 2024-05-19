package io.github.akuniutka.kanban.model;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static io.github.akuniutka.kanban.TestModels.*;
import static org.junit.jupiter.api.Assertions.*;

class SubtaskTest {
    @Test
    public void shouldCreateSubtask() {
        Subtask subtask = new Subtask();
        assertNotNull(subtask, "subtask was not created");
    }

    @Test
    public void shouldHaveIdOfIntegerType() {
        Subtask subtask = new Subtask();

        subtask.setId(TEST_SUBTASK_ID);
        long actualId = subtask.getId();

        assertEquals(TEST_SUBTASK_ID, actualId, "subtask has wrong id");
    }

    @Test
    public void shouldHaveCorrectType() {
        Subtask subtask = new Subtask();

        assertEquals(TaskType.SUBTASK, subtask.getType(), "task has wrong type");
    }

    @Test
    public void shouldHaveEpicId() {
        Subtask subtask = new Subtask();

        subtask.setEpicId(TEST_EPIC_ID);
        long actualEpicId = subtask.getEpicId();

        assertEquals(TEST_EPIC_ID, actualEpicId, "subtask has wrong epic id");
    }

    @Test
    public void shouldHaveTitle() {
        Subtask subtask = new Subtask();

        subtask.setTitle(TEST_TITLE);
        String actualTitle = subtask.getTitle();

        assertEquals(TEST_TITLE, actualTitle, "subtask has wrong title");
    }

    @Test
    public void shouldHaveDescription() {
        Subtask subtask = new Subtask();

        subtask.setDescription(TEST_DESCRIPTION);
        String actualDescription = subtask.getDescription();

        assertEquals(TEST_DESCRIPTION, actualDescription, "subtask has wrong description");
    }

    @Test
    public void shouldHaveDuration() {
        Subtask subtask = new Subtask();

        subtask.setDuration(TEST_DURATION);
        long actualDuration = subtask.getDuration();

        assertEquals(TEST_DURATION, actualDuration, "subtask has wrong duration");
    }

    @Test
    public void shouldAcceptZeroDuration() {
        Subtask subtask = new Subtask();

        subtask.setDuration(0L);
        long actualDuration = subtask.getDuration();

        assertEquals(0L, actualDuration, "subtask has wrong duration");
    }

    @Test
    public void shouldThrowWhenNegativeDuration() {
        Subtask subtask = new Subtask();

        Exception exception = assertThrows(IllegalArgumentException.class, () -> subtask.setDuration(-TEST_DURATION));
        assertEquals("duration cannot be negative", exception.getMessage(), "message for exception is wrong");
    }

    @Test
    public void shouldHaveStartTime() {
        Subtask subtask = new Subtask();

        subtask.setStartTime(TEST_START_TIME);
        LocalDateTime actualStartTime = subtask.getStartTime();

        assertEquals(TEST_START_TIME, actualStartTime, "subtask has wrong start time");
    }

    @Test
    public void shouldTruncateStartTimeToMinutes() {
        Subtask subtask = new Subtask();

        subtask.setStartTime(TEST_START_TIME.plusSeconds(25));
        LocalDateTime actualStartTime = subtask.getStartTime();

        assertEquals(TEST_START_TIME, actualStartTime, "subtask has wrong start time");
    }

    @Test
    public void shouldAcceptNullStartTime() {
        Subtask subtask = new Subtask();

        subtask.setStartTime(null);
        LocalDateTime actualStartTime = subtask.getStartTime();

        assertNull(actualStartTime, "subtask start time should be null");
    }

    @Test
    public void shouldReturnNullEndTimeWhenStartTimeNull() {
        Subtask subtask = new Subtask();
        subtask.setStartTime(null);

        LocalDateTime actualEndTime = subtask.getEndTime();

        assertNull(actualEndTime, "subtask end time should be null");
    }

    @Test
    public void shouldReturnStartTimeAsEndTimeWhenDurationZero() {
        Subtask subtask = new Subtask();
        subtask.setDuration(0L);
        subtask.setStartTime(TEST_START_TIME);

        LocalDateTime actualEndTime = subtask.getEndTime();

        assertEquals(TEST_START_TIME, actualEndTime, "subtask has wrong end time");
    }

    @Test
    public void shouldReturnCorrectEndTime() {
        Subtask subtask = new Subtask();
        subtask.setDuration(TEST_DURATION);
        subtask.setStartTime(TEST_START_TIME);

        LocalDateTime actualEndTime = subtask.getEndTime();

        assertEquals(TEST_END_TIME, actualEndTime, "subtask has wrong end time");
    }

    @Test
    public void shouldHaveStatus() {
        Subtask subtask = new Subtask();

        subtask.setStatus(TEST_STATUS);
        TaskStatus actualStatus = subtask.getStatus();

        assertEquals(TEST_STATUS, actualStatus, "subtask has wrong status");
    }

    @Test
    public void shouldBeEqualWhenEqualIds() {
        Subtask subtask = createTestSubtask(TEST_SUBTASK_ID, TEST_EPIC_ID, TEST_TITLE, TEST_DESCRIPTION, TEST_DURATION,
                TEST_START_TIME, TEST_STATUS);
        Subtask anotherSubtask = new Subtask();
        anotherSubtask.setId(TEST_SUBTASK_ID);

        assertEquals(subtask, anotherSubtask, "subtasks with same id must be considered equal");
    }

    @Test
    public void shouldNotBeEqualWhenNotEqualIds() {
        Subtask subtask = createTestSubtask(TEST_SUBTASK_ID, TEST_EPIC_ID, TEST_TITLE, TEST_DESCRIPTION, TEST_DURATION,
                TEST_START_TIME, TEST_STATUS);
        Subtask anotherSubtask = createTestSubtask(ANOTHER_TEST_ID, TEST_EPIC_ID, TEST_TITLE, TEST_DESCRIPTION,
                TEST_DURATION, TEST_START_TIME, TEST_STATUS);

        assertNotEquals(subtask, anotherSubtask, "tasks with different ids may not considered equal");
    }

    @Test
    public void shouldConvertToStringWhenFieldsNull() {
        String expectedString = "Subtask{id=null, epicId=null, title=null, description=null, duration=0, "
                + "startTime=null, status=null}";
        Subtask subtask = createTestSubtask();

        String actualString = subtask.toString();

        assertEquals(expectedString, actualString, "string representation of subtask is wrong");
    }

    @Test
    public void shouldConvertToStringWhenFieldsNonNull() {
        String expectedString = "Subtask{id=3, epicId=2, title=\"Title\", description.length=11, duration=30, "
                + "startTime=2000-05-01T13:30, status=IN_PROGRESS}";
        Subtask subtask = createTestSubtask(TEST_SUBTASK_ID, TEST_EPIC_ID, TEST_TITLE, TEST_DESCRIPTION, TEST_DURATION,
                TEST_START_TIME, TEST_STATUS);

        String actualString = subtask.toString();

        assertEquals(expectedString, actualString, "string representation of subtask is wrong");
    }
}