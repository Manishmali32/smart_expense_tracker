package com.example.expensetracker.controller;

import com.example.expensetracker.entity.Income;
import com.example.expensetracker.entity.User;
import com.example.expensetracker.service.IncomeService;
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
@RequestMapping("/incomes")
@RequiredArgsConstructor
public class IncomeController {

    private final IncomeService incomeService;
    private final UserService   userService;

    @GetMapping
    public String list(@AuthenticationPrincipal UserDetails ud, Model model) {
        User user = userService.findByEmail(ud.getUsername());
        model.addAttribute("currentPage",  "incomes");
        model.addAttribute("incomes",      incomeService.getAllIncomesByUser(user));
        model.addAttribute("totalIncome",  incomeService.getTotalIncome(user));
        model.addAttribute("sources",      IncomeService.SOURCES);
        return "income/list";
    }

    @GetMapping("/new")
    public String newForm(Model model) {
        model.addAttribute("currentPage", "incomes");
        model.addAttribute("income",      new Income());
        model.addAttribute("sources",     IncomeService.SOURCES);
        model.addAttribute("isEdit",      false);
        return "income/form";
    }

    @PostMapping("/new")
    public String create(
            @Valid @ModelAttribute Income income,
            BindingResult br,
            @AuthenticationPrincipal UserDetails ud,
            Model model,
            RedirectAttributes ra) {

        if (br.hasErrors()) {
            model.addAttribute("currentPage", "incomes");
            model.addAttribute("sources",     IncomeService.SOURCES);
            model.addAttribute("isEdit",      false);
            return "income/form";
        }
        incomeService.saveIncome(income, userService.findByEmail(ud.getUsername()));
        ra.addFlashAttribute("successMessage", "Income added successfully!");
        return "redirect:/incomes";
    }

    @GetMapping("/{id}/edit")
    public String editForm(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails ud,
            Model model) {

        User user   = userService.findByEmail(ud.getUsername());
        Income income = incomeService.getIncomeById(id);
        if (!income.getUser().getId().equals(user.getId()))
            return "redirect:/incomes";

        model.addAttribute("currentPage", "incomes");
        model.addAttribute("income",      income);
        model.addAttribute("sources",     IncomeService.SOURCES);
        model.addAttribute("isEdit",      true);
        return "income/form";
    }

    @PostMapping("/{id}/edit")
    public String update(
            @PathVariable Long id,
            @Valid @ModelAttribute Income income,
            BindingResult br,
            @AuthenticationPrincipal UserDetails ud,
            Model model,
            RedirectAttributes ra) {

        if (br.hasErrors()) {
            model.addAttribute("currentPage", "incomes");
            model.addAttribute("sources",     IncomeService.SOURCES);
            model.addAttribute("isEdit",      true);
            return "income/form";
        }
        income.setId(id);
        incomeService.updateIncome(income, userService.findByEmail(ud.getUsername()));
        ra.addFlashAttribute("successMessage", "Income updated successfully!");
        return "redirect:/incomes";
    }

    @PostMapping("/{id}/delete")
    public String delete(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails ud,
            RedirectAttributes ra) {

        incomeService.deleteIncome(id, userService.findByEmail(ud.getUsername()));
        ra.addFlashAttribute("successMessage", "Income deleted.");
        return "redirect:/incomes";
    }
}
