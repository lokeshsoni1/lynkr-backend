package com.lynkr.backend.security;

import com.lynkr.backend.model.User;
import com.lynkr.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class SecurityUtils {

    private final UserRepository userRepository;

    public Optional<User> getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || authentication.getPrincipal().equals("anonymousUser")) {
            return Optional.empty();
        }

        Object principal = authentication.getPrincipal();
        String email;
        if (principal instanceof UserDetails userDetails) {
            email = userDetails.getUsername();
        } else {
            email = principal.toString();
        }

        return userRepository.findByEmail(email);
    }
}
