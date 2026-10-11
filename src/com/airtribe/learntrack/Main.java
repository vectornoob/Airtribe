package com.airtribe.learntrack;

import java.util.List;
import java.util.Scanner;

import com.airtribe.learntrack.constants.AppConstants;
import com.airtribe.learntrack.entity.Course;
import com.airtribe.learntrack.entity.Enrollment;
import com.airtribe.learntrack.entity.Student;
import com.airtribe.learntrack.enums.EnrollmentStatus;
import com.airtribe.learntrack.exception.LearnTrackException;
import com.airtribe.learntrack.repository.CourseRepository;
import com.airtribe.learntrack.repository.EnrollmentRepository;
import com.airtribe.learntrack.repository.StudentRepository;
import com.airtribe.learntrack.service.CourseService;
import com.airtribe.learntrack.service.EnrollmentService;
import com.airtribe.learntrack.service.StudentService;

public class Main {

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        StudentRepository studentRepository = new StudentRepository();
        CourseRepository courseRepository = new CourseRepository();
        EnrollmentRepository enrollmentRepository = new EnrollmentRepository();

        StudentService studentService = new StudentService(studentRepository);
        CourseService courseService = new CourseService(courseRepository);
        EnrollmentService enrollmentService = new EnrollmentService(
                enrollmentRepository, studentRepository, courseRepository);

        boolean running = true;

        System.out.println("Welcome to " + AppConstants.APP_NAME + "!");

        while (running) {
            printMainMenu();
            int choice = readInt(scanner, "Enter your choice: ");

            try {
                switch (choice) {
                    case 1:
                        manageStudents(scanner, studentService);
                        break;
                    case 2:
                        manageCourses(scanner, courseService);
                        break;
                    case 3:
                        manageEnrollments(scanner, enrollmentService,
                                studentService, courseService);
                        break;
                    case 0:
                        running = false;
                        System.out.println(AppConstants.EXIT_MESSAGE);
                        break;
                    default:
                        System.out.println("Invalid choice. Please choose 0, 1, 2, or 3.");
                }
            } catch (LearnTrackException exception) {
                System.out.println("Operation failed: " + exception.getMessage());
            } catch (RuntimeException exception) {
                System.out.println("Unexpected input or operation error: "
                        + exception.getMessage());
            }
        }

        scanner.close();
    }

    private static void printMainMenu() {
        System.out.println("\n===== LearnTrack Main Menu =====");
        System.out.println("1. Manage Students");
        System.out.println("2. Manage Courses");
        System.out.println("3. Manage Enrollments");
        System.out.println("0. Exit");
    }

    private static void manageStudents(Scanner scanner, StudentService service) {
        boolean back = false;

        while (!back) {
            System.out.println("\n--- Student Management ---");
            System.out.println("1. Add student");
            System.out.println("2. List students");
            System.out.println("3. Find student by ID");
            System.out.println("4. Update student");
            System.out.println("5. Deactivate student");
            System.out.println("0. Back to main menu");

            int choice = readInt(scanner, "Enter your choice: ");

            try {
                switch (choice) {
                    case 1: {
                        String firstName = readText(scanner, "First name: ");
                        String lastName = readText(scanner, "Last name: ");
                        String email = readText(scanner, "Email: ");
                        String batch = readText(scanner, "Batch: ");
                        Student student = service.addStudent(firstName, lastName, email, batch);
                        System.out.println("Student added successfully. ID: " + student.getId());
                        break;
                    }
                    case 2:
                        printStudents(service.getAllStudents());
                        break;
                    case 3: {
                        int id = readInt(scanner, "Student ID: ");
                        service.getStudentById(id).displayDetails();
                        break;
                    }
                    case 4: {
                        int id = readInt(scanner, "Student ID to update: ");
                        String firstName = readText(scanner, "New first name: ");
                        String lastName = readText(scanner, "New last name: ");
                        String email = readText(scanner, "New email: ");
                        String batch = readText(scanner, "New batch: ");
                        service.updateStudent(id, firstName, lastName, email, batch);
                        System.out.println("Student updated successfully.");
                        break;
                    }
                    case 5: {
                        int id = readInt(scanner, "Student ID to deactivate: ");
                        service.removeStudent(id);
                        System.out.println("Student deactivated. The record remains in memory.");
                        break;
                    }
                    case 0:
                        back = true;
                        break;
                    default:
                        System.out.println("Invalid choice.");
                }
            } catch (LearnTrackException exception) {
                System.out.println("Operation failed: " + exception.getMessage());
            }
        }
    }

    private static void printStudents(List<Student> students) {
        if (students.isEmpty()) {
            System.out.println("No students have been added yet.");
            return;
        }

        for (Student student : students) {
            student.displayDetails();
            System.out.println("--------------------");
        }
    }

    private static void manageCourses(Scanner scanner, CourseService service) {
        boolean back = false;

        while (!back) {
            System.out.println("\n--- Course Management ---");
            System.out.println("1. Add course");
            System.out.println("2. List courses");
            System.out.println("3. Find course by ID");
            System.out.println("4. Update course");
            System.out.println("5. Deactivate course");
            System.out.println("0. Back to main menu");

            int choice = readInt(scanner, "Enter your choice: ");

            try {
                switch (choice) {
                    case 1: {
                        String name = readText(scanner, "Course name: ");
                        String description = readText(scanner, "Description: ");
                        int duration = readInt(scanner, "Duration in weeks: ");
                        Course course = service.addCourse(name, description, duration);
                        System.out.println("Course added successfully. ID: " + course.getId());
                        break;
                    }
                    case 2:
                        printCourses(service.getAllCourses());
                        break;
                    case 3: {
                        int id = readInt(scanner, "Course ID: ");
                        System.out.println(service.getCourseById(id));
                        break;
                    }
                    case 4: {
                        int id = readInt(scanner, "Course ID to update: ");
                        String name = readText(scanner, "New course name: ");
                        String description = readText(scanner, "New description: ");
                        int duration = readInt(scanner, "New duration in weeks: ");
                        service.updateCourse(id, name, description, duration);
                        System.out.println("Course updated successfully.");
                        break;
                    }
                    case 5: {
                        int id = readInt(scanner, "Course ID to deactivate: ");
                        service.removeCourse(id);
                        System.out.println("Course deactivated. The record remains in memory.");
                        break;
                    }
                    case 0:
                        back = true;
                        break;
                    default:
                        System.out.println("Invalid choice.");
                }
            } catch (LearnTrackException exception) {
                System.out.println("Operation failed: " + exception.getMessage());
            }
        }
    }

    private static void printCourses(List<Course> courses) {
        if (courses.isEmpty()) {
            System.out.println("No courses have been added yet.");
            return;
        }

        for (Course course : courses) {
            System.out.println(course);
        }
    }

    private static void manageEnrollments(Scanner scanner,
                                          EnrollmentService enrollmentService,
                                          StudentService studentService,
                                          CourseService courseService) {
        boolean back = false;

        while (!back) {
            System.out.println("\n--- Enrollment Management ---");
            System.out.println("1. Enroll student in course");
            System.out.println("2. List enrollments");
            System.out.println("3. Update enrollment status");
            System.out.println("4. Cancel enrollment");
            System.out.println("0. Back to main menu");

            int choice = readInt(scanner, "Enter your choice: ");

            try {
                switch (choice) {
                    case 1: {
                        System.out.println("Available students:");
                        printStudents(studentService.getAllStudents());
                        System.out.println("Available courses:");
                        printCourses(courseService.getAllCourses());

                        int studentId = readInt(scanner, "Student ID: ");
                        int courseId = readInt(scanner, "Course ID: ");
                        Enrollment enrollment = enrollmentService.enrollStudent(studentId, courseId);
                        System.out.println("Enrollment created successfully. ID: "
                                + enrollment.getId());
                        break;
                    }
                    case 2: {
                        List<Enrollment> enrollments = enrollmentService.getAllEnrollments();
                        if (enrollments.isEmpty()) {
                            System.out.println("No enrollments have been created yet.");
                        } else {
                            for (Enrollment enrollment : enrollments) {
                                System.out.println(enrollment);
                            }
                        }
                        break;
                    }
                    case 3: {
                        int id = readInt(scanner, "Enrollment ID: ");
                        System.out.println("1. ACTIVE");
                        System.out.println("2. COMPLETED");
                        System.out.println("3. CANCELLED");
                        int statusChoice = readInt(scanner, "Choose status: ");
                        EnrollmentStatus status;

                        if (statusChoice == 1) {
                            status = EnrollmentStatus.ACTIVE;
                        } else if (statusChoice == 2) {
                            status = EnrollmentStatus.COMPLETED;
                        } else if (statusChoice == 3) {
                            status = EnrollmentStatus.CANCELLED;
                        } else {
                            System.out.println("Invalid status choice.");
                            break;
                        }

                        enrollmentService.updateStatus(id, status);
                        System.out.println("Enrollment status updated.");
                        break;
                    }
                    case 4: {
                        int id = readInt(scanner, "Enrollment ID to cancel: ");
                        enrollmentService.cancelEnrollment(id);
                        System.out.println("Enrollment cancelled.");
                        break;
                    }
                    case 0:
                        back = true;
                        break;
                    default:
                        System.out.println("Invalid choice.");
                }
            } catch (LearnTrackException exception) {
                System.out.println("Operation failed: " + exception.getMessage());
            }
        }
    }

    private static String readText(Scanner scanner, String prompt) {
        System.out.print(prompt);
        return scanner.nextLine().trim();
    }

    private static int readInt(Scanner scanner, String prompt) {
        while (true) {
            System.out.print(prompt);
            String input = scanner.nextLine().trim();

            try {
                return Integer.parseInt(input);
            } catch (NumberFormatException exception) {
                System.out.println("Please enter a whole number.");
            }
        }
    }
}
