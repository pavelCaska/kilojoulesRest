package com.pc.kilojoulesrest.controller;

import com.pc.kilojoulesrest.config.ExtendedUserDetails;
import com.pc.kilojoulesrest.entity.User;
import com.pc.kilojoulesrest.entity.UserProfile;
import com.pc.kilojoulesrest.model.ErrorDTO;
import com.pc.kilojoulesrest.model.LoginRequestDTO;
import com.pc.kilojoulesrest.model.UserProfileDto;
import com.pc.kilojoulesrest.repository.UserProfileRepository;
import com.pc.kilojoulesrest.service.JwtService;
import com.pc.kilojoulesrest.service.UserProfileService;
import com.pc.kilojoulesrest.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.function.ServerRequest;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class UserController {

    private final UserService userService;
    private final UserProfileService userProfileService;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;


    public UserController(UserService userService, UserProfileService userProfileService, JwtService jwtService, AuthenticationManager authenticationManager) {
        this.userService = userService;
        this.userProfileService = userProfileService;
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
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(loginRequestDTO.getUsername(),
                            loginRequestDTO.getPassword()));
            Map<String, Object> response = new HashMap<>();
            response.put("status", "ok");
            response.put("access_token", jwtService.generateToken(loginRequestDTO.getUsername()));

            return ResponseEntity.ok(response);
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(
            @Valid @RequestBody LoginRequestDTO loginRequestDTO,
            BindingResult bindingResult) {

        if (bindingResult.hasErrors()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(userService.buildErrorResponseForLogin(bindingResult));
        }
        if (userService.existsUserByUsername(loginRequestDTO.getUsername())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ErrorDTO("Username already exists."));
        }

        User registeredUser = userService.registerNewUser(loginRequestDTO.getUsername(), loginRequestDTO.getPassword());
        Map<String, String> response = new HashMap<>();
        response.put("message", "User " + registeredUser.getUsername() + " registered successfully.");

        return ResponseEntity.ok(response);
    }

    @GetMapping("/user/profile")
    public ResponseEntity<?> fetchUserProfile(@AuthenticationPrincipal ExtendedUserDetails userDetails) {
        if(!userProfileService.existsUserProfileById(userDetails.getUserId())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ErrorDTO("User profile doesn't exist. You should create one first."));
        }

        UserProfile userProfile = userProfileService.fetchUserProfileByUser(userDetails.getUserId());
        UserProfileDto dto = UserProfileDto.fromEntity(userProfile);
        return ResponseEntity.ok(dto);
    }

    @GetMapping("/user/profile/{id}")
    public ResponseEntity<?> fetchUserProfileById(@PathVariable Long id, @AuthenticationPrincipal ExtendedUserDetails userDetails) {
        if(!userProfileService.existsUserProfileById(id)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ErrorDTO("User profile doesn't exist."));
        }

        UserProfile userProfile = userProfileService.fetchUserProfileByUser(id);
        UserProfileDto dto = UserProfileDto.fromEntity(userProfile);
        return ResponseEntity.ok(dto);
    }

    @PostMapping("/user/profile")
    public ResponseEntity<?> createUserProfile(@Valid @RequestBody UserProfileDto dto, BindingResult bindingResult,
                                               @AuthenticationPrincipal UserDetails userDetails) {
        if(bindingResult.hasErrors()){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(userService.buildErrorResponseForLogin(bindingResult));
        }

        if (userProfileService.existsUserProfileByEmail(dto.getEmail())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ErrorDTO("User profile with this email already exists."));
        }

        User user = userService.fetchUserByUsername(userDetails.getUsername());
        UserProfile profile = UserProfile.builder()
                .firstName(dto.getFirstName())
                .lastName(dto.getLastName())
                .city(dto.getCity())
                .country(dto.getCountry())
                .email(dto.getEmail())
                .user(user)
                .build();
        userProfileService.save(profile);

        UserProfileDto output = UserProfileDto.fromEntity(profile);
        HttpHeaders headers = new HttpHeaders();
        headers.setLocation(ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(profile.getUserAccountId())
                .toUri());
        return ResponseEntity.status(HttpStatus.CREATED).headers(headers).body(output);
    }

    @PutMapping("/user/profile")
    public ResponseEntity<?> updateUserProfile(@Valid @RequestBody UserProfileDto dto, BindingResult bindingResult,
                                               @AuthenticationPrincipal ExtendedUserDetails userDetails) {
        if(!userProfileService.existsUserProfileById(userDetails.getUserId())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ErrorDTO("User profile doesn't exist. You should create one first."));
        }

        if(bindingResult.hasErrors()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(userService.buildErrorResponseForLogin(bindingResult));
        }

        UserProfile userProfile = userProfileService.fetchUserProfileByUser(userDetails.getUserId());
        if(!userProfile.getEmail().equals(dto.getEmail()) && userProfileService.existsUserProfileByEmail(dto.getEmail())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ErrorDTO("Email must be unique, this one already exists."));
        }

        userProfile.setFirstName(dto.getFirstName());
        userProfile.setLastName(dto.getLastName());
        userProfile.setCity(dto.getCity());
        userProfile.setCountry(dto.getCountry());
        userProfile.setEmail(dto.getEmail());
        userProfileService.save(userProfile);

        UserProfileDto output = UserProfileDto.fromEntity(userProfile);
        return ResponseEntity.ok(dto);
    }

    @PutMapping("/user/profile/{id}")
    public ResponseEntity<?> updateUserProfileById(@PathVariable Long id,
                                                   @Valid @RequestBody UserProfileDto dto, BindingResult bindingResult,
                                                   @AuthenticationPrincipal ExtendedUserDetails userDetails) {
        if(!userProfileService.existsUserProfileById(id)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ErrorDTO("User profile doesn't exist."));
        }

        if (!userDetails.getUserId().equals(id) && !userDetails.getAuthorities().contains("ADMIN")) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ErrorDTO("You are not authorized to access this user profile."));
        }

        if(bindingResult.hasErrors()){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(userService.buildErrorResponseForLogin(bindingResult));
        }

        UserProfile userProfile = userProfileService.fetchUserProfileByUser(id);
        userProfile.setFirstName(dto.getFirstName());
        userProfile.setLastName(dto.getLastName());
        userProfile.setCity(dto.getCity());
        userProfile.setCountry(dto.getCountry());
        userProfile.setEmail(dto.getEmail());
        userProfileService.save(userProfile);

        UserProfileDto output = UserProfileDto.fromEntity(userProfile);
        return ResponseEntity.ok(dto);
    }

    @DeleteMapping("user/{userId}")
    public ResponseEntity<?> deleteUser(@PathVariable Long userId) {
        userService.deleteUser(userId);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @GetMapping("/isRunning")
    public String isRunning() {
        return "Service is running.";
    }

}
