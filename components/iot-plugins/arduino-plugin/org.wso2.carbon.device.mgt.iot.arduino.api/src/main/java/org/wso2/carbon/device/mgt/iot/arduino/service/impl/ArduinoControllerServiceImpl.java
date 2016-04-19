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

package org.wso2.carbon.device.mgt.iot.arduino.service.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.analytics.dataservice.commons.SORT;
import org.wso2.carbon.analytics.dataservice.commons.SortByField;
import org.wso2.carbon.analytics.datasource.commons.exception.AnalyticsException;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.device.mgt.iot.arduino.service.impl.dto.DeviceData;
import org.wso2.carbon.device.mgt.iot.arduino.service.impl.dto.SensorRecord;
import org.wso2.carbon.device.mgt.iot.arduino.service.impl.util.APIUtil;
import org.wso2.carbon.device.mgt.iot.arduino.service.impl.util.ArduinoServiceUtils;
import org.wso2.carbon.device.mgt.iot.arduino.plugin.constants.ArduinoConstants;
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
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.concurrent.ConcurrentHashMap;

public class ArduinoControllerServiceImpl implements ArduinoControllerService {

    private static Log log = LogFactory.getLog(ArduinoControllerServiceImpl.class);
    private static Map<String, LinkedList<String>> internalControlsQueue = new HashMap<>();
    private ConcurrentHashMap<String, String> deviceToIpMap = new ConcurrentHashMap<>();

    @Override
    @Path("device/register/{deviceId}/{ip}/{port}")
    @POST
    public Response registerDeviceIP(@PathParam("deviceId") String deviceId, @PathParam("ip") String deviceIP,
                                     @PathParam("port") String devicePort, @Context HttpServletRequest request) {
        String result;
        if (log.isDebugEnabled()) {
            log.debug("Got register call from IP: " + deviceIP + " for Device ID: " + deviceId + " of owner: ");
        }
        String deviceHttpEndpoint = deviceIP + ":" + devicePort;
        deviceToIpMap.put(deviceId, deviceHttpEndpoint);
        result = "Device-IP Registered";
        if (log.isDebugEnabled()) {
            log.debug(result);
        }
        return Response.ok(result).build();
    }

    @Override
    @Path("device/{deviceId}/bulb")
    @POST
    public Response switchBulb(@PathParam("deviceId") String deviceId, @QueryParam("protocol") String protocol,
                               @FormParam("state") String state) {

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
        return Response.status(Response.Status.OK.getStatusCode()).build();
    }

    @Override
    @Path("device/sensor")
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public Response pushData(DeviceData dataMsg) {
        String owner = PrivilegedCarbonContext.getThreadLocalCarbonContext().getUsername();
        String deviceId = dataMsg.deviceId;
        float pinData = dataMsg.value;
        if (!ArduinoServiceUtils.publishToDAS(dataMsg.deviceId, dataMsg.value)) {
            log.warn("An error occured whilst trying to publish pin data of Arduino with ID [" +
                     deviceId + "] of owner [" + owner + "]");
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode()).build();
        }
        return Response.status(Response.Status.OK.getStatusCode()).build();
    }

    @Override
    @Path("device/{deviceId}/controls")
    @GET
    public Response readControls(@PathParam("deviceId") String deviceId, @QueryParam("protocol") String protocol) {
        String result;
        LinkedList<String> deviceControlList = internalControlsQueue.get(deviceId);
        String owner = PrivilegedCarbonContext.getThreadLocalCarbonContext().getUsername();
        if (deviceControlList == null) {
            result = "No controls have been set for device " + deviceId + " of owner " + owner;
            if (log.isDebugEnabled()) {
                log.debug(result);
            }
            return Response.status(Response.Status.CONFLICT.getStatusCode()).entity(result).build();
        } else {
            try {
                result = deviceControlList.remove();
                if (log.isDebugEnabled()) {
                    log.debug(result);
                }
                return Response.status(Response.Status.ACCEPTED.getStatusCode()).entity(result).build();
            } catch (NoSuchElementException ex) {
                result = "There are no more controls for device " + deviceId + " of owner " + owner;
                if (log.isDebugEnabled()) {
                    log.debug(result);
                }
                return Response.status(Response.Status.NO_CONTENT.getStatusCode()).entity(result).build();
            }
        }
    }

    @Override
    @Path("device/temperature")
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public Response pushTemperatureData(final DeviceData dataMsg, @Context HttpServletRequest request) {
        String owner = PrivilegedCarbonContext.getThreadLocalCarbonContext().getUsername();
        String deviceId = dataMsg.deviceId;
        float temperature = dataMsg.value;
        if (!ArduinoServiceUtils.publishToDAS(dataMsg.deviceId, dataMsg.value)) {
            log.warn("An error occured whilst trying to publish temperature data of Arduino with ID [" + deviceId +
                     "] of owner [" + owner + "]");
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode()).build();
        }
        return Response.status(Response.Status.OK.getStatusCode()).build();
    }

    @Override
    @Path("device/stats/{deviceId}/sensors/temperature")
    @GET
    @Consumes("application/json")
    @Produces("application/json")
    public Response getArduinoTemperatureStats(@PathParam("deviceId") String deviceId, @QueryParam("from") long from,
                                               @QueryParam("to") long to) {
        String fromDate = String.valueOf(from);
        String toDate = String.valueOf(to);
        String query = "deviceId:" + deviceId + " AND deviceType:" +
                       ArduinoConstants.DEVICE_TYPE + " AND time : [" + fromDate + " TO " + toDate + "]";
        String sensorTableName = ArduinoConstants.TEMPERATURE_EVENT_TABLE;
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

}
