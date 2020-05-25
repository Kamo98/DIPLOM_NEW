package ru.vkr.vkr.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.vkr.vkr.entity.Problem;

public interface ProblemRepository extends JpaRepository<Problem, Long> {
}
