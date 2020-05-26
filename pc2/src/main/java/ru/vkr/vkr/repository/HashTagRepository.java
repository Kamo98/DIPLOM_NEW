package ru.vkr.vkr.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.vkr.vkr.entity.HashTag;


public interface HashTagRepository extends JpaRepository<HashTag, Long> {
}
