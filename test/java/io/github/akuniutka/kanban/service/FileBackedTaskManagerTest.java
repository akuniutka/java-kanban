package io.github.akuniutka.kanban.service;

import io.github.akuniutka.kanban.exception.ManagerLoadException;
import io.github.akuniutka.kanban.exception.ManagerSaveException;
import io.github.akuniutka.kanban.model.Epic;
import io.github.akuniutka.kanban.model.Subtask;
import io.github.akuniutka.kanban.model.Task;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.List;

import static io.github.akuniutka.kanban.TestModels.*;
import static org.junit.jupiter.api.Assertions.*;

class FileBackedTaskManagerTest extends AbstractTaskManagerTest {
    protected static final String WRONG_FILE_FORMAT = "wrong file format";
    protected final Path path;

    public FileBackedTaskManagerTest() throws IOException {
        this.path = Files.createTempFile("kanban", null);
        this.manager = new FileBackedTaskManager(this.path, this.historyManager);
    }

    @Test
    public void shouldCreateFileBackedTaskManagerOfInterfaceType() {
        assertNotNull(manager, "task manager was not created");
    }

    @Test
    public void shouldThrowWhenFileIsNull() {
        final Exception exception = assertThrows(NullPointerException.class,
                () -> new FileBackedTaskManager(null, historyManager));
        assertEquals("cannot start: file is null", exception.getMessage(), WRONG_EXCEPTION_MESSAGE);
    }

    @Test
    public void shouldImmediatelyThrowWhenCannotInitializeDataFile() {
        final String filename = ".";
        final String expectedMessage = "cannot write to file \"" + filename + "\"";

        final Exception exception = assertThrows(ManagerSaveException.class,
                () -> new FileBackedTaskManager(Paths.get(filename), historyManager));
        assertEquals(expectedMessage, exception.getMessage(), WRONG_EXCEPTION_MESSAGE);
    }

    @Test
    public void shouldThrowWhenHistoryManagerIsNull() {
        final Exception exception = assertThrows(NullPointerException.class,
                () -> new FileBackedTaskManager(path, null));
        assertEquals("cannot start: history manager is null", exception.getMessage(), WRONG_EXCEPTION_MESSAGE);
    }

    @Test
    public void shouldSaveWhenAddTask() throws IOException {
        String expectedString = """
                id,type,name,status,description,duration,start,epic
                %%d,TASK,"%s",%s,"%s",%s,%s,
                """.formatted(testTask.getTitle(), testTask.getStatus(), testTask.getDescription(),
                testTask.getDuration().toMinutes(), testTask.getStartTime());

        final long taskId = manager.addTask(testTask);

        expectedString = expectedString.formatted(taskId);
        final String actualString = Files.readString(path);
        assertEquals(expectedString, actualString, WRONG_FILE_FORMAT);
    }

    @Test
    public void shouldSaveWhenAddTaskAndFieldsNull() throws IOException {
        String expectedString = """
                id,type,name,status,description,duration,start,epic
                %d,TASK,null,null,null,null,null,
                """;

        final long taskId = manager.addTask(fromEmptyTask().build());

        expectedString = expectedString.formatted(taskId);
        final String actualString = Files.readString(path);
        assertEquals(expectedString, actualString, WRONG_FILE_FORMAT);
    }

    @Test
    public void shouldSaveWhenUpdateTask() throws IOException {
        final long taskId = manager.addTask(testTask);
        final String expectedString = """
                id,type,name,status,description,duration,start,epic
                %d,TASK,"%s",%s,"%s",%s,%s,
                """.formatted(taskId, modifiedTask.getTitle(), modifiedTask.getStatus(),
                modifiedTask.getDescription(), modifiedTask.getDuration().toMinutes(), modifiedTask.getStartTime());
        final Task update = fromModifiedTask().withId(taskId).build();

        manager.updateTask(update);

        final String actualString = Files.readString(path);
        assertEquals(expectedString, actualString, WRONG_FILE_FORMAT);
    }

    @Test
    public void shouldSaveWhenRemoveTask() throws IOException {
        final long taskId = manager.addTask(testTask);
        final String expectedString = """
                id,type,name,status,description,duration,start,epic
                """;

        manager.removeTask(taskId);

        final String actualString = Files.readString(path);
        assertEquals(expectedString, actualString, WRONG_FILE_FORMAT);
    }

    @Test
    public void shouldSaveWhenAddEpic() throws IOException {
        String expectedString = """
                id,type,name,status,description,duration,start,epic
                %%d,EPIC,"%s",,"%s",,,
                """.formatted(testEpic.getTitle(), testEpic.getDescription());

        final long epicId = manager.addEpic(testEpic);

        expectedString = expectedString.formatted(epicId);
        final String actualString = Files.readString(path);
        assertEquals(expectedString, actualString, WRONG_FILE_FORMAT);
    }

    @Test
    public void shouldSaveWhenAddEpicAndFieldsNull() throws IOException {
        String expectedString = """
                id,type,name,status,description,duration,start,epic
                %d,EPIC,null,,null,,,
                """;

        final long epicId = manager.addEpic(fromEmptyEpic().build());

        expectedString = expectedString.formatted(epicId);
        final String actualString = Files.readString(path);
        assertEquals(expectedString, actualString, WRONG_FILE_FORMAT);
    }

    @Test
    public void shouldSaveWhenUpdateEpic() throws IOException {
        final long epicId = manager.addEpic(testEpic);
        final String expectedString = """
                id,type,name,status,description,duration,start,epic
                %d,EPIC,"%s",,"%s",,,
                """.formatted(epicId, modifiedEpic.getTitle(), modifiedEpic.getDescription());
        final Epic update = fromModifiedEpic().withId(epicId).build();

        manager.updateEpic(update);

        final String actualString = Files.readString(path);
        assertEquals(expectedString, actualString, WRONG_FILE_FORMAT);
    }

    @Test
    public void shouldSaveWhenRemoveEpic() throws IOException {
        final long epicId = manager.addEpic(testEpic);
        final String expectedString = """
                id,type,name,status,description,duration,start,epic
                """;

        manager.removeEpic(epicId);

        final String actualString = Files.readString(path);
        assertEquals(expectedString, actualString, WRONG_FILE_FORMAT);
    }

    @Test
    public void shouldSaveWhenAddSubtask() throws IOException {
        final Subtask expectedSubtask = fromTestSubtask().build();
        String expectedString = """
                id,type,name,status,description,duration,start,epic
                %%d,EPIC,"%s",,"%s",,,
                %%d,SUBTASK,"%s",%s,"%s",%s,%s,%%d
                """.formatted(testEpic.getTitle(), testEpic.getDescription(), expectedSubtask.getTitle(),
                expectedSubtask.getStatus(), expectedSubtask.getDescription(),
                expectedSubtask.getDuration().toMinutes(), expectedSubtask.getStartTime());
        final long epicId = manager.addEpic(testEpic);
        final Subtask subtask = fromTestSubtask().withId(null).withEpicId(epicId).build();

        final long subtaskId = manager.addSubtask(subtask);

        expectedString = expectedString.formatted(epicId, subtaskId, epicId);
        final String actualString = Files.readString(path);
        assertEquals(expectedString, actualString, WRONG_FILE_FORMAT);
    }

    @Test
    public void shouldSaveWhenAddSubtaskAndFieldsNull() throws IOException {
        String expectedString = """
                id,type,name,status,description,duration,start,epic
                %%d,EPIC,"%s",,"%s",,,
                %%d,SUBTASK,null,null,null,null,null,%%d
                """.formatted(testEpic.getTitle(), testEpic.getDescription());
        final long epicId = manager.addEpic(testEpic);
        final Subtask subtask = fromEmptySubtask().withEpicId(epicId).build();

        final long subtaskId = manager.addSubtask(subtask);

        expectedString = expectedString.formatted(epicId, subtaskId, epicId);
        final String actualString = Files.readString(path);
        assertEquals(expectedString, actualString, WRONG_FILE_FORMAT);
    }

    @Test
    public void shouldSaveWhenUpdateSubtask() throws IOException {
        final Subtask expectedSubtask = fromModifiedSubtask().build();
        String expectedString = """
                id,type,name,status,description,duration,start,epic
                %%d,EPIC,"%s",,"%s",,,
                %%d,SUBTASK,"%s",%s,"%s",%s,%s,%%d
                """.formatted(testEpic.getTitle(), testEpic.getDescription(), expectedSubtask.getTitle(),
                expectedSubtask.getStatus(), expectedSubtask.getDescription(),
                expectedSubtask.getDuration().toMinutes(), expectedSubtask.getStartTime());
        final long epicId = manager.addEpic(testEpic);
        final Subtask subtask = fromTestSubtask().withId(null).withEpicId(epicId).build();
        final long subtaskId = manager.addSubtask(subtask);
        final Subtask update = fromModifiedSubtask().withId(subtaskId).withEpicId(epicId).build();

        manager.updateSubtask(update);

        expectedString = expectedString.formatted(epicId, subtaskId, epicId);
        final String actualString = Files.readString(path);
        assertEquals(expectedString, actualString, WRONG_FILE_FORMAT);
    }

    @Test
    public void shouldSaveWhenRemoveSubtask() throws IOException {
        String expectedString = """
                id,type,name,status,description,duration,start,epic
                %%d,EPIC,"%s",,"%s",,,
                """.formatted(testEpic.getTitle(), testEpic.getDescription());
        final long epicId = manager.addEpic(testEpic);
        final Subtask subtask = fromTestSubtask().withId(null).withEpicId(epicId).build();
        final long subtaskId = manager.addSubtask(subtask);

        manager.removeSubtask(subtaskId);

        expectedString = expectedString.formatted(epicId);
        final String actualString = Files.readString(path);
        assertEquals(expectedString, actualString, WRONG_FILE_FORMAT);
    }

    @Test
    public void shouldSaveWhenRemoveTasks() throws IOException {
        manager.addTask(testTask);
        manager.addTask(modifiedTask);
        final String expectedString = """
                id,type,name,status,description,duration,start,epic
                """;

        manager.removeTasks();

        final String actualString = Files.readString(path);
        assertEquals(expectedString, actualString, WRONG_FILE_FORMAT);
    }

    @Test
    public void shouldSaveWhenRemoveEpics() throws IOException {
        manager.addEpic(testEpic);
        manager.addEpic(modifiedEpic);
        final String expectedString = """
                id,type,name,status,description,duration,start,epic
                """;

        manager.removeEpics();

        final String actualString = Files.readString(path);
        assertEquals(expectedString, actualString, WRONG_FILE_FORMAT);
    }

    @Test
    public void shouldSaveWhenRemoveSubtasks() throws IOException {
        String expectedString = """
                id,type,name,status,description,duration,start,epic
                %%d,EPIC,"%s",,"%s",,,
                """.formatted(testEpic.getTitle(), testEpic.getDescription());
        final long epicId = manager.addEpic(testEpic);
        final Subtask subtaskA = fromTestSubtask().withId(null).withEpicId(epicId).build();
        final Subtask subtaskB = fromModifiedSubtask().withId(null).withEpicId(epicId).build();
        manager.addSubtask(subtaskA);
        manager.addSubtask(subtaskB);

        manager.removeSubtasks();

        expectedString = expectedString.formatted(epicId);
        final String actualString = Files.readString(path);
        assertEquals(expectedString, actualString, WRONG_FILE_FORMAT);
    }

    @Test
    public void shouldThrowWhenFileToLoadIsNull() {
        final Exception exception = assertThrows(NullPointerException.class,
                () -> FileBackedTaskManager.loadFromFile(null, historyManager));
        assertEquals("cannot start: file is null", exception.getMessage(), WRONG_EXCEPTION_MESSAGE);
    }

    @Test
    public void shouldThrowWhenCannotLoadFromFile() {
        final String filename = ".";
        final String expectedMessage = "cannot load from file \"" + filename + "\"";

        final Exception exception = assertThrows(ManagerLoadException.class,
                () -> FileBackedTaskManager.loadFromFile(Paths.get(filename), historyManager));
        assertEquals(expectedMessage, exception.getMessage(), WRONG_EXCEPTION_MESSAGE);
    }

    @Test
    public void shouldThrowWhenFileIsEmpty() throws IOException {
        fillTestFileWithData("");
        final String expectedMessage = """
                wrong file header, expected "id,type,name,status,description,duration,start,epic"\
                """;

        final Exception exception = assertThrows(ManagerLoadException.class,
                () -> FileBackedTaskManager.loadFromFile(path, historyManager));
        assertEquals(expectedMessage, exception.getMessage(), WRONG_EXCEPTION_MESSAGE);
    }

    @Test
    public void shouldThrowWhenFileContainsNoHeader() throws IOException {
        fillTestFileWithData("""
                """);
        final String expectedMessage = """
                wrong file header, expected "id,type,name,status,description,duration,start,epic"\
                """;

        final Exception exception = assertThrows(ManagerLoadException.class,
                () -> FileBackedTaskManager.loadFromFile(path, historyManager));
        assertEquals(expectedMessage, exception.getMessage(), WRONG_EXCEPTION_MESSAGE);
    }

    @Test
    public void shouldThrowWhenFileContainsWrongHeader() throws IOException {
        fillTestFileWithData("""
                id, type,name,status,description,duration,start,epic
                """);
        final String expectedMessage = """
                wrong file header, expected "id,type,name,status,description,duration,start,epic"\
                """;

        final Exception exception = assertThrows(ManagerLoadException.class,
                () -> FileBackedTaskManager.loadFromFile(path, historyManager));
        assertEquals(expectedMessage, exception.getMessage(), WRONG_EXCEPTION_MESSAGE);
    }

    @Test
    public void shouldThrowWhenTaskIdFromFileIsEmpty() throws IOException {
        fillTestFileWithData("""
                id,type,name,status,description,duration,start,epic
                %s
                """.formatted(""));
        final String expectedMessage = "line does not start with numeric id";

        final Exception exception = assertThrows(ManagerLoadException.class,
                () -> FileBackedTaskManager.loadFromFile(path, historyManager));
        assertEquals(expectedMessage, exception.getMessage(), WRONG_EXCEPTION_MESSAGE);
    }

    @Test
    public void shouldThrowWhenTaskFromFileHasUnknownType() throws IOException {
        fillTestFileWithData("""
                id,type,name,status,description,duration,start,epic
                1,text,"Title",IN_PROGRESS,"Description",30,2000-05-01T13:30,
                """);
        final String expectedMessage = "unknown task type for id=1";

        final Exception exception = assertThrows(ManagerLoadException.class,
                () -> FileBackedTaskManager.loadFromFile(path, historyManager));
        assertEquals(expectedMessage, exception.getMessage(), WRONG_EXCEPTION_MESSAGE);
    }

    @Test
    public void shouldNotLoadTaskWhenNotEnoughFields() throws IOException {
        fillTestFileWithData("""
                id,type,name,status,description,duration,start,epic
                1,TASK,"Title",IN_PROGRESS,"Description",30,2000-05-01T13:30
                """);
        final String expectedMessage = "unexpected end of line for id=1";

        final Exception exception = assertThrows(ManagerLoadException.class,
                () -> FileBackedTaskManager.loadFromFile(path, historyManager));
        assertEquals(expectedMessage, exception.getMessage(), WRONG_EXCEPTION_MESSAGE);
    }

    @Test
    public void shouldNotLoadTaskWhenExcessiveFields() throws IOException {
        fillTestFileWithData("""
                id,type,name,status,description,duration,start,epic
                1,TASK,"Task",IN_PROGRESS,"Task description",30,2000-05-01T13:30,,
                """);
        final String expectedMessage = "unexpected data for id=1";

        final Exception exception = assertThrows(ManagerLoadException.class,
                () -> FileBackedTaskManager.loadFromFile(path, historyManager));
        assertEquals(expectedMessage, exception.getMessage(), WRONG_EXCEPTION_MESSAGE);
    }

    @Test
    public void shouldNotLoadTaskWhenIdNotNumber() throws IOException {
        fillTestFileWithData("""
                id,type,name,status,description,duration,start,epic
                one,TASK,"Title",IN_PROGRESS,"Description",30,2000-05-01T13:30,
                """);
        final String expectedMessage = "line does not start with numeric id";

        final Exception exception = assertThrows(ManagerLoadException.class,
                () -> FileBackedTaskManager.loadFromFile(path, historyManager));
        assertEquals(expectedMessage, exception.getMessage(), WRONG_EXCEPTION_MESSAGE);
    }

    @Test
    public void shouldNotLoadTaskWhenIdNotInteger() throws IOException {
        fillTestFileWithData("""
                id,type,name,status,description,duration,start,epic
                1.5,TASK,"Title",IN_PROGRESS,"Description",30,2000-05-01T13:30,
                """);
        final String expectedMessage = "line does not start with numeric id";

        final Exception exception = assertThrows(ManagerLoadException.class,
                () -> FileBackedTaskManager.loadFromFile(path, historyManager));
        assertEquals(expectedMessage, exception.getMessage(), WRONG_EXCEPTION_MESSAGE);
    }

    @Test
    public void shouldNotLoadTaskWhenIdIsAnotherTaskId() throws IOException {
        fillTestFileWithData("""
                id,type,name,status,description,duration,start,epic
                1,TASK,"Task A",NEW,"Task A description",30,2000-05-01T13:30,
                1,TASK,"Task B",NEW,"Task B description",90,2000-05-01T15:00,
                """);
        final String expectedMessage = "duplicate id=1";

        final Exception exception = assertThrows(ManagerLoadException.class,
                () -> FileBackedTaskManager.loadFromFile(path, historyManager));
        assertEquals(expectedMessage, exception.getMessage(), WRONG_EXCEPTION_MESSAGE);
    }

    @Test
    public void shouldNotLoadTaskWhenIdIsEpicId() throws IOException {
        fillTestFileWithData("""
                id,type,name,status,description,duration,start,epic
                1,EPIC,"Epic",,"Epic description",,,
                1,TASK,"Task",NEW,"Task description",30,2000-05-01T13:30,
                """);
        final String expectedMessage = "duplicate id=1";

        final Exception exception = assertThrows(ManagerLoadException.class,
                () -> FileBackedTaskManager.loadFromFile(path, historyManager));
        assertEquals(expectedMessage, exception.getMessage(), WRONG_EXCEPTION_MESSAGE);
    }

    @Test
    public void shouldNotLoadTaskWhenIdIsSubtaskId() throws IOException {
        fillTestFileWithData("""
                id,type,name,status,description,duration,start,epic
                1,EPIC,"Epic",,"Epic description",,,
                2,SUBTASK,"Subtask",NEW,"Subtask description",90,2000-05-01T15:00,1
                2,TASK,"Task",NEW,"Task description",30,2000-05-01T13:30,
                """);
        final String expectedMessage = "duplicate id=2";

        final Exception exception = assertThrows(ManagerLoadException.class,
                () -> FileBackedTaskManager.loadFromFile(path, historyManager));
        assertEquals(expectedMessage, exception.getMessage(), WRONG_EXCEPTION_MESSAGE);
    }

    @Test
    public void shouldNotLoadTaskWhenTitleDoesNotStartWithQuote() throws IOException {
        fillTestFileWithData("""
                id,type,name,status,description,duration,start,epic
                1,TASK,Title",IN_PROGRESS,"Description",30,2000-05-01T13:30,
                """);
        final String expectedMessage = "no comma before opening double quote for id=1";

        final Exception exception = assertThrows(ManagerLoadException.class,
                () -> FileBackedTaskManager.loadFromFile(path, historyManager));
        assertEquals(expectedMessage, exception.getMessage(), WRONG_EXCEPTION_MESSAGE);
    }

    @Test
    public void shouldNotLoadTaskWhenTitleDoesNotEndWithQuote() throws IOException {
        fillTestFileWithData("""
                id,type,name,status,description,duration,start,epic
                1,TASK,"Title,IN_PROGRESS,"Description",30,2000-05-01T13:30,
                """);
        final String expectedMessage = "no comma after closing double quote for id=1";

        final Exception exception = assertThrows(ManagerLoadException.class,
                () -> FileBackedTaskManager.loadFromFile(path, historyManager));
        assertEquals(expectedMessage, exception.getMessage(), WRONG_EXCEPTION_MESSAGE);
    }

    @Test
    public void shouldNotLoadTaskWhenTitleHasNoQuotes() throws IOException {
        fillTestFileWithData("""
                id,type,name,status,description,duration,start,epic
                1,TASK,Title,IN_PROGRESS,"Description",30,2000-05-01T13:30,
                """);
        final String expectedMessage = "text value must be inside double quotes for id=1";

        final Exception exception = assertThrows(ManagerLoadException.class,
                () -> FileBackedTaskManager.loadFromFile(path, historyManager));
        assertEquals(expectedMessage, exception.getMessage(), WRONG_EXCEPTION_MESSAGE);
    }

    @Test
    public void shouldNotLoadTaskWhenHasUnknownStatus() throws IOException {
        fillTestFileWithData("""
                id,type,name,status,description,duration,start,epic
                1,TASK,"Title",text,"Description",30,2000-05-01T13:30,
                """);
        final String expectedMessage = "unknown task status for id=1";

        final Exception exception = assertThrows(ManagerLoadException.class,
                () -> FileBackedTaskManager.loadFromFile(path, historyManager));
        assertEquals(expectedMessage, exception.getMessage(), WRONG_EXCEPTION_MESSAGE);
    }

    @Test
    public void shouldNotLoadTaskWhenDescriptionDoesNotStartWithQuote() throws IOException {
        fillTestFileWithData("""
                id,type,name,status,description,duration,start,epic
                1,TASK,"Title",IN_PROGRESS,Description",30,2000-05-01T13:30,
                """);
        final String expectedMessage = "no comma before opening double quote for id=1";

        final Exception exception = assertThrows(ManagerLoadException.class,
                () -> FileBackedTaskManager.loadFromFile(path, historyManager));
        assertEquals(expectedMessage, exception.getMessage(), WRONG_EXCEPTION_MESSAGE);
    }

    @Test
    public void shouldNotLoadTaskWhenDescriptionDoesNotEndWithQuote() throws IOException {
        fillTestFileWithData("""
                id,type,name,status,description,duration,start,epic
                1,TASK,"Title",IN_PROGRESS,"Description,30,2000-05-01T13:30,
                """);
        final String expectedMessage = "no closing double quote for id=1";

        final Exception exception = assertThrows(ManagerLoadException.class,
                () -> FileBackedTaskManager.loadFromFile(path, historyManager));
        assertEquals(expectedMessage, exception.getMessage(), WRONG_EXCEPTION_MESSAGE);
    }

    @Test
    public void shouldNotLoadTaskWhenDescriptionHasNoQuotes() throws IOException {
        fillTestFileWithData("""
                id,type,name,status,description,duration,start,epic
                1,TASK,"Title",IN_PROGRESS,Description,30,2000-05-01T13:30,
                """);
        final String expectedMessage = "text value must be inside double quotes for id=1";

        final Exception exception = assertThrows(ManagerLoadException.class,
                () -> FileBackedTaskManager.loadFromFile(path, historyManager));
        assertEquals(expectedMessage, exception.getMessage(), WRONG_EXCEPTION_MESSAGE);
    }

    @Test
    public void shouldNotLoadTaskWhenDurationNotNumber() throws IOException {
        fillTestFileWithData("""
                id,type,name,status,description,duration,start,epic
                1,TASK,"Title",IN_PROGRESS,"Description",thirty,2000-05-01T13:30,
                """);
        final String expectedMessage = "wrong duration format for id=1";

        final Exception exception = assertThrows(ManagerLoadException.class,
                () -> FileBackedTaskManager.loadFromFile(path, historyManager));
        assertEquals(expectedMessage, exception.getMessage(), WRONG_EXCEPTION_MESSAGE);
    }

    @Test
    public void shouldNotLoadTaskWhenDurationNotInteger() throws IOException {
        fillTestFileWithData("""
                id,type,name,status,description,duration,start,epic
                1,TASK,"Title",IN_PROGRESS,"Description",30.5,2000-05-01T13:30,
                """);
        final String expectedMessage = "wrong duration format for id=1";

        final Exception exception = assertThrows(ManagerLoadException.class,
                () -> FileBackedTaskManager.loadFromFile(path, historyManager));
        assertEquals(expectedMessage, exception.getMessage(), WRONG_EXCEPTION_MESSAGE);
    }

    @Test
    public void shouldNotLoadTaskWhenDurationZero() throws IOException {
        fillTestFileWithData("""
                id,type,name,status,description,duration,start,epic
                1,TASK,"Title",IN_PROGRESS,"Description",0,2000-05-01T13:30,
                """);
        final String expectedMessage = "duration cannot be negative or zero for id=1";

        final Exception exception = assertThrows(ManagerLoadException.class,
                () -> FileBackedTaskManager.loadFromFile(path, historyManager));
        assertEquals(expectedMessage, exception.getMessage(), WRONG_EXCEPTION_MESSAGE);
    }

    @Test
    public void shouldNotLoadTaskWhenDurationNegative() throws IOException {
        fillTestFileWithData("""
                id,type,name,status,description,duration,start,epic
                1,TASK,"Title",IN_PROGRESS,"Description",-30,2000-05-01T13:30,
                """);
        final String expectedMessage = "duration cannot be negative or zero for id=1";

        final Exception exception = assertThrows(ManagerLoadException.class,
                () -> FileBackedTaskManager.loadFromFile(path, historyManager));
        assertEquals(expectedMessage, exception.getMessage(), WRONG_EXCEPTION_MESSAGE);
    }

    @Test
    public void shouldNotLoadTaskWhenStartTimeNotDateTime() throws IOException {
        fillTestFileWithData("""
                id,type,name,status,description,duration,start,epic
                1,TASK,"Title",IN_PROGRESS,"Description",30,text,
                """);
        final String expectedMessage = "wrong start time format for id=1";

        final Exception exception = assertThrows(ManagerLoadException.class,
                () -> FileBackedTaskManager.loadFromFile(path, historyManager));
        assertEquals(expectedMessage, exception.getMessage(), WRONG_EXCEPTION_MESSAGE);
    }

    @Test
    public void shouldNotLoadTaskWhenDurationNullAndStartTimeNotNull() throws IOException {
        fillTestFileWithData("""
                id,type,name,status,description,duration,start,epic
                1,TASK,"Title",IN_PROGRESS,"Description",null,2000-05-01T13:30,
                """);
        final String expectedMessage = "duration and start time must be either both set or both null for id=1";

        final Exception exception = assertThrows(ManagerLoadException.class,
                () -> FileBackedTaskManager.loadFromFile(path, historyManager));
        assertEquals(expectedMessage, exception.getMessage(), WRONG_EXCEPTION_MESSAGE);
    }

    @Test
    public void shouldNotLoadTaskWhenDurationNotNullAndStartTimeNull() throws IOException {
        fillTestFileWithData("""
                id,type,name,status,description,duration,start,epic
                1,TASK,"Title",IN_PROGRESS,"Description",30,null,
                """);
        final String expectedMessage = "duration and start time must be either both set or both null for id=1";

        final Exception exception = assertThrows(ManagerLoadException.class,
                () -> FileBackedTaskManager.loadFromFile(path, historyManager));
        assertEquals(expectedMessage, exception.getMessage(), WRONG_EXCEPTION_MESSAGE);
    }

    @Test
    public void shouldNotLoadTaskWhenAnotherPrioritizedTaskCoversStartTime() throws IOException {
        fillTestFileWithData("""
                id,type,name,status,description,duration,start,epic
                2,EPIC,null,,null,,,
                3,SUBTASK,"Title",IN_PROGRESS,"Description",30,2000-05-01T13:15,2
                1000,TASK,"Title",IN_PROGRESS,"Description",30,2000-05-01T13:30,
                """);
        final String expectedMessage = "conflict with another task for time slot for id=1000";

        final Exception exception = assertThrows(ManagerLoadException.class,
                () -> FileBackedTaskManager.loadFromFile(path, historyManager));
        assertEquals(expectedMessage, exception.getMessage(), WRONG_EXCEPTION_MESSAGE);
    }

    @Test
    public void shouldNotLoadTaskWhenAnotherPrioritizedTaskCoversEndTime() throws IOException {
        fillTestFileWithData("""
                id,type,name,status,description,duration,start,epic
                2,EPIC,null,,null,,,
                3,SUBTASK,"Title",IN_PROGRESS,"Description",30,2000-05-01T13:45,2
                1000,TASK,"Title",IN_PROGRESS,"Description",30,2000-05-01T13:30,
                """);
        final String expectedMessage = "conflict with another task for time slot for id=1000";

        final Exception exception = assertThrows(ManagerLoadException.class,
                () -> FileBackedTaskManager.loadFromFile(path, historyManager));
        assertEquals(expectedMessage, exception.getMessage(), WRONG_EXCEPTION_MESSAGE);
    }

    @Test
    public void shouldNotLoadTaskWhenAnotherPrioritizedTaskCoversWholeInterval() throws IOException {
        fillTestFileWithData("""
                id,type,name,status,description,duration,start,epic
                2,EPIC,null,,null,,,
                3,SUBTASK,"Title",IN_PROGRESS,"Description",60,2000-05-01T13:15,2
                1000,TASK,"Title",IN_PROGRESS,"Description",30,2000-05-01T13:30,
                """);
        final String expectedMessage = "conflict with another task for time slot for id=1000";

        final Exception exception = assertThrows(ManagerLoadException.class,
                () -> FileBackedTaskManager.loadFromFile(path, historyManager));
        assertEquals(expectedMessage, exception.getMessage(), WRONG_EXCEPTION_MESSAGE);
    }

    @Test
    public void shouldNotLoadTaskWhenAnotherPrioritizedTaskWithinInterval() throws IOException {
        fillTestFileWithData("""
                id,type,name,status,description,duration,start,epic
                2,EPIC,null,,null,,,
                3,SUBTASK,"Title",IN_PROGRESS,"Description",20,2000-05-01T13:35,2
                1000,TASK,"Title",IN_PROGRESS,"Description",30,2000-05-01T13:30,
                """);
        final String expectedMessage = "conflict with another task for time slot for id=1000";

        final Exception exception = assertThrows(ManagerLoadException.class,
                () -> FileBackedTaskManager.loadFromFile(path, historyManager));
        assertEquals(expectedMessage, exception.getMessage(), WRONG_EXCEPTION_MESSAGE);
    }

    @Test
    public void shouldNotLoadTaskWhenAnotherPrioritizedTaskWithinIntervalLeftAligned() throws IOException {
        fillTestFileWithData("""
                id,type,name,status,description,duration,start,epic
                2,EPIC,null,,null,,,
                3,SUBTASK,"Title",IN_PROGRESS,"Description",20,2000-05-01T13:30,2
                1000,TASK,"Title",IN_PROGRESS,"Description",30,2000-05-01T13:30,
                """);
        final String expectedMessage = "conflict with another task for time slot for id=1000";

        final Exception exception = assertThrows(ManagerLoadException.class,
                () -> FileBackedTaskManager.loadFromFile(path, historyManager));
        assertEquals(expectedMessage, exception.getMessage(), WRONG_EXCEPTION_MESSAGE);
    }

    @Test
    public void shouldNotLoadTaskWhenAnotherPrioritizedTaskWithinIntervalRightAligned() throws IOException {
        fillTestFileWithData("""
                id,type,name,status,description,duration,start,epic
                2,EPIC,null,,null,,,
                3,SUBTASK,"Title",IN_PROGRESS,"Description",20,2000-05-01T13:40,2
                1000,TASK,"Title",IN_PROGRESS,"Description",30,2000-05-01T13:30,
                """);
        final String expectedMessage = "conflict with another task for time slot for id=1000";

        final Exception exception = assertThrows(ManagerLoadException.class,
                () -> FileBackedTaskManager.loadFromFile(path, historyManager));
        assertEquals(expectedMessage, exception.getMessage(), WRONG_EXCEPTION_MESSAGE);
    }

    @Test
    public void shouldNotLoadTaskWhenAnotherPrioritizedTaskWithSameInterval() throws IOException {
        fillTestFileWithData("""
                id,type,name,status,description,duration,start,epic
                2,EPIC,null,,null,,,
                3,SUBTASK,"Title",IN_PROGRESS,"Description",30,2000-05-01T13:30,2
                1000,TASK,"Title",IN_PROGRESS,"Description",30,2000-05-01T13:30,
                """);
        final String expectedMessage = "conflict with another task for time slot for id=1000";

        final Exception exception = assertThrows(ManagerLoadException.class,
                () -> FileBackedTaskManager.loadFromFile(path, historyManager));
        assertEquals(expectedMessage, exception.getMessage(), WRONG_EXCEPTION_MESSAGE);
    }

    @Test
    public void shouldNotLoadTaskWhenHasEpicId() throws IOException {
        fillTestFileWithData("""
                id,type,name,status,description,duration,start,epic
                1,EPIC,"Epic",,"Epic description",,,
                2,TASK,"Task",IN_PROGRESS,"Task description",30,2000-05-01T13:30,1
                """);
        final String expectedMessage = "unexpected data for id=2";

        final Exception exception = assertThrows(ManagerLoadException.class,
                () -> FileBackedTaskManager.loadFromFile(path, historyManager));
        assertEquals(expectedMessage, exception.getMessage(), WRONG_EXCEPTION_MESSAGE);
    }

    @Test
    public void shouldLoadTaskToGetAndTasksAndPrioritizedWhenStartTimeNotNull() throws IOException {
        final Task expectedTask = fromTestTask().build();
        final List<Task> expectedTasks = List.of(expectedTask);
        fillTestFileWithData("""
                id,type,name,status,description,duration,start,epic
                1,TASK,"Title",IN_PROGRESS,"Description",30,2000-05-01T13:30,
                """);

        manager = FileBackedTaskManager.loadFromFile(path, historyManager);
        final Task savedTask = manager.getTask(1L);
        final List<Task> tasks = manager.getTasks();
        final List<Task> prioritized = manager.getPrioritizedTasks();

        assertAll("task loaded with errors",
                () -> assertTaskEquals(expectedTask, savedTask, "task loaded with errors"),
                () -> assertListEquals(expectedTasks, tasks, "task loaded with errors"),
                () -> assertListEquals(expectedTasks, prioritized, "task loaded with errors")
        );
    }

    @Test
    public void shouldLoadTaskToGetAndTasksAndPrioritizedWithStartTimeTruncatedToMinutes() throws IOException {
        final Task expectedTask = fromTestTask().build();
        final List<Task> expectedTasks = List.of(expectedTask);
        fillTestFileWithData("""
                id,type,name,status,description,duration,start,epic
                1,TASK,"Title",IN_PROGRESS,"Description",30,2000-05-01T13:30:25,
                """);

        manager = FileBackedTaskManager.loadFromFile(path, historyManager);
        final Task savedTask = manager.getTask(1L);
        final List<Task> tasks = manager.getTasks();
        final List<Task> prioritized = manager.getPrioritizedTasks();

        assertAll("task loaded with errors",
                () -> assertTaskEquals(expectedTask, savedTask, "task loaded with errors"),
                () -> assertListEquals(expectedTasks, tasks, "task loaded with errors"),
                () -> assertListEquals(expectedTasks, prioritized, "task loaded with errors")
        );
    }

    @Test
    public void shouldLoadTaskToGetAndTasksNotPrioritizedWhenStartTimeNull() throws IOException {
        final Task expectedTask = fromTestTask().withDuration(null).withStartTime(null).build();
        final List<Task> expectedTasks = List.of(expectedTask);
        fillTestFileWithData("""
                id,type,name,status,description,duration,start,epic
                1,TASK,"Title",IN_PROGRESS,"Description",null,null,
                """);

        manager = FileBackedTaskManager.loadFromFile(path, historyManager);
        final Task savedTask = manager.getTask(1L);
        final List<Task> tasks = manager.getTasks();
        final List<Task> prioritized = manager.getPrioritizedTasks();

        assertAll("task loaded with errors",
                () -> assertTaskEquals(expectedTask, savedTask, "task loaded with errors"),
                () -> assertListEquals(expectedTasks, tasks, "task loaded with errors"),
                () -> assertTrue(prioritized.isEmpty(), "task loaded with errors")
        );
    }

    @Test
    public void shouldLoadTaskToGetAndTasksNotPrioritizedWhenFieldsNull() throws IOException {
        final Task expectedTask = fromEmptyTask().withId(TEST_TASK_ID).build();
        final List<Task> expectedTasks = List.of(expectedTask);
        fillTestFileWithData("""
                id,type,name,status,description,duration,start,epic
                1,TASK,null,null,null,null,null,
                """);

        manager = FileBackedTaskManager.loadFromFile(path, historyManager);
        final Task savedTask = manager.getTask(1L);
        final List<Task> tasks = manager.getTasks();
        final List<Task> prioritized = manager.getPrioritizedTasks();

        assertAll("task loaded with errors",
                () -> assertTaskEquals(expectedTask, savedTask, "task loaded with errors"),
                () -> assertListEquals(expectedTasks, tasks, "task loaded with errors"),
                () -> assertTrue(prioritized.isEmpty(), "task loaded with errors")
        );
    }

    @Test
    public void shouldLoadTaskToGetAndTasksAndPrioritizedWhenExactlyBeforeAnotherPrioritizedTask() throws IOException {
        final Subtask expectedSubtask = fromTestSubtask().withStartTime(TEST_START_TIME.plus(TEST_DURATION)).build();
        final Task expectedTask = fromTestTask().withId(ANOTHER_TEST_ID).build();
        final List<Task> expectedTasks = List.of(expectedTask);
        final List<Task> expectedPrioritized = List.of(expectedTask, expectedSubtask);
        fillTestFileWithData("""
                id,type,name,status,description,duration,start,epic
                2,EPIC,null,,null,,,
                3,SUBTASK,"Title",IN_PROGRESS,"Description",30,2000-05-01T14:00,2
                1000,TASK,"Title",IN_PROGRESS,"Description",30,2000-05-01T13:30,
                """);

        manager = FileBackedTaskManager.loadFromFile(path, historyManager);
        final Task savedTask = manager.getTask(1000L);
        final List<Task> tasks = manager.getTasks();
        final List<Task> prioritized = manager.getPrioritizedTasks();

        assertAll("task loaded with errors",
                () -> assertTaskEquals(expectedTask, savedTask, "task loaded with errors"),
                () -> assertListEquals(expectedTasks, tasks, "task loaded with errors"),
                () -> assertListEquals(expectedPrioritized, prioritized, "task loaded with errors")
        );
    }

    @Test
    public void shouldLoadTaskToGetAndTasksAndPrioritizedWhenWithStartTimeTruncatedExactlyBeforeAnotherTask()
            throws IOException {
        final Subtask expectedSubtask = fromTestSubtask().withStartTime(TEST_START_TIME.plus(TEST_DURATION)).build();
        final Task expectedTask = fromTestTask().withId(ANOTHER_TEST_ID).build();
        final List<Task> expectedTasks = List.of(expectedTask);
        final List<Task> expectedPrioritized = List.of(expectedTask, expectedSubtask);
        fillTestFileWithData("""
                id,type,name,status,description,duration,start,epic
                2,EPIC,null,,null,,,
                3,SUBTASK,"Title",IN_PROGRESS,"Description",30,2000-05-01T14:00,2
                1000,TASK,"Title",IN_PROGRESS,"Description",30,2000-05-01T13:30:25,
                """);

        manager = FileBackedTaskManager.loadFromFile(path, historyManager);
        final Task savedTask = manager.getTask(1000L);
        final List<Task> tasks = manager.getTasks();
        final List<Task> prioritized = manager.getPrioritizedTasks();

        assertAll("task loaded with errors",
                () -> assertTaskEquals(expectedTask, savedTask, "task loaded with errors"),
                () -> assertListEquals(expectedTasks, tasks, "task loaded with errors"),
                () -> assertListEquals(expectedPrioritized, prioritized, "task loaded with errors")
        );
    }

    @Test
    public void shouldLoadTaskToGetAndTasksAndPrioritizedWhenExactlyAfterAnotherPrioritizedTask() throws IOException {
        final Subtask expectedSubtask = fromTestSubtask().withStartTime(TEST_START_TIME.minus(TEST_DURATION)).build();
        final Task expectedTask = fromTestTask().withId(ANOTHER_TEST_ID).build();
        final List<Task> expectedTasks = List.of(expectedTask);
        final List<Task> expectedPrioritized = List.of(expectedSubtask, expectedTask);
        fillTestFileWithData("""
                id,type,name,status,description,duration,start,epic
                2,EPIC,null,,null,,,
                3,SUBTASK,"Title",IN_PROGRESS,"Description",30,2000-05-01T13:00,2
                1000,TASK,"Title",IN_PROGRESS,"Description",30,2000-05-01T13:30,
                """);

        manager = FileBackedTaskManager.loadFromFile(path, historyManager);
        final Task savedTask = manager.getTask(1000L);
        final List<Task> tasks = manager.getTasks();
        final List<Task> prioritized = manager.getPrioritizedTasks();

        assertAll("task loaded with errors",
                () -> assertTaskEquals(expectedTask, savedTask, "task loaded with errors"),
                () -> assertListEquals(expectedTasks, tasks, "task loaded with errors"),
                () -> assertListEquals(expectedPrioritized, prioritized, "task loaded with errors")
        );
    }

    @Test
    public void shouldNotLoadEpicWhenNotEnoughFields() throws IOException {
        fillTestFileWithData("""
                id,type,name,status,description,duration,start,epic
                2,EPIC,"Title",,"Description",,
                """);
        final String expectedMessage = "unexpected end of line for id=2";

        final Exception exception = assertThrows(ManagerLoadException.class,
                () -> FileBackedTaskManager.loadFromFile(path, historyManager));
        assertEquals(expectedMessage, exception.getMessage(), WRONG_EXCEPTION_MESSAGE);
    }

    @Test
    public void shouldNotLoadEpicWhenExcessiveFields() throws IOException {
        fillTestFileWithData("""
                id,type,name,status,description,duration,start,epic
                2,EPIC,"Epic",,"Epic description",,,,
                """);
        final String expectedMessage = "unexpected data for id=2";

        final Exception exception = assertThrows(ManagerLoadException.class,
                () -> FileBackedTaskManager.loadFromFile(path, historyManager));
        assertEquals(expectedMessage, exception.getMessage(), WRONG_EXCEPTION_MESSAGE);
    }

    @Test
    public void shouldNotLoadEpicWhenIdNotNumber() throws IOException {
        fillTestFileWithData("""
                id,type,name,status,description,duration,start,epic
                two,EPIC,"Title",,"Description",,,
                """);
        final String expectedMessage = "line does not start with numeric id";

        final Exception exception = assertThrows(ManagerLoadException.class,
                () -> FileBackedTaskManager.loadFromFile(path, historyManager));
        assertEquals(expectedMessage, exception.getMessage(), WRONG_EXCEPTION_MESSAGE);
    }

    @Test
    public void shouldNotLoadEpicWhenIdNotInteger() throws IOException {
        fillTestFileWithData("""
                id,type,name,status,description,duration,start,epic
                2.5,EPIC,"Title",,"Description",,,
                """);
        final String expectedMessage = "line does not start with numeric id";

        final Exception exception = assertThrows(ManagerLoadException.class,
                () -> FileBackedTaskManager.loadFromFile(path, historyManager));
        assertEquals(expectedMessage, exception.getMessage(), WRONG_EXCEPTION_MESSAGE);
    }

    @Test
    public void shouldNotLoadEpicWhenIdIsTaskId() throws IOException {
        fillTestFileWithData("""
                id,type,name,status,description,duration,start,epic
                1,TASK,"Task",NEW,"Task description",30,2000-05-01T13:30,
                1,EPIC,"Epic",,"Epic description",,,
                """);
        final String expectedMessage = "duplicate id=1";

        final Exception exception = assertThrows(ManagerLoadException.class,
                () -> FileBackedTaskManager.loadFromFile(path, historyManager));
        assertEquals(expectedMessage, exception.getMessage(), WRONG_EXCEPTION_MESSAGE);
    }

    @Test
    public void shouldNotLoadEpicWhenIdIsAnotherEpicId() throws IOException {
        fillTestFileWithData("""
                id,type,name,status,description,duration,start,epic
                1,EPIC,"Epic A",,"Epic A description",,,
                1,EPIC,"Epic B",,"Epic B description",,,
                """);
        final String expectedMessage = "duplicate id=1";

        final Exception exception = assertThrows(ManagerLoadException.class,
                () -> FileBackedTaskManager.loadFromFile(path, historyManager));
        assertEquals(expectedMessage, exception.getMessage(), WRONG_EXCEPTION_MESSAGE);
    }

    @Test
    public void shouldNotLoadEpicWhenIdIsSubtaskId() throws IOException {
        fillTestFileWithData("""
                id,type,name,status,description,duration,start,epic
                1,EPIC,"Epic A",,"Epic A description",,,
                2,SUBTASK,"Subtask",NEW,"Subtask description",30,2000-05-01T13:30,1
                2,EPIC,"Epic B",,"Epic B description",,,
                """);
        final String expectedMessage = "duplicate id=2";

        final Exception exception = assertThrows(ManagerLoadException.class,
                () -> FileBackedTaskManager.loadFromFile(path, historyManager));
        assertEquals(expectedMessage, exception.getMessage(), WRONG_EXCEPTION_MESSAGE);
    }

    @Test
    public void shouldNotLoadEpicWhenTitleDoesNotStartWithQuote() throws IOException {
        fillTestFileWithData("""
                id,type,name,status,description,duration,start,epic
                2,EPIC,Title",,"Description",,,
                """);
        final String expectedMessage = "no comma before opening double quote for id=2";

        final Exception exception = assertThrows(ManagerLoadException.class,
                () -> FileBackedTaskManager.loadFromFile(path, historyManager));
        assertEquals(expectedMessage, exception.getMessage(), WRONG_EXCEPTION_MESSAGE);
    }

    @Test
    public void shouldNotLoadEpicWhenTitleDoesNotEndWithQuote() throws IOException {
        fillTestFileWithData("""
                id,type,name,status,description,duration,start,epic
                2,EPIC,"Title,,"Description",,,
                """);
        final String expectedMessage = "no comma after closing double quote for id=2";

        final Exception exception = assertThrows(ManagerLoadException.class,
                () -> FileBackedTaskManager.loadFromFile(path, historyManager));
        assertEquals(expectedMessage, exception.getMessage(), WRONG_EXCEPTION_MESSAGE);
    }

    @Test
    public void shouldNotLoadEpicWhenTitleHasNoQuotes() throws IOException {
        fillTestFileWithData("""
                id,type,name,status,description,duration,start,epic
                2,EPIC,Title,,"Description",,,
                """);
        final String expectedMessage = "text value must be inside double quotes for id=2";

        final Exception exception = assertThrows(ManagerLoadException.class,
                () -> FileBackedTaskManager.loadFromFile(path, historyManager));
        assertEquals(expectedMessage, exception.getMessage(), WRONG_EXCEPTION_MESSAGE);
    }

    @Test
    public void shouldNotLoadEpicWhenHasStatus() throws IOException {
        fillTestFileWithData("""
                id,type,name,status,description,duration,start,epic
                2,EPIC,"Title",DONE,"Description",,,
                """);
        final String expectedMessage = "explicit epic status for id=2";

        final Exception exception = assertThrows(ManagerLoadException.class,
                () -> FileBackedTaskManager.loadFromFile(path, historyManager));
        assertEquals(expectedMessage, exception.getMessage(), WRONG_EXCEPTION_MESSAGE);
    }

    @Test
    public void shouldNotLoadEpicWhenDescriptionDoesNotStartWithQuote() throws IOException {
        fillTestFileWithData("""
                id,type,name,status,description,duration,start,epic
                2,EPIC,"Title",,Description",,,
                """);
        final String expectedMessage = "no comma before opening double quote for id=2";

        final Exception exception = assertThrows(ManagerLoadException.class,
                () -> FileBackedTaskManager.loadFromFile(path, historyManager));
        assertEquals(expectedMessage, exception.getMessage(), WRONG_EXCEPTION_MESSAGE);
    }

    @Test
    public void shouldNotLoadEpicWhenDescriptionDoesNotEndWithQuote() throws IOException {
        fillTestFileWithData("""
                id,type,name,status,description,duration,start,epic
                2,EPIC,"Title",,"Description,,,
                """);
        final String expectedMessage = "no closing double quote for id=2";

        final Exception exception = assertThrows(ManagerLoadException.class,
                () -> FileBackedTaskManager.loadFromFile(path, historyManager));
        assertEquals(expectedMessage, exception.getMessage(), WRONG_EXCEPTION_MESSAGE);
    }

    @Test
    public void shouldNotLoadEpicWhenDescriptionHasNoQuotes() throws IOException {
        fillTestFileWithData("""
                id,type,name,status,description,duration,start,epic
                2,EPIC,"Title",,Description,,,
                """);
        final String expectedMessage = "text value must be inside double quotes for id=2";

        final Exception exception = assertThrows(ManagerLoadException.class,
                () -> FileBackedTaskManager.loadFromFile(path, historyManager));
        assertEquals(expectedMessage, exception.getMessage(), WRONG_EXCEPTION_MESSAGE);
    }

    @Test
    public void shouldNotLodEpicWhenHasDuration() throws IOException {
        fillTestFileWithData("""
                id,type,name,status,description,duration,start,epic
                2,EPIC,"Title",,"Description",30,,
                """);
        final String expectedMessage = "explicit epic duration for id=2";

        final Exception exception = assertThrows(ManagerLoadException.class,
                () -> FileBackedTaskManager.loadFromFile(path, historyManager));
        assertEquals(expectedMessage, exception.getMessage(), WRONG_EXCEPTION_MESSAGE);
    }

    @Test
    public void shouldNotLoadEpicWhenHasStartTime() throws IOException {
        fillTestFileWithData("""
                id,type,name,status,description,duration,start,epic
                2,EPIC,"Title",,"Description",,2000-05-01T13:30,
                """);
        String expectedMessage = "explicit epic start time for id=2";

        Exception exception = assertThrows(ManagerLoadException.class,
                () -> FileBackedTaskManager.loadFromFile(path, historyManager));
        assertEquals(expectedMessage, exception.getMessage(), WRONG_EXCEPTION_MESSAGE);
    }

    @Test
    public void shouldNotLoadEpicWhenHasEpicId() throws IOException {
        fillTestFileWithData("""
                id,type,name,status,description,duration,start,epic
                1,EPIC,"Epic A",,"Epic A description",,,
                2,EPIC,"Epic B",,"Epic B description",,,1
                """);
        final String expectedMessage = "unexpected data for id=2";

        final Exception exception = assertThrows(ManagerLoadException.class,
                () -> FileBackedTaskManager.loadFromFile(path, historyManager));
        assertEquals(expectedMessage, exception.getMessage(), WRONG_EXCEPTION_MESSAGE);
    }

    @Test
    public void shouldLoadEpicToGetAndEpics() throws IOException {
        final Epic expectedEpic = fromTestEpic().build();
        final List<Epic> expectedEpics = List.of(expectedEpic);
        fillTestFileWithData("""
                id,type,name,status,description,duration,start,epic
                2,EPIC,"Title",,"Description",,,
                """);

        manager = FileBackedTaskManager.loadFromFile(path, historyManager);
        final Epic savedEpic = manager.getEpic(2L);
        final List<Epic> epics = manager.getEpics();

        assertAll("task loaded with errors",
                () -> assertTaskEquals(expectedEpic, savedEpic, "task loaded with errors"),
                () -> assertListEquals(expectedEpics, epics, "task loaded with errors")
        );
    }

    @Test
    public void shouldLoadEpicToGetAndEpicsWhenFieldsNull() throws IOException {
        final Epic expectedEpic = fromEmptyEpic().withId(TEST_EPIC_ID).build();
        final List<Epic> expectedEpics = List.of(expectedEpic);
        fillTestFileWithData("""
                id,type,name,status,description,duration,start,epic
                2,EPIC,null,,null,,,
                """);

        manager = FileBackedTaskManager.loadFromFile(path, historyManager);
        final Epic savedEpic = manager.getEpic(2L);
        final List<Epic> epics = manager.getEpics();

        assertAll("task loaded with errors",
                () -> assertTaskEquals(expectedEpic, savedEpic, "task loaded with errors"),
                () -> assertListEquals(expectedEpics, epics, "task loaded with errors")
        );
    }

    @Test
    public void shouldNotLoadSubtaskWhenNotEnoughFields() throws IOException {
        fillTestFileWithData("""
                id,type,name,status,description,duration,start,epic
                2,EPIC,"Epic",,"Epic description",,,
                3,SUBTASK,"Subtask",NEW,"Subtask description",30,2000-05-01T13:30
                """);
        final String expectedMessage = "unexpected end of line for id=3";

        final Exception exception = assertThrows(ManagerLoadException.class,
                () -> FileBackedTaskManager.loadFromFile(path, historyManager));
        assertEquals(expectedMessage, exception.getMessage(), WRONG_EXCEPTION_MESSAGE);
    }

    @Test
    public void shouldNotLoadSubtaskWhenExcessiveFields() throws IOException {
        fillTestFileWithData("""
                id,type,name,status,description,duration,start,epic
                2,EPIC,"Epic",,"Epic description",,,
                3,SUBTASK,"Subtask",NEW,"Subtask description",30,2000-05-01T13:30,2,
                """);
        final String expectedMessage = "unexpected data for id=3";

        final Exception exception = assertThrows(ManagerLoadException.class,
                () -> FileBackedTaskManager.loadFromFile(path, historyManager));
        assertEquals(expectedMessage, exception.getMessage(), WRONG_EXCEPTION_MESSAGE);
    }

    @Test
    public void shouldNotLoadSubtaskWhenIdNotNumber() throws IOException {
        fillTestFileWithData("""
                id,type,name,status,description,duration,start,epic
                2,EPIC,null,,null,,,
                three,SUBTASK,"Title",IN_PROGRESS,"Description",30,2000-05-01T13:30,2
                """);
        final String expectedMessage = "line does not start with numeric id";

        final Exception exception = assertThrows(ManagerLoadException.class,
                () -> FileBackedTaskManager.loadFromFile(path, historyManager));
        assertEquals(expectedMessage, exception.getMessage(), WRONG_EXCEPTION_MESSAGE);
    }

    @Test
    public void shouldNotLoadSubtaskWhenIdNotInteger() throws IOException {
        fillTestFileWithData("""
                id,type,name,status,description,duration,start,epic
                2,EPIC,null,,null,,,
                3.5,SUBTASK,"Title",IN_PROGRESS,"Description",30,2000-05-01T13:30,2
                """);
        final String expectedMessage = "line does not start with numeric id";

        final Exception exception = assertThrows(ManagerLoadException.class,
                () -> FileBackedTaskManager.loadFromFile(path, historyManager));
        assertEquals(expectedMessage, exception.getMessage(), WRONG_EXCEPTION_MESSAGE);
    }

    @Test
    public void shouldNotLoadSubtaskWhenIdIsTaskId() throws IOException {
        fillTestFileWithData("""
                id,type,name,status,description,duration,start,epic
                1,TASK,"Task",NEW,"Task description",90,2000-05-01T15:00,
                2,EPIC,"Epic",,"Subtask description",,,
                1,SUBTASK,"Subtask",NEW,"Subtask description",30,2000-05-01T13:30,2
                """);
        final String expectedMessage = "duplicate id=1";

        final Exception exception = assertThrows(ManagerLoadException.class,
                () -> FileBackedTaskManager.loadFromFile(path, historyManager));
        assertEquals(expectedMessage, exception.getMessage(), WRONG_EXCEPTION_MESSAGE);
    }

    @Test
    public void shouldNotLoadSubtaskWhenIdIsEpicId() throws IOException {
        fillTestFileWithData("""
                id,type,name,status,description,duration,start,epic
                1,EPIC,"Epic A",,"Epic A description",,,
                2,EPIC,"Epic B",,"Epic B description",,,
                1,SUBTASK,"Subtask",NEW,"Subtask description",30,2000-05-01T13:30,2
                """);
        final String expectedMessage = "duplicate id=1";

        final Exception exception = assertThrows(ManagerLoadException.class,
                () -> FileBackedTaskManager.loadFromFile(path, historyManager));
        assertEquals(expectedMessage, exception.getMessage(), WRONG_EXCEPTION_MESSAGE);
    }

    @Test
    public void shouldNotLoadSubtaskWhenIdIsAnotherSubtaskId() throws IOException {
        fillTestFileWithData("""
                id,type,name,status,description,duration,start,epic
                1,EPIC,"Epic",,"Epic description",,,
                2,SUBTASK,"Subtask A",NEW,"Subtask A description",90,2000-05-01T15:00,1
                2,SUBTASK,"Subtask B",NEW,"Subtask B description",30,2000-05-01T13:30,1
                """);
        final String expectedMessage = "duplicate id=2";

        final Exception exception = assertThrows(ManagerLoadException.class,
                () -> FileBackedTaskManager.loadFromFile(path, historyManager));
        assertEquals(expectedMessage, exception.getMessage(), WRONG_EXCEPTION_MESSAGE);
    }

    @Test
    public void shouldNotLoadSubtaskWhenTitleDoesNotStartWithQuote() throws IOException {
        fillTestFileWithData("""
                id,type,name,status,description,duration,start,epic
                2,EPIC,null,,null,,,
                3,SUBTASK,Title",IN_PROGRESS,"Description",30,2000-05-01T13:30,2
                """);
        final String expectedMessage = "no comma before opening double quote for id=3";

        final Exception exception = assertThrows(ManagerLoadException.class,
                () -> FileBackedTaskManager.loadFromFile(path, historyManager));
        assertEquals(expectedMessage, exception.getMessage(), WRONG_EXCEPTION_MESSAGE);
    }

    @Test
    public void shouldNotLoadSubtaskWhenTitleDoesNotEndWithQuote() throws IOException {
        fillTestFileWithData("""
                id,type,name,status,description,duration,start,epic
                2,EPIC,null,,null,,,
                3,SUBTASK,"Title,IN_PROGRESS,"Description",30,2000-05-01T13:30,2
                """);
        final String expectedMessage = "no comma after closing double quote for id=3";

        final Exception exception = assertThrows(ManagerLoadException.class,
                () -> FileBackedTaskManager.loadFromFile(path, historyManager));
        assertEquals(expectedMessage, exception.getMessage(), WRONG_EXCEPTION_MESSAGE);
    }

    @Test
    public void shouldNotLoadSubtaskWhenTitleHasNoQuotes() throws IOException {
        fillTestFileWithData("""
                id,type,name,status,description,duration,start,epic
                2,EPIC,null,,null,,,
                3,SUBTASK,Title,IN_PROGRESS,"Description",30,2000-05-01T13:30,2
                """);
        final String expectedMessage = "text value must be inside double quotes for id=3";

        final Exception exception = assertThrows(ManagerLoadException.class,
                () -> FileBackedTaskManager.loadFromFile(path, historyManager));
        assertEquals(expectedMessage, exception.getMessage(), WRONG_EXCEPTION_MESSAGE);
    }

    @Test
    public void shouldNotLoadSubtaskWhenHasUnknownStatus() throws IOException {
        fillTestFileWithData("""
                id,type,name,status,description,duration,start,epic
                2,EPIC,null,,null,,,
                3,SUBTASK,"Title",text,"Description",30,2000-05-01T13:30,2
                """);
        final String expectedMessage = "unknown task status for id=3";

        final Exception exception = assertThrows(ManagerLoadException.class,
                () -> FileBackedTaskManager.loadFromFile(path, historyManager));
        assertEquals(expectedMessage, exception.getMessage(), WRONG_EXCEPTION_MESSAGE);
    }

    @Test
    public void shouldNotLoadSubtaskWhenDescriptionDoesNotStartWithQuote() throws IOException {
        fillTestFileWithData("""
                id,type,name,status,description,duration,start,epic
                2,EPIC,null,,null,,,
                3,SUBTASK,"Title",IN_PROGRESS,Description",30,2000-05-01T13:30,2
                """);
        final String expectedMessage = "no comma before opening double quote for id=3";

        final Exception exception = assertThrows(ManagerLoadException.class,
                () -> FileBackedTaskManager.loadFromFile(path, historyManager));
        assertEquals(expectedMessage, exception.getMessage(), WRONG_EXCEPTION_MESSAGE);
    }

    @Test
    public void shouldNotLoadSubtaskWhenDescriptionDoesNotEndWithQuote() throws IOException {
        fillTestFileWithData("""
                id,type,name,status,description,duration,start,epic
                2,EPIC,null,,null,,,
                3,SUBTASK,"Title",IN_PROGRESS,"Description,30,2000-05-01T13:30,2
                """);
        final String expectedMessage = "no closing double quote for id=3";

        final Exception exception = assertThrows(ManagerLoadException.class,
                () -> FileBackedTaskManager.loadFromFile(path, historyManager));
        assertEquals(expectedMessage, exception.getMessage(), WRONG_EXCEPTION_MESSAGE);
    }

    @Test
    public void shouldNotLoadSubtaskWhenDescriptionHasNoQuotes() throws IOException {
        fillTestFileWithData("""
                id,type,name,status,description,duration,start,epic
                2,EPIC,null,,null,,,
                3,TASK,"Title",IN_PROGRESS,Description,30,2000-05-01T13:30,2
                """);
        final String expectedMessage = "text value must be inside double quotes for id=3";

        final Exception exception = assertThrows(ManagerLoadException.class,
                () -> FileBackedTaskManager.loadFromFile(path, historyManager));
        assertEquals(expectedMessage, exception.getMessage(), WRONG_EXCEPTION_MESSAGE);
    }

    @Test
    public void shouldNotLoadSubtaskWhenDurationNotNumber() throws IOException {
        fillTestFileWithData("""
                id,type,name,status,description,duration,start,epic
                2,EPIC,null,,null,,,
                3,SUBTASK,"Title",IN_PROGRESS,"Description",thirty,2000-05-01T13:30,2
                """);
        final String expectedMessage = "wrong duration format for id=3";

        final Exception exception = assertThrows(ManagerLoadException.class,
                () -> FileBackedTaskManager.loadFromFile(path, historyManager));
        assertEquals(expectedMessage, exception.getMessage(), WRONG_EXCEPTION_MESSAGE);
    }

    @Test
    public void shouldNotLoadSubtaskWhenDurationNotInteger() throws IOException {
        fillTestFileWithData("""
                id,type,name,status,description,duration,start,epic
                2,EPIC,null,,null,,,
                3,SUBTASK,"Title",IN_PROGRESS,"Description",30.5,2000-05-01T13:30,2
                """);
        final String expectedMessage = "wrong duration format for id=3";

        final Exception exception = assertThrows(ManagerLoadException.class,
                () -> FileBackedTaskManager.loadFromFile(path, historyManager));
        assertEquals(expectedMessage, exception.getMessage(), WRONG_EXCEPTION_MESSAGE);
    }

    @Test
    public void shouldNotLoadSubtaskWhenDurationZero() throws IOException {
        fillTestFileWithData("""
                id,type,name,status,description,duration,start,epic
                2,EPIC,null,,null,,,
                3,SUBTASK,"Title",IN_PROGRESS,"Description",0,2000-05-01T13:30,2
                """);
        final String expectedMessage = "duration cannot be negative or zero for id=3";

        final Exception exception = assertThrows(ManagerLoadException.class,
                () -> FileBackedTaskManager.loadFromFile(path, historyManager));
        assertEquals(expectedMessage, exception.getMessage(), WRONG_EXCEPTION_MESSAGE);
    }

    @Test
    public void shouldNotLoadSubtaskWhenDurationNegative() throws IOException {
        fillTestFileWithData("""
                id,type,name,status,description,duration,start,epic
                2,EPIC,null,,null,,,
                3,SUBTASK,"Title",IN_PROGRESS,"Description",-30,2000-05-01T13:30,2
                """);
        final String expectedMessage = "duration cannot be negative or zero for id=3";

        final Exception exception = assertThrows(ManagerLoadException.class,
                () -> FileBackedTaskManager.loadFromFile(path, historyManager));
        assertEquals(expectedMessage, exception.getMessage(), WRONG_EXCEPTION_MESSAGE);
    }

    @Test
    public void shouldNotLoadSubtaskWhenStartTimeNotDateTime() throws IOException {
        fillTestFileWithData("""
                id,type,name,status,description,duration,start,epic
                2,EPIC,null,,null,,,
                3,SUBTASK,"Title",IN_PROGRESS,"Description",30,text,2
                """);
        final String expectedMessage = "wrong start time format for id=3";

        final Exception exception = assertThrows(ManagerLoadException.class,
                () -> FileBackedTaskManager.loadFromFile(path, historyManager));
        assertEquals(expectedMessage, exception.getMessage(), WRONG_EXCEPTION_MESSAGE);
    }

    @Test
    public void shouldNotLoadSubtaskWhenDurationNullAndStartTimeNotNull() throws IOException {
        fillTestFileWithData("""
                id,type,name,status,description,duration,start,epic
                2,EPIC,null,,null,,,
                3,SUBTASK,"Title",IN_PROGRESS,"Description",null,2000-05-01T13:30,2
                """);
        final String expectedMessage = "duration and start time must be either both set or both null for id=3";

        final Exception exception = assertThrows(ManagerLoadException.class,
                () -> FileBackedTaskManager.loadFromFile(path, historyManager));
        assertEquals(expectedMessage, exception.getMessage(), WRONG_EXCEPTION_MESSAGE);
    }

    @Test
    public void shouldNotLoadSubtaskWhenDurationNotNullAndStartTimeNull() throws IOException {
        fillTestFileWithData("""
                id,type,name,status,description,duration,start,epic
                2,EPIC,null,,null,,,
                3,SUBTASK,"Title",IN_PROGRESS,"Description",30,null,2
                """);
        final String expectedMessage = "duration and start time must be either both set or both null for id=3";

        final Exception exception = assertThrows(ManagerLoadException.class,
                () -> FileBackedTaskManager.loadFromFile(path, historyManager));
        assertEquals(expectedMessage, exception.getMessage(), WRONG_EXCEPTION_MESSAGE);
    }

    @Test
    public void shouldNotLoadSubtaskWhenAnotherPrioritizedTaskCoversStartTime() throws IOException {
        fillTestFileWithData("""
                id,type,name,status,description,duration,start,epic
                1,TASK,"Title",IN_PROGRESS,"Description",30,2000-05-01T13:15,
                2,EPIC,null,,null,,,
                3,SUBTASK,"Title",IN_PROGRESS,"Description",30,2000-05-01T13:30,2
                """);
        final String expectedMessage = "conflict with another task for time slot for id=3";

        final Exception exception = assertThrows(ManagerLoadException.class,
                () -> FileBackedTaskManager.loadFromFile(path, historyManager));
        assertEquals(expectedMessage, exception.getMessage(), WRONG_EXCEPTION_MESSAGE);
    }

    @Test
    public void shouldNotLoadSubtaskWhenAnotherPrioritizedTaskCoversEndTime() throws IOException {
        fillTestFileWithData("""
                id,type,name,status,description,duration,start,epic
                1,TASK,"Title",IN_PROGRESS,"Description",30,2000-05-01T13:45,
                2,EPIC,null,,null,,,
                3,SUBTASK,"Title",IN_PROGRESS,"Description",30,2000-05-01T13:30,2
                """);
        final String expectedMessage = "conflict with another task for time slot for id=3";

        final Exception exception = assertThrows(ManagerLoadException.class,
                () -> FileBackedTaskManager.loadFromFile(path, historyManager));
        assertEquals(expectedMessage, exception.getMessage(), WRONG_EXCEPTION_MESSAGE);
    }

    @Test
    public void shouldNotLoadSubtaskWhenAnotherPrioritizedTaskCoversWholeInterval() throws IOException {
        fillTestFileWithData("""
                id,type,name,status,description,duration,start,epic
                1,TASK,"Title",IN_PROGRESS,"Description",60,2000-05-01T13:15,
                2,EPIC,null,,null,,,
                3,SUBTASK,"Title",IN_PROGRESS,"Description",30,2000-05-01T13:30,2
                """);
        final String expectedMessage = "conflict with another task for time slot for id=3";

        final Exception exception = assertThrows(ManagerLoadException.class,
                () -> FileBackedTaskManager.loadFromFile(path, historyManager));
        assertEquals(expectedMessage, exception.getMessage(), WRONG_EXCEPTION_MESSAGE);
    }

    @Test
    public void shouldNotLoadSubtaskWhenAnotherPrioritizedTaskWithinInterval() throws IOException {
        fillTestFileWithData("""
                id,type,name,status,description,duration,start,epic
                1,TASK,"Title",IN_PROGRESS,"Description",20,2000-05-01T13:35,
                2,EPIC,null,,null,,,
                3,SUBTASK,"Title",IN_PROGRESS,"Description",30,2000-05-01T13:30,2
                """);
        final String expectedMessage = "conflict with another task for time slot for id=3";

        final Exception exception = assertThrows(ManagerLoadException.class,
                () -> FileBackedTaskManager.loadFromFile(path, historyManager));
        assertEquals(expectedMessage, exception.getMessage(), WRONG_EXCEPTION_MESSAGE);
    }

    @Test
    public void shouldNotLoadSubtaskWhenAnotherPrioritizedTaskWithinIntervalLeftAligned() throws IOException {
        fillTestFileWithData("""
                id,type,name,status,description,duration,start,epic
                1,TASK,"Title",IN_PROGRESS,"Description",20,2000-05-01T13:30,
                2,EPIC,null,,null,,,
                3,SUBTASK,"Title",IN_PROGRESS,"Description",30,2000-05-01T13:30,2
                """);
        final String expectedMessage = "conflict with another task for time slot for id=3";

        final Exception exception = assertThrows(ManagerLoadException.class,
                () -> FileBackedTaskManager.loadFromFile(path, historyManager));
        assertEquals(expectedMessage, exception.getMessage(), WRONG_EXCEPTION_MESSAGE);
    }

    @Test
    public void shouldNotLoadSubtaskWhenAnotherPrioritizedTaskWithinIntervalRightAligned() throws IOException {
        fillTestFileWithData("""
                id,type,name,status,description,duration,start,epic
                1,TASK,"Title",IN_PROGRESS,"Description",20,2000-05-01T13:40,
                2,EPIC,null,,null,,,
                3,SUBTASK,"Title",IN_PROGRESS,"Description",30,2000-05-01T13:30,2
                """);
        final String expectedMessage = "conflict with another task for time slot for id=3";

        final Exception exception = assertThrows(ManagerLoadException.class,
                () -> FileBackedTaskManager.loadFromFile(path, historyManager));
        assertEquals(expectedMessage, exception.getMessage(), WRONG_EXCEPTION_MESSAGE);
    }

    @Test
    public void shouldNotLoadSubtaskWhenAnotherPrioritizedTaskWithSameInterval() throws IOException {
        fillTestFileWithData("""
                id,type,name,status,description,duration,start,epic
                1,TASK,"Title",IN_PROGRESS,"Description",30,2000-05-01T13:30,
                2,EPIC,null,,null,,,
                3,SUBTASK,"Title",IN_PROGRESS,"Description",30,2000-05-01T13:30,2
                """);
        final String expectedMessage = "conflict with another task for time slot for id=3";

        final Exception exception = assertThrows(ManagerLoadException.class,
                () -> FileBackedTaskManager.loadFromFile(path, historyManager));
        assertEquals(expectedMessage, exception.getMessage(), WRONG_EXCEPTION_MESSAGE);
    }

    @Test
    public void shouldNotLoadSubtaskWhenHasNoEpicId() throws IOException {
        fillTestFileWithData("""
                id,type,name,status,description,duration,start,epic
                1,EPIC,"Epic",,"Epic description",,,
                2,SUBTASK,"Subtask",NEW,"Subtask description",30,2000-05-01T13:30,
                """);
        final String expectedMessage = "wrong epic id format for id=2";

        final Exception exception = assertThrows(ManagerLoadException.class,
                () -> FileBackedTaskManager.loadFromFile(path, historyManager));
        assertEquals(expectedMessage, exception.getMessage(), WRONG_EXCEPTION_MESSAGE);
    }

    @Test
    public void shouldNotLoadSubtaskWhenEpicIdNotNumber() throws IOException {
        fillTestFileWithData("""
                id,type,name,status,description,duration,start,epic
                1,EPIC,"Epic",,"Epic description",,,
                2,SUBTASK,"Subtask",NEW,"Subtask description",30,2000-05-01T13:30,one
                """);
        final String expectedMessage = "wrong epic id format for id=2";

        final Exception exception = assertThrows(ManagerLoadException.class,
                () -> FileBackedTaskManager.loadFromFile(path, historyManager));
        assertEquals(expectedMessage, exception.getMessage(), WRONG_EXCEPTION_MESSAGE);
    }

    @Test
    public void shouldNotLoadSubtaskWhenEpicIdNotInteger() throws IOException {
        fillTestFileWithData("""
                id,type,name,status,description,duration,start,epic
                1,EPIC,"Epic",,"Epic description",,,
                2,SUBTASK,"Subtask",NEW,"Subtask description",30,2000-05-01T13:30,1.5
                """);
        final String expectedMessage = "wrong epic id format for id=2";

        final Exception exception = assertThrows(ManagerLoadException.class,
                () -> FileBackedTaskManager.loadFromFile(path, historyManager));
        assertEquals(expectedMessage, exception.getMessage(), WRONG_EXCEPTION_MESSAGE);
    }

    @Test
    public void shouldNotLoadSubtaskWhenUnknownEpicId() throws IOException {
        fillTestFileWithData("""
                id,type,name,status,description,duration,start,epic
                1,EPIC,"Epic",,"Epic description",,,
                2,SUBTASK,"Subtask",NEW,"Subtask description",30,2000-05-01T13:30,3
                """);
        final String expectedMessage = "no epic with id=3";

        final Exception exception = assertThrows(ManagerLoadException.class,
                () -> FileBackedTaskManager.loadFromFile(path, historyManager));
        assertEquals(expectedMessage, exception.getMessage(), WRONG_EXCEPTION_MESSAGE);
    }

    @Test
    public void shouldNotLoadSubtaskWhenEpicIdIsTaskId() throws IOException {
        fillTestFileWithData("""
                id,type,name,status,description,duration,start,epic
                1,TASK,"Task",NEW,"Task description",90,2000-05-01T15:00,
                2,EPIC,"Epic",,"Epic description",,,
                3,SUBTASK,"Subtask",NEW,"Subtask description",30,2000-05-01T13:30,1
                """);
        final String expectedMessage = "no epic with id=1";

        final Exception exception = assertThrows(ManagerLoadException.class,
                () -> FileBackedTaskManager.loadFromFile(path, historyManager));
        assertEquals(expectedMessage, exception.getMessage(), WRONG_EXCEPTION_MESSAGE);
    }

    @Test
    public void shouldNotLoadSubtaskWhenEpicIdIsSubtaskId() throws IOException {
        fillTestFileWithData("""
                id,type,name,status,description,duration,start,epic
                1,EPIC,"Epic",,"Epic description",,,
                2,SUBTASK,"Subtask A",NEW,"Subtask A description",90,2000-05-01T15:00,1
                3,SUBTASK,"Subtask B",NEW,"Subtask B description",30,2000-05-01T13:30,2
                """);
        String expectedMessage = "no epic with id=2";

        Exception exception = assertThrows(ManagerLoadException.class,
                () -> FileBackedTaskManager.loadFromFile(path, historyManager));
        assertEquals(expectedMessage, exception.getMessage(), WRONG_EXCEPTION_MESSAGE);
    }

    @Test
    public void shouldLoadSubtaskToGetAndEpicAndSubtasksAndPrioritizedWhenStartTimeNotNull() throws IOException {
        final Subtask expectedSubtask = fromTestSubtask().build();
        final List<Subtask> expectedSubtasks = List.of(expectedSubtask);
        fillTestFileWithData("""
                id,type,name,status,description,duration,start,epic
                2,EPIC,"Title",,"Description",,,
                3,SUBTASK,"Title",IN_PROGRESS,"Description",30,2000-05-01T13:30,2
                """);

        manager = FileBackedTaskManager.loadFromFile(path, historyManager);
        final Subtask savedSubtask = manager.getSubtask(3L);
        final List<Subtask> epicSubtasks = manager.getEpicSubtasks(2L);
        final List<Subtask> subtasks = manager.getSubtasks();
        final List<Task> prioritized = manager.getPrioritizedTasks();

        assertAll("subtask loaded with errors",
                () -> assertTaskEquals(expectedSubtask, savedSubtask, "subtask loaded with errors"),
                () -> assertListEquals(expectedSubtasks, epicSubtasks, "subtask loaded with errors"),
                () -> assertListEquals(expectedSubtasks, subtasks, "subtask loaded with errors"),
                () -> assertListEquals(expectedSubtasks, prioritized, "subtask loaded with errors")
        );
    }

    @Test
    public void shouldLoadSubtaskToGetAndEpicAndSubtasksAndPrioritizedWithStartTimeTruncated() throws IOException {
        final Subtask expectedSubtask = fromTestSubtask().build();
        final List<Subtask> expectedSubtasks = List.of(expectedSubtask);
        fillTestFileWithData("""
                id,type,name,status,description,duration,start,epic
                2,EPIC,"Title",,"Description",,,
                3,SUBTASK,"Title",IN_PROGRESS,"Description",30,2000-05-01T13:30:25,2
                """);

        manager = FileBackedTaskManager.loadFromFile(path, historyManager);
        final Subtask savedSubtask = manager.getSubtask(3L);
        final List<Subtask> epicSubtasks = manager.getEpicSubtasks(2L);
        final List<Subtask> subtasks = manager.getSubtasks();
        final List<Task> prioritized = manager.getPrioritizedTasks();

        assertAll("subtask loaded with errors",
                () -> assertTaskEquals(expectedSubtask, savedSubtask, "subtask loaded with errors"),
                () -> assertListEquals(expectedSubtasks, epicSubtasks, "subtask loaded with errors"),
                () -> assertListEquals(expectedSubtasks, subtasks, "subtask loaded with errors"),
                () -> assertListEquals(expectedSubtasks, prioritized, "subtask loaded with errors")
        );
    }

    @Test
    public void shouldLoadSubtaskToGetAndEpicAndSubtasksNotPrioritizedWhenStartTimeNull() throws IOException {
        final Subtask expectedSubtask = fromTestSubtask().withDuration(null).withStartTime(null).build();
        final List<Subtask> expectedSubtasks = List.of(expectedSubtask);
        fillTestFileWithData("""
                id,type,name,status,description,duration,start,epic
                2,EPIC,"Title",,"Description",,,
                3,SUBTASK,"Title",IN_PROGRESS,"Description",null,null,2
                """);

        manager = FileBackedTaskManager.loadFromFile(path, historyManager);
        final Subtask savedSubtask = manager.getSubtask(3L);
        final List<Subtask> epicSubtasks = manager.getEpicSubtasks(2L);
        final List<Subtask> subtasks = manager.getSubtasks();
        final List<Task> prioritized = manager.getPrioritizedTasks();

        assertAll("subtask loaded with errors",
                () -> assertTaskEquals(expectedSubtask, savedSubtask, "subtask loaded with errors"),
                () -> assertListEquals(expectedSubtasks, epicSubtasks, "subtask loaded with errors"),
                () -> assertListEquals(expectedSubtasks, subtasks, "subtask loaded with errors"),
                () -> assertTrue(prioritized.isEmpty(), "subtask loaded with errors")
        );
    }

    @Test
    public void shouldLoadSubtaskToGetAndEpicAndSubtasksNotPrioritizedWhenFieldsNull() throws IOException {
        final Subtask expectedSubtask = fromEmptySubtask().withId(TEST_SUBTASK_ID).withEpicId(TEST_EPIC_ID).build();
        final List<Subtask> expectedSubtasks = List.of(expectedSubtask);
        fillTestFileWithData("""
                id,type,name,status,description,duration,start,epic
                2,EPIC,"Title",,"Description",,,
                3,SUBTASK,null,null,null,null,null,2
                """);

        manager = FileBackedTaskManager.loadFromFile(path, historyManager);
        final Subtask savedSubtask = manager.getSubtask(3L);
        final List<Subtask> epicSubtasks = manager.getEpicSubtasks(2L);
        final List<Subtask> subtasks = manager.getSubtasks();
        final List<Task> prioritized = manager.getPrioritizedTasks();

        assertAll("subtask loaded with errors",
                () -> assertTaskEquals(expectedSubtask, savedSubtask, "subtask loaded with errors"),
                () -> assertListEquals(expectedSubtasks, epicSubtasks, "subtask loaded with errors"),
                () -> assertListEquals(expectedSubtasks, subtasks, "subtask loaded with errors"),
                () -> assertTrue(prioritized.isEmpty(), "subtask loaded with errors")
        );
    }

    @Test
    public void shouldLoadSubtaskToGetAndEpicAndSubtasksAndPrioritizedWhenExactlyBeforePrioritize() throws IOException {
        final Task expectedTask = fromTestTask().withStartTime(TEST_START_TIME.plus(TEST_DURATION)).build();
        final Subtask expectedSubtask = fromTestSubtask().build();
        final List<Subtask> expectedSubtasks = List.of(expectedSubtask);
        final List<Task> expectedPrioritized = List.of(expectedSubtask, expectedTask);
        fillTestFileWithData("""
                id,type,name,status,description,duration,start,epic
                1,TASK,"Title",IN_PROGRESS,"Description",30,2000-05-01T14:00,
                2,EPIC,null,,null,,,
                3,SUBTASK,"Title",IN_PROGRESS,"Description",30,2000-05-01T13:30,2
                """);

        manager = FileBackedTaskManager.loadFromFile(path, historyManager);
        final Subtask savedSubtask = manager.getSubtask(3L);
        final List<Subtask> epicSubtasks = manager.getEpicSubtasks(2L);
        final List<Subtask> subtasks = manager.getSubtasks();
        final List<Task> prioritized = manager.getPrioritizedTasks();

        assertAll("subtask loaded with errors",
                () -> assertTaskEquals(expectedSubtask, savedSubtask, "subtask loaded with errors"),
                () -> assertListEquals(expectedSubtasks, epicSubtasks, "subtask loaded with errors"),
                () -> assertListEquals(expectedSubtasks, subtasks, "subtask loaded with errors"),
                () -> assertListEquals(expectedPrioritized, prioritized, "subtask loaded with errors")
        );
    }

    @Test
    public void shouldLoadSubtaskToGetAndEpicAndSubtasksAndPrioritizedWhenWithStartTimeTruncatedExactlyBeforeTask()
            throws IOException {
        final Task expectedTask = fromTestTask().withStartTime(TEST_START_TIME.plus(TEST_DURATION)).build();
        final Subtask expectedSubtask = fromTestSubtask().build();
        final List<Subtask> expectedSubtasks = List.of(expectedSubtask);
        final List<Task> expectedPrioritized = List.of(expectedSubtask, expectedTask);
        fillTestFileWithData("""
                id,type,name,status,description,duration,start,epic
                1,TASK,"Title",IN_PROGRESS,"Description",30,2000-05-01T14:00,
                2,EPIC,null,,null,,,
                3,SUBTASK,"Title",IN_PROGRESS,"Description",30,2000-05-01T13:30:25,2
                """);

        manager = FileBackedTaskManager.loadFromFile(path, historyManager);
        final Subtask savedSubtask = manager.getSubtask(3L);
        final List<Subtask> epicSubtasks = manager.getEpicSubtasks(2L);
        final List<Subtask> subtasks = manager.getSubtasks();
        final List<Task> prioritized = manager.getPrioritizedTasks();

        assertAll("subtask loaded with errors",
                () -> assertTaskEquals(expectedSubtask, savedSubtask, "subtask loaded with errors"),
                () -> assertListEquals(expectedSubtasks, epicSubtasks, "subtask loaded with errors"),
                () -> assertListEquals(expectedSubtasks, subtasks, "subtask loaded with errors"),
                () -> assertListEquals(expectedPrioritized, prioritized, "subtask loaded with errors")
        );
    }

    @Test
    public void shouldLoadSubtaskToGetAndEpicAndSubtasksAndPrioritizedWhenExactlyAfterPrioritized() throws IOException {
        final Task expectedTask = fromTestTask().withStartTime(TEST_START_TIME.minus(TEST_DURATION)).build();
        final Subtask expectedSubtask = fromTestSubtask().build();
        final List<Subtask> expectedSubtasks = List.of(expectedSubtask);
        final List<Task> expectedPrioritized = List.of(expectedTask, expectedSubtask);
        fillTestFileWithData("""
                id,type,name,status,description,duration,start,epic
                1,TASK,"Title",IN_PROGRESS,"Description",30,2000-05-01T13:00,
                2,EPIC,null,,null,,,
                3,SUBTASK,"Title",IN_PROGRESS,"Description",30,2000-05-01T13:30,2
                """);

        manager = FileBackedTaskManager.loadFromFile(path, historyManager);
        final Subtask savedSubtask = manager.getSubtask(3L);
        final List<Subtask> epicSubtasks = manager.getEpicSubtasks(2L);
        final List<Subtask> subtasks = manager.getSubtasks();
        final List<Task> prioritized = manager.getPrioritizedTasks();

        assertAll("subtask loaded with errors",
                () -> assertTaskEquals(expectedSubtask, savedSubtask, "subtask loaded with errors"),
                () -> assertListEquals(expectedSubtasks, epicSubtasks, "subtask loaded with errors"),
                () -> assertListEquals(expectedSubtasks, subtasks, "subtask loaded with errors"),
                () -> assertListEquals(expectedPrioritized, prioritized, "subtask loaded with errors")
        );
    }

    @Test
    public void shouldSetEpicStartTimeNullWhenLoadNoSubtasks() throws IOException {
        fillTestFileWithData("""
                id,type,name,status,description,duration,start,epic
                1,EPIC,"Title",,"Description",,,
                """);

        manager = FileBackedTaskManager.loadFromFile(path, historyManager);
        final LocalDateTime expectedStartTime = manager.getEpic(1L).getStartTime();

        assertNull(expectedStartTime, "wrong epic start time");
    }

    @Test
    public void shouldSetEpicStartTimeNullWhenLoadSubtasksAndSubtasksStartTimeNull() throws IOException {
        fillTestFileWithData("""
                id,type,name,status,description,duration,start,epic
                1,EPIC,"Title",,"Description",,,
                2,SUBTASK,null,NEW,null,null,null,1
                3,SUBTASK,"Title",IN_PROGRESS,"Description",null,null,1
                4,SUBTASK,"Modified Title",DONE,"Modified Description",null,null,1
                """);

        manager = FileBackedTaskManager.loadFromFile(path, historyManager);
        final LocalDateTime expectedStartTime = manager.getEpic(1L).getStartTime();

        assertNull(expectedStartTime, "wrong epic start time");
    }

    @Test
    public void shouldSetEpicMinStartTimeWhenLoadSubtasksAndSubtasksStartTimeNotNull() throws IOException {
        fillTestFileWithData("""
                id,type,name,status,description,duration,start,epic
                1,EPIC,"Title",,"Description",,,
                2,SUBTASK,null,NEW,null,null,null,1
                3,SUBTASK,"Title",IN_PROGRESS,"Description",30,2000-05-01T13:30,1
                4,SUBTASK,"Modified Title",DONE,"Modified Description",90,2000-05-01T15:00,1
                """);

        manager = FileBackedTaskManager.loadFromFile(path, historyManager);
        final LocalDateTime expectedStartTime = manager.getEpic(1L).getStartTime();

        assertEquals(TEST_START_TIME, expectedStartTime, "wrong epic start time");
    }

    @Test
    public void shouldSetEpicMinStartTimeWhenLoadSubtasksAndSubtasksStartTimeNotNullInOppositeOrder() throws
            IOException {
        fillTestFileWithData("""
                id,type,name,status,description,duration,start,epic
                1,EPIC,"Title",,"Description",,,
                2,SUBTASK,null,NEW,null,null,null,1
                3,SUBTASK,"Modified Title",DONE,"Modified Description",90,2000-05-01T15:00,1
                4,SUBTASK,"Title",IN_PROGRESS,"Description",30,2000-05-01T13:30,1
                """);

        manager = FileBackedTaskManager.loadFromFile(path, historyManager);
        final LocalDateTime expectedStartTime = manager.getEpic(1L).getStartTime();

        assertEquals(TEST_START_TIME, expectedStartTime, "wrong epic start time");
    }

    @Test
    public void shouldSetEpicEndTimeNullWhenLoadNoSubtasks() throws IOException {
        fillTestFileWithData("""
                id,type,name,status,description,duration,start,epic
                1,EPIC,"Title",,"Description",,,
                """);

        manager = FileBackedTaskManager.loadFromFile(path, historyManager);
        final LocalDateTime expectedEndTime = manager.getEpic(1L).getEndTime();

        assertNull(expectedEndTime, "wrong epic end time");
    }

    @Test
    public void shouldSetEpicEndTimeNullWhenLoadSubtasksAndSubtasksEndTimeNull() throws IOException {
        fillTestFileWithData("""
                id,type,name,status,description,duration,start,epic
                1,EPIC,"Title",,"Description",,,
                2,SUBTASK,null,NEW,null,null,null,1
                3,SUBTASK,"Title",IN_PROGRESS,"Description",null,null,1
                4,SUBTASK,"Modified Title",DONE,"Modified Description",null,null,1
                """);

        manager = FileBackedTaskManager.loadFromFile(path, historyManager);
        final LocalDateTime expectedEndTime = manager.getEpic(1L).getEndTime();

        assertNull(expectedEndTime, "wrong epic end time");
    }

    @Test
    public void shouldSetEpicMaxEndTimeWhenLoadSubtasksAndSubtasksEndTimeNotNull() throws IOException {
        fillTestFileWithData("""
                id,type,name,status,description,duration,start,epic
                1,EPIC,"Title",,"Description",,,
                2,SUBTASK,null,NEW,null,null,null,1
                3,SUBTASK,"Title",IN_PROGRESS,"Description",30,2000-05-01T13:30,1
                4,SUBTASK,"Modified Title",DONE,"Modified Description",90,2000-05-01T15:00,1
                """);

        manager = FileBackedTaskManager.loadFromFile(path, historyManager);
        final LocalDateTime expectedEndTime = manager.getEpic(1L).getEndTime();

        assertEquals(MODIFIED_END_TIME, expectedEndTime, "wrong epic end time");
    }

    @Test
    public void shouldSetEpicMaxEndTimeWhenLoadSubtasksAndSubtasksEndTimeNotNullInOppositeOrder() throws IOException {
        fillTestFileWithData("""
                id,type,name,status,description,duration,start,epic
                1,EPIC,"Title",,"Description",,,
                2,SUBTASK,null,NEW,null,null,null,1
                3,SUBTASK,"Modified Title",DONE,"Modified Description",90,2000-05-01T15:00,1
                4,SUBTASK,"Title",IN_PROGRESS,"Description",30,2000-05-01T13:30,1
                """);

        manager = FileBackedTaskManager.loadFromFile(path, historyManager);
        final LocalDateTime expectedEndTime = manager.getEpic(1L).getEndTime();

        assertEquals(MODIFIED_END_TIME, expectedEndTime, "wrong epic start time");
    }

    @Test
    public void shouldResetLastUsedIdWhenLoadFile() throws IOException {
        fillTestFileWithData("""
                id,type,name,status,description,duration,start,epic
                1000,TASK,"Title",IN_PROGRESS,"Description",30,2000-05-01T13:30,
                """);

        manager = FileBackedTaskManager.loadFromFile(path, historyManager);
        final long taskId = manager.addTask(modifiedTask);

        assertEquals(1001, taskId, "last used id loaded incorrectly");
    }

    protected void fillTestFileWithData(String data) throws IOException {
        Files.writeString(path, data, StandardCharsets.UTF_8);
    }
}
