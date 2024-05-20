package io.github.akuniutka.kanban.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static io.github.akuniutka.kanban.TestModels.*;
import static org.junit.jupiter.api.Assertions.*;

class EpicTest {
    private static final String WRONG_EXCEPTION_MESSAGE = "message for exception is wrong";
    private List<Subtask> testSubtasks;
    private List<Subtask> twoTestSubtasks;

    @BeforeEach
    public void setUp() {
        Subtask testSubtask = createTestSubtask(TEST_SUBTASK_ID, TEST_EPIC_ID, TEST_TITLE, TEST_DESCRIPTION,
                TEST_DURATION, TEST_START_TIME, TEST_STATUS);
        Subtask anotherTestSubtask = createTestSubtask(ANOTHER_TEST_ID, TEST_EPIC_ID, MODIFIED_TEST_TITLE,
                MODIFIED_TEST_DESCRIPTION, MODIFIED_TEST_DURATION, MODIFIED_TEST_START_TIME, MODIFIED_TEST_STATUS);
        testSubtasks = List.of(testSubtask);
        twoTestSubtasks = List.of(testSubtask, anotherTestSubtask);
    }

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
    public void shouldKeepSubtasks() {
        Epic epic = new Epic();
        List<Subtask> subtasks = testSubtasks;

        epic.setSubtasks(subtasks);
        List<Subtask> actualSubtasks = epic.getSubtasks();

        assertEquals(testSubtasks, actualSubtasks, "epic has wrong list of subtasks");
        Subtask subtask = subtasks.getFirst();
        assertEquals(TEST_EPIC_ID, subtask.getEpicId(), "epic id changed in subtask");
        assertEquals(TEST_TITLE, subtask.getTitle(), "subtask title changed");
        assertEquals(TEST_DESCRIPTION, subtask.getDescription(), "subtask description changed");
        assertEquals(TEST_DURATION, subtask.getDuration(), "subtask duration changed");
        assertEquals(TEST_START_TIME, subtask.getStartTime(), "subtask start time changed");
        assertEquals(TEST_STATUS, subtask.getStatus(), "subtask status changed");
    }

    @Test
    public void shouldThrowWhenSubtasksNull() {
        Epic epic = new Epic();

        Exception exception = assertThrows(NullPointerException.class, () -> epic.setSubtasks(null));
        assertEquals("list of subtasks cannot be null", exception.getMessage(), WRONG_EXCEPTION_MESSAGE);
    }

    @Test
    public void shouldHaveZeroDurationWhenNoSubtasks() {
        Epic epic = new Epic();
        epic.getSubtasks().clear();

        long actualDuration = epic.getDuration();

        assertEquals(0L, actualDuration, "epic has wrong duration");
    }

    @Test
    public void shouldHaveDurationWhenSubtaskWithDuration() {
        Epic epic = new Epic();
        epic.setSubtasks(testSubtasks);

        long actualDuration = epic.getDuration();

        assertEquals(TEST_DURATION, actualDuration, "epic has wrong duration");
    }

    @Test
    public void shouldHaveAggregateDurationWhenSeveralSubtasksWithDuration() {
        Epic epic = new Epic();
        epic.setSubtasks(twoTestSubtasks);

        long actualDuration = epic.getDuration();

        assertEquals(TEST_DURATION + MODIFIED_TEST_DURATION, actualDuration, "epic has wrong duration");
    }

    @Test
    public void shouldThrowWhenSetDuration() {
        Epic epic = new Epic();

        Exception exception = assertThrows(UnsupportedOperationException.class, () -> epic.setDuration(TEST_DURATION));
        assertEquals("cannot explicitly set epic duration", exception.getMessage(), WRONG_EXCEPTION_MESSAGE);
    }

    @Test
    public void shouldHaveNullStartTimeWhenNoSubtasks() {
        Epic epic = new Epic();
        epic.getSubtasks().clear();

        LocalDateTime actualStartTime = epic.getStartTime();

        assertNull(actualStartTime, "epic has wrong start time");
    }

    @Test
    public void shouldHaveNullStartTimeWhenNoSubtasksWithStartTime() {
        Epic epic = new Epic();
        testSubtasks.getFirst().setStartTime(null);
        epic.setSubtasks(testSubtasks);

        LocalDateTime actualStartTime = epic.getStartTime();

        assertNull(actualStartTime, "epic has wrong start time");
    }

    @Test
    public void shouldHaveStartTimeWhenSubtaskWithStartTime() {
        Epic epic = new Epic();
        epic.setSubtasks(testSubtasks);

        LocalDateTime actualStartTime = epic.getStartTime();

        assertEquals(TEST_START_TIME, actualStartTime, "epic has wrong start time");
    }

    @Test
    public void shouldHaveMinStartTimeWhenSeveralSubtasksWithStartTime() {
        Epic epic = new Epic();
        epic.setSubtasks(twoTestSubtasks);

        LocalDateTime actualStartTime = epic.getStartTime();

        assertEquals(TEST_START_TIME, actualStartTime, "epic has wrong start time");
    }

    @Test
    public void shouldThrowWhenSetStartTime() {
        Epic epic = new Epic();

        Exception exception = assertThrows(UnsupportedOperationException.class,
                () -> epic.setStartTime(TEST_START_TIME));
        assertEquals("cannot explicitly set epic start time", exception.getMessage(), WRONG_EXCEPTION_MESSAGE);
    }

    @Test
    public void shouldHaveNullEndTimeWhenNoSubtasks() {
        Epic epic = new Epic();
        epic.getSubtasks().clear();

        LocalDateTime actualEndTime = epic.getEndTime();

        assertNull(actualEndTime, "epic has wrong end time");
    }

    @Test
    public void shouldHaveNullEndTimeWhenNoSubtasksWithEndTime() {
        Epic epic = new Epic();
        testSubtasks.getFirst().setStartTime(null);
        epic.setSubtasks(testSubtasks);

        LocalDateTime actualEndTime = epic.getEndTime();

        assertNull(actualEndTime, "epic has wrong end time");
    }

    @Test
    public void shouldHaveEndTimeWhenSubtaskWithEndTime() {
        Epic epic = new Epic();
        epic.setSubtasks(testSubtasks);

        LocalDateTime actualEndTime = epic.getEndTime();

        assertEquals(TEST_END_TIME, actualEndTime, "epic has wrong end time");
    }

    @Test
    public void shouldHaveMaxEndTimeWhenSeveralSubtasksWithEndTime() {
        Epic epic = new Epic();
        epic.setSubtasks(twoTestSubtasks);

        LocalDateTime actualEndTime = epic.getEndTime();

        assertEquals(MODIFIED_TEST_END_TIME, actualEndTime, "epic has wrong start time");
    }

    @Test
    public void shouldHaveStatusNewWhenNoSubtasks() {
        Epic epic = new Epic();
        epic.getSubtasks().clear();

        TaskStatus actualStatus = epic.getStatus();

        assertEquals(TaskStatus.NEW, actualStatus, "epic has wrong status");
    }

    @Test
    public void shouldHaveStatusNewWhenAllSubtasksHaveStatusNew() {
        Epic epic = new Epic();
        twoTestSubtasks.getFirst().setStatus(TaskStatus.NEW);
        twoTestSubtasks.getLast().setStatus(TaskStatus.NEW);
        epic.setSubtasks(twoTestSubtasks);

        TaskStatus actualStatus = epic.getStatus();

        assertEquals(TaskStatus.NEW, actualStatus, "epic has wrong status");
    }

    @Test
    public void shouldHaveStatusDoneWhenAllSubtasksHaveStatusDone() {
        Epic epic = new Epic();
        twoTestSubtasks.getFirst().setStatus(TaskStatus.DONE);
        twoTestSubtasks.getLast().setStatus(TaskStatus.DONE);
        epic.setSubtasks(twoTestSubtasks);

        TaskStatus actualStatus = epic.getStatus();

        assertEquals(TaskStatus.DONE, actualStatus, "epic has wrong status");
    }

    @Test
    public void shouldHaveStatusInProgressWhenNotAllSubtasksHaveStatusNewNeitherDone() {
        Epic epic = new Epic();
        twoTestSubtasks.getFirst().setStatus(TaskStatus.NEW);
        twoTestSubtasks.getLast().setStatus(TaskStatus.DONE);
        epic.setSubtasks(twoTestSubtasks);

        TaskStatus actualStatus = epic.getStatus();

        assertEquals(TaskStatus.IN_PROGRESS, actualStatus, "epic has wrong status");
    }

    @Test
    public void shouldThrowWhenSetStatus() {
        Epic epic = new Epic();

        Exception exception = assertThrows(UnsupportedOperationException.class, () -> epic.setStatus(TEST_STATUS));
        assertEquals("cannot explicitly set epic status", exception.getMessage(), WRONG_EXCEPTION_MESSAGE);
    }

    @Test
    public void shouldBeEqualWhenEqualIds() {
        Epic epic = createTestEpic(TEST_EPIC_ID, TEST_TITLE, TEST_DESCRIPTION);
        epic.setSubtasks(testSubtasks);
        Epic anotherEpic = new Epic();
        anotherEpic.setId(TEST_EPIC_ID);

        assertEquals(epic, anotherEpic, "epics with same id must be considered equal");
    }

    @Test
    public void shouldNotBeEqualWhenNotEqualIds() {
        Epic epic = createTestEpic(TEST_EPIC_ID, TEST_TITLE, TEST_DESCRIPTION);
        epic.setSubtasks(testSubtasks);
        Epic anotherEpic = createTestEpic(ANOTHER_TEST_ID, TEST_TITLE, TEST_DESCRIPTION);
        anotherEpic.setSubtasks(testSubtasks);

        assertNotEquals(epic, anotherEpic, "epics with different ids may not considered equal");
    }

    @Test
    public void shouldConvertToStringWhenFieldsNull() {
        String expectedEpicString =
                "Epic{id=null, title=null, description=null, subtasks=[], duration=0, " + "startTime=null, status=NEW}";
        Epic epic = new Epic();

        String actualString = epic.toString();

        assertEquals(expectedEpicString, actualString, "string representation of epic is wrong");
    }

    @Test
    public void shouldConvertToStringWhenFieldsNonNull() {
        String expectedEpicString = String.format("Epic{id=2, title=\"Title\", description.length=11, subtasks=[%s], "
                + "duration=30, startTime=2000-05-01T13:30, status=IN_PROGRESS}", testSubtasks.getFirst());
        Epic epic = createTestEpic(TEST_EPIC_ID, TEST_TITLE, TEST_DESCRIPTION);
        epic.setSubtasks(testSubtasks);

        String actualString = epic.toString();

        assertEquals(expectedEpicString, actualString, "string representation of epic is wrong");
    }
}