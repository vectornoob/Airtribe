package com.taskmanager.exception;
public class ServiceUnavailableException extends ApiException { private static final long serialVersionUID = 1L;
  public ServiceUnavailableException(){super(503,"SERVICE_UNAVAILABLE","Required LLM configuration is missing");}
  public ServiceUnavailableException(String message){super(503,"SERVICE_UNAVAILABLE",message);}
}
