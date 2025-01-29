package com.gf.server.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.gf.server.dto.ReqResDTO;
import com.gf.server.entities.GF_Client;
import com.gf.server.exceptions.FailedSaveException;
import com.gf.server.repositories.ClientRepository;
import com.gf.server.repositories.ExerciseInstanceRepository;

import jakarta.persistence.EntityNotFoundException;

@Service
public class GF_DataManagementService {
    
    @Autowired
    private ClientRepository clientRepository;

    @Autowired
    private ExerciseInstanceRepository exerciseInstanceRepository;

    public ReqResDTO createClient(ReqResDTO request) throws FailedSaveException {

        if (clientRepository.findByEmail(request.client().getEmail()).isPresent()) {
            throw new FailedSaveException("Cannot create new instance of client with email " + request.client().getEmail() + "; already exists");
        }

        GF_Client newClient = new GF_Client();

        newClient.setEmail(request.client().getEmail());
        newClient.setFirstName(request.client().getFirstName());
        newClient.setLastName(request.client().getLastName());
        
        GF_Client createdClient = this.clientRepository.save(newClient);

        ReqResDTO response = new ReqResDTO(
            null, 
            null, 
            null, 
            null, 
            createdClient, 
            null
        );

        return response;
    }

    public ReqResDTO getClientByEmail(ReqResDTO request) throws EntityNotFoundException {

        GF_Client client = this.clientRepository.findByEmail(request.client().getEmail()).orElseThrow(() -> new EntityNotFoundException());

        ReqResDTO response = new ReqResDTO(
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
