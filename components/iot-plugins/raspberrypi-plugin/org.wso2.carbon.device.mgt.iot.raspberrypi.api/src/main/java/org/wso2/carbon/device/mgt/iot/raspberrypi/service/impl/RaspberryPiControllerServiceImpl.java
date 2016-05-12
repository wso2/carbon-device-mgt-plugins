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
import org.wso2.carbon.device.mgt.common.DeviceIdentifier;
import org.wso2.carbon.device.mgt.common.authorization.DeviceAccessAuthorizationException;
import org.wso2.carbon.device.mgt.common.group.mgt.DeviceGroupConstants;
import org.wso2.carbon.device.mgt.iot.raspberrypi.service.impl.dto.SensorRecord;
import org.wso2.carbon.device.mgt.iot.raspberrypi.service.impl.util.APIUtil;
import org.wso2.carbon.device.mgt.iot.raspberrypi.plugin.constants.RaspberrypiConstants;

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
import java.util.List;
import java.util.Map;

public class RaspberryPiControllerServiceImpl implements RaspberryPiControllerService {

    private static Log log = LogFactory.getLog(RaspberryPiControllerServiceImpl.class);

    @Path("device/{deviceId}/bulb")
    @POST
    public Response switchBulb(@PathParam("deviceId") String deviceId, @QueryParam("state") String state) {
        try {
            if (!APIUtil.getDeviceAccessAuthorizationService().isUserAuthorized(new DeviceIdentifier(deviceId,
                RaspberrypiConstants.DEVICE_TYPE), DeviceGroupConstants.Permissions.DEFAULT_OPERATOR_PERMISSIONS)) {
                return Response.status(Response.Status.UNAUTHORIZED.getStatusCode()).build();
            }
            String switchToState = state.toUpperCase();
            if (!switchToState.equals(RaspberrypiConstants.STATE_ON) && !switchToState.equals(
                    RaspberrypiConstants.STATE_OFF)) {
                log.error("The requested state change shoud be either - 'ON' or 'OFF'");
                return Response.status(Response.Status.BAD_REQUEST.getStatusCode()).build();
            }
            String actualMessage = RaspberrypiConstants.BULB_CONTEXT + ":" + state;
            Map<String, String> dynamicProperties = new HashMap<>();
            String publishTopic = APIUtil.getTenantDomainOftheUser() + "/"
                    + RaspberrypiConstants.DEVICE_TYPE + "/" + deviceId;
            dynamicProperties.put(RaspberrypiConstants.ADAPTER_TOPIC_PROPERTY, publishTopic);
            APIUtil.getOutputEventAdapterService().publish(RaspberrypiConstants.MQTT_ADAPTER_NAME,
                                                           dynamicProperties, actualMessage);
            return Response.ok().build();
        } catch (DeviceAccessAuthorizationException e) {
            log.error(e.getErrorMessage(), e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
    }

    @Path("device/stats/{deviceId}")
    @GET
    @Consumes("application/json")
    @Produces("application/json")
    public Response getRaspberryPiTemperatureStats(@PathParam("deviceId") String deviceId,
                                                   @QueryParam("from") long from, @QueryParam("to") long to) {
        String fromDate = String.valueOf(from);
        String toDate = String.valueOf(to);
        String query = "deviceId:" + deviceId + " AND deviceType:" +
                RaspberrypiConstants.DEVICE_TYPE + " AND time : [" + fromDate + " TO " + toDate + "]";
        String sensorTableName = RaspberrypiConstants.TEMPERATURE_EVENT_TABLE;
        try {
            if (!APIUtil.getDeviceAccessAuthorizationService().isUserAuthorized(new DeviceIdentifier(deviceId,
                    RaspberrypiConstants.DEVICE_TYPE), DeviceGroupConstants.Permissions.DEFAULT_STATS_MONITOR_PERMISSIONS)) {
                return Response.status(Response.Status.UNAUTHORIZED.getStatusCode()).build();
            }
            List<SortByField> sortByFields = new ArrayList<>();
            SortByField sortByField = new SortByField("time", SORT.ASC, false);
            sortByFields.add(sortByField);
            List<SensorRecord> sensorRecords = APIUtil.getAllEventsForDevice(sensorTableName, query, sortByFields);
            return Response.status(Response.Status.OK.getStatusCode()).entity(sensorRecords).build();
        } catch (AnalyticsException e) {
            String errorMsg = "Error on retrieving stats on table " + sensorTableName + " with query " + query;
            log.error(errorMsg);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode()).entity(errorMsg).build();
        } catch (DeviceAccessAuthorizationException e) {
            log.error(e.getErrorMessage(), e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
    }

}
