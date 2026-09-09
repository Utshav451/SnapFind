package com.snapfind.backend.config;

import com.snapfind.backend.entity.User;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
public class AuthUtil {

    //Get the currently logged-in user
    public User getCurrentUser() {
        return (User) Objects.requireNonNull(SecurityContextHolder
                        .getContext()
                        .getAuthentication())
                .getPrincipal();
    }
}
