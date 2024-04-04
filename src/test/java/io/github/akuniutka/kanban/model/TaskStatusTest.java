package io.github.akuniutka.kanban.model;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

class TaskStatusTest {
    @Test
    public void shouldSupportThreeStatuses() {
        assertEquals(3, TaskStatus.values().length);
    }

    @ParameterizedTest
    @ValueSource(strings = {"NEW", "IN_PROGRESS", "DONE"})
    public void shouldSupportStatusesRequired(String status) {
        assertNotNull(TaskStatus.valueOf(status));
    }
}