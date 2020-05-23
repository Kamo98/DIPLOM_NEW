package ru.vkr.vkr.controller;

import edu.csus.ecs.pc2.core.model.Problem;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import ru.vkr.vkr.entity.Course;
import ru.vkr.vkr.entity.Group;
import ru.vkr.vkr.facade.ProblemFacade;
import ru.vkr.vkr.service.CourseService;
import ru.vkr.vkr.service.GroupService;

import java.util.Collection;

@Controller
public class ProblemController {
    @Autowired
    private CourseService courseService;
    @Autowired
    private GroupService groupService;
    @Autowired
    private ProblemFacade problemFacade;

    @ModelAttribute
    public void addAttributes(Model model) {
        Collection<Course> teacherCourses = courseService.getCoursesByCurrentTeacher();
        Collection<Group> teacherGroups = groupService.getGroupsByCurrentTeacher();
        model.addAttribute("teacherCourses", teacherCourses);
        model.addAttribute("teacherGroups", teacherGroups);
    }

    @GetMapping("/teacher/problem")
    public String getProblem(Model model) {
        Collection<Problem> problems = problemFacade.getAllProblems();
        model.addAttribute("problems", problems);
        return "teacher/problem";
    }


    @GetMapping("/teacher/problem-create")
    public String problemCreateGet(Model model) {
        model.addAttribute("isCreate", true);
        problemFacade.addMyProblem();
        return "teacher/problem";
    }

}
