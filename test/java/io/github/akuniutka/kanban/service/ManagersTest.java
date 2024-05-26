package io.github.akuniutka.kanban.service;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ManagersTest {
    @Test
    public void shouldReturnInMemoryTaskManagerByDefault() {
        TaskManager manager = Managers.getDefault();
        assertNotNull(manager, "task manager was not created");
        assertInstanceOf(InMemoryTaskManager.class, manager, "wrong class of task manager");
    }

    @Test
    public void shouldReturnInMemoryHistoryManagerByDefault() {
        HistoryManager manager = Managers.getDefaultHistory();
        assertNotNull(manager, "history manager was not created");
        assertInstanceOf(InMemoryHistoryManager.class, manager, "wrong class of history manager");
    }
}
