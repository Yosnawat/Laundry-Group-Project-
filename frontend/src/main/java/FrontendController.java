import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class FrontendController {

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @GetMapping("/")
    public String home() {
        return "redirect:/login";
    }

    @GetMapping("/student/dashboard")
    public String studentDashboard() {
        return "Dashboard";
    }

    @GetMapping("/manager/dashboard")
    public String managerDashboard() {
        return "Dashboard";
    }

    @GetMapping("/booking")
    public String booking() {
        return "booking";
    }

    @GetMapping("/rating")
    public String rating() {
        return "rating";
    }

    @GetMapping("/timer")
    public String timer() {
        return "timer";
    }
}
