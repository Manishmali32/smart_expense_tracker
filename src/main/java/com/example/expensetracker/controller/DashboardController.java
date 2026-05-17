package com.example.expensetracker.controller;

import com.example.expensetracker.entity.User;
import com.example.expensetracker.service.ExpenseService;
import com.example.expensetracker.service.IncomeService;
import com.example.expensetracker.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Month;
import java.util.*;

@Controller
@RequiredArgsConstructor
public class DashboardController {

    private final ExpenseService expenseService;
    private final IncomeService  incomeService;
    private final UserService    userService;

    @GetMapping("/dashboard")
    public String dashboard(@AuthenticationPrincipal UserDetails userDetails, Model model) {

        User currentUser = userService.findByEmail(userDetails.getUsername());

        // ── expense stats ──────────────────────────────────────────
        BigDecimal totalExpenses = nvl(expenseService.getTotalExpenses(currentUser));
        BigDecimal totalIncome   = nvl(incomeService.getTotalIncome(currentUser));
        BigDecimal balance       = totalIncome.subtract(totalExpenses);

        // Pre-compute booleans & strings for the template (avoids T() SpEL in Thymeleaf)
        boolean balancePositive = balance.compareTo(BigDecimal.ZERO) >= 0;
        String  balanceSign     = balancePositive ? "+" : "-";
        String  balanceAbs      = formatRupee(balance.abs());
        String  balanceCardClass = balancePositive ? "bg-primary text-white" : "bg-danger text-white";

        String savingsRate = "N/A";
        if (totalIncome.compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal rate = balance.multiply(BigDecimal.valueOf(100))
                                     .divide(totalIncome, 2, RoundingMode.HALF_UP);
            savingsRate = rate.toPlainString() + "%";
        }

        model.addAttribute("user",            currentUser);
        model.addAttribute("totalExpenses",   totalExpenses);
        model.addAttribute("expenseCount",    expenseService.countExpenses(currentUser));
        model.addAttribute("categoryTotals",  expenseService.getCategoryTotals(currentUser));
        model.addAttribute("recentExpenses",  expenseService.getAllExpensesByUser(currentUser));
        model.addAttribute("totalIncome",     totalIncome);
        model.addAttribute("recentIncomes",   incomeService.getAllIncomesByUser(currentUser));

        // Pre-computed values — no T() needed in templates
        model.addAttribute("balance",         balance);
        model.addAttribute("balancePositive", balancePositive);
        model.addAttribute("balanceSign",     balanceSign);
        model.addAttribute("balanceAbs",      balanceAbs);
        model.addAttribute("balanceCardClass",balanceCardClass);
        model.addAttribute("savingsRate",     savingsRate);

        // ── monthly chart data (last 6 months) ──────────────────────
        Map<String, BigDecimal> expMap = new LinkedHashMap<>();
        for (Object[] row : expenseService.getMonthlyExpenseTotals(currentUser)) {
            int yr = ((Number) row[0]).intValue();
            int mo = ((Number) row[1]).intValue();
            expMap.put(yr + "-" + mo, nvl((BigDecimal) row[2]));
        }
        Map<String, BigDecimal> incMap = new LinkedHashMap<>();
        for (Object[] row : incomeService.getMonthlyIncomeTotals(currentUser)) {
            int yr = ((Number) row[0]).intValue();
            int mo = ((Number) row[1]).intValue();
            incMap.put(yr + "-" + mo, nvl((BigDecimal) row[2]));
        }

        Calendar cal = Calendar.getInstance();
        List<String>     chartLabels = new ArrayList<>();
        List<BigDecimal> chartExp    = new ArrayList<>();
        List<BigDecimal> chartInc    = new ArrayList<>();

        for (int i = 5; i >= 0; i--) {
            Calendar c = (Calendar) cal.clone();
            c.add(Calendar.MONTH, -i);
            int yr  = c.get(Calendar.YEAR);
            int mo  = c.get(Calendar.MONTH) + 1;
            String key = yr + "-" + mo;
            chartLabels.add(Month.of(mo).name().substring(0, 3) + " " + yr);
            chartExp.add(expMap.getOrDefault(key, BigDecimal.ZERO));
            chartInc.add(incMap.getOrDefault(key, BigDecimal.ZERO));
        }

        model.addAttribute("chartLabels", chartLabels);
        model.addAttribute("chartExp",    chartExp);
        model.addAttribute("chartInc",    chartInc);

        model.addAttribute("currentPage", "dashboard");
        return "dashboard";
    }

    private static BigDecimal nvl(BigDecimal value) {
        return value != null ? value : BigDecimal.ZERO;
    }

    private static String formatRupee(BigDecimal value) {
        return String.format("%,.2f", value);
    }
}
