package ru.vkr.vkr.domain;

import edu.csus.ecs.pc2.api.IContest;
import edu.csus.ecs.pc2.api.IProblem;
import edu.csus.ecs.pc2.api.ServerConnection;
import edu.csus.ecs.pc2.api.exceptions.LoginFailureException;
import edu.csus.ecs.pc2.api.exceptions.NotLoggedInException;
import edu.csus.ecs.pc2.api.implementation.ConnectionEventListenerList;
import edu.csus.ecs.pc2.api.implementation.Contest;
import edu.csus.ecs.pc2.api.listener.IConnectionEventListener;
import edu.csus.ecs.pc2.core.InternalController;
import edu.csus.ecs.pc2.core.model.InternalContest;
import javafx.util.Pair;
import ru.vkr.vkr.domain.run.RunStatistic;
import ru.vkr.vkr.domain.run.RunStatisticListener;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Vector;


public class Pc2 {
    private static HashMap<String, Pair<ServerConnection, Long> > connections = new HashMap<>();
    private static HashMap<String, RunStatisticListener>  runStatisticListeners = new HashMap<>();

    private static ServerConnection createNewServerConnection() {
        return new ServerConnection();
    }

    public static void start(String auth) {
        if (!connections.containsKey(auth)) {
            ServerConnection serverConnection = createNewServerConnection();
            try {
                IContest contest = serverConnection.login(auth, auth);
                initContest(contest, auth);
                for (IProblem problem : contest.getProblems()) {
                    System.out.println(problem.getShortName() + ")  " + problem.getName() + " " + problem.getValidatorFileName());
                }
                cleanConnectionEventListenerList(serverConnection.getContest());
            } catch (LoginFailureException e) {
                System.out.println("Could not login because " + e.getMessage());
            } catch (NotLoggedInException e) {
                e.printStackTrace();
            }
            //put the team's connection to the PC2 server into the (static, class-wide) connections map under the team's "id"
            connections.put(auth, new Pair<>(serverConnection, 1L));
        } else {
            Long newCountConnection = connections.get(auth).getValue() + 1;
            connections.put(auth, new Pair<>(connections.get(auth).getKey(), newCountConnection));
        }
    }

    public static InternalController getInternalController(String auth) {
        try {
            ServerConnection serverConnection = getServerConnection(auth);
            Class classController = serverConnection.getClass();
            Field fieldController = classController.getDeclaredField("controller");
            fieldController.setAccessible(true);
            Object objectController = fieldController.get(serverConnection);
            return (InternalController) objectController;
        } catch (IllegalAccessException | NoSuchFieldException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static InternalContest getInternalContest(String auth) {
        try {
            ServerConnection serverConnection = getServerConnection(auth);
            Class classContest = serverConnection.getClass();
            Field fieldContest = classContest.getDeclaredField("internalContest");
            fieldContest.setAccessible(true);
            Object objectController = fieldContest.get(serverConnection);
            return (InternalContest) objectController;
        } catch (IllegalAccessException | NoSuchFieldException e) {
            e.printStackTrace();
        }
        return null;
    }

    private static void initContest(IContest contest, String auth) {
        RunStatistic runStatistic = null;
        try (FileInputStream fileInputStream = new FileInputStream("C:\\diplom\\save.ser");
             ObjectInputStream objectInputStream = new ObjectInputStream(fileInputStream)) {
            runStatistic = (RunStatistic) objectInputStream.readObject();
        } catch (IOException | ClassNotFoundException e) {
            runStatistic = new RunStatistic();
            e.printStackTrace();
        } finally {
            RunStatisticListener runStatisticListener = new RunStatisticListener(runStatistic);
            contest.addRunListener(runStatisticListener);
            runStatisticListeners.put(auth, runStatisticListener);
        }
    }

    //Очищает список слушателей ConnectionEventListener, чтобы не обнулялся contest в ServerConnection
    private static void cleanConnectionEventListenerList (Contest contest) {
        try {
            Class classController = contest.getClass();
            Field fieldController = classController.getDeclaredField("connectionEventListenerList");
            fieldController.setAccessible(true);
            Object objectController = fieldController.get(contest);
            ConnectionEventListenerList listenerList = (ConnectionEventListenerList) objectController;

            Class classController2 = listenerList.getClass();
            Field fieldController2 = classController2.getDeclaredField("listenerList");
            fieldController2.setAccessible(true);
            Object objectController2 = fieldController2.get(listenerList);
            Vector<IConnectionEventListener> listenerVector = (Vector<IConnectionEventListener>) objectController2;
            listenerVector.removeAllElements();

        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    public static ServerConnection getServerConnection(String auth) {
        return connections.get(auth).getKey();
    }

    public static RunStatisticListener getRunStatisticListener(String auth) {
        return runStatisticListeners.get(auth);
    }

    public static void logoff(String auth) {
        Long newCountConnection = connections.get(auth).getValue() - 1;
        connections.put(auth, new Pair<>(connections.get(auth).getKey(), newCountConnection));
        if (newCountConnection <= 0L) {
            ServerConnection serverConnection = getServerConnection(auth);
            try {
                Thread.sleep(1000);
                serverConnection.logoff();
            } catch (NotLoggedInException | InterruptedException e) {
                e.printStackTrace();
            }
            //remove the team's PC2 server connection from the global hashmap
            connections.remove(auth, new Pair<>(serverConnection, newCountConnection));
        }
    }
}
