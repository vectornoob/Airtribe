package com.airtribe.learntrack.service;

import java.util.List;

import com.airtribe.learntrack.entity.Course;
import com.airtribe.learntrack.exception.LearnTrackException;
import com.airtribe.learntrack.repository.CourseRepository;
import com.airtribe.learntrack.util.IdGenerator;

public class CourseService {

    private final CourseRepository repository;

    public CourseService(CourseRepository repository) {
        this.repository = repository;
    }

    public Course addCourse(String name, String description, int duration) {
        if (name == null || name.isBlank()
                || description == null || description.isBlank()
                || duration <= 0) {
            throw new LearnTrackException(
                    "Enter a course name, description, and positive duration."
            );
        }

        Course course = new Course(
                IdGenerator.nextCourseId(),
                name,
                description,
                duration
        );

        repository.save(course);
        return course;
    }

    public List<Course> getAllCourses() {
        return repository.findAll();
    }

    public Course getCourseById(int id) {
        Course course = repository.findById(id);

        if (course == null) {
            throw new LearnTrackException("Course not found: " + id);
        }

        return course;
    }

    public void updateCourse(int id, String name,
                             String description, int duration) {
        Course course = getCourseById(id);

        if (name == null || name.isBlank()
                || description == null || description.isBlank()
                || duration <= 0) {
            throw new LearnTrackException(
                    "Enter a course name, description, and positive duration."
            );
        }

        course.setCourseName(name);
        course.setDescription(description);
        course.setDurationInWeeks(duration);
    }

    public void removeCourse(int id) {
        Course course = getCourseById(id);
        course.setActive(false);
    }
}