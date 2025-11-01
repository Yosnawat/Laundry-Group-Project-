package service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import model.AppConstants;
import model.Machine;
import model.User;
import repo.MachineRepository;
import repo.RatingRepository;
import repo.UserRepository;

@Service
@Transactional
public class MachineService {
    
    @Autowired
    private MachineRepository machineRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private RatingRepository ratingRepository;
    
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
    
    // Get machine by ID
    public Optional<Machine> getMachineById(Long machineId) {
        return machineRepository.findById(machineId);
    }
    
    /**
     * Smart matching algorithm to find the best available machine for a user
     * Considers: availability, location preferences, and machine ratings
     */
    public Optional<Machine> findBestAvailableMachine(Long userId, String preferredLocation) {
        try {
            User user = userRepository.findById(userId).orElse(null);
            if (user == null) {
                return Optional.empty();
            }
            
            // Get all available machines
            List<Machine> availableMachines = machineRepository.findByStatus(AppConstants.STATUS_AVAILABLE);
            
            if (availableMachines.isEmpty()) {
                return Optional.empty();
            }
            
            // Filter by preferred location if provided
            if (preferredLocation != null && !preferredLocation.isEmpty()) {
                List<Machine> machinesByLocation = availableMachines.stream()
                    .filter(m -> m.getLocation() != null && m.getLocation().equalsIgnoreCase(preferredLocation))
                    .collect(Collectors.toList());
                
                if (!machinesByLocation.isEmpty()) {
                    return Optional.of(rankMachinesByRating(machinesByLocation).get(0));
                }
            }
            
            // Return the best rated machine from all available
            return Optional.of(rankMachinesByRating(availableMachines).get(0));
            
        } catch (Exception e) {
            return Optional.empty();
        }
    }
    
    /**
     * Get a list of suggested machines ranked by rating and quality
     */
    public List<Machine> getSuggestedMachines(Long userId, String preferredLocation, int limit) {
        try {
            User user = userRepository.findById(userId).orElse(null);
            if (user == null) {
                return List.of();
            }
            
            List<Machine> availableMachines = machineRepository.findByStatus(AppConstants.STATUS_AVAILABLE);
            
            if (availableMachines.isEmpty()) {
                return List.of();
            }
            
            // Filter by location if provided
            if (preferredLocation != null && !preferredLocation.isEmpty()) {
                availableMachines = availableMachines.stream()
                    .filter(m -> m.getLocation() != null && m.getLocation().equalsIgnoreCase(preferredLocation))
                    .collect(Collectors.toList());
            }
            
            // Rank by rating and return top N
            List<Machine> ranked = rankMachinesByRating(availableMachines);
            return ranked.stream().limit(limit).collect(Collectors.toList());
            
        } catch (Exception e) {
            return List.of();
        }
    }
    
    /**
     * Get available machines grouped by location
     */
    public Map<String, List<Machine>> getAvailableMachinesByLocationGroup() {
        List<Machine> availableMachines = getAvailableMachines();
        return availableMachines.stream()
            .collect(Collectors.groupingBy(m -> m.getLocation() != null ? m.getLocation() : "Unknown"));
    }
    
    /**
     * Helper method to rank machines by average rating
     */
    private List<Machine> rankMachinesByRating(List<Machine> machines) {
        return machines.stream()
            .map(machine -> {
                Double avgRating = ratingRepository.getAverageRatingByMachineId(machine.getId());
                return new MachineWithRating(machine, avgRating != null ? avgRating : 0.0);
            })
            .sorted((a, b) -> Double.compare(b.getAverageRating(), a.getAverageRating()))
            .map(MachineWithRating::getMachine)
            .collect(Collectors.toList());
    }
    
    /**
     * Helper class to associate machines with their average rating
     */
    private static class MachineWithRating {
        private Machine machine;
        private Double averageRating;
        
        public MachineWithRating(Machine machine, Double averageRating) {
            this.machine = machine;
            this.averageRating = averageRating;
        }
        
        public Machine getMachine() {
            return machine;
        }
        
        public Double getAverageRating() {
            return averageRating;
        }
    }
}
