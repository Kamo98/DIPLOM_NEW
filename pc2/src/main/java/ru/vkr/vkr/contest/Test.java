/*
package ru.vkr.vkr.contest;

import edu.csus.ecs.pc2.api.IClient;
import edu.csus.ecs.pc2.api.IContest;
import edu.csus.ecs.pc2.api.ITeam;
import edu.csus.ecs.pc2.api.ServerConnection;
import edu.csus.ecs.pc2.api.exceptions.LoginFailureException;
import edu.csus.ecs.pc2.api.exceptions.NotLoggedInException;
import edu.csus.ecs.pc2.core.model.ClientType;

import java.util.ArrayList;
import java.util.List;

public class Test {

    private ServerConnection serverConnection;
    private final int countTeams;

    public Test() {
        countTeams = 50;
        connect();
    }

    public List<String> generate_teams() {
        List<String> resultsGen = new ArrayList<>();
        try {

            IContest contest = serverConnection.getContest();
            //contest.getProblemDetails()[0].


            for (int i = 0; i < countTeams; i++) {
                String name = "C_" + (i + 1);
                try {
                    serverConnection.addAccount(ClientType.Type.TEAM.name(), name, "12345");
                    IClient client;
                    //resultsGen.add(name + "  успешно создан");
                    //Thread.sleep(200);
                } catch (Exception e) {
                    resultsGen.add(name + "  не создан. Ошибка!");
                    e.printStackTrace();
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return resultsGen;
    }

    public ITeam[] get_all_teams() {
        ITeam[] teams = null;
        try {
             teams = serverConnection.getContest().getTeams();
        } catch (NotLoggedInException e) {
            e.printStackTrace();
        }
        return teams;
    }

    private void connect() {

        String login = "root";
        String password = "administrator1";

        try {
            serverConnection = new ServerConnection();
            serverConnection.login(login, password);

        } catch (LoginFailureException e) {
            System.out.println("Could not login because " + e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void disconnect () {
        try {
            serverConnection.logoff();
        } catch (NotLoggedInException e) {
            e.printStackTrace();
        }
    }
}
*/
