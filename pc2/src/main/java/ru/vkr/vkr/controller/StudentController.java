package ru.vkr.vkr.controller;

import edu.csus.ecs.pc2.core.InternalController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import ru.vkr.vkr.domain.BridgePc2;
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

//import ru.vkr.vkr.contest.Test;

@Controller
public class StudentController {

    @Autowired
    private SubmitRunService submitRunService;
    @Autowired
    private StudentService studentService;
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
        InternalController internalController = BridgePc2.getInternalController();
        SubmitRunForm submitRunForm = new SubmitRunForm();
        Problem problem = problemService.getProblemById(problemId);
        if (problem.getPathToTextProblem() == null || problem.getPathToTextProblem().equals("")) {
            model.addAttribute("statement", false);
        } else {
            model.addAttribute("statement", true);
        }
        model.addAttribute("problem", problem);
        model.addAttribute("langs", BridgePc2.getInternalContest().getLanguages());
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

    @GetMapping("/student/updateNumInProblems")
    public String updateNumInProblems () {
        problemFacade.updateNumInProblems();
        return "redirect:/student/course";
    }

    @GetMapping("/student/source/{numberRun}")
    public String showSource(Model model,
                             @PathVariable int numberRun)  {
        String sourceCode = submitRunService.showSourceCode(numberRun);
        model.addAttribute("source", sourceCode);
        return "source";
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
        return "/submitions";
    }
}
