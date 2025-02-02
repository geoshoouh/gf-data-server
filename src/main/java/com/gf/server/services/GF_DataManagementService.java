package com.gf.server.services;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.gf.server.entities.ExerciseRecord;
import com.gf.server.entities.GF_Client;
import com.gf.server.enumerations.EquipmentEnum;
import com.gf.server.enumerations.ExerciseEnum;
import com.gf.server.exceptions.FailedSaveException;
import com.gf.server.repositories.ClientRepository;
import com.gf.server.repositories.ExerciseRecordRepository;

import jakarta.persistence.EntityNotFoundException;

@Service
public class GF_DataManagementService {
    
    @Autowired
    private ClientRepository clientRepository;

    @Autowired
    private ExerciseRecordRepository exerciseRecordRepository;

    public GF_Client createClient(String email, String lastName, String firstName) throws FailedSaveException {

        if (clientRepository.findByEmail(email).isPresent()) {
            throw new FailedSaveException("Cannot create new instance of client with email " + email + "; already exists");
        }

        GF_Client newClient = new GF_Client();

        newClient.setEmail(email);
        newClient.setFirstName(firstName);
        newClient.setLastName(lastName);
        
        return this.clientRepository.save(newClient);
    }

    public GF_Client getClientByEmail(String email) throws EntityNotFoundException {

        return this.clientRepository.findByEmail(email).orElseThrow(() -> new EntityNotFoundException());
    }

    public ExerciseRecord createExerciseRecord(ExerciseRecord exerciseRecord) {

        ExerciseRecord savedRecord = this.exerciseRecordRepository.save(exerciseRecord);

        return savedRecord;
    }

    public ExerciseRecord getLatestExerciseRecord(String clientEmail, EquipmentEnum equipmentType, ExerciseEnum exercise) throws EntityNotFoundException {
        
        Optional<GF_Client> client = this.clientRepository.findByEmail(clientEmail);

        if (!client.isPresent()) {
            throw new EntityNotFoundException();
        }

        Optional<List<ExerciseRecord>> exerciseRecord = this.exerciseRecordRepository.findByClientAndEquipmentTypeAndExerciseOrderByDateTime(client.get(), equipmentType, exercise);

        if (!exerciseRecord.isPresent()) {
            throw new EntityNotFoundException();
        }

        return exerciseRecord.get().get(0);
    }

    public void clearClientRepository() {

        this.clientRepository.deleteAll();
    }

    public void clearExerciseRecordRepository() {

        this.exerciseRecordRepository.deleteAll();
    }

    public Long getClientCount() {

        return this.clientRepository.count();
    }

    public Long getExerciseRecordCount() {

        return this.exerciseRecordRepository.count();
    }
}
