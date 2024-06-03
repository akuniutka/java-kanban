package io.github.akuniutka.kanban.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Epic extends Task {
    private List<Subtask> subtasks;
    private LocalDateTime endTime;

    public Epic() {
        this.subtasks = new ArrayList<>();
    }

    @Override
    public TaskType getType() {
        return TaskType.EPIC;
    }

    public List<Subtask> getSubtasks() {
        return subtasks;
    }

    public void setSubtasks(List<Subtask> subtasks) {
        Objects.requireNonNull(subtasks, "list of subtasks cannot be null");
        this.subtasks = subtasks;
    }

    @Override
    public LocalDateTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
    }

    @Override
    public String toString() {
        return """
                Epic{id=%s, type=%s, title=%s, description%s, subtasks=%s, duration=%s, startTime=%s, endTime=%s, \
                status=%s}\
                """.formatted(getId(), getType(), getTitle() == null ? "null" : "\"" + getTitle() + "\"",
                getDescription() == null ? "=null" : ".length=" + getDescription().length(), subtasks, getDuration(),
                getStartTime(), getEndTime(), getStatus());
    }
}
