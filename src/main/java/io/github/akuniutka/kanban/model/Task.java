package io.github.akuniutka.kanban.model;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Objects;

public class Task {
    private Long id;
    private String title;
    private String description;
    private Duration duration;
    private LocalDateTime startTime;
    private TaskStatus status;

    public Long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public TaskType getType() {
        return TaskType.TASK;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Duration getDuration() {
        return duration;
    }

    public void setDuration(Duration duration) {
        this.duration = duration;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }

    public LocalDateTime getEndTime() {
        return startTime == null || duration == null ? null : startTime.plus(duration);
    }

    public TaskStatus getStatus() {
        return status;
    }

    public void setStatus(TaskStatus status) {
        this.status = status;
    }

    @Override
    public final boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Task task = (Task) o;
        return Objects.equals(id, task.id);
    }

    @Override
    public final int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return """
                Task{id=%s, type=%s, title=%s, description%s, duration=%s, startTime=%s, endTime=%s, status=%s}\
                """.formatted(id, getType(), title == null ? "null" : "\"" + title + "\"",
                description == null ? "=null" : ".length=" + description.length(), getDuration(), startTime,
                getEndTime(), status);
    }
}
