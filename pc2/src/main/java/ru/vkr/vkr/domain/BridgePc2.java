package ru.vkr.vkr.domain;

import edu.csus.ecs.pc2.api.IContest;
import edu.csus.ecs.pc2.api.IProblem;
import edu.csus.ecs.pc2.api.ServerConnection;
import edu.csus.ecs.pc2.api.exceptions.LoginFailureException;
import edu.csus.ecs.pc2.api.exceptions.NotLoggedInException;
import edu.csus.ecs.pc2.core.InternalController;
import edu.csus.ecs.pc2.core.model.InternalContest;

import java.lang.reflect.Field;

public class BridgePc2 {
    private static ServerConnection serverConnection;
    private static InternalController internalController;
    private static InternalContest internalContest;

    public static void start(String auth) {
        try {
            serverConnection = new ServerConnection();
            IContest contest = serverConnection.login(auth, auth);

            for (IProblem problem : contest.getProblems()) {
                System.out.println(problem.getShortName() + ")  " + problem.getName() + " " + problem.getValidatorFileName());
            }
            System.out.println("\n\n");
            for (IProblem problem : contest.getAllProblems()) {
                System.out.println(problem.getShortName() + ")  " + problem.getName() + " " + problem.getValidatorFileName());
            }
            try {
                Class classController = serverConnection.getClass();
                Field fieldController = classController.getDeclaredField("controller");
                fieldController.setAccessible(true);
                Object objectController = fieldController.get(serverConnection);
                internalController = (InternalController) objectController;
            } catch (IllegalAccessException | NoSuchFieldException e) {
                e.printStackTrace();
            }

            try {
                Class classContest = serverConnection.getClass();
                Field fieldContest = classContest.getDeclaredField("internalContest");
                fieldContest.setAccessible(true);
                Object objectController = fieldContest.get(serverConnection);
                internalContest = (InternalContest) objectController;
            } catch (IllegalAccessException | NoSuchFieldException e) {
                e.printStackTrace();
            }

        } catch (LoginFailureException e) {
            System.out.println("Could not login because " + e.getMessage());
        }
    }

    public static ServerConnection getServerConnection() {
        return serverConnection;
    }

    public static InternalController getInternalController() {
        return internalController;
    }

    public static InternalContest getInternalContest() {
       return internalContest;
    }

    public static void logoff() {
        try {
            serverConnection.logoff();
        } catch (NotLoggedInException e) {
            e.printStackTrace();
        }
    }
}
