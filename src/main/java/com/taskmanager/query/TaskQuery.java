package com.taskmanager.query;
public class TaskQuery {
 private final String search;private final Boolean completed;private final String priority;private final Integer parentId;private final int page,size;private final String sortBy;
 private TaskQuery(Builder b){search=b.search;completed=b.completed;priority=b.priority;parentId=b.parentId;page=b.page;size=b.size;sortBy=b.sortBy;}
 public String getSearch(){return search;}public Boolean getCompleted(){return completed;}public String getPriority(){return priority;}public Integer getParentId(){return parentId;}public int getPage(){return page;}public int getSize(){return size;}public String getSortBy(){return sortBy;}
 public static Builder builder(){return new Builder();}
 public static class Builder {private String search,priority,sortBy="TITLE";private Boolean completed;private Integer parentId;private int page=0,size=10;
  public Builder search(String v){search=v;return this;}public Builder completed(Boolean v){completed=v;return this;}public Builder priority(String v){priority=v;return this;}public Builder parentId(Integer v){parentId=v;return this;}public Builder page(int v){page=v;return this;}public Builder size(int v){size=v;return this;}public Builder sortBy(String v){sortBy=v==null?"TITLE":v;return this;}
  public TaskQuery build(){if(page<0||size<1)throw new IllegalArgumentException("page must be >=0 and size >=1");return new TaskQuery(this);}
 }
}
