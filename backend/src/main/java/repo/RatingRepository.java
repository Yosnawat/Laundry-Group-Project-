package repo;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import model.Rating;

@Repository
public interface RatingRepository extends JpaRepository<Rating, Long> {
    
    // Find ratings by machine
    @Query("SELECT r FROM Rating r WHERE r.machine.id = :machineId ORDER BY r.createdAt DESC")
    List<Rating> findByMachineId(@Param("machineId") Long machineId);
    
    // Find ratings by user
    @Query("SELECT r FROM Rating r WHERE r.user.id = :userId ORDER BY r.createdAt DESC")
    List<Rating> findByUserId(@Param("userId") Long userId);
    
    // Find rating by booking
    @Query("SELECT r FROM Rating r WHERE r.booking.id = :bookingId")
    Optional<Rating> findByBookingId(@Param("bookingId") Long bookingId);
    
    // Check if booking is already rated
    @Query("SELECT COUNT(r) > 0 FROM Rating r WHERE r.booking.id = :bookingId")
    boolean existsByBookingId(@Param("bookingId") Long bookingId);
    
    // Get average rating for a machine
    @Query("SELECT AVG(r.rating) FROM Rating r WHERE r.machine.id = :machineId")
    Double getAverageRatingByMachineId(@Param("machineId") Long machineId);
    
    // Count ratings for a machine
    @Query("SELECT COUNT(r) FROM Rating r WHERE r.machine.id = :machineId")
    Long countByMachineId(@Param("machineId") Long machineId);
    
    // Get rating distribution for a machine
    @Query("SELECT r.rating, COUNT(r) FROM Rating r WHERE r.machine.id = :machineId GROUP BY r.rating")
    List<Object[]> getRatingDistributionByMachineId(@Param("machineId") Long machineId);
    
    // Get ratings with reviews for a machine
    @Query("SELECT r FROM Rating r WHERE r.machine.id = :machineId AND r.reviewText IS NOT NULL AND r.reviewText != '' ORDER BY r.createdAt DESC")
    List<Rating> findByMachineIdWithReviews(@Param("machineId") Long machineId);
    
    // Get recent ratings (last N ratings)
    @Query("SELECT r FROM Rating r ORDER BY r.createdAt DESC")
    List<Rating> findRecentRatings();
}