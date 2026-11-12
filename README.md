# Emergency Blood Request and Donor Coordination System

> **Academic College Project Documentation**  
> Dynamic Web Application developed using **Java Servlet, JSP, JDBC, and MySQL 8.0**.  
> Architecture: **MVC (Model-View-Controller)** with **DAO (Data Access Object)** pattern.  
> Target Container: **Apache Tomcat 10.1** (Jakarta EE 10 / Servlet 6.0).  

---

## 1. Project Introduction

The **Emergency Blood Request and Donor Coordination System** (`BloodConnect`) is a dynamic healthcare coordination web platform developed for academic curriculum presentation. It bridges the critical time gap during medical emergencies by intelligently connecting hospitals, patients, and eligible voluntary blood donors.

The application adheres strictly to standard Java Enterprise architecture without third-party frameworks like Spring Boot or client-side single-page applications like React or Angular, making it beginner-friendly, clean, and straightforward to explain in a college technical viva.

---

## 2. Project Objectives

1. **Expedite Emergency Matching:** Quickly identify clinically compatible blood donors based on recipient blood groups and geographical proximity.
2. **Eliminate Coordination Delays:** Enable voluntary blood donors to review pending emergencies and submit instant responses with contact details.
3. **Prevent Ineligible Matching:** Filter donors by age (18–65), active availability status, and clinical compatibility matrix.
4. **Maintain Transparent Clinical Audit Trails:** Record official completed donations and units donated while strictly prohibiting destructive deletions of historical medical records.
5. **Role-Based Operational Security:** Provide isolated dashboards and permission barriers for Administrators, Voluntary Donors, and Emergency Requesters.
6. **Real-Time Communication:** Deliver in-app notification alerts for new blood requests, donor responses, and administrative status updates.
7. **Clinical Analytics:** Supply comprehensive visual reports for blood group distributions, requisition fulfillment rates, and monthly collection volumes.

---

## 3. Technologies Used

| Layer | Technology | Specification / Version | Purpose |
| :--- | :--- | :--- | :--- |
| **Language** | Java | Java 17 LTS | Core server-side programming language |
| **Controller** | Java Servlet | Jakarta Servlet 6.0 (`jakarta.servlet.*`) | HTTP request routing, input validation, session management |
| **View (UI)** | JSP & JSTL | JSP 3.1 / JSTL 3.0 | Dynamic presentation layer with server-side rendering |
| **Persistence** | JDBC | Standard JDBC 4.3 | Parameterized database transactions via PreparedStatement |
| **Database** | MySQL | MySQL 8.0 Server | Relational database management system |
| **Styling** | CSS3 & Bootstrap | Bootstrap 5.3.3 | Healthcare UI design, responsive grid, rounded cards |
| **Icons** | Bootstrap Icons | Bootstrap Icons 1.11.3 | Standardized medical and navigational icons |
| **Scripting** | JavaScript | ECMAScript 6+ | Dynamic client-side validation and role toggles |
| **Analytics** | Chart.js | Chart.js 4.4 | Clean client-side canvas charts for administrative reports |
| **Server** | Apache Tomcat | Tomcat 10.1.x | Jakarta EE 10 compliant Servlet/JSP container |
| **Build Tool** | Apache Maven | Maven 3.9+ | Dependency management, testing, and WAR generation |

---

## 4. Software Requirements

- **Operating System:** Windows 10/11, Linux (Ubuntu/Debian/CentOS), or macOS
- **Java Development Kit (JDK):** JDK 17 LTS or newer
- **Web Server:** Apache Tomcat 10.1.x
- **Database Server:** MySQL 8.0 Server (or MySQL 8.3/8.4)
- **Database Client:** MySQL Workbench 8.0 or MySQL Command Line Client
- **IDE:** Eclipse IDE for Enterprise Java and Web Developers (or IntelliJ IDEA / VS Code)
- **Build Tool:** Apache Maven 3.8+

---

## 5. Hardware Requirements

- **Processor:** Dual-Core 2.0 GHz or higher (Quad-Core recommended)
- **RAM:** Minimum 4 GB RAM (8 GB recommended for running IDE, Tomcat, and MySQL concurrently)
- **Storage:** Minimum 500 MB free hard disk space for project source code, database, and logs
- **Display:** 1366 x 768 minimum screen resolution (1080p Full HD recommended)

---

## 6. System Architecture

The application is structured using the classical **Model-View-Controller (MVC)** architectural pattern combined with the **Data Access Object (DAO)** pattern:

```
+-----------------------------------------------------------------------------------+
|                                PRESENTATION (VIEW)                                |
|  JSP Pages: index.jsp, login.jsp, register.jsp, search-donors.jsp,                |
|             donor-dashboard.jsp, requester-dashboard.jsp, admin-dashboard.jsp,     |
|             donor-profile.jsp, blood-request.jsp, donation-history.jsp,           |
|             notifications.jsp, admin-reports.jsp                                  |
+----------------------------------------+------------------------------------------+
                                         | HTTP GET / POST
                                         v
+-----------------------------------------------------------------------------------+
|                              CONTROLLER TIER (SERVLETS)                           |
|  Filters: AuthenticationFilter, CharacterEncodingFilter                           |
|  Servlets: LoginServlet, LogoutServlet, RegisterServlet, BloodRequestServlet,    |
|            DonorProfileServlet, DonorSearchServlet, DonorResponseServlet,         |
|            DonationHistoryServlet, NotificationServlet, AdminReportServlet        |
+----------------------------------------+------------------------------------------+
                                         | Java Method Calls
                                         v
+-----------------------------------------------------------------------------------+
|                              MODEL & BUSINESS LOGIC                               |
|  POJO Models: User, Donor, BloodRequest, DonorResponse, Donation, Notification    |
|  Utilities: DBConnectionUtil, PasswordUtil (SHA-256)                              |
+----------------------------------------+------------------------------------------+
                                         | Parameterized Queries (PreparedStatement)|
                                         v
+-----------------------------------------------------------------------------------+
|                           DATA ACCESS LAYER (DAO PATTERN)                         |
|  DAOs: UserDAO, DonorDAO, BloodRequestDAO, DonorResponseDAO,                      |
|        DonationDAO, NotificationDAO                                               |
+----------------------------------------+------------------------------------------+
                                         | JDBC Driver Connection
                                         v
+-----------------------------------------------------------------------------------+
|                             DATABASE: MySQL 8.0                                   |
|  Tables: users, donors, blood_requests, donor_responses,                          |
|          donations, blood_compatibility, notifications                            |
+-----------------------------------------------------------------------------------+
```

---

## 7. Database Design

The database schema (`blood_donor_db`) comprises **7 interrelated tables** that maintain relational integrity via primary keys, foreign keys, cascading constraints, and lookup indices.

```
+------------------+         1:1         +------------------+
|      users       |-------------------->|      donors      |
+------------------+                     +------------------+
| id (PK)          |                     | id (PK)          |
| full_name        |                     | user_id (FK, UQ) |
| email (UQ)       |                     | blood_group      |
| password         |                     | age, gender      |
| phone            |                     | city, state      |
| role             |                     | is_available     |
| status           |                     | total_donations  |
+------------------+                     +------------------+
        |                                         |
        | 1:N                                     | 1:N
        v                                         v
+------------------+                     +------------------+
|  blood_requests  |                     |  donor_responses |
+------------------+                     +------------------+
| id (PK)          |                     | id (PK)          |
| requester_id(FK) |<--------------------| request_id (FK)  |
| patient_name     |       1:N           | donor_id (FK)    |
| blood_group      |                     | status           |
| units_needed     |                     | message          |
| hospital_name    |                     +------------------+
| hospital_city    |                              |
| urgency_level    |                              |
| status           |                              |
| required_date    |                              |
+------------------+                              |
        |                                         |
        | 1:N (Optional Link)                     | 1:N
        v                                         v
+-----------------------------------------------------------+
|                         donations                         |
+-----------------------------------------------------------+
| id (PK), donor_id (FK), request_id (FK, NULLABLE)         |
| units_donated, donation_date, hospital_or_camp, status    |
+-----------------------------------------------------------+

+-----------------------------+         +-----------------------------+
|     blood_compatibility     |         |        notifications        |
+-----------------------------+         +-----------------------------+
| donor_blood_group (PK)      |         | id (PK)                     |
| recipient_blood_group (PK)  |         | user_id (FK)                |
+-----------------------------+         | title, message, type        |
                                        | is_read, created_at         |
                                        +-----------------------------+
```

### Table Definitions
1. **`users`:** Stores core credentials, SHA-256 hashed passwords, contact telephone numbers, and roles (`ADMIN`, `DONOR`, `REQUESTER`).
2. **`donors`:** Voluntary blood donor profiles linked 1:1 with `users`. Tracks blood group, age, gender, location, emergency availability, and donation count.
3. **`blood_requests`:** Emergency requisitions submitted by requesters. Tracks patient name, blood group, units, hospital, urgency (`NORMAL`, `URGENT`, `CRITICAL`), status (`PENDING`, `MATCHED`, `FULFILLED`, `CANCELLED`), and required date.
4. **`donor_responses`:** Responses submitted by voluntary donors to blood requests (`ACCEPTED`, `REJECTED`, `PENDING`).
5. **`donations`:** Official clinical audit history recording units donated, date, hospital/camp, and status (`COMPLETED`, `SCHEDULED`, `CANCELLED`).
6. **`blood_compatibility`:** Cross-compatibility matrix mapping donor blood groups to recipient blood groups (supports clinical queries including universal donors and recipients).
7. **`notifications`:** In-app emergency notifications alert messages for matched donors and status updates.

---

## 8. Module Descriptions

### Module 1: Authentication & User Management
- Dual-role registration for Donors and Requesters.
- Client and server-side validation for email syntax, password confirmation, minimum length, and mobile digits.
- Prevention of duplicate email registrations.
- Password encryption using cryptographic SHA-256 hashing.
- Role-based redirect upon login (`ADMIN` &rarr; `admin-dashboard.jsp`, `DONOR` &rarr; `donor-dashboard.jsp`, `REQUESTER` &rarr; `requester-dashboard.jsp`).
- Complete session invalidation and browser cache disabling on logout.

### Module 2: Donor Management
- Complete profile view and management for registered donors.
- Editable personal, demographic, medical, and location fields.
- Emergency availability toggle (`Available` vs `Inactive`) enabling soft deactivation while preserving donation records.
- Flexible multi-criteria donor search by blood group, city, and active availability.

### Module 3: Emergency Blood Request Management
- Submission of emergency blood requisitions by authenticated Requesters.
- Validation of patient details, units needed (1–20 units), hospital address, urgency level, and date.
- Requester-specific dashboard displaying pending, matched, and fulfilled requests.
- Soft cancellation of pending requests without deleting audit history.
- Prevention of cross-user unauthorized access.

### Module 4: Intelligent Donor Matching & Responses
- Automated preliminary matching using the clinical cross-compatibility matrix and hospital city locality.
- Donor portal allowing voluntary donors to review matching requests and accept or decline with notes.
- Prevention of duplicate responses from the same donor for the same requisition.
- Automatic status advancement (`PENDING` &rarr; `MATCHED`) upon donor acceptance.

### Module 5: Donation History Management
- Administrative logging of verified blood donations linked to donors and emergency requests.
- Automatic synchronization of donor's `total_donations` count and `last_donation_date`.
- Automatic advancement of request status to `FULFILLED` upon donation completion.
- Donor dashboard displaying private donation logs, units donated, and estimated lives saved ($1\text{ unit} \approx 3\text{ lives}$).
- Soft cancellation (`CANCELLED`) for historical compliance.

### Module 6: Notification Management
- In-app notification center for all user roles.
- Triggers:
  1. Successful donor registration welcome.
  2. New blood request submission (notifies requester and broadcasts to compatible donors).
  3. Donor accepting a blood request (notifies requester with donor contact details).
  4. Donor declining a blood request.
  5. Administrative requisition status transitions.
  6. Verified blood donation completion.
- Dynamic unread badge counter in the navigation bar.
- Individual and bulk "Mark as Read" actions with user data privacy isolation.

### Module 7: Administrative Dashboard & Reports
- Real-time display of 7 dynamic statistics retrieved directly from MySQL:
  1. Total Registered Donors
  2. Total Requesters
  3. Total Blood Requests
  4. Pending Blood Requests
  5. Matched Blood Requests
  6. Completed Donations
  7. Total Units Donated
- Live Recent Activities stream: recently registered donors, recently submitted requests, latest donor responses, and recently completed donations.
- Comprehensive Analytical Reports:
  - Report 1: Donors by Blood Group (table and Chart.js bar chart).
  - Report 2: Blood Requests by Status (table and Chart.js doughnut chart).
  - Report 3: Monthly Donation Volume and Units Collected (table and Chart.js trend chart).
  - Printer-ready layout (`window.print()`) for laboratory evaluation and viva demonstrations.

---

## 9. Project Folder Structure

```
emergency-blood-coordination/
├── pom.xml                                           # Maven build descriptor (Java 17, Servlet 6, JUnit 5)
├── database.sql                                      # Complete MySQL 8.0 schema and seed dataset
├── README.md                                         # Project documentation
└── src/
    ├── main/
    │   ├── java/
    │   │   └── com/blooddonation/
    │   │       ├── controller/                       # Controller Layer (Servlets)
    │   │       │   ├── AdminReportServlet.java       # Reports controller (/reports)
    │   │       │   ├── BloodRequestServlet.java      # Request management (/blood-request)
    │   │       │   ├── DonationHistoryServlet.java   # Donation audit controller (/donation-history)
    │   │       │   ├── DonorProfileServlet.java      # Donor profile controller (/donor/profile)
    │   │       │   ├── DonorResponseServlet.java     # Response controller (/donor-response)
    │   │       │   ├── DonorSearchServlet.java       # Search controller (/donors/search)
    │   │       │   ├── HomeController.java           # Landing controller (/)
    │   │       │   ├── LoginServlet.java             # Authentication controller (/login)
    │   │       │   ├── LogoutServlet.java            # Sign-out controller (/logout)
    │   │       │   ├── NotificationServlet.java      # Notification controller (/notifications)
    │   │       │   └── RegisterServlet.java          # Registration controller (/register)
    │   │       ├── dao/                              # Data Access Layer (PreparedStatement DAO)
    │   │       │   ├── BloodRequestDAO.java          # Requests database operations
    │   │       │   ├── DonationDAO.java              # Donations database operations
    │   │       │   ├── DonorDAO.java                 # Donors database operations
    │   │       │   ├── DonorResponseDAO.java         # Responses database operations
    │   │       │   ├── NotificationDAO.java          # Notifications database operations
    │   │       │   └── UserDAO.java                  # User database operations
    │   │       ├── filter/                           # Filter Layer
    │   │       │   ├── AuthenticationFilter.java     # Role-based security & cache control
    │   │       │   └── CharacterEncodingFilter.java  # UTF-8 encoding filter
    │   │       ├── model/                            # Model Layer (POJOs)
    │   │       │   ├── BloodRequest.java
    │   │       │   ├── Donation.java
    │   │       │   ├── Donor.java
    │   │       │   ├── DonorResponse.java
    │   │       │   ├── Notification.java
    │   │       │   ├── Role.java
    │   │       │   └── User.java
    │   │       └── util/                             # Utility Layer
    │   │           ├── DBConnectionUtil.java         # Centralized JDBC connection manager
    │   │           └── PasswordUtil.java             # SHA-256 cryptographic utility
    │   ├── resources/
    │   │   └── db.properties                         # Centralized database configuration
    │   └── webapp/                                   # View Layer (JSP Pages & Static Assets)
    │       ├── admin-dashboard.jsp                   # Admin overview & live activities
    │       ├── admin-reports.jsp                     # Analytical charts & tables
    │       ├── blood-request.jsp                     # Emergency request form & editor
    │       ├── donation-history.jsp                  # Donation logs & record modal
    │       ├── donor-dashboard.jsp                   # Donor dashboard & matches
    │       ├── donor-profile.jsp                     # Donor profile management
    │       ├── index.jsp                             # Landing home page
    │       ├── login.jsp                             # Login page
    │       ├── notifications.jsp                     # User notifications center
    │       ├── register.jsp                          # Registration page
    │       ├── requester-dashboard.jsp               # Requester tracking dashboard
    │       ├── search-donors.jsp                     # Public & donor search registry
    │       ├── includes/
    │       │   ├── footer.jsp                        # Standardized healthcare footer
    │       │   ├── header.jsp                        # Stylesheet & font includes
    │       │   └── navbar.jsp                        # Role-aware sticky navigation bar
    │       ├── css/
    │       │   └── style.css                         # Healthcare theme styling
    │       ├── js/
    │       │   └── main.js                           # Form validation scripts
    │       └── WEB-INF/
    │           └── web.xml                           # Servlet 6.0 deployment descriptor
    └── test/
        └── java/
            └── com/blooddonation/test/               # Integration Test Suites
                ├── AdminAndNotificationManagementTest.java
                ├── AuthenticationUserManagementTest.java
                ├── BloodRequestManagementTest.java
                ├── DatabaseDAOTest.java
                ├── DonationHistoryManagementTest.java
                ├── DonorManagementTest.java
                ├── DonorMatchingAndResponseTest.java
                └── EndToEndWorkflowTest.java
```

---

## 10. Database Setup Instructions

### Option A: Using MySQL Workbench 8.0
1. Open **MySQL Workbench** and connect to your local MySQL instance (`localhost:3306`).
2. Go to **File &rarr; Open SQL Script...** (or press `Ctrl + Shift + O`).
3. Select `emergency-blood-coordination/database.sql` and click **Open**.
4. Click the **Execute (Execute entire query)** button (lightning icon).
5. Refresh the **SCHEMAS** panel in the left sidebar; you will see `blood_donor_db` with all 7 tables populated with seed data.

### Option B: Using MySQL Command Line Client
```powershell
mysql -u root -p < "C:\Users\Pavithra R\.gemini\antigravity\scratch\emergency-blood-coordination\database.sql"
```

---

## 11. JDBC Configuration

Database connection settings are centralized in [`src/main/resources/db.properties`](file:///src/main/resources/db.properties):

```properties
db.url=jdbc:mysql://localhost:3306/blood_donor_db?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true
db.user=root
db.password=Pavi@123
db.driver=com.mysql.cj.jdbc.Driver
```

[`DBConnectionUtil.java`](file:///src/main/java/com/blooddonation/util/DBConnectionUtil.java) reads these properties using the Java ClassLoader, providing robust database connectivity with zero hardcoding.

---

## 12. How to Run the Project

### Building the Project
From the project root directory, run:
```powershell
mvn clean test
mvn package
```
This compiles the code, executes all integration tests, and produces the deployment archive:
`target/emergency-blood-coordination.war`

### Running in Eclipse IDE
1. Open **Eclipse IDE for Enterprise Java and Web Developers**.
2. Select **File &rarr; Import... &rarr; Maven &rarr; Existing Maven Projects**.
3. Browse to `emergency-blood-coordination` and click **Finish**.
4. Right-click project &rarr; **Run As &rarr; Run on Server**.
5. Select **Apache Tomcat v10.1 Server** (configured with Java 17).
6. Open your web browser and navigate to:
   `http://localhost:8080/emergency-blood-coordination/`

---

## 13. Login Credentials for Demonstration

All sample passwords use SHA-256 cryptographic hashing:

| Role | Name | Email | Password | Primary Features |
| :--- | :--- | :--- | :--- | :--- |
| **ADMIN** | System Administrator | `admin@bloodconnect.org` | `Admin@123` | Dashboard stats, status updates, verified donation logging, analytical reports |
| **DONOR (O+)** | Rahul Sharma | `rahul.donor@gmail.com` | `Donor@123` | View matching requests, accept/reject requests, view personal donation history |
| **DONOR (A+)** | Priya Patel | `priya.donor@gmail.com` | `Donor@123` | Compatibility matches, profile updates, availability toggling |
| **DONOR (B+)** | Ankit Verma | `ankit.donor@gmail.com` | `Donor@123` | Response history, notifications |
| **REQUESTER** | Vikram Mehta | `vikram.req@gmail.com` | `Req@123` | Create emergency blood requests, track donor responses, cancel pending requests |
| **REQUESTER** | Deepa Nair | `deepa.req@gmail.com` | `Req@123` | Hospital coordination, fulfillment tracking |

---

## 14. Testing Results

All 8 integration test suites run against the live MySQL database (`blood_donor_db`) with a **100% pass rate**:

| # | Test Suite | Description | Tests Run | Result |
| :---: | :--- | :--- | :---: | :---: |
| 1 | `DatabaseDAOTest` | Verifies MySQL connection, 7 tables, foreign key constraints, password hashing, and DAO queries | 1 | **PASSED** |
| 2 | `AuthenticationUserManagementTest` | Validates registration, duplicate email rejection, role-based login, invalid credentials, and session invalidation | 1 | **PASSED** |
| 3 | `DonorManagementTest` | Tests donor profile CRUD, soft deactivation, blood group/city search, and compatibility cross-matching | 1 | **PASSED** |
| 4 | `BloodRequestManagementTest` | Validates blood requisition creation, ownership privacy, soft cancellation, admin status updates, and multi-criteria filters | 1 | **PASSED** |
| 5 | `DonorMatchingAndResponseTest` | Tests cross-compatibility lookup, locality filtering, response submission, duplicate prevention, and data isolation | 1 | **PASSED** |
| 6 | `DonationHistoryManagementTest` | Validates official donation recording, donor stat synchronization, admin-only controls, date filters, and soft cancellations | 1 | **PASSED** |
| 7 | `AdminAndNotificationManagementTest` | Validates 7 dynamic dashboard statistics, 4 live activity feeds, notification types, unread counts, and analytical reports | 1 | **PASSED** |
| 8 | `EndToEndWorkflowTest` | Validates the complete 16-step end-to-end application lifecycle across all roles and workflows | 1 | **PASSED** |
| | **Overall Maven Test Suite** | **8 Test Suites / 56 Functional Test Cases** | **8 / 8** | **100% SUCCESS** |
