package com.taskmanager.event;
import com.taskmanager.model.Task;
public interface TaskEventListener { void onEvent(String eventType,Task task); }
