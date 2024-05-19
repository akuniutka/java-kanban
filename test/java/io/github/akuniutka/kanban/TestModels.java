package io.github.akuniutka.kanban;

import io.github.akuniutka.kanban.model.Epic;
import io.github.akuniutka.kanban.model.Subtask;
import io.github.akuniutka.kanban.model.Task;
import io.github.akuniutka.kanban.model.TaskStatus;

import java.time.LocalDateTime;
import java.util.List;

public final class TestModels {
    public static final String TEST_TITLE = "Title";
    public static final String TEST_DESCRIPTION = "Description";
    public static final long TEST_DURATION = 30L;
    public static final LocalDateTime TEST_START_TIME = LocalDateTime.of(2000, 5, 1, 13, 30);
    public static final LocalDateTime TEST_END_TIME = LocalDateTime.of(2000, 5, 1, 14, 0);
    public static final TaskStatus TEST_STATUS = TaskStatus.IN_PROGRESS;
    public static final String MODIFIED_TEST_TITLE = "Modified Title";
    public static final String MODIFIED_TEST_DESCRIPTION = "Modified description";
    public static final long MODIFIED_TEST_DURATION = 90L;
    public static final long MODIFIED_TEST_EPIC_DURATION = 180L;
    public static final LocalDateTime MODIFIED_TEST_START_TIME = LocalDateTime.of(2000, 5, 1, 15, 0);
    public static final TaskStatus MODIFIED_TEST_STATUS = TaskStatus.DONE;
    public static final long TEST_TASK_ID = 1L;
    public static final long TEST_EPIC_ID = 2L;
    public static final long TEST_SUBTASK_ID = 3L;
    public static final long ANOTHER_TEST_ID = 1_000L;
    public static final List<Long> TEST_SUBTASK_IDS = List.of(4L, 5L, 6L);

    private TestModels() {
    }

    public static Task createTestTask() {
        return createTestTask(null, null, 0, null, null);
    }

    public static Task createTestTask(String title, String description, long duration, LocalDateTime startTime,
            TaskStatus taskStatus) {
        return createTestTask(null, title, description, duration, startTime, taskStatus);
    }

    public static Task createTestTask(Long id, String title, String description, long duration, LocalDateTime startTime,
            TaskStatus taskStatus) {
        final Task task = new Task();
        if (id != null) {
            task.setId(id);
        }
        task.setTitle(title);
        task.setDescription(description);
        task.setDuration(duration);
        task.setStartTime(startTime);
        task.setStatus(taskStatus);
        return task;
    }

    public static Epic createTestEpic() {
        return createTestEpic(null, null);
    }

    public static Epic createTestEpic(String title, String description) {
        return createTestEpic(null, title, description);
    }

    public static Epic createTestEpic(Long id, String title, String description) {
        final Epic epic = new Epic();
        if (id != null) {
            epic.setId(id);
        }
        epic.setTitle(title);
        epic.setDescription(description);
        return epic;
    }

    public static Subtask createTestSubtask() {
        return createTestSubtask(null, null, 0, null, null);
    }

    public static Subtask createTestSubtask(String title, String description, long duration, LocalDateTime startTime,
            TaskStatus taskStatus) {
        return createTestSubtask(null, title, description, duration, startTime, taskStatus);
    }

    public static Subtask createTestSubtask(Long epicId, String title, String description, long duration,
            LocalDateTime startTime, TaskStatus taskStatus) {
        return createTestSubtask(null, epicId, title, description, duration, startTime, taskStatus);
    }

    public static Subtask createTestSubtask(Long id, Long epicId, String title, String description, long duration,
            LocalDateTime startTime, TaskStatus taskStatus) {
        final Subtask subtask = new Subtask();
        if (id != null) {
            subtask.setId(id);
        }
        if (epicId != null) {
            subtask.setEpicId(epicId);
        }
        subtask.setTitle(title);
        subtask.setDescription(description);
        subtask.setDuration(duration);
        subtask.setStartTime(startTime);
        subtask.setStatus(taskStatus);
        return subtask;
    }
}
