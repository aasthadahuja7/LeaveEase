package com.leavemanagment.leave_app.config;

import com.leavemanagment.leave_app.service.CustomUserDetailsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Autowired
    private CustomUserDetailsService customUserDetailsService;

    public SecurityConfig() {
        System.out.println("[DEBUG] SecurityConfig bean created");
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(authz -> authz
                .requestMatchers("/", "/login", "/login.html", "/signup", "/signup.html", "/register", "/css/**", "/js/**", "/images/**", 
                               "/static/**", "/uploads/**", "/index.html", "/style.css", "/script.js", "/test.html", "/create-users", "/test-user", "/debug-login",
                               "/api/test-email", "/api/test-n8n", "/api/test-notification", "/api/test-employee-email", "/api/test-hr-email").permitAll()
                .requestMatchers("/api/dashboard/hr/**").hasAnyRole("HR", "ADMIN")
                .requestMatchers("/dashboard.html").authenticated()
                .requestMatchers("/hr-dashboard.html").hasAnyRole("HR", "ADMIN")
                .anyRequest().authenticated()
            )
            .formLogin(form -> form
                .loginPage("/login.html")
                .loginProcessingUrl("/login")
                .defaultSuccessUrl("/dashboard", true)
                .failureUrl("/login.html?error=true")
                .permitAll()
            )
            .userDetailsService(customUserDetailsService)
            .logout(logout -> logout
                .logoutUrl("/logout")
                .logoutSuccessUrl("/login.html?logout=true")
                .permitAll()
            )
            .rememberMe(me -> me.key("uniqueAndSecret"));
        return http.build();
    }
}