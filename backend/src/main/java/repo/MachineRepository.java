package repo;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import model.Machine;

@Repository
public interface MachineRepository extends JpaRepository<Machine, Long> {

    List<Machine> findByStatus(String status);

    Machine findByMachineNumber(String machineNumber);

    List<Machine> findByCurrentUserId(Long userId);

}
