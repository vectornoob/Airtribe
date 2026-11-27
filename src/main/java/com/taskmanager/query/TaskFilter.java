package com.taskmanager.query;
import com.taskmanager.model.Task;
public interface TaskFilter { boolean matches(Task task,TaskQuery query); }
