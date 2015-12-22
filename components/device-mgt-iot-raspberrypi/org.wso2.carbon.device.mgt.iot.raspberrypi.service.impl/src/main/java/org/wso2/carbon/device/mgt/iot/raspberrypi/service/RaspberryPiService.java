/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.device.mgt.iot.raspberrypi.service;

import org.apache.commons.io.FileUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.annotations.api.API;
import org.wso2.carbon.apimgt.annotations.device.DeviceType;
import org.wso2.carbon.apimgt.annotations.device.feature.Feature;
import org.wso2.carbon.apimgt.webapp.publisher.KeyGenerationUtil;
import org.wso2.carbon.device.mgt.common.Device;
import org.wso2.carbon.device.mgt.common.DeviceIdentifier;
import org.wso2.carbon.device.mgt.common.DeviceManagementException;
import org.wso2.carbon.device.mgt.common.EnrolmentInfo;
import org.wso2.carbon.device.mgt.iot.DeviceManagement;
import org.wso2.carbon.device.mgt.iot.DeviceValidator;
import org.wso2.carbon.device.mgt.iot.apimgt.AccessTokenInfo;
import org.wso2.carbon.device.mgt.iot.apimgt.TokenClient;
import org.wso2.carbon.device.mgt.iot.controlqueue.xmpp.XmppAccount;
import org.wso2.carbon.device.mgt.iot.controlqueue.xmpp.XmppConfig;
import org.wso2.carbon.device.mgt.iot.controlqueue.xmpp.XmppServerClient;
import org.wso2.carbon.device.mgt.iot.exception.AccessTokenException;
import org.wso2.carbon.device.mgt.iot.exception.DeviceControllerException;
import org.wso2.carbon.device.mgt.iot.raspberrypi.plugin.constants.RaspberrypiConstants;
import org.wso2.carbon.device.mgt.iot.raspberrypi.service.dto.DeviceJSON;
import org.wso2.carbon.device.mgt.iot.raspberrypi.service.transport.RaspberryPiMQTTSubscriber;
import org.wso2.carbon.device.mgt.iot.raspberrypi.service.util.RaspberrypiServiceUtils;
import org.wso2.carbon.device.mgt.iot.sensormgt.SensorDataManager;
import org.wso2.carbon.device.mgt.iot.sensormgt.SensorRecord;
import org.wso2.carbon.device.mgt.iot.util.ZipArchive;
import org.wso2.carbon.device.mgt.iot.util.ZipUtil;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@API( name="raspberrypi", version="1.0.0", context="/raspberrypi")
@DeviceType( value = "raspberrypi")
public class RaspberryPiService {

    private static Log log = LogFactory.getLog(RaspberryPiService.class);

    //TODO; replace this tenant domain
    private static final String SUPER_TENANT = "carbon.super";

    @Context  //injected response proxy supporting multiple thread
    private HttpServletResponse response;

    public static final String HTTP_PROTOCOL = "HTTP";
    public static final String MQTT_PROTOCOL = "MQTT";

    private ConcurrentHashMap<String, String> deviceToIpMap = new ConcurrentHashMap<>();
    private RaspberryPiMQTTSubscriber raspberryPiMQTTSubscriber;

    /**
     * @param raspberryPiMQTTSubscriber
     */
    public void setRaspberryPiMQTTSubscriber(
            final RaspberryPiMQTTSubscriber raspberryPiMQTTSubscriber) {
        this.raspberryPiMQTTSubscriber = raspberryPiMQTTSubscriber;

        Runnable xmppStarter = new Runnable() {
            @Override
            public void run() {
                raspberryPiMQTTSubscriber.initConnector();
                raspberryPiMQTTSubscriber.connectAndSubscribe();
            }
        };

        Thread xmppStarterThread = new Thread(xmppStarter);
        xmppStarterThread.setDaemon(true);
        xmppStarterThread.start();
    }

    /**
     * @return
     */
    public RaspberryPiMQTTSubscriber getRaspberryPiMQTTSubscriber() {
        return raspberryPiMQTTSubscriber;
    }

    /*	---------------------------------------------------------------------------------------
                                Device management specific APIs
                     Also contains utility methods required for the execution of these APIs
        ---------------------------------------------------------------------------------------	*/
    @Path("manager/device/register")
    @PUT
    public boolean register(@QueryParam("deviceId") String deviceId,
                            @QueryParam("name") String name, @QueryParam("owner") String owner) {

        DeviceManagement deviceManagement = new DeviceManagement(SUPER_TENANT);

        DeviceIdentifier deviceIdentifier = new DeviceIdentifier();
        deviceIdentifier.setId(deviceId);
        deviceIdentifier.setType(RaspberrypiConstants.DEVICE_TYPE);
        try {
            if (deviceManagement.getDeviceManagementService().isEnrolled(deviceIdentifier)) {
                response.setStatus(Response.Status.CONFLICT.getStatusCode());
                return false;
            }

            Device device = new Device();
            device.setDeviceIdentifier(deviceId);
            EnrolmentInfo enrolmentInfo = new EnrolmentInfo();
            enrolmentInfo.setDateOfEnrolment(new Date().getTime());
            enrolmentInfo.setDateOfLastUpdate(new Date().getTime());
            enrolmentInfo.setStatus(EnrolmentInfo.Status.ACTIVE);
            enrolmentInfo.setOwnership(EnrolmentInfo.OwnerShip.BYOD);
            device.setName(name);
            device.setType(RaspberrypiConstants.DEVICE_TYPE);
            enrolmentInfo.setOwner(owner);
            device.setEnrolmentInfo(enrolmentInfo);
            boolean added = deviceManagement.getDeviceManagementService().enrollDevice(device);
            if (added) {
                response.setStatus(Response.Status.OK.getStatusCode());
            } else {
                response.setStatus(Response.Status.NOT_ACCEPTABLE.getStatusCode());
            }

            return added;
        } catch (DeviceManagementException e) {
            response.setStatus(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
            return false;
        } finally {
            deviceManagement.endTenantFlow();
        }
    }

    @Path("manager/device/remove/{device_id}")
    @DELETE
    public void removeDevice(@PathParam("device_id") String deviceId,
                             @Context HttpServletResponse response) {

        DeviceManagement deviceManagement = new DeviceManagement(SUPER_TENANT);
        DeviceIdentifier deviceIdentifier = new DeviceIdentifier();
        deviceIdentifier.setId(deviceId);
        deviceIdentifier.setType(RaspberrypiConstants.DEVICE_TYPE);
        try {
            boolean removed = deviceManagement.getDeviceManagementService().disenrollDevice(
                    deviceIdentifier);
            if (removed) {
                response.setStatus(Response.Status.OK.getStatusCode());
            } else {
                response.setStatus(Response.Status.NOT_ACCEPTABLE.getStatusCode());
            }
        } catch (DeviceManagementException e) {
            response.setStatus(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
        } finally {
            deviceManagement.endTenantFlow();
        }


    }

    @Path("manager/device/update/{device_id}")
    @POST
    public boolean updateDevice(@PathParam("device_id") String deviceId,
                                @QueryParam("name") String name,
                                @Context HttpServletResponse response) {

        DeviceManagement deviceManagement = new DeviceManagement(SUPER_TENANT);

        DeviceIdentifier deviceIdentifier = new DeviceIdentifier();
        deviceIdentifier.setId(deviceId);
        deviceIdentifier.setType(RaspberrypiConstants.DEVICE_TYPE);
        try {
            Device device = deviceManagement.getDeviceManagementService().getDevice(deviceIdentifier);
            device.setDeviceIdentifier(deviceId);

            // device.setDeviceTypeId(deviceTypeId);
            device.getEnrolmentInfo().setDateOfLastUpdate(new Date().getTime());

            device.setName(name);
            device.setType(RaspberrypiConstants.DEVICE_TYPE);

            boolean updated = deviceManagement.getDeviceManagementService().modifyEnrollment(device);

            if (updated) {
                response.setStatus(Response.Status.OK.getStatusCode());

            } else {
                response.setStatus(Response.Status.NOT_ACCEPTABLE.getStatusCode());
            }
            return updated;
        } catch (DeviceManagementException e) {
            log.error(e.getErrorMessage());
            return false;
        } finally {
            deviceManagement.endTenantFlow();
        }

    }

    @Path("manager/device/{device_id}")
    @GET
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Device getDevice(@PathParam("device_id") String deviceId) {

        DeviceManagement deviceManagement = new DeviceManagement(SUPER_TENANT);
        DeviceIdentifier deviceIdentifier = new DeviceIdentifier();
        deviceIdentifier.setId(deviceId);
        deviceIdentifier.setType(RaspberrypiConstants.DEVICE_TYPE);

        try {
            return deviceManagement.getDeviceManagementService().getDevice(deviceIdentifier);
        } catch (DeviceManagementException ex) {
            log.error("Error occurred while retrieving device with Id " + deviceId + "\n" + ex);
            return null;
        } finally {
            deviceManagement.endTenantFlow();
        }

    }

    @Path("manager/devices/{username}")
    @GET
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Device[] getRaspberrypiDevices(@PathParam("username") String username) {

        DeviceManagement deviceManagement = new DeviceManagement(SUPER_TENANT);

        try {
            List<Device> userDevices = deviceManagement.getDeviceManagementService().getDevicesOfUser(username);
            ArrayList<Device> usersRaspberrypiDevices = new ArrayList<>();
            for (Device device : userDevices) {
                if (device.getType().equals(RaspberrypiConstants.DEVICE_TYPE) &&
                        device.getEnrolmentInfo().getStatus().equals(EnrolmentInfo.Status.ACTIVE)) {
                    usersRaspberrypiDevices.add(device);
                }
            }
            return usersRaspberrypiDevices.toArray(new Device[]{});

        } catch (DeviceManagementException e) {
            response.setStatus(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
            return null;
        } finally {
            deviceManagement.endTenantFlow();
        }
    }

    @Path("manager/device/{sketch_type}/download")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response downloadSketch(@QueryParam("owner") String owner,
                                   @QueryParam("deviceName") String customDeviceName,
                                   @PathParam("sketch_type") String
                                           sketchType) {

        try {
            ZipArchive zipFile = createDownloadFile(owner, customDeviceName, sketchType);
            Response.ResponseBuilder response = Response.ok(FileUtils.readFileToByteArray(zipFile.getZipFile()));
            response.type("application/zip");
            response.header("Content-Disposition", "attachment; filename=\"" + zipFile.getFileName() + "\"");
            return response.build();

        } catch (IllegalArgumentException ex) {
            return Response.status(400).entity(ex.getMessage()).build();//bad request
        } catch (DeviceManagementException ex) {
            return Response.status(500).entity(ex.getMessage()).build();
        } catch (AccessTokenException ex) {
            return Response.status(500).entity(ex.getMessage()).build();
        } catch (DeviceControllerException ex) {
            return Response.status(500).entity(ex.getMessage()).build();
        } catch (IOException ex) {
            return Response.status(500).entity(ex.getMessage()).build();
        }
    }


    @Path("manager/device/{sketch_type}/generate_link")
    @GET
    public Response generateSketchLink(@QueryParam("owner") String owner,
                                       @QueryParam("deviceName") String customDeviceName,
                                       @PathParam("sketch_type") String sketchType) {

        try {
            ZipArchive zipFile = createDownloadFile(owner, customDeviceName, sketchType);
            Response.ResponseBuilder rb = Response.ok(zipFile.getDeviceId());
            return rb.build();
        } catch (IllegalArgumentException ex) {
            return Response.status(400).entity(ex.getMessage()).build();//bad request
        } catch (DeviceManagementException ex) {
            return Response.status(500).entity(ex.getMessage()).build();
        } catch (AccessTokenException ex) {
            return Response.status(500).entity(ex.getMessage()).build();
        } catch (DeviceControllerException ex) {
            return Response.status(500).entity(ex.getMessage()).build();
        }
    }


    private ZipArchive createDownloadFile(String owner, String customDeviceName, String sketchType)
            throws DeviceManagementException, AccessTokenException, DeviceControllerException {
        if (owner == null) {
            throw new IllegalArgumentException("Error on createDownloadFile() Owner is null!");
        }

        //create new device id
        String deviceId = shortUUID();

        KeyGenerationUtil.createApplicationKeys("raspberry");

        TokenClient accessTokenClient = new TokenClient(RaspberrypiConstants.DEVICE_TYPE);
        AccessTokenInfo accessTokenInfo = accessTokenClient.getAccessToken(owner, deviceId);

        //create token
        String accessToken = accessTokenInfo.getAccess_token();
        String refreshToken = accessTokenInfo.getRefresh_token();
        //adding registering data

        XmppAccount newXmppAccount = new XmppAccount();
        newXmppAccount.setAccountName(owner + "_" + deviceId);
        newXmppAccount.setUsername(deviceId);
        newXmppAccount.setPassword(accessToken);
        newXmppAccount.setEmail(deviceId + "@wso2.com");

        XmppServerClient xmppServerClient = new XmppServerClient();
        xmppServerClient.initControlQueue();
        boolean status;

        if (XmppConfig.getInstance().isEnabled()) {
            status = xmppServerClient.createXMPPAccount(newXmppAccount);
            if (!status) {
                String msg =
                        "XMPP Account was not created for device - " + deviceId + " of owner - " + owner +
                                ".XMPP might have been disabled in org.wso2.carbon.device.mgt.iot.common.config" +
                                ".server.configs";
                log.warn(msg);
                throw new DeviceManagementException(msg);
            }
        }

        //Register the device with CDMF
        String deviceName = customDeviceName + "_" + deviceId;
        status = register(deviceId, deviceName, owner);

        if (!status) {
            String msg = "Error occurred while registering the device with " + "id: " + deviceId + " owner:" + owner;
            throw new DeviceManagementException(msg);
        }

        ZipUtil ziputil = new ZipUtil();
        ZipArchive zipFile = ziputil.createZipFile(owner, SUPER_TENANT, sketchType, deviceId, deviceName, accessToken,
                                                   refreshToken);
        zipFile.setDeviceId(deviceId);
        return zipFile;
    }

    private static String shortUUID() {
        UUID uuid = UUID.randomUUID();
        long l = ByteBuffer.wrap(uuid.toString().getBytes(StandardCharsets.UTF_8)).getLong();
        return Long.toString(l, Character.MAX_RADIX);
    }

    /*	---------------------------------------------------------------------------------------
                    Device specific APIs - Control APIs + Data-Publishing APIs
        ---------------------------------------------------------------------------------------	*/

    @Path("controller/register/{owner}/{deviceId}/{ip}/{port}")
    @POST
    public String registerDeviceIP(@PathParam("owner") String owner,
                                   @PathParam("deviceId") String deviceId,
                                   @PathParam("ip") String deviceIP,
                                   @PathParam("port") String devicePort,
                                   @Context HttpServletResponse response,
                                   @Context HttpServletRequest request) {

        //TODO:: Need to get IP from the request itself
        String result;

        if (log.isDebugEnabled()) {
            log.debug("Got register call from IP: " + deviceIP + " for Device ID: " + deviceId + " of owner: " + owner);
        }

        String deviceHttpEndpoint = deviceIP + ":" + devicePort;
        deviceToIpMap.put(deviceId, deviceHttpEndpoint);

        result = "Device-IP Registered";
        response.setStatus(Response.Status.OK.getStatusCode());

        if (log.isDebugEnabled()) {
            log.debug(result);
        }

        return result;
    }


    /**
     * @param owner
     * @param deviceId
     * @param protocol
     * @param state
     * @param response
     */
    @Path("controller/bulb")
    @POST
    @Feature( code="bulb", name="Bulb On / Off", type="operation",
            description="Switch on/off Raspberry Pi agent's bulb. (On / Off)")
    public void switchBulb(@HeaderParam("owner") String owner, @HeaderParam("deviceId") String deviceId,
                           @HeaderParam("protocol") String protocol, @FormParam("state") String state,
                           @Context HttpServletResponse response) {

        try {
            DeviceValidator deviceValidator = new DeviceValidator();
            if (!deviceValidator.isExist(owner, SUPER_TENANT, new DeviceIdentifier(deviceId,
                                                                                   RaspberrypiConstants.DEVICE_TYPE))) {
                response.setStatus(Response.Status.UNAUTHORIZED.getStatusCode());
                return;
            }
        } catch (DeviceManagementException e) {
            log.error("DeviceValidation Failed for deviceId: " + deviceId + " of user: " + owner);
            response.setStatus(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
            return;
        }

        String switchToState = state.toUpperCase();

        if (!switchToState.equals(RaspberrypiConstants.STATE_ON) && !switchToState.equals(
                RaspberrypiConstants.STATE_OFF)) {
            log.error("The requested state change shoud be either - 'ON' or 'OFF'");
            response.setStatus(Response.Status.BAD_REQUEST.getStatusCode());
            return;
        }

        String protocolString = protocol.toUpperCase();
        String callUrlPattern = RaspberrypiConstants.BULB_CONTEXT + switchToState;

        if (log.isDebugEnabled()) {
            log.debug("Sending request to switch-bulb of device [" + deviceId + "] via " + protocolString);
        }

        try {
            switch (protocolString) {
                case HTTP_PROTOCOL:
                    String deviceHTTPEndpoint = deviceToIpMap.get(deviceId);
                    if (deviceHTTPEndpoint == null) {
                        response.setStatus(Response.Status.PRECONDITION_FAILED.getStatusCode());
                        return;
                    }

                    RaspberrypiServiceUtils.sendCommandViaHTTP(deviceHTTPEndpoint, callUrlPattern, true);
                    break;

                case MQTT_PROTOCOL:
                    String mqttMessage = RaspberrypiConstants.BULB_CONTEXT.replace("/", "");
                    RaspberrypiServiceUtils.sendCommandViaMQTT(owner, deviceId, mqttMessage, switchToState);
                    break;

                default:
                    response.setStatus(Response.Status.NOT_ACCEPTABLE.getStatusCode());
                    return;
            }
        } catch (DeviceManagementException e) {
            log.error("Failed to send switch-bulb request to device [" + deviceId + "] via " + protocolString);
            response.setStatus(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
            return;
        }

        response.setStatus(Response.Status.OK.getStatusCode());
    }


    /**
     * @param owner
     * @param deviceId
     * @param protocol
     * @param response
     * @return
     */
    @Path("controller/readtemperature")
    @GET
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Feature( code="readtemperature", name="Temperature", type="monitor",
            description="Request temperature reading from Raspberry Pi agent")
    public SensorRecord requestTemperature(@HeaderParam("owner") String owner,
                                           @HeaderParam("deviceId") String deviceId,
                                           @HeaderParam("protocol") String protocol,
                                           @Context HttpServletResponse response) {
        SensorRecord sensorRecord = null;

        DeviceValidator deviceValidator = new DeviceValidator();
        try {
            if (!deviceValidator.isExist(owner, SUPER_TENANT, new DeviceIdentifier(deviceId,
                                                                                   RaspberrypiConstants.DEVICE_TYPE))) {
                response.setStatus(Response.Status.UNAUTHORIZED.getStatusCode());
            }
        } catch (DeviceManagementException e) {
            response.setStatus(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
        }

        String protocolString = protocol.toUpperCase();

        if (log.isDebugEnabled()) {
            log.debug(
                    "Sending request to read raspberrypi-temperature of device [" + deviceId + "] via " +
                            protocolString);
        }

        try {
            switch (protocolString) {
                case HTTP_PROTOCOL:
                    String deviceHTTPEndpoint = deviceToIpMap.get(deviceId);
                    if (deviceHTTPEndpoint == null) {
                        response.setStatus(Response.Status.PRECONDITION_FAILED.getStatusCode());
                    }

                    String temperatureValue = RaspberrypiServiceUtils.sendCommandViaHTTP(deviceHTTPEndpoint,
                                                                                         RaspberrypiConstants
                                                                                                 .TEMPERATURE_CONTEXT,
                                                                                         false);
                    SensorDataManager.getInstance().setSensorRecord(deviceId, RaspberrypiConstants.SENSOR_TEMPERATURE,
                                                                    temperatureValue,
                                                                    Calendar.getInstance().getTimeInMillis());
                    break;

                case MQTT_PROTOCOL:
                    String mqttMessage = RaspberrypiConstants.BULB_CONTEXT.replace("/", "");
                    RaspberrypiServiceUtils.sendCommandViaMQTT(owner, deviceId, mqttMessage, "");
                    break;
                default:
                    response.setStatus(Response.Status.NOT_ACCEPTABLE.getStatusCode());
            }
            sensorRecord = SensorDataManager.getInstance().getSensorRecord(deviceId,
                                                                           RaspberrypiConstants.SENSOR_TEMPERATURE);
        } catch (DeviceManagementException | DeviceControllerException e) {
            response.setStatus(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
        }

        response.setStatus(Response.Status.OK.getStatusCode());
        return sensorRecord;
    }

    /**
     * @param dataMsg
     * @param response
     */
    @Path("controller/push_temperature")
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public void pushTemperatureData(final DeviceJSON dataMsg,
                                    @Context HttpServletResponse response,
                                    @Context HttpServletRequest request) {
        String owner = dataMsg.owner;
        String deviceId = dataMsg.deviceId;
        String deviceIp = dataMsg.reply;            //TODO:: Get IP from request
        float temperature = dataMsg.value;

        try {
            DeviceValidator deviceValidator = new DeviceValidator();
            if (!deviceValidator.isExist(owner, SUPER_TENANT, new DeviceIdentifier(deviceId,
                                                                                   RaspberrypiConstants.DEVICE_TYPE))) {
                response.setStatus(Response.Status.UNAUTHORIZED.getStatusCode());
                log.warn("Temperature data Received from unregistered raspberrypi device [" + deviceId +
                                 "] for owner [" + owner + "]");
                return;
            }

            String registeredIp = deviceToIpMap.get(deviceId);

            if (registeredIp == null) {
                log.warn("Unregistered IP: Temperature Data Received from an un-registered IP " + deviceIp +
                                 " for device ID - " + deviceId);
                response.setStatus(Response.Status.PRECONDITION_FAILED.getStatusCode());
                return;
            } else if (!registeredIp.equals(deviceIp)) {
                log.warn("Conflicting IP: Received IP is " + deviceIp + ". Device with ID " + deviceId +
                                 " is already registered under some other IP. Re-registration required");
                response.setStatus(Response.Status.CONFLICT.getStatusCode());
                return;
            }

            if (log.isDebugEnabled()) {
                log.debug("Received Pin Data Value: " + temperature + " degrees C");
            }
            SensorDataManager.getInstance().setSensorRecord(deviceId, RaspberrypiConstants.SENSOR_TEMPERATURE,
                                                            String.valueOf(temperature),
                                                            Calendar.getInstance().getTimeInMillis());

            if (!RaspberrypiServiceUtils.publishToDAS(dataMsg.owner, dataMsg.deviceId, dataMsg.value)) {
                response.setStatus(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
                log.warn("An error occured whilst trying to publish temperature data of raspberrypi with ID [" +
                                 deviceId + "] of owner [" + owner + "]");
            }

        } catch (DeviceManagementException e) {
            String errorMsg = "Validation attempt for deviceId [" + deviceId + "] of owner [" + owner + "] failed.\n";
            log.error(errorMsg + Response.Status.INTERNAL_SERVER_ERROR.getReasonPhrase() + "\n" + e.getErrorMessage());
        }
    }

}
