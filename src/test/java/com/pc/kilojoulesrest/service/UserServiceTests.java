package com.pc.kilojoulesrest.service;

import com.pc.kilojoulesrest.entity.User;
import com.pc.kilojoulesrest.exception.RecordNotFoundException;
import com.pc.kilojoulesrest.repository.UserRepository;
import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

@ExtendWith(MockitoExtension.class)
class UserServiceTests {

    @Mock
    private UserRepository userRepository;
    @InjectMocks
    private UserServiceImpl userService;
    @Mock
    private PasswordEncoder passwordEncoder;
    private User user;

    @BeforeEach
    void setup(){
        user = User.builder()
                .id(1L)
                .username("user1")
                .password("user1pwd")
                .roles("ROLE_USER")
                .build();
    }

    @Test
    @DisplayName("JUnit test for save operation")
    void givenUserObject_whenSaveUser_thenReturnSavedUser() {
        given(userRepository.save(user)).willReturn(user);

        User savedUser = userService.saveUser(user);

        assertThat(savedUser).isNotNull();
        assertThat(savedUser).isEqualTo(user);
    }

    @Test
    @DisplayName("JUnit test for password encoder")
    void givenStringPassword_whenEncodePassword_thenReturnEncodedPassword() {
        String inputPassword = "user1pwd";
        String encodedPassword = "$2a$10$jeEsBZFd60ZbwJEHYc5juenaA3HTjzKnAVM6lce/xY2U6DT00K20a";

        given(passwordEncoder.encode(inputPassword)).willReturn(encodedPassword);
        String result = userService.encodePassword(inputPassword);

        verify(passwordEncoder, times(1)).encode(inputPassword);

        org.junit.jupiter.api.Assertions.assertEquals(encodedPassword, result);
    }

    @Test
    @DisplayName("JUnit test for fetch user by username method")
    void givenUsername_whenFetchByUsername_thenReturnUserObject() {
        String username = user.getUsername();
        given(userRepository.findByUsername(username)).willReturn(Optional.of(user));

        User retrievedUser = userService.fetchUserByUsername(username);

        assertThat(retrievedUser).isNotNull();
        assertThat(retrievedUser.getUsername()).isEqualTo(username);
    }

    @Test
    @DisplayName("JUnit test for fetch user by username method throws custom exception")
    void givenUsername_whenFetchByUsername_thenThrowCustomException() {
        String username = "user2";
        given(userRepository.findByUsername(username)).willReturn(Optional.empty());

        assertThrows(RecordNotFoundException.class, () -> userService.fetchUserByUsername(username));

        verify(userRepository, times(1)).findByUsername(username);
    }
}