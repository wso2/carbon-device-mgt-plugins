/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.carbon.device.mgt.iot.androidsense.controller.service.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.analytics.datasource.commons.Record;
import org.wso2.carbon.analytics.datasource.commons.exception.AnalyticsException;
import org.wso2.carbon.apimgt.annotations.api.API;
import org.wso2.carbon.apimgt.annotations.device.DeviceType;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.device.mgt.analytics.exception.DataPublisherConfigurationException;
import org.wso2.carbon.device.mgt.analytics.service.DeviceAnalyticsService;
import org.wso2.carbon.device.mgt.iot.androidsense.controller.service.impl.util.SensorData;
import org.wso2.carbon.device.mgt.iot.androidsense.plugin.constants.AndroidSenseConstants;
import org.wso2.carbon.device.mgt.iot.androidsense.controller.service.impl.util.DeviceData;
import org.wso2.carbon.device.mgt.iot.exception.DeviceControllerException;
import org.wso2.carbon.device.mgt.iot.sensormgt.SensorDataManager;
import org.wso2.carbon.device.mgt.iot.sensormgt.SensorRecord;
import org.wso2.carbon.apimgt.annotations.device.feature.Feature;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

@DeviceType( value = "android_sense" )
@API( name="android_sense", version="1.0.0", context="/android_sense")
public class AndroidSenseControllerService {

    private static final String ACCELEROMETER_STREAM_DEFINITION = "org.wso2.iot.devices.accelerometer";
    private static final String BATTERY_STREAM_DEFINITION = "org.wso2.iot.devices.battery";
    private static final String GPS_STREAM_DEFINITION = "org.wso2.iot.devices.gps";
    private static final String GRAVITY_STREAM_DEFINITION = "org.wso2.iot.devices.gravity";
    private static final String GYROSCOPE_STREAM_DEFINITION = "org.wso2.iot.devices.gyroscope";
    private static final String LIGHT_STREAM_DEFINITION = "org.wso2.iot.devices.light";
    private static final String MAGNETIC_STREAM_DEFINITION = "org.wso2.iot.devices.magnetic";
    private static final String PRESSURE_STREAM_DEFINITION = "org.wso2.iot.devices.pressure";
    private static final String PROXIMITY_STREAM_DEFINITION = "org.wso2.iot.devices.proximity";
    private static final String ROTATION_STREAM_DEFINITION = "org.wso2.iot.devices.rotation";

    private static final String SENSOR_ACCELEROMETER = "accelerometer";
    private static final String SENSOR_BATTERY = "battery";
    private static final String SENSOR_GPS = "gps";
    private static final String SENSOR_GRAVITY = "gravity";
    private static final String SENSOR_GYROSCOPE = "gyroscope";
    private static final String SENSOR_LIGHT = "light";
    private static final String SENSOR_MAGNETIC = "magnetic";
    private static final String SENSOR_PRESSURE = "pressure";
    private static final String SENSOR_PROXIMITY = "proximity";
    private static final String SENSOR_ROTATION = "rotation";
    private static Log log = LogFactory.getLog(AndroidSenseControllerService.class);

    //TODO; replace this tenant domain
    private final String SUPER_TENANT = "carbon.super";

    @Context  //injected response proxy supporting multiple thread
    private HttpServletResponse response;

    /*    Service to push all the sensor data collected by the Android
           Called by the Android device  */
    @Path("controller/sensordata")
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public void pushSensorData(
            final DeviceData dataMsg, @Context HttpServletResponse response) {

        PrivilegedCarbonContext.startTenantFlow();
        PrivilegedCarbonContext ctx = PrivilegedCarbonContext.getThreadLocalCarbonContext();
        ctx.setTenantDomain("carbon.super", true);
        DeviceAnalyticsService deviceAnalyticsService = (DeviceAnalyticsService) ctx
                .getOSGiService(DeviceAnalyticsService.class, null);

        SensorData[] sensorData = dataMsg.values;
        String streamDef = null;
        Object payloadData[] = null;
        String sensorName = null;

        for (SensorData sensor : sensorData) {
            if(sensor.key.equals("battery")){
                streamDef = BATTERY_STREAM_DEFINITION;
                payloadData = new Float[]{Float.parseFloat(sensor.value)};
                sensorName = SENSOR_BATTERY;
            } else if (sensor.key.equals("GPS")){
                streamDef = GPS_STREAM_DEFINITION;
                String gpsValue = sensor.value;
                String gpsValues[] = gpsValue.split(",");
                Float gpsValuesF[] = new Float[2];
                gpsValuesF[0] = Float.parseFloat(gpsValues[0]);
                gpsValuesF[1] = Float.parseFloat(gpsValues[0]);
                payloadData = gpsValuesF;
                sensorName = SENSOR_GPS;
            } else {

                try{
                    int androidSensorId = Integer.parseInt(sensor.key);

                    String value = sensor.value;
                    String valuesM[] = value.split(",");
                    Float gValuesF[] = new Float[1];

                    switch (androidSensorId){
                        case 1:
                            streamDef = ACCELEROMETER_STREAM_DEFINITION;
                            gValuesF[0] = Float.parseFloat(valuesM[0]) * Float.parseFloat(valuesM[0]) * Float
                                    .parseFloat(valuesM[0]);
                            payloadData = gValuesF;
                            sensorName = SENSOR_ACCELEROMETER;
                            break;
                        case 2:
                            streamDef = MAGNETIC_STREAM_DEFINITION;
                            gValuesF[0] = Float.parseFloat(valuesM[0]) * Float.parseFloat(valuesM[0]) * Float
                                    .parseFloat(valuesM[0]);
                            payloadData = gValuesF;
                            sensorName = SENSOR_MAGNETIC;
                            break;
                        case 4:
                            streamDef = GYROSCOPE_STREAM_DEFINITION;
                            gValuesF[0] = Float.parseFloat(valuesM[0]) * Float.parseFloat(valuesM[0]) * Float
                                    .parseFloat(valuesM[0]);
                            payloadData = gValuesF;
                            sensorName = SENSOR_GYROSCOPE;
                            break;
                        case 5:
                            streamDef = LIGHT_STREAM_DEFINITION;
                            sensorName = SENSOR_LIGHT;
                            payloadData = new Float[]{Float.parseFloat(valuesM[0])};
                            break;
                        case 6:
                            streamDef = PRESSURE_STREAM_DEFINITION;
                            sensorName = SENSOR_PRESSURE;
                            payloadData = new Float[]{Float.parseFloat(valuesM[0])};
                            break;
                        case 8:
                            streamDef = PROXIMITY_STREAM_DEFINITION;
                            sensorName = SENSOR_PROXIMITY;
                            payloadData = new Float[]{Float.parseFloat(valuesM[0])};
                            break;
                        case 9:
                            streamDef = GRAVITY_STREAM_DEFINITION;
                            gValuesF[0] = Float.parseFloat(valuesM[0]) * Float.parseFloat(valuesM[0]) * Float
                                    .parseFloat(valuesM[0]);
                            payloadData = gValuesF;
                            sensorName = SENSOR_GRAVITY;
                            break;
                        case 11:
                            streamDef = ROTATION_STREAM_DEFINITION;
                            gValuesF[0] = Float.parseFloat(valuesM[0]) * Float.parseFloat(valuesM[0]) * Float
                                    .parseFloat(valuesM[0]);
                            payloadData = gValuesF;
                            sensorName = SENSOR_ROTATION;
                            break;

                    }
                }  catch (NumberFormatException e) {
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
                    response.setStatus(Response.Status.UNSUPPORTED_MEDIA_TYPE.getStatusCode());
                }
            }

        }

    }

    //TODO below endpoints needs to be removed and cep websocket have to be added
    @Path("controller/readlight")
    @GET
    @Consumes("application/json")
    @Produces("application/json")
    @Feature(code = "readlight", name = "Light", description = "Read Light data from the device", type = "monitor")
    public SensorRecord readLight(
            @HeaderParam("owner") String owner, @HeaderParam("deviceId") String deviceId,
            @Context HttpServletResponse response) {
        SensorRecord sensorRecord = null;

        try {
            sensorRecord = SensorDataManager.getInstance().getSensorRecord(deviceId, SENSOR_LIGHT);
        } catch (DeviceControllerException e) {
            response.setStatus(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
        }

        response.setStatus(Response.Status.OK.getStatusCode());
        return sensorRecord;
    }

    @Path("controller/readbattery")
    @GET
    @Consumes("application/json")
    @Produces("application/json")
    @Feature(code = "readbattery", name = "Battery", description = "Read Battery data from the device",
            type = "monitor")
    public SensorRecord readBattery(
            @HeaderParam("owner") String owner, @HeaderParam("deviceId") String deviceId,
            @Context HttpServletResponse response) {
        SensorRecord sensorRecord = null;

        try {
            sensorRecord = SensorDataManager.getInstance().getSensorRecord(deviceId, SENSOR_BATTERY);
        } catch (DeviceControllerException e) {
            response.setStatus(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
        }

        response.setStatus(Response.Status.OK.getStatusCode());
        return sensorRecord;
    }

    @Path("controller/readgps")
    @GET
    @Consumes("application/json")
    @Produces("application/json")
    @Feature(code = "readgps", name = "gps", description = "Read GPS data from the device", type = "monitor")
    public SensorRecord readGPS(
            @HeaderParam("owner") String owner, @HeaderParam("deviceId") String deviceId,
            @Context HttpServletResponse response) {
        SensorRecord sensorRecord = null;

        try {
            sensorRecord = SensorDataManager.getInstance().getSensorRecord(deviceId, SENSOR_GPS);
        } catch (DeviceControllerException e) {
            response.setStatus(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
        }

        response.setStatus(Response.Status.OK.getStatusCode());
        return sensorRecord;
    }

    @Path("controller/readmagnetic")
    @GET
    @Consumes("application/json")
    @Produces("application/json")
    @Feature(code = "readmagnetic", name = "Magnetic", description = "Read Magnetic data from the device",
            type = "monitor")
    public SensorRecord readMagnetic(
            @HeaderParam("owner") String owner, @HeaderParam("deviceId") String deviceId,
            @Context HttpServletResponse response) {
        SensorRecord sensorRecord = null;

        try {
            sensorRecord = SensorDataManager.getInstance().getSensorRecord(deviceId, SENSOR_MAGNETIC);
        } catch (DeviceControllerException e) {
            response.setStatus(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
        }

        response.setStatus(Response.Status.OK.getStatusCode());
        return sensorRecord;
    }

    @Path("controller/readaccelerometer")
    @GET
    @Consumes("application/json")
    @Produces("application/json")
    @Feature(code = "readaccelerometer", name = "Accelerometer", description = "Read Accelerometer data from the " +
                                                                               "device",  type = "monitor")
    public SensorRecord readAccelerometer(
            @HeaderParam("owner") String owner, @HeaderParam("deviceId") String deviceId,
            @Context HttpServletResponse response) {
        SensorRecord sensorRecord = null;

        try {
            sensorRecord = SensorDataManager.getInstance().getSensorRecord(deviceId, SENSOR_ACCELEROMETER);
        } catch (DeviceControllerException e) {
            response.setStatus(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
        }

        response.setStatus(Response.Status.OK.getStatusCode());
        return sensorRecord;
    }

    @Path("controller/readrotation")
    @GET
    @Consumes("application/json")
    @Produces("application/json")
    @Feature(code = "readrotation", name = "Rotation", description = "Read Rotational Vector data from the device",
            type = "monitor")
    public SensorRecord readRotation(
            @HeaderParam("owner") String owner, @HeaderParam("deviceId") String deviceId,
            @Context HttpServletResponse response) {
        SensorRecord sensorRecord = null;

        try {
            sensorRecord = SensorDataManager.getInstance().getSensorRecord(deviceId, SENSOR_ROTATION);
        } catch (DeviceControllerException e) {
            response.setStatus(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
        }

        response.setStatus(Response.Status.OK.getStatusCode());
        return sensorRecord;
    }

    @Path("controller/readproximity")
    @GET
    @Consumes("application/json")
    @Produces("application/json")
    @Feature(code = "readproximity", name = "Proximity", description = "Read Proximity data from the device",
            type = "monitor")
    public SensorRecord readProximity(
            @HeaderParam("owner") String owner, @HeaderParam("deviceId") String deviceId,
            @Context HttpServletResponse response) {
        SensorRecord sensorRecord = null;

        try {
            sensorRecord = SensorDataManager.getInstance().getSensorRecord(deviceId, SENSOR_PROXIMITY);
        } catch (DeviceControllerException e) {
            response.setStatus(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
        }

        response.setStatus(Response.Status.OK.getStatusCode());
        return sensorRecord;
    }

    @Path("controller/readgyroscope")
    @GET
    @Consumes("application/json")
    @Produces("application/json")
    @Feature(code = "readgyroscope", name = "Gyroscope", description = "Read Gyroscope data from the device",
            type = "monitor")
    public SensorRecord readGyroscope(
            @HeaderParam("owner") String owner, @HeaderParam("deviceId") String deviceId,
            @Context HttpServletResponse response) {
        SensorRecord sensorRecord = null;

        try {
            sensorRecord = SensorDataManager.getInstance().getSensorRecord(deviceId, SENSOR_GYROSCOPE);
        } catch (DeviceControllerException e) {
            response.setStatus(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
        }

        response.setStatus(Response.Status.OK.getStatusCode());
        return sensorRecord;
    }

    @Path("controller/readpressure")
    @GET
    @Consumes("application/json")
    @Produces("application/json")
    @Feature(code = "readpressure", name = "Pressure", description = "Read Pressure data from the device", type = "monitor")
    public SensorRecord readPressure(
            @HeaderParam("owner") String owner, @HeaderParam("deviceId") String deviceId,
            @Context HttpServletResponse response) {
        SensorRecord sensorRecord = null;

        try {
            sensorRecord = SensorDataManager.getInstance().getSensorRecord(deviceId, SENSOR_PRESSURE);
        } catch (DeviceControllerException e) {
            response.setStatus(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
        }

        response.setStatus(Response.Status.OK.getStatusCode());
        return sensorRecord;
    }

    @Path("controller/readgravity")
    @GET
    @Consumes("application/json")
    @Produces("application/json")
    @Feature(code = "readgravity", name = "Gravity", description = "Read Gravity data from the device", type = "monitor")
    public SensorRecord readGravity(
            @HeaderParam("owner") String owner, @HeaderParam("deviceId") String deviceId,
            @Context HttpServletResponse response) {
        SensorRecord sensorRecord = null;

        try {
            sensorRecord = SensorDataManager.getInstance().getSensorRecord(deviceId, SENSOR_GRAVITY);
        } catch (DeviceControllerException e) {
            response.setStatus(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
        }

        response.setStatus(Response.Status.OK.getStatusCode());
        return sensorRecord;
    }

    /**
     * Retreive Sensor data for the device type
     * @param deviceId
     * @param sensor
     * @param user
     * @param from
     * @param to
     * @return
     */
    @Path("controller/stats/device/{deviceId}/sensors/{sensorName}")
    @GET
    @Consumes("application/json")
    @Produces("application/json")
    public SensorData[] getAndroidSenseDeviceStats(@PathParam("deviceId") String deviceId,
                                           @PathParam("sensorName") String sensor,
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
                AndroidSenseConstants.DEVICE_TYPE + " AND time : [" + fromDate + " TO " + toDate + "]";
        String sensorTableName = getSensorEventTableName(sensor);
        try {
            List<Record> records = deviceAnalyticsService.getAllEventsForDevice(sensorTableName, query);

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
                sensorData.setValue("" + (float) record.getValue(sensor));
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

    /**
     * get the event table from the sensor name.
     * TODO : this needs to be managed with sensor management.
     * @param sensorName
     * @return
     */
    private String getSensorEventTableName(String sensorName){
        String sensorEventTableName;
        switch (sensorName) {
            case SENSOR_ACCELEROMETER:
                sensorEventTableName = ACCELEROMETER_STREAM_DEFINITION.replace('.', '_').toUpperCase();
                break;
            case SENSOR_BATTERY:
                sensorEventTableName = BATTERY_STREAM_DEFINITION.replace('.', '_').toUpperCase();
                break;
            case SENSOR_GPS:
                sensorEventTableName = GPS_STREAM_DEFINITION.replace('.', '_').toUpperCase();
                break;
            case SENSOR_GRAVITY:
                sensorEventTableName = GRAVITY_STREAM_DEFINITION.replace('.', '_').toUpperCase();
                break;
            case SENSOR_GYROSCOPE:
                sensorEventTableName = GYROSCOPE_STREAM_DEFINITION.replace('.', '_').toUpperCase();
                break;
            case SENSOR_LIGHT:
                sensorEventTableName = LIGHT_STREAM_DEFINITION.replace('.', '_').toUpperCase();
                break;
            case SENSOR_MAGNETIC:
                sensorEventTableName = MAGNETIC_STREAM_DEFINITION.replace('.', '_').toUpperCase();
                break;
            case SENSOR_PRESSURE:
                sensorEventTableName = PRESSURE_STREAM_DEFINITION.replace('.', '_').toUpperCase();
                break;
            case SENSOR_PROXIMITY:
                sensorEventTableName = PROXIMITY_STREAM_DEFINITION.replace('.', '_').toUpperCase();
                break;
            case SENSOR_ROTATION:
                sensorEventTableName = ROTATION_STREAM_DEFINITION.replace('.', '_').toUpperCase();
                break;
            default:
                sensorEventTableName = "";
        }
        return sensorEventTableName;
    }

}
