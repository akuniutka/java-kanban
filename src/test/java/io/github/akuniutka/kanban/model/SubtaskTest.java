package io.github.akuniutka.kanban.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SubtaskTest {
    @Test
    public void shouldCreateSubtask() {
        Subtask subtask = new Subtask();
        assertNotNull(subtask);
    }

    @Test
    public void shouldHaveIdOfIntegerType() {
        long id = 1L;
        Subtask subtask = new Subtask();
        subtask.setId(id);
        long actualId = subtask.getId();
        assertEquals(id, actualId);
    }

    @Test
    public void shouldKeepEpicId() {
        long epicId = 1L;
        Subtask subtask = new Subtask();
        subtask.setEpicId(epicId);
        assertEquals(epicId, subtask.getEpicId());
    }

    @Test
    public void shouldHaveTitle() {
        String title = "Title";
        Subtask subtask = new Subtask();
        subtask.setTitle(title);
        assertEquals(title, subtask.getTitle());
    }

    @Test
    public void shouldHaveDescription() {
        String description = "Description";
        Subtask subtask = new Subtask();
        subtask.setDescription(description);
        assertEquals(description, subtask.getDescription());
    }

    @Test
    public void shouldSupportStatusNew() {
        Subtask subtask = new Subtask();
        subtask.setStatus(TaskStatus.NEW);
        assertEquals(TaskStatus.NEW, subtask.getStatus());
    }

    @Test
    public void shouldSupportStatusInProgress() {
        Subtask subtask = new Subtask();
        subtask.setStatus(TaskStatus.IN_PROGRESS);
        assertEquals(TaskStatus.IN_PROGRESS, subtask.getStatus());
    }

    @Test
    public void shouldSupportStatusDone() {
        Subtask subtask = new Subtask();
        subtask.setStatus(TaskStatus.DONE);
        assertEquals(TaskStatus.DONE, subtask.getStatus());
    }

    @Test
    public void shouldBeEqualWhenEqualIds() {
        long id = 1L;
        Subtask subtask = new Subtask();
        subtask.setId(id);
        subtask.setEpicId(5L);
        subtask.setTitle("Title");
        subtask.setDescription("Description");
        subtask.setStatus(TaskStatus.IN_PROGRESS);
        Subtask anotherSubtask = new Subtask();
        anotherSubtask.setId(id);
        assertEquals(subtask, anotherSubtask);
    }

    @Test
    public void shouldNotBeEqualWhenNotEqualIds() {
        long id = 1L;
        long anotherId = 2L;
        long epicId = 5L;
        String title = "Title";
        String description = "Description";
        TaskStatus status = TaskStatus.IN_PROGRESS;
        Subtask subtask = new Subtask();
        subtask.setId(id);
        subtask.setEpicId(epicId);
        subtask.setTitle(title);
        subtask.setDescription(description);
        subtask.setStatus(status);
        Subtask anotherSubtask = new Subtask();
        anotherSubtask.setId(anotherId);
        anotherSubtask.setEpicId(epicId);
        anotherSubtask.setTitle(title);
        anotherSubtask.setDescription(description);
        anotherSubtask.setStatus(status);
        assertNotEquals(subtask, anotherSubtask);
    }
}