package ru.vkr.vkr.service;

import edu.csus.ecs.pc2.core.InternalController;
import edu.csus.ecs.pc2.core.model.Language;
import edu.csus.ecs.pc2.core.model.Problem;
import edu.csus.ecs.pc2.core.model.SerializedFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

import java.io.File;

@Service
public class SubmitRunService {
    private static Logger logger = LoggerFactory.getLogger(SubmitRunService.class);

    SerializedFile[] otherFiles = null;

    @Autowired
    ApplicationContext applicationContext;

    private boolean fileExists(String fileName) {
        File file = new File(fileName);
        return file.isFile();
    }

    protected void submitRun(Problem problem, Language language, String fileName) {
        InternalController internalController = (InternalController) applicationContext.getBean("getInternalController");
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
}
