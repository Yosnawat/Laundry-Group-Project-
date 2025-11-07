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
import model.BookingStatus;
import model.Rating;
import service.BookingService;
import service.RatingService;

@RestController
@RequestMapping("/api/ratings")
@CrossOrigin(origins = "*", maxAge = 3600)
public class RatingController {

    @Autowired
    private RatingService ratingService;

    @Autowired
    private BookingService bookingService;

    // Submit a rating and review for a completed booking
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
            if (booking.getStatus() != BookingStatus.COMPLETED) {
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

    // Get all ratings for a specific machine
    @GetMapping("/machine/{machineId}")
    public ResponseEntity<List<Rating>> getRatingsByMachine(@PathVariable Long machineId) {
        List<Rating> ratings = ratingService.getRatingsByMachine(machineId);
        return ResponseEntity.ok(ratings);
    }

    // Get all ratings by a specific user
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<Rating>> getRatingsByUser(@PathVariable Long userId) {
        List<Rating> ratings = ratingService.getRatingsByUser(userId);
        return ResponseEntity.ok(ratings);
    }

    // Get rating statistics for a machine
    @GetMapping("/machine/{machineId}/stats")
    public ResponseEntity<MachineRatingStats> getMachineRatingStats(@PathVariable Long machineId) {
        MachineRatingStats stats = ratingService.getMachineRatingStats(machineId);
        return ResponseEntity.ok(stats);
    }

    // Get all ratings (for admin purposes)
    @GetMapping
    public ResponseEntity<List<Rating>> getAllRatings() {
        List<Rating> ratings = ratingService.getAllRatings();
        return ResponseEntity.ok(ratings);
    }

    // Check if a booking can be rated
    @GetMapping("/booking/{bookingId}/can-rate")
    public ResponseEntity<Map<String, Boolean>> canRateBooking(
            @PathVariable Long bookingId,
            @RequestParam Long userId) {
        
        boolean canRate = ratingService.canRateBooking(bookingId, userId);
        Map<String, Boolean> response = new HashMap<>();
        response.put("canRate", canRate);
        return ResponseEntity.ok(response);
    }

    // Get rating for a specific booking
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
    
    // Get ratings with reviews for a specific machine
    @GetMapping("/machine/{machineId}/reviews")
    public ResponseEntity<List<Rating>> getRatingsWithReviews(@PathVariable Long machineId) {
        try {
            List<Rating> ratings = ratingService.getRatingsWithReviews(machineId);
            return ResponseEntity.ok(ratings);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    // Get recent ratings across the system
    @GetMapping("/recent")
    public ResponseEntity<List<Rating>> getRecentRatings() {
        try {
            List<Rating> ratings = ratingService.getRecentRatings();
            return ResponseEntity.ok(ratings);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // Helper method to create error response
    private Map<String, String> createErrorResponse(String message, String code) {
        Map<String, String> error = new HashMap<>();
        error.put("error", message);
        error.put("code", code);
        return error;
    }

    // Inner classes for request/response DTOs
    public static class RatingRequest {
        private Long bookingId;
        private Long userId;
        private Integer rating;
        private String reviewText;

        // Getters and setters
        public Long getBookingId() { return bookingId; }
        public void setBookingId(Long bookingId) { this.bookingId = bookingId; }

        public Long getUserId() { return userId; }
        public void setUserId(Long userId) { this.userId = userId; }

        public Integer getRating() { return rating; }
        public void setRating(Integer rating) { this.rating = rating; }

        public String getReviewText() { return reviewText; }
        public void setReviewText(String reviewText) { this.reviewText = reviewText; }
    }

    public static class MachineRatingStats {
        private Double averageRating;
        private Long totalRatings;
        private Map<Integer, Long> ratingDistribution;

        public MachineRatingStats(Double averageRating, Long totalRatings, Map<Integer, Long> ratingDistribution) {
            this.averageRating = averageRating;
            this.totalRatings = totalRatings;
            this.ratingDistribution = ratingDistribution;
        }

        // Getters and setters
        public Double getAverageRating() { return averageRating; }
        public void setAverageRating(Double averageRating) { this.averageRating = averageRating; }

        public Long getTotalRatings() { return totalRatings; }
        public void setTotalRatings(Long totalRatings) { this.totalRatings = totalRatings; }

        public Map<Integer, Long> getRatingDistribution() { return ratingDistribution; }
        public void setRatingDistribution(Map<Integer, Long> ratingDistribution) { this.ratingDistribution = ratingDistribution; }
    }
}