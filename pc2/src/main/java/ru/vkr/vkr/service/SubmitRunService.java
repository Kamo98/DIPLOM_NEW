package ru.vkr.vkr.service;

import edu.csus.ecs.pc2.core.InternalController;
import edu.csus.ecs.pc2.core.log.Log;
import edu.csus.ecs.pc2.core.model.*;
import edu.csus.ecs.pc2.core.security.Permission;
import edu.csus.ecs.pc2.ui.MultipleFileViewer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import ru.vkr.vkr.domain.FileManager;
import ru.vkr.vkr.domain.RunSubmitDto;
import ru.vkr.vkr.facade.ProblemFacade;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Future;

@Service
public class SubmitRunService {
    private static Logger logger = LoggerFactory.getLogger(SubmitRunService.class);
    SerializedFile[] otherFiles = null;
    private final String nameOfFolder = "submition";
    private boolean serverReplied;
    @Autowired
    private ApplicationContext applicationContext;


    private ElementId getElementIdRun(int index) {
        InternalController internalController = (InternalController) applicationContext.getBean("getInternalController");
        return internalController.getContest().getRuns()[index].getElementId();
    }


    public void submitRun(Problem problem, int languageIndex, MultipartFile file) {
        InternalController internalController = (InternalController) applicationContext.getBean("getInternalController");
        Language language = internalController.getContest().getLanguages()[languageIndex];


        String fileName = FileManager.loadFileToServer(file, nameOfFolder);
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
            return;
        }

        try {
            logger.info("submitRun for " + problem + " " + language + " file: " + fileName);
            internalController.submitRun(problem, language, fileName, otherFiles);
        } catch (Exception e) {
            // TODO need to make this cleaner
            logger.error("Exception " + e.getMessage());
        }
    }

    public List<RunSubmitDto> getRunSummit() {
        InternalController internalController = (InternalController) applicationContext.getBean("getInternalController");
        IInternalContest contest = internalController.getContest();
        List<RunSubmitDto> runSubmitDtos = new ArrayList<>();
        for (Run run : internalController.getContest().getRuns()) {
            runSubmitDtos.add(new RunSubmitDto(run.getNumber(),
                    getProblemTitle(contest, run.getProblemId()),
                    new Long(run.getElapsedMins()),
                    getLanguageTitle(contest, run.getLanguageId()),
                    getJudgementResultString(contest, run)));
        }
        return runSubmitDtos;
    }

    private String getProblemTitle(IInternalContest contest, ElementId problemId) {
        Problem problem = contest.getProblem(problemId);
        if (problem != null) {
            return problem.toString();
        }
        return "Problem ?";
    }

    private String getLanguageTitle(IInternalContest contest, ElementId languageId) {
        for (Language language : contest.getLanguages()) {
            if (language.getElementId().equals(languageId)) {
                return language.toString();
            }
        }
        return "Language ?";
    }

    /**
     * Return the judgement/status for the run.
     *
     * @param run
     * @return a string that represents the state of the run
     */
    private String getJudgementResultString(IInternalContest contest, Run run) {

        String result = "";

        if (run.isJudged()) {

            if (run.isSolved()) {
                result = contest.getJudgements()[0].getDisplayName();

                // only consider changing the state to new if we are a team
                if (!run.getJudgementRecord().isSendToTeam()) {
                    result = Run.RunStates.NEW.toString();
                }

            } else {
                result = "No";
                JudgementRecord judgementRecord = run.getJudgementRecord();
                if (judgementRecord != null && judgementRecord.getJudgementId() != null) {
                    if (judgementRecord.isUsedValidator() && judgementRecord.getValidatorResultString() != null) {
                        result = judgementRecord.getValidatorResultString();
                    } else {
                        Judgement judgement = contest.getJudgement(judgementRecord.getJudgementId());
                        if (judgement != null) {
                            result = judgement.toString();
                        }
                    }
                    if (!judgementRecord.isSendToTeam()) {
                        result = Run.RunStates.NEW.toString();
                    }
                }
            }
        } else {
            result = Run.RunStates.NEW.toString();
        }
        if (run.isDeleted()) {
            result = "DEL " + result;
        }
        return result;
    }

    @Async("threadPoolTaskExecutor")
    public Future<String> showSourceForSelectedRun(int index) {

        InternalController internalController = (InternalController) applicationContext.getBean("getInternalController");
        IInternalContest contest = internalController.getContest();

        // make sure we're allowed to fetch a run
        if (!isAllowed(contest, Permission.Type.ALLOWED_TO_FETCH_RUN)) {
            internalController.getLog().log(Log.WARNING, "Account does not have the permission ALLOWED_TO_FETCH_RUN; cannot view run source.");
            showMessage("Unable to fetch run, check log");
        }

        // we are allowed to view source and there's exactly one run selected; try to obtain the run source and display it in a MFV
        try {
            ElementId elementId = getElementIdRun(index);
            Run run = contest.getRun(elementId);

            // make sure we found the currently selected run
            if (run != null) {

                showMessage("Preparing to display source code for run " + run.getNumber() + " at site " + run.getSiteNumber());
                internalController.getLog().log(Log.INFO, "Preparing to display source code for run " + run.getNumber() + " at site " + run.getSiteNumber());

                //the following forces a (read-only) checkout from the server; it makes more sense to first see if we already have the
                // necessary RunFiles and then if not to issue a "Fetch" request rather than a "checkout" request
//                internalController.checkOutRun(run, true, false); // checkoutRun(run, isReadOnlyRequest, isComputerJudgedRequest)

                //check if we already have the RunFiles for the run
                if (!contest.isRunFilesPresent(run)) {

                    //we don't have the files; request them from the server
                    internalController.fetchRun(run);

                    // wait for the server to reply (i.e., to make a callback to the run listener) -- but only for up to 30 sec
                    int waitedMS = 0;
                    serverReplied = false;

                    //check if we got a reply from the server
                    if (serverReplied) {
                        RunFiles fetchedRunFiles = contest.getRunFiles(run);
                        //the server replied; see if we got some RunFiles
                        if (fetchedRunFiles != null) {

                            //we got some RunFiles from the server; put them into the contest model
                            contest.updateRunFiles(run, fetchedRunFiles);

                        } else {

                            //we got a reply from the server but we didn't get any RunFiles
                            internalController.getLog().log(Log.WARNING, "Server failed to return RunFiles in response to fetch run request");
                            internalController.getLog().log(Log.WARNING, "Unable to fetch source files for run " + run.getNumber() + " from server");
                            return new AsyncResult<String>("Unable to fetch selected run; check log");
                        }

                    } else {

                        // the server failed to reply to the fetchRun request within the time limit
                        internalController.getLog().log(Log.WARNING, "No response from server to fetch run request after " + waitedMS + "ms");
                        internalController.getLog().log(Log.WARNING, "Unable to fetch run " + run.getNumber() + " from server");
                        return new AsyncResult<String>("Unable to fetch selected run; check log");
                    }
                }

                //if we get here we know there should be RunFiles in the contest model -- but let's sanity-check that
                if (!contest.isRunFilesPresent(run)) {
                    //something bad happened -- we SHOULD have RunFiles at this point!
                    internalController.getLog().log(Log.SEVERE, "Unable to find RunFiles for run " + run.getNumber() + " -- server error?");
                    return new AsyncResult<String>("Unable to fetch selected run; check log");
                } else {
                    //get the RunFiles
                    RunFiles runFiles = contest.getRunFiles(run);

                    if (runFiles != null) {
                        // get the (serialized) source files out of the RunFiles
                        SerializedFile mainFile = runFiles.getMainFile();
                        return new AsyncResult<String>(new String(mainFile.getBuffer()));
                    } else {
                        // runfiles is null
                        internalController.getLog().log(Log.WARNING, "Unable to obtain RunFiles for Site " + run.getSiteNumber() + " run " + run.getNumber());
                        showMessage("Unable to obtain RunFiles for selected run");
                    }

                }

            } else {
                // contest.getRun() returned null
                internalController.getLog().log(Log.WARNING, "Selected run not found");
                showMessage("Selected run not found");
            }

        } catch (Exception e) {
            internalController.getLog().log(Log.WARNING, "Exception logged ", e);
            showMessage("Unable to show run source, check log");
        }
        return null;
    }

    String showMessage(String message) {
        logger.info(message);
        return message;
    }


    public boolean isAllowed(IInternalContest contest, Permission.Type type) {
        return contest.isAllowed(type);
    }
}
