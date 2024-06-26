package com.pc.kilojoulesrest.config;

import org.springframework.security.core.userdetails.UserDetails;

public interface ExtendedUserDetails extends UserDetails {

    Long getUserId();
}
