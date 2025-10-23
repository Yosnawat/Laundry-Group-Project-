package th.mfu;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface MachineRepository extends JpaRepository<Machine, Long> {

    List<Machine> findByStatus(String status);

    List<Machine> findByType(String type);

    Machine findByMachineNumber(String machineNumber);

    List<Machine> findByCurrentUserId(Long userId);

}