package ru.vkr.vkr.domain;

import edu.csus.ecs.pc2.core.IInternalController;
import edu.csus.ecs.pc2.core.Utilities;
import edu.csus.ecs.pc2.core.log.Log;
import edu.csus.ecs.pc2.core.model.*;
import edu.csus.ecs.pc2.core.security.Permission;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;

public class RunStatisticListener {

    private Log log = null;

    private boolean displayConfirmation = true;

    private IInternalController controller;

    private IInternalContest contest;


    public void setController(IInternalController controller) {
        this.controller = controller;
        this.contest = controller.getContest();
        this.contest.addRunListener(new RunListenerImplementation());
    }

    IInternalContest getContest() {
        return this.contest;
    }

    IInternalController getController() {
        return this.controller;
    }

    public boolean isAllowed(Permission.Type type) {
        return getContest().isAllowed(type);
    }

    public void saveRunStatistic() {
        //создаем 2 потока для сериализации объекта и сохранения его в файл
        try(FileOutputStream outputStream = new FileOutputStream("Y:\\diplom\\save.ser");
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(outputStream)) {
            // сохраняем игру в файл
            objectOutputStream.writeObject(getController().getRunStatistic());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    /**
     * Run Listener
     *
     * @author pc2@ecs.csus.edu
     * @version $Id$
     */

    // $HeadURL$
    public class RunListenerImplementation implements IRunListener {

        public void runAdded(RunEvent event) {
            // check if this is a team; if so, pop up a confirmation dialog
            if (getContest().getClientId().getClientType() == ClientType.Type.TEAM) {
                showResponseToTeam(event);
            }
        }

        public void refreshRuns(RunEvent event) {

        }

        public void runChanged(RunEvent event) {

            // check if this is a team; if so, pop up a response dialog
            if (getContest().getClientId().getClientType() == ClientType.Type.TEAM) {
                showResponseToTeam(event);
            }

            //code copied from FetchRunService.RunListenerImplementation.runChanged():

            RunEvent.Action action = event.getAction();
            RunEvent.Action details = event.getDetailedAction();
            Run aRun = event.getRun();
            RunFiles aRunFiles = event.getRunFiles();
            String msg = event.getMessage();

            getController().getLog().log(Log.INFO, "RunsPane.RunListener: Action=" + action + "; DetailedAction=" + details + "; msg=" + msg
                    + "; run=" + aRun + "; runFiles=" + aRunFiles);


            if (event.getRun() != null) {

                // RUN_NOT_AVAILABLE is undirected (sentToClient is null)
                if (event.getAction().equals(RunEvent.Action.RUN_NOT_AVAILABLE)) {

                    getController().getLog().log(Log.WARNING, "Reply from server: requested run not available");

                } else {

                    //make sure this RunEvent was meant for me
                    if (event.getSentToClientId() != null && event.getSentToClientId().equals(getContest().getClientId())) {

                        getController().getLog().log(Log.INFO, "Reply from server: " + "Run Status=" + event.getAction()
                                + "; run=" + event.getRun() + ";  runFiles=" + event.getRunFiles());

                    } else {

                        ClientId toClient = event.getSentToClientId();
                        ClientId myID = getContest().getClientId();

                        getController().getLog().log(Log.INFO, "Event not for me: sent to " + toClient + " but my ID is " + myID);
                    }
                }

            } else {
                //run from server was null
                getController().getLog().log(Log.WARNING, "Run received from server was null");
            }
        }

        public void runRemoved(RunEvent event) {
        }

        /**
         * Show the Run Judgement.
         * <p>
         * Checks the run in the specified run event and (potentially) displays a results dialog. If the run has been judged, has a valid judgement record, and is the "active" judgement for scoring
         * purposes, displays a modal MessageDialog to the Team containing the judgement results. This method assumes the caller has already verified this is a TEAM client; failure to do that on the
         * caller's part will cause other clients to see the Run Response dialog...
         */
        private void showResponseToTeam(RunEvent event) {

            if (!displayConfirmation) {
                return;
            }

            Run theRun = event.getRun();
            String problemName = getContest().getProblem(theRun.getProblemId()).toString();
            String languageName = getContest().getLanguage(theRun.getLanguageId()).toString();
            int runId = theRun.getNumber();
            saveRunStatistic();

            // getContest().getProblem(theRun.getProblemId()).getElementId().getNum();

            // build an HTML tag that sets the font size and color for the answer
            String responseFormat = "";

            // check if the run has been judged
            if (theRun.isJudged() && (!theRun.isDeleted())) {

                // check if there's a legit judgement
                JudgementRecord judgementRecord = theRun.getJudgementRecord();
                if (judgementRecord != null) {

                    if (!judgementRecord.isSendToTeam()) {

                        /**
                         * Do not show this judgement to the team, the judge indicated
                         * that the team should not be notified.
                         */
                        return; // ------------------------------------------------------ RETURN
                    }

                    // check if this is the scoreable judgement
                    boolean isActive = judgementRecord.isActive();
                    if (isActive) {

                        String response = getContest().getJudgement(judgementRecord.getJudgementId()).toString();

                        // it's a valid judging response (presumably to a team);
                        // get the info from the run and display it in a modal popup
                        if (judgementRecord.isSolved()) {
                            responseFormat += "<FONT COLOR=\"00FF00\" SIZE=+2>"; // green, larger
                        } else {
                            responseFormat += "<FONT COLOR=RED>"; // red, current size

                            String validatorJudgementName = judgementRecord.getValidatorResultString();
                            if (judgementRecord.isUsedValidator() && validatorJudgementName != null) {
                                if (validatorJudgementName.trim().length() == 0) {
                                    validatorJudgementName = "undetermined";
                                }
                                response = validatorJudgementName;
                            }
                        }

                        String judgeComment = theRun.getCommentsForTeam();
                        try {
                            // передача данных от подсистемы тестирования --> подсистеме сбора статистики
                            getController().getRunStatistic().updateStatisticData(theRun, getContest().getProblem(theRun.getProblemId()), response);
                            saveRunStatistic();

                            String displayString = "<HTML><FONT SIZE=+1>Judge's Response<BR><BR>" + "Problem: <FONT COLOR=BLUE>" + Utilities.forHTML(problemName) + "</FONT><BR><BR>"
                                    + "Language: <FONT COLOR=BLUE>" + Utilities.forHTML(languageName) + "</FONT><BR><BR>" + "Run Id: <FONT COLOR=BLUE>" + runId + "</FONT><BR><BR><BR>"
                                    + "Judge's Response: " + responseFormat + Utilities.forHTML(response) + "</FONT><BR><BR><BR>";

                            if (theRun.getStatus().equals(Run.RunStates.MANUAL_REVIEW)) {
                                displayString += "<FONT SIZE='+2'>NOTE: This is a <FONT COLOR='RED'>Preliminary</FONT> Judgement</FONT><BR><BR><BR>";
                            }

                            if (judgeComment != null) {
                                if (judgeComment.length() > 0) {
                                    displayString += "Judge's Comment: " + Utilities.forHTML(judgeComment) + "<BR><BR><BR>";
                                }
                            }

                            displayString += "</FONT></HTML>";


                        } catch (Exception e) {
                            // TODO need to make this cleaner
                            log.warning("Exception handling Run Response: " + e.getMessage());
                        }
                    }
                }
            } else if (!theRun.isDeleted()) {
                // not judged
                try {
                    String displayString = "<HTML><FONT SIZE=+1>Confirmation of Run Receipt<BR><BR>" + "Problem: <FONT COLOR=BLUE>" + Utilities.forHTML(problemName) + "</FONT><BR><BR>"
                            + "Language: <FONT COLOR=BLUE>" + Utilities.forHTML(languageName) + "</FONT><BR><BR>" + "Run Id: <FONT COLOR=BLUE>" + runId + "</FONT><BR><BR><BR>";

                    displayString += "</FONT></HTML>";


                } catch (Exception e) {
                    log.warning("Exception handling Run Confirmation: " + e.getMessage());
                }
            }
        }
    }
}

