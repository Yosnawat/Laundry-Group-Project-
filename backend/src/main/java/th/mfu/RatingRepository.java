package th.mfu;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface RatingRepository extends JpaRepository<RatingEntity, Long> {
    List<RatingEntity> findByServiceIdAndUserId(Long serviceId, Long userId);

    boolean existsByServiceIdAndUserId(Long serviceId, Long userId);

    List<RatingEntity> findByServiceId(Long serviceId);
    
    List<RatingEntity> findByUserId(Long userId);
}