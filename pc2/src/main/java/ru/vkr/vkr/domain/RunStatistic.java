//package ru.vkr.vkr.domain;
//
//import edu.csus.ecs.pc2.core.model.Problem;
//import edu.csus.ecs.pc2.core.model.Run;
//
//import java.io.Serializable;
//import java.util.HashMap;
//
//
//public final class RunStatistic implements Serializable {
//    private static final long serialVersionUID = 1L;
//
//    private HashMap<Long, StatisticOfTask> statisticOfTaskHashMap;
//
//    public class StatisticOfTask implements Serializable {
//        // количество успешных сдач
//        private Long countYes;
//        // количество неуспешных сдач
//        private Long countNo;
//        // количество студентов успешно сдавших
//        private Long countStudentYes;
//        // количество студентов неуспешно сдавших
//        private Long countStudentNo;
//        // количество вердиктов по каждому типу
//        private HashMap<String, Long> verdict;
//
//        // студенты с их вердиктами по данной задачи
//        private HashMap<String, String> teams;
//
//        StatisticOfTask() {
//            countNo = 0L;
//            countStudentNo = 0L;
//            countYes = 0L;
//            countStudentYes = 0L;
//            verdict = new HashMap<>();
//            teams = new HashMap<>();
//        }
//
//        // общее количество попыток
//        public Long getCount() {
//            return this.countNo + this.countYes;
//        }
//
//        public Long getCountYes() {
//            return countYes;
//        }
//
//        public Long getCountNo() {
//            return countNo;
//        }
//
//        public Long getCountStudentYes() {
//            return countStudentYes;
//        }
//
//        public Long getCountStudentNo() {
//            return countStudentNo;
//        }
//
//        public HashMap<String, Long> getVerdict() {
//            return verdict;
//        }
//    }
//
//    public HashMap<Long, StatisticOfTask> getStatisticOfTaskHashMap() {
//        return this.statisticOfTaskHashMap;
//    }
//
//    public RunStatistic() {
//        statisticOfTaskHashMap = new HashMap<>();
//    }
//
//    void updateStatisticData(Run run, Problem problem, String response) {
//        StatisticOfTask statisticOfTask;
//        if (statisticOfTaskHashMap.containsKey(problem.getElementId().getNum())) {
//            statisticOfTask = statisticOfTaskHashMap.get(problem.getElementId().getNum());
//        } else {
//            statisticOfTask = new StatisticOfTask();
//        }
//        // обновляем количство успешных и неуспешынх сдач
//        if (response.equals("Yes")) {
//            statisticOfTask.countYes++;
//        } else {
//            statisticOfTask.countNo++;
//        }
//
//        // обновляем количество вердиктов по каждлму типу
//        Long countVerdict = 1L;
//        if (statisticOfTask.verdict.containsKey(response)) {
//            countVerdict = statisticOfTask.verdict.get(response) + 1;
//        }
//        statisticOfTask.verdict.put(response, countVerdict);
//
//        // обновляем количество студентов успешно/неуспешно сдавших
//        String studentLoginPc2 = run.getSubmitter().getName();
//
//        if (statisticOfTask.teams.containsKey(studentLoginPc2) &&
//                !statisticOfTask.teams.get(studentLoginPc2).equals("Yes") && response.equals("Yes")) {
//            statisticOfTask.countStudentYes++;
//            statisticOfTask.countStudentNo--;
//            statisticOfTask.teams.put(studentLoginPc2, "Yes");
//        }
//
//        if (!statisticOfTask.teams.containsKey(studentLoginPc2)) {
//            statisticOfTask.teams.put(studentLoginPc2, response);
//            if (response.equals("Yes")) {
//                statisticOfTask.countStudentYes++;
//            } else {
//                statisticOfTask.countStudentNo++;
//            }
//        }
//
//        statisticOfTaskHashMap.put(problem.getElementId().getNum(), statisticOfTask);
//    }
//}