package ru.vkr.vkr.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import ru.vkr.vkr.domain.ROLE;
import ru.vkr.vkr.entity.Role;
import ru.vkr.vkr.entity.Teacher;
import ru.vkr.vkr.entity.User;
import ru.vkr.vkr.facade.IAuthenticationFacade;
import ru.vkr.vkr.repository.RoleRepository;
import ru.vkr.vkr.repository.StudentRepository;
import ru.vkr.vkr.repository.TeacherRepository;
import ru.vkr.vkr.repository.UserRepository;
import ru.vkr.vkr.service.UserService;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

@Controller
public class GetUserWithCustomInterfaceController {
    @Autowired
    private IAuthenticationFacade authenticationFacade;
    @Autowired
    private RoleRepository roleRepository;
    @Autowired
    UserService userService;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private TeacherRepository teacherRepository;
    @Autowired
    private StudentRepository studentRepository;
    @PersistenceContext
    EntityManager entityManager;

    // получить имя текущего атворизованного пользователя
    @RequestMapping(value = "/username", method = RequestMethod.GET)
    @ResponseBody
    public String currentUserNameSimple() {
        Authentication authentication = authenticationFacade.getAuthentication();
        User user = (User) userService.loadUserByUsername(authentication.getName());
        Role role = user.getRole().iterator().next();
        if (role.getId().equals(ROLE.ROLE_TEACHER.getId())) {
            Teacher currentTeacher = entityManager.createQuery("select t from Teacher t where t.user.id = :paramId", Teacher.class).
                    setParameter("paramId", user.getId()).getSingleResult();
            return (currentTeacher.getSurname() + " " + currentTeacher.getName());
        }
        return user.getUsername();
    }
}
