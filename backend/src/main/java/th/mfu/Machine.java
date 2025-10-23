package th.mfu;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "machines")
public class Machine {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String machineNumber;

    private String status;
    private String type;
    private String location;
    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "current_user_id")
    private th.mfu.model.User currentUser;

    private LocalDateTime usageStartTime;

    // getters/setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getMachineNumber() { return machineNumber; }
    public void setMachineNumber(String machineNumber) { this.machineNumber = machineNumber; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public th.mfu.model.User getCurrentUser() { return currentUser; }
    public void setCurrentUser(th.mfu.model.User currentUser) { this.currentUser = currentUser; }
    public LocalDateTime getUsageStartTime() { return usageStartTime; }
    public void setUsageStartTime(LocalDateTime usageStartTime) { this.usageStartTime = usageStartTime; }
}
