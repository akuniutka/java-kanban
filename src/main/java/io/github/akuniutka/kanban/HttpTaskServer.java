package io.github.akuniutka.kanban;

import com.sun.net.httpserver.HttpServer;
import io.github.akuniutka.kanban.model.Epic;
import io.github.akuniutka.kanban.model.Subtask;
import io.github.akuniutka.kanban.model.Task;
import io.github.akuniutka.kanban.service.Managers;
import io.github.akuniutka.kanban.service.TaskManager;
import io.github.akuniutka.kanban.web.HttpRequestHandler;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.Map;

public class HttpTaskServer {
    private static final int PORT = 8080;
    private final TaskManager taskManager;
    private final HttpServer httpServer;

    public HttpTaskServer(TaskManager taskManager) throws IOException {
        this.taskManager = taskManager;
        this.httpServer = HttpServer.create(new InetSocketAddress(PORT), 0);
        loadContext();
    }

    public static void main(String[] args) throws IOException {
        System.out.println("Поехали!");
        HttpTaskServer httpTaskServer = new HttpTaskServer(Managers.getDefault());
        httpTaskServer.start();
    }

    public void start() {
        httpServer.start();
        System.out.println("HTTP server started at port: " + PORT);
    }

    public void stop() {
        httpServer.stop(0);
        System.out.println("HTTP server stopped.");
    }

    protected void loadContext() {
        addHandler(new HttpRequestHandler<>("/api/v1/tasks", Task.class,
                taskManager::getTasks,
                taskManager::getTaskById,
                taskManager::createTask,
                taskManager::updateTask,
                taskManager::deleteTask));
        addHandler(new HttpRequestHandler<>("/api/v1/epics", Epic.class,
                taskManager::getEpics,
                taskManager::getEpicById,
                taskManager::createEpic,
                taskManager::updateEpic,
                taskManager::deleteEpic,
                Map.of("/subtasks", taskManager::getEpicSubtasks)));
        addHandler(new HttpRequestHandler<>("/api/v1/subtasks", Subtask.class,
                taskManager::getSubtasks,
                taskManager::getSubtaskById,
                taskManager::createSubtask,
                taskManager::updateSubtask,
                taskManager::deleteSubtask));
        addHandler(new HttpRequestHandler<>("/api/v1/getHistory", Task.class,
                taskManager::getHistory));
        addHandler(new HttpRequestHandler<>("/api/v1/getPrioritizedTasks", Task.class,
                taskManager::getPrioritizedTasks));
    }

    protected void addHandler(HttpRequestHandler<? extends Task> handler) {
        httpServer.createContext(handler.getPath(), handler);
    }
}
