package com.cyberopus.security;

import com.cyberopus.entity.User;
import com.cyberopus.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SecurityUtils {

    private final UserRepository userRepository;

    /**
     * Returns the authenticated User entity from the SecurityContext.
     * Throws UsernameNotFoundException if the principal is not found.
     */
    public User getCurrentUser() {
        String email = SecurityContextHolder.getContext()
                .getAuthentication()
                .getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Authenticated user not found: " + email));
    }

    public Long getCurrentUserId() {
        return getCurrentUser().getId();
    }
}
