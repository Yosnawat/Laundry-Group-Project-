package repo;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import model.Role;
import model.User;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    // Find a user by their student ID (unique)
    User findByStudentId(String studentId);

    // Find a user by email (unique)
    User findByEmail(String email);

    // Check existence by studentId
    Boolean existsByStudentId(String studentId);
    
    // Find all users having a specific Role enum value
    List<User> findByRole(Role role);
}
