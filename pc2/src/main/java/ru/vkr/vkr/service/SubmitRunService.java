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
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import ru.vkr.vkr.domain.RunSubmitDto;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class SubmitRunService {
    private static Logger logger = LoggerFactory.getLogger(SubmitRunService.class);

    SerializedFile[] otherFiles = null;

    @Value("${spring.servlet.multipart.location}")
    private String location;

    private boolean serverReplied;


    @Autowired
    ApplicationContext applicationContext;

    private boolean fileExists(String fileName) {
        File file = new File(fileName);
        return file.isFile();
    }

    private ElementId getElementIdRun(int index) {
        InternalController internalController = (InternalController) applicationContext.getBean("getInternalController");
        return internalController.getContest().getRuns()[index].getElementId();
    }

    String convertFileToString(MultipartFile file) {
        if (file != null && !file.getOriginalFilename().isEmpty()) {
            String newLocation = location + UUID.randomUUID().toString();
            File uploadDir = new File(newLocation);
            if (!uploadDir.exists()) {
                uploadDir.mkdir();
            }
            String resultFilename = newLocation + "\\" + file.getOriginalFilename();
            try {
                file.transferTo(new File(resultFilename));
            } catch (IOException e) {
                e.printStackTrace();
            }
            return resultFilename;
        }
        return "";
    }

    public void submitRun(int problemIndex, int languageIndex, MultipartFile file) {
        InternalController internalController = (InternalController) applicationContext.getBean("getInternalController");
        Problem problem = internalController.getContest().getProblems()[problemIndex];
        Language language = internalController.getContest().getLanguages()[languageIndex];

        String fileName = convertFileToString(file);
        if (!fileExists(fileName)) {
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


    public void showSource(int index) {
        Thread viewSourceThread = new Thread() {
            public void run() {
                showSourceForSelectedRun(index);
            }
        };
        viewSourceThread.setName("ViewSourceThread-");
        viewSourceThread.start();
    }


    /**
     * Displays a {@link MultipleFileViewer} containing the source code for the run (submission) which is currently selected in the Runs grid.
     *
     * If no run is selected, or more than one run is selected, prompts the user to select just one run (row) in the grid
     * and does nothing else.
     */
    public void showSourceForSelectedRun(int index) {

        InternalController internalController = (InternalController) applicationContext.getBean("getInternalController");
        IInternalContest contest = internalController.getContest();

        // make sure we're allowed to fetch a run
        if (!isAllowed(contest, Permission.Type.ALLOWED_TO_FETCH_RUN)) {
            internalController.getLog().log(Log.WARNING, "Account does not have the permission ALLOWED_TO_FETCH_RUN; cannot view run source.");
            showMessage("Unable to fetch run, check log");
            return;
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
                    while (!serverReplied && waitedMS < 30000) {
                        Thread.sleep(100);
                        waitedMS += 100;
                    }

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
                            showMessage("Unable to fetch selected run; check log");
                            return;
                        }

                    } else {

                        // the server failed to reply to the fetchRun request within the time limit
                        internalController.getLog().log(Log.WARNING, "No response from server to fetch run request after " + waitedMS + "ms");
                        internalController.getLog().log(Log.WARNING, "Unable to fetch run " + run.getNumber() + " from server");
                        showMessage("Unable to fetch selected run; check log");
                        return;
                    }
                }

                //if we get here we know there should be RunFiles in the contest model -- but let's sanity-check that
                if (!contest.isRunFilesPresent(run)) {

                    //something bad happened -- we SHOULD have RunFiles at this point!
                    internalController.getLog().log(Log.SEVERE, "Unable to find RunFiles for run " + run.getNumber() + " -- server error?");
                    showMessage("Unable to fetch selected run; check log");
                    return;

                } else {

                    //get the RunFiles
                    RunFiles runFiles = contest.getRunFiles(run);

                    if (runFiles != null) {

                        // get the (serialized) source files out of the RunFiles
                        SerializedFile mainFile = runFiles.getMainFile();
                        SerializedFile[] otherFiles = runFiles.getOtherFiles();

                        // create a MultiFileViewer in which to display the runFiles
                        MultipleFileViewer mfv = new MultipleFileViewer(internalController.getLog(), "Source files for Site " + run.getSiteNumber() + " Run " + run.getNumber());
                        mfv.setContestAndController(contest, internalController);

                        // add any other files to the MFV (these are added first so that the mainFile will appear at index 0)
                        boolean otherFilesPresent = false;
                        boolean otherFilesLoadedOK = false;
                        if (otherFiles != null) {
                            otherFilesPresent = true;
                            otherFilesLoadedOK = true;
                            for (SerializedFile otherFile : otherFiles) {
                                otherFilesLoadedOK &= mfv.addFilePane(otherFile.getName(), otherFile);
                            }
                        }

                        // add the mainFile to the MFV
                        boolean mainFilePresent = false;
                        boolean mainFileLoadedOK = false;
                        if (mainFile != null) {
                            mainFilePresent = true;
                            mainFileLoadedOK = mfv.addFilePane("Main File" + " (" + mainFile.getName() + ")", mainFile);
                        }

                        // if we successfully added all files, show the MFV
                        if ((!mainFilePresent || (mainFilePresent && mainFileLoadedOK))
                                && (!otherFilesPresent || (otherFilesPresent && otherFilesLoadedOK))) {
                            mfv.setSelectedIndex(0);  //always make leftmost selected; normally this will be MainFile
                            mfv.setVisible(true);
                            showMessage("");
                        } else {
                            internalController.getLog().log(Log.WARNING, "Unable to load run source files into MultiFileViewer");
                            showMessage("Unable to load run source files into MultiFileViewer");
                        }

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

    }
    
    void showMessage(String message) {
        logger.info(message);
    }


    public boolean isAllowed(IInternalContest contest, Permission.Type type) {
        return contest.isAllowed(type);
    }
}
