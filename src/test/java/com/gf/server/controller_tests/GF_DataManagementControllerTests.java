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
import com.gf.server.entities.ExerciseRecord;
import com.gf.server.entities.GF_Client;
import com.gf.server.enumerations.EquipmentEnum;
import com.gf.server.enumerations.ExerciseEnum;
import com.gf.server.services.GF_DataManagementService;
import com.google.gson.Gson;

import static org.mockito.Mockito.doReturn;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Random;
import java.util.List;

import org.apache.commons.lang3.RandomStringUtils;

@SpringBootTest
@AutoConfigureMockMvc
public class GF_DataManagementControllerTests {

    @MockitoSpyBean
    GF_DataManagementController dataManagementController;

    @Autowired 
    GF_DataManagementService dataManagementService;

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
        GF_Client client1 = this.clientCreationUtilPersistent();
        GF_Client client2 = this.clientCreationUtilPersistent();
        GF_Client client3 = this.clientCreationUtilPersistent();

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
        GF_Client client1 = this.clientCreationUtilPersistent();
        GF_Client client2 = this.clientCreationUtilPersistent();

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
}
