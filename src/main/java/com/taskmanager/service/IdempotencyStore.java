package com.taskmanager.service;
import com.taskmanager.model.ApiResponse;import java.util.*;
public final class IdempotencyStore {
 private static final IdempotencyStore INSTANCE=new IdempotencyStore();
 public record Entry(String fingerprint,ApiResponse response){}
 private final Map<String,Entry> entries=new HashMap<>();
 private IdempotencyStore(){}
 public static IdempotencyStore getInstance(){return INSTANCE;}
 public synchronized Entry get(String key){return entries.get(key);}
 public synchronized void put(String key,String fingerprint,ApiResponse response){entries.put(key,new Entry(fingerprint,response));}
 public synchronized void clear(){entries.clear();}
}
