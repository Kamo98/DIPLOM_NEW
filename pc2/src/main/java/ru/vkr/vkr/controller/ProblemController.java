package ru.vkr.vkr.controller;

import edu.csus.ecs.pc2.api.exceptions.NotLoggedInException;
import javafx.util.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import ru.vkr.vkr.entity.Complexity;
import ru.vkr.vkr.entity.Course;
import ru.vkr.vkr.entity.HashTag;
import ru.vkr.vkr.entity.Problem;
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
    private BridgePc2Service bridgePc2Service;
    @Autowired
    private ChapterService chapterService;


    //todo: в этом методе отчасти дублируется код из такого же в TeacherController, а это не хорошо
    @ModelAttribute
    public void addAttributes(Model model) throws NotLoggedInException {
        Collection<Course> teacherCourses = courseService.getCoursesByCurrentTeacher();
        List<Problem> teacherProblems_ = problemService.getProblemsByCurrentTeacher();
        Collection<Problem> teacherProblems = new ArrayList<>();
        int countVisibleProblems = Math.min(5, teacherProblems_.size());
        for (int i = 0; i < countVisibleProblems; i++)
            teacherProblems.add(teacherProblems_.get(i));
        TheoryMaterialForm theoryMaterialForm = new TheoryMaterialForm();
        SubmitRunForm submitRunForm = new SubmitRunForm();
        SubmitRunForm submitRunFormPerfectSolution = new SubmitRunForm();

        model.addAttribute("runs", submitRunService.getRunSummit());
        model.addAttribute("theoryMaterialForm", theoryMaterialForm);
        model.addAttribute("teacherCourses", teacherCourses);
        model.addAttribute("teacherProblems", teacherProblems);
        model.addAttribute("isTeacher", true);
        model.addAttribute("langs", bridgePc2Service.getServerConnection().getContest().getLanguages());
        model.addAttribute("submitRunForm", submitRunForm);
        model.addAttribute("perfectSolution", submitRunFormPerfectSolution);
    }

    @GetMapping("/teacher/problem-create")
    public String problemCreateGet(Model model, Problem problem) {
        model.addAttribute("isCreate", true);
        //Сложность задачи
        List<Complexity> complexities = problemService.getAllComplexity();
        model.addAttribute("complexities", complexities);
        return "teacher/problem";
    }

    @PostMapping("/teacher/problem-create")
    public String problemCreatePost(Model model, Problem problem) {
        problemService.setAuthorForNewCourse(problem);
        problem.setPubl(false);
        problemService.save(problem);
        //Инициализируем задачу в pc2
        problemFacade.initProblem(problem);
        return "redirect:/teacher/problem/" + problem.getId();
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

        List<Pair<String, String>> fileTests = problemFacade.getAllTestsById(problem);
        model.addAttribute("fileTests", fileTests);
        TestSettingsForm testSettingsForm = new TestSettingsForm();
        problemFacade.setTestsParamsToForm(testSettingsForm, problem);
        model.addAttribute("testSettingsForm", testSettingsForm);

        //Для чекера
        CheckerSettingsForm checkerSettingsForm = new CheckerSettingsForm();
        problemFacade.setCheckerParamsToForm(checkerSettingsForm, problem);
        model.addAttribute("checkerSettingsForm", checkerSettingsForm);

        //Теги
        List<HashTag> hashTags = hashTagService.getAllTags();
        model.addAttribute("hashTags", hashTags);
        ChoiceTagsForm choiceTagsForm = new ChoiceTagsForm();
        //choiceTagsForm.setTagList(problem.getHashTags());

        Set<HashTag> hashTagList = problem.getHashTags();

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
        AttachProblemForm attachProblemForm = new AttachProblemForm();
        model.addAttribute("attachProblemForm", attachProblemForm);


        //Сложность задачи
        List<Complexity> complexities = problemService.getAllComplexity();
        model.addAttribute("complexities", complexities);

        // Идеальные решения задачи
        model.addAttribute("perfectSolutions", problem.getPerfectSolutions());
        model.addAttribute("mapSolutionAvailable", chapterService.getMapSolutionAvailable(problem));

        return "teacher/problem";
    }


    //Изменение основных параметров здадачи
    @PostMapping("/teacher/problem/{problemId}")
    public String postProblem(@PathVariable Long problemId, Problem newProblem) {
        //todo: нужна валидация данных
        Problem problem = problemService.getProblemById(problemId);
        problem.setMemoryLimit(newProblem.getMemoryLimit());
        problem.setTimeLimit(newProblem.getTimeLimit());
        problem.setComplexity(newProblem.getComplexity());
        problemFacade.updateMainParams(newProblem.getName(), problem);

        problem.setName(newProblem.getName());
        problemService.save(problem);
        return "redirect:/teacher/problem/" + problemId;
    }

//    //Публикация задачи
//    @GetMapping("/teacher/problem/{problemId}/publish")
//    public String postProblem(@PathVariable Long problemId) {
//        Problem problem = problemService.getProblemById(problemId);
//
//        //Проверка задачи на готовность к публикации
//        problemFacade.check_tests(problem);
//
//    }


    //Обновление тегов задачи
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
        problemService.setHashTagsToProblem(problem, hashTagVisibleMap);
        return "redirect:/teacher/problem/" + problemId;
    }


    //Загрузка тестов
    @PostMapping("/teacher/problem/{problemId}/tests-upload")
    public String uploadProblemTests(RedirectAttributes redirectAttributes, LoadTestsForm loadTestsForm, @PathVariable Long problemId) throws IOException {
        //todo: нужна валидация данных
        logger.info("Количество тестовых файлов = " + loadTestsForm.getDirTests().length);

        Problem problem = problemService.getProblemById(problemId);

        if (problemFacade.loadTestFiles(loadTestsForm, problem)) {
            problemFacade.addTestsToProblem(problem);
        } else {
            redirectAttributes.addFlashAttribute("errorUploadTests", true);
        }

        redirectAttributes.addFlashAttribute("activeTabMenu", "linkTestsProblem");
        return "redirect:/teacher/problem/" + problemId;
    }

    //Изменение параметров тестов
    @PostMapping("/teacher/problem/{problemId}/tests-settings")
    public String testsSettings(RedirectAttributes redirectAttributes, TestSettingsForm testSettingsForm, @PathVariable Long problemId) {
        Problem problem = problemService.getProblemById(problemId);
        problemFacade.setParamOfTests(problem, testSettingsForm);

        redirectAttributes.addFlashAttribute("activeTabMenu", "linkTestsProblem");
        return "redirect:/teacher/problem/" + problemId;
    }


    //Костыльная загрузка тестов через диск
//    @GetMapping("/teacher/problem/{problemId}/tests-upload/pc2")
//    public String uploadProblemTestsToPC2(@PathVariable Long problemId) throws IOException {
//        Problem problem = problemService.getProblemById(problemId);
//        problemFacade.addTestsToProblem(problem);
//        return "redirect:/teacher/problem/" + problemId;
//    }


    //Удаление теста
    @GetMapping("/teacher/problem/{problemId}/test-delete/{testNum}")
    public String deleteProblemTest(RedirectAttributes redirectAttributes, @PathVariable Long problemId, @PathVariable Integer testNum) {
        Problem problem = problemService.getProblemById(problemId);
        problemFacade.deleteTestFile(problem, testNum - 1);

        redirectAttributes.addFlashAttribute("activeTabMenu", "linkTestsProblem");
        return "redirect:/teacher/problem/" + problemId;
    }

    //Удаление всех тестов
    @GetMapping("/teacher/problem/{problemId}/test-deleteAll")
    public String deleteProblemAllTests(RedirectAttributes redirectAttributes, @PathVariable Long problemId) {
        Problem problem = problemService.getProblemById(problemId);
        problemFacade.deleteAllTestFiles(problem);

        redirectAttributes.addFlashAttribute("activeTabMenu", "linkTestsProblem");
        return "redirect:/teacher/problem/" + problemId;
    }

    //Установка параметров чекера
    @PostMapping("/teacher/problem/{problemId}/checker-settings")
    public String checkerSettings(RedirectAttributes redirectAttributes, CheckerSettingsForm checkerSettingsForm, @PathVariable Long problemId) {
        Problem problem = problemService.getProblemById(problemId);

        problemFacade.setParamsOfChecker(problem, checkerSettingsForm);

        redirectAttributes.addFlashAttribute("activeTabMenu", "linkCheckerProblem");
        return "redirect:/teacher/problem/" + problemId;
    }

    //Все теги
    @GetMapping("/teacher/tags")
    public String getAllTags(Model model) {
        Collection<HashTag> hashTags = hashTagService.getAllTags();
        model.addAttribute("hashTags", hashTags);
        return "/teacher/tags";
    }

    // добавление условия к задачи
    @PostMapping("/teacher/problem/{problemId}/add-statement")
    public String addStatementMaterial(RedirectAttributes redirectAttributes, Model model,
                                       @PathVariable Long problemId,
                                       @ModelAttribute("theoryMaterialForm") TheoryMaterialForm theoryMaterialForm) {
        problemService.loadStatement(problemService.getProblemById(problemId), theoryMaterialForm);

        redirectAttributes.addFlashAttribute("activeTabMenu", "linkStatementProblem");
        return "redirect:/teacher/problem/" + problemId;
    }

    //Удаление условия задачи
    @GetMapping("/teacher/problem/{problemId}/delete-statement")
    public String deleteChapter(RedirectAttributes redirectAttributes, Model model,
                                @PathVariable Long problemId) {
        problemService.deleteStatement(problemService.getProblemById(problemId));
        redirectAttributes.addFlashAttribute("activeTabMenu", "linkStatementProblem");
        return "redirect:/teacher/problem/" + problemId;
    }

    //Отправка решения
    @PostMapping("/teacher/submit/{problemId}")
    public String sendFileSubmit(RedirectAttributes redirectAttributes, Model model,
                                 @ModelAttribute("submitRunForm") SubmitRunForm submitRunForm,
                                 @PathVariable Long problemId) {
        submitRunService.submitRun(problemId, submitRunForm);
        redirectAttributes.addFlashAttribute("activeTabMenu", "linkViewRunsProblem");
        return "redirect:/teacher/problem/" + problemId;
    }

    //Публикация задачи
    @GetMapping("/teacher/problem/{problemId}/publish")
    public String publishProblem(RedirectAttributes redirectAttributes,
                                 @PathVariable Long problemId) {

        Problem problem = problemService.getProblemById(problemId);

        if (problem.isPubl())
            return "redirect:/teacher/problem/" + problemId;

        List<String> errorsPublish = new ArrayList<>();

        //Валидация, если вдруг пригодится
//        if (problem.getName().trim().equals(""))
//            errorsPublish.add("Задача не может быть опубликована с пустым именем");
//
//        if (problem.getPathToTextProblem() == null || problem.getPathToTextProblem().trim().equals(""))
//            errorsPublish.add("Задача не может быть опубликована без файла с условием");
//
//        if (!problemFacade.check_tests(problem))
//            errorsPublish.add("Задача не может быть опубликована без тестовых файлов");

        if (errorsPublish.size() == 0) {
            problem.setPubl(true);
            problemService.save(problem);
        }
        redirectAttributes.addFlashAttribute("errorsPublish", errorsPublish);
        return "redirect:/teacher/problem/" + problemId;
    }

    //Публикация задачи
    @GetMapping("/teacher/problem/{problemId}/depublish")
    public String depublishProblem(@PathVariable Long problemId) {
        Problem problem = problemService.getProblemById(problemId);
        problem.setPubl(false);
        problemService.save(problem);
        return "redirect:/teacher/problem/" + problemId;
    }


    // Добавить идеальное решение
    @PostMapping("/teacher/problem/{problemId}/add-perfect-sol")
    public String addPerfectSolutionProblem(RedirectAttributes redirectAttributes,
                                            @ModelAttribute("perfectSolution") SubmitRunForm submitRunForm,
                                            @PathVariable Long problemId) {
        problemFacade.addPerfectSolution(problemId, submitRunForm);
        redirectAttributes.addFlashAttribute("activeTabMenu", "linkViewPerfectSolution");
        return "redirect:/teacher/problem/" + problemId;
    }

    //Удаление идеального решения
    @GetMapping("/teacher/problem/{problemId}/perfSol-delete/{perfectSolId}")
    public String deletePerfectSolution(RedirectAttributes redirectAttributes,
                                        @PathVariable Long problemId,
                                        @PathVariable Long perfectSolId) {
        problemService.deletePerfectSolution(perfectSolId);
        redirectAttributes.addFlashAttribute("activeTabMenu", "linkViewPerfectSolution");
        return "redirect:/teacher/problem/" + problemId;
    }


    //Удаление задачи
    @GetMapping("/teacher/problem/{problemId}/delete")
    public String deleteProblem(RedirectAttributes redirectAttributes, @PathVariable Long problemId) {
        Problem problem = problemService.getProblemById(problemId);
        problemFacade.deleteAllTestFiles(problem);
        problemFacade.deleteDirectoryTests(problem);
        problemService.deleteStatement(problem);
        problemService.deleteProblem(problem);
        return "redirect:/teacher/";
    }

}
