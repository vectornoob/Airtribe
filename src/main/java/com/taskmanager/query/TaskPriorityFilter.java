package com.taskmanager.query;
import com.taskmanager.model.Task;
public class TaskPriorityFilter implements TaskFilter {public boolean matches(Task t,TaskQuery q){return q.getPriority()==null||t.getPriority().name().equalsIgnoreCase(q.getPriority());}}
