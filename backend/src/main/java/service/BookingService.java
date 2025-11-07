package service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import model.AppConstants;
import model.Booking;
import model.BookingStatus;
import model.Machine;
import repo.BookingRepository;
import repo.MachineRepository;

@Service
public class BookingService {

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private MachineRepository machineRepository; 

    @Transactional(readOnly = true)
    public List<Booking> getAllBookings() {
        return bookingRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Optional<Booking> getBookingById(Long id) {
        return bookingRepository.findById(id);
    }

    @Transactional(readOnly = true)
    public List<Booking> getBookingsByUserId(Long userId) {
        return bookingRepository.findByUserIdWithDetails(userId);
    }

    @Transactional(readOnly = true)
    public List<Booking> getBookingsByMachineId(Long machineId) {
        return bookingRepository.findByMachineId(machineId);
    }

    @Transactional(readOnly = true)
    public List<Booking> getBookingsByStatus(BookingStatus status) {
        return bookingRepository.findByStatus(status);
    }

    public Long getTotalBookings() {
        return bookingRepository.count();
    }

    public Long getBookingCountByStatus(BookingStatus status) {
        return (long) bookingRepository.findByStatus(status).size();
    }

    @Transactional
    public Booking createBooking(Booking booking) {
        if (booking.getUser() == null || booking.getUser().getId() == null) {
            throw new IllegalArgumentException("User ID is required");
        }
        if (booking.getMachine() == null || booking.getMachine().getId() == null) {
            throw new IllegalArgumentException("Machine ID is required");
        }
        if (booking.getBookingDate() == null) {
            throw new IllegalArgumentException("Booking date and time is required");
        }
        boolean isSlotTaken = bookingRepository.existsActiveBookingForMachineAtTime(
                booking.getMachine().getId(),
                booking.getBookingDate());
        if (isSlotTaken) {
            throw new IllegalStateException("This time slot for this machine is already taken.");
        }
        if (booking.getStatus() == null) {
            booking.setStatus(BookingStatus.PENDING);
        }
        return bookingRepository.save(booking);
    }

    @Transactional
    public Booking updateBooking(Long id, Booking bookingDetails) {
        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Booking not found with ID: " + id));
        if (bookingDetails.getBookingDate() != null) {
            booking.setBookingDate(bookingDetails.getBookingDate());
        }
        if (bookingDetails.getStatus() != null) {
            booking.setStatus(bookingDetails.getStatus());
        }
        if (bookingDetails.getAmount() != null) {
            booking.setAmount(bookingDetails.getAmount());
        }
        if (bookingDetails.getService() != null) {
            booking.setService(bookingDetails.getService());
        }
        return bookingRepository.save(booking);
    }

    @Transactional
    public boolean deleteBooking(Long id) {
        if (bookingRepository.existsById(id)) {
            bookingRepository.deleteById(id);
            return true;
        }
        return false;
    }

    @Transactional(readOnly = true)
    public List<Booking> findUpcomingBookingsByUserId(Long userId) {
        // "Upcoming" means any booking that is PENDING or CONFIRMED
        return bookingRepository.findByUserIdAndStatusInWithDetails(
            userId, 
            List.of(BookingStatus.PENDING, BookingStatus.CONFIRMED)
        );
    }

    @Transactional(readOnly = true)
    public List<Booking> getBookingsByDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        return bookingRepository.findActiveBookingsByDateRangeWithMachine(startDate, endDate);
    }

    @Transactional
    public Booking approveBooking(Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new IllegalArgumentException("Booking not found with ID: " + bookingId));

        if (booking.getStatus() == BookingStatus.IN_PROGRESS) {
            return booking;
        }

        if (booking.getStatus() == BookingStatus.PENDING) {
            booking.setStatus(BookingStatus.IN_PROGRESS);
            
            Machine machine = booking.getMachine();
            if (machine != null) {
                machine.setStatus(AppConstants.STATUS_IN_USE);
                machine.setCurrentUser(booking.getUser()); 
                machine.setUsageStartTime(LocalDateTime.now()); 
                machineRepository.save(machine);
            } else {
                 throw new IllegalStateException("Booking is not linked to a machine.");
            }
            
            return bookingRepository.save(booking);
        } else {
            throw new IllegalStateException("ทำรายการไม่สำเร็จ สถานะปัจจุบันคือ: " + booking.getStatus());
        }
    }

    @Transactional
    public Booking completeBooking(Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new IllegalArgumentException("Booking not found with ID: " + bookingId));

        if (booking.getStatus() == BookingStatus.IN_PROGRESS) {
            booking.setStatus(BookingStatus.COMPLETED);

            Machine machine = booking.getMachine();
            if (machine != null) {
                machine.setStatus(AppConstants.STATUS_AVAILABLE);
                machine.setCurrentUser(null); 
                machine.setUsageStartTime(null); 
                machineRepository.save(machine);
            }

            return bookingRepository.save(booking);
        } else {
            throw new IllegalStateException("Only in-progress bookings can be completed");
        }
    }

    @Transactional(readOnly = true)
    public List<Booking> getCompletedBookingsForRating(Long userId) {
        return bookingRepository.findByUserIdAndStatusWithDetails(userId, BookingStatus.COMPLETED);
    }

    @Transactional(readOnly = true)
    public List<Booking> getCompletedBookings() {
        return bookingRepository.findByStatus(BookingStatus.COMPLETED);
    }

    @Transactional(readOnly = true)
    public List<Booking> getInProgressBookings() {
        return bookingRepository.findByStatus(BookingStatus.IN_PROGRESS);
    }

    // --- (NEW) METHOD FOR TIMER PAGE FIX ---
    @Transactional(readOnly = true)
    public Optional<Booking> findActiveBookingByStudentId(String studentId) {
        List<Booking> activeBookings = bookingRepository.findByUserStudentIdAndStatusIn(
            studentId,
            List.of(BookingStatus.PENDING, BookingStatus.IN_PROGRESS)
        );
        
        if (activeBookings.isEmpty()) {
            return Optional.empty();
        } else {
            return Optional.of(activeBookings.get(0));
        }
    }
}