package com.synctalk.config;

import com.synctalk.model.User;
import com.synctalk.repository.ChatMessageRepository;
import com.synctalk.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * Reseeds the two demo accounts and wipes chat history every time the app
 * starts. Nothing here is meant to persist — that's what makes it safe to
 * leave running as a public demo (Render's free tier restarts it often
 * enough on its own, but this also covers manual restarts).
 */
@Component
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(UserRepository userRepository,
                            ChatMessageRepository chatMessageRepository,
                            PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.chatMessageRepository = chatMessageRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        chatMessageRepository.deleteAll();
        userRepository.deleteAll();

        for (DemoAccounts.Account account : DemoAccounts.ALL) {
            User user = new User(
                    account.username(),
                    passwordEncoder.encode(account.rawPassword()),
                    account.displayName()
            );
            userRepository.save(user);
        }
    }
}
