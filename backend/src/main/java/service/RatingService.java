package service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import controller.RatingController.MachineRatingStats;
import model.Booking;
import model.Rating; // This import is still correct (now imports the entity)
import model.User;
import repo.BookingRepository;
import repo.MachineRepository;
import repo.RatingRepository;
import repo.UserRepository;

/**
 * Service for managing machine ratings and reviews.
 * Handles rating submission, validation, statistics, and retrieval operations.
 * Ensures users can only rate their own completed bookings once.
 */
@Service
public class RatingService {
    
    @Autowired
    private RatingRepository ratingRepository;
    
    @Autowired
    private BookingRepository bookingRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private MachineRepository machineRepository;

    /**
     * Submits a new rating for a completed booking.
     * Updates the booking record with the rating score.
     * * Workflow:
     * 1. Validates booking exists and is completed
     * 2. Verifies user owns the booking
     * 3. Checks booking hasn't been rated already
     * 4. Creates rating entity with score and review text
     * 5. Updates booking record with rating score
     * 6. Saves rating and updated booking to database
     * * @param bookingId Booking ID to rate
     * @param userId User ID submitting the rating
     * @param ratingValue Rating score (1-5 stars)
     * @param reviewText Optional review text
     * @return Created rating entity
     * @throws IllegalStateException if booking not found, not completed, not owned by user, or already rated
     */
    @Transactional
    public Rating submitRating(Long bookingId, Long userId, Integer ratingValue, String reviewText) {
        // Validate booking exists and is completed
        Optional<Booking> bookingOpt = bookingRepository.findById(bookingId);
        if (!bookingOpt.isPresent()) {
            throw new IllegalStateException("Booking not found");
        }

        Booking booking = bookingOpt.get();
        
        // --- (MODIFIED) ---
        // We now check the 'statusName' string from the BookingStatus entity.
        if (booking.getStatus() == null || !"COMPLETED".equals(booking.getStatus().getName())) {
        // --- (END OF MODIFICATION) ---
            throw new IllegalStateException("Can only rate completed bookings");
        }

        // Check if user owns the booking
        if (!booking.getUser().getId().equals(userId)) {
            throw new IllegalStateException("Cannot rate other user's booking");
        }

        // Check if already rated
        if (ratingRepository.existsByBookingId(bookingId)) {
            throw new IllegalStateException("Booking already rated");
        }

        // Get entities
        Optional<User> userOpt = userRepository.findById(userId);
        if (!userOpt.isPresent()) {
            throw new IllegalStateException("User not found");
        }

        // Create and save rating
        Rating newRating = new Rating();
        newRating.setUser(userOpt.get());
        newRating.setMachine(booking.getMachine());
        newRating.setBooking(booking);
        newRating.setRating(ratingValue);
        newRating.setReviewText(reviewText);

        // --- (THIS IS THE FIX) ---
        // 1. Update the booking object with the rating score
        booking.setRating(ratingValue);
        // 2. Save the updated booking
        bookingRepository.save(booking);
        // --- (END OF FIX) ---

        return ratingRepository.save(newRating);
    }

    /**
     * Checks if a booking has already been rated.
     * * @param bookingId Booking ID to check
     * @return true if rated, false otherwise
     */
    public boolean isBookingRated(Long bookingId) {
        return ratingRepository.existsByBookingId(bookingId);
    }

    /**
     * Retrieves all ratings for a specific machine.
     * * @param machineId Machine ID to get ratings for
     * @return List of ratings for the machine
     */
    public List<Rating> getRatingsByMachine(Long machineId) {
        return ratingRepository.findByMachineId(machineId);
    }

    /**
     * Retrieves all ratings submitted by a specific user.
     * * @param userId User ID to get ratings for
     * @return List of ratings by the user
     */
    public List<Rating> getRatingsByUser(Long userId) {
        return ratingRepository.findByUserId(userId);
    }

    /**
     * Retrieves the rating for a specific booking.
     * * @param bookingId Booking ID to get rating for
     * @return Optional containing rating if exists
     */
    public Optional<Rating> getRatingByBooking(Long bookingId) {
        return ratingRepository.findByBookingId(bookingId);
    }

    /**
     * Retrieves all ratings in the system.
     * * @return List of all ratings
     */
    public List<Rating> getAllRatings() {
        return ratingRepository.findAll();
    }

    /**
     * Calculates rating statistics for a specific machine.
     * * Workflow:
     * 1. Queries average rating from database
     * 2. Counts total number of ratings
     * 3. Retrieves rating distribution (how many 1-star, 2-star, etc.)
     * 4. Initializes distribution map with 0 counts for all ratings (1-5)
     * 5. Fills in actual counts from database
     * 6. Constructs and returns MachineRatingStats object
     * * @param machineId Machine ID to get statistics for
     * @return MachineRatingStats containing average, total, and distribution
     */
    public MachineRatingStats getMachineRatingStats(Long machineId) {
        Double averageRating = ratingRepository.getAverageRatingByMachineId(machineId);
        Long totalRatings = ratingRepository.countByMachineId(machineId);
        
        // Get rating distribution
        List<Object[]> distributionData = ratingRepository.getRatingDistributionByMachineId(machineId);
        Map<Integer, Long> ratingDistribution = new HashMap<>();
        
        // Initialize all ratings (1-5) with 0 count
        for (int i = 1; i <= 5; i++) {
            ratingDistribution.put(i, 0L);
        }
        
        // Fill actual distribution data
        for (Object[] data : distributionData) {
            Integer rating = (Integer) data[0];
            Long count = (Long) data[1];
            ratingDistribution.put(rating, count);
        }

        return new MachineRatingStats(
            averageRating != null ? averageRating : 0.0,
            totalRatings,
            ratingDistribution
        );
    }

    /**
     * Validates if a user can rate a specific booking.
     * * Checks:
     * 1. Booking exists in database
     * 2. Booking status is COMPLETED
     * 3. User owns the booking
     * 4. Booking has not been rated yet
     * * @param bookingId Booking ID to validate
     * @param userId User ID to validate
     * @return true if all conditions met, false otherwise
     */
    public boolean canRateBooking(Long bookingId, Long userId) {
        // Check if booking exists
        Optional<Booking> bookingOpt = bookingRepository.findById(bookingId);
        if (!bookingOpt.isPresent()) {
            return false;
        }

        Booking booking = bookingOpt.get();
        
        // Check if booking is completed
        // --- (MODIFIED) ---
        // We now check the 'statusName' string from the BookingStatus entity.
        if (booking.getStatus() == null || !"COMPLETED".equals(booking.getStatus().getName())) {
        // --- (END OF MODIFICATION) ---
            return false;
        }

        // Check if user owns the booking
        if (!booking.getUser().getId().equals(userId)) {
            return false;
        }

        // Check if not already rated
        return !ratingRepository.existsByBookingId(bookingId);
    }

    /**
     * Retrieves ratings with review text for a specific machine.
     * Only returns ratings that have non-empty review text.
     * * @param machineId Machine ID to get reviews for
     * @return List of ratings with reviews
     */
    public List<Rating> getRatingsWithReviews(Long machineId) {
        return ratingRepository.findByMachineIdWithReviews(machineId);
    }

    /**
     * Retrieves the most recent ratings across all machines.
     * Useful for displaying recent activity or feedback.
     * * @return List of recent ratings ordered by creation time
     */
    public List<Rating> getRecentRatings() {
        return ratingRepository.findRecentRatings();
    }
}