package com.example.lovai.DTO;

public class UpdateCoupleRequest {
    private String title;
    private String anniversaryDate;
    private PartnerRequest partner;

    public UpdateCoupleRequest() {}

    public UpdateCoupleRequest(String title, String anniversaryDate, PartnerRequest partner) {
        this.title = title;
        this.anniversaryDate = anniversaryDate;
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

    public PartnerRequest getPartner() {
        return partner;
    }

    public void setPartner(PartnerRequest partner) {
        this.partner = partner;
    }
}
