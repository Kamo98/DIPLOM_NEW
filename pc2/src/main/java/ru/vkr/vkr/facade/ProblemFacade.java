package ru.vkr.vkr.facade;

import ch.qos.logback.core.net.server.Client;
import edu.csus.ecs.pc2.core.InternalController;
import edu.csus.ecs.pc2.core.Utilities;
import edu.csus.ecs.pc2.core.model.*;
import edu.csus.ecs.pc2.ui.MultipleDataSetPane;
import edu.csus.ecs.pc2.validator.pc2Validator.PC2ValidatorSettings;
import org.antlr.v4.runtime.misc.Array2DHashSet;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import ru.vkr.vkr.form.LoadTestsForm;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.*;

@Component
public class ProblemFacade {

    @Value("${upload.path}")
    private String uploadPath;

    @Autowired
    private ApplicationContext applicationContext;


    public Collection<Problem> getAllProblems() {
        InternalController internalController = (InternalController) applicationContext.getBean("getInternalController");
        ArrayList<Problem> problems = new ArrayList<>();
        Collections.addAll(problems, internalController.getContest().getProblems());
        return problems;
    }

    public void addMyProblem() {
        InternalController internalController = (InternalController) applicationContext.getBean("getInternalController");

        String baseDirectoryName = "H:\\Университет\\8 сем - FINISH!!!\\Диплом\\DIPLOM\\pc2v9-PC2_RELEASE_9_6_x\\pc2\\src\\main\\resources\\static\\tests\\T121";

        //Основные параметры задачи
        Problem problem = new Problem("Test121");
        problem.setShortName("T121");
        problem.setTimeOutInSeconds(2);

        //Поток для чтения
        problem.setReadInputDataFromSTDIN(true);

        //Параметры чекера
        problem.setValidatorType(Problem.VALIDATOR_TYPE.PC2VALIDATOR);
        PC2ValidatorSettings pc2ValidatorSettings = new PC2ValidatorSettings();
        pc2ValidatorSettings.setWhichPC2Validator(1);
        problem.setPC2ValidatorSettings(pc2ValidatorSettings);
        problem.setShowValidationToJudges(true);

        //Тесты
//        problem.setDataFileName("01.in");
//        problem.setAnswerFileName("01.ans");
//        problem.addTestCaseFilenames("01.in", "01.ans");
//        problem.addTestCaseFilenames("02.in", "02.ans");
//        problem.addTestCaseFilenames("03.in", "03.ans");
//        problem.setUsingExternalDataFiles(false);
//        problem.setExternalDataFileLocation("H:\\Университет\\8 сем - FINISH!!!\\Диплом\\DIPLOM\\pc2v9-PC2_RELEASE_9_6_x\\pc2\\src\\main\\resources\\static\\tests\\Test121");
//        //problem.j

        //Судьи
        problem.setComputerJudged(true);
        problem.setManualReview(false);
        problem.setShowCompareWindow(true);


//        AutoJudgeSetting autoJudgeSetting = new AutoJudgeSetting("TestJudge");
//
//        ClientId[] clientIds = internalController.getContest().getLocalLoggedInClients(ClientType.Type.JUDGE);
//        //clientIds[0].
//
//        Vector<Account> judges = internalController.getContest().getAccounts(ClientType.Type.JUDGE);

        IInternalContest contest = internalController.getContest();


        final boolean externalFiles = true;
        problem.setUsingExternalDataFiles(externalFiles);
        problem.setExternalDataFileLocation("H:\\Университет\\8 сем - FINISH!!!\\Диплом\\DIPLOM\\pc2v9-PC2_RELEASE_9_6_x\\pc2\\src\\main\\resources\\static\\tests\\T121");
        problem.addTestCaseFilenames("01.in", "01.ans");
        problem.addTestCaseFilenames("02.in", "02.ans");
        problem.addTestCaseFilenames("03.in", "03.ans");

        ProblemDataFiles problemDataFiles = loadDataFiles(problem, null, baseDirectoryName, ".in", ".ans", externalFiles);



        Problem problems[] = contest.getProblems();
//        //Problem problem = problems[problems.length-1].copy("Test120_copy");
//
//        ProblemDataFiles problemDataFiles = new ProblemDataFiles(problem);
//
//        SerializedFile fIn[] = new SerializedFile[]{
//                new SerializedFile(baseDirectoryName + "\\01.in"),
//                new SerializedFile(baseDirectoryName + "\\02.in"),
//                new SerializedFile(baseDirectoryName + "\\03.in"),
//        };
//
//        SerializedFile fOut[] = new SerializedFile[]{
//                new SerializedFile(baseDirectoryName + "\\01.ans"),
//                new SerializedFile(baseDirectoryName + "\\02.ans"),
//                new SerializedFile(baseDirectoryName + "\\03.ans"),
//        };
//        problemDataFiles.setJudgesDataFiles(fIn);
//        problemDataFiles.setJudgesAnswerFiles(fOut);
        //problemDataFiles.set

        internalController.addNewProblem(problem, problemDataFiles);
    }


    public void loadTestFiles(LoadTestsForm loadTestsForm) throws IOException {
        String extensionIn = loadTestsForm.getExtensionIn();
        String extensionAns = loadTestsForm.getExtensionAns();

        String problemId = "problem-1";
        String uloadDirPath = uploadPath + "/tests/" + problemId;
        File uploadDir = new File(uloadDirPath);

        if (!uploadDir.exists())
            uploadDir.mkdir();


        //Изменение расширений файлов с extensionIn и extensionAns на .in и .ans
        HashMap<String, MultipartFile> filesIn = new HashMap<>();
        HashMap<String, MultipartFile> filesAns = new HashMap<>();

        for (int i = 0; i < loadTestsForm.getDirTests().length; i++) {
            MultipartFile file = loadTestsForm.getDirTests()[i];
            String origFileName = file.getOriginalFilename();

            if (origFileName.endsWith(extensionIn)) {           //Файл входной
                String onlyName = origFileName.substring(0, origFileName.length() - extensionIn.length());
                if (filesAns.containsKey(onlyName)) {
                    //Нашли пару файлов, значит загружаем тест
                    String resFileName = onlyName + ".in";
                    file.transferTo(new File(uloadDirPath + "/" + resFileName));
                    resFileName = onlyName + ".ans";
                    filesAns.get(onlyName).transferTo(new File(uloadDirPath + "/" + resFileName));

                    filesAns.remove(onlyName);
                } else
                    filesIn.put(onlyName, file);

            } else if (origFileName.endsWith(extensionAns)) {   //Файл выходной
                String onlyName = origFileName.substring(0, origFileName.length() - extensionAns.length());
                if (filesIn.containsKey(onlyName)) {
                    //Нашли пару файлов, значит загружаем тест
                    String resFileName = onlyName + ".in";
                    filesIn.get(onlyName).transferTo(new File(uloadDirPath + "/" + resFileName));
                    resFileName = onlyName + ".ans";
                    file.transferTo(new File(uloadDirPath + "/" + resFileName));

                    filesIn.remove(onlyName);
                } else
                    filesAns.put(onlyName, file);
            }
        }

        
    }



    private ProblemDataFiles loadDataFiles(Problem aProblem, ProblemDataFiles files, String dataFileBaseDirectory, String dataExtension, String answerExtension, boolean externalDataFiles) {

        if (files == null) {
            files = new ProblemDataFiles(aProblem);
        } else {
            /**
             * A sanity check. It makes no sense to update an existing ProblemDataFiles for a different Problem.
             */
            if (aProblem != null && !files.getProblemId().equals(aProblem.getElementId())) {
                throw new RuntimeException("problem and data files are not for the same problem " + aProblem.getElementId() + " vs " + files.getProblemId());
            }
        }

        String[] inputFileNames = Utilities.getFileNames(dataFileBaseDirectory, dataExtension);

        String[] answerFileNames = Utilities.getFileNames(dataFileBaseDirectory, answerExtension);

        if (inputFileNames.length == 0) {
            throw new RuntimeException("No input data files with required  '" + dataExtension + "'  extension found in " + dataFileBaseDirectory);
        }

        if (answerFileNames.length == 0) {
            throw new RuntimeException("No Judge's answer files with required  '" + answerExtension + "'  extension found in " + dataFileBaseDirectory);
        }

        if (answerFileNames.length != inputFileNames.length) {
            throw new RuntimeException("Mismatch: expecting the same number of  '" + dataExtension + "'  and  '" + answerExtension + "'  files in " + dataFileBaseDirectory + "\n (found "
                    + inputFileNames.length + "  '" + dataExtension + "'  files vs. " + answerFileNames.length + "  '" + answerExtension + "'  files)");
        }

        SerializedFile[] inputFiles = Utilities.createSerializedFiles(dataFileBaseDirectory, inputFileNames, externalDataFiles);
        SerializedFile[] answertFiles = Utilities.createSerializedFiles(dataFileBaseDirectory, answerFileNames, externalDataFiles);
        files.setJudgesDataFiles(inputFiles);
        files.setJudgesAnswerFiles(answertFiles);

        return files;
    }
}












