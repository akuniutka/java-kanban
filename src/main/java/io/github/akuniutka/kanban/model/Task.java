package io.github.akuniutka.kanban.model;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Objects;

public class Task {
    private Long id;
    private String title;
    private String description;
    private Duration duration;
    private LocalDateTime startTime;
    private TaskStatus status;

    public Task() {
        this.duration = Duration.ofMinutes(0L);
        this.status = TaskStatus.NEW;
    }

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

    public long getDuration() {
        return duration.toMinutes();
    }

    public void setDuration(long minutes) {
        if (minutes < 0) {
            throw new IllegalArgumentException("duration cannot be negative");
        }
        this.duration = Duration.ofMinutes(minutes);
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime == null ? null : startTime.truncatedTo(ChronoUnit.MINUTES);
    }

    public LocalDateTime getEndTime() {
        return startTime == null ? null : startTime.plus(duration);
    }

    public TaskStatus getStatus() {
        return status;
    }

    public void setStatus(TaskStatus status) {
        this.status = status;
    }

    @Override
    public boolean equals(Object o) {
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
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Task{id=%s, title=%s, description%s, duration=%d, startTime=%s, status=%s}".formatted(id,
                title == null ? "null" : "\"" + title + "\"",
                description == null ? "=null" : ".length=" + description.length(), duration.toMinutes(), startTime,
                status);
    }
}
