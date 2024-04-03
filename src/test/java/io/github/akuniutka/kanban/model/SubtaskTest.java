package io.github.akuniutka.kanban.model;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import static org.junit.jupiter.api.Assertions.*;

class SubtaskTest {
    @Test
    public void shouldCreateSubtask() {
        Subtask subtask = new Subtask();
        assertNotNull(subtask, "subtask was not created");
    }

    @Test
    public void shouldHaveIdOfIntegerType() {
        long id = 1L;
        Subtask subtask = new Subtask();

        subtask.setId(id);
        long actualId = subtask.getId();

        assertEquals(id, actualId, "subtask has wrong id");
    }

    @Test
    public void shouldKeepEpicId() {
        long epicId = 1L;
        Subtask subtask = new Subtask();

        subtask.setEpicId(epicId);
        long actualEpicId = subtask.getEpicId();

        assertEquals(epicId, actualEpicId, "subtask has wrong epic id");
    }

    @Test
    public void shouldHaveTitle() {
        String title = "Title";
        Subtask subtask = new Subtask();

        subtask.setTitle(title);
        String actualTitle = subtask.getTitle();

        assertEquals(title, actualTitle, "subtask has wrong title");
    }

    @Test
    public void shouldHaveDescription() {
        String description = "Description";
        Subtask subtask = new Subtask();

        subtask.setDescription(description);
        String actualDescription = subtask.getDescription();

        assertEquals(description, actualDescription, "subtask has wrong description");
    }

    @ParameterizedTest
    @EnumSource(TaskStatus.class)
    public void shouldSupportAllStatuses(TaskStatus status) {
        Subtask subtask = new Subtask();

        subtask.setStatus(status);
        TaskStatus actualStatus = subtask.getStatus();

        assertEquals(status, actualStatus, "subtask has wrong status");
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

        assertEquals(subtask, anotherSubtask, "subtasks with same id must be considered equal");
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

        assertNotEquals(subtask, anotherSubtask, "tasks with different ids may not considered equal");
    }
}