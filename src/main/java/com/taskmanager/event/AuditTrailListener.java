package com.taskmanager.event;
import com.taskmanager.model.Task;import java.util.*;
public class AuditTrailListener implements TaskEventListener {
 private final Map<String,Integer> counts=new LinkedHashMap<>();
 public synchronized void onEvent(String type,Task task){counts.merge(type,1,Integer::sum);}
 public synchronized Map<String,Integer> getCounts(){return new LinkedHashMap<>(counts);}
}
