package ru.vkr.vkr.controller;

import edu.csus.ecs.pc2.core.InternalController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import ru.vkr.vkr.entity.Course;
import ru.vkr.vkr.form.SearchProblemForm;
import ru.vkr.vkr.form.SubmitRunForm;
import ru.vkr.vkr.service.SearchService;
import ru.vkr.vkr.service.StudentService;
import ru.vkr.vkr.service.SubmitRunService;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

//import ru.vkr.vkr.contest.Test;

@Controller
public class StudentController {

    @Autowired
    private SubmitRunService submitRunService;
    @Autowired
    private StudentService studentService;
    @Autowired
    private ApplicationContext applicationContext;
    @Autowired
    private SearchService searchService;

    @ModelAttribute
    public void addAttributes(Model model) {
        List<Course> studentCourses = new ArrayList<>();
        model.addAttribute("studentCourses", studentCourses);
        model.addAttribute("isStudent", true);
    }

    @GetMapping("/student/course")
    public String getCourse(Model model) {
        Course course = studentService.getCourse();
        model.addAttribute("course", course);
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
        model.addAttribute("runs", submitRunService.getRunSummit());
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

    @GetMapping("/student/source/{indexRun}")
    public String showSource(Model model,
                             @PathVariable int indexRun) throws ExecutionException, InterruptedException {
        System.out.println("Invoking an asynchronous method. "
                + Thread.currentThread().getName());
        Future<String> future1 = submitRunService.showSourceForSelectedRun(indexRun);
        String result;
        while (true) {
            if (future1.isDone()) {
                result = future1.get();
                break;
            }
            System.out.println("Continue doing something else. ");
            Thread.sleep(100);
        }

        Future<String> future2 = submitRunService.showSourceForSelectedRun(indexRun);
        while (true) {
            if (future2.isDone()) {
                result = future2.get();
                break;
            }
            System.out.println("Continue doing something else. ");
            Thread.sleep(100);
        }
        model.addAttribute("source", result);
        return "student/source";
    }



    /**
     * Пул задач
     * ********************************************
     */

    @GetMapping("/student/pool-problems")
    public String poolProblems(Model model) {
        searchService.poolProblemsGet(model);
        return "/pool-problems";
    }

    @PostMapping("/student/pool-problems")
    public String poolSearchProblems(Model model, SearchProblemForm searchProblemForm) {
        searchService.poolSearchProblems(model, searchProblemForm);
        return "/pool-problems";
    }

    @GetMapping("/student/pool-problems/{hashTagId}")
    public String poolProblemOneHashtag(@PathVariable("hashTagId") Long hashTagId, Model model) {
        searchService.poolSearchProblems(model, hashTagId);
        return "/pool-problems";
    }
}
