package repo;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import model.Booking;
import model.BookingStatus;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {

    @Override
    @Query("SELECT b FROM Booking b JOIN FETCH b.machine m JOIN FETCH b.user u")
    List<Booking> findAll();

    @Query("SELECT b FROM Booking b JOIN FETCH b.machine JOIN FETCH b.user WHERE b.user.id = :userId")
    List<Booking> findByUserIdWithDetails(@Param("userId") Long userId);

    List<Booking> findByUserId(Long userId); 
    List<Booking> findByMachineId(Long machineId);
    List<Booking> findByStatus(BookingStatus status);
    
    List<Booking> findByUserIdAndStatus(Long userId, BookingStatus status);
    List<Booking> findByBookingDateBetween(LocalDateTime startDate, LocalDateTime endDate);

    @Query("SELECT b FROM Booking b JOIN FETCH b.machine m JOIN FETCH b.user u " +
           "WHERE b.user.id = :userId AND b.status IN :statuses " +
           "ORDER BY b.bookingDate ASC")
    List<Booking> findByUserIdAndStatusInWithDetails(
        @Param("userId") Long userId, 
        @Param("statuses") List<BookingStatus> statuses
    );

    @Query("SELECT COUNT(b) > 0 FROM Booking b " +
           "WHERE b.machine.id = :machineId " +
           "AND b.bookingDate = :bookingDate " +
           "AND b.status IN ('PENDING', 'CONFIRMED', 'IN_PROGRESS')")
    boolean existsActiveBookingForMachineAtTime(
        @Param("machineId") Long machineId, 
        @Param("bookingDate") LocalDateTime bookingDate
    );

    @Query("SELECT b FROM Booking b JOIN FETCH b.machine m JOIN FETCH b.user u " +
           "WHERE b.bookingDate BETWEEN :startDate AND :endDate " +
           "AND b.status IN ('PENDING', 'CONFIRMED', 'IN_PROGRESS')")
    List<Booking> findActiveBookingsByDateRangeWithMachine(
        @Param("startDate") LocalDateTime startDate, 
        @Param("endDate") LocalDateTime endDate
    );

    @Query("SELECT b FROM Booking b JOIN FETCH b.machine m JOIN FETCH b.user u " +
           "WHERE b.user.id = :userId AND b.status = :status " +
           "ORDER BY b.bookingDate DESC")
    List<Booking> findByUserIdAndStatusWithDetails(
        @Param("userId") Long userId, 
        @Param("status") BookingStatus status
    );

    
    
    // --- (NEW) METHOD FOR TIMER PAGE FIX ---
    List<Booking> findByUserStudentIdAndStatusIn(String studentId, List<BookingStatus> statuses);
}