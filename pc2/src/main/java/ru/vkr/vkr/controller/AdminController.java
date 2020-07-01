package ru.vkr.vkr.controller;

import edu.csus.ecs.pc2.core.InternalController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import ru.vkr.vkr.domain.ROLE;
import ru.vkr.vkr.domain.exception.Pc2Exception;
import ru.vkr.vkr.entity.Teacher;
import ru.vkr.vkr.facade.AdminFacade;
import ru.vkr.vkr.form.UserForm;
import ru.vkr.vkr.repository.TeacherRepository;

import java.util.List;

@Controller
public class AdminController {
    private static Logger logger = LoggerFactory.getLogger(AdminController.class);

    @Autowired
    private AdminFacade adminFacade;
    @Autowired
    private TeacherRepository teacherRepository;

    // вывод списка всех преподавателей
    @GetMapping("/admin/teachers")
    public String userList(Model model) {
        UserForm userForm = new UserForm();
        List<Teacher> teachers = teacherRepository.findAll();

        model.addAttribute("userForm", userForm);
        model.addAttribute("teachers", teachers);
        return "admin/teachers";
    }

    // добавление списка преподавателей
    @PostMapping("/admin/addTeachers")
    public String addTeacher(Model model, @ModelAttribute("userForm") UserForm userForm) throws Pc2Exception {
        logger.info("/admin/addTeachers");
        if (adminFacade.addUsers(userForm, ROLE.ROLE_TEACHER) == null) {
            model.addAttribute(userForm);

            System.out.println("ОШИБКА, пользователи не добавлены");
            return "admin/teachers";
        }
        return "redirect:/admin/teachers";
    }

    // редактирование ФИО преподавателя
    @ResponseBody
    @PostMapping ("/admin/editTeacher/")
    public String editFioTeacher(Model model, @RequestParam(value = "fio") String newFio,
                                              @RequestParam(value = "idTeacher") Long idTeacher) {
        adminFacade.editTeacher(idTeacher, newFio);
        return newFio;
    }

    // удаление преподавателя
    @GetMapping("/admin/delTeacher/{teacherId}")
    public String deleteTeacher(Model model, @PathVariable Long teacherId) {
        adminFacade.deleteTeacher(teacherId);
        return "redirect:/admin/teachers";
    }
}
