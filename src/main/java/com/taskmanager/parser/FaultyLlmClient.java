package com.taskmanager.parser;
/** Test double for malformed model output or simulated provider failure. */
public class FaultyLlmClient implements LlmClient {
 private final String reply;private final boolean throwsException;
 public FaultyLlmClient(String reply){this.reply=reply;this.throwsException=false;}
 public FaultyLlmClient(){this.reply=null;this.throwsException=true;}
 public String complete(String prompt){if(throwsException)throw new RuntimeException("Simulated LLM timeout");return reply;}
}
