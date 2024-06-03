package io.github.akuniutka.kanban;

import io.github.akuniutka.kanban.model.Subtask;
import io.github.akuniutka.kanban.model.TaskStatus;

import java.time.Duration;
import java.time.LocalDateTime;

public class SubtaskBuilder {
    private Long id;
    private Long epicId;
    private String title;
    private String description;
    private Duration duration;
    private LocalDateTime startTime;
    private TaskStatus status;

    public SubtaskBuilder withId(Long id) {
        this.id = id;
        return this;
    }

    public SubtaskBuilder withEpicId(Long epicId) {
        this.epicId = epicId;
        return this;
    }

    public SubtaskBuilder withTitle(String title) {
        this.title = title;
        return this;
    }

    public SubtaskBuilder withDescription(String description) {
        this.description = description;
        return this;
    }

    public SubtaskBuilder withDuration(Duration duration) {
        this.duration = duration;
        return this;
    }

    public SubtaskBuilder withStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
        return this;
    }

    public SubtaskBuilder withStatus(TaskStatus status) {
        this.status = status;
        return this;
    }

    public Subtask build() {
        Subtask subtask = new Subtask();
        subtask.setId(id);
        subtask.setEpicId(epicId);
        subtask.setTitle(title);
        subtask.setDescription(description);
        subtask.setDuration(duration);
        subtask.setStartTime(startTime);
        subtask.setStatus(status);
        return subtask;
    }
}
