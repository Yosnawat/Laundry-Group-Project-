package th.mfu.model;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Unique student identifier (optional for some users)
    @Column(nullable = false, unique = true)
    private String studentId;

    // Full name shown in UI
    @Column(nullable = false)
    private String name;

    // Email used for login alternative to studentId
    @Column(nullable = false, unique = true)
    private String email;

    // Hashed password (BCrypt) or legacy plain-text
    @Column(nullable = false)
    private String password;

    // Role enum used for authorization decisions
    @Enumerated(EnumType.STRING)
    private th.mfu.model.Role role;

    // Whether the account is active
    @Column(nullable = false)
    private Boolean isActive = true;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    // getters and setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getStudentId() { return studentId; }
    public void setStudentId(String studentId) { this.studentId = studentId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public th.mfu.model.Role getRole() { return role; }
    public void setRole(th.mfu.model.Role role) { this.role = role; }

    public Boolean getIsActive() { return isActive; }
    public void setIsActive(Boolean isActive) { this.isActive = isActive; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
