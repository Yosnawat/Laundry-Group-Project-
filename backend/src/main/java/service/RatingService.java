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
import model.BookingStatus;
import model.Machine;
import model.Rating;
import model.User;
import repo.BookingRepository;
import repo.MachineRepository;
import repo.RatingRepository;
import repo.UserRepository;

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

    @Transactional
    public Rating submitRating(Long bookingId, Long userId, Integer ratingValue, String reviewText) {
        // Validate booking exists and is completed
        Optional<Booking> bookingOpt = bookingRepository.findById(bookingId);
        if (!bookingOpt.isPresent()) {
            throw new IllegalStateException("Booking not found");
        }

        Booking booking = bookingOpt.get();
        if (booking.getStatus() != BookingStatus.COMPLETED) {
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

        return ratingRepository.save(newRating);
    }

    public boolean isBookingRated(Long bookingId) {
        return ratingRepository.existsByBookingId(bookingId);
    }

    public List<Rating> getRatingsByMachine(Long machineId) {
        return ratingRepository.findByMachineId(machineId);
    }

    public List<Rating> getRatingsByUser(Long userId) {
        return ratingRepository.findByUserId(userId);
    }

    public Optional<Rating> getRatingByBooking(Long bookingId) {
        return ratingRepository.findByBookingId(bookingId);
    }

    public List<Rating> getAllRatings() {
        return ratingRepository.findAll();
    }

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

    public boolean canRateBooking(Long bookingId, Long userId) {
        // Check if booking exists
        Optional<Booking> bookingOpt = bookingRepository.findById(bookingId);
        if (!bookingOpt.isPresent()) {
            return false;
        }

        Booking booking = bookingOpt.get();
        
        // Check if booking is completed
        if (booking.getStatus() != BookingStatus.COMPLETED) {
            return false;
        }

        // Check if user owns the booking
        if (!booking.getUser().getId().equals(userId)) {
            return false;
        }

        // Check if not already rated
        return !ratingRepository.existsByBookingId(bookingId);
    }

    public List<Rating> getRatingsWithReviews(Long machineId) {
        return ratingRepository.findByMachineIdWithReviews(machineId);
    }

    public List<Rating> getRecentRatings() {
        return ratingRepository.findRecentRatings();
    }
}