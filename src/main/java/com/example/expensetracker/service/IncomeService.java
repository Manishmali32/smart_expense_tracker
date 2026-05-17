package com.example.expensetracker.service;

import com.example.expensetracker.entity.Income;
import com.example.expensetracker.entity.User;
import com.example.expensetracker.repository.IncomeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

@Service
@RequiredArgsConstructor
public class IncomeService {

    private final IncomeRepository incomeRepository;

    public static final List<String> SOURCES = Arrays.asList(
            "Salary", "Freelance", "Business", "Investment", "Rental", "Gift", "Other"
    );

    @Transactional(readOnly = true)
    public List<Income> getAllIncomesByUser(User user) {
        return incomeRepository.findByUserOrderByIncomeDateDesc(user);
    }

    @Transactional
    public void saveIncome(Income income, User user) {
        income.setUser(user);
        incomeRepository.save(income);
    }

    @Transactional(readOnly = true)
    public Income getIncomeById(Long id) {
        return incomeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Income not found: " + id));
    }

    @Transactional
    public void updateIncome(Income updated, User user) {
        Income existing = getIncomeById(updated.getId());
        if (!existing.getUser().getId().equals(user.getId()))
            throw new RuntimeException("Access denied.");
        existing.setTitle(updated.getTitle());
        existing.setAmount(updated.getAmount());
        existing.setSource(updated.getSource());
        existing.setIncomeDate(updated.getIncomeDate());
        existing.setNotes(updated.getNotes());
        incomeRepository.save(existing);
    }

    @Transactional
    public void deleteIncome(Long id, User user) {
        Income income = getIncomeById(id);
        if (!income.getUser().getId().equals(user.getId()))
            throw new RuntimeException("Access denied.");
        incomeRepository.delete(income);
    }

    @Transactional(readOnly = true)
    public BigDecimal getTotalIncome(User user) {
        return incomeRepository.getTotalIncomeByUser(user);
    }

    @Transactional(readOnly = true)
    public List<Object[]> getMonthlyIncomeTotals(User user) {
        return incomeRepository.getMonthlyIncomeTotals(user);
    }

    @Transactional(readOnly = true)
    public long countIncomes(User user) {
        return incomeRepository.countByUser(user);
    }

    public List<String> getAllSources() {
        return SOURCES;
    }
}
