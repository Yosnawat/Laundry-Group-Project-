package service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import controller.MachineDetailDTO;
import controller.MachineDetailDTO.RatingReviewDTO;
import model.AppConstants;
import model.Machine;
import model.Rating;
import model.User;
import repo.MachineRepository;
import repo.RatingRepository;
import repo.UserRepository;

@Service
@Transactional
public class MachineManagementService {
    
    @Autowired
    private MachineRepository machineRepository;
    
    @Autowired
    private RatingRepository ratingRepository;
    
    @Autowired
    private UserRepository userRepository;

    // ===== USER OPERATIONS (Read Only) =====

    /**
     * Get all available machines for browsing
     */
    public List<Machine> getAvailableMachines() {
        return machineRepository.findByStatus(AppConstants.STATUS_AVAILABLE);
    }

    /**
     * Get all machines (for admin/manager list)
     */
    public List<Machine> getAllMachines() {
        return machineRepository.findAll();
    }

    /**
     * Get machine by ID
     */
    public Optional<Machine> getMachineById(Long machineId) {
        return machineRepository.findById(machineId);
    }

    /**
     * Get machine details with ratings and reviews
     * This is the main endpoint for machine detail page
     */
    public MachineDetailDTO getMachineDetail(Long machineId) {
        Machine machine = machineRepository.findById(machineId)
            .orElseThrow(() -> new IllegalArgumentException("Machine not found with ID: " + machineId));

        // Get rating statistics
        Double averageRating = ratingRepository.getAverageRatingByMachineId(machineId);
        Long totalRatings = ratingRepository.countByMachineId(machineId);
        
        // Get rating distribution
        List<Object[]> distributionData = ratingRepository.getRatingDistributionByMachineId(machineId);
        Map<Integer, Long> ratingDistribution = new HashMap<>();
        for (int i = 1; i <= 5; i++) {
            ratingDistribution.put(i, 0L);
        }
        for (Object[] data : distributionData) {
            Integer rating = (Integer) data[0];
            Long count = (Long) data[1];
            ratingDistribution.put(rating, count);
        }

        // Get reviews (ratings with review text)
        List<Rating> reviews = ratingRepository.findByMachineIdWithReviews(machineId);
        List<RatingReviewDTO> reviewDTOs = reviews.stream()
            .map(RatingReviewDTO::new)
            .collect(Collectors.toList());

        return new MachineDetailDTO(
            machine,
            averageRating != null ? averageRating : 0.0,
            totalRatings,
            ratingDistribution,
            reviewDTOs
        );
    }

    /**
     * Search machines with multiple criteria
     */
    public List<Machine> searchMachines(String name, String location, String machineType,
                                       Double minPrice, Double maxPrice, String status) {
        return machineRepository.searchMachines(name, location, machineType, minPrice, maxPrice, status);
    }

    /**
     * Get machines by location
     */
    public List<Machine> getMachinesByLocation(String location) {
        return machineRepository.findByLocation(location);
    }

    /**
     * Get machines by type
     */
    public List<Machine> getMachinesByType(String type) {
        return machineRepository.findByMachineType(type);
    }

    /**
     * Get machines in price range
     */
    public List<Machine> getMachinesByPriceRange(Double minPrice, Double maxPrice) {
        return machineRepository.findByPriceRange(minPrice, maxPrice);
    }

    // ===== MANAGER OPERATIONS (Create, Update, Delete) =====

    /**
     * Create a new machine (Manager only)
     */
    public Machine createMachine(String machineNumber, String name, String machineType,
                                String brand, String model, String capacity,
                                String location, String description, String features,
                                Double pricePerHour, Double pricePerDay) {
        
        // Validation
        if (machineNumber == null || machineNumber.isEmpty()) {
            throw new IllegalArgumentException("Machine number cannot be empty");
        }

        // Check if machine number already exists
        Machine existingMachine = machineRepository.findByMachineNumber(machineNumber);
        if (existingMachine != null) {
            throw new IllegalArgumentException("Machine number already exists: " + machineNumber);
        }

        if (name == null || name.isEmpty()) {
            throw new IllegalArgumentException("Machine name cannot be empty");
        }

        if (machineType == null || machineType.isEmpty()) {
            throw new IllegalArgumentException("Machine type cannot be empty");
        }

        if (pricePerHour == null || pricePerHour < 0) {
            throw new IllegalArgumentException("Price per hour must be non-negative");
        }

        if (pricePerDay == null || pricePerDay < 0) {
            throw new IllegalArgumentException("Price per day must be non-negative");
        }

        // Create machine
        Machine machine = new Machine();
        machine.setMachineNumber(machineNumber);
        machine.setName(name);
        machine.setMachineType(machineType);
        machine.setBrand(brand);
        machine.setModel(model);
        machine.setCapacity(capacity);
        machine.setLocation(location);
        machine.setDescription(description);
        machine.setFeatures(features);
        machine.setPricePerHour(pricePerHour);
        machine.setPricePerDay(pricePerDay);
        machine.setStatus(AppConstants.STATUS_AVAILABLE);

        return machineRepository.save(machine);
    }

    /**
     * Update machine details (Manager only)
     */
    public Machine updateMachine(Long machineId, String name, String machineType,
                                String brand, String model, String capacity,
                                String location, String description, String features,
                                Double pricePerHour, Double pricePerDay, String status) {
        
        Machine machine = machineRepository.findById(machineId)
            .orElseThrow(() -> new IllegalArgumentException("Machine not found with ID: " + machineId));

        // Update fields if provided
        if (name != null && !name.isEmpty()) {
            machine.setName(name);
        }
        if (machineType != null && !machineType.isEmpty()) {
            machine.setMachineType(machineType);
        }
        if (brand != null && !brand.isEmpty()) {
            machine.setBrand(brand);
        }
        if (model != null && !model.isEmpty()) {
            machine.setModel(model);
        }
        if (capacity != null && !capacity.isEmpty()) {
            machine.setCapacity(capacity);
        }
        if (location != null && !location.isEmpty()) {
            machine.setLocation(location);
        }
        if (description != null && !description.isEmpty()) {
            machine.setDescription(description);
        }
        if (features != null && !features.isEmpty()) {
            machine.setFeatures(features);
        }
        if (pricePerHour != null && pricePerHour >= 0) {
            machine.setPricePerHour(pricePerHour);
        }
        if (pricePerDay != null && pricePerDay >= 0) {
            machine.setPricePerDay(pricePerDay);
        }
        if (status != null && !status.isEmpty()) {
            machine.setStatus(status);
        }

        return machineRepository.save(machine);
    }

    /**
     * Update only machine status (Manager only)
     */
    public Machine updateMachineStatus(Long machineId, String status) {
        Machine machine = machineRepository.findById(machineId)
            .orElseThrow(() -> new IllegalArgumentException("Machine not found with ID: " + machineId));

        if (status == null || status.isEmpty()) {
            throw new IllegalArgumentException("Status cannot be empty");
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

    /**
     * Update only pricing information (Manager only)
     */
    public Machine updateMachinePricing(Long machineId, Double pricePerHour, Double pricePerDay) {
        Machine machine = machineRepository.findById(machineId)
            .orElseThrow(() -> new IllegalArgumentException("Machine not found with ID: " + machineId));

        if (pricePerHour != null && pricePerHour >= 0) {
            machine.setPricePerHour(pricePerHour);
        }
        if (pricePerDay != null && pricePerDay >= 0) {
            machine.setPricePerDay(pricePerDay);
        }

        return machineRepository.save(machine);
    }

    /**
     * Delete a machine (Manager only)
     */
    public void deleteMachine(Long machineId) {
        Machine machine = machineRepository.findById(machineId)
            .orElseThrow(() -> new IllegalArgumentException("Machine not found with ID: " + machineId));

        machineRepository.deleteById(machineId);
    }

    /**
     * Assign machine to user (for booking/usage)
     */
    public Machine assignMachine(Long machineId, Long userId) {
        Machine machine = machineRepository.findById(machineId)
            .orElseThrow(() -> new IllegalArgumentException("Machine not found"));

        User user = userRepository.findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("User not found"));

        machine.setStatus(AppConstants.STATUS_IN_USE);
        machine.setCurrentUser(user);
        machine.setUsageStartTime(LocalDateTime.now());

        return machineRepository.save(machine);
    }

    /**
     * Release machine (when user finishes)
     */
    public Machine releaseMachine(Long machineId) {
        Machine machine = machineRepository.findById(machineId)
            .orElseThrow(() -> new IllegalArgumentException("Machine not found"));

        machine.setStatus(AppConstants.STATUS_AVAILABLE);
        machine.setCurrentUser(null);
        machine.setUsageStartTime(null);

        return machineRepository.save(machine);
    }

    /**
     * Get machines in use
     */
    public List<Machine> getInUseMachines() {
        return machineRepository.findByStatus(AppConstants.STATUS_IN_USE);
    }

    /**
     * Get machines by current user
     */
    public List<Machine> getMachinesByCurrentUser(Long userId) {
        return machineRepository.findByCurrentUserId(userId);
    }
}
