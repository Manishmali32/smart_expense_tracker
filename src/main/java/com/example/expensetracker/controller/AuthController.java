package com.example.expensetracker.controller;

import com.example.expensetracker.entity.User;
import com.example.expensetracker.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Handles /login, /register, and root / redirect.
 */
@Controller
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;

    // Redirect root to dashboard (Spring Security will redirect to /login if not logged in)
    @GetMapping("/")
    public String root() {
        return "redirect:/dashboard";
    }

    // ── LOGIN ──────────────────────────────────────────────────
    @GetMapping("/login")
    public String loginPage(
            @RequestParam(value = "error",  required = false) String error,
            @RequestParam(value = "logout", required = false) String logout,
            Model model) {

        if (error  != null) model.addAttribute("errorMessage",   "Invalid email or password. Please try again.");
        if (logout != null) model.addAttribute("successMessage", "You have been logged out successfully.");
        return "auth/login";
    }

    // ── REGISTER (GET) ─────────────────────────────────────────
    @GetMapping("/register")
    public String registerPage(Model model) {
        model.addAttribute("user", new User());
        return "auth/register";
    }

    // ── REGISTER (POST) ────────────────────────────────────────
    @PostMapping("/register")
    public String processRegister(
            @Valid @ModelAttribute("user") User user,
            BindingResult bindingResult,
            RedirectAttributes redirectAttrs,
            Model model) {

        if (bindingResult.hasErrors()) {
            return "auth/register";
        }

        try {
            userService.registerUser(user);
            redirectAttrs.addFlashAttribute("successMessage",
                    "Account created! Please log in.");
            return "redirect:/login";
        } catch (RuntimeException ex) {
            model.addAttribute("errorMessage", ex.getMessage());
            return "auth/register";
        }
    }
}
