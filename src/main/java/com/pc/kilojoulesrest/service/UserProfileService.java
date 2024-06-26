package com.pc.kilojoulesrest.service;

import com.pc.kilojoulesrest.entity.UserProfile;

public interface UserProfileService {
    UserProfile fetchUserProfileByUser(Long id);

    boolean existsUserProfileById(Long id);

    void save(UserProfile profile);

    boolean existsUserProfileByEmail(String email);
}
