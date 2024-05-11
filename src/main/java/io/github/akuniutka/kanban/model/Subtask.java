package io.github.akuniutka.kanban.model;

public class Subtask extends Task {
    private Long epicId;

    @Override
    public TaskType getType() {
        return TaskType.SUBTASK;
    }

    public Long getEpicId() {
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
        string += ", status=" + getStatus();
        string += "}";
        return string;
    }
}
