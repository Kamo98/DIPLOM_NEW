package ru.vkr.vkr.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.vkr.vkr.entity.Problem;
import ru.vkr.vkr.repository.ProblemRepository;

@Service
public class ProblemService {

    private static Logger logger = LoggerFactory.getLogger(UserService.class);

    @Autowired
    private ProblemRepository problemRepository;

    public void save(Problem problem) {
        logger.info("save problem " + problem.toString());
        problemRepository.save(problem);
    }


    public Problem getProblemById(Long problemId) {
        return problemRepository.findById(problemId).get();
    }
}
