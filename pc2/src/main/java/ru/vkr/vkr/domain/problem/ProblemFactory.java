package ru.vkr.vkr.domain.problem;

import edu.csus.ecs.pc2.api.IProblem;
import edu.csus.ecs.pc2.api.exceptions.NotLoggedInException;
import edu.csus.ecs.pc2.core.model.Problem;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.vkr.vkr.service.BridgePc2Service;

import java.util.HashMap;
import java.util.Map;

@Component
public class ProblemFactory {
    @Autowired
    private BridgePc2Service bridgePc2Service;

    private final Map<Long, Problem> problemMap;
    private final Map<Long, IProblem> iProblemMap;

    public ProblemFactory() {
        problemMap = new HashMap<>();
        iProblemMap = new HashMap<>();
    }

    public Problem getProblem(Long problemId) {
        if (problemMap.containsKey(problemId)) {
            return problemMap.get(problemId);
        } else {
            String name = "problem-" + problemId;
            Problem[] problems = bridgePc2Service.getInternalContest().getProblems();
            for (Problem problem : problems) {
                //problemMap.put(Long.parseLong(problem.getShortName().split("-")[1]), problem);
                if (problem.getShortName().equals(name)) {
                    return problem;
                }
            }
        }
        return null;
    }

    public IProblem getIProblem(Long problemId) {
        if (iProblemMap.containsKey(problemId)) {
            return iProblemMap.get(problemId);
        } else {
            try {
                String name = "problem-" + problemId;
                IProblem[] iProblems = bridgePc2Service.getServerConnection().getContest().getProblems();
                for (IProblem iProblem : iProblems) {
                    // iProblemMap.put(Long.parseLong(iProblem.getShortName().split("-")[1]), iProblem);
                    if (iProblem.getShortName().equals(name)) {
                        return iProblem;
                    }
                }
            } catch (NotLoggedInException e) {
                e.printStackTrace();
            }
        }
        return null;
    }
}
