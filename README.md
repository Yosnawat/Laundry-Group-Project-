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



FRONTEND CODING:
AJAX/THYMELEAF/REACTJS

# Laundry Group — backend 

Key points
- Login accepts either `studentId` OR `email` plus a `password`.
- Passwords are BCrypt-hashed on register (via `PasswordEncoder`) and verified on login.

Quick start — build and run

```bash
cd backend
# build jar
mvn -DskipTests package

# run (development)
mvn -DskipTests spring-boot:run

# or run the packaged jar
java -jar target/backend-1.0-SNAPSHOT.jar
```

The server listens on port 8080 by default. Watch the console for Spring Boot startup lines (Hibernate DDL, "Started", Tomcat port).

Quick functional tests (use a second terminal)

- Register a user
```bash
curl -i -X POST http://localhost:8080/api/auth/register \
    -H "Content-Type: application/json" \
    -d '{"studentId":"S123","name":"Test User","email":"test@example.com","password":"secret","role":"STUDENT"}'
```

- Login by studentId
```bash
curl -i -X POST http://localhost:8080/api/auth/login \
    -H "Content-Type: application/json" \
    -d '{"studentId":"S123","password":"secret"}'
```

- Login by email
```bash
curl -i -X POST http://localhost:8080/api/auth/login \
    -H "Content-Type: application/json" \
    -d '{"email":"test@example.com","password":"secret"}'
```

Expected responses
- Register: HTTP 200 with JSON {"status":"ok","id":...}
- Login: HTTP 200 with JSON {"status":"ok","userId":..., "studentId":..., "name":..., "role":...}

Notes for developers
- If you see 401 with `WWW-Authenticate: Basic realm="Realm"`, Spring Security is protecting the endpoint — restart the server after adding `SecurityConfig` (we included a permissive config for `/api/auth/**`).
- If startup fails because of SQL inserts (application tries to run `user.sql` before tables exist), the project is configured to let Hibernate update the schema (`spring.jpa.hibernate.ddl-auto=update`) and defer datasource initialization.
- A temporary debug endpoint exists at `GET /api/auth/debug/users` (returns users without passwords) to help verify registration; remove it before production.


```

Quick tests (use a separate terminal while the server is running):

