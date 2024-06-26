package io.github.akuniutka.kanban.model;

import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.LocalDateTime;

import static io.github.akuniutka.kanban.TestModels.*;
import static org.junit.jupiter.api.Assertions.*;

class SubtaskTest {
    @Test
    public void shouldCreateSubtask() {
        final Subtask subtask = new Subtask();
        assertNotNull(subtask, "subtask was not created");
    }

    @Test
    public void shouldHaveIdOfIntegerType() {
        final Subtask subtask = new Subtask();

        subtask.setId(TEST_SUBTASK_ID);
        final Long actualId = subtask.getId();

        assertEquals(TEST_SUBTASK_ID, actualId, "subtask has wrong id");
    }

    @Test
    public void shouldAcceptNullId() {
        final Subtask subtask = new Subtask();

        subtask.setId(null);
        final Long actualId = subtask.getId();

        assertNull(actualId, "subtask id should be null");
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
        final Long actualEpicId = subtask.getEpicId();

        assertEquals(TEST_EPIC_ID, actualEpicId, "subtask has wrong epic id");
    }

    @Test
    public void shouldAcceptNullEpicId() {
        final Subtask subtask = new Subtask();

        subtask.setEpicId(null);
        final Long actualEpicId = subtask.getEpicId();

        assertNull(actualEpicId, "subtask epic id should be null");
    }

    @Test
    public void shouldHaveTitle() {
        final Subtask subtask = new Subtask();

        subtask.setTitle(TEST_TITLE);
        final String actualTitle = subtask.getTitle();

        assertEquals(TEST_TITLE, actualTitle, "subtask has wrong title");
    }

    @Test
    public void shouldAcceptNullTitle() {
        final Subtask subtask = new Subtask();

        subtask.setTitle(null);
        final String actualTitle = subtask.getTitle();

        assertNull(actualTitle, "subtask title should be null");
    }

    @Test
    public void shouldHaveDescription() {
        final Subtask subtask = new Subtask();

        subtask.setDescription(TEST_DESCRIPTION);
        final String actualDescription = subtask.getDescription();

        assertEquals(TEST_DESCRIPTION, actualDescription, "subtask has wrong description");
    }

    @Test
    public void shouldAcceptNullDescription() {
        final Subtask subtask = new Subtask();

        subtask.setDescription(null);
        final String actualDescription = subtask.getDescription();

        assertNull(actualDescription, "subtask description should be null");
    }

    @Test
    public void shouldHaveDuration() {
        final Subtask subtask = new Subtask();

        subtask.setDuration(TEST_DURATION);
        final Duration actualDuration = subtask.getDuration();

        assertEquals(TEST_DURATION, actualDuration, "subtask has wrong duration");
    }

    @Test
    public void shouldAcceptNullDuration() {
        final Subtask subtask = new Subtask();

        subtask.setDuration(null);
        final Duration actualDuration = subtask.getDuration();

        assertNull(actualDuration, "subtask has wrong duration");
    }

    @Test
    public void shouldHaveStartTime() {
        final Subtask subtask = new Subtask();

        subtask.setStartTime(TEST_START_TIME);
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
    public void shouldAcceptNullStatus() {
        final Subtask subtask = new Subtask();

        subtask.setStatus(null);
        final TaskStatus actualStatus = subtask.getStatus();

        assertNull(actualStatus, "subtask status should be null");
    }

    @Test
    public void shouldBeEqualWhenEqualIds() {
        final Subtask subtask = fromTestSubtask().build();
        final Subtask anotherSubtask = fromModifiedSubtask().build();

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
        final String expected = """
                Subtask{id=null, type=SUBTASK, epicId=null, title=null, description=null, duration=null, \
                startTime=null, endTime=null, status=null}\
                """;
        final Subtask subtask = new Subtask();

        final String actual = subtask.toString();

        assertEquals(expected, actual, "string representation of subtask is wrong");
    }

    @Test
    public void shouldConvertToStringWhenFieldsNonNull() {
        final String expected = """
                Subtask{id=3, type=SUBTASK, epicId=2, title="Title", description.length=11, duration=PT30M, \
                startTime=2000-05-01T13:30, endTime=2000-05-01T14:00, status=IN_PROGRESS}\
                """;
        final Subtask subtask = fromTestSubtask().build();

        final String actual = subtask.toString();

        assertEquals(expected, actual, "string representation of subtask is wrong");
    }
}
