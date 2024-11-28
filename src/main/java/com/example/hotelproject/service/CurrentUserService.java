package com.example.hotelproject.service;

import com.example.hotelproject.config.SecurityUser;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

@Service
public class CurrentUserService {

    public SecurityUser getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated() || !(authentication.getPrincipal() instanceof SecurityUser)) {
            throw new AccessDeniedException("User is not authenticated");
        }

        return (SecurityUser) authentication.getPrincipal();
    }

    public Long getCurrentUserId() {
        return getCurrentUser().getUser().getId();
    }
}