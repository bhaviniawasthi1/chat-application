package com.synctalk.controller;

import com.synctalk.model.User;
import com.synctalk.repository.UserRepository;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;
import java.util.Map;

@Controller
public class PageController {

    private final UserRepository userRepository;

    public PageController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @GetMapping("/")
    public String landing(Model model) {
        model.addAttribute("accounts", userRepository.findAllByOrderByUsernameAsc());
        return "landing";
    }

    @GetMapping("/login")
    public String login(@org.springframework.web.bind.annotation.RequestParam(required = false) String user, Model model) {
        model.addAttribute("prefillUser", user == null ? "" : user);
        return "login";
    }

    @GetMapping("/chat")
    public String chat(@AuthenticationPrincipal User user, Model model) {
        model.addAttribute("displayName", user.getDisplayName());
        model.addAttribute("username", user.getUsername());
        return "chat";
    }

    @org.springframework.web.bind.annotation.ResponseBody
    @GetMapping("/api/status")
    public List<Map<String, Object>> status() {
        return userRepository.findAllByOrderByUsernameAsc().stream()
                .map(u -> Map.<String, Object>of(
                        "displayName", u.getDisplayName(),
                        "inUse", u.isInUse()))
                .toList();
    }
}
