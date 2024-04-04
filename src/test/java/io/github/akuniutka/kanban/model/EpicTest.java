package io.github.akuniutka.kanban.model;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class EpicTest {
    private static final long TEST_ID = 1L;
    private static final long ANOTHER_TEST_ID = 2L;
    private static final String TEST_TITLE = "Title";
    private static final String TEST_DESCRIPTION = "Description";
    private static final TaskStatus TEST_STATUS = TaskStatus.IN_PROGRESS;

    @Test
    public void shouldCreateEpic() {
        Epic epic = new Epic();
        assertNotNull(epic, "epic was not created");
    }

    @Test
    public void shouldHaveIdOfIntegerType() {
        Epic epic = new Epic();

        epic.setId(TEST_ID);
        long actualId = epic.getId();

        assertEquals(TEST_ID, actualId, "epic has wrong id");
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
        List<Long> expectedIds = new ArrayList<>();
        expectedIds.add(5L);
        expectedIds.add(3L);
        Epic epic = new Epic();

        epic.addSubtaskId(5L);
        epic.addSubtaskId(3L);
        List<Long> actualIds = epic.getSubtaskIds();

        assertEquals(expectedIds, actualIds, "epic has wrong list of subtask ids");
    }

    @Test
    public void shouldDoNothingWhenAlreadyContainsSubtaskIdBeingAdded() {
        List<Long> expectedIds = new ArrayList<>();
        expectedIds.add(5L);
        expectedIds.add(3L);
        Epic epic = new Epic();

        epic.addSubtaskId(5L);
        epic.addSubtaskId(3L);
        epic.addSubtaskId(3L);
        List<Long> actualIds = epic.getSubtaskIds();

        assertEquals(expectedIds, actualIds, "epic has wrong list of subtask ids");
    }

    @Test
    public void shouldDropSubtaskIdWhenContainsIt() {
        List<Long> expectedIds = new ArrayList<>();
        expectedIds.add(3L);
        Epic epic = new Epic();

        epic.addSubtaskId(5L);
        epic.addSubtaskId(3L);
        epic.removeSubtaskId(5L);
        List<Long> actualIds = epic.getSubtaskIds();

        assertEquals(expectedIds, actualIds, "epic has wrong list of subtask ids");
    }

    @Test
    public void shouldDoNothingWhenDoesNotContainSubtaskIdBeingDropped() {
        List<Long> expectedIds = new ArrayList<>();
        expectedIds.add(5L);
        expectedIds.add(3L);
        Epic epic = new Epic();

        epic.addSubtaskId(5L);
        epic.addSubtaskId(3L);
        epic.removeSubtaskId(4L);
        List<Long> actualIds = epic.getSubtaskIds();

        assertEquals(expectedIds, actualIds, "epic has wrong list of subtask ids");
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
        epic.setId(TEST_ID);
        epic.setTitle(TEST_TITLE);
        epic.setDescription(TEST_DESCRIPTION);
        epic.setStatus(TEST_STATUS);
        Epic anotherEpic = new Epic();
        anotherEpic.setId(TEST_ID);

        assertEquals(epic, anotherEpic, "epics with same id must be considered equal");
    }

    @Test
    public void shouldNotBeEqualWhenNotEqualIds() {
        Epic epic = new Epic();
        epic.setId(TEST_ID);
        epic.setTitle(TEST_TITLE);
        epic.setDescription(TEST_DESCRIPTION);
        epic.setStatus(TEST_STATUS);
        Epic anotherEpic = new Epic();
        epic.setId(ANOTHER_TEST_ID);
        epic.setTitle(TEST_TITLE);
        epic.setDescription(TEST_DESCRIPTION);
        epic.setStatus(TEST_STATUS);

        assertNotEquals(epic, anotherEpic, "epics with different ids may not considered equal");
    }
}