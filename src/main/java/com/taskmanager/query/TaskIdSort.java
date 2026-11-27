package com.taskmanager.query;
import com.taskmanager.model.Task;import java.util.*;
public class TaskIdSort implements SortStrategy {public void sort(List<Task> tasks){tasks.sort(Comparator.comparingInt(Task::getId));}}
