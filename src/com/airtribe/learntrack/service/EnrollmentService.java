package com.airtribe.learntrack.service;

import java.time.LocalDate;
import java.util.List;

import com.airtribe.learntrack.entity.Course;
import com.airtribe.learntrack.entity.Enrollment;
import com.airtribe.learntrack.entity.Student;
import com.airtribe.learntrack.enums.EnrollmentStatus;
import com.airtribe.learntrack.exception.LearnTrackException;
import com.airtribe.learntrack.repository.EnrollmentRepository;
import com.airtribe.learntrack.repository.StudentRepository;
import com.airtribe.learntrack.repository.CourseRepository;
import com.airtribe.learntrack.util.IdGenerator;

public class EnrollmentService {

    private final EnrollmentRepository enrollmentRepository;
    private final StudentRepository studentRepository;
    private final CourseRepository courseRepository;

    public EnrollmentService(
            EnrollmentRepository enrollmentRepository,
            StudentRepository studentRepository,
            CourseRepository courseRepository) {
        this.enrollmentRepository = enrollmentRepository;
        this.studentRepository = studentRepository;
        this.courseRepository = courseRepository;
    }

    public Enrollment enrollStudent(int studentId, int courseId) {
        Student student = studentRepository.findById(studentId);
        Course course = courseRepository.findById(courseId);

        if (student == null || !student.isActive()) {
            throw new LearnTrackException(
                    "Student does not exist or is inactive."
            );
        }

        if (course == null || !course.isActive()) {
            throw new LearnTrackException(
                    "Course does not exist or is inactive."
            );
        }

        for (Enrollment existing : enrollmentRepository.findAll()) {
            if (existing.getStudentId() == studentId
                    && existing.getCourseId() == courseId
                    && existing.getStatus() == EnrollmentStatus.ACTIVE) {
                throw new LearnTrackException(
                        "Student is already actively enrolled in this course."
                );
            }
        }

        Enrollment enrollment = new Enrollment(
                IdGenerator.nextEnrollmentId(),
                studentId,
                courseId,
                LocalDate.now(),
                EnrollmentStatus.ACTIVE
        );

        enrollmentRepository.save(enrollment);
        return enrollment;
    }

    public List<Enrollment> getAllEnrollments() {
        return enrollmentRepository.findAll();
    }

    public void updateStatus(int id, EnrollmentStatus status) {
        Enrollment enrollment = enrollmentRepository.findById(id);

        if (enrollment == null) {
            throw new LearnTrackException("Enrollment not found: " + id);
        }

        if (status == null) {
            throw new LearnTrackException("Enrollment status is required.");
        }

        enrollment.setStatus(status);
    }

    public void cancelEnrollment(int id) {
        updateStatus(id, EnrollmentStatus.CANCELLED);
    }
}
