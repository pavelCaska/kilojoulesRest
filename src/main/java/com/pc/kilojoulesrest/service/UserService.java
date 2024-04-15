package com.pc.kilojoulesrest.service;

import com.pc.kilojoulesrest.entity.User;
import org.springframework.validation.BindingResult;

import java.util.Map;

public interface UserService {

    User saveUser(User user);

    String encodePassword(String pwd);

    User fetchUserByUsername(String username);

    Map<String, String> buildErrorResponseForLogin(BindingResult bindingResult);
}
