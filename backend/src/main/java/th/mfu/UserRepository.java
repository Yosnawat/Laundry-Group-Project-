@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    User findByStudentId(String studentId);

    User findByEmail(String email);

    Boolean existsByStudentId(String studentId);
    
    List<User> findByRole(String role);
}