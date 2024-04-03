package io.github.akuniutka.kanban.model;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import static org.junit.jupiter.api.Assertions.*;

public class TaskTest {
    @Test
    public void shouldCreateTask() {
        Task task = new Task();
        assertNotNull(task, "task was not created");
    }

    @Test
    public void shouldHaveIdOfIntegerType() {
        long id = 1L;
        Task task = new Task();

        task.setId(id);
        long actualId = task.getId();

        assertEquals(id, actualId, "task has wrong id");
    }

    @Test
    public void shouldHaveTitle() {
        String title = "Title";
        Task task = new Task();

        task.setTitle(title);
        String actualTitle = task.getTitle();

        assertEquals(title, actualTitle, "task has wrong title");
    }

    @Test
    public void shouldHaveDescription() {
        String description = "Description";
        Task task = new Task();

        task.setDescription(description);
        String actualDescription = task.getDescription();

        assertEquals(description, actualDescription, "task has wrong description");
    }

    @ParameterizedTest
    @EnumSource(TaskStatus.class)
    public void shouldSupportAllStatuses(TaskStatus status) {
        Task task = new Task();

        task.setStatus(status);
        TaskStatus actualStatus = task.getStatus();

        assertEquals(status, actualStatus, "task has wrong status");
    }

    @Test
    public void shouldBeEqualWhenEqualIds() {
        long id = 1L;
        Task task = new Task();
        task.setId(id);
        task.setTitle("Title");
        task.setDescription("Description");
        task.setStatus(TaskStatus.IN_PROGRESS);
        Task anotherTask = new Task();
        anotherTask.setId(id);

        assertEquals(task, anotherTask, "tasks with same id must be considered equal");
    }

    @Test
    public void shouldNotBeEqualWhenNotEqualIds() {
        long id = 1L;
        long anotherId = 2L;
        String title = "Title";
        String description = "Description";
        TaskStatus status = TaskStatus.IN_PROGRESS;
        Task task = new Task();
        task.setId(id);
        task.setTitle(title);
        task.setDescription(description);
        task.setStatus(status);
        Task anotherTask = new Task();
        anotherTask.setId(anotherId);
        anotherTask.setTitle(title);
        anotherTask.setDescription(description);
        anotherTask.setStatus(status);

        assertNotEquals(task, anotherTask, "tasks with different ids may not considered equal");
    }
}
