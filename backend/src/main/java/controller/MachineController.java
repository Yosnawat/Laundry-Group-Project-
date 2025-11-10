package controller;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import model.Machine;
import repo.RatingRepository;
import service.MachineManagementService;
import util.Validator;

/**
 * REST Controller for machine management operations
 * Handles both user browsing and manager CRUD operations
 */
@RestController
@RequestMapping("/api/machines")
@CrossOrigin(origins = "*", maxAge = 3600)
public class MachineController {

    @Autowired
    private MachineManagementService machineManagementService;

    @Autowired
    private RatingRepository ratingRepository;

    @GetMapping("/available")
    public ResponseEntity<List<Machine>> getAvailableMachines() {
        return ResponseEntity.ok(machineManagementService.getFunctionalMachines());
    }

    @GetMapping("/summary")
    public ResponseEntity<List<MachineSummaryDTO>> getMachinesSummary() {
        List<Machine> machines = machineManagementService.getFunctionalMachines();
        List<MachineSummaryDTO> summaries = machines.stream()
            .map(machine -> {
                Double avgRating = ratingRepository.getAverageRatingByMachineId(machine.getId());
                Long totalRatings = ratingRepository.countByMachineId(machine.getId());
                
                return new MachineSummaryDTO(
                    machine.getId(),
                    machine.getName(),
                    machine.getMachineNumber(),
                    machine.getMachineType(),
                    machine.getLocation(),
                    machine.getStatus(),
                    machine.getPricePerHour(),
                    machine.getPricePerDay(),
                    avgRating != null ? avgRating : 0.0,
                    totalRatings != null ? totalRatings : 0L
                );
            })
            .collect(Collectors.toList());
        
        return ResponseEntity.ok(summaries);
    }

    @GetMapping("/{machineId}/detail")
    public ResponseEntity<MachineDetailDTO> getMachineDetail(@PathVariable Long machineId) {
        Validator.validateId(machineId, "Machine ID");
        return ResponseEntity.ok(machineManagementService.getMachineDetail(machineId));
    }

    @GetMapping
    public ResponseEntity<List<Machine>> getAllMachines() {
        return ResponseEntity.ok(machineManagementService.getAllMachines());
    }

    @GetMapping("/{machineId}")
    public ResponseEntity<Machine> getMachineById(@PathVariable Long machineId) {
        Validator.validateId(machineId, "Machine ID");
        return machineManagementService.getMachineById(machineId)
                .map(ResponseEntity::ok)
                .orElseThrow(() -> new IllegalArgumentException("Machine not found with ID: " + machineId));
    }

    @GetMapping("/search")
    public ResponseEntity<List<Machine>> searchMachines(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String location,
            @RequestParam(required = false) String machineType,
            @RequestParam(required = false) Double minPrice,
            @RequestParam(required = false) Double maxPrice,
            @RequestParam(required = false) String status) {
        return ResponseEntity.ok(machineManagementService.searchMachines(
                name, location, machineType, minPrice, maxPrice, status));
    }

    @GetMapping("/by-location/{location}")
    public ResponseEntity<List<Machine>> getMachinesByLocation(@PathVariable String location) {
        Validator.validateNotEmpty(location, "Location");
        return ResponseEntity.ok(machineManagementService.getMachinesByLocation(location));
    }

    @GetMapping("/by-type/{type}")
    public ResponseEntity<List<Machine>> getMachinesByType(@PathVariable String type) {
        Validator.validateNotEmpty(type, "Machine Type");
        return ResponseEntity.ok(machineManagementService.getMachinesByType(type));
    }

    @PostMapping
    public ResponseEntity<Machine> createMachine(@RequestBody CreateMachineRequest request) {
        validateCreateRequest(request);
        Machine createdMachine = machineManagementService.createMachine(
                request.getMachineNumber(),
                request.getName(),
                request.getMachineType(),
                request.getBrand(),
                request.getModel(),
                request.getCapacity(),
                request.getLocation(),
                request.getDescription(),
                request.getFeatures(),
                request.getPricePerHour(),
                request.getPricePerDay());
        return ResponseEntity.status(HttpStatus.CREATED).body(createdMachine);
    }

    @PutMapping("/{machineId}")
    public ResponseEntity<Machine> updateMachine(
            @PathVariable Long machineId,
            @RequestBody UpdateMachineRequest request) {
        Validator.validateId(machineId, "Machine ID");
        Machine updatedMachine = machineManagementService.updateMachine(
                machineId,
                request.getName(),
                request.getMachineType(),
                request.getBrand(),
                request.getModel(),
                request.getCapacity(),
                request.getLocation(),
                request.getDescription(),
                request.getFeatures(),
                request.getPricePerHour(),
                request.getPricePerDay(),
                request.getStatus());
        return ResponseEntity.ok(updatedMachine);
    }

    @PutMapping("/{machineId}/status")
    public ResponseEntity<Machine> updateMachineStatus(
            @PathVariable Long machineId,
            @RequestParam String status) {
        Validator.validateId(machineId, "Machine ID");
        Validator.validateNotEmpty(status, "Status");
        return ResponseEntity.ok(machineManagementService.updateMachineStatus(machineId, status));
    }

    @PutMapping("/{machineId}/pricing")
    public ResponseEntity<Machine> updateMachinePricing(
            @PathVariable Long machineId,
            @RequestParam Double pricePerHour,
            @RequestParam Double pricePerDay) {
        Validator.validateId(machineId, "Machine ID");
        Validator.validatePrice(pricePerHour, "Price per hour");
        Validator.validatePrice(pricePerDay, "Price per day");
        return ResponseEntity.ok(
                machineManagementService.updateMachinePricing(machineId, pricePerHour, pricePerDay));
    }

    @DeleteMapping("/{machineId}")
    public ResponseEntity<Void> deleteMachine(@PathVariable Long machineId) {
        Validator.validateId(machineId, "Machine ID");
        machineManagementService.deleteMachine(machineId);
        return ResponseEntity.noContent().build();
    }

    private void validateCreateRequest(CreateMachineRequest request) {
        Validator.validateNotEmpty(request.getMachineNumber(), "Machine number");
        Validator.validateNotEmpty(request.getName(), "Machine name");
        Validator.validateNotEmpty(request.getMachineType(), "Machine type");
        Validator.validatePrice(request.getPricePerHour(), "Price per hour");
        Validator.validatePrice(request.getPricePerDay(), "Price per day");
    }

    public static class CreateMachineRequest {
        private String machineNumber;
        private String name;
        private String machineType;
        private String brand;
        private String model;
        private String capacity;
        private String location;
        private String description;
        private String features;
        private Double pricePerHour;
        private Double pricePerDay;

        public String getMachineNumber() { return machineNumber; }
        public void setMachineNumber(String machineNumber) { this.machineNumber = machineNumber; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getMachineType() { return machineType; }
        public void setMachineType(String machineType) { this.machineType = machineType; }
        public String getBrand() { return brand; }
        public void setBrand(String brand) { this.brand = brand; }
        public String getModel() { return model; }
        public void setModel(String model) { this.model = model; }
        public String getCapacity() { return capacity; }
        public void setCapacity(String capacity) { this.capacity = capacity; }
        public String getLocation() { return location; }
        public void setLocation(String location) { this.location = location; }
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        public String getFeatures() { return features; }
        public void setFeatures(String features) { this.features = features; }
        public Double getPricePerHour() { return pricePerHour; }
        public void setPricePerHour(Double pricePerHour) { this.pricePerHour = pricePerHour; }
        public Double getPricePerDay() { return pricePerDay; }
        public void setPricePerDay(Double pricePerDay) { this.pricePerDay = pricePerDay; }
    }

    public static class UpdateMachineRequest {
        private String name;
        private String machineType;
        private String brand;
        private String model;
        private String capacity;
        private String location;
        private String description;
        private String features;
        private Double pricePerHour;
        private Double pricePerDay;
        private String status;

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getMachineType() { return machineType; }
        public void setMachineType(String machineType) { this.machineType = machineType; }
        public String getBrand() { return brand; }
        public void setBrand(String brand) { this.brand = brand; }
        public String getModel() { return model; }
        public void setModel(String model) { this.model = model; }
        public String getCapacity() { return capacity; }
        public void setCapacity(String capacity) { this.capacity = capacity; }
        public String getLocation() { return location; }
        public void setLocation(String location) { this.location = location; }
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        public String getFeatures() { return features; }
        public void setFeatures(String features) { this.features = features; }
        public Double getPricePerHour() { return pricePerHour; }
        public void setPricePerHour(Double pricePerHour) { this.pricePerHour = pricePerHour; }
        public Double getPricePerDay() { return pricePerDay; }
        public void setPricePerDay(Double pricePerDay) { this.pricePerDay = pricePerDay; }
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
    }
}
