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

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class AuthController {

    private final UserService userService;

    public AuthController(UserService userService) {
        this.userService = userService;
    }

    // --- DTOs for requests ---
    public static class LoginRequest {
        public String studentId;
        public String email;
        public String password;
        public String role; // Role selection from UI (must match actual user role)
    }

    public static class RegisterRequest {
        public String studentId;
        public String name;
        public String email;
        public String password;
        public String role; // "STUDENT" or "MANAGER"
    }

    // --- Login ---
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

    // --- Register ---
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

    // --- List all users (without passwords) ---
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
    }//
}
