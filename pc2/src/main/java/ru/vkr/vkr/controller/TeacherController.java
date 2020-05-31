package ru.vkr.vkr.controller;

import com.sun.org.apache.xpath.internal.operations.Mod;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import ru.vkr.vkr.domain.ROLE;
import ru.vkr.vkr.entity.*;
import ru.vkr.vkr.facade.AdminFacade;
import ru.vkr.vkr.facade.TeacherFacade;
import ru.vkr.vkr.form.SubmitRunForm;
import ru.vkr.vkr.form.SubscriptionForm;
import ru.vkr.vkr.form.TheoryMaterialForm;
import ru.vkr.vkr.form.UserForm;
import ru.vkr.vkr.repository.StudentRepository;
import ru.vkr.vkr.service.*;

import java.util.List;

import javax.validation.Valid;
import java.util.Collection;
import java.util.Set;

@Controller
public class TeacherController {

    @Autowired
    private CourseService courseService;
    @Autowired
    private GroupService groupService;
    @Autowired
    private UserService userService;
    @Autowired
    private ProblemService problemService;
    @Autowired
    private ChapterService chapterService;
    @Autowired
    private StudentRepository studentRepository;
    @Autowired
    private TeacherFacade teacherFacade;

    //todo: костыльное решение для активизации нужной вкладки при загрузке страницы
    private String pageTabAttribute = "tabMain";

    //Для вывода сообщений пользоваетлю
    private String messageAttribute = "no";


    //На любой странице teacher/* будут отображаться курсы
    @ModelAttribute
    public void addAttributes(Model model) {
        Collection<Course> teacherCourses = courseService.getCoursesByCurrentTeacher();
        Collection<Group> teacherGroups = groupService.getGroupsByCurrentTeacher();
        Collection<Problem> teacherProblems = problemService.getProblemsByCurrentTeacher();
        model.addAttribute("teacherCourses", teacherCourses);
        model.addAttribute("teacherGroups", teacherGroups);
        model.addAttribute("teacherProblems", teacherProblems);
        model.addAttribute(pageTabAttribute, true);
        pageTabAttribute = "tabMain";
        model.addAttribute(messageAttribute, true);
        messageAttribute = "no";
    }



    @GetMapping("/teacher")
    public String mainTeacher() {
        return "teacher/main";
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
    public String courseCreatePost (Model model, @Valid Course course, BindingResult bindingResult) {
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
    public String chapterCreateGet (Model model,
                                    @PathVariable Long courseId,
                                    Chapter chapter) {
        model.addAttribute("course_id", courseId);
        model.addAttribute("isCreate", true);
        return "teacher/theme";
    }


    //Создание новой темы/лабы
    @PostMapping("/teacher/course/{courseId}/chapter-create")
    public String createChapter (Model model,
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
        Set<Theory> theories = chapter.getChapterTheories();
        model.addAttribute("theories", theories);
        model.addAttribute("course_id", course_id);
        model.addAttribute("chapter", chapter);
        model.addAttribute("isCreate", false);
        return "teacher/theme";
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
    public String createGroup (Model model,
                                  @PathVariable Long courseId,
                                  Group group) {
        model.addAttribute("course_id", courseId);
        model.addAttribute("isCreate", true);
        return "teacher/group";
    }

    // Создание новой группы
    @PostMapping("/teacher/course/{courseId}/group-create")
    public String createGroup (Model model,
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
        UserForm userForm = new UserForm();
        model.addAttribute("course_id", courseId);
        model.addAttribute("group", group);
        model.addAttribute("userForm", userForm);
        return "teacher/group";
    }

    //Для изменения параметров темы/лабы
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
        for(Long userId : usersId) {
            Student student = studentRepository.getOne(userId);
            student.setGroup(group);
            studentRepository.save(student);
        }

        return "redirect:/teacher/course/" + courseId + "/group/" + groupId;
    }

    // удаление группы
    @GetMapping("/teacher/course/{courseId}/delete-group/{groupId}")
    public String deleteGroup(Model model,
                              @PathVariable Long courseId,
                              @PathVariable Long groupId) {
        Group group = groupService.getGroupById(groupId);
        groupService.deleteGroup(group);
        return "redirect:/teacher/course/" + courseId;
    }

    // редактирование ФИО студнета
    @ResponseBody
    @PostMapping ("/teacher/editStudent/")
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
}
