package ru.vkr.vkr.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.vkr.vkr.entity.Theory;

public interface TheoryRepository extends JpaRepository<Theory, Long> {
}
