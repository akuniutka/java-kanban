package io.github.akuniutka.kanban.service;

public class InMemoryTaskManagerTest extends AbstractTaskManagerTest {
    public InMemoryTaskManagerTest() {
        this.manager = new InMemoryTaskManager(this.historyManager);
    }
}
