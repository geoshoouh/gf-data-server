package com.gf.server.service_tests;

import org.junit.jupiter.api.AfterEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.gf.server.services.GF_DataManagementService;

@SpringBootTest
public class GF_DataManagementServiceTests {
    
    @Autowired
    GF_DataManagementService dataManagementService;

    @AfterEach
    void clearRepositories() {
        
        this.dataManagementService.clearClientRepository();
        this.dataManagementService.clearExerciseRecordRepository();
    }
}
