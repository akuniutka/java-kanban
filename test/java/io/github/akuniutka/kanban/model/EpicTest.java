package io.github.akuniutka.kanban.model;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static io.github.akuniutka.kanban.TestModels.*;
import static org.junit.jupiter.api.Assertions.*;

class EpicTest {
    @Test
    public void shouldCreateEpic() {
        Epic epic = new Epic();
        assertNotNull(epic, "epic was not created");
    }

    @Test
    public void shouldHaveIdOfIntegerType() {
        Epic epic = new Epic();

        epic.setId(TEST_EPIC_ID);
        long actualId = epic.getId();

        assertEquals(TEST_EPIC_ID, actualId, "epic has wrong id");
    }

    @Test
    public void shouldHaveCorrectType() {
        Epic epic = new Epic();

        assertEquals(TaskType.EPIC, epic.getType(), "task has wrong type");
    }

    @Test
    public void shouldHaveTitle() {
        Epic epic = new Epic();

        epic.setTitle(TEST_TITLE);
        String actualTitle = epic.getTitle();

        assertEquals(TEST_TITLE, actualTitle, "epic has wrong title");
    }

    @Test
    public void shouldHaveDescription() {
        Epic epic = new Epic();

        epic.setDescription(TEST_DESCRIPTION);
        String actualDescription = epic.getDescription();

        assertEquals(TEST_DESCRIPTION, actualDescription, "epic has wrong description");
    }

    @Test
    public void shouldKeepSubtaskIds() {
        Epic epic = new Epic();

        epic.setSubtaskIds(TEST_SUBTASK_IDS);
        List<Long> actualSubtaskIds = epic.getSubtaskIds();

        assertEquals(TEST_SUBTASK_IDS, actualSubtaskIds, "epic has wrong list of subtask ids");
    }

    @Test
    public void shouldHaveDuration() {
        Epic epic = new Epic();

        epic.setDuration(TEST_DURATION);
        long actualDuration = epic.getDuration();

        assertEquals(TEST_DURATION, actualDuration, "epic has wrong duration");
    }

    @Test
    public void shouldAcceptZeroDuration() {
        Epic epic = new Epic();

        epic.setDuration(0L);
        long actualDuration = epic.getDuration();

        assertEquals(0L, actualDuration, "epic has wrong duration");
    }

    @Test
    public void shouldThrowWhenNegativeDuration() {
        Epic epic = new Epic();

        Exception exception = assertThrows(IllegalArgumentException.class, () -> epic.setDuration(-TEST_DURATION));
        assertEquals("duration cannot be negative", exception.getMessage(), "message for exception is wrong");
    }

    @Test
    public void shouldHaveStartTime() {
        Epic epic = new Epic();

        epic.setStartTime(TEST_START_TIME);
        LocalDateTime actualStartTime = epic.getStartTime();

        assertEquals(TEST_START_TIME, actualStartTime, "epic has wrong start time");
    }

    @Test
    public void shouldTruncateStartTimeToMinutes() {
        Epic epic = new Epic();

        epic.setStartTime(TEST_START_TIME.plusSeconds(25));
        LocalDateTime actualStartTime = epic.getStartTime();

        assertEquals(TEST_START_TIME, actualStartTime, "epic has wrong start time");
    }

    @Test
    public void shouldAcceptNullStartTime() {
        Epic epic = new Epic();

        epic.setStartTime(null);
        LocalDateTime actualStartTime = epic.getStartTime();

        assertNull(actualStartTime, "epic start time should be null");
    }

    @Test
    public void shouldReturnNullEndTimeWhenStartTimeNull() {
        Epic epic = new Epic();
        epic.setStartTime(null);

        LocalDateTime actualEndTime = epic.getEndTime();

        assertNull(actualEndTime, "epic end time should be null");
    }

    @Test
    public void shouldReturnStartTimeAsWndTimeWhenDurationZero() {
        Epic epic = new Epic();
        epic.setDuration(0L);
        epic.setStartTime(TEST_START_TIME);

        LocalDateTime actualEndTime = epic.getEndTime();

        assertEquals(TEST_START_TIME, actualEndTime, "epic has wrong end time");
    }

    @Test
    public void shouldReturnCorrectEndTime() {
        Epic epic = new Epic();
        epic.setDuration(TEST_DURATION);
        epic.setStartTime(TEST_START_TIME);

        LocalDateTime actualEndTime = epic.getEndTime();

        assertEquals(TEST_END_TIME, actualEndTime, "epic has wrong end time");
    }

    @Test
    public void shouldHaveStatus() {
        Epic epic = new Epic();

        epic.setStatus(TEST_STATUS);
        TaskStatus actualStatus = epic.getStatus();

        assertEquals(TEST_STATUS, actualStatus, "epic has wrong status");
    }

    @Test
    public void shouldBeEqualWhenEqualIds() {
        Epic epic = createTestEpic(TEST_EPIC_ID, TEST_TITLE, TEST_DESCRIPTION);
        epic.setSubtaskIds(TEST_SUBTASK_IDS);
        epic.setDuration(TEST_DURATION);
        epic.setStartTime(TEST_START_TIME);
        epic.setStatus(TEST_STATUS);
        Epic anotherEpic = new Epic();
        anotherEpic.setId(TEST_EPIC_ID);

        assertEquals(epic, anotherEpic, "epics with same id must be considered equal");
    }

    @Test
    public void shouldNotBeEqualWhenNotEqualIds() {
        Epic epic = createTestEpic(TEST_EPIC_ID, TEST_TITLE, TEST_DESCRIPTION);
        epic.setSubtaskIds(TEST_SUBTASK_IDS);
        epic.setDuration(TEST_DURATION);
        epic.setStartTime(TEST_START_TIME);
        epic.setStatus(TEST_STATUS);
        Epic anotherEpic = createTestEpic(ANOTHER_TEST_ID, TEST_TITLE, TEST_DESCRIPTION);
        anotherEpic.setSubtaskIds(TEST_SUBTASK_IDS);
        anotherEpic.setDuration(TEST_DURATION);
        anotherEpic.setStartTime(TEST_START_TIME);
        anotherEpic.setStatus(TEST_STATUS);

        assertNotEquals(epic, anotherEpic, "epics with different ids may not considered equal");
    }

    @Test
    public void shouldConvertToStringWhenFieldsNull() {
        String expectedString = "Epic{id=null, title=null, description=null, subtaskIds=[], duration=0, startTime=null,"
                + " status=null}";
        Epic epic = new Epic();
        epic.setStatus(null);

        String actualString = epic.toString();

        assertEquals(expectedString, actualString, "string representation of epic is wrong");
    }

    @Test
    public void shouldConvertToStringWhenFieldsNonNull() {
        String expectedString = "Epic{id=2, title=\"Title\", description.length=11, subtaskIds=[4, 5, 6], duration=30, "
                + "startTime=2000-05-01T13:30, status=IN_PROGRESS}";
        Epic epic = createTestEpic(TEST_EPIC_ID, TEST_TITLE, TEST_DESCRIPTION);
        epic.setSubtaskIds(TEST_SUBTASK_IDS);
        epic.setDuration(TEST_DURATION);
        epic.setStartTime(TEST_START_TIME);
        epic.setStatus(TEST_STATUS);

        String actualString = epic.toString();

        assertEquals(expectedString, actualString, "string representation of epic is wrong");
    }
}