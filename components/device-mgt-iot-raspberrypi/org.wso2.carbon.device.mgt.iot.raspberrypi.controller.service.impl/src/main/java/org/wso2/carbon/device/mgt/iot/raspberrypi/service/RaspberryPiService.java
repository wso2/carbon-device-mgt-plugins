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
import org.wso2.carbon.device.mgt.iot.controlqueue.mqtt.MqttConfig;
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

@API(name = "raspberrypi", version = "1.0.0", context = "/raspberrypi")
@DeviceType(value = "raspberrypi")
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
        /*if (MqttConfig.getInstance().isEnabled()) {
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
        } else {
            log.warn("MQTT disabled in 'devicemgt-config.xml");
        }*/
    }

    /**
     * @return
     */
    public RaspberryPiMQTTSubscriber getRaspberryPiMQTTSubscriber() {
        return raspberryPiMQTTSubscriber;
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

            String deviceHTTPEndpoint = deviceToIpMap.get(deviceId);
            if (deviceHTTPEndpoint == null) {
                response.setStatus(Response.Status.PRECONDITION_FAILED.getStatusCode());
                return;
            }

            RaspberrypiServiceUtils.sendCommandViaHTTP(deviceHTTPEndpoint, callUrlPattern, true);
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
