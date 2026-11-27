package com.taskmanager.query;
import com.taskmanager.model.Task;import java.util.*;
public class TaskDueDateSort implements SortStrategy {public void sort(List<Task> tasks){tasks.sort(Comparator.comparing(Task::getDueDate,Comparator.nullsLast(Comparator.naturalOrder())));}}
