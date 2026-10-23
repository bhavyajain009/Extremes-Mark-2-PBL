package com.extremis.service;

import com.extremis.dao.ClassDAO;
import com.extremis.dao.DBConnection;
import com.extremis.dao.StudentDAO;
import com.extremis.model.BatchEntity;
import com.extremis.model.ClassEntity;
import com.extremis.model.Student;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Service for generating student import templates and mass importing students from Excel (.xlsx) and CSV files.
 */
public class StudentImportService {

    private final StudentDAO studentDAO = new StudentDAO();
    private final ClassDAO classDAO = new ClassDAO();

    public static class ImportResult {
        private int totalRows = 0;
        private int importedCount = 0;
        private int updatedCount = 0;
        private int failedCount = 0;
        private final List<String> errors = new ArrayList<>();

        public int getTotalRows() { return totalRows; }
        public void setTotalRows(int totalRows) { this.totalRows = totalRows; }

        public int getImportedCount() { return importedCount; }
        public void incrementImported() { this.importedCount++; }

        public int getUpdatedCount() { return updatedCount; }
        public void incrementUpdated() { this.updatedCount++; }

        public int getFailedCount() { return failedCount; }
        public void incrementFailed() { this.failedCount++; }

        public List<String> getErrors() { return errors; }
        public void addError(String error) { this.errors.add(error); }

        public boolean isSuccess() {
            return failedCount == 0 && (importedCount > 0 || updatedCount > 0);
        }
    }

    /**
     * Generates a beautifully formatted Excel template (.xlsx) with sample data and instructions.
     */
    public byte[] generateExcelTemplate() throws IOException {
        try (Workbook wb = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = wb.createSheet("Student_Import_Template");

            // Header styling: Navy fill, Bold White Text, Thin border
            CellStyle headerStyle = wb.createCellStyle();
            Font headerFont = wb.createFont();
            headerFont.setBold(true);
            headerFont.setColor(IndexedColors.WHITE.getIndex());
            headerFont.setFontHeightInPoints((short) 11);
            headerStyle.setFont(headerFont);
            headerStyle.setFillForegroundColor(IndexedColors.DARK_BLUE.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

            // Sample rows styling: soft alternating background
            CellStyle sampleStyle = wb.createCellStyle();
            Font sampleFont = wb.createFont();
            sampleFont.setColor(IndexedColors.GREY_50_PERCENT.getIndex());
            sampleStyle.setFont(sampleFont);

            String[] headers = {
                    "Roll Number*",
                    "Full Name*",
                    "Email Address*",
                    "Class Code*",
                    "Batch Code*",
                    "Password (Optional)"
            };

            Row headerRow = sheet.createRow(0);
            headerRow.setHeightInPoints(25);
            for (int i = 0; i < headers.length; i++) {
                Cell c = headerRow.createCell(i);
                c.setCellValue(headers[i]);
                c.setCellStyle(headerStyle);
            }

            // Seed 3 realistic example rows
            String[][] sampleData = {
                    {"24EARAD035", "Vikram Rathore", "vikram.24earad035@aryacollege.edu.in", "AI-A", "A1", "StudentPassword123!"},
                    {"24EARAD036", "Pooja Choudhary", "pooja.24earad036@aryacollege.edu.in", "AI-A", "A2", "StudentPassword123!"},
                    {"24ECS010", "Harsh Vardhan", "harsh.24ecs010@aryacollege.edu.in", "CS-A", "C1", "StudentPassword123!"}
            };

            for (int r = 0; r < sampleData.length; r++) {
                Row row = sheet.createRow(r + 1);
                row.setHeightInPoints(20);
                for (int c = 0; c < sampleData[r].length; c++) {
                    Cell cell = row.createCell(c);
                    cell.setCellValue(sampleData[r][c]);
                }
            }

            // Auto-size all columns
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
                sheet.setColumnWidth(i, Math.max(sheet.getColumnWidth(i) + 1200, 4500));
            }

            wb.write(out);
            return out.toByteArray();
        }
    }

    /**
     * Generates a standard CSV template.
     */
    public byte[] generateCsvTemplate() {
        StringBuilder sb = new StringBuilder();
        sb.append("Roll Number,Full Name,Email Address,Class Code,Batch Code,Password\n");
        sb.append("24EARAD035,Vikram Rathore,vikram.24earad035@aryacollege.edu.in,AI-A,A1,StudentPassword123!\n");
        sb.append("24EARAD036,Pooja Choudhary,pooja.24earad036@aryacollege.edu.in,AI-A,A2,StudentPassword123!\n");
        sb.append("24ECS010,Harsh Vardhan,harsh.24ecs010@aryacollege.edu.in,CS-A,C1,StudentPassword123!\n");
        return sb.toString().getBytes(StandardCharsets.UTF_8);
    }

    /**
     * Processes mass student import from either Excel (.xlsx/.xls) or CSV.
     */
    public ImportResult importStudents(InputStream inputStream, String fileName) {
        ImportResult result = new ImportResult();
        if (fileName == null || inputStream == null) {
            result.addError("No file was provided for import.");
            result.incrementFailed();
            return result;
        }

        String lower = fileName.toLowerCase();
        if (lower.endsWith(".csv")) {
            return importFromCsv(inputStream, result);
        } else if (lower.endsWith(".xlsx") || lower.endsWith(".xls")) {
            return importFromExcel(inputStream, result);
        } else {
            result.addError("Unsupported file format. Please upload an Excel (.xlsx, .xls) or CSV file.");
            result.incrementFailed();
            return result;
        }
    }

    private ImportResult importFromExcel(InputStream is, ImportResult result) {
        try (Workbook wb = WorkbookFactory.create(is)) {
            Sheet sheet = wb.getSheetAt(0);
            if (sheet == null) {
                result.addError("The uploaded Excel workbook contains no sheets.");
                result.incrementFailed();
                return result;
            }

            int lastRowNum = sheet.getLastRowNum();
            if (lastRowNum < 1) {
                result.addError("The uploaded Excel file does not contain any data rows.");
                result.incrementFailed();
                return result;
            }

            Row headerRow = sheet.getRow(0);
            if (headerRow == null) {
                result.addError("The Excel sheet is missing a header row.");
                result.incrementFailed();
                return result;
            }

            DataFormatter formatter = new DataFormatter();
            Map<String, Integer> colMap = buildColumnMapping(headerRow, formatter);

            if (!colMap.containsKey("roll") || !colMap.containsKey("name") || !colMap.containsKey("email")) {
                result.addError("Required column headers ('Roll Number', 'Full Name', 'Email Address') were not found in the Excel header row.");
                result.incrementFailed();
                return result;
            }

            for (int r = 1; r <= lastRowNum; r++) {
                Row row = sheet.getRow(r);
                if (row == null || isRowEmpty(row)) continue;

                result.setTotalRows(result.getTotalRows() + 1);
                String rollNo = getCellValue(row, colMap.get("roll"), formatter);
                String name = getCellValue(row, colMap.get("name"), formatter);
                String email = getCellValue(row, colMap.get("email"), formatter);
                String classCode = getCellValue(row, colMap.get("class"), formatter);
                String batchCode = getCellValue(row, colMap.get("batch"), formatter);
                String password = getCellValue(row, colMap.get("password"), formatter);

                processSingleStudent(r + 1, rollNo, name, email, classCode, batchCode, password, result);
            }

        } catch (Exception e) {
            result.addError("Error parsing Excel file: " + e.getMessage());
            result.incrementFailed();
        }
        return result;
    }

    private ImportResult importFromCsv(InputStream is, ImportResult result) {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
            String headerLine = reader.readLine();
            if (headerLine == null) {
                result.addError("The CSV file is empty.");
                result.incrementFailed();
                return result;
            }

            // Strip UTF-8 BOM if present
            if (headerLine.startsWith("\uFEFF")) {
                headerLine = headerLine.substring(1);
            }

            List<String> headers = parseCsvLine(headerLine);
            Map<String, Integer> colMap = new HashMap<>();
            for (int i = 0; i < headers.size(); i++) {
                String h = headers.get(i).trim().toLowerCase();
                if (h.contains("roll")) colMap.put("roll", i);
                else if (h.contains("name")) colMap.put("name", i);
                else if (h.contains("email") || h.contains("mail")) colMap.put("email", i);
                else if (h.contains("class") || h.contains("branch")) colMap.put("class", i);
                else if (h.contains("batch") || h.contains("sec")) colMap.put("batch", i);
                else if (h.contains("pass")) colMap.put("password", i);
            }

            if (!colMap.containsKey("roll") || !colMap.containsKey("name") || !colMap.containsKey("email")) {
                result.addError("Required CSV columns ('Roll Number', 'Full Name', 'Email Address') were not found in the header row.");
                result.incrementFailed();
                return result;
            }

            String line;
            int lineNum = 1;
            while ((line = reader.readLine()) != null) {
                lineNum++;
                line = line.trim();
                if (line.isEmpty()) continue;

                result.setTotalRows(result.getTotalRows() + 1);
                List<String> values = parseCsvLine(line);

                String rollNo = getCsvVal(values, colMap.get("roll"));
                String name = getCsvVal(values, colMap.get("name"));
                String email = getCsvVal(values, colMap.get("email"));
                String classCode = getCsvVal(values, colMap.get("class"));
                String batchCode = getCsvVal(values, colMap.get("batch"));
                String password = getCsvVal(values, colMap.get("password"));

                processSingleStudent(lineNum, rollNo, name, email, classCode, batchCode, password, result);
            }

        } catch (Exception e) {
            result.addError("Error parsing CSV file: " + e.getMessage());
            result.incrementFailed();
        }
        return result;
    }

    private void processSingleStudent(int rowNum, String rollNo, String name, String email,
                                      String classCode, String batchCode, String password, ImportResult result) {
        if (rollNo == null || rollNo.trim().isEmpty()) {
            result.addError("Row " + rowNum + ": Skipped because Roll Number is empty.");
            result.incrementFailed();
            return;
        }
        rollNo = rollNo.trim().toUpperCase();

        if (name == null || name.trim().isEmpty()) {
            result.addError("Row " + rowNum + " (" + rollNo + "): Skipped because Student Name is empty.");
            result.incrementFailed();
            return;
        }
        name = name.trim();

        if (email == null || email.trim().isEmpty()) {
            email = rollNo.toLowerCase() + "@aryacollege.edu.in";
        } else {
            email = email.trim();
        }

        Long classId = null;
        String branch = "AI";
        String section = "Section A";
        if (classCode != null && !classCode.trim().isEmpty()) {
            ClassEntity ce = classDAO.getClassByCode(classCode.trim());
            if (ce != null) {
                classId = ce.getId();
                branch = ce.getDepartment();
                section = ce.getName();
            } else {
                result.addError("Row " + rowNum + " (" + rollNo + "): Warning: Class code '" + classCode.trim() + "' not found. Assigned default class.");
            }
        }

        Long batchId = null;
        if (classId != null && batchCode != null && !batchCode.trim().isEmpty()) {
            BatchEntity be = classDAO.getBatchByClassAndCode(classId, batchCode.trim());
            if (be != null) {
                batchId = be.getId();
            } else {
                List<BatchEntity> batches = classDAO.getBatchesByClassId(classId);
                if (!batches.isEmpty()) batchId = batches.get(0).getId();
            }
        } else if (classId != null) {
            List<BatchEntity> batches = classDAO.getBatchesByClassId(classId);
            if (!batches.isEmpty()) batchId = batches.get(0).getId();
        }

        String passHash = (password != null && !password.trim().isEmpty()) ?
                DBConnection.hashPassword(password.trim()) :
                DBConnection.hashPassword("StudentPassword123!");

        Student existing = studentDAO.findByRollNo(rollNo);
        if (existing != null) {
            existing.setName(name);
            existing.setEmail(email);
            if (classId != null) existing.setClassId(classId);
            if (batchId != null) existing.setBatchId(batchId);
            existing.setBranch(branch);
            existing.setSection(section);
            if (password != null && !password.trim().isEmpty()) {
                existing.setPasswordHash(passHash);
            }

            boolean ok = studentDAO.updateStudent(existing);
            if (ok) {
                result.incrementUpdated();
            } else {
                result.addError("Row " + rowNum + " (" + rollNo + "): Failed to update existing student record in database.");
                result.incrementFailed();
            }
        } else {
            Student s = new Student();
            s.setRollNo(rollNo);
            s.setName(name);
            s.setEmail(email);
            s.setBranch(branch);
            s.setSection(section);
            s.setClassId(classId);
            s.setBatchId(batchId);
            s.setPasswordHash(passHash);
            s.setPermissionsAccepted(true);

            boolean ok = studentDAO.insertStudent(s);
            if (ok) {
                result.incrementImported();
            } else {
                result.addError("Row " + rowNum + " (" + rollNo + "): Database insertion failed.");
                result.incrementFailed();
            }
        }
    }

    private Map<String, Integer> buildColumnMapping(Row headerRow, DataFormatter formatter) {
        Map<String, Integer> map = new HashMap<>();
        for (int c = 0; c < headerRow.getLastCellNum(); c++) {
            Cell cell = headerRow.getCell(c);
            if (cell == null) continue;
            String text = formatter.formatCellValue(cell).trim().toLowerCase();
            if (text.contains("roll")) map.put("roll", c);
            else if (text.contains("name")) map.put("name", c);
            else if (text.contains("email") || text.contains("mail")) map.put("email", c);
            else if (text.contains("class") || text.contains("branch")) map.put("class", c);
            else if (text.contains("batch") || text.contains("sec")) map.put("batch", c);
            else if (text.contains("pass")) map.put("password", c);
        }
        return map;
    }

    private String getCellValue(Row row, Integer colIdx, DataFormatter formatter) {
        if (colIdx == null) return null;
        Cell cell = row.getCell(colIdx);
        if (cell == null) return null;
        String val = formatter.formatCellValue(cell);
        return (val != null) ? val.trim() : null;
    }

    private boolean isRowEmpty(Row row) {
        if (row == null) return true;
        for (int c = row.getFirstCellNum(); c < row.getLastCellNum(); c++) {
            Cell cell = row.getCell(c);
            if (cell != null && cell.getCellType() != CellType.BLANK) {
                String val = cell.toString().trim();
                if (!val.isEmpty()) return false;
            }
        }
        return true;
    }

    private List<String> parseCsvLine(String line) {
        List<String> list = new ArrayList<>();
        StringBuilder cur = new StringBuilder();
        boolean inQuotes = false;
        for (int i = 0; i < line.length(); i++) {
            char ch = line.charAt(i);
            if (ch == '\"') {
                inQuotes = !inQuotes;
            } else if (ch == ',' && !inQuotes) {
                list.add(cur.toString().trim());
                cur.setLength(0);
            } else {
                cur.append(ch);
            }
        }
        list.add(cur.toString().trim());
        return list;
    }

    private String getCsvVal(List<String> values, Integer colIdx) {
        if (colIdx == null || colIdx < 0 || colIdx >= values.size()) return null;
        String v = values.get(colIdx);
        if (v == null) return null;
        v = v.trim();
        if (v.startsWith("\"") && v.endsWith("\"") && v.length() >= 2) {
            v = v.substring(1, v.length() - 1).trim();
        }
        return v;
    }
}
