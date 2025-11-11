package com.lovai.lovaiapi.dto.place;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public class SavePlaceRequest {

    @NotNull
    private UUID venueId;

    private String note;

    public UUID getVenueId() { return venueId; }
    public void setVenueId(UUID venueId) { this.venueId = venueId; }

    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }
}


