package controller;
import java.time.LocalDateTime; // หรือ java.util.Date ขึ้นอยู่กับโมเดล Rating ของคุณ
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

import model.Machine;
import model.Rating;

/**
 * Data Transfer Object for complete machine details
 * This DTO is used when displaying a machine detail page with all info including ratings and reviews
 */
public class MachineDetailDTO {
    // Machine information
    private Long id;
    private String machineNumber;
    private String name;
    private String machineType;
    private String brand;
    private String model;
    private String capacity;
    private String description;
    private String features;
    private String location;
    private String status;

    // Pricing
    private Double pricePerHour;
    private Double pricePerDay;

    // Rating and review information
    private Double averageRating;
    private Long totalRatings;
    private Map<Integer, Long> ratingDistribution;
    private List<RatingReviewDTO> reviews;
    private Integer userRatingCount; // How many ratings this machine has from users

    // Metadata
    private String createdAt;
    private String updatedAt;

    public MachineDetailDTO() {
    }

    public MachineDetailDTO(Machine machine, Double averageRating, Long totalRatings,
                           Map<Integer, Long> ratingDistribution, List<RatingReviewDTO> reviews) {
        this.id = machine.getId();
        this.machineNumber = machine.getMachineNumber();
        this.name = machine.getName();
        this.machineType = machine.getMachineType();
        this.brand = machine.getBrand();
        this.model = machine.getModel();
        this.capacity = machine.getCapacity();
        this.description = machine.getDescription();
        this.features = machine.getFeatures();
        this.location = machine.getLocation();
        this.status = machine.getStatus();
        this.pricePerHour = machine.getPricePerHour();
        this.pricePerDay = machine.getPricePerDay();
        this.averageRating = averageRating;
        this.totalRatings = totalRatings;
        this.ratingDistribution = ratingDistribution;
        this.reviews = reviews;
        this.userRatingCount = reviews != null ? reviews.size() : 0;
        this.createdAt = machine.getCreatedAt() != null ? machine.getCreatedAt().toString() : null;
        this.updatedAt = machine.getUpdatedAt() != null ? machine.getUpdatedAt().toString() : null;
    }

    // ===== Getters and Setters =====

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

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

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getFeatures() { return features; }
    public void setFeatures(String features) { this.features = features; }

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

    public Map<Integer, Long> getRatingDistribution() { return ratingDistribution; }
    public void setRatingDistribution(Map<Integer, Long> ratingDistribution) { this.ratingDistribution = ratingDistribution; }

    public List<RatingReviewDTO> getReviews() { return reviews; }
    public void setReviews(List<RatingReviewDTO> reviews) { this.reviews = reviews; }

    public Integer getUserRatingCount() { return userRatingCount; }
    public void setUserRatingCount(Integer userRatingCount) { this.userRatingCount = userRatingCount; }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }

    public String getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(String updatedAt) { this.updatedAt = updatedAt; }

    /**
     * Inner DTO for rating reviews
     */
    public static class RatingReviewDTO {
        private Long ratingId;
        private Integer rating;
        private String reviewText;
        private String userName;
        
        // --- ส่วนที่แก้ไข ---
        // เราจะเปลี่ยนชื่อ field นี้ให้ตรงกับที่ Thymeleaf เรียกใช้
        private String formattedCreatedAt; 

        // สร้างตัวจัดรูปแบบ (Formatter)
        // คุณสามารถเปลี่ยน 'dd/MM/yyyy HH:mm' เป็นรูปแบบอื่นที่ต้องการได้
        private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd MMMM yyyy 'at' HH:mm");

        public RatingReviewDTO() {
        }

        public RatingReviewDTO(Rating rating) {
            this.ratingId = rating.getId();
            this.rating = rating.getRating();
            this.reviewText = rating.getReviewText();
            this.userName = rating.getUser() != null ? rating.getUser().getName() : "Anonymous";
            
            // --- ส่วนที่แก้ไข ---
            // ตรวจสอบว่า createdAt ไม่ null
            if (rating.getCreatedAt() != null) {
                // สมมติว่า rating.getCreatedAt() คืนค่าเป็น LocalDateTime
                // ถ้าเป็น java.util.Date ให้ใช้วิธีอื่น
                this.formattedCreatedAt = rating.getCreatedAt().format(formatter);
            } else {
                this.formattedCreatedAt = null; // หรือ "N/A"
            }
        }

        // Getters and Setters
        public Long getRatingId() { return ratingId; }
        public void setRatingId(Long ratingId) { this.ratingId = ratingId; }

        public Integer getRating() { return rating; }
        public void setRating(Integer rating) { this.rating = rating; }

        public String getReviewText() { return reviewText; }
        public void setReviewText(String reviewText) { this.reviewText = reviewText; }

        public String getUserName() { return userName; }
        public void setUserName(String userName) { this.userName = userName; }

        // --- ส่วนที่แก้ไข ---
        /// เปลี่ยน Getter/Setter ให้ตรงกับชื่อ field ใหม่
        public String getFormattedCreatedAt() { return formattedCreatedAt; }
        public void setFormattedCreatedAt(String formattedCreatedAt) { this.formattedCreatedAt = formattedCreatedAt; }
    }
}