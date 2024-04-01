package io.github.akuniutka.kanban.model;

import java.util.List;
import java.util.ArrayList;

public class Epic extends Task {
    private List<Long> subtaskIds;

    public Epic() {
        this.subtaskIds = new ArrayList<>();
    }

    public List<Long> getSubtaskIds() {
        return new ArrayList<>(subtaskIds);
    }

    public void addSubtaskId(long subtaskId) {
        subtaskIds.add(subtaskId);
    }

    public void removeSubtaskId(long subtaskId) {
        subtaskIds.remove(subtaskId);
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
        if (getStatus() == null) {
            string += ", status=null";
        } else {
            string += ", status=" + getStatus();
        }
        string += "}";
        return string;
    }
}
