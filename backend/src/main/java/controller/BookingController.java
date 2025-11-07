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
import model.BookingStatus;
import service.BookingService;

@RestController
@RequestMapping("/api/bookings")
@CrossOrigin(origins = "*", maxAge = 3600)
public class BookingController {

    @Autowired
    private BookingService bookingService;

    // (ส่วนนี้เหมือนเดิม)
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

    // (เมธอดอื่นๆ ... getAllBookings, getBookingById, ฯลฯ.... เหมือนเดิม)
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
        try {
            BookingStatus bookingStatus = BookingStatus.valueOf(status.toUpperCase());
            List<Booking> bookings = bookingService.getBookingsByStatus(bookingStatus);
            return ResponseEntity.ok(bookings);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

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

    // --- (นี่คือส่วนที่แก้ไข) ---
    // เปลี่ยนจาก @PostMapping เป็น @PutMapping
   @PostMapping("/{id}/approve")
    public ResponseEntity<?> approveBooking(@PathVariable Long id) {
        System.out.println("🔥 MANAGER กำลังกดอนุมัติ Booking ID: " + id);

        try {
            Booking updatedBooking = bookingService.approveBooking(id);
            System.out.println("✅ อนุมัติสำเร็จใน DB! สถานะใหม่คือ: " + updatedBooking.getStatus());

            if (updatedBooking != null) {
                // --- ⬇️ แก้ไขตรงนี้ ⬇️ ---
                // แทนที่จะส่ง updatedBooking กลับไปทั้งก้อน (ซึ่งทำให้เกิด Error)
                // เราสร้าง Map ส่งกลับไปเฉพาะข้อมูลที่จำเป็นพอ
                Map<String, Object> response = new HashMap<>();
                response.put("id", updatedBooking.getId());
                response.put("status", updatedBooking.getStatus());
                response.put("message", "Approve Success");
                
                return ResponseEntity.ok(response);
                // --- ⬆️ จบส่วนที่แก้ไข ⬆️ ---
            }
            return ResponseEntity.notFound().build();
            
        } catch (IllegalStateException e) {
            System.out.println("❌ เกิดข้อผิดพลาด (IllegalState): " + e.getMessage());
            // e.printStackTrace(); // เอาออกได้ถ้าไม่อยากรก Console

            return ResponseEntity.badRequest().body(createErrorResponse(e.getMessage(),
                    "INVALID_STATUS"));
        } catch (Exception e) {
             System.out.println("❌❌ ERROR ไม่คาดคิด: " + e.getMessage());
             e.printStackTrace();
             return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    // --- (ส่วนนี้เหมือนเดิม) ---
    @GetMapping("/{id}/status")
    public ResponseEntity<?> getBookingStatus(@PathVariable Long id) {
        Optional<Booking> bookingOpt = bookingService.getBookingById(id);
        if (bookingOpt.isPresent()) {
            Booking booking = bookingOpt.get();
            Map<String, Object> response = new HashMap<>();
            response.put("status", booking.getStatus().name());

            // คำนวณเวลาที่เหลือ (หน่วย: วินาที)
            long remainingSeconds = 0;
            LocalDateTime now = LocalDateTime.now();

            if (booking.getStatus() == BookingStatus.PENDING) {
                // ถ้า PENDING: เวลาที่เหลือ = 15 นาที - เวลาที่ผ่านไปแล้วตั้งแต่กดจอง
                long elapsedSeconds = java.time.Duration.between(booking.getCreatedAt(), now).getSeconds();
                remainingSeconds = (15 * 60) - elapsedSeconds;

            } else if (booking.getStatus() == BookingStatus.IN_PROGRESS) {
                // ถ้า IN_PROGRESS: เวลาที่เหลือ = 60 นาที - เวลาที่ผ่านไปแล้วตั้งแต่เริ่มใช้งาน
                if (booking.getMachine() != null && booking.getMachine().getUsageStartTime() != null) {
                    long elapsedSeconds = java.time.Duration.between(booking.getMachine().getUsageStartTime(), now)
                            .getSeconds();
                    remainingSeconds = (60 * 60) - elapsedSeconds;
                } else {
                    // กรณี Error ไม่เจอเวลาเริ่ม ให้ Default ไปก่อน
                    remainingSeconds = 60 * 60;
                }
            }

            // ถ้าเวลาติดลบ (หมดเวลาแล้ว) ให้เป็น 0
            response.put("remainingSeconds", Math.max(0, remainingSeconds));

            return ResponseEntity.ok(response);
        }
        return ResponseEntity.notFound().build();
    }

    @PostMapping("/{id}/complete")
    public ResponseEntity<?> completeBooking(@PathVariable Long id) {
        try {
            Booking updatedBooking = bookingService.completeBooking(id);
            if (updatedBooking != null) {
                
                // --- (FIX) Create a safe response, just like in approveBooking ---
                
                // 1. Create a simple map for the user
                Map<String, Object> userMap = new HashMap<>();
                if (updatedBooking.getUser() != null) {
                    userMap.put("id", updatedBooking.getUser().getId());
                }

                // 2. Create a simple map for the machine
                Map<String, Object> machineMap = new HashMap<>();
                if (updatedBooking.getMachine() != null) {
                    machineMap.put("name", updatedBooking.getMachine().getName());
                }
                
                // 3. Create the final response
                Map<String, Object> response = new HashMap<>();
                response.put("id", updatedBooking.getId());
                response.put("status", updatedBooking.getStatus());
                response.put("user", userMap); // Add the simple user map
                response.put("machine", machineMap); // Add the simple machine map

                return ResponseEntity.ok(response);
                // --- (End of Fix) ---

            }
            return ResponseEntity.notFound().build();
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().body(createErrorResponse(e.getMessage(),
                    "INVALID_STATUS"));
        }
    }

    // (ส่วนที่เหลือของไฟล์)
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