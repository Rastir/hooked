package com.flaco.hooked.domain.request;

import jakarta.validation.constraints.NotBlank;

public class LogoutRequest {

    @NotBlank(message = "El refresh token es obligatorio")
    private String refreshToken;

    public LogoutRequest() {}

    public LogoutRequest(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    public String getRefreshToken() { return refreshToken; }
    public void setRefreshToken(String refreshToken) { this.refreshToken = refreshToken; }

    @Override
    public String toString() {
        return "LogoutRequest{refreshToken='[PROTECTED]'}";
    }
}