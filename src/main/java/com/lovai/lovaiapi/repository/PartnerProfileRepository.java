package com.lovai.lovaiapi.repository;

import com.lovai.lovaiapi.model.PartnerProfile;
import com.lovai.lovaiapi.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface PartnerProfileRepository extends JpaRepository<PartnerProfile, UUID> {
    
    Optional<PartnerProfile> findByUser(User user);
    
    boolean existsByUser(User user);
}
