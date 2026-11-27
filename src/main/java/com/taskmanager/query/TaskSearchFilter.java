package com.taskmanager.query;
import com.taskmanager.model.Task;
public class TaskSearchFilter implements TaskFilter {public boolean matches(Task t,TaskQuery q){return q.getSearch()==null||q.getSearch().isBlank()||(t.getTitle()+" "+t.getDescription()).toLowerCase().contains(q.getSearch().toLowerCase());}}
