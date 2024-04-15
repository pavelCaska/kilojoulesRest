package com.pc.kilojoulesrest.repository;

import com.pc.kilojoulesrest.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

//@Repository
public interface UserRepository extends JpaRepository<User, UUID> {
    Optional<User> findByUsername(String username);

//    List<User> findAllByRolesContains(String role);

}
