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
import org.springframework.web.bind.annotation.PutMapping; // (ตรวจสอบว่ามี Import นี้)
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import model.Booking;
import service.BookingService; // This import is still correct (now imports the entity)

@RestController
@RequestMapping("/api/bookings")
@CrossOrigin(origins = "*", maxAge = 3600)
public class BookingController {

    @Autowired
    private BookingService bookingService;

    @GetMapping("/date-range")
    public ResponseEntity<List<Booking>> getBookingsByDateRange(
            @RequestParam String startDate,
            @RequestParam String endDate) {
        try {
            LocalDateTime start = LocalDateTime.parse(startDate);
            LocalDateTime end = LocalDateTime.parse(endDate);
            List<Booking> bookings = bookingService.getBookingsByDateRange(start, end);
            return ResponseEntity.ok(bookings);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping
    public ResponseEntity<List<Booking>> getAllBookings() {
        List<Booking> bookings = bookingService.getAllBookings();
        return ResponseEntity.ok(bookings);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Booking> getBookingById(@PathVariable Long id) {
        Optional<Booking> booking = bookingService.getBookingById(id);
        return booking.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<Booking>> getBookingsByUserId(@PathVariable Long userId) {
        List<Booking> bookings = bookingService.getBookingsByUserId(userId);
        return ResponseEntity.ok(bookings);
    }

    @GetMapping("/machine/{machineId}")
    public ResponseEntity<List<Booking>> getBookingsByMachineId(@PathVariable Long machineId) {
        List<Booking> bookings = bookingService.getBookingsByMachineId(machineId);
        return ResponseEntity.ok(bookings);
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<List<Booking>> getBookingsByStatus(@PathVariable String status) {
        // --- (MODIFIED) ---
        // We no longer use BookingStatus.valueOf().
        // We pass the raw status string (e.g., "PENDING") to the service,
        // which will handle the query.
        try {
            // We pass the status string (which should be the 'statusName' like "PENDING")
            List<Booking> bookings = bookingService.getBookingsByStatus(status.toUpperCase());
            return ResponseEntity.ok(bookings);
        } catch (Exception e) {
            // Catch a more generic exception in case the service throws one
            return ResponseEntity.badRequest().build();
        }
        // --- (END OF MODIFICATION) ---
    }

    @GetMapping("/stats/total")
    public ResponseEntity<Long> getTotalBookings() {
        return ResponseEntity.ok(bookingService.getTotalBookings());
    }

    @GetMapping("/stats/by-status/{status}")
    public ResponseEntity<Long> getBookingCountByStatus(@PathVariable String status) {
        // --- (MODIFIED) ---
        // Same as getBookingsByStatus, we pass the raw string to the service.
        try {
            long count = bookingService.getBookingCountByStatus(status.toUpperCase());
            return ResponseEntity.ok(count);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
        // --- (END OF MODIFICATION) ---
    }

    @PostMapping
    public ResponseEntity<Booking> createBooking(@RequestBody Booking booking) {
        Booking createdBooking = bookingService.createBooking(booking);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdBooking);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Booking> updateBooking(@PathVariable Long id, @RequestBody Booking bookingDetails) {
        Booking updatedBooking = bookingService.updateBooking(id, bookingDetails);
        if (updatedBooking != null) {
            return ResponseEntity.ok(updatedBooking);
        }
        return ResponseEntity.notFound().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBooking(@PathVariable Long id) {
        if (bookingService.deleteBooking(id)) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }

    @PostMapping("/{id}/approve")
    public ResponseEntity<?> approveBooking(@PathVariable Long id) {
        try {
            Booking updatedBooking = bookingService.approveBooking(id);
            Map<String, Object> response = new HashMap<>();
            response.put("id", updatedBooking.getId());
            // --- (MODIFIED) ---
            // getStatus() now returns the BookingStatus ENTITY.
            // We get its name (e.g., "CONFIRMED") to put in the response.
            response.put("status", updatedBooking.getStatus().getName());
            // --- (END OF MODIFICATION) ---
            response.put("message", "Approve Success");
            return ResponseEntity.ok(response);
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().body(createErrorResponse(e.getMessage(), "INVALID_STATUS"));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/{id}/status")
    public ResponseEntity<?> getBookingStatus(@PathVariable Long id) {
        Optional<Booking> bookingOpt = bookingService.getBookingById(id);
        if (bookingOpt.isPresent()) {
            Booking booking = bookingOpt.get();
            Map<String, Object> response = new HashMap<>();
            
            response.put("status", booking.getStatus().getName());

            long remainingSeconds = 0;
            LocalDateTime now = LocalDateTime.now();

            // --- (MODIFIED) ---
            // We can't compare the entity (booking.getStatus()) to an enum (BookingStatus.PENDING).
            // We must compare the status name string.
            if (booking.getStatus().getName().equals("PENDING")) {
                long elapsedSeconds = java.time.Duration.between(booking.getCreatedAt(), now).getSeconds();
                remainingSeconds = (15 * 60) - elapsedSeconds;
            } else if (booking.getStatus().getName().equals("IN_PROGRESS")) {
            // --- (END OF MODIFICATION) ---
                if (booking.getMachine() != null && booking.getMachine().getUsageStartTime() != null) {
                    long elapsedSeconds = java.time.Duration.between(booking.getMachine().getUsageStartTime(), now).getSeconds();
                    remainingSeconds = (60 * 60) - elapsedSeconds;
                } else {
                    remainingSeconds = 60 * 60;
                }
            }

            response.put("remainingSeconds", Math.max(0, remainingSeconds));
            return ResponseEntity.ok(response);
        }
        return ResponseEntity.notFound().build();
    }

    @PostMapping("/{id}/complete")
    public ResponseEntity<?> completeBooking(@PathVariable Long id) {
        try {
            Booking updatedBooking = bookingService.completeBooking(id);
            Map<String, Object> userMap = new HashMap<>();
            if (updatedBooking.getUser() != null) {
                userMap.put("id", updatedBooking.getUser().getId());
            }

            Map<String, Object> machineMap = new HashMap<>();
            if (updatedBooking.getMachine() != null) {
                machineMap.put("name", updatedBooking.getMachine().getName());
            }
            
            Map<String, Object> response = new HashMap<>();
            response.put("id", updatedBooking.getId());
            // --- (MODIFIED) ---
            // getStatus() now returns the BookingStatus ENTITY.
            // We get its name (e.g., "COMPLETED") to put in the response.
            response.put("status", updatedBooking.getStatus().getName());
            // --- (END OF MODIFICATION) ---
            response.put("user", userMap);
            response.put("machine", machineMap);
            return ResponseEntity.ok(response);
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().body(createErrorResponse(e.getMessage(), "INVALID_STATUS"));
        }
    }

    @GetMapping("/user/{userId}/completed")
    public ResponseEntity<List<Booking>> getCompletedBookingsForRating(@PathVariable Long userId) {
        List<Booking> bookings = bookingService.getCompletedBookingsForRating(userId);
        return ResponseEntity.ok(bookings);
    }

    @GetMapping("/completed")
    public ResponseEntity<List<Booking>> getCompletedBookings() {
        List<Booking> bookings = bookingService.getCompletedBookings();
        return ResponseEntity.ok(bookings);
    }

    @GetMapping("/in-progress")
    public ResponseEntity<List<Booking>> getInProgressBookings() {
        List<Booking> bookings = bookingService.getInProgressBookings();
        return ResponseEntity.ok(bookings);
    }

    private Map<String, String> createErrorResponse(String message, String code) {
        Map<String, String> error = new HashMap<>();
        error.put("error", message);
        error.put("code", code);
        return error;
    }
}