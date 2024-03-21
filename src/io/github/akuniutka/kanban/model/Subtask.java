package io.github.akuniutka.kanban.model;

public class Subtask extends Task {
    private int epicId;

    public static Subtask copyOf(Subtask subtask) {
        Subtask newSubtask = null;
        if (subtask != null) {
            newSubtask = new Subtask();
            newSubtask.setId(subtask.getId());
            newSubtask.setEpicId(subtask.getEpicId());
            newSubtask.setTitle(subtask.getTitle());
            newSubtask.setDescription(subtask.getDescription());
            newSubtask.setStatus(subtask.getStatus());
        }
        return newSubtask;
    }

    public int getEpicId() {
        return epicId;
    }

    public void setEpicId(int epicId) {
        this.epicId = epicId;
    }

    @Override
    public String toString() {
        String string = "Subtask{";
        string += "id=" + getId();
        string += ", epicId=" + epicId;
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
        if (getStatus() == null) {
            string += ", status=null";
        } else {
            string += ", status=" + getStatus();
        }
        string += "}";
        return string;
    }
}
