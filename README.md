# Kanban (backend)

The backend part of "Kanban" task tracker. Implements data model and supports creating, 
updating, retrieving, deleting tasks.

## Task types

The application supports three types of tasks:
- **task**: a regular task which can be completed in one run,
- **epic**: a more complex task which consists of several steps (subtasks),
- **subtask**: a simple one-run task which represents a step of an epic.

## Task properties

Every task has the following properties:
- **id**: the unique identifier of the task,
- **title**: a short description of the task (e.g., "Buy vegetables"),
- **description**: details of the task,
- **status**: current status of the task (possible values: NEW, IN_PROGRESS, DONE).

Additionally, 
- epic maintains the list of ids of subtasks it consists of,
- subtask keeps the id of epic it belongs to.

## Supported operations

- creating a new task/ apic/ subtask,
- updating a task/ epic/ subtask,
- retrieving a task/ epic/ subtask by id,
- removing a task/ epic/ subtask,
- retrieving the list of subtasks of an epic,
- retrieving the list of all tasks/ epics/ subtasks,
- removing all tasks/ epics/ subtasks,
- keeping the list of tasks/ epics/ subtasks retrieved.

## Contact

Andrei Kuniutka [<akuniutka@gmail.com>](mailto:akuniutka@gmail.com)
