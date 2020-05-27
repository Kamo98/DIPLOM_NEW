package ru.vkr.vkr.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.vkr.vkr.entity.Chapter;

public interface ChapterRepository extends JpaRepository<Chapter, Long> {
}
