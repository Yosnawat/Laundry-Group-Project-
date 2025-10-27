package service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import model.Rating;
import repo.RatingRepository;

@Service
public class RatingService {
    private final RatingRepository ratingRepository;

    public RatingService(RatingRepository ratingRepository) {
        this.ratingRepository = ratingRepository;
    }

    @Transactional
    public Rating submitRating(Long serviceId, Long userId, Integer rating) {
        if (ratingRepository.existsByServiceIdAndUserId(serviceId, userId)) {
            throw new IllegalStateException("Service already rated by this user");
        }

        Rating newRating = new Rating();
        newRating.setServiceId(serviceId);
        newRating.setUserId(userId);
        newRating.setRating(rating);
        return ratingRepository.save(newRating);
    }

    public boolean isServiceRated(Long serviceId, Long userId) {
        return ratingRepository.existsByServiceIdAndUserId(serviceId, userId);
    }

    public List<Rating> getRatingsByService(Long serviceId) {
        return ratingRepository.findByServiceId(serviceId);
    }

    public List<Rating> getRatingsByUser(Long userId) {
        return ratingRepository.findByUserId(userId);
    }
}