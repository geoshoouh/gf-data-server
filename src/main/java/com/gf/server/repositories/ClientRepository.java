package com.gf.server.repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import com.gf.server.entities.GF_Client;

public interface ClientRepository extends JpaRepository<GF_Client, Long>{
    
}
