package controller;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import model.AppConstants;
import model.Booking;
import model.Machine; // This import is still correct (now imports the entity)
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

    @GetMapping("/student/{studentId}/dashboard")
    public String studentDashboard(@PathVariable String studentId, Model model) {
        User currentUser = userService.findByStudentId(studentId);
        
        if (currentUser == null) {
            return "redirect:/login";
        }
        if (currentUser.getRole() != Role.STUDENT) {
            return "redirect:/error";
        }
        if (!currentUser.getIsActive()) {
            return "redirect:/login";
        }
        model.addAttribute("currentUser", currentUser);

        List<Booking> allStudentBookings = bookingService.getBookingsByUserId(currentUser.getId());

        // --- (MODIFIED) ---
        // We now check the 'statusName' string from the BookingStatus entity,
        // instead of comparing with the old enum value.
        Optional<Booking> activeBookingOpt = allStudentBookings.stream()
                .filter(b -> b.getStatus() != null && "IN_PROGRESS".equals(b.getStatus().getName()))
                .findFirst();
        // --- (END OF MODIFICATION) ---
        
        model.addAttribute("activeBooking", activeBookingOpt.orElse(null));

        // --- (MODIFIED) ---
        // We check the 'statusName' string here as well.
        List<Booking> upcomingBookings = allStudentBookings.stream()
                .filter(b -> b.getStatus() == null || !"IN_PROGRESS".equals(b.getStatus().getName()))
                .collect(Collectors.toList());
        // --- (END OF MODIFICATION) ---
        
        model.addAttribute("studentBookings", upcomingBookings);

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
        
        List<Booking> completedBookings = 
                bookingService.getCompletedBookingsForRating(currentUser.getId());
        model.addAttribute("completedBookings", completedBookings != null ? completedBookings : List.of());
        
        model.addAttribute("managerStats", new HashMap<>());
        model.addAttribute("recentActivities", List.of());
        model.addAttribute("allMachines", allMachines != null ? allMachines : List.of());
        model.addAttribute("allUsers", List.of());
        model.addAttribute("allStatuses", List.of());
        model.addAttribute("todaysBookings", List.of());

        return "dashboard";
    }

    @GetMapping("/manager/{studentId}/dashboard")
    public String managerDashboard(@PathVariable String studentId, Model model) {
        User currentUser = userService.findByStudentId(studentId);
        
        if (currentUser == null) {
            return "redirect:/login";
        }
        if (currentUser.getRole() != Role.MANAGER) {
            return "redirect:/error";
        }
        if (!currentUser.getIsActive()) {
            return "redirect:/login";
        }
        model.addAttribute("currentUser", currentUser);
        
        List<Machine> allMachines = machineService.getAllMachines();
        List<User> allUsers = userService.findAllUsers();
        List<Booking> recentActivities = bookingService.getAllBookings().stream()
                .sorted((b1, b2) -> b2.getCreatedAt().compareTo(b1.getCreatedAt()))
                .limit(5)
                .collect(Collectors.toList());

        LocalDate today = LocalDate.now();
        List<Booking> todaysBookings = bookingService.getBookingsByDateRange(
                today.atStartOfDay(),
                today.atTime(LocalTime.MAX)
        );

        Map<String, Object> managerStats = new HashMap<>();
        long totalMachines = allMachines.size();
        long availableMachines = allMachines.stream().filter(m -> 
                AppConstants.STATUS_AVAILABLE.equals(m.getStatus())).count();
        long inUseMachines = allMachines.stream().filter(m -> 
                AppConstants.STATUS_IN_USE.equals(m.getStatus())).count();
        long maintenanceMachines = allMachines.stream().filter(m -> 
                AppConstants.STATUS_MAINTENANCE.equals(m.getStatus()) || 
                AppConstants.STATUS_OUT_OF_SERVICE.equals(m.getStatus())).count();
        long totalUsers = allUsers.size();
        
        managerStats.put("totalMachines", totalMachines);
        managerStats.put("availableMachines", availableMachines);
        managerStats.put("inUseMachines", inUseMachines);
        managerStats.put("maintenanceMachines", maintenanceMachines);
        managerStats.put("totalUsers", totalUsers);

        model.addAttribute("managerStats", managerStats);
        model.addAttribute("recentActivities", recentActivities);
        model.addAttribute("allMachines", allMachines != null ? allMachines : List.of());
        model.addAttribute("allUsers", allUsers != null ? allUsers : List.of());
        
        model.addAttribute("todaysBookings", todaysBookings != null ? todaysBookings : List.of());
        
        model.addAttribute("allStatuses", List.of(
                AppConstants.STATUS_AVAILABLE,
                AppConstants.STATUS_MAINTENANCE,
                AppConstants.STATUS_OUT_OF_SERVICE
        ));
        
        model.addAttribute("studentStats", new HashMap<>());
        model.addAttribute("studentBookings", List.of());
        model.addAttribute("completedBookings", List.of());
        
        return "dashboard";
    }
}