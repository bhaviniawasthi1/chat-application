package com.synctalk.security;

import com.synctalk.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.LogoutHandler;
import org.springframework.stereotype.Component;

/** Frees a demo account's seat as soon as its user logs out. */
@Component
public class SeatLogoutHandler implements LogoutHandler {

    private final UserRepository userRepository;

    public SeatLogoutHandler(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public void logout(HttpServletRequest request, HttpServletResponse response, Authentication authentication) {
        if (authentication == null) {
            return;
        }
        userRepository.findByUsername(authentication.getName()).ifPresent(user -> {
            user.setInUse(false);
            userRepository.save(user);
        });
    }
}
