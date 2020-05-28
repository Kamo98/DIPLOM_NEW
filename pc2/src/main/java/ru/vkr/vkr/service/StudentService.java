package ru.vkr.vkr.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.vkr.vkr.entity.Course;
import ru.vkr.vkr.entity.Student;
import ru.vkr.vkr.facade.AuthenticationFacade;
import ru.vkr.vkr.repository.CourseRepository;
import ru.vkr.vkr.repository.StudentRepository;

@Service
public class StudentService {
    @Autowired
    private StudentRepository studentRepository;
    @Autowired
    private CourseRepository courseRepository;
    @Autowired
    private AuthenticationFacade authenticationFacade;

    public Course getCourse() {
        Student student = authenticationFacade.getCurrentStudent();
        return student.getGroup().getCourseSubscriptions();
    }

}
