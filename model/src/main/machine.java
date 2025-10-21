@Entity
@Table(name = "machines")
public class Machine {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, unique = true)
    private String machineNumber;
    
    @Column(nullable = false)
    private String status; // AVAILABLE, IN_USE
    
    @Column(nullable = false)
    private String location;
    
    private String description;
    
    // traclk user
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "current_user_id")
    private User currentUser;
    
    private LocalDateTime usageStartTime;
    
    @CreatedDate
    private LocalDateTime createdAt;
    
    @LastModifiedDate
    private LocalDateTime updatedAt;
}