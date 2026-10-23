# Extremis – Role-Based Student Attendance Mobile Web App (PWA)

**Extremis** is a production-grade, demo-ready, mobile-first Progressive Web Application (PWA) built with Java 17+, Spring Boot 3, Spring Security, Thymeleaf, and SQLite/MySQL via JPA.

It replaces legacy desktop attendance tools with a GPS geofence-verified and live-camera selfie-verified attendance platform designed for students, faculty, and administrators.

---

## 🌟 Key Features

1. **Role-Based Portals & Strict Role Isolation:**
   - **Student Portal** (`/student/login` &rarr; `/student/dashboard`): One-tap Check-In with live selfie and GPS geofence verification, Check-Out, real-time in-campus status, and personal attendance history.
   - **Faculty Portal** (`/faculty/login` &rarr; `/faculty/dashboard`): Department student directory, daily attendance rosters, real-time campus presence counters, and individual verification record viewer (inspect student check-in selfies, GPS coordinates, and Google Maps location).
   - **Admin Portal** (`/admin/login` &rarr; `/admin/dashboard`): Full CRUD for Students and Faculty (with cascade warning for attendance history), Master Attendance Log with filters, Live Active Device Session Manager with instant force-reset, and Campus Geofences Manager with custom coordinates and radius.

2. **Single Active Device Policy for Students (PRD Section 3):**
   - Each student account can be active on **ONLY ONE device** at a time.
   - When a student attempts to log in from a secondary phone while an active session exists on another device, the login is rejected with:
     > *"This account is already logged in on another device. Please log out there first."*
   - Inactivity timeout of 30 minutes (based on `last_seen_at`) ensures students are never locked out permanently.
   - Admin can force-reset any student's locked session with a single click from the Admin Dashboard.
   - Faculty and Admin accounts maintain standard multi-session capability.

3. **Jaipur Bus / JCTSL Styled Permission Prompt Screen (PRD Section 4):**
   - Upon a student's first login (or whenever permissions are missing), an intuitive permission prompt appears with soft light styling, shield icons, real-time status pills (Red `!` &rarr; Green `✓`), Extremis Terms & Conditions checkbox, Settings gear modal (guides for Chrome, Safari, Edge), and a large red `CONTINUE →` action button.
   - CONTINUE remains disabled until both Location and Camera permissions are granted and terms are accepted.
   - Acceptance timestamp is recorded in the database.

4. **Campus Geofencing & Anti-Proxy Photo Verification (PRD Section 5):**
   - Live camera capture strictly via WebRTC `getUserMedia()` and `<canvas>` rendering (gallery and file uploads blocked).
   - Geofence calculation via the **Haversine formula** against configured campus coordinates (default: *Arya College of Engineering & IT, Kukas, Jaipur*).
   - Server-side rejection if the student is outside campus or if device GPS accuracy is worse than 100 meters.
   - Server-side timestamps only (client clock spoofing is ignored).
   - Secure disk storage under `/uploads/photos/` with access restricted strictly to authorized Faculty and Admins.

5. **PWA Installation:**
   - Manifest (`manifest.json`) and Service Worker (`sw.js`) enable "Add to Home Screen" on Android, iOS Safari, and desktop Chrome.

---

## 🚀 How to Run Locally (Port 8088)

### Prerequisites
- **Java 17+** (JDK 17 or higher)
- **Apache Maven 3.8+**

### Steps
1. Open a terminal in the project directory:
   ```bash
   cd extremis
   ```

2. Run the application using the embedded Jetty plugin:
   ```bash
   mvn jetty:run
   ```
   *(Or package the standard WAR for Tomcat 10: `mvn clean package` &rarr; deploy `target/extremis.war`)*

3. Open your browser and navigate to:
   ```
   http://localhost:8088
   ```

*(The SQLite database `extremis.db` and demo seed records will be created automatically on first run).*

### Demo Credentials:

1. **Student Accounts:**
   - **AI - Section A:** `24EARAD031` (Bhavya Kumar Bohra), `24EARAD001` (Aarav Sharma), `24EARAD002` (Ananya Gupta)
   - **CS - Section A:** `24ECS001` (Ishan Verma), `24ECS002` (Diya Joshi)
   - **ECE - Section A:** `24ECE001` (Aditya Roy), `24ECE002` (Tanvi Shah)
   - **Password:** `StudentPassword123!` (for all students)
   - *Policy:* Single active device restricted.

2. **Faculty Accounts (Class Teachers):**
   - **Faculty 1 (AI - Section A):** ID `FAC001` &bull; Dr. Rajesh Sharma
   - **Faculty 2 (CS - Section A):** ID `FAC002` &bull; Prof. Priya Verma
   - **Faculty 3 (ECE - Section A):** ID `FAC003` &bull; Dr. Amit Patel
   - **Password:** `FacultyPassword123!`
   - *Policy:* Each faculty only sees and monitors students of their assigned class.

3. **Administrator Account:**
   - **Username:** `admin`
   - **Password:** `AdminPassword123!`
   - *Controls:* Master overview, faculty list, and "Reset Device Lock" for any locked student.

---

## 📱 How to Run over HTTPS (Port 8443) for Phone Testing over Wi-Fi

Modern mobile browsers (Chrome on Android, Safari on iOS) require **HTTPS or localhost** to grant access to the Geolocation API and Camera (`getUserMedia`).

Extremis includes a pre-generated self-signed PKCS12 keystore (`keystore.p12`) configured for port 8443.

### 1. Launch with the SSL Profile
Run the following command:
```bash
mvn spring-boot:run -Dspring-boot.run.profiles=ssl
```
Or with Java directly:
```bash
mvn clean package -DskipTests
java -jar target/extremis-attendance-1.0.0.jar --spring.profiles.active=ssl
```

The server will start listening on **`https://localhost:8443`** (and on your local Wi-Fi IP address).

### 2. Connect Your Phone
1. Connect your phone to the same Wi-Fi network as your PC.
2. Find your PC's local IP address (e.g., `192.168.1.15`):
   - Windows: Run `ipconfig` in command prompt (look for *IPv4 Address*).
   - Mac/Linux: Run `ifconfig` or `ip a`.
3. Open Chrome or Safari on your phone and browse to:
   ```
   https://<YOUR-PC-IP>:8443
   ```
   *(Example: `https://192.168.1.15:8443`)*
4. Because the SSL certificate is self-signed, your browser will display a certificate warning:
   - **Chrome:** Tap *Advanced* &rarr; *Proceed to `<IP>` (unsafe)*.
   - **Safari:** Tap *Show Details* &rarr; *visit this website*.
5. Grant camera and location permissions when prompted by the app.

---

## 🔑 Demo Seed Credentials

The application automatically seeds the database with the following demo accounts on startup:

| Portal | Username / Roll | Password | Role / Details |
| :--- | :--- | :--- | :--- |
| **Admin** | `admin` | `AdminPassword123!` | System Administrator |
| **Faculty (CS)** | `faculty.cs` | `FacultyPassword123!` | Dr. Rajesh Sharma (Computer Science) |
| **Faculty (IT)** | `faculty.it` | `FacultyPassword123!` | Prof. Sunita Verma (Information Technology) |
| **Student 1** | `STU2024001` | `StudentPassword123!` | Aarav Gupta (Terms Pre-Accepted & Ready for Check-In) |
| **Student 2** | `STU2024002` | `StudentPassword123!` | Priya Sharma (Test Jaipur Bus Permission Screen) |
| **Student 3** | `STU2024003` | `StudentPassword123!` | Rohan Mehta (Information Technology) |
| **Student 4** | `STU2024004` | `StudentPassword123!` | Ananya Singh (Computer Science) |
| **Student 5** | `STU2024005` | `StudentPassword123!` | Vikram Patel (Information Technology) |
| **Student 6** | `STU2024006` | `StudentPassword123!` | Neha Joshi (Computer Science) |
| **Student 7** | `STU2024007` | `StudentPassword123!` | Aditya Verma (Information Technology) |
| **Student 8** | `STU2024008` | `StudentPassword123!` | Sneha Reddy (Computer Science) |
| **Student 9** | `STU2024009` | `StudentPassword123!` | Karan Malhotra (Information Technology) |
| **Student 10**| `STU2024010` | `StudentPassword123!` | Pooja Nair (Computer Science) |

---

## ⚙️ Configuration Properties (`application.properties`)

```properties
# Server HTTP Port
server.port=8088

# Database (Default: SQLite file-based database)
spring.datasource.url=jdbc:sqlite:extremis.db
spring.datasource.driver-class-name=org.sqlite.JDBC
spring.jpa.database-platform=org.hibernate.community.dialect.SQLiteDialect
spring.jpa.hibernate.ddl-auto=update

# Business Rules: Session timeout & GPS accuracy
app.session.timeout-minutes=30
app.geofence.max-accuracy-meters=100.0

# Server-Enforced Attendance Windows & Rules
attendance.checkin.start=08:50
attendance.checkin.end=09:05
attendance.checkout.start=15:40
attendance.checkout.end=23:59
attendance.lectures-per-day=5

# Scheduled Auto-Absent Job (Default: Daily at 18:00 server time)
attendance.auto-absent.cron=0 0 18 * * ?
```

### Auto-Absent & OD Business Rules
1. **Auto-Absent Scheduled Task (`@Scheduled` at 18:00):** Every working day (Monday through Saturday, excluding Sundays), any enrolled student who has not checked in by the auto-absent trigger time is automatically marked `ABSENT` in the attendance records.
2. **Missing Check-Out Rule:** A student who successfully checks in within the morning window but never checks out before midnight remains `PRESENT` with check-out time displayed as `—`. (Configurable rule: avoids unfairly penalizing students whose transport or battery died before evening departure).
3. **On Duty (OD) Approval:** Admins and Faculty can approve and record `OD` (On Duty) for any student on any calendar date with administrative remarks. OD is counted as attended alongside Present in percentage calculations.

---

## 🔑 Demo Seed Credentials (20 Students + Faculty + Admin)

All accounts are pre-seeded in `DataInitializer.java` on application launch:

| Username / ID | Password | Full Name | RTU Roll No | College Email | Branch & Section |
| :--- | :--- | :--- | :--- | :--- | :--- |
| **admin** | `AdminPassword123!` | System Administrator | — | admin@extremis.edu.in | Administration |
| **faculty.cs** | `FacultyPassword123!` | Dr. Rajesh Sharma | — | faculty.cs@extremis.edu.in | Computer Science |
| **faculty.it** | `FacultyPassword123!` | Prof. Sunita Verma | — | faculty.it@extremis.edu.in | Information Technology |
| **STU2024001** | `StudentPassword123!` | Aarav Gupta | 24EARAD031 | aarav.gupta@extremis.edu.in | AI – Section A |
| **STU2024002** | `StudentPassword123!` | Priya Sharma | 24EARCS032 | priya.sharma@extremis.edu.in | CS – Section A |
| **STU2024003** | `StudentPassword123!` | Rohan Mehta | 24EARIT033 | rohan.mehta@extremis.edu.in | IT – Section B |
| **STU2024004** | `StudentPassword123!` | Ananya Singh | 24EARAD034 | ananya.singh@extremis.edu.in | AI – Section A |
| **STU2024005** | `StudentPassword123!` | Vikram Patel | 24EARIT035 | vikram.patel@extremis.edu.in | IT – Section A |
| **STU2024006** | `StudentPassword123!` | Neha Joshi | 24EARCS036 | neha.joshi@extremis.edu.in | CS – Section B |
| **STU2024007** | `StudentPassword123!` | Aditya Verma | 24EARIT037 | aditya.verma@extremis.edu.in | IT – Section B |
| **STU2024008** | `StudentPassword123!` | Sneha Reddy | 24EARCS038 | sneha.reddy@extremis.edu.in | CS – Section A |
| **STU2024009** | `StudentPassword123!` | Karan Malhotra | 24EARIT039 | karan.malhotra@extremis.edu.in | IT – Section A |
| **STU2024010** | `StudentPassword123!` | Pooja Nair | 24EARCS040 | pooja.nair@extremis.edu.in | CS – Section B |
| **STU2024011** | `StudentPassword123!` | Rahul Choudhary | 24EARCS041 | rahul.choudhary@extremis.edu.in | CS – Section A |
| **STU2024012** | `StudentPassword123!` | Divya Agarwal | 24EARIT042 | divya.agarwal@extremis.edu.in | IT – Section B |
| **STU2024013** | `StudentPassword123!` | Aman Rajput | 24EARME043 | aman.rajput@extremis.edu.in | ME – Section A |
| **STU2024014** | `StudentPassword123!` | Ritu Singhania | 24EARCS044 | ritu.singhania@extremis.edu.in | CS – Section B |
| **STU2024015** | `StudentPassword123!` | Manish Saini | 24EARCE045 | manish.saini@extremis.edu.in | CE – Section A |
| **STU2024016** | `StudentPassword123!` | Simran Kaur | 24EARIT046 | simran.kaur@extremis.edu.in | IT – Section A |
| **STU2024017** | `StudentPassword123!` | Harsh Vardhan | 24EAREE047 | harsh.vardhan@extremis.edu.in | EE – Section A |
| **STU2024018** | `StudentPassword123!` | Meera Chawla | 24EARCS048 | meera.chawla@extremis.edu.in | CS – Section A |
| **STU2024019** | `StudentPassword123!` | Kunal Saxena | 24EAREC049 | kunal.saxena@extremis.edu.in | EC – Section B |
| **STU2024020** | `StudentPassword123!` | Tanvi Deshmukh | 24EARIT050 | tanvi.deshmukh@extremis.edu.in | IT – Section A |

---

## 🧪 Automated Testing

All automated JUnit tests can be executed via Maven:
```bash
mvn test
```

### Verified Test Suite (21 Tests Passing):
1. **`testCheckInOutsideWindowRejected`**: Enforces server-side rejection outside 08:50–09:05.
2. **`testCheckInWithinWindowSucceeds`**: Verifies valid check-in inside the window.
3. **`testCheckOutBeforeWindowRejected`**: Enforces check-out rejection before 15:40.
4. **`testCheckOutWithoutCheckInRejected`**: Blocks check-out without prior check-in.
5. **`testDuplicateCheckInRejected`**: Blocks duplicate check-ins on the same day.
6. **`testAutoAbsentJobMarksNonAttendees`**: Verifies auto-absent job marks students without check-in as ABSENT.
7. **`testMarkODSucceeds`**: Verifies faculty/admin OD marking.
8. **`testStudentReportShowsOnlyOwnData`**: Verifies strict ownership isolation.
9. **`SecurityRoleIsolationTest`**: 6 test cases verifying strict role-based route protection across Student, Faculty, and Admin portals.
10. **`ActiveSessionServiceTest`** & **`GeofenceServiceTest`**: Single active device, session timeout, and GPS Haversine distance tests.

---

## 📋 Manual Testing Checklist

| Test ID | Feature | Steps to Verify | Expected Outcome |
| :--- | :--- | :--- | :--- |
| **TC-01** | **Role Separation** | Navigate to `/faculty/dashboard` while unauthenticated. | Redirects to `/faculty/login`. |
| **TC-02** | **Cross-Role Denial** | Log in as Student `STU2024001` and navigate to `/admin/dashboard`. | Access Denied (HTTP 403 redirected to `/access-denied`). |
| **TC-03** | **Wrong Portal Login** | Attempt to log in with `admin` credentials on `/student/login`. | Rejected: *"Access Denied: This account is not a student account."* |
| **TC-04** | **Permanent Device Lock** | Log in with `STU2024002` on Browser A. Log out. Attempt login on Browser B. | Rejected with: *"This account is permanently locked to another phone/device."* |
| **TC-05** | **Single Active Session** | Open Browser A and log in as `STU2024001`. In Browser B, attempt login. | Rejected with: *"This account is already logged in on another device."* |
| **TC-06** | **Permissions Screen (Screen 6)** | Log in with unverified student. Check soft light styling, location card, camera card, Terms checkbox. | Continue button stays disabled until both permissions granted and terms checked. |
| **TC-07** | **Student Dashboard (Screen 1)** | Log in as `STU2024001`. View at 390px mobile width. | Notice banner, profile card (24EARAD031, AI - Section A), Today's Attendance card, Overall Attendance circular ring, Report teaser, Big Circular Action button, bottom nav. |
| **TC-08** | **Check-in Window Enforcement** | Attempt check-in outside 08:50–09:05 server time. | Button shows disabled state or rejected by server: *"Check-in is only permitted between 08:50 and 09:05."* |
| **TC-09** | **Check-Out Window Enforcement** | Attempt check-out before 15:40 server time. | Rejected by server: *"Check-out is only permitted after 15:40."* |
| **TC-10** | **Notifications (Screen 2)** | Tap bell icon or notice banner on dashboard. | View list of notices with colored badges (Important: red, General: blue, Academic: green). Unread badge clears upon visit. |
| **TC-11** | **Attendance Report (Screen 3)** | Tap 'Report' in bottom nav. | 4 summary tiles (Present, Absent, OD, Total). Filter by All/Present/Absent/OD. Toggle between List Table and Monthly Calendar view (Sun–Sat grid with colored status dots). |
| **TC-12** | **Check-In Process (Screen 4)** | Tap Check-In button. | Card 1 captures GPS lat/lng and accuracy. Card 2 shows live camera stream and selfie capture. Submit button enables only when both are captured. |
| **TC-13** | **My Profile (Screen 5)** | Tap 'Profile' in bottom nav. | Read-only rows for Full Name, College Email, RTU Roll Number, Branch, Section. Log Out button at bottom terminates active session. |
| **TC-14** | **Post Notice (Admin)** | Log in as `admin`, submit "Post Notice" form on dashboard. | New notice appears immediately on student dashboard and notifications screen. |
| **TC-15** | **Mark OD (Admin / Faculty)** | Admin/Faculty submits Mark OD for student and date. | Record is marked OD; counted as attended in student's overall attendance percentage. |

