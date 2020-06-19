package ru.vkr.vkr.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.vkr.vkr.entity.Problem;
import ru.vkr.vkr.entity.TagProblem;

public interface TagProblemRepository extends JpaRepository<TagProblem, Long> {
}
