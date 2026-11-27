package com.taskmanager.exception;
public class BadGatewayException extends ApiException { private static final long serialVersionUID = 1L;
  public BadGatewayException(){super(502,"LLM_BAD_GATEWAY","The LLM returned an invalid response or could not be reached");}
  public BadGatewayException(String message){super(502,"LLM_BAD_GATEWAY",message);}
}
