package io.github.akuniutka.kanban.model;

public class Subtask extends Task {
    private long epicId;

    public long getEpicId() {
        return epicId;
    }

    public void setEpicId(long epicId) {
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
