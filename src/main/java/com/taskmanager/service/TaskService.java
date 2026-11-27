package com.taskmanager.service;
import com.taskmanager.repository.*;import com.taskmanager.model.*;import com.taskmanager.exception.*;import com.taskmanager.event.*;import com.taskmanager.parser.*;import com.taskmanager.query.*;
import java.time.*;import java.time.format.DateTimeParseException;import java.util.*;
public class TaskService {
 private final TaskRepository repo;private final TaskParser parser;private final BreakdownStrategy breakdown;
 private final List<TaskEventListener> listeners=new ArrayList<>();
 public TaskService(TaskRepository repo,TaskParser parser,BreakdownStrategy breakdown){this.repo=repo;this.parser=parser;this.breakdown=breakdown;}
 public void addListener(TaskEventListener l){listeners.add(l);}public void removeListener(TaskEventListener l){listeners.remove(l);}
 private void notify(String type,Task t){for(TaskEventListener l:List.copyOf(listeners))try{l.onEvent(type,t);}catch(RuntimeException ignored){}}
 public Task create(TaskRequest r){Task t=buildValidated(r,null,null);Task saved=repo.save(t);notify("CREATED",saved);return saved;}
 public Task createSubtask(int parentId,TaskRequest r){Task parent=find(parentId);if(parent.getParentId()!=null)throw validation("parentId","subtask cannot have subtasks");
  Task t=buildValidated(r,parentId,null);Task saved=repo.save(t);notify("CREATED",saved);return saved;}
 public Task update(int id,TaskRequest r){Task old=find(id);Task t=buildValidated(r,old.getParentId(),old.getRecurrence()).toBuilder().id(id).build();Task saved=repo.save(t);notify("UPDATED",saved);if(saved.isCompleted()&&!old.isCompleted())notify("COMPLETED",saved);return saved;}
 public Task complete(int id){Task old=find(id);Task saved=repo.save(old.toBuilder().completed(true).build());notify("COMPLETED",saved);return saved;}
 public Task find(int id){return repo.findById(id).orElseThrow(()->new NotFoundException("Task "+id+" not found"));}
 public List<Task> all(){return repo.findAll();}
 public List<Task> subtasks(int id){find(id);List<Task> out=new ArrayList<>();for(Task t:repo.findAll())if(Objects.equals(t.getParentId(),id))out.add(t);return out;}
 public boolean delete(int id){Task t=find(id);for(Task child:subtasks(id)){repo.delete(child.getId());notify("DELETED",child);}boolean ok=repo.delete(id);notify("DELETED",t);return ok;}
 public List<Task> breakdown(int id){
  Task parent=find(id);if(parent.getParentId()!=null)throw validation("id","a subtask cannot be broken down");
  final List<TaskRequest> requests;
  try { requests=breakdown.breakdown(parent); }
  catch(RuntimeException ex){ throw new BadGatewayException("Breakdown provider failed: "+ex.getMessage()); }
  if(requests==null||requests.isEmpty())throw new BadGatewayException("Breakdown returned no subtasks");
  List<Task> validated=new ArrayList<>();
  try { for(TaskRequest request:requests)validated.add(buildValidated(request,id,null)); }
  catch(ValidationException ex){throw new BadGatewayException("Breakdown output failed validation: "+ex.getMessage());}
  // Validate the entire batch before the first repository write (all-or-nothing).
  List<Task> saved=new ArrayList<>();for(Task t:validated){Task s=repo.save(t);saved.add(s);notify("CREATED",s);}return saved;
 }
 public List<Task> query(TaskQuery q){List<Task> tasks=repo.findAll();List<TaskFilter> filters=List.of(new TaskSearchFilter(),new TaskStatusFilter(),new TaskPriorityFilter(),new TaskParentFilter());
  tasks.removeIf(t->filters.stream().anyMatch(f->!f.matches(t,q)));
  SortStrategy sort=switch(q.getSortBy().toUpperCase(Locale.ROOT)){case "TITLE"->new TaskTitleSort();case "DUE_DATE"->new TaskDueDateSort();case "PRIORITY"->new TaskPrioritySort();case "ID"->new TaskIdSort();default->throw new IllegalArgumentException("Unsupported sortBy: "+q.getSortBy());};
  sort.sort(tasks);long startLong=(long)q.getPage()*q.getSize();if(startLong>=tasks.size())return List.of();int start=(int)startLong,end=Math.min(start+q.getSize(),tasks.size());return new ArrayList<>(tasks.subList(start,end));}
 public Task createFromText(String text){if(parser==null)throw new ServiceUnavailableException();try{
  ParsedTask p=parser.parse(text,LocalDate.now().toString());TaskRequest r=new TaskRequest(p.title(),p.description(), "false",p.dueDate(),"MEDIUM");
  Task t=buildValidated(r,null,p.recurrence());Task saved=repo.save(t);notify("CREATED",saved);return saved;
 }catch(ApiException e){throw new BadGatewayException("LLM/parser output failed validation: "+e.getMessage());}catch(RuntimeException e){throw new BadGatewayException(e.getMessage());}}
 private Task buildValidated(TaskRequest r,Integer parentId,String recurrence){
  List<Map<String,String>> issues=new ArrayList<>();
  if(r==null){issue(issues,"request","must not be null");throw new ValidationException(issues);}
  String title=r.getTitle(),desc=r.getDescription();
  if(title==null||title.isBlank())issue(issues,"title","must not be empty");if(desc==null||desc.isBlank())issue(issues,"description","must not be empty");
  Boolean completed=null;if(!"true".equals(r.getCompleted())&&!"false".equals(r.getCompleted()))issue(issues,"completed","must be exactly true or false");else completed=Boolean.parseBoolean(r.getCompleted());
  LocalDate due=null;if(r.getDueDate()!=null&&!r.getDueDate().isBlank())try{due=LocalDate.parse(r.getDueDate());if(!due.toString().equals(r.getDueDate()))throw new DateTimeParseException("not canonical",r.getDueDate(),0);}catch(DateTimeParseException e){issue(issues,"dueDate","must be a real YYYY-MM-DD date");}
  Priority priority=null;try{priority=Priority.valueOf(r.getPriority()==null?"":r.getPriority());}catch(IllegalArgumentException e){issue(issues,"priority","must be LOW, MEDIUM, or HIGH");}
  if(!issues.isEmpty())throw new ValidationException(issues);
  return Task.builder().id(0).title(title.trim()).description(desc.trim()).completed(completed).dueDate(due).priority(priority).parentId(parentId).recurrence(recurrence).build();
 }
 private static void issue(List<Map<String,String>> l,String f,String i){l.add(Map.of("field",f,"issue",i));}
 private static ValidationException validation(String f,String i){List<Map<String,String>> l=new ArrayList<>();issue(l,f,i);return new ValidationException(l);}
}
