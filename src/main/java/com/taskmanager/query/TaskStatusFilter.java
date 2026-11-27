package com.taskmanager.query;
import com.taskmanager.model.Task;
public class TaskStatusFilter implements TaskFilter {public boolean matches(Task t,TaskQuery q){return q.getCompleted()==null||t.isCompleted()==q.getCompleted();}}
