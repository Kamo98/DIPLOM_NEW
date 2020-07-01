package ru.vkr.vkr.domain;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import ru.vkr.vkr.domain.exception.Pc2Exception;
import ru.vkr.vkr.service.BridgePc2Service;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Component
public class HandlerPc2 {
    @Autowired
    private HttpServletRequest request;

    public void handlerException() throws Pc2Exception {
        try {
            request.logout();
            SecurityContextHolder.clearContext();
        } catch (ServletException e) {
            e.printStackTrace();
        }
        throw new Pc2Exception();
    }
}
