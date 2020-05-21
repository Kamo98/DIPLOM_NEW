package ru.vkr.vkr.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
//import ru.vkr.vkr.contest.Test;
import ru.vkr.vkr.entity.Course;

import java.util.ArrayList;
import java.util.List;

@Controller
public class StudentController {

    @ModelAttribute
    public void addAttributes(Model model) {
        List<Course> studentCourses = new ArrayList<>();
        model.addAttribute("studentCourses", studentCourses);
    }

    @GetMapping("/student")
    public String mainStudent(Model model) {
        return "student/main";
    }


    @GetMapping("/student/course")
    public String getCourse(Model model) {
        return "student/course";
    }

    @GetMapping("/student/theme")
    public String getTheme(Model model) {
        return "student/theme";
    }


    @GetMapping("/student/problem")
    public String getProblem(Model model) {
        return "student/problem";
    }
}
