package com.taskmanager.exception;
public class ConflictException extends ApiException { private static final long serialVersionUID = 1L;
  public ConflictException(){super(422,"IDEMPOTENCY_CONFLICT","Idempotency key was used with a different request");}
  public ConflictException(String message){super(422,"IDEMPOTENCY_CONFLICT",message);}
}
