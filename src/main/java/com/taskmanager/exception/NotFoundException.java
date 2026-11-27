package com.taskmanager.exception;
public class NotFoundException extends ApiException { private static final long serialVersionUID = 1L;
  public NotFoundException(){super(404,"NOT_FOUND","Resource not found");}
  public NotFoundException(String message){super(404,"NOT_FOUND",message);}
}
