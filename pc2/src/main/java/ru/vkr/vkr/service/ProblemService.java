package ru.vkr.vkr.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.vkr.vkr.entity.HashTag;
import ru.vkr.vkr.entity.Problem;
import ru.vkr.vkr.entity.TagProblem;
import ru.vkr.vkr.facade.AuthenticationFacade;
import ru.vkr.vkr.repository.ProblemRepository;
import ru.vkr.vkr.repository.TagProblemRepository;

import java.util.*;

@Service
public class ProblemService {

    private static Logger logger = LoggerFactory.getLogger(ProblemService.class);

    @Autowired
    private ProblemRepository problemRepository;
    @Autowired
    private AuthenticationFacade authenticationFacade;
    @Autowired
    TagProblemRepository tagProblemRepository;


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

    public Collection<Problem> getProblemsByCurrentTeacher() {
        return problemRepository.findByTeacherAuthor_id(authenticationFacade.getCurrentTeacher().getId());
    }


    public List<Problem> getAllProblems() {
        return problemRepository.findAll();
    }

    public Set<TagProblem> setHashTagsToProblem(Problem problem, Map<HashTag, Boolean> hashTags) {
        tagProblemRepository.deleteByProblem_id(problem.getId());      //Очистить старые теги
        //todo: все теги задачи очищаются, а новые добавляются, нужно будет придумтаь что-то получше
        Set<TagProblem> tagProblems = new HashSet<>();
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
}
