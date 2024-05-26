package io.github.akuniutka.kanban;

import io.github.akuniutka.kanban.model.Epic;
import io.github.akuniutka.kanban.model.Subtask;

import java.util.List;

public class EpicBuilder {
    private Long id;
    private String title;
    private String description;
    private List<Subtask> subtasks;

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

    public Epic build() {
        Epic epic = new Epic();
        if (id != null) {
            epic.setId(id);
        }
        epic.setTitle(title);
        epic.setDescription(description);
        epic.setSubtasks(subtasks);
        return epic;
    }
}
