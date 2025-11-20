package com.securecourse.backend.auth;

import com.securecourse.backend.toggles.ToggleService;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final ToggleService toggleService;

    @PersistenceContext
    private EntityManager entityManager;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder, ToggleService toggleService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.toggleService = toggleService;
    }

    public User signup(String username, String password) {
        if (userRepository.findByUsername(username).isPresent()) {
            throw new RuntimeException("Username already exists");
        }
        User user = new User();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(password));
        return userRepository.save(user);
    }

    public User login(String username, String password) {
        if (toggleService.isSqlInjectionProtectionEnabled()) {
            // SAFE MODE: Use JPA (PreparedStatement) + BCrypt Check
            return userRepository.findByUsername(username)
                    .filter(u -> passwordEncoder.matches(password, u.getPassword()))
                    .orElse(null);
        } else {
            // UNSAFE MODE: SQL Injection Vulnerable
            // We construct a raw SQL query.
            // NOTE: For this demo, in Unsafe Mode, we also skip the strict password check 
            // to demonstrate a full "Authentication Bypass" via SQLi.
            // A real SQLi might bypass authentication by manipulating the WHERE clause 
            // to return a user even if the password check was in the SQL (which it isn't here due to hashing).
            // So we simulate the "Bypass" by accepting the result of the query.
            
            String sql = "SELECT * FROM users WHERE username = '" + username + "'";
            try {
                @SuppressWarnings("unchecked")
                List<User> users = entityManager.createNativeQuery(sql, User.class).getResultList();
                
                if (!users.isEmpty()) {
                    // In a real vulnerable app, the code might take the first result.
                    // If the user injected "' OR '1'='1", this list will contain ALL users.
                    // We return the first one (usually Admin).
                    return users.get(0);
                }
            } catch (Exception e) {
                // SQL Syntax errors (common during injection attempts)
                System.err.println("SQL Error: " + e.getMessage());
                return null;
            }
            return null;
        }
    }
}
