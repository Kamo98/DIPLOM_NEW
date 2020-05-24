package ru.vkr.vkr.service;

import edu.csus.ecs.pc2.core.InternalController;
import edu.csus.ecs.pc2.core.model.Language;
import edu.csus.ecs.pc2.core.model.Problem;
import edu.csus.ecs.pc2.core.model.SerializedFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

@Service
public class SubmitRunService {
    private static Logger logger = LoggerFactory.getLogger(SubmitRunService.class);

    SerializedFile[] otherFiles = null;

    @Value("${spring.servlet.multipart.location}")
    private String location;

    @Autowired
    ApplicationContext applicationContext;

    private boolean fileExists(String fileName) {
        File file = new File(fileName);
        return file.isFile();
    }

    String convertFileToString(MultipartFile file) {
        if (file != null && !file.getOriginalFilename().isEmpty()) {
            String newLocation = location  + UUID.randomUUID().toString();
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
}
