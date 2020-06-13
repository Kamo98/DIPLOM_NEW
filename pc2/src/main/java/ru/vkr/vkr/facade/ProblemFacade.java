package ru.vkr.vkr.facade;

import edu.csus.ecs.pc2.api.IProblem;
import edu.csus.ecs.pc2.api.exceptions.NotLoggedInException;
import edu.csus.ecs.pc2.core.IInternalController;
import edu.csus.ecs.pc2.core.InternalController;
import edu.csus.ecs.pc2.core.Utilities;
import edu.csus.ecs.pc2.core.exception.IllegalContestState;
import edu.csus.ecs.pc2.core.list.AccountList;
import edu.csus.ecs.pc2.core.model.*;
import edu.csus.ecs.pc2.core.scoring.DefaultStandingsRecordComparator;
import edu.csus.ecs.pc2.core.scoring.NewScoringAlgorithm;
import edu.csus.ecs.pc2.core.scoring.ProblemSummaryInfo;
import edu.csus.ecs.pc2.core.scoring.StandingsRecord;
import edu.csus.ecs.pc2.validator.clicsValidator.ClicsValidatorSettings;
import edu.csus.ecs.pc2.validator.customValidator.CustomValidatorSettings;
import javafx.util.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import ru.vkr.vkr.domain.BridgePc2;
import ru.vkr.vkr.domain.MonitorData;
import ru.vkr.vkr.entity.Student;
import ru.vkr.vkr.form.CheckerSettingsForm;
import ru.vkr.vkr.form.LoadTestsForm;
import ru.vkr.vkr.form.TestSettingsForm;
import ru.vkr.vkr.service.ProblemService;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;

@Component
public class ProblemFacade {

    @Value("${spring.servlet.multipart.location}")
    private String uploadPath;

    @Autowired
    private ApplicationContext applicationContext;
    @Autowired
    private ProblemService problemService;

    //Стандартные имена тестовых файлов
    private final String extensionInStandart = ".in";
    private final String extensionAnsStandart = ".ans";


    //todo: надо разделить на несколько методов, чтобы вызывать отдельные там где требуется та или иная папка
    private void makeDirectory (Long problemId) {
//        File pathTests = new File(uploadPath + "/tests");
//        if (!pathTests.exists())
//            pathTests.mkdir();

        String dirTests = uploadPath + "tests\\problem-" + problemId;
        File fileTests = new File(dirTests);
        if (!fileTests.exists())
            fileTests.mkdir();

//        File pathChecker = new File(uploadPath + "/checkers");
//        if (!pathChecker.exists())
//            pathChecker.mkdir();

//        String dirChecker = uploadPath + "/checkers/problem-" + problemId;
//        File fileChecker = new File(dirChecker);
//        if (!fileChecker.exists())
//            fileChecker.mkdir();


//        File pathStatement = new File(uploadPath + "/statement");
//        if (!pathStatement.exists())
//            pathStatement.mkdir();
//
//        String dirStatement = uploadPath + "/statements/problem-" + problemId;
//        File fileStatement = new File(dirStatement);
//        if (!fileStatement.exists())
//            fileStatement.mkdir();
    }


    public Long initProblem(ru.vkr.vkr.entity.Problem problemDb) {
        makeDirectory(problemDb.getId());

        InternalController internalController = BridgePc2.getInternalController();

        String baseDirectoryName = uploadPath + "tests\\problem-" + problemDb.getId();

        //Основные параметры задачи
        Problem problem = new Problem(problemDb.getName());
        problem.setShortName("problem-" + problemDb.getId());
        problem.setTimeOutInSeconds(problemDb.getTimeLimit());
        problem.setStopOnFirstFailedTestCase(true);

        //Поток для чтения
        problem.setReadInputDataFromSTDIN(true);

        //Судьи
        problem.setComputerJudged(true);
        problem.setManualReview(false);
        problem.setShowCompareWindow(true);

        //Стандартный чекер
        problem.setValidatorType(Problem.VALIDATOR_TYPE.CLICSVALIDATOR);


        //Добавить в списки автосудий задачу
        //todo: не совмем уверен что в список clientSettingsList будут попадать все автосудьи, которые есть в системе
        ClientSettings clientSettingsList[] = BridgePc2.getInternalContest().getClientSettingsList();
        for (int i = 0; i < clientSettingsList.length; i++) {
           if (clientSettingsList[i].isAutoJudging()) {
               Filter filter = clientSettingsList[i].getAutoJudgeFilter();
               filter.addProblem(problem);
               internalController.updateClientSettings(clientSettingsList[i]);
           }
        }

        ProblemDataFiles problemDataFiles = new ProblemDataFiles(problem);
        internalController.addNewProblem(problem, problemDataFiles);


        try{
            ElementId elementId = problem.getElementId();
            Class elementIdClass = elementId.getClass();
            Field elementIdField = elementIdClass.getDeclaredField("num");
            elementIdField.setAccessible(true);
            return elementIdField.getLong(elementId);
        } catch (IllegalAccessException | NoSuchFieldException e) {
            e.printStackTrace();
        }


        return 1L;
    }

    public void updateMainParams(String newName, ru.vkr.vkr.entity.Problem problemDb) {
        InternalController internalController = BridgePc2.getInternalController();

        //Ищем задачу
        Problem problem = findProblemInPC2(problemDb);
        if (problem != null) {
            //todo: после изменения имени задача перстаёт искаться
            //problem.setDisplayName(newName);
            problem.setTimeOutInSeconds(problemDb.getTimeLimit());

            internalController.updateProblem(problem);
        }
    }


    public void setParamOfTests(ru.vkr.vkr.entity.Problem problemDb, TestSettingsForm testSettingsForm) {
        InternalController internalController = BridgePc2.getInternalController();
        //Ищем задачу
        Problem problem = findProblemInPC2(problemDb);

        problem.setStopOnFirstFailedTestCase(testSettingsForm.isStopOnFirstFail());
        internalController.updateProblem(problem);
    }

    public void setTestsParamsToForm(TestSettingsForm testSettingsForm, ru.vkr.vkr.entity.Problem problemDb) {
        InternalController internalController = BridgePc2.getInternalController();
        //Ищем задачу
        Problem problem = findProblemInPC2(problemDb);
        testSettingsForm.setStopOnFirstFail(problem.isStopOnFirstFailedTestCase());
    }

    //Установка параметров чекера
    public void setParamsOfChecker(ru.vkr.vkr.entity.Problem problemDb, CheckerSettingsForm checkerSettingsForm) {
        InternalController internalController = BridgePc2.getInternalController();

        //Ищем задачу
        Problem problem = findProblemInPC2(problemDb);

//        problem.setValidatorType(Problem.VALIDATOR_TYPE.PC2VALIDATOR);
//        PC2ValidatorSettings pc2ValidatorSettings = new PC2ValidatorSettings();
//        pc2ValidatorSettings.setWhichPC2Validator(1);
//        problem.setPC2ValidatorSettings(pc2ValidatorSettings);

        if (checkerSettingsForm.isClicsValidator()) {
            problem.setValidatorType(Problem.VALIDATOR_TYPE.CLICSVALIDATOR);

            ClicsValidatorSettings clicsValidatorSettings = new ClicsValidatorSettings();

            if (checkerSettingsForm.isFloatAbsoluteTolerance())
                clicsValidatorSettings.setFloatAbsoluteTolerance(checkerSettingsForm.getAbsoluteTolerance());

            if (checkerSettingsForm.isFloatRelativeTolerance())
                clicsValidatorSettings.setFloatRelativeTolerance(checkerSettingsForm.getRelativeTolerance());

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


    public void setCheckerParamsToForm(CheckerSettingsForm checkerSettingsForm, ru.vkr.vkr.entity.Problem problemDb) {
        InternalController internalController = BridgePc2.getInternalController();
        //Ищем задачу
        Problem problem = findProblemInPC2(problemDb);

        if (problem != null) {
            if (problem.getValidatorType() == Problem.VALIDATOR_TYPE.CLICSVALIDATOR) {
                checkerSettingsForm.setClicsValidator(true);

                ClicsValidatorSettings clicsValidatorSettings = problem.getClicsValidatorSettings();

                checkerSettingsForm.setFloatAbsoluteTolerance(clicsValidatorSettings.isFloatAbsoluteToleranceSpecified());
                checkerSettingsForm.setAbsoluteTolerance(clicsValidatorSettings.getFloatAbsoluteTolerance());
                checkerSettingsForm.setFloatRelativeTolerance(clicsValidatorSettings.isFloatRelativeToleranceSpecified());
                checkerSettingsForm.setRelativeTolerance(clicsValidatorSettings.getFloatRelativeTolerance());
                checkerSettingsForm.setCaseSensitive(clicsValidatorSettings.isCaseSensitive());
                checkerSettingsForm.setSpaceSensitive(clicsValidatorSettings.isSpaceSensitive());
            }
        } else {
            //todo: задачу из базы не нашли в pc2
        }
    }


    //Загрузка тестов от преподавателя на сервер
    public void loadTestFiles(LoadTestsForm loadTestsForm, ru.vkr.vkr.entity.Problem problemDb) throws IOException {
        String extensionIn = loadTestsForm.getExtensionIn();
        String extensionAns = loadTestsForm.getExtensionAns();

        String uloadDirPath = uploadPath + "tests\\problem-" + problemDb.getId();


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
        InternalController internalController = BridgePc2.getInternalController();
        String baseDirectoryName = uploadPath + "tests\\problem-" + problemDb.getId();
        //Ищем задачу
        Problem problem = findProblemInPC2(problemDb);

        //Тесты
        final boolean externalFiles = true;
        problem.setUsingExternalDataFiles(externalFiles);
        problem.setExternalDataFileLocation(baseDirectoryName);

        ProblemDataFiles problemDataFiles = loadDataFiles(problem, null, baseDirectoryName, extensionInStandart, extensionAnsStandart, externalFiles);
        internalController.updateProblem(problem, problemDataFiles);
    }




    public Collection<Problem> getAllProblems() {
        InternalController internalController = BridgePc2.getInternalController();
        ArrayList<Problem> problems = new ArrayList<>();
        Collections.addAll(problems, BridgePc2.getInternalContest().getProblems());
        return problems;
    }


    public List<Pair<String, String>> getAllTestsById (ru.vkr.vkr.entity.Problem problemDb) {
//        Set<String> filesList = new HashSet<>();
//        File dirFile = new File(uploadPath + "tests\\problem-" + problemId);
//        if (dirFile.exists()) {
//            String[] filesArr = dirFile.list();
//            if (filesArr != null) {
//                for (String fileName : filesArr) {
//                    if (fileName.endsWith(extensionInStandart))     //Файл входной
//                        filesList.add(fileName.substring(0, fileName.length() - extensionInStandart.length()));
//                }
//            }
//        }
        List<Pair<String, String>> filesList = new ArrayList<>();
        Problem problem = findProblemInPC2(problemDb);
        IInternalController internalController = BridgePc2.getInternalController();
        ProblemDataFiles problemDataFiles = internalController.getProblemDataFiles(problem);
        int countTests = problemDataFiles.getJudgesDataFiles().length;
        for (int i = 0; i < countTests; i++)
            filesList.add(new Pair<>(problemDataFiles.getJudgesDataFiles()[i].getName(),
                    problemDataFiles.getJudgesAnswerFiles()[i].getName()));

        return filesList;
    }


    public void deleteTestFile(ru.vkr.vkr.entity.Problem problemDb, Integer testNum) {
        IInternalController internalController = BridgePc2.getInternalController();
        Problem problem = findProblemInPC2(problemDb);
        ProblemDataFiles problemDataFiles = internalController.getProblemDataFiles(problem);
        //problemDataFiles.removeAll();
        String fileIn = problemDataFiles.getJudgesDataFiles()[testNum].getName();
        String fileAns = problemDataFiles.getJudgesAnswerFiles()[testNum].getName();
        problemDataFiles.removeDataSet(testNum);

        problem.removeAllTestCaseFilenames();
        String[] inputFileNames = new String[problemDataFiles.getJudgesDataFiles().length];
        String[] answerFileNames = new String[problemDataFiles.getJudgesDataFiles().length];
        for (int i = 0; i < problemDataFiles.getJudgesDataFiles().length; i++)
            inputFileNames[i] = problemDataFiles.getJudgesDataFiles()[i].getName();
        for (int i = 0; i < problemDataFiles.getJudgesDataFiles().length; i++)
            answerFileNames[i] = problemDataFiles.getJudgesDataFiles()[i].getName();

        addTestsToProblem(problem, inputFileNames, answerFileNames);

        internalController.updateProblem(problem, problemDataFiles);

        String path = uploadPath + "tests\\problem-" + problemDb.getId();
        File dirFile = new File(path);
        if (dirFile.exists()) {
            File testIn = new File(path + "\\" + fileIn);
            if (testIn.exists())
                testIn.delete();
            File testAns = new File(path + "\\" + fileAns);
            if (testAns.exists())
                testAns.delete();
        }
    }

    public void deleteAllTestFiles(ru.vkr.vkr.entity.Problem problemDb) {
        IInternalController internalController = BridgePc2.getInternalController();
        Problem problem = findProblemInPC2(problemDb);
        ProblemDataFiles problemDataFiles = internalController.getProblemDataFiles(problem);
        problemDataFiles.removeAll();
        problem.removeAllTestCaseFilenames();
        internalController.updateProblem(problem, problemDataFiles);

        String path = uploadPath + "tests\\problem-" + problemDb.getId();
        File dirFile = new File(path);
        String[] entries = dirFile.list();
        for(String s : entries){
            File currentFile = new File(dirFile.getPath(), s);
            currentFile.delete();
        }
    }

    //Ищет задачу в PC2 по сущности из БД
    public Problem findProblemInPC2(ru.vkr.vkr.entity.Problem problemDb) {
        ElementId elementId = new ElementId("problem-" + problemDb.getId());
        try {
            Class elementIdClass = elementId.getClass();
            Field elementIdField = elementIdClass.getDeclaredField("num");
            elementIdField.setAccessible(true);
            elementIdField.set(elementId, problemDb.getNumElementId());
        } catch (IllegalAccessException | NoSuchFieldException e) {
            e.printStackTrace();
        }
        Problem problem = BridgePc2.getInternalContest().getProblem(elementId);
        if (problem == null) {
            elementId = new ElementId(problemDb.getName());
            try {
                Class elementIdClass = elementId.getClass();
                Field elementIdField = elementIdClass.getDeclaredField("num");
                elementIdField.setAccessible(true);
                elementIdField.set(elementId, problemDb.getNumElementId());
            } catch (IllegalAccessException | NoSuchFieldException e) {
                e.printStackTrace();
            }
            problem = BridgePc2.getInternalContest().getProblem(elementId);
        }
        return problem;
    }


    /***********************************
     * Скрипт для обновления задач в базе после экспорта
     */
    public void updateNumInProblems () {
        Problem problems[] = BridgePc2.getInternalContest().getProblems();
        for (Problem iproblem : problems) {
            String shortName = iproblem.getShortName();
            String arr[] = shortName.split("-");
            if (arr.length == 2){
                try {
                    ru.vkr.vkr.entity.Problem problem = problemService.getProblemById(Long.parseLong(arr[1]));
                    ElementId elementId = iproblem.getElementId();
                    Class elementIdClass = elementId.getClass();
                    Field elementIdField = elementIdClass.getDeclaredField("num");
                    elementIdField.setAccessible(true);
                    Long num = (Long) elementIdField.get(elementId);
                    problem.setNumElementId(num);
                    problemService.save(problem);
                } catch (Exception e) {
                }
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

        addTestsToProblem(aProblem, inputFileNames, answerFileNames);
        aProblem.setDataFileName(inputFileNames[0]);
        aProblem.setAnswerFileName(answerFileNames[0]);

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


    private void addTestsToProblem(Problem problem, String[] inputFileNames, String[] answerFileNames) {
        for (int i = 0; i < inputFileNames.length; i++)
            if (i < answerFileNames.length)
                problem.addTestCaseFilenames(inputFileNames[i], answerFileNames[i]);
    }



    public MonitorData getMonitor(List<Student> students, List<ru.vkr.vkr.entity.Problem> problems) {
        InternalController internalController = BridgePc2.getInternalController();
        MonitorData monitorData = new MonitorData();
        try {
            //Формируем сет логинов в PC2
            HashSet<String> loginPC2Users = new HashSet<>();
            for (Student st : students)
                loginPC2Users.add(st.getUser().getLoginPC2());
            HashMap<String, Student> loginPC2Student = new HashMap<>();
            for (Student st : students)
                loginPC2Student.put(st.getUser().getLoginPC2(), st);


            ArrayList<Problem> problemsPC2List = new ArrayList<>();
            for(ru.vkr.vkr.entity.Problem pr : problems) {
                Problem problem = findProblemInPC2(pr);
                if (problem != null)
                    problemsPC2List.add(problem);
            }

            Problem[] problemsPC2 = new Problem[problemsPC2List.size()];
            int i = 0;
            for(Problem pr : problemsPC2List)
                problemsPC2[i++] = pr;

            //Получаем монитор от PC2
            StandingsRecord[] standingsRecords = getStandingsRecords(BridgePc2.getInternalContest(),
                    new Properties(), loginPC2Users, problemsPC2);
            //Формируем данные для вывода
            for (StandingsRecord rec : standingsRecords) {
                //Заполняем строку монитора
                ClientId clientId = rec.getClientId();
                ArrayList<ProblemSummaryInfo> problemSummaryInfos = new ArrayList<>();
                Integer[] keysP = rec.getSummaryRow().getSortedKeys();
                for (int k : keysP) {
                    ProblemSummaryInfo problemSummaryInfo = rec.getSummaryRow().get(k);
                    problemSummaryInfos.add(problemSummaryInfo);
                }

                monitorData.addRecord(loginPC2Student.get(clientId.getName()),
                        rec, problemSummaryInfos);
            }
        } catch (IllegalContestState illegalContestState) {
            illegalContestState.printStackTrace();
        }
        return monitorData;
    }


    private StandingsRecord[] getStandingsRecords(
            IInternalContest contest,
            Properties properties,
            HashSet<String> loginPC2Users,
            Problem[] problemsPC2) throws IllegalContestState
    {
        NewScoringAlgorithm newScoringAlgorithm = new NewScoringAlgorithm();
        if (contest == null) {
            throw new IllegalArgumentException("contest is null");
        } else {
            newScoringAlgorithm.setContest(contest);
            Vector<Account> accountVector_ = newScoringAlgorithm.getContest().getAccounts(ClientType.Type.TEAM);
            Vector<Account> accountVector = new Vector<>();
            for(Account acc : accountVector_)
                if (loginPC2Users.contains(acc.getDisplayName()))
                    accountVector.add(acc);
            Account[] accounts = (Account[])accountVector.toArray(new Account[accountVector.size()]);
            AccountList accountList = new AccountList();
            Account[] var10 = accounts;
            int var9 = accounts.length;

            for(int var8 = 0; var8 < var9; ++var8) {
                Account account = var10[var8];
                accountList.add(account);
            }

            DefaultStandingsRecordComparator comparator = new DefaultStandingsRecordComparator();
            comparator.setCachedAccountList(accountList);
            Run[] runs = newScoringAlgorithm.getContest().getRuns();
//            newScoringAlgorithm.respectEOC = newScoringAlgorithm.isAllowed(newScoringAlgorithm.getContest(), newScoringAlgorithm.getContest().getClientId(), edu.csus.ecs.pc2.core.security.Permission.Type.RESPECT_EOC_SUPPRESSION);
//            if (newScoringAlgorithm.respectEOC) {
//                runs = newScoringAlgorithm.filterRunsbyEOC(newScoringAlgorithm.getContest(), runs);
//            }
//
//            if (honorScoreboardFreeze) {
//                runs = newScoringAlgorithm.filterRunsByScoreboardFreeze(newScoringAlgorithm.getContest(), runs);
//            }

            Class newScoringAlgorithmClass = newScoringAlgorithm.getClass();
            try {
                Method computeStandingMethod = newScoringAlgorithmClass.getDeclaredMethod(
                        "computeStandingStandingsRecords",
                        Run[].class, Account[].class, Properties.class, Problem[].class
                );
                computeStandingMethod.setAccessible(true);
                StandingsRecord[] standings = ( StandingsRecord[])computeStandingMethod.invoke(
                        newScoringAlgorithm,
                        runs, accounts, properties, problemsPC2);

                Arrays.sort(standings, comparator);
    //            if (newScoringAlgorithm.blockRanking) {
    //                newScoringAlgorithm.assignRanksBlock(standings);
    //            } else {
    //                newScoringAlgorithm.assignRanks(standings);
    //            }

                Method assignGroupRanksMethod = newScoringAlgorithmClass.getDeclaredMethod(
                        "assignGroupRanks",
                        IInternalContest.class, StandingsRecord[].class);
                assignGroupRanksMethod.setAccessible(true);
                assignGroupRanksMethod.invoke(
                        newScoringAlgorithm,
                        newScoringAlgorithm.getContest(), standings);
                return standings;

            } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
                e.printStackTrace();
                return null;
            }
        }
    }


    public void getStatisticForProblems(Collection<ru.vkr.vkr.entity.Problem> problemsDb) {
    }

}












