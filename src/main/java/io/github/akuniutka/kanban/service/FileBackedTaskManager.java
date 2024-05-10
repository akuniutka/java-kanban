package io.github.akuniutka.kanban.service;

import io.github.akuniutka.kanban.exception.CSVParsingException;
import io.github.akuniutka.kanban.exception.ManagerLoadException;
import io.github.akuniutka.kanban.exception.ManagerNoSuchEpicException;
import io.github.akuniutka.kanban.exception.ManagerSaveException;
import io.github.akuniutka.kanban.model.*;
import io.github.akuniutka.kanban.util.CSVLineParser;
import io.github.akuniutka.kanban.util.CSVToken;

import java.io.*;
import java.nio.charset.StandardCharsets;

public class FileBackedTaskManager extends InMemoryTaskManager {
    private static final String FILE_HEADER = "id,type,name,status,description,epic";
    private File datafile;

    public FileBackedTaskManager(File file, HistoryManager historyManager) {
        this(historyManager);
        this.datafile = file;
        save();
    }

    private FileBackedTaskManager(HistoryManager historyManager) {
        super(historyManager);
    }

    public static FileBackedTaskManager loadFromFile(File file, HistoryManager historyManager) {
        FileBackedTaskManager manager = new FileBackedTaskManager(historyManager);
        manager.datafile = file;
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
        try (BufferedWriter out = new BufferedWriter(new FileWriter(datafile, StandardCharsets.UTF_8))) {
            out.write("lastUsedId=%d%n%s%n".formatted(lastUsedId, FILE_HEADER));
            for (Task task : tasks.values()) {
                out.write(toString(task));
                out.newLine();
            }
            for (Epic epic : epics.values()) {
                out.write(toString(epic));
                out.newLine();
            }
            for (Subtask subtask : subtasks.values()) {
                out.write(toString(subtask));
                out.newLine();
            }
        } catch (IOException exception) {
            throw new ManagerSaveException("cannot write to file \"%s\"".formatted(datafile), exception);
        }
    }

    private String toString(Task task) {
        String string = task.getId().toString();
        if (task instanceof Epic) {
            string += ",EPIC";
        } else if (task instanceof Subtask) {
            string += ",SUBTASK";
        } else {
            string += ",TASK";
        }
        if (task.getTitle() == null) {
            string += ",null";
        } else {
            string += ",\"" + task.getTitle() + "\"";
        }
        if (task instanceof Epic) {
            string += ",";
        } else {
            string += "," + task.getStatus();
        }
        if (task.getDescription() == null) {
            string += ",null";
        } else {
            string += ",\"" + task.getDescription() + "\"";
        }
        if (task instanceof Subtask subtask) {
            string += "," + subtask.getEpicId();
        } else {
            string += ",";
        }
        return string;
    }

    private void load() {
        int curLine = 1;
        try (BufferedReader in = new BufferedReader(new FileReader(datafile, StandardCharsets.UTF_8))) {
            lastUsedId = extractLastUsedId(in.readLine());
            curLine++;
            checkFileHeader(in.readLine());
            while (in.ready()) {
                curLine++;
                String line = in.readLine();
                final Task task = fromString(line);
                checkIdForDuplicates(task.getId());
                try {
                    if (task instanceof Subtask subtask) {
                        saveSubtaskAndLinkToEpic(subtask);
                    } else if (task instanceof Epic epic) {
                        epics.put(epic.getId(), epic);
                    } else {
                        tasks.put(task.getId(), task);
                    }
                } catch (ManagerNoSuchEpicException exception) {
                    throw new CSVParsingException(exception.getMessage(), line.lastIndexOf(",") + 2);
                }
            }
        } catch (CSVParsingException exception) {
            String message = "%s (%s:%d:%d)".formatted(exception.getShortMessage(), datafile, curLine,
                    exception.getPositionInLine());
            throw new ManagerLoadException(message);
        } catch (IOException exception) {
            throw new ManagerLoadException("cannot load from file \"%s\"".formatted(datafile), exception);
        }
    }

    private long extractLastUsedId(String lastUsedIdString) {
        if (lastUsedIdString == null || !lastUsedIdString.startsWith("lastUsedId")) {
            throw new CSVParsingException("\"lastUsedId\" expected", 1);
        }
        if (lastUsedIdString.charAt(10) != '=') {
            throw new CSVParsingException("\"=\" expected", 11);
        }
        try {
            return Long.parseLong(lastUsedIdString.substring(11));
        } catch (NumberFormatException exception) {
            throw new CSVParsingException("number expected", 12);
        }
    }

    private void checkFileHeader(String header) {
        if (header == null || !header.startsWith("id")) {
            throw new CSVParsingException("\"id\" expected", 1);
        }
        String[] columnNames = {"type", "name", "status", "description", "epic"};
        int[] columnStarts = {3, 8, 13, 20, 32};
        for (int i = 0; i < columnNames.length; i++) {
            if (header.length() < columnStarts[i] || header.charAt(columnStarts[i] - 1) != ',') {
                throw new CSVParsingException("comma expected", columnStarts[i]);
            }
            if (header.indexOf(columnNames[i], columnStarts[i]) != columnStarts[i]) {
                throw new CSVParsingException('"' + columnNames[i] + "\" expected", columnStarts[i] + 1);
            }
        }
        if (header.length() > FILE_HEADER.length()) {
            throw new CSVParsingException("unexpected data", FILE_HEADER.length() + 1);
        }
    }

    private Task fromString(String taskString) {
        CSVLineParser parser = new CSVLineParser(taskString);
        final long id = extractId(parser.next());
        final TaskType type = extractType(parser.next());
        final Task task = switch (type) {
            case TASK -> new Task();
            case EPIC -> new Epic();
            case SUBTASK -> new Subtask();
        };
        task.setId(id);
        task.setTitle(extractText(parser.next()));
        CSVToken token = parser.next();
        if (type != TaskType.EPIC) {
            task.setStatus(extractStatus(token));
        } else if (!token.value().isEmpty()) {
            throw new CSVParsingException("explicit epic status", token.position() + 1);
        }
        task.setDescription(extractText(parser.next()));
        token = parser.next();
        if (task instanceof Subtask subtask) {
            subtask.setEpicId(extractId(token));
        } else if (!token.value().isEmpty()) {
            throw new CSVParsingException("unexpected data", token.position() + 1);
        }
        if (parser.hasNext()) {
            throw new CSVParsingException("unexpected data", token.position() + token.value().length() + 1);
        }
        return task;
    }

    private TaskType extractType(CSVToken token) {
        try {
            return TaskType.valueOf(token.value());
        } catch (IllegalArgumentException exception) {
            throw new CSVParsingException("unknown task type", token.position() + 1);
        }
    }

    private TaskStatus extractStatus(CSVToken token) {
        if ("null".equals(token.value())) {
            return null;
        }
        try {
            return TaskStatus.valueOf(token.value());
        } catch (IllegalArgumentException exception) {
            throw new CSVParsingException("unknown task status", token.position() + 1);
        }
    }

    private long extractId(CSVToken token) {
        try {
            return Long.parseLong(token.value());
        } catch (NumberFormatException exception) {
            throw new CSVParsingException("number expected", token.position() + 1);
        }
    }

    private String extractText(CSVToken token) {
        if ("null".equals(token.value())) {
            return null;
        }
        if (!token.isQuoted()) {
            throw new CSVParsingException("text value must be inside double quotes", token.position() + 1);
        }
        return token.value();
    }

    private void checkIdForDuplicates(long id) {
        if (tasks.containsKey(id) || epics.containsKey(id) || subtasks.containsKey(id)) {
            throw new CSVParsingException("duplicate task id", 1);
        }
    }
}
