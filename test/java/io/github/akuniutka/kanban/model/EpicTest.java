package io.github.akuniutka.kanban.model;

import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

import static io.github.akuniutka.kanban.TestModels.*;
import static org.junit.jupiter.api.Assertions.*;

class EpicTest {
    private static final String WRONG_EXCEPTION_MESSAGE = "message for exception is wrong";
    private final List<Subtask> testSubtasks;

    public EpicTest() {
        this.testSubtasks = List.of(fromTestSubtask().build());
    }

    @Test
    public void shouldCreateEpic() {
        final Epic epic = new Epic();
        assertNotNull(epic, "epic was not created");
    }

    @Test
    public void shouldHaveIdOfIntegerType() {
        final Epic epic = new Epic();

        epic.setId(TEST_EPIC_ID);
        final long actualId = epic.getId();

        assertEquals(TEST_EPIC_ID, actualId, "epic has wrong id");
    }

    @Test
    public void shouldHaveCorrectType() {
        final Epic epic = new Epic();

        assertEquals(TaskType.EPIC, epic.getType(), "task has wrong type");
    }

    @Test
    public void shouldHaveTitle() {
        final Epic epic = new Epic();

        epic.setTitle(TEST_TITLE);
        final String actualTitle = epic.getTitle();

        assertEquals(TEST_TITLE, actualTitle, "epic has wrong title");
    }

    @Test
    public void shouldHaveDescription() {
        final Epic epic = new Epic();

        epic.setDescription(TEST_DESCRIPTION);
        final String actualDescription = epic.getDescription();

        assertEquals(TEST_DESCRIPTION, actualDescription, "epic has wrong description");
    }

    @Test
    public void shouldKeepSubtasks() {
        final List<Subtask> expectedSubtasks = List.of(fromTestSubtask().build());
        final Epic epic = new Epic();

        epic.setSubtasks(testSubtasks);
        final List<Subtask> actualSubtasks = epic.getSubtasks();

        assertListEquals(expectedSubtasks, actualSubtasks, "epic has wrong list of subtasks");
    }

    @Test
    public void shouldThrowWhenSubtasksNull() {
        final Epic epic = new Epic();

        final Exception exception = assertThrows(NullPointerException.class, () -> epic.setSubtasks(null));
        assertEquals("list of subtasks cannot be null", exception.getMessage(), WRONG_EXCEPTION_MESSAGE);
    }

    @Test
    public void shouldHaveDuration() {
        final Epic epic = new Epic();

        epic.setDuration(TEST_DURATION);
        final Duration actualDuration = epic.getDuration();

        assertEquals(TEST_DURATION, actualDuration, "epic has wrong duration");
    }

    @Test
    public void shouldAcceptNullDuration() {
        final Epic epic = new Epic();

        epic.setDuration(null);
        final Duration actualDuration = epic.getDuration();

        assertNull(actualDuration, "epic duration should be null");
    }

    @Test
    public void shouldHaveStartTime() {
        final Epic epic = new Epic();

        epic.setStartTime(TEST_START_TIME);
        final LocalDateTime actualStartTime = epic.getStartTime();

        assertEquals(TEST_START_TIME, actualStartTime, "epic has wrong start time");
    }

    @Test
    public void shouldAcceptNullStartTime() {
        final Epic epic = new Epic();

        epic.setStartTime(null);
        final LocalDateTime actualStartTime = epic.getStartTime();

        assertNull(actualStartTime, "epic start time should be null");
    }

    @Test
    public void shouldHaveEndTime() {
        final Epic epic = new Epic();

        epic.setEndTime(TEST_END_TIME);
        final LocalDateTime actualEndTime = epic.getEndTime();

        assertEquals(TEST_END_TIME, actualEndTime, "epic has wrong end time");
    }

    @Test
    public void shouldAcceptNullEndTime() {
        final Epic epic = new Epic();

        epic.setEndTime(null);
        final LocalDateTime actualEndTime = epic.getEndTime();

        assertNull(actualEndTime, "epic end time should be null");
    }

    @Test
    public void shouldHaveStatusNewWhenNoSubtasks() {
        final Epic epic = new Epic();

        final TaskStatus actualStatus = epic.getStatus();

        assertEquals(TaskStatus.NEW, actualStatus, "epic has wrong status");
    }

    @Test
    public void shouldHaveStatusNewWhenAllSubtasksHaveStatusNew() {
        final Epic epic = new Epic();
        final List<Subtask> subtasks = List.of(fromTestSubtask().withStatus(TaskStatus.NEW).build(),
                fromModifiedSubtask().withStatus(TaskStatus.NEW).build());
        epic.setSubtasks(subtasks);

        final TaskStatus actualStatus = epic.getStatus();

        assertEquals(TaskStatus.NEW, actualStatus, "epic has wrong status");
    }

    @Test
    public void shouldHaveStatusDoneWhenAllSubtasksHaveStatusDone() {
        final Epic epic = new Epic();
        final List<Subtask> subtasks = List.of(fromTestSubtask().withStatus(TaskStatus.DONE).build(),
                fromModifiedSubtask().withStatus(TaskStatus.DONE).build());
        epic.setSubtasks(subtasks);

        final TaskStatus actualStatus = epic.getStatus();

        assertEquals(TaskStatus.DONE, actualStatus, "epic has wrong status");
    }

    @Test
    public void shouldHaveStatusInProgressWhenAllSubtasksHaveStatusInProgress() {
        final Epic epic = new Epic();
        final List<Subtask> subtasks = List.of(fromTestSubtask().withStatus(TaskStatus.IN_PROGRESS).build(),
                fromModifiedSubtask().withStatus(TaskStatus.IN_PROGRESS).build());
        epic.setSubtasks(subtasks);

        final TaskStatus actualStatus = epic.getStatus();

        assertEquals(TaskStatus.IN_PROGRESS, actualStatus, "epic has wrong status");
    }

    @Test
    public void shouldHaveStatusInProgressWhenSubtasksHaveStatusNewAndDone() {
        final Epic epic = new Epic();
        final List<Subtask> subtasks = List.of(fromTestSubtask().withStatus(TaskStatus.NEW).build(),
                fromModifiedSubtask().withStatus(TaskStatus.DONE).build());
        epic.setSubtasks(subtasks);

        final TaskStatus actualStatus = epic.getStatus();

        assertEquals(TaskStatus.IN_PROGRESS, actualStatus, "epic has wrong status");
    }

    @Test
    public void shouldHaveStatusInProgressWhenSubtasksHaveStatusNewAndInProgress() {
        final Epic epic = new Epic();
        final List<Subtask> subtasks = List.of(fromTestSubtask().withStatus(TaskStatus.NEW).build(),
                fromModifiedSubtask().withStatus(TaskStatus.IN_PROGRESS).build());
        epic.setSubtasks(subtasks);

        final TaskStatus actualStatus = epic.getStatus();

        assertEquals(TaskStatus.IN_PROGRESS, actualStatus, "epic has wrong status");
    }

    @Test
    public void shouldHaveStatusInProgressWhenSubtasksHaveStatusDoneAndInProgress() {
        final Epic epic = new Epic();
        final List<Subtask> subtasks = List.of(fromTestSubtask().withStatus(TaskStatus.DONE).build(),
                fromModifiedSubtask().withStatus(TaskStatus.IN_PROGRESS).build());
        epic.setSubtasks(subtasks);

        final TaskStatus actualStatus = epic.getStatus();

        assertEquals(TaskStatus.IN_PROGRESS, actualStatus, "epic has wrong status");
    }

    @Test
    public void shouldHaveStatusInProgressWhenSubtasksHaveStatusNewAndDoneAndInProgress() {
        final Epic epic = new Epic();
        final List<Subtask> subtasks = List.of(fromTestSubtask().withStatus(TaskStatus.NEW).build(),
                fromModifiedSubtask().withStatus(TaskStatus.DONE).build(),
                fromEmptySubtask().withStatus(TaskStatus.IN_PROGRESS).build());
        epic.setSubtasks(subtasks);

        final TaskStatus actualStatus = epic.getStatus();

        assertEquals(TaskStatus.IN_PROGRESS, actualStatus, "epic has wrong status");
    }

    @Test
    public void shouldThrowWhenSetStatus() {
        final Epic epic = new Epic();

        final Exception exception = assertThrows(UnsupportedOperationException.class,
                () -> epic.setStatus(TEST_STATUS));
        assertEquals("cannot explicitly set epic status", exception.getMessage(), WRONG_EXCEPTION_MESSAGE);
    }

    @Test
    public void shouldBeEqualWhenEqualIds() {
        final Epic epic = fromTestEpic().withSubtasks(testSubtasks).build();
        final Epic anotherEpic = fromEmptyEpic().withId(TEST_EPIC_ID).build();

        assertEquals(epic, anotherEpic, "epics with same id must be considered equal");
    }

    @Test
    public void shouldNotBeEqualWhenNotEqualIds() {
        final Epic epic = fromTestEpic().withSubtasks(testSubtasks).build();
        final Epic anotherEpic = fromTestEpic().withId(ANOTHER_TEST_ID).withSubtasks(testSubtasks).build();

        assertNotEquals(epic, anotherEpic, "epics with different ids may not considered equal");
    }

    @Test
    public void shouldConvertToStringWhenFieldsNull() {
        final String expected = """
                Epic{id=null, type=EPIC, title=null, description=null, subtasks=[], duration=null, startTime=null, \
                endTime=null, status=NEW}\
                """;
        final Epic epic = new Epic();

        final String actual = epic.toString();

        assertEquals(expected, actual, "string representation of epic is wrong");
    }

    @Test
    public void shouldConvertToStringWhenFieldsNonNull() {
        final String expectedEpicString = """
                Epic{id=2, type=EPIC, title="Title", description.length=11, subtasks=%s, duration=PT30M, \
                startTime=2000-05-01T13:30, endTime=2000-05-01T14:00, status=IN_PROGRESS}\
                """.formatted(testSubtasks);
        final Epic epic = fromTestEpic().withSubtasks(testSubtasks).withDuration(TEST_DURATION)
                .withStartTime(TEST_START_TIME).withEndTime(TEST_END_TIME).build();

        final String actualString = epic.toString();

        assertEquals(expectedEpicString, actualString, "string representation of epic is wrong");
    }
}
