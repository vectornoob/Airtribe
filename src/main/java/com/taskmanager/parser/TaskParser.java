package com.taskmanager.parser;
import com.taskmanager.model.ParsedTask;
public interface TaskParser { ParsedTask parse(String text,String today); }
