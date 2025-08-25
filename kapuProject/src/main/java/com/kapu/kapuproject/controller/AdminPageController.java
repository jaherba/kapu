package com.kapu.kapuproject.controller;

import com.kapu.kapuproject.model.User;
import com.kapu.kapuproject.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.security.Principal;
import java.util.Optional;

@Controller
public class AdminPageController {

    @Autowired
    private UserService userService;

    @GetMapping("/admin-page")
    public String adminPage(Model model, Principal principal) {
        String email = principal.getName();
        Optional<User> userOpt = userService.getUserByEmail(email);

        if (userOpt.isEmpty() || !userOpt.get().getName().equals("Reze")) {
            return "redirect:/access-denied";
        }

        model.addAttribute("currentUser", userOpt.get());
        return "admin-page";
    }

    @GetMapping("/special-controls")
    public String specialControls(Model model, Principal principal) {
        String email = principal.getName();
        Optional<User> userOpt = userService.getUserByEmail(email);

        if (userOpt.isEmpty() || !userOpt.get().getName().equals("Reze")) {
            return "redirect:/access-denied";
        }

        model.addAttribute("currentUser", userOpt.get());
        return "special-controls";
    }
}
