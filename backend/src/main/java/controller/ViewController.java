package controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable; // <-- IMPORT THIS

import model.BookingStatus;
import model.User; // <-- IMPORT THIS
import service.BookingService;
import service.UserService; // <-- IMPORT THIS

@Controller
public class ViewController {

    @Autowired
    private BookingService bookingService;
    
    @Autowired
    private UserService userService; // <-- ADD THIS

    @GetMapping("/")
    public String home() {
        return "redirect:/login";
    }
    
    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @GetMapping("/booking")
    public String booking(Model model) {
        // (This is your old method, it's fine)
        model.addAttribute("bookings", bookingService.getAllBookings());
        model.addAttribute("totalBookings", bookingService.getTotalBookings());
        model.addAttribute("confirmedBookings", bookingService.getBookingCountByStatus(BookingStatus.CONFIRMED));
        model.addAttribute("pendingBookings", bookingService.getBookingCountByStatus(BookingStatus.PENDING));
        model.addAttribute("cancelledBookings", bookingService.getBookingCountByStatus(BookingStatus.CANCELLED));
        return "booking";
    }

    /*
     * THIS IS THE NEW METHOD THAT FIXES YOUR PROBLEM.
     * It matches the link from dashboard.html, finds the user,
     * and serves the booking.html page.
     */
    @GetMapping("/student/{studentId}/booking")
    public String studentBooking(@PathVariable String studentId, Model model) {
        
        // Find the user who is booking
        User currentUser = userService.findByStudentId(studentId);
        if (currentUser == null) {
            return "redirect:/login?error=UserNotFound";
        }
        
        // Add the current user to the model so booking.html knows who they are
        model.addAttribute("currentUser", currentUser);
        
        // (You can add any other data the booking page needs here)
        // e.g., model.addAttribute("machines", machineService.getAllMachines());
        
        return "booking"; // This tells Spring to render "booking.html"
    }

    @GetMapping("/rating")
    public String rating(Model model) {
        return "rating";
    }

    @GetMapping("/timer")
    public String timer() {
        return "timer";
    }//
}