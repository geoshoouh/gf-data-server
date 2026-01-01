package com.gf.server.services;

import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.ArrayList;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

        exerciseRecord.setDateTime(Date.from(Instant.now()));
        
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

    public List<GF_Client> getAllClients() {
        return this.clientRepository.findAll();
    }

    public EquipmentEnum[] getAllEquipmentTypes() {
        return EquipmentEnum.values();
    }

    public ExerciseEnum[] getAllExerciseTypes() {
        return ExerciseEnum.values();
    }

    public List<ExerciseRecord> getExerciseRecordsAfterDate(String clientEmail, EquipmentEnum equipmentType, ExerciseEnum exercise, Date afterDate) throws EntityNotFoundException {
        
        Optional<GF_Client> client = this.clientRepository.findByEmail(clientEmail);

        if (!client.isPresent()) {
            throw new EntityNotFoundException();
        }

        Optional<List<ExerciseRecord>> exerciseRecords = this.exerciseRecordRepository.findByClientAndEquipmentTypeAndExerciseAndDateTimeAfterOrderByDateTimeDesc(client.get(), equipmentType, exercise, afterDate);

        if (!exerciseRecords.isPresent()) {
            return new ArrayList<>();
        }

        return exerciseRecords.get();
    }

    @Transactional
    public BulkUploadResult bulkCreateExerciseRecords(List<ExerciseRecord> exerciseRecords) {
        int successful = 0;
        int failed = 0;
        List<String> errors = new ArrayList<>();

        for (int i = 0; i < exerciseRecords.size(); i++) {
            ExerciseRecord record = exerciseRecords.get(i);
            try {
                // Validate client exists
                if (record.getClient() == null || record.getClient().getEmail() == null) {
                    errors.add("Row " + (i + 2) + ": Client email is required");
                    failed++;
                    continue;
                }

                GF_Client client = getClientByEmail(record.getClient().getEmail());
                record.setClient(client);

                // Set date time if not provided
                if (record.getDateTime() == null) {
                    record.setDateTime(Date.from(Instant.now()));
                }

                // Save the record
                this.exerciseRecordRepository.save(record);
                successful++;
            } catch (EntityNotFoundException e) {
                errors.add("Row " + (i + 2) + ": Client with email " + record.getClient().getEmail() + " not found");
                failed++;
            } catch (Exception e) {
                errors.add("Row " + (i + 2) + ": " + e.getMessage());
                failed++;
            }
        }

        return new BulkUploadResult(exerciseRecords.size(), successful, failed, errors);
    }

    public static class BulkUploadResult {
        private final int totalRecords;
        private final int successfulRecords;
        private final int failedRecords;
        private final List<String> errors;

        public BulkUploadResult(int totalRecords, int successfulRecords, int failedRecords, List<String> errors) {
            this.totalRecords = totalRecords;
            this.successfulRecords = successfulRecords;
            this.failedRecords = failedRecords;
            this.errors = errors;
        }

        public int getTotalRecords() { return totalRecords; }
        public int getSuccessfulRecords() { return successfulRecords; }
        public int getFailedRecords() { return failedRecords; }
        public List<String> getErrors() { return errors; }
    }
}
