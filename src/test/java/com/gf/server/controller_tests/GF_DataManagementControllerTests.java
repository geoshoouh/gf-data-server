package com.gf.server.controller_tests;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.util.Assert;

import com.gf.server.controllers.GF_DataManagementController;
import com.gf.server.dto.ReqResDTO;
import com.gf.server.dto.ListResponseDTO;
import com.gf.server.dto.ExerciseHistoryResponseDTO;
import com.gf.server.dto.BulkUploadResponseDTO;
import com.gf.server.entities.ExerciseRecord;
import com.gf.server.entities.GF_Client;
import com.gf.server.enumerations.EquipmentEnum;
import com.gf.server.enumerations.ExerciseEnum;
import com.gf.server.services.GF_DataManagementService;
import com.gf.server.services.ExcelParserService;
import com.google.gson.Gson;

import org.springframework.mock.web.MockMultipartFile;

import static org.mockito.Mockito.doReturn;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;

import java.util.Random;
import java.util.Date;

import org.apache.commons.lang3.RandomStringUtils;

@SpringBootTest
@AutoConfigureMockMvc
public class GF_DataManagementControllerTests {

    @MockitoSpyBean
    GF_DataManagementController dataManagementController;

    @Autowired 
    GF_DataManagementService dataManagementService;

    @Autowired
    ExcelParserService excelParserService;

    @Autowired
    MockMvc mockMvc;

    @Autowired
    Gson gson;

    String jwt = "Bearer eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJhZG1pbkBnbWFpbC5jb20iLCJpYXQiOjE3Mzg4MTE2NjMsImV4cCI6MTczODg5ODA2M30.ERUd3V_6up60Vw24LuExOeNeQZ1aXwhHYuBRdFnw_7g";

    // Persists
    GF_Client clientCreationUtilPersistent() {
        
        String newClientEmail = RandomStringUtils.randomAlphanumeric(7) + "@" + RandomStringUtils.randomAlphabetic(4) + ".com";
        String newClientFirstName = RandomStringUtils.randomAlphabetic(7);
        String newClientLastName = RandomStringUtils.randomAlphabetic(7);

        return this.dataManagementService.createClient(newClientEmail, newClientLastName, newClientFirstName);
    }

    // Does not Persist
    ExerciseRecord exerciseRecordCreationUtil() {

        GF_Client client = clientCreationUtilPersistent();

        ExerciseRecord exerciseRecord = new ExerciseRecord();
        
        exerciseRecord.setClient(client);
        exerciseRecord.setEquipmentType(EquipmentEnum.values()[new Random().nextInt(EquipmentEnum.values().length)]);
        exerciseRecord.setExercise(ExerciseEnum.values()[new Random().nextInt(ExerciseEnum.values().length)]);

        return exerciseRecord;
    }

    ExerciseRecord exerciseRecordCreationUtilPersistent() {

        return this.dataManagementService.createExerciseRecord(this.exerciseRecordCreationUtil());
    }

    @BeforeEach
    void setup() {
        doReturn(true).when(this.dataManagementController).validateToken(this.jwt);
    }

    @AfterEach
    void teardown() {
        
        this.dataManagementService.clearExerciseRecordRepository();
        this.dataManagementService.clearClientRepository();
    }

    @Test
    void canPingServer() throws Exception {

        this.mockMvc.perform(get("/ping-data-server")).andExpect(status().isOk());
    }

    @Test
    void trainerCanCreateNewClient() throws Exception {
        
        GF_Client client = new GF_Client();
        
        String email = RandomStringUtils.randomAlphanumeric(7) + "@" + RandomStringUtils.randomAlphabetic(4) + ".com";
        String lastName = RandomStringUtils.randomAlphanumeric(7);
        String firstName = RandomStringUtils.randomAlphanumeric(7);

        client.setEmail(email);
        client.setLastName(lastName);
        client.setFirstName(firstName);

        ReqResDTO request = new ReqResDTO(
            null,
            null,
            null,
            null,
            client,
            null);

        ReqResDTO response = gson.fromJson(this.mockMvc.perform(post("/trainer/new/client").header("Authorization", this.jwt)
                                                                    .contentType(MediaType.APPLICATION_JSON)
                                                                    .content(gson.toJson(request))).andExpect(status().isOk())
                                                                                                   .andReturn()
                                                                                                   .getResponse()
                                                                                                   .getContentAsString(), ReqResDTO.class);

        Assert.isTrue(response.client().getEmail().equals(email), "Expected " + email + "; was " + response.client().getEmail());
        Assert.isTrue(response.client().getLastName().equals(lastName), "Expected " + lastName + "; was " + response.client().getLastName());
        Assert.isTrue(response.client().getFirstName().equals(firstName), "Expected " + firstName + "; was " + response.client().getFirstName());
    }

    @Test
    void trainerCanCreateExerciseRecord() throws Exception {

        ExerciseRecord exerciseRecord = this.exerciseRecordCreationUtil();

        ReqResDTO request = new ReqResDTO(
            null,
            null,
            null,
            null,
            null,
            exerciseRecord);

        ReqResDTO response = gson.fromJson(this.mockMvc.perform(post("/trainer/new/record").header("Authorization", this.jwt)
                                                            .contentType(MediaType.APPLICATION_JSON)
                                                            .content(gson.toJson(request))).andExpect(status().isOk())
                                                                                        .andReturn()
                                                                                        .getResponse()
                                                                                        .getContentAsString(), ReqResDTO.class);

        Assert.notNull(response.exerciseRecord().getId(), "Response exercise record ID was null");
        Assert.notNull(response.exerciseRecord().getDateTime(), "Response exercise record datatime was null");
        Assert.isTrue(response.exerciseRecord().getClient().getId() == exerciseRecord.getClient().getId(), "Client ID of persisted exercise record did not match that of original.");
    }

    @Test
    void getLatestExerciseRecordGetsLatestExerciseRecord() throws Exception {

        for (int i = 0; i < 4; i++) {
            this.exerciseRecordCreationUtilPersistent();
        }

        ExerciseRecord latestExerciseRecord = this.exerciseRecordCreationUtilPersistent();
        String latestExerciseRecordClientEmail = latestExerciseRecord.getClient().getEmail();

        GF_Client requestClient = new GF_Client();

        requestClient.setEmail(latestExerciseRecordClientEmail);

        ReqResDTO request = new ReqResDTO(
            null,
            null,
            latestExerciseRecord.getEquipmentType(),
            latestExerciseRecord.getExercise(),
            requestClient,
            null);

        ReqResDTO response = gson.fromJson(this.mockMvc.perform(post("/trainer/get/record/latest").header("Authorization", this.jwt)
                                                                                                             .contentType(MediaType.APPLICATION_JSON)
                                                                                                             .content(gson.toJson(request)))
                                                        .andExpect(status().isOk())
                                                        .andReturn()
                                                        .getResponse()
                                                        .getContentAsString(), ReqResDTO.class);

        Assert.isTrue(response.exerciseRecord().getId() == latestExerciseRecord.getId(), "Expected exercise ID " + latestExerciseRecord.getId() + "; was " + response.exerciseRecord().getId());
    }

    @Test
    void canGetAllClients() throws Exception {
        
        // Create some test clients
        this.clientCreationUtilPersistent();
        this.clientCreationUtilPersistent();
        this.clientCreationUtilPersistent();

        ListResponseDTO response = gson.fromJson(this.mockMvc.perform(get("/trainer/get/clients").header("Authorization", this.jwt))
                                                        .andExpect(status().isOk())
                                                        .andReturn()
                                                        .getResponse()
                                                        .getContentAsString(), ListResponseDTO.class);

        Assert.notNull(response.clients(), "Clients list should not be null");
        Assert.isTrue(response.clients().size() >= 3, "Should have at least 3 clients");
        Assert.isTrue(response.message().contains("3"), "Message should indicate 3 clients were retrieved");
    }

    @Test
    void canGetAllEquipmentTypes() throws Exception {

        ListResponseDTO response = gson.fromJson(this.mockMvc.perform(get("/trainer/get/equipment-types").header("Authorization", this.jwt))
                                                        .andExpect(status().isOk())
                                                        .andReturn()
                                                        .getResponse()
                                                        .getContentAsString(), ListResponseDTO.class);

        Assert.notNull(response.equipmentTypes(), "Equipment types should not be null");
        Assert.isTrue(response.equipmentTypes().length == EquipmentEnum.values().length, "Should return all equipment types");
        Assert.isTrue(response.message().contains(String.valueOf(EquipmentEnum.values().length)), "Message should indicate correct number of equipment types");
    }

    @Test
    void canGetAllExerciseTypes() throws Exception {

        ListResponseDTO response = gson.fromJson(this.mockMvc.perform(get("/trainer/get/exercise-types").header("Authorization", this.jwt))
                                                        .andExpect(status().isOk())
                                                        .andReturn()
                                                        .getResponse()
                                                        .getContentAsString(), ListResponseDTO.class);

        Assert.notNull(response.exerciseTypes(), "Exercise types should not be null");
        Assert.isTrue(response.exerciseTypes().length == ExerciseEnum.values().length, "Should return all exercise types");
        Assert.isTrue(response.message().contains(String.valueOf(ExerciseEnum.values().length)), "Message should indicate correct number of exercise types");
    }

    @Test
    void canGetAllData() throws Exception {
        
        // Create some test clients
        this.clientCreationUtilPersistent();
        this.clientCreationUtilPersistent();

        ListResponseDTO response = gson.fromJson(this.mockMvc.perform(get("/trainer/get/all").header("Authorization", this.jwt))
                                                        .andExpect(status().isOk())
                                                        .andReturn()
                                                        .getResponse()
                                                        .getContentAsString(), ListResponseDTO.class);

        Assert.notNull(response.clients(), "Clients list should not be null");
        Assert.notNull(response.equipmentTypes(), "Equipment types should not be null");
        Assert.notNull(response.exerciseTypes(), "Exercise types should not be null");
        Assert.isTrue(response.clients().size() >= 2, "Should have at least 2 clients");
        Assert.isTrue(response.equipmentTypes().length == EquipmentEnum.values().length, "Should return all equipment types");
        Assert.isTrue(response.exerciseTypes().length == ExerciseEnum.values().length, "Should return all exercise types");
        Assert.isTrue(response.message().contains("2"), "Message should indicate 2 clients were retrieved");
    }

    @Test
    void canGetExerciseHistory() throws Exception {
        
        // Create a test client first
        GF_Client client = this.clientCreationUtilPersistent();
        
        // Create some exercise records
        for (int i = 0; i < 3; i++) {
            ExerciseRecord record = this.exerciseRecordCreationUtil();
            record.setClient(client);
            record.setEquipmentType(EquipmentEnum.NAUTILUS);
            record.setExercise(ExerciseEnum.BICEP_CURL);
            this.dataManagementService.createExerciseRecord(record);
        }

        // Create a date from 1 day ago using a proper format
        long oneDayAgo = System.currentTimeMillis() - 24 * 60 * 60 * 1000;
        Date afterDate = new Date(oneDayAgo);

        // Create a fresh client object with all fields for the request
        GF_Client requestClient = new GF_Client();
        requestClient.setId(client.getId());
        requestClient.setEmail(client.getEmail());
        requestClient.setFirstName(client.getFirstName());
        requestClient.setLastName(client.getLastName());

        // Create the request manually to avoid date serialization issues
        String requestJson = String.format(
            "{\"client\":{\"id\":%d,\"email\":\"%s\",\"firstName\":\"%s\",\"lastName\":\"%s\"},\"equipmentType\":\"NAUTILUS\",\"exerciseType\":\"BICEP_CURL\",\"afterDate\":%d}",
            requestClient.getId(),
            requestClient.getEmail(),
            requestClient.getFirstName(),
            requestClient.getLastName(),
            afterDate.getTime()
        );

        ExerciseHistoryResponseDTO response = gson.fromJson(this.mockMvc.perform(post("/trainer/get/record/history").header("Authorization", this.jwt)
                                                            .contentType(MediaType.APPLICATION_JSON)
                                                            .content(requestJson)).andExpect(status().isOk())
                                                                                        .andReturn()
                                                                                        .getResponse()
                                                                                        .getContentAsString(), ExerciseHistoryResponseDTO.class);

        Assert.notNull(response.exerciseRecords(), "Exercise records list should not be null");
        Assert.isTrue(response.exerciseRecords().size() >= 3, "Should have at least 3 exercise records");
        Assert.isTrue(response.message().contains("3"), "Message should indicate 3 exercise records were retrieved");
    }

    @Test
    void canDownloadUploadTemplate() throws Exception {
        this.mockMvc.perform(get("/trainer/download/upload-template").header("Authorization", this.jwt))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_OCTET_STREAM))
                .andExpect(header().string("Content-Disposition", "form-data; name=\"attachment\"; filename=\"exercise_records_template.xlsx\""))
                .andReturn();
    }

    @Test
    void canBulkUploadExerciseRecords() throws Exception {
        // Create a client first
        GF_Client client = this.clientCreationUtilPersistent();
        
        // Generate a valid Excel file
        byte[] templateBytes = excelParserService.generateTemplate();
        
        // Modify the template to have valid data (replace example row)
        try (org.apache.poi.xssf.usermodel.XSSFWorkbook workbook = new org.apache.poi.xssf.usermodel.XSSFWorkbook(new java.io.ByteArrayInputStream(templateBytes))) {
            org.apache.poi.ss.usermodel.Sheet sheet = workbook.getSheetAt(0);
            
            // Replace example row with valid data
            org.apache.poi.ss.usermodel.Row dataRow = sheet.getRow(1);
            dataRow.getCell(0).setCellValue(client.getEmail());
            dataRow.getCell(1).setCellValue("NAUTILUS");
            dataRow.getCell(2).setCellValue("BICEP_CURL");
            dataRow.getCell(3).setCellValue(50);
            dataRow.getCell(4).setCellValue(3);
            dataRow.getCell(5).setCellValue(2);
            dataRow.getCell(6).setCellValue(1);
            dataRow.getCell(7).setCellValue(1);
            
            // Add another valid row
            org.apache.poi.ss.usermodel.Row dataRow2 = sheet.createRow(2);
            dataRow2.createCell(0).setCellValue(client.getEmail());
            dataRow2.createCell(1).setCellValue("KINESIS");
            dataRow2.createCell(2).setCellValue("LEG_PRESS");
            dataRow2.createCell(3).setCellValue(75);
            dataRow2.createCell(4).setCellValue(5);
            dataRow2.createCell(5).setCellValue(4);
            dataRow2.createCell(6).setCellValue(2);
            dataRow2.createCell(7).setCellValue(2);
            
            // Write to byte array
            java.io.ByteArrayOutputStream outputStream = new java.io.ByteArrayOutputStream();
            workbook.write(outputStream);
            templateBytes = outputStream.toByteArray();
        }
        
        MockMultipartFile file = new MockMultipartFile(
            "file",
            "test.xlsx",
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
            templateBytes
        );

        BulkUploadResponseDTO response = gson.fromJson(
            this.mockMvc.perform(multipart("/trainer/bulk/upload/records")
                    .file(file)
                    .header("Authorization", this.jwt))
                    .andExpect(status().isOk())
                    .andReturn()
                    .getResponse()
                    .getContentAsString(),
            BulkUploadResponseDTO.class
        );

        Assert.notNull(response, "Response should not be null");
        Assert.isTrue(response.totalRecords() >= 2, "Should process at least 2 records");
        Assert.isTrue(response.successfulRecords() >= 2, "Should successfully create at least 2 records");
        Assert.isTrue(response.failedRecords() == 0, "Should have no failed records");
    }

    @Test
    void bulkUploadHandlesInvalidFile() throws Exception {
        // Create invalid file content
        byte[] invalidContent = "This is not an Excel file".getBytes();
        
        MockMultipartFile file = new MockMultipartFile(
            "file",
            "invalid.txt",
            "text/plain",
            invalidContent
        );

        this.mockMvc.perform(multipart("/trainer/bulk/upload/records")
                .file(file)
                .header("Authorization", this.jwt))
                .andExpect(status().isBadRequest())
                .andReturn();
    }

    @Test
    void bulkUploadHandlesInvalidClientEmails() throws Exception {
        // Create a client (not used but ensures database is set up)
        this.clientCreationUtilPersistent();
        
        // Generate template
        byte[] templateBytes = excelParserService.generateTemplate();
        
        // Modify to have invalid client email
        try (org.apache.poi.xssf.usermodel.XSSFWorkbook workbook = new org.apache.poi.xssf.usermodel.XSSFWorkbook(new java.io.ByteArrayInputStream(templateBytes))) {
            org.apache.poi.ss.usermodel.Sheet sheet = workbook.getSheetAt(0);
            
            // Replace example row with invalid client email
            org.apache.poi.ss.usermodel.Row dataRow = sheet.getRow(1);
            dataRow.getCell(0).setCellValue("nonexistent@email.com");
            dataRow.getCell(1).setCellValue("NAUTILUS");
            dataRow.getCell(2).setCellValue("BICEP_CURL");
            
            // Write to byte array
            java.io.ByteArrayOutputStream outputStream = new java.io.ByteArrayOutputStream();
            workbook.write(outputStream);
            templateBytes = outputStream.toByteArray();
        }
        
        MockMultipartFile file = new MockMultipartFile(
            "file",
            "test.xlsx",
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
            templateBytes
        );

        BulkUploadResponseDTO response = gson.fromJson(
            this.mockMvc.perform(multipart("/trainer/bulk/upload/records")
                    .file(file)
                    .header("Authorization", this.jwt))
                    .andExpect(status().isOk())
                    .andReturn()
                    .getResponse()
                    .getContentAsString(),
            BulkUploadResponseDTO.class
        );

        Assert.notNull(response, "Response should not be null");
        Assert.isTrue(response.failedRecords() > 0, "Should have failed records");
        Assert.isTrue(response.errors().size() > 0, "Should have error messages");
        Assert.isTrue(response.errors().get(0).contains("not found"), "Error should mention client not found");
    }
}
