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
import model.User; // --- 1. IMPORT USER ---
import repo.BookingRepository;
import repo.MachineRepository;
import repo.UserRepository; // --- 2. IMPORT USER REPOSITORY ---

@Service
public class BookingService {

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private MachineRepository machineRepository; 

    // --- 3. INJECT THE USER REPOSITORY ---
    @Autowired
    private UserRepository userRepository;

    /**
     * (No changes)
     */
    @Transactional(readOnly = true)
    public List<Booking> getAllBookings() {
        return bookingRepository.findAll();
    }

    /**
     * (No changes)
     */
    @Transactional(readOnly = true)
    public Optional<Booking> getBookingById(Long id) {
        return bookingRepository.findById(id);
    }

    /**
     * (No changes)
     */
    @Transactional(readOnly = true)
    public List<Booking> getBookingsByUserId(Long userId) {
        return bookingRepository.findByUserIdWithDetails(userId);
    }

    /**
     * (No changes)
     */
    @Transactional(readOnly = true)
    public List<Booking> getBookingsByMachineId(Long machineId) {
        return bookingRepository.findByMachineId(machineId);
    }

    /**
     * (This is correct from our previous fix)
     */
    @Transactional(readOnly = true)
    public List<Booking> getBookingsByStatus(String statusName) {
        return bookingRepository.findByStatus_Name(statusName);
    }

    /**
     * (No changes)
     */
    public Long getTotalBookings() {
        return bookingRepository.count();
    }

    /**
     * (This is correct from our previous fix)
     */
    public Long getBookingCountByStatus(String statusName) {
        return (long) bookingRepository.findByStatus_Name(statusName).size();
    }

    /**
     * ---
     * --- (THIS IS THE FIX) ---
     * ---
     * Creates a new booking with validation.
     * We can't save the 'booking' object from the request directly.
     * We must build a new, managed entity.
     */
    @Transactional
    public Booking createBooking(Booking bookingRequest) {
        
        // 1. Get IDs from the request object
        Long userId = bookingRequest.getUser().getId();
        Long machineId = bookingRequest.getMachine().getId();

        if (userId == null) {
            throw new IllegalArgumentException("User ID is required");
        }
        if (machineId == null) {
            throw new IllegalArgumentException("Machine ID is required");
        }
        if (bookingRequest.getBookingDate() == null) {
            throw new IllegalArgumentException("Booking date and time is required");
        }

        // 2. Load the REAL, MANAGED entities from the database
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found with ID: " + userId));
        
        Machine machine = machineRepository.findById(machineId)
                .orElseThrow(() -> new IllegalArgumentException("Machine not found with ID: " + machineId));

        // 3. Check for conflicts
        boolean isSlotTaken = bookingRepository.existsActiveBookingForMachineAtTime(
                machineId,
                bookingRequest.getBookingDate());
        
        if (isSlotTaken) {
            throw new IllegalStateException("This time slot for this machine is already taken.");
        }
        
        // 4. Create the new, valid Booking object
        Booking newBooking = new Booking();
        newBooking.setUser(user); // Set the managed User
        newBooking.setMachine(machine); // Set the managed Machine
        newBooking.setBookingDate(bookingRequest.getBookingDate());
        newBooking.setAmount(bookingRequest.getAmount());
        newBooking.setService(bookingRequest.getService());

        // 5. Create the default status
        BookingStatus defaultStatus = new BookingStatus("PENDING", "Pending");
        newBooking.setStatus(defaultStatus);
        
        // 6. Save the new, fully managed entity
        return bookingRepository.save(newBooking);
    }
    // --- (END OF FIX) ---


    /**
     * (No changes)
     */
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

    /**
     * (No changes)
     */
    @Transactional
    public boolean deleteBooking(Long id) {
        if (bookingRepository.existsById(id)) {
            bookingRepository.deleteById(id);
            return true;
        }
        return false;
    }

    /**
     * (No changes)
     */
    @Transactional(readOnly = true)
    public List<Booking> getBookingsByDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        return bookingRepository.findActiveBookingsByDateRangeWithMachine(startDate, endDate);
    }

    /**
     * (No changes)
     */
    @Transactional
    public Booking approveBooking(Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new IllegalArgumentException("Booking not found with ID: " + bookingId));

        BookingStatus status = booking.getStatus();
        if (status == null) {
            throw new IllegalStateException("Booking " + bookingId + " has no status.");
        }

        if ("IN_PROGRESS".equals(status.getName())) {
            return booking;
        }

        if ("PENDING".equals(status.getName())) {
            status.setName("IN_PROGRESS");
            status.setDisplayName("In Progress");
            
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
            throw new IllegalStateException("ทำรายการไม่สำเร็จ สถานะปัจจุบันคือ: " + status.getDisplayName());
        }
    }

    /**
     * (No changes)
     */
    @Transactional
    public Booking completeBooking(Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new IllegalArgumentException("Booking not found with ID: " + bookingId));

        BookingStatus status = booking.getStatus();
        if (status == null) {
            throw new IllegalStateException("Booking " + bookingId + " has no status.");
        }

        if ("IN_PROGRESS".equals(status.getName())) {
            status.setName("COMPLETED");
            status.setDisplayName("Completed");

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

    /**
     * (No changes)
     */
    @Transactional(readOnly = true)
    public List<Booking> getCompletedBookingsForRating(Long userId) {
        return bookingRepository.findByUserIdAndStatusWithDetails(userId, "COMPLETED");
    }

    /**
     * (This is correct from our previous fix)
     */
    @Transactional(readOnly = true)
    public List<Booking> getCompletedBookings() {
        return bookingRepository.findByStatus_Name("COMPLETED");
    }

    /**
     * (This is correct from our previous fix)
     */
    @Transactional(readOnly = true)
    public List<Booking> getInProgressBookings() {
        return bookingRepository.findByStatus_Name("IN_PROGRESS");
    }

    /**
     * (No changes)
     */
    @Transactional(readOnly = true)
    public Optional<Booking> findActiveBookingByStudentId(String studentId) {
        List<Booking> activeBookings = bookingRepository.findByUserStudentIdAndStatusIn(
                studentId,
                List.of("PENDING", "IN_PROGRESS")
        );
        
        if (activeBookings.isEmpty()) {
            return Optional.empty();
        } else {
            return Optional.of(activeBookings.get(0));
        }
    }
}