package com.gf.server.controllers;

import org.springframework.web.bind.annotation.RestController;

import com.gf.server.dto.ReqResDTO;
import com.gf.server.entities.GF_Client;
import com.gf.server.services.GF_DataManagementService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;


@RestController
public class GF_DataManagementController {

    @Autowired
    GF_DataManagementService dataManagementService;

    @GetMapping("/ping/data-server")
    public ResponseEntity<String> getMethodName() {
        return  ResponseEntity.ok("Genesis Personal Fitness Data Server is HEALTHY.\n");
    }

    @PostMapping("/trainer/new/user")
    public ResponseEntity<ReqResDTO> createClient(@RequestBody ReqResDTO request) {
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
