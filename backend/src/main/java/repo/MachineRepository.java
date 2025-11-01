package repo;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import model.Machine;

@Repository
public interface MachineRepository extends JpaRepository<Machine, Long> {

    List<Machine> findByStatus(String status);

    Machine findByMachineNumber(String machineNumber);

    List<Machine> findByCurrentUserId(Long userId);
    
    // Find available machines by location
    @Query("SELECT m FROM Machine m WHERE m.status = :status AND m.location = :location ORDER BY m.id ASC")
    List<Machine> findAvailableMachinesByLocation(@Param("status") String status, @Param("location") String location);
    
    // Find available machines ordered by ID
    @Query("SELECT m FROM Machine m WHERE m.status = :status ORDER BY m.id ASC")
    List<Machine> findAvailableMachinesOrdered(@Param("status") String status);

    // Find by location
    @Query("SELECT m FROM Machine m WHERE m.location = :location ORDER BY m.name ASC")
    List<Machine> findByLocation(@Param("location") String location);

    // Find by machine type
    @Query("SELECT m FROM Machine m WHERE m.machineType = :type ORDER BY m.name ASC")
    List<Machine> findByMachineType(@Param("type") String type);

    // Search with multiple criteria
    @Query("SELECT m FROM Machine m WHERE " +
           "(:name IS NULL OR LOWER(m.name) LIKE LOWER(CONCAT('%', :name, '%'))) AND " +
           "(:location IS NULL OR LOWER(m.location) LIKE LOWER(CONCAT('%', :location, '%'))) AND " +
           "(:type IS NULL OR m.machineType = :type) AND " +
           "(:minPrice IS NULL OR m.pricePerHour >= :minPrice) AND " +
           "(:maxPrice IS NULL OR m.pricePerHour <= :maxPrice) AND " +
           "(:status IS NULL OR m.status = :status) " +
           "ORDER BY m.name ASC")
    List<Machine> searchMachines(
        @Param("name") String name,
        @Param("location") String location,
        @Param("type") String type,
        @Param("minPrice") Double minPrice,
        @Param("maxPrice") Double maxPrice,
        @Param("status") String status
    );

    // Find machines in price range
    @Query("SELECT m FROM Machine m WHERE m.pricePerHour BETWEEN :minPrice AND :maxPrice ORDER BY m.pricePerHour ASC")
    List<Machine> findByPriceRange(@Param("minPrice") Double minPrice, @Param("maxPrice") Double maxPrice);
}
