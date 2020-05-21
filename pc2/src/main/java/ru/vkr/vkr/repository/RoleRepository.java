package ru.vkr.vkr.repository;


import org.springframework.data.jpa.repository.JpaRepository;
import ru.vkr.vkr.entity.Role;

public interface RoleRepository extends JpaRepository<Role, Long> {
}
