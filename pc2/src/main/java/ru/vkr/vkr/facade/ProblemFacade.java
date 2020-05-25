package ru.vkr.vkr.facade;

import ch.qos.logback.core.net.server.Client;
import edu.csus.ecs.pc2.core.InternalController;
import edu.csus.ecs.pc2.core.Utilities;
import edu.csus.ecs.pc2.core.model.*;
import edu.csus.ecs.pc2.ui.MultipleDataSetPane;
import edu.csus.ecs.pc2.validator.clicsValidator.ClicsValidatorSettings;
import edu.csus.ecs.pc2.validator.customValidator.CustomValidatorSettings;
import edu.csus.ecs.pc2.validator.pc2Validator.PC2ValidatorSettings;
import org.antlr.v4.runtime.misc.Array2DHashSet;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import ru.vkr.vkr.form.CheckerSettingsForm;
import ru.vkr.vkr.form.LoadTestsForm;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.*;

@Component
public class ProblemFacade {

    @Value("${spring.servlet.multipart.location}")
    private String uploadPath;

    @Autowired
    private ApplicationContext applicationContext;


    //todo: надо разделить на несколько методов, чтобы вызывать отдельные там где требуется та или иная папка
    private void makeDirectory (Long problemId) {
        File pathTests = new File(uploadPath + "/tests");
        if (!pathTests.exists())
            pathTests.mkdir();

        String dirTests = uploadPath + "/tests/problem-" + problemId;
        File fileTests = new File(dirTests);
        if (!fileTests.exists())
            fileTests.mkdir();

        File pathChecker = new File(uploadPath + "/checkers");
        if (!pathChecker.exists())
            pathChecker.mkdir();

        String dirChecker = uploadPath + "/checkers/problem-" + problemId;
        File fileChecker = new File(dirChecker);
        if (!fileChecker.exists())
            fileChecker.mkdir();


        File pathStatement = new File(uploadPath + "/statements");
        if (!pathStatement.exists())
            pathStatement.mkdir();

        String dirStatement = uploadPath + "/statements/problem-" + problemId;
        File fileStatement = new File(dirStatement);
        if (!fileStatement.exists())
            fileStatement.mkdir();
    }


    public Long initProblem(ru.vkr.vkr.entity.Problem problemDb) {
        makeDirectory(problemDb.getId());

        InternalController internalController = (InternalController) applicationContext.getBean("getInternalController");

        String baseDirectoryName = uploadPath + "/tests/problem-" + problemDb.getId();

        //Основные параметры задачи
        Problem problem = new Problem(problemDb.getName());
        problem.setShortName("problem-" + problemDb.getId());
        problem.setTimeOutInSeconds(problemDb.getTimeLimit());

        //Поток для чтения
        problem.setReadInputDataFromSTDIN(true);

        //Судьи
        problem.setComputerJudged(true);
        problem.setManualReview(false);
        problem.setShowCompareWindow(true);


        //Добавить в списки автосудий задачу
        //todo: не совмем уверен что в список clientSettingsList будут попадать все автосудьи, которые есть в системе
        ClientSettings clientSettingsList[] = internalController.getContest().getClientSettingsList();
        for (int i = 0; i < clientSettingsList.length; i++) {
           if (clientSettingsList[i].isAutoJudging()) {
               Filter filter = clientSettingsList[i].getAutoJudgeFilter();
               filter.addProblem(problem);
               internalController.updateClientSettings(clientSettingsList[i]);
           }
        }

        ProblemDataFiles problemDataFiles = new ProblemDataFiles(problem);
        internalController.addNewProblem(problem, problemDataFiles);

        return problem.getElementId().getNum();
    }


    //Установка параметров чекера
    public void setParamsOfChecker(ru.vkr.vkr.entity.Problem problemDb, CheckerSettingsForm checkerSettingsForm) {
        InternalController internalController = (InternalController) applicationContext.getBean("getInternalController");

        //Ищем задачу
        Problem problem = findProblemInPC2(internalController, problemDb);

//        problem.setValidatorType(Problem.VALIDATOR_TYPE.PC2VALIDATOR);
//        PC2ValidatorSettings pc2ValidatorSettings = new PC2ValidatorSettings();
//        pc2ValidatorSettings.setWhichPC2Validator(1);
//        problem.setPC2ValidatorSettings(pc2ValidatorSettings);

        if (checkerSettingsForm.isClicsValidator()) {
            problem.setValidatorType(Problem.VALIDATOR_TYPE.CLICSVALIDATOR);

            ClicsValidatorSettings clicsValidatorSettings = new ClicsValidatorSettings();

            if (checkerSettingsForm.isFloatAbsoluteTolerance())
                clicsValidatorSettings.setFloatAbsoluteTolerance(checkerSettingsForm.getAbsoluteTolerance());
            clicsValidatorSettings.setCaseSensitive(checkerSettingsForm.isCaseSensitive());
            clicsValidatorSettings.setSpaceSensitive(checkerSettingsForm.isSpaceSensitive());
            problem.setCLICSValidatorSettings(clicsValidatorSettings);
        } else {
            problem.setValidatorType(Problem.VALIDATOR_TYPE.CUSTOMVALIDATOR);

            CustomValidatorSettings customValidatorSettings = new CustomValidatorSettings();

            //todo: тут надо загружать чекер и компилировать его
            customValidatorSettings.setUseClicsValidatorInterface();
            customValidatorSettings.setValidatorCommandLine("{:validator} {:infile} {:outfile} {:ansfile} {:resfile}");
            //customValidatorSettings.setValidatorProgramName();
              problem.setCustomValidatorSettings(customValidatorSettings);
        }

        problem.setShowValidationToJudges(true);

        internalController.updateProblem(problem);
    }


    //Загрузка тестов от преподавателя на сервер
    public void loadTestFiles(LoadTestsForm loadTestsForm, ru.vkr.vkr.entity.Problem problemDb) throws IOException {
        String extensionIn = loadTestsForm.getExtensionIn();
        String extensionAns = loadTestsForm.getExtensionAns();

        String uloadDirPath = uploadPath + "/tests/problem-" + problemDb.getId();


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

    //Добавление тестов к задаче в PC2
    public void addTestsToProblem(ru.vkr.vkr.entity.Problem problemDb) {
        InternalController internalController = (InternalController) applicationContext.getBean("getInternalController");

        String baseDirectoryName = uploadPath + "/tests/problem-" + problemDb.getId();

        //Ищем задачу
        Problem problem = findProblemInPC2(internalController, problemDb);

        //Тесты
        final boolean externalFiles = true;
        problem.setUsingExternalDataFiles(externalFiles);
        problem.setExternalDataFileLocation(baseDirectoryName);
//        problem.addTestCaseFilenames("01.in", "01.ans");
//        problem.addTestCaseFilenames("02.in", "02.ans");
//        problem.addTestCaseFilenames("03.in", "03.ans");

        ProblemDataFiles problemDataFiles = loadDataFiles(problem, null, baseDirectoryName, ".in", ".ans", externalFiles);
        internalController.updateProblem(problem, problemDataFiles);
    }

    public Collection<Problem> getAllProblems() {
        InternalController internalController = (InternalController) applicationContext.getBean("getInternalController");
        ArrayList<Problem> problems = new ArrayList<>();
        Collections.addAll(problems, internalController.getContest().getProblems());
        return problems;
    }


    public Collection<String> getAllTestsById (Long problemId) {
        Collection<String> filesList = new ArrayList<>();
        File dirFile = new File(uploadPath + "/tests/problem-" + problemId);
        if (dirFile.exists()) {
            String[] filesArr = dirFile.list();
            if (filesArr != null)
                Collections.addAll(filesList, filesArr);
        }
        return filesList;
    }


    //Ищет задачу в PC2 по сущности из БД
    public Problem findProblemInPC2(InternalController internalController, ru.vkr.vkr.entity.Problem problemDb) {
        ElementId elementId = new ElementId(problemDb.getName());
        elementId.setNum(problemDb.getNumElementId());
        return internalController.getContest().getProblem(elementId);
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












