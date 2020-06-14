package ru.vkr.vkr.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.vkr.vkr.entity.Complexity;

public interface ComplexityRepository extends JpaRepository<Complexity, Long> {
}
