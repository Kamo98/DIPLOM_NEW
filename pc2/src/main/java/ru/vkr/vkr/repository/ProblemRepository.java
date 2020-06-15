package ru.vkr.vkr.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.vkr.vkr.entity.Problem;

import java.util.List;

public interface ProblemRepository extends JpaRepository<Problem, Long> {
//    List<Problem> findByTeacherAuthor_id(Long idTeacher);
    List<Problem> findByPubl(boolean publ);
}
