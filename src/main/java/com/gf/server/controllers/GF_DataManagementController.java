package com.gf.server.controllers;

import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.HttpClientErrorException.Unauthorized;

import com.gf.server.dto.ReqResDTO;
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
    private boolean validateToken(String token) throws Unauthorized {

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
    public ResponseEntity<ReqResDTO> createClient(@RequestHeader(HttpHeaders.AUTHORIZATION) String token, @RequestBody ReqResDTO request) {

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
    
}
