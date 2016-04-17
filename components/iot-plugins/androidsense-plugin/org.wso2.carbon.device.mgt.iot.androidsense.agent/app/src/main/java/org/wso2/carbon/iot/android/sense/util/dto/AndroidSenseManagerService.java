package org.wso2.carbon.iot.android.sense.util.dto;

import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;

public interface AndroidSenseManagerService {

    @Path("devices/{device_id}")
    @POST
    boolean register(@PathParam("device_id") String deviceId, @QueryParam("deviceName") String deviceName);
}
