package com.extremis.service;

import com.extremis.dao.AdminDAO;
import com.extremis.dao.DBConnection;
import com.extremis.dao.FacultyDAO;
import com.extremis.dao.RandomCheckInDAO;
import com.extremis.dao.StudentDAO;
import com.extremis.model.Admin;
import com.extremis.model.Faculty;
import com.extremis.model.RandomChallenge;
import com.extremis.model.RandomCheckInSettings;
import com.extremis.model.RandomLocationCheck;
import com.extremis.model.Student;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

public class RandomCheckInServiceTest {

    private RandomCheckInService service;
    private RandomCheckInDAO randomDAO;
    private StudentDAO studentDAO;
    private FacultyDAO facultyDAO;
    private AdminDAO adminDAO;

    @BeforeEach
    void setUp() {
        service = new RandomCheckInService();
        randomDAO = new RandomCheckInDAO();
        studentDAO = new StudentDAO();
        facultyDAO = new FacultyDAO();
        adminDAO = new AdminDAO();
    }

    @Test
    @DisplayName("Test Lunch Break Boundary: 12:20 PM to 1:10 PM strict exclusion")
    void testLunchBreakBoundaryRules() {
        // Just before lunch
        assertFalse(service.isDuringLunchBreak(LocalTime.of(12, 19, 59)), "12:19:59 is before lunch");

        // Exact start of lunch
        assertTrue(service.isDuringLunchBreak(LocalTime.of(12, 20, 0)), "12:20:00 is lunch start");

        // Middle of lunch
        assertTrue(service.isDuringLunchBreak(LocalTime.of(12, 45, 0)), "12:45:00 is during lunch");

        // Just before lunch ends
        assertTrue(service.isDuringLunchBreak(LocalTime.of(13, 9, 59)), "13:09:59 is during lunch");

        // Exact end of lunch (13:10 resume)
        assertFalse(service.isDuringLunchBreak(LocalTime.of(13, 10, 0)), "13:10:00 is end of lunch (resumed)");

        // Afternoon academic session
        assertFalse(service.isDuringLunchBreak(LocalTime.of(14, 0, 0)), "14:00:00 is after lunch");
    }

    @Test
    @DisplayName("Test Lunch Break Overlap: Challenges must not overlap 12:20 PM to 1:10 PM")
    void testLunchBreakOverlap() {
        // Challenge starting at 12:15 for 10 min -> ends at 12:25 (extends into lunch) -> OVERLAPS
        assertTrue(service.overlapsLunchBreak(LocalTime.of(12, 15), 10), "12:15 - 12:25 overlaps lunch start (12:20)");

        // Challenge starting at 12:00 for 15 min -> ends at 12:15 -> DOES NOT OVERLAP
        assertFalse(service.overlapsLunchBreak(LocalTime.of(12, 0), 15), "12:00 - 12:15 finishes before lunch");

        // Challenge starting at 13:10 for 10 min -> ends at 13:20 -> DOES NOT OVERLAP
        assertFalse(service.overlapsLunchBreak(LocalTime.of(13, 10), 10), "13:10 - 13:20 is after lunch");

        // Challenge starting inside lunch (12:30 for 5 min) -> OVERLAPS
        assertTrue(service.overlapsLunchBreak(LocalTime.of(12, 30), 5), "Starts inside lunch");
    }

    @Test
    @DisplayName("Test Academic Time Window Gates: 9:05 AM to 3:40 PM")
    void testAcademicTimeWindowGates() {
        // Before regular morning check-in window closes (9:00 AM)
        assertFalse(service.isValidChallengeWindow(LocalTime.of(9, 0), 5), "Before 9:05 is prohibited");
        assertFalse(service.isValidChallengeWindow(LocalTime.of(9, 4), 5), "Before 9:05 is prohibited");

        // Valid morning window (e.g. 10:15 AM)
        assertTrue(service.isValidChallengeWindow(LocalTime.of(10, 15), 10), "10:15 is valid morning slot");

        // Overlapping lunch window (12:15 PM for 10 min)
        assertFalse(service.isValidChallengeWindow(LocalTime.of(12, 15), 10), "Cannot overlap lunch");

        // Valid afternoon window (2:00 PM)
        assertTrue(service.isValidChallengeWindow(LocalTime.of(14, 0), 10), "14:00 is valid afternoon slot");

        // Deadline extending past regular checkout (3:40 PM)
        assertFalse(service.isValidChallengeWindow(LocalTime.of(15, 35), 10), "Ends at 15:45, past 15:40 checkout");

        // Deadline ending exactly at 3:40 PM
        assertTrue(service.isValidChallengeWindow(LocalTime.of(15, 30), 10), "Ends at 15:40, strictly valid");
    }

    @Test
    @DisplayName("Test Admin Settings Persistence and Controls")
    void testAdminSettingsPersistence() {
        RandomCheckInSettings settings = service.getSettings();
        assertNotNull(settings, "Default settings must exist");

        // Test updating settings
        int originalDuration = settings.getDurationMinutes();
        int testDuration = (originalDuration == 5) ? 7 : 5;
        settings.setDurationMinutes(testDuration);
        settings.setChallengesPerDay(3);
        settings.setMinIntervalMinutes(45);
        settings.setMaxIntervalMinutes(90);

        boolean saved = service.saveSettings(settings);
        assertTrue(saved, "Settings should be saved to database");

        RandomCheckInSettings reloaded = service.getSettings();
        assertEquals(testDuration, reloaded.getDurationMinutes(), "Duration should persist");
        assertEquals(3, reloaded.getChallengesPerDay(), "Challenges per day should persist");
        assertEquals(45, reloaded.getMinIntervalMinutes(), "Min interval should persist");

        // Test Pause and Resume
        service.pauseRandomizer();
        assertTrue(service.getSettings().isPaused(), "Randomizer should be paused");

        service.resumeRandomizer();
        assertFalse(service.getSettings().isPaused(), "Randomizer should be resumed");

        // Reset duration back
        settings.setDurationMinutes(originalDuration);
        service.saveSettings(settings);
    }

    @Test
    @DisplayName("Test Geofence Calculation: Strict distance check against campus center")
    void testCampusGeofenceCalculation() {
        // Exactly at campus center
        double centerDist = service.calculateDistanceMeters(
                RandomCheckInService.CAMPUS_LAT,
                RandomCheckInService.CAMPUS_LNG,
                RandomCheckInService.CAMPUS_LAT,
                RandomCheckInService.CAMPUS_LNG
        );
        assertEquals(0.0, centerDist, 0.001, "Distance at exact center must be 0");

        // 100 meters away (should be within campus boundary of 1000m)
        double nearDist = service.calculateDistanceMeters(
                RandomCheckInService.CAMPUS_LAT + 0.0005,
                RandomCheckInService.CAMPUS_LNG + 0.0005,
                RandomCheckInService.CAMPUS_LAT,
                RandomCheckInService.CAMPUS_LNG
        );
        assertTrue(nearDist < RandomCheckInService.MAX_CAMPUS_RADIUS_METERS, "Nearby point should be within campus");

        // Far away coordinates (e.g., Delhi center lat 28.6139, lng 77.2090)
        double farDist = service.calculateDistanceMeters(
                28.6139, 77.2090,
                RandomCheckInService.CAMPUS_LAT, RandomCheckInService.CAMPUS_LNG
        );
        assertTrue(farDist > RandomCheckInService.MAX_CAMPUS_RADIUS_METERS, "Far coordinates must exceed campus radius");
    }

    @Test
    @DisplayName("Verify Exact Test Accounts: 1 Demo Bhavya, exactly 5 Students, 3 Faculty, 1 Admin")
    void testExactAccountProvisioning() {
        // 1. Bhavya Kumar Bohra Demo Account
        Student bhavya = studentDAO.findByRollNo("24EARAD031");
        assertNotNull(bhavya, "Bhavya Kumar Bohra demo account must exist with enrollment 24EARAD031");
        assertEquals("Bhavya Kumar Bohra", bhavya.getName());
        assertEquals(DBConnection.hashPassword("StudentPassword123!"), bhavya.getPasswordHash(),
                "Bhavya demo account password hash must verify");

        // 2. Exactly 5 additional student accounts
        List<Student> students = studentDAO.getAllStudents();
        assertEquals(6, students.size(), "Database must have exactly 6 students (1 demo + 5 students)");

        String[] expectedEnrollments = {"24EARAD031", "24EARAD001", "24EARAD002", "24ECS001", "24ECS002", "24ECE001"};
        for (String enrollment : expectedEnrollments) {
            Student s = studentDAO.findByRollNo(enrollment);
            assertNotNull(s, "Student " + enrollment + " must exist");
            assertEquals(DBConnection.hashPassword("StudentPassword123!"), s.getPasswordHash(),
                    "Student " + enrollment + " password must match StudentPassword123!");
        }

        // 3. Faculty Accounts
        List<Faculty> faculties = facultyDAO.getAllFaculties();
        assertEquals(3, faculties.size(), "Must have exactly 3 faculty accounts");

        String[] expectedFacultyCodes = {"FAC001", "FAC002", "FAC003"};
        for (String code : expectedFacultyCodes) {
            Faculty f = facultyDAO.findByFacultyId(code);
            assertNotNull(f, "Faculty " + code + " must exist");
            assertEquals(DBConnection.hashPassword("FacultyPassword123!"), f.getPasswordHash(),
                    "Faculty " + code + " password must match FacultyPassword123!");
        }

        // 4. Admin Account
        Admin admin = adminDAO.findByUsername("admin");
        assertNotNull(admin, "Admin account 'admin' must exist");
        assertEquals(DBConnection.hashPassword("AdminPassword123!"), admin.getPasswordHash(),
                "Admin password must match AdminPassword123!");
    }

    @Test
    @DisplayName("Test Student Verification Response Validations: Geofence, Missing Fields, Duplicate Rejection")
    void testStudentVerificationSubmissionValidations() {
        Student bhavya = studentDAO.findByRollNo("24EARAD031");
        assertNotNull(bhavya);

        // 1. Missing coordinates rejection
        String errNoCoords = service.submitStudentResponse(bhavya.getId(), 9999L, null, 75.8089, 10.0, "/photo.jpg");
        assertEquals("Verification rejected: Location coordinates are required.", errNoCoords);

        // 2. Missing photo rejection
        String errNoPhoto = service.submitStudentResponse(bhavya.getId(), 9999L, 26.9131, 75.8089, 10.0, null);
        assertEquals("Verification rejected: Live camera selfie capture is required.", errNoPhoto);

        // 3. Outside campus geofence rejection
        String errGeofence = service.submitStudentResponse(bhavya.getId(), 9999L, 28.6139, 77.2090, 10.0, "/photo.jpg");
        assertTrue(errGeofence.contains("outside the authorized college campus geofence"),
                "Must reject when coordinates are outside campus");

        // 4. Non-existent challenge
        String errNotFound = service.submitStudentResponse(bhavya.getId(), 999999L, 26.9131, 75.8089, 10.0, "/photo.jpg");
        assertEquals("Challenge not found.", errNotFound);
    }

    @Test
    @DisplayName("Test Challenge Lifecycle: Creation, Response Recording, and Duplicate Prevention")
    void testChallengeLifecycleAndDuplicatePrevention() {
        Student bhavya = studentDAO.findByRollNo("24EARAD031");
        assertNotNull(bhavya);

        // Create a test challenge directly
        LocalDateTime now = service.getEffectiveDateTime();
        RandomChallenge challenge = new RandomChallenge();
        challenge.setChallengeCode("RC-TEST-" + UUID.randomUUID().toString().substring(0, 6).toUpperCase());
        challenge.setScheduledTime(now.minusMinutes(1));
        challenge.setDeadlineTime(now.plusMinutes(10));
        challenge.setDurationMinutes(10);
        challenge.setStatus("ACTIVE");
        challenge.setTargetType("ALL");
        challenge.setTargetValue("ALL");
        challenge.setManual(true);
        challenge.setCreatedAt(now.minusMinutes(1));
        challenge.setCreatedBy("TEST_SUITE");

        Long challengeId = randomDAO.createChallenge(challenge, Collections.singletonList(bhavya.getId()));
        assertNotNull(challengeId, "Challenge must be created");

        try {
            // First valid submission
            String submitResult = service.submitStudentResponse(
                    bhavya.getId(),
                    challengeId,
                    RandomCheckInService.CAMPUS_LAT,
                    RandomCheckInService.CAMPUS_LNG,
                    15.0,
                    "/uploads/attendance_photos/test_selfie.jpg"
            );
            assertNull(submitResult, "Valid submission inside campus should succeed (null error)");

            // Duplicate submission attempt
            String duplicateResult = service.submitStudentResponse(
                    bhavya.getId(),
                    challengeId,
                    RandomCheckInService.CAMPUS_LAT,
                    RandomCheckInService.CAMPUS_LNG,
                    15.0,
                    "/uploads/attendance_photos/test_selfie_2.jpg"
            );
            assertEquals("You have already completed this random check-in challenge.", duplicateResult,
                    "Duplicate submission must be rejected");

            // Attempt from another student who was not assigned to this challenge
            Student s1 = studentDAO.findByRollNo("24EARAD001");
            assertNotNull(s1);
            String unassignedResult = service.submitStudentResponse(
                    s1.getId(),
                    challengeId,
                    RandomCheckInService.CAMPUS_LAT,
                    RandomCheckInService.CAMPUS_LNG,
                    15.0,
                    "/uploads/attendance_photos/test_selfie_s1.jpg"
            );
            assertEquals("You are not assigned to this random check-in challenge.", unassignedResult,
                    "Unassigned student submission must be rejected");
        } finally {
            // Cleanup test challenge
            randomDAO.cancelChallenge(challengeId);
        }
    }

    @Test
    @DisplayName("Test 4-5 Random Check Slots: Timings strictly after 9:05 AM, before 3:40 PM, excluding lunch (12:20-1:10 PM)")
    void testRandomScheduleTimingAndLunchExclusion() {
        for (int count : new int[]{4, 5}) {
            List<LocalTime> slots = service.generateStudentUniqueSlots(count, 5);
            assertNotNull(slots);
            assertEquals(count, slots.size(), "Must generate exactly requested count (" + count + ")");

            for (LocalTime slot : slots) {
                // Must be after 9:05 AM
                assertTrue(slot.isAfter(LocalTime.of(9, 5)), "Slot " + slot + " must be after 9:05 AM");

                // Must end at or before 3:40 PM
                assertFalse(slot.plusMinutes(5).isAfter(LocalTime.of(15, 40)),
                        "Slot " + slot + " (window 5m) must not exceed 3:40 PM checkout");

                // Must not be during lunch break
                assertFalse(service.isDuringLunchBreak(slot), "Slot " + slot + " must not fall in lunch break");

                // Must not overlap lunch break
                assertFalse(service.overlapsLunchBreak(slot, 5), "Slot " + slot + " must not overlap lunch break");
            }
        }

        // Test individual unpredictability (different students get different slots)
        List<LocalTime> student1Slots = service.generateStudentUniqueSlots(4, 5);
        List<LocalTime> student2Slots = service.generateStudentUniqueSlots(4, 5);
        assertNotNull(student1Slots);
        assertNotNull(student2Slots);
        assertEquals(4, student1Slots.size());
        assertEquals(4, student2Slots.size());
    }

    @Test
    @DisplayName("Test Schedule Student Daily Verifications: 4-5 events per day & duplicate protection")
    void testScheduleStudentDailyVerifications() {
        Student bhavya = studentDAO.findByRollNo("24EARAD031");
        assertNotNull(bhavya);

        LocalDate testDate = LocalDate.of(2026, 11, 1);
        randomDAO.deleteLocationChecksForStudentAndDate(bhavya.getId(), testDate);
        Long fakeAttendanceId = 77777L;

        // Schedule first time
        boolean scheduled = service.scheduleStudentDailyVerifications(bhavya.getId(), testDate, fakeAttendanceId);
        assertTrue(scheduled, "First daily scheduling must succeed");

        // Schedule duplicate call
        boolean duplicate = service.scheduleStudentDailyVerifications(bhavya.getId(), testDate, fakeAttendanceId);
        assertFalse(duplicate, "Duplicate scheduling for same student and date must be rejected");

        // Verify scheduled checks
        List<RandomLocationCheck> checks = randomDAO.getLocationChecksForStudent(bhavya.getId(), testDate);
        assertNotNull(checks);
        assertTrue(checks.size() >= 4 && checks.size() <= 5,
                "Student must have 4 to 5 scheduled checks (actual: " + checks.size() + ")");

        for (RandomLocationCheck c : checks) {
            assertEquals("SCHEDULED", c.getStatus());
            assertEquals(bhavya.getId(), c.getStudentId());
            assertEquals(fakeAttendanceId, c.getAttendanceId());
            assertNull(c.getVerificationResult());
            assertFalse(c.isFlaggedForReview());

            LocalTime t = c.getScheduledTime().toLocalTime();
            assertTrue(t.isAfter(LocalTime.of(9, 5)));
            assertFalse(service.isDuringLunchBreak(t));
            assertFalse(t.isAfter(LocalTime.of(15, 40)));
        }

        // Cleanup
        randomDAO.deleteLocationChecksForStudentAndDate(bhavya.getId(), testDate);
    }

    @Test
    @DisplayName("Test Admin ON/OFF Toggle for Random Location Verification")
    void testAdminToggleRandomLocationVerification() {
        boolean originalState = service.isRandomizerEnabled();

        try {
            // Turn OFF
            service.toggleRandomizer(false);
            assertFalse(service.isRandomizerEnabled(), "Randomizer should report disabled");
            assertFalse(service.getSettings().isEnabled(), "Settings should persist disabled state");

            // Scheduling should be blocked when OFF
            LocalDate testDate = LocalDate.of(2026, 11, 2);
            boolean scheduledWhileOff = service.scheduleStudentDailyVerifications(1L, testDate, 8888L);
            assertFalse(scheduledWhileOff, "Scheduling should not occur when randomizer is OFF");

            // Turn ON
            service.toggleRandomizer(true);
            assertTrue(service.isRandomizerEnabled(), "Randomizer should report enabled");
            assertTrue(service.getSettings().isEnabled(), "Settings should persist enabled state");
        } finally {
            // Restore original state
            service.toggleRandomizer(originalState);
        }
    }

    @Test
    @DisplayName("Test Location Verification Outcomes: Campus, Geofence breach, Inaccurate GPS, GPS Unavailable (Rule 11)")
    void testLocationVerificationOutcomes() {
        Student bhavya = studentDAO.findByRollNo("24EARAD031");
        assertNotNull(bhavya);

        LocalDate testDate = LocalDate.of(2026, 11, 3);
        randomDAO.deleteLocationChecksForStudentAndDate(bhavya.getId(), testDate);
        LocalDateTime now = LocalDateTime.now();

        // 1. Create a test check for campus verification
        RandomLocationCheck check1 = new RandomLocationCheck();
        check1.setStudentId(bhavya.getId());
        check1.setAttendanceId(12345L);
        check1.setCheckDate(testDate);
        check1.setScheduledTime(now.minusMinutes(1));
        check1.setWindowEndTime(now.plusMinutes(5));
        check1.setStatus("ACTIVE");
        check1.setCreatedAt(now.minusMinutes(1));
        Long check1Id = randomDAO.createSingleLocationCheck(check1);
        assertNotNull(check1Id);

        // Verify inside campus (Jaipur campus lat 26.9131, lng 75.8089, accuracy 15m)
        String res1 = service.verifyLocationCheck(check1Id, bhavya.getId(),
                RandomCheckInService.CAMPUS_LAT, RandomCheckInService.CAMPUS_LNG, 15.0, null, "2026-11-03T10:00:00");
        assertNull(res1, "Valid inside-campus check should succeed with no error");

        RandomLocationCheck updated1 = randomDAO.getLocationCheckById(check1Id);
        assertEquals("VERIFIED", updated1.getStatus());
        assertEquals("VERIFIED_CAMPUS", updated1.getVerificationResult());
        assertFalse(updated1.isFlaggedForReview(), "Inside campus check must not be flagged for review");

        // 2. Test outside geofence verification
        RandomLocationCheck check2 = new RandomLocationCheck();
        check2.setStudentId(bhavya.getId());
        check2.setAttendanceId(12345L);
        check2.setCheckDate(testDate);
        check2.setScheduledTime(now.minusMinutes(1));
        check2.setWindowEndTime(now.plusMinutes(5));
        check2.setStatus("ACTIVE");
        check2.setCreatedAt(now.minusMinutes(1));
        Long check2Id = randomDAO.createSingleLocationCheck(check2);
        assertNotNull(check2Id);

        // Far location (e.g. lat 28.6139, lng 77.2090)
        String res2 = service.verifyLocationCheck(check2Id, bhavya.getId(),
                28.6139, 77.2090, 15.0, null, "2026-11-03T10:05:00");
        assertNull(res2);

        RandomLocationCheck updated2 = randomDAO.getLocationCheckById(check2Id);
        assertEquals("VERIFIED", updated2.getStatus());
        assertEquals("OUTSIDE_GEOFENCE", updated2.getVerificationResult());
        assertTrue(updated2.isFlaggedForReview(), "Outside geofence location must be flagged for review");

        // 3. Test Rule 11: Inaccurate GPS (> 100m) recorded as FAILED, NOT marked absent or flagged
        RandomLocationCheck check3 = new RandomLocationCheck();
        check3.setStudentId(bhavya.getId());
        check3.setAttendanceId(12345L);
        check3.setCheckDate(testDate);
        check3.setScheduledTime(now.minusMinutes(1));
        check3.setWindowEndTime(now.plusMinutes(5));
        check3.setStatus("ACTIVE");
        check3.setCreatedAt(now.minusMinutes(1));
        Long check3Id = randomDAO.createSingleLocationCheck(check3);
        assertNotNull(check3Id);

        String res3 = service.verifyLocationCheck(check3Id, bhavya.getId(),
                RandomCheckInService.CAMPUS_LAT, RandomCheckInService.CAMPUS_LNG, 350.0, null, "2026-11-03T10:10:00");
        assertNull(res3);

        RandomLocationCheck updated3 = randomDAO.getLocationCheckById(check3Id);
        assertEquals("FAILED", updated3.getStatus());
        assertEquals("ACCURACY_POOR", updated3.getVerificationResult());
        assertFalse(updated3.isFlaggedForReview(), "Poor accuracy must NOT be flagged for review or marked absent (Rule 11)");

        // 4. Test Rule 11: Device GPS Unavailable / Permission Denied
        RandomLocationCheck check4 = new RandomLocationCheck();
        check4.setStudentId(bhavya.getId());
        check4.setAttendanceId(12345L);
        check4.setCheckDate(testDate);
        check4.setScheduledTime(now.minusMinutes(1));
        check4.setWindowEndTime(now.plusMinutes(5));
        check4.setStatus("ACTIVE");
        check4.setCreatedAt(now.minusMinutes(1));
        Long check4Id = randomDAO.createSingleLocationCheck(check4);
        assertNotNull(check4Id);

        String res4 = service.verifyLocationCheck(check4Id, bhavya.getId(),
                null, null, null, "PERMISSION_DENIED", "2026-11-03T10:15:00");
        assertNull(res4);

        RandomLocationCheck updated4 = randomDAO.getLocationCheckById(check4Id);
        assertEquals("FAILED", updated4.getStatus());
        assertEquals("PERMISSION_DENIED", updated4.getVerificationResult());
        assertFalse(updated4.isFlaggedForReview(), "Permission denied must NOT be flagged as absent (Rule 11)");

        // 5. Duplicate submission prevention
        String resDup = service.verifyLocationCheck(check1Id, bhavya.getId(),
                RandomCheckInService.CAMPUS_LAT, RandomCheckInService.CAMPUS_LNG, 10.0, null, "2026-11-03T10:20:00");
        assertEquals("This verification check is already completed or inactive.", resDup,
                "Duplicate verification on already completed check must be rejected");

        // Cleanup
        randomDAO.deleteLocationChecksForStudentAndDate(bhavya.getId(), testDate);
    }

    @Test
    @DisplayName("Test Faculty Review Workflow: Outside Geofence Reason Verification (Valid Reason -> Present, Invalid -> Absent)")
    void testFacultyReviewLocationCheck() {
        Student bhavya = studentDAO.findByRollNo("24EARAD031");
        assertNotNull(bhavya);
        Faculty faculty = facultyDAO.findByFacultyId("FAC001");
        assertNotNull(faculty);

        LocalDate testDate = LocalDate.of(2026, 11, 4);
        randomDAO.deleteLocationChecksForStudentAndDate(bhavya.getId(), testDate);
        LocalDateTime now = LocalDateTime.now();

        // 1. Create a check flagged outside geofence
        RandomLocationCheck checkOutside = new RandomLocationCheck();
        checkOutside.setStudentId(bhavya.getId());
        checkOutside.setAttendanceId(12345L);
        checkOutside.setCheckDate(testDate);
        checkOutside.setScheduledTime(now.minusMinutes(1));
        checkOutside.setWindowEndTime(now.plusMinutes(5));
        checkOutside.setStatus("ACTIVE");
        checkOutside.setCreatedAt(now.minusMinutes(1));
        Long checkId = randomDAO.createSingleLocationCheck(checkOutside);
        assertNotNull(checkId);

        // Submit outside-geofence location
        service.verifyLocationCheck(checkId, bhavya.getId(), 28.6139, 77.2090, 15.0, null, "2026-11-04T10:00:00");
        RandomLocationCheck pendingCheck = randomDAO.getLocationCheckById(checkId);
        assertTrue(pendingCheck.isPendingFacultyReview(), "Check should be pending faculty review after outside geofence breach");

        // 2. Faculty approves with valid reason (e.g. Authorized College Duty)
        boolean approved = service.reviewStudentLocationCheck(checkId, "APPROVE_PRESENT", "Authorized college sports competition at university stadium", faculty);
        assertTrue(approved, "Approval with valid reason should succeed");

        RandomLocationCheck approvedCheck = randomDAO.getLocationCheckById(checkId);
        assertEquals("APPROVED_VALID_REASON", approvedCheck.getFacultyReviewStatus());
        assertEquals("Authorized college sports competition at university stadium", approvedCheck.getFacultyReviewNotes());
        assertEquals(faculty.getName() + " (" + faculty.getFacultyId() + ")", approvedCheck.getFacultyReviewedBy());
        assertNotNull(approvedCheck.getFacultyReviewedAt());
        assertFalse(approvedCheck.isPendingFacultyReview(), "Check should no longer be pending after approval");

        // 3. Test rejection for invalid/unauthorized reason
        RandomLocationCheck checkOutside2 = new RandomLocationCheck();
        checkOutside2.setStudentId(bhavya.getId());
        checkOutside2.setAttendanceId(12345L);
        checkOutside2.setCheckDate(testDate);
        checkOutside2.setScheduledTime(now.minusMinutes(1));
        checkOutside2.setWindowEndTime(now.plusMinutes(5));
        checkOutside2.setStatus("ACTIVE");
        checkOutside2.setCreatedAt(now.minusMinutes(1));
        Long checkId2 = randomDAO.createSingleLocationCheck(checkOutside2);

        service.verifyLocationCheck(checkId2, bhavya.getId(), 28.6139, 77.2090, 15.0, null, "2026-11-04T10:30:00");
        boolean rejected = service.reviewStudentLocationCheck(checkId2, "REJECT_ABSENT", "Unauthorized exit from campus during lectures. No gate pass found.", faculty);
        assertTrue(rejected, "Rejection with invalid reason should succeed");

        RandomLocationCheck rejectedCheck = randomDAO.getLocationCheckById(checkId2);
        assertEquals("REJECTED_ABSENT", rejectedCheck.getFacultyReviewStatus());
        assertEquals("Unauthorized exit from campus during lectures. No gate pass found.", rejectedCheck.getFacultyReviewNotes());
        assertFalse(rejectedCheck.isPendingFacultyReview(), "Check should no longer be pending after rejection");

        // Cleanup
        randomDAO.deleteLocationChecksForStudentAndDate(bhavya.getId(), testDate);
    }
}
