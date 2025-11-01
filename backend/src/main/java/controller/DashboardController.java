package controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors; // <-- ADD THIS IMPORT

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseBody;

import model.AppConstants;
import model.Booking;
import model.Machine;
import model.Role;
import model.User;
import service.BookingService;
import service.MachineService;
import service.UserService;

@Controller
public class DashboardController {

    private final UserService userService;
    private final MachineService machineService;
    private final BookingService bookingService;

    public DashboardController(UserService userService, MachineService machineService, BookingService bookingService) {
        this.userService = userService;
        this.machineService = machineService;
        this.bookingService = bookingService;
    }

    // ==================== Student Dashboard ====================
    
    // (This test endpoint is fine)
    @GetMapping("/student/{studentId}/test")
    @ResponseBody
    public String testUser(@PathVariable String studentId) {
        User user = userService.findByStudentId(studentId);
        if (user == null) {
            return "User NOT found with studentId: " + studentId;
        }
        return "User found: " + user.getName() + ", Role: " + user.getRole() + ", Active: " + user.getIsActive();
    }

    // (This student dashboard method is fine)
    @GetMapping("/student/{studentId}/dashboard")
    public String studentDashboard(@PathVariable String studentId, Model model) {
        System.out.println("========== STUDENT DASHBOARD CALLED ==========");
        System.out.println("StudentId from URL: " + studentId);
        
        User currentUser = userService.findByStudentId(studentId);
        
        System.out.println("User found: " + (currentUser != null));
        
        if (currentUser == null) {
            System.out.println("ERROR: User is null!");
            model.addAttribute("error", "Student not found with ID: " + studentId);
            return "redirect:/login";
        }
        
        System.out.println("User name: " + currentUser.getName());
        System.out.println("User role: " + currentUser.getRole());
        
        if (currentUser.getRole() != Role.STUDENT) {
            System.out.println("ERROR: User is not a student!");
            model.addAttribute("error", "Access denied: Not a student");
            return "redirect:/error";
        }
        
        if (!currentUser.getIsActive()) {
            System.out.println("ERROR: User is not active!");
            model.addAttribute("error", "Account is inactive");
            return "redirect:/login";
        }
        
        System.out.println("Adding currentUser to model");
        model.addAttribute("currentUser", currentUser);
        
        List<Booking> studentBookings = bookingService.getBookingsByUserId(currentUser.getId());
        model.addAttribute("studentBookings", studentBookings != null ? studentBookings : List.of());
        
        List<Machine> allMachines = machineService.getAllMachines();
        model.addAttribute("machines", allMachines != null ? allMachines : List.of());
        
        Map<String, Object> studentStats = new HashMap<>();
        studentStats.put("totalMachines", allMachines != null ? allMachines.size() : 0);
        
        long availableCount = allMachines != null ? 
            allMachines.stream().filter(m -> AppConstants.STATUS_AVAILABLE.equals(m.getStatus())).count() : 0;
        studentStats.put("availableMachines", availableCount);
        
        long inUseCount = allMachines != null ? 
            allMachines.stream().filter(m -> AppConstants.STATUS_IN_USE.equals(m.getStatus())).count() : 0;
        studentStats.put("inUseMachines", inUseCount);
        
        model.addAttribute("studentStats", studentStats);
        
        List<Booking> completedBookings = bookingService.getCompletedBookingsForRating(currentUser.getId());
        model.addAttribute("completedBookings", completedBookings != null ? completedBookings : List.of());
        
        // Add empty collections for manager view
        model.addAttribute("managerStats", new HashMap<>());
        model.addAttribute("recentActivities", List.of());
        model.addAttribute("allMachines", allMachines != null ? allMachines : List.of());
        model.addAttribute("allUsers", List.of());
        model.addAttribute("allStatuses", List.of(
            AppConstants.STATUS_AVAILABLE,
            AppConstants.STATUS_IN_USE,
            AppConstants.STATUS_MAINTENANCE,
            AppConstants.STATUS_OUT_OF_SERVICE
        ));
        
        System.out.println("========== RETURNING DASHBOARD VIEW FOR STUDENT ==========");
        return "dashboard";
    }

    // ==================== Manager Dashboard (FIXED) ====================
    
    @GetMapping("/manager/{studentId}/dashboard") // <-- FIX: Added {studentId} path variable
    public String managerDashboard(@PathVariable String studentId, Model model) { // <-- FIX: Added parameters
        System.out.println("========== MANAGER DASHBOARD CALLED ==========");
        System.out.println("Manager ID (from studentId field): " + studentId);
        
        // Find user by studentId
        User currentUser = userService.findByStudentId(studentId); 
        
        if (currentUser == null) {
            System.out.println("ERROR: User is null!");
            model.addAttribute("error", "Manager not found with ID: " + studentId);
            return "redirect:/login";
        }
        
        // Verify user is a MANAGER
        if (currentUser.getRole() != Role.MANAGER) {
            System.out.println("ERROR: User is not a manager!");
            model.addAttribute("error", "Access denied: Not a manager");
            return "redirect:/error";
        }
        
        // Check if user is active
        if (!currentUser.getIsActive()) {
            System.out.println("ERROR: User is not active!");
            model.addAttribute("error", "Account is inactive");
            return "redirect:/login";
        }
        
        // Add current user to model
        model.addAttribute("currentUser", currentUser);
        
        // --- Load MANAGER-specific data ---
        List<Machine> allMachines = machineService.getAllMachines();
        List<User> allUsers = userService.findAllUsers(); 
        // Get top 5 most recent bookings as "recent activity"
        List<Booking> recentActivities = bookingService.getAllBookings().stream()
            .sorted((b1, b2) -> b2.getCreatedAt().compareTo(b1.getCreatedAt()))
            .limit(5)
            .collect(Collectors.toList());

        // Calculate manager stats (assuming you have a 'getType()' on Machine)
        Map<String, Object> managerStats = new HashMap<>();
        long totalWashers = allMachines.stream().count(); // Simplified, update if you have types
        long availableWashers = allMachines.stream().filter(m -> AppConstants.STATUS_AVAILABLE.equals(m.getStatus())).count();
        long outOfService = allMachines.stream().filter(m -> AppConstants.STATUS_OUT_OF_SERVICE.equals(m.getStatus()) || AppConstants.STATUS_MAINTENANCE.equals(m.getStatus())).count();
        
        managerStats.put("totalWashers", totalWashers);
        managerStats.put("availableWashers", availableWashers);
        managerStats.put("totalDryers", 0); // Placeholder
        managerStats.put("availableDryers", 0); // Placeholder
        managerStats.put("outOfServiceMachines", outOfService);
        
        model.addAttribute("managerStats", managerStats);
        model.addAttribute("recentActivities", recentActivities);
        model.addAttribute("allMachines", allMachines != null ? allMachines : List.of());
        model.addAttribute("allUsers", allUsers != null ? allUsers : List.of());
        model.addAttribute("allStatuses", List.of(
            AppConstants.STATUS_AVAILABLE,
            AppConstants.STATUS_IN_USE,
            AppConstants.STATUS_MAINTENANCE,
            AppConstants.STATUS_OUT_OF_SERVICE
        ));
        
        // --- Add empty STUDENT data (so Thymeleaf doesn't crash) ---
        model.addAttribute("studentStats", new HashMap<>());
        model.addAttribute("studentBookings", List.of());
        model.addAttribute("completedBookings", List.of());

        System.out.println("========== RETURNING DASHBOARD VIEW FOR MANAGER ==========");
        return "dashboard";
    }//
}