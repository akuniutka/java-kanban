package io.github.akuniutka.kanban;

import io.github.akuniutka.kanban.model.Epic;
import io.github.akuniutka.kanban.model.Subtask;
import io.github.akuniutka.kanban.model.Task;
import io.github.akuniutka.kanban.model.TaskStatus;

import java.util.List;

public final class TestModels {
    public static final String TEST_TITLE = "Title";
    public static final String TEST_DESCRIPTION = "Description";
    public static final TaskStatus TEST_STATUS = TaskStatus.IN_PROGRESS;
    public static final String MODIFIED_TEST_TITLE = "Modified Title";
    public static final String MODIFIED_TEST_DESCRIPTION = "Modified description";
    public static final TaskStatus MODIFIED_TEST_STATUS = TaskStatus.DONE;
    public static final long TEST_TASK_ID = 1L;
    public static final long TEST_EPIC_ID = 2L;
    public static final long TEST_SUBTASK_ID = 3L;
    public static final long ANOTHER_TEST_ID = 1_000L;
    public static final List<Long> TEST_SUBTASK_IDS = List.of(4L, 5L, 6L);

    private TestModels() {
    }

    public static Task createTestTask() {
        return createTestTask(null, null, null);
    }

    public static Task createTestTask(String title, String description, TaskStatus taskStatus) {
        return createTestTask(null, title, description, taskStatus);
    }

    public static Task createTestTask(Long id, String title, String description, TaskStatus taskStatus) {
        final Task task = new Task();
        if (id != null) {
            task.setId(id);
        }
        task.setTitle(title);
        task.setDescription(description);
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
        return createTestSubtask(null, null, null);
    }

    public static Subtask createTestSubtask(String title, String description, TaskStatus taskStatus) {
        return createTestSubtask(null, title, description, taskStatus);
    }

    public static Subtask createTestSubtask(Long epicId, String title, String description, TaskStatus taskStatus) {
        return createTestSubtask(null, epicId, title, description, taskStatus);
    }

    public static Subtask createTestSubtask(Long id, Long epicId, String title, String description,
            TaskStatus taskStatus) {
        final Subtask subtask = new Subtask();
        if (id != null) {
            subtask.setId(id);
        }
        if (epicId != null) {
            subtask.setEpicId(epicId);
        }
        subtask.setTitle(title);
        subtask.setDescription(description);
        subtask.setStatus(taskStatus);
        return subtask;
    }
}
