package ru.vkr.vkr.domain;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Component
public class HandlerPc2 {
    @Autowired
    private HttpServletRequest request;

    public void handlerException() {
        try {
            request.logout();
            SecurityContextHolder.clearContext();
        } catch (ServletException e) {
            e.printStackTrace();
        }
    }
}
