package io.github.akuniutka.kanban.model;

import java.time.LocalDateTime;
import java.util.*;

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
    public Long getDuration() {
        Long duration = null;
        for (Subtask subtask : subtasks) {
            if (duration == null) {
                duration = subtask.getDuration();
            } else if (subtask.getDuration() != null) {
                duration += subtask.getDuration();
            }
        }
        return duration;
    }

    @Override
    public void setDuration(Long duration) {
        throw new UnsupportedOperationException("cannot explicitly set epic duration");
    }

    @Override
    public LocalDateTime getStartTime() {
        LocalDateTime startTime = null;
        for (Subtask subtask : subtasks) {
            if (startTime == null || (subtask.getStartTime() != null && subtask.getStartTime().isBefore(startTime))) {
                startTime = subtask.getStartTime();
            }
        }
        return startTime;
    }

    @Override
    public void setStartTime(LocalDateTime startTime) {
        throw new UnsupportedOperationException("cannot explicitly set epic start time");
    }

    @Override
    public LocalDateTime getEndTime() {
        LocalDateTime endTime = null;
        for (Subtask subtask : subtasks) {
            if (endTime == null || (subtask.getEndTime() != null && subtask.getEndTime().isAfter(endTime))) {
                endTime = subtask.getEndTime();
            }
        }
        return endTime;
    }

    @Override
    public TaskStatus getStatus() {
        final Set<TaskStatus> subtaskStatuses = new HashSet<>();
        for (Subtask subtask : subtasks) {
            subtaskStatuses.add(subtask.getStatus());
        }
        if (subtaskStatuses.isEmpty()) {
            return TaskStatus.NEW;
        } else if (subtaskStatuses.size() > 1) {
            return TaskStatus.IN_PROGRESS;
        } else {
            return subtaskStatuses.iterator().next();
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
