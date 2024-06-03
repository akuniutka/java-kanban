package io.github.akuniutka.kanban;

import io.github.akuniutka.kanban.model.Epic;
import io.github.akuniutka.kanban.model.TaskStatus;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

public class EpicBuilder {
    private Long id;
    private String title;
    private String description;
    private List<Long> subtaskIds;
    private Duration duration;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private TaskStatus status;

    public EpicBuilder withId(Long id) {
        this.id = id;
        return this;
    }

    public EpicBuilder withTitle(String title) {
        this.title = title;
        return this;
    }

    public EpicBuilder withDescription(String description) {
        this.description = description;
        return this;
    }

    public EpicBuilder withSubtaskIds(List<Long> subtaskIds) {
        this.subtaskIds = subtaskIds;
        return this;
    }

    public EpicBuilder withDuration(Duration duration) {
        this.duration = duration;
        return this;
    }

    public EpicBuilder withStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
        return this;
    }

    public EpicBuilder withEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
        return this;
    }

    public EpicBuilder withStatus(TaskStatus status) {
        this.status = status;
        return this;
    }

    public Epic build() {
        Epic epic = new Epic();
        if (id != null) {
            epic.setId(id);
        }
        epic.setTitle(title);
        epic.setDescription(description);
        epic.setSubtaskIds(subtaskIds);
        epic.setDuration(duration);
        epic.setStartTime(startTime);
        epic.setEndTime(endTime);
        epic.setStatus(status);
        return epic;
    }
}
