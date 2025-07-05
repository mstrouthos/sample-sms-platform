package com.mstrouthos.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public class SmsMessage {
    @JsonProperty("phoneNumber")
    public String phoneNumber;

    @JsonProperty("text")
    public String text;

    public SmsMessage() {}

    public SmsMessage(String phoneNumber, String text) {
        this.phoneNumber = phoneNumber;
        this.text = text;
    }
}