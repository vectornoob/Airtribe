package com.airtribe.learntrack.util;

public class IdGenerator {

    private static int studentCounter = 1;
    private static int courseCounter = 1;
    private static int enrollmentCounter = 1;

    private IdGenerator() {
    }

    public static int nextStudentId() {
        return studentCounter++;
    }

    public static int nextCourseId() {
        return courseCounter++;
    }

    public static int nextEnrollmentId() {
        return enrollmentCounter++;
    }
}