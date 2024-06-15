package io.github.akuniutka.kanban;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import io.github.akuniutka.kanban.exception.ManagerValidationException;
import io.github.akuniutka.kanban.exception.TaskNotFoundException;
import io.github.akuniutka.kanban.exception.TaskOverlapException;
import io.github.akuniutka.kanban.model.Epic;
import io.github.akuniutka.kanban.model.Subtask;
import io.github.akuniutka.kanban.model.Task;
import io.github.akuniutka.kanban.model.TaskStatus;
import io.github.akuniutka.kanban.web.DurationAdapter;
import io.github.akuniutka.kanban.web.LocalDateTimeAdapter;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static io.github.akuniutka.kanban.TestModels.*;
import static org.junit.jupiter.api.Assertions.*;

class HttpTaskServerTest {
    private static final String HOSTNAME = "http://localhost";
    private static final int PORT = 8080;
    private static final int OK = 200;
    private static final int CREATED = 201;
    private static final int BAD_REQUEST = 400;
    private static final int NOT_FOUND = 404;
    private static final int METHOD_NOT_ALLOWED = 405;
    private static final int NOT_ACCEPTABLE = 406;
    private static final int INTERNAL_SERVER_ERROR = 500;
    private static final String JSON = "application/json";
    private static final Gson gson = new GsonBuilder()
            .registerTypeAdapter(Duration.class, new DurationAdapter())
            .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter())
            .create();
    private final HttpClient client;
    private final HttpTaskServer httpTaskServer;
    private final MockTaskManager mock;
    private final Task emptyTask;
    private final Task testTask;
    private final Task modifiedTask;
    private final Epic emptyEpic;
    private final Epic testEpic;
    private final Epic modifiedEpic;
    private final Subtask emptySubtask;
    private final Subtask testSubtask;
    private final Subtask modifiedSubtask;
    private final String jsonEmptyTask;
    private final String jsonTestTask;
    private final String jsonModifiedTask;
    private final String jsonEmptyEpic;
    private final String jsonTestEpic;
    private final String jsonModifiedEpic;
    private final String jsonEmptySubtask;
    private final String jsonTestSubtask;
    private final String jsonModifiedSubtask;
    private final String jsonEmptyList;

    public HttpTaskServerTest() throws IOException {
        this.client = HttpClient.newHttpClient();
        this.mock = new MockTaskManager();
        this.httpTaskServer = new HttpTaskServer(mock);
        this.httpTaskServer.start();
        this.emptyTask = fromEmptyTask().withStatus(TaskStatus.NEW).build();
        this.testTask = fromTestTask().build();
        this.modifiedTask = fromModifiedTask().withId(ANOTHER_TEST_ID).build();
        this.emptyEpic = fromEmptyEpic().withStatus(TaskStatus.NEW).build();
        this.testEpic = fromTestEpic().build();
        this.modifiedEpic = fromModifiedEpic().withId(ANOTHER_TEST_ID).build();
        this.emptySubtask = fromEmptySubtask().withStatus(TaskStatus.NEW).build();
        this.testSubtask = fromTestSubtask().build();
        this.modifiedSubtask = fromModifiedSubtask().withId(ANOTHER_TEST_ID).build();
        this.jsonEmptyTask = gson.toJson(emptyTask);
        this.jsonTestTask = gson.toJson(testTask);
        this.jsonModifiedTask = gson.toJson(modifiedTask);
        this.jsonEmptyEpic = gson.toJson(emptyEpic);
        this.jsonTestEpic = gson.toJson(testEpic);
        this.jsonModifiedEpic = gson.toJson(modifiedEpic);
        this.jsonEmptySubtask = gson.toJson(emptySubtask);
        this.jsonTestSubtask = gson.toJson(testSubtask);
        this.jsonModifiedSubtask = gson.toJson(modifiedSubtask);
        this.jsonEmptyList = gson.toJson(Collections.emptyList());
    }

    @AfterEach
    public void tearDown() {
        httpTaskServer.stop();
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "/"})
    public void shouldPassTasksFromTaskManagerWhenGetTasks(String suffix) {
        final List<Task> tasks = List.of(emptyTask, testTask, modifiedTask);
        final String expectedBody = gson.toJson(tasks);
        mock.withGetTasks(() -> tasks);

        HttpResponse<String> response = get("/api/v1/tasks" + suffix);

        assertAll("wrong call of get all tasks",
                () -> assertEquals(1, mock.calls().getTasks(), "wrong number of calls to getTasks()"),
                () -> assertEquals(OK, response.statusCode(), "wrong status code"),
                () -> assertEquals(JSON, response.headers().firstValue("Content-Type").orElse(null),
                        "wrong content type"),
                () -> assertEquals(expectedBody, response.body(), "wrong body")
        );
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "/"})
    public void shouldPassTasksFromTaskManagerWhenGetTasksAndEmptyList(String suffix) {
        mock.withGetTasks(Collections::emptyList);

        HttpResponse<String> response = get("/api/v1/tasks" + suffix);

        assertAll("wrong call of get all tasks",
                () -> assertEquals(1, mock.calls().getTasks(), "wrong number of calls to getTasks()"),
                () -> assertEquals(OK, response.statusCode(), "wrong status code"),
                () -> assertEquals(JSON, response.headers().firstValue("Content-Type").orElse(null),
                        "wrong content type"),
                () -> assertEquals(jsonEmptyList, response.body(), "wrong body")
        );
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "/"})
    public void shouldRespondInternalServerErrorWhenGetTasksAndException(String suffix) {
        mock.withGetTasks(() -> {
            throw new RuntimeException();
        });

        HttpResponse<String> response = get("/api/v1/tasks" + suffix);

        assertEquals(INTERNAL_SERVER_ERROR, response.statusCode(), "wrong status code");
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "/"})
    public void shouldPassTaskToAndFromTaskManagerWhenPostTasks(String suffix) {
        final Task task = fromTestTask().withId(null).build();
        final List<Task> expectedTasks = List.of(task);
        final String requestBody = gson.toJson(task);
        mock.withCreateTask(t -> modifiedTask);

        HttpResponse<String> response = post("/api/v1/tasks" + suffix, requestBody);

        assertAll("task created incorrectly",
                () -> assertListEquals(expectedTasks, mock.calls().createTask(),
                        "task passed to createTask() incorrectly"),
                () -> assertEquals(CREATED, response.statusCode(), "wrong status code"),
                () -> assertEquals(JSON, response.headers().firstValue("Content-Type").orElse(null),
                        "wrong content type"),
                () -> assertEquals(jsonModifiedTask, response.body(), "wrong body")
        );
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "/"})
    public void shouldPassTaskToAndFromTaskManagerWhenPostTasksAndTaskEmpty(String suffix) {
        final List<Task> expectedTasks = List.of(emptyTask);
        mock.withCreateTask(t -> emptyTask);

        HttpResponse<String> response = post("/api/v1/tasks" + suffix, jsonEmptyTask);

        assertAll("task created incorrectly",
                () -> assertListEquals(expectedTasks, mock.calls().createTask(),
                        "task passed to createTask() incorrectly"),
                () -> assertEquals(CREATED, response.statusCode(), "wrong status code"),
                () -> assertEquals(JSON, response.headers().firstValue("Content-Type").orElse(null),
                        "wrong content type"),
                () -> assertEquals(jsonEmptyTask, response.body(), "wrong body")
        );
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "/"})
    public void shouldRespondBadRequestWhenPostTasksAndNoBody(String suffix) {
        HttpResponse<String> response = post("/api/v1/tasks" + suffix, "");

        assertEquals(BAD_REQUEST, response.statusCode(), "wrong status code");
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "/"})
    public void shouldRespondBadRequestWhenPostTasksAndValidationException(String suffix) {
        mock.withCreateTask(t -> {
            throw new ManagerValidationException("status cannot be null");
        });

        HttpResponse<String> response = post("/api/v1/tasks" + suffix, jsonEmptyTask);

        assertEquals(BAD_REQUEST, response.statusCode(), "wrong status code");
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "/"})
    public void shouldRespondNotAcceptableWhenPostTasksAndTaskOverlapException(String suffix) {
        mock.withCreateTask(t -> {
            throw new TaskOverlapException("conflict with another task for time slot");
        });

        HttpResponse<String> response = post("/api/v1/tasks" + suffix, jsonEmptyTask);

        assertEquals(NOT_ACCEPTABLE, response.statusCode(), "wrong status code");
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "/"})
    public void shouldRespondInternalServerErrorWhenPostTasksAndException(String suffix) {
        mock.withCreateTask(t -> {
            throw new RuntimeException();
        });

        HttpResponse<String> response = post("/api/v1/tasks" + suffix, jsonEmptyTask);

        assertEquals(INTERNAL_SERVER_ERROR, response.statusCode(), "wrong status code");
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "/"})
    public void shouldRespondMethodNotAllowedWhenDeleteTasks(String suffix) {
        HttpResponse<String> response = delete("/api/v1/tasks" + suffix);

        assertEquals(METHOD_NOT_ALLOWED, response.statusCode(), "wrong status code");
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "/"})
    public void shouldPassIdToAndTaskFromTaskManagerWhenGetTaskById(String suffix) {
        final List<Long> expectedIds = List.of(TEST_TASK_ID);
        mock.withGetTaskById(id -> Optional.of(testTask));

        HttpResponse<String> response = get("/api/v1/tasks/" + TEST_TASK_ID + suffix);

        assertAll("task retrieved incorrectly",
                () -> assertEquals(expectedIds, mock.calls().getTaskById(), "id passed to getTaskById() incorrectly"),
                () -> assertEquals(OK, response.statusCode(), "wrong status code"),
                () -> assertEquals(JSON, response.headers().firstValue("Content-Type").orElse(null),
                        "wrong content type"),
                () -> assertEquals(jsonTestTask, response.body(), "wrong body")
        );
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "/"})
    public void shouldPassIdToAndTaskFromTaskManagerWhenGetTaskByIdAndTaskEmpty(String suffix) {
        final Task task = fromEmptyTask().withId(ANOTHER_TEST_ID).withStatus(TaskStatus.NEW).build();
        final String expectedBody = gson.toJson(task);
        final List<Long> expectedIds = List.of(TEST_TASK_ID);
        mock.withGetTaskById(id -> Optional.of(task));

        HttpResponse<String> response = get("/api/v1/tasks/" + TEST_TASK_ID + suffix);

        assertAll("task retrieved incorrectly",
                () -> assertEquals(expectedIds, mock.calls().getTaskById(), "id passed to getTaskById() incorrectly"),
                () -> assertEquals(OK, response.statusCode(), "wrong status code"),
                () -> assertEquals(JSON, response.headers().firstValue("Content-Type").orElse(null),
                        "wrong content type"),
                () -> assertEquals(expectedBody, response.body(), "wrong body")
        );
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "/"})
    public void shouldRespondNotFoundWhenGetTaskByIdAndNotFound(String suffix) {
        mock.withGetTaskById(id -> Optional.empty());

        HttpResponse<String> response = get("/api/v1/tasks/" + TEST_TASK_ID + suffix);

        assertEquals(NOT_FOUND, response.statusCode(), "wrong status code");
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "/"})
    public void shouldRespondNotFoundWhenGetTaskByIdAndWrongId(String suffix) {
        HttpResponse<String> response = get("/api/v1/tasks/" + "TEST_TASK_ID" + suffix);

        assertEquals(NOT_FOUND, response.statusCode(), "wrong status code");
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "/"})
    public void shouldRespondInternalServerErrorWhenGetTaskByIdAndException(String suffix) {
        mock.withGetTaskById(id -> {
            throw new RuntimeException();
        });

        HttpResponse<String> response = get("/api/v1/tasks/" + TEST_TASK_ID + suffix);

        assertEquals(INTERNAL_SERVER_ERROR, response.statusCode(), "wrong status code");
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "/"})
    public void shouldPassTaskToAndFromTaskManagerWhenPutTask(String suffix) {
        final List<Task> expectedTasks = List.of(testTask);
        mock.withUpdateTask(t -> modifiedTask);

        HttpResponse<String> response = put("/api/v1/tasks/" + TEST_TASK_ID + suffix, jsonTestTask);

        assertAll("task updated incorrectly",
                () -> assertListEquals(expectedTasks, mock.calls().updateTask(),
                        "task passed to updateTask() incorrectly"),
                () -> assertEquals(CREATED, response.statusCode(), "wrong status code"),
                () -> assertEquals(JSON, response.headers().firstValue("Content-Type").orElse(null),
                        "wrong content type"),
                () -> assertEquals(jsonModifiedTask, response.body(), "wrong body")
        );
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "/"})
    public void shouldPassTaskToAndFromTaskManagerWhenPutTaskAndTaskEmpty(String suffix) {
        final Task task = fromEmptyTask().withId(ANOTHER_TEST_ID).withStatus(TaskStatus.NEW).build();
        final List<Task> expectedTasks = List.of(task);
        final String requestBody = gson.toJson(task);
        mock.withUpdateTask(t -> emptyTask);

        HttpResponse<String> response = put("/api/v1/tasks/" + ANOTHER_TEST_ID + suffix, requestBody);

        assertAll("task updated incorrectly",
                () -> assertListEquals(expectedTasks, mock.calls().updateTask(),
                        "task passed to updateTask() incorrectly"),
                () -> assertEquals(CREATED, response.statusCode(), "wrong status code"),
                () -> assertEquals(JSON, response.headers().firstValue("Content-Type").orElse(null),
                        "wrong content type"),
                () -> assertEquals(jsonEmptyTask, response.body(), "wrong body")
        );
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "/"})
    public void shouldRespondNotFoundWhenPutTaskAndWrongId(String suffix) {
        HttpResponse<String> response = put("/api/v1/tasks/" + "TEST_TASK_ID" + suffix, jsonTestTask);

        assertEquals(NOT_FOUND, response.statusCode(), "wrong status code");
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "/"})
    public void shouldRespondBadRequestWhenPutTaskAndIdNotFromEndpoint(String suffix) {
        HttpResponse<String> response = put("/api/v1/tasks/" + ANOTHER_TEST_ID + suffix, jsonTestTask);

        assertEquals(BAD_REQUEST, response.statusCode(), "wrong status code");
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "/"})
    public void shouldRespondBadRequestWhenPutTaskAndNoBody(String suffix) {
        HttpResponse<String> response = put("/api/v1/tasks/" + TEST_TASK_ID + suffix, "");

        assertEquals(BAD_REQUEST, response.statusCode(), "wrong status code");
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "/"})
    public void shouldRespondBadRequestWhenPutTaskAndValidationException(String suffix) {
        mock.withUpdateTask(t -> {
            throw new ManagerValidationException("status cannot be null");
        });

        HttpResponse<String> response = put("/api/v1/tasks/" + TEST_TASK_ID + suffix, jsonTestTask);

        assertEquals(BAD_REQUEST, response.statusCode(), "wrong status code");
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "/"})
    public void shouldRespondNotAcceptableWhenPutTaskAndTaskOverlapException(String suffix) {
        mock.withUpdateTask(t -> {
            throw new TaskOverlapException("conflict with another task for time slot");
        });

        HttpResponse<String> response = put("/api/v1/tasks/" + TEST_TASK_ID + suffix, jsonTestTask);

        assertEquals(NOT_ACCEPTABLE, response.statusCode(), "wrong status code");
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "/"})
    public void shouldRespondInternalServerErrorWhenPutTaskAndException(String suffix) {
        mock.withUpdateTask(t -> {
            throw new RuntimeException();
        });

        HttpResponse<String> response = put("/api/v1/tasks/" + TEST_TASK_ID + suffix, jsonTestTask);

        assertEquals(INTERNAL_SERVER_ERROR, response.statusCode(), "wrong status code");
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "/"})
    public void shouldPassIdToTaskManagerWhenDeleteTask(String suffix) {
        final List<Long> expectedIds = List.of(TEST_TASK_ID);
        mock.withDeleteTask(id -> {
            // Do nothing
        });

        HttpResponse<String> response = delete("/api/v1/tasks/" + TEST_TASK_ID + suffix);

        assertAll("task deleted incorrectly",
                () -> assertEquals(expectedIds, mock.calls().deleteTask(), "id passed to deleteTask() incorrectly"),
                () -> assertEquals(OK, response.statusCode(), "wrong status code")
        );
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "/"})
    public void shouldRespondNotFoundWhenDeleteTaskAndNotFound(String suffix) {
        mock.withDeleteTask(id -> {
            throw new TaskNotFoundException("no task with id=" + TEST_TASK_ID);
        });

        HttpResponse<String> response = delete("/api/v1/tasks/" + TEST_TASK_ID + suffix);

        assertEquals(NOT_FOUND, response.statusCode(), "wrong status code");
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "/"})
    public void shouldRespondNotFoundWhenDeleteTaskAndWrongId(String suffix) {
        HttpResponse<String> response = delete("/api/v1/tasks/" + "TEST_TASK_ID" + suffix);

        assertEquals(NOT_FOUND, response.statusCode(), "wrong status code");
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "/"})
    public void shouldRespondInternalServerErrorWhenDeleteTaskAndException(String suffix) {
        mock.withDeleteTask(id -> {
            throw new RuntimeException();
        });

        HttpResponse<String> response = delete("/api/v1/tasks/" + TEST_TASK_ID + suffix);

        assertEquals(INTERNAL_SERVER_ERROR, response.statusCode(), "wrong status code");
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "/"})
    public void shouldRespondMethodNotAllowedWhenPostTask(String suffix) {
        HttpResponse<String> response = post("/api/v1/tasks/" + TEST_TASK_ID + suffix, jsonTestTask);

        assertEquals(METHOD_NOT_ALLOWED, response.statusCode(), "wrong status code");
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "/"})
    public void shouldPassEpicsFromTaskManagerWhenGetEpics(String suffix) {
        final List<Epic> epics = List.of(emptyEpic, testEpic, modifiedEpic);
        final String expectedBody = gson.toJson(epics);
        mock.withGetEpics(() -> epics);

        HttpResponse<String> response = get("/api/v1/epics" + suffix);

        assertAll("wrong call of get all epics",
                () -> assertEquals(1, mock.calls().getEpics(), "wrong number of calls to getEpics()"),
                () -> assertEquals(OK, response.statusCode(), "wrong status code"),
                () -> assertEquals(JSON, response.headers().firstValue("Content-Type").orElse(null),
                        "wrong content type"),
                () -> assertEquals(expectedBody, response.body(), "wrong body")
        );
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "/"})
    public void shouldPassEpicsFromTaskManagerWhenGetEpicsAndEmptyList(String suffix) {
        mock.withGetEpics(Collections::emptyList);

        HttpResponse<String> response = get("/api/v1/epics" + suffix);

        assertAll("wrong call of get all epics",
                () -> assertEquals(1, mock.calls().getEpics(), "wrong number of calls to getEpics()"),
                () -> assertEquals(OK, response.statusCode(), "wrong status code"),
                () -> assertEquals(JSON, response.headers().firstValue("Content-Type").orElse(null),
                        "wrong content type"),
                () -> assertEquals(jsonEmptyList, response.body(), "wrong body")
        );
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "/"})
    public void shouldRespondInternalServerErrorWhenGetEpicsAndException(String suffix) {
        mock.withGetEpics(() -> {
            throw new RuntimeException();
        });

        HttpResponse<String> response = get("/api/v1/epics" + suffix);

        assertEquals(INTERNAL_SERVER_ERROR, response.statusCode(), "wrong status code");
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "/"})
    public void shouldPassEpicToAndFromTaskManagerWhenPostEpics(String suffix) {
        final Epic epic = fromTestEpic().withId(null).build();
        final List<Epic> expectedEpics = List.of(epic);
        final String requestBody = gson.toJson(epic);
        mock.withCreateEpic(e -> modifiedEpic);

        HttpResponse<String> response = post("/api/v1/epics" + suffix, requestBody);

        assertAll("epic created incorrectly",
                () -> assertListEquals(expectedEpics, mock.calls().createEpic(),
                        "epic passed to createEpic() incorrectly"),
                () -> assertEquals(CREATED, response.statusCode(), "wrong status code"),
                () -> assertEquals(JSON, response.headers().firstValue("Content-Type").orElse(null),
                        "wrong content type"),
                () -> assertEquals(jsonModifiedEpic, response.body(), "wrong body")
        );
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "/"})
    public void shouldPassEpicToAndFromTaskManagerWhenPostEpicsAndEpicEmpty(String suffix) {
        final List<Epic> expectedEpics = List.of(emptyEpic);
        mock.withCreateEpic(e -> emptyEpic);

        HttpResponse<String> response = post("/api/v1/epics" + suffix, jsonEmptyEpic);

        assertAll("epic created incorrectly",
                () -> assertListEquals(expectedEpics, mock.calls().createEpic(),
                        "epic passed to createEpic() incorrectly"),
                () -> assertEquals(CREATED, response.statusCode(), "wrong status code"),
                () -> assertEquals(JSON, response.headers().firstValue("Content-Type").orElse(null),
                        "wrong content type"),
                () -> assertEquals(jsonEmptyEpic, response.body(), "wrong body")
        );
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "/"})
    public void shouldRespondBadRequestWhenPostEpicsAndNoBody(String suffix) {
        HttpResponse<String> response = post("/api/v1/epics" + suffix, "");

        assertEquals(BAD_REQUEST, response.statusCode(), "wrong status code");
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "/"})
    public void shouldRespondBadRequestWhenPostEpicsAndValidationException(String suffix) {
        mock.withCreateEpic(e -> {
            throw new ManagerValidationException("status cannot be null");
        });

        HttpResponse<String> response = post("/api/v1/epics" + suffix, jsonEmptyEpic);

        assertEquals(BAD_REQUEST, response.statusCode(), "wrong status code");
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "/"})
    public void shouldRespondInternalServerErrorWhenPostEpicsAndException(String suffix) {
        mock.withCreateEpic(t -> {
            throw new RuntimeException();
        });

        HttpResponse<String> response = post("/api/v1/epics" + suffix, jsonEmptyEpic);

        assertEquals(INTERNAL_SERVER_ERROR, response.statusCode(), "wrong status code");
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "/"})
    public void shouldRespondMethodNotAllowedWhenDeleteEpics(String suffix) {
        HttpResponse<String> response = delete("/api/v1/epics" + suffix);

        assertEquals(METHOD_NOT_ALLOWED, response.statusCode(), "wrong status code");
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "/"})
    public void shouldPassIdToAndEpicFromTaskManagerWhenGetEpicById(String suffix) {
        final List<Long> expectedIds = List.of(TEST_EPIC_ID);
        mock.withGetEpicById(id -> Optional.of(testEpic));

        HttpResponse<String> response = get("/api/v1/epics/" + TEST_EPIC_ID + suffix);

        assertAll("epic retrieved incorrectly",
                () -> assertEquals(expectedIds, mock.calls().getEpicById(), "id passed to getEpicById() incorrectly"),
                () -> assertEquals(OK, response.statusCode(), "wrong status code"),
                () -> assertEquals(JSON, response.headers().firstValue("Content-Type").orElse(null),
                        "wrong content type"),
                () -> assertEquals(jsonTestEpic, response.body(), "wrong body")
        );
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "/"})
    public void shouldPassIdToAndEpicFromTaskManagerWhenGetEpicByIdAndEpicEmpty(String suffix) {
        final Epic epic = fromEmptyEpic().withId(ANOTHER_TEST_ID).withStatus(TaskStatus.NEW).build();
        final String expectedBody = gson.toJson(epic);
        final List<Long> expectedIds = List.of(TEST_EPIC_ID);
        mock.withGetEpicById(id -> Optional.of(epic));

        HttpResponse<String> response = get("/api/v1/epics/" + TEST_EPIC_ID + suffix);

        assertAll("epic retrieved incorrectly",
                () -> assertEquals(expectedIds, mock.calls().getEpicById(), "id passed to getEpicById() incorrectly"),
                () -> assertEquals(OK, response.statusCode(), "wrong status code"),
                () -> assertEquals(JSON, response.headers().firstValue("Content-Type").orElse(null),
                        "wrong content type"),
                () -> assertEquals(expectedBody, response.body(), "wrong body")
        );
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "/"})
    public void shouldRespondNotFoundWhenGetEpicByIdAndNotFound(String suffix) {
        mock.withGetEpicById(id -> Optional.empty());

        HttpResponse<String> response = get("/api/v1/epics/" + TEST_EPIC_ID + suffix);

        assertEquals(NOT_FOUND, response.statusCode(), "wrong status code");
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "/"})
    public void shouldRespondNotFoundWhenGetEpicByIdAndWrongId(String suffix) {
        HttpResponse<String> response = get("/api/v1/epics/" + "TEST_EPIC_ID" + suffix);

        assertEquals(NOT_FOUND, response.statusCode(), "wrong status code");
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "/"})
    public void shouldRespondInternalServerErrorWhenGetEpicByIdAndException(String suffix) {
        mock.withGetEpicById(id -> {
            throw new RuntimeException();
        });

        HttpResponse<String> response = get("/api/v1/epics/" + TEST_EPIC_ID + suffix);

        assertEquals(INTERNAL_SERVER_ERROR, response.statusCode(), "wrong status code");
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "/"})
    public void shouldPassEpicToAndFromTaskManagerWhenPutEpic(String suffix) {
        final List<Epic> expectedEpics = List.of(testEpic);
        mock.withUpdateEpic(e -> modifiedEpic);

        HttpResponse<String> response = put("/api/v1/epics/" + TEST_EPIC_ID + suffix, jsonTestEpic);

        assertAll("epic updated incorrectly",
                () -> assertListEquals(expectedEpics, mock.calls().updateEpic(),
                        "epic passed to updateEpic() incorrectly"),
                () -> assertEquals(CREATED, response.statusCode(), "wrong status code"),
                () -> assertEquals(JSON, response.headers().firstValue("Content-Type").orElse(null),
                        "wrong content type"),
                () -> assertEquals(jsonModifiedEpic, response.body(), "wrong body")
        );
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "/"})
    public void shouldPassEpicToAndFromTaskManagerWhenPutEpicAndEpicEmpty(String suffix) {
        final Epic epic = fromEmptyEpic().withId(ANOTHER_TEST_ID).withStatus(TaskStatus.NEW).build();
        final List<Epic> expectedEpics = List.of(epic);
        final String requestBody = gson.toJson(epic);
        mock.withUpdateEpic(e -> emptyEpic);

        HttpResponse<String> response = put("/api/v1/epics/" + ANOTHER_TEST_ID + suffix, requestBody);

        assertAll("epic updated incorrectly",
                () -> assertListEquals(expectedEpics, mock.calls().updateEpic(),
                        "epic passed to updateEpic() incorrectly"),
                () -> assertEquals(CREATED, response.statusCode(), "wrong status code"),
                () -> assertEquals(JSON, response.headers().firstValue("Content-Type").orElse(null),
                        "wrong content type"),
                () -> assertEquals(jsonEmptyEpic, response.body(), "wrong body")
        );
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "/"})
    public void shouldRespondNotFoundWhenPutEpicAndWrongId(String suffix) {
        HttpResponse<String> response = put("/api/v1/epics/" + "TEST_EPIC_ID" + suffix, jsonTestEpic);

        assertEquals(NOT_FOUND, response.statusCode(), "wrong status code");
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "/"})
    public void shouldRespondBadRequestWhenPutEpicAndIdNotFromEndpoint(String suffix) {
        HttpResponse<String> response = put("/api/v1/epics/" + ANOTHER_TEST_ID + suffix, jsonTestEpic);

        assertEquals(BAD_REQUEST, response.statusCode(), "wrong status code");
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "/"})
    public void shouldRespondBadRequestWhenPutEpicAndNoBody(String suffix) {
        HttpResponse<String> response = put("/api/v1/epics/" + TEST_EPIC_ID + suffix, "");

        assertEquals(BAD_REQUEST, response.statusCode(), "wrong status code");
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "/"})
    public void shouldRespondBadRequestWhenPutEpicAndValidationException(String suffix) {
        mock.withUpdateEpic(e -> {
            throw new ManagerValidationException("status must be null");
        });

        HttpResponse<String> response = put("/api/v1/epics/" + TEST_EPIC_ID + suffix, jsonTestEpic);

        assertEquals(BAD_REQUEST, response.statusCode(), "wrong status code");
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "/"})
    public void shouldRespondInternalServerErrorWhenPutEpicAndException(String suffix) {
        mock.withUpdateEpic(e -> {
            throw new RuntimeException();
        });

        HttpResponse<String> response = put("/api/v1/epics/" + TEST_EPIC_ID + suffix, jsonTestEpic);

        assertEquals(INTERNAL_SERVER_ERROR, response.statusCode(), "wrong status code");
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "/"})
    public void shouldPassIdToTaskManagerWhenDeleteEpic(String suffix) {
        final List<Long> expectedIds = List.of(TEST_EPIC_ID);
        mock.withDeleteEpic(id -> {
            // Do nothing
        });

        HttpResponse<String> response = delete("/api/v1/epics/" + TEST_EPIC_ID + suffix);

        assertAll("epic deleted incorrectly",
                () -> assertEquals(expectedIds, mock.calls().deleteEpic(), "id passed to deleteEpic() incorrectly"),
                () -> assertEquals(OK, response.statusCode(), "wrong status code")
        );
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "/"})
    public void shouldRespondNotFoundWhenDeleteEpicAndNotFound(String suffix) {
        mock.withDeleteEpic(id -> {
            throw new TaskNotFoundException("no epic with id=" + TEST_EPIC_ID);
        });

        HttpResponse<String> response = delete("/api/v1/epics/" + TEST_EPIC_ID + suffix);

        assertEquals(NOT_FOUND, response.statusCode(), "wrong status code");
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "/"})
    public void shouldRespondNotFoundWhenDeleteEpicAndWrongId(String suffix) {
        HttpResponse<String> response = delete("/api/v1/epics/" + "TEST_EPIC_ID" + suffix);

        assertEquals(NOT_FOUND, response.statusCode(), "wrong status code");
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "/"})
    public void shouldRespondInternalServerErrorWhenDeleteEpicAndException(String suffix) {
        mock.withDeleteEpic(id -> {
            throw new RuntimeException();
        });

        HttpResponse<String> response = delete("/api/v1/epics/" + TEST_EPIC_ID + suffix);

        assertEquals(INTERNAL_SERVER_ERROR, response.statusCode(), "wrong status code");
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "/"})
    public void shouldRespondMethodNotAllowedWhenPostEpic(String suffix) {
        HttpResponse<String> response = post("/api/v1/epics/" + TEST_EPIC_ID + suffix, jsonTestEpic);

        assertEquals(METHOD_NOT_ALLOWED, response.statusCode(), "wrong status code");
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "/"})
    public void shouldPassSubtasksFromTaskManagerWhenGetSubtasks(String suffix) {
        final List<Subtask> subtasks = List.of(emptySubtask, testSubtask, modifiedSubtask);
        final String expectedBody = gson.toJson(subtasks);
        mock.withGetSubtasks(() -> subtasks);

        HttpResponse<String> response = get("/api/v1/subtasks" + suffix);

        assertAll("wrong call of get all subtasks",
                () -> assertEquals(1, mock.calls().getSubtasks(), "wrong number of calls to getSubtasks()"),
                () -> assertEquals(OK, response.statusCode(), "wrong status code"),
                () -> assertEquals(JSON, response.headers().firstValue("Content-Type").orElse(null),
                        "wrong content type"),
                () -> assertEquals(expectedBody, response.body(), "wrong body")
        );
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "/"})
    public void shouldPassSubtasksFromTaskManagerWhenGetSubtasksAndEmptyList(String suffix) {
        mock.withGetSubtasks(Collections::emptyList);

        HttpResponse<String> response = get("/api/v1/subtasks" + suffix);

        assertAll("wrong call of get all subtasks",
                () -> assertEquals(1, mock.calls().getSubtasks(), "wrong number of calls to getSubtasks()"),
                () -> assertEquals(OK, response.statusCode(), "wrong status code"),
                () -> assertEquals(JSON, response.headers().firstValue("Content-Type").orElse(null),
                        "wrong content type"),
                () -> assertEquals(jsonEmptyList, response.body(), "wrong body")
        );
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "/"})
    public void shouldRespondInternalServerErrorWhenGetSubtasksAndException(String suffix) {
        mock.withGetSubtasks(() -> {
            throw new RuntimeException();
        });

        HttpResponse<String> response = get("/api/v1/subtasks" + suffix);

        assertEquals(INTERNAL_SERVER_ERROR, response.statusCode(), "wrong status code");
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "/"})
    public void shouldPassSubtaskToAndFromTaskManagerWhenPostSubtasks(String suffix) {
        final Subtask subtask = fromTestSubtask().withId(null).build();
        final List<Subtask> expectedSubtasks = List.of(subtask);
        final String requestBody = gson.toJson(subtask);
        mock.withCreateSubtask(t -> modifiedSubtask);

        HttpResponse<String> response = post("/api/v1/subtasks" + suffix, requestBody);

        assertAll("subtask created incorrectly",
                () -> assertListEquals(expectedSubtasks, mock.calls().createSubtask(),
                        "subtask passed to createSubtask() incorrectly"),
                () -> assertEquals(CREATED, response.statusCode(), "wrong status code"),
                () -> assertEquals(JSON, response.headers().firstValue("Content-Type").orElse(null),
                        "wrong content type"),
                () -> assertEquals(jsonModifiedSubtask, response.body(), "wrong body")
        );
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "/"})
    public void shouldPassSubtaskToAndFromTaskManagerWhenPostSubtasksAndSubtaskEmpty(String suffix) {
        final List<Subtask> expectedSubtasks = List.of(emptySubtask);
        mock.withCreateSubtask(t -> emptySubtask);

        HttpResponse<String> response = post("/api/v1/subtasks" + suffix, jsonEmptySubtask);

        assertAll("subtask created incorrectly",
                () -> assertListEquals(expectedSubtasks, mock.calls().createSubtask(),
                        "subtask passed to createSubtask() incorrectly"),
                () -> assertEquals(CREATED, response.statusCode(), "wrong status code"),
                () -> assertEquals(JSON, response.headers().firstValue("Content-Type").orElse(null),
                        "wrong content type"),
                () -> assertEquals(jsonEmptySubtask, response.body(), "wrong body")
        );
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "/"})
    public void shouldRespondBadRequestWhenPostSubtasksAndNoBody(String suffix) {
        HttpResponse<String> response = post("/api/v1/subtasks" + suffix, "");

        assertEquals(BAD_REQUEST, response.statusCode(), "wrong status code");
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "/"})
    public void shouldRespondBadRequestWhenPostSubtasksAndValidationException(String suffix) {
        mock.withCreateSubtask(t -> {
            throw new ManagerValidationException("status cannot be null");
        });

        HttpResponse<String> response = post("/api/v1/subtasks" + suffix, jsonEmptySubtask);

        assertEquals(BAD_REQUEST, response.statusCode(), "wrong status code");
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "/"})
    public void shouldRespondNotAcceptableWhenPostSubtasksAndTaskOverlapException(String suffix) {
        mock.withCreateSubtask(t -> {
            throw new TaskOverlapException("conflict with another task for time slot");
        });

        HttpResponse<String> response = post("/api/v1/subtasks" + suffix, jsonEmptySubtask);

        assertEquals(NOT_ACCEPTABLE, response.statusCode(), "wrong status code");
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "/"})
    public void shouldRespondInternalServerErrorWhenPostSubtasksAndException(String suffix) {
        mock.withCreateSubtask(t -> {
            throw new RuntimeException();
        });

        HttpResponse<String> response = post("/api/v1/subtasks" + suffix, jsonEmptySubtask);

        assertEquals(INTERNAL_SERVER_ERROR, response.statusCode(), "wrong status code");
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "/"})
    public void shouldRespondMethodNotAllowedWhenDeleteSubtasks(String suffix) {
        HttpResponse<String> response = delete("/api/v1/subtasks" + suffix);

        assertEquals(METHOD_NOT_ALLOWED, response.statusCode(), "wrong status code");
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "/"})
    public void shouldPassIdToAndSubtaskFromTaskManagerWhenGetSubtaskById(String suffix) {
        final List<Long> expectedIds = List.of(TEST_SUBTASK_ID);
        mock.withGetSubtaskById(id -> Optional.of(testSubtask));

        HttpResponse<String> response = get("/api/v1/subtasks/" + TEST_SUBTASK_ID + suffix);

        assertAll("subtask retrieved incorrectly",
                () -> assertEquals(expectedIds, mock.calls().getSubtaskById(),
                        "id passed to getSubtaskById() incorrectly"),
                () -> assertEquals(OK, response.statusCode(), "wrong status code"),
                () -> assertEquals(JSON, response.headers().firstValue("Content-Type").orElse(null),
                        "wrong content type"),
                () -> assertEquals(jsonTestSubtask, response.body(), "wrong body")
        );
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "/"})
    public void shouldPassIdToAndSubtaskFromTaskManagerWhenGetSubtaskByIdAndSubtaskEmpty(String suffix) {
        final Subtask subtask = fromEmptySubtask().withId(ANOTHER_TEST_ID).withStatus(TaskStatus.NEW).build();
        final String expectedBody = gson.toJson(subtask);
        final List<Long> expectedIds = List.of(TEST_SUBTASK_ID);
        mock.withGetSubtaskById(id -> Optional.of(subtask));

        HttpResponse<String> response = get("/api/v1/subtasks/" + TEST_SUBTASK_ID + suffix);

        assertAll("subtask retrieved incorrectly",
                () -> assertEquals(expectedIds, mock.calls().getSubtaskById(),
                        "id passed to getSubtaskById() incorrectly"),
                () -> assertEquals(OK, response.statusCode(), "wrong status code"),
                () -> assertEquals(JSON, response.headers().firstValue("Content-Type").orElse(null),
                        "wrong content type"),
                () -> assertEquals(expectedBody, response.body(), "wrong body")
        );
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "/"})
    public void shouldRespondNotFoundWhenGetSubtaskByIdAndNotFound(String suffix) {
        mock.withGetSubtaskById(id -> Optional.empty());

        HttpResponse<String> response = get("/api/v1/subtasks/" + TEST_SUBTASK_ID + suffix);

        assertEquals(NOT_FOUND, response.statusCode(), "wrong status code");
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "/"})
    public void shouldRespondNotFoundWhenGetSubtaskByIdAndWrongId(String suffix) {
        HttpResponse<String> response = get("/api/v1/subtasks/" + "TEST_SUBTASK_ID" + suffix);

        assertEquals(NOT_FOUND, response.statusCode(), "wrong status code");
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "/"})
    public void shouldRespondInternalServerErrorWhenGetSubtaskByIdAndException(String suffix) {
        mock.withGetSubtaskById(id -> {
            throw new RuntimeException();
        });

        HttpResponse<String> response = get("/api/v1/subtasks/" + TEST_SUBTASK_ID + suffix);

        assertEquals(INTERNAL_SERVER_ERROR, response.statusCode(), "wrong status code");
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "/"})
    public void shouldPassSubtaskToAndFromTaskManagerWhenPutSubtask(String suffix) {
        final List<Subtask> expectedSubtasks = List.of(testSubtask);
        mock.withUpdateSubtask(s -> modifiedSubtask);

        HttpResponse<String> response = put("/api/v1/subtasks/" + TEST_SUBTASK_ID + suffix, jsonTestSubtask);

        assertAll("subtask updated incorrectly",
                () -> assertListEquals(expectedSubtasks, mock.calls().updateSubtask(),
                        "subtask passed to updateSubtask() incorrectly"),
                () -> assertEquals(CREATED, response.statusCode(), "wrong status code"),
                () -> assertEquals(JSON, response.headers().firstValue("Content-Type").orElse(null),
                        "wrong content type"),
                () -> assertEquals(jsonModifiedSubtask, response.body(), "wrong body")
        );
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "/"})
    public void shouldPassSubtaskToAndFromTaskManagerWhenPutSubtaskAndSubtaskEmpty(String suffix) {
        final Subtask subtask = fromEmptySubtask().withId(ANOTHER_TEST_ID).withStatus(TaskStatus.NEW).build();
        final List<Subtask> expectedSubtasks = List.of(subtask);
        final String requestBody = gson.toJson(subtask);
        mock.withUpdateSubtask(s -> emptySubtask);

        HttpResponse<String> response = put("/api/v1/subtasks/" + ANOTHER_TEST_ID + suffix, requestBody);

        assertAll("subtask updated incorrectly",
                () -> assertListEquals(expectedSubtasks, mock.calls().updateSubtask(),
                        "subtask passed to updateSubtask() incorrectly"),
                () -> assertEquals(CREATED, response.statusCode(), "wrong status code"),
                () -> assertEquals(JSON, response.headers().firstValue("Content-Type").orElse(null),
                        "wrong content type"),
                () -> assertEquals(jsonEmptySubtask, response.body(), "wrong body")
        );
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "/"})
    public void shouldRespondNotFoundWhenPutSubtaskAndWrongId(String suffix) {
        HttpResponse<String> response = put("/api/v1/subtasks/" + "TEST_SUBTASK_ID" + suffix, jsonTestSubtask);

        assertEquals(NOT_FOUND, response.statusCode(), "wrong status code");
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "/"})
    public void shouldRespondBadRequestWhenPutSubtaskAndIdNotFromEndpoint(String suffix) {
        HttpResponse<String> response = put("/api/v1/subtasks/" + ANOTHER_TEST_ID + suffix, jsonTestSubtask);

        assertEquals(BAD_REQUEST, response.statusCode(), "wrong status code");
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "/"})
    public void shouldRespondBadRequestWhenPutSubtaskAndNoBody(String suffix) {
        HttpResponse<String> response = put("/api/v1/subtasks/" + TEST_SUBTASK_ID + suffix, "");

        assertEquals(BAD_REQUEST, response.statusCode(), "wrong status code");
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "/"})
    public void shouldRespondBadRequestWhenPutSubtaskAndValidationException(String suffix) {
        mock.withUpdateSubtask(s -> {
            throw new ManagerValidationException("status cannot be null");
        });

        HttpResponse<String> response = put("/api/v1/subtasks/" + TEST_SUBTASK_ID + suffix, jsonTestSubtask);

        assertEquals(BAD_REQUEST, response.statusCode(), "wrong status code");
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "/"})
    public void shouldRespondNotAcceptableWhenPutSubtaskAndTaskOverlapException(String suffix) {
        mock.withUpdateSubtask(s -> {
            throw new TaskOverlapException("conflict with another task for time slot");
        });

        HttpResponse<String> response = put("/api/v1/subtasks/" + TEST_SUBTASK_ID + suffix, jsonTestSubtask);

        assertEquals(NOT_ACCEPTABLE, response.statusCode(), "wrong status code");
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "/"})
    public void shouldRespondInternalServerErrorWhenPutSubtaskAndException(String suffix) {
        mock.withUpdateSubtask(s -> {
            throw new RuntimeException();
        });

        HttpResponse<String> response = put("/api/v1/subtasks/" + TEST_SUBTASK_ID + suffix, jsonTestSubtask);

        assertEquals(INTERNAL_SERVER_ERROR, response.statusCode(), "wrong status code");
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "/"})
    public void shouldPassIdToTaskManagerWhenDeleteSubtask(String suffix) {
        final List<Long> expectedIds = List.of(TEST_SUBTASK_ID);
        mock.withDeleteSubtask(id -> {
            // Do nothing
        });

        HttpResponse<String> response = delete("/api/v1/subtasks/" + TEST_SUBTASK_ID + suffix);

        assertAll("subtask deleted incorrectly",
                () -> assertEquals(expectedIds, mock.calls().deleteSubtask(),
                        "id passed to deleteSubtask() incorrectly"),
                () -> assertEquals(OK, response.statusCode(), "wrong status code")
        );
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "/"})
    public void shouldRespondNotFoundWhenDeleteSubtaskAndNotFound(String suffix) {
        mock.withDeleteSubtask(id -> {
            throw new TaskNotFoundException("no subtask with id=" + TEST_SUBTASK_ID);
        });

        HttpResponse<String> response = delete("/api/v1/subtasks/" + TEST_SUBTASK_ID + suffix);

        assertEquals(NOT_FOUND, response.statusCode(), "wrong status code");
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "/"})
    public void shouldRespondNotFoundWhenDeleteSubtaskAndWrongId(String suffix) {
        HttpResponse<String> response = delete("/api/v1/subtasks/" + "TEST_SUBTASK_ID" + suffix);

        assertEquals(NOT_FOUND, response.statusCode(), "wrong status code");
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "/"})
    public void shouldRespondInternalServerErrorWhenDeleteSubtaskAndException(String suffix) {
        mock.withDeleteSubtask(id -> {
            throw new RuntimeException();
        });

        HttpResponse<String> response = delete("/api/v1/subtasks/" + TEST_SUBTASK_ID + suffix);

        assertEquals(INTERNAL_SERVER_ERROR, response.statusCode(), "wrong status code");
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "/"})
    public void shouldRespondMethodNotAllowedWhenPostSubtask(String suffix) {
        HttpResponse<String> response = post("/api/v1/subtasks/" + TEST_SUBTASK_ID + suffix, jsonTestSubtask);

        assertEquals(METHOD_NOT_ALLOWED, response.statusCode(), "wrong status code");
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "/"})
    public void shouldPassIdToAndSubtasksFromTaskManagerWhenGetEpicSubtasks(String suffix) {
        final List<Subtask> subtasks = List.of(emptySubtask, testSubtask, modifiedSubtask);
        final String expectedBody = gson.toJson(subtasks);
        final List<Long> expectedIds = List.of(TEST_EPIC_ID);
        mock.withGetEpicSubtasks(id -> subtasks);

        HttpResponse<String> response = get("/api/v1/epics/" + TEST_EPIC_ID + "/subtasks" + suffix);

        assertAll("epic subtasks retrieved incorrectly",
                () -> assertEquals(expectedIds, mock.calls().getEpicSubtasks(),
                        "id passed to getEpicSubtasks() incorrectly"),
                () -> assertEquals(OK, response.statusCode(), "wrong status code"),
                () -> assertEquals(JSON, response.headers().firstValue("Content-Type").orElse(null),
                        "wrong content type"),
                () -> assertEquals(expectedBody, response.body(), "wrong body")
        );
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "/"})
    public void shouldPassIdToAndSubtasksFromTaskManagerWhenGetEpicSubtasksAndEmptyList(String suffix) {
        final List<Subtask> subtasks = Collections.emptyList();
        final String expectedBody = gson.toJson(subtasks);
        final List<Long> expectedIds = List.of(TEST_EPIC_ID);
        mock.withGetEpicSubtasks(id -> Collections.emptyList());

        HttpResponse<String> response = get("/api/v1/epics/" + TEST_EPIC_ID + "/subtasks" + suffix);

        assertAll("epic subtasks retrieved incorrectly",
                () -> assertEquals(expectedIds, mock.calls().getEpicSubtasks(),
                        "id passed to getEpicSubtasks() incorrectly"),
                () -> assertEquals(OK, response.statusCode(), "wrong status code"),
                () -> assertEquals(JSON, response.headers().firstValue("Content-Type").orElse(null),
                        "wrong content type"),
                () -> assertEquals(expectedBody, response.body(), "wrong body")
        );
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "/"})
    public void shouldRespondNotFoundWhenGetEpicSubtasksAndNotFound(String suffix) {
        mock.withGetEpicSubtasks(id -> {
            throw new TaskNotFoundException("no epic with is=" + TEST_EPIC_ID);
        });

        HttpResponse<String> response = get("/api/v1/epics/" + TEST_EPIC_ID + "/subtasks" + suffix);

        assertEquals(NOT_FOUND, response.statusCode(), "wrong status code");
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "/"})
    public void shouldRespondNotFoundWhenGetEpicSubtasksAndWrongId(String suffix) {
        HttpResponse<String> response = get("/api/v1/epics/" + "TEST_EPIC_ID" + "/subtasks" + suffix);

        assertEquals(NOT_FOUND, response.statusCode(), "wrong status code");
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "/"})
    public void shouldRespondInternalServerErrorWhenGetEpicSubtasksAndException(String suffix) {
        mock.withGetEpicSubtasks(id -> {
            throw new RuntimeException();
        });

        HttpResponse<String> response = get("/api/v1/epics/" + TEST_EPIC_ID + "/subtasks" + suffix);

        assertEquals(INTERNAL_SERVER_ERROR, response.statusCode(), "wrong status code");
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "/"})
    public void shouldRespondMethodNotAllowedWhenDeleteEpicSubtasks(String suffix) {
        HttpResponse<String> response = delete("/api/v1/epics/" + TEST_EPIC_ID + "/subtasks" + suffix);

        assertEquals(METHOD_NOT_ALLOWED, response.statusCode(), "wrong status code");
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "/"})
    public void shouldPassHistoryFromTaskManagerWhenGetHistory(String suffix) {
        final List<Task> history = List.of(emptyTask, testTask, modifiedTask, emptyEpic, testEpic, modifiedEpic,
                emptySubtask, testSubtask, modifiedSubtask);
        final String expectedBody = gson.toJson(history);
        mock.withGetHistory(() -> history);

        HttpResponse<String> response = get("/api/v1/history" + suffix);

        assertAll("wrong call of get history",
                () -> assertEquals(1, mock.calls().getHistory(), "wrong number of calls to getHistory()"),
                () -> assertEquals(OK, response.statusCode(), "wrong status code"),
                () -> assertEquals(JSON, response.headers().firstValue("Content-Type").orElse(null),
                        "wrong content type"),
                () -> assertEquals(expectedBody, response.body(), "wrong body")
        );
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "/"})
    public void shouldPassHistoryFromTaskManagerWhenGetHistoryAndEmptyList(String suffix) {
        mock.withGetHistory(Collections::emptyList);

        HttpResponse<String> response = get("/api/v1/history" + suffix);

        assertAll("wrong call of get history",
                () -> assertEquals(1, mock.calls().getHistory(), "wrong number of calls to getHistory()"),
                () -> assertEquals(OK, response.statusCode(), "wrong status code"),
                () -> assertEquals(JSON, response.headers().firstValue("Content-Type").orElse(null),
                        "wrong content type"),
                () -> assertEquals(jsonEmptyList, response.body(), "wrong body")
        );
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "/"})
    public void shouldRespondInternalServerErrorWhenGetHistoryAndException(String suffix) {
        mock.withGetHistory(() -> {
            throw new RuntimeException();
        });

        HttpResponse<String> response = get("/api/v1/history" + suffix);

        assertEquals(INTERNAL_SERVER_ERROR, response.statusCode(), "wrong status code");
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "/"})
    public void shouldRespondMethodNotAllowedWhenDeleteHistory(String suffix) {
        HttpResponse<String> response = delete("/api/v1/history" + suffix);

        assertEquals(METHOD_NOT_ALLOWED, response.statusCode(), "wrong status code");
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "/"})
    public void shouldPassPrioritizedFromTaskManagerWhenGetPrioritized(String suffix) {
        final List<Task> prioritized = List.of(emptyTask, testTask, modifiedTask, emptySubtask, testSubtask,
                modifiedSubtask);
        final String expectedBody = gson.toJson(prioritized);
        mock.withGetPrioritizedTasks(() -> prioritized);

        HttpResponse<String> response = get("/api/v1/prioritized" + suffix);

        assertAll("wrong call of get prioritized",
                () -> assertEquals(1, mock.calls().getPrioritizedTasks(),
                        "wrong number of calls to getPrioritizedTasks()"),
                () -> assertEquals(OK, response.statusCode(), "wrong status code"),
                () -> assertEquals(JSON, response.headers().firstValue("Content-Type").orElse(null),
                        "wrong content type"),
                () -> assertEquals(expectedBody, response.body(), "wrong body")
        );
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "/"})
    public void shouldPassPrioritizedFromTaskManagerWhenGetPrioritizedAndEmptyList(String suffix) {
        mock.withGetPrioritizedTasks(Collections::emptyList);

        HttpResponse<String> response = get("/api/v1/prioritized" + suffix);

        assertAll("wrong call of get prioritized",
                () -> assertEquals(1, mock.calls().getPrioritizedTasks(),
                        "wrong number of calls to getPrioritizedTasks()"),
                () -> assertEquals(OK, response.statusCode(), "wrong status code"),
                () -> assertEquals(JSON, response.headers().firstValue("Content-Type").orElse(null),
                        "wrong content type"),
                () -> assertEquals(jsonEmptyList, response.body(), "wrong body")
        );
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "/"})
    public void shouldRespondInternalServerErrorWhenGetPrioritizedAndException(String suffix) {
        mock.withGetPrioritizedTasks(() -> {
            throw new RuntimeException();
        });

        HttpResponse<String> response = get("/api/v1/prioritized" + suffix);

        assertEquals(INTERNAL_SERVER_ERROR, response.statusCode(), "wrong status code");
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "/"})
    public void shouldRespondMethodNotAllowedWhenDeletePrioritized(String suffix) {
        HttpResponse<String> response = delete("/api/v1/prioritized" + suffix);

        assertEquals(METHOD_NOT_ALLOWED, response.statusCode(), "wrong status code");
    }

    private HttpResponse<String> get(String url) {
        List<HttpResponse<String>> responses = new ArrayList<>();
        URI uri = URI.create(HOSTNAME + ":" + PORT + url);
        final HttpRequest request = HttpRequest.newBuilder()
                .GET()
                .uri(uri)
                .version(HttpClient.Version.HTTP_1_1)
                .header("Accept", JSON)
                .build();
        assertDoesNotThrow(() ->
                responses.add(client.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8))));
        return responses.getFirst();
    }

    private HttpResponse<String> post(String url, String body) {
        List<HttpResponse<String>> responses = new ArrayList<>();
        URI uri = URI.create(HOSTNAME + ":" + PORT + url);
        final HttpRequest request = HttpRequest.newBuilder()
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .uri(uri)
                .version(HttpClient.Version.HTTP_1_1)
                .header("Accept", JSON)
                .header("Content-Type", JSON)
                .build();
        assertDoesNotThrow(() ->
                responses.add(client.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8))));
        return responses.getFirst();
    }

    private HttpResponse<String> put(String url, String body) {
        List<HttpResponse<String>> responses = new ArrayList<>();
        URI uri = URI.create(HOSTNAME + ":" + PORT + url);
        final HttpRequest request = HttpRequest.newBuilder()
                .PUT(HttpRequest.BodyPublishers.ofString(body))
                .uri(uri)
                .version(HttpClient.Version.HTTP_1_1)
                .header("Accept", JSON)
                .header("Content-Type", JSON)
                .build();
        assertDoesNotThrow(() ->
                responses.add(client.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8))));
        return responses.getFirst();
    }

    private HttpResponse<String> delete(String url) {
        List<HttpResponse<String>> responses = new ArrayList<>();
        URI uri = URI.create(HOSTNAME + ":" + PORT + url);
        final HttpRequest request = HttpRequest.newBuilder()
                .DELETE()
                .uri(uri)
                .version(HttpClient.Version.HTTP_1_1)
                .build();
        assertDoesNotThrow(() ->
                responses.add(client.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8))));
        return responses.getFirst();
    }
}