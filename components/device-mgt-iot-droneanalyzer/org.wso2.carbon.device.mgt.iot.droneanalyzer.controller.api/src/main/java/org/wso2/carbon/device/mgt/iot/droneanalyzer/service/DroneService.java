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

@API( name="drone_analyzer", version="1.0.0", context="/drone_analyzer")
@DeviceType( value = "drone_analyzer")
public class DroneService {

    private static org.apache.commons.logging.Log log = LogFactory.getLog(DroneService.class);
    private static final String SUPER_TENANT = "carbon.super";
    @Context  //injected response proxy supporting multiple thread
    private HttpServletResponse response;
    private ConcurrentHashMap<String, String> deviceToIpMap = new ConcurrentHashMap<>();
    private DroneController droneController = new DroneControllerImpl();

    /*	---------------------------------------------------------------------------------------
                    Device specific APIs - Control APIs + Data-Publishing APIs
            Also contains utility methods required for the execution of these APIs
         ---------------------------------------------------------------------------------------	*/
    @Path("controller/register/{owner}/{deviceId}/{ip}/{port}")
    @POST
    public String registerDeviceIP(@PathParam("owner") String owner, @PathParam("deviceId") String deviceId,
                                   @PathParam("ip") String deviceIP,
                                   @PathParam("port") String devicePort,
                                   @Context HttpServletResponse response,
                                   @Context HttpServletRequest request) {
        String result;
        log.info("Got register call from IP: " + deviceIP + " for Device ID: " + deviceId +
                         " of owner: " + owner);
        String deviceHttpEndpoint = deviceIP + ":" + devicePort;
        deviceToIpMap.put(deviceId, deviceHttpEndpoint);
        result = "Device-IP Registered";
        response.setStatus(Response.Status.OK.getStatusCode());
        if (log.isDebugEnabled()) {
            log.debug(result);
        }
        log.info(owner + deviceId + deviceIP + devicePort );
        return result;

    }

    @Path("controller/send_command")
    @POST
    @Feature( code="send_command", name="Send Command", type="operation",
            description="Send Commands to Drone")
    public Response droneController(@HeaderParam("owner") String owner, @HeaderParam("deviceId") String deviceId,
                                 @QueryParam("action") String action, @QueryParam("duration") String duration,
                                 @QueryParam("speed") String speed){
        try {
            DeviceValidator deviceValidator = new DeviceValidator();
            if (!deviceValidator.isExist(owner, SUPER_TENANT, new DeviceIdentifier(deviceId,
                    DroneConstants.DEVICE_TYPE))) {
                return Response.status(Response.Status.UNAUTHORIZED.getStatusCode()).build();
            }
        } catch (DeviceManagementException e) {
            log.error("DeviceValidation Failed for deviceId: " + deviceId + " of user: " + owner);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode()).build();
        }
        try {
            DroneAnalyzerServiceUtils.sendControlCommand(droneController, deviceId, action, Double.valueOf(speed),
                    Double.valueOf(duration));
            return Response.status(Response.Status.ACCEPTED).build();

        } catch (DeviceManagementException e) {
           log.error("Drone command didn't success. Try again, \n"+ e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
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
