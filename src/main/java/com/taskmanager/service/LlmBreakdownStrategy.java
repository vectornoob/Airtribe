package com.taskmanager.service;
import com.taskmanager.model.*;import com.taskmanager.parser.LlmClient;import java.util.*;
public class LlmBreakdownStrategy implements BreakdownStrategy {
 private final LlmClient client;public LlmBreakdownStrategy(LlmClient client){this.client=client;}
 public List<TaskRequest> breakdown(Task task){String prompt="Break this task into 2-5 actionable subtasks. Return one per line exactly title|description, no numbering or other text. Task: "+task.getTitle()+" - "+task.getDescription();
  String reply=client.complete(prompt);List<TaskRequest> result=new ArrayList<>();
  for(String line:reply.split("\\R")){String[] p=line.split("\\|",2);if(p.length==2)result.add(new TaskRequest(p[0].trim(),p[1].trim(),"false",null,task.getPriority().name()));}
  return result;
 }
}
