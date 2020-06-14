package ru.vkr.vkr.domain.problem;

import edu.csus.ecs.pc2.api.IProblem;
import edu.csus.ecs.pc2.api.exceptions.NotLoggedInException;
import edu.csus.ecs.pc2.core.model.Problem;
import org.springframework.stereotype.Component;
import ru.vkr.vkr.domain.BridgePc2;

import java.util.HashMap;
import java.util.Map;

@Component
public class ProblemFactory {
    private final Map<Long, IProblem> problemMap;
    private final Map<Long, Problem> iProblemMap;

    public ProblemFactory() {
        problemMap = new HashMap<>();
        iProblemMap = new HashMap<>();
    }

    public Problem getProblem(Long problemId) {
        if (problemMap.containsKey(problemId)) {
            return iProblemMap.get(problemId);
        } else {
            String name = "problem-" + problemId;
            Problem problems[] = BridgePc2.getInternalContest().getProblems();
            for (Problem problem : problems) {
                iProblemMap.put(problemId, problem);
                if (problem.getShortName().equals(name)) {
                    return problem;
                }
            }
        }
        return null;
    }

    public IProblem getIProblem(Long problemId) {
        if (problemMap.containsKey(problemId)) {
            return problemMap.get(problemId);
        } else {
            try {
                String name = "problem-" + problemId;
                IProblem iProblems[] = BridgePc2.getServerConnection().getContest().getProblems();
                for (IProblem iProblem : iProblems) {
                    problemMap.put(problemId, iProblem);
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