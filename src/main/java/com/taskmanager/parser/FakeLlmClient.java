package com.taskmanager.parser;
import java.util.concurrent.atomic.AtomicInteger;
/** Deterministic test double: returns a canned JSON task reply and counts calls. */
public class FakeLlmClient implements LlmClient {
 private final String reply;private final AtomicInteger calls=new AtomicInteger();
 public FakeLlmClient(){this("{\"title\":\"Prepare presentation\",\"description\":\"Prepare slides for the meeting\",\"dueDate\":null,\"recurrence\":null}");}
 public FakeLlmClient(String reply){this.reply=reply;}
 public String complete(String prompt){calls.incrementAndGet();return reply;}
 public int getCallCount(){return calls.get();}
}
