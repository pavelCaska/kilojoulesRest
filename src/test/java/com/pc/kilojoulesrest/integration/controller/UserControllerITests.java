package com.pc.kilojoulesrest.integration.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pc.kilojoulesrest.entity.User;
import com.pc.kilojoulesrest.model.LoginRequestDTO;
import com.pc.kilojoulesrest.service.JwtService;
import com.pc.kilojoulesrest.service.UserService;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

@AutoConfigureMockMvc
@SpringBootTest
@ActiveProfiles("test")
public class UserControllerITests {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private UserService userService;
    @Autowired
    private JwtService jwtService;

    @Autowired
    private ObjectMapper om;

    @BeforeEach
    public void setUp() {
        User user = User.builder()
                .username("testUser")
                .password(userService.encodePassword("user1pwd"))
                .roles("ROLE_USER")
                .build();
        userService.saveUser(user);
    }

    @Test
    @Transactional
    @DisplayName("Integration test for user login with valid input")
    public void givenValidUserData_whenLogin_thenReturnsJwtToken() throws Exception {

        LoginRequestDTO loginRequestDTO = new LoginRequestDTO("testUser", "user1pwd");

        ResultActions response = mockMvc.perform(post("/api/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(loginRequestDTO)));

        response.andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.status").value("ok"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.access_token").isNotEmpty());
    }

    @Test
    @Transactional
    @DisplayName("Integration test for user login with invalid username and password")
    public void givenInvalidUserData_whenLogin_thenThrows() throws Exception {

        LoginRequestDTO loginRequestDTO = new LoginRequestDTO("", "user");

        ResultActions response = mockMvc.perform(post("/api/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(loginRequestDTO)));

        response.andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andExpect(MockMvcResultMatchers.jsonPath("$.password").exists())
                .andExpect(MockMvcResultMatchers.jsonPath("$.password").value("Password must consists of a minimum of 6 letters and/or digits."))
                .andExpect(MockMvcResultMatchers.jsonPath("$.username").exists())
                .andExpect(MockMvcResultMatchers.jsonPath("$.username").value("Username must be at least 6 characters long."));
    }

    @Test
    @Transactional
    @DisplayName("Integration test for non-existing user login")
    public void givenNonExistingUser_whenLogin_thenThrows() throws Exception {

        LoginRequestDTO loginRequestDTO = new LoginRequestDTO("testUser", "testUser1");

        ResultActions response = mockMvc.perform(post("/api/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(loginRequestDTO)));

        response.andExpect(MockMvcResultMatchers.status().isUnauthorized())
                .andExpect(MockMvcResultMatchers.jsonPath("$.error").exists())
                .andExpect(MockMvcResultMatchers.jsonPath("$.error").value("Authentication failed." +
                        " Incorrect username and/or password."));
    }

    @Test
    @Transactional
    @DisplayName("Integration test negative case for helper method isRunning")
    public void givenAuthenticatedUser_whenIsRunning_thenStringInBody() throws Exception {

        String token = jwtService.generateToken("testUser");
        ResultActions response = mockMvc.perform(get("/api/isRunning")
                        .header("Authorization", "Bearer " + token));

        response.andExpect(MockMvcResultMatchers.status().isForbidden());
    }

    @Test
    @Transactional
    @DisplayName("Integration test positive case for helper method isRunning")
    public void givenAdmin_whenIsRunning_thenForbidden() throws Exception {

        User admin = User.builder()
                .username("admin")
                .password(userService.encodePassword("admin1pwd"))
                .roles("ROLE_ADMIN")
                .build();
        userService.saveUser(admin);

        String token = jwtService.generateToken("admin");
        ResultActions response = mockMvc.perform(get("/api/isRunning")
                        .header("Authorization", "Bearer " + token));

        response.andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().string("Service is running."));
    }
}
