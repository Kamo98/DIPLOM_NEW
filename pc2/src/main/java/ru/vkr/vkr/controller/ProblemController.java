package ru.vkr.vkr.controller;

import edu.csus.ecs.pc2.core.InternalController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import ru.vkr.vkr.entity.*;
import ru.vkr.vkr.facade.ProblemFacade;
import ru.vkr.vkr.form.*;
import ru.vkr.vkr.service.*;

import java.io.IOException;
import java.util.*;

@Controller
public class ProblemController {
    private static Logger logger = LoggerFactory.getLogger(ProblemController.class);

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
    @Autowired
    private SubmitRunService submitRunService;
    @Autowired
    private ApplicationContext applicationContext;


    //todo: в этом методе отчасти дублируется код из такого же в TeacherController, а это не хорошо
    @ModelAttribute
    public void addAttributes(Model model) {
        Collection<Course> teacherCourses = courseService.getCoursesByCurrentTeacher();
        Collection<Problem> teacherProblems = problemService.getProblemsByCurrentTeacher();
        TheoryMaterialForm theoryMaterialForm = new TheoryMaterialForm();
        SubmitRunForm submitRunForm = new SubmitRunForm();
        InternalController internalController = (InternalController) applicationContext.getBean("getInternalController");


        model.addAttribute("theoryMaterialForm", theoryMaterialForm);
        model.addAttribute("teacherCourses", teacherCourses);
        model.addAttribute("teacherProblems", teacherProblems);
        model.addAttribute("isTeacher", true);
        model.addAttribute("langs", internalController.getContest().getLanguages());
        model.addAttribute("submitRunForm", submitRunForm);
    }

    @GetMapping("/teacher/problem/{problemId}")
    public String getProblem(Model model, @PathVariable Long problemId) {
        Problem problem = problemService.getProblemById(problemId);
        model.addAttribute("problem", problem);

        // Для условия
        if (problem.getPathToTextProblem() == null || problem.getPathToTextProblem().equals("")) {
            model.addAttribute("statement", false);
        } else {
            model.addAttribute("statement", true);
        }

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
        List<HashTag> hashTags = hashTagService.getAllTags();
        model.addAttribute("hashTags", hashTags);
        ChoiceTagsForm choiceTagsForm = new ChoiceTagsForm();
        //choiceTagsForm.setTagList(problem.getHashTags());

        Set<HashTag> hashTagList = problem.getHashTagsVisible();

        //todo: костыли в студию, но это быстрее, чем то, как thymeleaf формировал checkbox из объектов HashTag
        for (int i = 0; i < hashTags.size(); i++)
            choiceTagsForm.getTagList().add(hashTagList.contains(hashTags.get(i)));

//        for (int i = 0; i < hashTags.size(); i++)
//            if (hashTagList.contains(hashTags.get(i)))
//                choiceTagsForm.getTagList().add(hashTags.get(i).getId());
//            else
//                choiceTagsForm.getTagList().add(null);
        model.addAttribute("choiceTagsForm", choiceTagsForm);


        //Темы
        Set<Chapter> chapters = new HashSet<>();
        Collection<Course> teacherCourses = courseService.getCoursesByCurrentTeacher();
        for (Course course : teacherCourses)
            chapters.addAll(course.getChapters());
        model.addAttribute("chapters", chapters);
        AttachProblemForm attachProblemForm = new AttachProblemForm();
        model.addAttribute("attachProblemForm", attachProblemForm);

        return "teacher/problem";
    }


    //Изменение основных параметров здадачи
    @PostMapping("/teacher/problem/{problemId}")
    public String postProblem(@PathVariable Long problemId, Problem newProblem) {
        //todo: нужна валидация данных
        Problem problem = problemService.getProblemById(problemId);
        problem.setMemoryLimit(newProblem.getMemoryLimit());
        problem.setTimeLimit(newProblem.getTimeLimit());
        problemFacade.updateMainParams(newProblem.getName(), problem);

        //problem.setName(newProblem.getName());
        problemService.save(problem);
        return "redirect:/teacher/problem/" + problemId;
    }


    @PostMapping("/teacher/problem/{problemId}/update-tags")
    public String updateHashTagsPrbolem(@PathVariable Long problemId, ChoiceTagsForm choiceTagsForm) {
        Problem problem = problemService.getProblemById(problemId);
        //todo: это очень плохо (передавать теги по индексу), но по другому не получается
        List<HashTag> hashTags = hashTagService.getAllTags();
        Set<HashTag> tagsFromUser = new HashSet<>();
        for (int i = 0; i < choiceTagsForm.getTagList().size(); i++)
            if (choiceTagsForm.getTagList().get(i) != null && choiceTagsForm.getTagList().get(i))
                tagsFromUser.add(hashTags.get(i));

        //Формируем список тегов для базы
        Map<HashTag, Boolean> hashTagVisibleMap = hashTagService.checkAndAddParents(tagsFromUser);
        problem.setHashTags(problemService.setHashTagsToProblem(problem, hashTagVisibleMap));
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

    // добавление условия к задачи
    @PostMapping("/teacher/problem/{problemId}/add-statement")
    public String addStatementMaterial(Model model,
                                       @PathVariable Long problemId,
                                       @ModelAttribute("theoryMaterialForm") TheoryMaterialForm theoryMaterialForm) {
        problemService.loadStatement(problemService.getProblemById(problemId), theoryMaterialForm);
        return "redirect:/teacher/problem/" + problemId;
    }

    //Удаление условия задачи
    @GetMapping("/teacher/problem/{problemId}/delete-statement")
    public String deleteChapter(Model model,
                                @PathVariable Long problemId) {
        problemService.deleteStatement(problemService.getProblemById(problemId));
        return "redirect:/teacher/problem/" + problemId;
    }

    @PostMapping("/teacher/submit/{problemId}")
    public String sendFileSubmit(Model model,
                                 @ModelAttribute("submitRunForm") SubmitRunForm submitRunForm,
                                 @PathVariable Long problemId) {
        submitRunService.submitRun(problemFacade.findProblemInPC2(problemService.getProblemById(problemId)),
                submitRunForm.getLanguage(),
                submitRunForm.getMultipartFile());
        return "redirect:/teacher/problem/" + problemId;
    }

}
