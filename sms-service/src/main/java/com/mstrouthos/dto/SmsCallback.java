package com.mstrouthos.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public class SmsCallback {
    @JsonProperty("id")
    public String id;
    
    @JsonProperty("status")
    public String status;
    
    @JsonProperty("error_message")
    public String errorMessage;
    
    @JsonProperty("delivered_at")
    public String deliveredAt;
    
    // Constructors
    public SmsCallback() {}
    
    public SmsCallback(String id, String status, String errorMessage, String deliveredAt) {
        this.id = id;
        this.status = status;
        this.errorMessage = errorMessage;
        this.deliveredAt = deliveredAt;
    }
}