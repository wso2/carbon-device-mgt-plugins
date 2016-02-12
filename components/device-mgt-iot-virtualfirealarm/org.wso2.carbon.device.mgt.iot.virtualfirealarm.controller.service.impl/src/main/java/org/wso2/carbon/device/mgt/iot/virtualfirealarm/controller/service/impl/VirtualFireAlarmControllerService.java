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

package org.wso2.carbon.device.mgt.iot.virtualfirealarm.controller.service.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.analytics.datasource.commons.Record;
import org.wso2.carbon.analytics.datasource.commons.exception.AnalyticsException;
import org.wso2.carbon.apimgt.annotations.api.API;
import org.wso2.carbon.apimgt.annotations.device.DeviceType;
import org.wso2.carbon.apimgt.annotations.device.feature.Feature;
import org.wso2.carbon.certificate.mgt.core.dto.SCEPResponse;
import org.wso2.carbon.certificate.mgt.core.exception.KeystoreException;
import org.wso2.carbon.certificate.mgt.core.service.CertificateManagementService;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.device.mgt.analytics.service.DeviceAnalyticsService;
import org.wso2.carbon.device.mgt.common.DeviceIdentifier;
import org.wso2.carbon.device.mgt.common.DeviceManagementException;
import org.wso2.carbon.device.mgt.iot.DeviceManagement;
import org.wso2.carbon.device.mgt.iot.DeviceValidator;
import org.wso2.carbon.device.mgt.iot.controlqueue.mqtt.MqttConfig;
import org.wso2.carbon.device.mgt.iot.controlqueue.xmpp.XmppConfig;
import org.wso2.carbon.device.mgt.iot.exception.DeviceControllerException;
import org.wso2.carbon.device.mgt.iot.sensormgt.SensorDataManager;
import org.wso2.carbon.device.mgt.iot.sensormgt.SensorRecord;
import org.wso2.carbon.device.mgt.iot.transport.TransportHandlerException;
import org.wso2.carbon.device.mgt.iot.virtualfirealarm.controller.service.impl.dto.DeviceData;
import org.wso2.carbon.device.mgt.iot.virtualfirealarm.controller.service.impl.dto.SensorData;
import org.wso2.carbon.device.mgt.iot.virtualfirealarm.controller.service.impl.exception.VirtualFireAlarmException;
import org.wso2.carbon.device.mgt.iot.virtualfirealarm.controller.service.impl.transport.VirtualFireAlarmMQTTConnector;
import org.wso2.carbon.device.mgt.iot.virtualfirealarm.controller.service.impl.transport.VirtualFireAlarmXMPPConnector;
import org.wso2.carbon.device.mgt.iot.virtualfirealarm.controller.service.impl.util.SecurityManager;
import org.wso2.carbon.device.mgt.iot.virtualfirealarm.controller.service.impl.util.VirtualFireAlarmServiceUtils;
import org.wso2.carbon.device.mgt.iot.virtualfirealarm.controller.service.impl.util.scep.ContentType;
import org.wso2.carbon.device.mgt.iot.virtualfirealarm.controller.service.impl.util.scep.SCEPOperation;
import org.wso2.carbon.device.mgt.iot.virtualfirealarm.plugin.constants.VirtualFireAlarmConstants;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * This class consists the functions/APIs specific to the "actions" of the VirtualFirealarm device-type. These APIs
 * include the ones that are used by the [Device] to contact the server (i.e: Enrollment & Publishing Data) and the
 * ones used by the [Server/Owner] to contact the [Device] (i.e: sending control signals). This class also initializes
 * the transport 'Connectors' [XMPP & MQTT] specific to the VirtualFirealarm device-type in order to communicate with
 * such devices and to receive messages form it.
 */
@API(name = "virtual_firealarm", version = "1.0.0", context = "/virtual_firealarm")
@DeviceType(value = "virtual_firealarm")
@SuppressWarnings("Non-Annoted WebService")
public class VirtualFireAlarmControllerService {
    //TODO; replace this tenant domain
    private static final String SUPER_TENANT = "carbon.super";
    private static final String XMPP_PROTOCOL = "XMPP";
    private static final String HTTP_PROTOCOL = "HTTP";
    private static final String MQTT_PROTOCOL = "MQTT";
    private static Log log = LogFactory.getLog(VirtualFireAlarmControllerService.class);
    @Context  //injected response proxy supporting multiple thread
    private HttpServletResponse response;
    // consists of utility methods related to encrypting and decrypting messages
    private SecurityManager securityManager;
    // connects to the given MQTT broker and handles MQTT communication
    private VirtualFireAlarmMQTTConnector virtualFireAlarmMQTTConnector;
    // connects to the given XMPP server and handles XMPP communication
    private VirtualFireAlarmXMPPConnector virtualFireAlarmXMPPConnector;
    // holds a mapping of the IP addresses to Device-IDs for HTTP communication
    private ConcurrentHashMap<String, String> deviceToIpMap = new ConcurrentHashMap<>();

    private boolean waitForServerStartup() {
        while (!DeviceManagement.isServerReady()) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                return true;
            }
        }
        return false;
    }

    /**
     * Fetches the `SecurityManager` specific to this VirtualFirealarm controller service.
     *
     * @return the 'SecurityManager' instance bound to the 'securityManager' variable of this service.
     */
    @SuppressWarnings("Unused")
    public SecurityManager getSecurityManager() {
        return securityManager;
    }

    /**
     * Sets the `securityManager` variable of this VirtualFirealarm controller service.
     *
     * @param securityManager a 'SecurityManager' object that handles the encryption, decryption, signing and validation
     *                        of incoming messages from VirtualFirealarm device-types.
     */
    @SuppressWarnings("Unused")
    public void setSecurityManager(SecurityManager securityManager) {
        this.securityManager = securityManager;
        securityManager.initVerificationManager();
    }

    /**
     * Fetches the `VirtualFireAlarmXMPPConnector` specific to this VirtualFirealarm controller service.
     *
     * @return the 'VirtualFireAlarmXMPPConnector' instance bound to the 'virtualFireAlarmXMPPConnector' variable of
     * this service.
     */
    @SuppressWarnings("Unused")
    public VirtualFireAlarmXMPPConnector getVirtualFireAlarmXMPPConnector() {
        return virtualFireAlarmXMPPConnector;
    }

    /**
     * Sets the `virtualFireAlarmXMPPConnector` variable of this VirtualFirealarm controller service.
     *
     * @param virtualFireAlarmXMPPConnector a 'VirtualFireAlarmXMPPConnector' object that handles all XMPP related
     *                                      communications of any connected VirtualFirealarm device-type
     */
    @SuppressWarnings("Unused")
    public void setVirtualFireAlarmXMPPConnector(
            final VirtualFireAlarmXMPPConnector virtualFireAlarmXMPPConnector) {
        Runnable connector = new Runnable() {
            public void run() {
                if (waitForServerStartup()) {
                    return;
                }
                VirtualFireAlarmControllerService.this.virtualFireAlarmXMPPConnector = virtualFireAlarmXMPPConnector;

                if (XmppConfig.getInstance().isEnabled()) {
                    Runnable xmppStarter = new Runnable() {
                        @Override
                        public void run() {
                            virtualFireAlarmXMPPConnector.initConnector();
                            virtualFireAlarmXMPPConnector.connect();
                        }
                    };

                    Thread xmppStarterThread = new Thread(xmppStarter);
                    xmppStarterThread.setDaemon(true);
                    xmppStarterThread.start();
                } else {
                    log.warn("XMPP disabled in 'devicemgt-config.xml'. Hence, VirtualFireAlarmXMPPConnector not started.");
                }
            }
        };
        Thread connectorThread = new Thread(connector);
        connectorThread.setDaemon(true);
        connectorThread.start();
    }

    /**
     * Fetches the `VirtualFireAlarmMQTTConnector` specific to this VirtualFirealarm controller service.
     *
     * @return the 'VirtualFireAlarmMQTTConnector' instance bound to the 'virtualFireAlarmMQTTConnector' variable of
     * this service.
     */
    @SuppressWarnings("Unused")
    public VirtualFireAlarmMQTTConnector getVirtualFireAlarmMQTTConnector() {
        return virtualFireAlarmMQTTConnector;
    }

    /**
     * Sets the `virtualFireAlarmMQTTConnector` variable of this VirtualFirealarm controller service.
     *
     * @param virtualFireAlarmMQTTConnector a 'VirtualFireAlarmMQTTConnector' object that handles all MQTT related
     *                                      communications of any connected VirtualFirealarm device-type
     */
    @SuppressWarnings("Unused")
    public void setVirtualFireAlarmMQTTConnector(
            final VirtualFireAlarmMQTTConnector virtualFireAlarmMQTTConnector) {
        Runnable connector = new Runnable() {
            public void run() {
                if (waitForServerStartup()) {
                    return;
                }
                VirtualFireAlarmControllerService.this.virtualFireAlarmMQTTConnector = virtualFireAlarmMQTTConnector;
                if (MqttConfig.getInstance().isEnabled()) {
                    virtualFireAlarmMQTTConnector.connect();
                } else {
                    log.warn("MQTT disabled in 'devicemgt-config.xml'. Hence, VirtualFireAlarmMQTTConnector not started.");
                }
            }
        };
        Thread connectorThread = new Thread(connector);
        connectorThread.setDaemon(true);
        connectorThread.start();
    }

    /**
     * This is an API used/called by the device. It registers the IP of a VirtualFirealarm device against its DeviceID
     * when the device connects with the server for the first time. This DeviceID to IP mapping is necessary only for
     * cases where HTTP communication is to be used. At such instances, this mapping is used by the server to
     * identify the IP of the device to which it has some message to be sent. This method becomes useful only in
     * scenarios where HTTP communication is used in a setup where the IoT-Server and the devices communicating with it
     * are in the same IP network.
     *
     * @param owner      the name of the owner(username) of the device from which this register-IP call was initiated.
     * @param deviceId   the ID of the VirtualFirealarm device from which this register-IP call was initiated.
     * @param deviceIP   the IP of the VirtualFirealarm device which has sent this register-IP request.
     * @param devicePort the PORT on the VirtualFirealarm device (on this IP) that's open for HTTP communication.
     * @param request    the HTTP servlet request object received by default as part of the HTTP call to this API.
     * @param response   the HTTP servlet response object received  by default as part of the HTTP call to this API.
     * @return a custom message indicating whether the DeviceID to IP mapping was successful.
     */
    @POST
    @Path("controller/register/{owner}/{deviceId}/{ip}/{port}")
    public String registerDeviceIP(@PathParam("owner") String owner,
                                   @PathParam("deviceId") String deviceId,
                                   @PathParam("ip") String deviceIP,
                                   @PathParam("port") String devicePort,
                                   @Context HttpServletRequest request,
                                   @Context HttpServletResponse response) {
        //TODO:: Need to get IP from the request itself and have a mapping to owner
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
     * This is an API called/used from within the Server(Front-End) or by a device Owner. It sends a control command to
     * the VirtualFirealarm device to switch `ON` or `OFF` its buzzer. The method also takes in the protocol to be used
     * to connect-to and send the command to the device.
     *
     * @param owner    the name of the owner(username) of the device to which the control-command is to be sent.
     * @param deviceId the ID of the VirtualFirealarm device on which the buzzer needs to switched `ON` or `OFF`.
     * @param protocol the protocol (HTTP, MQTT, XMPP) to be used to connect-to & send the message to the device.
     * @param state    the state to which the buzzer on the device needs to be changed. Either "ON" or "OFF".
     *                 (Case-Insensitive String)
     * @param response the HTTP servlet response object received  by default as part of the HTTP call to this API.
     */
    @POST
    @Path("controller/buzz")
    @Feature(code = "buzz", name = "Buzzer On / Off", type = "operation",
             description = "Switch on/off Virtual Fire Alarm Buzzer. (On / Off)")
    public void switchBulb(@HeaderParam("owner") String owner,
                           @HeaderParam("deviceId") String deviceId,
                           @HeaderParam("protocol") String protocol,
                           @FormParam("state") String state,
                           @Context HttpServletResponse response) {

        try {
            DeviceValidator deviceValidator = new DeviceValidator();
            if (!deviceValidator.isExist(owner, SUPER_TENANT,
                                         new DeviceIdentifier(deviceId, VirtualFireAlarmConstants.DEVICE_TYPE))) {
                response.setStatus(Response.Status.UNAUTHORIZED.getStatusCode());
                return;
            }
        } catch (DeviceManagementException e) {
            log.error("DeviceValidation Failed for deviceId: " + deviceId + " of user: " + owner);
            response.setStatus(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
            return;
        }

        String switchToState = state.toUpperCase();

        if (!switchToState.equals(VirtualFireAlarmConstants.STATE_ON) && !switchToState.equals(
                VirtualFireAlarmConstants.STATE_OFF)) {
            log.error("The requested state change shoud be either - 'ON' or 'OFF'");
            response.setStatus(Response.Status.BAD_REQUEST.getStatusCode());
            return;
        }

        String protocolString = protocol.toUpperCase();
        String callUrlPattern = VirtualFireAlarmConstants.BULB_CONTEXT + switchToState;

        if (log.isDebugEnabled()) {
            log.debug("Sending request to switch-bulb of device [" + deviceId + "] via " +
                              protocolString);
        }

        try {
            switch (protocolString) {
                case HTTP_PROTOCOL:
                    String deviceHTTPEndpoint = deviceToIpMap.get(deviceId);
                    if (deviceHTTPEndpoint == null) {
                        response.setStatus(Response.Status.PRECONDITION_FAILED.getStatusCode());
                        return;
                    }

                    VirtualFireAlarmServiceUtils.sendCommandViaHTTP(deviceHTTPEndpoint, callUrlPattern, true);
                    break;

                case MQTT_PROTOCOL:
                    String mqttResource = VirtualFireAlarmConstants.BULB_CONTEXT.replace("/", "");
                    virtualFireAlarmMQTTConnector.publishDeviceData(owner, deviceId, mqttResource, switchToState);
                    break;

                case XMPP_PROTOCOL:
                    String xmppResource = VirtualFireAlarmConstants.BULB_CONTEXT.replace("/", "");
                    virtualFireAlarmXMPPConnector.publishDeviceData(owner, deviceId, xmppResource, switchToState);
                    break;

                default:
                    response.setStatus(Response.Status.NOT_ACCEPTABLE.getStatusCode());
                    return;
            }
        } catch (DeviceManagementException | TransportHandlerException e) {
            log.error("Failed to send switch-bulb request to device [" + deviceId + "] via " + protocolString);
            response.setStatus(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
            return;
        }

        response.setStatus(Response.Status.OK.getStatusCode());
    }


    /**
     * This is an API called/used from within the Server(Front-End) or by a device Owner. It sends a control command
     * to the VirtualFirealarm device to 'tell what's its current humidity reading'. The method also takes in the
     * protocol to be used to connect-to and send the command to the device.
     *
     * @param owner    the name of the owner(username) of the device to which the control-command is to be sent.
     * @param deviceId the ID of the VirtualFirealarm device on which the humidity reading is be read-from.
     * @param protocol the protocol (HTTP, MQTT, XMPP) to be used to connect-to & send the message to the device.
     * @param response the HTTP servlet response object received  by default as part of the HTTP call to this API.
     * @return an instance of the 'SensorRecord' object that holds the last updated humidity of the VirtualFirealarm
     * whose humidity reading was requested.
     */
    @GET
    @Path("controller/humidity")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Feature(code = "humidity", name = "Humidity", type = "monitor",
             description = "Read Humidity Readings from Virtual Fire Alarm")
    public SensorRecord requestHumidity(@HeaderParam("owner") String owner,
                                        @HeaderParam("deviceId") String deviceId,
                                        @HeaderParam("protocol") String protocol,
                                        @Context HttpServletResponse response) {
        //TODO::Need to use Web-Sockets to reply messages.
        SensorRecord sensorRecord = null;
        DeviceValidator deviceValidator = new DeviceValidator();
        try {
            if (!deviceValidator.isExist(owner, SUPER_TENANT, new DeviceIdentifier(
                    deviceId, VirtualFireAlarmConstants.DEVICE_TYPE))) {
                response.setStatus(Response.Status.UNAUTHORIZED.getStatusCode());
            }
        } catch (DeviceManagementException e) {
            response.setStatus(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
        }

        String protocolString = protocol.toUpperCase();

        if (log.isDebugEnabled()) {
            log.debug("Sending request to read humidity value of device [" + deviceId + "] via " + protocolString);
        }

        try {
            switch (protocolString) {
                case HTTP_PROTOCOL:
                    String deviceHTTPEndpoint = deviceToIpMap.get(deviceId);
                    if (deviceHTTPEndpoint == null) {
                        response.setStatus(Response.Status.PRECONDITION_FAILED.getStatusCode());
                    }

                    String humidityValue = VirtualFireAlarmServiceUtils.sendCommandViaHTTP(deviceHTTPEndpoint,
                                                                                           VirtualFireAlarmConstants
                                                                                                   .HUMIDITY_CONTEXT,
                                                                                           false);
                    SensorDataManager.getInstance().setSensorRecord(deviceId,
                                                                    VirtualFireAlarmConstants.SENSOR_TEMP,
                                                                    humidityValue,
                                                                    Calendar.getInstance().getTimeInMillis());
                    break;

                case MQTT_PROTOCOL:
                    String mqttResource = VirtualFireAlarmConstants.HUMIDITY_CONTEXT.replace("/", "");
                    virtualFireAlarmMQTTConnector.publishDeviceData(owner, deviceId, mqttResource, "");
                    break;

                case XMPP_PROTOCOL:
                    String xmppResource = VirtualFireAlarmConstants.HUMIDITY_CONTEXT.replace("/", "");
                    virtualFireAlarmXMPPConnector.publishDeviceData(owner, deviceId, xmppResource, "");
                    break;

                default:
                    response.setStatus(Response.Status.NOT_ACCEPTABLE.getStatusCode());
            }
            sensorRecord = SensorDataManager.getInstance().getSensorRecord(deviceId,
                                                                           VirtualFireAlarmConstants.SENSOR_HUMIDITY);
        } catch (DeviceManagementException | DeviceControllerException | TransportHandlerException e) {
            response.setStatus(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
        }

        response.setStatus(Response.Status.OK.getStatusCode());
        return sensorRecord;
    }

    /**
     * This is an API called/used from within the Server(Front-End) or by a device Owner. It sends a control command
     * to the VirtualFirealarm device to 'tell what's its current temperature reading'. The method also takes in the
     * protocol to be used to connect-to and send the command to the device.
     *
     * @param owner    the name of the owner(username) of the device to which the control-command is to be sent.
     * @param deviceId the ID of the VirtualFirealarm device on which the temperature reading is be read-from.
     * @param protocol the protocol (HTTP, MQTT, XMPP) to be used to connect-to & send the message to the device.
     * @param response the HTTP servlet response object received  by default as part of the HTTP call to this API.
     * @return an instance of the 'SensorRecord' object that holds the last updated temperature of the VirtualFirealarm
     * whose temperature reading was requested.
     */
    @GET
    @Path("controller/temperature")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Feature(code = "temperature", name = "Temperature", type = "monitor",
             description = "Request Temperature reading from Virtual Fire Alarm")
    public SensorRecord requestTemperature(@HeaderParam("owner") String owner,
                                           @HeaderParam("deviceId") String deviceId,
                                           @HeaderParam("protocol") String protocol,
                                           @Context HttpServletResponse response) {
        SensorRecord sensorRecord = null;

        DeviceValidator deviceValidator = new DeviceValidator();
        try {
            if (!deviceValidator.isExist(owner, SUPER_TENANT,
                                         new DeviceIdentifier(deviceId, VirtualFireAlarmConstants.DEVICE_TYPE))) {
                response.setStatus(Response.Status.UNAUTHORIZED.getStatusCode());
            }
        } catch (DeviceManagementException e) {
            response.setStatus(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
        }

        String protocolString = protocol.toUpperCase();

        if (log.isDebugEnabled()) {
            log.debug("Sending request to read virtual-firealarm-temperature of device " +
                              "[" + deviceId + "] via " + protocolString);
        }

        try {
            switch (protocolString) {
                case HTTP_PROTOCOL:
                    String deviceHTTPEndpoint = deviceToIpMap.get(deviceId);
                    if (deviceHTTPEndpoint == null) {
                        response.setStatus(Response.Status.PRECONDITION_FAILED.getStatusCode());
                    }

                    String temperatureValue = VirtualFireAlarmServiceUtils.sendCommandViaHTTP(
                            deviceHTTPEndpoint,
                            VirtualFireAlarmConstants.TEMPERATURE_CONTEXT,
                            false);

                    SensorDataManager.getInstance().setSensorRecord(deviceId,
                                                                    VirtualFireAlarmConstants
                                                                            .SENSOR_TEMP,
                                                                    temperatureValue,
                                                                    Calendar.getInstance()
                                                                            .getTimeInMillis());
                    break;

                case MQTT_PROTOCOL:
                    String mqttResource = VirtualFireAlarmConstants.TEMPERATURE_CONTEXT.replace("/", "");
                    virtualFireAlarmMQTTConnector.publishDeviceData(owner, deviceId, mqttResource, "");
                    break;

                case XMPP_PROTOCOL:
                    String xmppResource = VirtualFireAlarmConstants.TEMPERATURE_CONTEXT.replace("/", "");
                    virtualFireAlarmMQTTConnector.publishDeviceData(owner, deviceId, xmppResource, "");
                    break;

                default:
                    response.setStatus(Response.Status.NOT_ACCEPTABLE.getStatusCode());
            }
            sensorRecord = SensorDataManager.getInstance().getSensorRecord(deviceId,
                                                                           VirtualFireAlarmConstants.SENSOR_TEMP);
        } catch (DeviceManagementException | DeviceControllerException | TransportHandlerException e) {
            response.setStatus(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
        }

        response.setStatus(Response.Status.OK.getStatusCode());
        return sensorRecord;
    }

    /**
     * This is an API called/used by the VirtualFirealarm device to publish its temperature to the IoT-Server. The
     * received data from the device is stored in a 'DeviceRecord' under the device's ID in the 'SensorDataManager'
     * of the Server.
     *
     * @param dataMsg  the temperature data received from the device in JSON format complying to type 'DeviceData'.
     * @param response the HTTP servlet response object received  by default as part of the HTTP call to this API.
     */
    @POST
    @Path("controller/temperature")
    @Consumes(MediaType.APPLICATION_JSON)
    public void pushTemperatureData(final DeviceData dataMsg, @Context HttpServletResponse response) {
        String deviceId = dataMsg.deviceId;
        String deviceIp = dataMsg.reply;
        float temperature = dataMsg.value;

        String registeredIp = deviceToIpMap.get(deviceId);

        if (registeredIp == null) {
            log.warn("Unregistered IP: Temperature Data Received from an un-registered IP " +
                             deviceIp + " for device ID - " + deviceId);
            response.setStatus(Response.Status.PRECONDITION_FAILED.getStatusCode());
            return;
        } else if (!registeredIp.equals(deviceIp)) {
            log.warn("Conflicting IP: Received IP is " + deviceIp + ". Device with ID " + deviceId +
                             " is already registered under some other IP. Re-registration required");
            response.setStatus(Response.Status.CONFLICT.getStatusCode());
            return;
        }
        SensorDataManager.getInstance().setSensorRecord(deviceId,
                                                        VirtualFireAlarmConstants.SENSOR_TEMP,
                                                        String.valueOf(temperature),
                                                        Calendar.getInstance().getTimeInMillis());

        if (!VirtualFireAlarmServiceUtils.publishToDAS(dataMsg.owner, dataMsg.deviceId, dataMsg.value)) {
            response.setStatus(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
        }

    }


    /**
     * This is an API called/used by the SCEP Client of the VirtualFirealarm device in its SCEP enrollment process.
     * This acts as the endpoint exposed as part of the SCEP-Server for use by a SCEP Client. This is one of the two
     * method-signatures that takes different parameters according to the SCEP-Operation executed by the SCEP-Client
     * of the enrolling device. The API supports 2 SCEP Operations [GetCACert] and [GetCACaps].
     * <p/>
     * Operation [GetCACert] returns the CA cert of the SCEP-Server for the device to verify its authenticity.
     * Operation [GetCACaps] returns the CA Capabilities of the SCEP-Server.
     *
     * @param operation the SCEP operation requested by the client. [GetCACert] or [GetCACaps]
     * @param message   any messages pertaining to the requested SCEP Operation.
     * @return an HTTP Response object with either the CA-Cert or the CA-Capabilities according to the operation.
     */
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
                        VirtualFireAlarmServiceUtils.getCertificateManagementService();
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
                        responseBuilder = Response.ok(scepResponse.getEncodedResponse(), ContentType.X_X509_CA_CERT);
                        break;
                    case CA_RA_CERT_RECEIVED:

                        if (log.isDebugEnabled()) {
                            log.debug("CA and RA certificates received in GetCACert");
                        }

                        responseBuilder = Response.ok(scepResponse.getEncodedResponse(), ContentType.X_X509_CA_RA_CERT);
                        break;
                    default:
                        log.error("Invalid SCEP request");
                        responseBuilder = Response.serverError();
                        break;
                }

                return responseBuilder.build();
            } catch (VirtualFireAlarmException e) {
                log.error("Error occurred while enrolling the VirtualFireAlarm device", e);
            } catch (KeystoreException e) {
                log.error("Keystore error occurred while enrolling the VirtualFireAlarm device", e);
            }

        } else if (SCEPOperation.GET_CA_CAPS.getValue().equals(operation)) {

            if (log.isDebugEnabled()) {
                log.debug("Invoking GetCACaps");
            }

            try {
                CertificateManagementService certificateManagementService = VirtualFireAlarmServiceUtils.
                        getCertificateManagementService();
                byte caCaps[] = certificateManagementService.getCACapsSCEP();

                return Response.ok(caCaps, MediaType.TEXT_PLAIN).build();

            } catch (VirtualFireAlarmException e) {
                log.error("Error occurred while enrolling the device", e);
            }

        } else {
            log.error("Invalid SCEP operation " + operation);
        }

        return Response.serverError().build();
    }

    /**
     * This is an API called/used by the SCEP Client of the VirtualFirealarm device in its SCEP enrollment process.
     * This acts as the endpoint exposed as part of the SCEP-Server for use by a SCEP Client. This is one of the two
     * method-signatures that takes different parameters according to the SCEP-Operation executed by the SCEP-Client
     * of the enrolling device. This API supports the SCEP Operation [PKIOperation].
     * <p/>
     * Operation [PKIOperation] returns a certificate generated by the SCEP-Server for the enrolling device.
     *
     * @param operation   the final SCEP operation executed in the enrollment process - which is [PKIOperation]
     * @param inputStream an input stream consisting of the Certificate-Signing-Request (CSR) from the device.
     * @return an HTTP Response object with the signed certificate for the device by the CA of the SCEP Server.
     */
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
                CertificateManagementService certificateManagementService = VirtualFireAlarmServiceUtils.
                        getCertificateManagementService();
                byte pkiMessage[] = certificateManagementService.getPKIMessageSCEP(inputStream);

                return Response.ok(pkiMessage, ContentType.X_PKI_MESSAGE).build();

            } catch (VirtualFireAlarmException e) {
                log.error("Error occurred while enrolling the device", e);
            } catch (KeystoreException e) {
                log.error("Keystore error occurred while enrolling the device", e);
            }
        }
        return Response.serverError().build();
    }

    /**
     * Retreive Sensor data for the device type
     * @param deviceId
     * @param sensor
     * @param user
     * @param from
     * @param to
     * @return
     */
    @Path("controller/stats/device/{deviceId}/sensors/{sensorName}")
    @GET
    @Consumes("application/json")
    @Produces("application/json")
    public SensorData[] getVirtualFireAlarmDeviceStats(@PathParam("deviceId") String deviceId,
                                                   @PathParam("sensorName") String sensor,
                                                   @QueryParam("username") String user,
                                                   @QueryParam("from") long from,
                                                   @QueryParam("to") long to) {

        String fromDate = String.valueOf(from);
        String toDate = String.valueOf(to);

        List<SensorData> sensorDatas = new ArrayList<>();
        PrivilegedCarbonContext.startTenantFlow();
        PrivilegedCarbonContext ctx = PrivilegedCarbonContext.getThreadLocalCarbonContext();
        //TODO - get the JWT from api manager.
        ctx.setTenantDomain("carbon.super", true);
        DeviceAnalyticsService deviceAnalyticsService = (DeviceAnalyticsService) ctx
                .getOSGiService(DeviceAnalyticsService.class, null);
        String query = "owner:" + user + " AND deviceId:" + deviceId + " AND deviceType:" +
                VirtualFireAlarmConstants.DEVICE_TYPE + " AND time : [" + fromDate + " TO " + toDate + "]";
        String sensorTableName = getSensorEventTableName(sensor);
        try {
            List<Record> records = deviceAnalyticsService.getAllEventsForDevice(sensorTableName, query);
            Collections.sort(records, new Comparator<Record>() {
                @Override
                public int compare(Record o1, Record o2) {
                    long t1 = (Long) o1.getValue("time");
                    long t2 = (Long) o2.getValue("time");
                    if (t1 < t2) {
                        return -1;
                    } else if (t1 > t2) {
                        return 1;
                    } else {
                        return 0;
                    }
                }
            });

            for (Record record : records) {
                SensorData sensorData = new SensorData();
                sensorData.setTime((long) record.getValue("time"));
                sensorData.setValue("" + (float) record.getValue(sensor));
                sensorDatas.add(sensorData);
            }
            return sensorDatas.toArray(new SensorData[sensorDatas.size()]);
        } catch (AnalyticsException e) {
            String errorMsg = "Error on retrieving stats on table " + sensorTableName + " with query " + query;
            log.error(errorMsg);
            response.setStatus(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
            return sensorDatas.toArray(new SensorData[sensorDatas.size()]);
        } finally {
            PrivilegedCarbonContext.endTenantFlow();
        }
    }

    /**
     * get the event table from the sensor name.
     * TODO : this needs to be managed with sensor management.
     * @param sensorName
     * @return
     */
    private String getSensorEventTableName(String sensorName){
        String sensorEventTableName;
        switch (sensorName) {
            case VirtualFireAlarmConstants.SENSOR_TEMP:
                sensorEventTableName = VirtualFireAlarmConstants.TEMPERATURE_EVENT_TABLE;
                break;
            case VirtualFireAlarmConstants.SENSOR_HUMIDITY:
                sensorEventTableName = VirtualFireAlarmConstants.HUMIDITY_EVENT_TABLE;
                break;
            default:
                sensorEventTableName = "";
        }
        return sensorEventTableName;
    }
}
