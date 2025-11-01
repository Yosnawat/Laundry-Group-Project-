package service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import model.AppConstants;
import model.Machine;
import model.User;
import repo.MachineRepository;
import repo.UserRepository;

@Service
@Transactional
public class MachineService {
    
    @Autowired
    private MachineRepository machineRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    // Manager assigns machine to a student
    public Machine assignMachine(Long machineId, Long userId) {
        Machine machine = machineRepository.findById(machineId).orElse(null);
        if (machine == null) {
            throw new RuntimeException("Machine not found");
        }
        
        User user = userRepository.findById(userId).orElse(null);
        if (user == null) {
            throw new RuntimeException("User not found");
        }
            
        machine.setStatus(AppConstants.STATUS_IN_USE);
        machine.setCurrentUser(user);
        machine.setUsageStartTime(LocalDateTime.now());
        
        return machineRepository.save(machine);
    }
    
    // Manager releases machine (student finished)
    public Machine releaseMachine(Long machineId) {
        Machine machine = machineRepository.findById(machineId).orElse(null);
        if (machine == null) {
            throw new RuntimeException("Machine not found");
        }
            
        machine.setStatus(AppConstants.STATUS_AVAILABLE);
        machine.setCurrentUser(null);
        machine.setUsageStartTime(null);
        
        return machineRepository.save(machine);
    }
    
    // Manager changes machine status
    public Machine updateMachineStatus(Long machineId, String status) {
        Machine machine = machineRepository.findById(machineId).orElse(null);
        if (machine == null) {
            throw new RuntimeException("Machine not found");
        }
            
        machine.setStatus(status);
        
        // If setting to maintenance or out of service, release current user
        if (status.equals(AppConstants.STATUS_MAINTENANCE) || 
            status.equals(AppConstants.STATUS_OUT_OF_SERVICE)) {
            machine.setCurrentUser(null);
            machine.setUsageStartTime(null);
        }
        
        return machineRepository.save(machine);
    }
    
    // Students can view all machines
    public List<Machine> getAllMachines() {
        return machineRepository.findAll();
    }
    
    // Students can check available machines
    public List<Machine> getAvailableMachines() {
        return machineRepository.findByStatus(AppConstants.STATUS_AVAILABLE);
    }
    
    // Get machines currently in use
    public List<Machine> getInUseMachines() {
        return machineRepository.findByStatus(AppConstants.STATUS_IN_USE);
    }
    
    // Get machines by user
    public List<Machine> getMachinesByUser(Long userId) {
        return machineRepository.findByCurrentUserId(userId);
    }
    
    // Manager deletes machine
    public void deleteMachine(Long machineId) {
        machineRepository.deleteById(machineId);
    }
}
