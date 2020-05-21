package ru.vkr.vkr.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import ru.vkr.vkr.domain.ROLE;
import ru.vkr.vkr.entity.Course;
import ru.vkr.vkr.entity.Group;
import ru.vkr.vkr.entity.Student;
import ru.vkr.vkr.entity.User;
import ru.vkr.vkr.facade.AdminFacade;
import ru.vkr.vkr.facade.TeacherFacade;
import ru.vkr.vkr.form.SubscriptionForm;
import ru.vkr.vkr.form.UserForm;
import ru.vkr.vkr.repository.StudentRepository;
import ru.vkr.vkr.service.CourseService;
import ru.vkr.vkr.service.GroupService;
import ru.vkr.vkr.service.UserService;
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
    private AdminFacade adminFacade;
    @Autowired
    private UserService userService;
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
        model.addAttribute("teacherCourses", teacherCourses);
        model.addAttribute("teacherGroups", teacherGroups);
        model.addAttribute(pageTabAttribute, true);
        pageTabAttribute = "tabMain";
        model.addAttribute(messageAttribute, true);
        messageAttribute = "no";
    }



    @GetMapping("/teacher")
    public String mainTeacher() {
        return "teacher/main";
    }


    @GetMapping("/teacher/group/{groupId}")
    public String groupGet(Model model, @PathVariable Long groupId) {
        //todo: нужен контроль доступа к группе (только владелец имеет доступ)
        Group group = groupService.getGroupById(groupId);
        UserForm userForm = new UserForm();

        model.addAttribute("group", group);
        model.addAttribute("userForm", userForm);
        return "teacher/group";
    }

    @PostMapping("/teacher/group/{groupId}")
    public String groupPost(@Valid Group groupForm, @PathVariable Long groupId) {
        //todo: тоже нужна валидация, как и в случае с курсом
        Group group = groupService.getGroupById(groupId);
        group.setName(groupForm.getName());
        groupService.saveGroup(group);
        return "redirect:/teacher/group/" + groupId;
    }


    //Страница с созданием новой группы
    @GetMapping("/teacher/group-create")
    public String groupCreateGet (Model model, Group group) {
        model.addAttribute("isCreate", true);
        return "teacher/group";
    }

    //Создание новой группы
    @PostMapping("/teacher/group-create")
    public String groupCreatePost (Model model, @Valid Group group, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {        //Ошибки валидации есть
            model.addAttribute("isCreate", true);
            return "teacher/group";
        }

        //Устанавливаем владельца группы
        groupService.setAuthorForNewGroup(group);
        groupService.saveGroup(group);

        //И переходим к группе
        return "redirect:/teacher/group/" + group.getId();
    }

    //Удаление группы
    @GetMapping("/teacher/group-delete/{groupId}")
    public String deleteGroup(@PathVariable Long groupId) {
        Group group = groupService.getGroupById(groupId);
        groupService.deleteGroup(group);
        return "redirect:/teacher";
    }



    //Страница с курсом
    @GetMapping("/teacher/course/{courseId}")
    public String courseGet(Model model, @PathVariable Long courseId) {
        //todo: нужен контроль доступа к курсу (только автор имеет доступ)
        Course course = courseService.getCourseById(courseId);
        SubscriptionForm subscriptionForm = new SubscriptionForm();
        Set<Group> courseSubscribers = course.getSubscribers();
        model.addAttribute("course", course);
        model.addAttribute("subscriptionForm", subscriptionForm);
        model.addAttribute("courseSubscribers",  courseSubscribers);
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
        //course.setName(course.getName().trim());
        if (bindingResult.hasErrors()) {        //Ошибки валидации есть
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



    //Подписка группы на курс
    @PostMapping("/teacher/course/{courseId}/signUp")
    public String signUpForCourse(@PathVariable Long courseId, SubscriptionForm subscriptionForm) {
        //todo: нужна проверка на владение курсом и группой
        Course course = courseService.getCourseById(courseId);
        Group group = subscriptionForm.getGroup();

        if (courseService.containsGroup(course, group)) {
            //На курс такая группа уже подписана
            messageAttribute = "groupRepeat";
        } else {
            courseService.signUpForCourse(course, group);
            courseService.saveCourse(course);
        }

        pageTabAttribute = "pageSubscribes";            //Для активации вкладки с подписчиками
        return "redirect:/teacher/course/" + courseId;
    }


    //Отписка группы от курса
    @GetMapping("/teacher/course/{courseId}/signDown/{groupId}")
    public String signDownForCourse(@PathVariable Long courseId, @PathVariable Long groupId) {
        //todo: нужна проверка на владение курсом и группой
        Course course = courseService.getCourseById(courseId);
        Group group = groupService.getGroupById(groupId);

        courseService.signDownForCourse(course, group);
        courseService.saveCourse(course);

        pageTabAttribute = "pageSubscribes";            //Для активации вкладки с подписчиками
        return "redirect:/teacher/course/" + courseId;
    }




    @PostMapping("/teacher/group/{groupId}/addStudent")
    public String addStudent(Model model, UserForm userForm, @PathVariable Long groupId) {
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

        return "redirect:/teacher/group/" + groupId;
    }


    @ResponseBody
    @PostMapping ("/teacher/editStudent/")
    public String editFioStudent(Model model, @RequestParam(value = "fio") String newFio,
                                 @RequestParam(value = "idStudent") Long idStudent) {
        teacherFacade.editStudent(idStudent, newFio);
        return newFio;
    }


    //Удаление студента
    @GetMapping("/teacher/group/{groupId}/delete/{studentId}")
    public String deleteStudent(@PathVariable Long groupId, @PathVariable Long studentId) {
        Student student = studentRepository.getOne(studentId);
        studentRepository.delete(student);
        return "redirect:/teacher/group/" + groupId;
    }

    @GetMapping("/teacher/theme")
    public String testTheme() {
        return "teacher/theme";
    }
}
