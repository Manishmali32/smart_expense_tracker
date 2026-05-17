package com.example.expensetracker.repository;

import com.example.expensetracker.entity.Income;
import com.example.expensetracker.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface IncomeRepository extends JpaRepository<Income, Long> {

    List<Income> findByUserOrderByIncomeDateDesc(User user);

    @Query("SELECT COALESCE(SUM(i.amount), 0) FROM Income i WHERE i.user = :user")
    BigDecimal getTotalIncomeByUser(@Param("user") User user);

    /**
     * Monthly totals: [year, month, total].
     * Uses FUNCTION('YEAR',...) / FUNCTION('MONTH',...) so Hibernate dialect
     * can resolve them correctly for both MySQL and H2 in tests.
     */
    @Query("SELECT FUNCTION('YEAR', i.incomeDate), FUNCTION('MONTH', i.incomeDate), " +
           "COALESCE(SUM(i.amount), 0) " +
           "FROM Income i WHERE i.user = :user " +
           "GROUP BY FUNCTION('YEAR', i.incomeDate), FUNCTION('MONTH', i.incomeDate) " +
           "ORDER BY FUNCTION('YEAR', i.incomeDate), FUNCTION('MONTH', i.incomeDate)")
    List<Object[]> getMonthlyIncomeTotals(@Param("user") User user);

    long countByUser(User user);
}
