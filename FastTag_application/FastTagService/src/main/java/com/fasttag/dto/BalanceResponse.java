package com.fasttag.dto;

public class BalanceResponse {
    private boolean success;
    private String message;

    public BalanceResponse() {}
    public BalanceResponse(boolean success, String message) { this.success = success; this.message = message; }

    public boolean isSuccess() { return success; }
    public void setSuccess(boolean success) { this.success = success; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
}
