package com.leavemanagment.leave_app.service;

import com.leavemanagment.leave_app.model.User;
import com.leavemanagment.leave_app.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Optional;

import java.util.Collection;
import java.util.Collections;

@Service
public class CustomUserDetailsService implements UserDetailsService {
    
    @Autowired
    private UserRepository userRepository;
    
    public CustomUserDetailsService() {
        System.out.println("[DEBUG] CustomUserDetailsService bean created");
    }
    
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        System.out.println("🔍 Looking up user: " + username);
        
        Optional<User> userOpt = userRepository.findByUsername(username);
        if (userOpt.isEmpty()) {
            System.out.println("❌ User not found: " + username);
            throw new UsernameNotFoundException("User not found: " + username);
        }
        
        User user = userOpt.get();
        System.out.println("✅ User found: " + user.getUsername() + " with role: " + user.getRole());
        
        return new CustomUserPrincipal(user);
    }
    
    // Custom UserDetails implementation
    public static class CustomUserPrincipal implements UserDetails {
        private User user;
        
        public CustomUserPrincipal(User user) {
            this.user = user;
        }
        
        @Override
        public Collection<? extends GrantedAuthority> getAuthorities() {
            return Collections.singletonList(
                new SimpleGrantedAuthority("ROLE_" + user.getRole().name())
            );
        }
        
        @Override
        public String getPassword() {
            return user.getPassword();
        }
        
        @Override
        public String getUsername() {
            return user.getUsername();
        }
        
        @Override
        public boolean isAccountNonExpired() {
            return true;
        }
        
        @Override
        public boolean isAccountNonLocked() {
            return true;
        }
        
        @Override
        public boolean isCredentialsNonExpired() {
            return true;
        }
        
        @Override
        public boolean isEnabled() {
            return true;
        }
        
        // Get the full User object
        public User getUser() {
            return user;
        }
        
        public String getFullName() {
            return user.getFullName();
        }
        
        public String getRole() {
            return user.getRole().name();
        }
    }
}