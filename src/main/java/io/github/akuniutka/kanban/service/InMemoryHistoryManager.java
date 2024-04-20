package io.github.akuniutka.kanban.service;

import io.github.akuniutka.kanban.model.Task;

import java.util.HashMap;
import java.util.ArrayList;
import java.util.Map;
import java.util.List;

public class InMemoryHistoryManager implements HistoryManager {
    private final Map<Long, Node> nodes;
    private Node head;
    private Node tail;

    InMemoryHistoryManager() {
        this.nodes = new HashMap<>();
    }

    @Override
    public void add(Task task) {
        final long id = task.getId();
        final Node oldNode = nodes.get(id);
        if (oldNode != null) {
            removeNode(oldNode);
        }
        final Node newNode = linkLast(task);
        nodes.put(id, newNode);
    }

    @Override
    public List<Task> getHistory() {
        return getTasks();
    }

    private Node linkLast(Task task) {
        final Node oldTail = tail;
        final Node newTail = new Node(tail, task, null);
        tail = newTail;
        if (oldTail == null) {
            head = newTail;
        } else {
            oldTail.next = newTail;
        }
        return newTail;
    }

    private void removeNode(Node node) {
        final Node nextNode = node.next;
        final Node prevNode = node.prev;
        if (nextNode == null) {
            tail = prevNode;
        } else {
            nextNode.prev = prevNode;
        }
        if (prevNode == null) {
            head = nextNode;
        } else {
            prevNode.next = nextNode;
        }
        node.next = null;
        node.prev = null;
    }

    private List<Task> getTasks() {
        final ArrayList<Task> tasks = new ArrayList<>();
        Node node = head;
        while (node != null) {
            tasks.add(node.task);
            node = node.next;
        }
        return tasks;
    }

    private static class Node {
        Task task;
        Node next;
        Node prev;

        Node(Node prev, Task task, Node next) {
            this.task = task;
            this.next = next;
            this.prev = prev;
        }
    }
}
