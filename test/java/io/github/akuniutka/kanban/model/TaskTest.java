package io.github.akuniutka.kanban.model;

import org.junit.jupiter.api.Test;

import static io.github.akuniutka.kanban.TestModels.*;
import static org.junit.jupiter.api.Assertions.*;

public class TaskTest {
    @Test
    public void shouldCreateTask() {
        Task task = new Task();
        assertNotNull(task, "task was not created");
    }

    @Test
    public void shouldHaveIdOfIntegerType() {
        Task task = new Task();

        task.setId(TEST_TASK_ID);
        long actualId = task.getId();

        assertEquals(TEST_TASK_ID, actualId, "task has wrong id");
    }

    @Test
    public void shouldHaveCorrectType() {
        Task task = new Task();

        assertEquals(TaskType.TASK, task.getType(), "task has wrong type");
    }

    @Test
    public void shouldHaveTitle() {
        Task task = new Task();

        task.setTitle(TEST_TITLE);
        String actualTitle = task.getTitle();

        assertEquals(TEST_TITLE, actualTitle, "task has wrong title");
    }

    @Test
    public void shouldHaveDescription() {
        Task task = new Task();

        task.setDescription(TEST_DESCRIPTION);
        String actualDescription = task.getDescription();

        assertEquals(TEST_DESCRIPTION, actualDescription, "task has wrong description");
    }

    @Test
    public void shouldHaveStatus() {
        Task task = new Task();

        task.setStatus(TEST_STATUS);
        TaskStatus actualStatus = task.getStatus();

        assertEquals(TEST_STATUS, actualStatus, "task has wrong status");
    }

    @Test
    public void shouldBeEqualWhenEqualIds() {
        Task task = new Task();
        task.setId(TEST_TASK_ID);
        task.setTitle(TEST_TITLE);
        task.setDescription(TEST_DESCRIPTION);
        task.setStatus(TEST_STATUS);
        Task anotherTask = new Task();
        anotherTask.setId(TEST_TASK_ID);

        assertEquals(task, anotherTask, "tasks with same id must be considered equal");
    }

    @Test
    public void shouldNotBeEqualWhenNotEqualIds() {
        Task task = new Task();
        task.setId(TEST_TASK_ID);
        task.setTitle(TEST_TITLE);
        task.setDescription(TEST_DESCRIPTION);
        task.setStatus(TEST_STATUS);
        Task anotherTask = new Task();
        anotherTask.setId(ANOTHER_TEST_ID);
        anotherTask.setTitle(TEST_TITLE);
        anotherTask.setDescription(TEST_DESCRIPTION);
        anotherTask.setStatus(TEST_STATUS);

        assertNotEquals(task, anotherTask, "tasks with different ids may not considered equal");
    }
}
