package ru.vkr.vkr.domain.run;

import edu.csus.ecs.pc2.api.IClient;
import edu.csus.ecs.pc2.api.IRun;
import edu.csus.ecs.pc2.api.listener.IRunEventListener;
import ru.vkr.vkr.domain.FileManager;

public class RunStatisticListener implements IRunEventListener {
    private RunStatistic runStatistic;
    private boolean isSourceCode = false;

    public RunStatisticListener(RunStatistic runStatistic) {
        this.runStatistic = runStatistic;
    }

    public void setSourceCode(boolean sourceCode) {
        this.isSourceCode = sourceCode;
    }

    @Override
    public void runSubmitted(IRun iRun) {

    }

    @Override
    public void runDeleted(IRun iRun) {

    }

    @Override
    public void runCheckedOut(IRun iRun, boolean b) {

    }

    @Override
    public void runJudged(IRun iRun, boolean b) {
        if (b && iRun.getTeam().getType().equals(IClient.ClientType.TEAM_CLIENT) && !isSourceCode) {
            runStatistic.updateStatisticData(iRun);
            FileManager.saveRunStatistic(runStatistic);
        }
    }

    @Override
    public void runUpdated(IRun iRun, boolean b) {
        // ignore
    }

    @Override
    public void runCompiling(IRun iRun, boolean b) {
        // ignore
    }

    @Override
    public void runExecuting(IRun iRun, boolean b) {
        // ignore
    }

    @Override
    public void runValidating(IRun iRun, boolean b) {
        // ignore
    }

    @Override
    public void runJudgingCanceled(IRun iRun, boolean b) {
        if (b && iRun.getTeam().getType().equals(IClient.ClientType.TEAM_CLIENT) && !isSourceCode) {
            runStatistic.updateStatisticData(iRun);
            FileManager.saveRunStatistic(runStatistic);
        }
    }
}
