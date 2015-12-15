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

package org.wso2.carbon.device.mgt.iot.androidsense.service.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.device.mgt.analytics.exception.DataPublisherConfigurationException;
import org.wso2.carbon.device.mgt.analytics.service.DeviceAnalyticsService;
import org.wso2.carbon.device.mgt.common.Device;
import org.wso2.carbon.device.mgt.common.DeviceIdentifier;
import org.wso2.carbon.device.mgt.common.DeviceManagementException;
import org.wso2.carbon.device.mgt.common.EnrolmentInfo;
import org.wso2.carbon.device.mgt.iot.DeviceManagement;
import org.wso2.carbon.device.mgt.iot.androidsense.plugin.constants.AndroidSenseConstants;
import org.wso2.carbon.device.mgt.iot.androidsense.service.impl.util.DeviceJSON;
import org.wso2.carbon.device.mgt.iot.androidsense.service.impl.util.SensorJSON;
import org.wso2.carbon.device.mgt.iot.apimgt.AccessTokenInfo;
import org.wso2.carbon.device.mgt.iot.apimgt.TokenClient;
import org.wso2.carbon.device.mgt.iot.controlqueue.xmpp.XmppAccount;
import org.wso2.carbon.device.mgt.iot.controlqueue.xmpp.XmppConfig;
import org.wso2.carbon.device.mgt.iot.controlqueue.xmpp.XmppServerClient;
import org.wso2.carbon.device.mgt.iot.exception.AccessTokenException;
import org.wso2.carbon.device.mgt.iot.exception.DeviceControllerException;
import org.wso2.carbon.device.mgt.iot.sensormgt.SensorDataManager;
import org.wso2.carbon.device.mgt.iot.sensormgt.SensorRecord;
import org.wso2.carbon.device.mgt.iot.util.ZipArchive;
import org.wso2.carbon.device.mgt.iot.util.ZipUtil;

import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.UUID;

public class AndroidSenseService {

    private static final String BATTERY_STREAM_DEFINITION = "org.wso2.iot.devices.battery";
    private static final String LIGHT_STREAM_DEFINITION = "org.wso2.iot.devices.light";
    private static final String GPS_STREAM_DEFINITION = "org.wso2.iot.devices.gps";
    private static final String MAGNETIC_STREAM_DEFINITION = "org.wso2.iot.devices.magnetic";
    private static final String ACCELEROMETER_STREAM_DEFINITION = "org.wso2.iot.devices.accelerometer";
    private static final String GYROSCOPE_STREAM_DEFINITION = "org.wso2.iot.devices.gyroscope";
    private static final String PRESSURE_STREAM_DEFINITION = "org.wso2.iot.devices.pressure";
    private static final String GRAVITY_STREAM_DEFINITION = "org.wso2.iot.devices.gravity";
    private static final String ROTATION_STREAM_DEFINITION = "org.wso2.iot.devices.rotation";
    private static final String PROXIMITY_STREAM_DEFINITION = "org.wso2.iot.device.proximity";

    private static final String SENSOR_ACCELEROMETER = "accelerometer";
    private static final String SENSOR_GYROSCOPE = "gyroscope";
    private static final String SENSOR_PRESSURE = "pressure";
    private static final String SENSOR_GRAVITY = "gravity";
    private static final String SENSOR_ROTATION = "rotation";
    private static final String SENSOR_LIGHT = "light";
    private static final String SENSOR_GPS = "gps";
    private static final String SENSOR_PROXIMITY = "proximity";
    private static final String SENSOR_BATTERY = "battery";
    private static final String SENSOR_MAGNETIC = "magnetic";
    private static Log log = LogFactory.getLog(AndroidSenseService.class);

    //TODO; replace this tenant domain
    private final String SUPER_TENANT = "carbon.super";

    @Context  //injected response proxy supporting multiple thread
    private HttpServletResponse response;

    @Path("manager/device")
    @PUT
    public boolean register(@FormParam("deviceId") String deviceId,
                            @FormParam("owner") String owner) {

        DeviceManagement deviceManagement = new DeviceManagement(SUPER_TENANT);

        DeviceIdentifier deviceIdentifier = new DeviceIdentifier();
        deviceIdentifier.setId(deviceId);
        deviceIdentifier.setType(AndroidSenseConstants.DEVICE_TYPE);
        try {
            if (deviceManagement.getDeviceManagementService().isEnrolled(deviceIdentifier)) {
                response.setStatus(Response.Status.CONFLICT.getStatusCode());
                return true;
            }
            Device device = new Device();
            device.setDeviceIdentifier(deviceId);
            EnrolmentInfo enrolmentInfo = new EnrolmentInfo();

            enrolmentInfo.setDateOfEnrolment(new Date().getTime());
            enrolmentInfo.setDateOfLastUpdate(new Date().getTime());
            enrolmentInfo.setStatus(EnrolmentInfo.Status.ACTIVE);
            enrolmentInfo.setOwnership(EnrolmentInfo.OwnerShip.BYOD);
            String name = owner + " android " + deviceId;
            device.setName(name);
            device.setType(AndroidSenseConstants.DEVICE_TYPE);
            enrolmentInfo.setOwner(owner);
            device.setEnrolmentInfo(enrolmentInfo);
            boolean added = deviceManagement.getDeviceManagementService().enrollDevice(device);

            if (added) {
                response.setStatus(Response.Status.OK.getStatusCode());
            } else {
                response.setStatus(Response.Status.NOT_ACCEPTABLE.getStatusCode());
            }

            return added;
        } catch (DeviceManagementException e) {
            response.setStatus(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
            return false;
        } finally {
            deviceManagement.endTenantFlow();
        }
    }

    @Path("manager/device/remove/{device_id}")
    @DELETE
    public void removeDevice(@PathParam("device_id") String deviceId,
                             @Context HttpServletResponse response) {

        DeviceManagement deviceManagement = new DeviceManagement(SUPER_TENANT);
        DeviceIdentifier deviceIdentifier = new DeviceIdentifier();
        deviceIdentifier.setId(deviceId);
        deviceIdentifier.setType(AndroidSenseConstants.DEVICE_TYPE);
        try {
            boolean removed = deviceManagement.getDeviceManagementService().disenrollDevice(deviceIdentifier);
            if (removed) {
                response.setStatus(Response.Status.OK.getStatusCode());

            } else {
                response.setStatus(Response.Status.NOT_ACCEPTABLE.getStatusCode());

            }
        } catch (DeviceManagementException e) {
            response.setStatus(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
        } finally {
            deviceManagement.endTenantFlow();
        }

    }

    @Path("manager/device/update/{device_id}")
    @POST
    public boolean updateDevice(
            @PathParam("device_id") String deviceId, @QueryParam("name") String name,
            @Context HttpServletResponse response) {

        DeviceManagement deviceManagement = new DeviceManagement(SUPER_TENANT);

        DeviceIdentifier deviceIdentifier = new DeviceIdentifier();
        deviceIdentifier.setId(deviceId);
        deviceIdentifier.setType(AndroidSenseConstants.DEVICE_TYPE);
        try {
            Device device = deviceManagement.getDeviceManagementService().getDevice(deviceIdentifier);
            device.setDeviceIdentifier(deviceId);

            // device.setDeviceTypeId(deviceTypeId);
            device.getEnrolmentInfo().setDateOfLastUpdate(new Date().getTime());

            device.setName(name);
            device.setType(AndroidSenseConstants.DEVICE_TYPE);

            boolean updated = deviceManagement.getDeviceManagementService().modifyEnrollment(device);

            if (updated) {
                response.setStatus(Response.Status.OK.getStatusCode());

            } else {
                response.setStatus(Response.Status.NOT_ACCEPTABLE.getStatusCode());

            }
            return updated;
        } catch (DeviceManagementException e) {
            response.setStatus(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
            return false;
        } finally {
            deviceManagement.endTenantFlow();
        }

    }

    @Path("manager/device/{device_id}")
    @GET
    @Consumes("application/json")
    @Produces("application/json")
    public Device getDevice(
            @PathParam("device_id") String deviceId) {

        DeviceManagement deviceManagement = new DeviceManagement(SUPER_TENANT);
        DeviceIdentifier deviceIdentifier = new DeviceIdentifier();
        deviceIdentifier.setId(deviceId);
        deviceIdentifier.setType(AndroidSenseConstants.DEVICE_TYPE);

        try {
            return deviceManagement.getDeviceManagementService().getDevice(deviceIdentifier);

        } catch (DeviceManagementException e) {
            response.setStatus(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
            return null;
        } finally {
            deviceManagement.endTenantFlow();
        }

    }

    @Path("manager/device/{sketch_type}/download")
    @GET
    @Produces("application/octet-stream")
    public Response downloadSketch(@QueryParam("owner") String owner,
                                   @QueryParam("deviceName") String customDeviceName,
                                   @PathParam("sketch_type") String sketchType) {
        //TODO:: null check customDeviceName at UI level
        try {
            ZipArchive zipFile = createDownloadFile(owner, customDeviceName, sketchType);
            Response.ResponseBuilder rb = Response.ok(zipFile.getZipFile());
            rb.header("Content-Disposition",
                      "attachment; filename=\"" + zipFile.getFileName() + "\"");
            return rb.build();
        } catch (IllegalArgumentException ex) {
            return Response.status(400).entity(ex.getMessage()).build();//bad request
        } catch (DeviceManagementException ex) {
            return Response.status(500).entity(ex.getMessage()).build();
        } catch (AccessTokenException ex) {
            return Response.status(500).entity(ex.getMessage()).build();
        } catch (DeviceControllerException ex) {
            return Response.status(500).entity(ex.getMessage()).build();
        }

    }

    private ZipArchive createDownloadFile(String owner, String customDeviceName, String sketchType)
            throws DeviceManagementException, AccessTokenException, DeviceControllerException {
        if (owner == null) {
            throw new IllegalArgumentException("Error on createDownloadFile() Owner is null!");
        }

        //create new device id
        String deviceId = shortUUID();

        TokenClient accessTokenClient = new TokenClient(AndroidSenseConstants.DEVICE_TYPE);
        AccessTokenInfo accessTokenInfo = accessTokenClient.getAccessToken(owner, deviceId);

        //create token
        String accessToken = accessTokenInfo.getAccess_token();
        String refreshToken = accessTokenInfo.getRefresh_token();
        //adding registering data

        XmppAccount newXmppAccount = new XmppAccount();
        newXmppAccount.setAccountName(owner + "_" + deviceId);
        newXmppAccount.setUsername(deviceId);
        newXmppAccount.setPassword(accessToken);
        newXmppAccount.setEmail(deviceId + "@wso2.com");

        XmppServerClient xmppServerClient = new XmppServerClient();
        xmppServerClient.initControlQueue();
        boolean status;

        if (XmppConfig.getInstance().isEnabled()) {
            status = xmppServerClient.createXMPPAccount(newXmppAccount);
            if (!status) {
                String msg =
                        "XMPP Account was not created for device - " + deviceId + " of owner - " +
                        owner +
                        ".XMPP might have been disabled in org.wso2.carbon.device.mgt.iot" +
                        ".common.config.server.configs";
                log.warn(msg);
                throw new DeviceManagementException(msg);
            }
        }

        //Register the device with CDMF
        String deviceName = customDeviceName + "_" + deviceId;
        status = register(deviceId, owner);

        if (!status) {
            String msg = "Error occurred while registering the device with " + "id: " + deviceId
                         + " owner:" + owner;
            throw new DeviceManagementException(msg);
        }


        ZipUtil ziputil = new ZipUtil();
        ZipArchive zipFile = ziputil.downloadSketch(owner, SUPER_TENANT, sketchType, deviceId, deviceName,
                                                    accessToken, refreshToken);
        zipFile.setDeviceId(deviceId);
        return zipFile;
    }

    private static String shortUUID() {
        UUID uuid = UUID.randomUUID();
        long l = ByteBuffer.wrap(uuid.toString().getBytes(StandardCharsets.UTF_8)).getLong();
        return Long.toString(l, Character.MAX_RADIX);
    }


    /*    Service to push all the sensor data collected by the Android
           Called by the Android device  */
    @Path("controller/sensordata")
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public void pushSensorData(
            final DeviceJSON dataMsg, @Context HttpServletResponse response) {

        PrivilegedCarbonContext.startTenantFlow();
        PrivilegedCarbonContext ctx = PrivilegedCarbonContext.getThreadLocalCarbonContext();
        ctx.setTenantDomain("carbon.super", true);
        DeviceAnalyticsService deviceAnalyticsService = (DeviceAnalyticsService) ctx
                .getOSGiService(DeviceAnalyticsService.class, null);

        SensorJSON[] sensorData = dataMsg.values;
        String streamDef = null;
        Object payloadData[] = null;
        String sensorName = null;

        for (SensorJSON sensor : sensorData) {
            switch (sensor.key) {
                case "battery":
                    streamDef = BATTERY_STREAM_DEFINITION;
                    payloadData = new Object[]{Float.parseFloat(sensor.value)};
                    sensorName = SENSOR_BATTERY;
                    break;
                case "GPS":
                    streamDef = GPS_STREAM_DEFINITION;
                    String gpsValue = sensor.value;
                    String gpsValues[] = gpsValue.split(",");
                    Float gpsValuesF[] = new Float[2];
                    gpsValuesF[0] = Float.parseFloat(gpsValues[0]);
                    gpsValuesF[1] = Float.parseFloat(gpsValues[0]);
                    payloadData = gpsValuesF;
                    sensorName = SENSOR_GPS;
                    break;
                default:
                    try {
                        int androidSensorId = Integer.parseInt(sensor.key);

                        if (androidSensorId == 2) {
                            streamDef = MAGNETIC_STREAM_DEFINITION;
                            String value = sensor.value;
                            String valuesM[] = value.split(",");
                            Float gValuesF[] = new Float[1];
                            gValuesF[0] = Float.parseFloat(valuesM[0]) * Float.parseFloat(valuesM[0]) * Float
                                    .parseFloat(valuesM[0]);
                            payloadData = gValuesF;
                            sensorName = SENSOR_MAGNETIC;
                        } else if (androidSensorId == 5) {
                            streamDef = LIGHT_STREAM_DEFINITION;
                            sensorName = SENSOR_LIGHT;
                            payloadData = new Object[]{Float.parseFloat(sensor.value)};
                        } else if (androidSensorId == 1) {
                            streamDef = ACCELEROMETER_STREAM_DEFINITION;
                            String value = sensor.value;
                            String valuesM[] = value.split(",");
                            Float gValuesF[] = new Float[1];
                            gValuesF[0] = Float.parseFloat(valuesM[0]) * Float.parseFloat(valuesM[0]) * Float
                                    .parseFloat(valuesM[0]);
                            payloadData = gValuesF;
                            sensorName = SENSOR_ACCELEROMETER;
                        } else if (androidSensorId == 4) {
                            streamDef = GYROSCOPE_STREAM_DEFINITION;
                            String value = sensor.value;
                            String valuesG[] = value.split(",");
                            Float gValuesF[] = new Float[1];
                            gValuesF[0] = Float.parseFloat(valuesG[0]) * Float.parseFloat(valuesG[0]) * Float
                                    .parseFloat(valuesG[0]);
                            payloadData = gValuesF;
                            sensorName = SENSOR_GYROSCOPE;
                        } else if (androidSensorId == 9) {
                            streamDef = GRAVITY_STREAM_DEFINITION;
                            String value = sensor.value;
                            String valuesG[] = value.split(",");
                            Float gValuesF[] = new Float[1];
                            gValuesF[0] = Float.parseFloat(valuesG[0]) * Float.parseFloat(valuesG[0]) * Float
                                    .parseFloat(valuesG[0]);
                            payloadData = gValuesF;
                            sensorName = SENSOR_GRAVITY;
                        } else if (androidSensorId == 11) {
                            streamDef = ROTATION_STREAM_DEFINITION;
                            String value = sensor.value;
                            String valuesG[] = value.split(",");
                            Float gValuesF[] = new Float[1];
                            gValuesF[0] = Float.parseFloat(valuesG[0]) * Float.parseFloat(valuesG[0]) * Float
                                    .parseFloat(valuesG[0]);
                            payloadData = gValuesF;
                            sensorName = SENSOR_ROTATION;
                        } else if (androidSensorId == 8) {
                            streamDef = PROXIMITY_STREAM_DEFINITION;
                            sensorName = SENSOR_PROXIMITY;
                            payloadData = new Object[]{Float.parseFloat(sensor.value)};
                        } else if (androidSensorId == 6) {
                            streamDef = PRESSURE_STREAM_DEFINITION;
                            sensorName = SENSOR_PRESSURE;
                            payloadData = new Object[]{Float.parseFloat(sensor.value)};
                        }
                        //Add the remaining sensor types.

                    } catch (NumberFormatException e) {
                        continue;
                    }

            }
            Object metdaData[] = {dataMsg.owner, AndroidSenseConstants.DEVICE_TYPE, dataMsg.deviceId, sensor.time};

            if (streamDef != null && payloadData != null && payloadData.length > 0) {
                try {
                    SensorDataManager.getInstance()
                            .setSensorRecord(dataMsg.deviceId, sensorName, sensor.value, sensor.time);
                    deviceAnalyticsService.publishEvent(streamDef, "1.0.0", metdaData, new Object[0], payloadData);
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
    public SensorRecord readRotation(
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

    @Path("controller/readproximity")
    @GET
    @Consumes("application/json")
    @Produces("application/json")
    public SensorRecord readProximity(
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

    @Path("controller/readgyroscope")
    @GET
    @Consumes("application/json")
    @Produces("application/json")
    public SensorRecord readGyroscope(
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

    @Path("controller/readpressure")
    @GET
    @Consumes("application/json")
    @Produces("application/json")
    public SensorRecord readPressure(
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

    @Path("controller/readgravity")
    @GET
    @Consumes("application/json")
    @Produces("application/json")
    public SensorRecord readGravity(
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

}
