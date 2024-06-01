package io.github.akuniutka.kanban;

import io.github.akuniutka.kanban.model.Task;
import io.github.akuniutka.kanban.model.TaskStatus;

import java.time.Duration;
import java.time.LocalDateTime;

public class TaskBuilder {
    private Long id;
    private String title;
    private String description;
    private Duration duration;
    private LocalDateTime startTime;
    private TaskStatus status;

    public TaskBuilder withId(Long id) {
        this.id = id;
        return this;
    }

    public TaskBuilder withTitle(String title) {
        this.title = title;
        return this;
    }

    public TaskBuilder withDescription(String description) {
        this.description = description;
        return this;
    }

    public TaskBuilder withDuration(Duration duration) {
        this.duration = duration;
        return this;
    }

    public TaskBuilder withStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
        return this;
    }

    public TaskBuilder withStatus(TaskStatus status) {
        this.status = status;
        return this;
    }

    public Task build() {
        Task task = new Task();
        if (id != null) {
            task.setId(id);
        }
        task.setTitle(title);
        task.setDescription(description);
        task.setDuration(duration);
        task.setStartTime(startTime);
        task.setStatus(status);
        return task;
    }
}
