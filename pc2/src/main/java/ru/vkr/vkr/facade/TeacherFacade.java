package ru.vkr.vkr.facade;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.vkr.vkr.entity.Student;
import ru.vkr.vkr.entity.Teacher;
import ru.vkr.vkr.repository.StudentRepository;

@Component
public class TeacherFacade {
    @Autowired
    private StudentRepository studentRepository;

    public void editStudent(Long studentId, String newFio) {
        Student student = studentRepository.getOne(studentId);
        String []fio = newFio.split(" +");
        student.setSurname(fio[0]);
        student.setName(fio[1]);
        student.setMiddleName(fio[2]);
        studentRepository.save(student);
    }
}
