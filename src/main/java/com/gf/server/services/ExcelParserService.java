package com.gf.server.services;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import com.gf.server.entities.ExerciseRecord;
import com.gf.server.entities.GF_Client;
import com.gf.server.enumerations.EquipmentEnum;
import com.gf.server.enumerations.ExerciseEnum;

@Service
public class ExcelParserService {

    public List<ExerciseRecord> parseExerciseRecords(InputStream inputStream) throws Exception {
        List<ExerciseRecord> records = new ArrayList<>();
        
        try (Workbook workbook = WorkbookFactory.create(inputStream)) {
            Sheet sheet = workbook.getSheetAt(0);
            
            // Skip header row (row 0)
            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;
                
                try {
                    ExerciseRecord record = parseRow(row);
                    if (record != null) {
                        records.add(record);
                    }
                } catch (Exception e) {
                    // Skip invalid rows but continue processing
                    continue;
                }
            }
        }
        
        return records;
    }

    private ExerciseRecord parseRow(Row row) throws Exception {
        ExerciseRecord record = new ExerciseRecord();
        
        // Expected columns:
        // 0: Client Email
        // 1: Equipment Type
        // 2: Exercise Type
        // 3: Resistance
        // 4: Seat Setting
        // 5: Pad Setting
        // 6: Right Arm
        // 7: Left Arm
        // 8: Date Time (optional)
        
        String email = getCellValueAsString(row.getCell(0));
        if (email == null || email.trim().isEmpty()) {
            return null;
        }
        
        // Skip example rows - check if email contains "example" (case insensitive)
        String emailLower = email.trim().toLowerCase();
        if (emailLower.contains("example") || emailLower.contains("sample") || 
            emailLower.equals("client@example.com") || emailLower.equals("example@example.com")) {
            return null; // Skip example rows
        }
        
        GF_Client client = new GF_Client();
        client.setEmail(email.trim());
        record.setClient(client);
        
        // Equipment Type
        String equipmentStr = getCellValueAsString(row.getCell(1));
        if (equipmentStr != null) {
            try {
                record.setEquipmentType(EquipmentEnum.valueOf(equipmentStr.trim().toUpperCase()));
            } catch (IllegalArgumentException e) {
                throw new Exception("Invalid equipment type: " + equipmentStr);
            }
        }
        
        // Exercise Type
        String exerciseStr = getCellValueAsString(row.getCell(2));
        if (exerciseStr != null) {
            try {
                record.setExercise(ExerciseEnum.valueOf(exerciseStr.trim().toUpperCase()));
            } catch (IllegalArgumentException e) {
                throw new Exception("Invalid exercise type: " + exerciseStr);
            }
        }
        
        // Resistance
        record.setResistance(getCellValueAsInt(row.getCell(3), 0));
        
        // Seat Setting
        record.setSeatSetting(getCellValueAsInt(row.getCell(4), 0));
        
        // Pad Setting
        record.setPadSetting(getCellValueAsInt(row.getCell(5), 0));
        
        // Right Arm
        record.setRightArm(getCellValueAsInt(row.getCell(6), 0));
        
        // Left Arm
        record.setLeftArm(getCellValueAsInt(row.getCell(7), 0));
        
        // Date Time (optional)
        Date dateTime = getCellValueAsDate(row.getCell(8));
        if (dateTime != null) {
            record.setDateTime(dateTime);
        }
        
        return record;
    }

    private String getCellValueAsString(Cell cell) {
        if (cell == null) return null;
        
        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue();
            case NUMERIC:
                return String.valueOf((long) cell.getNumericCellValue());
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            default:
                return null;
        }
    }

    private int getCellValueAsInt(Cell cell, int defaultValue) {
        if (cell == null) return defaultValue;
        
        switch (cell.getCellType()) {
            case NUMERIC:
                return (int) cell.getNumericCellValue();
            case STRING:
                try {
                    return Integer.parseInt(cell.getStringCellValue().trim());
                } catch (NumberFormatException e) {
                    return defaultValue;
                }
            default:
                return defaultValue;
        }
    }

    private Date getCellValueAsDate(Cell cell) {
        if (cell == null) return null;
        
        switch (cell.getCellType()) {
            case NUMERIC:
                if (org.apache.poi.ss.usermodel.DateUtil.isCellDateFormatted(cell)) {
                    return cell.getDateCellValue();
                }
                return null;
            case STRING:
                try {
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    return sdf.parse(cell.getStringCellValue());
                } catch (Exception e) {
                    try {
                        SimpleDateFormat sdf2 = new SimpleDateFormat("yyyy-MM-dd");
                        return sdf2.parse(cell.getStringCellValue());
                    } catch (Exception e2) {
                        return null;
                    }
                }
            default:
                return null;
        }
    }

    public byte[] generateTemplate() throws Exception {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Exercise Records");
            
            // Create header row
            Row headerRow = sheet.createRow(0);
            String[] headers = {
                "Client Email",
                "Equipment Type",
                "Exercise Type",
                "Resistance",
                "Seat Setting",
                "Pad Setting",
                "Right Arm",
                "Left Arm",
                "Date Time"
            };
            
            CellStyle headerStyle = workbook.createCellStyle();
            org.apache.poi.ss.usermodel.Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);
            
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }
            
            // Create example row
            Row exampleRow = sheet.createRow(1);
            exampleRow.createCell(0).setCellValue("client@example.com");
            exampleRow.createCell(1).setCellValue("NAUTILUS");
            exampleRow.createCell(2).setCellValue("BICEP_CURL");
            exampleRow.createCell(3).setCellValue(50);
            exampleRow.createCell(4).setCellValue(3);
            exampleRow.createCell(5).setCellValue(2);
            exampleRow.createCell(6).setCellValue(1);
            exampleRow.createCell(7).setCellValue(1);
            exampleRow.createCell(8).setCellValue("2025-01-15 10:30:00");
            
            // Style the example row in italics to indicate it's an example
            CellStyle exampleStyle = workbook.createCellStyle();
            org.apache.poi.ss.usermodel.Font exampleFont = workbook.createFont();
            exampleFont.setItalic(true);
            exampleStyle.setFont(exampleFont);
            for (int i = 0; i < headers.length; i++) {
                exampleRow.getCell(i).setCellStyle(exampleStyle);
            }
            
            // Auto-size columns
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }
            
            // Convert to byte array
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            workbook.write(outputStream);
            return outputStream.toByteArray();
        }
    }
}
