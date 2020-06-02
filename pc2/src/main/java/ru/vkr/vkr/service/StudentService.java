package ru.vkr.vkr.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.vkr.vkr.entity.*;
import ru.vkr.vkr.facade.AuthenticationFacade;
import ru.vkr.vkr.repository.CourseRepository;
import ru.vkr.vkr.repository.StudentRepository;

import java.util.List;
import java.util.Set;

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

    public Set<Problem> getProblemByChapter(Chapter chapter) {
        return chapter.getProblems();
    }

    public Set<Theory> getTheoryByChapter(Chapter chapter) {
        return chapter.getChapterTheories();
    }

}
