# Laundry Booking System# Laundry Booking SystemTO DO THIS WEEK(LOGIN PAGE):



Spring Boot backend for laundry machine booking with authentication.



## Build & RunSpring Boot backend with role-based user authentication, machine booking, and rating system.LOGIN PAGE WILL HAVE 2 WAYS TO LOGIN DIVIDED BY ROLE IN USER.java(manager and student) VALIDATE IF THE USER IS CORRECT WITH THE LOGIN FORM VALIDATE BY name and password in USER.java which info found in user.sql



```bash

cd backend

mvn clean install---1.IF VALIDATION SUCCESSFUL, direct to booking page 2. IF NOT SUCCESSFUL, give error

mvn spring-boot:run

```



Server: `http://localhost:8080`## Quick StartTHERE IS ALSO CREATE ACCOUNT PAGE with the button in login page, THINGS TO BE ASKED FOR CREATION found in user.java:



## Testingprivate String studentId;



### Test Login```bash

```bash

mvn -f backend/pom.xml test -Dtest=AuthControllerTestcd backend@Column(nullable = false)

```

mvn clean installprivate String name;

### Manual Test

```bashmvn spring-boot:run

# Register

curl -X POST http://localhost:8080/api/auth/register \```@Column(nullable = false, unique = true)

  -H "Content-Type: application/json" \

  -d '{"studentId":"S123","name":"Test","email":"test@example.com","password":"pass","role":"STUDENT"}'private String email;



# LoginServer runs on `http://localhost:8080`

curl -X POST http://localhost:8080/api/auth/login \

  -H "Content-Type: application/json" \@Column(nullable = false)

  -d '{"studentId":"S123","password":"pass"}'

```---private String password;



## API



| Method | Endpoint | Description |## Testing Login & Spring Boot@Enumerated(EnumType.STRING)

|--------|----------|-------------|

| POST | `/api/auth/register` | Create account |private Role role;

| POST | `/api/auth/login` | Login |

| GET | `/api/auth/users` | List users |### Run Login TestsTHINGS TO WORK ON:



## Stack```bash



- Spring Boot 2.7.14mvn -f backend/pom.xml test -Dtest=AuthControllerTestCustomer  Create new account Login Front page(Time-slot) Book slot page(which Machine)  Timer page after confirm Completion Page End​ Service Page(rating system)  Machine details Page

- Spring Security + BCrypt

- JUnit 5 + Mockito```

- H2 (test) / MySQL (prod)

- MavenAdmin The money is given Editing Page for Washing Machine Editing Page for Students booked Confirm Page



## TODO### Test Results



- [ ] Login page (studentId or email)- 6 tests totalDatabase :  Booking Payment Confirmed Students Rating Registered Student

- [ ] Account creation page

- [ ] Booking interface- Tests: login success (studentId & email), invalid credentials, missing fields

- [ ] Machine management

- [ ] Rating system- Expected: All pass ✅FRONTEND CODING: AJAX/THYMELEAF/REACTJS

- [ ] Admin panel

### Manual Test with cURL

**Register user:**
```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{"studentId":"S123","name":"Test","email":"test@example.com","password":"pass123","role":"STUDENT"}'
```

**Login with studentId:**
```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"studentId":"S123","password":"pass123"}'
```

**Login with email:**
```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"test@example.com","password":"pass123"}'
```

---

## Development Planning

### LOGIN PAGE (THIS WEEK)
- 2 login methods: studentId OR email
- Password validation from USER.java
- Success: redirect to booking page
- Failure: show error

### CREATE ACCOUNT PAGE
Required fields:
- studentId
- name
- email
- password
- role (STUDENT/MANAGER)

### TO DO
**Customer Interface:**
- Create account
- Login
- Front page (time slots)
- Book slot
- Timer page
- Completion page
- Rating system

**Admin Interface:**
- Payment tracking
- Machine management
- Student bookings
- Confirmation page

**Database:**
- Booking table
- Payment confirmations
- Ratings
- Student registrations

**Frontend:** AJAX / THYMELEAF / REACTJS

---

## API Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/auth/register` | Create account |
| POST | `/api/auth/login` | Login (studentId or email) |
| GET | `/api/auth/users` | List users |

---

## Tech Stack

- Spring Boot 2.7.14
- Spring Security + BCrypt
- JUnit 5 + MockMvc + Mockito
- H2 (testing), MySQL (production)
- Maven

---

## Troubleshooting

```bash
# All tests
mvn -f backend/pom.xml test

# Clean rebuild
mvn -f backend/pom.xml clean install

# Change port
# Edit backend/src/main/resources/application.properties
server.port=8081
```
