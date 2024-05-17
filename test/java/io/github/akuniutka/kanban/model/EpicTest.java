package io.github.akuniutka.kanban.model;

import org.junit.jupiter.api.Test;

import java.util.List;

import static io.github.akuniutka.kanban.TestModels.*;
import static org.junit.jupiter.api.Assertions.*;

class EpicTest {
    @Test
    public void shouldCreateEpic() {
        Epic epic = new Epic();
        assertNotNull(epic, "epic was not created");
    }

    @Test
    public void shouldHaveIdOfIntegerType() {
        Epic epic = new Epic();

        epic.setId(TEST_EPIC_ID);
        long actualId = epic.getId();

        assertEquals(TEST_EPIC_ID, actualId, "epic has wrong id");
    }

    @Test
    public void shouldHaveCorrectType() {
        Epic epic = new Epic();

        assertEquals(TaskType.EPIC, epic.getType(), "task has wrong type");
    }

    @Test
    public void shouldHaveTitle() {
        Epic epic = new Epic();

        epic.setTitle(TEST_TITLE);
        String actualTitle = epic.getTitle();

        assertEquals(TEST_TITLE, actualTitle, "epic has wrong title");
    }

    @Test
    public void shouldHaveDescription() {
        Epic epic = new Epic();

        epic.setDescription(TEST_DESCRIPTION);
        String actualDescription = epic.getDescription();

        assertEquals(TEST_DESCRIPTION, actualDescription, "epic has wrong description");
    }

    @Test
    public void shouldKeepSubtaskIds() {
        Epic epic = new Epic();

        epic.setSubtaskIds(TEST_SUBTASK_IDS);
        List<Long> actualSubtaskIds = epic.getSubtaskIds();

        assertEquals(TEST_SUBTASK_IDS, actualSubtaskIds, "epic has wrong list of subtask ids");
    }

    @Test
    public void shouldHaveStatus() {
        Epic epic = new Epic();

        epic.setStatus(TEST_STATUS);
        TaskStatus actualStatus = epic.getStatus();

        assertEquals(TEST_STATUS, actualStatus, "epic has wrong status");
    }

    @Test
    public void shouldBeEqualWhenEqualIds() {
        Epic epic = new Epic();
        epic.setId(TEST_EPIC_ID);
        epic.setTitle(TEST_TITLE);
        epic.setDescription(TEST_DESCRIPTION);
        epic.setStatus(TEST_STATUS);
        Epic anotherEpic = new Epic();
        anotherEpic.setId(TEST_EPIC_ID);

        assertEquals(epic, anotherEpic, "epics with same id must be considered equal");
    }

    @Test
    public void shouldNotBeEqualWhenNotEqualIds() {
        Epic epic = new Epic();
        epic.setId(TEST_EPIC_ID);
        epic.setTitle(TEST_TITLE);
        epic.setDescription(TEST_DESCRIPTION);
        epic.setStatus(TEST_STATUS);
        Epic anotherEpic = new Epic();
        anotherEpic.setId(ANOTHER_TEST_ID);
        anotherEpic.setTitle(TEST_TITLE);
        anotherEpic.setDescription(TEST_DESCRIPTION);
        anotherEpic.setStatus(TEST_STATUS);

        assertNotEquals(epic, anotherEpic, "epics with different ids may not considered equal");
    }
}