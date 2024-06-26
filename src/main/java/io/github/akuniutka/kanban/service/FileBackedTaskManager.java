package io.github.akuniutka.kanban.service;

import io.github.akuniutka.kanban.exception.CSVParsingException;
import io.github.akuniutka.kanban.exception.ManagerLoadException;
import io.github.akuniutka.kanban.exception.ManagerSaveException;
import io.github.akuniutka.kanban.exception.ManagerValidationException;
import io.github.akuniutka.kanban.model.*;
import io.github.akuniutka.kanban.util.CSVLineParser;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class FileBackedTaskManager extends InMemoryTaskManager {
    private static final String FILE_HEADER = "id,type,name,status,description,duration,start,epic";
    private Path path;

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
    public void deleteTasks() {
        super.deleteTasks();
        save();
    }

    @Override
    public Task createTask(Task task) {
        final Task savedTask = super.createTask(task);
        save();
        return savedTask;
    }

    @Override
    public Task updateTask(Task task) {
        final Task savedTask = super.updateTask(task);
        save();
        return savedTask;
    }

    @Override
    public void deleteTask(long id) {
        super.deleteTask(id);
        save();
    }

    @Override
    public void deleteEpics() {
        super.deleteEpics();
        save();
    }

    @Override
    public Epic createEpic(Epic epic) {
        final Epic savedEpic = super.createEpic(epic);
        save();
        return savedEpic;
    }

    @Override
    public Epic updateEpic(Epic epic) {
        final Epic savedEpic = super.updateEpic(epic);
        save();
        return savedEpic;
    }

    @Override
    public void deleteEpic(long id) {
        super.deleteEpic(id);
        save();
    }

    @Override
    public void deleteSubtasks() {
        super.deleteSubtasks();
        save();
    }

    @Override
    public Subtask createSubtask(Subtask subtask) {
        final Subtask savedSubtask = super.createSubtask(subtask);
        save();
        return savedSubtask;
    }

    @Override
    public Subtask updateSubtask(Subtask subtask) {
        final Subtask savedSubtask = super.updateSubtask(subtask);
        save();
        return savedSubtask;
    }

    @Override
    public void deleteSubtask(long id) {
        super.deleteSubtask(id);
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
                task.getType() != TaskType.EPIC ?
                        (task.getDuration() != null ? task.getDuration().toMinutes() : "null") : "",
                task.getType() != TaskType.EPIC ? task.getStartTime() : "",
                task.getType() == TaskType.SUBTASK ? ((Subtask) task).getEpicId() : "");
    }

    private String quoteIfNotNull(String text) {
        return text == null ? "null" : '"' + text + '"';
    }

    private void load() {
        try {
            if (!Files.exists(path)) {
                return;
            }
            List<String> lines = Files.readAllLines(path, StandardCharsets.UTF_8);
            if (lines.isEmpty()) {
                return;
            }
            checkFileHeader(lines.getFirst());
            lines.stream()
                    .skip(1L)
                    .map(this::fromString)
                    .peek(task -> requireNoDuplicateId(task.getId()))
                    .forEach(task -> {
                        try {
                            switch (task) {
                                case Subtask subtask -> updateSubtask(subtask);
                                case Epic epic -> updateEpic(epic);
                                default -> this.updateTask(task);
                            }
                        } catch (ManagerValidationException exception) {
                            throw new ManagerLoadException(exception.getMessage() + " for id=" + task.getId());
                        }
                    });
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
        } catch (CSVParsingException exception) {
            throw new ManagerLoadException(exception.getMessage() + " for id=" + id);
        }
    }

    private long extractId(String token) {
        try {
            return Long.parseLong(token);
        } catch (NumberFormatException exception) {
            throw new ManagerLoadException("line does not start with numeric id");
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

    private Duration extractDuration(String token) {
        if ("null".equals(token)) {
            return null;
        }
        try {
            long minutes = Long.parseLong(token);
            return Duration.ofMinutes(minutes);
        } catch (NumberFormatException | ArithmeticException exception) {
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

    private void requireNoDuplicateId(long id) {
        if (getTaskTypeById(id) != null) {
            throw new ManagerLoadException("duplicate id=" + id);
        }
    }
}
