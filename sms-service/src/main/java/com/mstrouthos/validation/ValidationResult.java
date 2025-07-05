package com.mstrouthos.validation;

import java.util.List;

/**
 * Represents the result of a validation operation.
 * Contains validation status, error messages, and detailed error information.
 */
public class ValidationResult {
    private final boolean valid;
    private final String errorMessage;
    private final List<String> errorDetails;

    public ValidationResult(boolean valid, String errorMessage, List<String> errorDetails) {
        this.valid = valid;
        this.errorMessage = errorMessage;
        this.errorDetails = errorDetails;
    }

    /**
     * Creates a successful validation result.
     */
    public static ValidationResult success() {
        return new ValidationResult(true, null, null);
    }

    /**
     * Creates a failed validation result with a single error.
     */
    public static ValidationResult failure(String errorMessage) {
        return new ValidationResult(false, errorMessage, List.of(errorMessage));
    }

    /**
     * Creates a failed validation result with multiple errors.
     */
    public static ValidationResult failure(String mainError, List<String> errorDetails) {
        return new ValidationResult(false, mainError, errorDetails);
    }

    /**
     * Creates a failed validation result with multiple errors (main error derived from list).
     */
    public static ValidationResult failure(List<String> errorDetails) {
        if (errorDetails == null || errorDetails.isEmpty()) {
            return success();
        }
        String mainError = errorDetails.size() == 1 ? errorDetails.get(0) : "Multiple validation errors";
        return new ValidationResult(false, mainError, errorDetails);
    }

    public boolean isValid() {
        return valid;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public List<String> getErrorDetails() {
        return errorDetails;
    }

    /**
     * Returns error details as a JSON array string.
     * Used for API responses.
     */
    public String getErrorDetailsJson() {
        if (errorDetails == null || errorDetails.isEmpty()) {
            return "[]";
        }
        
        StringBuilder json = new StringBuilder("[");
        for (int i = 0; i < errorDetails.size(); i++) {
            if (i > 0) json.append(",");
            json.append("\"").append(errorDetails.get(i).replace("\"", "\\\"")).append("\"");
        }
        json.append("]");
        return json.toString();
    }

    @Override
    public String toString() {
        return "ValidationResult{" +
                "valid=" + valid +
                ", errorMessage='" + errorMessage + '\'' +
                ", errorDetails=" + errorDetails +
                '}';
    }
}