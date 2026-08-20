package com.synctalk.config;

import java.util.List;

/**
 * Exactly two shared demo accounts. This is a portfolio project, not a
 * real messenger — anyone can grab one of these two seats, chat with
 * whoever is holding the other one, then log out to free it up.
 */
public final class DemoAccounts {

    private DemoAccounts() {
    }

    public record Account(String username, String rawPassword, String displayName) {
    }

    public static final List<Account> ALL = List.of(
            new Account("alex", "alex123", "Alex"),
            new Account("sam", "sam123", "Sam")
    );
}
