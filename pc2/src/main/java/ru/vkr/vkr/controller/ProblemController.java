package ru.vkr.vkr.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.multipart.MultipartFile;
import ru.vkr.vkr.entity.Course;
import ru.vkr.vkr.entity.Group;
import ru.vkr.vkr.entity.Problem;
import ru.vkr.vkr.facade.ProblemFacade;
import ru.vkr.vkr.form.LoadTestsForm;
import ru.vkr.vkr.service.CourseService;
import ru.vkr.vkr.service.GroupService;
import ru.vkr.vkr.service.ProblemService;

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
    @Autowired
    private ProblemService problemService;


    @ModelAttribute
    public void addAttributes(Model model) {
        Collection<Course> teacherCourses = courseService.getCoursesByCurrentTeacher();
        Collection<Group> teacherGroups = groupService.getGroupsByCurrentTeacher();
        model.addAttribute("teacherCourses", teacherCourses);
        model.addAttribute("teacherGroups", teacherGroups);
    }

    @GetMapping("/teacher/problem/{problemId}")
    public String getProblem(Model model, @PathVariable Long problemId) {
        Problem problem = problemService.getProblemById(problemId);
        model.addAttribute("problem", problem);

        //Для тестов
        LoadTestsForm loadTestsForm = new LoadTestsForm();
        model.addAttribute("loadTestsForm", loadTestsForm);

        Collection<String> fileTests = problemFacade.getAllTestsById(problemId);
        model.addAttribute("fileTests", fileTests);

        return "teacher/problem";
    }

    @PostMapping("/teacher/problem/{problemId}/tests-upload")
    public String uploadProblemTests(LoadTestsForm loadTestsForm, @PathVariable Long problemId) throws IOException {
        logger.info("Количество тестовых файлов = " + loadTestsForm.getDirTests().length);

        Problem problem = problemService.getProblemById(problemId);

        String problemPc2Id = problemFacade.loadTestFiles(loadTestsForm, problem);

        problem.setElementId(problemPc2Id);
        problemService.save(problem);

        return "redirect:/teacher/problem/" + problemId;
    }


    @GetMapping("/teacher/problem-create")
    public String problemCreateGet(Model model, Problem problem) {
        model.addAttribute("isCreate", true);
        return "teacher/problem";
    }

    @PostMapping("/teacher/problem-create")
    public String problemCreatePost(Model model, Problem problem) {
        problemService.save(problem);
        problemFacade.makeDirectory(problem.getId());


        return "redirect:/teacher/problem/" + problem.getId();
    }



}
