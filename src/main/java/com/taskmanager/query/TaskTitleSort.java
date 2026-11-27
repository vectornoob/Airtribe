package com.taskmanager.query;
import com.taskmanager.model.Task;import java.util.*;
public class TaskTitleSort implements SortStrategy {public void sort(List<Task> t){t.sort(Comparator.comparing(Task::getTitle,String.CASE_INSENSITIVE_ORDER));}}
