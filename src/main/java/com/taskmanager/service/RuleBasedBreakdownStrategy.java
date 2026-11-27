package com.taskmanager.service;
import com.taskmanager.model.*;import java.util.*;
public class RuleBasedBreakdownStrategy implements BreakdownStrategy {
 public List<TaskRequest> breakdown(Task task){String[] parts=task.getTitle().split("(?i)\\s+and\\s+|,");List<TaskRequest> out=new ArrayList<>();
  for(String p:parts){String title=p.trim();if(!title.isBlank())out.add(new TaskRequest(title,"Subtask for: "+task.getTitle(),"false",null,task.getPriority().name()));}
  if(out.isEmpty())out.add(new TaskRequest(task.getTitle(),"Work on: "+task.getTitle(),"false",null,task.getPriority().name()));return out;}
}
