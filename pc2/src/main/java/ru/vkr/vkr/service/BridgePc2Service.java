package ru.vkr.vkr.service;

import edu.csus.ecs.pc2.api.*;
import edu.csus.ecs.pc2.api.exceptions.LoginFailureException;
import edu.csus.ecs.pc2.api.exceptions.NotLoggedInException;
import edu.csus.ecs.pc2.api.implementation.Contest;
import edu.csus.ecs.pc2.core.InternalController;
import edu.csus.ecs.pc2.core.model.InternalContest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.vkr.vkr.domain.HandlerPc2;
import ru.vkr.vkr.domain.Pc2;
import ru.vkr.vkr.domain.run.RunStatisticListener;
import ru.vkr.vkr.facade.AuthenticationFacade;

@Service
public class BridgePc2Service {
    @Autowired
    private AuthenticationFacade authenticationFacade;
    @Autowired
    private HandlerPc2 handlerPc2;

    private String getCurName() {
        return authenticationFacade.getCurrentUser() != null ?
                authenticationFacade.getCurrentUser().getLoginPC2()
                : null;
    }

    public boolean start(String auth) {
        try {
            Pc2.start(auth);
            return true;
        } catch (LoginFailureException | NotLoggedInException | NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
            handlerPc2.handlerException();
            return false;
        }
    }

    public void logout(String auth) {
        try {
            Pc2.logoff(auth);
        } catch (NotLoggedInException e) {
            e.printStackTrace();
        }
    }

    public InternalController getInternalController() {
        try {
            return Pc2.getInternalController(getCurName());
        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
            handlerPc2.handlerException();
        }
        return null;
    }

    public InternalContest getInternalContest() {
        try {
            return Pc2.getInternalContest(getCurName());
        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
            handlerPc2.handlerException();
        }
        return null;
    }

    public Contest getContest() {
        try {
            if (getServerConnection().getContest() == null) {
                throw new NotLoggedInException();
            }
            return getServerConnection().getContest();
        } catch (NotLoggedInException e) {
            e.printStackTrace();
            handlerPc2.handlerException();
        }
        return null;
    }

    public ITeam[] getContestTeams() {
        return getContest() != null ?
                getContest().getTeams() :
                null;
    }

    public IClient[] getContestClients() {
        return getContest() != null ?
                getContest().getClients():
                null;
    }

    public ILanguage[] getContestLanguages() {
        return getContest() != null ?
                getContest().getLanguages():
                null;
    }

    public void submitRun(IProblem iProblem, ILanguage iLanguage, String fileName,
                          String[] additionalFileNames, long overrideSubmissionTimeMS, long overrideRunId) {
        try {
            getServerConnection().submitRun(iProblem, iLanguage, fileName,
                    additionalFileNames, overrideSubmissionTimeMS, overrideRunId);
        } catch (Exception e) {
            e.printStackTrace();
            handlerPc2.handlerException();
        }
    }

    public void addAccount(String role, String login, String password) {
        try {
            getServerConnection().addAccount(role, login, password);
        } catch (Exception e) {
            e.printStackTrace();
            handlerPc2.handlerException();
        }
    }

    public ServerConnection getServerConnection() {
        return Pc2.getServerConnection(getCurName());
    }

    public RunStatisticListener getRunStatisticListener() {
        return Pc2.getRunStatisticListener(getCurName());
    }
}
