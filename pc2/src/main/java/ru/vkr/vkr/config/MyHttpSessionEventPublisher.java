package ru.vkr.vkr.config;

import org.springframework.context.ApplicationContext;
import org.springframework.security.core.session.SessionInformation;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.security.web.session.HttpSessionEventPublisher;
import org.springframework.web.context.support.WebApplicationContextUtils;
import ru.vkr.vkr.entity.User;
import ru.vkr.vkr.service.BridgePc2Service;

import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionEvent;

public class MyHttpSessionEventPublisher extends HttpSessionEventPublisher {

    // ... Прочие методы
    @Override
    public void sessionCreated(HttpSessionEvent event) {
        super.sessionCreated(event);
    }

    @Override
    public void sessionDestroyed(HttpSessionEvent event) {
        String id=null;
        SessionRegistry sessionRegistry = getBean(event, "sessionRegistry");
        SessionInformation sessionInfo = (sessionRegistry != null ? sessionRegistry
                .getSessionInformation(event.getSession().getId()) : null);
        User user = null;
        if (sessionInfo != null) user = (User) sessionInfo.getPrincipal();
        if (null != user) {
            BridgePc2Service bridgePc2Service = getBeanBridgePc2Service(event, "bridgePc2Service");
            bridgePc2Service.logout(user.getLoginPC2());
            System.out.println("destroy session******************************" + user.getUsername() + "\n");
        }
        super.sessionDestroyed(event);
    }

    private SessionRegistry getBean(HttpSessionEvent event, String name){
        HttpSession session = event.getSession();
        ApplicationContext ctx =
                WebApplicationContextUtils.
                        getWebApplicationContext(session.getServletContext());
        return (SessionRegistry) (ctx != null ? ctx.getBean(name) : null);
    }

    private BridgePc2Service getBeanBridgePc2Service (HttpSessionEvent event, String name){
        HttpSession session = event.getSession();
        ApplicationContext ctx =
                WebApplicationContextUtils.
                        getWebApplicationContext(session.getServletContext());
        return (ctx != null ? ctx.getBean(BridgePc2Service.class) : null);
    }
}