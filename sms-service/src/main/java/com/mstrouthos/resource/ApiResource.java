package com.mstrouthos.resource;

import com.mstrouthos.dto.SmsMessage;
import com.mstrouthos.dto.SmsCallback;
import org.eclipse.microprofile.reactive.messaging.Channel;
import org.eclipse.microprofile.reactive.messaging.Emitter;
import org.jboss.logging.Logger;

import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.regex.Pattern;
import java.util.List;

@Path("/api/sms")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class ApiResource {
    private static final Logger LOG = Logger.getLogger(ApiResource.class);
    private static final Pattern PHONE_PATTERN = Pattern.compile("^\\+?[1-9]\\d{1,14}$");

    @Inject
    @Channel("sms-queue")
    Emitter<SmsMessage> smsEmitter;

    @Inject
    EntityManager entityManager;

    @POST
    @Path("/send")
    @Transactional
    public Response sendSms(SmsMessage smsMessage) {
        LOG.infof("Received SMSA request for phone: %s", smsMessage.phoneNumber);  

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

    @GET
    @Path("/messages")
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

    @POST
    @Path("/callback")
    @Transactional
    public Response callback(SmsCallback callback) {
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