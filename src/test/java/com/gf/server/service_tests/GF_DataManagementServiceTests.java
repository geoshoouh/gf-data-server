package com.gf.server.service_tests;

import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.Random;

import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.util.Assert;

import com.gf.server.entities.ExerciseRecord;
import com.gf.server.entities.GF_Client;
import com.gf.server.enumerations.EquipmentEnum;
import com.gf.server.enumerations.ExerciseEnum;
import com.gf.server.services.GF_DataManagementService;

@SpringBootTest
public class GF_DataManagementServiceTests {
    
    @Autowired
    GF_DataManagementService dataManagementService;

    GF_Client clientCreationUtilPersistent(String email) {
        
        String newClientEmail = email;
        String newClientFirstName = RandomStringUtils.randomAlphabetic(7);
        String newClientLastName = RandomStringUtils.randomAlphabetic(7);

        return this.dataManagementService.createClient(newClientEmail, newClientLastName, newClientFirstName);
    }

    GF_Client clientCreationUtilPersistent() {
        return this.clientCreationUtilPersistent(RandomStringUtils.randomAlphanumeric(7) + "@" + RandomStringUtils.randomAlphabetic(4) + ".com");
    }

    ExerciseRecord exerciseRecordCreationUtil() {

        GF_Client client = clientCreationUtilPersistent();

        ExerciseRecord exerciseRecord = new ExerciseRecord();
        
        exerciseRecord.setClient(client);
        exerciseRecord.setDateTime(Date.from(Instant.now()));
        exerciseRecord.setEquipmentType(EquipmentEnum.values()[new Random().nextInt(EquipmentEnum.values().length)]);
        exerciseRecord.setExercise(ExerciseEnum.values()[new Random().nextInt(ExerciseEnum.values().length)]);

        return exerciseRecord;
    }

    ExerciseRecord exerciseRecordCreationUtilPersistent() {

        return this.dataManagementService.createExerciseRecord(this.exerciseRecordCreationUtil());
    }

    @AfterEach
    void teardown() {
        
        this.dataManagementService.clearExerciseRecordRepository();
        this.dataManagementService.clearClientRepository();
    }

    @Test
    void dataManagementCanCreateNewClients() {

        String newClientEmail = RandomStringUtils.randomAlphanumeric(7) + "@" + RandomStringUtils.randomAlphabetic(4) + ".com";
        String newClientFirstName = RandomStringUtils.randomAlphabetic(7);
        String newClientLastName = RandomStringUtils.randomAlphabetic(7);

        GF_Client createdClient = this.dataManagementService.createClient(newClientEmail, newClientLastName, newClientFirstName);

        Assert.isTrue(this.dataManagementService.getClientCount() == 1L, "Expect client count 1; was " + this.dataManagementService.getClientCount());
        Assert.isTrue(createdClient.getEmail() == newClientEmail, "Expected created client value " + newClientEmail + ", was " + createdClient.getEmail());
        Assert.isTrue(createdClient.getFirstName() == newClientFirstName, "Expected created client value " + newClientFirstName + ", was " + createdClient.getFirstName());
        Assert.isTrue(createdClient.getLastName() == newClientLastName, "Expected created client value " + newClientLastName + ", was " + createdClient.getLastName());
    }

    @Test
    void getClientByEmailGetsClientByEmail() {

        String email = RandomStringUtils.randomAlphanumeric(7) + "@" + RandomStringUtils.randomAlphabetic(4) + ".com";

        this.clientCreationUtilPersistent(email);

        GF_Client foundClient = this.dataManagementService.getClientByEmail(email);

        Assert.isTrue(foundClient.getEmail() == email, "Expect email " + email + "; was " + foundClient.getEmail());
    }

    @Test
    void createNewExerciseRecordCreatesNewExerciseRecord() {

        ExerciseRecord exerciseRecord = this.exerciseRecordCreationUtil();

        Assert.isNull(exerciseRecord.getId(), "Expected pre-insert exercise record to have null ID");

        ExerciseRecord createdExerciseRecord = this.dataManagementService.createExerciseRecord(exerciseRecord);

        Assert.notNull(createdExerciseRecord.getId(), "Exercise record ID was null");
        Assert.isTrue(exerciseRecord.getClient().getId() == createdExerciseRecord.getClient().getId(), "Client ID's not equal");
    }

    @Test
    void getLatestExerciseRecordGetsLatestExerciseRecord() {

        for (int i = 0; i < 4; i++) {
            this.exerciseRecordCreationUtilPersistent();
        }

        ExerciseRecord latestExerciseRecord = this.exerciseRecordCreationUtilPersistent();

        Assert.isTrue(this.dataManagementService.getExerciseRecordCount() == 5L, "Expected exercise record count to be 5; was " + this.dataManagementService.getExerciseRecordCount());

        ExerciseRecord fetchedLatestExerciseRecord = this.dataManagementService.getLatestExerciseRecord(latestExerciseRecord.getClient().getEmail(), latestExerciseRecord.getEquipmentType(), latestExerciseRecord.getExercise());

        Assert.isTrue(fetchedLatestExerciseRecord.getId() == latestExerciseRecord.getId(), "Expected id on latest record " + latestExerciseRecord.getId() + "; was " + fetchedLatestExerciseRecord.getId());
    }

    @Test
    void getExerciseRecordsAfterDateReturnsRecordsAfterDate() throws InterruptedException {

        // Create a client
        GF_Client client = this.clientCreationUtilPersistent();
        
        // Create a baseline date
        Date baselineDate = new Date();
        
        // Wait a moment to ensure time difference
        Thread.sleep(100);
        
        // Create exercise records after the baseline date
        for (int i = 0; i < 3; i++) {
            ExerciseRecord record = new ExerciseRecord();
            record.setClient(client);
            record.setEquipmentType(EquipmentEnum.NAUTILUS);
            record.setExercise(ExerciseEnum.BICEP_CURL);
            record.setResistance(50 + i);
            record.setSeatSetting(3);
            record.setPadSetting(2);
            record.setRightArm(1);
            record.setLeftArm(1);
            this.dataManagementService.createExerciseRecord(record);
        }

        // Get records after baseline date
        List<ExerciseRecord> records = this.dataManagementService.getExerciseRecordsAfterDate(
            client.getEmail(), 
            EquipmentEnum.NAUTILUS, 
            ExerciseEnum.BICEP_CURL, 
            baselineDate
        );

        Assert.isTrue(records.size() == 3, "Expected 3 records after date; was " + records.size());
        
        // Verify records are ordered by date descending (newest first)
        for (int i = 0; i < records.size() - 1; i++) {
            Assert.isTrue(records.get(i).getDateTime().after(records.get(i + 1).getDateTime()) || 
                         records.get(i).getDateTime().equals(records.get(i + 1).getDateTime()), 
                         "Records should be ordered by date descending");
        }
    }

    @Test
    void getExerciseRecordsAfterDateReturnsEmptyListWhenNoRecordsAfterDate() {

        // Create a client
        GF_Client client = this.clientCreationUtilPersistent();
        
        // Create a future date
        Date futureDate = new Date(System.currentTimeMillis() + 24 * 60 * 60 * 1000); // 1 day in future
        
        // Create exercise records now (before the future date)
        for (int i = 0; i < 3; i++) {
            ExerciseRecord record = new ExerciseRecord();
            record.setClient(client);
            record.setEquipmentType(EquipmentEnum.NAUTILUS);
            record.setExercise(ExerciseEnum.BICEP_CURL);
            record.setResistance(50 + i);
            record.setSeatSetting(3);
            record.setPadSetting(2);
            record.setRightArm(1);
            record.setLeftArm(1);
            this.dataManagementService.createExerciseRecord(record);
        }

        // Get records after future date
        List<ExerciseRecord> records = this.dataManagementService.getExerciseRecordsAfterDate(
            client.getEmail(), 
            EquipmentEnum.NAUTILUS, 
            ExerciseEnum.BICEP_CURL, 
            futureDate
        );

        Assert.isTrue(records.isEmpty(), "Expected empty list for future date; was " + records.size());
    }

    @Test
    void getExerciseRecordsAfterDateFiltersByEquipmentAndExercise() throws InterruptedException {

        // Create a client
        GF_Client client = this.clientCreationUtilPersistent();
        
        // Create a baseline date
        Date baselineDate = new Date();
        
        // Wait a moment to ensure time difference
        Thread.sleep(100);
        
        // Create records with different equipment and exercises
        ExerciseRecord record1 = new ExerciseRecord();
        record1.setClient(client);
        record1.setEquipmentType(EquipmentEnum.NAUTILUS);
        record1.setExercise(ExerciseEnum.BICEP_CURL);
        record1.setResistance(50);
        this.dataManagementService.createExerciseRecord(record1);
        
        ExerciseRecord record2 = new ExerciseRecord();
        record2.setClient(client);
        record2.setEquipmentType(EquipmentEnum.KINESIS);
        record2.setExercise(ExerciseEnum.BICEP_CURL);
        record2.setResistance(60);
        this.dataManagementService.createExerciseRecord(record2);
        
        ExerciseRecord record3 = new ExerciseRecord();
        record3.setClient(client);
        record3.setEquipmentType(EquipmentEnum.NAUTILUS);
        record3.setExercise(ExerciseEnum.LEG_PRESS);
        record3.setResistance(70);
        this.dataManagementService.createExerciseRecord(record3);

        // Get records for specific equipment and exercise
        List<ExerciseRecord> records = this.dataManagementService.getExerciseRecordsAfterDate(
            client.getEmail(), 
            EquipmentEnum.NAUTILUS, 
            ExerciseEnum.BICEP_CURL, 
            baselineDate
        );

        Assert.isTrue(records.size() == 1, "Expected 1 record for specific equipment/exercise; was " + records.size());
        Assert.isTrue(records.get(0).getEquipmentType() == EquipmentEnum.NAUTILUS, "Expected NAUTILUS equipment");
        Assert.isTrue(records.get(0).getExercise() == ExerciseEnum.BICEP_CURL, "Expected BICEP_CURL exercise");
    }

    @Test
    void getExerciseRecordsAfterDateThrowsExceptionForNonExistentClient() {

        // Create a baseline date
        Date baselineDate = new Date();
        
        // Try to get records for non-existent client
        try {
            this.dataManagementService.getExerciseRecordsAfterDate(
                "nonexistent@email.com", 
                EquipmentEnum.NAUTILUS, 
                ExerciseEnum.BICEP_CURL, 
                baselineDate
            );
            Assert.isTrue(false, "Expected EntityNotFoundException to be thrown");
        } catch (jakarta.persistence.EntityNotFoundException e) {
            // Expected exception
        }
    }
}
