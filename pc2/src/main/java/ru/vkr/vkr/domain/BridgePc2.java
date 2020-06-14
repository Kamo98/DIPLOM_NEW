package ru.vkr.vkr.domain;

import edu.csus.ecs.pc2.api.IContest;
import edu.csus.ecs.pc2.api.IProblem;
import edu.csus.ecs.pc2.api.ServerConnection;
import edu.csus.ecs.pc2.api.exceptions.LoginFailureException;
import edu.csus.ecs.pc2.api.exceptions.NotLoggedInException;
import edu.csus.ecs.pc2.core.InternalController;
import edu.csus.ecs.pc2.core.model.InternalContest;
import ru.vkr.vkr.domain.run.RunStatistic;
import ru.vkr.vkr.domain.run.RunStatisticListener;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.lang.reflect.Field;

public class BridgePc2 {
    private static ServerConnection serverConnection;
    private static InternalController internalController;
    private static InternalContest internalContest;
    private static RunStatisticListener runStatisticListener;

    public static void start(String auth) {
        try {
            serverConnection = new ServerConnection();
            IContest contest = serverConnection.login(auth, auth);
            initInternalController();
            initInternalContest();
            initContest(contest);

            for (IProblem problem : contest.getProblems()) {
                System.out.println(problem.getShortName() + ")  " + problem.getName() + " " + problem.getValidatorFileName());
            }
            System.out.println("\n\n");
            for (IProblem problem : contest.getAllProblems()) {
                System.out.println(problem.getShortName() + ")  " + problem.getName() + " " + problem.getValidatorFileName());
            }

        } catch (LoginFailureException e) {
            System.out.println("Could not login because " + e.getMessage());
        }
    }

    private static void initInternalController() {
        try {
            Class classController = serverConnection.getClass();
            Field fieldController = classController.getDeclaredField("controller");
            fieldController.setAccessible(true);
            Object objectController = fieldController.get(serverConnection);
            internalController = (InternalController) objectController;
        } catch (IllegalAccessException | NoSuchFieldException e) {
            e.printStackTrace();
        }
    }

    private static void initInternalContest() {
        try {
            Class classContest = serverConnection.getClass();
            Field fieldContest = classContest.getDeclaredField("internalContest");
            fieldContest.setAccessible(true);
            Object objectController = fieldContest.get(serverConnection);
            internalContest = (InternalContest) objectController;
        } catch (IllegalAccessException | NoSuchFieldException e) {
            e.printStackTrace();
        }
    }

    private static void initContest(IContest contest) {
        RunStatistic runStatistic = null;
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

    public static RunStatisticListener getRunStatisticListener() {
        return runStatisticListener;
    }

    public static void logoff() {
        try {
            Thread.sleep(1000);
            serverConnection.logoff();
        } catch (NotLoggedInException | InterruptedException e) {
            e.printStackTrace();
        }
    }
}
