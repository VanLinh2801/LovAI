package com.example.lovai.DTO;

import java.util.UUID;

public class CoupleResponse {
    private String id;
    private String title;
    private String anniversaryDate;
    private String userId;
    private PartnerResponse partner;
    public CoupleResponse(){}

    public CoupleResponse(String id, String title, String anniversaryDate, String userId, PartnerResponse partner) {
        this.id = id;
        this.title = title;
        this.anniversaryDate = anniversaryDate;
        this.userId = userId;
        this.partner = partner;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getAnniversaryDate() {
        return anniversaryDate;
    }

    public void setAnniversaryDate(String anniversaryDate) {
        this.anniversaryDate = anniversaryDate;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public PartnerResponse getPartner() {
        return partner;
    }

    public void setPartner(PartnerResponse partner) {
        this.partner = partner;
    }




}
