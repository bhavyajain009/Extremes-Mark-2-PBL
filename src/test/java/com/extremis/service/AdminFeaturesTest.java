package com.extremis.service;

import com.extremis.dao.AttendanceTimerDAO;
import com.extremis.dao.ClassDAO;
import com.extremis.dao.StudentDAO;
import com.extremis.model.AttendanceTimerSettings;
import com.extremis.model.ClassEntity;
import com.extremis.model.Student;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.time.LocalTime;
import java.util.List;

public class AdminFeaturesTest {

    private static AttendanceTimerDAO timerDAO;
    private static StudentDAO studentDAO;
    private static ClassDAO classDAO;
    private static StudentImportService importService;

    @BeforeAll
    public static void setup() {
        timerDAO = new AttendanceTimerDAO();
        studentDAO = new StudentDAO();
        classDAO = new ClassDAO();
        importService = new StudentImportService();
    }

    @Test
    public void testCheckinAndCheckoutTimerControlsAndSlots() {
        // 1. Get initial settings
        AttendanceTimerSettings s = timerDAO.getSettings();
        Assertions.assertNotNull(s);

        // 2. Change slot durations and times
        s.setCheckinTimerEnabled(true);
        s.setCheckinStartTime(LocalTime.of(8, 30));
        s.setCheckinEndTime(LocalTime.of(9, 30));
        s.setCheckinDurationMinutes(60);

        s.setCheckoutTimerEnabled(true);
        s.setCheckoutStartTime(LocalTime.of(15, 0));
        s.setCheckoutEndTime(LocalTime.of(17, 30));
        s.setCheckoutDurationMinutes(150);

        boolean saved = timerDAO.saveSettings(s);
        Assertions.assertTrue(saved);

        AttendanceTimerSettings reloaded = timerDAO.getSettings();
        Assertions.assertEquals(LocalTime.of(8, 30), reloaded.getCheckinStartTime());
        Assertions.assertEquals(LocalTime.of(9, 30), reloaded.getCheckinEndTime());
        Assertions.assertEquals(60, reloaded.getCheckinDurationMinutes());
        Assertions.assertEquals(LocalTime.of(15, 0), reloaded.getCheckoutStartTime());
        Assertions.assertEquals(LocalTime.of(17, 30), reloaded.getCheckoutEndTime());
        Assertions.assertEquals(150, reloaded.getCheckoutDurationMinutes());

        // 3. Test Turning OFF checkin timer
        boolean okOff = timerDAO.setCheckinTimerEnabled(false);
        Assertions.assertTrue(okOff);
        AttendanceTimerSettings offSettings = timerDAO.getSettings();
        Assertions.assertFalse(offSettings.isCheckinTimerEnabled());
        // When timer is off, window is active anytime
        Assertions.assertTrue(offSettings.isCheckinWindowActive(LocalTime.of(11, 45)));

        // 4. Test Turning ON checkin timer again
        boolean okOn = timerDAO.setCheckinTimerEnabled(true);
        Assertions.assertTrue(okOn);
        AttendanceTimerSettings onSettings = timerDAO.getSettings();
        Assertions.assertTrue(onSettings.isCheckinTimerEnabled());
        Assertions.assertTrue(onSettings.isCheckinWindowActive(LocalTime.of(9, 0)));
        Assertions.assertFalse(onSettings.isCheckinWindowActive(LocalTime.of(11, 45)));
    }

    @Test
    public void testClassWiseStudentList() {
        List<ClassEntity> classes = classDAO.getAllClasses();
        Assertions.assertFalse(classes.isEmpty(), "Classes should exist in database");

        ClassEntity c1 = classes.get(0);
        List<Student> classStudents = studentDAO.getStudentsByClassId(c1.getId());
        Assertions.assertNotNull(classStudents);
        for (Student s : classStudents) {
            Assertions.assertEquals(c1.getId(), s.getClassId());
        }
    }

    @Test
    public void testAddAndRemoveStudent() {
        String testRoll = "TEST_ROLL_999";
        // Clean up if left from previous runs
        Student existing = studentDAO.findByRollNo(testRoll);
        if (existing != null) {
            studentDAO.deleteStudent(existing.getId());
        }

        Assertions.assertFalse(studentDAO.rollNoExists(testRoll));

        Student s = new Student();
        s.setRollNo(testRoll);
        s.setName("Test Student Automated");
        s.setEmail("test999@aryacollege.edu.in");
        s.setBranch("AI");
        s.setSection("Section A");
        s.setClassId(1L);
        s.setBatchId(1L);

        boolean added = studentDAO.insertStudent(s);
        Assertions.assertTrue(added, "Should successfully insert student");
        Assertions.assertTrue(studentDAO.rollNoExists(testRoll));

        Student retrieved = studentDAO.findByRollNo(testRoll);
        Assertions.assertNotNull(retrieved);
        Assertions.assertEquals("Test Student Automated", retrieved.getName());

        // Delete test student
        boolean deleted = studentDAO.deleteStudent(retrieved.getId());
        Assertions.assertTrue(deleted);
        Assertions.assertFalse(studentDAO.rollNoExists(testRoll));
    }

    @Test
    public void testTemplateGenerationAndCsvMassImport() throws Exception {
        // 1. Generate Excel Template (.xlsx)
        byte[] xlsxBytes = importService.generateExcelTemplate();
        Assertions.assertNotNull(xlsxBytes);
        Assertions.assertTrue(xlsxBytes.length > 1000, "Excel template should have binary content");

        // 2. Generate CSV Template
        byte[] csvBytes = importService.generateCsvTemplate();
        Assertions.assertNotNull(csvBytes);
        String csvContent = new String(csvBytes);
        Assertions.assertTrue(csvContent.contains("Roll Number"));

        // 3. Test Mass Import with CSV data
        String testCsv = "Roll Number,Full Name,Email Address,Class Code,Batch Code,Password\n" +
                "24TEST_CSV_01,CSV Imported Student 1,csv1@test.com,AI-A,A1,Pass123!\n" +
                "24TEST_CSV_02,CSV Imported Student 2,csv2@test.com,CS-A,C1,Pass123!\n";

        StudentImportService.ImportResult result = importService.importStudents(
                new ByteArrayInputStream(testCsv.getBytes()), "import_data.csv");

        Assertions.assertEquals(2, result.getTotalRows());
        Assertions.assertTrue(result.getImportedCount() >= 1 || result.getUpdatedCount() >= 1);

        Student s1 = studentDAO.findByRollNo("24TEST_CSV_01");
        Assertions.assertNotNull(s1);
        Assertions.assertEquals("CSV Imported Student 1", s1.getName());

        Student s2 = studentDAO.findByRollNo("24TEST_CSV_02");
        Assertions.assertNotNull(s2);
        Assertions.assertEquals("CSV Imported Student 2", s2.getName());

        // Clean up test imported records
        studentDAO.deleteStudent(s1.getId());
        studentDAO.deleteStudent(s2.getId());
    }

    @Test
    public void testExcelTemplateMassImport() throws Exception {
        // Generate template which contains 3 sample students
        byte[] templateBytes = importService.generateExcelTemplate();
        StudentImportService.ImportResult result = importService.importStudents(
                new ByteArrayInputStream(templateBytes), "template.xlsx");

        Assertions.assertTrue(result.getTotalRows() >= 3);
        Assertions.assertTrue(result.getImportedCount() + result.getUpdatedCount() >= 3);

        // Verify Vikram Rathore from template was imported
        Student s = studentDAO.findByRollNo("24EARAD035");
        Assertions.assertNotNull(s);
        Assertions.assertEquals("Vikram Rathore", s.getName());
    }
}
