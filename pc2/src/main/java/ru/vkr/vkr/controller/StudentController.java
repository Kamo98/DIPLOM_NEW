package ru.vkr.vkr.controller;

import edu.csus.ecs.pc2.core.InternalController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import ru.vkr.vkr.entity.Chapter;
import ru.vkr.vkr.entity.Course;
import ru.vkr.vkr.entity.Problem;
import ru.vkr.vkr.facade.ProblemFacade;
import ru.vkr.vkr.form.SearchProblemForm;
import ru.vkr.vkr.form.SubmitRunForm;
import ru.vkr.vkr.service.*;

import java.util.ArrayList;
import java.util.Collection;
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
    @Autowired
    private ChapterService chapterService;
    @Autowired
    private ProblemService problemService;
    @Autowired
    private ProblemFacade problemFacade;

    @ModelAttribute
    public void addAttributes(Model model) {
        List<Course> studentCourses = new ArrayList<>();
        Collection<Problem> studentProblems = problemService.getProblemsByCurrentStudent();
        model.addAttribute("studentCourses", studentCourses);
        model.addAttribute("isStudent", true);
        model.addAttribute("studentProblems", studentProblems);
    }

    @GetMapping("/student/course")
    public String getCourse(Model model) {
        Course course = studentService.getCourse();
        model.addAttribute("course", course);
        return "student/course";
    }

    @GetMapping("/student/chapter/{chapterId}")
    public String readChapter(Model model,
                           @PathVariable Long chapterId) {
        Chapter chapter = chapterService.getChapterById(chapterId);
        problemFacade.getStatisticForProblems(chapter.getChapterProblems());
        model.addAttribute("chapter", chapter);
        return "student/theme";
    }


    @GetMapping("/student/problem/{problemId}")
    public String getProblem(Model model, @PathVariable Long problemId) {
        InternalController internalController = (InternalController) applicationContext.getBean("getInternalController");
        SubmitRunForm submitRunForm = new SubmitRunForm();
        Problem problem = problemService.getProblemById(problemId);
        if (problem.getPathToTextProblem() == null || problem.getPathToTextProblem().equals("")) {
            model.addAttribute("statement", false);
        } else {
            model.addAttribute("statement", true);
        }
        model.addAttribute("problem", problem);
        model.addAttribute("runs", submitRunService.getRunSummit());
        model.addAttribute("langs", internalController.getContest().getLanguages());
        model.addAttribute("submitRunForm", submitRunForm);
        return "student/problem";
    }

    @PostMapping("/student/submit/{problemId}")
    public String sendFileSubmit(Model model,
                                 @ModelAttribute("submitRunForm") SubmitRunForm submitRunForm,
                                 @PathVariable Long problemId) {
        submitRunService.submitRun(problemFacade.findProblemInPC2(problemService.getProblemById(problemId)),
                submitRunForm.getLanguage(),
                submitRunForm.getMultipartFile());
        return "redirect:/student/problem/" + problemId;
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

    @GetMapping("/student/submitions")
    public String showSubmitions(Model model) {
        model.addAttribute("runs", submitRunService.getRunSummit());
        return "/submitions";
    }
}
