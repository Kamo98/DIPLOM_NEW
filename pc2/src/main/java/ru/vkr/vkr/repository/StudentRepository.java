package ru.vkr.vkr.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.vkr.vkr.entity.Student;

public interface StudentRepository extends JpaRepository<Student, Long> {
}
