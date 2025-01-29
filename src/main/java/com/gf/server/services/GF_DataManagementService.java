package com.gf.server.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.gf.server.dto.ReqResDTO;
import com.gf.server.entities.GF_Client;
import com.gf.server.repositories.ClientRepository;
import com.gf.server.repositories.ExerciseInstanceRepository;

import jakarta.persistence.EntityNotFoundException;

@Service
public class GF_DataManagementService {
    
    @Autowired
    private ClientRepository clientRepository;

    @Autowired
    private ExerciseInstanceRepository exerciseInstanceRepository;

    public ReqResDTO getClientByEmail(ReqResDTO request) throws EntityNotFoundException {

        GF_Client client = this.clientRepository.findByEmail(request.clientEmail()).orElseThrow(() -> new EntityNotFoundException());

        ReqResDTO response = new ReqResDTO(
            null, 
            null, 
            null, 
            null, 
            null, 
            client, 
            null
        );

        return response;
    }
}
