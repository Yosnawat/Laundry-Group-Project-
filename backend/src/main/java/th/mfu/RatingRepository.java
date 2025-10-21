package com.example.demo.repository;

import com.example.demo.entity.Rating;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

package com.example.demo.repository;

import com.example.demo.entity.Rating;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface RatingRepository extends JpaRepository<Rating, Long> {
    List<Rating> findByServiceIdAndUserId(Long serviceId, Long userId);

    boolean existsByServiceIdAndUserId(Long serviceId, Long userId);

    List<Rating> findByServiceId(Long serviceId);
    
    List<Rating> findByUserId(Long userId);
}