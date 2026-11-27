package com.taskmanager.api;
import com.taskmanager.model.*;import com.taskmanager.service.*;import com.taskmanager.exception.*;import com.taskmanager.query.*;
import java.util.*;
public class TaskApi {
 private final TaskService service;private final IdempotencyStore store=IdempotencyStore.getInstance();
 public TaskApi(TaskService service){this.service=service;}
 public ApiResponse createTask(TaskRequest r){return createTask(r,null);}
 public ApiResponse createTask(TaskRequest r,String key){return idem(key,r==null?"<null-request>":r.fingerprint(),()->ok(201,service.create(r)));}
 public ApiResponse listTasks(){return call(200,()->service.all());}
 public ApiResponse getTask(int id){return call(200,()->service.find(id));}
 public ApiResponse updateTask(int id,TaskRequest r){return call(200,()->service.update(id,r));}
 public ApiResponse completeTask(int id){return call(200,()->service.complete(id));}
 public ApiResponse deleteTask(int id){return call(200,()->Map.of("deleted",service.delete(id),"id",id));}
 public ApiResponse createSubtask(int parentId,TaskRequest r){return call(201,()->service.createSubtask(parentId,r));}
 public ApiResponse listSubtasks(int parentId){return call(200,()->service.subtasks(parentId));}
 public ApiResponse breakDownTask(int id){return breakDownTask(id,null);}
 public ApiResponse breakDownTask(int id,String key){return idem(key,"breakdown:"+id,()->ok(201,service.breakdown(id)));}
 public ApiResponse createTaskFromText(String text){return createTaskFromText(text,null);}
 public ApiResponse createTaskFromText(String text,String key){return idem(key,"text:"+String.valueOf(text),()->ok(201,service.createFromText(text)));}
 public ApiResponse queryTasks(TaskQuery q){return call(200,()->service.query(q));}
 private ApiResponse call(int status,Action action){try{return ok(status,action.run());}catch(ApiException e){return new ApiResponse(e.getStatusCode(),ErrorResponseFactory.from(e));}catch(Exception e){return new ApiResponse(500,ErrorResponseFactory.internal("Internal server error"));}}
 private ApiResponse idem(String key,String fingerprint,Action action){if(key==null||key.isBlank())return executeApi(action);
  synchronized(store){
   try{IdempotencyStore.Entry e=store.get(key);if(e!=null){if(!e.fingerprint().equals(fingerprint))throw new ConflictException();return e.response();}
    ApiResponse response=executeApi(action);if(response.getStatusCode()>=200&&response.getStatusCode()<300)store.put(key,fingerprint,response);return response;
   }catch(ApiException e){return new ApiResponse(e.getStatusCode(),ErrorResponseFactory.from(e));}
  }}
 private ApiResponse executeApi(Action action){try{return (ApiResponse)action.run();}catch(ApiException e){return new ApiResponse(e.getStatusCode(),ErrorResponseFactory.from(e));}catch(Exception e){return new ApiResponse(500,ErrorResponseFactory.internal("Internal server error"));}}
 private ApiResponse ok(int status,Object body){return new ApiResponse(status,toJsonBody(body));}
 private Object toJsonBody(Object body){if(body instanceof Task t)return taskMap(t);if(body instanceof List<?> l){List<Object> out=new ArrayList<>();for(Object x:l)out.add(toJsonBody(x));return out;}return body;}
 private Map<String,Object> taskMap(Task t){Map<String,Object> m=new LinkedHashMap<>();m.put("id",t.getId());m.put("title",t.getTitle());m.put("description",t.getDescription());m.put("completed",t.isCompleted());m.put("dueDate",t.getDueDate()==null?null:t.getDueDate().toString());m.put("priority",t.getPriority().name());m.put("parentId",t.getParentId());m.put("recurrence",t.getRecurrence());return m;}
 @FunctionalInterface private interface Action{Object run();}
}
