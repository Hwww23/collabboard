package com.collabboard.collabboard.security;

import org.springframework.security.core.context.SecurityContextHolder;

public class CurrentUser {

    /**
     * Gets the current authenticated user's ID from the security context.
     * Our JwtFilter stores the userId as the principal.
     * Call this from any controller to get the logged-in user's id.
     */
    public static Long getId() {
        return (Long) SecurityContextHolder.getContext()
                .getAuthentication()
                .getPrincipal();
    }
}