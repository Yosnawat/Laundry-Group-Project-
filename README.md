TO DO THIS WEEK(LOGIN PAGE):

LOGIN PAGE WILL HAVE 2 WAYS TO LOGIN DIVIDED BY ROLE IN USER.java(manager and student) VALIDATE IF THE USER IS CORRECT WITH THE LOGIN FORM VALIDATE BY name and password in USER.java which info found in user.sql

1.IF VALIDATION SUCCESSFUL, direct to booking page 2. IF NOT SUCCESSFUL, give error

THERE IS ALSO CREATE ACCOUNT PAGE with the button in login page, THINGS TO BE ASKED FOR CREATION found in user.java:
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

Customer  Create new account Login Front page(Time-slot) Book slot page(which Machine)  Timer page after confirm Completion Page End​ Service Page(rating system)  Machine details Page

Admin The money is given Editing Page for Washing Machine Editing Page for Students booked Confirm Page

Database :  Booking Payment Confirmed Students Rating Registered Student

FRONTEND CODING: AJAX/THYMELEAF/REACTJS

## Testing

### Running Tests

#### Backend Tests
To run all backend tests:
```bash
mvn -f backend/pom.xml test
```

To run a specific test class:
```bash
mvn -f backend/pom.xml test -Dtest=AuthControllerTest
```

To run tests with a specific pattern:
```bash
mvn -f backend/pom.xml test -Dtest=*Controller*
```

#### Test Configuration
- Test configuration file: `backend/src/test/resources/application-test.properties`
- Test classes are located in: `backend/src/test/java/`

#### Current Tests
- **AuthControllerTest**: Tests for authentication controller endpoints and user login/registration functionality

### Test Reports
After running tests, reports are generated in:
- `backend/target/surefire-reports/` - Test execution reports and XML results