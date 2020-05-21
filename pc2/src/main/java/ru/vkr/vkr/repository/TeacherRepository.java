package ru.vkr.vkr.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.vkr.vkr.entity.Teacher;

public interface TeacherRepository extends JpaRepository<Teacher, Long> {
}
