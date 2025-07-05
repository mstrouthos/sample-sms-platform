// REST Client for callback endpoint
package com.mstrouthos.client;

import com.mstrouthos.dto.SmsCallback;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

import jakarta.ws.rs.*;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.concurrent.CompletionStage;

@RegisterRestClient(configKey = "sms-callback-api")
@Path("/api/sms")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public interface SmsCallbackClient {
    @POST
    @Path("/callback")
    @Consumes(MediaType.APPLICATION_JSON)
    CompletionStage<Response> sendCallback(SmsCallback callback);
}