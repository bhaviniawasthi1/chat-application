package com.synctalk.security;

import com.synctalk.model.User;
import com.synctalk.repository.UserRepository;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

/** Marks a demo account "in use" the moment its login succeeds. */
@Component
public class SeatAuthenticationSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final UserRepository userRepository;

    public SeatAuthenticationSuccessHandler(UserRepository userRepository) {
        this.userRepository = userRepository;
        setDefaultTargetUrl("/chat");
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                         Authentication authentication) throws IOException, ServletException {
        userRepository.findByUsername(authentication.getName()).ifPresent(user -> {
            user.setInUse(true);
            userRepository.save(user);
        });
        super.onAuthenticationSuccess(request, response, authentication);
    }
}
