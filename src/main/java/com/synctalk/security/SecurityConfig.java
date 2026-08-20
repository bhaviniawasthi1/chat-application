package com.synctalk.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final SeatAuthenticationSuccessHandler successHandler;
    private final SeatLogoutHandler logoutHandler;

    public SecurityConfig(SeatAuthenticationSuccessHandler successHandler, SeatLogoutHandler logoutHandler) {
        this.successHandler = successHandler;
        this.logoutHandler = logoutHandler;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/", "/login", "/css/**", "/js/**", "/img/**", "/favicon.ico", "/api/status", "/ws/**").permitAll()
                        .anyRequest().authenticated()
                )
                .formLogin(form -> form
                        .loginPage("/login")
                        .successHandler(successHandler)
                        .failureUrl("/login?error")
                        .permitAll()
                )
                .logout(logout -> logout
                        .addLogoutHandler(logoutHandler)
                        .logoutSuccessUrl("/login")
                        .permitAll()
                )
                // Chat runs over SockJS/STOMP with its own message-level checks; the
                // handshake endpoint has no CSRF token available to it.
                .csrf(csrf -> csrf.ignoringRequestMatchers("/ws/**"));
        return http.build();
    }
}
