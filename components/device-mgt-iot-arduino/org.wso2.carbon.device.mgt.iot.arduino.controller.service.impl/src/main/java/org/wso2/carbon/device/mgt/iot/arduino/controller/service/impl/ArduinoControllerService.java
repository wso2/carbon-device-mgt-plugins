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

package org.wso2.carbon.device.mgt.iot.arduino.controller.service.impl;

import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.annotations.api.API;
import org.wso2.carbon.apimgt.annotations.device.DeviceType;
import org.wso2.carbon.apimgt.annotations.device.feature.Feature;
import org.wso2.carbon.device.mgt.iot.arduino.plugin.constants.ArduinoConstants;
import org.wso2.carbon.device.mgt.iot.arduino.controller.service.impl.dto.DeviceJSON;
import org.wso2.carbon.device.mgt.iot.arduino.controller.service.impl.transport.ArduinoMQTTConnector;
import org.wso2.carbon.device.mgt.iot.arduino.controller.service.impl.util.ArduinoServiceUtils;
import org.wso2.carbon.device.mgt.iot.controlqueue.mqtt.MqttConfig;
import org.wso2.carbon.device.mgt.iot.exception.DeviceControllerException;
import org.wso2.carbon.device.mgt.iot.sensormgt.SensorDataManager;
import org.wso2.carbon.device.mgt.iot.sensormgt.SensorRecord;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Calendar;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.concurrent.ConcurrentHashMap;

@API( name="arduino", version="1.0.0", context="/arduino")
@DeviceType( value = "arduino")
public class ArduinoControllerService {

    private static Log log = LogFactory.getLog(ArduinoControllerService.class);

    //TODO; replace this tenant domain
    private static final String SUPER_TENANT = "carbon.super";

    @Context  //injected response proxy supporting multiple thread
    private HttpServletResponse response;

    public static final String HTTP_PROTOCOL = "HTTP";
    public static final String MQTT_PROTOCOL = "MQTT";

    private ArduinoMQTTConnector arduinoMQTTConnector;
    private static Map<String, LinkedList<String>> replyMsgQueue = new HashMap<>();
    private static Map<String, LinkedList<String>> internalControlsQueue = new HashMap<>();
    private ConcurrentHashMap<String, String> deviceToIpMap = new ConcurrentHashMap<>();

    /**
     * @param arduinoMQTTConnector an object of type "ArduinoMQTTConnector" specific for this ArduinoControllerService
     */
    @SuppressWarnings("unused")
    public void setArduinoMQTTConnector(
            final ArduinoMQTTConnector arduinoMQTTConnector) {
        this.arduinoMQTTConnector = arduinoMQTTConnector;
        if (MqttConfig.getInstance().isEnabled()) {
            arduinoMQTTConnector.connect();
        } else {
            log.warn("MQTT disabled in 'devicemgt-config.xml'. Hence, ArduinoMQTTConnector not started.");
        }
    }

    /**
     * @return the "ArduinoMQTTConnector" object of this ArduinoControllerService instance
     */
    @SuppressWarnings("unused")
    public ArduinoMQTTConnector getArduinoMQTTConnector() {
        return arduinoMQTTConnector;
    }

    /**
     * @return the queue containing all the MQTT reply messages from all Arduinos communicating to this service
     */
    public static Map<String, LinkedList<String>> getReplyMsgQueue() {
        return replyMsgQueue;
    }

    /**
     * @return the queue containing all the MQTT controls received to be sent to any Arduinos connected to this server
     */
    public static Map<String, LinkedList<String>> getInternalControlsQueue() {
        return internalControlsQueue;
    }

    /*	---------------------------------------------------------------------------------------
                    Device specific APIs - Control APIs + Data-Publishing APIs
        ---------------------------------------------------------------------------------------	*/

    /**
     * @param owner
     * @param deviceId
     * @param deviceIP
     * @param devicePort
     * @param response
     * @param request
     * @return
     */
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
    @Feature( code="bulb", name="Control Bulb", type="operation",
            description="Control Bulb on Arduino Uno")
    public void switchBulb(@HeaderParam("owner") String owner,
                           @HeaderParam("deviceId") String deviceId,
                           @HeaderParam("protocol") String protocol,
                           @FormParam("state") String state,
                           @Context HttpServletResponse response) {

        LinkedList<String> deviceControlList = internalControlsQueue.get(deviceId);

        String operation = "BULB:" + state.toUpperCase();
        log.info(operation);
        if (deviceControlList == null) {
            deviceControlList = new LinkedList<>();
            deviceControlList.add(operation);
            internalControlsQueue.put(deviceId,deviceControlList);
        } else {
            deviceControlList.add(operation);
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
            description="Request temperature reading from Arduino agent")
    public SensorRecord requestTemperature(@HeaderParam("owner") String owner,
                                           @HeaderParam("deviceId") String deviceId,
                                           @HeaderParam("protocol") String protocol,
                                           @Context HttpServletResponse response) {
        SensorRecord sensorRecord = null;

        try {
            sensorRecord = SensorDataManager.getInstance().getSensorRecord(deviceId,
                    ArduinoConstants.SENSOR_TEMPERATURE);
        } catch ( DeviceControllerException e) {
            response.setStatus(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
        }

        response.setStatus(Response.Status.OK.getStatusCode());
        return sensorRecord;
    }

    /**
     * @param dataMsg
     * @param response
     */
    @Path("controller/pushdata")
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public void pushData(final DeviceJSON dataMsg, @Context HttpServletResponse response) {

        String owner = dataMsg.owner;
        String deviceId = dataMsg.deviceId;
        float pinData = dataMsg.value;

        SensorDataManager.getInstance().setSensorRecord(deviceId, ArduinoConstants.SENSOR_TEMPERATURE,
                                                            String.valueOf(pinData),
                                                            Calendar.getInstance().getTimeInMillis());

        if (!ArduinoServiceUtils.publishToDAS(dataMsg.owner, dataMsg.deviceId, dataMsg.value)) {
            response.setStatus(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
            log.warn("An error occured whilst trying to publish pin data of Arduino with ID [" + deviceId +
                                 "] of owner [" + owner + "]");
        }

    }

    /**
     * @param owner
     * @param deviceId
     * @param response
     * @return
     */
    @Path("controller/readcontrols")
    @GET
    public String readControls(@HeaderParam("owner") String owner,
                               @HeaderParam("deviceId") String deviceId,
                               @HeaderParam("protocol") String protocol,
                               @Context HttpServletResponse response) {
        String result;
        LinkedList<String> deviceControlList = internalControlsQueue.get(deviceId);

        if (deviceControlList == null) {
            result = "No controls have been set for device " + deviceId + " of owner " + owner;
            response.setStatus(HttpStatus.SC_NO_CONTENT);
        } else {
            try {
                result = deviceControlList.remove();
                response.setStatus(HttpStatus.SC_ACCEPTED);

            } catch (NoSuchElementException ex) {
                result = "There are no more controls for device " + deviceId + " of owner " +
                        owner;
                response.setStatus(HttpStatus.SC_NO_CONTENT);
            }
        }

        if (log.isDebugEnabled()) {
            log.debug(result);
        }

        return result;
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
        float temperature = dataMsg.value;

        SensorDataManager.getInstance().setSensorRecord(deviceId, ArduinoConstants.SENSOR_TEMPERATURE,
                                                            String.valueOf(temperature),
                                                            Calendar.getInstance().getTimeInMillis());

        if (!ArduinoServiceUtils.publishToDAS(dataMsg.owner, dataMsg.deviceId, dataMsg.value)) {
            response.setStatus(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
            log.warn("An error occured whilst trying to publish temperature data of Arduino with ID [" + deviceId +
                                 "] of owner [" + owner + "]");
        }
    }
}
