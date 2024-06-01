package io.github.akuniutka.kanban;

import io.github.akuniutka.kanban.model.*;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public final class TestModels {
    public static final long TEST_TASK_ID = 1L;
    public static final long TEST_EPIC_ID = 2L;
    public static final long TEST_SUBTASK_ID = 3L;
    public static final long ANOTHER_TEST_ID = 1_000L;
    public static final String TEST_TITLE = "Title";
    public static final String TEST_DESCRIPTION = "Description";
    public static final Duration TEST_DURATION = Duration.ofMinutes(30L);
    public static final LocalDateTime TEST_START_TIME = LocalDateTime.of(2000, 5, 1, 13, 30);
    public static final LocalDateTime TEST_END_TIME = LocalDateTime.of(2000, 5, 1, 14, 0);
    public static final TaskStatus TEST_STATUS = TaskStatus.IN_PROGRESS;
    public static final String MODIFIED_TITLE = "Modified Title";
    public static final String MODIFIED_DESCRIPTION = "Modified Description";
    public static final Duration MODIFIED_DURATION = Duration.ofMinutes(90L);
    public static final LocalDateTime MODIFIED_START_TIME = LocalDateTime.of(2000, 5, 1, 15, 0);
    public static final LocalDateTime MODIFIED_END_TIME = LocalDateTime.of(2000, 5, 1, 16, 30);
    public static final TaskStatus MODIFIED_STATUS = TaskStatus.DONE;

    private TestModels() {
    }

    public static TaskBuilder fromEmptyTask() {
        return new TaskBuilder();
    }

    public static TaskBuilder fromTestTask() {
        return new TaskBuilder().withId(TEST_TASK_ID).withTitle(TEST_TITLE).withDescription(TEST_DESCRIPTION)
                .withDuration(TEST_DURATION).withStartTime(TEST_START_TIME).withStatus(TEST_STATUS);
    }

    public static TaskBuilder fromModifiedTask() {
        return new TaskBuilder().withId(TEST_TASK_ID).withTitle(MODIFIED_TITLE).withDescription(MODIFIED_DESCRIPTION)
                .withDuration(MODIFIED_DURATION).withStartTime(MODIFIED_START_TIME).withStatus(MODIFIED_STATUS);
    }

    public static EpicBuilder fromEmptyEpic() {
        return new EpicBuilder().withSubtasks(Collections.emptyList());
    }

    public static EpicBuilder fromTestEpic() {
        return new EpicBuilder().withId(TEST_EPIC_ID).withTitle(TEST_TITLE).withDescription(TEST_DESCRIPTION)
                .withSubtasks(Collections.emptyList());
    }

    public static EpicBuilder fromModifiedEpic() {
        return new EpicBuilder().withId(TEST_EPIC_ID).withTitle(MODIFIED_TITLE).withDescription(MODIFIED_DESCRIPTION)
                .withSubtasks(Collections.emptyList());
    }

    public static SubtaskBuilder fromEmptySubtask() {
        return new SubtaskBuilder();
    }

    public static SubtaskBuilder fromTestSubtask() {
        return new SubtaskBuilder().withId(TEST_SUBTASK_ID).withEpicId(TEST_EPIC_ID).withTitle(TEST_TITLE)
                .withDescription(TEST_DESCRIPTION).withDuration(TEST_DURATION).withStartTime(TEST_START_TIME)
                .withStatus(TEST_STATUS);
    }

    public static SubtaskBuilder fromModifiedSubtask() {
        return new SubtaskBuilder().withId(TEST_SUBTASK_ID).withEpicId(TEST_EPIC_ID).withTitle(MODIFIED_TITLE)
                .withDescription(MODIFIED_DESCRIPTION).withDuration(MODIFIED_DURATION)
                .withStartTime(MODIFIED_START_TIME).withStatus(MODIFIED_STATUS);
    }

    public static void assertTaskEquals(Task expected, Task actual, String message) {
        if (expected == null) {
            throw new IllegalArgumentException("value to check against should not be null");
        }
        assertNotNull(actual, "should be not null");
        assertEquals(expected.getType(), actual.getType(), "wrong type");
        assertAll(message,
                () -> assertEquals(expected.getId(), actual.getId(), "wrong id"),
                () -> assertEquals(expected.getTitle(), actual.getTitle(), "wrong title"),
                () -> assertEquals(expected.getDescription(), actual.getDescription(), "wrong description"),
                () -> assertEquals(expected.getDuration(), actual.getDuration(), "wrong duration"),
                () -> assertEquals(expected.getStartTime(), actual.getStartTime(), "wrong start time"),
                () -> assertEquals(expected.getEndTime(), actual.getEndTime(), "wrong end time"),
                () -> assertEquals(expected.getStatus(), actual.getStatus(), "wrong status"),
                () -> {
                    if (expected.getType() == TaskType.SUBTASK) {
                        assertEquals(((Subtask) expected).getEpicId(), ((Subtask) actual).getEpicId(), "wrong epic id");
                    }
                },
                () -> {
                    if (expected.getType() == TaskType.EPIC) {
                        assert expected instanceof Epic;
                        assertListEquals(((Epic) expected).getSubtasks(), ((Epic) actual).getSubtasks(),
                                "wrong subtask list");
                    }
                });
    }

    public static void assertListEquals(List<? extends Task> expected, List<? extends Task> actual, String message) {
        if (expected == null) {
            throw new IllegalArgumentException("value to check against should not be null");
        }
        assertNotNull(actual, "should be not null");
        assertEquals(expected.size(), actual.size(), "wrong list size");
        for (int i = 0; i < expected.size(); i++) {
            assertTaskEquals(expected.get(i), actual.get(i), message);
        }
    }
}
