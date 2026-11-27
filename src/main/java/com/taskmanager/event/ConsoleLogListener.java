package com.taskmanager.event;
import com.taskmanager.model.Task;
public class ConsoleLogListener implements TaskEventListener {public void onEvent(String type,Task task){System.out.println("[EVENT] "+type+" task#"+task.getId());}}
