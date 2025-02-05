package com.gf.server.controllers;

import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestClient;

import com.gf.server.dto.ReqResDTO;
import com.gf.server.entities.GF_Client;
import com.gf.server.services.GF_DataManagementService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;


@RestController
public class GF_DataManagementController {

    @Autowired
    GF_DataManagementService dataManagementService;


    private boolean validateToken(String token) {

        boolean retVal = false;

        RestClient restClient = RestClient.create();

        Boolean result = restClient.get()
                                   .uri("10.97.207.231:8080/auth/token/validate/trainer")
                                   .header("Authorization", "Bearer " + token)
                                   .retrieve()
                                   .body(Boolean.class);
        
        if (result != null) {
            retVal = result.booleanValue();
        }

        return retVal;
    }

    @GetMapping("/ping/data-server")
    public ResponseEntity<String> getMethodName() {
        return  ResponseEntity.ok("Genesis Personal Fitness Data Server is HEALTHY.\n");
    }

    @PostMapping("/trainer/new/user")
    public ResponseEntity<ReqResDTO> createClient(@RequestHeader(HttpHeaders.AUTHORIZATION) @RequestBody ReqResDTO request) {
        GF_Client client = this.dataManagementService.createClient(request.client().getEmail(), request.client().getLastName(), request.client().getFirstName());

        ReqResDTO response = new ReqResDTO(
            "Successfully created client with ID " + client.getId(),
            null, 
            null, 
            null, 
            client, 
            null);
        
        return ResponseEntity.ok(response);
    }
    
}
