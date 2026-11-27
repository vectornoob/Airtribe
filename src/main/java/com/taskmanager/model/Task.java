package com.taskmanager.model;

import java.time.LocalDate;
import java.util.Objects;
import java.util.Optional;

public final class Task {
    private final int id;
    private final String title, description;
    private final boolean completed;
    private final LocalDate dueDate;
    private final Priority priority;
    private final Integer parentId;
    private final String recurrence;

    private Task(Builder b) {
        id=b.id; title=b.title; description=b.description; completed=b.completed;
        dueDate=b.dueDate; priority=b.priority; parentId=b.parentId; recurrence=b.recurrence;
    }
    public int getId(){return id;} public String getTitle(){return title;}
    public String getDescription(){return description;} public boolean isCompleted(){return completed;}
    public LocalDate getDueDate(){return dueDate;} public Priority getPriority(){return priority;}
    public Integer getParentId(){return parentId;} public String getRecurrence(){return recurrence;}
    public Builder toBuilder(){return new Builder().id(id).title(title).description(description).completed(completed)
        .dueDate(dueDate).priority(priority).parentId(parentId).recurrence(recurrence);}
    public static Builder builder(){return new Builder();}
    public static final class Builder {
        private int id; private String title,description; private Boolean completed;
        private LocalDate dueDate; private Priority priority=Priority.MEDIUM; private Integer parentId; private String recurrence;
        public Builder id(int v){id=v;return this;} public Builder title(String v){title=v;return this;}
        public Builder description(String v){description=v;return this;} public Builder completed(boolean v){completed=v;return this;}
        public Builder dueDate(LocalDate v){dueDate=v;return this;} public Builder priority(Priority v){priority=v;return this;}
        public Builder parentId(Integer v){parentId=v;return this;} public Builder recurrence(String v){recurrence=v;return this;}
        public Task build(){Objects.requireNonNull(title,"title");Objects.requireNonNull(description,"description");
            if(completed==null)throw new IllegalStateException("completed is required");
            return new Task(this);}
    }
    @Override public String toString(){return "Task{id="+id+", title='"+title+"', completed="+completed+
        ", dueDate="+dueDate+", priority="+priority+", parentId="+parentId+"}";}
}
