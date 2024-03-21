package io.github.akuniutka.kanban.model;

import java.util.ArrayList;
import java.util.HashMap;

public class Epic extends Task {
    private final HashMap<Long, Object> subtaskIds;

    public Epic() {
        this.subtaskIds = new HashMap<>();
    }

    public ArrayList<Long> getSubtaskIds() {
        ArrayList<Long> subtaskIdList = new ArrayList<>();
        for (long subtaskId : subtaskIds.keySet()) {
            subtaskIdList.add(subtaskId);
        }
        return subtaskIdList;
    }

    public void addSubtaskId(long subtaskId) {
        subtaskIds.put(subtaskId, null);
    }

    public void removeSubtaskId(long subtaskId) {
        subtaskIds.remove(subtaskId);
    }

    public boolean containsSubtask(long subtaskId) {
        return subtaskIds.containsKey(subtaskId);
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
        string += ", subtaskIds=" + subtaskIds.keySet();
        if (getStatus() == null) {
            string += ", status=null";
        } else {
            string += ", status=" + getStatus();
        }
        string += "}";
        return string;
    }
}
