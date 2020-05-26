package ru.vkr.vkr.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import ru.vkr.vkr.entity.Course;
import ru.vkr.vkr.entity.Group;
import ru.vkr.vkr.entity.HashTag;
import ru.vkr.vkr.entity.Problem;
import ru.vkr.vkr.facade.ProblemFacade;
import ru.vkr.vkr.form.CheckerSettingsForm;
import ru.vkr.vkr.form.LoadTestsForm;
import ru.vkr.vkr.service.CourseService;
import ru.vkr.vkr.service.GroupService;
import ru.vkr.vkr.service.HashTagService;
import ru.vkr.vkr.service.ProblemService;

import java.io.IOException;
import java.util.Collection;

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
    @Autowired
    private HashTagService hashTagService;


    //todo: в этом методе отчасти дублируется код из такого же в TeacherController, а это не хорошо
    @ModelAttribute
    public void addAttributes(Model model) {
        Collection<Course> teacherCourses = courseService.getCoursesByCurrentTeacher();
        Collection<Group> teacherGroups = groupService.getGroupsByCurrentTeacher();
        Collection<Problem> teacherProblems = problemService.getProblemsByCurrentTeacher();
        model.addAttribute("teacherCourses", teacherCourses);
        model.addAttribute("teacherGroups", teacherGroups);
        model.addAttribute("teacherProblems", teacherProblems);
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

        //Для чекера
        CheckerSettingsForm checkerSettingsForm = new CheckerSettingsForm();
        problemFacade.setCheckerParamsToForm(checkerSettingsForm, problem);
        model.addAttribute("checkerSettingsForm", checkerSettingsForm);

        //Теги
        Collection<HashTag> hashTags = hashTagService.getAllTags();
        model.addAttribute("hashTags", hashTags);

        return "teacher/problem";
    }


    //Изменение основных параметров здадачи
    @PostMapping("/teacher/problem/{problemId}")
    public String postProblem(@PathVariable Long problemId, Problem newProblem) {
        //todo: нужна валидация данных
        Problem problem = problemService.getProblemById(problemId);
        problem.setName(newProblem.getName());
        problem.setMemoryLimit(newProblem.getMemoryLimit());
        problem.setTimeLimit(newProblem.getTimeLimit());
        problemService.save(problem);
        return "redirect:/teacher/problem/" + problemId;
    }



    //Загрузка тестов
    @PostMapping("/teacher/problem/{problemId}/tests-upload")
    public String uploadProblemTests(LoadTestsForm loadTestsForm, @PathVariable Long problemId) throws IOException {
        //todo: нужна валидация данных
        logger.info("Количество тестовых файлов = " + loadTestsForm.getDirTests().length);

        Problem problem = problemService.getProblemById(problemId);

        problemFacade.loadTestFiles(loadTestsForm, problem);
        problemFacade.addTestsToProblem(problem);

        return "redirect:/teacher/problem/" + problemId;
    }


    @GetMapping("/teacher/problem/{problemId}/test-delete/{testName}")
    public String deleteProblemTest(@PathVariable Long problemId, @PathVariable String testName) {
        problemFacade.deleteTestFile(problemId, testName);
        return "redirect:/teacher/problem/" + problemId;
    }


    //Установка параметров чекера
    @PostMapping("/teacher/problem/{problemId}/checker-settings")
    public String checkerSettings(CheckerSettingsForm checkerSettingsForm, @PathVariable Long problemId) {
        Problem problem = problemService.getProblemById(problemId);

        problemFacade.setParamsOfChecker(problem, checkerSettingsForm);

        return "redirect:/teacher/problem/" + problemId;
    }

    @GetMapping("/teacher/problem-create")
    public String problemCreateGet(Model model, Problem problem) {
        model.addAttribute("isCreate", true);
        return "teacher/problem";
    }

    @PostMapping("/teacher/problem-create")
    public String problemCreatePost(Model model, Problem problem) {
        problemService.setAuthorForNewCourse(problem);
        problemService.save(problem);

        //Инициализируем задачу в pc2
        Long problemPc2NumId = problemFacade.initProblem(problem);

        problem.setNumElementId(problemPc2NumId);
        problemService.save(problem);


        return "redirect:/teacher/problem/" + problem.getId();
    }





    @GetMapping("/teacher/tags")
    public String getAllTags(Model model) {
        Collection<HashTag> hashTags = hashTagService.getAllTags();
        model.addAttribute("hashTags", hashTags);
        return "/teacher/tags";
    }

}
