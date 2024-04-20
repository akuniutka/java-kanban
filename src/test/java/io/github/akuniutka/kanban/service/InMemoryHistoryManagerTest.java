package io.github.akuniutka.kanban.service;

import io.github.akuniutka.kanban.model.Epic;
import io.github.akuniutka.kanban.model.Subtask;
import io.github.akuniutka.kanban.model.Task;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static io.github.akuniutka.kanban.TestModels.*;
import static org.junit.jupiter.api.Assertions.*;

class InMemoryHistoryManagerTest {
    private HistoryManager manager;
    private Task testTask;
    private Task modifiedTestTask;
    private Epic testEpic;
    private Epic modifiedTestEpic;
    private Subtask testSubtask;
    private Subtask modifiedTestSubtask;

    @BeforeEach
    public void setUp() {
        manager = new InMemoryHistoryManager();
        testTask = createTestTask(TEST_TASK_ID, TEST_TITLE, TEST_DESCRIPTION, TEST_STATUS);
        modifiedTestTask = createTestTask(TEST_TASK_ID, MODIFIED_TEST_TITLE, MODIFIED_TEST_DESCRIPTION,
                MODIFIED_TEST_STATUS);
        testEpic = createTestEpic(TEST_EPIC_ID, TEST_TITLE, TEST_DESCRIPTION);
        modifiedTestEpic = createTestEpic(TEST_EPIC_ID, MODIFIED_TEST_TITLE, MODIFIED_TEST_DESCRIPTION);
        testSubtask = createTestSubtask(TEST_SUBTASK_ID, TEST_EPIC_ID, TEST_TITLE, TEST_DESCRIPTION, TEST_STATUS);
        modifiedTestSubtask = createTestSubtask(TEST_SUBTASK_ID, TEST_EPIC_ID, MODIFIED_TEST_TITLE,
                MODIFIED_TEST_DESCRIPTION, MODIFIED_TEST_STATUS);
    }

    @Test
    public void shouldCreateInMemoryHistoryManagerOfInterfaceType() {
        assertNotNull(manager, "history manager was not created");
    }

    @Test
    public void shouldKeepTasks() {
        manager.add(testTask);
        List<Task> tasks = manager.getHistory();

        assertNotNull(tasks, "should return list of tasks");
        assertEquals(1, tasks.size(), "list should contain exactly 1 element");
        assertEquals(Task.class, tasks.getFirst().getClass(), "element in list should be of Task class");
        Task savedTask = tasks.getFirst();
        assertEquals(TEST_TASK_ID, savedTask.getId(), "task id should not change");
        assertEquals(TEST_TITLE, savedTask.getTitle(), "task title should not change");
        assertEquals(TEST_DESCRIPTION, savedTask.getDescription(), "task description should not change");
        assertEquals(TEST_STATUS, savedTask.getStatus(), "task status should not change");
    }

    @Test
    public void shouldKeepLatestTaskVersion() {
        manager.add(testTask);
        manager.add(modifiedTestTask);
        List<Task> tasks = manager.getHistory();

        assertNotNull(tasks, "should return list of tasks");
        assertEquals(1, tasks.size(), "list should contain exactly 1 element");
        assertEquals(Task.class, tasks.getFirst().getClass(), "element in list should be of Task class");
        Task savedTask = tasks.getFirst();
        assertEquals(TEST_TASK_ID, savedTask.getId(), "task id should not change");
        assertEquals(MODIFIED_TEST_TITLE, savedTask.getTitle(), "task title is not actual");
        assertEquals(MODIFIED_TEST_DESCRIPTION, savedTask.getDescription(), "task description is not actual");
        assertEquals(MODIFIED_TEST_STATUS, savedTask.getStatus(), "task status is not actual");
    }

    @Test
    public void shouldAllowTaskWithNullFields() {
        Task emptyTask = createTestTask();
        emptyTask.setId(TEST_TASK_ID);

        manager.add(emptyTask);
        List<Task> tasks = manager.getHistory();

        assertNotNull(tasks, "should return list of tasks");
        assertEquals(1, tasks.size(), "list should contain exactly 1 element");
        assertEquals(Task.class, tasks.getFirst().getClass(), "element in list should be of Task class");
        Task savedTask = tasks.getFirst();
        assertEquals(TEST_TASK_ID, savedTask.getId(), "task id should not change");
        assertNull(savedTask.getTitle(), "task title should not change");
        assertNull(savedTask.getDescription(), "task description should not change");
        assertNull(savedTask.getStatus(), "task status should not change");
    }

    @Test
    public void shouldNotModifyOriginalTask() {
        manager.add(testTask);

        assertEquals(TEST_TASK_ID, testTask.getId(), "task id should not change");
        assertEquals(TEST_TITLE, testTask.getTitle(), "task title should not change");
        assertEquals(TEST_DESCRIPTION, testTask.getDescription(), "task description should not change");
        assertEquals(TEST_STATUS, testTask.getStatus(), "task status should not change");
    }

    @Test
    public void shouldKeepEpics() {
        manager.add(testEpic);
        List<Task> tasks = manager.getHistory();

        assertNotNull(tasks, "should return list of tasks");
        assertEquals(1, tasks.size(), "list should contain exactly 1 element");
        assertEquals(Epic.class, tasks.getFirst().getClass(), "element in list should be of Epic class");
        Epic savedEpic = (Epic) tasks.getFirst();
        assertEquals(TEST_EPIC_ID, savedEpic.getId(), "epic id should not change");
        assertEquals(TEST_TITLE, savedEpic.getTitle(), "epic title should not change");
        assertEquals(TEST_DESCRIPTION, savedEpic.getDescription(), "epic description should not change");
    }

    @Test
    public void shouldKeepLatestEpicVersion() {
        manager.add(testEpic);
        manager.add(modifiedTestEpic);
        List<Task> tasks = manager.getHistory();

        assertNotNull(tasks, "should return list of tasks");
        assertEquals(1, tasks.size(), "list should contain exactly 1 element");
        assertEquals(Epic.class, tasks.getFirst().getClass(), "element in list should be of Epic class");
        Epic savedEpic = (Epic) tasks.getFirst();
        assertEquals(TEST_EPIC_ID, savedEpic.getId(), "epic id should not change");
        assertEquals(MODIFIED_TEST_TITLE, savedEpic.getTitle(), "epic title is not actual");
        assertEquals(MODIFIED_TEST_DESCRIPTION, savedEpic.getDescription(), "epic description is not actual");
    }

    @Test
    public void shouldAllowEpicWithNullFields() {
        Epic emptyEpic = createTestEpic();
        emptyEpic.setId(TEST_EPIC_ID);

        manager.add(emptyEpic);
        List<Task> tasks = manager.getHistory();

        assertNotNull(tasks, "should return list of tasks");
        assertEquals(1, tasks.size(), "list should contain exactly 1 element");
        assertEquals(Epic.class, tasks.getFirst().getClass(), "element in list should be of Epic class");
        Epic savedEpic = (Epic) tasks.getFirst();
        assertEquals(TEST_EPIC_ID, savedEpic.getId(), "epic id should not change");
        assertNull(savedEpic.getTitle(), "epic title should not change");
        assertNull(savedEpic.getDescription(), "epic description should not change");
    }

    @Test
    public void shouldNotModifyOriginalEpic() {
        testEpic.setSubtaskIds(TEST_SUBTASK_IDS);
        testEpic.setStatus(TEST_STATUS);

        manager.add(testEpic);

        assertEquals(TEST_EPIC_ID, testEpic.getId(), "epic id should not change");
        assertEquals(TEST_TITLE, testEpic.getTitle(), "epic title should not change");
        assertEquals(TEST_DESCRIPTION, testEpic.getDescription(), "epic description should not change");
        assertEquals(TEST_SUBTASK_IDS, testEpic.getSubtaskIds(), "list of epic's subtasks should not change");
        assertEquals(TEST_STATUS, testEpic.getStatus(), "epic status should not change");
    }

    @Test
    public void shouldKeepSubtasks() {
        manager.add(testSubtask);
        List<Task> tasks = manager.getHistory();

        assertNotNull(tasks, "should return list of tasks");
        assertEquals(1, tasks.size(), "list should contain exactly 1 element");
        assertEquals(Subtask.class, tasks.getFirst().getClass(), "element in list should be of Subtask class");
        Subtask savedSubtask = (Subtask) tasks.getFirst();
        assertEquals(TEST_SUBTASK_ID, savedSubtask.getId(), "subtask id should not change");
        assertEquals(TEST_EPIC_ID, savedSubtask.getEpicId(), "epic id of status should not change");
        assertEquals(TEST_TITLE, savedSubtask.getTitle(), "subtask title should not change");
        assertEquals(TEST_DESCRIPTION, savedSubtask.getDescription(), "subtask description should not change");
        assertEquals(TEST_STATUS, savedSubtask.getStatus(), "subtask status should not change");
    }

    @Test
    public void shouldKeepLatestSubtaskVersion() {
        manager.add(testSubtask);
        manager.add(modifiedTestSubtask);
        List<Task> tasks = manager.getHistory();

        assertNotNull(tasks, "should return list of tasks");
        assertEquals(1, tasks.size(), "list should contain exactly 1 element");
        assertEquals(Subtask.class, tasks.getFirst().getClass(), "element in list should be of Subtask class");
        Subtask savedSubtask = (Subtask) tasks.getFirst();
        assertEquals(TEST_SUBTASK_ID, savedSubtask.getId(), "subtask id should not change");
        assertEquals(TEST_EPIC_ID, savedSubtask.getEpicId(), "epic id of status should not change");
        assertEquals(MODIFIED_TEST_TITLE, savedSubtask.getTitle(), "subtask title is not actual");
        assertEquals(MODIFIED_TEST_DESCRIPTION, savedSubtask.getDescription(), "subtask description is not actual");
        assertEquals(MODIFIED_TEST_STATUS, savedSubtask.getStatus(), "subtask status is not actual");
    }

    @Test
    public void shouldAllowSubtaskWithNullFields() {
        Subtask emptySubtask = createTestSubtask();
        emptySubtask.setId(TEST_SUBTASK_ID);

        manager.add(emptySubtask);
        List<Task> tasks = manager.getHistory();

        assertNotNull(tasks, "should return list of tasks");
        assertEquals(1, tasks.size(), "list should contain exactly 1 element");
        assertEquals(Subtask.class, tasks.getFirst().getClass(), "element in list should be of Subtask class");
        Subtask savedSubtask = (Subtask) tasks.getFirst();
        assertEquals(TEST_SUBTASK_ID, savedSubtask.getId(), "subtask id should not change");
        assertNull(savedSubtask.getEpicId(), "epic id of status should not change");
        assertNull(savedSubtask.getTitle(), "subtask title should not change");
        assertNull(savedSubtask.getDescription(), "subtask description should not change");
        assertNull(savedSubtask.getStatus(), "subtask status should not change");
    }

    @Test
    public void shouldNotModifyOriginalSubtask() {
        manager.add(testSubtask);

        assertEquals(TEST_SUBTASK_ID, testSubtask.getId(), "subtask id should not change");
        assertEquals(TEST_EPIC_ID, testSubtask.getEpicId(), "epic id of status should not change");
        assertEquals(TEST_TITLE, testSubtask.getTitle(), "subtask title should not change");
        assertEquals(TEST_DESCRIPTION, testSubtask.getDescription(), "subtask description should not change");
        assertEquals(TEST_STATUS, testSubtask.getStatus(), "subtask status should not change");
    }

    @Test
    public void shouldKeepTasksInOrderTheyLastTimeVisited() {
        List<Task> expected = List.of(createTestEpic(), createTestSubtask(), createTestTask());
        expected.getFirst().setId(TEST_EPIC_ID);
        expected.get(1).setId(TEST_SUBTASK_ID);
        expected.getLast().setId(TEST_TASK_ID);

        manager.add(testTask);
        manager.add(testEpic);
        manager.add(testSubtask);
        manager.add(testTask);
        List<Task> actual = manager.getHistory();

        assertEquals(expected, actual, "should be three tasks inn order they last time visited");
    }

    @Test
    public void shouldRemoveTaskFromHistory() {
        manager.add(testTask);
        manager.add(testEpic);

        manager.remove(TEST_TASK_ID);
        List<Task> tasks = manager.getHistory();

        assertNotNull(tasks, "should return list of tasks");
        assertEquals(1, tasks.size(), "list should contain exactly 1 element");
        assertEquals(Epic.class, tasks.getFirst().getClass(), "element in list should be of Epic class");
        Epic savedEpic = (Epic) tasks.getFirst();
        assertEquals(TEST_EPIC_ID, savedEpic.getId(), "epic id should not change");
        assertEquals(TEST_TITLE, savedEpic.getTitle(), "epic title should not change");
        assertEquals(TEST_DESCRIPTION, savedEpic.getDescription(), "epic description should not change");
    }

    @Test
    public void shouldDoNothingWhenRemovingNotExistingTaskFromHistory() {
        manager.add(testEpic);

        manager.remove(TEST_TASK_ID);
        List<Task> tasks = manager.getHistory();

        assertNotNull(tasks, "should return list of tasks");
        assertEquals(1, tasks.size(), "list should contain exactly 1 element");
        assertEquals(Epic.class, tasks.getFirst().getClass(), "element in list should be of Epic class");
        Epic savedEpic = (Epic) tasks.getFirst();
        assertEquals(TEST_EPIC_ID, savedEpic.getId(), "epic id should not change");
        assertEquals(TEST_TITLE, savedEpic.getTitle(), "epic title should not change");
        assertEquals(TEST_DESCRIPTION, savedEpic.getDescription(), "epic description should not change");
    }
}