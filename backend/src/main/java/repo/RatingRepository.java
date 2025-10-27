package repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import model.Rating;


@Repository
public interface RatingRepository extends JpaRepository<Rating, Long> {
    List<Rating> findByServiceIdAndUserId(Long serviceId, Long userId);

    boolean existsByServiceIdAndUserId(Long serviceId, Long userId);

    List<Rating> findByServiceId(Long serviceId);
    
    List<Rating> findByUserId(Long userId);
}