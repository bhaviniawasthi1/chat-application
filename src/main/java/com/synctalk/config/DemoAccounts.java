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

    public record Account(String username, String rawPassword, String displayName, String initials, String avatarUrl) {
    }

    public static final List<Account> ALL = List.of(
            new Account("john", "john123", "John", "J", "/img/avatar-john.svg"),
            new Account("emily", "emily123", "Emily", "E", "/img/avatar-emily.svg")
    );

    public static Optional<Account> byUsername(String username) {
        return ALL.stream().filter(a -> a.username().equals(username)).findFirst();
    }
}
