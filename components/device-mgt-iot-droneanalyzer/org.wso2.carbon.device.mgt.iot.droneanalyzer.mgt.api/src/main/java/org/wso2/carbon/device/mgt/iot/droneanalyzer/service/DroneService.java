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
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.carbon.device.mgt.iot.droneanalyzer.service;

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
import org.wso2.carbon.device.mgt.iot.controlqueue.xmpp.XmppAccount;
import org.wso2.carbon.device.mgt.iot.controlqueue.xmpp.XmppConfig;
import org.wso2.carbon.device.mgt.iot.controlqueue.xmpp.XmppServerClient;
import org.wso2.carbon.device.mgt.iot.droneanalyzer.plugin.constants.DroneConstants;
import org.wso2.carbon.device.mgt.iot.droneanalyzer.plugin.controller.DroneController;
import org.wso2.carbon.device.mgt.iot.droneanalyzer.plugin.controller.impl.DroneControllerImpl;
import org.wso2.carbon.device.mgt.iot.droneanalyzer.service.exception.DroneAnalyzerException;
import org.wso2.carbon.device.mgt.iot.droneanalyzer.service.util.DroneAnalyzerServiceUtils;
import org.wso2.carbon.device.mgt.iot.droneanalyzer.service.util.scep.ContentType;
import org.wso2.carbon.device.mgt.iot.droneanalyzer.service.util.scep.SCEPOperation;
import org.wso2.carbon.device.mgt.iot.exception.AccessTokenException;
import org.wso2.carbon.device.mgt.iot.exception.DeviceControllerException;
import org.wso2.carbon.device.mgt.iot.util.ZipArchive;
import org.wso2.carbon.device.mgt.iot.util.ZipUtil;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@DeviceType( value = "drone_analyzer")
public class DroneService {

    private static org.apache.commons.logging.Log log = LogFactory.getLog(DroneService.class);
    private static final String SUPER_TENANT = "carbon.super";
    @Context  //injected response proxy supporting multiple thread
    private HttpServletResponse response;
    private ConcurrentHashMap<String, String> deviceToIpMap = new ConcurrentHashMap<>();
    private DroneController droneController = new DroneControllerImpl();
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
        deviceIdentifier.setType(DroneConstants.DEVICE_TYPE);
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
            device.setType(DroneConstants.DEVICE_TYPE);
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
    public void removeDevice(@PathParam("device_id") String deviceId, @Context HttpServletResponse response) {
        DeviceManagement deviceManagement = new DeviceManagement(SUPER_TENANT);
        DeviceIdentifier deviceIdentifier = new DeviceIdentifier();
        deviceIdentifier.setId(deviceId);
        deviceIdentifier.setType(DroneConstants.DEVICE_TYPE);
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
    public boolean updateDevice(@PathParam("device_id") String deviceId, @QueryParam("name") String name,
                                @Context HttpServletResponse response) {
        DeviceManagement deviceManagement = new DeviceManagement(SUPER_TENANT);
        DeviceIdentifier deviceIdentifier = new DeviceIdentifier();
        deviceIdentifier.setId(deviceId);
        deviceIdentifier.setType(DroneConstants.DEVICE_TYPE);
        try {
            Device device = deviceManagement.getDeviceManagementService().getDevice(deviceIdentifier);
            device.setDeviceIdentifier(deviceId);
            device.getEnrolmentInfo().setDateOfLastUpdate(new Date().getTime());
            device.setName(name);
            device.setType(DroneConstants.DEVICE_TYPE);
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

    @Path("manager/device/{device_id}")
    @GET
    @Consumes("application/json")
    @Produces("application/json")
    public Device getDevice(@PathParam("device_id") String deviceId) {
        DeviceManagement deviceManagement = new DeviceManagement(SUPER_TENANT);
        DeviceIdentifier deviceIdentifier = new DeviceIdentifier();
        deviceIdentifier.setId(deviceId);
        deviceIdentifier.setType(DroneConstants.DEVICE_TYPE);
        try {
            return deviceManagement.getDeviceManagementService().getDevice(deviceIdentifier);

        } catch (DeviceManagementException e) {
            response.setStatus(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
            return null;
        } finally {
            deviceManagement.endTenantFlow();
        }
    }

    @Path("manager/devices/{username}")
    @GET
    @Consumes("application/json")
    @Produces("application/json")
    public Device[] getDroneDevices(@PathParam("username") String username) {
        DeviceManagement deviceManagement = new DeviceManagement(SUPER_TENANT);
        try {
            List<Device> userDevices = deviceManagement.getDeviceManagementService().getDevicesOfUser(username);
            ArrayList<Device> userDevicesforDrone = new ArrayList<>();
            for (Device device : userDevices) {
                if (device.getType().equals(DroneConstants.DEVICE_TYPE) &&
                        device.getEnrolmentInfo().getStatus().equals(
                                EnrolmentInfo.Status.ACTIVE)) {
                    userDevicesforDrone.add(device);
                }
            }
            return userDevicesforDrone.toArray(new Device[]{});
        } catch (DeviceManagementException e) {
            response.setStatus(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
            return null;
        } finally {
            deviceManagement.endTenantFlow();
        }
    }



    @Path("manager/device/{sketch_type}/download")
    @GET
    @Produces("application/octet-stream")
    public Response downloadSketch(@QueryParam("owner") String owner, @QueryParam("deviceName") String deviceName,
                                   @PathParam("sketch_type") String sketchType) {
        if (owner == null) {
            return Response.status(400).build();//bad request
        }
        //create new device id
        String deviceId = shortUUID();
        //create token
        String token = UUID.randomUUID().toString();
        String refreshToken = UUID.randomUUID().toString();
        //adding registering data
        boolean status = register(deviceId, deviceName, owner);
        if (!status) {
            return Response.status(500).entity(
                    "Error occurred while registering the device with " + "id: " + deviceId
                            + " owner:" + owner).build();

        }
        ZipUtil ziputil = new ZipUtil();
        ZipArchive zipFile;
        try {
            zipFile = ziputil.createZipFile(owner, SUPER_TENANT, sketchType, deviceId, deviceName, token,
                                            refreshToken);
        } catch (DeviceManagementException ex) {
            return Response.status(500).entity("Error occurred while creating zip file").build();
        }
        Response.ResponseBuilder rb = Response.ok(zipFile.getZipFile());
        rb.header("Content-Disposition", "attachment; filename=\"" + zipFile.getFileName() + "\"");
        return rb.build();
    }

    @Path("manager/device/{sketch_type}/generate_link")
    @GET
    public Response generateSketchLink(@QueryParam("owner") String owner,
                                       @QueryParam("deviceName") String deviceName,
                                       @PathParam("sketch_type") String sketchType) {
        try {
            ZipArchive zipFile = createDownloadFile(owner, deviceName, sketchType);
            Response.ResponseBuilder rb = Response.ok(zipFile.getDeviceId());
            return rb.build();
        } catch (IllegalArgumentException ex) {
            return Response.status(400).entity(ex.getMessage()).build();
        } catch (DeviceManagementException ex) {
            return Response.status(500).entity(ex.getMessage()).build();
        } catch (AccessTokenException ex) {
            return Response.status(500).entity(ex.getMessage()).build();
        } catch (DeviceControllerException ex) {
            return Response.status(500).entity(ex.getMessage()).build();
        }
    }

    private ZipArchive createDownloadFile(String owner, String deviceName, String sketchType)
            throws DeviceManagementException, AccessTokenException, DeviceControllerException {
        if (owner == null) {
            throw new IllegalArgumentException("Error on createDownloadFile() Owner is null!");
        }
        KeyGenerationUtil.createApplicationKeys("drone");
        //create new device id
        String deviceId = shortUUID();
        TokenClient accessTokenClient = new TokenClient(DroneConstants.DEVICE_TYPE);
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
                        "XMPP Account was not created for device - " + deviceId + " of owner - " +
                                owner +
                                ".XMPP might have been disabled in org.wso2.carbon.device.mgt.iot" +
                                ".common.config.server.configs";
                log.warn(msg);
                throw new DeviceManagementException(msg);
            }
        }
        //Register the device with CDMF
        status = register(deviceId, deviceName, owner);
        if (!status) {
            String msg = "Error occurred while registering the device with " + "id: " + deviceId
                    + " owner:" + owner;
            throw new DeviceManagementException(msg);
        }
        ZipUtil ziputil = new ZipUtil();
        ZipArchive zipFile = ziputil.createZipFile(owner, SUPER_TENANT, sketchType, deviceId, deviceName,
                                                   accessToken, refreshToken);
        zipFile.setDeviceId(deviceId);
        return zipFile;
    }

    private static String shortUUID() {
        UUID uuid = UUID.randomUUID();
        long l = ByteBuffer.wrap(uuid.toString().getBytes(StandardCharsets.UTF_8)).getLong();
        return Long.toString(l, Character.MAX_RADIX);
    }

    @GET
    @Path("controller/scep")
    public Response scepRequest(@QueryParam("operation") String operation, @QueryParam("message") String message) {
        if (log.isDebugEnabled()) {
            log.debug("Invoking SCEP operation " + operation);
        }
        if (SCEPOperation.GET_CA_CERT.getValue().equals(operation)) {
            if (log.isDebugEnabled()) {
                log.debug("Invoking GetCACert");
            }
            try {
                CertificateManagementService certificateManagementService =
                        DroneAnalyzerServiceUtils.getCertificateManagementService();
                SCEPResponse scepResponse = certificateManagementService.getCACertSCEP();
                Response.ResponseBuilder responseBuilder;
                switch (scepResponse.getResultCriteria()) {
                    case CA_CERT_FAILED:
                        log.error("CA cert failed");
                        responseBuilder = Response.serverError();
                        break;
                    case CA_CERT_RECEIVED:

                        if (log.isDebugEnabled()) {
                            log.debug("CA certificate received in GetCACert");
                        }
                        responseBuilder = Response.ok(scepResponse.getEncodedResponse(),
                                                      ContentType.X_X509_CA_CERT);
                        break;
                    case CA_RA_CERT_RECEIVED:
                        if (log.isDebugEnabled()) {
                            log.debug("CA and RA certificates received in GetCACert");
                        }
                        responseBuilder = Response.ok(scepResponse.getEncodedResponse(),
                                                      ContentType.X_X509_CA_RA_CERT);
                        break;
                    default:
                        log.error("Invalid SCEP request");
                        responseBuilder = Response.serverError();
                        break;
                }
                return responseBuilder.build();
            } catch (DroneAnalyzerException e) {
                log.error("Error occurred while enrolling the drone device", e);
            } catch (KeystoreException e) {
                log.error("Keystore error occurred while enrolling the drone device", e);
            }

        } else if (SCEPOperation.GET_CA_CAPS.getValue().equals(operation)) {

            if (log.isDebugEnabled()) {
                log.debug("Invoking GetCACaps");
            }

            try {
                CertificateManagementService certificateManagementService = DroneAnalyzerServiceUtils.
                        getCertificateManagementService();
                byte caCaps[] = certificateManagementService.getCACapsSCEP();
                return Response.ok(caCaps, MediaType.TEXT_PLAIN).build();
            } catch (DroneAnalyzerException e) {
                log.error("Error occurred while enrolling the device", e);
            }

        } else {
            log.error("Invalid SCEP operation " + operation);
        }

        return Response.serverError().build();
    }

    @POST
    @Path("controller/scep")
    public Response scepRequestPost(@QueryParam("operation") String operation, InputStream inputStream) {

        if (log.isDebugEnabled()) {
            log.debug("Invoking SCEP operation " + operation);
        }

        if (SCEPOperation.PKI_OPERATION.getValue().equals(operation)) {

            if (log.isDebugEnabled()) {
                log.debug("Invoking PKIOperation");
            }
            try {
                CertificateManagementService certificateManagementService = DroneAnalyzerServiceUtils.
                        getCertificateManagementService();
                byte pkiMessage[] = certificateManagementService.getPKIMessageSCEP(inputStream);
                return Response.ok(pkiMessage, ContentType.X_PKI_MESSAGE).build();

            } catch (DroneAnalyzerException e) {
                log.error("Error occurred while enrolling the device", e);
            } catch (KeystoreException e) {
                log.error("Keystore error occurred while enrolling the device", e);
            }
        }
        return Response.serverError().build();
    }
}
