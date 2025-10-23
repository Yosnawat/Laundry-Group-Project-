TO DO THIS WEEK(LOGIN PAGE):

LOGIN PAGE WILL HAVE 2 WAYS TO LOGIN DIVIDED BY ROLE IN USER.java(manager and student)
VALIDATE IF THE USER IS CORRECT WITH THE LOGIN FORM VALIDATE BY name and password in USER.java which info found in user.sql

1.IF VALIDATION SUCCESSFUL, direct to booking page
2. IF NOT SUCCESSFUL, give error

THERE IS ALSO CREATE ACCOUNT PAGE with the button in login page,
THINGS TO BE ASKED FOR CREATION found in user.java:  
    private String studentId;
    
    @Column(nullable = false)
    private String name;
    
    @Column(nullable = false, unique = true)
    private String email;
    
    @Column(nullable = false)
    private String password;
    
    @Enumerated(EnumType.STRING)
    private Role role;







THINGS TO WORK ON: 



FRONTEND CODING:
AJAX/THYMELEAF/REACTJS

BACKEND CODING:
JPA/REST API/MVC


    fixed package/import errors, inlined missing model classes (User, Role), and added a BCrypt PasswordEncoder (spring-boot-starter-security)

    Requirements mapping: login supports studentId OR email + password; passwords are BCrypt-encoded on register and verified on login.
    
Notes:
    I inlined model classes so the backend compiles standalone use for localtest if you prefer the original multi-module layout, restore the model module and update the POM.
    I also 


Customer 
Create new account
Login
Front page(Time-slot)
Book slot page(which Machine) 
Timer page after confirm
Completion Page
End​ Service Page(rating system) 
Machine details Page


Admin
The money is given
Editing Page for Washing Machine
Editing Page for Students booked
Confirm Page


Database : 
Booking
Payment Confirmed Students
Rating
Registered Student


