package com.taskmanager.repository;
import com.taskmanager.model.Task;import java.util.*;
public interface TaskRepository {
 Task save(Task task); Optional<Task> findById(int id); List<Task> findAll(); boolean delete(int id); void clear();
}
