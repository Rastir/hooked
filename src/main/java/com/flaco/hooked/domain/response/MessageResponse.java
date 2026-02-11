package com.flaco.hooked.domain.response;

public class MessageResponse {

    private String message;
    private boolean success;

    public MessageResponse() {}

    public MessageResponse(String message) {
        this(message, true);
    }

    public MessageResponse(String message, boolean success) {
        this.message = message;
        this.success = success;
    }

    // Factory methods
    public static MessageResponse success(String message) {
        return new MessageResponse(message, true);
    }

    public static MessageResponse error(String message) {
        return new MessageResponse(message, false);
    }

    // Getters y setters
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public boolean isSuccess() { return success; }
    public void setSuccess(boolean success) { this.success = success; }

    @Override
    public String toString() {
        return "MessageResponse{success=" + success + ", message='" + message + "'}";
    }
}