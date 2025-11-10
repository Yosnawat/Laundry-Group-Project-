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
    
    // --- (MODIFIED) ---
    // This now queries by the 'statusName' field inside the 'status' entity
    List<Booking> findByStatus_StatusName(String statusName);
    
    // --- (MODIFIED) ---
    // This also queries by the 'statusName' field
    List<Booking> findByUserIdAndStatus_StatusName(Long userId, String statusName);
    // --- (END OF MODIFICATION) ---
    
    List<Booking> findByBookingDateBetween(LocalDateTime startDate, LocalDateTime endDate);

    // --- (MODIFIED) ---
    @Query("SELECT b FROM Booking b JOIN FETCH b.machine m JOIN FETCH b.user u " +
           "WHERE b.user.id = :userId AND b.status.statusName IN :statusNames " + // Changed b.status to b.status.statusName
           "ORDER BY b.bookingDate ASC")
    List<Booking> findByUserIdAndStatusInWithDetails(
        @Param("userId") Long userId, 
        @Param("statusNames") List<String> statusNames // Changed parameter type
    );
    // --- (END OF MODIFICATION) ---

    // --- (MODIFIED) ---
    @Query("SELECT COUNT(b) > 0 FROM Booking b " +
           "WHERE b.machine.id = :machineId " +
           "AND b.bookingDate = :bookingDate " +
           "AND b.status.statusName IN ('PENDING', 'CONFIRMED', 'IN_PROGRESS')") // Changed b.status to b.status.statusName
    boolean existsActiveBookingForMachineAtTime(
        @Param("machineId") Long machineId, 
        @Param("bookingDate") LocalDateTime bookingDate
    );
    // --- (END OF MODIFICATION) ---

    // --- (MODIFIED) ---
    @Query("SELECT b FROM Booking b JOIN FETCH b.machine m JOIN FETCH b.user u " +
           "WHERE b.bookingDate BETWEEN :startDate AND :endDate " +
           "AND b.status.statusName IN ('PENDING', 'CONFIRMED', 'IN_PROGRESS')") // Changed b.status to b.status.statusName
    List<Booking> findActiveBookingsByDateRangeWithMachine(
        @Param("startDate") LocalDateTime startDate, 
        @Param("endDate") LocalDateTime endDate
    );
    // --- (END OF MODIFICATION) ---

    // --- (MODIFIED) ---
    @Query("SELECT b FROM Booking b JOIN FETCH b.machine m JOIN FETCH b.user u " +
           "WHERE b.user.id = :userId AND b.status.statusName = :statusName " + // Changed b.status to b.status.statusName
           "ORDER BY b.bookingDate DESC")
    List<Booking> findByUserIdAndStatusWithDetails(
        @Param("userId") Long userId, 
        @Param("statusName") String statusName // Changed parameter type
    );
    // --- (END OF MODIFICATION) ---

    
    
    // --- (NEW) METHOD FOR TIMER PAGE FIX ---
    // --- (MODIFIED) ---
    // Changed List<BookingStatus> to List<String>
    // Added @Query to be explicit about the nested property query
    @Query("SELECT b FROM Booking b WHERE b.user.studentId = :studentId AND b.status.statusName IN :statusNames")
    List<Booking> findByUserStudentIdAndStatusIn(
        @Param("studentId") String studentId, 
        @Param("statusNames") List<String> statusNames
    );
    // --- (END OF MODIFICATION) ---
}