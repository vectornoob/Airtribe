package com.taskmanager.exception;
public class ApiException extends RuntimeException {
 private static final long serialVersionUID = 1L;
 private final int statusCode; private final String errorCode;
 public ApiException(int statusCode,String errorCode,String message){super(message);this.statusCode=statusCode;this.errorCode=errorCode;}
 public int getStatusCode(){return statusCode;} public String getErrorCode(){return errorCode;}
}
