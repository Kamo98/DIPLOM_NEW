package ru.vkr.vkr.domain;

import edu.csus.ecs.pc2.api.IContest;
import edu.csus.ecs.pc2.api.IProblem;
import edu.csus.ecs.pc2.api.ServerConnection;
import edu.csus.ecs.pc2.api.exceptions.LoginFailureException;
import edu.csus.ecs.pc2.api.exceptions.NotLoggedInException;
import edu.csus.ecs.pc2.core.InternalController;
import edu.csus.ecs.pc2.core.model.InternalContest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import ru.vkr.vkr.domain.run.RunStatistic;
import ru.vkr.vkr.domain.run.RunStatisticListener;
import ru.vkr.vkr.facade.AuthenticationFacade;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.lang.reflect.Field;
import java.util.Map;
import java.util.TreeMap;

public class BridgePc2 {
    private static Map<String, ServerConnection> serverConnections = new TreeMap<>();
    private static Map<String, InternalController> internalControllers = new TreeMap<>();
    private static Map<String, InternalContest> internalContests = new TreeMap<>();
    private static Map<String, RunStatisticListener> runStatisticListeners = new TreeMap<>();

    private static final AuthenticationFacade authenticationFacade = new AuthenticationFacade();

    public static void start(String auth) {
        String nameCurUser = getNameCurUser();
        try {
            ServerConnection serverConnection = new ServerConnection();
            IContest contest = serverConnection.login(auth, auth);
            internalControllers.put(nameCurUser, initInternalController(serverConnection));
            internalContests.put(nameCurUser, initInternalContest(serverConnection));
            runStatisticListeners.put(nameCurUser, initContest(contest));

            for (IProblem problem : contest.getProblems()) {
                System.out.println(problem.getShortName() + ")  " + problem.getName() + " " + problem.getValidatorFileName());
            }
            System.out.println("\n\n");
            for (IProblem problem : contest.getAllProblems()) {
                System.out.println(problem.getShortName() + ")  " + problem.getName() + " " + problem.getValidatorFileName());
            }

            serverConnections.put(nameCurUser, serverConnection);
        } catch (LoginFailureException e) {
            System.out.println("Could not login because " + e.getMessage());
        }
    }

    private static String getNameCurUser(){
        return authenticationFacade.getAuthentication().getName();
    }

    private static InternalController initInternalController(ServerConnection serverConnection) {
        try {
            Class classController = serverConnection.getClass();
            Field fieldController = classController.getDeclaredField("controller");
            fieldController.setAccessible(true);
            Object objectController = fieldController.get(serverConnection);
            return (InternalController) objectController;
        } catch (IllegalAccessException | NoSuchFieldException e) {
            e.printStackTrace();
            return null;
        }
    }

    private static InternalContest initInternalContest(ServerConnection serverConnection) {
        try {
            Class classContest = serverConnection.getClass();
            Field fieldContest = classContest.getDeclaredField("internalContest");
            fieldContest.setAccessible(true);
            Object objectController = fieldContest.get(serverConnection);
            return (InternalContest) objectController;
        } catch (IllegalAccessException | NoSuchFieldException e) {
            e.printStackTrace();
            return null;
        }
    }

    private static RunStatisticListener initContest(IContest contest) {
        RunStatistic runStatistic = null;
        RunStatisticListener runStatisticListener;
        try (FileInputStream fileInputStream = new FileInputStream("C:\\diplom\\save.ser");
             ObjectInputStream objectInputStream = new ObjectInputStream(fileInputStream)) {
            runStatistic = (RunStatistic) objectInputStream.readObject();
        } catch (IOException | ClassNotFoundException e) {
            runStatistic = new RunStatistic();
            e.printStackTrace();
        } finally {
            runStatisticListener = new RunStatisticListener(runStatistic);
            contest.addRunListener(runStatisticListener);
        }
        return runStatisticListener;
    }

    public static ServerConnection getServerConnection() {
        return serverConnections.get(getNameCurUser());
    }

    public static RunStatistic getRunStatistic() {
        return runStatisticListeners.get(getNameCurUser()).getRunStatistic();
    }

    public static InternalController getInternalController() {
        return internalControllers.get(getNameCurUser());
    }

    public static InternalContest getInternalContest() {
       return internalContests.get(getNameCurUser());
    }

    public static RunStatisticListener getRunStatisticListener() {
        return runStatisticListeners.get(getNameCurUser());
    }

    public static void logoff(String nameCurUser) {
        try {
            Thread.sleep(1000);
            serverConnections.get(nameCurUser).logoff();
            serverConnections.remove(nameCurUser);
            internalControllers.remove(nameCurUser);
            internalContests.remove(nameCurUser);
            runStatisticListeners.remove(nameCurUser);
        } catch (NotLoggedInException | InterruptedException e) {
            e.printStackTrace();
        }
    }
}
