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
     * @param status Booking status to filter by
     * @return List of bookings with the specified status
     */
    @Transactional(readOnly = true)
    public List<Booking> getBookingsByStatus(BookingStatus status) {
        return bookingRepository.findByStatus(status);
    }

    /**
     * Counts the total number of bookings.
     * @return Total booking count
     */
    public Long getTotalBookings() {
        return bookingRepository.count();
    }

    /**
     * Counts bookings with a specific status.
     * @param status Booking status to count
     * @return Number of bookings with the status
     */
    public Long getBookingCountByStatus(BookingStatus status) {
        return (long) bookingRepository.findByStatus(status).size();
    }

    /**
     * Creates a new booking with validation.
     * Workflow:
     * 1. Validates user and machine IDs are provided
     * 2. Validates booking date is provided
     * 3. Checks if time slot is already taken
     * 4. Sets status to PENDING if not provided
     * 5. Saves booking to database
     * 
     * @param booking Booking object to create
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
        if (booking.getStatus() == null) {
            booking.setStatus(BookingStatus.PENDING);
        }
        return bookingRepository.save(booking);
    }

    /**
     * Updates an existing booking with new details.
     * Only updates fields that are non-null in the bookingDetails parameter.
     * 
     * @param id ID of booking to update
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
     * 
     * @param bookingId ID of booking to approve
     * @return Updated booking
     * @throws IllegalArgumentException if booking not found
     * @throws IllegalStateException if booking status is not PENDING or IN_PROGRESS
     */
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

    /**
     * Completes an in-progress booking and releases the machine.
     * Workflow:
     * 1. Finds booking by ID
     * 2. Verifies booking is IN_PROGRESS
     * 3. Changes booking status to COMPLETED
     * 4. Updates machine status to AVAILABLE
     * 5. Clears machine's current user and usage time
     * 6. Saves both booking and machine
     * 
     * @param bookingId ID of booking to complete
     * @return Updated booking
     * @throws IllegalArgumentException if booking not found
     * @throws IllegalStateException if booking is not IN_PROGRESS
     */
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

    /**
     * Retrieves completed bookings for a user that can be rated.
     * @param userId User ID
     * @return List of completed bookings with details
     */
    @Transactional(readOnly = true)
    public List<Booking> getCompletedBookingsForRating(Long userId) {
        return bookingRepository.findByUserIdAndStatusWithDetails(userId, BookingStatus.COMPLETED);
    }

    /**
     * Retrieves all completed bookings in the system.
     * @return List of completed bookings
     */
    @Transactional(readOnly = true)
    public List<Booking> getCompletedBookings() {
        return bookingRepository.findByStatus(BookingStatus.COMPLETED);
    }

    /**
     * Retrieves all in-progress bookings in the system.
     * @return List of in-progress bookings
     */
    @Transactional(readOnly = true)
    public List<Booking> getInProgressBookings() {
        return bookingRepository.findByStatus(BookingStatus.IN_PROGRESS);
    }

    /**
     * Finds an active booking (PENDING or IN_PROGRESS) for a student.
     * @param studentId Student ID to search for
     * @return Optional containing the first active booking found
     */
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