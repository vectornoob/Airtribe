package com.airtribe.learntrack.service;

import java.util.List;

import com.airtribe.learntrack.entity.Student;
import com.airtribe.learntrack.exception.LearnTrackException;
import com.airtribe.learntrack.repository.StudentRepository;
import com.airtribe.learntrack.util.IdGenerator;

public class StudentService {

    private final StudentRepository repository;

    public StudentService(StudentRepository repository) {
        this.repository = repository;
    }

    public Student addStudent(String firstName, String lastName,
                              String email, String batch) {
        if (firstName == null || firstName.isBlank()
                || lastName == null || lastName.isBlank()
                || email == null || email.isBlank()
                || batch == null || batch.isBlank()) {
            throw new LearnTrackException("All student fields are required.");
        }

        Student student = new Student(
                IdGenerator.nextStudentId(),
                firstName,
                lastName,
                email,
                batch
        );

        repository.save(student);
        return student;
    }

    public List<Student> getAllStudents() {
        return repository.findAll();
    }

    public Student getStudentById(int id) {
        Student student = repository.findById(id);

        if (student == null) {
            throw new LearnTrackException("Student not found: " + id);
        }

        return student;
    }

    public void updateStudent(int id, String firstName,
                              String lastName, String email, String batch) {
        Student student = getStudentById(id);

        if (firstName == null || firstName.isBlank()
                || lastName == null || lastName.isBlank()
                || email == null || email.isBlank()
                || batch == null || batch.isBlank()) {
            throw new LearnTrackException("All student fields are required.");
        }

        student.setFirstName(firstName);
        student.setLastName(lastName);
        student.setEmail(email);
        student.setBatch(batch);
    }

    public void removeStudent(int id) {
        Student student = getStudentById(id);
        student.setActive(false);
    }
}
