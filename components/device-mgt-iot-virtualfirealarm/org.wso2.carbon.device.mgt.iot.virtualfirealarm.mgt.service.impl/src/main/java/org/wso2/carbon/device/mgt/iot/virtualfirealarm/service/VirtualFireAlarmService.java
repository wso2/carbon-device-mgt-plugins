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
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.device.mgt.iot.virtualfirealarm.service;

import org.apache.commons.io.FileUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.annotations.api.API;
import org.wso2.carbon.apimgt.annotations.device.DeviceType;
import org.wso2.carbon.apimgt.annotations.device.feature.Feature;
import org.wso2.carbon.apimgt.webapp.publisher.KeyGenerationUtil;
import org.wso2.carbon.certificate.mgt.core.dto.SCEPResponse;
import org.wso2.carbon.certificate.mgt.core.exception.KeystoreException;
import org.wso2.carbon.certificate.mgt.core.service.CertificateManagementService;
import org.wso2.carbon.device.mgt.common.Device;
import org.wso2.carbon.device.mgt.common.DeviceIdentifier;
import org.wso2.carbon.device.mgt.common.DeviceManagementException;
import org.wso2.carbon.device.mgt.common.EnrolmentInfo;
import org.wso2.carbon.device.mgt.iot.DeviceManagement;
import org.wso2.carbon.device.mgt.iot.DeviceValidator;
import org.wso2.carbon.device.mgt.iot.apimgt.AccessTokenInfo;
import org.wso2.carbon.device.mgt.iot.apimgt.TokenClient;
import org.wso2.carbon.device.mgt.iot.controlqueue.mqtt.MqttConfig;
import org.wso2.carbon.device.mgt.iot.controlqueue.xmpp.XmppAccount;
import org.wso2.carbon.device.mgt.iot.controlqueue.xmpp.XmppConfig;
import org.wso2.carbon.device.mgt.iot.controlqueue.xmpp.XmppServerClient;
import org.wso2.carbon.device.mgt.iot.exception.AccessTokenException;
import org.wso2.carbon.device.mgt.iot.exception.DeviceControllerException;
import org.wso2.carbon.device.mgt.iot.sensormgt.SensorDataManager;
import org.wso2.carbon.device.mgt.iot.sensormgt.SensorRecord;
import org.wso2.carbon.device.mgt.iot.transport.TransportHandlerException;
import org.wso2.carbon.device.mgt.iot.util.ZipArchive;
import org.wso2.carbon.device.mgt.iot.util.ZipUtil;
import org.wso2.carbon.device.mgt.iot.virtualfirealarm.plugin.constants.VirtualFireAlarmConstants;
import org.wso2.carbon.device.mgt.iot.virtualfirealarm.service.dto.DeviceJSON;
import org.wso2.carbon.device.mgt.iot.virtualfirealarm.service.exception.VirtualFireAlarmException;
import org.wso2.carbon.device.mgt.iot.virtualfirealarm.service.transport.VirtualFireAlarmMQTTConnector;
import org.wso2.carbon.device.mgt.iot.virtualfirealarm.service.transport.VirtualFireAlarmXMPPConnector;
import org.wso2.carbon.device.mgt.iot.virtualfirealarm.service.util.VerificationManager;
import org.wso2.carbon.device.mgt.iot.virtualfirealarm.service.util.VirtualFireAlarmServiceUtils;
import org.wso2.carbon.device.mgt.iot.virtualfirealarm.service.util.scep.ContentType;
import org.wso2.carbon.device.mgt.iot.virtualfirealarm.service.util.scep.SCEPOperation;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@API(name = "virtual_firealarm", version = "1.0.0", context = "/virtual_firealarm")
public class VirtualFireAlarmService {

    private static Log log = LogFactory.getLog(VirtualFireAlarmService.class);

    //TODO; replace this tenant domain
    private static final String SUPER_TENANT = "carbon.super";

    @Context  //injected response proxy supporting multiple thread
    private HttpServletResponse response;

    private VerificationManager verificationManager;
    private VirtualFireAlarmMQTTConnector virtualFireAlarmMQTTConnector;
    private VirtualFireAlarmXMPPConnector virtualFireAlarmXMPPConnector;

    /**
     * @param verificationManager
     */
    public void setVerificationManager(
            VerificationManager verificationManager) {
        this.verificationManager = verificationManager;
        verificationManager.initVerificationManager();
    }

    /**
     * @param virtualFireAlarmXMPPConnector
     */
    public void setVirtualFireAlarmXMPPConnector(
            final VirtualFireAlarmXMPPConnector virtualFireAlarmXMPPConnector) {
        this.virtualFireAlarmXMPPConnector = virtualFireAlarmXMPPConnector;

        if (MqttConfig.getInstance().isEnabled()) {
            Runnable mqttStarter = new Runnable() {
                @Override
                public void run() {
                    virtualFireAlarmXMPPConnector.initConnector();
                    virtualFireAlarmXMPPConnector.connect();
                }
            };

            Thread mqttStarterThread = new Thread(mqttStarter);
            mqttStarterThread.setDaemon(true);
            mqttStarterThread.start();
        } else {
            log.warn("MQTT disabled in 'devicemgt-config.xml'. Hence, VirtualFireAlarmMQTTConnector not started.");
        }
    }

    /**
     * @param virtualFireAlarmMQTTConnector
     */
    public void setVirtualFireAlarmMQTTConnector(
            final VirtualFireAlarmMQTTConnector virtualFireAlarmMQTTConnector) {
        this.virtualFireAlarmMQTTConnector = virtualFireAlarmMQTTConnector;
        if (XmppConfig.getInstance().isEnabled()) {
            virtualFireAlarmMQTTConnector.connect();
        } else {
            log.warn("XMPP disabled in 'devicemgt-config.xml'. Hence, VirtualFireAlarmXMPPConnector not started.");
        }
    }

    /**
     * @return
     */
    public VerificationManager getVerificationManager() {
        return verificationManager;
    }

    /**
     * @return
     */
    public VirtualFireAlarmXMPPConnector getVirtualFireAlarmXMPPConnector() {
        return virtualFireAlarmXMPPConnector;
    }

    /**
     * @return
     */
    public VirtualFireAlarmMQTTConnector getVirtualFireAlarmMQTTConnector() {
        return virtualFireAlarmMQTTConnector;
    }

    /*	---------------------------------------------------------------------------------------
                                Device management specific APIs
                     Also contains utility methods required for the execution of these APIs
        ---------------------------------------------------------------------------------------	*/

    /**
     * @param deviceId
     * @param name
     * @param owner
     * @return
     */
    @Path("manager/device/register")
    @PUT
    public boolean register(@QueryParam("deviceId") String deviceId,
                            @QueryParam("name") String name, @QueryParam("owner") String owner) {

        DeviceManagement deviceManagement = new DeviceManagement(SUPER_TENANT);

        DeviceIdentifier deviceIdentifier = new DeviceIdentifier();
        deviceIdentifier.setId(deviceId);
        deviceIdentifier.setType(VirtualFireAlarmConstants.DEVICE_TYPE);
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
            device.setType(VirtualFireAlarmConstants.DEVICE_TYPE);
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

    /**
     * @param deviceId
     * @param response
     */
    @Path("manager/device/remove/{device_id}")
    @DELETE
    public void removeDevice(@PathParam("device_id") String deviceId, @Context HttpServletResponse response) {

        DeviceManagement deviceManagement = new DeviceManagement(SUPER_TENANT);
        DeviceIdentifier deviceIdentifier = new DeviceIdentifier();
        deviceIdentifier.setId(deviceId);
        deviceIdentifier.setType(VirtualFireAlarmConstants.DEVICE_TYPE);
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

    /**
     * @param deviceId
     * @param name
     * @param response
     * @return
     */
    @Path("manager/device/update/{device_id}")
    @POST
    public boolean updateDevice(@PathParam("device_id") String deviceId,
                                @QueryParam("name") String name,
                                @Context HttpServletResponse response) {

        DeviceManagement deviceManagement = new DeviceManagement(SUPER_TENANT);

        DeviceIdentifier deviceIdentifier = new DeviceIdentifier();
        deviceIdentifier.setId(deviceId);
        deviceIdentifier.setType(VirtualFireAlarmConstants.DEVICE_TYPE);
        try {
            Device device = deviceManagement.getDeviceManagementService().getDevice(deviceIdentifier);
            device.setDeviceIdentifier(deviceId);

            // device.setDeviceTypeId(deviceTypeId);
            device.getEnrolmentInfo().setDateOfLastUpdate(new Date().getTime());

            device.setName(name);
            device.setType(VirtualFireAlarmConstants.DEVICE_TYPE);

            boolean updated = deviceManagement.getDeviceManagementService().modifyEnrollment(device);

            if (updated) {
                response.setStatus(Response.Status.OK.getStatusCode());
            } else {
                response.setStatus(Response.Status.NOT_ACCEPTABLE.getStatusCode());
            }
            return updated;
        } catch (DeviceManagementException e) {
            response.setStatus(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
            return false;
        } finally {
            deviceManagement.endTenantFlow();
        }

    }

    /**
     * @param deviceId
     * @return
     */
    @Path("manager/device/{device_id}")
    @GET
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Device getDevice(@PathParam("device_id") String deviceId) {

        DeviceManagement deviceManagement = new DeviceManagement(SUPER_TENANT);
        DeviceIdentifier deviceIdentifier = new DeviceIdentifier();
        deviceIdentifier.setId(deviceId);
        deviceIdentifier.setType(VirtualFireAlarmConstants.DEVICE_TYPE);

        try {
            return deviceManagement.getDeviceManagementService().getDevice(deviceIdentifier);

        } catch (DeviceManagementException e) {
            response.setStatus(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
            return null;
        } finally {
            deviceManagement.endTenantFlow();
        }

    }

    /**
     * @param username
     * @return
     */
    @Path("manager/devices/{username}")
    @GET
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Device[] getFirealarmDevices(@PathParam("username") String username) {

        DeviceManagement deviceManagement = new DeviceManagement(SUPER_TENANT);

        try {
            List<Device> userDevices =
                    deviceManagement.getDeviceManagementService().getDevicesOfUser(
                            username);
            ArrayList<Device> userDevicesforFirealarm = new ArrayList<>();
            for (Device device : userDevices) {

                if (device.getType().equals(VirtualFireAlarmConstants.DEVICE_TYPE) &&
                        device.getEnrolmentInfo().getStatus().equals(
                                EnrolmentInfo.Status.ACTIVE)) {
                    userDevicesforFirealarm.add(device);

                }
            }

            return userDevicesforFirealarm.toArray(new Device[]{});
        } catch (DeviceManagementException e) {
            response.setStatus(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
            return null;
        } finally {
            deviceManagement.endTenantFlow();
        }
    }

    /**
     * @param owner
     * @param customDeviceName
     * @param sketchType
     * @return
     */
    //TODO:: Needs to go to "common.war" cz all the devices have this
    @Path("manager/device/{sketch_type}/download")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response downloadSketch(@QueryParam("owner") String owner,
                                   @QueryParam("deviceName") String customDeviceName,
                                   @PathParam("sketch_type") String sketchType) {
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

    /**
     * @param owner
     * @param customDeviceName
     * @param sketchType
     * @return
     */
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

    /**
     * @param owner
     * @param customDeviceName
     * @param sketchType
     * @return
     * @throws DeviceManagementException
     * @throws AccessTokenException
     * @throws DeviceControllerException
     */
    private ZipArchive createDownloadFile(String owner, String customDeviceName, String sketchType)
            throws DeviceManagementException, AccessTokenException, DeviceControllerException {
        if (owner == null) {
            throw new IllegalArgumentException("Error on createDownloadFile() Owner is null!");
        }

        //create new device id
        String deviceId = shortUUID();

        KeyGenerationUtil.createApplicationKeys("virtual_firealarm");

        TokenClient accessTokenClient = new TokenClient(VirtualFireAlarmConstants.DEVICE_TYPE);
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
                                ".XMPP might have been disabled in org.wso2.carbon.device.mgt.iot" +
                                ".common.config.server.configs";
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
        ZipArchive zipFile = ziputil.createZipFile(owner, SUPER_TENANT, sketchType, deviceId, deviceName,
                                                   accessToken, refreshToken);
        zipFile.setDeviceId(deviceId);
        return zipFile;
    }

    /**
     * @return
     */
    private static String shortUUID() {
        UUID uuid = UUID.randomUUID();
        long l = ByteBuffer.wrap(uuid.toString().getBytes(StandardCharsets.UTF_8)).getLong();
        return Long.toString(l, Character.MAX_RADIX);
    }

}
