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

/**
 * Service for comprehensive machine management operations.
 * Handles both user-facing queries and manager CRUD operations.
 * Includes machine details with ratings, search functionality, and status management.
 */
@Service
@Transactional
public class MachineManagementService {
    
    @Autowired
    private MachineRepository machineRepository;
    
    @Autowired
    private RatingRepository ratingRepository;
    
    @Autowired
    private UserRepository userRepository;

    /**
     * Retrieves all available machines for browsing.
     * Uses JOIN FETCH to optimize database queries.
     * @return List of available machines
     */
    @Transactional(readOnly = true)
    public List<Machine> getAvailableMachines() {
        return machineRepository.findByStatusWithUser(AppConstants.STATUS_AVAILABLE);
    }

    /**
     * Retrieves all machines in the system.
     * Used for admin/manager listings.
     * @return List of all machines
     */
    @Transactional(readOnly = true)
    public List<Machine> getAllMachines() {
        return machineRepository.findAll();
    }

    /**
     * Finds a machine by ID.
     * @param machineId Machine ID
     * @return Optional containing machine if found
     */
    @Transactional(readOnly = true)
    public Optional<Machine> getMachineById(Long machineId) {
        return machineRepository.findById(machineId);
    }

    /**
     * Retrieves comprehensive machine details including ratings and reviews.
     * Workflow:
     * 1. Fetches machine by ID
     * 2. Calculates average rating and total ratings count
     * 3. Builds rating distribution map (1-5 stars)
     * 4. Retrieves all reviews with text
     * 5. Constructs and returns MachineDetailDTO
     * 
     * This is the main method for the machine detail page.
     * @param machineId Machine ID
     * @return Machine details with ratings
     * @throws IllegalArgumentException if machine not found
     */
    @Transactional(readOnly = true)
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
     * Searches machines with multiple optional criteria.
     * All parameters are optional - null values are ignored in search.
     * @param name Machine name (partial match)
     * @param location Machine location
     * @param machineType Machine type
     * @param minPrice Minimum price per hour
     * @param maxPrice Maximum price per hour
     * @param status Machine status
     * @return List of machines matching criteria
     */
    @Transactional(readOnly = true)
    public List<Machine> searchMachines(String name, String location, String machineType,
                                        Double minPrice, Double maxPrice, String status) {
        return machineRepository.searchMachines(name, location, machineType, minPrice, maxPrice, status);
    }

    /**
     * Retrieves machines at a specific location.
     * @param location Location to filter by
     * @return List of machines at the location
     */
    @Transactional(readOnly = true)
    public List<Machine> getMachinesByLocation(String location) {
        return machineRepository.findByLocation(location);
    }

    /**
     * Retrieves machines of a specific type.
     * @param type Machine type to filter by
     * @return List of machines of the type
     */
    @Transactional(readOnly = true)
    public List<Machine> getMachinesByType(String type) {
        return machineRepository.findByMachineType(type);
    }

    /**
     * Creates a new machine (Manager operation).
     * Workflow:
     * 1. Validates all required fields
     * 2. Checks machine number uniqueness
     * 3. Validates pricing is non-negative
     * 4. Creates machine with status AVAILABLE
     * 5. Saves to database
     * 
     * @param machineNumber Unique machine identifier
     * @param name Machine name
     * @param machineType Type of machine
     * @param brand Machine brand
     * @param model Machine model
     * @param capacity Machine capacity
     * @param location Physical location
     * @param description Machine description
     * @param features Special features
     * @param pricePerHour Hourly rate
     * @param pricePerDay Daily rate
     * @return Created machine
     * @throws IllegalArgumentException if validation fails
     */
    public Machine createMachine(String machineNumber, String name, String machineType,
                                 String brand, String model, String capacity,
                                 String location, String description, String features,
                                 Double pricePerHour, Double pricePerDay) {
        
        // Validation
        if (machineNumber == null || machineNumber.isEmpty()) {
            throw new IllegalArgumentException("Machine number cannot be empty");
        }
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
     * Updates an existing machine's details (Manager operation).
     * Only updates fields that are non-null and non-empty.
     * 
     * @param machineId Machine ID to update
     * @param name New name (optional)
     * @param machineType New type (optional)
     * @param brand New brand (optional)
     * @param model New model (optional)
     * @param capacity New capacity (optional)
     * @param location New location (optional)
     * @param description New description (optional)
     * @param features New features (optional)
     * @param pricePerHour New hourly rate (optional)
     * @param pricePerDay New daily rate (optional)
     * @param status New status (optional)
     * @return Updated machine
     * @throws IllegalArgumentException if machine not found
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
     * Updates only the machine status (Manager operation).
     * Automatically clears current user and usage time if status is MAINTENANCE or OUT_OF_SERVICE.
     * 
     * @param machineId Machine ID to update
     * @param status New status (AVAILABLE, IN_USE, MAINTENANCE, OUT_OF_SERVICE)
     * @return Updated machine
     * @throws IllegalArgumentException if machine not found or status is empty
     */
    public Machine updateMachineStatus(Long machineId, String status) {
        Machine machine = machineRepository.findById(machineId)
            .orElseThrow(() -> new IllegalArgumentException("Machine not found with ID: " + machineId));

        if (status == null || status.isEmpty()) {
            throw new IllegalArgumentException("Status cannot be empty");
        }

        machine.setStatus(status);

        if (status.equals(AppConstants.STATUS_MAINTENANCE) || 
            status.equals(AppConstants.STATUS_OUT_OF_SERVICE)) {
            machine.setCurrentUser(null);
            machine.setUsageStartTime(null);
        }

        return machineRepository.save(machine);
    }

    /**
     * Updates only the machine pricing (Manager operation).
     * 
     * @param machineId Machine ID to update
     * @param pricePerHour New hourly rate (optional, must be non-negative)
     * @param pricePerDay New daily rate (optional, must be non-negative)
     * @return Updated machine
     * @throws IllegalArgumentException if machine not found
     */
    public Machine updateMachinePricing(Long machineId, Double pricePerHour, Double pricePerDay) {
        Machine machine = machineRepository.findById(machineId)
            .orElseThrow(() -> new IllegalArgumentException("Machine not found with ID: ".concat(machineId.toString())));

        if (pricePerHour != null && pricePerHour >= 0) {
            machine.setPricePerHour(pricePerHour);
        }
        if (pricePerDay != null && pricePerDay >= 0) {
            machine.setPricePerDay(pricePerDay);
        }

        return machineRepository.save(machine);
    }

    /**
     * Deletes a machine from the system (Manager operation).
     * 
     * Workflow:
     * 1. Finds machine by ID
     * 2. Throws exception if not found
     * 3. Permanently removes from database
     * 
     * @param machineId Machine ID to delete
     * @throws IllegalArgumentException if machine not found
     */
    public void deleteMachine(Long machineId) {
        Machine machine = machineRepository.findById(machineId)
            .orElseThrow(() -> new IllegalArgumentException("Machine not found with ID: " + machineId));

        machineRepository.delete(machine);
    }

    /**
     * Assigns a machine to a user and starts usage tracking.
     * Used when a booking is approved or when a user starts using a machine.
     * 
     * Workflow:
     * 1. Finds machine by ID
     * 2. Finds user by ID
     * 3. Sets machine status to IN_USE
     * 4. Links user as current user
     * 5. Records usage start time
     * 
     * @param machineId Machine ID to assign
     * @param userId User ID to assign to
     * @return Updated machine with assignment
     * @throws IllegalArgumentException if machine or user not found
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
     * Releases a machine from user and stops usage tracking.
     * Used when a booking is completed or when a user finishes using a machine.
     * 
     * Workflow:
     * 1. Finds machine by ID
     * 2. Sets machine status to AVAILABLE
     * 3. Clears current user link
     * 4. Clears usage start time
     * 5. Saves changes to database
     * 
     * @param machineId Machine ID to release
     * @return Updated machine after release
     * @throws IllegalArgumentException if machine not found
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
     * Retrieves all functional machines (non-maintenance machines).
     * Returns machines with status: AVAILABLE or IN_USE (excludes MAINTENANCE and OUT_OF_SERVICE).
     * 
     * @return List of functional machines
     */
    @Transactional(readOnly = true)
    public List<Machine> getFunctionalMachines() {
        return machineRepository.findAllFunctionalMachines();
    }
}
