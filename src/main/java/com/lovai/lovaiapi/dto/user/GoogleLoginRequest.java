package com.lovai.lovaiapi.dto.user;

import jakarta.validation.constraints.NotBlank;

public class GoogleLoginRequest {

    @NotBlank(message = "Firebase ID Token không được để trống")
    private String firebaseIdToken;

    public GoogleLoginRequest() {}

    public GoogleLoginRequest(String firebaseIdToken) {
        this.firebaseIdToken = firebaseIdToken;
    }

    public String getFirebaseIdToken() {
        return firebaseIdToken;
    }

    public void setFirebaseIdToken(String firebaseIdToken) {
        this.firebaseIdToken = firebaseIdToken;
    }
}
