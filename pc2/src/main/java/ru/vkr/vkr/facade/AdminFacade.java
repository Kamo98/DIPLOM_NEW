package ru.vkr.vkr.facade;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.vkr.vkr.domain.ROLE;
import ru.vkr.vkr.entity.Teacher;
import ru.vkr.vkr.form.UserForm;
import ru.vkr.vkr.repository.TeacherRepository;
import ru.vkr.vkr.service.UserService;

import java.util.List;

@Component
public class AdminFacade {
    @Autowired
    private UserService userService;
    @Autowired
    private TeacherRepository teacherRepository;

    public List<Long> addUsers(UserForm userForm, ROLE role) {
        return userService.addUsers(userForm, ROLE.ROLE_TEACHER);
    }

    public void editTeacher(Long teacherId, String newFio) {
        Teacher teacher = teacherRepository.findById(teacherId).get();
        String []fio = newFio.split(" +");
        teacher.setSurname(fio[0]);
        teacher.setName(fio[1]);
        teacher.setMiddleName(fio[2]);
        teacherRepository.save(teacher);
    }

    public boolean deleteTeacher(Long teacherId) {
        Teacher teacher = teacherRepository.findById(teacherId).get();
        return userService.deleteUser(teacher.getUser().getId());
    }

    public List<Teacher> getTeachers() {
        return teacherRepository.findAll();
    }
}
