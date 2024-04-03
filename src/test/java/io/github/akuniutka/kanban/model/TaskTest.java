package io.github.akuniutka.kanban.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class TaskTest {
    @Test
    public void shouldCreateTask() {
        Task task = new Task();
        assertNotNull(task);
    }

    @Test
    public void shouldHaveIdOfIntegerType() {
        long id = 1L;
        Task task = new Task();
        task.setId(id);
        long actualId = task.getId();
        assertEquals(id, actualId);
    }

    @Test
    public void shouldHaveTitle() {
        String title = "Title";
        Task task = new Task();
        task.setTitle(title);
        assertEquals(title, task.getTitle());
    }

    @Test
    public void shouldHaveDescription() {
        String description = "Description";
        Task task = new Task();
        task.setDescription(description);
        assertEquals(description, task.getDescription());
    }

    @Test
    public void shouldSupportStatusNew() {
        Task task = new Task();
        task.setStatus(TaskStatus.NEW);
        assertEquals(TaskStatus.NEW, task.getStatus());
    }

    @Test
    public void shouldSupportStatusInProgress() {
        Task task = new Task();
        task.setStatus(TaskStatus.IN_PROGRESS);
        assertEquals(TaskStatus.IN_PROGRESS, task.getStatus());
    }

    @Test
    public void shouldSupportStatusDone() {
        Task task = new Task();
        task.setStatus(TaskStatus.DONE);
        assertEquals(TaskStatus.DONE, task.getStatus());
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
        assertEquals(task, anotherTask);
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
        assertNotEquals(task, anotherTask);
    }
}
