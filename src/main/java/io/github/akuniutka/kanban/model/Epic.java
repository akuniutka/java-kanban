package io.github.akuniutka.kanban.model;

import java.util.ArrayList;
import java.util.List;

public class Epic extends Task {
    private List<Long> subtaskIds;

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

    public void setSubtaskIds(List<Long> subtaskIds) {
        this.subtaskIds = subtaskIds;
    }

    @Override
    public String toString() {
        return "Epic{id=%s, title=%s, description%s, subtaskIds=%s, duration=%d, startTime=%s, status=%s}".formatted(
                getId(), getTitle() == null ? "null" : "\"" + getTitle() + "\"",
                getDescription() == null ? "=null" : ".length=" + getDescription().length(), subtaskIds, getDuration(),
                getStartTime(), getStatus());
    }
}
