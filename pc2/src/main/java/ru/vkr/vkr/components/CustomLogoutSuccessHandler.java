package ru.vkr.vkr.components;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;
import org.springframework.security.web.authentication.logout.SimpleUrlLogoutSuccessHandler;
import org.springframework.stereotype.Component;
import ru.vkr.vkr.entity.User;
import ru.vkr.vkr.service.BridgePc2Service;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Component
public class CustomLogoutSuccessHandler extends
        SimpleUrlLogoutSuccessHandler implements LogoutSuccessHandler {

    @Autowired
    ApplicationContext applicationContext;
    @Autowired
    private BridgePc2Service bridgePc2Service;

    @Override
    public void onLogoutSuccess(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication) throws IOException, ServletException {
        bridgePc2Service.logout(((User) authentication.getPrincipal()).getLoginPC2());
        super.onLogoutSuccess(request, response, authentication);
    }
}
