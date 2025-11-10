package service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import model.AppConstants;
import model.Machine;
import repo.MachineRepository;

/**
 * Service for basic machine operations.
 * This service provides read-only access to machine data.
 * For management operations (create, update, delete), use MachineManagementService.
 */
@Service
@Transactional
public class MachineService {
    
    @Autowired
    private MachineRepository machineRepository;
    
    /**
     * Retrieves all machines from the database.
     * Uses JOIN FETCH to eagerly load user associations.
     * @return List of all machines
     */
    @Transactional(readOnly = true)
    public List<Machine> getAllMachines() {
        return machineRepository.findAll();
    }
    
    /**
     * Retrieves all available machines.
     * Filters machines with status = AVAILABLE.
     * Uses JOIN FETCH for performance optimization.
     * @return List of available machines
     */
    @Transactional(readOnly = true)
    public List<Machine> getAvailableMachines() {
        return machineRepository.findByStatusWithUser(AppConstants.STATUS_AVAILABLE);
    }
    
    /**
     * Retrieves all machines currently in use.
     * Filters machines with status = IN_USE.
     * Uses JOIN FETCH for performance optimization.
     * @return List of in-use machines
     */
    @Transactional(readOnly = true)
    public List<Machine> getInUseMachines() {
        return machineRepository.findByStatusWithUser(AppConstants.STATUS_IN_USE);
    }
}
