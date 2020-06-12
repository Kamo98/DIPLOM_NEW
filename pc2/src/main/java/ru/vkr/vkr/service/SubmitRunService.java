package ru.vkr.vkr.service;

import edu.csus.ecs.pc2.api.ILanguage;
import edu.csus.ecs.pc2.api.IProblem;
import edu.csus.ecs.pc2.api.IRun;
import edu.csus.ecs.pc2.api.exceptions.NotLoggedInException;
import edu.csus.ecs.pc2.core.InternalController;
import edu.csus.ecs.pc2.core.log.Log;
import edu.csus.ecs.pc2.core.model.*;
import edu.csus.ecs.pc2.core.security.Permission;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import ru.vkr.vkr.domain.BridgePc2;
import ru.vkr.vkr.domain.FileManager;
import ru.vkr.vkr.domain.RunSubmitDto;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Future;

@Service
public class SubmitRunService {
    private static Logger logger = LoggerFactory.getLogger(SubmitRunService.class);
    private final String nameOfFolder = "submition";
    private boolean serverReplied;


    private ElementId getElementIdRun(int index) {
        InternalController internalController = BridgePc2.getInternalController();
        return BridgePc2.getInternalContest().getRuns()[index].getElementId();
    }


    public void submitRun(Problem problem, int languageIndex, MultipartFile file) {
        Language language = BridgePc2.getInternalContest().getLanguages()[languageIndex];
        IProblem iProblem = null;
        ILanguage iLanguage = null;
        try {
            IProblem problems[] = BridgePc2.getServerConnection().getContest().getProblems();
            for (IProblem iproblem : problems) {
                if (iproblem.getShortName().equals(problem.getShortName())) {
                    iProblem = iproblem;
                    break;
                }
            }

            ILanguage languages[] = BridgePc2.getServerConnection().getContest().getLanguages();
            for (ILanguage ilanguage : languages) {
                if (ilanguage.getName().equals(language.getDisplayName())) {
                    iLanguage = ilanguage;
                    break;
                }
            }
        } catch (NotLoggedInException e) {
            e.printStackTrace();
        }



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
            BridgePc2.getServerConnection().
                    submitRun(iProblem, iLanguage, fileName, new String[0], 0, 0);
        } catch (Exception e) {
            // TODO need to make this cleaner
            logger.error("Exception " + e.getMessage());
        }
    }

    public List<RunSubmitDto> getRunSummit() {
        IInternalContest contest = BridgePc2.getInternalContest();
        List<RunSubmitDto> runSubmitDtos = new ArrayList<>();
        // в отсортированном порядке
        IRun iRuns[] = null;
        try {
            iRuns = BridgePc2.getServerConnection().getContest().getRuns();
        } catch (NotLoggedInException e) {
            e.printStackTrace();
        }

        for (IRun run : iRuns) {
            runSubmitDtos.add(new RunSubmitDto(run.getNumber(),
                    run.getProblem().getName(),
                    run.getSubmissionTime(),
                    run.getLanguage().getName(),
                    run.isSolved() ? "Yes" : "No"));
        }
        return runSubmitDtos;
    }
}
