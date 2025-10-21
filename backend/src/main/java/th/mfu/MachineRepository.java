@Repository
public interface MachineRepository extends JpaRepository<Machine, Long> {

    List<Machine> findByStatus(String status);

    List<Machine> findByType(String type);

    Machine findByMachineNumber(String machineNumber);

    List<Machine> findByCurrentUserId(Long userId);

}