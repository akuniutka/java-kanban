package io.github.akuniutka.kanban.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SubtaskTest {
    private static final long TEST_ID = 1L;
    private static final long ANOTHER_TEST_ID = 2L;
    private static final long TEST_EPIC_ID = 5L;
    private static final String TEST_TITLE = "Title";
    private static final String TEST_DESCRIPTION = "Description";
    private static final TaskStatus TEST_STATUS = TaskStatus.IN_PROGRESS;

    @Test
    public void shouldCreateSubtask() {
        Subtask subtask = new Subtask();
        assertNotNull(subtask, "subtask was not created");
    }

    @Test
    public void shouldHaveIdOfIntegerType() {
        Subtask subtask = new Subtask();

        subtask.setId(TEST_ID);
        long actualId = subtask.getId();

        assertEquals(TEST_ID, actualId, "subtask has wrong id");
    }

    @Test
    public void shouldHaveCorrectType() {
        Subtask subtask = new Subtask();

        assertEquals(TaskType.SUBTASK, subtask.getType(), "task has wrong type");
    }

    @Test
    public void shouldHaveEpicId() {
        Subtask subtask = new Subtask();

        subtask.setEpicId(TEST_EPIC_ID);
        long actualEpicId = subtask.getEpicId();

        assertEquals(TEST_EPIC_ID, actualEpicId, "subtask has wrong epic id");
    }

    @Test
    public void shouldHaveTitle() {
        Subtask subtask = new Subtask();

        subtask.setTitle(TEST_TITLE);
        String actualTitle = subtask.getTitle();

        assertEquals(TEST_TITLE, actualTitle, "subtask has wrong title");
    }

    @Test
    public void shouldHaveDescription() {
        Subtask subtask = new Subtask();

        subtask.setDescription(TEST_DESCRIPTION);
        String actualDescription = subtask.getDescription();

        assertEquals(TEST_DESCRIPTION, actualDescription, "subtask has wrong description");
    }

    @Test
    public void shouldHaveStatus() {
        Subtask subtask = new Subtask();

        subtask.setStatus(TEST_STATUS);
        TaskStatus actualStatus = subtask.getStatus();

        assertEquals(TEST_STATUS, actualStatus, "subtask has wrong status");
    }

    @Test
    public void shouldBeEqualWhenEqualIds() {
        Subtask subtask = new Subtask();
        subtask.setId(TEST_ID);
        subtask.setEpicId(TEST_EPIC_ID);
        subtask.setTitle(TEST_TITLE);
        subtask.setDescription(TEST_DESCRIPTION);
        subtask.setStatus(TEST_STATUS);
        Subtask anotherSubtask = new Subtask();
        anotherSubtask.setId(TEST_ID);

        assertEquals(subtask, anotherSubtask, "subtasks with same id must be considered equal");
    }

    @Test
    public void shouldNotBeEqualWhenNotEqualIds() {
        Subtask subtask = new Subtask();
        subtask.setId(TEST_ID);
        subtask.setEpicId(TEST_EPIC_ID);
        subtask.setTitle(TEST_TITLE);
        subtask.setDescription(TEST_DESCRIPTION);
        subtask.setStatus(TEST_STATUS);
        Subtask anotherSubtask = new Subtask();
        anotherSubtask.setId(ANOTHER_TEST_ID);
        anotherSubtask.setEpicId(TEST_EPIC_ID);
        anotherSubtask.setTitle(TEST_TITLE);
        anotherSubtask.setDescription(TEST_DESCRIPTION);
        anotherSubtask.setStatus(TEST_STATUS);

        assertNotEquals(subtask, anotherSubtask, "tasks with different ids may not considered equal");
    }
}