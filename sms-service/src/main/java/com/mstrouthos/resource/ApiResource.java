package com.mstrouthos.resource;

import com.mstrouthos.dto.SmsMessage;
import com.mstrouthos.dto.SmsCallback;
import com.mstrouthos.validation.SmsValidator;
import com.mstrouthos.validation.ValidationResult;
import org.eclipse.microprofile.reactive.messaging.Channel;
import org.eclipse.microprofile.reactive.messaging.Emitter;
import org.jboss.logging.Logger;
import org.eclipse.microprofile.openapi.annotations.OpenAPIDefinition;
import org.eclipse.microprofile.openapi.annotations.info.Info;
import org.eclipse.microprofile.openapi.annotations.info.Contact;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;

import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.regex.Pattern;
import java.util.List;

@OpenAPIDefinition(
    info = @Info(
        title = "SMS API",
        version = "1.0.0",
        description = "API for sending SMS messages and managing message status",
        contact = @Contact(
            name = "SMS API Support",
            email = "support@mstrouthos.com"
        )
    )
)
@Path("/api/sms")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "SMS", description = "SMS messaging operations")
public class ApiResource {
    private static final Logger LOG = Logger.getLogger(ApiResource.class);
    private static final Pattern PHONE_PATTERN = Pattern.compile("^\\+?[1-9]\\d{1,14}$");

    @Inject
    @Channel("sms-queue")
    Emitter<SmsMessage> smsEmitter;

    @Inject
    EntityManager entityManager;

    @Inject
    SmsValidator smsValidator;

    /**
     * Sends an SMS message by validating the request, persisting it to the database,
     * and queuing it for delivery via RabbitMQ
     */
    @POST
    @Path("/send")
    @Transactional
    @Operation(
        summary = "Send SMS message",
        description = "Validates and queues an SMS message for delivery"
    )
    @APIResponse(
        responseCode = "200",
        description = "SMS message queued successfully",
        content = @Content(
            mediaType = MediaType.APPLICATION_JSON,
            schema = @Schema(example = "{\"status\":\"queued\",\"message\":\"SMS queued for delivery\"}")
        )
    )
    @APIResponse(
        responseCode = "400",
        description = "Invalid SMS request",
        content = @Content(
            mediaType = MediaType.APPLICATION_JSON,
            schema = @Schema(example = "{\"error\":\"Validation failed\",\"details\":{}}")
        )
    )
    @APIResponse(
        responseCode = "500",
        description = "Internal server error",
        content = @Content(
            mediaType = MediaType.APPLICATION_JSON,
            schema = @Schema(example = "{\"error\":\"Failed to queue message\"}")
        )
    )
    public Response sendSms(
        @Parameter(description = "SMS message details", required = true)
        SmsMessage smsMessage
    ) {
        LOG.infof("Received SMS request for phone: %s", smsMessage.phoneNumber);  

        ValidationResult validation = smsValidator.validateSmsRequest(smsMessage);
        if (!validation.isValid()) {
            LOG.warnf("SMS validation failed: %s", validation.getErrorMessage());
            return Response.status(400)
                .entity(String.format("{\"error\":\"%s\",\"details\":%s}", 
                       validation.getErrorMessage(), validation.getErrorDetailsJson()))
                .build();
        }

        try {
            smsMessage.createdAt = java.time.LocalDateTime.now();
            smsMessage.status = "QUEUED";

            entityManager.persist(smsMessage);

            smsEmitter.send(smsMessage);
            LOG.infof("SMS message queued successfully for: %s", smsMessage.phoneNumber);

            return Response.ok()
                .entity("{\"status\":\"queued\",\"message\":\"SMS queued for delivery\"}")
                .build();
        } catch (Exception e) {
            LOG.error("Failed to queue SMS message", e);

            return Response.status(500)
                .entity("{\"error\":\"Failed to queue message\"}")
                .build();
        }
    }

    /**
     * Retrieves all SMS messages from the database for the current user.
     */
    @GET
    @Path("/messages")
    @Operation(
        summary = "List SMS messages",
        description = "Retrieves all SMS messages for the current user"
    )
    @APIResponse(
        responseCode = "200",
        description = "List of SMS messages retrieved successfully",
        content = @Content(
            mediaType = MediaType.APPLICATION_JSON,
            schema = @Schema(implementation = SmsMessage.class)
        )
    )
    @APIResponse(
        responseCode = "500",
        description = "Internal server error",
        content = @Content(
            mediaType = MediaType.APPLICATION_JSON,
            schema = @Schema(example = "{\"error\":\"Failed to retrieve messages\"}")
        )
    )
    public Response listSms() {
        LOG.infof("Listing SMS messages for current user");

        try {
            List<SmsMessage> messages = entityManager
                .createQuery("SELECT s FROM SmsMessage s", SmsMessage.class)
                .getResultList();

            return Response.ok(messages).build();
        } catch (Exception e) {
            LOG.error("Failed to retrieve SMS messages for user");

            return Response.status(500)
                .entity("{\"error\":\"Failed to retrieve messages\"}")
                .build();
        }
    }

    /**
     * Handles SMS delivery status callbacks from the SMS provider,
     * updating the message status in the database.
     */
    @POST
    @Path("/callback")
    @Transactional
        @Operation(
        summary = "Handle SMS status callback",
        description = "Updates SMS message status based on delivery provider callback"
    )
    @APIResponse(
        responseCode = "200",
        description = "Status updated successfully"
    )
    @APIResponse(
        responseCode = "404",
        description = "SMS message not found"
    )
    @APIResponse(
        responseCode = "500",
        description = "Internal server error"
    )
    public Response callback(
        @Parameter(description = "SMS callback data", required = true)
        SmsCallback callback
    ) {
            LOG.infof("Received callback for SMS ID: %s with status: %s", callback.id, callback.status);
            
            try {
                int updated = entityManager.createQuery(
                    "UPDATE SmsMessage s SET s.status = :status WHERE s.id = :id")
                    .setParameter("status", callback.status.toUpperCase())
                    .setParameter("id", Long.valueOf(callback.id))
                    .executeUpdate();
                
                LOG.infof("Update status: %s", updated);

                if (updated > 0) {
                    LOG.infof("Successfully updated SMS %s status to %s", callback.id, callback.status);
                    return Response.ok().build();
                } else {
                    LOG.warnf("No SMS found with ID %s", callback.id);
                    return Response.status(404).build();
                }
            } catch (Exception e) {
                LOG.error("Failed to update SMS status", e);
            }

            return Response.status(500).build();
    }
}