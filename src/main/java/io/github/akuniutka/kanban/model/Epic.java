package io.github.akuniutka.kanban.model;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

public class Epic extends Task {
    private List<Subtask> subtasks;

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
    public Duration getDuration() {
        return subtasks.stream()
                .map(Subtask::getDuration)
                .filter(Objects::nonNull)
                .reduce(Duration::plus)
                .orElse(null);
    }

    @Override
    public void setDuration(Duration duration) {
        throw new UnsupportedOperationException("cannot explicitly set epic duration");
    }

    @Override
    public LocalDateTime getStartTime() {
        return subtasks.stream()
                .map(Subtask::getStartTime)
                .filter(Objects::nonNull)
                .min(Comparator.naturalOrder())
                .orElse(null);
    }

    @Override
    public void setStartTime(LocalDateTime startTime) {
        throw new UnsupportedOperationException("cannot explicitly set epic start time");
    }

    @Override
    public LocalDateTime getEndTime() {
        return subtasks.stream()
                .map(Subtask::getEndTime)
                .filter(Objects::nonNull)
                .max(Comparator.naturalOrder())
                .orElse(null);
    }

    @Override
    public TaskStatus getStatus() {
        final Set<TaskStatus> statuses = subtasks.stream()
                .map(Subtask::getStatus)
                .collect(Collectors.toSet());
        if (statuses.isEmpty()) {
            return TaskStatus.NEW;
        } else if (statuses.size() > 1) {
            return TaskStatus.IN_PROGRESS;
        } else {
            return statuses.iterator().next();
        }
    }

    @Override
    public void setStatus(TaskStatus status) {
        throw new UnsupportedOperationException("cannot explicitly set epic status");
    }

    @Override
    public String toString() {
        return "Epic{id=%s, title=%s, description%s, subtasks=%s, duration=%s, startTime=%s, status=%s}".formatted(
                getId(), getTitle() == null ? "null" : "\"" + getTitle() + "\"",
                getDescription() == null ? "=null" : ".length=" + getDescription().length(), subtasks, getDuration(),
                getStartTime(), getStatus());
    }
}
