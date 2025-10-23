package th.mfu;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
public class RatingService {
    private final RatingRepository ratingRepository;

    public RatingService(RatingRepository ratingRepository) {
        this.ratingRepository = ratingRepository;
    }

    @Transactional
    public RatingEntity submitRating(Long serviceId, Long userId, Integer rating) {
        if (ratingRepository.existsByServiceIdAndUserId(serviceId, userId)) {
            throw new IllegalStateException("Service already rated by this user");
        }

        RatingEntity newRating = new RatingEntity();
        newRating.setServiceId(serviceId);
        newRating.setUserId(userId);
        newRating.setRating(rating);
        return ratingRepository.save(newRating);
    }

    public boolean isServiceRated(Long serviceId, Long userId) {
        return ratingRepository.existsByServiceIdAndUserId(serviceId, userId);
    }

    public List<RatingEntity> getRatingsByService(Long serviceId) {
        return ratingRepository.findByServiceId(serviceId);
    }

    public List<RatingEntity> getRatingsByUser(Long userId) {
        return ratingRepository.findByUserId(userId);
    }
}