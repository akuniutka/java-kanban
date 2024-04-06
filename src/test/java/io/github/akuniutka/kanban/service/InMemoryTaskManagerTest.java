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

class InMemoryTaskManagerTest {
    private static final int OK = 0;
    private static final int WRONG_ARGUMENT = -1;
    private static final int MAX_HISTORY_SIZE = 10;
    private static final String TEST_TITLE = "Title";
    private static final String TEST_DESCRIPTION = "Description";
    private static final TaskStatus TEST_STATUS = TaskStatus.IN_PROGRESS;
    private TaskManager manager;

    @BeforeEach
    public void setUp() {
        manager = new InMemoryTaskManager(Managers.getDefaultHistory());
    }

    @Test
    public void shouldCreateInMemoryTaskManagerOfInterfaceType() {
        assertNotNull(manager, "task manager was not created");
    }

    @Test
    public void shouldKeepTasks() {
        Task task = createTaskFilledWithTestData();

        long id = manager.addTask(task);
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
        Task task = createTaskFilledWithTestData();
        Task anotherTask = new Task();

        long id = manager.addTask(task);
        anotherTask.setId(id);
        long anotherId = manager.addTask(anotherTask);
        Task savedTask = manager.getTask(id);

        assertNotEquals(id, anotherId, "new task should have new id");
        assertEquals(id, savedTask.getId(), "task id changed");
        assertEquals(TEST_TITLE, savedTask.getTitle(), "task title changed");
        assertEquals(TEST_DESCRIPTION, savedTask.getDescription(), "task description changed");
        assertEquals(TEST_STATUS, savedTask.getStatus(), "task status changed");
    }

    @Test
    public void shouldUpdateTask() {
        long id = manager.addTask(new Task());
        Task task = createTaskFilledWithTestData();
        task.setId(id);

        int result = manager.updateTask(task);
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
        Task task = createTaskFilledWithTestData();

        int result = manager.updateTask(task);

        assertEquals(WRONG_ARGUMENT, result, "should not process update for task with null id");
    }

    @Test
    public void shouldNotUpdateNonExistingTask() {
        Task task = createTaskFilledWithTestData();
        task.setId(-1L);

        int result = manager.updateTask(task);

        assertEquals(WRONG_ARGUMENT, result, "should not process update for unknown task");
    }

    @Test
    public void shouldRemoveTask() {
        long id = manager.addTask(new Task());

        manager.removeTask(id);
        Task savedTask = manager.getTask(id);

        assertNull(savedTask, "should have no access to removed task");
    }

    @Test
    public void shouldKeepEpics() {
        Epic epic = createEpicFilledWithTestData();

        long id = manager.addEpic(epic);
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
        Epic epic = createEpicFilledWithTestData();
        Epic anotherEpic = new Epic();

        long id = manager.addEpic(epic);
        anotherEpic.setId(id);
        long anotherId = manager.addEpic(anotherEpic);
        Epic savedEpic = manager.getEpic(id);

        assertNotEquals(id, anotherId, "new epic should have new id");
        assertEquals(id, savedEpic.getId(), "epic id changed");
        assertEquals(TEST_TITLE, savedEpic.getTitle(), "epic title changed");
        assertEquals(TEST_DESCRIPTION, savedEpic.getDescription(), "epic description changed");
    }

    @Test
    public void shouldUpdateEpic() {
        long id = manager.addEpic(new Epic());
        Epic epic = createEpicFilledWithTestData();
        epic.setId(id);

        int result = manager.updateEpic(epic);
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
        Epic epic = createEpicFilledWithTestData();

        int result = manager.updateEpic(epic);

        assertEquals(WRONG_ARGUMENT, result, "should not process update for epic with null id");
    }

    @Test
    public void shouldNotUpdateNonExistingEpic() {
        Epic epic = createEpicFilledWithTestData();
        epic.setId(-1L);

        int result = manager.updateEpic(epic);

        assertEquals(WRONG_ARGUMENT, result, "should not process update for unknown epic");
    }

    @ParameterizedTest
    @EnumSource(TaskStatus.class)
    public void shouldNotChangeEpicStatusByEpicUpdate(TaskStatus status) {
        long id = manager.addEpic(new Epic());
        Subtask subtask = createSubtaskFilledWithTestDataAndEpicId(id);
        subtask.setStatus(status);
        manager.addSubtask(subtask);

        for (TaskStatus newStatus : TaskStatus.values()) {
            Epic epicUpdate = new Epic();
            epicUpdate.setId(id);
            epicUpdate.setStatus(newStatus);

            manager.updateEpic(epicUpdate);
            Epic savedEpic = manager.getEpic(id);

            assertEquals(status, savedEpic.getStatus(), "epic update changed epic status");
        }
    }

    @Test
    public void shouldSetEpicToStatusNewWhenNoSubtasks() {
        Epic epic = createEpicFilledWithTestData();

        long id = manager.addEpic(epic);
        Epic savedEpic = manager.getEpic(id);

        assertEquals(TaskStatus.NEW, savedEpic.getStatus(), "empty epic should have status NEW");
    }

    @Test
    public void shouldSetEpicToStatusNewWhenNoMoreSubtasks() {
        Epic epic = createEpicFilledWithTestData();
        long id = manager.addEpic(epic);
        Subtask subtask = createSubtaskFilledWithTestDataAndEpicId(id);
        long subtaskId = manager.addSubtask(subtask);

        manager.removeSubtask(subtaskId);
        Epic savedEpic = manager.getEpic(id);

        assertEquals(TaskStatus.NEW, savedEpic.getStatus(), "empty epic should have status NEW");
    }

    @Test
    public void shouldSetEpicToStatusNewWhenAllSubtasksHaveStatusNew() {
        Epic epic = createEpicFilledWithTestData();
        long id = manager.addEpic(epic);
        Subtask subtaskA = createSubtaskFilledWithTestDataAndEpicId(id);
        subtaskA.setStatus(TaskStatus.NEW);
        Subtask subtaskB = createSubtaskFilledWithTestDataAndEpicId(id);
        subtaskB.setStatus(TaskStatus.NEW);

        manager.addSubtask(subtaskA);
        manager.addSubtask(subtaskB);
        Epic savedEpic = manager.getEpic(id);

        assertEquals(TaskStatus.NEW, savedEpic.getStatus(),
                "epic should have status NEW when all subtasks have status NEW");
    }

    @Test
    public void shouldSetEpicToStatusNewWhenAllSubtasksSetToStatusNew() {
        Epic epic = createEpicFilledWithTestData();
        long id = manager.addEpic(epic);
        Subtask subtaskA = createSubtaskFilledWithTestDataAndEpicId(id);
        long subtaskAId = manager.addSubtask(subtaskA);
        Subtask subtaskB = createSubtaskFilledWithTestDataAndEpicId(id);
        long subtaskBId = manager.addSubtask(subtaskB);
        Subtask subtaskAUpdated = new Subtask();
        subtaskAUpdated.setId(subtaskAId);
        subtaskAUpdated.setStatus(TaskStatus.NEW);
        Subtask subtaskBUpdated = new Subtask();
        subtaskBUpdated.setId(subtaskBId);
        subtaskBUpdated.setStatus(TaskStatus.NEW);

        manager.updateSubtask(subtaskAUpdated);
        manager.updateSubtask(subtaskBUpdated);
        Epic savedEpic = manager.getEpic(id);

        assertEquals(TaskStatus.NEW, savedEpic.getStatus(),
                "epic should have status NEW when all subtasks have status NEW");
    }

    @Test
    public void shouldSetEpicToStatusDoneWhenAllSubtasksHaveStatusDone() {
        Epic epic = createEpicFilledWithTestData();
        long id = manager.addEpic(epic);
        Subtask subtaskA = createSubtaskFilledWithTestDataAndEpicId(id);
        subtaskA.setStatus(TaskStatus.DONE);
        Subtask subtaskB = createSubtaskFilledWithTestDataAndEpicId(id);
        subtaskB.setStatus(TaskStatus.DONE);

        manager.addSubtask(subtaskA);
        manager.addSubtask(subtaskB);
        Epic savedEpic = manager.getEpic(id);

        assertEquals(TaskStatus.DONE, savedEpic.getStatus(),
                "epic should have status DONE when all subtasks have status DONE");
    }

    @Test
    public void shouldSetEpicToStatusDoneWhenAllSubtasksSetToStatusDone() {
        Epic epic = createEpicFilledWithTestData();
        long id = manager.addEpic(epic);
        Subtask subtaskA = createSubtaskFilledWithTestDataAndEpicId(id);
        long subtaskAId = manager.addSubtask(subtaskA);
        Subtask subtaskB = createSubtaskFilledWithTestDataAndEpicId(id);
        long subtaskBId = manager.addSubtask(subtaskB);
        Subtask subtaskAUpdated = new Subtask();
        subtaskAUpdated.setId(subtaskAId);
        subtaskAUpdated.setStatus(TaskStatus.DONE);
        Subtask subtaskBUpdated = new Subtask();
        subtaskBUpdated.setId(subtaskBId);
        subtaskBUpdated.setStatus(TaskStatus.DONE);

        manager.updateSubtask(subtaskAUpdated);
        manager.updateSubtask(subtaskBUpdated);
        Epic savedEpic = manager.getEpic(id);

        assertEquals(TaskStatus.DONE, savedEpic.getStatus(),
                "epic should have status DONE when all subtasks have status DONE");
    }

    @Test
    public void shouldSetEpicToStatusInProgressWhenNeitherAllSubtasksHaveStatusNewNorAllSubtasksHaveStatusDone() {
        Epic epic = createEpicFilledWithTestData();
        long id = manager.addEpic(epic);
        Subtask subtaskA = createSubtaskFilledWithTestDataAndEpicId(id);
        subtaskA.setStatus(TaskStatus.NEW);
        long subtaskAId = manager.addSubtask(subtaskA);
        Subtask subtaskB = createSubtaskFilledWithTestDataAndEpicId(id);
        subtaskB.setStatus(TaskStatus.NEW);
        manager.addSubtask(subtaskB);
        Subtask subtaskAUpdated = new Subtask();
        subtaskAUpdated.setId(subtaskAId);
        subtaskAUpdated.setStatus(TaskStatus.DONE);

        manager.updateSubtask(subtaskAUpdated);
        Epic savedEpic = manager.getEpic(id);

        assertEquals(TaskStatus.IN_PROGRESS, savedEpic.getStatus(), "epic should have status IN_PROGRESS when not "
                + "empty and neither all subtasks have status NEW nor all subtasks have status DONE");
    }

    @Test
    public void shouldRemoveEpic() {
        long id = manager.addEpic(new Epic());

        manager.removeEpic(id);
        Epic savedEpic = manager.getEpic(id);

        assertNull(savedEpic, "should have no access to removed epic");
    }

    @Test
    public void shouldKeepSubtasks() {
        long epicId = manager.addEpic(new Epic());
        Subtask subtask = createSubtaskFilledWithTestDataAndEpicId(epicId);

        long id = manager.addSubtask(subtask);
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
        long epicId = manager.addEpic(new Epic());
        long anotherEpicId = manager.addEpic(new Epic());
        Subtask subtask = createSubtaskFilledWithTestDataAndEpicId(epicId);
        Subtask anotherSubtask = new Subtask();
        anotherSubtask.setEpicId(anotherEpicId);

        long id = manager.addSubtask(subtask);
        anotherSubtask.setId(id);
        long anotherId = manager.addSubtask(anotherSubtask);
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
        Subtask subtask = createSubtaskFilledWithTestData();

        assertTrue(manager.addSubtask(subtask) < 0L, "subtask with null epic id should not be added");
    }

    @Test
    public void shouldNotAddSubtaskToNotExistingEpic() {
        long epicId = -1L;
        Subtask subtask = createSubtaskFilledWithTestDataAndEpicId(epicId);

        assertTrue(manager.addSubtask(subtask) < 0L, "subtask should not be added when epic does not exist");
    }

    @Test
    public void shouldNotAddSubtaskToTask() {
        long taskId = manager.addTask(new Task());
        Subtask subtask = createSubtaskFilledWithTestDataAndEpicId(taskId);

        assertTrue(manager.addSubtask(subtask) < 0L, "subtask should not be added to regular task");
    }

    @Test
    public void shouldNotAddSubtaskToSubtask() {
        long epicId = manager.addEpic(new Epic());
        Subtask anotherSubtask = new Subtask();
        anotherSubtask.setEpicId(epicId);
        long anotherSubtaskId = manager.addSubtask(anotherSubtask);
        Subtask subtask = createSubtaskFilledWithTestDataAndEpicId(anotherSubtaskId);

        assertTrue(manager.addSubtask(subtask) < 0L, "subtask should not be added to subtask");
    }

    @Test
    public void shouldUpdateSubtask() {
        long epicId = manager.addEpic(new Epic());
        Subtask originalSubtask = new Subtask();
        originalSubtask.setEpicId(epicId);
        long id = manager.addSubtask(originalSubtask);
        Subtask subtask = createSubtaskFilledWithTestData();
        subtask.setId(id);

        int result = manager.updateSubtask(subtask);
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
        Subtask subtask = createSubtaskFilledWithTestData();

        int result = manager.updateSubtask(subtask);

        assertEquals(WRONG_ARGUMENT, result, "should not process update for subtask with null id");
    }

    @Test
    public void shouldNotUpdateNonExistingSubtask() {
        Subtask subtask = createSubtaskFilledWithTestData();
        subtask.setId(-1L);

        int result = manager.updateSubtask(subtask);

        assertEquals(WRONG_ARGUMENT, result, "should not process update for unknown subtask");
    }

    @Test
    public void shouldRemoveSubtask() {
        long epicId = manager.addEpic(new Epic());
        Subtask subtask = new Subtask();
        subtask.setEpicId(epicId);
        long id = manager.addSubtask(subtask);

        manager.removeSubtask(id);
        Subtask savedSubtask = manager.getSubtask(id);

        assertNull(savedSubtask, "should have no access to removed subtask");
    }

    @Test
    public void shouldRemoveSubtaskWhenRemovedEpic() {
        long epicId = manager.addEpic(new Epic());
        Subtask subtask = new Subtask();
        subtask.setEpicId(epicId);
        long id = manager.addSubtask(subtask);

        manager.removeEpic(epicId);
        Subtask savedSubtask = manager.getSubtask(id);

        assertNull(savedSubtask, "should have no access to subtask of removed epic");
    }

    @Test
    public void shouldReturnSubtasksOfEpic() {
        long epicId = manager.addEpic(new Epic());
        Subtask subtaskA = createSubtaskFilledWithTestDataAndEpicId(epicId);
        long subtaskAId = manager.addSubtask(subtaskA);
        Subtask subtaskB = createSubtaskFilledWithTestDataAndEpicId(epicId);
        long subtaskBId = manager.addSubtask(subtaskB);
        Subtask subtaskC = createSubtaskFilledWithTestDataAndEpicId(epicId);
        long subtaskCId = manager.addSubtask(subtaskC);
        long anotherEpicId = manager.addEpic(new Epic());
        Subtask subtaskD = createSubtaskFilledWithTestDataAndEpicId(anotherEpicId);
        manager.addSubtask(subtaskD);
        manager.removeSubtask(subtaskBId);
        List<Subtask> expectedSubtaskList = createListOfSubtasksWithPresetIds(subtaskAId, subtaskCId);

        List<Subtask> actualSubtaskList = manager.getEpicSubtasks(epicId);

        assertEquals(expectedSubtaskList, actualSubtaskList, "incorrect list of subtasks returned");
    }

    @Test
    public void shouldNotChangeEpicSubtasksListByEpicUpdate() {
        long id = manager.addEpic(new Epic());
        Subtask subtaskA = createSubtaskFilledWithTestDataAndEpicId(id);
        long subtaskAId = manager.addSubtask(subtaskA);
        Subtask subtaskB = createSubtaskFilledWithTestDataAndEpicId(id);
        long subtaskBId = manager.addSubtask(subtaskB);
        List<Subtask> expectedSubtaskList = createListOfSubtasksWithPresetIds(subtaskAId, subtaskBId);
        Epic epicUpdate = createEpicFilledWithTestData();
        epicUpdate.setId(id);

        manager.updateEpic(epicUpdate);
        List<Subtask> actualSubtaskList = manager.getEpicSubtasks(id);

        assertEquals(expectedSubtaskList, actualSubtaskList, "epic update should change list of epic subtasks");
    }

    @Test
    public void shouldReturnListOfTasks() {
        Task taskA = createTaskFilledWithTestData();
        long taskAId = manager.addTask(taskA);
        Task taskB = createTaskFilledWithTestData();
        long taskBId = manager.addTask(taskB);
        Task taskC = createTaskFilledWithTestData();
        long taskCId = manager.addTask(taskC);
        manager.removeTask(taskBId);
        List<Task> expectedTaskList = new ArrayList<>();
        Task expectedTaskA = new Task();
        expectedTaskA.setId(taskAId);
        Task expectedTaskC = new Task();
        expectedTaskC.setId(taskCId);
        expectedTaskList.add(expectedTaskA);
        expectedTaskList.add(expectedTaskC);

        List<Task> actualTaskList = manager.getTasks();

        assertEquals(expectedTaskList, actualTaskList, "incorrect list of tasks returned");
    }

    @Test
    public void shouldRemoveAllTasks() {
        manager.addTask(new Task());
        manager.addTask(new Task());

        manager.removeTasks();
        List<Task> tasks = manager.getTasks();

        assertTrue(tasks.isEmpty(), "list of tasks should be empty");
    }

    @Test
    public void shouldReturnListOfEpics() {
        Epic epicA = createEpicFilledWithTestData();
        long epicAId = manager.addEpic(epicA);
        Epic epicB = createEpicFilledWithTestData();
        long epicBId = manager.addEpic(epicB);
        Epic epicC = createEpicFilledWithTestData();
        long epicCId = manager.addEpic(epicC);
        manager.removeEpic(epicBId);
        List<Epic> expectedEpicList = new ArrayList<>();
        Epic expectedEpicA = new Epic();
        expectedEpicA.setId(epicAId);
        Epic expectedEpicC = new Epic();
        expectedEpicC.setId(epicCId);
        expectedEpicList.add(expectedEpicA);
        expectedEpicList.add(expectedEpicC);

        List<Epic> actualEpicList = manager.getEpics();

        assertEquals(expectedEpicList, actualEpicList, "incorrect list of epics returned");
    }

    @Test
    public void shouldRemoveAllEpics() {
        manager.addEpic(new Epic());
        manager.addEpic(new Epic());

        manager.removeEpics();
        List<Epic> epics = manager.getEpics();

        assertTrue(epics.isEmpty(), "list of epics should be empty");
    }

    @Test
    public void shouldReturnListOfSubtasks() {
        long epicId = manager.addEpic(new Epic());
        Subtask subtaskA = createSubtaskFilledWithTestDataAndEpicId(epicId);
        long subtaskAId = manager.addSubtask(subtaskA);
        Subtask subtaskB = createSubtaskFilledWithTestDataAndEpicId(epicId);
        long subtaskBId = manager.addSubtask(subtaskB);
        Subtask subtaskC = createSubtaskFilledWithTestDataAndEpicId(epicId);
        long subtaskCId = manager.addSubtask(subtaskC);
        long anotherEpicId = manager.addEpic(new Epic());
        Subtask subtaskD = createSubtaskFilledWithTestDataAndEpicId(anotherEpicId);
        long subtaskDId = manager.addSubtask(subtaskD);
        manager.removeSubtask(subtaskBId);
        List<Subtask> expectedSubtaskList = createListOfSubtasksWithPresetIds(subtaskAId, subtaskCId, subtaskDId);

        List<Subtask> actualSubtaskList = manager.getSubtasks();

        assertEquals(expectedSubtaskList, actualSubtaskList, "incorrect list of subtasks returned");
    }

    @Test
    public void shouldRemoveAllSubtasks() {
        long epicId = manager.addEpic(new Epic());
        Subtask subtaskA = createSubtaskFilledWithTestDataAndEpicId(epicId);
        manager.addSubtask(subtaskA);
        long anotherEpicId = manager.addEpic(new Epic());
        Subtask subtaskB = createSubtaskFilledWithTestDataAndEpicId(anotherEpicId);
        manager.addSubtask(subtaskB);

        manager.removeSubtasks();
        List<Subtask> subtasks = manager.getSubtasks();

        assertTrue(subtasks.isEmpty(), "list of subtasks should be empty");
    }

    @Test
    public void shouldRemoveAllSubtasksWhenRemovedAllEpics() {
        long epicId = manager.addEpic(new Epic());
        Subtask subtaskA = createSubtaskFilledWithTestDataAndEpicId(epicId);
        manager.addSubtask(subtaskA);
        long anotherEpicId = manager.addEpic(new Epic());
        Subtask subtaskB = createSubtaskFilledWithTestDataAndEpicId(anotherEpicId);
        manager.addSubtask(subtaskB);

        manager.removeEpics();
        List<Subtask> subtasks = manager.getSubtasks();

        assertTrue(subtasks.isEmpty(), "list of subtasks should be empty");
    }

    @Test
    public void shouldUpdateEpicsWhenRemovedAllSubtasks() {
        long epicId = manager.addEpic(new Epic());
        Subtask subtaskA = createSubtaskFilledWithTestDataAndEpicId(epicId);
        manager.addSubtask(subtaskA);
        long anotherEpicId = manager.addEpic(new Epic());
        Subtask subtaskB = createSubtaskFilledWithTestDataAndEpicId(anotherEpicId);
        manager.addSubtask(subtaskB);

        manager.removeSubtasks();
        List<Subtask> actualSubtaskListA = manager.getEpicSubtasks(epicId);
        List<Subtask> actualSubtaskListB = manager.getEpicSubtasks(anotherEpicId);

        assertTrue(actualSubtaskListA.isEmpty(), "list of subtasks should be empty");
        assertTrue(actualSubtaskListB.isEmpty(), "list of subtasks should be empty");
    }

    @Test
    public void shouldReturnHistory() {
        Task task = createTaskFilledWithTestData();
        long taskId = manager.addTask(task);
        Epic epic = createEpicFilledWithTestData();
        long epicId = manager.addEpic(epic);
        Subtask subtask = createSubtaskFilledWithTestDataAndEpicId(epicId);
        long subtaskId = manager.addSubtask(subtask);
        Task expectedTask = new Task();
        expectedTask.setId(taskId);
        Epic expectedEpic = new Epic();
        expectedEpic.setId(epicId);
        Subtask expectedSubtask = new Subtask();
        expectedSubtask.setId(subtaskId);
        List<Task> expectedHistory = composeHistoryFromTasks(false, expectedTask, expectedEpic, expectedSubtask);

        manager.getTask(taskId);
        manager.getEpic(epicId);
        manager.getSubtask(subtaskId);
        List<Task> actualHistory = manager.getHistory();

        assertEquals(expectedHistory, actualHistory, "incorrect list of tasks returned");
    }

    @Test
    public void shouldRemoveOldTasksWhenHistoryLimitReached() {
        Task task = createTaskFilledWithTestData();
        long taskId = manager.addTask(task);
        Epic epic = createEpicFilledWithTestData();
        long epicId = manager.addEpic(epic);
        Subtask subtask = createSubtaskFilledWithTestDataAndEpicId(epicId);
        long subtaskId = manager.addSubtask(subtask);
        Task expectedTask = new Task();
        expectedTask.setId(taskId);
        Epic expectedEpic = new Epic();
        expectedEpic.setId(epicId);
        Subtask expectedSubtask = new Subtask();
        expectedSubtask.setId(subtaskId);
        List<Task> expectedHistory = composeHistoryFromTasks(true, expectedSubtask, expectedTask, expectedEpic);

        for (int i = 0; i < 4; i++) {
            manager.getTask(taskId);
            manager.getEpic(epicId);
            manager.getSubtask(subtaskId);
        }
        List<Task> actualHistory = manager.getHistory();

        assertEquals(expectedHistory, actualHistory, "incorrect list of tasks returned");
    }

    @Test
    public void shouldKeepTaskStateChangesInHistory() {
        List<Task> snapshots = new ArrayList<>();
        long taskId = manager.addTask(new Task());
        snapshots.add(copyTask(taskId));
        Task taskUpdate = createTaskFilledWithTestData();
        taskUpdate.setId(taskId);
        manager.updateTask(taskUpdate);
        snapshots.add(copyTask(taskId));
        long epicId = manager.addEpic(new Epic());
        snapshots.add(copyEpic(epicId));
        Epic epicUpdate = createEpicFilledWithTestData();
        epicUpdate.setId(epicId);
        manager.updateEpic(epicUpdate);
        Subtask subtask = new Subtask();
        subtask.setEpicId(epicId);
        subtask.setStatus(TaskStatus.DONE);
        long subtaskId = manager.addSubtask(subtask);
        snapshots.add(copyEpic(epicId));
        snapshots.add(copySubtask(subtaskId));
        Subtask subtaskUpdate = createSubtaskFilledWithTestDataAndEpicId(epicId);
        subtaskUpdate.setId(subtaskId);
        manager.updateSubtask(subtaskUpdate);
        snapshots.add(copySubtask(subtaskId));

        List<Task> history = manager.getHistory();

        assertEquals(snapshots, history, "incorrect list of tasks returned");
        for (int i = 0; i < snapshots.size(); i++) {
            assertEquals(snapshots.get(i).getTitle(), history.get(i).getTitle(), "incorrect task title");
            assertEquals(snapshots.get(i).getDescription(), history.get(i).getDescription(), "incorrect task "
                    + "description");
            if (!(snapshots.get(i) instanceof Epic)) {
                assertEquals(snapshots.get(i).getStatus(), history.get(i).getStatus(), "incorrect task status");
            }
            if (snapshots.get(i) instanceof Subtask snapshotSubtask) {
                Subtask historySubtask = (Subtask) history.get(i);
                assertEquals(snapshotSubtask.getEpicId(), historySubtask.getEpicId(), "incorrect epic id for subtask");
            }
        }
    }

    private Task createTaskFilledWithTestData() {
        Task task = new Task();
        task.setTitle(TEST_TITLE);
        task.setDescription(TEST_DESCRIPTION);
        task.setStatus(TEST_STATUS);
        return task;
    }

    private Epic createEpicFilledWithTestData() {
        Epic epic = new Epic();
        epic.setTitle(TEST_TITLE);
        epic.setDescription(TEST_DESCRIPTION);
        return epic;
    }

    private Subtask createSubtaskFilledWithTestData() {
        Subtask subtask = new Subtask();
        subtask.setTitle(TEST_TITLE);
        subtask.setDescription(TEST_DESCRIPTION);
        subtask.setStatus(TEST_STATUS);
        return subtask;
    }

    private Subtask createSubtaskFilledWithTestDataAndEpicId(long epicId) {
        Subtask subtask = createSubtaskFilledWithTestData();
        subtask.setEpicId(epicId);
        return subtask;
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

    private List<Task> composeHistoryFromTasks(boolean repeat, Task... tasks) {
        List<Task> list = new ArrayList<>();
        while (true) {
            for (Task task : tasks) {
                if (list.size() == MAX_HISTORY_SIZE) {
                    return list;
                }
                list.add(task);
            }
            if (!repeat) {
                return list;
            }
        }
    }

    private Task copyTask(long id) {
        Task task = manager.getTask(id);
        Task copy = new Task();
        copy.setId(task.getId());
        copy.setTitle(task.getTitle());
        copy.setDescription(task.getDescription());
        copy.setStatus(task.getStatus());
        return copy;
    }

    private Epic copyEpic(long id) {
        Epic epic = manager.getEpic(id);
        Epic copy = new Epic();
        copy.setId(epic.getId());
        copy.setTitle(epic.getTitle());
        copy.setDescription(epic.getDescription());
        return copy;
    }

    private Subtask copySubtask(long id) {
        Subtask subtask = manager.getSubtask(id);
        Subtask copy = new Subtask();
        copy.setId(subtask.getId());
        copy.setEpicId(subtask.getEpicId());
        copy.setTitle(subtask.getTitle());
        copy.setDescription(subtask.getDescription());
        copy.setStatus(subtask.getStatus());
        return copy;
    }
}