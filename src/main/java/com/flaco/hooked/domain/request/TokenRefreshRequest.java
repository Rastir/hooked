package com.flaco.hooked.domain.request;

import jakarta.validation.constraints.NotBlank;

public class TokenRefreshRequest {

    @NotBlank(message = "El refresh token es obligatorio")
    private String refreshToken;

    public TokenRefreshRequest() {}

    public TokenRefreshRequest(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    public String getRefreshToken() { return refreshToken; }
    public void setRefreshToken(String refreshToken) { this.refreshToken = refreshToken; }

    @Override
    public String toString() {
        return "TokenRefreshRequest{refreshToken='[PROTECTED]'}";
    }
}