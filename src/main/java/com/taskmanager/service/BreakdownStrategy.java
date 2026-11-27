package com.taskmanager.service;
import com.taskmanager.model.Task;import com.taskmanager.model.TaskRequest;import java.util.List;
public interface BreakdownStrategy { List<TaskRequest> breakdown(Task task); }
