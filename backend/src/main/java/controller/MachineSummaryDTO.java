package controller;

/**
 * Data Transfer Object for machine list with rating summary
 * Used by the machines-with-stats endpoint to show machines with their average rating and total reviews
 * Lightweight alternative to MachineDetailDTO for listing pages
 */
public class MachineSummaryDTO {
    private Long id;
    private String name;
    private String machineNumber;
    private String machineType;
    private String location;
    private String status;
    private Double pricePerHour;
    private Double pricePerDay;
    private Double averageRating;
    private Long totalRatings;

    public MachineSummaryDTO() {
    }

    public MachineSummaryDTO(Long id, String name, String machineNumber, String machineType,
                            String location, String status, Double pricePerHour, Double pricePerDay,
                            Double averageRating, Long totalRatings) {
        this.id = id;
        this.name = name;
        this.machineNumber = machineNumber;
        this.machineType = machineType;
        this.location = location;
        this.status = status;
        this.pricePerHour = pricePerHour;
        this.pricePerDay = pricePerDay;
        this.averageRating = averageRating;
        this.totalRatings = totalRatings;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getMachineNumber() { return machineNumber; }
    public void setMachineNumber(String machineNumber) { this.machineNumber = machineNumber; }

    public String getMachineType() { return machineType; }
    public void setMachineType(String machineType) { this.machineType = machineType; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Double getPricePerHour() { return pricePerHour; }
    public void setPricePerHour(Double pricePerHour) { this.pricePerHour = pricePerHour; }

    public Double getPricePerDay() { return pricePerDay; }
    public void setPricePerDay(Double pricePerDay) { this.pricePerDay = pricePerDay; }

    public Double getAverageRating() { return averageRating; }
    public void setAverageRating(Double averageRating) { this.averageRating = averageRating; }

    public Long getTotalRatings() { return totalRatings; }
    public void setTotalRatings(Long totalRatings) { this.totalRatings = totalRatings; }
}
