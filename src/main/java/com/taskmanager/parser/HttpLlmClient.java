package com.taskmanager.parser;
import java.net.URI;import java.net.http.*;import java.time.Duration;import com.taskmanager.util.Json;
public class HttpLlmClient implements LlmClient {
 private final String provider,key;private final HttpClient http=HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(15)).build();
 public HttpLlmClient(String provider,String key){this.provider=provider;this.key=key;}
 public String complete(String prompt){
  try{
   String url,body;HttpRequest.Builder b;
   if("openai".equalsIgnoreCase(provider)){
    url=System.getenv().getOrDefault("OPENAI_BASE_URL","https://api.openai.com/v1/chat/completions");
    body="{\"model\":\""+escape(System.getenv().getOrDefault("OPENAI_MODEL","gpt-6.2-sol"))+"\",\"messages\":[{\"role\":\"user\",\"content\":"+Json.quote(prompt)+"}]}";
    b=HttpRequest.newBuilder(URI.create(url)).header("Authorization","Bearer "+key).header("Content-Type","application/json");
   } else if("anthropic".equalsIgnoreCase(provider)){
    url="https://api.anthropic.com/v1/messages";
    body="{\"model\":\""+escape(System.getenv().getOrDefault("ANTHROPIC_MODEL","claude-haiku-5-5"))+"\",\"max_tokens\":800,\"messages\":[{\"role\":\"user\",\"content\":"+Json.quote(prompt)+"}]}";
    b=HttpRequest.newBuilder(URI.create(url)).header("x-api-key",key).header("anthropic-version","2023-06-01").header("Content-Type","application/json");
   } else if("gemini".equalsIgnoreCase(provider)){
    String model=System.getenv().getOrDefault("GEMINI_MODEL","gemini-3.8-flash");
    url="https://generativelanguage.googleapis.com/v1beta/models/"+model+":generateContent?key="+java.net.URLEncoder.encode(key,java.nio.charset.StandardCharsets.UTF_8);
    body="{\"contents\":[{\"parts\":[{\"text\":"+Json.quote(prompt)+"}]}]}";
    b=HttpRequest.newBuilder(URI.create(url)).header("Content-Type","application/json");
   } else throw new IllegalArgumentException("Set LLM_PROVIDER to openai, anthropic, or gemini.");
   HttpRequest req=b.timeout(Duration.ofSeconds(45)).POST(HttpRequest.BodyPublishers.ofString(body)).build();
   HttpResponse<String> response=http.send(req,HttpResponse.BodyHandlers.ofString());
   if(response.statusCode()<200||response.statusCode()>=300)throw new IllegalStateException("LLM HTTP "+response.statusCode()+": "+response.body());
   // Extract common text content safely from JSON response.
   String raw=response.body();boolean textField=!"openai".equalsIgnoreCase(provider);int marker=raw.indexOf(textField?"\"text\":":"\"content\":");
   if(marker<0)throw new IllegalStateException("No text content in LLM response");
   int start=raw.indexOf('"',marker+(textField?7:10));if(start<0)throw new IllegalStateException("No text content in LLM response");
   StringBuilder out=new StringBuilder();for(int i=start+1;i<raw.length();i++){char c=raw.charAt(i);if(c=='"')break;if(c=='\\'&&i+1<raw.length()){char n=raw.charAt(++i);out.append(switch(n){case 'n'->'\n';case 'r'->'\r';case 't'->'\t';default->n;});}else out.append(c);}return out.toString();
  }catch(Exception e){throw new RuntimeException("LLM call failed: "+e.getMessage(),e);}
 }
 private String escape(String s){return s.replace("\\","\\\\").replace("\"","\\\"");}
}
