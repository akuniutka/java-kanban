package io.github.akuniutka.kanban.service;

import io.github.akuniutka.kanban.exception.ManagerLoadException;
import io.github.akuniutka.kanban.exception.ManagerSaveException;
import io.github.akuniutka.kanban.model.Epic;
import io.github.akuniutka.kanban.model.Subtask;
import io.github.akuniutka.kanban.model.Task;
import io.github.akuniutka.kanban.model.TaskStatus;
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
    private static final String FILE_HEADER = "id,type,name,status,description,epic";
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
        testTask = createTestTask(TEST_TITLE, TEST_DESCRIPTION, TEST_STATUS);
        modifiedTestTask = createTestTask(MODIFIED_TEST_TITLE, MODIFIED_TEST_DESCRIPTION, MODIFIED_TEST_STATUS);
        emptyEpic = createTestEpic();
        testEpic = createTestEpic(TEST_TITLE, TEST_DESCRIPTION);
        modifiedTestEpic = createTestEpic(MODIFIED_TEST_TITLE, MODIFIED_TEST_DESCRIPTION);
        emptySubtask = createTestSubtask();
        testSubtask = createTestSubtask(TEST_TITLE, TEST_DESCRIPTION, TEST_STATUS);
        modifiedTestSubtask = createTestSubtask(MODIFIED_TEST_TITLE, MODIFIED_TEST_DESCRIPTION, MODIFIED_TEST_STATUS);
    }

    @Test
    public void shouldCreateFileBackedTaskManagerOfInterfaceType() {
        assertNotNull(manager, "task manager was not created");
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
                lastUsedId=%%d
                %s
                %%d,TASK,"%s",%s,"%s",
                """.formatted(FILE_HEADER, testTask.getTitle(), testTask.getStatus(), testTask.getDescription());

        long id = manager.addTask(testTask);

        expectedString = expectedString.formatted(id, id);
        String actualString = Files.readString(file.toPath());
        assertEquals(expectedString, actualString, WRONG_FILE_FORMAT);
    }

    @Test
    public void shouldSaveWhenTaskUpdated() throws IOException {
        long id = manager.addTask(testTask);
        String expectedString = """
                lastUsedId=%d
                %s
                %d,TASK,"%s",%s,"%s",
                """.formatted(id, FILE_HEADER, id, modifiedTestTask.getTitle(), modifiedTestTask.getStatus(),
                modifiedTestTask.getDescription());
        modifiedTestTask.setId(id);

        manager.updateTask(modifiedTestTask);

        String actualString = Files.readString(file.toPath());
        assertEquals(expectedString, actualString, WRONG_FILE_FORMAT);
    }

    @Test
    public void shouldCorrectlySaveTaskWithNullFields() throws IOException {
        String expectedString = """
                lastUsedId=%%d
                %s
                %%d,TASK,null,null,null,
                """.formatted(FILE_HEADER);

        long id = manager.addTask(emptyTask);

        expectedString = expectedString.formatted(id, id);
        String actualString = Files.readString(file.toPath());
        assertEquals(expectedString, actualString, WRONG_FILE_FORMAT);
    }

    @Test
    public void shouldSaveWhenTaskRemoved() throws IOException {
        long id = manager.addTask(testTask);
        String expectedString = """
                lastUsedId=%d
                %s
                """.formatted(id, FILE_HEADER);

        manager.removeTask(id);

        String actualString = Files.readString(file.toPath());
        assertEquals(expectedString, actualString, WRONG_FILE_FORMAT);
    }

    @Test
    public void shouldSaveWhenNewEpicAdded() throws IOException {
        String expectedString = """
                lastUsedId=%%d
                %s
                %%d,EPIC,"%s",,"%s",
                """.formatted(FILE_HEADER, testEpic.getTitle(), testEpic.getDescription());

        long id = manager.addEpic(testEpic);

        expectedString = expectedString.formatted(id, id);
        String actualString = Files.readString(file.toPath());
        assertEquals(expectedString, actualString, WRONG_FILE_FORMAT);
    }

    @Test
    public void shouldSaveWhenEpicUpdated() throws IOException {
        long id = manager.addEpic(testEpic);
        String expectedString = """
                lastUsedId=%d
                %s
                %d,EPIC,"%s",,"%s",
                """.formatted(id, FILE_HEADER, id, modifiedTestEpic.getTitle(), modifiedTestEpic.getDescription());
        modifiedTestEpic.setId(id);

        manager.updateEpic(modifiedTestEpic);

        String actualString = Files.readString(file.toPath());
        assertEquals(expectedString, actualString, WRONG_FILE_FORMAT);
    }

    @Test
    public void shouldCorrectlySaveEpicWithNullFields() throws IOException {
        String expectedString = """
                lastUsedId=%%d
                %s
                %%d,EPIC,null,,null,
                """.formatted(FILE_HEADER);

        long id = manager.addEpic(emptyEpic);

        expectedString = expectedString.formatted(id, id);
        String actualString = Files.readString(file.toPath());
        assertEquals(expectedString, actualString, WRONG_FILE_FORMAT);
    }

    @Test
    public void shouldSaveWhenEpicRemoved() throws IOException {
        long id = manager.addEpic(testEpic);
        String expectedString = """
                lastUsedId=%d
                %s
                """.formatted(id, FILE_HEADER);

        manager.removeEpic(id);

        String actualString = Files.readString(file.toPath());
        assertEquals(expectedString, actualString, WRONG_FILE_FORMAT);
    }

    @Test
    public void shouldSaveWhenNewSubtaskAdded() throws IOException {
        String expectedString = """
                lastUsedId=%%d
                %s
                %%d,EPIC,"%s",,"%s",
                %%d,SUBTASK,"%s",%s,"%s",%%d
                """.formatted(FILE_HEADER, testEpic.getTitle(), testEpic.getDescription(), testSubtask.getTitle(),
                testSubtask.getStatus(), testSubtask.getDescription());
        long epicId = manager.addEpic(testEpic);
        testSubtask.setEpicId(epicId);

        long subtaskId = manager.addSubtask(testSubtask);

        expectedString = expectedString.formatted(subtaskId, epicId, subtaskId, epicId);
        String actualString = Files.readString(file.toPath());
        assertEquals(expectedString, actualString, WRONG_FILE_FORMAT);
    }

    @Test
    public void shouldSaveWhenSubtaskUpdated() throws IOException {
        String expectedString = """
                lastUsedId=%%d
                %s
                %%d,EPIC,"%s",,"%s",
                %%d,SUBTASK,"%s",%s,"%s",%%d
                """.formatted(FILE_HEADER, testEpic.getTitle(), testEpic.getDescription(),
                modifiedTestSubtask.getTitle(), modifiedTestSubtask.getStatus(), modifiedTestSubtask.getDescription());
        long epicId = manager.addEpic(testEpic);
        testSubtask.setEpicId(epicId);
        long subtaskId = manager.addSubtask(testSubtask);
        modifiedTestSubtask.setEpicId(epicId);
        modifiedTestSubtask.setId(subtaskId);

        manager.updateSubtask(modifiedTestSubtask);

        expectedString = expectedString.formatted(subtaskId, epicId, subtaskId, epicId);
        String actualString = Files.readString(file.toPath());
        assertEquals(expectedString, actualString, WRONG_FILE_FORMAT);
    }

    @Test
    public void shouldCorrectlySaveSubtaskWithNullFields() throws IOException {
        String expectedString = """
                lastUsedId=%%d
                %s
                %%d,EPIC,"%s",,"%s",
                %%d,SUBTASK,null,null,null,%%d
                """.formatted(FILE_HEADER, testEpic.getTitle(), testEpic.getDescription());
        long epicId = manager.addEpic(testEpic);
        emptySubtask.setEpicId(epicId);

        long subtaskId = manager.addSubtask(emptySubtask);

        expectedString = expectedString.formatted(subtaskId, epicId, subtaskId, epicId);
        String actualString = Files.readString(file.toPath());
        assertEquals(expectedString, actualString, WRONG_FILE_FORMAT);
    }

    @Test
    public void shouldSaveWhenSubtaskRemoved() throws IOException {
        String expectedString = """
                lastUsedId=%%d
                %s
                %%d,EPIC,"%s",,"%s",
                """.formatted(FILE_HEADER, testEpic.getTitle(), testEpic.getDescription());
        long epicId = manager.addEpic(testEpic);
        testSubtask.setEpicId(epicId);
        long subtaskId = manager.addSubtask(testSubtask);

        manager.removeSubtask(subtaskId);

        expectedString = expectedString.formatted(subtaskId, epicId);
        String actualString = Files.readString(file.toPath());
        assertEquals(expectedString, actualString, WRONG_FILE_FORMAT);
    }

    @Test
    public void shouldSaveWhenAllTasksRemoved() throws IOException {
        manager.addTask(testTask);
        long id = manager.addTask(modifiedTestTask);
        String expectedString = """
                lastUsedId=%d
                %s
                """.formatted(id, FILE_HEADER);

        manager.removeTasks();

        String actualString = Files.readString(file.toPath());
        assertEquals(expectedString, actualString, WRONG_FILE_FORMAT);
    }

    @Test
    public void shouldSaveWhenAllEpicsRemoved() throws IOException {
        manager.addEpic(testEpic);
        long id = manager.addEpic(modifiedTestEpic);
        String expectedString = """
                lastUsedId=%d
                %s
                """.formatted(id, FILE_HEADER);

        manager.removeEpics();

        String actualString = Files.readString(file.toPath());
        assertEquals(expectedString, actualString, WRONG_FILE_FORMAT);
    }

    @Test
    public void shouldSaveWhenAllSubtasksRemoved() throws IOException {
        String expectedString = """
                lastUsedId=%%d
                %s
                %%d,EPIC,"%s",,"%s",
                """.formatted(FILE_HEADER, testEpic.getTitle(), testEpic.getDescription());
        long epicId = manager.addEpic(testEpic);
        testSubtask.setEpicId(epicId);
        manager.addSubtask(testSubtask);
        modifiedTestSubtask.setEpicId(epicId);
        long subtaskId = manager.addSubtask(modifiedTestSubtask);

        manager.removeSubtasks();

        expectedString = expectedString.formatted(subtaskId, epicId);
        String actualString = Files.readString(file.toPath());
        assertEquals(expectedString, actualString, WRONG_FILE_FORMAT);
    }

    @Test
    public void shouldCorrectlySaveLastUsedId() throws IOException {
        long id = manager.addTask(testTask);
        String expectedString = """
                lastUsedId=%d
                %s
                """.formatted(id, FILE_HEADER);

        manager.removeTask(id);

        String actualString = Files.readString(file.toPath());
        assertEquals(expectedString, actualString, WRONG_FILE_FORMAT);
    }

    @Test
    public void shouldLoadTaskFromFile() throws IOException {
        file = makeTestFileFromString("""
                lastUsedId=1
                id,type,name,status,description,epic
                1,TASK,"Title",IN_PROGRESS,"Description",
                """);

        manager = FileBackedTaskManager.loadFromFile(file, historyManager);

        List<Task> tasks = manager.getTasks();
        assertEquals(1, tasks.size(), "list should contain exactly 1 element");
        assertEquals(Task.class, tasks.getFirst().getClass(), "element in list should be of Task class");
        Task task = tasks.getFirst();
        assertEquals(TEST_TASK_ID, task.getId(), "task id loaded incorrectly");
        assertEquals(TEST_TITLE, task.getTitle(), "task title loaded incorrectly");
        assertEquals(TEST_DESCRIPTION, task.getDescription(), "task description loaded incorrectly");
        assertEquals(TEST_STATUS, task.getStatus(), "task status loaded incorrectly");
    }

    @Test
    public void shouldLoadTaskWithNullFieldsFromFile() throws IOException {
        file = makeTestFileFromString("""
                lastUsedId=1
                id,type,name,status,description,epic
                1,TASK,null,null,null,
                """);

        manager = FileBackedTaskManager.loadFromFile(file, historyManager);

        List<Task> tasks = manager.getTasks();
        assertEquals(1, tasks.size(), "list should contain exactly 1 element");
        assertEquals(Task.class, tasks.getFirst().getClass(), "element in list should be of Task class");
        Task task = tasks.getFirst();
        assertEquals(TEST_TASK_ID, task.getId(), "task id loaded incorrectly");
        assertNull(task.getTitle(), "task title loaded incorrectly");
        assertNull(task.getDescription(), "task description loaded incorrectly");
        assertNull(task.getStatus(), "task status loaded incorrectly");
    }

    @Test
    public void shouldLoadEpicFromFile() throws IOException {
        file = makeTestFileFromString("""
                lastUsedId=2
                id,type,name,status,description,epic
                2,EPIC,"Title",,"Description",
                """);

        manager = FileBackedTaskManager.loadFromFile(file, historyManager);

        List<Epic> epics = manager.getEpics();
        assertEquals(1, epics.size(), "list should contain exactly 1 element");
        assertEquals(Epic.class, epics.getFirst().getClass(), "element in list should be of Epic class");
        Epic epic = epics.getFirst();
        assertEquals(TEST_EPIC_ID, epic.getId(), "epic id loaded incorrectly");
        assertEquals(TEST_TITLE, epic.getTitle(), "epic title loaded incorrectly");
        assertEquals(TEST_DESCRIPTION, epic.getDescription(), "epic description loaded incorrectly");
        assertTrue(epic.getSubtaskIds().isEmpty(), "list of epic subtasks should be empty");
    }

    @Test
    public void shouldLoadEpicWithNullFieldsFromFile() throws IOException {
        file = makeTestFileFromString("""
                lastUsedId=2
                id,type,name,status,description,epic
                2,EPIC,null,,null,
                """);

        manager = FileBackedTaskManager.loadFromFile(file, historyManager);

        List<Epic> epics = manager.getEpics();
        assertEquals(1, epics.size(), "list should contain exactly 1 element");
        assertEquals(Epic.class, epics.getFirst().getClass(), "element in list should be of Epic class");
        Epic epic = epics.getFirst();
        assertEquals(TEST_EPIC_ID, epic.getId(), "epic id loaded incorrectly");
        assertNull(epic.getTitle(), "epic title loaded incorrectly");
        assertNull(epic.getDescription(), "epic description loaded incorrectly");
        assertTrue(epic.getSubtaskIds().isEmpty(), "list of epic subtasks should be empty");
    }

    @Test
    public void shouldLoadSubtaskFromFile() throws IOException {
        file = makeTestFileFromString("""
                lastUsedId=3
                id,type,name,status,description,epic
                2,EPIC,"Title",,"Description",
                3,SUBTASK,"Title",IN_PROGRESS,"Description",2
                """);

        manager = FileBackedTaskManager.loadFromFile(file, historyManager);

        List<Subtask> subtasks = manager.getSubtasks();
        assertEquals(1, subtasks.size(), "list should contain exactly 1 element");
        assertEquals(Subtask.class, subtasks.getFirst().getClass(), "element in list should be of Subtask class");
        Subtask subtask = subtasks.getFirst();
        assertEquals(TEST_SUBTASK_ID, subtask.getId(), "subtask id loaded incorrectly");
        assertEquals(TEST_EPIC_ID, subtask.getEpicId(), "epic id of subtask loaded incorrectly");
        assertEquals(TEST_TITLE, subtask.getTitle(), "subtask title loaded incorrectly");
        assertEquals(TEST_DESCRIPTION, subtask.getDescription(), "subtask description loaded incorrectly");
        assertEquals(TEST_STATUS, subtask.getStatus(), "subtask status loaded incorrectly");
        Epic epic = manager.getEpics().getFirst();
        assertEquals(1, epic.getSubtaskIds().size(), "list of epic subtasks should contain exactly 1 element");
        assertEquals(TEST_SUBTASK_ID, epic.getSubtaskIds().getFirst(), "subtask id loaded incorrectly");
    }

    @Test
    public void shouldLoadSubtaskWithNullFieldsFromFile() throws IOException {
        file = makeTestFileFromString("""
                lastUsedId=3
                id,type,name,status,description,epic
                2,EPIC,"Title",,"Description",
                3,SUBTASK,null,null,null,2
                """);

        manager = FileBackedTaskManager.loadFromFile(file, historyManager);

        List<Subtask> subtasks = manager.getSubtasks();
        assertEquals(1, subtasks.size(), "list should contain exactly 1 element");
        assertEquals(Subtask.class, subtasks.getFirst().getClass(), "element in list should be of Subtask class");
        Subtask subtask = subtasks.getFirst();
        assertEquals(TEST_SUBTASK_ID, subtask.getId(), "subtask id loaded incorrectly");
        assertEquals(TEST_EPIC_ID, subtask.getEpicId(), "epic id of subtask loaded incorrectly");
        assertNull(subtask.getTitle(), "subtask title loaded incorrectly");
        assertNull(subtask.getDescription(), "subtask description loaded incorrectly");
        assertNull(subtask.getStatus(), "subtask status loaded incorrectly");
        Epic epic = manager.getEpics().getFirst();
        assertEquals(1, epic.getSubtaskIds().size(), "list of epic subtasks should contain exactly 1 element");
        assertEquals(TEST_SUBTASK_ID, epic.getSubtaskIds().getFirst(), "subtask id loaded incorrectly");
    }

    @Test
    public void shouldRecalculateEpicStatusOnLoad() throws IOException {
        file = makeTestFileFromString("""
                lastUsedId=10
                id,type,name,status,description,epic
                1,EPIC,"Epic 1",,"Epic with no subtasks",
                2,EPIC,"Epic 2",,"Epic with all subtasks in status NEW",
                3,EPIC,"Epic 3",,"Epic with all subtasks in status DONE",
                4,EPIC,"Epic 4",,"Epic with subtasks in different statuses",
                5,SUBTASK,"Subtask 1",NEW,"Subtask 1 description",2
                6,SUBTASK,"Subtask 2",NEW,"Subtask 2 description",2
                7,SUBTASK,"Subtask 3",DONE,"Subtask 3 description",3
                8,SUBTASK,"Subtask 4",DONE,"Subtask 4 description",3
                9,SUBTASK,"Subtask 5",NEW,"Subtask 5 description",4
                10,SUBTASK,"Subtask 6",DONE,"Subtask 6 description",4
                """);

        manager = FileBackedTaskManager.loadFromFile(file, historyManager);

        List<Epic> epics = manager.getEpics();
        assertEquals(4, epics.size(), "list should contain exactly 4 elements");
        assertEquals(TaskStatus.NEW, epics.getFirst().getStatus(), "epic should have status NEW");
        assertEquals(TaskStatus.NEW, epics.get(1).getStatus(), "epic should have status NEW");
        assertEquals(TaskStatus.DONE, epics.get(2).getStatus(), "epic should have status DONE");
        assertEquals(TaskStatus.IN_PROGRESS, epics.getLast().getStatus(), "epic should have status IN_PROGRESS");
    }

    @Test
    public void shouldLoadLastUsedIdFromFile() throws IOException {
        file = makeTestFileFromString("""
                lastUsedId=1000
                id,type,name,status,description,epic
                """);

        manager = FileBackedTaskManager.loadFromFile(file, historyManager);
        long id = manager.addTask(testTask);

        assertEquals(1001, id, "last used id loaded incorrectly");
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
        String expectedMessage = "\"lastUsedId\" expected (" + file + ":1:1)";

        ManagerLoadException exception = assertThrows(ManagerLoadException.class,
                () -> FileBackedTaskManager.loadFromFile(file, historyManager));
        assertEquals(expectedMessage, exception.getMessage(), WRONG_EXCEPTION_MESSAGE);
    }

    @Test
    public void shouldThrowWhenFileLoadedDoesNotStartWithLastUsedId() throws IOException {
        file = makeTestFileFromString("""
                randomText=1000
                id,type,name,status,description,epic
                """);
        String expectedMessage = "\"lastUsedId\" expected (" + file + ":1:1)";

        Exception exception = assertThrows(ManagerLoadException.class,
                () -> FileBackedTaskManager.loadFromFile(file, historyManager));
        assertEquals(expectedMessage, exception.getMessage(), WRONG_EXCEPTION_MESSAGE);
    }

    @Test
    public void shouldThrowWhenLastUserIdInFileNotFollowedByEquals() throws IOException {
        file = makeTestFileFromString("""
                lastUsedId:1000
                id,type,name,status,description,epic
                """);
        String expectedMessage = "\"=\" expected (" + file + ":1:11)";

        Exception exception = assertThrows(ManagerLoadException.class,
                () -> FileBackedTaskManager.loadFromFile(file, historyManager));
        assertEquals(expectedMessage, exception.getMessage(), WRONG_EXCEPTION_MESSAGE);
    }

    @Test
    public void shouldThrowWhenLastUserIdInFileIsNotNumber() throws IOException {
        file = makeTestFileFromString("""
                lastUsedId=thousand
                id,type,name,status,description,epic
                """);
        String expectedMessage = "number expected (" + file + ":1:12)";

        Exception exception = assertThrows(ManagerLoadException.class,
                () -> FileBackedTaskManager.loadFromFile(file, historyManager));
        assertEquals(expectedMessage, exception.getMessage(), WRONG_EXCEPTION_MESSAGE);
    }

    @Test
    public void shouldThrowWhenFileContainsNoHeader() throws IOException {
        file = makeTestFileFromString("""
                lastUsedId=1000
                """);
        String expectedMessage = "\"id\" expected (" + file + ":2:1)";

        Exception exception = assertThrows(ManagerLoadException.class,
                () -> FileBackedTaskManager.loadFromFile(file, historyManager));
        assertEquals(expectedMessage, exception.getMessage(), WRONG_EXCEPTION_MESSAGE);
    }

    @Test
    public void shouldThrowWhenCannotFindIdInFileHeader() throws IOException {
        file = makeTestFileFromString("""
                lastUsedId=1000
                text,type,name,status,description,epic
                """);
        String expectedMessage = "\"id\" expected (" + file + ":2:1)";

        Exception exception = assertThrows(ManagerLoadException.class,
                () -> FileBackedTaskManager.loadFromFile(file, historyManager));
        assertEquals(expectedMessage, exception.getMessage(), WRONG_EXCEPTION_MESSAGE);
    }

    @Test
    public void shouldThrowWhenCannotFindFirstCommaInFileHeader() throws IOException {
        file = makeTestFileFromString("""
                lastUsedId=1000
                id:type:name:status:description:epic
                """);
        String expectedMessage = "comma expected (" + file + ":2:3)";

        Exception exception = assertThrows(ManagerLoadException.class,
                () -> FileBackedTaskManager.loadFromFile(file, historyManager));
        assertEquals(expectedMessage, exception.getMessage(), WRONG_EXCEPTION_MESSAGE);
    }

    @Test
    public void shouldThrowWhenCannotFindTypeInFileHeader() throws IOException {
        file = makeTestFileFromString("""
                lastUsedId=1000
                id,text,name,status,description,epic
                """);
        String expectedMessage = "\"type\" expected (" + file + ":2:4)";

        Exception exception = assertThrows(ManagerLoadException.class,
                () -> FileBackedTaskManager.loadFromFile(file, historyManager));
        assertEquals(expectedMessage, exception.getMessage(), WRONG_EXCEPTION_MESSAGE);
    }

    @Test
    public void shouldThrowWhenCannotFindSecondCommaInFileHeader() throws IOException {
        file = makeTestFileFromString("""
                lastUsedId=1000
                id,type:name,status,description,epic
                """);
        String expectedMessage = "comma expected (" + file + ":2:8)";

        Exception exception = assertThrows(ManagerLoadException.class,
                () -> FileBackedTaskManager.loadFromFile(file, historyManager));
        assertEquals(expectedMessage, exception.getMessage(), WRONG_EXCEPTION_MESSAGE);
    }

    @Test
    public void shouldThrowWhenCannotFindNameInFileHeader() throws IOException {
        file = makeTestFileFromString("""
                lastUsedId=1000
                id,type,text,status,description,epic
                """);
        String expectedMessage = "\"name\" expected (" + file + ":2:9)";

        Exception exception = assertThrows(ManagerLoadException.class,
                () -> FileBackedTaskManager.loadFromFile(file, historyManager));
        assertEquals(expectedMessage, exception.getMessage(), WRONG_EXCEPTION_MESSAGE);
    }

    @Test
    public void shouldThrowWhenCannotFindThirdCommaInFileHeader() throws IOException {
        file = makeTestFileFromString("""
                lastUsedId=1000
                id,type,name:status,description,epic
                """);
        String expectedMessage = "comma expected (" + file + ":2:13)";

        Exception exception = assertThrows(ManagerLoadException.class,
                () -> FileBackedTaskManager.loadFromFile(file, historyManager));
        assertEquals(expectedMessage, exception.getMessage(), WRONG_EXCEPTION_MESSAGE);
    }

    @Test
    public void shouldThrowWhenCannotFindStatusInFileHeader() throws IOException {
        file = makeTestFileFromString("""
                lastUsedId=1000
                id,type,name,text,description,epic
                """);
        String expectedMessage = "\"status\" expected (" + file + ":2:14)";

        Exception exception = assertThrows(ManagerLoadException.class,
                () -> FileBackedTaskManager.loadFromFile(file, historyManager));
        assertEquals(expectedMessage, exception.getMessage(), WRONG_EXCEPTION_MESSAGE);
    }

    @Test
    public void shouldThrowWhenCannotFindFourthCommaInFileHeader() throws IOException {
        file = makeTestFileFromString("""
                lastUsedId=1000
                id,type,name,status:description,epic
                """);
        String expectedMessage = "comma expected (" + file + ":2:20)";

        Exception exception = assertThrows(ManagerLoadException.class,
                () -> FileBackedTaskManager.loadFromFile(file, historyManager));
        assertEquals(expectedMessage, exception.getMessage(), WRONG_EXCEPTION_MESSAGE);
    }

    @Test
    public void shouldThrowWhenCannotFindDescriptionInFileHeader() throws IOException {
        file = makeTestFileFromString("""
                lastUsedId=1000
                id,type,name,status,text,epic
                """);
        String expectedMessage = "\"description\" expected (" + file + ":2:21)";

        Exception exception = assertThrows(ManagerLoadException.class,
                () -> FileBackedTaskManager.loadFromFile(file, historyManager));
        assertEquals(expectedMessage, exception.getMessage(), WRONG_EXCEPTION_MESSAGE);
    }

    @Test
    public void shouldThrowWhenCannotFindFifthCommaInFileHeader() throws IOException {
        file = makeTestFileFromString("""
                lastUsedId=1000
                id,type,name,status,description:epic
                """);
        String expectedMessage = "comma expected (" + file + ":2:32)";

        Exception exception = assertThrows(ManagerLoadException.class,
                () -> FileBackedTaskManager.loadFromFile(file, historyManager));
        assertEquals(expectedMessage, exception.getMessage(), WRONG_EXCEPTION_MESSAGE);
    }

    @Test
    public void shouldThrowWhenCannotFindEpicInFileHeader() throws IOException {
        file = makeTestFileFromString("""
                lastUsedId=1000
                id,type,name,status,description,text
                """);
        String expectedMessage = "\"epic\" expected (" + file + ":2:33)";

        Exception exception = assertThrows(ManagerLoadException.class,
                () -> FileBackedTaskManager.loadFromFile(file, historyManager));
        assertEquals(expectedMessage, exception.getMessage(), WRONG_EXCEPTION_MESSAGE);
    }

    @Test
    public void shouldThrowWhenFileHeaderHasExcessiveCharacters() throws IOException {
        file = makeTestFileFromString("""
                lastUsedId=1000
                id,type,name,status,description,epic,
                """);
        String expectedMessage = "unexpected data (" + file + ":2:37)";

        Exception exception = assertThrows(ManagerLoadException.class,
                () -> FileBackedTaskManager.loadFromFile(file, historyManager));
        assertEquals(expectedMessage, exception.getMessage(), WRONG_EXCEPTION_MESSAGE);
    }

    @Test
    public void shouldThrowWhenTaskFromFileHasNotEnoughFields() throws IOException {
        file = makeTestFileFromString("""
                lastUsedId=1000
                id,type,name,status,description,epic
                1,TASK,"Title",IN_PROGRESS,"Description"
                """);
        String expectedMessage = "unexpected end of line (" + file + ":3:41)";

        Exception exception = assertThrows(ManagerLoadException.class,
                () -> FileBackedTaskManager.loadFromFile(file, historyManager));
        assertEquals(expectedMessage, exception.getMessage(), WRONG_EXCEPTION_MESSAGE);
    }

    @Test
    public void shouldThrowWhenEpicFromFileHasNotEnoughFields() throws IOException {
        file = makeTestFileFromString("""
                lastUsedId=1000
                id,type,name,status,description,epic
                1,EPIC,"Title",,"Description"
                """);
        String expectedMessage = "unexpected end of line (" + file + ":3:30)";

        Exception exception = assertThrows(ManagerLoadException.class,
                () -> FileBackedTaskManager.loadFromFile(file, historyManager));
        assertEquals(expectedMessage, exception.getMessage(), WRONG_EXCEPTION_MESSAGE);
    }

    @Test
    public void shouldThrowWhenSubtaskFromFileHasNotEnoughFields() throws IOException {
        file = makeTestFileFromString("""
                lastUsedId=1000
                id,type,name,status,description,epic
                1,EPIC,"Epic",,"Epic description",
                2,SUBTASK,"Subtask",NEW,"Subtask description"
                """);
        String expectedMessage = "unexpected end of line (" + file + ":4:46)";

        Exception exception = assertThrows(ManagerLoadException.class,
                () -> FileBackedTaskManager.loadFromFile(file, historyManager));
        assertEquals(expectedMessage, exception.getMessage(), WRONG_EXCEPTION_MESSAGE);
    }

    @Test
    public void shouldThrowWhenTaskFromFileHasExcessiveFields() throws IOException {
        file = makeTestFileFromString("""
                lastUsedId=1000
                id,type,name,status,description,epic
                1,TASK,"Task",IN_PROGRESS,"Task description",,
                """);
        String expectedMessage = "unexpected data (" + file + ":3:46)";

        Exception exception = assertThrows(ManagerLoadException.class,
                () -> FileBackedTaskManager.loadFromFile(file, historyManager));
        assertEquals(expectedMessage, exception.getMessage(), WRONG_EXCEPTION_MESSAGE);
    }

    @Test
    public void shouldThrowWhenEpicFromFileHasExcessiveFields() throws IOException {
        file = makeTestFileFromString("""
                lastUsedId=1000
                id,type,name,status,description,epic
                1,EPIC,"Epic",,"Epic description",,
                """);
        String expectedMessage = "unexpected data (" + file + ":3:35)";

        Exception exception = assertThrows(ManagerLoadException.class,
                () -> FileBackedTaskManager.loadFromFile(file, historyManager));
        assertEquals(expectedMessage, exception.getMessage(), WRONG_EXCEPTION_MESSAGE);
    }

    @Test
    public void shouldThrowWhenSubtaskFromFileHasExcessiveFields() throws IOException {
        file = makeTestFileFromString("""
                lastUsedId=1000
                id,type,name,status,description,epic
                1,EPIC,"Epic",,"Epic description",
                2,SUBTASK,"Subtask",NEW,"Subtask description",1,
                """);
        String expectedMessage = "unexpected data (" + file + ":4:48)";

        Exception exception = assertThrows(ManagerLoadException.class,
                () -> FileBackedTaskManager.loadFromFile(file, historyManager));
        assertEquals(expectedMessage, exception.getMessage(), WRONG_EXCEPTION_MESSAGE);
    }

    @Test
    public void shouldThrowWhenTaskIdFromFileNotNumber() throws IOException {
        file = makeTestFileFromString("""
                lastUsedId=1000
                id,type,name,status,description,epic
                one,TASK,"Title",IN_PROGRESS,"Description",
                """);
        String expectedMessage = "number expected (" + file + ":3:1)";

        Exception exception = assertThrows(ManagerLoadException.class,
                () -> FileBackedTaskManager.loadFromFile(file, historyManager));
        assertEquals(expectedMessage, exception.getMessage(), WRONG_EXCEPTION_MESSAGE);
    }

    @Test
    public void shouldThrowWhenTaskFromFileHasUnknownType() throws IOException {
        file = makeTestFileFromString("""
                lastUsedId=1000
                id,type,name,status,description,epic
                1,text,"Title",IN_PROGRESS,"Description",
                """);
        String expectedMessage = "unknown task type (" + file + ":3:3)";

        Exception exception = assertThrows(ManagerLoadException.class,
                () -> FileBackedTaskManager.loadFromFile(file, historyManager));
        assertEquals(expectedMessage, exception.getMessage(), WRONG_EXCEPTION_MESSAGE);
    }

    @Test
    public void shouldThrowWhenTaskTitleFromFileDoesNotStartWithQuote() throws IOException {
        file = makeTestFileFromString("""
                lastUsedId=1000
                id,type,name,status,description,epic
                1,TASK,Title",IN_PROGRESS,"Description",
                """);
        String expectedMessage = "unexpected double quote (" + file + ":3:13)";

        Exception exception = assertThrows(ManagerLoadException.class,
                () -> FileBackedTaskManager.loadFromFile(file, historyManager));
        assertEquals(expectedMessage, exception.getMessage(), WRONG_EXCEPTION_MESSAGE);
    }

    @Test
    public void shouldThrowWhenTaskTitleFromFileDoesNotEndWithQuote() throws IOException {
        file = makeTestFileFromString("""
                lastUsedId=1000
                id,type,name,status,description,epic
                1,TASK,"Title,IN_PROGRESS,"Description",
                """);
        String expectedMessage = "comma expected (" + file + ":3:28)";

        Exception exception = assertThrows(ManagerLoadException.class,
                () -> FileBackedTaskManager.loadFromFile(file, historyManager));
        assertEquals(expectedMessage, exception.getMessage(), WRONG_EXCEPTION_MESSAGE);
    }

    @Test
    public void shouldThrowWhenTaskTitleFromFileHasNoQuotes() throws IOException {
        file = makeTestFileFromString("""
                lastUsedId=1000
                id,type,name,status,description,epic
                1,TASK,Title,IN_PROGRESS,"Description",
                """);
        String expectedMessage = "text value must be inside double quotes (" + file + ":3:8)";

        Exception exception = assertThrows(ManagerLoadException.class,
                () -> FileBackedTaskManager.loadFromFile(file, historyManager));
        assertEquals(expectedMessage, exception.getMessage(), WRONG_EXCEPTION_MESSAGE);
    }

    @Test
    public void shouldThrowWhenTaskFromFileHasUnknownStatus() throws IOException {
        file = makeTestFileFromString("""
                lastUsedId=1000
                id,type,name,status,description,epic
                1,TASK,"Title",text,"Description",
                """);
        String expectedMessage = "unknown task status (" + file + ":3:16)";

        Exception exception = assertThrows(ManagerLoadException.class,
                () -> FileBackedTaskManager.loadFromFile(file, historyManager));
        assertEquals(expectedMessage, exception.getMessage(), WRONG_EXCEPTION_MESSAGE);
    }

    @Test
    public void shouldThrowWhenEpicFromFileHasStatus() throws IOException {
        file = makeTestFileFromString("""
                lastUsedId=1000
                id,type,name,status,description,epic
                1,EPIC,"Title",DONE,"Description",
                """);
        String expectedMessage = "explicit epic status (" + file + ":3:16)";

        Exception exception = assertThrows(ManagerLoadException.class,
                () -> FileBackedTaskManager.loadFromFile(file, historyManager));
        assertEquals(expectedMessage, exception.getMessage(), WRONG_EXCEPTION_MESSAGE);
    }

    @Test
    public void shouldThrowWhenTaskDescriptionFromFileDoesNotStartWithQuote() throws IOException {
        file = makeTestFileFromString("""
                lastUsedId=1000
                id,type,name,status,description,epic
                1,TASK,"Title",IN_PROGRESS,Description",
                """);
        String expectedMessage = "unexpected double quote (" + file + ":3:39)";

        Exception exception = assertThrows(ManagerLoadException.class,
                () -> FileBackedTaskManager.loadFromFile(file, historyManager));
        assertEquals(expectedMessage, exception.getMessage(), WRONG_EXCEPTION_MESSAGE);
    }

    @Test
    public void shouldThrowWhenTaskDescriptionFromFileDoesNotEndWithQuote() throws IOException {
        file = makeTestFileFromString("""
                lastUsedId=1000
                id,type,name,status,description,epic
                1,TASK,"Title",IN_PROGRESS,"Description,
                """);
        String expectedMessage = "double quote expected (" + file + ":3:41)";

        Exception exception = assertThrows(ManagerLoadException.class,
                () -> FileBackedTaskManager.loadFromFile(file, historyManager));
        assertEquals(expectedMessage, exception.getMessage(), WRONG_EXCEPTION_MESSAGE);
    }

    @Test
    public void shouldThrowWhenTaskDescriptionFromFileHasNoQuotes() throws IOException {
        file = makeTestFileFromString("""
                lastUsedId=1000
                id,type,name,status,description,epic
                1,TASK,"Title",IN_PROGRESS,Description,
                """);
        String expectedMessage = "text value must be inside double quotes (" + file + ":3:28)";

        Exception exception = assertThrows(ManagerLoadException.class,
                () -> FileBackedTaskManager.loadFromFile(file, historyManager));
        assertEquals(expectedMessage, exception.getMessage(), WRONG_EXCEPTION_MESSAGE);
    }

    @Test
    public void shouldThrowWhenTaskFromFileHasEpicId() throws IOException {
        file = makeTestFileFromString("""
                lastUsedId=1000
                id,type,name,status,description,epic
                1,EPIC,"Epic",,"Epic description",
                2,TASK,"Task",IN_PROGRESS,"Task description",1
                """);
        String expectedMessage = "unexpected data (" + file + ":4:46)";

        Exception exception = assertThrows(ManagerLoadException.class,
                () -> FileBackedTaskManager.loadFromFile(file, historyManager));
        assertEquals(expectedMessage, exception.getMessage(), WRONG_EXCEPTION_MESSAGE);
    }

    @Test
    public void shouldThrowWhenEpicFromFileHasEpicId() throws IOException {
        file = makeTestFileFromString("""
                lastUsedId=1000
                id,type,name,status,description,epic
                1,EPIC,"Epic A",,"Epic A description",
                2,EPIC,"Epic B",,"Epic B description",1
                """);
        String expectedMessage = "unexpected data (" + file + ":4:39)";

        Exception exception = assertThrows(ManagerLoadException.class,
                () -> FileBackedTaskManager.loadFromFile(file, historyManager));
        assertEquals(expectedMessage, exception.getMessage(), WRONG_EXCEPTION_MESSAGE);
    }

    @Test
    public void shouldThrowWhenSubtaskFromFileHasNoEpicId() throws IOException {
        file = makeTestFileFromString("""
                lastUsedId=1000
                id,type,name,status,description,epic
                1,EPIC,"Epic",,"Epic description",
                2,SUBTASK,"Subtask",NEW,"Subtask description",
                """);
        String expectedMessage = "number expected (" + file + ":4:47)";

        Exception exception = assertThrows(ManagerLoadException.class,
                () -> FileBackedTaskManager.loadFromFile(file, historyManager));
        assertEquals(expectedMessage, exception.getMessage(), WRONG_EXCEPTION_MESSAGE);
    }

    @Test
    public void shouldThrowWhenEpicIdOfSubtaskFromFileIsNotNumber() throws IOException {
        file = makeTestFileFromString("""
                lastUsedId=1000
                id,type,name,status,description,epic
                1,EPIC,"Epic",,"Epic description",
                2,SUBTASK,"Subtask",NEW,"Subtask description",one
                """);
        String expectedMessage = "number expected (" + file + ":4:47)";

        Exception exception = assertThrows(ManagerLoadException.class,
                () -> FileBackedTaskManager.loadFromFile(file, historyManager));
        assertEquals(expectedMessage, exception.getMessage(), WRONG_EXCEPTION_MESSAGE);
    }

    @Test
    public void shouldThrowWhenSubtaskFromFileHasUnknownEpicId() throws IOException {
        file = makeTestFileFromString("""
                lastUsedId=1000
                id,type,name,status,description,epic
                1,EPIC,"Epic",,"Epic description",
                2,SUBTASK,"Subtask",NEW,"Subtask description",3
                """);
        String expectedMessage = "no epic with id=3 (" + file + ":4:47)";

        Exception exception = assertThrows(ManagerLoadException.class,
                () -> FileBackedTaskManager.loadFromFile(file, historyManager));
        assertEquals(expectedMessage, exception.getMessage(), WRONG_EXCEPTION_MESSAGE);
    }

    @Test
    public void shouldThrowWhenSubtaskFromFileHasTaskIdAsEpicId() throws IOException {
        file = makeTestFileFromString("""
                lastUsedId=1000
                id,type,name,status,description,epic
                1,TASK,"Task",NEW,"Task description",
                2,EPIC,"Epic",,"Epic description",
                3,SUBTASK,"Subtask",NEW,"Subtask description",1
                """);
        String expectedMessage = "no epic with id=1 (" + file + ":5:47)";

        Exception exception = assertThrows(ManagerLoadException.class,
                () -> FileBackedTaskManager.loadFromFile(file, historyManager));
        assertEquals(expectedMessage, exception.getMessage(), WRONG_EXCEPTION_MESSAGE);
    }

    @Test
    public void shouldThrowWhenSubtaskFromFileHasSubtaskIdAsEpicId() throws IOException {
        file = makeTestFileFromString("""
                lastUsedId=1000
                id,type,name,status,description,epic
                1,EPIC,"Epic",,"Epic description",
                2,SUBTASK,"Subtask A",NEW,"Subtask A description",1
                3,SUBTASK,"Subtask B",NEW,"Subtask B description",2
                """);
        String expectedMessage = "no epic with id=2 (" + file + ":5:51)";

        Exception exception = assertThrows(ManagerLoadException.class,
                () -> FileBackedTaskManager.loadFromFile(file, historyManager));
        assertEquals(expectedMessage, exception.getMessage(), WRONG_EXCEPTION_MESSAGE);
    }

    @Test
    public void shouldThrowWhenTaskFromFileHasIdOfAnotherTask() throws IOException {
        file = makeTestFileFromString("""
                lastUsedId=1000
                id,type,name,status,description,epic
                1,TASK,"Task A",NEW,"Task A description",
                1,TASK,"Task B",NEW,"Task B description",
                """);
        String expectedMessage = "duplicate task id (" + file + ":4:1)";

        Exception exception = assertThrows(ManagerLoadException.class,
                () -> FileBackedTaskManager.loadFromFile(file, historyManager));
        assertEquals(expectedMessage, exception.getMessage(), WRONG_EXCEPTION_MESSAGE);
    }

    @Test
    public void shouldThrowWhenTaskFromFileHasIdOfEpic() throws IOException {
        file = makeTestFileFromString("""
                lastUsedId=1000
                id,type,name,status,description,epic
                1,EPIC,"Epic",,"Epic description",
                1,TASK,"Task",NEW,"Task description",
                """);
        String expectedMessage = "duplicate task id (" + file + ":4:1)";

        Exception exception = assertThrows(ManagerLoadException.class,
                () -> FileBackedTaskManager.loadFromFile(file, historyManager));
        assertEquals(expectedMessage, exception.getMessage(), WRONG_EXCEPTION_MESSAGE);
    }

    @Test
    public void shouldThrowWhenTaskFromFileHasIdOfSubtask() throws IOException {
        file = makeTestFileFromString("""
                lastUsedId=1000
                id,type,name,status,description,epic
                1,EPIC,"Epic",,"Epic description",
                2,SUBTASK,"Subtask",NEW,"Subtask description",1
                2,TASK,"Task",NEW,"Task description",
                """);
        String expectedMessage = "duplicate task id (" + file + ":5:1)";

        Exception exception = assertThrows(ManagerLoadException.class,
                () -> FileBackedTaskManager.loadFromFile(file, historyManager));
        assertEquals(expectedMessage, exception.getMessage(), WRONG_EXCEPTION_MESSAGE);
    }

    @Test
    public void shouldThrowWhenEpicFromFileHasIdOfTask() throws IOException {
        file = makeTestFileFromString("""
                lastUsedId=1000
                id,type,name,status,description,epic
                1,TASK,"Task",NEW,"Task description",
                1,EPIC,"Epic",,"Epic description",
                """);
        String expectedMessage = "duplicate task id (" + file + ":4:1)";

        Exception exception = assertThrows(ManagerLoadException.class,
                () -> FileBackedTaskManager.loadFromFile(file, historyManager));
        assertEquals(expectedMessage, exception.getMessage(), WRONG_EXCEPTION_MESSAGE);
    }

    @Test
    public void shouldThrowWhenEpicFromFileHasIdOfAnotherEpic() throws IOException {
        file = makeTestFileFromString("""
                lastUsedId=1000
                id,type,name,status,description,epic
                1,EPIC,"Epic A",,"Epic A description",
                1,EPIC,"Epic B",,"Epic B description",
                """);
        String expectedMessage = "duplicate task id (" + file + ":4:1)";

        Exception exception = assertThrows(ManagerLoadException.class,
                () -> FileBackedTaskManager.loadFromFile(file, historyManager));
        assertEquals(expectedMessage, exception.getMessage(), WRONG_EXCEPTION_MESSAGE);
    }

    @Test
    public void shouldThrowWhenEpicFromFileHasIdOfSubtask() throws IOException {
        file = makeTestFileFromString("""
                lastUsedId=1000
                id,type,name,status,description,epic
                1,EPIC,"Epic A",,"Epic A description",
                2,SUBTASK,"Subtask",NEW,"Subtask description",1
                2,EPIC,"Epic B",,"Epic B description",
                """);
        String expectedMessage = "duplicate task id (" + file + ":5:1)";

        Exception exception = assertThrows(ManagerLoadException.class,
                () -> FileBackedTaskManager.loadFromFile(file, historyManager));
        assertEquals(expectedMessage, exception.getMessage(), WRONG_EXCEPTION_MESSAGE);
    }

    @Test
    public void shouldThrowWhenSubtaskFromFileHasIdOfTask() throws IOException {
        file = makeTestFileFromString("""
                lastUsedId=1000
                id,type,name,status,description,epic
                1,TASK,"Task",NEW,"Task description",
                2,EPIC,"Epic",,"Subtask description",
                1,SUBTASK,"Subtask",NEW,"Subtask description",2
                """);
        String expectedMessage = "duplicate task id (" + file + ":5:1)";

        Exception exception = assertThrows(ManagerLoadException.class,
                () -> FileBackedTaskManager.loadFromFile(file, historyManager));
        assertEquals(expectedMessage, exception.getMessage(), WRONG_EXCEPTION_MESSAGE);
    }

    @Test
    public void shouldThrowWhenSubtaskFromFileHasIdOfEpic() throws IOException {
        file = makeTestFileFromString("""
                lastUsedId=1000
                id,type,name,status,description,epic
                1,EPIC,"Epic A",,"Epic A description",
                2,EPIC,"Epic B",,"Epic B description",
                1,SUBTASK,"Subtask",NEW,"Subtask description",2
                """);
        String expectedMessage = "duplicate task id (" + file + ":5:1)";

        Exception exception = assertThrows(ManagerLoadException.class,
                () -> FileBackedTaskManager.loadFromFile(file, historyManager));
        assertEquals(expectedMessage, exception.getMessage(), WRONG_EXCEPTION_MESSAGE);
    }

    @Test
    public void shouldThrowWhenSubtaskFromFileHasIdOfAnotherSubtask() throws IOException {
        file = makeTestFileFromString("""
                lastUsedId=1000
                id,type,name,status,description,epic
                1,EPIC,"Epic",,"Epic description",
                2,SUBTASK,"Subtask A",NEW,"Subtask A description",1
                2,SUBTASK,"Subtask B",NEW,"Subtask B description",1
                """);
        String expectedMessage = "duplicate task id (" + file + ":5:1)";

        Exception exception = assertThrows(ManagerLoadException.class,
                () -> FileBackedTaskManager.loadFromFile(file, historyManager));
        assertEquals(expectedMessage, exception.getMessage(), WRONG_EXCEPTION_MESSAGE);
    }

    private File makeTestFileFromString(String testData) throws IOException {
        File testFile = File.createTempFile("kanban", null);
        Files.writeString(testFile.toPath(), testData, StandardCharsets.UTF_8);
        return testFile;
    }
}