package io.github.akuniutka.kanban.service;

import io.github.akuniutka.kanban.model.Epic;
import io.github.akuniutka.kanban.model.Subtask;
import io.github.akuniutka.kanban.model.Task;
import io.github.akuniutka.kanban.model.TaskStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static io.github.akuniutka.kanban.TestModels.*;

class InMemoryTaskManagerTest {
    private static final int OK = 0;
    private static final int WRONG_ARGUMENT = -1;
    private TaskManager manager;
    private HistoryManager historyManager;
    private Task emptyTask;
    private Task testTask;
    private Epic emptyEpic;
    private Epic testEpic;
    private Subtask emptySubtask;
    private Subtask testSubtask;

    @BeforeEach
    public void setUp() {
        historyManager = new InMemoryHistoryManager();
        manager = new InMemoryTaskManager(historyManager);
        emptyTask = createTestTask();
        testTask = createTestTask(TEST_TITLE, TEST_DESCRIPTION, TEST_STATUS);
        emptyEpic = createTestEpic();
        testEpic = createTestEpic(TEST_TITLE, TEST_DESCRIPTION);
        emptySubtask = createTestSubtask();
        testSubtask = createTestSubtask(TEST_TITLE, TEST_DESCRIPTION, TEST_STATUS);
    }

    @Test
    public void shouldCreateInMemoryTaskManagerOfInterfaceType() {
        assertNotNull(manager, "task manager was not created");
    }

    @Test
    public void shouldKeepTasks() {
        long id = manager.addTask(testTask);
        Task savedTask = manager.getTask(id);

        assertNotNull(savedTask, "task not found");
        assertEquals(id, savedTask.getId(), "task id differs from returned by manager class");
        assertEquals(TEST_TITLE, savedTask.getTitle(), "task title changed");
        assertEquals(TEST_DESCRIPTION, savedTask.getDescription(), "task description changed");
        assertEquals(TEST_STATUS, savedTask.getStatus(), "task status changed");
    }

    @Test
    public void shouldNotAddNullTask() {
        long id = manager.addTask(null);

        assertEquals(WRONG_ARGUMENT, id, "null task should not be added");
    }

    @Test
    public void shouldNotOverwriteExistingTaskWhenAddingNewOne() {
        long id = manager.addTask(testTask);
        emptyTask.setId(id);
        long anotherId = manager.addTask(emptyTask);
        Task savedTask = manager.getTask(id);

        assertNotEquals(id, anotherId, "new task should have new id");
        assertEquals(id, savedTask.getId(), "task id changed");
        assertEquals(TEST_TITLE, savedTask.getTitle(), "task title changed");
        assertEquals(TEST_DESCRIPTION, savedTask.getDescription(), "task description changed");
        assertEquals(TEST_STATUS, savedTask.getStatus(), "task status changed");
    }

    @Test
    public void shouldUpdateTask() {
        long id = manager.addTask(emptyTask);
        testTask.setId(id);

        int result = manager.updateTask(testTask);
        Task savedTask = manager.getTask(id);

        assertEquals(OK, result, "should return 0 when processed task update");
        assertEquals(id, savedTask.getId(), "task id changed");
        assertEquals(TEST_TITLE, savedTask.getTitle(), "task title not updated");
        assertEquals(TEST_DESCRIPTION, savedTask.getDescription(), "task description not updated");
        assertEquals(TEST_STATUS, savedTask.getStatus(), "task status not updated");
    }

    @Test
    public void shouldNotUpdateNullTask() {
        int result = manager.updateTask(null);

        assertEquals(WRONG_ARGUMENT, result, "should not process update for null task");
    }

    @Test
    public void shouldNotUpdateTaskWhenIdNotSet() {
        int result = manager.updateTask(testTask);

        assertEquals(WRONG_ARGUMENT, result, "should not process update for task with null id");
    }

    @Test
    public void shouldNotUpdateNonExistingTask() {
        testTask.setId(-1L);

        int result = manager.updateTask(testTask);

        assertEquals(WRONG_ARGUMENT, result, "should not process update for unknown task");
    }

    @Test
    public void shouldRemoveTask() {
        long id = manager.addTask(testTask);

        manager.removeTask(id);
        Task savedTask = manager.getTask(id);

        assertNull(savedTask, "should have no access to removed task");
    }

    @Test
    public void shouldKeepEpics() {
        long id = manager.addEpic(testEpic);
        Epic savedEpic = manager.getEpic(id);

        assertNotNull(savedEpic, "epic not found");
        assertEquals(id, savedEpic.getId(), "epic id differs from returned by manager");
        assertEquals(TEST_TITLE, savedEpic.getTitle(), "epic title changed");
        assertEquals(TEST_DESCRIPTION, savedEpic.getDescription(), "epic description changed");
    }

    @Test
    public void shouldNotAddNullEpic() {
        long id = manager.addEpic(null);

        assertEquals(WRONG_ARGUMENT, id, "null epic should not be added");
    }

    @Test
    public void shouldNotOverwriteExistingEpicWhenAddingNewOne() {
        long id = manager.addEpic(testEpic);
        emptyEpic.setId(id);
        long anotherId = manager.addEpic(emptyEpic);
        Epic savedEpic = manager.getEpic(id);

        assertNotEquals(id, anotherId, "new epic should have new id");
        assertEquals(id, savedEpic.getId(), "epic id changed");
        assertEquals(TEST_TITLE, savedEpic.getTitle(), "epic title changed");
        assertEquals(TEST_DESCRIPTION, savedEpic.getDescription(), "epic description changed");
    }

    @Test
    public void shouldUpdateEpic() {
        long id = manager.addEpic(emptyEpic);
        testEpic.setId(id);

        int result = manager.updateEpic(testEpic);
        Epic savedEpic = manager.getEpic(id);

        assertEquals(OK, result, "should return 0 when processed epic update");
        assertEquals(id, savedEpic.getId(), "epic id changed");
        assertEquals(TEST_TITLE, savedEpic.getTitle(), "epic title not updated");
        assertEquals(TEST_DESCRIPTION, savedEpic.getDescription(), "epic description not updated");
    }

    @Test
    public void shouldNotUpdateNullEpic() {
        int result = manager.updateEpic(null);

        assertEquals(WRONG_ARGUMENT, result, "should not process update for null epic");
    }

    @Test
    public void shouldNotUpdateEpicWhenIdNotSet() {
        int result = manager.updateEpic(testEpic);

        assertEquals(WRONG_ARGUMENT, result, "should not process update for epic with null id");
    }

    @Test
    public void shouldNotUpdateNonExistingEpic() {
        testEpic.setId(-1L);

        int result = manager.updateEpic(testEpic);

        assertEquals(WRONG_ARGUMENT, result, "should not process update for unknown epic");
    }

    @ParameterizedTest
    @EnumSource(TaskStatus.class)
    public void shouldNotChangeEpicStatusByEpicUpdate(TaskStatus status) {
        long id = manager.addEpic(emptyEpic);
        testSubtask.setEpicId(id);
        testSubtask.setStatus(status);
        manager.addSubtask(testSubtask);

        for (TaskStatus newStatus : TaskStatus.values()) {
            Epic epicUpdate = new Epic();
            epicUpdate.setId(id);
            epicUpdate.setStatus(newStatus);

            manager.updateEpic(epicUpdate);
            Epic savedEpic = manager.getEpic(id);

            assertEquals(status, savedEpic.getStatus(), "epic update changed epic status");
        }
    }

    @ParameterizedTest
    @EnumSource(TaskStatus.class)
    public void shouldSetEpicToStatusNewWhenNoSubtasks(TaskStatus status) {
        testEpic.setStatus(status);
        long id = manager.addEpic(testEpic);
        Epic savedEpic = manager.getEpic(id);

        assertEquals(TaskStatus.NEW, savedEpic.getStatus(), "empty epic should have status NEW");
    }

    @ParameterizedTest
    @EnumSource(TaskStatus.class)
    public void shouldSetEpicToStatusNewWhenNoMoreSubtasks(TaskStatus status) {
        long id = manager.addEpic(testEpic);
        testSubtask.setEpicId(id);
        testSubtask.setStatus(status);
        long subtaskId = manager.addSubtask(testSubtask);

        manager.removeSubtask(subtaskId);
        Epic savedEpic = manager.getEpic(id);

        assertEquals(TaskStatus.NEW, savedEpic.getStatus(), "empty epic should have status NEW");
    }

    @Test
    public void shouldSetEpicToStatusNewWhenAllSubtasksHaveStatusNew() {
        long id = manager.addEpic(testEpic);
        testSubtask.setEpicId(id);
        testSubtask.setStatus(TaskStatus.NEW);
        emptySubtask.setEpicId(id);
        emptySubtask.setStatus(TaskStatus.NEW);

        manager.addSubtask(testSubtask);
        manager.addSubtask(emptySubtask);
        Epic savedEpic = manager.getEpic(id);

        assertEquals(TaskStatus.NEW, savedEpic.getStatus(),
                "epic should have status NEW when all subtasks have status NEW");
    }

    @ParameterizedTest
    @EnumSource(TaskStatus.class)
    public void shouldSetEpicToStatusNewWhenAllSubtasksSetToStatusNew(TaskStatus status) {
        long id = manager.addEpic(testEpic);
        testSubtask.setEpicId(id);
        testSubtask.setStatus(TaskStatus.NEW);
        manager.addSubtask(testSubtask);
        emptySubtask.setEpicId(id);
        emptySubtask.setStatus(status);
        long subtaskId = manager.addSubtask(emptySubtask);
        Subtask subtaskUpdate = new Subtask();
        subtaskUpdate.setId(subtaskId);
        subtaskUpdate.setStatus(TaskStatus.NEW);

        manager.updateSubtask(subtaskUpdate);
        Epic savedEpic = manager.getEpic(id);

        assertEquals(TaskStatus.NEW, savedEpic.getStatus(),
                "epic should have status NEW when all subtasks have status NEW");
    }

    @ParameterizedTest
    @EnumSource(TaskStatus.class)
    public void shouldSetEpicToStatusNewWhenAllSubtasksLeftHaveStatusNew(TaskStatus status) {
        long id = manager.addEpic(testEpic);
        testSubtask.setEpicId(id);
        testSubtask.setStatus(TaskStatus.NEW);
        manager.addSubtask(testSubtask);
        emptySubtask.setEpicId(id);
        emptySubtask.setStatus(TaskStatus.NEW);
        manager.addSubtask(emptySubtask);
        Subtask subtask = new Subtask();
        subtask.setEpicId(id);
        subtask.setStatus(status);
        long subtaskId = manager.addSubtask(subtask);

        manager.removeSubtask(subtaskId);
        Epic savedEpic = manager.getEpic(id);

        assertEquals(TaskStatus.NEW, savedEpic.getStatus(),
                "epic should have status NEW when all subtasks have status NEW");
    }

    @Test
    public void shouldSetEpicToStatusDoneWhenAllSubtasksHaveStatusDone() {
        long id = manager.addEpic(testEpic);
        testSubtask.setEpicId(id);
        testSubtask.setStatus(TaskStatus.DONE);
        emptySubtask.setEpicId(id);
        emptySubtask.setStatus(TaskStatus.DONE);

        manager.addSubtask(testSubtask);
        manager.addSubtask(emptySubtask);
        Epic savedEpic = manager.getEpic(id);

        assertEquals(TaskStatus.DONE, savedEpic.getStatus(),
                "epic should have status DONE when all subtasks have status DONE");
    }

    @ParameterizedTest
    @EnumSource(TaskStatus.class)
    public void shouldSetEpicToStatusDoneWhenAllSubtasksSetToStatusDone(TaskStatus status) {
        long id = manager.addEpic(testEpic);
        testSubtask.setEpicId(id);
        testSubtask.setStatus(TaskStatus.DONE);
        manager.addSubtask(testSubtask);
        emptySubtask.setEpicId(id);
        emptySubtask.setStatus(status);
        long subtaskId = manager.addSubtask(emptySubtask);
        Subtask subtaskUpdate = new Subtask();
        subtaskUpdate.setId(subtaskId);
        subtaskUpdate.setStatus(TaskStatus.DONE);

        manager.updateSubtask(subtaskUpdate);
        Epic savedEpic = manager.getEpic(id);

        assertEquals(TaskStatus.DONE, savedEpic.getStatus(),
                "epic should have status DONE when all subtasks have status DONE");
    }

    @ParameterizedTest
    @EnumSource(TaskStatus.class)
    public void shouldSetEpicToStatusDoneWhenAllSubtasksLeftHaveStatusDone(TaskStatus status) {
        long id = manager.addEpic(testEpic);
        testSubtask.setEpicId(id);
        testSubtask.setStatus(TaskStatus.DONE);
        manager.addSubtask(testSubtask);
        emptySubtask.setEpicId(id);
        emptySubtask.setStatus(TaskStatus.DONE);
        manager.addSubtask(emptySubtask);
        Subtask subtask = new Subtask();
        subtask.setEpicId(id);
        subtask.setStatus(status);
        long subtaskId = manager.addSubtask(subtask);

        manager.removeSubtask(subtaskId);
        Epic savedEpic = manager.getEpic(id);

        assertEquals(TaskStatus.DONE, savedEpic.getStatus(),
                "epic should have status DONE when all subtasks have status DONE");
    }

    @ParameterizedTest
    @EnumSource(TaskStatus.class)
    public void shouldSetEpicToStatusInProgressWhenNotAllSubtasksHaveStatusNewNeitherDone(TaskStatus statusB) {
        for (TaskStatus statusA : TaskStatus.values()) {
            if (statusA == statusB && statusA != TaskStatus.IN_PROGRESS) {
                continue;
            }
            long id = manager.addEpic(new Epic());
            testSubtask.setEpicId(id);
            testSubtask.setStatus(statusA);
            emptySubtask.setEpicId(id);
            emptySubtask.setStatus(statusB);

            manager.addSubtask(testSubtask);
            manager.addSubtask(emptySubtask);
            Epic savedEpic = manager.getEpic(id);

            assertEquals(TaskStatus.IN_PROGRESS, savedEpic.getStatus(), "epic should have status IN_PROGRESS when "
                    + "not empty and neither all subtasks have status NEW nor all subtasks have status DONE");
        }
    }

    @ParameterizedTest
    @EnumSource(TaskStatus.class)
    public void shouldSetEpicToStatusInProgressWhenNotAllSubtasksSetToStatusNewNeitherDone(TaskStatus statusB) {
        long id = manager.addEpic(testEpic);
        testSubtask.setEpicId(id);
        testSubtask.setStatus(statusB);
        long subtaskId = manager.addSubtask(testSubtask);
        emptySubtask.setEpicId(id);
        emptySubtask.setStatus(statusB);
        manager.addSubtask(emptySubtask);

        for (TaskStatus statusA : TaskStatus.values()) {
            if (statusA == statusB && statusA != TaskStatus.IN_PROGRESS) {
                continue;
            }
            Subtask subtaskUpdate = new Subtask();
            subtaskUpdate.setId(subtaskId);
            subtaskUpdate.setStatus(statusA);

            manager.updateSubtask(subtaskUpdate);
            Epic savedEpic = manager.getEpic(id);

            assertEquals(TaskStatus.IN_PROGRESS, savedEpic.getStatus(), "epic should have status IN_PROGRESS when "
                    + "not empty and neither all subtasks have status NEW nor all subtasks have status DONE");
        }
    }

    @ParameterizedTest
    @EnumSource(TaskStatus.class)
    public void shouldSetEpicToStatusInProgressWhenNotAllSubtasksLeftHaveStatusNewNeitherDone(TaskStatus statusC) {
        for (TaskStatus statusA : TaskStatus.values()) {
            for (TaskStatus statusB : TaskStatus.values()) {
                if (statusA == statusB && statusB != TaskStatus.IN_PROGRESS) {
                    continue;
                }
                long id = manager.addEpic(new Epic());
                testSubtask.setEpicId(id);
                testSubtask.setStatus(statusA);
                manager.addSubtask(testSubtask);
                emptySubtask.setEpicId(id);
                emptySubtask.setStatus(statusB);
                manager.addSubtask(emptySubtask);
                Subtask subtask = new Subtask();
                subtask.setEpicId(id);
                subtask.setStatus(statusC);
                long subtaskId = manager.addSubtask(subtask);

                manager.removeSubtask(subtaskId);
                Epic savedEpic = manager.getEpic(id);

                assertEquals(TaskStatus.IN_PROGRESS, savedEpic.getStatus(), "epic should have status IN_PROGRESS when "
                        + "not empty and neither all subtasks have status NEW nor all subtasks have status DONE");
            }
        }
    }

    @Test
    public void shouldRemoveEpic() {
        long id = manager.addEpic(testEpic);

        manager.removeEpic(id);
        Epic savedEpic = manager.getEpic(id);

        assertNull(savedEpic, "should have no access to removed epic");
    }

    @Test
    public void shouldKeepSubtasks() {
        long epicId = manager.addEpic(testEpic);
        testSubtask.setEpicId(epicId);

        long id = manager.addSubtask(testSubtask);
        Subtask savedSubtask = manager.getSubtask(id);

        assertNotNull(savedSubtask, "subtask not found");
        assertEquals(id, savedSubtask.getId(), "subtask id differs from returned by manager");
        assertEquals(epicId, savedSubtask.getEpicId(), "epic id changed in subtask");
        assertEquals(TEST_TITLE, savedSubtask.getTitle(), "subtask title changed");
        assertEquals(TEST_DESCRIPTION, savedSubtask.getDescription(), "subtask description changed");
        assertEquals(TEST_STATUS, savedSubtask.getStatus(), "subtask status changed");
    }

    @Test
    public void shouldNotAddNullSubtask() {
        long id = manager.addSubtask(null);

        assertEquals(WRONG_ARGUMENT, id, "null subtask should not be added");
    }

    @Test
    public void shouldNotOverwriteExistingSubtaskWhenAddingNewOne() {
        long epicId = manager.addEpic(testEpic);
        long anotherEpicId = manager.addEpic(emptyEpic);
        testSubtask.setEpicId(epicId);
        emptySubtask.setEpicId(anotherEpicId);

        long id = manager.addSubtask(testSubtask);
        emptySubtask.setId(id);
        long anotherId = manager.addSubtask(emptySubtask);
        Subtask savedSubtask = manager.getSubtask(id);

        assertNotEquals(id, anotherId, "new subtask should have new id");
        assertEquals(id, savedSubtask.getId(), "subtask id changed");
        assertEquals(epicId, savedSubtask.getEpicId(), "epic id changed in subtask");
        assertEquals(TEST_TITLE, savedSubtask.getTitle(), "subtask title changed");
        assertEquals(TEST_DESCRIPTION, savedSubtask.getDescription(), "subtask description changed");
        assertEquals(TEST_STATUS, savedSubtask.getStatus(), "subtask status changed");
    }

    @Test
    public void shouldNotAddSubtaskToNull() {
        assertTrue(manager.addSubtask(testSubtask) < 0L, "subtask with null epic id should not be added");
    }

    @Test
    public void shouldNotAddSubtaskToNotExistingEpic() {
        long epicId = -1L;
        testSubtask.setEpicId(epicId);

        assertTrue(manager.addSubtask(testSubtask) < 0L, "subtask should not be added when epic does not exist");
    }

    @Test
    public void shouldNotAddSubtaskToTask() {
        long taskId = manager.addTask(testTask);
        testSubtask.setEpicId(taskId);

        assertTrue(manager.addSubtask(testSubtask) < 0L, "subtask should not be added to regular task");
    }

    @Test
    public void shouldNotAddSubtaskToSubtask() {
        long epicId = manager.addEpic(testEpic);
        emptySubtask.setEpicId(epicId);
        long subtaskId = manager.addSubtask(emptySubtask);
        testSubtask.setEpicId(subtaskId);

        assertTrue(manager.addSubtask(testSubtask) < 0L, "subtask should not be added to subtask");
    }

    @Test
    public void shouldUpdateSubtask() {
        long epicId = manager.addEpic(testEpic);
        emptySubtask.setEpicId(epicId);
        long id = manager.addSubtask(emptySubtask);
        testSubtask.setId(id);

        int result = manager.updateSubtask(testSubtask);
        Subtask savedSubtask = manager.getSubtask(id);

        assertEquals(OK, result, "should return 0 when processed subtask update");
        assertEquals(id, savedSubtask.getId(), "subtask id changed");
        assertEquals(epicId, savedSubtask.getEpicId(), "epic id changed in subtask");
        assertEquals(TEST_TITLE, savedSubtask.getTitle(), "subtask title not updated");
        assertEquals(TEST_DESCRIPTION, savedSubtask.getDescription(), "subtask description not updated");
        assertEquals(TEST_STATUS, savedSubtask.getStatus(), "subtask status not updated");
    }

    @Test
    public void shouldNotUpdateNullSubtask() {
        int result = manager.updateSubtask(null);

        assertEquals(WRONG_ARGUMENT, result, "should not process update for null subtask");
    }

    @Test
    public void shouldNotUpdateSubtaskWhenIdNotSet() {
        int result = manager.updateSubtask(testSubtask);

        assertEquals(WRONG_ARGUMENT, result, "should not process update for subtask with null id");
    }

    @Test
    public void shouldNotUpdateNonExistingSubtask() {
        testTask.setId(-1L);

        int result = manager.updateSubtask(testSubtask);

        assertEquals(WRONG_ARGUMENT, result, "should not process update for unknown subtask");
    }

    @Test
    public void shouldRemoveSubtask() {
        long epicId = manager.addEpic(testEpic);
        testSubtask.setEpicId(epicId);
        long id = manager.addSubtask(testSubtask);

        manager.removeSubtask(id);
        Subtask savedSubtask = manager.getSubtask(id);

        assertNull(savedSubtask, "should have no access to removed subtask");
    }

    @Test
    public void shouldRemoveSubtaskWhenRemovedEpic() {
        long epicId = manager.addEpic(testEpic);
        testSubtask.setEpicId(epicId);
        long id = manager.addSubtask(testSubtask);

        manager.removeEpic(epicId);
        Subtask savedSubtask = manager.getSubtask(id);

        assertNull(savedSubtask, "should have no access to subtask of removed epic");
    }

    @Test
    public void shouldReturnSubtasksOfEpic() {
        long epicId = manager.addEpic(testEpic);
        long anotherEpicId = manager.addEpic(emptyEpic);
        Subtask subtaskA = createSubtaskFilledWithTestDataAndEpicId(epicId);
        Subtask subtaskB = createSubtaskFilledWithTestDataAndEpicId(epicId);
        Subtask subtaskC = createSubtaskFilledWithTestDataAndEpicId(epicId);
        Subtask subtaskD = createSubtaskFilledWithTestDataAndEpicId(anotherEpicId);
        long subtaskIdA = manager.addSubtask(subtaskA);
        long subtaskIdB = manager.addSubtask(subtaskB);
        long subtaskIdC = manager.addSubtask(subtaskC);
        manager.addSubtask(subtaskD);
        manager.removeSubtask(subtaskIdB);
        List<Subtask> expectedSubtaskList = createListOfSubtasksWithPresetIds(subtaskIdA, subtaskIdC);

        List<Subtask> actualSubtaskList = manager.getEpicSubtasks(epicId);

        assertEquals(expectedSubtaskList, actualSubtaskList, "incorrect list of subtasks returned");
    }

    @Test
    public void shouldNotChangeEpicSubtasksListByEpicUpdate() {
        long id = manager.addEpic(emptyEpic);
        testSubtask.setEpicId(id);
        emptySubtask.setEpicId(id);
        long subtaskIdA = manager.addSubtask(testSubtask);
        long subtaskIdB = manager.addSubtask(emptySubtask);
        List<Subtask> expectedSubtaskList = createListOfSubtasksWithPresetIds(subtaskIdA, subtaskIdB);
        testEpic.setId(id);

        manager.updateEpic(testEpic);
        List<Subtask> actualSubtaskList = manager.getEpicSubtasks(id);

        assertEquals(expectedSubtaskList, actualSubtaskList, "epic update should change list of epic subtasks");
    }

    @Test
    public void shouldReturnListOfTasks() {
        long taskIdA = manager.addTask(new Task());
        long taskIdB = manager.addTask(new Task());
        long taskIdC = manager.addTask(new Task());
        manager.removeTask(taskIdB);
        testTask.setId(taskIdA);
        emptyTask.setId(taskIdC);
        List<Task> expectedTaskList = List.of(testTask, emptyTask);

        List<Task> actualTaskList = manager.getTasks();

        assertEquals(expectedTaskList, actualTaskList, "incorrect list of tasks returned");
    }

    @Test
    public void shouldRemoveAllTasks() {
        manager.addTask(testTask);
        manager.addTask(emptyTask);

        manager.removeTasks();
        List<Task> tasks = manager.getTasks();

        assertTrue(tasks.isEmpty(), "list of tasks should be empty");
    }

    @Test
    public void shouldReturnListOfEpics() {
        long epicIdA = manager.addEpic(new Epic());
        long epicIdB = manager.addEpic(new Epic());
        long epicIdC = manager.addEpic(new Epic());
        manager.removeEpic(epicIdB);
        testEpic.setId(epicIdA);
        emptyEpic.setId(epicIdC);
        List<Epic> expectedEpicList = List.of(testEpic, emptyEpic);

        List<Epic> actualEpicList = manager.getEpics();

        assertEquals(expectedEpicList, actualEpicList, "incorrect list of epics returned");
    }

    @Test
    public void shouldRemoveAllEpics() {
        manager.addEpic(testEpic);
        manager.addEpic(emptyEpic);

        manager.removeEpics();
        List<Epic> epics = manager.getEpics();

        assertTrue(epics.isEmpty(), "list of epics should be empty");
    }

    @Test
    public void shouldReturnListOfSubtasks() {
        long epicId = manager.addEpic(testEpic);
        long anotherEpicId = manager.addEpic(emptyEpic);
        Subtask subtaskA = createSubtaskFilledWithTestDataAndEpicId(epicId);
        Subtask subtaskB = createSubtaskFilledWithTestDataAndEpicId(epicId);
        Subtask subtaskC = createSubtaskFilledWithTestDataAndEpicId(epicId);
        Subtask subtaskD = createSubtaskFilledWithTestDataAndEpicId(anotherEpicId);
        long subtaskIdA = manager.addSubtask(subtaskA);
        long subtaskIdB = manager.addSubtask(subtaskB);
        long subtaskIdC = manager.addSubtask(subtaskC);
        long subtaskIdD = manager.addSubtask(subtaskD);
        manager.removeSubtask(subtaskIdB);
        List<Subtask> expectedSubtaskList = createListOfSubtasksWithPresetIds(subtaskIdA, subtaskIdC, subtaskIdD);

        List<Subtask> actualSubtaskList = manager.getSubtasks();

        assertEquals(expectedSubtaskList, actualSubtaskList, "incorrect list of subtasks returned");
    }

    @Test
    public void shouldRemoveAllSubtasks() {
        long epicId = manager.addEpic(testEpic);
        testSubtask.setEpicId(epicId);
        manager.addSubtask(testSubtask);
        long anotherEpicId = manager.addEpic(emptyEpic);
        emptySubtask.setEpicId(anotherEpicId);
        manager.addSubtask(emptySubtask);

        manager.removeSubtasks();
        List<Subtask> subtasks = manager.getSubtasks();

        assertTrue(subtasks.isEmpty(), "list of subtasks should be empty");
    }

    @Test
    public void shouldRemoveAllSubtasksWhenRemovedAllEpics() {
        long epicId = manager.addEpic(testEpic);
        testSubtask.setEpicId(epicId);
        manager.addSubtask(testSubtask);
        long anotherEpicId = manager.addEpic(emptyEpic);
        emptySubtask.setEpicId(anotherEpicId);
        manager.addSubtask(emptySubtask);

        manager.removeEpics();
        List<Subtask> subtasks = manager.getSubtasks();

        assertTrue(subtasks.isEmpty(), "list of subtasks should be empty");
    }

    @Test
    public void shouldUpdateEpicsWhenRemovedAllSubtasks() {
        long epicId = manager.addEpic(testEpic);
        testSubtask.setEpicId(epicId);
        manager.addSubtask(testSubtask);
        long anotherEpicId = manager.addEpic(emptyEpic);
        emptySubtask.setEpicId(anotherEpicId);
        manager.addSubtask(emptySubtask);

        manager.removeSubtasks();
        List<Subtask> actualSubtaskListA = manager.getEpicSubtasks(epicId);
        List<Subtask> actualSubtaskListB = manager.getEpicSubtasks(anotherEpicId);

        assertTrue(actualSubtaskListA.isEmpty(), "list of subtasks should be empty");
        assertTrue(actualSubtaskListB.isEmpty(), "list of subtasks should be empty");
    }

    @Test
    public void shouldAddVisitedTaskToHistory() {
        final long taskId = manager.addTask(testTask);

        manager.getTask(taskId);
        final List<Task> tasks = historyManager.getHistory();

        assertEquals(1, tasks.size(), "history should contain exactly 1 element");
        assertEquals(Task.class, tasks.getFirst().getClass(), "element in history should be of Task class");
        Task savedTask = tasks.getFirst();
        assertEquals(taskId, savedTask.getId(), "task id should not change");
        assertEquals(TEST_TITLE, savedTask.getTitle(), "task title should not change");
        assertEquals(TEST_DESCRIPTION, savedTask.getDescription(), "task description should not change");
        assertEquals(TEST_STATUS, savedTask.getStatus(), "task status should not change");
    }

    @Test
    public void shouldAddVisitedEpicToHistory() {
        final long epicId = manager.addEpic(testEpic);

        manager.getEpic(epicId);
        final List<Task> tasks = historyManager.getHistory();

        assertEquals(1, tasks.size(), "history should contain exactly 1 element");
        assertEquals(Epic.class, tasks.getFirst().getClass(), "element in history should be of Epic class");
        Epic savedEpic = (Epic) tasks.getFirst();
        assertEquals(epicId, savedEpic.getId(), "epic id should not change");
        assertEquals(TEST_TITLE, savedEpic.getTitle(), "epic title should not change");
        assertEquals(TEST_DESCRIPTION, savedEpic.getDescription(), "epic description should not change");
    }

    @Test
    public void shouldAddVisitedSubtaskToHistory() {
        final long epicId = manager.addEpic(testEpic);
        testSubtask.setEpicId(epicId);
        final long subtaskId = manager.addSubtask(testSubtask);

        manager.getSubtask(subtaskId);
        final List<Task> tasks = historyManager.getHistory();

        assertEquals(1, tasks.size(), "history should contain exactly 1 element");
        assertEquals(Subtask.class, tasks.getFirst().getClass(), "element in history should be of Subtask class");
        Subtask savedSubtask = (Subtask) tasks.getFirst();
        assertEquals(subtaskId, savedSubtask.getId(), "subtask id should not change");
        assertEquals(epicId, savedSubtask.getEpicId(), "epic id of status should not change");
        assertEquals(TEST_TITLE, savedSubtask.getTitle(), "subtask title should not change");
        assertEquals(TEST_DESCRIPTION, savedSubtask.getDescription(), "subtask description should not change");
        assertEquals(TEST_STATUS, savedSubtask.getStatus(), "subtask status should not change");
    }

    @Test
    public void shouldReturnHistory() {
        testTask.setId(TEST_TASK_ID);
        testEpic.setId(TEST_EPIC_ID);
        testSubtask.setId(TEST_SUBTASK_ID);
        testSubtask.setEpicId(TEST_EPIC_ID);
        historyManager.add(testTask);
        historyManager.add(testEpic);
        historyManager.add(testSubtask);

        final List<Task> tasks = manager.getHistory();

        assertEquals(3, tasks.size(), "history should contain exactly 3 elements");
        assertEquals(Task.class, tasks.getFirst().getClass(), "1st element in history should be of Task class");
        Task savedTask = tasks.getFirst();
        assertEquals(TEST_TASK_ID, savedTask.getId(), "task id should not change");
        assertEquals(TEST_TITLE, savedTask.getTitle(), "task title should not change");
        assertEquals(TEST_DESCRIPTION, savedTask.getDescription(), "task description should not change");
        assertEquals(TEST_STATUS, savedTask.getStatus(), "task status should not change");
        assertEquals(Epic.class, tasks.get(1).getClass(), "2nd element in history should be of Epic class");
        Epic savedEpic = (Epic) tasks.get(1);
        assertEquals(TEST_EPIC_ID, savedEpic.getId(), "epic id should not change");
        assertEquals(TEST_TITLE, savedEpic.getTitle(), "epic title should not change");
        assertEquals(TEST_DESCRIPTION, savedEpic.getDescription(), "epic description should not change");
        assertEquals(Subtask.class, tasks.getLast().getClass(), "3rd element in history should be of Subtask class");
        Subtask savedSubtask = (Subtask) tasks.getLast();
        assertEquals(TEST_SUBTASK_ID, savedSubtask.getId(), "subtask id should not change");
        assertEquals(TEST_EPIC_ID, savedSubtask.getEpicId(), "epic id of status should not change");
        assertEquals(TEST_TITLE, savedSubtask.getTitle(), "subtask title should not change");
        assertEquals(TEST_DESCRIPTION, savedSubtask.getDescription(), "subtask description should not change");
        assertEquals(TEST_STATUS, savedSubtask.getStatus(), "subtask status should not change");
    }

    private Subtask createSubtaskFilledWithTestDataAndEpicId(long epicId) {
        return createTestSubtask(epicId, TEST_TITLE, TEST_DESCRIPTION, TEST_STATUS);
    }

    private List<Subtask> createListOfSubtasksWithPresetIds(long... ids) {
        List<Subtask> subtasks = new ArrayList<>();
        for (long id : ids) {
            Subtask subtask = new Subtask();
            subtask.setId(id);
            subtasks.add(subtask);
        }
        return subtasks;
    }
}