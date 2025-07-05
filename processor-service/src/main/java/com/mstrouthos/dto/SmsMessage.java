package com.mstrouthos.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public class SmsMessage {
    @JsonProperty("id")
    public String id;
    
    @JsonProperty("phone_number")
    public String phoneNumber;
    
    @JsonProperty("message")
    public String message;
    
    @JsonProperty("created_at")
    public String createdAt;

    @JsonProperty("status")
    public String status;
    
    public SmsMessage() {}
    
    public SmsMessage(String id, String phoneNumber, String message, String createdAt, String status) {
        this.id = id;
        this.phoneNumber = phoneNumber;
        this.message = message;
        this.createdAt = createdAt;
        this.status = status;
    }
}