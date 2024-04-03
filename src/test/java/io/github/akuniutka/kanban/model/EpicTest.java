package io.github.akuniutka.kanban.model;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class EpicTest {
    @Test
    public void shouldCreateEpic() {
        Epic epic = new Epic();
        assertNotNull(epic);
    }

    @Test
    public void shouldHaveIdOfIntegerType() {
        long id = 1L;
        Epic epic = new Epic();
        epic.setId(id);
        long actualId = epic.getId();
        assertEquals(id, actualId);
    }

    @Test
    public void shouldHaveTitle() {
        String title = "Title";
        Epic epic = new Epic();
        epic.setTitle(title);
        assertEquals(title, epic.getTitle());
    }

    @Test
    public void shouldHaveDescription() {
        String description = "Description";
        Epic epic = new Epic();
        epic.setDescription(description);
        assertEquals(description, epic.getDescription());
    }

    @Test
    public void shouldKeepSubtaskIds() {
        List<Long> expected = new ArrayList<>();
        expected.add(5L);
        expected.add(3L);
        Epic epic = new Epic();
        epic.addSubtaskId(5L);
        epic.addSubtaskId(3L);
        assertEquals(expected, epic.getSubtaskIds());
    }

    @Test
    public void shouldDropSubtaskId() {
        List<Long> expected = new ArrayList<>();
        expected.add(3L);
        Epic epic = new Epic();
        epic.addSubtaskId(5L);
        epic.addSubtaskId(3L);
        epic.removeSubtaskId(5L);
        assertEquals(expected, epic.getSubtaskIds());
    }

    @Test
    public void shouldSupportStatusNew() {
        Epic epic = new Epic();
        epic.setStatus(TaskStatus.NEW);
        assertEquals(TaskStatus.NEW, epic.getStatus());
    }

    @Test
    public void shouldSupportStatusInProgress() {
        Epic epic = new Epic();
        epic.setStatus(TaskStatus.IN_PROGRESS);
        assertEquals(TaskStatus.IN_PROGRESS, epic.getStatus());
    }

    @Test
    public void shouldSupportStatusDone() {
        Epic epic = new Epic();
        epic.setStatus(TaskStatus.DONE);
        assertEquals(TaskStatus.DONE, epic.getStatus());
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
        assertEquals(epic, anotherEpic);
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
        assertNotEquals(epic, anotherEpic);
    }
}