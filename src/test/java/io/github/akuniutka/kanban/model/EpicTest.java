package io.github.akuniutka.kanban.model;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class EpicTest {
    @Test
    public void shouldCreateEpic() {
        Epic epic = new Epic();
        assertNotNull(epic, "epic was not created");
    }

    @Test
    public void shouldHaveIdOfIntegerType() {
        long id = 1L;
        Epic epic = new Epic();

        epic.setId(id);
        long actualId = epic.getId();

        assertEquals(id, actualId, "epic has wrong id");
    }

    @Test
    public void shouldHaveTitle() {
        String title = "Title";
        Epic epic = new Epic();

        epic.setTitle(title);
        String actualTitle = epic.getTitle();

        assertEquals(title, actualTitle, "epic has wrong title");
    }

    @Test
    public void shouldHaveDescription() {
        String description = "Description";
        Epic epic = new Epic();

        epic.setDescription(description);
        String actualDescription = epic.getDescription();

        assertEquals(description, actualDescription, "epic has wrong description");
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
    public void shouldDropSubtaskId() {
        List<Long> expectedIds = new ArrayList<>();
        expectedIds.add(3L);
        Epic epic = new Epic();

        epic.addSubtaskId(5L);
        epic.addSubtaskId(3L);
        epic.removeSubtaskId(5L);
        List<Long> actualIds = epic.getSubtaskIds();

        assertEquals(expectedIds, actualIds, "epic has wrong list of subtask ids");
    }

    @ParameterizedTest
    @EnumSource(TaskStatus.class)
    public void shouldSupportAllStatuses(TaskStatus status) {
        Epic epic = new Epic();

        epic.setStatus(status);
        TaskStatus actualStatus = epic.getStatus();

        assertEquals(status, actualStatus, "epic has wrong status");
    }

    @Test
    public void shouldBeEqualWhenEqualIds() {
        long id = 1L;
        Epic epic = new Epic();
        epic.setId(id);
        epic.setTitle("Title");
        epic.setDescription("Description");
        epic.setStatus(TaskStatus.IN_PROGRESS);
        Epic anotherEpic = new Epic();
        anotherEpic.setId(id);

        assertEquals(epic, anotherEpic, "epics with same id must be considered equal");
    }

    @Test
    public void shouldNotBeEqualWhenNotEqualIds() {
        long id = 1L;
        long anotherId = 2L;
        String title = "Title";
        String description = "Description";
        TaskStatus status = TaskStatus.IN_PROGRESS;
        Epic epic = new Epic();
        epic.setId(id);
        epic.setTitle(title);
        epic.setDescription(description);
        epic.setStatus(status);
        Epic anotherEpic = new Epic();
        epic.setId(anotherId);
        epic.setTitle(title);
        epic.setDescription(description);
        epic.setStatus(status);

        assertNotEquals(epic, anotherEpic, "epics with different ids may not considered equal");
    }
}