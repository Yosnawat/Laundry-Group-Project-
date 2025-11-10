package model;

import java.time.LocalDateTime;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.MapsId;
import javax.persistence.OneToOne;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import javax.persistence.Table;

@Entity
@Table(name = "booking_status") // Table to store the dependent status info
public class BookingStatus {

    @Id
    @Column(name = "booking_id") // This IS the Primary Key
    private Long id;

    /**
     * This is the owning side of the relationship.
     * @MapsId tells JPA to use the ID of this 'booking' field
     * as the value for the '@Id' field (this.id) above.
     * This makes the primary key a foreign key.
     */
    @OneToOne(fetch = FetchType.LAZY)
    @MapsId 
    @JoinColumn(name = "booking_id")
    private Booking booking;

    // This field holds the status value (e.g., "PENDING")
    @Column(name = "status_name", nullable = false)
    private String statusName;

    // This field holds the display value (e.g., "Pending")
    @Column(name = "display_name", nullable = false)
    private String displayName;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
    
    // You could add other status-related fields here,
    // e.g., private String notes;

    @PrePersist
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // --- Constructors ---
    public BookingStatus() {
    }

    // Helper constructor
    public BookingStatus(String statusName, String displayName) {
        this.statusName = statusName;
        this.displayName = displayName;
    }

    // --- Getters and Setters ---

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Booking getBooking() {
        return booking;
    }

    public void setBooking(Booking booking) {
        this.booking = booking;
    }

    public String getStatusName() {
        return statusName;
    }

    public void setStatusName(String statusName) {
        this.statusName = statusName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}