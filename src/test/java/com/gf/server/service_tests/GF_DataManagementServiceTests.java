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
        
        String newClientEmail = email;
        String newClientFirstName = RandomStringUtils.randomAlphabetic(7);
        String newClientLastName = RandomStringUtils.randomAlphabetic(7);

        return this.dataManagementService.createClient(newClientEmail, newClientLastName, newClientFirstName);
    }

    @AfterEach
    void clearRepositories() {
        
        this.dataManagementService.clearClientRepository();
        this.dataManagementService.clearExerciseRecordRepository();
    }

    @Test
    void dataManagementCanCreateNewClients() {

        String newClientEmail = RandomStringUtils.randomAlphanumeric(7) + "@" + RandomStringUtils.randomAlphabetic(4) + ".com";
        String newClientFirstName = RandomStringUtils.randomAlphabetic(7);
        String newClientLastName = RandomStringUtils.randomAlphabetic(7);

        GF_Client createdClient = this.dataManagementService.createClient(newClientEmail, newClientLastName, newClientFirstName);

        Assert.isTrue(this.dataManagementService.getClientCount() == 1L, "Expect client count 1; was " + this.dataManagementService.getClientCount());
        Assert.isTrue(createdClient.getEmail() == newClientEmail, "Expected created client value " + newClientEmail + ", was " + createdClient.getEmail());
        Assert.isTrue(createdClient.getFirstName() == newClientFirstName, "Expected created client value " + newClientFirstName + ", was " + createdClient.getFirstName());
        Assert.isTrue(createdClient.getLastName() == newClientLastName, "Expected created client value " + newClientLastName + ", was " + createdClient.getLastName());
    }

    @Test
    void getClientByEmailGetsClientByEmail() {

        String email = RandomStringUtils.randomAlphanumeric(7) + "@" + RandomStringUtils.randomAlphabetic(4) + ".com";

        this.clientCreationUtil(email);

        GF_Client foundClient = this.dataManagementService.getClientByEmail(email);

        Assert.isTrue(foundClient.getEmail() == email, "Expect email " + email + "; was " + foundClient.getEmail());
    }
}
