package com.airtribe.learntrack.entity;

public class Student extends Person {

    private String batch;
    private boolean active;


    public Student(int id, String firstName, String lastName,
                   String email, String batch) {
        super(id, firstName, lastName, email);
        this.batch = batch;
        this.active = true;
    }

    public Student(int id, String firstName, String lastName,
                   String email, String batch, boolean active) {
        super(id, firstName, lastName, email);
        this.batch = batch;
        this.active = active;
    }

    public String getBatch() {
        return batch;
    }

    public void setBatch(String batch) {
        this.batch = batch;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    @Override
    public void displayDetails() {
        super.displayDetails();
        System.out.println("Batch: " + batch);
        System.out.println("Active: " + active);
    }
}