package io.github.akuniutka.kanban.model;

import java.util.ArrayList;
import java.util.HashMap;

public class Epic extends Task {
    private final HashMap<Integer, Object> subtaskIds;

    public Epic() {
        this.subtaskIds = new HashMap<>();
    }

    public static Epic copyOf(Epic epic) {
        Epic newEpic = null;
        if (epic != null) {
            newEpic = new Epic();
            newEpic.setId(epic.getId());
            newEpic.setTitle(epic.getTitle());
            newEpic.setDescription(epic.getDescription());
            for (int subtaskId : epic.subtaskIds.keySet()) {
                newEpic.addSubtaskId(subtaskId);
            }
            newEpic.setStatus(epic.getStatus());
        }
        return newEpic;
    }

    public ArrayList<Integer> getSubtaskIds() {
        ArrayList<Integer> subtaskIdList = new ArrayList<>();
        for (int subtaskId : subtaskIds.keySet()) {
            subtaskIdList.add(subtaskId);
        }
        return subtaskIdList;
    }

    public void addSubtaskId(int subtaskId) {
        subtaskIds.put(subtaskId, null);
    }

    public void removeSubtaskId(int subtaskId) {
        subtaskIds.remove(subtaskId);
    }

    public boolean containsSubtask(int subtaskId) {
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
