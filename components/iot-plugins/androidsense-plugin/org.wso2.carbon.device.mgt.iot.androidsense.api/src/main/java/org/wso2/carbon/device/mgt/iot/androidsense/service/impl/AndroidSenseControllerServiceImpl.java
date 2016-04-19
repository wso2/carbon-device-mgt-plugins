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
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.device.mgt.iot.androidsense.service.impl.transport.AndroidSenseMQTTConnector;
import org.wso2.carbon.device.mgt.iot.androidsense.service.impl.util.APIUtil;
import org.wso2.carbon.device.mgt.iot.androidsense.service.impl.util.SensorRecord;
import org.wso2.carbon.device.mgt.iot.androidsense.plugin.constants.AndroidSenseConstants;
import org.wso2.carbon.device.mgt.iot.controlqueue.mqtt.MqttConfig;
import org.wso2.carbon.device.mgt.iot.service.IoTServerStartupListener;
import org.wso2.carbon.device.mgt.iot.transport.TransportHandlerException;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.List;

/**
 * The api for
 */
public class AndroidSenseControllerServiceImpl implements AndroidSenseControllerService {

    private static Log log = LogFactory.getLog(AndroidSenseControllerServiceImpl.class);
    private static AndroidSenseMQTTConnector androidSenseMQTTConnector;

    @Path("device/{deviceId}/words")
    @POST
    public Response sendKeyWords(@PathParam("deviceId") String deviceId, @FormParam("keywords") String keywords) {
        try {
            String username = PrivilegedCarbonContext.getThreadLocalCarbonContext().getUsername();
            androidSenseMQTTConnector.publishDeviceData(username, deviceId, "add", keywords);
            return Response.ok().build();
        } catch (TransportHandlerException e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode()).build();
        }
    }

    @Path("device/{deviceId}/words/threshold")
    @POST
    public Response sendThreshold(@PathParam("deviceId") String deviceId, @FormParam("threshold") String threshold) {
        try {
            String username = PrivilegedCarbonContext.getThreadLocalCarbonContext().getUsername();
            androidSenseMQTTConnector.publishDeviceData(username, deviceId, "threshold", threshold);
            return Response.ok().build();
        } catch (TransportHandlerException e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode()).build();
        }
    }

    @Path("device/{deviceId}/words")
    @DELETE
    public Response removeKeyWords(@PathParam("deviceId") String deviceId, @QueryParam("words") String words) {
        try {
            String username = PrivilegedCarbonContext.getThreadLocalCarbonContext().getUsername();
            androidSenseMQTTConnector.publishDeviceData(username, deviceId, "remove", words);
            return Response.ok().build();
        } catch (TransportHandlerException e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode()).build();
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
        String user = PrivilegedCarbonContext.getThreadLocalCarbonContext().getUsername();
        String query = "owner:" + user + " AND deviceId:" + deviceId + " AND deviceType:" +
                       AndroidSenseConstants.DEVICE_TYPE + " AND time : [" + fromDate + " TO " + toDate + "]";
        if (sensor.equals(AndroidSenseConstants.SENSOR_WORDCOUNT)) {
            query = "owner:" + user + " AND deviceId:" + deviceId;
        }
        String sensorTableName = getSensorEventTableName(sensor);
        List<SensorRecord> sensorDatas;
        try {
            if (sensor.equals(AndroidSenseConstants.SENSOR_WORDCOUNT)) {
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
                sensorEventTableName = "DevicePROXIMITYSummaryData";
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

    /**
     * Fetches the `AndroidSenseMQTTConnector` specific to this Android Sense controller service.
     *
     * @return the 'AndroidSenseMQTTConnector' instance bound to the 'AndroidSenseMQTTConnector' variable of
     * this service.
     */
    @SuppressWarnings("Unused")
    public AndroidSenseMQTTConnector getAndroidSenseMQTTConnector() {
        return androidSenseMQTTConnector;
    }

    /**
     * Sets the `AndroidSenseMQTTConnector` variable of this Android Sense controller service.
     *
     * @param androidSenseMQTTConnector a 'AndroidSenseMQTTConnector' object that handles all MQTT related
     *                                  communications of any connected Android Sense device-type
     */
    @SuppressWarnings("Unused")
    public void setAndroidSenseMQTTConnector(final AndroidSenseMQTTConnector androidSenseMQTTConnector) {
        Runnable connector = new Runnable() {
            public void run() {
                if (waitForServerStartup()) {
                    return;
                }
                //The delay is added till the server starts up.
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                AndroidSenseControllerServiceImpl.androidSenseMQTTConnector = androidSenseMQTTConnector;
                if (MqttConfig.getInstance().isEnabled()) {
                    synchronized (androidSenseMQTTConnector) {
                        androidSenseMQTTConnector.connect();
                    }
                } else {
                    log.warn("MQTT disabled in 'devicemgt-config.xml'. Hence, VirtualFireAlarmMQTTConnector not started.");
                }
            }
        };
        Thread connectorThread = new Thread(connector);
        connectorThread.setDaemon(true);
        connectorThread.start();
    }

    private boolean waitForServerStartup() {
        while (!IoTServerStartupListener.isServerReady()) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                return true;
            }
        }
        return false;
    }
}
