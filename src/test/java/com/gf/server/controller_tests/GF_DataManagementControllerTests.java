package com.gf.server.controller_tests;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class GF_DataManagementControllerTests {

    @Autowired
    MockMvc mockMvc;

    @Test
    void canPingServer() throws Exception {

        this.mockMvc.perform(get("/ping/data-server")).andExpect(status().isOk());
    }
}
