package io.github.akuniutka.kanban.service;

import io.github.akuniutka.kanban.exception.ManagerLoadException;
import io.github.akuniutka.kanban.exception.ManagerSaveException;
import io.github.akuniutka.kanban.model.Epic;
import io.github.akuniutka.kanban.model.Subtask;
import io.github.akuniutka.kanban.model.Task;
import io.github.akuniutka.kanban.model.TaskType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.List;

import static io.github.akuniutka.kanban.TestModels.*;
import static org.junit.jupiter.api.Assertions.*;

class FileBackedTaskManagerTest {
    private static final String WRONG_FILE_FORMAT = "wrong file format";
    private static final String WRONG_EXCEPTION_MESSAGE = "message for exception is wrong";
    private TaskManager manager;
    private File file;
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
    public void setUp() throws IOException {
        historyManager = new InMemoryHistoryManager();
        file = File.createTempFile("kanban", null);
        manager = new FileBackedTaskManager(file, historyManager);
        emptyTask = createTestTask();
        testTask = createTestTask(TEST_TITLE, TEST_DESCRIPTION, TEST_DURATION, TEST_START_TIME, TEST_STATUS);
        modifiedTestTask = createTestTask(MODIFIED_TEST_TITLE, MODIFIED_TEST_DESCRIPTION, MODIFIED_TEST_DURATION,
                MODIFIED_TEST_START_TIME, MODIFIED_TEST_STATUS);
        emptyEpic = createTestEpic();
        testEpic = createTestEpic(TEST_TITLE, TEST_DESCRIPTION);
        modifiedTestEpic = createTestEpic(MODIFIED_TEST_TITLE, MODIFIED_TEST_DESCRIPTION);
        emptySubtask = createTestSubtask();
        testSubtask = createTestSubtask(TEST_TITLE, TEST_DESCRIPTION, TEST_DURATION, TEST_START_TIME, TEST_STATUS);
        modifiedTestSubtask = createTestSubtask(MODIFIED_TEST_TITLE, MODIFIED_TEST_DESCRIPTION, MODIFIED_TEST_DURATION,
                MODIFIED_TEST_START_TIME, MODIFIED_TEST_STATUS);
    }

    @Test
    public void shouldCreateFileBackedTaskManagerOfInterfaceType() {
        assertNotNull(manager, "task manager was not created");
    }

    @Test
    public void shouldThrowWhenFileIsNull() {
        Exception exception = assertThrows(NullPointerException.class,
                () -> new FileBackedTaskManager(null, historyManager));
        assertEquals("cannot start: file is null", exception.getMessage(), WRONG_EXCEPTION_MESSAGE);
    }

    @Test
    public void shouldImmediatelyThrowWhenCannotInitializeDataFile() {
        String filename = ".";
        String expectedMessage = "cannot write to file \"" + filename + "\"";

        Exception exception = assertThrows(ManagerSaveException.class,
                () -> new FileBackedTaskManager(new File(filename), historyManager));
        assertEquals(expectedMessage, exception.getMessage(), WRONG_EXCEPTION_MESSAGE);
    }

    @Test
    public void shouldSaveWhenNewTaskAdded() throws IOException {
        String expectedString = """
                id,type,name,status,description,duration,start,epic
                %%d,TASK,"%s",%s,"%s",%s,%s,
                """.formatted(testTask.getTitle(), testTask.getStatus(), testTask.getDescription(),
                testTask.getDuration(), testTask.getStartTime());

        long id = manager.addTask(testTask);

        expectedString = expectedString.formatted(id);
        String actualString = Files.readString(file.toPath());
        assertEquals(expectedString, actualString, WRONG_FILE_FORMAT);
    }

    @Test
    public void shouldSaveWhenTaskUpdated() throws IOException {
        long id = manager.addTask(testTask);
        String expectedString = """
                id,type,name,status,description,duration,start,epic
                %d,TASK,"%s",%s,"%s",%s,%s,
                """.formatted(id, modifiedTestTask.getTitle(), modifiedTestTask.getStatus(),
                modifiedTestTask.getDescription(), modifiedTestTask.getDuration(), modifiedTestTask.getStartTime());
        modifiedTestTask.setId(id);

        manager.updateTask(modifiedTestTask);

        String actualString = Files.readString(file.toPath());
        assertEquals(expectedString, actualString, WRONG_FILE_FORMAT);
    }

    @Test
    public void shouldCorrectlySaveTaskWithNullFields() throws IOException {
        String expectedString = """
                id,type,name,status,description,duration,start,epic
                %d,TASK,null,null,null,null,null,
                """;

        long id = manager.addTask(emptyTask);

        expectedString = expectedString.formatted(id);
        String actualString = Files.readString(file.toPath());
        assertEquals(expectedString, actualString, WRONG_FILE_FORMAT);
    }

    @Test
    public void shouldSaveWhenTaskRemoved() throws IOException {
        long id = manager.addTask(testTask);
        String expectedString = """
                id,type,name,status,description,duration,start,epic
                """;

        manager.removeTask(id);

        String actualString = Files.readString(file.toPath());
        assertEquals(expectedString, actualString, WRONG_FILE_FORMAT);
    }

    @Test
    public void shouldSaveWhenNewEpicAdded() throws IOException {
        String expectedString = """
                id,type,name,status,description,duration,start,epic
                %%d,EPIC,"%s",,"%s",,,
                """.formatted(testEpic.getTitle(), testEpic.getDescription());

        long id = manager.addEpic(testEpic);

        expectedString = expectedString.formatted(id);
        String actualString = Files.readString(file.toPath());
        assertEquals(expectedString, actualString, WRONG_FILE_FORMAT);
    }

    @Test
    public void shouldSaveWhenEpicUpdated() throws IOException {
        long id = manager.addEpic(testEpic);
        String expectedString = """
                id,type,name,status,description,duration,start,epic
                %d,EPIC,"%s",,"%s",,,
                """.formatted(id, modifiedTestEpic.getTitle(), modifiedTestEpic.getDescription());
        modifiedTestEpic.setId(id);

        manager.updateEpic(modifiedTestEpic);

        String actualString = Files.readString(file.toPath());
        assertEquals(expectedString, actualString, WRONG_FILE_FORMAT);
    }

    @Test
    public void shouldCorrectlySaveEpicWithNullFields() throws IOException {
        String expectedString = """
                id,type,name,status,description,duration,start,epic
                %d,EPIC,null,,null,,,
                """;

        long id = manager.addEpic(emptyEpic);

        expectedString = expectedString.formatted(id);
        String actualString = Files.readString(file.toPath());
        assertEquals(expectedString, actualString, WRONG_FILE_FORMAT);
    }

    @Test
    public void shouldSaveWhenEpicRemoved() throws IOException {
        long id = manager.addEpic(testEpic);
        String expectedString = """
                id,type,name,status,description,duration,start,epic
                """;

        manager.removeEpic(id);

        String actualString = Files.readString(file.toPath());
        assertEquals(expectedString, actualString, WRONG_FILE_FORMAT);
    }

    @Test
    public void shouldSaveWhenNewSubtaskAdded() throws IOException {
        String expectedString = """
                id,type,name,status,description,duration,start,epic
                %%d,EPIC,"%s",,"%s",,,
                %%d,SUBTASK,"%s",%s,"%s",%s,%s,%%d
                """.formatted(testEpic.getTitle(), testEpic.getDescription(), testSubtask.getTitle(),
                testSubtask.getStatus(), testSubtask.getDescription(), testSubtask.getDuration(),
                testSubtask.getStartTime());
        long epicId = manager.addEpic(testEpic);
        testSubtask.setEpicId(epicId);

        long subtaskId = manager.addSubtask(testSubtask);

        expectedString = expectedString.formatted(epicId, subtaskId, epicId);
        String actualString = Files.readString(file.toPath());
        assertEquals(expectedString, actualString, WRONG_FILE_FORMAT);
    }

    @Test
    public void shouldSaveWhenSubtaskUpdated() throws IOException {
        String expectedString = """
                id,type,name,status,description,duration,start,epic
                %%d,EPIC,"%s",,"%s",,,
                %%d,SUBTASK,"%s",%s,"%s",%s,%s,%%d
                """.formatted(testEpic.getTitle(), testEpic.getDescription(), modifiedTestSubtask.getTitle(),
                modifiedTestSubtask.getStatus(), modifiedTestSubtask.getDescription(),
                modifiedTestSubtask.getDuration(), modifiedTestSubtask.getStartTime());
        long epicId = manager.addEpic(testEpic);
        testSubtask.setEpicId(epicId);
        long subtaskId = manager.addSubtask(testSubtask);
        modifiedTestSubtask.setEpicId(epicId);
        modifiedTestSubtask.setId(subtaskId);

        manager.updateSubtask(modifiedTestSubtask);

        expectedString = expectedString.formatted(epicId, subtaskId, epicId);
        String actualString = Files.readString(file.toPath());
        assertEquals(expectedString, actualString, WRONG_FILE_FORMAT);
    }

    @Test
    public void shouldCorrectlySaveSubtaskWithNullFields() throws IOException {
        String expectedString = """
                id,type,name,status,description,duration,start,epic
                %%d,EPIC,"%s",,"%s",,,
                %%d,SUBTASK,null,null,null,null,null,%%d
                """.formatted(testEpic.getTitle(), testEpic.getDescription());
        long epicId = manager.addEpic(testEpic);
        emptySubtask.setEpicId(epicId);

        long subtaskId = manager.addSubtask(emptySubtask);

        expectedString = expectedString.formatted(epicId, subtaskId, epicId);
        String actualString = Files.readString(file.toPath());
        assertEquals(expectedString, actualString, WRONG_FILE_FORMAT);
    }

    @Test
    public void shouldSaveWhenSubtaskRemoved() throws IOException {
        String expectedString = """
                id,type,name,status,description,duration,start,epic
                %%d,EPIC,"%s",,"%s",,,
                """.formatted(testEpic.getTitle(), testEpic.getDescription());
        long epicId = manager.addEpic(testEpic);
        testSubtask.setEpicId(epicId);
        long subtaskId = manager.addSubtask(testSubtask);

        manager.removeSubtask(subtaskId);

        expectedString = expectedString.formatted(epicId);
        String actualString = Files.readString(file.toPath());
        assertEquals(expectedString, actualString, WRONG_FILE_FORMAT);
    }

    @Test
    public void shouldSaveWhenAllTasksRemoved() throws IOException {
        manager.addTask(testTask);
        manager.addTask(modifiedTestTask);
        String expectedString = """
                id,type,name,status,description,duration,start,epic
                """;

        manager.removeTasks();

        String actualString = Files.readString(file.toPath());
        assertEquals(expectedString, actualString, WRONG_FILE_FORMAT);
    }

    @Test
    public void shouldSaveWhenAllEpicsRemoved() throws IOException {
        manager.addEpic(testEpic);
        manager.addEpic(modifiedTestEpic);
        String expectedString = """
                id,type,name,status,description,duration,start,epic
                """;

        manager.removeEpics();

        String actualString = Files.readString(file.toPath());
        assertEquals(expectedString, actualString, WRONG_FILE_FORMAT);
    }

    @Test
    public void shouldSaveWhenAllSubtasksRemoved() throws IOException {
        String expectedString = """
                id,type,name,status,description,duration,start,epic
                %%d,EPIC,"%s",,"%s",,,
                """.formatted(testEpic.getTitle(), testEpic.getDescription());
        long epicId = manager.addEpic(testEpic);
        testSubtask.setEpicId(epicId);
        manager.addSubtask(testSubtask);
        modifiedTestSubtask.setEpicId(epicId);
        manager.addSubtask(modifiedTestSubtask);

        manager.removeSubtasks();

        expectedString = expectedString.formatted(epicId);
        String actualString = Files.readString(file.toPath());
        assertEquals(expectedString, actualString, WRONG_FILE_FORMAT);
    }

    @Test
    public void shouldLoadTaskFromFile() throws IOException {
        fillTestFileWithData("""
                id,type,name,status,description,duration,start,epic
                1,TASK,"Title",IN_PROGRESS,"Description",30,2000-05-01T13:30,
                """);

        manager = FileBackedTaskManager.loadFromFile(file, historyManager);

        List<Task> tasks = manager.getTasks();
        assertEquals(1, tasks.size(), "list should contain exactly 1 element");
        assertEquals(TaskType.TASK, tasks.getFirst().getType(), "element in list should be of TASK type");
        Task task = tasks.getFirst();
        assertEquals(TEST_TASK_ID, task.getId(), "task id loaded incorrectly");
        assertEquals(TEST_TITLE, task.getTitle(), "task title loaded incorrectly");
        assertEquals(TEST_DESCRIPTION, task.getDescription(), "task description loaded incorrectly");
        assertEquals(TEST_DURATION, task.getDuration(), "task duration loaded incorrectly");
        assertEquals(TEST_START_TIME, task.getStartTime(), "task start time loaded incorrectly");
        assertEquals(TEST_STATUS, task.getStatus(), "task status loaded incorrectly");
    }

    @Test
    public void shouldLoadTaskWithNullFieldsFromFile() throws IOException {
        fillTestFileWithData("""
                id,type,name,status,description,duration,start,epic
                1,TASK,null,null,null,null,null,
                """);

        manager = FileBackedTaskManager.loadFromFile(file, historyManager);

        List<Task> tasks = manager.getTasks();
        assertEquals(1, tasks.size(), "list should contain exactly 1 element");
        assertEquals(TaskType.TASK, tasks.getFirst().getType(), "element in list should be of TASK type");
        Task task = tasks.getFirst();
        assertEquals(TEST_TASK_ID, task.getId(), "task id loaded incorrectly");
        assertNull(task.getTitle(), "task title loaded incorrectly");
        assertNull(task.getDescription(), "task description loaded incorrectly");
        assertNull(task.getDuration(), "task duration loaded incorrectly");
        assertNull(task.getStartTime(), "task start time loaded incorrectly");
        assertNull(task.getStatus(), "task status loaded incorrectly");
    }

    @Test
    public void shouldLoadEpicFromFile() throws IOException {
        fillTestFileWithData("""
                id,type,name,status,description,duration,start,epic
                2,EPIC,"Title",,"Description",,,
                """);

        manager = FileBackedTaskManager.loadFromFile(file, historyManager);

        List<Epic> epics = manager.getEpics();
        assertEquals(1, epics.size(), "list should contain exactly 1 element");
        assertEquals(TaskType.EPIC, epics.getFirst().getType(), "element in list should be of EPIC type");
        Epic epic = epics.getFirst();
        assertEquals(TEST_EPIC_ID, epic.getId(), "epic id loaded incorrectly");
        assertEquals(TEST_TITLE, epic.getTitle(), "epic title loaded incorrectly");
        assertEquals(TEST_DESCRIPTION, epic.getDescription(), "epic description loaded incorrectly");
        assertTrue(epic.getSubtasks().isEmpty(), "list of epic subtasks should be empty");
    }

    @Test
    public void shouldLoadEpicWithNullFieldsFromFile() throws IOException {
        fillTestFileWithData("""
                id,type,name,status,description,duration,start,epic
                2,EPIC,null,,null,,,
                """);

        manager = FileBackedTaskManager.loadFromFile(file, historyManager);

        List<Epic> epics = manager.getEpics();
        assertEquals(1, epics.size(), "list should contain exactly 1 element");
        assertEquals(TaskType.EPIC, epics.getFirst().getType(), "element in list should be of EPIC type");
        Epic epic = epics.getFirst();
        assertEquals(TEST_EPIC_ID, epic.getId(), "epic id loaded incorrectly");
        assertNull(epic.getTitle(), "epic title loaded incorrectly");
        assertNull(epic.getDescription(), "epic description loaded incorrectly");
        assertTrue(epic.getSubtasks().isEmpty(), "list of epic subtasks should be empty");
    }

    @Test
    public void shouldLoadSubtaskFromFile() throws IOException {
        fillTestFileWithData("""
                id,type,name,status,description,duration,start,epic
                2,EPIC,"Title",,"Description",,,
                3,SUBTASK,"Title",IN_PROGRESS,"Description",30,2000-05-01T13:30,2
                """);

        manager = FileBackedTaskManager.loadFromFile(file, historyManager);

        List<Subtask> subtasks = manager.getSubtasks();
        assertEquals(1, subtasks.size(), "list should contain exactly 1 element");
        assertEquals(TaskType.SUBTASK, subtasks.getFirst().getType(), "element in list should be of SUBTASK type");
        Subtask subtask = subtasks.getFirst();
        assertEquals(TEST_SUBTASK_ID, subtask.getId(), "subtask id loaded incorrectly");
        assertEquals(TEST_EPIC_ID, subtask.getEpicId(), "epic id of subtask loaded incorrectly");
        assertEquals(TEST_TITLE, subtask.getTitle(), "subtask title loaded incorrectly");
        assertEquals(TEST_DESCRIPTION, subtask.getDescription(), "subtask description loaded incorrectly");
        assertEquals(TEST_DURATION, subtask.getDuration(), "subtask duration loaded incorrectly");
        assertEquals(TEST_START_TIME, subtask.getStartTime(), "subtask start time loaded incorrectly");
        assertEquals(TEST_STATUS, subtask.getStatus(), "subtask status loaded incorrectly");
        Epic epic = manager.getEpics().getFirst();
        assertEquals(1, epic.getSubtasks().size(), "list of epic subtasks should contain exactly 1 element");
        subtask = epic.getSubtasks().getFirst();
        assertEquals(TEST_SUBTASK_ID, subtask.getId(), "subtask id loaded incorrectly");
        assertEquals(TEST_EPIC_ID, subtask.getEpicId(), "epic id of subtask loaded incorrectly");
        assertEquals(TEST_TITLE, subtask.getTitle(), "subtask title loaded incorrectly");
        assertEquals(TEST_DESCRIPTION, subtask.getDescription(), "subtask description loaded incorrectly");
        assertEquals(TEST_DURATION, subtask.getDuration(), "subtask duration loaded incorrectly");
        assertEquals(TEST_START_TIME, subtask.getStartTime(), "subtask start time loaded incorrectly");
        assertEquals(TEST_STATUS, subtask.getStatus(), "subtask status loaded incorrectly");
    }

    @Test
    public void shouldLoadSubtaskWithNullFieldsFromFile() throws IOException {
        fillTestFileWithData("""
                id,type,name,status,description,duration,start,epic
                2,EPIC,"Title",,"Description",,,
                3,SUBTASK,null,null,null,null,null,2
                """);

        manager = FileBackedTaskManager.loadFromFile(file, historyManager);

        List<Subtask> subtasks = manager.getSubtasks();
        assertEquals(1, subtasks.size(), "list should contain exactly 1 element");
        assertEquals(TaskType.SUBTASK, subtasks.getFirst().getType(), "element in list should be of SUBTASK type");
        Subtask subtask = subtasks.getFirst();
        assertEquals(TEST_SUBTASK_ID, subtask.getId(), "subtask id loaded incorrectly");
        assertEquals(TEST_EPIC_ID, subtask.getEpicId(), "epic id of subtask loaded incorrectly");
        assertNull(subtask.getTitle(), "subtask title loaded incorrectly");
        assertNull(subtask.getDescription(), "subtask description loaded incorrectly");
        assertNull(subtask.getDuration(), "subtask duration loaded incorrectly");
        assertNull(subtask.getStartTime(), "subtask start time loaded incorrectly");
        assertNull(subtask.getStatus(), "subtask status loaded incorrectly");
        Epic epic = manager.getEpics().getFirst();
        assertEquals(1, epic.getSubtasks().size(), "list of epic subtasks should contain exactly 1 element");
        subtask = epic.getSubtasks().getFirst();
        assertEquals(TEST_SUBTASK_ID, subtask.getId(), "subtask id loaded incorrectly");
        assertEquals(TEST_EPIC_ID, subtask.getEpicId(), "epic id of subtask loaded incorrectly");
        assertNull(subtask.getTitle(), "subtask title loaded incorrectly");
        assertNull(subtask.getDescription(), "subtask description loaded incorrectly");
        assertNull(subtask.getDuration(), "subtask duration loaded incorrectly");
        assertNull(subtask.getStartTime(), "subtask start time loaded incorrectly");
        assertNull(subtask.getStatus(), "subtask status loaded incorrectly");
    }

    @Test
    public void shouldAddTaskAndSubtasksToListOfPrioritizedTaskAndSubtasks() throws IOException {
        fillTestFileWithData("""
                id,type,name,status,description,duration,start,epic
                1,TASK,"Modified Title",DONE,"Modified Description",90,2000-05-01T15:00,
                2,EPIC,null,,null,,,
                3,SUBTASK,"Title",IN_PROGRESS,"Description",30,2000-05-01T13:30,2
                4,TASK,"Title",IN_PROGRESS,"Description",null,null,
                5,SUBTASK,"Modified Title",DONE,"Modified Description",null,null,2
                """);

        manager = FileBackedTaskManager.loadFromFile(file, historyManager);
        List<Task> tasks = manager.getPrioritizedTasks();

        assertNotNull(tasks, "should return list of tasks");
        assertEquals(2, tasks.size(), "list should contain exactly 2 elements");
        assertEquals(TaskType.SUBTASK, tasks.getFirst().getType(), "1st element should be of SUBTASK type");
        Subtask savedSubtask = (Subtask) tasks.getFirst();
        assertEquals(TEST_SUBTASK_ID, savedSubtask.getId(), "subtask id should not change");
        assertEquals(TEST_EPIC_ID, savedSubtask.getEpicId(), "epic id of status should not change");
        assertEquals(TEST_TITLE, savedSubtask.getTitle(), "subtask title should not change");
        assertEquals(TEST_DESCRIPTION, savedSubtask.getDescription(), "subtask description should not change");
        assertEquals(TEST_DURATION, savedSubtask.getDuration(), "subtask duration should not change");
        assertEquals(TEST_START_TIME, savedSubtask.getStartTime(), "subtask start time should not change");
        assertEquals(TEST_STATUS, savedSubtask.getStatus(), "subtask status should not change");
        assertEquals(TaskType.TASK, tasks.getLast().getType(), "2nd element should be of TASK type");
        Task savedTask = tasks.getLast();
        assertEquals(TEST_TASK_ID, savedTask.getId(), "task id should not change");
        assertEquals(MODIFIED_TEST_TITLE, savedTask.getTitle(), "task title should not change");
        assertEquals(MODIFIED_TEST_DESCRIPTION, savedTask.getDescription(), "task description should not change");
        assertEquals(MODIFIED_TEST_DURATION, savedTask.getDuration(), "task duration should not change");
        assertEquals(MODIFIED_TEST_START_TIME, savedTask.getStartTime(), "task start time should not change");
        assertEquals(MODIFIED_TEST_STATUS, savedTask.getStatus(), "task status should not change");
    }

    @Test
    public void shouldAddTaskWhenExactlyAfterAnotherPrioritizedTask() throws IOException {
        fillTestFileWithData("""
                id,type,name,status,description,duration,start,epic
                1,TASK,"Title",IN_PROGRESS,"Description",30,2000-05-01T13:00,
                2,EPIC,null,,null,,,
                3,SUBTASK,"Title",IN_PROGRESS,"Description",30,2000-05-01T13:30,2
                """);

        manager = FileBackedTaskManager.loadFromFile(file, historyManager);
        List<Task> tasks = manager.getPrioritizedTasks();

        assertNotNull(tasks, "should return list of tasks");
        assertEquals(2, tasks.size(), "list should contain exactly 2 elements");
        assertEquals(TaskType.TASK, tasks.getFirst().getType(), "1st element should be of TASK type");
        Task savedTask = tasks.getFirst();
        assertEquals(TEST_TASK_ID, savedTask.getId(), "task id should not change");
        assertEquals(TEST_TITLE, savedTask.getTitle(), "task title should not change");
        assertEquals(TEST_DESCRIPTION, savedTask.getDescription(), "task description should not change");
        assertEquals(TEST_DURATION, savedTask.getDuration(), "task duration should not change");
        assertEquals(TEST_START_TIME.minusMinutes(TEST_DURATION), savedTask.getStartTime(),
                "task start time should not change");
        assertEquals(TEST_STATUS, savedTask.getStatus(), "task status should not change");
        assertEquals(TaskType.SUBTASK, tasks.getLast().getType(), "2nd element should be of SUBTASK type");
        Subtask savedSubtask = (Subtask) tasks.getLast();
        assertEquals(TEST_SUBTASK_ID, savedSubtask.getId(), "subtask id should not change");
        assertEquals(TEST_EPIC_ID, savedSubtask.getEpicId(), "epic id of status should not change");
        assertEquals(TEST_TITLE, savedSubtask.getTitle(), "subtask title should not change");
        assertEquals(TEST_DESCRIPTION, savedSubtask.getDescription(), "subtask description should not change");
        assertEquals(TEST_DURATION, savedSubtask.getDuration(), "subtask duration should not change");
        assertEquals(TEST_START_TIME, savedSubtask.getStartTime(), "subtask start time should not change");
        assertEquals(TEST_STATUS, savedSubtask.getStatus(), "subtask status should not change");
    }

    @Test
    public void shouldAddTaskWhenExactlyBeforeAnotherPrioritizedTask() throws IOException {
        fillTestFileWithData("""
                id,type,name,status,description,duration,start,epic
                1,TASK,"Title",IN_PROGRESS,"Description",30,2000-05-01T14:00,
                2,EPIC,null,,null,,,
                3,SUBTASK,"Title",IN_PROGRESS,"Description",30,2000-05-01T13:30,2
                """);

        manager = FileBackedTaskManager.loadFromFile(file, historyManager);
        List<Task> tasks = manager.getPrioritizedTasks();

        assertNotNull(tasks, "should return list of tasks");
        assertEquals(2, tasks.size(), "list should contain exactly 2 elements");
        assertEquals(TaskType.SUBTASK, tasks.getFirst().getType(), "1st element should be of SUBTASK type");
        Subtask savedSubtask = (Subtask) tasks.getFirst();
        assertEquals(TEST_SUBTASK_ID, savedSubtask.getId(), "subtask id should not change");
        assertEquals(TEST_EPIC_ID, savedSubtask.getEpicId(), "epic id of status should not change");
        assertEquals(TEST_TITLE, savedSubtask.getTitle(), "subtask title should not change");
        assertEquals(TEST_DESCRIPTION, savedSubtask.getDescription(), "subtask description should not change");
        assertEquals(TEST_DURATION, savedSubtask.getDuration(), "subtask duration should not change");
        assertEquals(TEST_START_TIME, savedSubtask.getStartTime(), "subtask start time should not change");
        assertEquals(TEST_STATUS, savedSubtask.getStatus(), "subtask status should not change");
        assertEquals(TaskType.TASK, tasks.getLast().getType(), "2nd element should be of TASK type");
        Task savedTask = tasks.getLast();
        assertEquals(TEST_TASK_ID, savedTask.getId(), "task id should not change");
        assertEquals(TEST_TITLE, savedTask.getTitle(), "task title should not change");
        assertEquals(TEST_DESCRIPTION, savedTask.getDescription(), "task description should not change");
        assertEquals(TEST_DURATION, savedTask.getDuration(), "task duration should not change");
        assertEquals(TEST_START_TIME.plusMinutes(TEST_DURATION), savedTask.getStartTime(),
                "task start time should not change");
        assertEquals(TEST_STATUS, savedTask.getStatus(), "task status should not change");
    }

    @Test
    public void shouldResetLastUsedIdWhenLoadFile() throws IOException {
        fillTestFileWithData("""
                id,type,name,status,description,duration,start,epic
                1000,TASK,"Title",IN_PROGRESS,"Description",30,2000-05-01T13:30,
                """);

        manager = FileBackedTaskManager.loadFromFile(file, historyManager);
        long id = manager.addTask(modifiedTestTask);

        assertEquals(1001, id, "last used id loaded incorrectly");
    }

    @Test
    public void shouldThrowWhenFileToLoadIsNull() {
        Exception exception = assertThrows(NullPointerException.class,
                () -> FileBackedTaskManager.loadFromFile(null, historyManager));
        assertEquals("cannot start: file is null", exception.getMessage(), WRONG_EXCEPTION_MESSAGE);
    }

    @Test
    public void shouldThrowWhenCannotLoadFromFile() {
        String filename = ".";
        String expectedMessage = "cannot load from file \"" + filename + "\"";

        Exception exception = assertThrows(ManagerLoadException.class,
                () -> FileBackedTaskManager.loadFromFile(new File(filename), historyManager));
        assertEquals(expectedMessage, exception.getMessage(), WRONG_EXCEPTION_MESSAGE);
    }

    @Test
    public void shouldThrowWhenFileIsEmpty() throws IOException {
        file = File.createTempFile("kanban", null);
        String expectedMessage = "\"id\" expected (" + file + ":1:1)";

        ManagerLoadException exception = assertThrows(ManagerLoadException.class,
                () -> FileBackedTaskManager.loadFromFile(file, historyManager));
        assertEquals(expectedMessage, exception.getMessage(), WRONG_EXCEPTION_MESSAGE);
    }

    @Test
    public void shouldThrowWhenFileContainsNoHeader() throws IOException {
        fillTestFileWithData("""
                """);
        String expectedMessage = "\"id\" expected (" + file + ":1:1)";

        Exception exception = assertThrows(ManagerLoadException.class,
                () -> FileBackedTaskManager.loadFromFile(file, historyManager));
        assertEquals(expectedMessage, exception.getMessage(), WRONG_EXCEPTION_MESSAGE);
    }

    @Test
    public void shouldThrowWhenCannotFindIdInFileHeader() throws IOException {
        fillTestFileWithData("""
                text,type,name,status,description,duration,start,epic
                """);
        String expectedMessage = "\"id\" expected (" + file + ":1:1)";

        Exception exception = assertThrows(ManagerLoadException.class,
                () -> FileBackedTaskManager.loadFromFile(file, historyManager));
        assertEquals(expectedMessage, exception.getMessage(), WRONG_EXCEPTION_MESSAGE);
    }

    @Test
    public void shouldThrowWhenCannotFindFirstCommaInFileHeader() throws IOException {
        fillTestFileWithData("""
                id:type:name:status:description:duration,start,epic
                """);
        String expectedMessage = "comma expected (" + file + ":1:3)";

        Exception exception = assertThrows(ManagerLoadException.class,
                () -> FileBackedTaskManager.loadFromFile(file, historyManager));
        assertEquals(expectedMessage, exception.getMessage(), WRONG_EXCEPTION_MESSAGE);
    }

    @Test
    public void shouldThrowWhenCannotFindTypeInFileHeader() throws IOException {
        fillTestFileWithData("""
                id,text,name,status,description,duration,start,epic
                """);
        String expectedMessage = "\"type\" expected (" + file + ":1:4)";

        Exception exception = assertThrows(ManagerLoadException.class,
                () -> FileBackedTaskManager.loadFromFile(file, historyManager));
        assertEquals(expectedMessage, exception.getMessage(), WRONG_EXCEPTION_MESSAGE);
    }

    @Test
    public void shouldThrowWhenCannotFindSecondCommaInFileHeader() throws IOException {
        fillTestFileWithData("""
                id,type:name,status,description,duration,start,epic
                """);
        String expectedMessage = "comma expected (" + file + ":1:8)";

        Exception exception = assertThrows(ManagerLoadException.class,
                () -> FileBackedTaskManager.loadFromFile(file, historyManager));
        assertEquals(expectedMessage, exception.getMessage(), WRONG_EXCEPTION_MESSAGE);
    }

    @Test
    public void shouldThrowWhenCannotFindNameInFileHeader() throws IOException {
        fillTestFileWithData("""
                id,type,text,status,description,duration,start,epic
                """);
        String expectedMessage = "\"name\" expected (" + file + ":1:9)";

        Exception exception = assertThrows(ManagerLoadException.class,
                () -> FileBackedTaskManager.loadFromFile(file, historyManager));
        assertEquals(expectedMessage, exception.getMessage(), WRONG_EXCEPTION_MESSAGE);
    }

    @Test
    public void shouldThrowWhenCannotFindThirdCommaInFileHeader() throws IOException {
        fillTestFileWithData("""
                id,type,name:status,description,duration,start,epic
                """);
        String expectedMessage = "comma expected (" + file + ":1:13)";

        Exception exception = assertThrows(ManagerLoadException.class,
                () -> FileBackedTaskManager.loadFromFile(file, historyManager));
        assertEquals(expectedMessage, exception.getMessage(), WRONG_EXCEPTION_MESSAGE);
    }

    @Test
    public void shouldThrowWhenCannotFindStatusInFileHeader() throws IOException {
        fillTestFileWithData("""
                id,type,name,text,description,duration,start,epic
                """);
        String expectedMessage = "\"status\" expected (" + file + ":1:14)";

        Exception exception = assertThrows(ManagerLoadException.class,
                () -> FileBackedTaskManager.loadFromFile(file, historyManager));
        assertEquals(expectedMessage, exception.getMessage(), WRONG_EXCEPTION_MESSAGE);
    }

    @Test
    public void shouldThrowWhenCannotFindFourthCommaInFileHeader() throws IOException {
        fillTestFileWithData("""
                id,type,name,status:description,duration,start,epic
                """);
        String expectedMessage = "comma expected (" + file + ":1:20)";

        Exception exception = assertThrows(ManagerLoadException.class,
                () -> FileBackedTaskManager.loadFromFile(file, historyManager));
        assertEquals(expectedMessage, exception.getMessage(), WRONG_EXCEPTION_MESSAGE);
    }

    @Test
    public void shouldThrowWhenCannotFindDescriptionInFileHeader() throws IOException {
        fillTestFileWithData("""
                id,type,name,status,text,duration,start,epic
                """);
        String expectedMessage = "\"description\" expected (" + file + ":1:21)";

        Exception exception = assertThrows(ManagerLoadException.class,
                () -> FileBackedTaskManager.loadFromFile(file, historyManager));
        assertEquals(expectedMessage, exception.getMessage(), WRONG_EXCEPTION_MESSAGE);
    }

    @Test
    public void shouldThrowWhenCannotFindFifthCommaInFileHeader() throws IOException {
        fillTestFileWithData("""
                id,type,name,status,description:duration,start,epic
                """);
        String expectedMessage = "comma expected (" + file + ":1:32)";

        Exception exception = assertThrows(ManagerLoadException.class,
                () -> FileBackedTaskManager.loadFromFile(file, historyManager));
        assertEquals(expectedMessage, exception.getMessage(), WRONG_EXCEPTION_MESSAGE);
    }

    @Test
    public void shouldThrowWhenCannotFindDurationInFileHeader() throws IOException {
        fillTestFileWithData("""
                id,type,name,status,description,text,start,epic
                """);
        String expectedMessage = "\"duration\" expected (" + file + ":1:33)";

        Exception exception = assertThrows(ManagerLoadException.class,
                () -> FileBackedTaskManager.loadFromFile(file, historyManager));
        assertEquals(expectedMessage, exception.getMessage(), WRONG_EXCEPTION_MESSAGE);
    }

    @Test
    public void shouldThrowWhenCannotFindSixthCommaInFileHeader() throws IOException {
        fillTestFileWithData("""
                id,type,name,status,description,duration:start,epic
                """);
        String expectedMessage = "comma expected (" + file + ":1:41)";

        Exception exception = assertThrows(ManagerLoadException.class,
                () -> FileBackedTaskManager.loadFromFile(file, historyManager));
        assertEquals(expectedMessage, exception.getMessage(), WRONG_EXCEPTION_MESSAGE);
    }

    @Test
    public void shouldThrowWhenCannotFindStartInFileHeader() throws IOException {
        fillTestFileWithData("""
                id,type,name,status,description,duration,text,epic
                """);
        String expectedMessage = "\"start\" expected (" + file + ":1:42)";

        Exception exception = assertThrows(ManagerLoadException.class,
                () -> FileBackedTaskManager.loadFromFile(file, historyManager));
        assertEquals(expectedMessage, exception.getMessage(), WRONG_EXCEPTION_MESSAGE);
    }

    @Test
    public void shouldThrowWhenCannotFindSeventhCommaInFileHeader() throws IOException {
        fillTestFileWithData("""
                id,type,name,status,description,duration,start:epic
                """);
        String expectedMessage = "comma expected (" + file + ":1:47)";

        Exception exception = assertThrows(ManagerLoadException.class,
                () -> FileBackedTaskManager.loadFromFile(file, historyManager));
        assertEquals(expectedMessage, exception.getMessage(), WRONG_EXCEPTION_MESSAGE);
    }

    @Test
    public void shouldThrowWhenCannotFindEpicInFileHeader() throws IOException {
        fillTestFileWithData("""
                id,type,name,status,description,duration,start,text
                """);
        String expectedMessage = "\"epic\" expected (" + file + ":1:48)";

        Exception exception = assertThrows(ManagerLoadException.class,
                () -> FileBackedTaskManager.loadFromFile(file, historyManager));
        assertEquals(expectedMessage, exception.getMessage(), WRONG_EXCEPTION_MESSAGE);
    }

    @Test
    public void shouldThrowWhenFileHeaderHasExcessiveCharacters() throws IOException {
        fillTestFileWithData("""
                id,type,name,status,description,duration,start,epic,
                """);
        String expectedMessage = "unexpected data (" + file + ":1:52)";

        Exception exception = assertThrows(ManagerLoadException.class,
                () -> FileBackedTaskManager.loadFromFile(file, historyManager));
        assertEquals(expectedMessage, exception.getMessage(), WRONG_EXCEPTION_MESSAGE);
    }

    @Test
    public void shouldThrowWhenTaskFromFileHasNotEnoughFields() throws IOException {
        fillTestFileWithData("""
                id,type,name,status,description,duration,start,epic
                1,TASK,"Title",IN_PROGRESS,"Description",30,2000-05-01T13:30
                """);
        String expectedMessage = "unexpected end of line (" + file + ":2:61)";

        Exception exception = assertThrows(ManagerLoadException.class,
                () -> FileBackedTaskManager.loadFromFile(file, historyManager));
        assertEquals(expectedMessage, exception.getMessage(), WRONG_EXCEPTION_MESSAGE);
    }

    @Test
    public void shouldThrowWhenEpicFromFileHasNotEnoughFields() throws IOException {
        fillTestFileWithData("""
                id,type,name,status,description,duration,start,epic
                1,EPIC,"Title",,"Description",,
                """);
        String expectedMessage = "unexpected end of line (" + file + ":2:32)";

        Exception exception = assertThrows(ManagerLoadException.class,
                () -> FileBackedTaskManager.loadFromFile(file, historyManager));
        assertEquals(expectedMessage, exception.getMessage(), WRONG_EXCEPTION_MESSAGE);
    }

    @Test
    public void shouldThrowWhenSubtaskFromFileHasNotEnoughFields() throws IOException {
        fillTestFileWithData("""
                id,type,name,status,description,duration,start,epic
                1,EPIC,"Epic",,"Epic description",,,
                2,SUBTASK,"Subtask",NEW,"Subtask description",30,2000-05-01T13:30
                """);
        String expectedMessage = "unexpected end of line (" + file + ":3:66)";

        Exception exception = assertThrows(ManagerLoadException.class,
                () -> FileBackedTaskManager.loadFromFile(file, historyManager));
        assertEquals(expectedMessage, exception.getMessage(), WRONG_EXCEPTION_MESSAGE);
    }

    @Test
    public void shouldThrowWhenTaskFromFileHasExcessiveFields() throws IOException {
        fillTestFileWithData("""
                id,type,name,status,description,duration,start,epic
                1,TASK,"Task",IN_PROGRESS,"Task description",30,2000-05-01T13:30,,
                """);
        String expectedMessage = "unexpected data (" + file + ":2:66)";

        Exception exception = assertThrows(ManagerLoadException.class,
                () -> FileBackedTaskManager.loadFromFile(file, historyManager));
        assertEquals(expectedMessage, exception.getMessage(), WRONG_EXCEPTION_MESSAGE);
    }

    @Test
    public void shouldThrowWhenEpicFromFileHasExcessiveFields() throws IOException {
        fillTestFileWithData("""
                id,type,name,status,description,duration,start,epic
                1,EPIC,"Epic",,"Epic description",,,,
                """);
        String expectedMessage = "unexpected data (" + file + ":2:37)";

        Exception exception = assertThrows(ManagerLoadException.class,
                () -> FileBackedTaskManager.loadFromFile(file, historyManager));
        assertEquals(expectedMessage, exception.getMessage(), WRONG_EXCEPTION_MESSAGE);
    }

    @Test
    public void shouldThrowWhenSubtaskFromFileHasExcessiveFields() throws IOException {
        fillTestFileWithData("""
                id,type,name,status,description,duration,start,epic
                1,EPIC,"Epic",,"Epic description",,,
                2,SUBTASK,"Subtask",NEW,"Subtask description",30,2000-05-01T13:30,1,
                """);
        String expectedMessage = "unexpected data (" + file + ":3:68)";

        Exception exception = assertThrows(ManagerLoadException.class,
                () -> FileBackedTaskManager.loadFromFile(file, historyManager));
        assertEquals(expectedMessage, exception.getMessage(), WRONG_EXCEPTION_MESSAGE);
    }

    @Test
    public void shouldThrowWhenTaskIdFromFileNotNumber() throws IOException {
        fillTestFileWithData("""
                id,type,name,status,description,duration,start,epic
                one,TASK,"Title",IN_PROGRESS,"Description",30,2000-05-01T13:30,
                """);
        String expectedMessage = "number expected (" + file + ":2:1)";

        Exception exception = assertThrows(ManagerLoadException.class,
                () -> FileBackedTaskManager.loadFromFile(file, historyManager));
        assertEquals(expectedMessage, exception.getMessage(), WRONG_EXCEPTION_MESSAGE);
    }

    @Test
    public void shouldThrowWhenTaskFromFileHasUnknownType() throws IOException {
        fillTestFileWithData("""
                id,type,name,status,description,duration,start,epic
                1,text,"Title",IN_PROGRESS,"Description",30,2000-05-01T13:30,
                """);
        String expectedMessage = "unknown task type (" + file + ":2:3)";

        Exception exception = assertThrows(ManagerLoadException.class,
                () -> FileBackedTaskManager.loadFromFile(file, historyManager));
        assertEquals(expectedMessage, exception.getMessage(), WRONG_EXCEPTION_MESSAGE);
    }

    @Test
    public void shouldThrowWhenTaskTitleFromFileDoesNotStartWithQuote() throws IOException {
        fillTestFileWithData("""
                id,type,name,status,description,duration,start,epic
                1,TASK,Title",IN_PROGRESS,"Description",30,2000-05-01T13:30,
                """);
        String expectedMessage = "unexpected double quote (" + file + ":2:13)";

        Exception exception = assertThrows(ManagerLoadException.class,
                () -> FileBackedTaskManager.loadFromFile(file, historyManager));
        assertEquals(expectedMessage, exception.getMessage(), WRONG_EXCEPTION_MESSAGE);
    }

    @Test
    public void shouldThrowWhenTaskTitleFromFileDoesNotEndWithQuote() throws IOException {
        fillTestFileWithData("""
                id,type,name,status,description,duration,start,epic
                1,TASK,"Title,IN_PROGRESS,"Description",30,2000-05-01T13:30,
                """);
        String expectedMessage = "comma expected (" + file + ":2:28)";

        Exception exception = assertThrows(ManagerLoadException.class,
                () -> FileBackedTaskManager.loadFromFile(file, historyManager));
        assertEquals(expectedMessage, exception.getMessage(), WRONG_EXCEPTION_MESSAGE);
    }

    @Test
    public void shouldThrowWhenTaskTitleFromFileHasNoQuotes() throws IOException {
        fillTestFileWithData("""
                id,type,name,status,description,duration,start,epic
                1,TASK,Title,IN_PROGRESS,"Description",30,2000-05-01T13:30,
                """);
        String expectedMessage = "text value must be inside double quotes (" + file + ":2:8)";

        Exception exception = assertThrows(ManagerLoadException.class,
                () -> FileBackedTaskManager.loadFromFile(file, historyManager));
        assertEquals(expectedMessage, exception.getMessage(), WRONG_EXCEPTION_MESSAGE);
    }

    @Test
    public void shouldThrowWhenTaskFromFileHasUnknownStatus() throws IOException {
        fillTestFileWithData("""
                id,type,name,status,description,duration,start,epic
                1,TASK,"Title",text,"Description",30,2000-05-01T13:30,
                """);
        String expectedMessage = "unknown task status (" + file + ":2:16)";

        Exception exception = assertThrows(ManagerLoadException.class,
                () -> FileBackedTaskManager.loadFromFile(file, historyManager));
        assertEquals(expectedMessage, exception.getMessage(), WRONG_EXCEPTION_MESSAGE);
    }

    @Test
    public void shouldThrowWhenEpicFromFileHasStatus() throws IOException {
        fillTestFileWithData("""
                id,type,name,status,description,duration,start,epic
                1,EPIC,"Title",DONE,"Description",,,
                """);
        String expectedMessage = "explicit epic status (" + file + ":2:16)";

        Exception exception = assertThrows(ManagerLoadException.class,
                () -> FileBackedTaskManager.loadFromFile(file, historyManager));
        assertEquals(expectedMessage, exception.getMessage(), WRONG_EXCEPTION_MESSAGE);
    }

    @Test
    public void shouldThrowWhenTaskDescriptionFromFileDoesNotStartWithQuote() throws IOException {
        fillTestFileWithData("""
                id,type,name,status,description,duration,start,epic
                1,TASK,"Title",IN_PROGRESS,Description",30,2000-05-01T13:30,
                """);
        String expectedMessage = "unexpected double quote (" + file + ":2:39)";

        Exception exception = assertThrows(ManagerLoadException.class,
                () -> FileBackedTaskManager.loadFromFile(file, historyManager));
        assertEquals(expectedMessage, exception.getMessage(), WRONG_EXCEPTION_MESSAGE);
    }

    @Test
    public void shouldThrowWhenTaskDescriptionFromFileDoesNotEndWithQuote() throws IOException {
        fillTestFileWithData("""
                id,type,name,status,description,duration,start,epic
                1,TASK,"Title",IN_PROGRESS,"Description,30,2000-05-01T13:30,
                """);
        String expectedMessage = "double quote expected (" + file + ":2:61)";

        Exception exception = assertThrows(ManagerLoadException.class,
                () -> FileBackedTaskManager.loadFromFile(file, historyManager));
        assertEquals(expectedMessage, exception.getMessage(), WRONG_EXCEPTION_MESSAGE);
    }

    @Test
    public void shouldThrowWhenTaskDescriptionFromFileHasNoQuotes() throws IOException {
        fillTestFileWithData("""
                id,type,name,status,description,duration,start,epic
                1,TASK,"Title",IN_PROGRESS,Description,30,2000-05-01T13:30,
                """);
        String expectedMessage = "text value must be inside double quotes (" + file + ":2:28)";

        Exception exception = assertThrows(ManagerLoadException.class,
                () -> FileBackedTaskManager.loadFromFile(file, historyManager));
        assertEquals(expectedMessage, exception.getMessage(), WRONG_EXCEPTION_MESSAGE);
    }

    @Test
    public void shouldThrowWhenTaskDurationFromFileNotNumber() throws IOException {
        fillTestFileWithData("""
                id,type,name,status,description,duration,start,epic
                1,TASK,"Title",IN_PROGRESS,"Description",thirty,2000-05-01T13:30,
                """);
        String expectedMessage = "number expected (" + file + ":2:42)";

        Exception exception = assertThrows(ManagerLoadException.class,
                () -> FileBackedTaskManager.loadFromFile(file, historyManager));
        assertEquals(expectedMessage, exception.getMessage(), WRONG_EXCEPTION_MESSAGE);
    }

    @Test
    public void shouldThrowWhenTaskDurationFromFileZero() throws IOException {
        fillTestFileWithData("""
                id,type,name,status,description,duration,start,epic
                1,TASK,"Title",IN_PROGRESS,"Description",0,2000-05-01T13:30,
                """);
        String expectedMessage = "duration cannot be negative or zero (" + file + ":2:42)";

        Exception exception = assertThrows(ManagerLoadException.class,
                () -> FileBackedTaskManager.loadFromFile(file, historyManager));
        assertEquals(expectedMessage, exception.getMessage(), WRONG_EXCEPTION_MESSAGE);
    }

    @Test
    public void shouldThrowWhenTaskDurationFromFileNegative() throws IOException {
        fillTestFileWithData("""
                id,type,name,status,description,duration,start,epic
                1,TASK,"Title",IN_PROGRESS,"Description",-30,2000-05-01T13:30,
                """);
        String expectedMessage = "duration cannot be negative or zero (" + file + ":2:42)";

        Exception exception = assertThrows(ManagerLoadException.class,
                () -> FileBackedTaskManager.loadFromFile(file, historyManager));
        assertEquals(expectedMessage, exception.getMessage(), WRONG_EXCEPTION_MESSAGE);
    }

    @Test
    public void shouldThrowWhenEpicFromFileHasDuration() throws IOException {
        fillTestFileWithData("""
                id,type,name,status,description,duration,start,epic
                1,EPIC,"Title",,"Description",30,,
                """);
        String expectedMessage = "explicit epic duration (" + file + ":2:31)";

        Exception exception = assertThrows(ManagerLoadException.class,
                () -> FileBackedTaskManager.loadFromFile(file, historyManager));
        assertEquals(expectedMessage, exception.getMessage(), WRONG_EXCEPTION_MESSAGE);
    }

    @Test
    public void shouldThrowWhenTaskStartTimeFromFileNotDateTime() throws IOException {
        fillTestFileWithData("""
                id,type,name,status,description,duration,start,epic
                1,TASK,"Title",IN_PROGRESS,"Description",30,text,
                """);
        String expectedMessage = "date and time expected (" + file + ":2:45)";

        Exception exception = assertThrows(ManagerLoadException.class,
                () -> FileBackedTaskManager.loadFromFile(file, historyManager));
        assertEquals(expectedMessage, exception.getMessage(), WRONG_EXCEPTION_MESSAGE);
    }

    @Test
    public void shouldThrowWhenTaskStartTimeFromFileNullWhileDurationNotNull() throws IOException {
        fillTestFileWithData("""
                id,type,name,status,description,duration,start,epic
                1,TASK,"Title",IN_PROGRESS,"Description",30,null,
                """);
        String expectedMessage = "duration and start time must be either both set or both null (" + file + ":2:45)";

        Exception exception = assertThrows(ManagerLoadException.class,
                () -> FileBackedTaskManager.loadFromFile(file, historyManager));
        assertEquals(expectedMessage, exception.getMessage(), WRONG_EXCEPTION_MESSAGE);
    }

    @Test
    public void shouldThrowWhenTaskStartTimeFromFileNotNullWhileDurationNull() throws IOException {
        fillTestFileWithData("""
                id,type,name,status,description,duration,start,epic
                1,TASK,"Title",IN_PROGRESS,"Description",null,2000-05-01T13:30,
                """);
        String expectedMessage = "duration and start time must be either both set or both null (" + file + ":2:47)";

        Exception exception = assertThrows(ManagerLoadException.class,
                () -> FileBackedTaskManager.loadFromFile(file, historyManager));
        assertEquals(expectedMessage, exception.getMessage(), WRONG_EXCEPTION_MESSAGE);
    }

    @Test
    public void shouldThrowWhenAnotherPrioritizedTaskCoversStartTime() throws IOException {
        fillTestFileWithData("""
                id,type,name,status,description,duration,start,epic
                1,TASK,"Title",IN_PROGRESS,"Description",30,2000-05-01T13:15,
                2,EPIC,null,,null,,,
                3,SUBTASK,"Title",IN_PROGRESS,"Description",30,2000-05-01T13:30,2
                """);
        String expectedMessage = "conflict with another task for time slot (" + file + ":4:48)";

        Exception exception = assertThrows(ManagerLoadException.class,
                () -> FileBackedTaskManager.loadFromFile(file, historyManager));
        assertEquals(expectedMessage, exception.getMessage(), WRONG_EXCEPTION_MESSAGE);
    }

    @Test
    public void shouldThrowWhenAnotherPrioritizedTaskCoversEndTime() throws IOException {
        fillTestFileWithData("""
                id,type,name,status,description,duration,start,epic
                1,TASK,"Title",IN_PROGRESS,"Description",30,2000-05-01T13:45,
                2,EPIC,null,,null,,,
                3,SUBTASK,"Title",IN_PROGRESS,"Description",30,2000-05-01T13:30,2
                """);
        String expectedMessage = "conflict with another task for time slot (" + file + ":4:48)";

        Exception exception = assertThrows(ManagerLoadException.class,
                () -> FileBackedTaskManager.loadFromFile(file, historyManager));
        assertEquals(expectedMessage, exception.getMessage(), WRONG_EXCEPTION_MESSAGE);
    }

    @Test
    public void shouldThrowWhenAnotherPrioritizedTaskCoversWholeInterval() throws IOException {
        fillTestFileWithData("""
                id,type,name,status,description,duration,start,epic
                1,TASK,"Title",IN_PROGRESS,"Description",60,2000-05-01T13:15,
                2,EPIC,null,,null,,,
                3,SUBTASK,"Title",IN_PROGRESS,"Description",30,2000-05-01T13:30,2
                """);
        String expectedMessage = "conflict with another task for time slot (" + file + ":4:48)";

        Exception exception = assertThrows(ManagerLoadException.class,
                () -> FileBackedTaskManager.loadFromFile(file, historyManager));
        assertEquals(expectedMessage, exception.getMessage(), WRONG_EXCEPTION_MESSAGE);
    }

    @Test
    public void shouldThrowWhenAnotherPrioritizedTaskWithinInterval() throws IOException {
        fillTestFileWithData("""
                id,type,name,status,description,duration,start,epic
                1,TASK,"Title",IN_PROGRESS,"Description",20,2000-05-01T13:35,
                2,EPIC,null,,null,,,
                3,SUBTASK,"Title",IN_PROGRESS,"Description",30,2000-05-01T13:30,2
                """);
        String expectedMessage = "conflict with another task for time slot (" + file + ":4:48)";

        Exception exception = assertThrows(ManagerLoadException.class,
                () -> FileBackedTaskManager.loadFromFile(file, historyManager));
        assertEquals(expectedMessage, exception.getMessage(), WRONG_EXCEPTION_MESSAGE);
    }

    @Test
    public void shouldThrowWhenAnotherPrioritizedTaskWithinIntervalLeftAligned() throws IOException {
        fillTestFileWithData("""
                id,type,name,status,description,duration,start,epic
                1,TASK,"Title",IN_PROGRESS,"Description",20,2000-05-01T13:30,
                2,EPIC,null,,null,,,
                3,SUBTASK,"Title",IN_PROGRESS,"Description",30,2000-05-01T13:30,2
                """);
        String expectedMessage = "conflict with another task for time slot (" + file + ":4:48)";

        Exception exception = assertThrows(ManagerLoadException.class,
                () -> FileBackedTaskManager.loadFromFile(file, historyManager));
        assertEquals(expectedMessage, exception.getMessage(), WRONG_EXCEPTION_MESSAGE);
    }

    @Test
    public void shouldThrowWhenAnotherPrioritizedTaskWithinIntervalRightAligned() throws IOException {
        fillTestFileWithData("""
                id,type,name,status,description,duration,start,epic
                1,TASK,"Title",IN_PROGRESS,"Description",20,2000-05-01T13:40,
                2,EPIC,null,,null,,,
                3,SUBTASK,"Title",IN_PROGRESS,"Description",30,2000-05-01T13:30,2
                """);
        String expectedMessage = "conflict with another task for time slot (" + file + ":4:48)";

        Exception exception = assertThrows(ManagerLoadException.class,
                () -> FileBackedTaskManager.loadFromFile(file, historyManager));
        assertEquals(expectedMessage, exception.getMessage(), WRONG_EXCEPTION_MESSAGE);
    }

    @Test
    public void shouldThrowWhenAnotherPrioritizedTaskWithSameInterval() throws IOException {
        fillTestFileWithData("""
                id,type,name,status,description,duration,start,epic
                1,TASK,"Title",IN_PROGRESS,"Description",30,2000-05-01T13:30,
                2,EPIC,null,,null,,,
                3,SUBTASK,"Title",IN_PROGRESS,"Description",30,2000-05-01T13:30,2
                """);
        String expectedMessage = "conflict with another task for time slot (" + file + ":4:48)";

        Exception exception = assertThrows(ManagerLoadException.class,
                () -> FileBackedTaskManager.loadFromFile(file, historyManager));
        assertEquals(expectedMessage, exception.getMessage(), WRONG_EXCEPTION_MESSAGE);
    }

    @Test
    public void shouldThrowWhenEpicFromFileHasStartTime() throws IOException {
        fillTestFileWithData("""
                id,type,name,status,description,duration,start,epic
                1,EPIC,"Title",,"Description",,2000-05-01T13:30,
                """);
        String expectedMessage = "explicit epic start time (" + file + ":2:32)";

        Exception exception = assertThrows(ManagerLoadException.class,
                () -> FileBackedTaskManager.loadFromFile(file, historyManager));
        assertEquals(expectedMessage, exception.getMessage(), WRONG_EXCEPTION_MESSAGE);
    }

    @Test
    public void shouldThrowWhenTaskFromFileHasEpicId() throws IOException {
        fillTestFileWithData("""
                id,type,name,status,description,duration,start,epic
                1,EPIC,"Epic",,"Epic description",,,
                2,TASK,"Task",IN_PROGRESS,"Task description",30,2000-05-01T13:30,1
                """);
        String expectedMessage = "unexpected data (" + file + ":3:66)";

        Exception exception = assertThrows(ManagerLoadException.class,
                () -> FileBackedTaskManager.loadFromFile(file, historyManager));
        assertEquals(expectedMessage, exception.getMessage(), WRONG_EXCEPTION_MESSAGE);
    }

    @Test
    public void shouldThrowWhenEpicFromFileHasEpicId() throws IOException {
        fillTestFileWithData("""
                id,type,name,status,description,duration,start,epic
                1,EPIC,"Epic A",,"Epic A description",,,
                2,EPIC,"Epic B",,"Epic B description",,,1
                """);
        String expectedMessage = "unexpected data (" + file + ":3:41)";

        Exception exception = assertThrows(ManagerLoadException.class,
                () -> FileBackedTaskManager.loadFromFile(file, historyManager));
        assertEquals(expectedMessage, exception.getMessage(), WRONG_EXCEPTION_MESSAGE);
    }

    @Test
    public void shouldThrowWhenSubtaskFromFileHasNoEpicId() throws IOException {
        fillTestFileWithData("""
                id,type,name,status,description,duration,start,epic
                1,EPIC,"Epic",,"Epic description",,,
                2,SUBTASK,"Subtask",NEW,"Subtask description",30,2000-05-01T13:30,
                """);
        String expectedMessage = "number expected (" + file + ":3:67)";

        Exception exception = assertThrows(ManagerLoadException.class,
                () -> FileBackedTaskManager.loadFromFile(file, historyManager));
        assertEquals(expectedMessage, exception.getMessage(), WRONG_EXCEPTION_MESSAGE);
    }

    @Test
    public void shouldThrowWhenEpicIdOfSubtaskFromFileIsNotNumber() throws IOException {
        fillTestFileWithData("""
                id,type,name,status,description,duration,start,epic
                1,EPIC,"Epic",,"Epic description",,,
                2,SUBTASK,"Subtask",NEW,"Subtask description",30,2000-05-01T13:30,one
                """);
        String expectedMessage = "number expected (" + file + ":3:67)";

        Exception exception = assertThrows(ManagerLoadException.class,
                () -> FileBackedTaskManager.loadFromFile(file, historyManager));
        assertEquals(expectedMessage, exception.getMessage(), WRONG_EXCEPTION_MESSAGE);
    }

    @Test
    public void shouldThrowWhenSubtaskFromFileHasUnknownEpicId() throws IOException {
        fillTestFileWithData("""
                id,type,name,status,description,duration,start,epic
                1,EPIC,"Epic",,"Epic description",,,
                2,SUBTASK,"Subtask",NEW,"Subtask description",30,2000-05-01T13:30,3
                """);
        String expectedMessage = "no epic with id=3 (" + file + ":3:67)";

        Exception exception = assertThrows(ManagerLoadException.class,
                () -> FileBackedTaskManager.loadFromFile(file, historyManager));
        assertEquals(expectedMessage, exception.getMessage(), WRONG_EXCEPTION_MESSAGE);
    }

    @Test
    public void shouldThrowWhenSubtaskFromFileHasTaskIdAsEpicId() throws IOException {
        fillTestFileWithData("""
                id,type,name,status,description,duration,start,epic
                1,TASK,"Task",NEW,"Task description",90,2000-05-01T15:00,
                2,EPIC,"Epic",,"Epic description",,,
                3,SUBTASK,"Subtask",NEW,"Subtask description",30,2000-05-01T13:30,1
                """);
        String expectedMessage = "no epic with id=1 (" + file + ":4:67)";

        Exception exception = assertThrows(ManagerLoadException.class,
                () -> FileBackedTaskManager.loadFromFile(file, historyManager));
        assertEquals(expectedMessage, exception.getMessage(), WRONG_EXCEPTION_MESSAGE);
    }

    @Test
    public void shouldThrowWhenSubtaskFromFileHasSubtaskIdAsEpicId() throws IOException {
        fillTestFileWithData("""
                id,type,name,status,description,duration,start,epic
                1,EPIC,"Epic",,"Epic description",,,
                2,SUBTASK,"Subtask A",NEW,"Subtask A description",90,2000-05-01T15:00,1
                3,SUBTASK,"Subtask B",NEW,"Subtask B description",30,2000-05-01T13:30,2
                """);
        String expectedMessage = "no epic with id=2 (" + file + ":4:71)";

        Exception exception = assertThrows(ManagerLoadException.class,
                () -> FileBackedTaskManager.loadFromFile(file, historyManager));
        assertEquals(expectedMessage, exception.getMessage(), WRONG_EXCEPTION_MESSAGE);
    }

    @Test
    public void shouldThrowWhenTaskFromFileHasIdOfAnotherTask() throws IOException {
        fillTestFileWithData("""
                id,type,name,status,description,duration,start,epic
                1,TASK,"Task A",NEW,"Task A description",30,2000-05-01T13:30,
                1,TASK,"Task B",NEW,"Task B description",90,2000-05-01T15:00,
                """);
        String expectedMessage = "duplicate id=1 (" + file + ":3:1)";

        Exception exception = assertThrows(ManagerLoadException.class,
                () -> FileBackedTaskManager.loadFromFile(file, historyManager));
        assertEquals(expectedMessage, exception.getMessage(), WRONG_EXCEPTION_MESSAGE);
    }

    @Test
    public void shouldThrowWhenTaskFromFileHasIdOfEpic() throws IOException {
        fillTestFileWithData("""
                id,type,name,status,description,duration,start,epic
                1,EPIC,"Epic",,"Epic description",,,
                1,TASK,"Task",NEW,"Task description",30,2000-05-01T13:30,
                """);
        String expectedMessage = "duplicate id=1 (" + file + ":3:1)";

        Exception exception = assertThrows(ManagerLoadException.class,
                () -> FileBackedTaskManager.loadFromFile(file, historyManager));
        assertEquals(expectedMessage, exception.getMessage(), WRONG_EXCEPTION_MESSAGE);
    }

    @Test
    public void shouldThrowWhenTaskFromFileHasIdOfSubtask() throws IOException {
        fillTestFileWithData("""
                id,type,name,status,description,duration,start,epic
                1,EPIC,"Epic",,"Epic description",,,
                2,SUBTASK,"Subtask",NEW,"Subtask description",90,2000-05-01T15:00,1
                2,TASK,"Task",NEW,"Task description",30,2000-05-01T13:30,
                """);
        String expectedMessage = "duplicate id=2 (" + file + ":4:1)";

        Exception exception = assertThrows(ManagerLoadException.class,
                () -> FileBackedTaskManager.loadFromFile(file, historyManager));
        assertEquals(expectedMessage, exception.getMessage(), WRONG_EXCEPTION_MESSAGE);
    }

    @Test
    public void shouldThrowWhenEpicFromFileHasIdOfTask() throws IOException {
        fillTestFileWithData("""
                id,type,name,status,description,duration,start,epic
                1,TASK,"Task",NEW,"Task description",30,2000-05-01T13:30,
                1,EPIC,"Epic",,"Epic description",,,
                """);
        String expectedMessage = "duplicate id=1 (" + file + ":3:1)";

        Exception exception = assertThrows(ManagerLoadException.class,
                () -> FileBackedTaskManager.loadFromFile(file, historyManager));
        assertEquals(expectedMessage, exception.getMessage(), WRONG_EXCEPTION_MESSAGE);
    }

    @Test
    public void shouldThrowWhenEpicFromFileHasIdOfAnotherEpic() throws IOException {
        fillTestFileWithData("""
                id,type,name,status,description,duration,start,epic
                1,EPIC,"Epic A",,"Epic A description",,,
                1,EPIC,"Epic B",,"Epic B description",,,
                """);
        String expectedMessage = "duplicate id=1 (" + file + ":3:1)";

        Exception exception = assertThrows(ManagerLoadException.class,
                () -> FileBackedTaskManager.loadFromFile(file, historyManager));
        assertEquals(expectedMessage, exception.getMessage(), WRONG_EXCEPTION_MESSAGE);
    }

    @Test
    public void shouldThrowWhenEpicFromFileHasIdOfSubtask() throws IOException {
        fillTestFileWithData("""
                id,type,name,status,description,duration,start,epic
                1,EPIC,"Epic A",,"Epic A description",,,
                2,SUBTASK,"Subtask",NEW,"Subtask description",30,2000-05-01T13:30,1
                2,EPIC,"Epic B",,"Epic B description",,,
                """);
        String expectedMessage = "duplicate id=2 (" + file + ":4:1)";

        Exception exception = assertThrows(ManagerLoadException.class,
                () -> FileBackedTaskManager.loadFromFile(file, historyManager));
        assertEquals(expectedMessage, exception.getMessage(), WRONG_EXCEPTION_MESSAGE);
    }

    @Test
    public void shouldThrowWhenSubtaskFromFileHasIdOfTask() throws IOException {
        fillTestFileWithData("""
                id,type,name,status,description,duration,start,epic
                1,TASK,"Task",NEW,"Task description",90,2000-05-01T15:00,
                2,EPIC,"Epic",,"Subtask description",,,
                1,SUBTASK,"Subtask",NEW,"Subtask description",30,2000-05-01T13:30,2
                """);
        String expectedMessage = "duplicate id=1 (" + file + ":4:1)";

        Exception exception = assertThrows(ManagerLoadException.class,
                () -> FileBackedTaskManager.loadFromFile(file, historyManager));
        assertEquals(expectedMessage, exception.getMessage(), WRONG_EXCEPTION_MESSAGE);
    }

    @Test
    public void shouldThrowWhenSubtaskFromFileHasIdOfEpic() throws IOException {
        fillTestFileWithData("""
                id,type,name,status,description,duration,start,epic
                1,EPIC,"Epic A",,"Epic A description",,,
                2,EPIC,"Epic B",,"Epic B description",,,
                1,SUBTASK,"Subtask",NEW,"Subtask description",30,2000-05-01T13:30,2
                """);
        String expectedMessage = "duplicate id=1 (" + file + ":4:1)";

        Exception exception = assertThrows(ManagerLoadException.class,
                () -> FileBackedTaskManager.loadFromFile(file, historyManager));
        assertEquals(expectedMessage, exception.getMessage(), WRONG_EXCEPTION_MESSAGE);
    }

    @Test
    public void shouldThrowWhenSubtaskFromFileHasIdOfAnotherSubtask() throws IOException {
        fillTestFileWithData("""
                id,type,name,status,description,duration,start,epic
                1,EPIC,"Epic",,"Epic description",,,
                2,SUBTASK,"Subtask A",NEW,"Subtask A description",90,2000-05-01T15:00,1
                2,SUBTASK,"Subtask B",NEW,"Subtask B description",30,2000-05-01T13:30,1
                """);
        String expectedMessage = "duplicate id=2 (" + file + ":4:1)";

        Exception exception = assertThrows(ManagerLoadException.class,
                () -> FileBackedTaskManager.loadFromFile(file, historyManager));
        assertEquals(expectedMessage, exception.getMessage(), WRONG_EXCEPTION_MESSAGE);
    }

    private void fillTestFileWithData(String data) throws IOException {
        Files.writeString(file.toPath(), data, StandardCharsets.UTF_8);
    }
}
