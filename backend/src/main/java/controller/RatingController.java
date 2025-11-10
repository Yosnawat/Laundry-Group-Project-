package controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import model.Booking;
import model.Rating; // This import is still correct (now imports the entity)
import service.BookingService;
import service.RatingService;

/**
 * REST controller for machine rating and review management.
 * Handles rating submission, retrieval, statistics, and validation.
 */
@RestController
@RequestMapping("/api/ratings")
@CrossOrigin(origins = "*", maxAge = 3600)
public class RatingController {

    @Autowired
    private RatingService ratingService;

    @Autowired
    private BookingService bookingService;

    /**
     * Submits a rating and review for a completed booking.
     * * Workflow:
     * 1. Validates booking exists and is completed
     * 2. Verifies user owns the booking
     * 3. Checks booking hasn't been rated already
     * 4. Validates rating value (1-5)
     * 5. Calls RatingService to save rating
     * 6. Returns safe response (prevents JSON infinite loop)
     * * @param ratingRequest RatingRequest containing bookingId, userId, rating, reviewText
     * @return ResponseEntity with rating ID or error message
     */
    @PostMapping
    public ResponseEntity<?> submitRating(@RequestBody RatingRequest ratingRequest) {
        try {
            // Validate the booking exists and is completed
            Optional<Booking> bookingOpt = bookingService.getBookingById(ratingRequest.getBookingId());
            if (!bookingOpt.isPresent()) {
                return ResponseEntity.badRequest()
                        .body(createErrorResponse("Booking not found", "BOOKING_NOT_FOUND"));
            }

            Booking booking = bookingOpt.get();
            
            // --- (MODIFIED) ---
            // We now check the 'statusName' string from the BookingStatus entity,
            // instead of comparing with the old enum value.
            if (booking.getStatus() == null || !"COMPLETED".equals(booking.getStatus().getName())) {
            // --- (END OF MODIFICATION) ---
                return ResponseEntity.badRequest()
                        .body(createErrorResponse("Can only rate completed bookings", "BOOKING_NOT_COMPLETED"));
            }

            // Check if user owns the booking
            if (!booking.getUser().getId().equals(ratingRequest.getUserId())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(createErrorResponse("Cannot rate other user's booking", "UNAUTHORIZED"));
            }

            // Check if already rated
            if (ratingService.isBookingRated(ratingRequest.getBookingId())) {
                return ResponseEntity.badRequest()
                        .body(createErrorResponse("Booking already rated", "ALREADY_RATED"));
            }

            // Validate rating value
            if (ratingRequest.getRating() < 1 || ratingRequest.getRating() > 5) {
                return ResponseEntity.badRequest()
                        .body(createErrorResponse("Rating must be between 1 and 5", "INVALID_RATING"));
            }

            // Submit the rating
            Rating savedRating = ratingService.submitRating(
                    ratingRequest.getBookingId(),
                    ratingRequest.getUserId(),
                    ratingRequest.getRating(),
                    ratingRequest.getReviewText()
            );

            // --- (THIS IS THE FIX) ---
            // DO NOT return savedRating, it causes an infinite JSON loop.
            // Return a simple, safe map instead.
            Map<String, Object> response = new HashMap<>();
            response.put("id", savedRating.getId());
            response.put("message", "Rating submitted successfully");
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
            // --- (END OF FIX) ---

        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest()
                    .body(createErrorResponse(e.getMessage(), "VALIDATION_ERROR"));
        } catch (Exception e) {
            e.printStackTrace(); // Log the full error
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Internal server error", "SERVER_ERROR"));
        }
    }

    /**
     * Retrieves all ratings for a specific machine.
     * * @param machineId Machine ID to get ratings for
     * @return ResponseEntity with list of ratings
     */
    @GetMapping("/machine/{machineId}")
    public ResponseEntity<List<Rating>> getRatingsByMachine(@PathVariable Long machineId) {
        List<Rating> ratings = ratingService.getRatingsByMachine(machineId);
        return ResponseEntity.ok(ratings);
    }

    /**
     * Retrieves all ratings submitted by a specific user.
     * * @param userId User ID to get ratings for
     * @return ResponseEntity with list of user's ratings
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<Rating>> getRatingsByUser(@PathVariable Long userId) {
        List<Rating> ratings = ratingService.getRatingsByUser(userId);
        return ResponseEntity.ok(ratings);
    }

    /**
     * Retrieves rating statistics for a specific machine.
     * Returns average rating, total rating count, and distribution (1-5 stars).
     * * @param machineId Machine ID to get statistics for
     * @return ResponseEntity with MachineRatingStats
     */
    @GetMapping("/machine/{machineId}/stats")
    public ResponseEntity<MachineRatingStats> getMachineRatingStats(@PathVariable Long machineId) {
        MachineRatingStats stats = ratingService.getMachineRatingStats(machineId);
        return ResponseEntity.ok(stats);
    }

    /**
     * Retrieves all ratings in the system (admin endpoint).
     * * @return ResponseEntity with list of all ratings
     */
    @GetMapping
    public ResponseEntity<List<Rating>> getAllRatings() {
        List<Rating> ratings = ratingService.getAllRatings();
        return ResponseEntity.ok(ratings);
    }

    /**
     * Checks if a user can rate a specific booking.
     * Validates booking is completed, user owns it, and it hasn't been rated.
     * * @param bookingId Booking ID to check
     * @param userId User ID making the request
     * @return ResponseEntity with canRate boolean
     */
    @GetMapping("/booking/{bookingId}/can-rate")
    public ResponseEntity<Map<String, Boolean>> canRateBooking(
            @PathVariable Long bookingId,
            @RequestParam Long userId) {
        
        boolean canRate = ratingService.canRateBooking(bookingId, userId);
        Map<String, Boolean> response = new HashMap<>();
        response.put("canRate", canRate);
        return ResponseEntity.ok(response);
    }

    /**
     * Retrieves the rating for a specific booking.
     * * @param bookingId Booking ID to get rating for
     * @return ResponseEntity with rating or 404 if not found
     */
    @GetMapping("/booking/{bookingId}")
    public ResponseEntity<?> getRatingByBooking(@PathVariable Long bookingId) {
        try {
            Optional<Rating> rating = ratingService.getRatingByBooking(bookingId);
            if (rating.isPresent()) {
                return ResponseEntity.ok(rating.get());
            }
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(createErrorResponse("No rating found for this booking", "RATING_NOT_FOUND"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Error retrieving rating: " + e.getMessage(), "SERVER_ERROR"));
        }
    }
    
    /**
     * Retrieves ratings with review text for a specific machine.
     * Only returns ratings that have non-empty review text.
     * * @param machineId Machine ID to get reviews for
     * @return ResponseEntity with list of ratings with reviews
     */
    @GetMapping("/machine/{machineId}/reviews")
    public ResponseEntity<List<Rating>> getRatingsWithReviews(@PathVariable Long machineId) {
        try {
            List<Rating> ratings = ratingService.getRatingsWithReviews(machineId);
            return ResponseEntity.ok(ratings);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * Retrieves the most recent ratings across all machines.
     * Useful for displaying recent activity or feedback on dashboard.
     * * @return ResponseEntity with list of recent ratings
     */
    @GetMapping("/recent")
    public ResponseEntity<List<Rating>> getRecentRatings() {
        try {
            List<Rating> ratings = ratingService.getRecentRatings();
            return ResponseEntity.ok(ratings);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Helper method to create standardized error responses.
     * * @param message Error message to display
     * @param code Error code for client handling
     * @return Map containing error and code fields
     */
    private Map<String, String> createErrorResponse(String message, String code) {
        Map<String, String> error = new HashMap<>();
        error.put("error", message);
        error.put("code", code);
        return error;
    }

    /**
     * DTO for rating submission requests.
     */
    public static class RatingRequest {
        private Long bookingId;
        private Long userId;
        private Integer rating;
        private String reviewText;

        /**
         * Gets booking ID.
         * @return Booking ID
         */
        public Long getBookingId() { return bookingId; }
        /**
         * Sets booking ID.
         * @param bookingId Booking ID to set
         */
        public void setBookingId(Long bookingId) { this.bookingId = bookingId; }

        /**
         * Gets user ID.
         * @return User ID
         */
        public Long getUserId() { return userId; }
        /**
         * Sets user ID.
         * @param userId User ID to set
         */
        public void setUserId(Long userId) { this.userId = userId; }

        /**
         * Gets rating value.
         * @return Rating value (1-5)
         */
        public Integer getRating() { return rating; }
        /**
         * Sets rating value.
         * @param rating Rating value to set
         */
        public void setRating(Integer rating) { this.rating = rating; }

        /**
         * Gets review text.
         * @return Review text
         */
        public String getReviewText() { return reviewText; }
        /**
         * Sets review text.
         * @param reviewText Review text to set
         */
        public void setReviewText(String reviewText) { this.reviewText = reviewText; }
    }

    /**
     * DTO for machine rating statistics.
     * Contains average rating, total count, and distribution by star rating.
     */
    public static class MachineRatingStats {
        private Double averageRating;
        private Long totalRatings;
        private Map<Integer, Long> ratingDistribution;

        /**
         * Constructs MachineRatingStats with all fields.
         * * @param averageRating Average rating value
         * @param totalRatings Total number of ratings
         * @param ratingDistribution Map of rating value to count
         */
        public MachineRatingStats(Double averageRating, Long totalRatings, Map<Integer, Long> ratingDistribution) {
            this.averageRating = averageRating;
            this.totalRatings = totalRatings;
            this.ratingDistribution = ratingDistribution;
        }

        /**
         * Gets average rating.
         * @return Average rating
         */
        public Double getAverageRating() { return averageRating; }
        /**
         * Sets average rating.
         * @param averageRating Average rating to set
         */
        public void setAverageRating(Double averageRating) { this.averageRating = averageRating; }

        /**
         * Gets total ratings count.
         * @return Total ratings count
         */
        public Long getTotalRatings() { return totalRatings; }
        /**
         * Sets total ratings count.
         * @param totalRatings Total ratings to set
         */
        public void setTotalRatings(Long totalRatings) { this.totalRatings = totalRatings; }

        /**
         * Gets rating distribution map.
         * @return Map of rating value (1-5) to count
         */
        public Map<Integer, Long> getRatingDistribution() { return ratingDistribution; }
        /**
         * Sets rating distribution map.
         * @param ratingDistribution Rating distribution to set
         */
        public void setRatingDistribution(Map<Integer, Long> ratingDistribution) { this.ratingDistribution = ratingDistribution; }
    }
}