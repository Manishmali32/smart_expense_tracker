package com.example.expensetracker.config;

import com.example.expensetracker.service.CustomUserDetailsService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

/**
 * Spring Security configuration — fully compatible with Spring Boot 3.3 / Java 24.
 *
 * Key differences from the old (Spring Boot 2.x) way:
 *  - Uses SecurityFilterChain @Bean instead of extending WebSecurityConfigurerAdapter (removed in Boot 3)
 *  - Uses DaoAuthenticationProvider @Bean instead of configuring AuthenticationManagerBuilder
 *  - All annotations are from spring-security 6.x
 */
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final CustomUserDetailsService userDetailsService;

    /**
     * BCrypt password encoder.
     * Strength 12 = 2^12 hashing rounds (secure default for production).
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12);
    }

    /**
     * DaoAuthenticationProvider wires:
     *  - our UserDetailsService (loads user from MySQL)
     *  - our PasswordEncoder (BCrypt verifies the submitted password)
     *
     * Spring Security picks this up automatically — no manual registration needed.
     */
    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    /**
     * HTTP Security rules — defines which URLs need login and how login/logout work.
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        http
            // ── URL access rules ────────────────────────────────────
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(
                    "/register",    // registration page (public)
                    "/login",       // login page (public)
                    "/css/**",      // static assets (public)
                    "/js/**",
                    "/images/**",
                    "/favicon.ico"
                ).permitAll()
                .anyRequest().authenticated() // everything else → must be logged in
            )

            // ── Custom login page ────────────────────────────────────
            .formLogin(form -> form
                .loginPage("/login")                     // GET /login → show our custom page
                .loginProcessingUrl("/login")            // POST /login → Spring Security handles
                .defaultSuccessUrl("/dashboard", true)   // redirect after successful login
                .failureUrl("/login?error=true")         // redirect after wrong credentials
                .usernameParameter("email")              // our form field is named "email"
                .passwordParameter("password")
                .permitAll()
            )

            // ── Logout ───────────────────────────────────────────────
            .logout(logout -> logout
                .logoutRequestMatcher(new AntPathRequestMatcher("/logout", "POST"))
                .logoutSuccessUrl("/login?logout=true")
                .invalidateHttpSession(true)
                .deleteCookies("JSESSIONID")
                .clearAuthentication(true)
                .permitAll()
            )

            // ── Wire our DaoAuthenticationProvider ───────────────────
            .authenticationProvider(authenticationProvider());

        return http.build();
    }
}
