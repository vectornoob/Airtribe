package com.taskmanager.api;
import com.taskmanager.exception.*;
import java.util.*;
public final class ErrorResponseFactory {
 private ErrorResponseFactory(){}
 public static Map<String,Object> from(ApiException e){
  Map<String,Object> error=new LinkedHashMap<>();error.put("code",e.getErrorCode());error.put("message",e.getMessage());
  List<Map<String,String>> details=e instanceof ValidationException v?v.getDetails():List.of();
  error.put("details",details);return Map.of("error",error);
 }
 public static Map<String,Object> internal(String message){
  return Map.of("error",Map.of("code","INTERNAL_ERROR","message",message,"details",List.of()));
 }
}
