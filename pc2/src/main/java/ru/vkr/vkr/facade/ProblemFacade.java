package ru.vkr.vkr.facade;

import edu.csus.ecs.pc2.api.IProblemDetails;
import edu.csus.ecs.pc2.api.IStanding;
import edu.csus.ecs.pc2.api.ITeam;
import edu.csus.ecs.pc2.api.exceptions.NotLoggedInException;
import edu.csus.ecs.pc2.api.implementation.Contest;
import edu.csus.ecs.pc2.core.IInternalController;
import edu.csus.ecs.pc2.core.InternalController;
import edu.csus.ecs.pc2.core.Utilities;
import edu.csus.ecs.pc2.core.model.*;
import edu.csus.ecs.pc2.validator.clicsValidator.ClicsValidatorSettings;
import edu.csus.ecs.pc2.validator.customValidator.CustomValidatorSettings;
import javafx.util.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import ru.vkr.vkr.domain.BridgePc2;
import ru.vkr.vkr.domain.FileManager;
import ru.vkr.vkr.domain.problem.ProblemFactory;
import ru.vkr.vkr.domain.run.MonitorData;
import ru.vkr.vkr.domain.run.RunStatistic;
import ru.vkr.vkr.entity.Student;
import ru.vkr.vkr.form.CheckerSettingsForm;
import ru.vkr.vkr.form.LoadTestsForm;
import ru.vkr.vkr.form.TestSettingsForm;
import ru.vkr.vkr.service.ProblemService;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
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
    private void makeDirectory(Long problemId) {
        String dirTests = uploadPath + "tests\\problem-" + problemId;
        File fileTests = new File(dirTests);
        if (!fileTests.exists())
            fileTests.mkdir();
    }


    public void initProblem(ru.vkr.vkr.entity.Problem problemDb) {
        makeDirectory(problemDb.getId());
        InternalController internalController = BridgePc2.getInternalController();

        //Основные параметры задачи
        Problem problem = new Problem(problemDb.getName());
        problem.setShortName("problem-" + problemDb.getId());
        problem.setTimeOutInSeconds(problemDb.getTimeLimit());
        problem.setStopOnFirstFailedTestCase(false);

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
    }

    public void updateMainParams(String newName, ru.vkr.vkr.entity.Problem problemDb) {
        InternalController internalController = BridgePc2.getInternalController();
        //Ищем задачу
        Problem problem = findProblemInPC2(problemDb);
        if (problem != null) {
            problem.setDisplayName(newName);
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
        //Ищем задачу
        Problem problem = findProblemInPC2(problemDb);
        if (problem != null)
            testSettingsForm.setStopOnFirstFail(problem.isStopOnFirstFailedTestCase());
    }

    //Установка параметров чекера
    public void setParamsOfChecker(ru.vkr.vkr.entity.Problem problemDb, CheckerSettingsForm checkerSettingsForm) {
        InternalController internalController = BridgePc2.getInternalController();

        //Ищем задачу
        Problem problem = findProblemInPC2(problemDb);

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
    public boolean loadTestFiles(LoadTestsForm loadTestsForm, ru.vkr.vkr.entity.Problem problemDb)  {
        String extensionIn = loadTestsForm.getExtensionIn();
        String extensionAns = loadTestsForm.getExtensionAns();
        String uloadDirPath = "tests\\problem-" + problemDb.getId();

        Map<String, MultipartFile> filesIn = new TreeMap<>();
        Map<String, MultipartFile> filesAns = new TreeMap<>();

        // попытка сформировать тесты автоматически
        for (int i = 0; i < loadTestsForm.getDirTests().length; i++) {
            MultipartFile file = loadTestsForm.getDirTests()[i];
            String fileName = file.getOriginalFilename();
            if (isOutputTest(fileName)) {
                filesAns.put(fileName, file);
            } else {
                filesIn.put(fileName, file);
            }
        }

        if (filesIn.size() != filesAns.size()) {
            filesIn.clear();
            filesAns.clear();
            // попытка сформировать тесты по типу, заданному пользователем
            for (int i = 0; i < loadTestsForm.getDirTests().length; i++) {
                MultipartFile file = loadTestsForm.getDirTests()[i];
                String[] fileNameArray = file.getOriginalFilename().split("\\.");
                String fileFormat = "." + fileNameArray[fileNameArray.length - 1];
                if (fileFormat.equals(extensionIn)) {
                    filesIn.put(file.getOriginalFilename(), file);
                } else if (fileFormat.equals(extensionAns)) {
                    filesAns.put(file.getOriginalFilename(), file);
                }
            }
        }

        if (filesIn.isEmpty() || filesAns.isEmpty() || filesIn.size() != filesAns.size()) {
            return false;
        }

        FileManager.loadFileMapToServer(filesIn, uloadDirPath, ".in");
        FileManager.loadFileMapToServer(filesAns, uloadDirPath, ".ans");
        return true;
    }


    private static boolean isOutputTest(String fileName) {
        return (fileName.contains(".ans")
                || fileName.contains(".out")
                || fileName.contains("out.")
                || fileName.contains(".a")
                || fileName.contains("output")
                || isOutputSpecificTest(fileName));
    }

    private static boolean isOutputSpecificTest(String fileName) {
        String[] fileNameArray = fileName.split("\\.");
        for (String word : fileNameArray) {
            for (int i = 0; i < word.length(); ++i) {
                if (Character.isDigit(word.charAt(i)) && i < word.length() - 1
                        && (word.charAt(i + 1) == 'a' || word.charAt(i + 1) == 'A'
                        || word.charAt(i + 1) == 'o' || word.charAt(i + 1) == 'O')) {
                    return true;
                }
            }
        }
        return false;
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

    public List<Pair<String, String>> getAllTestsById(ru.vkr.vkr.entity.Problem problemDb) {
        List<Pair<String, String>> filesList = new ArrayList<>();
        Problem problem = findProblemInPC2(problemDb);
        if (problem == null)
            return filesList;
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
        for (String s : entries) {
            File currentFile = new File(dirFile.getPath(), s);
            currentFile.delete();
        }
    }

    //Ищет задачу в PC2 по сущности из БД
    public Problem findProblemInPC2(ru.vkr.vkr.entity.Problem problemDb) {
        return ((ProblemFactory) applicationContext.getBean("getProblemFactory")).getProblem(problemDb.getId());
    }

    /***********************************
     * Скрипт для обновления задач в базе после экспорта
     */
    public void updateNumInProblems() {
//        problemService.getAllProblems();
//        System.out.println();
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
        List<Long> timeSolved = new ArrayList<>();
        List<Pair<Integer, Student>> standingOfStudent = new ArrayList<>();
        List<List<IProblemDetails>> problemDetailsOfStudent = new ArrayList<>();

        List<Pair<Student, IStanding>> iStandings = getStandingsRecords(students);
        for (Pair<Student, IStanding> studentIStandingPair : iStandings) {
            List<IProblemDetails> iProblemDetailsList = new ArrayList<>();
            int countIsSolved = 0;
            long timeSolve = 0;
            for (ru.vkr.vkr.entity.Problem problem : problems) {
                IProblemDetails problemDetails = getProblemDetailsByProblem(studentIStandingPair.getValue(), problem);
                if (problemDetails != null) {
                    if (problemDetails.isSolved()) {
                        countIsSolved++;
                        timeSolve += problemDetails.getPenaltyPoints();
                    }
                }
                iProblemDetailsList.add(problemDetails);
            }
            timeSolved.add(timeSolve);
            standingOfStudent.add(new Pair<>(countIsSolved, studentIStandingPair.getKey()));
            problemDetailsOfStudent.add(iProblemDetailsList);
        }
        MonitorData monitorData = new MonitorData(standingOfStudent, problemDetailsOfStudent, timeSolved);
        return monitorData;
    }

    private IProblemDetails getProblemDetailsByProblem(IStanding standing,
                                                       ru.vkr.vkr.entity.Problem problem) {
        if (standing == null) {
            return null;
        } else {
            for (IProblemDetails problemDetails : standing.getProblemDetails()) {
                if (problemDetails.getProblem().getShortName().equals("problem-" + problem.getId())) {
                    return problemDetails;
                }
            }
        }
        return null;
    }

    private List<Pair<Student, IStanding>> getStandingsRecords(List<Student> students) {
        List<Pair<Student, IStanding>> iStandings = new ArrayList<>();
        try {
            Contest contest = BridgePc2.getServerConnection().getContest();
            ITeam iTeams[] = contest.getTeams();
            for (Student student : students) {
                boolean kostil = false;
                for (ITeam iTeam : iTeams) {
                    if (iTeam.getLoginName().equals(student.getUser().getLoginPC2())) {
                        kostil = true;
                        iStandings.add(new Pair<>(student, contest.getStanding(iTeam)));
                        break;
                    }
                }
                if (!kostil) iStandings.add(new Pair<>(student, null));
            }
        } catch (NotLoggedInException e) {
            e.printStackTrace();
        }
        return iStandings;
    }

    public void getStatisticForProblems(Collection<ru.vkr.vkr.entity.Problem> problemsDb) {
        HashMap<Long, RunStatistic.StatisticOfTask> statisticOfTask = BridgePc2.getRunStatistic().getStatisticOfTaskHashMap();

        for (ru.vkr.vkr.entity.Problem pr : problemsDb) {
            if (statisticOfTask.containsKey(pr.getId())) {
                RunStatistic.StatisticOfTask stat = statisticOfTask.get(pr.getId());

                Float acceptedToTotal = (stat.getCountYes() * (float) 100.0) / stat.getCount();
                pr.setAcceptedToTotal(Math.round(acceptedToTotal));
                pr.setAcceptedSubmit(stat.getCountYes());
                pr.setTotalSubmit(stat.getCount());

                Long totalStudent = stat.getCountStudentYes() + stat.getCountStudentNo();
                Float studentAccToTotal = (stat.getCountStudentYes() * (float) 100.0) / totalStudent;
                pr.setStudentAcceptToTotal(Math.round(studentAccToTotal));
                pr.setStudentAccSubmit(stat.getCountStudentYes());
                pr.setTotalStudentSubmit(totalStudent);
            }
        }
    }


    //Проверка на наличие хотя бы одного теста
    public boolean check_tests(ru.vkr.vkr.entity.Problem problemDb) {
        Problem problem = findProblemInPC2(problemDb);
        IInternalController internalController = BridgePc2.getInternalController();
        ProblemDataFiles problemDataFiles = internalController.getProblemDataFiles(problem);
        return problemDataFiles.getJudgesDataFiles().length > 0;
    }
}












