package com.extremis.service;

import com.extremis.dao.AttendanceDAO;
import com.extremis.dao.DBConnection;
import com.extremis.dao.RandomCheckInDAO;
import com.extremis.dao.StudentDAO;
import com.extremis.model.AttendanceRecord;
import com.extremis.model.Faculty;
import com.extremis.model.RandomChallenge;
import com.extremis.model.RandomCheckInSettings;
import com.extremis.model.RandomLocationCheck;
import com.extremis.model.Student;
import com.extremis.model.StudentRandomChallenge;

import java.io.InputStream;
import java.security.SecureRandom;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Properties;
import java.util.UUID;

public class RandomCheckInService {

    public static final ZoneId KOLKATA_ZONE = ZoneId.of("Asia/Kolkata");

    // Regular schedule gates (Asia/Kolkata)
    public static final LocalTime REGULAR_CHECKIN_END = LocalTime.of(9, 5);
    public static final LocalTime REGULAR_CHECKOUT_START = LocalTime.of(15, 40);

    // Lunch break exclusion gates (Asia/Kolkata)
    public static final LocalTime LUNCH_START = LocalTime.of(12, 20);
    public static final LocalTime LUNCH_END = LocalTime.of(13, 10);

    // Campus geofence reference coordinates (Arya College / Jaipur)
    public static final double CAMPUS_LAT = 26.9131;
    public static final double CAMPUS_LNG = 75.8089;
    public static final double MAX_CAMPUS_RADIUS_METERS = 1000.0; // 1km campus radius
    public static final double MAX_ACCURACY_METERS = 100.0;

    private final RandomCheckInDAO randomCheckInDAO = new RandomCheckInDAO();
    private final StudentDAO studentDAO = new StudentDAO();
    private final AttendanceDAO attendanceDAO = new AttendanceDAO();
    private final SecureRandom secureRandom = new SecureRandom();

    public LocalTime getEffectiveTime() {
        try (InputStream is = getClass().getClassLoader().getResourceAsStream("config.properties")) {
            if (is != null) {
                Properties props = new Properties();
                props.load(is);
                String override = props.getProperty("dev.timeOverride");
                if (override != null && !override.trim().isEmpty()) {
                    return LocalTime.parse(override.trim());
                }
            }
        } catch (Exception ignored) {
        }
        return LocalTime.now(KOLKATA_ZONE);
    }

    public LocalDate getEffectiveDate() {
        return LocalDate.now(KOLKATA_ZONE);
    }

    public LocalDateTime getEffectiveDateTime() {
        return LocalDateTime.of(getEffectiveDate(), getEffectiveTime());
    }

    public RandomCheckInSettings getSettings() {
        return randomCheckInDAO.getSettings();
    }

    public boolean saveSettings(RandomCheckInSettings settings) {
        return randomCheckInDAO.saveSettings(settings);
    }

    public boolean pauseRandomizer() {
        return randomCheckInDAO.setPaused(true);
    }

    public boolean resumeRandomizer() {
        return randomCheckInDAO.setPaused(false);
    }

    public boolean toggleRandomizer(boolean enabled) {
        boolean ok = randomCheckInDAO.setEnabled(enabled);
        if (ok) {
            if (!enabled) {
                cancelAllPendingLocationChecksToday();
            } else {
                scheduleAllEligibleStudentsToday();
            }
        }
        return ok;
    }

    public boolean isRandomizerEnabled() {
        return getSettings().isEnabled();
    }

    public boolean cancelChallenge(Long challengeId) {
        return randomCheckInDAO.cancelChallenge(challengeId);
    }

    /**
     * Determines whether a time falls strictly inside the lunch break (12:20 to 13:10).
     */
    public boolean isDuringLunchBreak(LocalTime time) {
        return !time.isBefore(LUNCH_START) && time.isBefore(LUNCH_END);
    }

    /**
     * Determines whether a challenge window (startTime to startTime + duration) overlaps the lunch break.
     */
    public boolean overlapsLunchBreak(LocalTime startTime, int durationMinutes) {
        LocalTime endTime = startTime.plusMinutes(durationMinutes);
        // Overlaps if startTime is before lunch end AND endTime is after lunch start
        return startTime.isBefore(LUNCH_END) && endTime.isAfter(LUNCH_START);
    }

    /**
     * Determines whether a proposed challenge window is valid with respect to academic rules and lunch break.
     */
    public boolean isValidChallengeWindow(LocalTime startTime, int durationMinutes) {
        if (startTime.isBefore(REGULAR_CHECKIN_END)) {
            return false;
        }
        LocalTime endTime = startTime.plusMinutes(durationMinutes);
        if (endTime.isAfter(REGULAR_CHECKOUT_START)) {
            return false;
        }
        return !overlapsLunchBreak(startTime, durationMinutes);
    }

    /**
     * Schedules the automatic random challenges for the given academic date.
     */
    public synchronized void scheduleDailyChallengesIfMissing(LocalDate date) {
        RandomCheckInSettings settings = getSettings();
        if (!settings.isEnabled() || settings.isPaused()) {
            return;
        }

        if (randomCheckInDAO.hasChallengesScheduledForDate(date)) {
            return;
        }

        int challengesNeeded = Math.max(1, settings.getChallengesPerDay());
        int duration = Math.max(1, settings.getDurationMinutes());
        int minInterval = Math.max(10, settings.getMinIntervalMinutes());

        LocalTime earliest = settings.getEarliestTime().isBefore(REGULAR_CHECKIN_END) ?
                REGULAR_CHECKIN_END.plusMinutes(5) : settings.getEarliestTime();
        LocalTime latest = settings.getLatestTime().isAfter(REGULAR_CHECKOUT_START) ?
                REGULAR_CHECKOUT_START.minusMinutes(duration) : settings.getLatestTime();

        List<LocalTime> validSlots = generateRandomSlots(earliest, latest, duration, challengesNeeded, minInterval);

        if (validSlots.isEmpty()) {
            // Fallback safe defaults if interval was too wide for slots
            validSlots = generateSafeDefaultSlots(duration);
        }

        List<Long> eligibleStudents = getEligibleStudentIds(settings.getTargetType(), settings.getTargetValue());

        for (int i = 0; i < validSlots.size(); i++) {
            LocalTime slot = validSlots.get(i);
            LocalDateTime scheduledTime = LocalDateTime.of(date, slot);
            LocalDateTime deadlineTime = scheduledTime.plusMinutes(duration);

            RandomChallenge c = new RandomChallenge();
            c.setChallengeCode("RC-" + date.toString().replace("-", "") + "-" + (i + 1) + "-" + UUID.randomUUID().toString().substring(0, 4).toUpperCase());
            c.setScheduledTime(scheduledTime);
            c.setDeadlineTime(deadlineTime);
            c.setDurationMinutes(duration);
            c.setStatus("SCHEDULED");
            c.setTargetType(settings.getTargetType());
            c.setTargetValue(settings.getTargetValue());
            c.setManual(false);
            c.setCreatedAt(getEffectiveDateTime());
            c.setCreatedBy("SYSTEM");

            randomCheckInDAO.createChallenge(c, eligibleStudents);
        }
    }

    /**
     * Manually triggers an immediate random challenge.
     */
    public synchronized String triggerManualChallenge(int durationMinutes, String targetType, String targetValue, String createdBy) {
        LocalTime nowTime = getEffectiveTime();

        if (nowTime.isBefore(REGULAR_CHECKIN_END)) {
            return "Cannot trigger challenge: Regular morning check-in window is open until 9:05 AM.";
        }
        if (isDuringLunchBreak(nowTime)) {
            return "Cannot trigger challenge: Lunch break is currently in progress (12:20 PM – 1:10 PM).";
        }
        if (overlapsLunchBreak(nowTime, durationMinutes)) {
            return "Cannot trigger challenge: Challenge duration would extend into the lunch break (12:20 PM).";
        }
        if (nowTime.plusMinutes(durationMinutes).isAfter(REGULAR_CHECKOUT_START)) {
            return "Cannot trigger challenge: Challenge deadline cannot extend at or past checkout time (3:40 PM).";
        }

        List<Long> eligibleStudents = getEligibleStudentIds(targetType, targetValue);
        if (eligibleStudents.isEmpty()) {
            return "No eligible students found for target " + targetType + " - " + targetValue + ".";
        }

        LocalDateTime scheduledTime = getEffectiveDateTime();
        LocalDateTime deadlineTime = scheduledTime.plusMinutes(durationMinutes);

        RandomChallenge c = new RandomChallenge();
        c.setChallengeCode("RC-MAN-" + UUID.randomUUID().toString().substring(0, 6).toUpperCase());
        c.setScheduledTime(scheduledTime);
        c.setDeadlineTime(deadlineTime);
        c.setDurationMinutes(durationMinutes);
        c.setStatus("ACTIVE"); // Manual triggers go straight to ACTIVE
        c.setTargetType(targetType != null ? targetType : "ALL");
        c.setTargetValue(targetValue != null ? targetValue : "ALL");
        c.setManual(true);
        c.setCreatedAt(scheduledTime);
        c.setCreatedBy(createdBy != null ? createdBy : "ADMIN");

        Long id = randomCheckInDAO.createChallenge(c, eligibleStudents);
        return (id != null) ? null : "Failed to record manual challenge due to a database error.";
    }

    /**
     * Background periodic tick: activates challenges that have reached their scheduled start time.
     */
    public synchronized void activateDueChallenges() {
        RandomCheckInSettings settings = getSettings();
        if (!settings.isEnabled() || settings.isPaused()) {
            return;
        }

        LocalDateTime now = getEffectiveDateTime();
        List<RandomChallenge> due = randomCheckInDAO.getScheduledChallengesDue(now);

        for (RandomChallenge c : due) {
            LocalTime scheduledLocal = c.getScheduledTime().toLocalTime();

            // If scheduled start falls into lunch break, automatically reschedule to 1:10 PM
            if (isDuringLunchBreak(scheduledLocal) || overlapsLunchBreak(scheduledLocal, c.getDurationMinutes())) {
                LocalDateTime postLunch = LocalDateTime.of(c.getScheduledTime().toLocalDate(), LUNCH_END.plusMinutes(1));
                c.setScheduledTime(postLunch);
                c.setDeadlineTime(postLunch.plusMinutes(c.getDurationMinutes()));
                // Update in DB
                updateRescheduledChallenge(c);
                continue;
            }

            randomCheckInDAO.markChallengeActive(c.getId());
        }
    }

    /**
     * Background periodic tick: marks expired challenges as completed and penalizes unverified students.
     */
    public synchronized void processExpiredChallenges() {
        LocalDateTime now = getEffectiveDateTime();
        List<RandomChallenge> expired = randomCheckInDAO.getExpiredActiveChallenges(now);

        for (RandomChallenge c : expired) {
            randomCheckInDAO.markChallengeCompleted(c.getId());
            List<Long> missedStudentIds = randomCheckInDAO.markPendingStudentsMissed(c.getId());

            LocalDate challengeDate = c.getScheduledTime().toLocalDate();

            for (Long sId : missedStudentIds) {
                applyMissedChallengeAttendancePenalty(sId, challengeDate, c.getChallengeCode());
            }
        }
    }

    /**
     * Submits a student's verification response for an active challenge.
     */
    public synchronized String submitStudentResponse(Long studentId, Long challengeId, Double lat, Double lng, Double accuracy, String photoPath) {
        if (lat == null || lng == null) {
            return "Verification rejected: Location coordinates are required.";
        }
        if (photoPath == null || photoPath.trim().isEmpty()) {
            return "Verification rejected: Live camera selfie capture is required.";
        }

        // Campus geofence validation
        double dist = calculateDistanceMeters(lat, lng, CAMPUS_LAT, CAMPUS_LNG);
        if (dist > MAX_CAMPUS_RADIUS_METERS) {
            return "Verification rejected: You are outside the authorized college campus geofence (" + Math.round(dist) + "m away).";
        }

        LocalDateTime now = getEffectiveDateTime();

        RandomChallenge c = randomCheckInDAO.getChallengeById(challengeId);
        if (c == null) {
            return "Challenge not found.";
        }
        if (!"ACTIVE".equals(c.getStatus())) {
            return "Challenge is no longer active.";
        }
        if (now.isAfter(c.getDeadlineTime())) {
            return "Response rejected: The challenge response deadline has expired.";
        }

        StudentRandomChallenge sc = randomCheckInDAO.getStudentChallenge(challengeId, studentId);
        if (sc == null) {
            return "You are not assigned to this random check-in challenge.";
        }
        if ("RESPONDED".equals(sc.getStatus())) {
            return "You have already completed this random check-in challenge.";
        }
        if ("MISSED".equals(sc.getStatus())) {
            return "Cannot submit: This challenge was marked missed after deadline.";
        }

        boolean ok = randomCheckInDAO.updateStudentResponse(challengeId, studentId, lat, lng, accuracy, photoPath, now);
        if (!ok) {
            return "Failed to save response. Please try again.";
        }

        // Ensure today's attendance record is verified PRESENT
        LocalDate today = now.toLocalDate();
        AttendanceRecord todayAtt = attendanceDAO.findByStudentAndDate(studentId, today);
        if (todayAtt != null) {
            // Confirm student status is PRESENT
            if ("ABSENT".equalsIgnoreCase(todayAtt.getStatus()) && todayAtt.getOverrideReason() != null && todayAtt.getOverrideReason().contains("Missed Random")) {
                // Restore present if erroneously marked
                attendanceDAO.recordOverride(studentId, today, "PRESENT", "Verified Random Check-In passed", "SYSTEM_RANDOMIZER", 1L);
            }
        }

        return null; // Success
    }

    public RandomChallenge getActiveChallengeForStudent(Long studentId) {
        return randomCheckInDAO.getActiveChallengeForStudent(studentId, getEffectiveDateTime());
    }

    public RandomChallenge getCurrentActiveChallenge() {
        return randomCheckInDAO.getCurrentActiveChallenge(getEffectiveDateTime());
    }

    public RandomChallenge getNextScheduledChallenge() {
        return randomCheckInDAO.getNextScheduledChallenge(getEffectiveDate(), getEffectiveDateTime());
    }

    public List<RandomChallenge> getChallengesForDate(LocalDate date) {
        return randomCheckInDAO.getChallengesForDate(date);
    }

    public List<RandomChallenge> getAllChallenges(int limit) {
        return randomCheckInDAO.getAllChallenges(limit);
    }

    public List<StudentRandomChallenge> getStudentResponses(Long challengeId) {
        return randomCheckInDAO.getStudentResponsesForChallenge(challengeId);
    }

    public RandomChallenge getChallengeById(Long challengeId) {
        return randomCheckInDAO.getChallengeById(challengeId);
    }

    // --- Private Helper Methods ---

    private List<Long> getEligibleStudentIds(String targetType, String targetValue) {
        List<Long> ids = new ArrayList<>();
        if ("CLASS".equalsIgnoreCase(targetType) && targetValue != null && !targetValue.equalsIgnoreCase("ALL")) {
            // Target is a class like "AI-A"
            List<Student> list = studentDAO.getAllStudents();
            for (Student s : list) {
                String classCode = s.getBranch() + "-" + (s.getSection().contains(" ") ? s.getSection().split(" ")[1] : s.getSection());
                if (targetValue.equalsIgnoreCase(classCode) || targetValue.equalsIgnoreCase(s.getBranch())) {
                    ids.add(s.getId());
                }
            }
        } else {
            // Target is all active students
            List<Student> list = studentDAO.getAllStudents();
            for (Student s : list) {
                ids.add(s.getId());
            }
        }
        return ids;
    }

    private void applyMissedChallengeAttendancePenalty(Long studentId, LocalDate date, String challengeCode) {
        AttendanceRecord record = attendanceDAO.findByStudentAndDate(studentId, date);
        String reason = "Missed Random Attendance Verification Challenge (" + challengeCode + ")";
        String overriddenBy = "SYSTEM_RANDOMIZER";

        if (record != null) {
            String sql = "UPDATE attendance SET status = 'ABSENT', is_override = 1, override_reason = ?, overridden_by = ? WHERE id = ?";
            try (Connection conn = DBConnection.getConnection();
                 PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, reason);
                ps.setString(2, overriddenBy);
                ps.setLong(3, record.getId());
                ps.executeUpdate();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        } else {
            // If student had not checked in, record as ABSENT due to missed verification
            String sql = "INSERT OR IGNORE INTO attendance (student_id, date, status, is_override, override_reason, overridden_by) " +
                    "VALUES (?, ?, 'ABSENT', 1, ?, ?)";
            try (Connection conn = DBConnection.getConnection();
                 PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setLong(1, studentId);
                ps.setString(2, date.toString());
                ps.setString(3, reason);
                ps.setString(4, overriddenBy);
                ps.executeUpdate();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        // Record immutable override audit
        String auditSql = "INSERT INTO attendance_overrides (attendance_id, student_id, faculty_id, original_status, updated_status, reason, attendance_date, override_time) " +
                "VALUES (?, ?, 1, 'PRESENT', 'ABSENT', ?, ?, datetime('now'))";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(auditSql)) {
            ps.setLong(1, record != null ? record.getId() : 0L);
            ps.setLong(2, studentId);
            ps.setString(3, reason);
            ps.setString(4, date.toString());
            ps.executeUpdate();
        } catch (SQLException ignored) {}
    }

    private void updateRescheduledChallenge(RandomChallenge c) {
        String sql = "UPDATE random_challenges SET scheduled_time = ?, deadline_time = ? WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, c.getScheduledTime().toString());
            ps.setString(2, c.getDeadlineTime().toString());
            ps.setLong(3, c.getId());
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private List<LocalTime> generateRandomSlots(LocalTime earliest, LocalTime latest, int duration, int count, int minInterval) {
        List<LocalTime> slots = new ArrayList<>();

        // Morning window: from earliest to lunch start (12:20 - duration)
        LocalTime morningEnd = LUNCH_START.minusMinutes(duration);
        boolean hasMorning = earliest.isBefore(morningEnd);

        // Afternoon window: from lunch end (13:10) to latest
        LocalTime afternoonStart = LUNCH_END;
        boolean hasAfternoon = afternoonStart.isBefore(latest);

        // Collect valid 5-minute candidate slices in morning and afternoon
        List<LocalTime> morningCandidates = new ArrayList<>();
        if (hasMorning) {
            LocalTime cur = earliest;
            while (!cur.isAfter(morningEnd)) {
                morningCandidates.add(cur);
                cur = cur.plusMinutes(5);
            }
        }

        List<LocalTime> afternoonCandidates = new ArrayList<>();
        if (hasAfternoon) {
            LocalTime cur = afternoonStart;
            while (!cur.isAfter(latest)) {
                afternoonCandidates.add(cur);
                cur = cur.plusMinutes(5);
            }
        }

        // Try picking slots satisfying minInterval
        int morningCount = count / 2;
        int afternoonCount = count - morningCount;

        if (morningCandidates.isEmpty()) {
            afternoonCount = count;
            morningCount = 0;
        } else if (afternoonCandidates.isEmpty()) {
            morningCount = count;
            afternoonCount = 0;
        }

        pickSlotsFromCandidates(morningCandidates, morningCount, minInterval, slots);
        pickSlotsFromCandidates(afternoonCandidates, afternoonCount, minInterval, slots);

        Collections.sort(slots);
        return slots;
    }

    private void pickSlotsFromCandidates(List<LocalTime> candidates, int needed, int minInterval, List<LocalTime> result) {
        if (candidates.isEmpty() || needed <= 0) return;
        List<LocalTime> pool = new ArrayList<>(candidates);
        Collections.shuffle(pool, secureRandom);

        int picked = 0;
        for (LocalTime candidate : pool) {
            if (picked >= needed) break;
            boolean ok = true;
            for (LocalTime existing : result) {
                long diffMin = Math.abs(java.time.Duration.between(existing, candidate).toMinutes());
                if (diffMin < minInterval) {
                    ok = false;
                    break;
                }
            }
            if (ok) {
                result.add(candidate);
                picked++;
            }
        }
    }

    private List<LocalTime> generateSafeDefaultSlots(int duration) {
        List<LocalTime> list = new ArrayList<>();
        // 10:30 AM (pre-lunch)
        LocalTime slot1 = LocalTime.of(10, 30);
        if (isValidChallengeWindow(slot1, duration)) {
            list.add(slot1);
        }
        // 2:00 PM (post-lunch)
        LocalTime slot2 = LocalTime.of(14, 0);
        if (isValidChallengeWindow(slot2, duration)) {
            list.add(slot2);
        }
        return list;
    }

    public double calculateDistanceMeters(double lat1, double lon1, double lat2, double lon2) {
        final int R = 6371000; // Radius of the Earth in meters
        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c;
    }

    // =========================================================================
    // Individual Random Location Verification Checks (Requirements Section 2, 3, 4)
    // =========================================================================

    /**
     * Schedules 4-5 random location verification events for an individual student for today.
     * Guaranteed gates:
     * 1. Only after successful morning check-in.
     * 2. No checks before 9:05 AM or at/after 3:40 PM.
     * 3. Never scheduled or executed during lunch break (12:20 PM - 1:10 PM).
     * 4. Each student has their own unique unpredictable random verification schedule.
     */
    public synchronized boolean scheduleStudentDailyVerifications(Long studentId, LocalDate date, Long attendanceId) {
        RandomCheckInSettings settings = getSettings();
        if (!settings.isEnabled() || settings.isPaused()) {
            return false;
        }

        if (randomCheckInDAO.hasStudentLocationChecks(studentId, date)) {
            return false; // Prevent duplicate daily schedules for the same student
        }

        int windowMinutes = Math.max(5, settings.getDurationMinutes());
        // Pick randomly 4 or 5 checks for this student
        int totalChecks = 4 + secureRandom.nextInt(2); // 4 or 5

        List<LocalTime> studentSlots = generateStudentUniqueSlots(totalChecks, windowMinutes);
        if (studentSlots.isEmpty()) {
            return false;
        }

        List<RandomLocationCheck> checks = new ArrayList<>();
        LocalDateTime now = getEffectiveDateTime();

        for (LocalTime slot : studentSlots) {
            LocalDateTime scheduledTime = LocalDateTime.of(date, slot);
            LocalDateTime windowEndTime = scheduledTime.plusMinutes(windowMinutes);

            RandomLocationCheck c = new RandomLocationCheck();
            c.setStudentId(studentId);
            c.setAttendanceId(attendanceId);
            c.setCheckDate(date);
            c.setScheduledTime(scheduledTime);
            c.setWindowEndTime(windowEndTime);
            c.setStatus("SCHEDULED");
            c.setVerificationResult(null);
            c.setCreatedAt(now);

            checks.add(c);
        }

        return randomCheckInDAO.createLocationChecks(checks);
    }

    /**
     * Generates 4-5 unique, unpredictable slot times for an individual student.
     * Strict rules:
     * - Earliest start time: 9:06 AM (after 9:05 AM morning check-in closes)
     * - Morning window end: slot + window <= 12:20 PM (before lunch begins)
     * - Lunch exclusion: 12:20 PM - 1:10 PM strictly excluded
     * - Afternoon window start: 1:10 PM
     * - Afternoon window end: slot + window <= 3:40 PM (before checkout starts)
     */
    public List<LocalTime> generateStudentUniqueSlots(int count, int windowMinutes) {
        List<LocalTime> slots = new ArrayList<>();

        // Morning boundaries: 09:10 to (12:20 - windowMinutes)
        LocalTime morningEarliest = LocalTime.of(9, 10);
        LocalTime morningLatest = LUNCH_START.minusMinutes(windowMinutes);

        // Afternoon boundaries: 13:15 to (15:40 - windowMinutes)
        LocalTime afternoonEarliest = LUNCH_END.plusMinutes(5);
        LocalTime afternoonLatest = REGULAR_CHECKOUT_START.minusMinutes(windowMinutes);

        int morningCount = count / 2; // e.g. 2
        int afternoonCount = count - morningCount; // e.g. 2 or 3

        // Collect 5-minute candidate slices in morning
        List<LocalTime> morningPool = new ArrayList<>();
        LocalTime t = morningEarliest;
        while (!t.isAfter(morningLatest)) {
            morningPool.add(t);
            t = t.plusMinutes(5);
        }

        // Collect 5-minute candidate slices in afternoon
        List<LocalTime> afternoonPool = new ArrayList<>();
        t = afternoonEarliest;
        while (!t.isAfter(afternoonLatest)) {
            afternoonPool.add(t);
            t = t.plusMinutes(5);
        }

        // Randomly pick separated slots for morning
        pickSeparatedRandomSlots(morningPool, morningCount, 25, slots);

        // Randomly pick separated slots for afternoon
        pickSeparatedRandomSlots(afternoonPool, afternoonCount, 25, slots);

        Collections.sort(slots);
        return slots;
    }

    private void pickSeparatedRandomSlots(List<LocalTime> pool, int needed, int minIntervalMinutes, List<LocalTime> result) {
        if (pool == null || pool.isEmpty() || needed <= 0) return;
        List<LocalTime> shuffled = new ArrayList<>(pool);
        Collections.shuffle(shuffled, secureRandom);

        int picked = 0;
        for (LocalTime candidate : shuffled) {
            if (picked >= needed) break;
            boolean ok = true;
            for (LocalTime existing : result) {
                long diff = Math.abs(java.time.Duration.between(existing, candidate).toMinutes());
                if (diff < minIntervalMinutes) {
                    ok = false;
                    break;
                }
            }
            if (ok) {
                result.add(candidate);
                picked++;
            }
        }
    }

    public synchronized void scheduleAllEligibleStudentsToday() {
        RandomCheckInSettings settings = getSettings();
        if (!settings.isEnabled() || settings.isPaused()) {
            return;
        }

        LocalDate today = getEffectiveDate();
        List<Student> students = studentDAO.getAllStudents();
        for (Student s : students) {
            AttendanceRecord att = attendanceDAO.findByStudentAndDate(s.getId(), today);
            if (att != null && att.getCheckInTime() != null) {
                // Student checked in today
                if (!randomCheckInDAO.hasStudentLocationChecks(s.getId(), today)) {
                    scheduleStudentDailyVerifications(s.getId(), today, att.getId());
                }
            }
        }
    }

    public synchronized void cancelAllPendingLocationChecksToday() {
        randomCheckInDAO.cancelAllPendingLocationChecksToday(getEffectiveDate());
    }

    public RandomLocationCheck getPendingLocationCheckForStudent(Long studentId) {
        RandomCheckInSettings settings = getSettings();
        if (!settings.isEnabled() || settings.isPaused()) {
            return null;
        }
        return randomCheckInDAO.getPendingLocationCheckForStudent(studentId, getEffectiveDateTime());
    }

    public synchronized String verifyLocationCheck(Long checkId, Long studentId, Double lat, Double lng, Double accuracy, String error, String clientTimestamp) {
        RandomLocationCheck check = randomCheckInDAO.getLocationCheckById(checkId);
        if (check == null) {
            return "Verification check event not found.";
        }
        if (!check.getStudentId().equals(studentId)) {
            return "Unauthorized: Verification check does not belong to this student.";
        }
        if (!"SCHEDULED".equals(check.getStatus()) && !"ACTIVE".equals(check.getStatus())) {
            return "This verification check is already completed or inactive.";
        }

        LocalDateTime now = getEffectiveDateTime();
        RandomCheckInSettings settings = getSettings();
        if (!settings.isEnabled()) {
            return "Random location verification is currently turned OFF by administrator.";
        }

        // Case 1: Client reported device error (permission denied, GPS unavailable, timeout)
        if (error != null && !error.trim().isEmpty()) {
            String sanitizedError = error.trim().toUpperCase();
            String reason = "Device reported error: " + error;
            randomCheckInDAO.updateLocationCheckResponse(
                    checkId, studentId, "FAILED", sanitizedError, null, null, null, null, now, clientTimestamp, reason, false
            );
            // CRITICAL Rule 11: Do not automatically mark a student absent merely because GPS is unavailable,
            // permission is denied, the phone is offline, or location accuracy is poor.
            return null; // Successfully processed and recorded as failure
        }

        // Case 2: Location coordinates missing
        if (lat == null || lng == null) {
            randomCheckInDAO.updateLocationCheckResponse(
                    checkId, studentId, "FAILED", "GPS_UNAVAILABLE", null, null, null, null, now, clientTimestamp, "Location coordinates unavailable", false
            );
            return null;
        }

        double distance = calculateDistanceMeters(lat, lng, CAMPUS_LAT, CAMPUS_LNG);

        // Case 3: Poor location accuracy (> 100 meters)
        if (accuracy != null && accuracy > MAX_ACCURACY_METERS) {
            String reason = "GPS accuracy (" + Math.round(accuracy) + "m) exceeds authorized limit of 100m";
            randomCheckInDAO.updateLocationCheckResponse(
                    checkId, studentId, "FAILED", "ACCURACY_POOR", lat, lng, accuracy, distance, now, clientTimestamp, reason, false
            );
            // Rule 11: Recorded as verification failure, NOT automatically marked absent
            return null;
        }

        // Case 4: Location inside college campus geofence (<= 1000m)
        if (distance <= MAX_CAMPUS_RADIUS_METERS) {
            randomCheckInDAO.updateLocationCheckResponse(
                    checkId, studentId, "VERIFIED", "VERIFIED_CAMPUS", lat, lng, accuracy, distance, now, clientTimestamp, "Verified within college campus geofence", false
            );
            return null; // Verified inside
        }

        // Case 5: Verified outside college campus geofence (> 1000m) with high accuracy
        String reason = "Verified outside campus geofence (" + Math.round(distance) + "m away) - Pending Faculty Verification";
        randomCheckInDAO.updateLocationCheckResponse(
                checkId, studentId, "VERIFIED", "OUTSIDE_GEOFENCE", lat, lng, accuracy, distance, now, clientTimestamp, reason, true
        );

        // Put attendance under verification for faculty to review whether student had a true reason
        LocalDate today = check.getCheckDate();
        AttendanceRecord att = attendanceDAO.findByStudentAndDate(studentId, today);
        if (att != null) {
            attendanceDAO.recordOverride(
                    studentId, today, "PENDING_VERIFICATION",
                    "Attendance Under Verification: Outside campus geofence (" + Math.round(distance) + "m away) during random check. Awaiting faculty verification of reason.",
                    "SYSTEM_RANDOMIZER", 1L
            );
        }

        return null;
    }

    /**
     * Faculty reviews a student's outside-campus random verification:
     * - If decision is APPROVE_PRESENT: Student had a true/valid reason (official duty, medical, etc.) -> Mark PRESENT.
     * - If decision is REJECT_ABSENT: Student had no true reason -> Confirm ABSENT.
     */
    public synchronized boolean reviewStudentLocationCheck(Long checkId, String decision, String reasonNotes, Faculty faculty) {
        if (checkId == null || decision == null || faculty == null) {
            return false;
        }

        RandomLocationCheck check = randomCheckInDAO.getLocationCheckById(checkId);
        if (check == null) {
            return false;
        }

        LocalDateTime now = getEffectiveDateTime();
        String reviewerIdentifier = faculty.getName() + " (" + faculty.getFacultyId() + ")";
        String sanitizedNotes = (reasonNotes != null && !reasonNotes.trim().isEmpty()) ? reasonNotes.trim() : "Faculty location review completed";

        if ("APPROVE_PRESENT".equalsIgnoreCase(decision)) {
            attendanceDAO.recordOverride(
                    check.getStudentId(), check.getCheckDate(), "PRESENT",
                    "Faculty Verified (Valid Reason): " + sanitizedNotes,
                    reviewerIdentifier, faculty.getId()
            );
            return randomCheckInDAO.resolveLocationCheckReview(
                    checkId, "APPROVED_VALID_REASON", sanitizedNotes, reviewerIdentifier, now
            );
        } else {
            attendanceDAO.recordOverride(
                    check.getStudentId(), check.getCheckDate(), "ABSENT",
                    "Faculty Confirmed Absent (Unauthorized Outside Geofence): " + sanitizedNotes,
                    reviewerIdentifier, faculty.getId()
            );
            return randomCheckInDAO.resolveLocationCheckReview(
                    checkId, "REJECTED_ABSENT", sanitizedNotes, reviewerIdentifier, now
            );
        }
    }

    public synchronized void activateDueLocationChecks() {
        RandomCheckInSettings settings = getSettings();
        if (!settings.isEnabled() || settings.isPaused()) {
            return;
        }

        LocalDateTime now = getEffectiveDateTime();
        List<RandomLocationCheck> due = randomCheckInDAO.getActiveLocationChecksDue(now);
        for (RandomLocationCheck c : due) {
            randomCheckInDAO.markLocationCheckActive(c.getId());
        }
    }

    public synchronized void processExpiredLocationChecks() {
        LocalDateTime now = getEffectiveDateTime();
        List<RandomLocationCheck> expired = randomCheckInDAO.getExpiredActiveLocationChecks(now);
        for (RandomLocationCheck c : expired) {
            randomCheckInDAO.markLocationCheckExpired(c.getId(), now);
            // CRITICAL Rule 11: Do NOT automatically mark student absent because phone was offline/unreachable
        }
    }

    public List<RandomLocationCheck> getRecentLocationChecks(int limit) {
        return randomCheckInDAO.getRecentLocationChecks(limit);
    }

    public List<RandomLocationCheck> getLocationChecksForDate(LocalDate date) {
        return randomCheckInDAO.getLocationChecksForDate(date);
    }

    public List<RandomLocationCheck> getLocationChecksForStudent(Long studentId, LocalDate date) {
        return randomCheckInDAO.getLocationChecksForStudent(studentId, date);
    }

    public List<RandomLocationCheck> getLocationChecksForFaculty(Long facultyId, LocalDate date) {
        return randomCheckInDAO.getLocationChecksForFaculty(facultyId, date);
    }
}
