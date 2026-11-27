package com.taskmanager.util;
import java.util.*;
public final class Json {
 private Json(){}
 public static String stringify(Object v){
  if(v==null)return "null"; if(v instanceof String s)return quote(s); if(v instanceof Number||v instanceof Boolean)return v.toString();
  if(v instanceof Map<?,?> m){StringJoiner j=new StringJoiner(",","{","}");m.forEach((k,x)->j.add(quote(String.valueOf(k))+":"+stringify(x)));return j.toString();}
  if(v instanceof Iterable<?> it){StringJoiner j=new StringJoiner(",","[","]");for(Object x:it)j.add(stringify(x));return j.toString();}
  return quote(String.valueOf(v));
 }
 public static String quote(String s){StringBuilder b=new StringBuilder("\"");
  for(char c:s.toCharArray()){switch(c){case '"'->b.append("\\\"");case '\\'->b.append("\\\\");case '\n'->b.append("\\n");case '\r'->b.append("\\r");case '\t'->b.append("\\t");default->{if(c<32)b.append(String.format("\\u%04x",(int)c));else b.append(c);}}}return b.append('"').toString();}
 // Small JSON object reader sufficient for the task parser's flat string fields.
 public static Map<String,String> readStringObject(String json){
  Map<String,String> out=new LinkedHashMap<>(); int i=skip(json,0);
  if(i>=json.length()||json.charAt(i++)!='{')throw new IllegalArgumentException("Expected JSON object");
  while(true){i=skip(json,i);if(i<json.length()&&json.charAt(i)=='}')return out;
   Parsed key=parseString(json,i);i=skip(json,key.end);if(i>=json.length()||json.charAt(i++)!=':')throw new IllegalArgumentException("Expected colon");
   i=skip(json,i);String val;if(i<json.length()&&json.charAt(i)=='"'){Parsed p=parseString(json,i);val=p.value;i=p.end;}
   else {int start=i;while(i<json.length()&&json.charAt(i)!=','&&json.charAt(i)!='}')i++;String raw=json.substring(start,i).trim();val=raw.equals("null")?null:raw;}
   out.put(key.value,val);i=skip(json,i);if(i<json.length()&&json.charAt(i)==','){i++;continue;}if(i<json.length()&&json.charAt(i)=='}')return out;throw new IllegalArgumentException("Expected comma or }");
  }
 }
 private record Parsed(String value,int end){}
 private static Parsed parseString(String s,int i){if(s.charAt(i)!='"')throw new IllegalArgumentException("Expected string");StringBuilder b=new StringBuilder();i++;
  while(i<s.length()){char c=s.charAt(i++);if(c=='"')return new Parsed(b.toString(),i);if(c=='\\'){if(i>=s.length())break;char e=s.charAt(i++);switch(e){case '"','\\','/'->b.append(e);case 'n'->b.append('\n');case 'r'->b.append('\r');case 't'->b.append('\t');case 'b'->b.append('\b');case 'f'->b.append('\f');case 'u'->{if(i+4>s.length())throw new IllegalArgumentException("Bad unicode");b.append((char)Integer.parseInt(s.substring(i,i+4),16));i+=4;}default->throw new IllegalArgumentException("Bad escape");}}else b.append(c);}throw new IllegalArgumentException("Unclosed string");}
 private static int skip(String s,int i){while(i<s.length()&&Character.isWhitespace(s.charAt(i)))i++;return i;}
}
