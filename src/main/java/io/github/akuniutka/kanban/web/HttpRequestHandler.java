package io.github.akuniutka.kanban.web;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import io.github.akuniutka.kanban.exception.ManagerValidationException;
import io.github.akuniutka.kanban.exception.TaskNotFoundException;
import io.github.akuniutka.kanban.exception.TaskOverlapException;
import io.github.akuniutka.kanban.model.Task;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;

public class HttpRequestHandler<T extends Task> implements HttpHandler {
    private static final int OK = 200;
    private static final int CREATED = 201;
    private static final int BAD_REQUEST = 400;
    private static final int NOT_FOUND = 404;
    private static final int METHOD_NOT_ALLOWED = 405;
    private static final int NOT_ACCEPTABLE = 406;
    private static final int INTERNAL_SERVER_ERROR = 500;
    private static final Gson gson = new GsonBuilder()
            .registerTypeAdapter(Duration.class, new DurationAdapter())
            .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter())
            .create();
    private final String path;
    private final Class<T> elementType;
    private final Supplier<List<T>> getAll;
    private final Function<Long, Optional<T>> getById;
    private final UnaryOperator<T> create;
    private final UnaryOperator<T> update;
    private final Consumer<Long> delete;
    private final Map<String, Function<Long, Object>> elementAspects;
    private final boolean isCollectionHandlerAvailable;
    private final boolean isElementHandlerAvailable;

    public HttpRequestHandler(String path, Class<T> elementType, Supplier<List<T>> getAll,
            Function<Long, Optional<T>> getById, UnaryOperator<T> create, UnaryOperator<T> update,
            Consumer<Long> delete, Map<String, Function<Long, Object>> elementAspects) {
        this.path = path;
        this.elementType = elementType;
        this.getAll = getAll;
        this.getById = getById;
        this.create = create;
        this.update = update;
        this.delete = delete;
        this.elementAspects = elementAspects;
        this.isCollectionHandlerAvailable = (getAll != null) || (create != null);
        this.isElementHandlerAvailable = (getById != null) || (update != null) || (delete != null);
    }

    public HttpRequestHandler(String path, Class<T> elementType, Supplier<List<T>> getAll,
            Function<Long, Optional<T>> getById, UnaryOperator<T> create, UnaryOperator<T> update,
            Consumer<Long> delete) {
        this(path, elementType, getAll, getById, create, update, delete, null);
    }

    public HttpRequestHandler(String path, Class<T> elementType, Supplier<List<T>> getAll) {
        this(path, elementType, getAll, null, null, null, null, null);
    }

    public String getPath() {
        return path;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        try {
            final String requestedPath = exchange.getRequestURI().getPath();
            final String method = exchange.getRequestMethod();
            System.out.println(">> " + method + " " + requestedPath);
            String elementAspect = elementAspects == null ? null : elementAspects.keySet().stream()
                    .filter(suffix -> requestedPath.matches(path + "/.+" + suffix + "/?"))
                    .findFirst().orElse(null);
            if (elementAspect != null) {
                final long id = extractId(requestedPath.substring(path.length() + 1,
                        requestedPath.lastIndexOf(elementAspect)));
                handleElementAspectRequest(exchange, method, id, elementAspect);
            } else if (isElementHandlerAvailable && requestedPath.matches(path + "/.+")) {
                final long id = extractId(requestedPath.substring(path.length() + 1));
                handleElementRequest(exchange, method, id);
            } else if (isCollectionHandlerAvailable && requestedPath.matches(this.path + "/?")) {
                handleCollectionRequest(exchange, method);
            } else {
                respond(exchange, NOT_FOUND);
            }
        } catch (TaskNotFoundException exception) {
            logAndRespond(exception, exchange, NOT_FOUND);
        } catch (TaskOverlapException exception) {
            logAndRespond(exception, exchange, NOT_ACCEPTABLE);
        } catch (JsonSyntaxException | ManagerValidationException exception) {
            logAndRespond(exception, exchange, BAD_REQUEST);
        } catch (Exception exception) {
            logAndRespond(exception, exchange, INTERNAL_SERVER_ERROR);
        } finally {
            exchange.close();
        }
    }

    protected void handleElementAspectRequest(HttpExchange exchange, String method, long id, String elementAspect)
            throws IOException {
        if ("GET".equals(method)) {
            respond(exchange, OK, elementAspects.get(elementAspect).apply(id));
        } else {
            respond(exchange, METHOD_NOT_ALLOWED);
        }
    }

    protected void handleElementRequest(HttpExchange exchange, String method, long id) throws IOException {
        if (getById != null && "GET".equals(method)) {
            final Optional<T> element = getById.apply(id);
            if (element.isEmpty()) {
                respond(exchange, NOT_FOUND);
            } else {
                respond(exchange, OK, element.get());
            }
        } else if (update != null && "PUT".equals(method)) {
            final T element = getBody(exchange);
            element.setId(id);
            respond(exchange, CREATED, update.apply(element));
        } else if (delete != null && "DELETE".equals(method)) {
            delete.accept(id);
            respond(exchange, OK);
        } else {
            respond(exchange, METHOD_NOT_ALLOWED);
        }
    }

    protected void handleCollectionRequest(HttpExchange exchange, String method) throws IOException {
        if (getAll != null && "GET".equals(method)) {
            respond(exchange, OK, getAll.get());
        } else if (create != null && "POST".equals(method)) {
            final T element = getBody(exchange);
            respond(exchange, CREATED, create.apply(element));
        } else {
            respond(exchange, METHOD_NOT_ALLOWED);
        }
    }

    protected long extractId(String idString) {
        if (!idString.matches("\\d+")) {
            throw new TaskNotFoundException("no element with id=" + idString);
        }
        try {
            return Long.parseLong(idString);
        } catch (NumberFormatException exception) {
            throw new TaskNotFoundException("no element with id=" + idString);
        }
    }

    protected T getBody(HttpExchange exchange) throws IOException {
        final String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
        System.out.println(body);
        if (body.isBlank()) {
            throw new ManagerValidationException("empty request body");
        }
        return gson.fromJson(body, elementType);
    }

    protected void respond(HttpExchange exchange, int code) throws IOException {
        exchange.sendResponseHeaders(code, -1);
        System.out.println("<< " + code);
    }

    protected void respond(HttpExchange exchange, int code, Object data) throws IOException {
        exchange.getResponseHeaders().set("Content-Type", "application/json");
        final byte[] body = gson.toJson(data).getBytes(StandardCharsets.UTF_8);
        exchange.sendResponseHeaders(code, body.length);
        try (OutputStream out = exchange.getResponseBody()) {
            out.write(body);
        }
        System.out.printf("<< %d (%d bytes)%n", code, body.length);
    }

    protected void logAndRespond(Exception exception, HttpExchange exchange, int code) throws IOException {
        System.out.println(exception.getMessage());
        respond(exchange, code);
    }
}
