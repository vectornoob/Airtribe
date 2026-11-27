package com.taskmanager.query;
import com.taskmanager.model.Task;import java.util.Objects;
public class TaskParentFilter implements TaskFilter {public boolean matches(Task task,TaskQuery query){return query.getParentId()==null||Objects.equals(query.getParentId(),task.getParentId());}}
