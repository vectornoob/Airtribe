package com.taskmanager.exception;
import java.util.*;
@SuppressWarnings("serial")
public class ValidationException extends ApiException {
 private static final long serialVersionUID = 1L;
 private final List<Map<String,String>> details;
 public ValidationException(List<Map<String,String>> details){super(400,"VALIDATION_ERROR","Validation failed");this.details=List.copyOf(details);}
 public List<Map<String,String>> getDetails(){return details;}
}
