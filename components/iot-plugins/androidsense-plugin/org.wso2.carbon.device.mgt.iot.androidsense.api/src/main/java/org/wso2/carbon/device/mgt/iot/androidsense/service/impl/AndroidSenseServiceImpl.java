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
import org.wso2.carbon.analytics.dataservice.commons.SORT;
import org.wso2.carbon.analytics.dataservice.commons.SortByField;
import org.wso2.carbon.analytics.datasource.commons.exception.AnalyticsException;
import org.wso2.carbon.device.mgt.common.Device;
import org.wso2.carbon.device.mgt.common.DeviceIdentifier;
import org.wso2.carbon.device.mgt.common.DeviceManagementException;
import org.wso2.carbon.device.mgt.common.EnrolmentInfo;
import org.wso2.carbon.device.mgt.common.authorization.DeviceAccessAuthorizationException;
import org.wso2.carbon.device.mgt.common.configuration.mgt.ConfigurationManagementException;
import org.wso2.carbon.device.mgt.common.group.mgt.DeviceGroupConstants;
import org.wso2.carbon.device.mgt.common.operation.mgt.Operation;
import org.wso2.carbon.device.mgt.common.operation.mgt.OperationManagementException;
import org.wso2.carbon.device.mgt.core.operation.mgt.CommandOperation;
import org.wso2.carbon.device.mgt.iot.androidsense.service.impl.util.APIUtil;
import org.wso2.carbon.device.mgt.iot.androidsense.service.impl.util.AndroidConfiguration;
import org.wso2.carbon.device.mgt.iot.androidsense.service.impl.util.Constants;
import org.wso2.carbon.device.mgt.iot.androidsense.service.impl.util.SensorRecord;
import org.wso2.carbon.device.mgt.iot.androidsense.plugin.constants.AndroidSenseConstants;
import org.wso2.carbon.device.mgt.iot.util.Utils;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * The api for
 */
public class AndroidSenseServiceImpl implements AndroidSenseService {

    private static Log log = LogFactory.getLog(AndroidSenseServiceImpl.class);
    private static String DEFAULT_MQTT_ENDPOINT = "tcp://localhost:1883";

    @Path("device/{deviceId}/words")
    @POST
    public Response sendKeyWords(@PathParam("deviceId") String deviceId, @QueryParam("keywords") String keywords) {
        try {
            if (!APIUtil.getDeviceAccessAuthorizationService().isUserAuthorized(new DeviceIdentifier(deviceId,
                      AndroidSenseConstants.DEVICE_TYPE))) {
                return Response.status(Response.Status.UNAUTHORIZED.getStatusCode()).build();
            }
            String publishTopic = APIUtil.getAuthenticatedUserTenantDomain()
                    + "/" + AndroidSenseConstants.DEVICE_TYPE + "/" + deviceId + "/command/words";

            Operation commandOp = new CommandOperation();
            commandOp.setCode("keywords");
            commandOp.setType(Operation.Type.COMMAND);
            commandOp.setEnabled(true);
            commandOp.setPayLoad(keywords);

            Properties props = new Properties();
            props.setProperty(AndroidSenseConstants.MQTT_ADAPTER_TOPIC_PROPERTY_NAME, publishTopic);
            commandOp.setProperties(props);

            List<DeviceIdentifier> deviceIdentifiers = new ArrayList<>();
            deviceIdentifiers.add(new DeviceIdentifier(deviceId, AndroidSenseConstants.DEVICE_TYPE));
            APIUtil.getDeviceManagementService().addOperation(AndroidSenseConstants.DEVICE_TYPE, commandOp,
                                                              deviceIdentifiers);
            return Response.ok().build();
        } catch (DeviceAccessAuthorizationException e) {
            log.error(e.getErrorMessage(), e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode()).build();
        } catch (OperationManagementException e) {
            String msg = "Error occurred while executing command operation to send keywords";
            log.error(msg, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
    }

    @Path("device/{deviceId}/words/threshold")
    @POST
    public Response sendThreshold(@PathParam("deviceId") String deviceId, @QueryParam("threshold") String threshold) {
        try {
            if (!APIUtil.getDeviceAccessAuthorizationService().isUserAuthorized(new DeviceIdentifier(deviceId,
                    AndroidSenseConstants.DEVICE_TYPE), DeviceGroupConstants.Permissions.DEFAULT_OPERATOR_PERMISSIONS)) {
                return Response.status(Response.Status.UNAUTHORIZED.getStatusCode()).build();
            }
            String publishTopic = APIUtil.getAuthenticatedUserTenantDomain()
                    + "/" + AndroidSenseConstants.DEVICE_TYPE + "/" + deviceId + "/command/threshold";

            Operation commandOp = new CommandOperation();
            commandOp.setCode("threshold");
            commandOp.setType(Operation.Type.COMMAND);
            commandOp.setEnabled(true);
            commandOp.setPayLoad(threshold);

            Properties props = new Properties();
            props.setProperty(AndroidSenseConstants.MQTT_ADAPTER_TOPIC_PROPERTY_NAME, publishTopic);
            commandOp.setProperties(props);

            List<DeviceIdentifier> deviceIdentifiers = new ArrayList<>();
            deviceIdentifiers.add(new DeviceIdentifier(deviceId, AndroidSenseConstants.DEVICE_TYPE));
            APIUtil.getDeviceManagementService().addOperation(AndroidSenseConstants.DEVICE_TYPE, commandOp,
                                                              deviceIdentifiers);
            return Response.ok().build();
        } catch (DeviceAccessAuthorizationException e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode()).build();
        } catch (OperationManagementException e) {
            String msg = "Error occurred while executing command operation to set threashold";
            log.error(msg, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
    }

    @Path("device/{deviceId}/words")
    @DELETE
    public Response removeKeyWords(@PathParam("deviceId") String deviceId, @QueryParam("words") String words) {
        try {
            if (!APIUtil.getDeviceAccessAuthorizationService().isUserAuthorized(new DeviceIdentifier(deviceId,
                AndroidSenseConstants.DEVICE_TYPE), DeviceGroupConstants.Permissions.DEFAULT_OPERATOR_PERMISSIONS)) {
                return Response.status(Response.Status.UNAUTHORIZED.getStatusCode()).build();
            }
            String publishTopic = APIUtil.getAuthenticatedUserTenantDomain()
                    + "/" + AndroidSenseConstants.DEVICE_TYPE + "/" + deviceId + "/command/remove";

            Operation commandOp = new CommandOperation();
            commandOp.setCode("remove");
            commandOp.setType(Operation.Type.COMMAND);
            commandOp.setEnabled(true);
            commandOp.setPayLoad(words);

            Properties props = new Properties();
            props.setProperty(AndroidSenseConstants.MQTT_ADAPTER_TOPIC_PROPERTY_NAME, publishTopic);
            commandOp.setProperties(props);

            List<DeviceIdentifier> deviceIdentifiers = new ArrayList<>();
            deviceIdentifiers.add(new DeviceIdentifier(deviceId, AndroidSenseConstants.DEVICE_TYPE));
            APIUtil.getDeviceManagementService().addOperation(AndroidSenseConstants.DEVICE_TYPE, commandOp,
                                                              deviceIdentifiers);
            return Response.ok().build();
        } catch (DeviceAccessAuthorizationException e) {
            log.error(e.getErrorMessage(), e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode()).build();
        } catch (OperationManagementException e) {
            String msg = "Error occurred while executing command operation to remove words";
            log.error(msg, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
    }

    @Path("stats/{deviceId}/sensors/{sensorName}")
    @GET
    @Consumes("application/json")
    @Produces("application/json")
    public Response getAndroidSenseDeviceStats(@PathParam("deviceId") String deviceId, @PathParam("sensorName") String sensor,
                                        @QueryParam("from") long from, @QueryParam("to") long to) {
        String fromDate = String.valueOf(from);
        String toDate = String.valueOf(to);
        String query = "deviceId:" + deviceId + " AND deviceType:" +
                AndroidSenseConstants.DEVICE_TYPE + " AND time : [" + fromDate + " TO " + toDate + "]";
        if (sensor.equals(AndroidSenseConstants.SENSOR_WORDCOUNT)) {
            query = "deviceId:" + deviceId;
        }
        String sensorTableName = getSensorEventTableName(sensor);

        try {
            if (!APIUtil.getDeviceAccessAuthorizationService().isUserAuthorized(new DeviceIdentifier(deviceId,
                    AndroidSenseConstants.DEVICE_TYPE), DeviceGroupConstants.Permissions.DEFAULT_STATS_MONITOR_PERMISSIONS)) {
                return Response.status(Response.Status.UNAUTHORIZED.getStatusCode()).build();
            }
            List<SensorRecord> sensorDatas;
            if (!sensor.equals(AndroidSenseConstants.SENSOR_WORDCOUNT)) {
                List<SortByField> sortByFields = new ArrayList<>();
                SortByField sortByField = new SortByField("time", SORT.ASC, false);
                sortByFields.add(sortByField);
                sensorDatas = APIUtil.getAllEventsForDevice(sensorTableName, query, sortByFields);
            } else {
                sensorDatas = APIUtil.getAllEventsForDevice(sensorTableName, query, null);
            }
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
                sensorEventTableName = "DEVICE_ACCELEROMETER_SUMMARY";
                break;
            case AndroidSenseConstants.SENSOR_BATTERY:
                sensorEventTableName = "DEVICE_BATTERY_SUMMARY";
                break;
            case AndroidSenseConstants.SENSOR_GPS:
                sensorEventTableName = "DEVICE_GPS_SUMMARY";
                break;
            case AndroidSenseConstants.SENSOR_GRAVITY:
                sensorEventTableName = "DEVICE_GRAVITY_SUMMARY";
                break;
            case AndroidSenseConstants.SENSOR_GYROSCOPE:
                sensorEventTableName = "DEVICE_GYROSCOPE_SUMMARY";
                break;
            case AndroidSenseConstants.SENSOR_LIGHT:
                sensorEventTableName = "DEVICE_LIGHT_SUMMARY";
                break;
            case AndroidSenseConstants.SENSOR_MAGNETIC:
                sensorEventTableName = "DEVICE_MAGNETIC_SUMMARY";
                break;
            case AndroidSenseConstants.SENSOR_PRESSURE:
                sensorEventTableName = "DEVICE_PRESSURE_SUMMARY";
                break;
            case AndroidSenseConstants.SENSOR_PROXIMITY:
                sensorEventTableName = "DEVICE_PROXIMITY_SUMMARY";
                break;
            case AndroidSenseConstants.SENSOR_ROTATION:
                sensorEventTableName = "DEVICE_ROTATION_SUMMARY";
                break;
            case AndroidSenseConstants.SENSOR_WORDCOUNT:
                sensorEventTableName = "WORD_COUNT_SUMMARY";
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
                String mqttEndpoint = DEFAULT_MQTT_ENDPOINT;
                if (mqttEndpoint.contains(Constants.LOCALHOST)) {
                    mqttEndpoint = mqttEndpoint.replace(Constants.LOCALHOST, Utils.getServerUrl());
                }
                androidConfiguration.setMqttEndpoint(mqttEndpoint);
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
