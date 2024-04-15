package com.pc.kilojoulesrest.repository;

import com.pc.kilojoulesrest.entity.User;
import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

@DataJpaTest
@Import(BCryptPasswordEncoder.class)
class UserRepositoryTests {

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;

    @Test
    @DisplayName("JUnit test for save user operation")
    public void givenUserObject_whenSave_thenReturnSavedUser() {
        User user = User.builder()
                .username("testUser")
                .password(passwordEncoder.encode("testPassword"))
                .roles("ROLE_USER")
                .build();

        User savedUser = userRepository.save(user);

        assertThat(savedUser).isNotNull();
        assertThat(savedUser.getUsername()).isEqualTo("testUser");
        assertThat(passwordEncoder.matches("testPassword", savedUser.getPassword())).isTrue();
        assertThat(savedUser.getRoles()).isEqualTo("ROLE_USER");
    }

    @Test
    @DisplayName("JUnit test for findByUsername")
    public void givenUserObject_whenFindByUsername_thenReturnUser() {
        User user1 = User.builder()
                .username("testUser1")
                .password(passwordEncoder.encode("testPassword"))
                .roles("ROLE_USER")
                .build();
        userRepository.save(user1);

        User user2 = User.builder()
                .username("testUser2")
                .password(passwordEncoder.encode("testPassword"))
                .roles("ROLE_USER")
                .build();
        userRepository.save(user2);

        Optional<User> foundUser = userRepository.findByUsername("testUser1");

        assertThat(foundUser).isPresent();
        assertThat(foundUser.get().getUsername()).isEqualTo("testUser1");
    }
}