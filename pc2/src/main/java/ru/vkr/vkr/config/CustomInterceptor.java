package ru.vkr.vkr.config;

import org.springframework.context.ApplicationContext;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;
import ru.vkr.vkr.service.BridgePc2Service;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static ru.vkr.vkr.config.MvcConfig.getBeanBridgePc2Service;

@Component
public class CustomInterceptor extends HandlerInterceptorAdapter {

    @Override
    public boolean preHandle(
            HttpServletRequest request,
            HttpServletResponse response,
            Object handler) throws Exception {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (!(auth == null || (auth instanceof AnonymousAuthenticationToken))) {
            BridgePc2Service bridgePc2Service = getBeanBridgePc2Service(request.getSession());
            if (bridgePc2Service != null &&
                    bridgePc2Service.getServerConnection() != null &&
                    (!bridgePc2Service.getServerConnection().isLoggedIn())) {
                request.logout();
                response.sendRedirect(request.getContextPath() + "/login");
                return false;
            }
        }
        System.out.println("******validate***********");
        return true;
    }
}