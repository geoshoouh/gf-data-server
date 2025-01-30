package com.gf.server.service_tests;

import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.util.Assert;

import com.gf.server.dto.ReqResDTO;
import com.gf.server.entities.GF_Client;
import com.gf.server.services.GF_DataManagementService;

@SpringBootTest
public class GF_DataManagementServiceTests {
    
    @Autowired
    GF_DataManagementService dataManagementService;

    GF_Client clientCreationUtil(String email) {

        GF_Client newClient = new GF_Client();

        newClient.setEmail(email);

        ReqResDTO request = new ReqResDTO(
            null, 
            null, 
            null, 
            null, 
            newClient, 
            null
        );

        return this.dataManagementService.createClient(request).client();
    }

    @AfterEach
    void clearRepositories() {
        
        this.dataManagementService.clearClientRepository();
        this.dataManagementService.clearExerciseRecordRepository();
    }

    @Test
    void dataManagementCanCreateNewClients() {

        GF_Client newClient = new GF_Client();

        newClient.setEmail(RandomStringUtils.randomAlphanumeric(7) + "@" + RandomStringUtils.randomAlphabetic(4) + ".com");
        newClient.setFirstName(RandomStringUtils.randomAlphabetic(7));
        newClient.setLastName(RandomStringUtils.randomAlphabetic(7));

        ReqResDTO request = new ReqResDTO(
            null, 
            null, 
            null, 
            null, 
            newClient, 
            null
        );

        GF_Client createdClient = this.dataManagementService.createClient(request).client();

        Assert.isTrue(this.dataManagementService.getClientCount() == 1L, "Expect client count 1; was " + this.dataManagementService.getClientCount());
        Assert.isTrue(createdClient.getEmail() == newClient.getEmail(), "Expected created client value " + newClient.getEmail() + ", was " + createdClient.getEmail());
        Assert.isTrue(createdClient.getFirstName() == newClient.getFirstName(), "Expected created client value " + newClient.getFirstName() + ", was " + createdClient.getFirstName());
        Assert.isTrue(createdClient.getLastName() == newClient.getLastName(), "Expected created client value " + newClient.getLastName() + ", was " + createdClient.getLastName());
    }

    @Test
    void getClientByEmailGetsClientByEmail() {

        String email = RandomStringUtils.randomAlphanumeric(7) + "@" + RandomStringUtils.randomAlphabetic(4) + ".com";

        GF_Client createdClient = this.clientCreationUtil(email);

        ReqResDTO request = new ReqResDTO(
            null, 
            null, 
            null, 
            null, 
            createdClient, 
            null
        );

        GF_Client foundClient = this.dataManagementService.getClientByEmail(request).client();

        Assert.isTrue(foundClient.getEmail() == email, "Expect email " + email + "; was " + foundClient.getEmail());
    }
}
