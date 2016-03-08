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

package org.wso2.carbon.device.mgt.iot.arduino.controller.service.impl;

import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.analytics.datasource.commons.Record;
import org.wso2.carbon.analytics.datasource.commons.exception.AnalyticsException;
import org.wso2.carbon.apimgt.annotations.api.API;
import org.wso2.carbon.apimgt.annotations.device.DeviceType;
import org.wso2.carbon.apimgt.annotations.device.feature.Feature;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.device.mgt.analytics.service.DeviceAnalyticsService;
import org.wso2.carbon.device.mgt.iot.DeviceManagement;
import org.wso2.carbon.device.mgt.iot.arduino.controller.service.impl.dto.DeviceData;
import org.wso2.carbon.device.mgt.iot.arduino.controller.service.impl.dto.SensorData;
import org.wso2.carbon.device.mgt.iot.arduino.controller.service.impl.transport.ArduinoMQTTConnector;
import org.wso2.carbon.device.mgt.iot.arduino.controller.service.impl.util.ArduinoServiceUtils;
import org.wso2.carbon.device.mgt.iot.arduino.plugin.constants.ArduinoConstants;
import org.wso2.carbon.device.mgt.iot.controlqueue.mqtt.MqttConfig;
import org.wso2.carbon.device.mgt.iot.exception.DeviceControllerException;
import org.wso2.carbon.device.mgt.iot.sensormgt.SensorDataManager;
import org.wso2.carbon.device.mgt.iot.sensormgt.SensorRecord;

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
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.concurrent.ConcurrentHashMap;

@API(name = "arduino", version = "1.0.0", context = "/arduino")
@DeviceType(value = "arduino")
public class ArduinoControllerService {

    public static final String HTTP_PROTOCOL = "HTTP";
    public static final String MQTT_PROTOCOL = "MQTT";
    //TODO; replace this tenant domain
    private static final String SUPER_TENANT = "carbon.super";
    private static Log log = LogFactory.getLog(ArduinoControllerService.class);
    private static Map<String, LinkedList<String>> replyMsgQueue = new HashMap<>();
    private static Map<String, LinkedList<String>> internalControlsQueue = new HashMap<>();
    @Context  //injected response proxy supporting multiple thread
    private HttpServletResponse response;
    private ArduinoMQTTConnector arduinoMQTTConnector;
    private ConcurrentHashMap<String, String> deviceToIpMap = new ConcurrentHashMap<>();

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
     * @return the "ArduinoMQTTConnector" object of this ArduinoControllerService instance
     */
    @SuppressWarnings("unused")
    public ArduinoMQTTConnector getArduinoMQTTConnector() {
        return arduinoMQTTConnector;
    }

    /**
     * @param arduinoMQTTConnector an object of type "ArduinoMQTTConnector" specific for this ArduinoControllerService
     */
    @SuppressWarnings("unused")
    public void setArduinoMQTTConnector(final ArduinoMQTTConnector arduinoMQTTConnector) {
        Runnable connector = new Runnable() {
            public void run() {
                if (waitForServerStartup()) {
                    return;
                }
                ArduinoControllerService.this.arduinoMQTTConnector = arduinoMQTTConnector;
                if (MqttConfig.getInstance().isEnabled()) {
                    arduinoMQTTConnector.connect();
                } else {
                    log.warn("MQTT disabled in 'devicemgt-config.xml'. Hence, ArduinoMQTTConnector not started.");
                }
            }
        };
        Thread connectorThread = new Thread(connector);
        connectorThread.setDaemon(true);
        connectorThread.start();
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
    @Feature(code = "bulb", name = "Control Bulb", type = "operation", description = "Control Bulb on Arduino Uno")
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
            internalControlsQueue.put(deviceId, deviceControlList);
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
    @Feature(code = "readtemperature", name = "Temperature", type = "monitor",
            description = "Request temperature reading from Arduino agent")
    public SensorRecord requestTemperature(@HeaderParam("owner") String owner,
                                           @HeaderParam("deviceId") String deviceId,
                                           @HeaderParam("protocol") String protocol,
                                           @Context HttpServletResponse response) {
        SensorRecord sensorRecord = null;

        try {
            sensorRecord = SensorDataManager.getInstance().getSensorRecord(deviceId,
                                                                           ArduinoConstants.SENSOR_TEMPERATURE);
        } catch (DeviceControllerException e) {
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
    public void pushData(final DeviceData dataMsg, @Context HttpServletResponse response) {

        String owner = dataMsg.owner;
        String deviceId = dataMsg.deviceId;
        float pinData = dataMsg.value;

        SensorDataManager.getInstance().setSensorRecord(deviceId,
                                                        ArduinoConstants.SENSOR_TEMPERATURE,
                                                        String.valueOf(pinData),
                                                        Calendar.getInstance().getTimeInMillis());

        if (!ArduinoServiceUtils.publishToDAS(dataMsg.owner, dataMsg.deviceId, dataMsg.value)) {
            response.setStatus(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
            log.warn("An error occured whilst trying to publish pin data of Arduino with ID [" +
                     deviceId +
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
                result = "There are no more controls for device " + deviceId + " of owner " + owner;
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
    public void pushTemperatureData(final DeviceData dataMsg,
                                    @Context HttpServletResponse response,
                                    @Context HttpServletRequest request) {
        String owner = dataMsg.owner;
        String deviceId = dataMsg.deviceId;
        float temperature = dataMsg.value;

        SensorDataManager.getInstance().setSensorRecord(deviceId,
                                                        ArduinoConstants.SENSOR_TEMPERATURE,
                                                        String.valueOf(temperature),
                                                        Calendar.getInstance().getTimeInMillis());

        if (!ArduinoServiceUtils.publishToDAS(dataMsg.owner, dataMsg.deviceId, dataMsg.value)) {
            response.setStatus(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
            log.warn("An error occured whilst trying to publish temperature data of Arduino with ID [" + deviceId +
                     "] of owner [" + owner + "]");
        }
    }

    /**
     * Retreive Sensor data for the device type
     *
     * @param deviceId
     * @param user
     * @param from
     * @param to
     * @return
     */
    @Path("controller/stats/device/{deviceId}/sensors/temperature")
    @GET
    @Consumes("application/json")
    @Produces("application/json")
    public SensorData[] getArduinoTemperatureStats(@PathParam("deviceId") String deviceId,
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
                       ArduinoConstants.DEVICE_TYPE + " AND time : [" + fromDate + " TO " + toDate + "]";
        String sensorTableName = ArduinoConstants.TEMPERATURE_EVENT_TABLE;
        try {
            List<Record> records = deviceAnalyticsService.getAllEventsForDevice(sensorTableName,
                                                                                query);

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
                sensorData.setValue("" + (float) record.getValue(ArduinoConstants.SENSOR_TEMPERATURE));
                sensorDatas.add(sensorData);
            }
            return sensorDatas.toArray(new SensorData[sensorDatas.size()]);
        } catch (AnalyticsException e) {
            String errorMsg =
                    "Error on retrieving stats on table " + sensorTableName + " with query " + query;
            log.error(errorMsg);
            response.setStatus(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
            return sensorDatas.toArray(new SensorData[sensorDatas.size()]);
        } finally {
            PrivilegedCarbonContext.endTenantFlow();
        }
    }

}
