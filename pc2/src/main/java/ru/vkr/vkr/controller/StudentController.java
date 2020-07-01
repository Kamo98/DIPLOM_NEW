package ru.vkr.vkr.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import ru.vkr.vkr.domain.exception.Pc2Exception;
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
    @Autowired
    private BridgePc2Service bridgePc2Service;

    @ModelAttribute
    public void addAttributes(Model model) {
        List<Course> studentCourses = new ArrayList<>();
        Collection<Problem> studentProblems = problemService.getProblemsByCurrentStudent();
        model.addAttribute("studentCourses", studentCourses);
        model.addAttribute("isStudent", true);
        model.addAttribute("studentProblems", studentProblems);
    }

    @GetMapping("/student")
    public String mainStudent(Model model) {
        return "redirect:/student/course";
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
    public String getProblem(Model model, @PathVariable Long problemId) throws Pc2Exception {
        SubmitRunForm submitRunForm = new SubmitRunForm();
        Problem problem = problemService.getProblemById(problemId);
        if (problem.getPathToTextProblem() == null || problem.getPathToTextProblem().equals("")) {
            model.addAttribute("statement", false);
        } else {
            model.addAttribute("statement", true);
        }
        model.addAttribute("problem", problem);
        model.addAttribute("langs", bridgePc2Service.getContestLanguages());
        model.addAttribute("submitRunForm", submitRunForm);
        return "student/problem";
    }


    @GetMapping("/student/chapter/{chapterId}/problem/{problemId}")
    public String getProblemFromTheme(Model model,
                                      @PathVariable Long problemId,
                                      @PathVariable Long chapterId) throws Pc2Exception {
        SubmitRunForm submitRunForm = new SubmitRunForm();
        Problem problem = problemService.getProblemById(problemId);
        if (problem.getPathToTextProblem() == null || problem.getPathToTextProblem().equals("")) {
            model.addAttribute("statement", false);
        } else {
            model.addAttribute("statement", true);
        }
        model.addAttribute("problem", problem);
        model.addAttribute("langs", bridgePc2Service.getContestLanguages());
        model.addAttribute("submitRunForm", submitRunForm);

        // Идеальные решения задачи при условии возможности его просмотра студентом
        if (chapterService.isPerfectSolutionAvailable(chapterId, problemId))
            model.addAttribute("perfectSolutions", problem.getPerfectSolutions());
        return "student/problem";
    }

    @PostMapping("/student/submit/{problemId}")
    public String sendFileSubmit(RedirectAttributes redirectAttributes, Model model,
                                 @ModelAttribute("submitRunForm") SubmitRunForm submitRunForm,
                                 @PathVariable Long problemId) throws Pc2Exception {
        submitRunService.submitRun(problemId, submitRunForm);
        redirectAttributes.addFlashAttribute("activeTabMenu", "linkViewRuns");
        return "redirect:/student/problem/" + problemId;
    }

    @GetMapping("/student/updateNumInProblems")
    public String updateNumInProblems () {
        problemFacade.updateNumInProblems();
        return "redirect:/student/course";
    }

    @GetMapping("/student/source/{numberRun}")
    public String showSource(Model model,
                             @PathVariable int numberRun) throws Pc2Exception {
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
        return "poolproblems";
    }

    @PostMapping("/student/pool-problems")
    public String poolSearchProblems(Model model, SearchProblemForm searchProblemForm) {
        searchService.poolSearchProblems(model, searchProblemForm);
        return "poolproblems";
    }

    @GetMapping("/student/pool-problems/{hashTagId}")
    public String poolProblemOneHashtag(@PathVariable("hashTagId") Long hashTagId, Model model) {
        searchService.poolSearchProblems(model, hashTagId);
        return "poolproblems";
    }


    @GetMapping("/student/pool-chapters")
    public String poolChapters(Model model) {
        searchService.poolChaptersGet(model);
        return "poolchapters";
    }
}
