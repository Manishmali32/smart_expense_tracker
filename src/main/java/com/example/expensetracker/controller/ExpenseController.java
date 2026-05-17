package com.example.expensetracker.controller;

import com.example.expensetracker.entity.Expense;
import com.example.expensetracker.entity.User;
import com.example.expensetracker.service.ExpenseService;
import com.example.expensetracker.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/expenses")
@RequiredArgsConstructor
public class ExpenseController {

    private final ExpenseService expenseService;
    private final UserService    userService;

    @GetMapping
    public String list(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(value = "category", required = false) String category,
            Model model) {

        User u = userService.findByEmail(userDetails.getUsername());
        var expenses = (category != null && !category.isBlank() && !"All".equals(category))
                ? expenseService.getExpensesByUserAndCategory(u, category)
                : expenseService.getAllExpensesByUser(u);

        model.addAttribute("currentPage",       "expenses");
        model.addAttribute("user",              u);
        model.addAttribute("expenses",          expenses);
        model.addAttribute("categories",        expenseService.getAllCategories());
        model.addAttribute("selectedCategory",  category);
        model.addAttribute("totalExpenses",     expenseService.getTotalExpenses(u));
        return "expense/list";
    }

    @GetMapping("/new")
    public String newForm(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        model.addAttribute("currentPage", "expenses");
        model.addAttribute("user",        userService.findByEmail(userDetails.getUsername()));
        model.addAttribute("expense",     new Expense());
        model.addAttribute("categories",  expenseService.getAllCategories());
        model.addAttribute("pageTitle",   "Add New Expense");
        return "expense/form";
    }

    @PostMapping("/new")
    public String saveNew(
            @Valid @ModelAttribute("expense") Expense expense,
            BindingResult br,
            @AuthenticationPrincipal UserDetails userDetails,
            RedirectAttributes ra,
            Model model) {

        User u = userService.findByEmail(userDetails.getUsername());
        if (br.hasErrors()) {
            model.addAttribute("currentPage", "expenses");
            model.addAttribute("user",        u);
            model.addAttribute("categories",  expenseService.getAllCategories());
            model.addAttribute("pageTitle",   "Add New Expense");
            return "expense/form";
        }
        expenseService.saveExpense(expense, u);
        ra.addFlashAttribute("successMessage", "Expense added successfully!");
        return "redirect:/expenses";
    }

    @GetMapping("/{id}/edit")
    public String editForm(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails,
            Model model) {

        User u      = userService.findByEmail(userDetails.getUsername());
        Expense exp = expenseService.getExpenseById(id);
        if (!exp.getUser().getId().equals(u.getId())) return "redirect:/expenses";

        model.addAttribute("currentPage", "expenses");
        model.addAttribute("user",        u);
        model.addAttribute("expense",     exp);
        model.addAttribute("categories",  expenseService.getAllCategories());
        model.addAttribute("pageTitle",   "Edit Expense");
        return "expense/form";
    }

    @PostMapping("/{id}/edit")
    public String updateExpense(
            @PathVariable Long id,
            @Valid @ModelAttribute("expense") Expense expense,
            BindingResult br,
            @AuthenticationPrincipal UserDetails userDetails,
            RedirectAttributes ra,
            Model model) {

        User u = userService.findByEmail(userDetails.getUsername());
        if (br.hasErrors()) {
            model.addAttribute("currentPage", "expenses");
            model.addAttribute("user",        u);
            model.addAttribute("categories",  expenseService.getAllCategories());
            model.addAttribute("pageTitle",   "Edit Expense");
            return "expense/form";
        }
        expense.setId(id);
        expenseService.updateExpense(expense, u);
        ra.addFlashAttribute("successMessage", "Expense updated successfully!");
        return "redirect:/expenses";
    }

    @PostMapping("/{id}/delete")
    public String delete(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails,
            RedirectAttributes ra) {

        User u = userService.findByEmail(userDetails.getUsername());
        try {
            expenseService.deleteExpense(id, u);
            ra.addFlashAttribute("successMessage", "Expense deleted.");
        } catch (RuntimeException ex) {
            ra.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/expenses";
    }
}
