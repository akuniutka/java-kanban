package io.github.akuniutka.kanban.service;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class InMemoryTaskManagerTest extends AbstractTaskManagerTest {
    public InMemoryTaskManagerTest() {
        this.manager = new InMemoryTaskManager(this.historyManager);
    }

    @Test
    public void shouldCreateInMemoryTaskManagerOfInterfaceType() {
        assertNotNull(manager, "task manager was not created");
    }

    @Test
    public void shouldThrowWhenHistoryManagerIsNull() {
        final Exception exception = assertThrows(NullPointerException.class, () -> new InMemoryTaskManager(null));
        assertEquals("cannot start: history manager is null", exception.getMessage(), WRONG_EXCEPTION_MESSAGE);
    }
}
