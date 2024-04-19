package io.github.akuniutka.kanban.service;

import io.github.akuniutka.kanban.model.Epic;
import io.github.akuniutka.kanban.model.Subtask;
import io.github.akuniutka.kanban.model.Task;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class InMemoryHistoryManagerTest {
    private HistoryManager manager;

    @BeforeEach
    public void setUp() {
        manager = new InMemoryHistoryManager();
    }

    @Test
    public void shouldCreateInMemoryHistoryManagerOfInterfaceType() {
        assertNotNull(manager, "history manager was not created");
    }

    @Test
    public void shouldKeepTasksEpicsSubtasks() {
        Task task = new Task();
        task.setId(1L);
        Epic epic = new Epic();
        epic.setId(2L);
        Subtask subtask = new Subtask();
        subtask.setId(3L);
        List<Task> expectedHistory = new ArrayList<>();
        Task expectedTask = new Task();
        expectedTask.setId(1L);
        Epic expectedEpic = new Epic();
        expectedEpic.setId(2L);
        Subtask expectedSubtask = new Subtask();
        expectedSubtask.setId(3L);
        expectedHistory.add(expectedTask);
        expectedHistory.add(expectedEpic);
        expectedHistory.add(expectedSubtask);

        manager.add(task);
        manager.add(epic);
        manager.add(subtask);
        List<Task> actualHistory = manager.getHistory();

        assertEquals(expectedHistory, actualHistory, "incorrect list of tasks returned");
    }
}