package ru.vkr.vkr.domain.run;

import edu.csus.ecs.pc2.api.IProblemDetails;
import javafx.util.Pair;
import ru.vkr.vkr.entity.Student;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class MonitorData {
    private List<Record> records;

    class Record {
        int countOfPass;
        long timeSolve;
        String fio;
        List<IProblemDetails> iProblemDetailsList;

        public Record(int countOfPass, long timeSolve, String fio, List<IProblemDetails> iProblemDetailsList) {
            this.countOfPass = countOfPass;
            this.timeSolve = timeSolve;
            this.fio = fio;
            this.iProblemDetailsList = iProblemDetailsList;
        }

        public int getCountOfPass() {
            return countOfPass;
        }

        public long getTimeSolve() {
            return timeSolve;
        }

        public String getFio() {
            return fio;
        }

        public List<IProblemDetails> getiProblemDetailsList() {
            return iProblemDetailsList;
        }
    }

    class RecordComparator implements Comparator<Record> {
        @Override
        public int compare(Record t1, Record t2) {
            if (Integer.compare(t1.countOfPass, t2.countOfPass) == 0) {
                return Long.compare(t1.timeSolve, t2.timeSolve);
            }
            return -Integer.compare(t1.countOfPass, t2.countOfPass);
        }
    }

    public MonitorData(List<Pair<Integer, Student>> students, List<List<IProblemDetails>> problemDetails, List<Long> timeSolved) {
        this.records = new ArrayList<>();
        for (int i = 0; i < students.size(); ++i) {
            records.add(new Record(students.get(i).getKey(), timeSolved.get(i),
                    students.get(i).getValue().toString(), problemDetails.get(i)));
        }
        records.sort(new RecordComparator());
    }

    public List<Record> getRecords() {
        return records;
    }
}
