package io.github.akuniutka.kanban.service;

import io.github.akuniutka.kanban.exception.*;
import io.github.akuniutka.kanban.model.*;
import io.github.akuniutka.kanban.util.CSVLineParser;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class FileBackedTaskManager extends InMemoryTaskManager {
    private static final String FILE_HEADER = "id,type,name,status,description,duration,start,epic";
    private Path path;

    public FileBackedTaskManager(Path path, HistoryManager historyManager) {
        this(historyManager);
        Objects.requireNonNull(path, "cannot start: file is null");
        this.path = path;
        save();
    }

    private FileBackedTaskManager(HistoryManager historyManager) {
        super(historyManager);
    }

    public static FileBackedTaskManager loadFromFile(Path path, HistoryManager historyManager) {
        Objects.requireNonNull(path, "cannot start: file is null");
        FileBackedTaskManager manager = new FileBackedTaskManager(historyManager);
        manager.path = path;
        manager.load();
        manager.save();
        return manager;
    }

    @Override
    public void removeTasks() {
        super.removeTasks();
        save();
    }

    @Override
    public long addTask(Task task) {
        final long taskId = super.addTask(task);
        save();
        return taskId;
    }

    @Override
    public void updateTask(Task task) {
        super.updateTask(task);
        save();
    }

    @Override
    public void removeTask(long id) {
        super.removeTask(id);
        save();
    }

    @Override
    public void removeEpics() {
        super.removeEpics();
        save();
    }

    @Override
    public long addEpic(Epic epic) {
        final long epicId = super.addEpic(epic);
        save();
        return epicId;
    }

    @Override
    public void updateEpic(Epic epic) {
        super.updateEpic(epic);
        save();
    }

    @Override
    public void removeEpic(long id) {
        super.removeEpic(id);
        save();
    }

    @Override
    public void removeSubtasks() {
        super.removeSubtasks();
        save();
    }

    @Override
    public long addSubtask(Subtask subtask) {
        final long subtaskId = super.addSubtask(subtask);
        save();
        return subtaskId;
    }

    @Override
    public void updateSubtask(Subtask subtask) {
        super.updateSubtask(subtask);
        save();
    }

    @Override
    public void removeSubtask(long id) {
        super.removeSubtask(id);
        save();
    }

    private void save() {
        List<String> lines = new ArrayList<>();
        lines.add(FILE_HEADER);
        lines.addAll(tasks.values().stream().map(this::toString).toList());
        lines.addAll(epics.values().stream().map(this::toString).toList());
        lines.addAll(subtasks.values().stream().map(this::toString).toList());
        try {
            Files.write(path, lines, StandardCharsets.UTF_8);
        } catch (IOException exception) {
            throw new ManagerSaveException("cannot write to file \"%s\"".formatted(path), exception);
        }
    }

    private String toString(Task task) {
        return "%s,%s,%s,%s,%s,%s,%s,%s".formatted(task.getId(), task.getType(), quoteIfNotNull(task.getTitle()),
                task.getType() != TaskType.EPIC ? task.getStatus() : "", quoteIfNotNull(task.getDescription()),
                task.getType() != TaskType.EPIC ? task.getDuration() : "",
                task.getType() != TaskType.EPIC ? task.getStartTime() : "",
                task.getType() == TaskType.SUBTASK ? ((Subtask) task).getEpicId() : "");
    }

    private String quoteIfNotNull(String text) {
        return text == null ? "null" : '"' + text + '"';
    }

    private void load() {
        try {
            List<String> lines = Files.readAllLines(path, StandardCharsets.UTF_8);
            checkFileHeader(lines.isEmpty() ? "" : lines.getFirst());
            lines.stream()
                    .skip(1L)
                    .map(this::fromString)
                    .peek(this::updateLastUsedId)
                    .peek(task -> {
                        switch (task.getType()) {
                            case TASK -> tasks.put(task.getId(), task);
                            case EPIC -> epics.put(task.getId(), (Epic) task);
                            case SUBTASK -> saveSubtaskAndLinkToEpic((Subtask) task);
                            default -> throw new AssertionError();
                        }
                    })
                    .filter(task -> task.getType() == TaskType.TASK || task.getType() == TaskType.SUBTASK)
                    .forEach(this::addToPrioritizedIfStartTimeNotNull);
        } catch (TaskNotFoundException exception) {
            throw new ManagerLoadException(exception.getMessage());
        } catch (IOException exception) {
            throw new ManagerLoadException("cannot load from file \"%s\"".formatted(path), exception);
        }
    }

    private void checkFileHeader(String header) {
        if (!FILE_HEADER.equals(header)) {
            throw new ManagerLoadException("wrong file header, expected \"%s\"".formatted(FILE_HEADER));
        }
    }

    private Task fromString(String taskString) {
        CSVLineParser parser = new CSVLineParser(taskString);
        final long id = extractId(parser.next());
        try {
            final TaskType type = extractType(parser.next());
            final Task task = switch (type) {
                case TASK -> new Task();
                case EPIC -> new Epic();
                case SUBTASK -> new Subtask();
            };
            task.setId(id);
            task.setTitle(extractText(parser.next()));
            String token = parser.next();
            if (type != TaskType.EPIC) {
                task.setStatus(extractStatus(token));
                task.setDescription(extractText(parser.next()));
                task.setDuration(extractDuration(parser.next()));
                task.setStartTime(extractDateTime(parser.next()));
                requireDoesNotOverlapOtherTasks(task);
            } else {
                requireNoStatusForEpic(token);
                task.setDescription(extractText(parser.next()));
                requireNoDurationForEpic(parser.next());
                requireNoStartTimeForEpic(parser.next());
            }
            token = parser.next();
            if (type == TaskType.SUBTASK) {
                ((Subtask) task).setEpicId(extractEpicId(token));
            } else {
                requireNoEpicIdForNotSubtask(token);
            }
            requireNoMoreData(parser);
            return task;
        } catch (CSVParsingException | ManagerValidationException exception) {
            throw new ManagerLoadException(exception.getMessage() + " for id=" + id);
        }
    }

    private long extractId(String token) {
        try {
            final long id = Long.parseLong(token);
            requireIdNotExist(id);
            return id;
        } catch (NumberFormatException exception) {
            throw new ManagerLoadException("line does not start with numeric id");
        } catch (DuplicateIdException exception) {
            throw new ManagerLoadException(exception.getMessage());
        }
    }

    private TaskType extractType(String token) {
        try {
            return TaskType.valueOf(token);
        } catch (IllegalArgumentException exception) {
            throw new CSVParsingException("unknown task type");
        }
    }

    private TaskStatus extractStatus(String token) {
        if ("null".equals(token)) {
            return null;
        }
        try {
            return TaskStatus.valueOf(token);
        } catch (IllegalArgumentException exception) {
            throw new CSVParsingException("unknown task status");
        }
    }

    private void requireNoStatusForEpic(String token) {
        if (!token.isEmpty()) {
            throw new CSVParsingException("explicit epic status");
        }
    }

    private String extractText(String token) {
        if ("null".equals(token)) {
            return null;
        }
        if (token.length() < 2 || token.charAt(0) != '"' || token.charAt(token.length() - 1) != '"') {
            throw new CSVParsingException("text value must be inside double quotes");
        }
        return token.substring(1, token.length() - 1);
    }

    private Long extractDuration(String token) {
        if ("null".equals(token)) {
            return null;
        }
        try {
            return Long.parseLong(token);
        } catch (NumberFormatException exception) {
            throw new CSVParsingException("wrong duration format");
        }
    }

    private void requireNoDurationForEpic(String token) {
        if (!token.isEmpty()) {
            throw new CSVParsingException("explicit epic duration");
        }
    }

    private LocalDateTime extractDateTime(String token) {
        if ("null".equals(token)) {
            return null;
        }
        try {
            return LocalDateTime.parse(token);
        } catch (DateTimeParseException exception) {
            throw new CSVParsingException("wrong start time format");
        }
    }

    private void requireNoStartTimeForEpic(String token) {
        if (!token.isEmpty()) {
            throw new CSVParsingException("explicit epic start time");
        }
    }

    private long extractEpicId(String token) {
        try {
            return Long.parseLong(token);
        } catch (NumberFormatException exception) {
            throw new CSVParsingException("wrong epic id format");
        }
    }

    private void requireNoEpicIdForNotSubtask(String token) {
        if (!token.isEmpty()) {
            throw new CSVParsingException("unexpected data");
        }
    }

    private void requireNoMoreData(CSVLineParser parser) {
        if (parser.hasNext()) {
            throw new CSVParsingException("unexpected data");
        }
    }

    private void updateLastUsedId(Task task) {
        lastUsedId = Math.max(lastUsedId, task.getId());
    }
}
