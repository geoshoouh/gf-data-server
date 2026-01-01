package com.gf.server.service_tests;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.List;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.util.Assert;

import com.gf.server.entities.ExerciseRecord;
import com.gf.server.enumerations.EquipmentEnum;
import com.gf.server.enumerations.ExerciseEnum;
import com.gf.server.services.ExcelParserService;

@SpringBootTest
public class ExcelParserServiceTests {

    @Autowired
    ExcelParserService excelParserService;

    @Test
    void canGenerateTemplate() throws Exception {
        byte[] template = excelParserService.generateTemplate();
        
        Assert.notNull(template, "Template should not be null");
        Assert.isTrue(template.length > 0, "Template should not be empty");
        
        // Verify it's a valid Excel file
        try (Workbook workbook = new XSSFWorkbook(new ByteArrayInputStream(template))) {
            Sheet sheet = workbook.getSheetAt(0);
            Assert.notNull(sheet, "Sheet should exist");
            
            // Check header row
            Row headerRow = sheet.getRow(0);
            Assert.notNull(headerRow, "Header row should exist");
            Assert.isTrue(headerRow.getCell(0).getStringCellValue().equals("Client Email"), "First header should be Client Email");
            
            // Check example row
            Row exampleRow = sheet.getRow(1);
            Assert.notNull(exampleRow, "Example row should exist");
            Assert.isTrue(exampleRow.getCell(0).getStringCellValue().contains("example"), "Example row should contain example email");
        }
    }

    @Test
    void canParseValidExcelFile() throws Exception {
        // Create a test Excel file
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Exercise Records");
            
            // Header row
            Row headerRow = sheet.createRow(0);
            headerRow.createCell(0).setCellValue("Client Email");
            headerRow.createCell(1).setCellValue("Equipment Type");
            headerRow.createCell(2).setCellValue("Exercise Type");
            headerRow.createCell(3).setCellValue("Resistance");
            headerRow.createCell(4).setCellValue("Seat Setting");
            headerRow.createCell(5).setCellValue("Pad Setting");
            headerRow.createCell(6).setCellValue("Right Arm");
            headerRow.createCell(7).setCellValue("Left Arm");
            headerRow.createCell(8).setCellValue("Date Time");
            
            // Data row
            Row dataRow = sheet.createRow(1);
            dataRow.createCell(0).setCellValue("test@example.com");
            dataRow.createCell(1).setCellValue("NAUTILUS");
            dataRow.createCell(2).setCellValue("BICEP_CURL");
            dataRow.createCell(3).setCellValue(50);
            dataRow.createCell(4).setCellValue(3);
            dataRow.createCell(5).setCellValue(2);
            dataRow.createCell(6).setCellValue(1);
            dataRow.createCell(7).setCellValue(1);
            
            // Convert to input stream
            java.io.ByteArrayOutputStream outputStream = new java.io.ByteArrayOutputStream();
            workbook.write(outputStream);
            InputStream inputStream = new ByteArrayInputStream(outputStream.toByteArray());
            
            // Parse
            List<ExerciseRecord> records = excelParserService.parseExerciseRecords(inputStream);
            
            // Should skip example row (contains "example" in email)
            Assert.isTrue(records.isEmpty(), "Should skip example rows");
        }
    }

    @Test
    void canParseExcelFileWithValidData() throws Exception {
        // Create a test Excel file
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Exercise Records");
            
            // Header row
            Row headerRow = sheet.createRow(0);
            headerRow.createCell(0).setCellValue("Client Email");
            headerRow.createCell(1).setCellValue("Equipment Type");
            headerRow.createCell(2).setCellValue("Exercise Type");
            headerRow.createCell(3).setCellValue("Resistance");
            headerRow.createCell(4).setCellValue("Seat Setting");
            headerRow.createCell(5).setCellValue("Pad Setting");
            headerRow.createCell(6).setCellValue("Right Arm");
            headerRow.createCell(7).setCellValue("Left Arm");
            
            // Data row with valid email (not example)
            Row dataRow = sheet.createRow(1);
            dataRow.createCell(0).setCellValue("testuser@gmail.com");
            dataRow.createCell(1).setCellValue("KINESIS");
            dataRow.createCell(2).setCellValue("LEG_PRESS");
            dataRow.createCell(3).setCellValue(75);
            dataRow.createCell(4).setCellValue(5);
            dataRow.createCell(5).setCellValue(4);
            dataRow.createCell(6).setCellValue(2);
            dataRow.createCell(7).setCellValue(2);
            
            // Convert to input stream
            java.io.ByteArrayOutputStream outputStream = new java.io.ByteArrayOutputStream();
            workbook.write(outputStream);
            InputStream inputStream = new ByteArrayInputStream(outputStream.toByteArray());
            
            // Parse
            List<ExerciseRecord> records = excelParserService.parseExerciseRecords(inputStream);
            
            Assert.isTrue(records.size() == 1, "Should parse 1 record");
            ExerciseRecord record = records.get(0);
            Assert.isTrue(record.getClient().getEmail().equals("testuser@gmail.com"), "Email should match");
            Assert.isTrue(record.getEquipmentType() == EquipmentEnum.KINESIS, "Equipment type should match");
            Assert.isTrue(record.getExercise() == ExerciseEnum.LEG_PRESS, "Exercise type should match");
            Assert.isTrue(record.getResistance() == 75, "Resistance should match");
            Assert.isTrue(record.getSeatSetting() == 5, "Seat setting should match");
            Assert.isTrue(record.getPadSetting() == 4, "Pad setting should match");
            Assert.isTrue(record.getRightArm() == 2, "Right arm should match");
            Assert.isTrue(record.getLeftArm() == 2, "Left arm should match");
        }
    }

    @Test
    void skipsExampleRows() throws Exception {
        // Create a test Excel file with example rows
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Exercise Records");
            
            // Header row
            Row headerRow = sheet.createRow(0);
            headerRow.createCell(0).setCellValue("Client Email");
            headerRow.createCell(1).setCellValue("Equipment Type");
            headerRow.createCell(2).setCellValue("Exercise Type");
            
            // Example row 1
            Row exampleRow1 = sheet.createRow(1);
            exampleRow1.createCell(0).setCellValue("client@example.com");
            exampleRow1.createCell(1).setCellValue("NAUTILUS");
            exampleRow1.createCell(2).setCellValue("BICEP_CURL");
            
            // Example row 2
            Row exampleRow2 = sheet.createRow(2);
            exampleRow2.createCell(0).setCellValue("sample@test.com");
            exampleRow2.createCell(1).setCellValue("KINESIS");
            exampleRow2.createCell(2).setCellValue("LEG_PRESS");
            
            // Valid row
            Row validRow = sheet.createRow(3);
            validRow.createCell(0).setCellValue("realuser@gmail.com");
            validRow.createCell(1).setCellValue("KEISER");
            validRow.createCell(2).setCellValue("SQUAT");
            
            // Convert to input stream
            java.io.ByteArrayOutputStream outputStream = new java.io.ByteArrayOutputStream();
            workbook.write(outputStream);
            InputStream inputStream = new ByteArrayInputStream(outputStream.toByteArray());
            
            // Parse
            List<ExerciseRecord> records = excelParserService.parseExerciseRecords(inputStream);
            
            // Should only parse the valid row, skip both example rows
            Assert.isTrue(records.size() == 1, "Should parse only 1 valid record");
            Assert.isTrue(records.get(0).getClient().getEmail().equals("realuser@gmail.com"), "Should parse valid email");
        }
    }

    @Test
    void handlesEmptyRows() throws Exception {
        // Create a test Excel file with empty rows
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Exercise Records");
            
            // Header row
            Row headerRow = sheet.createRow(0);
            headerRow.createCell(0).setCellValue("Client Email");
            
            // Empty row (row 1) - no cells set
            sheet.createRow(1);
            
            // Valid row
            Row validRow = sheet.createRow(2);
            validRow.createCell(0).setCellValue("test@gmail.com");
            validRow.createCell(1).setCellValue("NAUTILUS");
            validRow.createCell(2).setCellValue("BICEP_CURL");
            
            // Convert to input stream
            java.io.ByteArrayOutputStream outputStream = new java.io.ByteArrayOutputStream();
            workbook.write(outputStream);
            InputStream inputStream = new ByteArrayInputStream(outputStream.toByteArray());
            
            // Parse
            List<ExerciseRecord> records = excelParserService.parseExerciseRecords(inputStream);
            
            // Should skip empty row, parse valid row
            Assert.isTrue(records.size() == 1, "Should parse only 1 valid record");
        }
    }

    @Test
    void handlesDateParsing() throws Exception {
        // Create a test Excel file with date
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Exercise Records");
            
            // Header row
            Row headerRow = sheet.createRow(0);
            headerRow.createCell(0).setCellValue("Client Email");
            headerRow.createCell(8).setCellValue("Date Time");
            
            // Data row with date string
            Row dataRow = sheet.createRow(1);
            dataRow.createCell(0).setCellValue("test@gmail.com");
            dataRow.createCell(1).setCellValue("NAUTILUS");
            dataRow.createCell(2).setCellValue("BICEP_CURL");
            dataRow.createCell(8).setCellValue("2025-01-15 10:30:00");
            
            // Convert to input stream
            java.io.ByteArrayOutputStream outputStream = new java.io.ByteArrayOutputStream();
            workbook.write(outputStream);
            InputStream inputStream = new ByteArrayInputStream(outputStream.toByteArray());
            
            // Parse
            List<ExerciseRecord> records = excelParserService.parseExerciseRecords(inputStream);
            
            Assert.isTrue(records.size() == 1, "Should parse 1 record");
            ExerciseRecord record = records.get(0);
            Assert.notNull(record.getDateTime(), "Date time should be parsed");
        }
    }
}
