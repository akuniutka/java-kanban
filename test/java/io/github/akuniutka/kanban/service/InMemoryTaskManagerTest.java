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
import java.util.NoSuchElementException;

import static io.github.akuniutka.kanban.TestModels.*;
import static org.junit.jupiter.api.Assertions.*;

class InMemoryTaskManagerTest {
    private static final String WRONG_EXCEPTION_MESSAGE = "message for exception is wrong";
    private TaskManager manager;
    private HistoryManager historyManager;
    private Task emptyTask;
    private Task testTask;
    private Task modifiedTestTask;
    private Epic emptyEpic;
    private Epic testEpic;
    private Epic modifiedTestEpic;
    private Subtask emptySubtask;
    private Subtask testSubtask;
    private Subtask modifiedTestSubtask;

    @BeforeEach
    public void setUp() {
        historyManager = new InMemoryHistoryManager();
        manager = new InMemoryTaskManager(historyManager);
        emptyTask = createTestTask();
        testTask = createTaskFilledWithTestData();
        modifiedTestTask = createTestTask(MODIFIED_TEST_TITLE, MODIFIED_TEST_DESCRIPTION, MODIFIED_TEST_STATUS);
        emptyEpic = createTestEpic();
        testEpic = createEpicFilledWithTestData();
        modifiedTestEpic = createTestEpic(MODIFIED_TEST_TITLE, MODIFIED_TEST_DESCRIPTION);
        emptySubtask = createTestSubtask();
        testSubtask = createTestSubtask(TEST_TITLE, TEST_DESCRIPTION, TEST_STATUS);
        modifiedTestSubtask = createTestSubtask(MODIFIED_TEST_TITLE, MODIFIED_TEST_DESCRIPTION, MODIFIED_TEST_STATUS);
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
    public void shouldThrowWhenRetrievingNonExistingTask() {
        long id = -1L;
        String expectedMessage = "no task with id=" + id;

        Exception exception = assertThrows(NoSuchElementException.class, () -> manager.getTask(id));
        assertEquals(expectedMessage, exception.getMessage(), WRONG_EXCEPTION_MESSAGE);
    }

    @Test
    public void shouldAllowAddTaskWithNullFields() {
        long id = manager.addTask(emptyTask);
        Task savedTask = manager.getTask(id);

        assertNotNull(savedTask, "task not found");
        assertEquals(id, savedTask.getId(), "task id differs from returned by manager class");
        assertNull(savedTask.getTitle(), "task title changed");
        assertNull(savedTask.getDescription(), "task description changed");
        assertNull(savedTask.getStatus(), "task status changed");
    }

    @Test
    public void shouldNotAddNullTask() {
        String expectedMessage = "cannot add null to list of tasks";

        Exception exception = assertThrows(NullPointerException.class, () -> manager.addTask(null));
        assertEquals(expectedMessage, exception.getMessage(), WRONG_EXCEPTION_MESSAGE);
    }

    @Test
    public void shouldNotOverwriteExistingTaskWhenAddingNewOne() {
        long id = manager.addTask(testTask);
        modifiedTestTask.setId(id);
        long anotherId = manager.addTask(modifiedTestTask);
        Task savedTask = manager.getTask(id);

        assertNotEquals(id, anotherId, "new task should have new id");
        assertEquals(id, savedTask.getId(), "task id changed");
        assertEquals(TEST_TITLE, savedTask.getTitle(), "task title changed");
        assertEquals(TEST_DESCRIPTION, savedTask.getDescription(), "task description changed");
        assertEquals(TEST_STATUS, savedTask.getStatus(), "task status changed");
    }

    @Test
    public void shouldUpdateTask() {
        long id = manager.addTask(testTask);
        modifiedTestTask.setId(id);

        manager.updateTask(modifiedTestTask);
        Task savedTask = manager.getTask(id);

        assertEquals(id, savedTask.getId(), "task id changed");
        assertEquals(MODIFIED_TEST_TITLE, savedTask.getTitle(), "task title not updated");
        assertEquals(MODIFIED_TEST_DESCRIPTION, savedTask.getDescription(), "task description not updated");
        assertEquals(MODIFIED_TEST_STATUS, savedTask.getStatus(), "task status not updated");
    }

    @Test
    public void shouldAllowUpdateTaskFieldsToNull() {
        long id = manager.addTask(testTask);
        emptyTask.setId(id);

        manager.updateTask(emptyTask);
        Task savedTask = manager.getTask(id);

        assertEquals(id, savedTask.getId(), "task id changed");
        assertNull(savedTask.getTitle(), "task title not updated");
        assertNull(savedTask.getDescription(), "task description not updated");
        assertNull(savedTask.getStatus(), "task status not updated");
    }

    @Test
    public void shouldNotUpdateNullTask() {
        String expectedMessage = "cannot apply null update to task";

        Exception exception = assertThrows(NullPointerException.class, () -> manager.updateTask(null));
        assertEquals(expectedMessage, exception.getMessage(), WRONG_EXCEPTION_MESSAGE);
    }

    @Test
    public void shouldNotUpdateTaskWhenIdNotSet() {
        String expectedMessage = "no task with id=null";

        Exception exception = assertThrows(NoSuchElementException.class, () -> manager.updateTask(testTask));
        assertEquals(expectedMessage, exception.getMessage(), WRONG_EXCEPTION_MESSAGE);
    }

    @Test
    public void shouldNotUpdateNonExistingTask() {
        long taskId = -1L;
        String expectedMessage = "no task with id=" + taskId;
        testTask.setId(taskId);

        Exception exception = assertThrows(NoSuchElementException.class, () -> manager.updateTask(testTask));
        assertEquals(expectedMessage, exception.getMessage(), WRONG_EXCEPTION_MESSAGE);
    }

    @Test
    public void shouldRemoveTask() {
        long id = manager.addTask(testTask);
        String expectedMessage = "no task with id=" + id;

        manager.removeTask(id);

        Exception exception = assertThrows(NoSuchElementException.class, () -> manager.getTask(id));
        assertEquals(expectedMessage, exception.getMessage(), WRONG_EXCEPTION_MESSAGE);
    }

    @Test
    public void shouldThrowRemovingNonExistingTask() {
        long id = -1L;
        String expectedMessage = "no task with id=" + id;

        Exception exception = assertThrows(NoSuchElementException.class, () -> manager.removeTask(id));
        assertEquals(expectedMessage, exception.getMessage(), WRONG_EXCEPTION_MESSAGE);
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
    public void shouldThrowWhenRetrievingNonExistingEpic() {
        long id = -1L;
        String expectedMessage = "no epic with id=" + id;

        Exception exception = assertThrows(NoSuchElementException.class, () -> manager.getEpic(id));
        assertEquals(expectedMessage, exception.getMessage(), WRONG_EXCEPTION_MESSAGE);
    }

    @Test
    public void shouldAllowAddEpicWithNullFields() {
        long id = manager.addEpic(emptyEpic);
        Epic savedEpic = manager.getEpic(id);

        assertNotNull(savedEpic, "epic not found");
        assertEquals(id, savedEpic.getId(), "epic id differs from returned by manager");
        assertNull(savedEpic.getTitle(), "epic title changed");
        assertNull(savedEpic.getDescription(), "epic description changed");
    }

    @Test
    public void shouldNotAddNullEpic() {
        String expectedMessage = "cannot add null to list of epics";

        Exception exception = assertThrows(NullPointerException.class, () -> manager.addEpic(null));
        assertEquals(expectedMessage, exception.getMessage(), WRONG_EXCEPTION_MESSAGE);
    }

    @Test
    public void shouldNotOverwriteExistingEpicWhenAddingNewOne() {
        long id = manager.addEpic(testEpic);
        modifiedTestEpic.setId(id);
        long anotherId = manager.addEpic(modifiedTestEpic);
        Epic savedEpic = manager.getEpic(id);

        assertNotEquals(id, anotherId, "new epic should have new id");
        assertEquals(id, savedEpic.getId(), "epic id changed");
        assertEquals(TEST_TITLE, savedEpic.getTitle(), "epic title changed");
        assertEquals(TEST_DESCRIPTION, savedEpic.getDescription(), "epic description changed");
    }

    @Test
    public void shouldUpdateEpic() {
        long id = manager.addEpic(testEpic);
        modifiedTestEpic.setId(id);

        manager.updateEpic(modifiedTestEpic);
        Epic savedEpic = manager.getEpic(id);

        assertEquals(id, savedEpic.getId(), "epic id changed");
        assertEquals(MODIFIED_TEST_TITLE, savedEpic.getTitle(), "epic title not updated");
        assertEquals(MODIFIED_TEST_DESCRIPTION, savedEpic.getDescription(), "epic description not updated");
    }

    @Test
    public void shouldAllowUpdateEpicFieldsToNull() {
        long id = manager.addEpic(testEpic);
        emptyEpic.setId(id);

        manager.updateEpic(emptyEpic);
        Epic savedEpic = manager.getEpic(id);

        assertEquals(id, savedEpic.getId(), "epic id changed");
        assertNull(savedEpic.getTitle(), "epic title not updated");
        assertNull(savedEpic.getDescription(), "epic description not updated");
    }

    @Test
    public void shouldNotUpdateNullEpic() {
        String expectedMessage = "cannot apply null update to epic";

        Exception exception = assertThrows(NullPointerException.class, () -> manager.updateEpic(null));
        assertEquals(expectedMessage, exception.getMessage(), WRONG_EXCEPTION_MESSAGE);
    }

    @Test
    public void shouldNotUpdateEpicWhenIdNotSet() {
        String expectedMessage = "no epic with id=null";

        Exception exception = assertThrows(NoSuchElementException.class, () -> manager.updateEpic(testEpic));
        assertEquals(expectedMessage, exception.getMessage(), WRONG_EXCEPTION_MESSAGE);
    }

    @Test
    public void shouldNotUpdateNonExistingEpic() {
        long id = -1L;
        String expectedMessage = "no epic with id=" + id;
        testEpic.setId(id);

        Exception exception = assertThrows(NoSuchElementException.class, () -> manager.updateEpic(testEpic));
        assertEquals(expectedMessage, exception.getMessage(), WRONG_EXCEPTION_MESSAGE);
    }

    @ParameterizedTest
    @EnumSource(TaskStatus.class)
    public void shouldNotChangeEpicStatusByEpicUpdate(TaskStatus status) {
        long id = manager.addEpic(testEpic);
        testSubtask.setEpicId(id);
        testSubtask.setStatus(status);
        manager.addSubtask(testSubtask);

        for (TaskStatus newStatus : TaskStatus.values()) {
            Epic epicUpdate = createEpicFilledWithTestData();
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
        modifiedTestSubtask.setEpicId(id);
        modifiedTestSubtask.setStatus(TaskStatus.NEW);

        manager.addSubtask(testSubtask);
        manager.addSubtask(modifiedTestSubtask);
        Epic savedEpic = manager.getEpic(id);

        assertEquals(TaskStatus.NEW, savedEpic.getStatus(),
                "epic should have status NEW when all subtasks have status NEW");
    }

    @ParameterizedTest
    @EnumSource(TaskStatus.class)
    public void shouldSetEpicToStatusNewWhenAllSubtasksSetToStatusNew(TaskStatus status) {
        long epicId = manager.addEpic(testEpic);
        testSubtask.setEpicId(epicId);
        testSubtask.setStatus(TaskStatus.NEW);
        manager.addSubtask(testSubtask);
        modifiedTestSubtask.setEpicId(epicId);
        modifiedTestSubtask.setStatus(status);
        long subtaskId = manager.addSubtask(modifiedTestSubtask);
        Subtask subtaskUpdate = createTestSubtask(subtaskId, epicId, TEST_TITLE, TEST_DESCRIPTION, TaskStatus.NEW);

        manager.updateSubtask(subtaskUpdate);
        Epic savedEpic = manager.getEpic(epicId);

        assertEquals(TaskStatus.NEW, savedEpic.getStatus(),
                "epic should have status NEW when all subtasks have status NEW");
    }

    @ParameterizedTest
    @EnumSource(TaskStatus.class)
    public void shouldSetEpicToStatusNewWhenAllSubtasksLeftHaveStatusNew(TaskStatus status) {
        long epicId = manager.addEpic(testEpic);
        testSubtask.setEpicId(epicId);
        testSubtask.setStatus(TaskStatus.NEW);
        manager.addSubtask(testSubtask);
        modifiedTestSubtask.setEpicId(epicId);
        modifiedTestSubtask.setStatus(TaskStatus.NEW);
        manager.addSubtask(modifiedTestSubtask);
        Subtask subtask = createTestSubtask(epicId, TEST_TITLE, TEST_DESCRIPTION, status);
        long subtaskId = manager.addSubtask(subtask);

        manager.removeSubtask(subtaskId);
        Epic savedEpic = manager.getEpic(epicId);

        assertEquals(TaskStatus.NEW, savedEpic.getStatus(),
                "epic should have status NEW when all subtasks have status NEW");
    }

    @Test
    public void shouldSetEpicToStatusDoneWhenAllSubtasksHaveStatusDone() {
        long epicId = manager.addEpic(testEpic);
        testSubtask.setEpicId(epicId);
        testSubtask.setStatus(TaskStatus.DONE);
        modifiedTestSubtask.setEpicId(epicId);
        modifiedTestSubtask.setStatus(TaskStatus.DONE);

        manager.addSubtask(testSubtask);
        manager.addSubtask(modifiedTestSubtask);
        Epic savedEpic = manager.getEpic(epicId);

        assertEquals(TaskStatus.DONE, savedEpic.getStatus(),
                "epic should have status DONE when all subtasks have status DONE");
    }

    @ParameterizedTest
    @EnumSource(TaskStatus.class)
    public void shouldSetEpicToStatusDoneWhenAllSubtasksSetToStatusDone(TaskStatus status) {
        long epicId = manager.addEpic(testEpic);
        testSubtask.setEpicId(epicId);
        testSubtask.setStatus(TaskStatus.DONE);
        manager.addSubtask(testSubtask);
        modifiedTestSubtask.setEpicId(epicId);
        modifiedTestSubtask.setStatus(status);
        long subtaskId = manager.addSubtask(modifiedTestSubtask);
        Subtask subtaskUpdate = createTestSubtask(subtaskId, epicId, TEST_TITLE, TEST_DESCRIPTION, TaskStatus.DONE);

        manager.updateSubtask(subtaskUpdate);
        Epic savedEpic = manager.getEpic(epicId);

        assertEquals(TaskStatus.DONE, savedEpic.getStatus(),
                "epic should have status DONE when all subtasks have status DONE");
    }

    @ParameterizedTest
    @EnumSource(TaskStatus.class)
    public void shouldSetEpicToStatusDoneWhenAllSubtasksLeftHaveStatusDone(TaskStatus status) {
        long epicId = manager.addEpic(testEpic);
        testSubtask.setEpicId(epicId);
        testSubtask.setStatus(TaskStatus.DONE);
        manager.addSubtask(testSubtask);
        modifiedTestSubtask.setEpicId(epicId);
        modifiedTestSubtask.setStatus(TaskStatus.DONE);
        manager.addSubtask(modifiedTestSubtask);
        Subtask subtask = createTestSubtask(epicId, TEST_TITLE, TEST_DESCRIPTION, status);
        long subtaskId = manager.addSubtask(subtask);

        manager.removeSubtask(subtaskId);
        Epic savedEpic = manager.getEpic(epicId);

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
            long epicId = manager.addEpic(createEpicFilledWithTestData());
            testSubtask.setEpicId(epicId);
            testSubtask.setStatus(statusA);
            modifiedTestSubtask.setEpicId(epicId);
            modifiedTestSubtask.setStatus(statusB);

            manager.addSubtask(testSubtask);
            manager.addSubtask(modifiedTestSubtask);
            Epic savedEpic = manager.getEpic(epicId);

            assertEquals(TaskStatus.IN_PROGRESS, savedEpic.getStatus(), "epic should have status IN_PROGRESS when "
                    + "not empty and neither all subtasks have status NEW nor all subtasks have status DONE");
        }
    }

    @ParameterizedTest
    @EnumSource(TaskStatus.class)
    public void shouldSetEpicToStatusInProgressWhenNotAllSubtasksSetToStatusNewNeitherDone(TaskStatus statusB) {
        long epicId = manager.addEpic(testEpic);
        testSubtask.setEpicId(epicId);
        testSubtask.setStatus(statusB);
        long subtaskId = manager.addSubtask(testSubtask);
        modifiedTestSubtask.setEpicId(epicId);
        modifiedTestSubtask.setStatus(statusB);
        manager.addSubtask(modifiedTestSubtask);

        for (TaskStatus statusA : TaskStatus.values()) {
            if (statusA == statusB && statusA != TaskStatus.IN_PROGRESS) {
                continue;
            }
            Subtask subtaskUpdate = createTestSubtask(subtaskId, epicId, TEST_TITLE, TEST_DESCRIPTION, statusA);

            manager.updateSubtask(subtaskUpdate);
            Epic savedEpic = manager.getEpic(epicId);

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
                long epicId = manager.addEpic(createEpicFilledWithTestData());
                testSubtask.setEpicId(epicId);
                testSubtask.setStatus(statusA);
                manager.addSubtask(testSubtask);
                modifiedTestSubtask.setEpicId(epicId);
                modifiedTestSubtask.setStatus(statusB);
                manager.addSubtask(modifiedTestSubtask);
                Subtask subtask = createTestSubtask(epicId, TEST_TITLE, TEST_DESCRIPTION, statusC);
                long subtaskId = manager.addSubtask(subtask);

                manager.removeSubtask(subtaskId);
                Epic savedEpic = manager.getEpic(epicId);

                assertEquals(TaskStatus.IN_PROGRESS, savedEpic.getStatus(), "epic should have status IN_PROGRESS when "
                        + "not empty and neither all subtasks have status NEW nor all subtasks have status DONE");
            }
        }
    }

    @Test
    public void shouldRemoveEpic() {
        long id = manager.addEpic(testEpic);
        String expectedMessage = "no epic with id=" + id;

        manager.removeEpic(id);

        Exception exception = assertThrows(NoSuchElementException.class, () -> manager.getEpic(id));
        assertEquals(expectedMessage, exception.getMessage(), WRONG_EXCEPTION_MESSAGE);
    }

    @Test
    public void shouldThrowWhenRemovingNonExistingEpic() {
        long id = -1L;
        String expectedMessage = "no epic with id=" + id;

        Exception exception = assertThrows(NoSuchElementException.class, () -> manager.removeEpic(id));
        assertEquals(expectedMessage, exception.getMessage(), WRONG_EXCEPTION_MESSAGE);
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
    public void shouldThrowWhenRetrievingNonExistingSubtask() {
        long id = -1L;
        String expectedMessage = "no subtask with id=" + id;

        Exception exception = assertThrows(NoSuchElementException.class, () -> manager.getSubtask(id));
        assertEquals(expectedMessage, exception.getMessage(), WRONG_EXCEPTION_MESSAGE);
    }

    @Test
    public void shouldAllowAddSubtaskWithNullFields() {
        long epicId = manager.addEpic(testEpic);
        emptySubtask.setEpicId(epicId);

        long id = manager.addSubtask(emptySubtask);
        Subtask savedSubtask = manager.getSubtask(id);

        assertNotNull(savedSubtask, "subtask not found");
        assertEquals(id, savedSubtask.getId(), "subtask id differs from returned by manager");
        assertEquals(epicId, savedSubtask.getEpicId(), "epic id changed in subtask");
        assertNull(savedSubtask.getTitle(), "subtask title changed");
        assertNull(savedSubtask.getDescription(), "subtask description changed");
        assertNull(savedSubtask.getStatus(), "subtask status changed");
    }

    @Test
    public void shouldNotAddNullSubtask() {
        String expectedMessage = "cannot add null to list of subtasks";

        Exception exception = assertThrows(NullPointerException.class, () -> manager.addSubtask(null));
        assertEquals(expectedMessage, exception.getMessage(), WRONG_EXCEPTION_MESSAGE);
    }

    @Test
    public void shouldNotOverwriteExistingSubtaskWhenAddingNewOne() {
        long epicId = manager.addEpic(testEpic);
        long anotherEpicId = manager.addEpic(modifiedTestEpic);
        testSubtask.setEpicId(epicId);
        modifiedTestSubtask.setEpicId(anotherEpicId);

        long id = manager.addSubtask(testSubtask);
        modifiedTestSubtask.setId(id);
        long anotherId = manager.addSubtask(modifiedTestSubtask);
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
        String expectedMessage = "no epic with id=null";

        Exception exception = assertThrows(NoSuchElementException.class, () -> manager.addSubtask(testSubtask));
        assertEquals(expectedMessage, exception.getMessage(), WRONG_EXCEPTION_MESSAGE);
    }

    @Test
    public void shouldNotAddSubtaskToNotExistingEpic() {
        long epicId = -1L;
        String expectedMessage = "no epic with id=" + epicId;
        testSubtask.setEpicId(epicId);

        Exception exception = assertThrows(NoSuchElementException.class, () -> manager.addSubtask(testSubtask));
        assertEquals(expectedMessage, exception.getMessage(), WRONG_EXCEPTION_MESSAGE);
    }

    @Test
    public void shouldNotAddSubtaskToTask() {
        long taskId = manager.addTask(testTask);
        String expectedMessage = "no epic with id=" + taskId;
        testSubtask.setEpicId(taskId);

        Exception exception = assertThrows(NoSuchElementException.class, () -> manager.addSubtask(testSubtask));
        assertEquals(expectedMessage, exception.getMessage(), WRONG_EXCEPTION_MESSAGE);
    }

    @Test
    public void shouldNotAddSubtaskToSubtask() {
        long epicId = manager.addEpic(testEpic);
        testSubtask.setEpicId(epicId);
        long subtaskId = manager.addSubtask(testSubtask);
        String expectedMessage = "no epic with id=" + subtaskId;
        modifiedTestSubtask.setEpicId(subtaskId);

        Exception exception = assertThrows(NoSuchElementException.class, () -> manager.addSubtask(modifiedTestSubtask));
        assertEquals(expectedMessage, exception.getMessage(), WRONG_EXCEPTION_MESSAGE);
    }

    @Test
    public void shouldUpdateSubtask() {
        long epicId = manager.addEpic(testEpic);
        testSubtask.setEpicId(epicId);
        long id = manager.addSubtask(testSubtask);
        modifiedTestSubtask.setId(id);

        manager.updateSubtask(modifiedTestSubtask);
        Subtask savedSubtask = manager.getSubtask(id);

        assertEquals(id, savedSubtask.getId(), "subtask id changed");
        assertEquals(epicId, savedSubtask.getEpicId(), "epic id changed in subtask");
        assertEquals(MODIFIED_TEST_TITLE, savedSubtask.getTitle(), "subtask title not updated");
        assertEquals(MODIFIED_TEST_DESCRIPTION, savedSubtask.getDescription(), "subtask description not updated");
        assertEquals(MODIFIED_TEST_STATUS, savedSubtask.getStatus(), "subtask status not updated");
    }

    @Test
    public void shouldAllowUpdateSubtaskFieldsToNull() {
        long epicId = manager.addEpic(testEpic);
        testSubtask.setEpicId(epicId);
        long id = manager.addSubtask(testSubtask);
        emptySubtask.setId(id);

        manager.updateSubtask(emptySubtask);
        Subtask savedSubtask = manager.getSubtask(id);

        assertEquals(id, savedSubtask.getId(), "subtask id changed");
        assertEquals(epicId, savedSubtask.getEpicId(), "epic id changed in subtask");
        assertNull(savedSubtask.getTitle(), "subtask title not updated");
        assertNull(savedSubtask.getDescription(), "subtask description not updated");
        assertNull(savedSubtask.getStatus(), "subtask status not updated");
    }

    @Test
    public void shouldNotUpdateNullSubtask() {
        String expectedMessage = "cannot apply null update to subtask";

        Exception exception = assertThrows(NullPointerException.class, () -> manager.updateSubtask(null));
        assertEquals(expectedMessage, exception.getMessage(), WRONG_EXCEPTION_MESSAGE);
    }

    @Test
    public void shouldNotUpdateSubtaskWhenIdNotSet() {
        String expectedMessage = "no subtask with id=null";
        long epicId = manager.addEpic(testEpic);
        testSubtask.setEpicId(epicId);

        Exception exception = assertThrows(NoSuchElementException.class, () -> manager.updateSubtask(testSubtask));
        assertEquals(expectedMessage, exception.getMessage(), WRONG_EXCEPTION_MESSAGE);
    }

    @Test
    public void shouldNotUpdateNonExistingSubtask() {
        long id = -1L;
        String expectedMessage = "no subtask with id=" + id;
        long epicId = manager.addEpic(testEpic);
        testSubtask.setEpicId(epicId);
        testSubtask.setId(id);

        Exception exception = assertThrows(NoSuchElementException.class, () -> manager.updateSubtask(testSubtask));
        assertEquals(expectedMessage, exception.getMessage(), WRONG_EXCEPTION_MESSAGE);
    }

    @Test
    public void shouldRemoveSubtask() {
        long epicId = manager.addEpic(testEpic);
        testSubtask.setEpicId(epicId);
        long id = manager.addSubtask(testSubtask);
        String expectedMessage = "no subtask with id=" + id;

        manager.removeSubtask(id);

        Exception exception = assertThrows(NoSuchElementException.class, () -> manager.getSubtask(id));
        assertEquals(expectedMessage, exception.getMessage(), WRONG_EXCEPTION_MESSAGE);
    }

    @Test
    public void shouldRemoveSubtaskWhenRemovedEpic() {
        long epicId = manager.addEpic(testEpic);
        testSubtask.setEpicId(epicId);
        long id = manager.addSubtask(testSubtask);
        String expectedMessage = "no subtask with id=" + id;

        manager.removeEpic(epicId);

        Exception exception = assertThrows(NoSuchElementException.class, () -> manager.getSubtask(id));
        assertEquals(expectedMessage, exception.getMessage(), WRONG_EXCEPTION_MESSAGE);
    }

    @Test
    public void shouldThrowWhenRemovingNonExistingSubtask() {
        long id = -1L;
        String expectedMessage = "no subtask with id=" + id;

        Exception exception = assertThrows(NoSuchElementException.class, () -> manager.removeSubtask(id));
        assertEquals(expectedMessage, exception.getMessage(), WRONG_EXCEPTION_MESSAGE);
    }

    @Test
    public void shouldReturnSubtasksOfEpic() {
        long epicId = manager.addEpic(testEpic);
        long anotherEpicId = manager.addEpic(modifiedTestEpic);
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
    public void shouldThrowWhenRetrievingSubtasksOfNonExistingEpic() {
        long epicId = -1L;
        String expectedMessage = "no epic with id=" + epicId;

        Exception exception = assertThrows(NoSuchElementException.class, () -> manager.getEpicSubtasks(epicId));
        assertEquals(expectedMessage, exception.getMessage(), WRONG_EXCEPTION_MESSAGE);
    }

    @Test
    public void shouldNotChangeEpicSubtasksListByEpicUpdate() {
        long id = manager.addEpic(testEpic);
        testSubtask.setEpicId(id);
        modifiedTestSubtask.setEpicId(id);
        long subtaskIdA = manager.addSubtask(testSubtask);
        long subtaskIdB = manager.addSubtask(modifiedTestSubtask);
        List<Subtask> expectedSubtaskList = createListOfSubtasksWithPresetIds(subtaskIdA, subtaskIdB);
        modifiedTestEpic.setId(id);

        manager.updateEpic(modifiedTestEpic);
        List<Subtask> actualSubtaskList = manager.getEpicSubtasks(id);

        assertEquals(expectedSubtaskList, actualSubtaskList, "epic update should change list of epic subtasks");
    }

    @Test
    public void shouldReturnListOfTasks() {
        Task taskA = createTaskFilledWithTestData();
        Task taskB = createTaskFilledWithTestData();
        Task taskC = createTaskFilledWithTestData();
        long taskIdA = manager.addTask(taskA);
        long taskIdB = manager.addTask(taskB);
        long taskIdC = manager.addTask(taskC);
        manager.removeTask(taskIdB);
        testTask.setId(taskIdA);
        modifiedTestTask.setId(taskIdC);
        List<Task> expectedTaskList = List.of(testTask, modifiedTestTask);

        List<Task> actualTaskList = manager.getTasks();

        assertEquals(expectedTaskList, actualTaskList, "incorrect list of tasks returned");
    }

    @Test
    public void shouldRemoveAllTasks() {
        manager.addTask(testTask);
        manager.addTask(modifiedTestTask);

        manager.removeTasks();
        List<Task> tasks = manager.getTasks();

        assertTrue(tasks.isEmpty(), "list of tasks should be empty");
    }

    @Test
    public void shouldReturnListOfEpics() {
        Epic epicA = createEpicFilledWithTestData();
        Epic epicB = createEpicFilledWithTestData();
        Epic epicC = createEpicFilledWithTestData();
        long epicIdA = manager.addEpic(epicA);
        long epicIdB = manager.addEpic(epicB);
        long epicIdC = manager.addEpic(epicC);
        manager.removeEpic(epicIdB);
        testEpic.setId(epicIdA);
        modifiedTestEpic.setId(epicIdC);
        List<Epic> expectedEpicList = List.of(testEpic, modifiedTestEpic);

        List<Epic> actualEpicList = manager.getEpics();

        assertEquals(expectedEpicList, actualEpicList, "incorrect list of epics returned");
    }

    @Test
    public void shouldRemoveAllEpics() {
        manager.addEpic(testEpic);
        manager.addEpic(modifiedTestEpic);

        manager.removeEpics();
        List<Epic> epics = manager.getEpics();

        assertTrue(epics.isEmpty(), "list of epics should be empty");
    }

    @Test
    public void shouldReturnListOfSubtasks() {
        long epicId = manager.addEpic(testEpic);
        long anotherEpicId = manager.addEpic(modifiedTestEpic);
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
        long anotherEpicId = manager.addEpic(modifiedTestEpic);
        modifiedTestSubtask.setEpicId(anotherEpicId);
        manager.addSubtask(modifiedTestSubtask);

        manager.removeSubtasks();
        List<Subtask> subtasks = manager.getSubtasks();

        assertTrue(subtasks.isEmpty(), "list of subtasks should be empty");
    }

    @Test
    public void shouldRemoveAllSubtasksWhenRemovedAllEpics() {
        long epicId = manager.addEpic(testEpic);
        testSubtask.setEpicId(epicId);
        manager.addSubtask(testSubtask);
        long anotherEpicId = manager.addEpic(modifiedTestEpic);
        modifiedTestSubtask.setEpicId(anotherEpicId);
        manager.addSubtask(modifiedTestSubtask);

        manager.removeEpics();
        List<Subtask> subtasks = manager.getSubtasks();

        assertTrue(subtasks.isEmpty(), "list of subtasks should be empty");
    }

    @Test
    public void shouldUpdateEpicsWhenRemovedAllSubtasks() {
        long epicId = manager.addEpic(testEpic);
        testSubtask.setEpicId(epicId);
        manager.addSubtask(testSubtask);
        long anotherEpicId = manager.addEpic(modifiedTestEpic);
        modifiedTestSubtask.setEpicId(anotherEpicId);
        manager.addSubtask(modifiedTestSubtask);

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

    @Test
    public void shouldRemoveTaskFromHistory() {
        final long id = manager.addTask(testTask);
        manager.getTask(id);

        manager.removeTask(id);
        List<Task> tasks = historyManager.getHistory();

        assertTrue(tasks.isEmpty());
    }

    @Test
    public void shouldRemoveEpicFromHistory() {
        final long id = manager.addEpic(testEpic);
        manager.getEpic(id);

        manager.removeEpic(id);
        List<Task> tasks = historyManager.getHistory();

        assertTrue(tasks.isEmpty());
    }

    @Test
    public void shouldRemoveSubtaskFromHistory() {
        final long epicId = manager.addEpic(testEpic);
        testSubtask.setEpicId(epicId);
        final long id = manager.addSubtask(testSubtask);
        manager.getSubtask(id);

        manager.removeSubtask(id);
        List<Task> tasks = historyManager.getHistory();

        assertTrue(tasks.isEmpty());
    }

    @Test
    public void shouldRemoveSubtaskOfEpicFromHistory() {
        final long epicId = manager.addEpic(testEpic);
        testSubtask.setEpicId(epicId);
        final long idA = manager.addSubtask(testSubtask);
        final long anotherEpicId = manager.addEpic(modifiedTestEpic);
        modifiedTestSubtask.setEpicId(anotherEpicId);
        final long idB = manager.addSubtask(modifiedTestSubtask);
        manager.getSubtask(idA);
        manager.getSubtask(idB);

        manager.removeEpic(epicId);
        List<Task> tasks = historyManager.getHistory();

        assertEquals(1, tasks.size(), "history should contain exactly 1 element");
        assertEquals(Subtask.class, tasks.getFirst().getClass(), "element in history should be of Subtask class");
        Subtask savedSubtask = (Subtask) tasks.getFirst();
        assertEquals(idB, savedSubtask.getId(), "subtask id should not change");
        assertEquals(anotherEpicId, savedSubtask.getEpicId(), "epic id of status should not change");
        assertEquals(MODIFIED_TEST_TITLE, savedSubtask.getTitle(), "subtask title should not change");
        assertEquals(MODIFIED_TEST_DESCRIPTION, savedSubtask.getDescription(), "subtask description should not change");
        assertEquals(MODIFIED_TEST_STATUS, savedSubtask.getStatus(), "subtask status should not change");
    }

    @Test
    public void shouldRemoveAllTasksFromHistory() {
        final long idA = manager.addTask(testTask);
        final long idB = manager.addTask(modifiedTestTask);
        manager.getTask(idA);
        manager.getTask(idB);

        manager.removeTasks();
        List<Task> tasks = historyManager.getHistory();

        assertTrue(tasks.isEmpty());
    }

    @Test
    public void shouldRemoveAllEpicsFromHistory() {
        final long idA = manager.addEpic(testEpic);
        final long idB = manager.addEpic(modifiedTestEpic);
        manager.getEpic(idA);
        manager.getEpic(idB);

        manager.removeEpics();
        List<Task> tasks = historyManager.getHistory();

        assertTrue(tasks.isEmpty());
    }

    @Test
    public void shouldRemoveAllSubtasksFromHistory() {
        final long epicId = manager.addEpic(testEpic);
        testSubtask.setEpicId(epicId);
        final long idA = manager.addSubtask(testSubtask);
        final long anotherEpicId = manager.addEpic(modifiedTestEpic);
        modifiedTestSubtask.setEpicId(anotherEpicId);
        final long idB = manager.addSubtask(modifiedTestSubtask);
        manager.getSubtask(idA);
        manager.getSubtask(idB);

        manager.removeSubtasks();
        List<Task> tasks = historyManager.getHistory();

        assertTrue(tasks.isEmpty());
    }

    @Test
    public void shouldRemoveAllSubtasksFromHistoryWhenRemovedAllEpics() {
        final long epicId = manager.addEpic(testEpic);
        testSubtask.setEpicId(epicId);
        final long idA = manager.addSubtask(testSubtask);
        final long anotherEpicId = manager.addEpic(modifiedTestEpic);
        modifiedTestSubtask.setEpicId(anotherEpicId);
        final long idB = manager.addSubtask(modifiedTestSubtask);
        manager.getSubtask(idA);
        manager.getSubtask(idB);

        manager.removeEpics();
        List<Task> tasks = historyManager.getHistory();

        assertTrue(tasks.isEmpty());
    }

    private Task createTaskFilledWithTestData() {
        return createTestTask(TEST_TITLE, TEST_DESCRIPTION, TEST_STATUS);
    }

    private Epic createEpicFilledWithTestData() {
        return createTestEpic(TEST_TITLE, TEST_DESCRIPTION);
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