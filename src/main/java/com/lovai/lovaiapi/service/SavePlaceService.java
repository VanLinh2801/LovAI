package com.lovai.lovaiapi.service;

import com.lovai.lovaiapi.exception.BadRequestException;
import com.lovai.lovaiapi.exception.NotFoundException;
import com.lovai.lovaiapi.model.ExternalVenue;
import com.lovai.lovaiapi.model.SavePlace;
import com.lovai.lovaiapi.model.User;
import com.lovai.lovaiapi.repository.ExternalVenueRepository;
import com.lovai.lovaiapi.repository.SavePlaceRepository;
import com.lovai.lovaiapi.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class SavePlaceService {

    private final SavePlaceRepository savePlaceRepository;
    private final ExternalVenueRepository externalVenueRepository;
    private final UserRepository userRepository;

    public SavePlaceService(SavePlaceRepository savePlaceRepository,
                            ExternalVenueRepository externalVenueRepository,
                            UserRepository userRepository) {
        this.savePlaceRepository = savePlaceRepository;
        this.externalVenueRepository = externalVenueRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public SavePlace savePlace(UUID userId, UUID venueId, String note) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found"));
        ExternalVenue venue = externalVenueRepository.findById(venueId)
                .orElseThrow(() -> new NotFoundException("Venue not found"));

        if (savePlaceRepository.existsByUserAndVenue(user, venue)) {
            throw new BadRequestException("Place already saved");
        }

        SavePlace save = SavePlace.builder()
                .user(user)
                .venue(venue)
                .note(note)
                .build();
        return savePlaceRepository.save(save);
    }

    @Transactional
    public void removeSave(UUID userId, UUID venueId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found"));
        ExternalVenue venue = externalVenueRepository.findById(venueId)
                .orElseThrow(() -> new NotFoundException("Venue not found"));

        long removed = savePlaceRepository.deleteByUserAndVenue(user, venue);
        if (removed == 0) {
            throw new NotFoundException("Saved place not found");
        }
    }

    @Transactional(readOnly = true)
    public List<SavePlace> listSaves(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found"));
        return savePlaceRepository.findAllByUserOrderByCreatedAtDesc(user);
    }
}


