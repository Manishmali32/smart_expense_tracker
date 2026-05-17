package com.example.expensetracker;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Entry point for Smart Expense Tracker.
 * Compatible with Java 24 + Spring Boot 3.3.x
 */
@SpringBootApplication
public class ExpenseTrackerApplication {

    public static void main(String[] args) {
        SpringApplication.run(ExpenseTrackerApplication.class, args);
        System.out.println("\n✅ Smart Expense Tracker is running!");
        System.out.println("👉 Open http://localhost:8080 in your browser\n");
    }
}
