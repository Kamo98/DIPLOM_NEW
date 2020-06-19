package ru.vkr.vkr.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.querydsl.QSort;
import org.springframework.stereotype.Service;
import ru.vkr.vkr.domain.FileManager;
import ru.vkr.vkr.entity.*;
import ru.vkr.vkr.facade.AuthenticationFacade;
import ru.vkr.vkr.form.TheoryMaterialForm;
import ru.vkr.vkr.repository.ComplexityRepository;
import ru.vkr.vkr.repository.PerfectSolutionRepository;
import ru.vkr.vkr.repository.ProblemRepository;
import ru.vkr.vkr.repository.TagProblemRepository;

import java.util.*;

@Service
public class ProblemService {

    private static Logger logger = LoggerFactory.getLogger(ProblemService.class);

    private final String nameOfFolder = "statements";

    @Autowired
    private ProblemRepository problemRepository;
    @Autowired
    private AuthenticationFacade authenticationFacade;
    @Autowired
    private TagProblemRepository tagProblemRepository;
    @Autowired
    private ComplexityRepository complexityRepository;
    @Autowired
    private PerfectSolutionRepository perfectSolutionRepository;

    public void loadStatement(Problem problem, TheoryMaterialForm theoryMaterialForm) {
        String sourceStatement = FileManager.loadFileNewRandomDirToServer(theoryMaterialForm.getMultipartFile(), nameOfFolder);
        problem.setNameOfTextProblem(theoryMaterialForm.getName());
        problem.setPathToTextProblem(sourceStatement);
        problemRepository.save(problem);
    }

    public void deleteStatement(Problem problem) {
        problem.setPathToTextProblem(null);
        problem.setNameOfTextProblem(null);
        problemRepository.save(problem);
    }

    public void save(Problem problem) {
        logger.info("save problem " + problem.toString());
        problemRepository.save(problem);
    }


    public Problem getProblemById(Long problemId) {
        return problemRepository.findById(problemId).get();
    }


    //Устанавливает автора создаваемой задачи
    public void setAuthorForNewCourse(Problem problem){
        //Автор задачи - текущий пользователь (преподаватель)
        problem.setTeacherAuthor(authenticationFacade.getCurrentTeacher());
    }

    public List<Problem> getProblemsByCurrentTeacher() {
        return authenticationFacade.getCurrentTeacher().getProblems();
    }

    public Collection<Problem> getProblemsByCurrentStudent() {
        List<Chapter> chapterSet = authenticationFacade.getCurrentStudent().getGroup().getCourseSubscriptions().getChapters();
        List<Problem> problemSet = new ArrayList<>();
        for (Chapter chapter : chapterSet) {
            for (Problem problem : chapter.getChapterProblems())
            if (problemSet.size() < 5) {
                problemSet.add(problem);
            } else {
                return problemSet;
            }
        }
        return problemSet;
    }


    public List<Problem> getAllPublicProblems() {
        return problemRepository.findByPubl(true);
    }

    public List<TagProblem> setHashTagsToProblem(Problem problem, Map<HashTag, Boolean> hashTags) {
        tagProblemRepository.deleteByProblem_id(problem.getId());      //Очистить старые теги
        //todo: все теги задачи очищаются, а новые добавляются, нужно будет придумтаь что-то получше
        List<TagProblem> tagProblems = new ArrayList<>();
        for (Map.Entry<HashTag, Boolean> newTag : hashTags.entrySet()) {
            TagProblem tagProblem = new TagProblem();
            tagProblem.setHashTag(newTag.getKey());
            tagProblem.setProblem(problem);
            tagProblem.setVisible(newTag.getValue());
            tagProblemRepository.save(tagProblem);

            tagProblems.add(tagProblem);
        }
        return  tagProblems;
    }


    public List<Complexity> getAllComplexity() {
        return complexityRepository.findAll();
    }

    public void complexityInit() {
        complexityRepository.deleteAll();
        Complexity complexity = new Complexity();
        complexity.setName("Очень низкая");
        complexityRepository.save(complexity);
        complexity = new Complexity();
        complexity.setName("Низкая");
        complexityRepository.save(complexity);
        complexity = new Complexity();
        complexity.setName("Средняя");
        complexityRepository.save(complexity);
        complexity = new Complexity();
        complexity.setName("Высокая");
        complexityRepository.save(complexity);
        complexity = new Complexity();
        complexity.setName("Очень высокая");
        complexityRepository.save(complexity);
    }


    public void deletePerfectSolution(Long perfectSolId) {
        perfectSolutionRepository.deleteById(perfectSolId);
    }

}
