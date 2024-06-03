package io.github.akuniutka.kanban.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class Epic extends Task {
    private List<Long> subtaskIds;
    private LocalDateTime endTime;

    public Epic() {
        this.subtaskIds = new ArrayList<>();
    }

    @Override
    public TaskType getType() {
        return TaskType.EPIC;
    }

    public List<Long> getSubtaskIds() {
        return subtaskIds;
    }

    public void setSubtaskIds(List<Long> subtasks) {
        this.subtaskIds = subtasks;
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
                Epic{id=%s, type=%s, title=%s, description%s, subtaskIds=%s, duration=%s, startTime=%s, endTime=%s, \
                status=%s}\
                """.formatted(getId(), getType(), getTitle() == null ? "null" : "\"" + getTitle() + "\"",
                getDescription() == null ? "=null" : ".length=" + getDescription().length(), subtaskIds, getDuration(),
                getStartTime(), getEndTime(), getStatus());
    }
}
