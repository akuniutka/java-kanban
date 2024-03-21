package io.github.akuniutka.kanban.model;

public class Task {
    private int id;
    private String title;
    private String description;
    private TaskStatus status;

    public Task() {
        this.status = TaskStatus.NEW;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public TaskStatus getStatus() {
        return status;
    }

    public void setStatus(TaskStatus status) {
        this.status = status;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Task task = (Task) o;
        return id == task.id;
    }

    @Override
    public int hashCode() {
        return id;
    }

    @Override
    public String toString() {
        String string = "Task{";
        string += "id=" + id;
        if (title == null) {
            string += ", title=null";
        } else {
            string += ", title='" + title + "'";
        }
        if (description == null) {
            string += ", description=null";
        } else {
            string += ", description.length=" + description.length();
        }
        if (status == null) {
            string += ", status=null";
        } else {
            string += ", status=" + status;
        }
        string += "}";
        return string;
    }
}
