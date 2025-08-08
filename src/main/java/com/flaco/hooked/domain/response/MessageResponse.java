package com.flaco.hooked.domain.response;

public class MessageResponse {

    private String message;
    private boolean success;

    public MessageResponse() {}


    public MessageResponse(String message) {
        this.message = message;
        this.success = true;
    }

    public MessageResponse(String message, boolean success) {
        this.message = message;
        this.success = success;
    }

    // Métodos estáticos para crear respuestas comunes
    public static MessageResponse success(String message) {
        return new MessageResponse(message, true);
    }

    public static MessageResponse error(String message) {
        return new MessageResponse(message, false);
    }

    // Getters
    public String getMessage() { return message; }
    public boolean isSuccess() { return success; }

    // Setters
    public void setMessage(String message) { this.message = message; }
    public void setSuccess(boolean success) { this.success = success; }

    @Override
    public String toString() {
        return "MessageResponse{" +
                "message='" + message + '\'' +
                ", success=" + success +
                '}';
    }
}
