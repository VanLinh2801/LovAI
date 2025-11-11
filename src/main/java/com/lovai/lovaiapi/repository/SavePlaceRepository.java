package com.lovai.lovaiapi.repository;

import com.lovai.lovaiapi.model.SavePlace;
import com.lovai.lovaiapi.model.User;
import com.lovai.lovaiapi.model.ExternalVenue;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SavePlaceRepository extends JpaRepository<SavePlace, UUID> {
    Optional<SavePlace> findByUserAndVenue(User user, ExternalVenue venue);
    List<SavePlace> findAllByUserOrderByCreatedAtDesc(User user);
    boolean existsByUserAndVenue(User user, ExternalVenue venue);
    long deleteByUserAndVenue(User user, ExternalVenue venue);
}


