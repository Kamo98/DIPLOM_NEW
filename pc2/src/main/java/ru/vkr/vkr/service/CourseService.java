package ru.vkr.vkr.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.vkr.vkr.entity.Course;
import ru.vkr.vkr.entity.Group;
import ru.vkr.vkr.facade.AuthenticationFacade;
import ru.vkr.vkr.repository.CourseRepository;
import ru.vkr.vkr.repository.GroupRepository;

import java.util.Collection;

@Service
public class CourseService {
    @Autowired
    private CourseRepository courseRepository;
    @Autowired
    private AuthenticationFacade authenticationFacade;
    @Autowired
    private GroupRepository groupRepository;



    private static Logger logger = LoggerFactory.getLogger(UserService.class);

    public void saveCourse(Course course) {
        logger.info("save course " + course.toString());
        courseRepository.save(course);
    }

    public void deleteCourse(Course course) {
        logger.info("delete course " + course.toString());
        courseRepository.delete(course);
    }

    //Устанавливает автора создаваемого курса
    public void setAuthorForNewCourse(Course course){
        //Автор курса - текущий пользователь (преподаватель)
        course.setTeacherAuthor(authenticationFacade.getCurrentTeacher());
    }


    public Course getCourseById(Long idCourse) {
        return courseRepository.getOne(idCourse);
    }

    public Collection<Course> getCoursesByCurrentTeacher() {
        return courseRepository.findByTeacherAuthor_id(authenticationFacade.getCurrentTeacher().getId());
    }

    public boolean containsGroup(Course course, Group group) {
        return course.getSubscribers().contains(group);
    }

    public void signUpForCourse(Course course, Group group) {
        course.getSubscribers().add(group);
    }

    public void signDownForCourse(Course course, Group group) {
        course.getSubscribers().remove(group);
    }
}
