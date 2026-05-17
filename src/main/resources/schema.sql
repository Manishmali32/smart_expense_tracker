-- Run this ONCE in MySQL before starting the app.
-- Spring Boot (JPA ddl-auto=update) will also create tables automatically,
-- so this script is only needed if you prefer manual setup.

CREATE DATABASE IF NOT EXISTS expense_tracker
    CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE expense_tracker;

CREATE TABLE IF NOT EXISTS users (
    id        BIGINT      NOT NULL AUTO_INCREMENT,
    full_name VARCHAR(100) NOT NULL,
    email     VARCHAR(150) NOT NULL UNIQUE,
    password  VARCHAR(255) NOT NULL,
    PRIMARY KEY (id)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS expenses (
    id           BIGINT         NOT NULL AUTO_INCREMENT,
    title        VARCHAR(100)   NOT NULL,
    amount       DECIMAL(12,2)  NOT NULL,
    category     VARCHAR(50)    NOT NULL,
    expense_date DATE           NOT NULL,
    notes        TEXT,
    user_id      BIGINT         NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS incomes (
    id          BIGINT         NOT NULL AUTO_INCREMENT,
    title       VARCHAR(100)   NOT NULL,
    amount      DECIMAL(12,2)  NOT NULL,
    source      VARCHAR(50)    NOT NULL,
    income_date DATE           NOT NULL,
    notes       TEXT,
    user_id     BIGINT         NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_income_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB;
