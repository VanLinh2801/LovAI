package com.lovai.lovaiapi.repository;

import com.lovai.lovaiapi.model.ExternalVenue;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ExternalVenueRepository extends JpaRepository<ExternalVenue, UUID> {
    Optional<ExternalVenue> findByProviderAndExternalId(String provider, String externalId);
}


