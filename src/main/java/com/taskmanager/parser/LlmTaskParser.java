package com.taskmanager.parser;
import com.taskmanager.model.ParsedTask;import com.taskmanager.util.SimpleJson;import java.util.Map;
public class LlmTaskParser implements TaskParser {
 private final LlmClient client;public LlmTaskParser(LlmClient client){this.client=client;}
 public ParsedTask parse(String text,String today){
  String prompt="Today's date is "+today+". Convert the user's request into one task. Return ONLY a JSON object with string fields title, description, dueDate, recurrence. Use null for unknown optional fields. Do not invent a date. User request: "+text;
  Map<String,String> m=SimpleJson.readStringObject(client.complete(prompt));
  return new ParsedTask(m.get("title"),m.get("description"),m.get("dueDate"),m.get("recurrence"));
 }
}
