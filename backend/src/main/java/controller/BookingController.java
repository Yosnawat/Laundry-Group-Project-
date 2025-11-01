package controller;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import model.Booking;
import model.BookingStatus;
import service.BookingService;

@RestController
@RequestMapping("/api/bookings")
@CrossOrigin(origins = "*", maxAge = 3600)
public class BookingController {

    @Autowired
    private BookingService bookingService;

    // Get all bookings
    @GetMapping
    public ResponseEntity<List<Booking>> getAllBookings() {
        List<Booking> bookings = bookingService.getAllBookings();
        return ResponseEntity.ok(bookings);
    }

    // Get booking by ID
    @GetMapping("/{id}")
    public ResponseEntity<Booking> getBookingById(@PathVariable Long id) {
        Optional<Booking> booking = bookingService.getBookingById(id);
        return booking.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    // Get bookings by user ID
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<Booking>> getBookingsByUserId(@PathVariable Long userId) {
        List<Booking> bookings = bookingService.getBookingsByUserId(userId);
        return ResponseEntity.ok(bookings);
    }

    // Get bookings by machine ID
    @GetMapping("/machine/{machineId}")
    public ResponseEntity<List<Booking>> getBookingsByMachineId(@PathVariable Long machineId) {
        List<Booking> bookings = bookingService.getBookingsByMachineId(machineId);
        return ResponseEntity.ok(bookings);
    }

    // Get bookings by status
    @GetMapping("/status/{status}")
    public ResponseEntity<List<Booking>> getBookingsByStatus(@PathVariable String status) {
        try {
            BookingStatus bookingStatus = BookingStatus.valueOf(status.toUpperCase());
            List<Booking> bookings = bookingService.getBookingsByStatus(bookingStatus);
            return ResponseEntity.ok(bookings);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // Get booking statistics
    @GetMapping("/stats/total")
    public ResponseEntity<Long> getTotalBookings() {
        return ResponseEntity.ok(bookingService.getTotalBookings());
    }

    @GetMapping("/stats/by-status/{status}")
    public ResponseEntity<Long> getBookingCountByStatus(@PathVariable String status) {
        try {
            BookingStatus bookingStatus = BookingStatus.valueOf(status.toUpperCase());
            long count = bookingService.getBookingCountByStatus(bookingStatus);
            return ResponseEntity.ok(count);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // Create new booking
    @PostMapping
    public ResponseEntity<Booking> createBooking(@RequestBody Booking booking) {
        Booking createdBooking = bookingService.createBooking(booking);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdBooking);
    }

    // Update booking
    @PutMapping("/{id}")
    public ResponseEntity<Booking> updateBooking(@PathVariable Long id, @RequestBody Booking bookingDetails) {
        Booking updatedBooking = bookingService.updateBooking(id, bookingDetails);
        if (updatedBooking != null) {
            return ResponseEntity.ok(updatedBooking);
        }
        return ResponseEntity.notFound().build();
    }

    // Delete booking
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBooking(@PathVariable Long id) {
        if (bookingService.deleteBooking(id)) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }

    // Get bookings by date range
    @GetMapping("/date-range")
    public ResponseEntity<List<Booking>> getBookingsByDateRange(
            @RequestParam LocalDateTime startDate,
            @RequestParam LocalDateTime endDate) {
        List<Booking> bookings = bookingService.getBookingsByDateRange(startDate, endDate);
        return ResponseEntity.ok(bookings);
    }

    // Start a booking (mark as in progress)
    @PostMapping("/{id}/start")
    public ResponseEntity<?> startBooking(@PathVariable Long id) {
        try {
            Booking updatedBooking = bookingService.startBooking(id);
            if (updatedBooking != null) {
                return ResponseEntity.ok(updatedBooking);
            }
            return ResponseEntity.notFound().build();
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().body(createErrorResponse(e.getMessage(), "INVALID_STATUS"));
        }
    }

    // Complete a booking (mark as completed)
    @PostMapping("/{id}/complete")
    public ResponseEntity<?> completeBooking(@PathVariable Long id) {
        try {
            Booking updatedBooking = bookingService.completeBooking(id);
            if (updatedBooking != null) {
                return ResponseEntity.ok(updatedBooking);
            }
            return ResponseEntity.notFound().build();
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().body(createErrorResponse(e.getMessage(), "INVALID_STATUS"));
        }
    }

    // Get completed bookings for a user that can be rated
    @GetMapping("/user/{userId}/completed")
    public ResponseEntity<List<Booking>> getCompletedBookingsForRating(@PathVariable Long userId) {
        List<Booking> bookings = bookingService.getCompletedBookingsForRating(userId);
        return ResponseEntity.ok(bookings);
    }

    // Get all completed bookings
    @GetMapping("/completed")
    public ResponseEntity<List<Booking>> getCompletedBookings() {
        List<Booking> bookings = bookingService.getCompletedBookings();
        return ResponseEntity.ok(bookings);
    }

    // Get all in-progress bookings
    @GetMapping("/in-progress")
    public ResponseEntity<List<Booking>> getInProgressBookings() {
        List<Booking> bookings = bookingService.getInProgressBookings();
        return ResponseEntity.ok(bookings);
    }

    // Helper method to create error response
    private Map<String, String> createErrorResponse(String message, String code) {
        Map<String, String> error = new HashMap<>();
        error.put("error", message);
        error.put("code", code);
        return error;
    }//
}
