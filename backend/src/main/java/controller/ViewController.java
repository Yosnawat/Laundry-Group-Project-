package controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable; // Import this

import controller.MachineDetailDTO;
import model.Booking; // Import this
import model.BookingStatus;
import model.User; 
import service.BookingService;
import service.MachineManagementService;
import service.UserService; 

@Controller
public class ViewController {

    @Autowired
    private BookingService bookingService;
    
    @Autowired
    private UserService userService;

    @Autowired
    private MachineManagementService machineManagementService; 

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
        model.addAttribute("bookings", bookingService.getAllBookings());
        model.addAttribute("totalBookings", bookingService.getTotalBookings());
        model.addAttribute("confirmedBookings", bookingService.getBookingCountByStatus(BookingStatus.CONFIRMED));
        model.addAttribute("pendingBookings", bookingService.getBookingCountByStatus(BookingStatus.PENDING));
        model.addAttribute("cancelledBookings", bookingService.getBookingCountByStatus(BookingStatus.CANCELLED));
        return "booking";
    }

    /*
     * This is your original, working method.
     * I have added model.addAttribute("student", currentUser)
     * to ensure it also works if your 'rating.html' is a copy of 'dashboard.html'.
     */
    @GetMapping("/student/{studentId}/booking")
    public String studentBooking(@PathVariable String studentId, Model model) {
        
        // Find the user who is booking
        User currentUser = userService.findByStudentId(studentId);
        if (currentUser == null) {
            return "redirect:/login?error=UserNotFound";
        }
        
        // Add the current user to the model
        model.addAttribute("currentUser", currentUser);
        model.addAttribute("student", currentUser); // Add this for compatibility
        
        // (You must add machine data here for the booking page to work)
        // e.g., model.addAttribute("machines", machineService.getAllMachines());
        
        return "booking"; // This tells Spring to render "booking.html"
    }

    // --- (THIS IS THE FIX for the rating page) ---
    @GetMapping("/student/{studentId}/rating")
    public String showStudentRatingPage(@PathVariable String studentId, Model model) {
        
        User currentUser = userService.findByStudentId(studentId);
        if (currentUser == null) {
            return "redirect:/login?error=UserNotFound";
        }

        // This line fixes your "student.id" error
        model.addAttribute("student", currentUser);

        // Your rating.html page (copied from dashboard) needs this data
        List<Booking> upcoming = bookingService.findUpcomingBookingsByUserId(currentUser.getId()); 
        List<Booking> completed = bookingService.getCompletedBookingsForRating(currentUser.getId());

        model.addAttribute("upcomingBookings", upcoming);
        model.addAttribute("completedHistory", completed);
        
        // Render the rating.html template
        return "rating";
    }

    @GetMapping("/timer")
    public String timer() {
        return "timer";
    }

    @GetMapping("/machine/{machineId}")
    public String showMachinePage(@PathVariable Long machineId, Model model) {
        try {
            // Get machine detail with all ratings and reviews
            MachineDetailDTO machineDetail = machineManagementService.getMachineDetail(machineId);
            
            // Add individual attributes that machine.html expects
            model.addAttribute("machine", machineDetail);
            model.addAttribute("averageRating", machineDetail.getAverageRating());
            model.addAttribute("totalRatings", machineDetail.getTotalRatings());
            model.addAttribute("recentReviews", machineDetail.getReviews());
            
            return "machine";
        } catch (IllegalArgumentException e) {
            return "redirect:/";
        } catch (Exception e) {
            e.printStackTrace();
            return "redirect:/";
        }
    }
}