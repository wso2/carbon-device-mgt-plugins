package org.wso2.carbon.iot.android.sense.util.dto;

import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

@Path("/token")
public interface TokenIssuerService {

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    AccessTokenInfo getToken(@QueryParam("grant_type") String grant, @QueryParam("username") String username,
            @QueryParam("password") String password);
}
