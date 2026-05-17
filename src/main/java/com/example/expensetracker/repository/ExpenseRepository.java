package com.example.expensetracker.repository;

import com.example.expensetracker.entity.Expense;
import com.example.expensetracker.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface ExpenseRepository extends JpaRepository<Expense, Long> {

    List<Expense> findByUserOrderByExpenseDateDesc(User user);

    List<Expense> findByUserAndCategoryOrderByExpenseDateDesc(User user, String category);

    @Query("SELECT COALESCE(SUM(e.amount), 0) FROM Expense e WHERE e.user = :user")
    BigDecimal getTotalExpenseByUser(@Param("user") User user);

    @Query("SELECT e.category, COALESCE(SUM(e.amount), 0) " +
           "FROM Expense e WHERE e.user = :user " +
           "GROUP BY e.category ORDER BY SUM(e.amount) DESC")
    List<Object[]> getTotalByCategory(@Param("user") User user);

    /**
     * Monthly totals: [year, month, total].
     * Uses FUNCTION() wrapper for Hibernate dialect compatibility (MySQL + H2).
     */
    @Query("SELECT FUNCTION('YEAR', e.expenseDate), FUNCTION('MONTH', e.expenseDate), " +
           "COALESCE(SUM(e.amount), 0) " +
           "FROM Expense e WHERE e.user = :user " +
           "GROUP BY FUNCTION('YEAR', e.expenseDate), FUNCTION('MONTH', e.expenseDate) " +
           "ORDER BY FUNCTION('YEAR', e.expenseDate), FUNCTION('MONTH', e.expenseDate)")
    List<Object[]> getMonthlyExpenseTotals(@Param("user") User user);

    long countByUser(User user);
}
