package ru.vkr.vkr.controller;

import javafx.util.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import ru.vkr.vkr.domain.ROLE;
import ru.vkr.vkr.entity.*;
import ru.vkr.vkr.facade.ProblemFacade;
import ru.vkr.vkr.facade.TeacherFacade;
import ru.vkr.vkr.form.*;
import ru.vkr.vkr.repository.StudentRepository;
import ru.vkr.vkr.service.*;

import java.util.*;

import javax.validation.Valid;

@Controller
public class TeacherController {

    @Autowired
    private SubmitRunService submitRunService;
    @Autowired
    private CourseService courseService;
    @Autowired
    private GroupService groupService;
    @Autowired
    private UserService userService;
    @Autowired
    private ProblemService problemService;
    @Autowired
    private ProblemFacade problemFacade;
    @Autowired
    private ChapterService chapterService;
    @Autowired
    private StudentRepository studentRepository;
    @Autowired
    private TeacherFacade teacherFacade;
    @Autowired
    private SearchService searchService;

    //Для вывода сообщений пользоваетлю
    private String messageAttribute = "no";


    //На любой странице teacher/* будут отображаться курсы
    @ModelAttribute
    public void addAttributes(Model model) {
        Collection<Course> teacherCourses = courseService.getCoursesByCurrentTeacher();
        List<Problem> teacherProblems_ = problemService.getProblemsByCurrentTeacher();
        int countVisibleProblems = Math.min(5, teacherProblems_.size());
        Collection<Problem> teacherProblems = new ArrayList<>();
        for (int i = 0; i < countVisibleProblems; i++)
            teacherProblems.add(teacherProblems_.get(i));
        model.addAttribute("teacherCourses", teacherCourses);
        model.addAttribute("teacherProblems", teacherProblems);
        model.addAttribute("isTeacher", true);
        model.addAttribute(messageAttribute, true);
        messageAttribute = "no";
    }


    @GetMapping("/teacher")
    public String mainTeacher() {
        //return "teacher/main";
        return "redirect:/teacher/course/304";
    }

    @GetMapping("/teacher/problems")
    public String allProblems(Model model) {
        List<Problem> allTeacherProblems = problemService.getProblemsByCurrentTeacher();
        problemFacade.getStatisticForProblems(allTeacherProblems);
        model.addAttribute("allTeacherProblems", allTeacherProblems);
        return "teacher/problems";
    }

    //Страница с курсом
    @GetMapping("/teacher/course/{courseId}")
    public String courseGet(Model model, @PathVariable Long courseId) {
        //todo: нужен контроль доступа к курсу (только автор имеет доступ)
        Course course = courseService.getCourseById(courseId);
        model.addAttribute("course", course);
        return "teacher/course";
    }

    //Для изменения параметров курса
    @PostMapping("/teacher/course/{courseId}")
    public String coursePost(@Valid Course courseForm, @PathVariable Long courseId, BindingResult bindingResult) {
        //todo: при создании курса валидация работает, при обновлении нет, нужно разбираться
//        if (bindingResult.hasErrors()) {        //Ошибки валидации есть
//            return "teacher/course";
//        }
        Course course = courseService.getCourseById(courseId);
        course.setName(courseForm.getName());
        courseService.saveCourse(course);
        return "redirect:/teacher/course/" + course.getId();
    }


    //Страница с созданием нового курса
    @GetMapping("/teacher/course-create")
    public String courseCreate(Model model, Course course) {
        model.addAttribute("isCreate", true);
        return "teacher/course";
    }

    //Создание нового курса
    @PostMapping("/teacher/course-create")
    public String courseCreatePost(Model model, @Valid Course course, BindingResult bindingResult) {
        // проверка на ошибки валидации
        if (bindingResult.hasErrors()) {
            model.addAttribute("isCreate", true);
            return "teacher/course";
        }

        //Устанавливаем автора курса
        courseService.setAuthorForNewCourse(course);
        courseService.saveCourse(course);

        //И переходим к курсу
        return "redirect:/teacher/course/" + course.getId();
    }


    //Удаление курса
    @GetMapping("/teacher/course-delete/{courseId}")
    public String deletCourse(@PathVariable Long courseId) {
        Course course = courseService.getCourseById(courseId);
        courseService.deleteCourse(course);
        return "redirect:/teacher";
    }

    //Страница с созданием новой темы/лабы
    @GetMapping("/teacher/course/{courseId}/chapter-create")
    public String chapterCreateGet(Model model,
                                   @PathVariable Long courseId,
                                   Chapter chapter) {
        model.addAttribute("course_id", courseId);
        model.addAttribute("isCreate", true);
        return "teacher/theme";
    }


    //Создание новой темы/лабы
    @PostMapping("/teacher/course/{courseId}/chapter-create")
    public String createChapter(Model model,
                                @PathVariable Long courseId,
                                @Valid Chapter chapter,
                                BindingResult bindingResult) {
        // проверка на ошибки валидации
        if (bindingResult.hasErrors()) {
            model.addAttribute("course_id", courseId);
            model.addAttribute("isCreate", true);
            return "teacher/theme";
        }
        Course course = courseService.getCourseById(courseId);
        // Привязываем курс к теме
        chapterService.setCourse(chapter, course);
        // Сохраняем изменения
        chapterService.saveChapter(chapter);
        // И переходим к теме/лабе
        return "redirect:/teacher/course/" + courseId + "/chapter/" + chapter.getId();
    }

    // страница для просмотра данных темы/лабы
    @GetMapping("/teacher/course/{course_id}/chapter/{chapter_id}")
    public String readChapter(Model model,
                              @PathVariable Long course_id,
                              @PathVariable Long chapter_id) {
        TheoryMaterialForm theoryMaterialForm = new TheoryMaterialForm();
        model.addAttribute("theoryMaterialForm", theoryMaterialForm);

        Chapter chapter = chapterService.getChapterById(chapter_id);
        problemFacade.getStatisticForProblems(chapter.getChapterProblems());
        List<Theory> theories = chapter.getChapterTheories();
        model.addAttribute("theories", theories);
        model.addAttribute("course_id", course_id);
        model.addAttribute("chapter", chapter);
        model.addAttribute("isCreate", false);


//        List<Group> groups = chapter.getCourseChapters().getSubscribers();
//        List<Student> students = new ArrayList<>();
//        for (Group gr : groups)
//            students.addAll(gr.getStudents());
//
//        model.addAttribute("monitor", problemFacade.getMonitor(students, chapter.getChapterProblems()));

        return "teacher/theme";
    }


    //Загрузка монитора
    //@ResponseBody
    @PostMapping("/teacher/theme/monitor")
    public String getMonitor(Model model, @RequestParam(value = "idChapter") Long idChapter) {
        Chapter chapter = chapterService.getChapterById(idChapter);
        List<Group> groups = chapter.getCourseChapters().getSubscribers();
        List<Student> students = new ArrayList<>();
        for (Group gr : groups)
            students.addAll(gr.getStudents());
        model.addAttribute("chapter", chapter);
        model.addAttribute("monitor", problemFacade.getMonitor(students, chapter.getChapterProblems()));
        return "monitor";
    }


    @ResponseBody
    @PostMapping("/teacher/themes-for-course/")
    public String getThemeOfCourse(@RequestParam(value = "id_course") Long idCourse) {
        Course course = courseService.getCourseById(idCourse);
        String options = "";
        for (Chapter chapter : course.getChapters())
            options +=  "<option value=\"" + chapter.getId() + "\">"
                + chapter.getName() + "</option>";
        return options;
    }

    //Пикрепление задачи к теме
    @PostMapping("/teacher/problem/{problemId}/attach-to-chapter")
    public String attachProblemToChapter(Model model,
                                         AttachProblemForm attachProblemForm,
                                         @PathVariable Long problemId) {
        if (attachProblemForm.getChapter() != null) {
            Problem problem = problemService.getProblemById(problemId);
            chapterService.attachProblem(attachProblemForm.getChapter(), problem);
        }
        return "redirect:/teacher/problem/" + problemId;
    }

    //Открепление задачи от темы на странице темы
    @GetMapping("/teacher/chapter/{chapterId}/dettach-problem/{problemId}")
    public String dettachProblemFromChapter(RedirectAttributes redirectAttributes,
                                          @PathVariable Long chapterId,
                                          @PathVariable Long problemId) {
        Problem problem = problemService.getProblemById(problemId);
        Chapter chapter = chapterService.getChapterById(chapterId);
        chapterService.dettachProblem(chapter, problem);

        redirectAttributes.addFlashAttribute("activeTabMenu", "linkThemeProblems");
        return "redirect:/teacher/course/" + chapter.getCourseChapters().getId() + "/chapter/" + chapterId;
    }

    //Открепление задачи от темы на странцие задачи
    @GetMapping("/teacher/problem/{problemId}/dettach-problem/{chapterId}")
    public String dettachChapterFromProblem(@PathVariable Long chapterId,
                                            @PathVariable Long problemId) {
        Problem problem = problemService.getProblemById(problemId);
        Chapter chapter = chapterService.getChapterById(chapterId);
        chapterService.dettachProblem(chapter, problem);
        return "redirect:/teacher/problem/" + problemId;
    }

    // добавление теоретического материала
    @PostMapping("/teacher/course/{course_id}/chapter/{chapter_id}/add-theory")
    public String addTheoryMaterial(Model model,
                                    @PathVariable Long course_id,
                                    @PathVariable Long chapter_id,
                                    @ModelAttribute("theoryMaterialForm") TheoryMaterialForm theoryMaterialForm) {
        chapterService.loadTheory(chapterService.getChapterById(chapter_id), theoryMaterialForm);
        return "redirect:/teacher/course/" + course_id + "/chapter/" + chapter_id;
    }

    //Для изменения параметров темы/лабы
    @PostMapping("/teacher/course/{course_id}/chapter/{chapter_id}")
    public String updateChapterSettings(Model model,
                                        @Valid Chapter chapterForm,
                                        @PathVariable Long course_id,
                                        @PathVariable Long chapter_id,
                                        BindingResult bindingResult) {
        //todo: при создании курса валидация работает, при обновлении нет, нужно разбираться
//        if (bindingResult.hasErrors()) {        //Ошибки валидации есть
//            return "teacher/course";
//        }
        Chapter chapter = chapterService.getChapterById(chapter_id);
        chapterService.updateName(chapter, chapterForm.getName());
        chapterService.saveChapter(chapter);
        return "redirect:/teacher/course/" + course_id + "/chapter/" + chapter_id;
    }

    //Удаление темы/лабы
    @GetMapping("/teacher/course/{courseId}/delete-chapter/{chapterId}")
    public String deleteChapter(Model model,
                                @PathVariable Long courseId,
                                @PathVariable Long chapterId) {
        Chapter chapter = chapterService.getChapterById(chapterId);
        chapterService.deleteChapter(chapter);
        return "redirect:/teacher/course/" + courseId;
    }

    //************************************************************

    //Страница с созданием новой группы
    @GetMapping("/teacher/course/{courseId}/group-create")
    public String createGroup(Model model,
                              @PathVariable Long courseId,
                              Group group) {
        model.addAttribute("course_id", courseId);
        model.addAttribute("isCreate", true);
        return "teacher/group";
    }

    // Создание новой группы
    @PostMapping("/teacher/course/{courseId}/group-create")
    public String createGroup(Model model,
                              @PathVariable Long courseId,
                              @Valid Group group,
                              BindingResult bindingResult) {
        model.addAttribute("course_id", courseId);
        // проверка на ошибки валидации
        if (bindingResult.hasErrors()) {
            model.addAttribute("isCreate", true);
            return "teacher/group";
        }
        // Устанавливаем владельца группы
        groupService.setAuthorForNewGroup(group);
        // Устанавливаем курс
        groupService.setCourse(group, courseService.getCourseById(courseId));
        // Сохраняем изменения
        groupService.saveGroup(group);
        //И переходим к группе
        return "redirect:/teacher/course/" + courseId + "/group/" + group.getId();
    }

    //страница для просмотра данных группы
    @GetMapping("/teacher/course/{courseId}/group/{groupId}")
    public String readGroup(Model model,
                            @PathVariable Long courseId,
                            @PathVariable Long groupId) {
        //todo: нужен контроль доступа к группе (только владелец имеет доступ)
        Group group = groupService.getGroupById(groupId);
        Course course = courseService.getCourseById(courseId);
        UserForm userForm = new UserForm();
        model.addAttribute("course_id", courseId);
        model.addAttribute("group", group);
        model.addAttribute("userForm", userForm);


        //Загрузка монитора
//        Set<Problem> problems = new HashSet<>();
//        for (Chapter chapter : course.getChapters())
//            problems.addAll(chapter.getChapterProblems());
//        MonitorData monitor = problemFacade.getMonitor(group.getStudents(), problems);
//        model.addAttribute("monitor", monitor);
//        model.addAttribute("problems", problems);
        return "teacher/group";
    }

    //Для изменения параметров группы
    @PostMapping("/teacher/course/{courseId}/group/{groupId}")
    public String groupPost(Model model,
                            @Valid Group groupForm,
                            @PathVariable Long courseId,
                            @PathVariable Long groupId) {
        //todo: тоже нужна валидация, как и в случае с курсом
        model.addAttribute("course_id", courseId);
        Group group = groupService.getGroupById(groupId);
        group.setName(groupForm.getName());
        groupService.saveGroup(group);
        return "redirect:/teacher/course/" + courseId + "/group/" + groupId;
    }

    //Загрузка монитора
//    @PostMapping("/teacher/course/{courseId}/group/{groupId}/monitor")
//    public String getMonitor(Model model,
//                             @PathVariable Long courseId,
//                             @PathVariable Long groupId) {
//
//    }


    //Удаление группы

    /**
     * Работа с группой
     * ********************************************
     */

    // добавление студентов в группу
    @PostMapping("/teacher/course/{courseId}/addStudents/{groupId}")
    public String addStudent(Model model,
                             UserForm userForm,
                             @PathVariable Long courseId,
                             @PathVariable Long groupId) {
        Group group = groupService.getGroupById(groupId);
        List<Long> usersId = userService.addUsers(userForm, ROLE.ROLE_STUDENT);
        if (usersId == null) {
            //todo: возможном нужна валидация при добавлении студентов
            System.out.println("ОШИБКА, пользователи не добавлены");
            return "redirect:/teacher/group/" + groupId;
        }

        //Сделать всех новых пользователе йчленами группы
        for (Long userId : usersId) {
            Student student = studentRepository.getOne(userId);
            student.setGroup(group);
            studentRepository.save(student);
        }

        return "redirect:/teacher/course/" + courseId + "/group/" + groupId;
    }

    // удаление группы
    @GetMapping("/teacher/course/{courseId}/delete-group/{groupId}")
    public String deleteGroup(RedirectAttributes redirectAttributes, Model model,
                              @PathVariable Long courseId,
                              @PathVariable Long groupId) {
        Group group = groupService.getGroupById(groupId);
        groupService.deleteGroup(group);

        redirectAttributes.addFlashAttribute("activeTabMenu", "linkSubscribeOfCourse");
        return "redirect:/teacher/course/" + courseId;
    }

    // редактирование ФИО студнета
    @ResponseBody
    @PostMapping("/teacher/editStudent/")
    public String editFioStudent(Model model, @RequestParam(value = "fio") String newFio,
                                 @RequestParam(value = "idStudent") Long idStudent) {
        teacherFacade.editStudent(idStudent, newFio);
        return newFio;
    }


    //Удаление студента
    @GetMapping("/teacher/course/{courseId}/delete-student/{groupId}/{studentId}")
    public String deleteStudent(Model model,
                                @PathVariable Long courseId,
                                @PathVariable Long groupId,
                                @PathVariable Long studentId) {
        Student student = studentRepository.getOne(studentId);
        studentRepository.delete(student);
        return "redirect:/teacher/course/" + courseId + "/group/" + groupId;
    }


    //Удаление теор материала
    @GetMapping("/teacher/course/{courseId}/delete-tm/{chapterId}/{tmId}")
    public String deleteChapter(Model model,
                                @PathVariable Long tmId,
                                @PathVariable Long courseId,
                                @PathVariable Long chapterId) {
        chapterService.deleteTheory(chapterId, tmId);
        return "redirect:/teacher/course/" + courseId + "/chapter/" + chapterId;
    }

    /**
     * Пул задач
     * ********************************************
     */

    @GetMapping("/teacher/pool-problems")
    public String poolProblems(Model model) {
        searchService.poolProblemsGet(model);
        return "/pool-problems";
    }

    @PostMapping("/teacher/pool-problems")
    public String poolSearchProblems(Model model, SearchProblemForm searchProblemForm) {
        searchService.poolSearchProblems(model, searchProblemForm);
        return "/pool-problems";
    }


    @GetMapping("/teacher/pool-problems/{hashTagId}")
    public String poolProblemOneHashtag(@PathVariable("hashTagId") Long hashTagId, Model model) {
        searchService.poolSearchProblems(model, hashTagId);
        return "/pool-problems";
    }


    @GetMapping("/teacher/pool-chapters")
    public String poolChapters(Model model) {
        searchService.poolChaptersGet(model);
        return "/pool-chapters";
    }

    @GetMapping("/teacher/submitions")
    public String showSubmitions(Model model) {
        return "/submitions";
    }



    //Для вставки в бд тегов
    @GetMapping("/teacher/gen-tags")
    public String genTags() {
        searchService.genTags();
        return "redirect:/teacher/pool-problems";
    }

    @GetMapping("/teacher/updateNumInProblems")
    public String updateNumInProblems() {
        problemFacade.updateNumInProblems();
        return "redirect:/teacher";
    }

    @GetMapping("/teacher/init-complexity")
    public String complexityInit() {
        problemService.complexityInit();
        return "redirect:/teacher";
    }

}
