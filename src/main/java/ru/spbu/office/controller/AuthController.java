package ru.spbu.office.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import ru.spbu.office.service.RegistrationCheck;
import ru.spbu.office.service.UserService;

@Controller
public class AuthController {

    private final UserService users;

    public AuthController(UserService users) {
        this.users = users;
    }

    @GetMapping("/")
    public String root() {
        return "redirect:/equipment";
    }

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @GetMapping("/register")
    public String registerForm() {
        return "register";
    }

    @PostMapping("/register")
    public String register(@RequestParam String login,
                           @RequestParam String password,
                           Model model) {
        login = login.trim();
        String error = RegistrationCheck.getError(login, password);
        if (error == null && users.exists(login)) {
            error = "Такой логин уже занят";
        }
        if (error != null) {
            model.addAttribute("error", error);
            model.addAttribute("login", login);
            return "register";
        }
        users.register(login, password);
        return "redirect:/login?registered";
    }
}
