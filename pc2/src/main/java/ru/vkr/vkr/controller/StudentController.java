package ru.vkr.vkr.controller;

import edu.csus.ecs.pc2.core.InternalController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import ru.vkr.vkr.entity.Course;
import ru.vkr.vkr.form.SubmitRunForm;
import ru.vkr.vkr.form.UserForm;
import ru.vkr.vkr.service.SubmitRunService;

import java.util.ArrayList;
import java.util.List;

//import ru.vkr.vkr.contest.Test;

@Controller
public class StudentController {

    @Autowired
    private SubmitRunService submitRunService;
    @Autowired
    private ApplicationContext applicationContext;

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
        InternalController internalController = (InternalController) applicationContext.getBean("getInternalController");
        SubmitRunForm submitRunForm = new SubmitRunForm();

        model.addAttribute("langs", internalController.getContest().getLanguages());
        model.addAttribute("problems", internalController.getContest().getProblems());
        model.addAttribute("submitRunForm", submitRunForm);
        return "student/problem";
    }

    @PostMapping("/student/submit")
    public String sendFileSubmit(Model model,
                                 @ModelAttribute("submitRunForm") SubmitRunForm submitRunForm) {
        submitRunService.submitRun(submitRunForm.getProblem(), submitRunForm.getLanguage(),
                submitRunForm.getMultipartFile());

        return "redirect:/student/problem";
    }



}
