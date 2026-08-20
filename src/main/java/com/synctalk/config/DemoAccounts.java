package com.synctalk.config;

import java.util.List;
import java.util.Optional;

/**
 * Two shared demo personas so visitors can try the real-time chat without
 * a signup flow. Anyone can grab a persona, chat with whoever's holding
 * the other one, then log out to free it up for the next pair.
 */
public final class DemoAccounts {

    private DemoAccounts() {
    }

    public record Account(String username, String rawPassword, String displayName, String avatarEmoji) {
    }

    public static final List<Account> ALL = List.of(
            new Account("ada", "ada123", "Ada", "👩‍💻"),
            new Account("turing", "turing123", "Turing", "👨‍💻")
    );

    public static Optional<Account> byUsername(String username) {
        return ALL.stream().filter(a -> a.username().equals(username)).findFirst();
    }
}
