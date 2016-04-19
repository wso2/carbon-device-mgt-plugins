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

package org.wso2.carbon.device.mgt.iot.raspberrypi.service.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.analytics.dataservice.commons.SORT;
import org.wso2.carbon.analytics.dataservice.commons.SortByField;
import org.wso2.carbon.analytics.datasource.commons.exception.AnalyticsException;
import org.wso2.carbon.device.mgt.common.DeviceManagementException;
import org.wso2.carbon.device.mgt.iot.controlqueue.mqtt.MqttConfig;
import org.wso2.carbon.device.mgt.iot.raspberrypi.service.impl.dto.DeviceData;
import org.wso2.carbon.device.mgt.iot.raspberrypi.service.impl.dto.SensorRecord;
import org.wso2.carbon.device.mgt.iot.raspberrypi.service.impl.transport.RaspberryPiMQTTConnector;
import org.wso2.carbon.device.mgt.iot.raspberrypi.service.impl.util.APIUtil;
import org.wso2.carbon.device.mgt.iot.raspberrypi.service.impl.util.RaspberrypiServiceUtils;
import org.wso2.carbon.device.mgt.iot.raspberrypi.plugin.constants.RaspberrypiConstants;
import org.wso2.carbon.device.mgt.iot.service.IoTServerStartupListener;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
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
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class RaspberryPiControllerServiceImpl implements RaspberryPiControllerService {

    private static Log log = LogFactory.getLog(RaspberryPiControllerServiceImpl.class);
    private ConcurrentHashMap<String, String> deviceToIpMap = new ConcurrentHashMap<>();
    private RaspberryPiMQTTConnector raspberryPiMQTTConnector;

    @Path("device/register/{deviceId}/{ip}/{port}")
    @POST
    public Response registerDeviceIP(@PathParam("deviceId") String deviceId, @PathParam("ip") String deviceIP,
                                     @PathParam("port") String devicePort, @Context HttpServletRequest request) {
        String result;
        if (log.isDebugEnabled()) {
            log.debug("Got register call from IP: " + deviceIP + " for Device ID: " + deviceId);
        }
        String deviceHttpEndpoint = deviceIP + ":" + devicePort;
        deviceToIpMap.put(deviceId, deviceHttpEndpoint);
        result = "Device-IP Registered";
        if (log.isDebugEnabled()) {
            log.debug(result);
        }
        return Response.ok().entity(result).build();
    }

    @Path("device/{deviceId}/bulb")
    @POST
    public Response switchBulb(@PathParam("deviceId") String deviceId, @FormParam("state") String state) {
        String switchToState = state.toUpperCase();
        if (!switchToState.equals(RaspberrypiConstants.STATE_ON) && !switchToState.equals(
                RaspberrypiConstants.STATE_OFF)) {
            log.error("The requested state change shoud be either - 'ON' or 'OFF'");
            return Response.status(Response.Status.BAD_REQUEST.getStatusCode()).build();
        }
        String callUrlPattern = RaspberrypiConstants.BULB_CONTEXT + switchToState;
        try {
            String deviceHTTPEndpoint = deviceToIpMap.get(deviceId);
            if (deviceHTTPEndpoint == null) {
                return Response.status(Response.Status.PRECONDITION_FAILED.getStatusCode()).build();
            }
            RaspberrypiServiceUtils.sendCommandViaHTTP(deviceHTTPEndpoint, callUrlPattern, true);
        } catch (DeviceManagementException e) {
            log.error("Failed to send switch-bulb request to device [" + deviceId + "] via ");
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode()).build();
        }
        return Response.ok().build();
    }

    @Path("device/push_temperature")
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public Response pushTemperatureData(final DeviceData dataMsg, @Context HttpServletRequest request) {
        String owner = dataMsg.owner;
        String deviceId = dataMsg.deviceId;
        String deviceIp = dataMsg.reply;
        float temperature = dataMsg.value;
        String registeredIp = deviceToIpMap.get(deviceId);
        if (registeredIp == null) {
            log.warn("Unregistered IP: Temperature Data Received from an un-registered IP " + deviceIp +
                     " for device ID - " + deviceId);
            return Response.status(Response.Status.PRECONDITION_FAILED.getStatusCode()).build();
        } else if (!registeredIp.equals(deviceIp)) {
            log.warn("Conflicting IP: Received IP is " + deviceIp + ". Device with ID " + deviceId +
                     " is already registered under some other IP. Re-registration required");
            return Response.status(Response.Status.CONFLICT.getStatusCode()).build();
        }
        if (log.isDebugEnabled()) {
            log.debug("Received Pin Data Value: " + temperature + " degrees C");
        }
        if (!RaspberrypiServiceUtils.publishToDAS(dataMsg.deviceId, dataMsg.value)) {
            log.warn("An error occured whilst trying to publish temperature data of raspberrypi with ID [" +
                     deviceId + "] of owner [" + owner + "]");
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode()).build();
        }
        return Response.ok().build();
    }

    @Path("device/stats/{deviceId}/sensors/temperature")
    @GET
    @Consumes("application/json")
    @Produces("application/json")
    public Response getRaspberryPiTemperatureStats(@PathParam("deviceId") String deviceId,
                                                   @QueryParam("username") String user,
                                                   @QueryParam("from") long from, @QueryParam("to") long to) {
        String fromDate = String.valueOf(from);
        String toDate = String.valueOf(to);
        String query = "owner:" + user + " AND deviceId:" + deviceId + " AND deviceType:" +
                       RaspberrypiConstants.DEVICE_TYPE + " AND time : [" + fromDate + " TO " + toDate + "]";
        String sensorTableName = RaspberrypiConstants.TEMPERATURE_EVENT_TABLE;
        try {
            List<SortByField> sortByFields = new ArrayList<>();
            SortByField sortByField = new SortByField("time", SORT.ASC, false);
            sortByFields.add(sortByField);
            List<SensorRecord> sensorRecords = APIUtil.getAllEventsForDevice(sensorTableName, query, sortByFields);
            return Response.status(Response.Status.OK.getStatusCode()).entity(sensorRecords).build();
        } catch (AnalyticsException e) {
            String errorMsg = "Error on retrieving stats on table " + sensorTableName + " with query " + query;
            log.error(errorMsg);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode()).entity(errorMsg).build();
        }
    }

    private boolean waitForServerStartup() {
        while (!IoTServerStartupListener.isServerReady()) {
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                return true;
            }
        }
        return false;
    }

    public RaspberryPiMQTTConnector getRaspberryPiMQTTConnector() {
        return raspberryPiMQTTConnector;
    }

    public void setRaspberryPiMQTTConnector(
            final RaspberryPiMQTTConnector raspberryPiMQTTConnector) {
        Runnable connector = new Runnable() {
            public void run() {
                if (waitForServerStartup()) {
                    return;
                }
                RaspberryPiControllerServiceImpl.this.raspberryPiMQTTConnector = raspberryPiMQTTConnector;
                if (MqttConfig.getInstance().isEnabled()) {
                    raspberryPiMQTTConnector.connect();
                } else {
                    log.warn("MQTT disabled in 'devicemgt-config.xml'. Hence, RaspberryPiMQTTConnector not started.");
                }
            }
        };
        Thread connectorThread = new Thread(connector);
        connectorThread.start();
    }

}
