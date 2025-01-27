package com.gf.server.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.gf.server.repositories.ClientRepository;
import com.gf.server.repositories.ExerciseInstanceRepository;

@Service
public class GF_DataManagementService {
    
    @Autowired
    private ClientRepository clientRepository;

    @Autowired
    private ExerciseInstanceRepository exerciseInstanceRepository;

    
}
