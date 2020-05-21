package ru.vkr.vkr.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import ru.vkr.vkr.entity.Course;
import ru.vkr.vkr.entity.Group;
import ru.vkr.vkr.service.CourseService;
import ru.vkr.vkr.service.GroupService;

import java.util.Collection;

@Controller
public class ProblemController {
    @Autowired
    private CourseService courseService;
    @Autowired
    private GroupService groupService;

    @ModelAttribute
    public void addAttributes(Model model) {
        Collection<Course> teacherCourses = courseService.getCoursesByCurrentTeacher();
        Collection<Group> teacherGroups = groupService.getGroupsByCurrentTeacher();
        model.addAttribute("teacherCourses", teacherCourses);
        model.addAttribute("teacherGroups", teacherGroups);
    }

    @GetMapping("/teacher/problem")
    public String getProblem() {
        return "teacher/problem";
    }

}
