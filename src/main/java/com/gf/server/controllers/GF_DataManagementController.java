package com.gf.server.controllers;

import org.springframework.web.bind.annotation.RestController;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;

@RestController
public class GF_DataManagementController {

    @GetMapping("/ping/data-server")
    public ResponseEntity<String> getMethodName() {
        return  ResponseEntity.ok("Genesis Personal Fitness Data Server is HEALTHY.\n");
    }
}
