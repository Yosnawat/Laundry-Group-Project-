package service;

import java.util.List;
import java.util.Optional;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import model.Role;
import model.User;
import repo.UserRepository;

/**
 * Service that handles user registration and authentication.
 *
 * Responsibilities:
 * - register(User): validate uniqueness then hash password and save
 * - authenticate(...): find user by studentId or email and verify password
 */
@Service
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Register a new user..
     */
    public User register(User user) {
        if (user.getStudentId() != null && userRepository.existsByStudentId(user.getStudentId())) {
            throw new IllegalArgumentException("Student ID already used");
        }
        if (user.getEmail() != null && userRepository.findByEmail(user.getEmail()) != null) {
            throw new IllegalArgumentException("Email already used");
        }
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        return userRepository.save(user);
    }

    /**
     * Authenticate a user by studentId OR email and a raw (plain) password.
     * Behavior:
     * - tries to find a user by studentId first, then by email
     * - if stored password looks like a BCrypt hash (prefix $2a$/$2b$/$2y$) uses PasswordEncoder.matches
     * - otherwise falls back to plain-text comparison (kept for legacy compatibility)
     * Returns Optional.of(user) on success or Optional.empty() on failure.
     */
    public Optional<User> authenticate(String studentId, String email, String rawPassword) {
        if ((studentId == null || studentId.isBlank()) && (email == null || email.isBlank())) {
            return Optional.empty();
        }

        User user = null;
        if (studentId != null && !studentId.isBlank()) {
            user = userRepository.findByStudentId(studentId);
        }
        if (user == null && email != null && !email.isBlank()) {
            user = userRepository.findByEmail(email);
        }
        if (user == null) return Optional.empty();

        String stored = user.getPassword();
        if (stored == null) return Optional.empty();

        // BCrypt check if hash-looking, else plain text fallback
        if (stored.startsWith("$2a$") || stored.startsWith("$2b$") || stored.startsWith("$2y$")) {
            if (passwordEncoder.matches(rawPassword, stored)) return Optional.of(user);
            return Optional.empty();
        } else {
            if (stored.equals(rawPassword)) return Optional.of(user);
            return Optional.empty();
        }
    }

    /**
     * Authenticate with role validation.
     * Verifies that the requested role matches the user's actual role in the database.
     */
    public Optional<User> authenticate(String studentId, String email, String rawPassword, String requestedRole) {
        // First authenticate with password
        Optional<User> authenticated = authenticate(studentId, email, rawPassword);
        if (!authenticated.isPresent()) {
            return Optional.empty();
        }

        // Then verify the role matches
        User user = authenticated.get();
        if (requestedRole == null || requestedRole.isBlank()) {
            return Optional.empty(); // Role must be provided
        }

        try {
            Role reqRole = Role.valueOf(requestedRole.toUpperCase());
            if (user.getRole() != reqRole) {
                return Optional.empty(); // Role mismatch
            }
        } catch (IllegalArgumentException e) {
            return Optional.empty(); // Invalid role
        }

        return Optional.of(user);
    }

    /** Return all users (used by debug endpoint). Caller must not expose passwords. */
    public List<User> findAllUsers() {
        return userRepository.findAll();
    }

    /** Find user by studentId */
    public User findByStudentId(String studentId) {
        return userRepository.findByStudentId(studentId);
    }

    /** Find user by email */
    public User findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    /** Find users by role */
    public List<User> findByRole(Role role) {
        return userRepository.findByRole(role);
    }

    /** Find user by ID */
    public Optional<User> findById(Long id) {
        return userRepository.findById(id);
    }
}
