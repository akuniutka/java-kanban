package io.github.akuniutka.kanban;

import io.github.akuniutka.kanban.model.Task;
import io.github.akuniutka.kanban.model.Subtask;
import io.github.akuniutka.kanban.model.Epic;
import io.github.akuniutka.kanban.model.TaskStatus;
import io.github.akuniutka.kanban.service.TaskManager;

import java.util.ArrayList;

public class Main {

    public static void main(String[] args) {
        System.out.println("Поехали!");
        TaskManager taskManager = new TaskManager();

        testTasks(taskManager);
        testEpics(taskManager);
        testSubtasks(taskManager);
    }

    private static void testTasks(TaskManager taskManager) {
        System.out.println("\nTesting tasks:\n");

        Task studyTheoryTask = new Task();
        studyTheoryTask.setTitle("Study theory");
        studyTheoryTask.setDescription("Read, understand and practice 4th sprint theory");
        Task getHomeworkTask = new Task();
        getHomeworkTask.setTitle("Get homework");

        studyTheoryTask = taskManager.addTask(studyTheoryTask);
        getHomeworkTask = taskManager.addTask(getHomeworkTask);
        printAllTasks(taskManager);
        System.out.println();

        studyTheoryTask = taskManager.getTask(studyTheoryTask.getId());
        getHomeworkTask = taskManager.getTask(getHomeworkTask.getId());
        taskManager.getTask(-1);
        System.out.println();

        studyTheoryTask = taskManager.removeTask(studyTheoryTask.getId());
        printAllTasks(taskManager);
        taskManager.removeTask(studyTheoryTask.getId());
        printAllTasks(taskManager);
        System.out.println();

        getHomeworkTask.setDescription("Get access to 4th sprint homework");
        getHomeworkTask.setStatus(TaskStatus.IN_PROGRESS);
        taskManager.updateTask(getHomeworkTask);
        printAllTasks(taskManager);
        System.out.println();

        taskManager.removeTasks();
        printAllTasks(taskManager);
        System.out.println();
    }

    private static void testEpics(TaskManager taskManager) {
        System.out.println("\nTesting epics:\n");

        Epic buyCarEpic = new Epic();
        buyCarEpic.setTitle("Buy a new car");
        Epic buyHouseEpic = new Epic();
        buyHouseEpic.setTitle("Buy a new house");
        buyHouseEpic.setDescription("Buy a new house on Baltic seashore");

        buyCarEpic = taskManager.addEpic(buyCarEpic);
        buyHouseEpic = taskManager.addEpic(buyHouseEpic);
        printAllEpics(taskManager);
        System.out.println();

        buyCarEpic = taskManager.getEpic(buyCarEpic.getId());
        buyHouseEpic = taskManager.getEpic(buyHouseEpic.getId());
        taskManager.getEpic(-1);
        System.out.println();

        buyCarEpic = taskManager.removeEpic(buyCarEpic.getId());
        printAllEpics(taskManager);
        taskManager.removeEpic(buyCarEpic.getId());
        printAllEpics(taskManager);
        System.out.println();

        buyHouseEpic.setDescription("Buy a new house on the Black Sea");
        buyHouseEpic.setStatus(TaskStatus.IN_PROGRESS);
        taskManager.updateEpic(buyHouseEpic);
        printAllEpics(taskManager);
        System.out.println();

        taskManager.removeEpics();
        printAllEpics(taskManager);
        System.out.println();
    }

    private static void testSubtasks(TaskManager taskManager) {
        System.out.println("\nTesting subtasks:\n");

        Epic kanban = new Epic();
        kanban.setTitle("Kanban application");
        kanban.setDescription("Create backend for task manager");
        kanban = taskManager.addEpic(kanban);

        Subtask stepOne = new Subtask();
        stepOne.setTitle("Implement Task class");
        stepOne.setEpicId(kanban.getId());
        Subtask stepTwo = new Subtask();
        stepTwo.setTitle("Implement Subtask class");
        stepTwo.setEpicId(kanban.getId());
        Subtask stepThree = new Subtask();
        stepThree.setTitle("Implement Epic class");
        stepThree.setEpicId(kanban.getId());

        stepOne = taskManager.addSubtask(stepOne);
        stepTwo = taskManager.addSubtask(stepTwo);
        stepThree = taskManager.addSubtask(stepThree);
        kanban = taskManager.getEpic(kanban.getId());
        printAllSubtasks(taskManager);
        System.out.println();

        stepOne = taskManager.getSubtask(stepOne.getId());
        stepTwo = taskManager.getSubtask(stepTwo.getId());
        stepThree = taskManager.getSubtask(stepThree.getId());
        System.out.println();

        for (Subtask subtask : taskManager.getEpicSubtasks(kanban.getId())) {
            System.out.println(subtask);
        }
        System.out.println();

        stepOne.setDescription("Make Task class the super class for Epic and Subtask classes");
        stepOne.setStatus(TaskStatus.DONE);
        taskManager.updateSubtask(stepOne);
        kanban = taskManager.getEpic(kanban.getId());
        printAllSubtasks(taskManager);
        System.out.println();

        stepThree = taskManager.removeSubtask(stepThree.getId());
        kanban = taskManager.getEpic(kanban.getId());
        printAllSubtasks(taskManager);
        taskManager.removeSubtask(stepThree.getId());
        kanban = taskManager.getEpic(kanban.getId());
        printAllSubtasks(taskManager);
        taskManager.removeSubtask(stepTwo.getId());
        kanban = taskManager.getEpic(kanban.getId());
        printAllSubtasks(taskManager);
        System.out.println();

        taskManager.removeSubtasks();
        taskManager.getEpic(kanban.getId());
        printAllSubtasks(taskManager);
        System.out.println();
    }

    private static void printAllTasks(TaskManager taskManager) {
        ArrayList<Task> tasks = taskManager.getTasks();
        if (!tasks.isEmpty()) {
            for (Task task : tasks) {
                System.out.println(task);
            }
        }
    }

    private static void printAllEpics(TaskManager taskManager) {
        ArrayList<Epic> epics = taskManager.getEpics();
        if (!epics.isEmpty()) {
            for (Epic epic : epics) {
                System.out.println(epic);
            }
        }
    }

    private static void printAllSubtasks(TaskManager taskManager) {
        ArrayList<Subtask> subtasks = taskManager.getSubtasks();
        if (!subtasks.isEmpty()) {
            for (Subtask subtask : subtasks) {
                System.out.println(subtask);
            }
        }
    }
}
