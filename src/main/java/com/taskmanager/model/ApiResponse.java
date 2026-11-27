package com.taskmanager.model;
import com.taskmanager.util.Json;
public class ApiResponse {
 private final int statusCode; private final Object body;
 public ApiResponse(int statusCode,Object body){this.statusCode=statusCode;this.body=body;}
 public int getStatusCode(){return statusCode;} public Object getBody(){return body;}
 public String toJson(){return Json.stringify(body);}
 @Override public String toString(){return "ApiResponse{statusCode="+statusCode+", body="+toJson()+"}";}
}
