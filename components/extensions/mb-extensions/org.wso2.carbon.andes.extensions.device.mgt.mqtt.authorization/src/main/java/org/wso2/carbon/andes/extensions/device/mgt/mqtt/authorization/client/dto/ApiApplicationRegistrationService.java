package org.wso2.carbon.andes.extensions.device.mgt.mqtt.authorization.client.dto;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

/**
 * This is the application registration service that exposed for apimApplicationRegistration
 */

@Path("/register")
public interface ApiApplicationRegistrationService {

    /**
     * This method is used to register api application
     *
     * @param registrationProfile contains the necessary attributes that are needed in order to register an app.
     */
    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    ApiApplicationKey register(ApiRegistrationProfile registrationProfile);
}
