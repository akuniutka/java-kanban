package io.github.akuniutka.kanban.model;

public class Subtask extends Task {
    private Long epicId;

    @Override
    public TaskType getType() {
        return TaskType.SUBTASK;
    }

    public Long getEpicId() {
        return epicId;
    }

    public void setEpicId(long epicId) {
        this.epicId = epicId;
    }

    @Override
    public String toString() {
        return "Subtask{id=%s, epicId=%s, title=%s, description%s, duration=%d, startTime=%s, status=%s}".formatted(
                getId(), epicId, getTitle() == null ? "null" : "\"" + getTitle() + "\"",
                getDescription() == null ? "=null" : ".length=" + getDescription().length(), getDuration(),
                getStartTime(), getStatus());
    }
}
