package com.extremis.service;

import com.extremis.dao.AttendanceDAO;
import com.extremis.dao.FacultyDAO;
import com.extremis.dao.RandomCheckInDAO;
import com.extremis.dao.StudentDAO;
import com.extremis.model.AttendanceRecord;
import com.extremis.model.Faculty;
import com.extremis.model.RandomLocationCheck;
import com.extremis.model.Student;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class LiveWorkflowVerificationTest {

    private final RandomCheckInService service = new RandomCheckInService();
    private final RandomCheckInDAO randomDAO = new RandomCheckInDAO();
    private final StudentDAO studentDAO = new StudentDAO();
    private final FacultyDAO facultyDAO = new FacultyDAO();
    private final AttendanceDAO attendanceDAO = new AttendanceDAO();

    @Test
    @DisplayName("Verify Full End-to-End Random Location Verification & Faculty Decision Workflow")
    void testEndToEndRandomLocationWorkflow() {
        Student bhavya = studentDAO.findByRollNo("24EARAD031");
        assertNotNull(bhavya, "Student 24EARAD031 must exist in DB");

        Faculty faculty = facultyDAO.findByFacultyId("FAC001");
        assertNotNull(faculty, "Faculty FAC001 must exist in DB");

        LocalDate today = service.getEffectiveDate();
        LocalDateTime now = service.getEffectiveDateTime();

        // 1. Setup morning attendance record for Bhavya if none exists
        AttendanceRecord att = attendanceDAO.findByStudentAndDate(bhavya.getId(), today);
        Long attendanceId;
        if (att == null) {
            AttendanceRecord rec = new AttendanceRecord();
            rec.setStudentId(bhavya.getId());
            rec.setDate(today);
            rec.setCheckInTime(LocalTime.of(8, 55));
            rec.setStatus("PRESENT");
            rec.setCheckinLat(RandomCheckInService.CAMPUS_LAT);
            rec.setCheckinLng(RandomCheckInService.CAMPUS_LNG);
            rec.setCheckinAccuracy(12.0);
            rec.setCheckinPhotoPath("SELFIE_HASH_OK");
            boolean ok = attendanceDAO.insertCheckIn(rec);
            assertTrue(ok);
            att = attendanceDAO.findByStudentAndDate(bhavya.getId(), today);
        }
        assertNotNull(att);
        attendanceId = att.getId();
        assertNotNull(attendanceId);

        // 2. Clear old test checks for today to have a clean state
        randomDAO.deleteLocationChecksForStudentAndDate(bhavya.getId(), today);

        // 3. Test Check 1: Inside Campus (< 1000m)
        RandomLocationCheck campusCheck = new RandomLocationCheck();
        campusCheck.setStudentId(bhavya.getId());
        campusCheck.setAttendanceId(attendanceId);
        campusCheck.setCheckDate(today);
        campusCheck.setScheduledTime(now.minusMinutes(10));
        campusCheck.setWindowEndTime(now.plusMinutes(10));
        campusCheck.setStatus("ACTIVE");
        campusCheck.setCreatedAt(now.minusMinutes(10));
        Long campusCheckId = randomDAO.createSingleLocationCheck(campusCheck);
        assertNotNull(campusCheckId);

        // Inside campus coordinates (Jaipur campus lat 26.9131, lng 75.8089)
        String resCampus = service.verifyLocationCheck(campusCheckId, bhavya.getId(),
                RandomCheckInService.CAMPUS_LAT + 0.001, RandomCheckInService.CAMPUS_LNG + 0.001, 15.0, null,
                now.toString());
        assertNull(resCampus, "Inside campus check must succeed");

        RandomLocationCheck updatedCampus = randomDAO.getLocationCheckById(campusCheckId);
        assertEquals("VERIFIED", updatedCampus.getStatus());
        assertEquals("VERIFIED_CAMPUS", updatedCampus.getVerificationResult());
        assertFalse(updatedCampus.isFlaggedForReview(), "Inside campus check must NOT be flagged for review");
        assertTrue(updatedCampus.getDistanceMeters() < 1000.0, "Distance must be < 1000m");

        // 4. Test Check 2: Outside Geofence (> 1000m) -> Goes to PENDING_VERIFICATION
        RandomLocationCheck outsideCheck = new RandomLocationCheck();
        outsideCheck.setStudentId(bhavya.getId());
        outsideCheck.setAttendanceId(attendanceId);
        outsideCheck.setCheckDate(today);
        outsideCheck.setScheduledTime(now.minusMinutes(5));
        outsideCheck.setWindowEndTime(now.plusMinutes(15));
        outsideCheck.setStatus("ACTIVE");
        outsideCheck.setCreatedAt(now.minusMinutes(5));
        Long outsideCheckId = randomDAO.createSingleLocationCheck(outsideCheck);
        assertNotNull(outsideCheckId);

        // Location 5.4 km away (e.g. Jaipur central bus stand, lat 26.9260, lng
        // 75.7900)
        String resOutside = service.verifyLocationCheck(outsideCheckId, bhavya.getId(),
                26.9260, 75.7900, 20.0, null, now.toString());
        assertNull(resOutside, "Outside check submission must succeed");

        RandomLocationCheck updatedOutside = randomDAO.getLocationCheckById(outsideCheckId);
        assertEquals("VERIFIED", updatedOutside.getStatus());
        assertEquals("OUTSIDE_GEOFENCE", updatedOutside.getVerificationResult());
        assertTrue(updatedOutside.isFlaggedForReview(), "Outside geofence check MUST be flagged for review");
        assertTrue(updatedOutside.getDistanceMeters() > 1000.0, "Distance must be > 1000m");
        assertTrue(updatedOutside.isPendingFacultyReview(), "Check must be pending faculty review");

        // Verify Attendance status is PENDING_VERIFICATION
        AttendanceRecord attPending = attendanceDAO.findByStudentAndDate(bhavya.getId(), today);
        assertEquals("PENDING_VERIFICATION", attPending.getStatus(),
                "Attendance must be PENDING_VERIFICATION while awaiting faculty verification of reason");

        // 5. Test Faculty Visibility: Faculty can see this check
        List<RandomLocationCheck> facultyChecks = service.getLocationChecksForFaculty(faculty.getId(), today);
        assertFalse(facultyChecks.isEmpty(), "Faculty must see location checks for their assigned students");
        boolean foundOutside = false;
        for (RandomLocationCheck c : facultyChecks) {
            if (c.getId().equals(outsideCheckId)) {
                foundOutside = true;
                break;
            }
        }
        assertTrue(foundOutside, "Faculty must see the outside geofence check");

        // 6. Test Faculty Review: Decision = APPROVE_PRESENT (Student had valid reason)
        boolean reviewApproved = service.reviewStudentLocationCheck(
                outsideCheckId, "APPROVE_PRESENT", "Approved duty for inter-college hackathon representation", faculty);
        assertTrue(reviewApproved, "Faculty approval must succeed");

        RandomLocationCheck postApprovalCheck = randomDAO.getLocationCheckById(outsideCheckId);
        assertEquals("APPROVED_VALID_REASON", postApprovalCheck.getFacultyReviewStatus());
        assertEquals("Approved duty for inter-college hackathon representation",
                postApprovalCheck.getFacultyReviewNotes());
        assertFalse(postApprovalCheck.isPendingFacultyReview(),
                "Check must no longer be pending after faculty approval");

        // Verify Attendance status is now updated to PRESENT
        AttendanceRecord attApproved = attendanceDAO.findByStudentAndDate(bhavya.getId(), today);
        assertEquals("PRESENT", attApproved.getStatus(),
                "Attendance must be updated to PRESENT when faculty approves with valid reason");

        System.out.println("=== END-TO-END RANDOM LOCATION VERIFICATION TEST PASSED SUCCESSFULLY ===");
    }

    @Test
    @DisplayName("Setup Live Pending Location Verification for Browser UI Testing")
    void testSetupLivePendingVerification() {
        Student bhavya = studentDAO.findByRollNo("24EARAD031");
        assertNotNull(bhavya);

        LocalDate today = service.getEffectiveDate();
        LocalDateTime now = service.getEffectiveDateTime();

        AttendanceRecord att = attendanceDAO.findByStudentAndDate(bhavya.getId(), today);
        Long attendanceId;
        if (att == null) {
            AttendanceRecord rec = new AttendanceRecord();
            rec.setStudentId(bhavya.getId());
            rec.setDate(today);
            rec.setCheckInTime(LocalTime.of(8, 55));
            rec.setStatus("PRESENT");
            rec.setCheckinLat(RandomCheckInService.CAMPUS_LAT);
            rec.setCheckinLng(RandomCheckInService.CAMPUS_LNG);
            rec.setCheckinAccuracy(12.0);
            rec.setCheckinPhotoPath("SELFIE_HASH_OK");
            attendanceDAO.insertCheckIn(rec);
            att = attendanceDAO.findByStudentAndDate(bhavya.getId(), today);
        }
        attendanceId = att.getId();

        // Clear old checks
        randomDAO.deleteLocationChecksForStudentAndDate(bhavya.getId(), today);

        // Check 1: Inside campus (verified)
        RandomLocationCheck campusCheck = new RandomLocationCheck();
        campusCheck.setStudentId(bhavya.getId());
        campusCheck.setAttendanceId(attendanceId);
        campusCheck.setCheckDate(today);
        campusCheck.setScheduledTime(now.minusHours(2));
        campusCheck.setWindowEndTime(now.minusHours(2).plusMinutes(10));
        campusCheck.setStatus("ACTIVE");
        campusCheck.setCreatedAt(now.minusHours(2));
        Long c1 = randomDAO.createSingleLocationCheck(campusCheck);
        service.verifyLocationCheck(c1, bhavya.getId(), RandomCheckInService.CAMPUS_LAT,
                RandomCheckInService.CAMPUS_LNG, 15.0, null, now.minusHours(2).toString());

        // Check 2: Outside Geofence awaiting faculty review!
        RandomLocationCheck outsideCheck = new RandomLocationCheck();
        outsideCheck.setStudentId(bhavya.getId());
        outsideCheck.setAttendanceId(attendanceId);
        outsideCheck.setCheckDate(today);
        outsideCheck.setScheduledTime(now.minusMinutes(10));
        outsideCheck.setWindowEndTime(now.plusMinutes(10));
        outsideCheck.setStatus("ACTIVE");
        outsideCheck.setCreatedAt(now.minusMinutes(10));
        Long c2 = randomDAO.createSingleLocationCheck(outsideCheck);

        // Submits location 5.4km outside campus
        service.verifyLocationCheck(c2, bhavya.getId(), 26.9260, 75.7900, 20.0, null, now.toString());

        System.out.println("LIVE PENDING CHECK CREATED WITH ID: " + c2);
    }
}
