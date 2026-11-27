package com.taskmanager.repository;
import com.taskmanager.model.Task;import java.util.*;import java.util.concurrent.atomic.AtomicInteger;
public final class InMemoryTaskRepository implements TaskRepository {
 private static final InMemoryTaskRepository INSTANCE=new InMemoryTaskRepository();
 private final Map<Integer,Task> tasks=new LinkedHashMap<>();private final AtomicInteger nextId=new AtomicInteger(1);
 private InMemoryTaskRepository(){}
 public static InMemoryTaskRepository getInstance(){return INSTANCE;}
 public synchronized Task save(Task task){int id=task.getId()>0?task.getId():nextId.getAndIncrement();Task saved=task.toBuilder().id(id).build();tasks.put(id,saved);return saved;}
 public synchronized Optional<Task> findById(int id){return Optional.ofNullable(tasks.get(id));}
 public synchronized List<Task> findAll(){return new ArrayList<>(tasks.values());}
 public synchronized boolean delete(int id){return tasks.remove(id)!=null;}
 public synchronized void clear(){tasks.clear();nextId.set(1);}
}
