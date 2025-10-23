package th.mfu;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import th.mfu.model.User;
import th.mfu.service.UserService;
import th.mfu.model.Role;

import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserService userService;
    public AuthController(UserService userService) {
        this.userService = userService;
    }

    // Simple DTOs used to deserialize incoming JSON for login/register
    public static class LoginRequest {
        public String studentId; // optional
        public String email;     // optional
        public String password;  // required
    }

    public static class RegisterRequest {
        public String studentId;
        public String name;
        public String email;
        public String password;
        public String role; // e.g. "STUDENT" or "MANAGER"
    }

    @PostMapping("/login")
    /**
     * POST /api/auth/login
     * Accepts a LoginRequest with either studentId or email and a password.
     * Returns 200 with user info (excluding password) on success or 401 on invalid credentials.
     */
    public ResponseEntity<?> login(@RequestBody LoginRequest req) {
        if (req == null || ( (req.studentId==null || req.studentId.isBlank()) && (req.email==null || req.email.isBlank()) )) {
            return ResponseEntity.badRequest().body(Map.of("status","error","message","studentId or email required"));
        }
        if (req.password == null || req.password.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("status","error","message","password required"));
        }

    // Delegate authentication logic to UserService
    Optional<User> uOpt = userService.authenticate(req.studentId, req.email, req.password);
        return uOpt.map(user -> {
            // DON'T return password
            return ResponseEntity.ok(Map.of(
                "status","ok",
                "userId", user.getId(),
                "studentId", user.getStudentId(),
                "name", user.getName(),
                "role", user.getRole() != null ? user.getRole().name() : null,
                "redirect", "/booking.html"
            ));
        }).orElseGet(() -> ResponseEntity.status(401).body(Map.of("status","error","message","Invalid credentials")));
    }

    /**
     * POST /api/auth/register
     * Creates a new user. Required fields: name, email, password.
     * StudentId and role are optional. Password is stored hashed by UserService.
     */
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest req) {
        if (req == null || req.name == null || req.name.isBlank() || req.email == null || req.email.isBlank() || req.password == null || req.password.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("status","error","message","name, email and password required"));
        }

        try {
            User user = new User();
            user.setStudentId(req.studentId);
            user.setName(req.name);
            user.setEmail(req.email);
            user.setPassword(req.password);

            // attempt to set Role enum if available
            if (req.role != null && !req.role.isBlank()) {
                try {
                    user.setRole(Role.valueOf(req.role.toUpperCase()));
                } catch (Exception ex) {
                    // if Role isn't an enum or invalid value, ignore (or set null)
                }
            }

            // UserService will hash the password and persist the user
            User saved = userService.register(user);
            return ResponseEntity.ok(Map.of("status","ok","id", saved.getId()));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(Map.of("status","error","message", ex.getMessage()));
        } catch (Exception ex) {
            return ResponseEntity.status(500).body(Map.of("status","error","message","Server error"));
        }
    }

    // --- Temporary debug endpoint: list users (without passwords)
    // Use this to verify registrations during development. Remove in production.
    @GetMapping("/debug/users")
    public ResponseEntity<?> listUsers() {
        var list = userService.findAllUsers();
        // map to safe view (exclude password)
        var safe = list.stream().map(u -> Map.of(
            "id", u.getId(),
            "studentId", u.getStudentId(),
            "name", u.getName(),
            "email", u.getEmail(),
            "role", u.getRole() != null ? u.getRole().name() : null
        )).collect(Collectors.toList());
        return ResponseEntity.ok(Map.of("status","ok","users", safe));
    }
}
