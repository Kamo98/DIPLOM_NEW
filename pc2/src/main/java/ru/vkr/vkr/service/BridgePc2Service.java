package ru.vkr.vkr.service;

import edu.csus.ecs.pc2.api.ServerConnection;
import edu.csus.ecs.pc2.core.InternalController;
import edu.csus.ecs.pc2.core.model.InternalContest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.vkr.vkr.domain.Pc2;
import ru.vkr.vkr.domain.run.RunStatisticListener;
import ru.vkr.vkr.facade.AuthenticationFacade;

@Service
public class BridgePc2Service {
    @Autowired
    private AuthenticationFacade authenticationFacade;

    private String getCurName() {
        return authenticationFacade.getCurrentUser() != null ?
                authenticationFacade.getCurrentUser().getLoginPC2()
                : null;
    }

    public void start(String auth) {
        Pc2.start(auth);
    }

    public void logout(String auth) {
        Pc2.logoff(auth);
    }

    public InternalController getInternalController() {
        return Pc2.getInternalController(getCurName());
    }

    public InternalContest getInternalContest() {
        return Pc2.getInternalContest(getCurName());
    }

    public ServerConnection getServerConnection() {
        return Pc2.getServerConnection(getCurName());
    }

    public RunStatisticListener getRunStatisticListener() {
        return Pc2.getRunStatisticListener(getCurName());
    }
}
