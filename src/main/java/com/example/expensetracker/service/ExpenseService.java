package com.example.expensetracker.service;

import com.example.expensetracker.entity.Expense;
import com.example.expensetracker.entity.User;
import com.example.expensetracker.repository.ExpenseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ExpenseService {

    private final ExpenseRepository expenseRepository;

    public static final List<String> CATEGORIES = Arrays.asList(
            "Food", "Travel", "Shopping", "Bills", "Health", "Entertainment", "Other"
    );

    @Transactional(readOnly = true)
    public List<Expense> getAllExpensesByUser(User user) {
        return expenseRepository.findByUserOrderByExpenseDateDesc(user);
    }

    @Transactional(readOnly = true)
    public List<Expense> getExpensesByUserAndCategory(User user, String category) {
        return expenseRepository.findByUserAndCategoryOrderByExpenseDateDesc(user, category);
    }

    @Transactional
    public void saveExpense(Expense expense, User user) {
        expense.setUser(user);
        expenseRepository.save(expense);
    }

    @Transactional(readOnly = true)
    public Expense getExpenseById(Long id) {
        return expenseRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Expense not found: " + id));
    }

    @Transactional
    public void updateExpense(Expense updated, User user) {
        Expense existing = getExpenseById(updated.getId());
        if (!existing.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Access denied.");
        }
        existing.setTitle(updated.getTitle());
        existing.setAmount(updated.getAmount());
        existing.setCategory(updated.getCategory());
        existing.setExpenseDate(updated.getExpenseDate());
        existing.setNotes(updated.getNotes());
        expenseRepository.save(existing);
    }

    @Transactional
    public void deleteExpense(Long id, User user) {
        Expense expense = getExpenseById(id);
        if (!expense.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Access denied.");
        }
        expenseRepository.delete(expense);
    }

    @Transactional(readOnly = true)
    public BigDecimal getTotalExpenses(User user) {
        return expenseRepository.getTotalExpenseByUser(user);
    }

    @Transactional(readOnly = true)
    public Map<String, BigDecimal> getCategoryTotals(User user) {
        List<Object[]> rows = expenseRepository.getTotalByCategory(user);
        Map<String, BigDecimal> map = new LinkedHashMap<>();
        for (Object[] row : rows) {
            map.put((String) row[0], (BigDecimal) row[1]);
        }
        return map;
    }

    @Transactional(readOnly = true)
    public List<Object[]> getMonthlyExpenseTotals(User user) {
        return expenseRepository.getMonthlyExpenseTotals(user);
    }

    @Transactional(readOnly = true)
    public long countExpenses(User user) {
        return expenseRepository.countByUser(user);
    }

    public List<String> getAllCategories() {
        return CATEGORIES;
    }
}
