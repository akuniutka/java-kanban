package io.github.akuniutka.kanban.model;

import java.util.ArrayList;
import java.util.List;

public class Epic extends Task {
    private List<Long> subtaskIds;

    public Epic() {
        this.subtaskIds = new ArrayList<>();
    }

    public List<Long> getSubtaskIds() {
        return subtaskIds;
    }

    public void setSubtaskIds(List<Long> subtaskIds) {
        this.subtaskIds = subtaskIds;
    }

    @Override
    public String toString() {
        String string = "Epic{";
        string += "id=" + getId();
        if (getTitle() == null) {
            string += ", title=null";
        } else {
            string += ", title='" + getTitle() + "'";
        }
        if (getDescription() == null) {
            string += ", description=null";
        } else {
            string += ", description.length=" + getDescription().length();
        }
        string += ", subtaskIds=" + subtaskIds;
        string += ", status=" + getStatus();
        string += "}";
        return string;
    }
}
