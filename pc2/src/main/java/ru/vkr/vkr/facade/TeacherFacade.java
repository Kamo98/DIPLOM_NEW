package ru.vkr.vkr.facade;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import ru.vkr.vkr.entity.Student;
import ru.vkr.vkr.repository.StudentRepository;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.transaction.Transactional;

@Component
public class TeacherFacade {
    @Autowired
    private StudentRepository studentRepository;
    @PersistenceContext
    private EntityManager entityManager;

    public void editStudent(Long studentId, String newFio) {
        Student student = studentRepository.getOne(studentId);
        String[] fio = newFio.split(" +");
        student.setSurname(fio[0]);
        student.setName(fio[1]);
        student.setMiddleName(fio[2]);
        studentRepository.save(student);
    }

    public void accessPerfectSolution(Long chapterId, Long problemId) {
        String query = "select * from t_chapter_problems t  where t.problem_id = ? and t.chapter_id = ?";
        entityManager.createNativeQuery(query).
                setParameter(1, problemId).
                setParameter(2, chapterId);
        System.out.println();
    }
}
