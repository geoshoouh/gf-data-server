package com.gf.server.controller_tests;

import org.junit.jupiter.api.AfterEach;
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
import com.gf.server.entities.GF_Client;
import com.gf.server.services.GF_DataManagementService;
import com.google.gson.Gson;

import static org.mockito.Mockito.doReturn;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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

    @AfterEach
    void clearRepositories() {
        
        this.dataManagementService.clearClientRepository();
        this.dataManagementService.clearExerciseRecordRepository();
    }

    GF_Client clientCreationUtil() {
        
        String newClientEmail = RandomStringUtils.randomAlphanumeric(7) + "@" + RandomStringUtils.randomAlphabetic(4) + ".com";
        String newClientFirstName = RandomStringUtils.randomAlphabetic(7);
        String newClientLastName = RandomStringUtils.randomAlphabetic(7);

        return this.dataManagementService.createClient(newClientEmail, newClientLastName, newClientFirstName);
    }

    @Test
    void canPingServer() throws Exception {

        this.mockMvc.perform(get("/ping/data-server")).andExpect(status().isOk());
    }

    @Test
    void trainerCanCreateNewClient() throws Exception {

        String trainerToken = "Bearer eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJhZG1pbkBnbWFpbC5jb20iLCJpYXQiOjE3Mzg4MTE2NjMsImV4cCI6MTczODg5ODA2M30.ERUd3V_6up60Vw24LuExOeNeQZ1aXwhHYuBRdFnw_7g";
        
        doReturn(true).when(this.dataManagementController).validateToken(trainerToken);

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

        ReqResDTO response = gson.fromJson(this.mockMvc.perform(post("/trainer/new/client").header("Authorization", trainerToken)
                                                                    .contentType(MediaType.APPLICATION_JSON)
                                                                    .content(gson.toJson(request))).andExpect(status().isOk())
                                                                                                   .andReturn()
                                                                                                   .getResponse()
                                                                                                   .getContentAsString(), ReqResDTO.class);

        Assert.isTrue(response.client().getEmail().equals(email), "Expected " + email + "; was " + response.client().getEmail());
        Assert.isTrue(response.client().getLastName().equals(lastName), "Expected " + lastName + "; was " + response.client().getLastName());
        Assert.isTrue(response.client().getFirstName().equals(firstName), "Expected " + firstName + "; was " + response.client().getFirstName());
    }
}
