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
import org.wso2.carbon.device.mgt.analytics.data.publisher.exception.DataPublisherConfigurationException;
import org.wso2.carbon.device.mgt.analytics.data.publisher.service.EventsPublisherService;
import org.wso2.carbon.device.mgt.iot.androidsense.service.impl.transport.AndroidSenseMQTTConnector;
import org.wso2.carbon.device.mgt.iot.androidsense.service.impl.util.APIUtil;
import org.wso2.carbon.device.mgt.iot.androidsense.service.impl.util.DeviceData;
import org.wso2.carbon.device.mgt.iot.androidsense.service.impl.util.SensorData;
import org.wso2.carbon.device.mgt.iot.androidsense.service.impl.util.SensorRecord;
import org.wso2.carbon.device.mgt.iot.androidsense.plugin.constants.AndroidSenseConstants;
import org.wso2.carbon.device.mgt.iot.controlqueue.mqtt.MqttConfig;
import org.wso2.carbon.device.mgt.iot.exception.DeviceControllerException;
import org.wso2.carbon.device.mgt.iot.sensormgt.SensorDataManager;
import org.wso2.carbon.device.mgt.iot.service.IoTServerStartupListener;
import org.wso2.carbon.device.mgt.iot.transport.TransportHandlerException;

import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.List;

/**
 * The api for
 */
public class AndroidSenseControllerServiceImpl implements AndroidSenseControllerService {

    private static Log log = LogFactory.getLog(AndroidSenseControllerServiceImpl.class);
    private static AndroidSenseMQTTConnector androidSenseMQTTConnector;

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

    public Response addSensorData(DeviceData dataMsg) {
        PrivilegedCarbonContext ctx = PrivilegedCarbonContext.getThreadLocalCarbonContext();
        EventsPublisherService deviceAnalyticsService = (EventsPublisherService) ctx
                .getOSGiService(EventsPublisherService.class, null);
        SensorData[] sensorData = dataMsg.values;
        String streamDef = null;
        Object payloadData[] = null;
        String sensorName = null;
        for (SensorData sensor : sensorData) {
            switch (sensor.key) {
                case "battery":
                    streamDef = AndroidSenseConstants.BATTERY_STREAM_DEFINITION;
                    payloadData = new Float[]{Float.parseFloat(sensor.value)};
                    sensorName = AndroidSenseConstants.SENSOR_BATTERY;
                    break;
                case "GPS":
                    streamDef = AndroidSenseConstants.GPS_STREAM_DEFINITION;
                    String gpsValue = sensor.value;
                    String gpsValuesString[] = gpsValue.split(",");
                    Float gpsValues[] = new Float[2];
                    gpsValues[0] = Float.parseFloat(gpsValuesString[0]);
                    gpsValues[1] = Float.parseFloat(gpsValuesString[0]);
                    payloadData = gpsValues;
                    sensorName = AndroidSenseConstants.SENSOR_GPS;
                    break;
                default:
                    try {
                        int androidSensorId = Integer.parseInt(sensor.key);
                        String value = sensor.value;
                        String sensorValueString[] = value.split(",");
                        Float sensorValues[] = new Float[1];
                        switch (androidSensorId) {
                            case 1:
                                streamDef = AndroidSenseConstants.ACCELEROMETER_STREAM_DEFINITION;
                                sensorValues[0] = Float.parseFloat(sensorValueString[0]) *
                                                  Float.parseFloat(sensorValueString[0]) * Float.parseFloat(sensorValueString[0]);
                                payloadData = sensorValues;
                                sensorName = AndroidSenseConstants.SENSOR_ACCELEROMETER;
                                break;
                            case 2:
                                streamDef = AndroidSenseConstants.MAGNETIC_STREAM_DEFINITION;
                                sensorValues[0] = Float.parseFloat(sensorValueString[0]) *
                                                  Float.parseFloat(sensorValueString[0]) * Float.parseFloat(sensorValueString[0]);
                                payloadData = sensorValues;
                                sensorName = AndroidSenseConstants.SENSOR_MAGNETIC;
                                break;
                            case 4:
                                streamDef = AndroidSenseConstants.GYROSCOPE_STREAM_DEFINITION;
                                sensorValues[0] = Float.parseFloat(sensorValueString[0]) *
                                                  Float.parseFloat(sensorValueString[0]) * Float.parseFloat(sensorValueString[0]);
                                payloadData = sensorValues;
                                sensorName = AndroidSenseConstants.SENSOR_GYROSCOPE;
                                break;
                            case 5:
                                streamDef = AndroidSenseConstants.LIGHT_STREAM_DEFINITION;
                                sensorName = AndroidSenseConstants.SENSOR_LIGHT;
                                payloadData = new Float[]{Float.parseFloat(sensorValueString[0])};
                                break;
                            case 6:
                                streamDef = AndroidSenseConstants.PRESSURE_STREAM_DEFINITION;
                                sensorName = AndroidSenseConstants.SENSOR_PRESSURE;
                                payloadData = new Float[]{Float.parseFloat(sensorValueString[0])};
                                break;
                            case 8:
                                streamDef = AndroidSenseConstants.PROXIMITY_STREAM_DEFINITION;
                                sensorName = AndroidSenseConstants.SENSOR_PROXIMITY;
                                payloadData = new Float[]{Float.parseFloat(sensorValueString[0])};
                                break;
                            case 9:
                                streamDef = AndroidSenseConstants.GRAVITY_STREAM_DEFINITION;
                                sensorValues[0] = Float.parseFloat(sensorValueString[0]) *
                                                  Float.parseFloat(sensorValueString[0]) * Float.parseFloat(sensorValueString[0]);
                                payloadData = sensorValues;
                                sensorName = AndroidSenseConstants.SENSOR_GRAVITY;
                                break;
                            case 11:
                                streamDef = AndroidSenseConstants.ROTATION_STREAM_DEFINITION;
                                sensorValues[0] = Float.parseFloat(sensorValueString[0]) *
                                                  Float.parseFloat(sensorValueString[0]) * Float.parseFloat(sensorValueString[0]);
                                payloadData = sensorValues;
                                sensorName = AndroidSenseConstants.SENSOR_ROTATION;
                                break;
                        }
                    } catch (NumberFormatException e) {
                        log.error("Invalid sensor value is sent from the device");
                        continue;
                    }
            }
            Object metaData[] = {dataMsg.owner, AndroidSenseConstants.DEVICE_TYPE, dataMsg.deviceId, sensor.time};
            if (streamDef != null && payloadData != null && payloadData.length > 0) {
                try {
                    SensorDataManager.getInstance()
                            .setSensorRecord(dataMsg.deviceId, sensorName, sensor.value, sensor.time);
                    deviceAnalyticsService.publishEvent(streamDef, "1.0.0", metaData, new Object[0], payloadData);
                } catch (DataPublisherConfigurationException e) {
                    return Response.status(Response.Status.UNSUPPORTED_MEDIA_TYPE.getStatusCode()).build();
                }
            }
        }
        return Response.ok().build();
    }

    public Response getLightData(String deviceId) {
        try {
            org.wso2.carbon.device.mgt.iot.sensormgt.SensorRecord sensorRecord = SensorDataManager.getInstance().getSensorRecord(deviceId, AndroidSenseConstants
                    .SENSOR_LIGHT);
            return Response.ok().entity(sensorRecord).build();
        } catch (DeviceControllerException e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode()).build();
        }
    }

    public Response getBattery(String deviceId) {
        try {
            org.wso2.carbon.device.mgt.iot.sensormgt.SensorRecord sensorRecord = SensorDataManager.getInstance().getSensorRecord(deviceId, AndroidSenseConstants
                    .SENSOR_BATTERY);
            return Response.ok().entity(sensorRecord).build();
        } catch (DeviceControllerException e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode()).build();
        }
    }

    public Response getGPS(String deviceId) {
        try {
            org.wso2.carbon.device.mgt.iot.sensormgt.SensorRecord sensorRecord = SensorDataManager.getInstance().getSensorRecord(deviceId, AndroidSenseConstants
                    .SENSOR_GPS);
            return Response.ok().entity(sensorRecord).build();
        } catch (DeviceControllerException e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode()).build();
        }
    }

    public Response readMagnetic(String deviceId) {
        try {
            org.wso2.carbon.device.mgt.iot.sensormgt.SensorRecord sensorRecord = SensorDataManager.getInstance().getSensorRecord(deviceId, AndroidSenseConstants
                    .SENSOR_MAGNETIC);
            return Response.ok().entity(sensorRecord).build();
        } catch (DeviceControllerException e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode()).build();
        }
    }

    public Response readAccelerometer(String deviceId) {
        try {
            org.wso2.carbon.device.mgt.iot.sensormgt.SensorRecord sensorRecord = SensorDataManager.getInstance().getSensorRecord(deviceId,
                                                                           AndroidSenseConstants.SENSOR_ACCELEROMETER);
            return Response.ok().entity(sensorRecord).build();
        } catch (DeviceControllerException e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode()).build();
        }
    }

    public Response readRotation(String deviceId) {
        try {
            org.wso2.carbon.device.mgt.iot.sensormgt.SensorRecord sensorRecord = SensorDataManager.getInstance().getSensorRecord(deviceId,
                                                                           AndroidSenseConstants.SENSOR_ROTATION);
            return Response.ok().entity(sensorRecord).build();
        } catch (DeviceControllerException e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode()).build();
        }
    }

    public Response readProximity(String deviceId) {
        try {
            org.wso2.carbon.device.mgt.iot.sensormgt.SensorRecord sensorRecord = SensorDataManager.getInstance().getSensorRecord(deviceId,
                                                                           AndroidSenseConstants.SENSOR_PROXIMITY);
            return Response.ok().entity(sensorRecord).build();
        } catch (DeviceControllerException e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode()).build();
        }
    }

    public Response readGyroscope(String deviceId) {
        try {
            org.wso2.carbon.device.mgt.iot.sensormgt.SensorRecord sensorRecord = SensorDataManager.getInstance().getSensorRecord(deviceId,
                                                                           AndroidSenseConstants.SENSOR_GYROSCOPE);
            return Response.ok().entity(sensorRecord).build();
        } catch (DeviceControllerException e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode()).build();
        }
    }

    public Response readPressure(String deviceId) {
        try {
            org.wso2.carbon.device.mgt.iot.sensormgt.SensorRecord sensorRecord = SensorDataManager.getInstance().getSensorRecord(deviceId,
                                                                           AndroidSenseConstants.SENSOR_PRESSURE);
            return Response.ok().entity(sensorRecord).build();
        } catch (DeviceControllerException e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode()).build();
        }
    }

    public Response readGravity(String deviceId) {
        try {
            org.wso2.carbon.device.mgt.iot.sensormgt.SensorRecord sensorRecord = SensorDataManager.getInstance().getSensorRecord(deviceId,
                                                                           AndroidSenseConstants.SENSOR_GRAVITY);
            return Response.ok().entity(sensorRecord).build();
        } catch (DeviceControllerException e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode()).build();
        }
    }

    public Response getWords(String deviceId, String sessionId) {
        try {
            org.wso2.carbon.device.mgt.iot.sensormgt.SensorRecord sensorRecord = SensorDataManager.getInstance().getSensorRecord(deviceId,
                                                                                        AndroidSenseConstants.SENSOR_WORDCOUNT);
            return Response.ok().entity(sensorRecord).build();
        } catch (DeviceControllerException e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode()).build();
        }
    }

    public Response sendKeyWords(String deviceId, String keywords) {
        try {
            String username = PrivilegedCarbonContext.getThreadLocalCarbonContext().getUsername();
            androidSenseMQTTConnector.publishDeviceData(username, deviceId, "add", keywords);
            return Response.ok().build();
        } catch (TransportHandlerException e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode()).build();
        }
    }

    public Response sendThreshold(String deviceId, String threshold) {
        try {
            String username = PrivilegedCarbonContext.getThreadLocalCarbonContext().getUsername();
            androidSenseMQTTConnector.publishDeviceData(username, deviceId, "threshold", threshold);
            return Response.ok().build();
        } catch (TransportHandlerException e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode()).build();
        }
    }

    public Response removeKeyWords(String deviceId, String words) {
        try {
            String username = PrivilegedCarbonContext.getThreadLocalCarbonContext().getUsername();
            androidSenseMQTTConnector.publishDeviceData(username, deviceId, "remove", words);
            return Response.ok().build();
        } catch (TransportHandlerException e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode()).build();
        }
    }

    public Response getAndroidSenseDeviceStats(String deviceId, String sensor, long from, long to) {
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
}
