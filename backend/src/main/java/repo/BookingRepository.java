package repo;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import model.Booking;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {

    @Override
    @Query("SELECT b FROM Booking b JOIN FETCH b.machine m JOIN FETCH b.user u")
    List<Booking> findAll();

    @Query("SELECT b FROM Booking b JOIN FETCH b.machine JOIN FETCH b.user WHERE b.user.id = :userId")
    List<Booking> findByUserIdWithDetails(@Param("userId") Long userId);

    List<Booking> findByUserId(Long userId); 
    List<Booking> findByMachineId(Long machineId);
    
    // --- (FIXED) ---
    // This queries by the 'name' field inside the 'status' entity
    List<Booking> findByStatus_Name(String name);
    
    // --- (FIXED) ---
    // This also queries by the 'name' field
    List<Booking> findByUserIdAndStatus_Name(Long userId, String name);
    // --- (END OF FIX) ---
    
    List<Booking> findByBookingDateBetween(LocalDateTime startDate, LocalDateTime endDate);

    // --- (FIXED) ---
    @Query("SELECT b FROM Booking b JOIN FETCH b.machine m JOIN FETCH b.user u " +
           "WHERE b.user.id = :userId AND b.status.name IN :statusNames " + // Changed b.status.statusName to b.status.name
           "ORDER BY b.bookingDate ASC")
    List<Booking> findByUserIdAndStatusInWithDetails(
        @Param("userId") Long userId, 
        @Param("statusNames") List<String> statusNames
    );
    // --- (END OF FIX) ---

    // --- (FIXED) ---
    @Query("SELECT COUNT(b) > 0 FROM Booking b " +
           "WHERE b.machine.id = :machineId " +
           "AND b.bookingDate = :bookingDate " +
           "AND b.status.name IN ('PENDING', 'CONFIRMED', 'IN_PROGRESS')") // Changed b.status.statusName to b.status.name
    boolean existsActiveBookingForMachineAtTime(
        @Param("machineId") Long machineId, 
        @Param("bookingDate") LocalDateTime bookingDate
    );
    // --- (END OF FIX) ---

    // --- (FIXED) ---
    @Query("SELECT b FROM Booking b JOIN FETCH b.machine m JOIN FETCH b.user u " +
           "WHERE b.bookingDate BETWEEN :startDate AND :endDate " +
           "AND b.status.name IN ('PENDING', 'CONFIRMED', 'IN_PROGRESS')") // Changed b.status.statusName to b.status.name
    List<Booking> findActiveBookingsByDateRangeWithMachine(
        @Param("startDate") LocalDateTime startDate, 
        @Param("endDate") LocalDateTime endDate
    );
    // --- (END OF FIX) ---

    // --- (FIXED) ---
    @Query("SELECT b FROM Booking b JOIN FETCH b.machine m JOIN FETCH b.user u " +
           "WHERE b.user.id = :userId AND b.status.name = :statusName " + // Changed b.status.statusName to b.status.name
           "ORDER BY b.bookingDate DESC")
    List<Booking> findByUserIdAndStatusWithDetails(
        @Param("userId") Long userId, 
        @Param("statusName") String statusName // Parameter name is fine, query is fixed
    );
    // --- (END OF FIX) ---

    
    
    // --- (FIXED) METHOD FOR TIMER PAGE FIX ---
    @Query("SELECT b FROM Booking b WHERE b.user.studentId = :studentId AND b.status.name IN :statusNames") // Changed b.status.statusName to b.status.name
    List<Booking> findByUserStudentIdAndStatusIn(
        @Param("studentId") String studentId, 
        @Param("statusNames") List<String> statusNames
    );
    // --- (END OF FIX) ---
}