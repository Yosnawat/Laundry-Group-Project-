package com.example.demo.service;

import com.example.demo.entity.Rating;
import com.example.demo.entity.User;
import com.example.demo.repository.RatingRepository;
import com.example.demo.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RatingService {
    
    private final RatingRepository ratingRepository;
    private final UserRepository userRepository;
    
    @Transactional
    public Rating submitRating(Long serviceId, Long userId, Integer rating) {
        if (ratingRepository.existsByServiceIdAndUserId(serviceId, userId)) {
            throw new IllegalStateException("Service already rated by this user");
        }
        
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("User not found"));
        
        Rating newRating = new Rating();
        newRating.setServiceId(serviceId);
        newRating.setUser(user);
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