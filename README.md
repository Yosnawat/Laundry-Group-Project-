# Laundry Booking System# Laundry Booking SystemTO DO THIS WEEK(LOGIN PAGE):



Spring Boot backend with role-based user authentication, machine booking, and rating system.



---Spring Boot backend with role-based user authentication, machine booking, and rating system.LOGIN PAGE WILL HAVE 2 WAYS TO LOGIN DIVIDED BY ROLE IN USER.java(manager and student)



## Quick StartVALIDATE IF THE USER IS CORRECT WITH THE LOGIN FORM VALIDATE BY name and password in USER.java which info found in user.sql



### Build & Run Backend## Development Planning

```bash

cd backend1.IF VALIDATION SUCCESSFUL, direct to booking page

mvn clean install

mvn spring-boot:run### TO DO THIS WEEK (LOGIN PAGE)2. IF NOT SUCCESSFUL, give error

```

Backend runs on `http://localhost:8080`



### Build Frontend**Login Page Requirements:**THERE IS ALSO CREATE ACCOUNT PAGE with the button in login page,

```bash

cd frontend- LOGIN PAGE WILL HAVE 2 WAYS TO LOGIN DIVIDED BY ROLE IN USER.java (manager and student)THINGS TO BE ASKED FOR CREATION found in user.java:  

# Configure CORS settings in backend application.properties

```- VALIDATE IF THE USER IS CORRECT WITH THE LOGIN FORM VALIDATE BY name and password in USER.java which info found in user.sql    private String studentId;



---    



## Testing Guide**Login Flow:**    @Column(nullable = false)



### How to Test Login & Spring Boot1. IF VALIDATION SUCCESSFUL, direct to booking page    private String name;



#### 1. Run All Tests2. IF NOT SUCCESSFUL, give error    

```bash

mvn -f backend/pom.xml test    @Column(nullable = false, unique = true)

```

This runs all test classes in the project.**Create Account Page:**    private String email;



#### 2. Run Only Login Tests- THERE IS ALSO CREATE ACCOUNT PAGE with the button in login page    

```bash

mvn -f backend/pom.xml test -Dtest=AuthControllerTest- THINGS TO BE ASKED FOR CREATION found in user.java:    @Column(nullable = false)

```

This runs the 6 comprehensive login authentication tests.  - `private String studentId;`    private String password;



#### 3. Run a Single Test Method  - `@Column(nullable = false) private String name;`    

```bash

mvn -f backend/pom.xml test -Dtest=AuthControllerTest#testLoginSuccess_WithStudentId  - `@Column(nullable = false, unique = true) private String email;`    @Enumerated(EnumType.STRING)

```

Replace `testLoginSuccess_WithStudentId` with any test method name:  - `@Column(nullable = false) private String password;`    private Role role;

- `testLoginSuccess_WithStudentId` - Login with student ID

- `testLoginSuccess_WithEmail` - Login with email  - `@Enumerated(EnumType.STRING) private Role role;`

- `testLoginFailure_InvalidCredentials` - Invalid credentials (should fail)

- `testLoginValidation_MissingStudentIdAndEmail` - Missing ID/email validation

- `testLoginValidation_MissingPassword` - Missing password validation

- `testLoginValidation_NullPassword` - Null password validation---



#### 4. Run Tests with Verbose Output

```bash

mvn -f backend/pom.xml test -Dtest=AuthControllerTest -X### THINGS TO WORK ON

```

Shows detailed debug information for troubleshooting.



#### 5. Run Tests and Skip Compilation**Customer Interface:**

```bash

mvn -f backend/pom.xml test -Dtest=AuthControllerTest -DskipTests=false- Create new accountTHINGS TO WORK ON: 

```

- Login

#### 6. View Test Results

```bash- Front page (Time-slot)Customer 

# After running tests, view detailed results

cat backend/target/surefire-reports/AuthControllerTest.txt- Book slot page (which Machine)Create new account



# View XML results- Timer page after confirmLogin

cat backend/target/surefire-reports/TEST-AuthControllerTest.xml

```- Completion PageFront page(Time-slot)



### Manual Testing with cURL- End Service Page (rating system)Book slot page(which Machine) 



#### 1. Register a User- Machine details PageTimer page after confirm

```bash

curl -i -X POST http://localhost:8080/api/auth/register \Completion Page

    -H "Content-Type: application/json" \

    -d '{"studentId":"S123","name":"Test User","email":"test@example.com","password":"secret","role":"STUDENT"}'**Admin Interface:**End​ Service Page(rating system) 

```

- The money is givenMachine details Page

Expected Response (200 OK):

```json- Editing Page for Washing Machine

{"status":"ok","id":1}

```- Editing Page for Students booked



#### 2. Login with Student ID- Confirm PageAdmin

```bash

curl -i -X POST http://localhost:8080/api/auth/login \The money is given

    -H "Content-Type: application/json" \

    -d '{"studentId":"S123","password":"secret"}'**Database:**Editing Page for Washing Machine

```

- BookingEditing Page for Students booked

Expected Response (200 OK):

```json- Payment Confirmed StudentsConfirm Page

{"id":1,"studentId":"S123","name":"Test User","email":"test@example.com","role":"STUDENT"}

```- Rating



#### 3. Login with Email- Registered Student

```bash

curl -i -X POST http://localhost:8080/api/auth/login \Database : 

    -H "Content-Type: application/json" \

    -d '{"email":"test@example.com","password":"secret"}'**Frontend Coding:**Booking

```

- AJAX / THYMELEAF / REACTJSPayment Confirmed Students

Expected Response (200 OK):

```jsonRating

{"id":1,"studentId":"S123","name":"Test User","email":"test@example.com","role":"STUDENT"}

```---Registered Student



#### 4. Failed Login (Invalid Credentials)

```bash

curl -i -X POST http://localhost:8080/api/auth/login \## Quick Start

    -H "Content-Type: application/json" \

    -d '{"studentId":"S123","password":"wrongpassword"}'

```

### Build & Run BackendFRONTEND CODING:

Expected Response (401 Unauthorized):

```json```bashAJAX/THYMELEAF/REACTJS

{"error":"Invalid credentials"}

```cd backend



#### 5. Failed Login (Missing Password)mvn clean install# Laundry Group — backend 

```bash

curl -i -X POST http://localhost:8080/api/auth/login \mvn spring-boot:run

    -H "Content-Type: application/json" \

    -d '{"studentId":"S123","password":""}'```Key points

```

Backend runs on `http://localhost:8080`- Login accepts either `studentId` OR `email` plus a `password`.

Expected Response (400 Bad Request):

```json- Passwords are BCrypt-hashed on register (via `PasswordEncoder`) and verified on login.

{"error":"password required"}

```### Build Frontend



---```bashQuick start — build and run



## Test Suite: AuthControllerTestcd frontend



**Status:** ✅ All 6 tests passing# Configure CORS settings in backend application.properties```bash



| Test | Scenario | Expected |```cd backend

|------|----------|----------|

| `testLoginSuccess_WithStudentId` | Valid studentId login | 200 OK |# build jar

| `testLoginSuccess_WithEmail` | Valid email login | 200 OK |

| `testLoginFailure_InvalidCredentials` | Wrong password | 401 Unauthorized |---mvn -DskipTests package

| `testLoginValidation_MissingStudentIdAndEmail` | No ID/email | 400 Bad Request |

| `testLoginValidation_MissingPassword` | No password | 400 Bad Request |

| `testLoginValidation_NullPassword` | Null password | 400 Bad Request |

## Testing# run (development)

---

mvn -DskipTests spring-boot:run

## API Endpoints

### Run All Tests

### Authentication (`/api/auth`)

| Method | Endpoint | Description |```bash# or run the packaged jar

|--------|----------|-------------|

| POST | `/api/auth/login` | Login with studentId/email + password |mvn -f backend/pom.xml testjava -jar target/backend-1.0-SNAPSHOT.jar

| POST | `/api/auth/register` | Create new account |

| GET | `/api/auth/users` | List all users |``````



---



## Project Structure### Run Specific Test ClassThe server listens on port 8080 by default. Watch the console for Spring Boot startup lines (Hibernate DDL, "Started", Tomcat port).



``````bash

backend/src/main/java/

├── App.java                    # Main applicationmvn -f backend/pom.xml test -Dtest=AuthControllerTestQuick functional tests (use a second terminal)

├── AuthController.java         # Login/auth endpoints

├── MachineController.java      # Machine endpoints```

├── UserController.java         # User management

├── ViewController.java         # View endpoints- Register a user

├── config/                     # Spring config

│   ├── CorsConfig.java### Run Single Test Method```bash

│   ├── PasswordConfig.java

│   └── SecurityConfig.java```bashcurl -i -X POST http://localhost:8080/api/auth/register \

├── model/                      # Entities

│   ├── User.javamvn -f backend/pom.xml test -Dtest=AuthControllerTest#testLoginSuccess_WithStudentId    -H "Content-Type: application/json" \

│   ├── Machine.java

│   ├── Rating.java```    -d '{"studentId":"S123","name":"Test User","email":"test@example.com","password":"secret","role":"STUDENT"}'

│   ├── Role.java

│   └── AppConstants.java```

├── repo/                       # Data repositories

│   ├── UserRepository.java### View Test Results

│   ├── MachineRepository.java

│   └── RatingRepository.java```bash- Login by studentId

└── service/                    # Business logic

    ├── UserService.java# After running tests, view results```bash

    ├── MachineService.java

    └── RatingService.javacat backend/target/surefire-reports/AuthControllerTest.txtcurl -i -X POST http://localhost:8080/api/auth/login \



backend/src/test/java/```    -H "Content-Type: application/json" \

├── AuthControllerTest.java     # 6 login tests

└── resources/    -d '{"studentId":"S123","password":"secret"}'

    └── application-test.properties   # Test config (H2)

```### Test Output```



---```



## Technology StackTests run: 6, Failures: 0, Errors: 0, Skipped: 0- Login by email



- **Backend:** Spring Boot 2.7.14BUILD SUCCESS```bash

- **Database:** H2 (in-memory for testing), MySQL (production)

- **Security:** Spring Security + BCrypt```curl -i -X POST http://localhost:8080/api/auth/login \

- **Testing:** JUnit 5 + MockMvc + Mockito

- **Build:** Maven    -H "Content-Type: application/json" \

- **ORM:** Hibernate + Spring Data JPA

---    -d '{"email":"test@example.com","password":"secret"}'

---

```

## Features

## API Endpoints

✅ User authentication (studentId or email)  

✅ Role-based access (STUDENT, MANAGER)  Expected responses

✅ BCrypt password encryption  

✅ 6 comprehensive login tests  ### Authentication (`/api/auth`)- Register: HTTP 200 with JSON {"status":"ok","id":...}

✅ CORS-enabled API  

✅ H2 database for testing  | Method | Endpoint | Description |- Login: HTTP 200 with JSON {"status":"ok","userId":..., "studentId":..., "name":..., "role":...}



---|--------|----------|-------------|



## Development Planning| POST | `/api/auth/login` | Login with studentId/email + password |Notes for developers



### TO DO THIS WEEK (LOGIN PAGE)| POST | `/api/auth/register` | Create new account |- If you see 401 with `WWW-Authenticate: Basic realm="Realm"`, Spring Security is protecting the endpoint — restart the server after adding `SecurityConfig` (we included a permissive config for `/api/auth/**`).



**Login Page Requirements:**| GET | `/api/auth/users` | List all users |- If startup fails because of SQL inserts (application tries to run `user.sql` before tables exist), the project is configured to let Hibernate update the schema (`spring.jpa.hibernate.ddl-auto=update`) and defer datasource initialization.

- LOGIN PAGE WILL HAVE 2 WAYS TO LOGIN DIVIDED BY ROLE IN USER.java (manager and student)

- VALIDATE IF THE USER IS CORRECT WITH THE LOGIN FORM VALIDATE BY name and password in USER.java which info found in user.sql- A temporary debug endpoint exists at `GET /api/auth/debug/users` (returns users without passwords) to help verify registration; remove it before production.



**Login Flow:**### Login Example

1. IF VALIDATION SUCCESSFUL, direct to booking page

2. IF NOT SUCCESSFUL, give error**Request:**



**Create Account Page:**```json```

- THERE IS ALSO CREATE ACCOUNT PAGE with the button in login page

- THINGS TO BE ASKED FOR CREATION found in user.java:{

  - `private String studentId;`

  - `@Column(nullable = false) private String name;`  "studentId": "S12345",Quick tests (use a separate terminal while the server is running):

  - `@Column(nullable = false, unique = true) private String email;`

  - `@Column(nullable = false) private String password;`  "password": "password123"

  - `@Enumerated(EnumType.STRING) private Role role;`

}

### THINGS TO WORK ON```



**Customer Interface:****Success Response (200):**

- Create new account```json

- Login{

- Front page (Time-slot)  "id": 1,

- Book slot page (which Machine)  "studentId": "S12345",

- Timer page after confirm  "name": "John Doe",

- Completion Page  "role": "STUDENT"

- End Service Page (rating system)}

- Machine details Page```



**Admin Interface:****Error Response (401):**

- The money is given```json

- Editing Page for Washing Machine{

- Editing Page for Students booked  "error": "Invalid credentials"

- Confirm Page}

```

**Database:**

- Booking---

- Payment Confirmed Students

- Rating## Test Suite: AuthControllerTest

- Registered Student

**Status:** ✅ All 6 tests passing

**Frontend Coding:**

- AJAX / THYMELEAF / REACTJS| Test | Scenario | Expected |

|------|----------|----------|

---| `testLoginSuccess_WithStudentId` | Valid studentId login | 200 OK |

| `testLoginSuccess_WithEmail` | Valid email login | 200 OK |

## Troubleshooting| `testLoginFailure_InvalidCredentials` | Wrong password | 401 Unauthorized |

| `testLoginValidation_MissingStudentIdAndEmail` | No ID/email | 400 Bad Request |

**Tests failing?**| `testLoginValidation_MissingPassword` | No password | 400 Bad Request |

```bash| `testLoginValidation_NullPassword` | Null password | 400 Bad Request |

mvn -f backend/pom.xml clean test

```---



**Port 8080 already in use?**## Project Structure

```bash

# Change in backend/src/main/resources/application.properties```

server.port=8081backend/src/main/java/

```├── App.java                    # Main application

├── AuthController.java         # Login/auth endpoints

**H2 Database issues?**├── MachineController.java      # Machine endpoints

```bash├── UserController.java         # User management

# Reset database on startup├── ViewController.java         # View endpoints

spring.jpa.hibernate.ddl-auto=create-drop├── config/                     # Spring config

```├── model/                      # Entities (User, Machine, Rating)

├── repo/                       # Data repositories

**Spring Boot won't start?**└── service/                    # Business logic

```bash

# Check logs for errorsbackend/src/test/java/

mvn -f backend/pom.xml spring-boot:run -X└── AuthControllerTest.java     # 6 login tests

``````


---

## Technology Stack

- **Backend:** Spring Boot 2.7.14
- **Database:** H2 (in-memory)
- **Security:** Spring Security + BCrypt
- **Testing:** JUnit 5 + MockMvc + Mockito
- **Build:** Maven

---

## Features

✅ User authentication (studentId or email)  
✅ Role-based access (STUDENT, MANAGER)  
✅ BCrypt password encryption  
✅ 6 comprehensive login tests  
✅ CORS-enabled API  
✅ H2 database with JPA  

---

## Troubleshooting

**Tests failing?**
```bash
mvn -f backend/pom.xml clean test
```

**Port in use?**
```bash
# Change in application.properties
server.port=8081
```
