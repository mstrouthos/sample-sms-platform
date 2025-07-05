package com.mstrouthos.validation;

import com.mstrouthos.dto.SmsMessage;
import org.jboss.logging.Logger;

import jakarta.enterprise.context.ApplicationScoped;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Validator for SMS messages.
 * Handles validation logic for SMS requests including phone numbers and text content.
 */
@ApplicationScoped
public class SmsValidator {
    private static final Logger LOG = Logger.getLogger(SmsValidator.class);

    private static final Pattern PHONE_PATTERN = Pattern.compile("^\\+[1-9][0-9]{6,14}$");;
    private static final int MAX_TEXT_LENGTH = 160;
    private static final int MIN_TEXT_LENGTH = 1;

    /**
     * Validates an SMS message request.
     * 
     * @param smsMessage The SMS message to validate
     * @return ValidationResult containing validation status and error details
     */
    public ValidationResult validateSmsRequest(SmsMessage smsMessage) {
        List<String> errors = new ArrayList<>();

        if (smsMessage == null) {
            return ValidationResult.failure("Request body is required", 
                                          List.of("Missing SMS message object"));
        }

        validatePhoneNumber(smsMessage.phoneNumber, errors);
        validateTextMessage(smsMessage.text, errors);

        return errors.isEmpty() ? ValidationResult.success() : ValidationResult.failure(errors);
    }

    /**
     * Validates phone number format and presence.
     */
    private void validatePhoneNumber(String phoneNumber, List<String> errors) {
        if (phoneNumber == null || phoneNumber.trim().isEmpty()) {
            errors.add("Phone number is required");
            return;
        }

        String normalizedPhone = normalizePhoneNumber(phoneNumber);
        if (normalizedPhone == null || normalizedPhone.trim().isEmpty()) {
            errors.add("Phone number cannot be empty after normalization");
            return;
        }

        if (!PHONE_PATTERN.matcher(normalizedPhone).matches()) {
            errors.add("Invalid phone number format. Use international format (e.g., +1234567890)");
        }
    }

    /**
     * Validates text message content and length.
     */
    private void validateTextMessage(String text, List<String> errors) {
        if (text == null || text.trim().isEmpty()) {
            errors.add("Text message is required");
            return;
        }

        if (text.trim().length() < MIN_TEXT_LENGTH) {
            errors.add("Text message cannot be empty");
        }

        if (text.length() > MAX_TEXT_LENGTH) {
            errors.add(String.format("Text message too long. Maximum %d characters allowed", MAX_TEXT_LENGTH));
        }
    }

    /**
     * Normalizes phone number by removing formatting characters.
     * 
     * @param phoneNumber The phone number to normalize
     * @return Normalized phone number string
     */
    public String normalizePhoneNumber(String phoneNumber) {
        if (phoneNumber == null) {
            return null;
        }
        
        // Remove spaces, dashes, parentheses, and dots
        String normalized = phoneNumber.replaceAll("[\\s\\-\\(\\)\\.]+", "");
        
        if (!normalized.startsWith("+") && !normalized.isEmpty()) {
            LOG.warnf("Phone number doesn't start with +, consider adding country code: %s", normalized);
        }
        
        LOG.infof("Normalized phone number: %s", normalized);

        return normalized;
    }

    /**
     * Validates phone number format only.
     */
    public boolean isValidPhoneNumber(String phoneNumber) {
        if (phoneNumber == null || phoneNumber.trim().isEmpty()) {
            return false;
        }
        
        String normalized = normalizePhoneNumber(phoneNumber);
        
        return normalized != null && PHONE_PATTERN.matcher(normalized).matches();
    }

    /**
     * Validates text message length only (utility method).
     */
    public boolean isValidTextLength(String text) {
        return text != null && 
               text.trim().length() >= MIN_TEXT_LENGTH && 
               text.length() <= MAX_TEXT_LENGTH;
    }

    /**
     * Gets the maximum allowed text length.
     */
    public int getMaxTextLength() {
        return MAX_TEXT_LENGTH;
    }

    /**
     * Gets the minimum required text length.
     */
    public int getMinTextLength() {
        return MIN_TEXT_LENGTH;
    }
}