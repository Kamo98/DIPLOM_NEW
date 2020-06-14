package ru.vkr.vkr.domain.run;

import edu.csus.ecs.pc2.core.scoring.ProblemSummaryInfo;
import edu.csus.ecs.pc2.core.scoring.StandingsRecord;
import org.springframework.stereotype.Component;
import ru.vkr.vkr.entity.Student;

import java.util.ArrayList;

@Component
public class MonitorData {
    private ArrayList<Student> students = new ArrayList<>();
    private ArrayList<StandingsRecord> records = new ArrayList<>();
    private ArrayList<ArrayList<ProblemSummaryInfo>> problemInfos = new ArrayList<>();

    public void addRecord (Student student, StandingsRecord standingsRecord, ArrayList<ProblemSummaryInfo> problemSummaryInfo) {
        students.add(student);
        records.add(standingsRecord);
        problemInfos.add(problemSummaryInfo);
    }

    public ArrayList<Student> getStudents() {
        return students;
    }

    public Student getStudent(Integer index) {
        return students.get(index);
    }

    public ArrayList<StandingsRecord> getRecords () {
        return records;
    }

    public ArrayList<ProblemSummaryInfo> getProblemInfos (Integer index) {
        return problemInfos.get(index);
    }
}
