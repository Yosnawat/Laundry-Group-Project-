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

    /**
     * Retrieves all bookings from the database.
     * @return List of all bookings
     */
    @Transactional(readOnly = true)
    public List<Booking> getAllBookings() {
        return bookingRepository.findAll();
    }

    /**
     * Finds a specific booking by ID.
     * @param id Booking ID
     * @return Optional containing the booking if found
     */
    @Transactional(readOnly = true)
    public Optional<Booking> getBookingById(Long id) {
        return bookingRepository.findById(id);
    }

    /**
     * Retrieves all bookings for a specific user with full details.
     * @param userId User ID
     * @return List of bookings for the user
     */
    @Transactional(readOnly = true)
    public List<Booking> getBookingsByUserId(Long userId) {
        return bookingRepository.findByUserIdWithDetails(userId);
    }

    /**
     * Retrieves all bookings for a specific machine.
     * @param machineId Machine ID
     * @return List of bookings for the machine
     */
    @Transactional(readOnly = true)
    public List<Booking> getBookingsByMachineId(Long machineId) {
        return bookingRepository.findByMachineId(machineId);
    }

    /**
     * Retrieves all bookings with a specific status.
     * @param statusName Booking status name (e.g., "PENDING") to filter by
     * @return List of bookings with the specified status
     */
    @Transactional(readOnly = true)
    // --- (MODIFIED) ---
    // The signature is changed from (BookingStatus status) to (String statusName)
    // to match the controller and new entity structure.
    public List<Booking> getBookingsByStatus(String statusName) {
        // This now queries by the 'statusName' field *within* the BookingStatus entity
        return bookingRepository.findByStatus_StatusName(statusName);
    }
    // --- (END OF MODIFICATION) ---

    /**
     * Counts the total number of bookings.
     * @return Total booking count
     */
    public Long getTotalBookings() {
        return bookingRepository.count();
    }

    /**
     * Counts bookings with a specific status.
     * @param statusName Booking status name (e.g., "PENDING") to count
     * @return Number of bookings with the status
     */
    // --- (MODIFIED) ---
    // The signature is changed from (BookingStatus status) to (String statusName)
    public Long getBookingCountByStatus(String statusName) {
        // This preserves your original logic of .size() on the findByStatus method.
        // The repository method is now 'findByStatus_StatusName'
        return (long) bookingRepository.findByStatus_StatusName(statusName).size();
        // NOTE: A more efficient way is to use a count query in the repository:
        // return bookingRepository.countByStatus_StatusName(statusName);
    }
    // --- (END OF MODIFICATION) ---

    /**
     * Creates a new booking with validation.
     * Workflow:
     * 1. Validates user and machine IDs are provided
     * 2. Validates booking date is provided
     * 3. Checks if time slot is already taken
     * 4. Sets status to PENDING if not provided
     * 5. Saves booking to database
     * * @param booking Booking object to create
     * @return Created booking
     * @throws IllegalArgumentException if validation fails
     * @throws IllegalStateException if time slot is already taken
     */
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
        
        // --- (MODIFIED) ---
        // We can no longer set 'BookingStatus.PENDING'.
        // We must create the new dependent BookingStatus entity.
        if (booking.getStatus() == null) {
            // Create the new entity
            BookingStatus defaultStatus = new BookingStatus("PENDING", "Pending");
            // Link it to the booking (the helper method in Booking.java handles both sides)
            booking.setStatus(defaultStatus);
        }
        // --- (END OF MODIFICATION) ---
        
        return bookingRepository.save(booking);
    }

    /**
     * Updates an existing booking with new details.
     * Only updates fields that are non-null in the bookingDetails parameter.
     * * @param id ID of booking to update
     * @param bookingDetails Booking object containing updated fields
     * @return Updated booking
     * @throws IllegalArgumentException if booking not found
     */
    @Transactional
    public Booking updateBooking(Long id, Booking bookingDetails) {
        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Booking not found with ID: " + id));
        if (bookingDetails.getBookingDate() != null) {
            booking.setBookingDate(bookingDetails.getBookingDate());
        }
        
        // --- (NO CHANGE NEEDED HERE) ---
        // This logic is still correct. If the request body provides a new 'status' object,
        // the booking.setStatus() helper will link it, and CascadeType.ALL + orphanRemoval
        // will replace the old status entity in the database.
        if (bookingDetails.getStatus() != null) {
            booking.setStatus(bookingDetails.getStatus());
        }
        // --- (END OF NO CHANGE) ---
        
        if (bookingDetails.getAmount() != null) {
            booking.setAmount(bookingDetails.getAmount());
        }
        if (bookingDetails.getService() != null) {
            booking.setService(bookingDetails.getService());
        }
        return bookingRepository.save(booking);
    }

    /**
     * Deletes a booking by ID.
     * @param id Booking ID to delete
     * @return true if deleted, false if booking not found
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
     * Retrieves bookings within a date range.
     * @param startDate Start of date range
     * @param endDate End of date range
     * @return List of bookings in the date range
     */
    @Transactional(readOnly = true)
    public List<Booking> getBookingsByDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        return bookingRepository.findActiveBookingsByDateRangeWithMachine(startDate, endDate);
    }

    /**
     * Approves a pending booking and starts machine usage.
     * Workflow:
     * 1. Finds booking by ID
     * 2. Returns immediately if already IN_PROGRESS
     * 3. If PENDING, changes status to IN_PROGRESS
     * 4. Updates machine status to IN_USE
     * 5. Sets current user and usage start time on machine
     * 6. Saves both booking and machine
     * * @param bookingId ID of booking to approve
     * @return Updated booking
     * @throws IllegalArgumentException if booking not found
     * @throws IllegalStateException if booking status is not PENDING or IN_PROGRESS
     */
    @Transactional
    public Booking approveBooking(Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new IllegalArgumentException("Booking not found with ID: " + bookingId));

        // --- (MODIFIED) ---
        // We must get the status entity and check its 'statusName' property.
        BookingStatus status = booking.getStatus();
        if (status == null) {
            throw new IllegalStateException("Booking " + bookingId + " has no status.");
        }

        if ("IN_PROGRESS".equals(status.getStatusName())) {
        // --- (END OF MODIFICATION) ---
            return booking;
        }

        // --- (MODIFIED) ---
        if ("PENDING".equals(status.getStatusName())) {
            // We update the fields of the *existing* status entity
            status.setStatusName("IN_PROGRESS");
            status.setDisplayName("In Progress");
            // --- (END OF MODIFICATION) ---
            
            Machine machine = booking.getMachine();
            if (machine != null) {
                machine.setStatus(AppConstants.STATUS_IN_USE);
                machine.setCurrentUser(booking.getUser()); 
                machine.setUsageStartTime(LocalDateTime.now()); 
                machineRepository.save(machine);
            } else {
                 throw new IllegalStateException("Booking is not linked to a machine.");
            }
            
            // Saving the booking will persist the changes to its status entity
            return bookingRepository.save(booking);
        } else {
            // --- (MODIFIED) ---
            // Use the display name for a cleaner error message
            throw new IllegalStateException("ทำรายการไม่สำเร็จ สถานะปัจจุบันคือ: " + status.getDisplayName());
            // --- (END OF MODIFICATION) ---
        }
    }

    /**
     * Completes an in-progress booking and releases the machine.
     * Workflow:
     * 1. Finds booking by ID
     * 2. Verifies booking is IN_PROGRESS
     * 3. Changes booking status to COMPLETED
     * 4. Updates machine status to AVAILABLE
     * 5. Clears machine's current user and usage time
     * 6. Saves both booking and machine
     * * @param bookingId ID of booking to complete
     * @return Updated booking
     * @throws IllegalArgumentException if booking not found
     * @throws IllegalStateException if booking is not IN_PROGRESS
     */
    @Transactional
    public Booking completeBooking(Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new IllegalArgumentException("Booking not found with ID: " + bookingId));

        // --- (MODIFIED) ---
        BookingStatus status = booking.getStatus();
        if (status == null) {
            throw new IllegalStateException("Booking " + bookingId + " has no status.");
        }

        if ("IN_PROGRESS".equals(status.getStatusName())) {
            // We update the fields of the *existing* status entity
            status.setStatusName("COMPLETED");
            status.setDisplayName("Completed");
            // --- (END OF MODIFICATION) ---

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
     * Retrieves completed bookings for a user that can be rated.
     * @param userId User ID
     * @return List of completed bookings with details
     */
    @Transactional(readOnly = true)
    public List<Booking> getCompletedBookingsForRating(Long userId) {
        // --- (MODIFIED) ---
        // We pass the string "COMPLETED" instead of the enum.
        // The repository method signature must also be changed.
        return bookingRepository.findByUserIdAndStatusWithDetails(userId, "COMPLETED");
        // --- (END OF MODIFICATION) ---
    }

    /**
     * Retrieves all completed bookings in the system.
     * @return List of completed bookings
     */
    @Transactional(readOnly = true)
    public List<Booking> getCompletedBookings() {
        // --- (MODIFIED) ---
        // This will now use the repository method findByStatus_StatusName
        return bookingRepository.findByStatus_StatusName("COMPLETED");
        // --- (END OF MODIFICATION) ---
    }

    /**
     * Retrieves all in-progress bookings in the system.
     * @return List of in-progress bookings
     */
    @Transactional(readOnly = true)
    public List<Booking> getInProgressBookings() {
        // --- (MODIFIED) ---
        // This will now use the repository method findByStatus_StatusName
        return bookingRepository.findByStatus_StatusName("IN_PROGRESS");
        // --- (END OF MODIFICATION) ---
    }

    /**
     * Finds an active booking (PENDING or IN_PROGRESS) for a student.
     * @param studentId Student ID to search for
     * @return Optional containing the first active booking found
     */
    @Transactional(readOnly = true)
    public Optional<Booking> findActiveBookingByStudentId(String studentId) {
        // --- (MODIFIED) ---
        // We now pass a List<String> instead of List<BookingStatus>.
        // The repository method signature must also be changed.
        List<Booking> activeBookings = bookingRepository.findByUserStudentIdAndStatusIn(
                studentId,
                List.of("PENDING", "IN_PROGRESS")
        );
        // --- (END OF MODIFICATION) ---
        
        if (activeBookings.isEmpty()) {
            return Optional.empty();
        } else {
            return Optional.of(activeBookings.get(0));
        }
    }
}