package service;

import java.util.List;
import java.util.Optional;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import model.Role;
import model.User;
import repo.UserRepository;

/**
 * Service that handles user registration, authentication, and lookup.
 * 
 * Responsibilities:
 * - User registration with password hashing (BCrypt)
 * - Authentication by student ID or email with password verification
 * - Role-based authentication validation
 * - User lookup operations
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
     * Registers a new user in the system.
     * 
     * Workflow:
     * 1. Validates student ID uniqueness
     * 2. Validates email uniqueness
     * 3. Hashes password using BCrypt
     * 4. Saves user to database
     * 
     * @param user User object with studentId, email, password, and role
     * @return Created user entity
     * @throws IllegalArgumentException if student ID or email already exists
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
     * Authenticates a user by student ID or email with password verification.
     * Supports both BCrypt hashed passwords and legacy plain-text passwords.
     * 
     * Workflow:
     * 1. Validates at least one identifier (studentId or email) is provided
     * 2. Searches for user by studentId first
     * 3. Falls back to email search if student ID not found
     * 4. Verifies password using BCrypt if stored password is hashed (starts with $2a$/$2b$/$2y$)
     * 5. Falls back to plain-text comparison for legacy compatibility
     * 
     * @param studentId Student ID to search for (optional)
     * @param email Email to search for (optional)
     * @param rawPassword Plain-text password to verify
     * @return Optional containing user if authentication successful
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
     * Authenticates a user with role validation.
     * First authenticates with password, then verifies the user's role matches the requested role.
     * 
     * Workflow:
     * 1. Performs password authentication using authenticate() method
     * 2. Returns empty if password authentication fails
     * 3. Validates requested role is not null or blank
     * 4. Converts requested role string to Role enum
     * 5. Compares user's actual role with requested role
     * 6. Returns user only if roles match exactly
     * 
     * @param studentId Student ID to search for (optional)
     * @param email Email to search for (optional)
     * @param rawPassword Plain-text password to verify
     * @param requestedRole Role the user claims to have (STUDENT or MANAGER)
     * @return Optional containing user if both authentication and role validation succeed
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

    /**
     * Retrieves all users in the system.
     * Warning: Caller must not expose password hashes in API responses.
     * 
     * @return List of all users
     */
    public List<User> findAllUsers() {
        return userRepository.findAll();
    }

    /**
     * Finds a user by their student ID.
     * 
     * @param studentId Student ID to search for
     * @return User entity if found, null otherwise
     */
    public User findByStudentId(String studentId) {
        return userRepository.findByStudentId(studentId);
    }

    /**
     * Finds a user by their email address.
     * 
     * @param email Email to search for
     * @return User entity if found, null otherwise
     */
    public User findByEmail(String email) {
        return userRepository.findByEmail(email);
    }
}