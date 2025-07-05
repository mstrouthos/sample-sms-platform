package com.mstrouthos.resource;

import com.mstrouthos.dto.SmsMessage;
import org.eclipse.microprofile.reactive.messaging.Channel;
import org.eclipse.microprofile.reactive.messaging.Emitter;
import org.jboss.logging.Logger;

import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.regex.Pattern;

@Path("api/sms")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class ApiResource {
    private static final Logger LOG = Logger.getLogger(ApiResource.class);
    private static final Pattern PHONE_PATTERN = Pattern.compile("^\\+?[1-9]\\d{1,14}$");

    @Inject
    @Channel("sms-queue")
    Emitter<SmsMessage> smsEmitter;

    @POST
    @Path("/send")
    public Response sendSms(SmsMessage smsMessage) {
        LOG.infof("Received SMS request for phone: %s", smsMessage.phoneNumber);

        try {
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
}