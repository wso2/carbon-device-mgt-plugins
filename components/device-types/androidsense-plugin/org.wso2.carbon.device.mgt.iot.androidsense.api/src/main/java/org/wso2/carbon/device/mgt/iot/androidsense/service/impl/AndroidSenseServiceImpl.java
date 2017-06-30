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

package org.wso2.carbon.device.mgt.iot.androidsense.service.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.analytics.dataservice.commons.SortByField;
import org.wso2.carbon.analytics.dataservice.commons.SortType;
import org.wso2.carbon.analytics.datasource.commons.exception.AnalyticsException;
import org.wso2.carbon.device.mgt.common.*;
import org.wso2.carbon.device.mgt.common.authorization.DeviceAccessAuthorizationException;
import org.wso2.carbon.device.mgt.common.configuration.mgt.ConfigurationManagementException;
import org.wso2.carbon.device.mgt.common.group.mgt.DeviceGroupConstants;
import org.wso2.carbon.device.mgt.iot.androidsense.service.impl.constants.AndroidSenseConstants;
import org.wso2.carbon.device.mgt.iot.androidsense.service.impl.util.APIUtil;
import org.wso2.carbon.device.mgt.iot.androidsense.service.impl.util.AndroidConfiguration;
import org.wso2.carbon.device.mgt.iot.androidsense.service.impl.util.SensorRecord;

import javax.ws.rs.*;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * This api is for Android Sense Device Type
 */
public class AndroidSenseServiceImpl implements AndroidSenseService {

    private static Log log = LogFactory.getLog(AndroidSenseServiceImpl.class);

    @Path("stats/{deviceId}/sensors/{sensorName}")
    @GET
    @Consumes("application/json")
    @Produces("application/json")
    public Response getAndroidSenseDeviceStats(@PathParam("deviceId") String deviceId, @PathParam("sensorName") String sensor,
                                        @QueryParam("from") long from, @QueryParam("to") long to) {
        String fromDate = String.valueOf(from);
        String toDate = String.valueOf(to);
        String query = "meta_deviceId:" + deviceId + " AND meta_timestamp : [" + fromDate + " TO " + toDate + "]";
        String sensorTableName = getSensorEventTableName(sensor);

        try {
            if (!APIUtil.getDeviceAccessAuthorizationService().isUserAuthorized(new DeviceIdentifier(deviceId,
                    AndroidSenseConstants.DEVICE_TYPE), DeviceGroupConstants.Permissions.DEFAULT_STATS_MONITOR_PERMISSIONS)) {
                return Response.status(Response.Status.UNAUTHORIZED.getStatusCode()).build();
            }
            List<SensorRecord> sensorDatas;
            List<SortByField> sortByFields = new ArrayList<>();
            SortByField sortByField = new SortByField("meta_timestamp", SortType.ASC);
            sortByFields.add(sortByField);
            sensorDatas = APIUtil.getAllEventsForDevice(sensorTableName, query, sortByFields);
            return Response.ok().entity(sensorDatas).build();
        } catch (AnalyticsException e) {
            String errorMsg = "Error on retrieving stats on table " + sensorTableName + " with query " + query;
            log.error(errorMsg);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode()).entity(errorMsg).build();
        } catch (DeviceAccessAuthorizationException e) {
            log.error(e.getErrorMessage());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode()).build();
        }
    }

    /**
     * get the event table from the sensor name.
     */
    private String getSensorEventTableName(String sensorName) {
        String sensorEventTableName;
        switch (sensorName) {
            case AndroidSenseConstants.SENSOR_ACCELEROMETER:
                sensorEventTableName = AndroidSenseConstants.SENSOR_ACCELEROMETER_TABLE;
                break;
            case AndroidSenseConstants.SENSOR_BATTERY:
                sensorEventTableName = AndroidSenseConstants.SENSOR_BATTERY_TABLE;
                break;
            case AndroidSenseConstants.SENSOR_GRAVITY:
                sensorEventTableName = AndroidSenseConstants.SENSOR_GRAVITY_TABLE;
                break;
            case AndroidSenseConstants.SENSOR_GYROSCOPE:
                sensorEventTableName = AndroidSenseConstants.SENSOR_GYROSCOPE_TABLE;
                break;
            case AndroidSenseConstants.SENSOR_LIGHT:
                sensorEventTableName = AndroidSenseConstants.SENSOR_LIGHT_TABLE;
                break;
            case AndroidSenseConstants.SENSOR_MAGNETIC:
                sensorEventTableName = AndroidSenseConstants.SENSOR_MAGNETIC_TABLE;
                break;
            case AndroidSenseConstants.SENSOR_PRESSURE:
                sensorEventTableName = AndroidSenseConstants.SENSOR_PRESSURE_TABLE;
                break;
            case AndroidSenseConstants.SENSOR_PROXIMITY:
                sensorEventTableName = AndroidSenseConstants.SENSOR_PROXIMITY_TABLE;
                break;
            case AndroidSenseConstants.SENSOR_ROTATION:
                sensorEventTableName = AndroidSenseConstants.SENSOR_ROTATION_TABLE;
                break;
            default:
                sensorEventTableName = "";
        }
        return sensorEventTableName;
    }

    @Path("device/{device_id}/register")
    @POST
    public Response register(@PathParam("device_id") String deviceId, @QueryParam("deviceName") String deviceName) {
        DeviceIdentifier deviceIdentifier = new DeviceIdentifier();
        deviceIdentifier.setId(deviceId);
        deviceIdentifier.setType(AndroidSenseConstants.DEVICE_TYPE);
        try {
            if (APIUtil.getDeviceManagementService().isEnrolled(deviceIdentifier)) {
                AndroidConfiguration androidConfiguration = new AndroidConfiguration();
                androidConfiguration.setTenantDomain(APIUtil.getAuthenticatedUserTenantDomain());
                androidConfiguration.setMqttEndpoint(APIUtil.getMqttEndpoint());
                return Response.status(Response.Status.ACCEPTED.getStatusCode()).entity(androidConfiguration.toString())
                        .build();
            }
            Device device = new Device();
            device.setDeviceIdentifier(deviceId);
            EnrolmentInfo enrolmentInfo = new EnrolmentInfo();
            enrolmentInfo.setDateOfEnrolment(new Date().getTime());
            enrolmentInfo.setDateOfLastUpdate(new Date().getTime());
            enrolmentInfo.setStatus(EnrolmentInfo.Status.ACTIVE);
            device.setName(deviceName);
            device.setType(AndroidSenseConstants.DEVICE_TYPE);
            enrolmentInfo.setOwner(APIUtil.getAuthenticatedUser());
            enrolmentInfo.setOwnership(EnrolmentInfo.OwnerShip.BYOD);
            device.setEnrolmentInfo(enrolmentInfo);
            boolean added = APIUtil.getDeviceManagementService().enrollDevice(device);
            if (added) {
                AndroidConfiguration androidConfiguration = new AndroidConfiguration();
                androidConfiguration.setTenantDomain(APIUtil.getAuthenticatedUserTenantDomain());
                androidConfiguration.setMqttEndpoint(APIUtil.getMqttEndpoint());
                return Response.ok(androidConfiguration.toString()).build();
            } else {
                return Response.status(Response.Status.NOT_ACCEPTABLE.getStatusCode()).entity(false).build();
            }
        } catch (DeviceManagementException e) {
            log.error(e.getMessage(), e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode()).entity(false).build();
        } catch (ConfigurationManagementException e) {
            log.error(e.getMessage(), e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode()).entity(false).build();
        }
    }
}