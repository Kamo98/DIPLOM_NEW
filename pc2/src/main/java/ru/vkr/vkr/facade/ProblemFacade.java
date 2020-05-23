package ru.vkr.vkr.facade;

import ch.qos.logback.core.net.server.Client;
import edu.csus.ecs.pc2.core.InternalController;
import edu.csus.ecs.pc2.core.model.*;
import edu.csus.ecs.pc2.validator.pc2Validator.PC2ValidatorSettings;
import org.antlr.v4.runtime.misc.Array2DHashSet;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.awt.*;
import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Vector;

@Component
public class ProblemFacade {

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

        String dataPath = "H:\\Университет\\8 сем - FINISH!!!\\Диплом\\DIPLOM\\pc2v9-PC2_RELEASE_9_6_x\\pc2\\src\\main\\resources\\static\\tests\\Test121";

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


//        problem.setUsingExternalDataFiles(true);
//        problem.setExternalDataFileLocation("H:\\Университет\\8 сем - FINISH!!!\\Диплом\\DIPLOM\\pc2v9-PC2_RELEASE_9_6_x\\pc2\\src\\main\\resources\\static\\tests\\Test121");
//        problem.addTestCaseFilenames("01.in", "01.ans");
//        problem.addTestCaseFilenames("02.in", "02.ans");
//        problem.addTestCaseFilenames("03.in", "03.ans");


        Problem problems[] = contest.getProblems();
        //Problem problem = problems[problems.length-1].copy("Test120_copy");

        ProblemDataFiles problemDataFiles = new ProblemDataFiles(problem);

        SerializedFile fIn[] = new SerializedFile[]{
                new SerializedFile(dataPath + "\\01.in"),
                new SerializedFile(dataPath + "\\02.in"),
                new SerializedFile(dataPath + "\\03.in"),
        };

        SerializedFile fOut[] = new SerializedFile[]{
                new SerializedFile(dataPath + "\\01.ans"),
                new SerializedFile(dataPath + "\\02.ans"),
                new SerializedFile(dataPath + "\\03.ans"),
        };
        problemDataFiles.setJudgesDataFiles(fIn);
        problemDataFiles.setJudgesAnswerFiles(fOut);
        //problemDataFiles.set
        internalController.addNewProblem(problem, problemDataFiles);


//        contest.addProblem(problem);
//        internalController.setContest(contest);


    }
}












