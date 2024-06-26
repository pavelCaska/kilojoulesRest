package com.pc.kilojoulesrest.repository;

import com.pc.kilojoulesrest.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByUsername(String username);

    boolean existsUserByUsername(String username);

    Optional<User> findUserById(Long userId);
}
