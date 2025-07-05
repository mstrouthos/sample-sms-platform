package com.mstrouthos.dto;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "sms_messages")
public class SmsMessage {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    @Column(name = "phone_number", nullable = false)
    public String phoneNumber;

    @Column(name = "message", nullable = false)
    public String text;

    @Column(name = "created_at")
    public LocalDateTime createdAt;

    @Column(name = "status")
    public String status;

    public SmsMessage() {}

    public SmsMessage(String phoneNumber, String text) {
        this.phoneNumber = phoneNumber;
        this.text = text;
        this.createdAt = LocalDateTime.now();
        this.status = "QUEUED";
    }
}