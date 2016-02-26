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

package org.wso2.carbon.device.mgt.iot.raspberrypi.controller.service.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.analytics.datasource.commons.Record;
import org.wso2.carbon.analytics.datasource.commons.exception.AnalyticsException;
import org.wso2.carbon.apimgt.annotations.api.API;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.device.mgt.analytics.service.DeviceAnalyticsService;
import org.wso2.carbon.device.mgt.common.DeviceIdentifier;
import org.wso2.carbon.device.mgt.common.DeviceManagementException;
import org.wso2.carbon.device.mgt.extensions.feature.mgt.annotations.DeviceType;
import org.wso2.carbon.device.mgt.extensions.feature.mgt.annotations.Feature;
import org.wso2.carbon.device.mgt.iot.DeviceManagement;
import org.wso2.carbon.device.mgt.iot.DeviceValidator;
import org.wso2.carbon.device.mgt.iot.controlqueue.mqtt.MqttConfig;
import org.wso2.carbon.device.mgt.iot.exception.DeviceControllerException;
import org.wso2.carbon.device.mgt.iot.raspberrypi.controller.service.impl.dto.DeviceData;
import org.wso2.carbon.device.mgt.iot.raspberrypi.controller.service.impl.dto.SensorData;
import org.wso2.carbon.device.mgt.iot.raspberrypi.controller.service.impl.transport.RaspberryPiMQTTConnector;
import org.wso2.carbon.device.mgt.iot.raspberrypi.controller.service.impl.util.RaspberrypiServiceUtils;
import org.wso2.carbon.device.mgt.iot.raspberrypi.plugin.constants.RaspberrypiConstants;
import org.wso2.carbon.device.mgt.iot.sensormgt.SensorDataManager;
import org.wso2.carbon.device.mgt.iot.sensormgt.SensorRecord;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
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
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

@API(name = "raspberrypi", version = "1.0.0", context = "/raspberrypi")
@DeviceType(value = "raspberrypi")
public class RaspberryPiControllerService {

    public static final String HTTP_PROTOCOL = "HTTP";
    public static final String MQTT_PROTOCOL = "MQTT";
    //TODO; replace this tenant domain
    private static final String SUPER_TENANT = "carbon.super";
    private static Log log = LogFactory.getLog(RaspberryPiControllerService.class);
    @Context  //injected response proxy supporting multiple thread
    private HttpServletResponse response;
    private ConcurrentHashMap<String, String> deviceToIpMap = new ConcurrentHashMap<>();
    private RaspberryPiMQTTConnector raspberryPiMQTTConnector;

    private boolean waitForServerStartup() {
        while (!DeviceManagement.isServerReady()) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                return true;
            }
        }
        return false;
    }

    /**
     * @return
     */
    public RaspberryPiMQTTConnector getRaspberryPiMQTTConnector() {
        return raspberryPiMQTTConnector;
    }

    /**
     * @param raspberryPiMQTTConnector
     */
    public void setRaspberryPiMQTTConnector(
            final RaspberryPiMQTTConnector raspberryPiMQTTConnector) {
        Runnable connector = new Runnable() {
            public void run() {
                if (waitForServerStartup()) {
                    return;
                }
                RaspberryPiControllerService.this.raspberryPiMQTTConnector = raspberryPiMQTTConnector;
                if (MqttConfig.getInstance().isEnabled()) {
                    raspberryPiMQTTConnector.connect();
                } else {
                    log.warn("MQTT disabled in 'devicemgt-config.xml'. Hence, RaspberryPiMQTTConnector not started.");
                }
            }
        };
        Thread connectorThread = new Thread(connector);
        connectorThread.setDaemon(true);
        connectorThread.start();
    }


    /*	---------------------------------------------------------------------------------------
                    Device specific APIs - Control APIs + Data-Publishing APIs
        ---------------------------------------------------------------------------------------	*/

    @Path("controller/register/{owner}/{deviceId}/{ip}/{port}")
    @POST
    public String registerDeviceIP(@PathParam("owner") String owner,
                                   @PathParam("deviceId") String deviceId,
                                   @PathParam("ip") String deviceIP,
                                   @PathParam("port") String devicePort,
                                   @Context HttpServletResponse response,
                                   @Context HttpServletRequest request) {

        //TODO:: Need to get IP from the request itself
        String result;

        if (log.isDebugEnabled()) {
            log.debug("Got register call from IP: " + deviceIP + " for Device ID: " + deviceId + " of owner: " + owner);
        }

        String deviceHttpEndpoint = deviceIP + ":" + devicePort;
        deviceToIpMap.put(deviceId, deviceHttpEndpoint);

        result = "Device-IP Registered";
        response.setStatus(Response.Status.OK.getStatusCode());

        if (log.isDebugEnabled()) {
            log.debug(result);
        }

        return result;
    }


    /**
     * @param owner
     * @param deviceId
     * @param protocol
     * @param state
     * @param response
     */
    @Path("controller/bulb")
    @POST
    @Feature( code="bulb", name="Bulb On / Off", type="operation",
            description="Switch on/off Raspberry Pi agent's bulb. (On / Off)")
    public void switchBulb(@HeaderParam("owner") String owner, @HeaderParam("deviceId") String deviceId,
                           @HeaderParam("protocol") String protocol, @FormParam("state") String state,
                           @Context HttpServletResponse response) {

        try {
            DeviceValidator deviceValidator = new DeviceValidator();
            if (!deviceValidator.isExist(owner, SUPER_TENANT, new DeviceIdentifier(deviceId,
                                                                                   RaspberrypiConstants.DEVICE_TYPE))) {
                response.setStatus(Response.Status.UNAUTHORIZED.getStatusCode());
                return;
            }
        } catch (DeviceManagementException e) {
            log.error("DeviceValidation Failed for deviceId: " + deviceId + " of user: " + owner);
            response.setStatus(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
            return;
        }

        String switchToState = state.toUpperCase();

        if (!switchToState.equals(RaspberrypiConstants.STATE_ON) && !switchToState.equals(
                RaspberrypiConstants.STATE_OFF)) {
            log.error("The requested state change shoud be either - 'ON' or 'OFF'");
            response.setStatus(Response.Status.BAD_REQUEST.getStatusCode());
            return;
        }

        String protocolString = protocol.toUpperCase();
        String callUrlPattern = RaspberrypiConstants.BULB_CONTEXT + switchToState;

        if (log.isDebugEnabled()) {
            log.debug("Sending request to switch-bulb of device [" + deviceId + "] via " + protocolString);
        }

        try {

            String deviceHTTPEndpoint = deviceToIpMap.get(deviceId);
            if (deviceHTTPEndpoint == null) {
                response.setStatus(Response.Status.PRECONDITION_FAILED.getStatusCode());
                return;
            }

            RaspberrypiServiceUtils.sendCommandViaHTTP(deviceHTTPEndpoint, callUrlPattern, true);
        } catch (DeviceManagementException e) {
            log.error("Failed to send switch-bulb request to device [" + deviceId + "] via " + protocolString);
            response.setStatus(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
            return;
        }

        response.setStatus(Response.Status.OK.getStatusCode());
    }


    /**
     * @param owner
     * @param deviceId
     * @param protocol
     * @param response
     * @return
     */
    @Path("controller/readtemperature")
    @GET
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Feature( code="readtemperature", name="Temperature", type="monitor",
            description="Request temperature reading from Raspberry Pi agent")
    public SensorRecord requestTemperature(@HeaderParam("owner") String owner,
                                           @HeaderParam("deviceId") String deviceId,
                                           @HeaderParam("protocol") String protocol,
                                           @Context HttpServletResponse response) {
        SensorRecord sensorRecord = null;

        DeviceValidator deviceValidator = new DeviceValidator();
        try {
            if (!deviceValidator.isExist(owner, SUPER_TENANT, new DeviceIdentifier(deviceId,
                                                                                   RaspberrypiConstants.DEVICE_TYPE))) {
                response.setStatus(Response.Status.UNAUTHORIZED.getStatusCode());
            }
        } catch (DeviceManagementException e) {
            response.setStatus(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
        }

        String protocolString = protocol.toUpperCase();

        if (log.isDebugEnabled()) {
            log.debug(
                    "Sending request to read raspberrypi-temperature of device [" + deviceId + "] via " +
                            protocolString);
        }

        try {
            String deviceHTTPEndpoint = deviceToIpMap.get(deviceId);
            if (deviceHTTPEndpoint == null) {
                response.setStatus(Response.Status.PRECONDITION_FAILED.getStatusCode());
            }

            String temperatureValue = RaspberrypiServiceUtils.sendCommandViaHTTP(deviceHTTPEndpoint,
                    RaspberrypiConstants.TEMPERATURE_CONTEXT, false);
            SensorDataManager.getInstance().setSensorRecord(deviceId, RaspberrypiConstants.SENSOR_TEMPERATURE,
                    temperatureValue, Calendar.getInstance().getTimeInMillis());
            sensorRecord = SensorDataManager.getInstance().getSensorRecord(deviceId,
                                                                           RaspberrypiConstants.SENSOR_TEMPERATURE);
        } catch (DeviceManagementException | DeviceControllerException e) {
            response.setStatus(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
        }

        response.setStatus(Response.Status.OK.getStatusCode());
        return sensorRecord;
    }

    /**
     * @param dataMsg
     * @param response
     */
    @Path("controller/push_temperature")
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public void pushTemperatureData(final DeviceData dataMsg,
                                    @Context HttpServletResponse response,
                                    @Context HttpServletRequest request) {
        String owner = dataMsg.owner;
        String deviceId = dataMsg.deviceId;
        String deviceIp = dataMsg.reply;            //TODO:: Get IP from request
        float temperature = dataMsg.value;

        try {
            DeviceValidator deviceValidator = new DeviceValidator();
            if (!deviceValidator.isExist(owner, SUPER_TENANT, new DeviceIdentifier(deviceId,
                                                                                   RaspberrypiConstants.DEVICE_TYPE))) {
                response.setStatus(Response.Status.UNAUTHORIZED.getStatusCode());
                log.warn("Temperature data Received from unregistered raspberrypi device [" + deviceId +
                                 "] for owner [" + owner + "]");
                return;
            }

            String registeredIp = deviceToIpMap.get(deviceId);

            if (registeredIp == null) {
                log.warn("Unregistered IP: Temperature Data Received from an un-registered IP " + deviceIp +
                                 " for device ID - " + deviceId);
                response.setStatus(Response.Status.PRECONDITION_FAILED.getStatusCode());
                return;
            } else if (!registeredIp.equals(deviceIp)) {
                log.warn("Conflicting IP: Received IP is " + deviceIp + ". Device with ID " + deviceId +
                                 " is already registered under some other IP. Re-registration required");
                response.setStatus(Response.Status.CONFLICT.getStatusCode());
                return;
            }

            if (log.isDebugEnabled()) {
                log.debug("Received Pin Data Value: " + temperature + " degrees C");
            }
            SensorDataManager.getInstance().setSensorRecord(deviceId, RaspberrypiConstants.SENSOR_TEMPERATURE,
                                                            String.valueOf(temperature),
                                                            Calendar.getInstance().getTimeInMillis());

            if (!RaspberrypiServiceUtils.publishToDAS(dataMsg.owner, dataMsg.deviceId, dataMsg.value)) {
                response.setStatus(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
                log.warn("An error occured whilst trying to publish temperature data of raspberrypi with ID [" +
                                 deviceId + "] of owner [" + owner + "]");
            }

        } catch (DeviceManagementException e) {
            String errorMsg = "Validation attempt for deviceId [" + deviceId + "] of owner [" + owner + "] failed.\n";
            log.error(errorMsg + Response.Status.INTERNAL_SERVER_ERROR.getReasonPhrase() + "\n" + e.getErrorMessage());
        }
    }

    /**
     * Retreive Sensor data for the device type
     * @param deviceId
     * @param user
     * @param from
     * @param to
     * @return
     */
    @Path("controller/stats/device/{deviceId}/sensors/temperature")
    @GET
    @Consumes("application/json")
    @Produces("application/json")
    public SensorData[] getArduinoTemperatureStats(@PathParam("deviceId") String deviceId,
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
                RaspberrypiConstants.DEVICE_TYPE + " AND time : [" + fromDate + " TO " + toDate + "]";
        String sensorTableName = RaspberrypiConstants.TEMPERATURE_EVENT_TABLE;
        try {
            List<Record> records = deviceAnalyticsService.getAllEventsForDevice(sensorTableName,
                                                                                query);

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
                sensorData.setValue("" + (float) record.getValue(RaspberrypiConstants.SENSOR_TEMPERATURE));
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

}
