package ru.vkr.vkr.facade;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import ru.vkr.vkr.entity.Teacher;
import ru.vkr.vkr.entity.User;
import ru.vkr.vkr.service.UserService;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

@Component
public class AuthenticationFacade implements IAuthenticationFacade {

    @Autowired
    private UserService userService;
    @PersistenceContext
    private EntityManager entityManager;


    @Override
    public Authentication getAuthentication() {
        return SecurityContextHolder.getContext().getAuthentication();
    }

    //todo: сделал не очень хорошо, но пока так
    public Teacher getCurrentTeacher() {
        Authentication authentication = getAuthentication();
        User curUser = (User) userService.loadUserByUsername(authentication.getName());
        return entityManager.createQuery("select t from Teacher t where t.user.id = :paramId", Teacher.class).
                setParameter("paramId", curUser.getId()).getSingleResult();
    }
}