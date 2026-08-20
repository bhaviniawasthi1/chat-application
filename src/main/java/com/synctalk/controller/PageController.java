package com.synctalk.controller;

import com.synctalk.config.DemoAccounts;
import com.synctalk.model.User;
import com.synctalk.repository.UserRepository;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;
import java.util.Map;

@Controller
public class PageController {

    private final UserRepository userRepository;

    public PageController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public record AccountView(String username, String rawPassword, String displayName, String avatarEmoji, boolean inUse) {
    }

    @GetMapping("/")
    public String landing(Model model) {
        List<AccountView> views = DemoAccounts.ALL.stream()
                .map(account -> {
                    boolean inUse = userRepository.findByUsername(account.username())
                            .map(User::isInUse)
                            .orElse(false);
                    return new AccountView(account.username(), account.rawPassword(), account.displayName(),
                            account.avatarEmoji(), inUse);
                })
                .toList();
        model.addAttribute("accounts", views);
        return "landing";
    }

    @GetMapping("/login")
    public String login(@RequestParam(required = false) String user,
                         @RequestParam(required = false) String pass,
                         Model model) {
        model.addAttribute("prefillUser", user == null ? "" : user);
        model.addAttribute("prefillPass", pass == null ? "" : pass);
        return "login";
    }

    @GetMapping("/chat")
    public String chat(@AuthenticationPrincipal User user, Model model) {
        model.addAttribute("displayName", user.getDisplayName());
        model.addAttribute("username", user.getUsername());

        String myAvatar = DemoAccounts.byUsername(user.getUsername())
                .map(DemoAccounts.Account::avatarEmoji).orElse("🙂");
        DemoAccounts.Account other = DemoAccounts.ALL.stream()
                .filter(a -> !a.username().equals(user.getUsername()))
                .findFirst().orElse(null);

        model.addAttribute("myAvatar", myAvatar);
        model.addAttribute("otherName", other == null ? "the other seat" : other.displayName());
        model.addAttribute("otherAvatar", other == null ? "🙂" : other.avatarEmoji());
        return "chat";
    }

    @ResponseBody
    @GetMapping("/api/status")
    public List<Map<String, Object>> status() {
        return DemoAccounts.ALL.stream()
                .map(account -> {
                    boolean inUse = userRepository.findByUsername(account.username())
                            .map(User::isInUse)
                            .orElse(false);
                    return Map.<String, Object>of(
                            "displayName", account.displayName(),
                            "inUse", inUse);
                })
                .toList();
    }
}
