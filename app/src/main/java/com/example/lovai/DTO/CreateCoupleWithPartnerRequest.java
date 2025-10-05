package com.example.lovai.DTO;

public class CreateCoupleWithPartnerRequest {
    private String title;
    private String anniversaryDate;
    private String userId;
    private PartnerRequest partner;

    public CreateCoupleWithPartnerRequest() {}

    public CreateCoupleWithPartnerRequest(String title, String anniversaryDate, String userId, PartnerRequest partner) {
        this.title = title;
        this.anniversaryDate = anniversaryDate;
        this.userId = userId;
        this.partner = partner;
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

    public PartnerRequest getPartner() {
        return partner;
    }

    public void setPartner(PartnerRequest partner) {
        this.partner = partner;
    }
}
