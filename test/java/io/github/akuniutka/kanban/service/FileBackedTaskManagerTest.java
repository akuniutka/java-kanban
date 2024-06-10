package io.github.akuniutka.kanban.service;

import io.github.akuniutka.kanban.exception.ManagerLoadException;
import io.github.akuniutka.kanban.exception.ManagerSaveException;
import io.github.akuniutka.kanban.model.Epic;
import io.github.akuniutka.kanban.model.Subtask;
import io.github.akuniutka.kanban.model.Task;
import io.github.akuniutka.kanban.model.TaskStatus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.DosFileAttributeView;
import java.nio.file.attribute.PosixFileAttributeView;
import java.nio.file.attribute.PosixFilePermission;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

import static io.github.akuniutka.kanban.TestModels.*;
import static org.junit.jupiter.api.Assertions.*;

class FileBackedTaskManagerTest extends AbstractTaskManagerTest {
    protected static final String WRONG_FILE_FORMAT = "wrong file format";
    protected final Path path;

    public FileBackedTaskManagerTest() throws IOException {
        this.path = Files.createTempFile("kanban", null);
        this.manager = FileBackedTaskManager.loadFromFile(this.path, this.historyManager);
    }

    @Test
    public void shouldCreateFileBackedTaskManagerOfInterfaceType() {
        assertNotNull(manager, "task manager was not created");
    }

    @Test
    public void shouldThrowWhenFileIsNull() {
        final Exception exception = assertThrows(NullPointerException.class,
                () -> FileBackedTaskManager.loadFromFile(null, historyManager));
        assertEquals("cannot start: file is null", exception.getMessage(), WRONG_EXCEPTION_MESSAGE);
    }

    @Test
    public void shouldThrowWhenHistoryManagerNull() {
        final Exception exception = assertThrows(NullPointerException.class,
                () -> FileBackedTaskManager.loadFromFile(path, null));
        assertEquals("cannot start: history manager is null", exception.getMessage(), WRONG_EXCEPTION_MESSAGE);
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
    public void shouldNotThrowWhenFileNotYetCreated() throws IOException {
        Files.delete(path);
        assertDoesNotThrow(() -> FileBackedTaskManager.loadFromFile(path, historyManager),
                "should not throw when file not yet created");
    }

    @Test
    public void shouldNotThrowWhenFileEmpty() {
        assertNotNull(manager, "task manager was not created");
    }

    @Test
    public void shouldThrowWhenFileContainsNoHeader() throws IOException {
        fillTestFileWithData("""
                %s
                """.formatted(""));
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
    public void shouldNotThrowWhenFileContainsOnlyHeader() throws IOException {
        fillTestFileWithData("""
                id,type,name,status,description,duration,start,epic
                """);
        assertDoesNotThrow(() -> FileBackedTaskManager.loadFromFile(path, historyManager),
                "should not throw when file contains only header");
    }

    @Test
    public void shouldNotThrowWhenFileContainsOnlyHeaderAndNoNewLine() throws IOException {
        fillTestFileWithData("""
                id,type,name,status,description,duration,start,epic\
                """);
        assertDoesNotThrow(() -> FileBackedTaskManager.loadFromFile(path, historyManager),
                "should not throw when file contains only header");
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
    public void shouldNotLoadTaskWhenAnotherTaskIdAsId() throws IOException {
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
    public void shouldNotLoadTaskWhenEpicIdAsId() throws IOException {
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
    public void shouldNotLoadTaskWhenSubtaskIdAsId() throws IOException {
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
    public void shouldNotLoadTaskWhenStatusNull() throws IOException {
        fillTestFileWithData("""
                id,type,name,status,description,duration,start,epic
                1,TASK,"Title",null,"Description",30,2000-05-01T13:30,
                """);
        final String expectedMessage = "status cannot be null for id=1";

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

    @ParameterizedTest
    @MethodSource("io.github.akuniutka.kanban.TestModels#getOverlappingTimeSlots")
    public void shouldNotLoadTaskWhenOverlapAnotherPrioritizedTask(Duration duration, LocalDateTime startTime)
            throws IOException {
        fillTestFileWithData("""
                id,type,name,status,description,duration,start,epic
                2,EPIC,null,,null,,,
                3,SUBTASK,"Title",IN_PROGRESS,"Description",%s,%s,2
                1000,TASK,"Title",IN_PROGRESS,"Description",30,2000-05-01T13:30,
                """.formatted(duration.toMinutes(), startTime));
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
    public void shouldLoadTaskWhenDurationNotNullAndStartTimeNotNull() throws IOException {
        final Task expectedTask = fromTestTask().build();
        final List<Task> expectedTasks = List.of(expectedTask);
        fillTestFileWithData("""
                id,type,name,status,description,duration,start,epic
                1,TASK,"Title",IN_PROGRESS,"Description",30,2000-05-01T13:30,
                """);

        manager = FileBackedTaskManager.loadFromFile(path, historyManager);
        final Task savedTask = manager.getTaskById(1L);
        final List<Task> tasks = manager.getTasks();
        final List<Task> prioritized = manager.getPrioritizedTasks();

        assertAll("task loaded with errors",
                () -> assertTaskEquals(expectedTask, savedTask, "task loaded with errors"),
                () -> assertListEquals(expectedTasks, tasks, "task loaded with errors"),
                () -> assertListEquals(expectedTasks, prioritized, "task loaded with errors")
        );
    }

    @Test
    public void shouldLoadTaskWithStartTimeTruncatedToMinutes() throws IOException {
        final Task expectedTask = fromTestTask().build();
        final List<Task> expectedTasks = List.of(expectedTask);
        fillTestFileWithData("""
                id,type,name,status,description,duration,start,epic
                1,TASK,"Title",IN_PROGRESS,"Description",30,2000-05-01T13:30:25,
                """);

        manager = FileBackedTaskManager.loadFromFile(path, historyManager);
        final Task savedTask = manager.getTaskById(1L);
        final List<Task> tasks = manager.getTasks();
        final List<Task> prioritized = manager.getPrioritizedTasks();

        assertAll("task loaded with errors",
                () -> assertTaskEquals(expectedTask, savedTask, "task loaded with errors"),
                () -> assertListEquals(expectedTasks, tasks, "task loaded with errors"),
                () -> assertListEquals(expectedTasks, prioritized, "task loaded with errors")
        );
    }

    @Test
    public void shouldLoadTaskWhenDurationNullAndStartTimeNull() throws IOException {
        final Task expectedTask = fromTestTask().withDuration(null).withStartTime(null).build();
        final List<Task> expectedTasks = List.of(expectedTask);
        fillTestFileWithData("""
                id,type,name,status,description,duration,start,epic
                1,TASK,"Title",IN_PROGRESS,"Description",null,null,
                """);

        manager = FileBackedTaskManager.loadFromFile(path, historyManager);
        final Task savedTask = manager.getTaskById(1L);
        final List<Task> tasks = manager.getTasks();
        final List<Task> prioritized = manager.getPrioritizedTasks();

        assertAll("task loaded with errors",
                () -> assertTaskEquals(expectedTask, savedTask, "task loaded with errors"),
                () -> assertListEquals(expectedTasks, tasks, "task loaded with errors"),
                () -> assertTrue(prioritized.isEmpty(), "task loaded with errors")
        );
    }

    @Test
    public void shouldLoadTaskWhenFieldsNull() throws IOException {
        final Task expectedTask = fromEmptyTask().withId(TEST_TASK_ID).withStatus(TaskStatus.NEW).build();
        final List<Task> expectedTasks = List.of(expectedTask);
        fillTestFileWithData("""
                id,type,name,status,description,duration,start,epic
                1,TASK,null,NEW,null,null,null,
                """);

        manager = FileBackedTaskManager.loadFromFile(path, historyManager);
        final Task savedTask = manager.getTaskById(1L);
        final List<Task> tasks = manager.getTasks();
        final List<Task> prioritized = manager.getPrioritizedTasks();

        assertAll("task loaded with errors",
                () -> assertTaskEquals(expectedTask, savedTask, "task loaded with errors"),
                () -> assertListEquals(expectedTasks, tasks, "task loaded with errors"),
                () -> assertTrue(prioritized.isEmpty(), "task loaded with errors")
        );
    }

    @Test
    public void shouldLoadTaskWhenExactlyBeforeAnotherPrioritizedTask() throws IOException {
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
        final Task savedTask = manager.getTaskById(1000L);
        final List<Task> tasks = manager.getTasks();
        final List<Task> prioritized = manager.getPrioritizedTasks();

        assertAll("task loaded with errors",
                () -> assertTaskEquals(expectedTask, savedTask, "task loaded with errors"),
                () -> assertListEquals(expectedTasks, tasks, "task loaded with errors"),
                () -> assertListEquals(expectedPrioritized, prioritized, "task loaded with errors")
        );
    }

    @Test
    public void shouldLoadTaskWhenWithStartTimeTruncatedExactlyBeforeAnotherPrioritizedTask() throws IOException {
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
        final Task savedTask = manager.getTaskById(1000L);
        final List<Task> tasks = manager.getTasks();
        final List<Task> prioritized = manager.getPrioritizedTasks();

        assertAll("task loaded with errors",
                () -> assertTaskEquals(expectedTask, savedTask, "task loaded with errors"),
                () -> assertListEquals(expectedTasks, tasks, "task loaded with errors"),
                () -> assertListEquals(expectedPrioritized, prioritized, "task loaded with errors")
        );
    }

    @Test
    public void shouldLoadTaskWhenExactlyAfterAnotherPrioritizedTask() throws IOException {
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
        final Task savedTask = manager.getTaskById(1000L);
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
    public void shouldNotLoadEpicWhenTaskIdAsId() throws IOException {
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
    public void shouldNotLoadEpicWhenAnotherEpicIdAsId() throws IOException {
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
    public void shouldNotLoadEpicWhenSubtaskIdAsId() throws IOException {
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
    public void shouldLoadEpic() throws IOException {
        final Epic expectedEpic = fromTestEpic().withStatus(TaskStatus.NEW).build();
        final List<Epic> expectedEpics = List.of(expectedEpic);
        fillTestFileWithData("""
                id,type,name,status,description,duration,start,epic
                2,EPIC,"Title",,"Description",,,
                """);

        manager = FileBackedTaskManager.loadFromFile(path, historyManager);
        final Epic savedEpic = manager.getEpicById(2L);
        final List<Epic> epics = manager.getEpics();

        assertAll("task loaded with errors",
                () -> assertTaskEquals(expectedEpic, savedEpic, "task loaded with errors"),
                () -> assertListEquals(expectedEpics, epics, "task loaded with errors")
        );
    }

    @Test
    public void shouldLoadEpicWhenFieldsNull() throws IOException {
        final Epic expectedEpic = fromEmptyEpic().withId(TEST_EPIC_ID).withStatus(TaskStatus.NEW).build();
        final List<Epic> expectedEpics = List.of(expectedEpic);
        fillTestFileWithData("""
                id,type,name,status,description,duration,start,epic
                2,EPIC,null,,null,,,
                """);

        manager = FileBackedTaskManager.loadFromFile(path, historyManager);
        final Epic savedEpic = manager.getEpicById(2L);
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
    public void shouldNotLoadSubtaskWhenTaskIdAsId() throws IOException {
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
    public void shouldNotLoadSubtaskWhenEpicIdAsId() throws IOException {
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
    public void shouldNotLoadSubtaskWhenAnotherSubtaskIdAsId() throws IOException {
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
    public void shouldNotLoadSubtaskWhenStatusNull() throws IOException {
        fillTestFileWithData("""
                id,type,name,status,description,duration,start,epic
                2,EPIC,null,,null,,,
                3,SUBTASK,"Title",null,"Description",30,2000-05-01T13:30,2
                """);
        final String expectedMessage = "status cannot be null for id=3";

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

    @ParameterizedTest
    @MethodSource("io.github.akuniutka.kanban.TestModels#getOverlappingTimeSlots")
    public void shouldNotLoadSubtaskWhenOverlapAnotherPrioritizedTask(Duration duration, LocalDateTime startTime)
            throws IOException {
        fillTestFileWithData("""
                id,type,name,status,description,duration,start,epic
                1,TASK,"Title",IN_PROGRESS,"Description",%s,%s,
                2,EPIC,null,,null,,,
                3,SUBTASK,"Title",IN_PROGRESS,"Description",30,2000-05-01T13:30,2
                """.formatted(duration.toMinutes(), startTime));
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
    public void shouldNotLoadSubtaskWhenTaskIdAsEpicId() throws IOException {
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
    public void shouldNotLoadSubtaskWhenSubtaskIdAsEpicId() throws IOException {
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
    public void shouldLoadSubtaskWhenDurationNotNullAndStartTimeNotNull() throws IOException {
        final Subtask expectedSubtask = fromTestSubtask().build();
        final List<Subtask> expectedSubtasks = List.of(expectedSubtask);
        fillTestFileWithData("""
                id,type,name,status,description,duration,start,epic
                2,EPIC,"Title",,"Description",,,
                3,SUBTASK,"Title",IN_PROGRESS,"Description",30,2000-05-01T13:30,2
                """);

        manager = FileBackedTaskManager.loadFromFile(path, historyManager);
        final Subtask savedSubtask = manager.getSubtaskById(3L);
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
    public void shouldLoadSubtaskWithStartTimeTruncated() throws IOException {
        final Subtask expectedSubtask = fromTestSubtask().build();
        final List<Subtask> expectedSubtasks = List.of(expectedSubtask);
        fillTestFileWithData("""
                id,type,name,status,description,duration,start,epic
                2,EPIC,"Title",,"Description",,,
                3,SUBTASK,"Title",IN_PROGRESS,"Description",30,2000-05-01T13:30:25,2
                """);

        manager = FileBackedTaskManager.loadFromFile(path, historyManager);
        final Subtask savedSubtask = manager.getSubtaskById(3L);
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
    public void shouldLoadSubtaskWhenDurationNullAndStartTimeNull() throws IOException {
        final Subtask expectedSubtask = fromTestSubtask().withDuration(null).withStartTime(null).build();
        final List<Subtask> expectedSubtasks = List.of(expectedSubtask);
        fillTestFileWithData("""
                id,type,name,status,description,duration,start,epic
                2,EPIC,"Title",,"Description",,,
                3,SUBTASK,"Title",IN_PROGRESS,"Description",null,null,2
                """);

        manager = FileBackedTaskManager.loadFromFile(path, historyManager);
        final Subtask savedSubtask = manager.getSubtaskById(3L);
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
    public void shouldLoadSubtaskWhenFieldsNull() throws IOException {
        final Subtask expectedSubtask = fromEmptySubtask().withId(TEST_SUBTASK_ID).withEpicId(TEST_EPIC_ID)
                .withStatus(TaskStatus.NEW).build();
        final List<Subtask> expectedSubtasks = List.of(expectedSubtask);
        fillTestFileWithData("""
                id,type,name,status,description,duration,start,epic
                2,EPIC,"Title",,"Description",,,
                3,SUBTASK,null,NEW,null,null,null,2
                """);

        manager = FileBackedTaskManager.loadFromFile(path, historyManager);
        final Subtask savedSubtask = manager.getSubtaskById(3L);
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
    public void shouldLoadSubtaskWhenExactlyBeforeAnotherPrioritizeTask() throws IOException {
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
        final Subtask savedSubtask = manager.getSubtaskById(3L);
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
    public void shouldLoadSubtaskWhenWithStartTimeTruncatedExactlyBeforeAnotherPrioritizedTask() throws IOException {
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
        final Subtask savedSubtask = manager.getSubtaskById(3L);
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
    public void shouldLoadSubtaskWhenExactlyAfterAnotherPrioritizedTask() throws IOException {
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
        final Subtask savedSubtask = manager.getSubtaskById(3L);
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
    public void shouldSetEpicDurationNullWhenLoadNoSubtask() throws IOException {
        fillTestFileWithData("""
                id,type,name,status,description,duration,start,epic
                1,EPIC,"Title",,"Description",,,
                """);

        manager = FileBackedTaskManager.loadFromFile(path, historyManager);
        final Duration duration = manager.getEpicById(1L).getDuration();

        assertNull(duration, "wrong epic duration");
    }

    @Test
    public void shouldSetEpicDurationNullWhenLoadSubtaskAndSubtaskDurationNull() throws IOException {
        fillTestFileWithData("""
                id,type,name,status,description,duration,start,epic
                1,EPIC,"Title",,"Description",,,
                2,SUBTASK,null,NEW,null,null,null,1
                3,SUBTASK,"Title",IN_PROGRESS,"Description",null,null,1
                4,SUBTASK,"Modified Title",DONE,"Modified Description",null,null,1
                """);

        manager = FileBackedTaskManager.loadFromFile(path, historyManager);
        final Duration duration = manager.getEpicById(1L).getDuration();

        assertNull(duration, "wrong epic duration");
    }

    @Test
    public void shouldSetEpicDurationNotNullWhenLoadSubtaskAndSubtaskDurationNotNull() throws IOException {
        fillTestFileWithData("""
                id,type,name,status,description,duration,start,epic
                1,EPIC,"Title",,"Description",,,
                2,SUBTASK,null,NEW,null,null,null,1
                3,SUBTASK,"Title",IN_PROGRESS,"Description",30,2000-05-01T13:30,1
                4,SUBTASK,"Modified Title",DONE,"Modified Description",90,2000-05-01T15:00,1
                """);

        manager = FileBackedTaskManager.loadFromFile(path, historyManager);
        final Duration duration = manager.getEpicById(1L).getDuration();

        assertEquals(TEST_DURATION.plus(MODIFIED_DURATION), duration, "wrong epic duration");
    }

    @Test
    public void shouldSetEpicStartTimeNullWhenLoadNoSubtask() throws IOException {
        fillTestFileWithData("""
                id,type,name,status,description,duration,start,epic
                1,EPIC,"Title",,"Description",,,
                """);

        manager = FileBackedTaskManager.loadFromFile(path, historyManager);
        final LocalDateTime startTime = manager.getEpicById(1L).getStartTime();

        assertNull(startTime, "wrong epic start time");
    }

    @Test
    public void shouldSetEpicStartTimeNullWhenLoadSubtaskAndSubtaskStartTimeNull() throws IOException {
        fillTestFileWithData("""
                id,type,name,status,description,duration,start,epic
                1,EPIC,"Title",,"Description",,,
                2,SUBTASK,null,NEW,null,null,null,1
                3,SUBTASK,"Title",IN_PROGRESS,"Description",null,null,1
                4,SUBTASK,"Modified Title",DONE,"Modified Description",null,null,1
                """);

        manager = FileBackedTaskManager.loadFromFile(path, historyManager);
        final LocalDateTime startTime = manager.getEpicById(1L).getStartTime();

        assertNull(startTime, "wrong epic start time");
    }

    @Test
    public void shouldSetEpicMinStartTimeWhenLoadSubtaskAndSubtaskStartTimeNotNull() throws IOException {
        fillTestFileWithData("""
                id,type,name,status,description,duration,start,epic
                1,EPIC,"Title",,"Description",,,
                2,SUBTASK,null,NEW,null,null,null,1
                3,SUBTASK,"Title",IN_PROGRESS,"Description",30,2000-05-01T13:30,1
                4,SUBTASK,"Modified Title",DONE,"Modified Description",90,2000-05-01T15:00,1
                """);

        manager = FileBackedTaskManager.loadFromFile(path, historyManager);
        final LocalDateTime startTime = manager.getEpicById(1L).getStartTime();

        assertEquals(TEST_START_TIME, startTime, "wrong epic start time");
    }

    @Test
    public void shouldSetEpicMinStartTimeWhenLoadSubtaskAndSubtaskStartTimeNotNullAndInOppositeOrder()
            throws IOException {
        fillTestFileWithData("""
                id,type,name,status,description,duration,start,epic
                1,EPIC,"Title",,"Description",,,
                2,SUBTASK,null,NEW,null,null,null,1
                3,SUBTASK,"Modified Title",DONE,"Modified Description",90,2000-05-01T15:00,1
                4,SUBTASK,"Title",IN_PROGRESS,"Description",30,2000-05-01T13:30,1
                """);

        manager = FileBackedTaskManager.loadFromFile(path, historyManager);
        final LocalDateTime startTime = manager.getEpicById(1L).getStartTime();

        assertEquals(TEST_START_TIME, startTime, "wrong epic start time");
    }

    @Test
    public void shouldSetEpicEndTimeNullWhenLoadNoSubtask() throws IOException {
        fillTestFileWithData("""
                id,type,name,status,description,duration,start,epic
                1,EPIC,"Title",,"Description",,,
                """);

        manager = FileBackedTaskManager.loadFromFile(path, historyManager);
        final LocalDateTime endTime = manager.getEpicById(1L).getEndTime();

        assertNull(endTime, "wrong epic end time");
    }

    @Test
    public void shouldSetEpicEndTimeNullWhenLoadSubtaskAndSubtaskEndTimeNull() throws IOException {
        fillTestFileWithData("""
                id,type,name,status,description,duration,start,epic
                1,EPIC,"Title",,"Description",,,
                2,SUBTASK,null,NEW,null,null,null,1
                3,SUBTASK,"Title",IN_PROGRESS,"Description",null,null,1
                4,SUBTASK,"Modified Title",DONE,"Modified Description",null,null,1
                """);

        manager = FileBackedTaskManager.loadFromFile(path, historyManager);
        final LocalDateTime endTime = manager.getEpicById(1L).getEndTime();

        assertNull(endTime, "wrong epic end time");
    }

    @Test
    public void shouldSetEpicMaxEndTimeWhenLoadSubtaskAndSubtaskEndTimeNotNull() throws IOException {
        fillTestFileWithData("""
                id,type,name,status,description,duration,start,epic
                1,EPIC,"Title",,"Description",,,
                2,SUBTASK,null,NEW,null,null,null,1
                3,SUBTASK,"Title",IN_PROGRESS,"Description",30,2000-05-01T13:30,1
                4,SUBTASK,"Modified Title",DONE,"Modified Description",90,2000-05-01T15:00,1
                """);

        manager = FileBackedTaskManager.loadFromFile(path, historyManager);
        final LocalDateTime endTime = manager.getEpicById(1L).getEndTime();

        assertEquals(MODIFIED_END_TIME, endTime, "wrong epic end time");
    }

    @Test
    public void shouldSetEpicMaxEndTimeWhenLoadSubtaskAndSubtaskEndTimeNotNullAndInOppositeOrder() throws IOException {
        fillTestFileWithData("""
                id,type,name,status,description,duration,start,epic
                1,EPIC,"Title",,"Description",,,
                2,SUBTASK,null,NEW,null,null,null,1
                3,SUBTASK,"Modified Title",DONE,"Modified Description",90,2000-05-01T15:00,1
                4,SUBTASK,"Title",IN_PROGRESS,"Description",30,2000-05-01T13:30,1
                """);

        manager = FileBackedTaskManager.loadFromFile(path, historyManager);
        final LocalDateTime endTime = manager.getEpicById(1L).getEndTime();

        assertEquals(MODIFIED_END_TIME, endTime, "wrong epic start time");
    }

    @Test
    public void shouldSetEpicStatusNewWhenLoadNoSubtask() throws IOException {
        fillTestFileWithData("""
                id,type,name,status,description,duration,start,epic
                1,EPIC,"Title",,"Description",,,
                """);

        manager = FileBackedTaskManager.loadFromFile(path, historyManager);
        final TaskStatus actualStatus = manager.getEpicById(1L).getStatus();

        assertEquals(TaskStatus.NEW, actualStatus, "wrong epic status");
    }

    @ParameterizedTest
    @CsvSource({"NEW,NEW", "NEW,IN_PROGRESS", "NEW,DONE", "IN_PROGRESS,NEW", "IN_PROGRESS,IN_PROGRESS",
            "IN_PROGRESS,DONE", "DONE,NEW", "DONE,IN_PROGRESS", "DONE,DONE"})
    public void shouldSetEpicStatusWhenLoadSubtask(TaskStatus statusA, TaskStatus statusB) throws IOException {
        fillTestFileWithData("""
                id,type,name,status,description,duration,start,epic
                1,EPIC,"Title",,"Description",,,
                2,SUBTASK,"Title",%s,"Description",30,2000-05-01T13:30,1
                3,SUBTASK,"Modified Title",%s,"Modified Description",90,2000-05-01T15:00,1
                """.formatted(statusA, statusB));
        final TaskStatus expectedStatus = statusA == statusB ? statusA : TaskStatus.IN_PROGRESS;

        manager = FileBackedTaskManager.loadFromFile(path, historyManager);
        final TaskStatus actualStatus = manager.getEpicById(1L).getStatus();

        assertEquals(expectedStatus, actualStatus, "wrong epic status");
    }

    @Test
    public void shouldSetEpicStatusInProgressWhenLoadSubtaskAndAllStatuses() throws IOException {
        fillTestFileWithData("""
                id,type,name,status,description,duration,start,epic
                1,EPIC,"Title",,"Description",,,
                2,SUBTASK,null,NEW,null,null,null,1
                3,SUBTASK,"Title",IN_PROGRESS,"Description",30,2000-05-01T13:30,1
                4,SUBTASK,"Modified Title",DONE,"Modified Description",90,2000-05-01T15:00,1
                """);

        manager = FileBackedTaskManager.loadFromFile(path, historyManager);
        final TaskStatus actualStatus = manager.getEpicById(1L).getStatus();

        assertEquals(TaskStatus.IN_PROGRESS, actualStatus, "wrong epic status");
    }

    @Test
    public void shouldResetLastUsedIdWhenLoadFile() throws IOException {
        fillTestFileWithData("""
                id,type,name,status,description,duration,start,epic
                1000,TASK,"Title",IN_PROGRESS,"Description",30,2000-05-01T13:30,
                """);

        manager = FileBackedTaskManager.loadFromFile(path, historyManager);
        final long taskId = manager.createTask(modifiedTask);

        assertEquals(1001, taskId, "last used id loaded incorrectly");
    }

    @Test
    public void shouldImmediatelyThrowWhenFileReadOnly() throws IOException {
        if (Files.getFileStore(path).supportsFileAttributeView(DosFileAttributeView.class)) {
            Files.setAttribute(path, "dos:readonly", true);
        } else if (Files.getFileStore(path).supportsFileAttributeView(PosixFileAttributeView.class)) {
            Files.setPosixFilePermissions(path, Set.of(PosixFilePermission.OWNER_READ));
        } else {
            throw new AssertionError("cannot test with read-only file");
        }
        final String expectedMessage = "cannot write to file \"" + path + "\"";

        final Exception exception = assertThrows(ManagerSaveException.class,
                () -> FileBackedTaskManager.loadFromFile(path, historyManager));
        assertEquals(expectedMessage, exception.getMessage(), WRONG_EXCEPTION_MESSAGE);
    }

    @Test
    public void shouldSaveWhenCreateTask() throws IOException {
        String expectedString = """
                id,type,name,status,description,duration,start,epic
                %%d,TASK,"%s",%s,"%s",%s,%s,
                """.formatted(testTask.getTitle(), testTask.getStatus(), testTask.getDescription(),
                testTask.getDuration().toMinutes(), testTask.getStartTime());

        final long taskId = manager.createTask(testTask);

        expectedString = expectedString.formatted(taskId);
        final String actualString = Files.readString(path);
        assertEquals(expectedString, actualString, WRONG_FILE_FORMAT);
    }

    @Test
    public void shouldSaveWhenCreateTaskAndFieldsNull() throws IOException {
        String expectedString = """
                id,type,name,status,description,duration,start,epic
                %d,TASK,null,NEW,null,null,null,
                """;

        final long taskId = manager.createTask(fromEmptyTask().withStatus(TaskStatus.NEW).build());

        expectedString = expectedString.formatted(taskId);
        final String actualString = Files.readString(path);
        assertEquals(expectedString, actualString, WRONG_FILE_FORMAT);
    }

    @Test
    public void shouldSaveWhenUpdateTask() throws IOException {
        final long taskId = manager.createTask(testTask);
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
    public void shouldSaveWhenDeleteTask() throws IOException {
        final long taskId = manager.createTask(testTask);
        final String expectedString = """
                id,type,name,status,description,duration,start,epic
                """;

        manager.deleteTask(taskId);

        final String actualString = Files.readString(path);
        assertEquals(expectedString, actualString, WRONG_FILE_FORMAT);
    }

    @Test
    public void shouldSaveWhenCreateEpic() throws IOException {
        String expectedString = """
                id,type,name,status,description,duration,start,epic
                %%d,EPIC,"%s",,"%s",,,
                """.formatted(testEpic.getTitle(), testEpic.getDescription());

        final long epicId = manager.createEpic(testEpic);

        expectedString = expectedString.formatted(epicId);
        final String actualString = Files.readString(path);
        assertEquals(expectedString, actualString, WRONG_FILE_FORMAT);
    }

    @Test
    public void shouldSaveWhenCreateEpicAndFieldsNull() throws IOException {
        String expectedString = """
                id,type,name,status,description,duration,start,epic
                %d,EPIC,null,,null,,,
                """;

        final long epicId = manager.createEpic(fromEmptyEpic().build());

        expectedString = expectedString.formatted(epicId);
        final String actualString = Files.readString(path);
        assertEquals(expectedString, actualString, WRONG_FILE_FORMAT);
    }

    @Test
    public void shouldSaveWhenUpdateEpic() throws IOException {
        final long epicId = manager.createEpic(testEpic);
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
    public void shouldSaveWhenDeleteEpic() throws IOException {
        final long epicId = manager.createEpic(testEpic);
        final String expectedString = """
                id,type,name,status,description,duration,start,epic
                """;

        manager.deleteEpic(epicId);

        final String actualString = Files.readString(path);
        assertEquals(expectedString, actualString, WRONG_FILE_FORMAT);
    }

    @Test
    public void shouldSaveWhenCreateSubtask() throws IOException {
        final Subtask expectedSubtask = fromTestSubtask().build();
        String expectedString = """
                id,type,name,status,description,duration,start,epic
                %%d,EPIC,"%s",,"%s",,,
                %%d,SUBTASK,"%s",%s,"%s",%s,%s,%%d
                """.formatted(testEpic.getTitle(), testEpic.getDescription(), expectedSubtask.getTitle(),
                expectedSubtask.getStatus(), expectedSubtask.getDescription(),
                expectedSubtask.getDuration().toMinutes(), expectedSubtask.getStartTime());
        final long epicId = manager.createEpic(testEpic);
        final Subtask subtask = fromTestSubtask().withId(null).withEpicId(epicId).build();

        final long subtaskId = manager.createSubtask(subtask);

        expectedString = expectedString.formatted(epicId, subtaskId, epicId);
        final String actualString = Files.readString(path);
        assertEquals(expectedString, actualString, WRONG_FILE_FORMAT);
    }

    @Test
    public void shouldSaveWhenCreateSubtaskAndFieldsNull() throws IOException {
        String expectedString = """
                id,type,name,status,description,duration,start,epic
                %%d,EPIC,"%s",,"%s",,,
                %%d,SUBTASK,null,NEW,null,null,null,%%d
                """.formatted(testEpic.getTitle(), testEpic.getDescription());
        final long epicId = manager.createEpic(testEpic);
        final Subtask subtask = fromEmptySubtask().withEpicId(epicId).withStatus(TaskStatus.NEW).build();

        final long subtaskId = manager.createSubtask(subtask);

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
        final long epicId = manager.createEpic(testEpic);
        final Subtask subtask = fromTestSubtask().withId(null).withEpicId(epicId).build();
        final long subtaskId = manager.createSubtask(subtask);
        final Subtask update = fromModifiedSubtask().withId(subtaskId).withEpicId(epicId).build();

        manager.updateSubtask(update);

        expectedString = expectedString.formatted(epicId, subtaskId, epicId);
        final String actualString = Files.readString(path);
        assertEquals(expectedString, actualString, WRONG_FILE_FORMAT);
    }

    @Test
    public void shouldSaveWhenDeleteSubtask() throws IOException {
        String expectedString = """
                id,type,name,status,description,duration,start,epic
                %%d,EPIC,"%s",,"%s",,,
                """.formatted(testEpic.getTitle(), testEpic.getDescription());
        final long epicId = manager.createEpic(testEpic);
        final Subtask subtask = fromTestSubtask().withId(null).withEpicId(epicId).build();
        final long subtaskId = manager.createSubtask(subtask);

        manager.deleteSubtask(subtaskId);

        expectedString = expectedString.formatted(epicId);
        final String actualString = Files.readString(path);
        assertEquals(expectedString, actualString, WRONG_FILE_FORMAT);
    }

    @Test
    public void shouldSaveWhenDeleteTasks() throws IOException {
        manager.createTask(testTask);
        manager.createTask(modifiedTask);
        final String expectedString = """
                id,type,name,status,description,duration,start,epic
                """;

        manager.deleteTasks();

        final String actualString = Files.readString(path);
        assertEquals(expectedString, actualString, WRONG_FILE_FORMAT);
    }

    @Test
    public void shouldSaveWhenDeleteEpics() throws IOException {
        manager.createEpic(testEpic);
        manager.createEpic(modifiedEpic);
        final String expectedString = """
                id,type,name,status,description,duration,start,epic
                """;

        manager.deleteEpics();

        final String actualString = Files.readString(path);
        assertEquals(expectedString, actualString, WRONG_FILE_FORMAT);
    }

    @Test
    public void shouldSaveWhenDeleteSubtasks() throws IOException {
        String expectedString = """
                id,type,name,status,description,duration,start,epic
                %%d,EPIC,"%s",,"%s",,,
                """.formatted(testEpic.getTitle(), testEpic.getDescription());
        final long epicId = manager.createEpic(testEpic);
        final Subtask subtaskA = fromTestSubtask().withId(null).withEpicId(epicId).build();
        final Subtask subtaskB = fromModifiedSubtask().withId(null).withEpicId(epicId).build();
        manager.createSubtask(subtaskA);
        manager.createSubtask(subtaskB);

        manager.deleteSubtasks();

        expectedString = expectedString.formatted(epicId);
        final String actualString = Files.readString(path);
        assertEquals(expectedString, actualString, WRONG_FILE_FORMAT);
    }

    protected void fillTestFileWithData(String data) throws IOException {
        Files.writeString(path, data, StandardCharsets.UTF_8);
    }
}
