package com.gf.server.controllers;

import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.HttpClientErrorException.Unauthorized;

import com.gf.server.dto.ReqResDTO;
import com.gf.server.dto.ListResponseDTO;
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

import java.util.List;

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

    @GetMapping("/ping-data-server")
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

        GF_Client existingClient = this.dataManagementService.getClientByEmail(request.exerciseRecord().getClient().getEmail());
        
        ExerciseRecord exerciseRecord = request.exerciseRecord();
        exerciseRecord.setClient(existingClient);
        
        ExerciseRecord savedRecord = this.dataManagementService.createExerciseRecord(exerciseRecord);
        
        ReqResDTO response = new ReqResDTO(
            "Successfully created exercise record with ID " + savedRecord.getId().toString(),
            null, 
            null, 
            null, 
            null, 
            savedRecord);

        return ResponseEntity.ok(response);
    }

    @PostMapping("/trainer/get/record/latest")
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

    @GetMapping("/trainer/get/clients")
    public ResponseEntity<ListResponseDTO> getAllClients(@RequestHeader(HttpHeaders.AUTHORIZATION) String token) throws Unauthorized {
        
        this.validateToken(token);

        List<GF_Client> clients = this.dataManagementService.getAllClients();

        ListResponseDTO response = new ListResponseDTO(
            "Successfully retrieved " + clients.size() + " clients",
            clients,
            null,
            null);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/trainer/get/equipment-types")
    public ResponseEntity<ListResponseDTO> getAllEquipmentTypes(@RequestHeader(HttpHeaders.AUTHORIZATION) String token) throws Unauthorized {
        
        this.validateToken(token);

        var equipmentTypes = this.dataManagementService.getAllEquipmentTypes();

        ListResponseDTO response = new ListResponseDTO(
            "Successfully retrieved " + equipmentTypes.length + " equipment types",
            null,
            equipmentTypes,
            null);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/trainer/get/exercise-types")
    public ResponseEntity<ListResponseDTO> getAllExerciseTypes(@RequestHeader(HttpHeaders.AUTHORIZATION) String token) throws Unauthorized {
        
        this.validateToken(token);

        var exerciseTypes = this.dataManagementService.getAllExerciseTypes();

        ListResponseDTO response = new ListResponseDTO(
            "Successfully retrieved " + exerciseTypes.length + " exercise types",
            null,
            null,
            exerciseTypes);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/trainer/get/all")
    public ResponseEntity<ListResponseDTO> getAllData(@RequestHeader(HttpHeaders.AUTHORIZATION) String token) throws Unauthorized {
        
        this.validateToken(token);

        List<GF_Client> clients = this.dataManagementService.getAllClients();
        var equipmentTypes = this.dataManagementService.getAllEquipmentTypes();
        var exerciseTypes = this.dataManagementService.getAllExerciseTypes();

        ListResponseDTO response = new ListResponseDTO(
            "Successfully retrieved " + clients.size() + " clients, " + equipmentTypes.length + " equipment types, and " + exerciseTypes.length + " exercise types",
            clients,
            equipmentTypes,
            exerciseTypes);

        return ResponseEntity.ok(response);
    }
    
}
