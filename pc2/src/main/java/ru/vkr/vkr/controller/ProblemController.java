package ru.vkr.vkr.controller;

import edu.csus.ecs.pc2.core.model.Problem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.multipart.MultipartFile;
import ru.vkr.vkr.entity.Course;
import ru.vkr.vkr.entity.Group;
import ru.vkr.vkr.facade.ProblemFacade;
import ru.vkr.vkr.form.LoadTestsForm;
import ru.vkr.vkr.service.CourseService;
import ru.vkr.vkr.service.GroupService;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.UUID;

@Controller
public class ProblemController {
    private static Logger logger = LoggerFactory.getLogger(AdminController.class);

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
        //Для тестов
        LoadTestsForm loadTestsForm = new LoadTestsForm();
        model.addAttribute("loadTestsForm", loadTestsForm);


        return "teacher/problem";
    }


    @GetMapping("/teacher/problem-create")
    public String problemCreateGet(Model model) {
        model.addAttribute("isCreate", true);
        problemFacade.addMyProblem();
        return "teacher/problem";
    }


    @PostMapping("/teacher/problem/tests-upload")
    public String uploadProblemTests(LoadTestsForm loadTestsForm) throws IOException {
        logger.info("Количество тестовых файлов = " + loadTestsForm.getDirTests().length);

        problemFacade.loadTestFiles(loadTestsForm);


        return "redirect:/teacher/problem";
    }

}
