package service;

import model.Booking;
import model.BookingStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import repo.BookingRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class BookingService {

    @Autowired
    private BookingRepository bookingRepository;

    // Get all bookings
    public List<Booking> getAllBookings() {
        return bookingRepository.findAll();
    }

    // Get booking by ID
    public Optional<Booking> getBookingById(Long id) {
        return bookingRepository.findById(id);
    }

    // Get bookings by user ID
    public List<Booking> getBookingsByUserId(Long userId) {
        return bookingRepository.findByUserId(userId);
    }

    // Get bookings by machine ID
    public List<Booking> getBookingsByMachineId(Long machineId) {
        return bookingRepository.findByMachineId(machineId);
    }

    // Get bookings by status
    public List<Booking> getBookingsByStatus(BookingStatus status) {
        return bookingRepository.findByStatus(status);
    }

    // Get booking statistics
    public Long getTotalBookings() {
        return bookingRepository.count();
    }

    public Long getBookingCountByStatus(BookingStatus status) {
        return (long) bookingRepository.findByStatus(status).size();
    }

    // Create new booking
    public Booking createBooking(Booking booking) {
        if (booking.getStatus() == null) {
            booking.setStatus(BookingStatus.PENDING);
        }
        return bookingRepository.save(booking);
    }

    // Update booking
    public Booking updateBooking(Long id, Booking bookingDetails) {
        Optional<Booking> bookingOptional = bookingRepository.findById(id);
        if (bookingOptional.isPresent()) {
            Booking booking = bookingOptional.get();
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
        return null;
    }

    // Delete booking
    public boolean deleteBooking(Long id) {
        if (bookingRepository.existsById(id)) {
            bookingRepository.deleteById(id);
            return true;
        }
        return false;
    }

    // Get bookings by date range
    public List<Booking> getBookingsByDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        return bookingRepository.findByBookingDateBetween(startDate, endDate);
    }

    // Mark booking as in progress (when user starts using the machine)
    public Booking startBooking(Long bookingId) {
        Optional<Booking> bookingOptional = bookingRepository.findById(bookingId);
        if (bookingOptional.isPresent()) {
            Booking booking = bookingOptional.get();
            if (booking.getStatus() == BookingStatus.CONFIRMED) {
                booking.setStatus(BookingStatus.IN_PROGRESS);
                return bookingRepository.save(booking);
            } else {
                throw new IllegalStateException("Only confirmed bookings can be started");
            }
        }
        return null;
    }

    // Mark booking as completed (when user finishes using the machine)
    public Booking completeBooking(Long bookingId) {
        Optional<Booking> bookingOptional = bookingRepository.findById(bookingId);
        if (bookingOptional.isPresent()) {
            Booking booking = bookingOptional.get();
            if (booking.getStatus() == BookingStatus.IN_PROGRESS) {
                booking.setStatus(BookingStatus.COMPLETED);
                return bookingRepository.save(booking);
            } else {
                throw new IllegalStateException("Only in-progress bookings can be completed");
            }
        }
        return null;
    }

    // Get completed bookings for a user that can be rated
    public List<Booking> getCompletedBookingsForRating(Long userId) {
        return bookingRepository.findByUserIdAndStatus(userId, BookingStatus.COMPLETED);
    }

    // Get all completed bookings
    public List<Booking> getCompletedBookings() {
        return bookingRepository.findByStatus(BookingStatus.COMPLETED);
    }

    // Get in-progress bookings
    public List<Booking> getInProgressBookings() {
        return bookingRepository.findByStatus(BookingStatus.IN_PROGRESS);
    }
}
