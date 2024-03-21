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

        studyTheoryTask = taskManager.createNewTask(studyTheoryTask);
        getHomeworkTask = taskManager.createNewTask(getHomeworkTask);
        printAllTasks(taskManager);
        System.out.println();

        studyTheoryTask = taskManager.getTaskById(studyTheoryTask.getId());
        System.out.println(studyTheoryTask);
        getHomeworkTask = taskManager.getTaskById(getHomeworkTask.getId());
        System.out.println(getHomeworkTask);
        System.out.println();

        studyTheoryTask = taskManager.removeTask(studyTheoryTask.getId());
        System.out.println("Removed task: " + studyTheoryTask);
        printAllTasks(taskManager);
        studyTheoryTask = taskManager.removeTask(-1);
        System.out.println("Removed task: " + studyTheoryTask);
        printAllTasks(taskManager);
        System.out.println();

        getHomeworkTask.setDescription("Get access to 4th sprint homework");
        getHomeworkTask.setStatus(TaskStatus.IN_PROGRESS);
        getHomeworkTask = taskManager.updateTask(getHomeworkTask);
        if (getHomeworkTask != null) {
            System.out.println("Task updated");
        }
        printAllTasks(taskManager);
        System.out.println();

        taskManager.removeAllTasks();
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

        buyCarEpic = taskManager.createNewEpic(buyCarEpic);
        buyHouseEpic = taskManager.createNewEpic(buyHouseEpic);
        printAllEpics(taskManager);
        System.out.println();

        buyCarEpic = taskManager.getEpicById(buyCarEpic.getId());
        System.out.println(buyCarEpic);
        buyHouseEpic = taskManager.getEpicById(buyHouseEpic.getId());
        System.out.println(buyHouseEpic);
        System.out.println();

        buyCarEpic = taskManager.removeEpic(buyCarEpic.getId());
        System.out.println("Removed epic: " + buyCarEpic);
        printAllEpics(taskManager);
        buyCarEpic = taskManager.removeEpic(-1);
        System.out.println("Removed epic: " + buyCarEpic);
        printAllEpics(taskManager);
        System.out.println();

        buyHouseEpic.setDescription("Buy a new house on the Black Sea");
        buyHouseEpic.setStatus(TaskStatus.IN_PROGRESS);
        buyHouseEpic = taskManager.updateEpic(buyHouseEpic);
        if (buyHouseEpic != null) {
            System.out.println("Epic updated");
        }
        printAllEpics(taskManager);
        System.out.println();

        taskManager.removeAllEpics();
        printAllEpics(taskManager);
        System.out.println();
    }

    private static void testSubtasks(TaskManager taskManager) {
        System.out.println("\nTesting subtasks:\n");

        Epic kanban = new Epic();
        kanban.setTitle("Kanban application");
        kanban.setDescription("Create backend for task manager");
        kanban = taskManager.createNewEpic(kanban);

        Subtask stepOne = new Subtask();
        stepOne.setTitle("Implement Task class");
        stepOne.setEpicId(kanban.getId());
        Subtask stepTwo = new Subtask();
        stepTwo.setTitle("Implement Subtask class");
        stepTwo.setEpicId(kanban.getId());
        Subtask stepThree = new Subtask();
        stepThree.setTitle("Implement Epic class");
        stepThree.setEpicId(kanban.getId());

        stepOne = taskManager.createNewSubtask(stepOne);
        stepTwo = taskManager.createNewSubtask(stepTwo);
        stepThree = taskManager.createNewSubtask(stepThree);
        kanban = taskManager.getEpicById(kanban.getId());
        System.out.println("Epic: " + kanban);
        printAllSubtasks(taskManager);
        System.out.println();

        stepOne = taskManager.getSubtaskById(stepOne.getId());
        System.out.println(stepOne);
        stepTwo = taskManager.getSubtaskById(stepTwo.getId());
        System.out.println(stepTwo);
        stepThree = taskManager.getSubtaskById(stepThree.getId());
        System.out.println(stepThree);
        System.out.println();

        for (Subtask subtask : taskManager.getEpicSubtasks(kanban.getId())) {
            System.out.println(subtask);
        }
        System.out.println();

        stepOne.setDescription("Make Task class the super class for Epic and Subtask classes");
        stepOne.setStatus(TaskStatus.DONE);
        stepOne = taskManager.updateSubtask(stepOne);
        if (stepOne != null) {
            System.out.println("Subtask updated");
        }
        kanban = taskManager.getEpicById(kanban.getId());
        System.out.println("Epic: " + kanban);
        printAllSubtasks(taskManager);
        System.out.println();

        stepThree = taskManager.removeSubtask(stepThree.getId());
        System.out.println("Removed subtask: " + stepThree);
        kanban = taskManager.getEpicById(kanban.getId());
        System.out.println("Epic: " + kanban);
        printAllSubtasks(taskManager);
        stepThree = taskManager.removeSubtask(-1);
        System.out.println("Removed subtask: " + stepThree);
        kanban = taskManager.getEpicById(kanban.getId());
        System.out.println("Epic: " + kanban);
        printAllSubtasks(taskManager);
        stepTwo = taskManager.removeSubtask(stepTwo.getId());
        System.out.println("Removed subtask: " + stepTwo);
        kanban = taskManager.getEpicById(kanban.getId());
        System.out.println("Epic: " + kanban);
        printAllSubtasks(taskManager);
        System.out.println();

        taskManager.removeAllSubtasks();
        kanban = taskManager.getEpicById(kanban.getId());
        System.out.println("Epic: " + kanban);
        printAllSubtasks(taskManager);
        System.out.println();
    }

    private static void printAllTasks(TaskManager taskManager) {
        ArrayList<Task> tasks = taskManager.getAllTasks();
        if (tasks.isEmpty()) {
            System.out.println("<empty>");
        } else {
            for (Task task : tasks) {
                System.out.println(task);
            }
        }
    }

    private static void printAllEpics(TaskManager taskManager) {
        ArrayList<Epic> epics = taskManager.getAllEpics();
        if (epics.isEmpty()) {
            System.out.println("<empty>");
        } else {
            for (Epic epic : epics) {
                System.out.println(epic);
            }
        }
    }

    private static void printAllSubtasks(TaskManager taskManager) {
        ArrayList<Subtask> subtasks = taskManager.getAllSubtasks();
        if (subtasks.isEmpty()) {
            System.out.println("<empty>");
        } else {
            for (Subtask subtask : subtasks) {
                System.out.println(subtask);
            }
        }
    }
}
