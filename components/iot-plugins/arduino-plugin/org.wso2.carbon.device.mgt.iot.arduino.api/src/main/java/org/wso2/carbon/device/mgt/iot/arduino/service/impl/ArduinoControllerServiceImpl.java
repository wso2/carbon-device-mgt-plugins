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
import org.wso2.carbon.device.mgt.common.DeviceIdentifier;
import org.wso2.carbon.device.mgt.common.authorization.DeviceAccessAuthorizationException;
import org.wso2.carbon.device.mgt.common.group.mgt.DeviceGroupConstants;
import org.wso2.carbon.device.mgt.iot.arduino.service.impl.dto.SensorRecord;
import org.wso2.carbon.device.mgt.iot.arduino.service.impl.util.APIUtil;
import org.wso2.carbon.device.mgt.iot.arduino.plugin.constants.ArduinoConstants;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
public class ArduinoControllerServiceImpl implements ArduinoControllerService {

    private static Log log = LogFactory.getLog(ArduinoControllerServiceImpl.class);
    private static Map<String, LinkedList<String>> internalControlsQueue = new HashMap<>();

    @Override
    @Path("device/{deviceId}/bulb")
    @POST
    public Response switchBulb(@PathParam("deviceId") String deviceId, @QueryParam("state") String state) {
        try {
            if (!APIUtil.getDeviceAccessAuthorizationService().isUserAuthorized(new DeviceIdentifier(deviceId,
                        ArduinoConstants.DEVICE_TYPE), DeviceGroupConstants.Permissions.DEFAULT_OPERATOR_PERMISSIONS)) {
                return Response.status(Response.Status.UNAUTHORIZED.getStatusCode()).build();
            }
            LinkedList<String> deviceControlList = internalControlsQueue.get(deviceId);
            String operation = "BULB:" + state.toUpperCase();
            if (deviceControlList == null) {
                deviceControlList = new LinkedList<>();
                deviceControlList.add(operation);
                internalControlsQueue.put(deviceId, deviceControlList);
            } else {
                deviceControlList.add(operation);
            }
            return Response.status(Response.Status.OK.getStatusCode()).build();
        } catch (DeviceAccessAuthorizationException e) {
            log.error(e.getErrorMessage(), e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
    }

    @Override
    @Path("device/{deviceId}/controls")
    @GET
    public Response readControls(@PathParam("deviceId") String deviceId) {
        try {
            if (!APIUtil.getDeviceAccessAuthorizationService().isUserAuthorized(new DeviceIdentifier(deviceId,
                    ArduinoConstants.DEVICE_TYPE), DeviceGroupConstants.Permissions.DEFAULT_OPERATOR_PERMISSIONS)) {
                return Response.status(Response.Status.UNAUTHORIZED.getStatusCode()).build();
            }
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
        } catch (DeviceAccessAuthorizationException e) {
            log.error(e.getErrorMessage(), e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
    }

    @Override
    @Path("device/stats/{deviceId}")
    @GET
    @Consumes("application/json")
    @Produces("application/json")
    public Response getArduinoTemperatureStats(@PathParam("deviceId") String deviceId, @QueryParam("from") long from,
                                               @QueryParam("to") long to) {
        try {
            if (!APIUtil.getDeviceAccessAuthorizationService().isUserAuthorized(new DeviceIdentifier(deviceId,
                   ArduinoConstants.DEVICE_TYPE), DeviceGroupConstants.Permissions.DEFAULT_STATS_MONITOR_PERMISSIONS)) {
                return Response.status(Response.Status.UNAUTHORIZED.getStatusCode()).build();
            }
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
        } catch (DeviceAccessAuthorizationException e) {
            log.error(e.getErrorMessage(), e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
    }

}
