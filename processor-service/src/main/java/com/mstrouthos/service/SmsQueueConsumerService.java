package com.mstrouthos.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mstrouthos.client.SmsCallbackClient;
import com.mstrouthos.dto.SmsMessage;
import com.mstrouthos.dto.SmsCallback;
import io.vertx.core.json.JsonObject;
import io.smallrye.reactive.messaging.annotations.Blocking;
import org.eclipse.microprofile.reactive.messaging.Incoming;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.jboss.logging.Logger;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Random;
import java.util.concurrent.CompletionStage;

@ApplicationScoped
public class SmsQueueConsumerService {
    
    private static final Logger LOG = Logger.getLogger(SmsQueueConsumerService.class);
    private static final Random random = new Random();
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    
    @Inject
    ObjectMapper objectMapper;

    // Error messages for failed deliveries
    private static final String[] ERROR_MESSAGES = {
        "Network timeout",
        "SMS service unavailable",
        "Rate limit exceeded",
        "Invalid message format"
    };
    
    @Inject
    @RestClient
    SmsCallbackClient callbackClient;
    
    @Incoming("sms-queue")
    @Blocking
    public CompletionStage<Void> processSmsMessage(JsonObject message) {
        LOG.infof("Process SMS message start");
        SmsMessage smsMessage = objectMapper.convertValue(message.getMap(), SmsMessage.class);
        LOG.infof("Processing SMS message: %s", smsMessage.id);
        
        boolean isDelivered = random.nextDouble() < 0.85;
        
        SmsCallback callback = createCallback(smsMessage, isDelivered);
        
        LOG.infof("Sending callback for SMS %s with status: %s", smsMessage.id, callback.status);
        
        return callbackClient.sendCallback(callback)
            .thenAccept(response -> {
                if (response.getStatus() >= 200 && response.getStatus() < 300) {
                    LOG.infof("Successfully sent callback for SMS %s", smsMessage.id);
                } else {
                    LOG.errorf("Failed to send callback for SMS %s. Status: %d", 
                              smsMessage.id, response.getStatus());
                }
            })
            .exceptionally(throwable -> {
                LOG.errorf("Error sending callback for SMS %s: %s", 
                          smsMessage.id, throwable.getMessage());
                return null;
            });
    }
    
    private SmsCallback createCallback(SmsMessage smsMessage, boolean isDelivered) {
        String status = isDelivered ? "delivered" : "failed";
        String errorMessage = isDelivered ? null : getRandomErrorMessage();
        String deliveredAt = isDelivered ? LocalDateTime.now().format(formatter) : null;
        
        return new SmsCallback(
            smsMessage.id,
            status,
            errorMessage,
            deliveredAt
        );
    }
    
    private String getRandomErrorMessage() {
        return ERROR_MESSAGES[random.nextInt(ERROR_MESSAGES.length)];
    }
}