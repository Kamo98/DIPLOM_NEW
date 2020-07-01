package ru.vkr.vkr.service;

import edu.csus.ecs.pc2.api.ILanguage;
import edu.csus.ecs.pc2.api.IProblem;
import edu.csus.ecs.pc2.api.IRun;
import edu.csus.ecs.pc2.api.exceptions.NotLoggedInException;
import edu.csus.ecs.pc2.api.implementation.Contest;
import edu.csus.ecs.pc2.core.model.Problem;
import edu.csus.ecs.pc2.core.model.Run;
import edu.csus.ecs.pc2.core.model.RunTestCase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;
import ru.vkr.vkr.domain.FileManager;
import ru.vkr.vkr.domain.problem.ProblemFactory;
import ru.vkr.vkr.domain.run.RunSubmitDto;
import ru.vkr.vkr.domain.run.RunSubmitDtoComparator;
import ru.vkr.vkr.form.SubmitRunForm;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

@Service
public class SubmitRunService {
    private static Logger logger = LoggerFactory.getLogger(SubmitRunService.class);
    private final String nameOfFolder = "submition";
    @Autowired
    private ApplicationContext applicationContext;
    @Autowired
    private BridgePc2Service bridgePc2Service;


    public void submitRun(Long problemId, SubmitRunForm submitRunForm) {
        IProblem iProblem = ((ProblemFactory) applicationContext.getBean("getProblemFactory")).getIProblem(problemId);
        ILanguage iLanguage = bridgePc2Service.getContestLanguages()[submitRunForm.getLanguage()];
        String fileName = getFileNameSubmitRun(submitRunForm, iLanguage);
        logger.info("submitRun for " + iProblem + " " + iLanguage + " file: " + fileName);
        bridgePc2Service.getRunStatisticListener().setSourceCode(false);
        bridgePc2Service.submitRun(iProblem, iLanguage, fileName, new String[0], 0, 0);
    }

    private String getFileNameSubmitRun(SubmitRunForm submitRunForm, ILanguage iLanguage) {
        String fileName = "";
        if (submitRunForm.isFlagSourceCode()) {
            fileName = FileManager.loadSourceToServer(submitRunForm.getSourceCode(), nameOfFolder, iLanguage);
        } else {
            fileName = FileManager.loadFileNewRandomDirToServer(submitRunForm.getMultipartFile(), nameOfFolder);
        }

        if (!FileManager.fileExists(fileName)) {
            File curdir = new File(".");
            String message = fileName + " not found";
            try {
                message = message + " in " + curdir.getCanonicalPath();
            } catch (Exception e) {
                // ignore exception
                message = message + ""; // What a waste of time and code.
            }
            logger.info(message);
            return "";
        }
        return fileName;
    }

    public List<RunSubmitDto> getRunSummit() {
        List<RunSubmitDto> runSubmitDtos = new ArrayList<>();
        Contest contest = bridgePc2Service.getContest();
        IRun iRuns[] = contest.getRuns();
        Method getInternalRunMethod = null;
        try {
            getInternalRunMethod = contest.getClass().getDeclaredMethod("getInternalRun", IRun.class);
            getInternalRunMethod.setAccessible(true);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }

        for (IRun run : iRuns) {
            runSubmitDtos.add(new RunSubmitDto(
                    -run.getNumber(),
                    run.getNumber(),
                    run.getProblem().getName(),
                    run.getSubmissionTime(),
                    run.getLanguage().getName(),
                    run.isFinalJudged() ? getResultRun(getInternalRunMethod, contest, run)
                            : "testing...",
                    Long.parseLong(run.getProblem().getShortName().split("-")[1])));
        }
        // в отсортированном порядке
        runSubmitDtos.sort(new RunSubmitDtoComparator());
        return runSubmitDtos;
    }

    private String getResultRun(Method method, Contest contest, IRun iRun) {
        if (iRun.isSolved()) {
            return "Yes";
        }
        try {
            Run run = (Run) method.invoke(contest, iRun);
            Problem problem = bridgePc2Service.getInternalContest().getProblem(run.getProblemId());
            if (!problem.isStopOnFirstFailedTestCase()) {
                RunTestCase runTestCases[] = run.getRunTestCases();
                int numberTestCases = problem.getNumberTestCases();
                int numberRunTestCases = runTestCases.length;
                int countPassTestCases = 0, currentNumberTestCase = 0;
                while (currentNumberTestCase < numberRunTestCases) {
                    if (runTestCases[currentNumberTestCase++].isPassed()) {
                        countPassTestCases++;
                    }
                }
                double result = ((double) (countPassTestCases) / numberTestCases) * 100;
                return iRun.getJudgementName() + "-->" + (int) result + "%";
            } else {
                return iRun.getJudgementName() + "-->" + run.getRunTestCases().length;
            }
        } catch (IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
        }
        return "No";
    }

    public String showSourceCode(int numberRun) {
        IRun iRun = bridgePc2Service.getContest().getRun(numberRun);
        bridgePc2Service.getRunStatisticListener().setSourceCode(true);
        return new String(iRun.getSourceCodeFileContents()[0], StandardCharsets.UTF_8);
    }
}
