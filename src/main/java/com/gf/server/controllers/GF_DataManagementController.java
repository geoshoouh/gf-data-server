package com.gf.server.controllers;

import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.HttpClientErrorException.Unauthorized;

import com.gf.server.dto.ReqResDTO;
import com.gf.server.entities.ExerciseRecord;
import com.gf.server.entities.GF_Client;
import com.gf.server.services.GF_DataManagementService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

@RestController
public class GF_DataManagementController {

    @Autowired
    GF_DataManagementService dataManagementService;


    @SuppressWarnings("null")
    public boolean validateToken(String token) throws Unauthorized {

        boolean retVal = false;

        RestClient restClient = RestClient.create();

        ResponseEntity<Boolean> result = restClient.get()
                                                  .uri("https://app.gfproto.xyz/auth/token/validate/trainer")
                                                  .header("Authorization", token)
                                                  .retrieve()
                                                  .toEntity(Boolean.class);
        
        if (result != null && result.getStatusCode() == HttpStatus.OK) {
            retVal = true;
        } else {
            throw Unauthorized.create(null, HttpStatus.UNAUTHORIZED,null, null, null, null);
        }
        
        return retVal;
    }

    @GetMapping("/ping/data-server")
    public ResponseEntity<String> getMethodName() {
        return  ResponseEntity.ok("Genesis Personal Fitness Data Server is HEALTHY.\n");
    }

    @PostMapping("/trainer/new/client")
    public ResponseEntity<ReqResDTO> createClient(@RequestHeader(HttpHeaders.AUTHORIZATION) String token, @RequestBody ReqResDTO request) throws Unauthorized {

        this.validateToken(token);

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
    
    @PostMapping("/trainer/new/record")
    public ResponseEntity<ReqResDTO> createExerciseRecord(@RequestHeader(HttpHeaders.AUTHORIZATION) String token, @RequestBody ReqResDTO request) throws Unauthorized {

        this.validateToken(token);

        ExerciseRecord exerciseRecord = this.dataManagementService.createExerciseRecord(request.exerciseRecord());
        
        ReqResDTO response = new ReqResDTO(
            "Successfully created exercise record with ID " + exerciseRecord.getId().toString(),
            null, 
            null, 
            null, 
            null, 
            exerciseRecord);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/trainer/get/record/latest")
    public ResponseEntity<ReqResDTO> getLatestExerciseRecord(@RequestHeader(HttpHeaders.AUTHORIZATION) String token, @RequestBody ReqResDTO request) throws Unauthorized {
        
        this.validateToken(token);

        ExerciseRecord latestRecord = this.dataManagementService.getLatestExerciseRecord(request.client().getEmail(), request.equipmentType(), request.exerciseType());

        ReqResDTO response = new ReqResDTO(
            "Successfully retrieved latest exercise for client " + request.client().getLastName() + " on " + request.equipmentType().toString() + " doing " + request.exerciseType().toString(),
            null, 
            null, 
            null, 
            null, 
            latestRecord);

        return ResponseEntity.ok(response);
    }
    
}
