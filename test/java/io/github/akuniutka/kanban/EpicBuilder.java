package io.github.akuniutka.kanban;

import io.github.akuniutka.kanban.model.Epic;
import io.github.akuniutka.kanban.model.Subtask;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

public class EpicBuilder {
    private Long id;
    private String title;
    private String description;
    private List<Subtask> subtasks;
    private Duration duration;
    private LocalDateTime startTime;
    private LocalDateTime endTime;

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

    public EpicBuilder withSubtasks(List<Subtask> subtasks) {
        this.subtasks = subtasks;
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

    public Epic build() {
        Epic epic = new Epic();
        if (id != null) {
            epic.setId(id);
        }
        epic.setTitle(title);
        epic.setDescription(description);
        epic.setSubtasks(subtasks);
        epic.setDuration(duration);
        epic.setStartTime(startTime);
        epic.setEndTime(endTime);
        return epic;
    }
}
