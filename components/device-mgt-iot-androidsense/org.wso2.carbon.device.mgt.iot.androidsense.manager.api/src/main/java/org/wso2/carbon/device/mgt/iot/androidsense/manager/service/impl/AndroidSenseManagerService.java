/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.device.mgt.iot.androidsense.manager.service.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.annotations.api.API;
import org.wso2.carbon.apimgt.webapp.publisher.KeyGenerationUtil;
import org.wso2.carbon.device.mgt.common.Device;
import org.wso2.carbon.device.mgt.common.DeviceIdentifier;
import org.wso2.carbon.device.mgt.common.DeviceManagementException;
import org.wso2.carbon.device.mgt.common.EnrolmentInfo;
import org.wso2.carbon.device.mgt.iot.androidsense.manager.service.impl.util.APIUtil;
import org.wso2.carbon.device.mgt.iot.androidsense.plugin.constants.AndroidSenseConstants;
import org.wso2.carbon.device.mgt.iot.apimgt.AccessTokenInfo;
import org.wso2.carbon.device.mgt.iot.apimgt.TokenClient;
import org.wso2.carbon.device.mgt.iot.exception.AccessTokenException;
import org.wso2.carbon.utils.CarbonUtils;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import java.io.File;
import java.util.Date;

@API( name="android_sense_mgt", version="1.0.0", context="/android_sense_mgt")
public class AndroidSenseManagerService {

    private static Log log = LogFactory.getLog(AndroidSenseManagerService.class);

    @Context  //injected response proxy supporting multiple thread
    private HttpServletResponse response;

    @Path("manager/device")
    @POST
    public boolean register(@FormParam("deviceId") String deviceId,
                            @FormParam("deviceName") String deviceName) {
        DeviceIdentifier deviceIdentifier = new DeviceIdentifier();
        deviceIdentifier.setId(deviceId);
        deviceIdentifier.setType(AndroidSenseConstants.DEVICE_TYPE);
        try {
            if (APIUtil.getDeviceManagementService().isEnrolled(deviceIdentifier)) {
                response.setStatus(Response.Status.CONFLICT.getStatusCode());
                getTokens(APIUtil.getAuthenticatedUser(), deviceId, response);
                return true;
            }
            KeyGenerationUtil.createApplicationKeys("android_sense");
            getTokens(APIUtil.getAuthenticatedUser(), deviceId, response);
            Device device = new Device();
            device.setDeviceIdentifier(deviceId);
            EnrolmentInfo enrolmentInfo = new EnrolmentInfo();
            enrolmentInfo.setDateOfEnrolment(new Date().getTime());
            enrolmentInfo.setDateOfLastUpdate(new Date().getTime());
            enrolmentInfo.setStatus(EnrolmentInfo.Status.ACTIVE);
            enrolmentInfo.setOwnership(EnrolmentInfo.OwnerShip.BYOD);
            device.setName(deviceName);
            device.setType(AndroidSenseConstants.DEVICE_TYPE);
            enrolmentInfo.setOwner(APIUtil.getAuthenticatedUser());
            device.setEnrolmentInfo(enrolmentInfo);
            boolean added = APIUtil.getDeviceManagementService().enrollDevice(device);
            if (added) {
                response.setStatus(Response.Status.OK.getStatusCode());
            } else {
                response.setStatus(Response.Status.NOT_ACCEPTABLE.getStatusCode());
            }
            return added;
        } catch (DeviceManagementException e) {
            response.setStatus(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
            return false;
        } catch (AccessTokenException e) {
            response.setStatus(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
            return false;
        }
    }

    @Path("manager/device/{device_id}")
    @DELETE
    public void removeDevice(@PathParam("device_id") String deviceId,
                             @Context HttpServletResponse response) {

        DeviceIdentifier deviceIdentifier = new DeviceIdentifier();
        deviceIdentifier.setId(deviceId);
        deviceIdentifier.setType(AndroidSenseConstants.DEVICE_TYPE);
        try {
            boolean removed = APIUtil.getDeviceManagementService().disenrollDevice(deviceIdentifier);
            if (removed) {
                response.setStatus(Response.Status.OK.getStatusCode());

            } else {
                response.setStatus(Response.Status.NOT_ACCEPTABLE.getStatusCode());

            }
        } catch (DeviceManagementException e) {
            response.setStatus(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
        }
    }

    @Path("manager/device/{device_id}")
    @PUT
    public boolean updateDevice(
            @PathParam("device_id") String deviceId, @QueryParam("name") String name,
            @Context HttpServletResponse response) {
        DeviceIdentifier deviceIdentifier = new DeviceIdentifier();
        deviceIdentifier.setId(deviceId);
        deviceIdentifier.setType(AndroidSenseConstants.DEVICE_TYPE);
        try {
            Device device = APIUtil.getDeviceManagementService().getDevice(deviceIdentifier);
            device.setDeviceIdentifier(deviceId);
            device.getEnrolmentInfo().setDateOfLastUpdate(new Date().getTime());
            device.setName(name);
            device.setType(AndroidSenseConstants.DEVICE_TYPE);
            boolean updated = APIUtil.getDeviceManagementService().modifyEnrollment(device);
            if (updated) {
                response.setStatus(Response.Status.OK.getStatusCode());
            } else {
                response.setStatus(Response.Status.NOT_ACCEPTABLE.getStatusCode());
            }
            return updated;
        } catch (DeviceManagementException e) {
            response.setStatus(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
            return false;
        }
    }

    @Path("manager/device/{device_id}")
    @GET
    @Consumes("application/json")
    @Produces("application/json")
    public Device getDevice(
            @PathParam("device_id") String deviceId) {
        DeviceIdentifier deviceIdentifier = new DeviceIdentifier();
        deviceIdentifier.setId(deviceId);
        deviceIdentifier.setType(AndroidSenseConstants.DEVICE_TYPE);
        try {
            return APIUtil.getDeviceManagementService().getDevice(deviceIdentifier);
        } catch (DeviceManagementException e) {
            response.setStatus(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
            return null;
        }
    }

    @Path("manager/device/{sketch_type}/download")
    @GET
    @Produces("application/octet-stream")
    public Response downloadSketch(@PathParam("sketch_type") String sketchType) {
        try {
            String sep = File.separator;
            String sketchFolder = "repository" + sep + "resources" + sep + "sketches" + sep + "android_sense" + sep;
            String archivesPath = CarbonUtils.getCarbonHome() + sep + sketchFolder;
            Response.ResponseBuilder rb = Response.ok(new File(archivesPath+sep+"androidsense.apk"));
            rb.header("Content-Disposition", "attachment; filename=\"" + "androidsense.apk" + "\"");
            return rb.build();
        } catch (IllegalArgumentException ex) {
            return Response.status(400).entity(ex.getMessage()).build();//bad request
        }

    }

    public void getTokens(String owner, String deviceId, HttpServletResponse response)
            throws AccessTokenException {
        TokenClient accessTokenClient = new TokenClient(AndroidSenseConstants.DEVICE_TYPE);
        AccessTokenInfo accessTokenInfo = accessTokenClient.getAccessToken(owner, deviceId);

        String accessToken = accessTokenInfo.getAccess_token();
        String refreshToken = accessTokenInfo.getRefresh_token();

        response.addHeader("access", accessToken);
        response.addHeader("refresh", refreshToken);
    }
}
