package ru.vkr.vkr.components;

import edu.csus.ecs.pc2.core.InternalController;
import edu.csus.ecs.pc2.core.model.ClientId;
import edu.csus.ecs.pc2.core.model.InternalContest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;
import org.springframework.security.web.authentication.logout.SimpleUrlLogoutSuccessHandler;
import org.springframework.stereotype.Component;
import ru.vkr.vkr.entity.User;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Component
public class CustomLogoutSuccessHandler extends
        SimpleUrlLogoutSuccessHandler implements LogoutSuccessHandler {

    @Autowired
    ApplicationContext applicationContext;

    @Override
    public void onLogoutSuccess(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication)
            throws IOException, ServletException {
        InternalController internalController = (InternalController) applicationContext.getBean("getInternalController");
        User successUser = (User)authentication.getPrincipal();
        ClientId clientId = InternalController.loginShortcutExpansion(1, successUser.getLoginPC2());
        internalController.logoffUser(clientId);
        internalController.setContest(new InternalContest()); // обновление главного контроллера
        super.onLogoutSuccess(request, response, authentication);
    }
}
