package controller;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import model.Role;
import model.User;
import service.UserService;

/**
 * REST controller for user authentication and registration.
 * Handles login, registration, and user listing endpoints.
 */
@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class AuthController {

    private final UserService userService;

    public AuthController(UserService userService) {
        this.userService = userService;
    }

    /**
     * DTO for login requests.
     */
    public static class LoginRequest {
        public String studentId;
        public String email;
        public String password;
        public String role;
    }

    /**
     * DTO for registration requests.
     */
    public static class RegisterRequest {
        public String studentId;
        public String name;
        public String email;
        public String password;
        public String role;
    }

    /**
     * Authenticates a user with credentials and role validation.
     * 
     * Workflow:
     * 1. Validates required fields (studentId/email, password, role)
     * 2. Calls UserService to authenticate with role check
     * 3. Returns user data (id, studentId, name, role) if successful
     * 4. Returns 401 error if credentials invalid
     * 
     * @param req LoginRequest containing studentId/email, password, and role
     * @return ResponseEntity with user data or error message
     */
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest req) {
        if ((req.studentId == null || req.studentId.isBlank()) &&
            (req.email == null || req.email.isBlank())) {
            return ResponseEntity.badRequest().body(Map.of("error", "studentId or email required"));
        }

        if (req.password == null || req.password.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "password required"));
        }

        if (req.role == null || req.role.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "role required"));
        }

        return userService.authenticate(req.studentId, req.email, req.password, req.role)
                .map(user -> ResponseEntity.ok(Map.of(
                        "id", user.getId(),
                        "studentId", user.getStudentId(),
                        "name", user.getName(),
                        "role", user.getRole() != null ? user.getRole().name() : null
                )))
                .orElseGet(() -> ResponseEntity.status(401).body(Map.of("error", "Invalid credentials")));
    }

    /**
     * Registers a new user in the system.
     * 
     * Workflow:
     * 1. Validates required fields (name, email, password)
     * 2. Creates User entity with provided data
     * 3. Parses and sets role if provided
     * 4. Calls UserService to register (hashes password, validates uniqueness)
     * 5. Returns created user ID
     * 
     * @param req RegisterRequest containing user registration data
     * @return ResponseEntity with new user ID or error message
     */
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest req) {
        if (req.name == null || req.name.isBlank() ||
            req.email == null || req.email.isBlank() ||
            req.password == null || req.password.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "name, email, and password required"));
        }

        User user = new User();
        user.setStudentId(req.studentId);
        user.setName(req.name);
        user.setEmail(req.email);
        user.setPassword(req.password);

        if (req.role != null) {
            try {
                user.setRole(Role.valueOf(req.role.toUpperCase()));
            } catch (Exception ignored) {}
        }

        User saved = userService.register(user);
        return ResponseEntity.ok(Map.of("id", saved.getId()));
    }

    /**
     * Retrieves all users in the system (admin/debug endpoint).
     * Note: Password hashes are excluded from response for security.
     * 
     * @return ResponseEntity with list of users (id, studentId, name, email, role)
     */
    @GetMapping("/users")
    public ResponseEntity<?> listUsers() {
        List<User> users = userService.findAllUsers();
        var safe = users.stream().map(u -> Map.of(
                "id", u.getId(),
                "studentId", u.getStudentId(),
                "name", u.getName(),
                "email", u.getEmail(),
                "role", u.getRole() != null ? u.getRole().name() : null
        )).collect(Collectors.toList());

        return ResponseEntity.ok(Map.of("users", safe));
    }
}
