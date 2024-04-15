package com.pc.kilojoulesrest.controller;

import com.pc.kilojoulesrest.model.ErrorDTO;
import com.pc.kilojoulesrest.model.LoginRequestDTO;
import com.pc.kilojoulesrest.service.JwtService;
import com.pc.kilojoulesrest.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class RestUserController {

    private final UserService userService;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;


    public RestUserController(UserService userService, JwtService jwtService, AuthenticationManager authenticationManager) {
        this.userService = userService;
        this.jwtService = jwtService;
        this.authenticationManager = authenticationManager;
    }

    @PostMapping("/login")
    public ResponseEntity<?> authenticateAndGetToken(
            @Valid @RequestBody LoginRequestDTO loginRequestDTO,
            BindingResult bindingResult) {

        if (bindingResult.hasErrors()) {

            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(userService.buildErrorResponseForLogin(bindingResult));
        }

        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(loginRequestDTO.getUsername(),
                            loginRequestDTO.getPassword()));
            Map<String, Object> response = new HashMap<>();
            response.put("status", "ok");
            response.put("access_token", jwtService.generateToken(loginRequestDTO.getUsername()));

            return ResponseEntity.ok(response);

        } catch (BadCredentialsException b) {

            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ErrorDTO("Authentication failed." +
                            " Incorrect username and/or password."));
        }
    }

    @GetMapping("/isRunning")
    public String isRunning() {
        return "Service is running";
    }

}
