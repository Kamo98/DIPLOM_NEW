package ru.vkr.vkr.domain;

import edu.csus.ecs.pc2.core.IInternalController;
import edu.csus.ecs.pc2.core.log.Log;
import edu.csus.ecs.pc2.core.model.*;
import edu.csus.ecs.pc2.core.security.Permission;

public class RunStatisticListener {

    private Log log = null;

    private boolean displayConfirmation = true;

    private IInternalController controller;

    private IInternalContest contest;


    public void setController(IInternalController controller) {
        this.controller = controller;
        this.contest = controller.getContest();
        controller.getContest().addRunListener(new MyRunListener());
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

    protected class MyRunListener implements IRunListener {

        public void runAdded(RunEvent event) {
            // ignore
        }

        public void runChanged(RunEvent event) {
            Run run = event.getRun();
            Run.RunStates status = run.getStatus();
            Account account = contest.getAccount(run.getSubmitter());
            // skip emitting xml for runs that are not completed or for an account that should not be shown
            if (!account.isAllowed(Permission.Type.DISPLAY_ON_SCOREBOARD) || !status.equals(Run.RunStates.JUDGED)) {
                return;
            }
            String response = getContest().getJudgement(run.getJudgementRecord().getJudgementId()).toString();
            // передача данных от подсистемы тестирования --> подсистеме сбора статистики
            getController().getRunStatistic().updateStatisticData(run, getContest().getProblem(run.getProblemId()), response);
            FileManager.saveRunStatistic(getController());
        }

        public void runRemoved(RunEvent event) {
            // ignore
        }

        public void refreshRuns(RunEvent event) {
            // ignore
        }
    }
}

