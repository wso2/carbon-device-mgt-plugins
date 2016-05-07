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

package org.wso2.carbon.device.mgt.iot.virtualfirealarm.service.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.analytics.dataservice.commons.SORT;
import org.wso2.carbon.analytics.dataservice.commons.SortByField;
import org.wso2.carbon.analytics.datasource.commons.exception.AnalyticsException;
import org.wso2.carbon.apimgt.annotations.api.Permission;
import org.wso2.carbon.device.mgt.common.DeviceIdentifier;
import org.wso2.carbon.device.mgt.common.DeviceManagementException;
import org.wso2.carbon.device.mgt.common.authorization.DeviceAccessAuthorizationException;
import org.wso2.carbon.device.mgt.common.group.mgt.DeviceGroupConstants;
import org.wso2.carbon.device.mgt.iot.controlqueue.mqtt.MqttConfig;
import org.wso2.carbon.device.mgt.iot.controlqueue.xmpp.XmppConfig;
import org.wso2.carbon.device.mgt.iot.service.IoTServerStartupListener;
import org.wso2.carbon.device.mgt.iot.transport.TransportHandlerException;
import org.wso2.carbon.device.mgt.iot.virtualfirealarm.plugin.constants.VirtualFireAlarmConstants;
import org.wso2.carbon.device.mgt.iot.virtualfirealarm.service.impl.dto.DeviceData;
import org.wso2.carbon.device.mgt.iot.virtualfirealarm.service.impl.dto.SensorRecord;
import org.wso2.carbon.device.mgt.iot.virtualfirealarm.service.impl.exception.VirtualFireAlarmException;
import org.wso2.carbon.device.mgt.iot.virtualfirealarm.service.impl.transport.VirtualFireAlarmMQTTConnector;
import org.wso2.carbon.device.mgt.iot.virtualfirealarm.service.impl.transport.VirtualFireAlarmXMPPConnector;
import org.wso2.carbon.device.mgt.iot.virtualfirealarm.service.impl.util.APIUtil;
import org.wso2.carbon.device.mgt.iot.virtualfirealarm.service.impl.util.SecurityManager;
import org.wso2.carbon.device.mgt.iot.virtualfirealarm.service.impl.util.VirtualFireAlarmServiceUtils;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.nio.charset.StandardCharsets;
import java.security.PrivateKey;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@SuppressWarnings("Non-Annoted WebService")
public class VirtualFireAlarmControllerServiceImpl implements VirtualFireAlarmControllerService {

    private static final String XMPP_PROTOCOL = "XMPP";
    private static final String HTTP_PROTOCOL = "HTTP";
    private static final String MQTT_PROTOCOL = "MQTT";
    private static Log log = LogFactory.getLog(VirtualFireAlarmControllerServiceImpl.class);
    // consists of utility methods related to encrypting and decrypting messages
    private org.wso2.carbon.device.mgt.iot.virtualfirealarm.service.impl.util.SecurityManager securityManager;
    // connects to the given MQTT broker and handles MQTT communication
    private VirtualFireAlarmMQTTConnector virtualFireAlarmMQTTConnector;
    // connects to the given XMPP server and handles XMPP communication
    private VirtualFireAlarmXMPPConnector virtualFireAlarmXMPPConnector;


    @POST
    @Path("device/{deviceId}/buzz")
    public Response switchBuzzer(@PathParam("deviceId") String deviceId, @QueryParam("protocol") String protocol,
                                 @FormParam("state") String state) {
        String switchToState = state.toUpperCase();
        if (!switchToState.equals(VirtualFireAlarmConstants.STATE_ON) && !switchToState.equals(
                VirtualFireAlarmConstants.STATE_OFF)) {
            log.error("The requested state change shoud be either - 'ON' or 'OFF'");
            return Response.status(Response.Status.BAD_REQUEST).build();
        }
        String protocolString = protocol.toUpperCase();
        String callUrlPattern = VirtualFireAlarmConstants.BULB_CONTEXT + switchToState;
        if (log.isDebugEnabled()) {
            log.debug("Sending request to switch-bulb of device [" + deviceId + "] via " +
                              protocolString);
        }
        try {
            if (!APIUtil.getDeviceAccessAuthorizationService().isUserAuthorized(
                    new DeviceIdentifier(deviceId, VirtualFireAlarmConstants.DEVICE_TYPE),
                    DeviceGroupConstants.Permissions.DEFAULT_OPERATOR_PERMISSIONS)) {
                return Response.status(Response.Status.UNAUTHORIZED.getStatusCode()).build();
            }
            switch (protocolString) {
                case XMPP_PROTOCOL:
                    String xmppResource = VirtualFireAlarmConstants.BULB_CONTEXT.replace("/", "");
                    virtualFireAlarmXMPPConnector.publishDeviceData(deviceId, xmppResource, switchToState);
                    break;
                default:
                    String mqttResource = VirtualFireAlarmConstants.BULB_CONTEXT.replace("/", "");
                    String publishTopic = "wso2/" + APIUtil.getTenantDomainOftheUser() + "/"
                            + VirtualFireAlarmConstants.DEVICE_TYPE + "/" + deviceId;
                    PrivateKey serverPrivateKey = SecurityManager.getServerPrivateKey();
                    String actualMessage = mqttResource + ":" + state.toUpperCase();
                    String encryptedMsg = VirtualFireAlarmServiceUtils.prepareSecurePayLoad(actualMessage,
                                                                                            serverPrivateKey);
                    Map<String, String> dynamicProperties = new HashMap<>();
                    dynamicProperties.put(VirtualFireAlarmConstants.ADAPTER_TOPIC_PROPERTY, publishTopic);
                    APIUtil.getOutputEventAdapterService().publish(VirtualFireAlarmConstants.ADAPTER_NAME,
                                                                   dynamicProperties, encryptedMsg);
                    break;
            }
            return Response.ok().build();
        } catch (TransportHandlerException e) {
            log.error("Failed to send switch-bulb request to device [" + deviceId + "] via " + protocolString);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        } catch (DeviceAccessAuthorizationException e) {
            log.error(e.getErrorMessage(), e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        } catch (VirtualFireAlarmException e) {
            String errorMsg = "Preparing Secure payload failed for device - [" + deviceId + "]";
            log.error(errorMsg);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PUT
    @Path("device/{deviceId}/policy")
    public Response updatePolicy(@PathParam("deviceId") String deviceId, @QueryParam("protocol") String protocol,
                                 @FormParam("policy") String policy) {
        String protocolString = protocol.toUpperCase();
        if (log.isDebugEnabled()) {
            log.debug("Sending request to update-policy of device [" + deviceId + "] via " +
                              protocolString);
        }
        try {
            if (!APIUtil.getDeviceAccessAuthorizationService().isUserAuthorized(
                    new DeviceIdentifier(deviceId, VirtualFireAlarmConstants.DEVICE_TYPE),
                    DeviceGroupConstants.Permissions.DEFAULT_MANAGE_POLICIES_PERMISSIONS)) {
                return Response.status(Response.Status.UNAUTHORIZED.getStatusCode()).build();
            }
            switch (protocolString) {
                case XMPP_PROTOCOL:
                    String xmppResource = VirtualFireAlarmConstants.POLICY_CONTEXT.replace("/", "");
                    virtualFireAlarmXMPPConnector.publishDeviceData(deviceId, xmppResource, policy);
                    break;
                default:
                    String mqttResource = VirtualFireAlarmConstants.POLICY_CONTEXT.replace("/", "");
                    virtualFireAlarmMQTTConnector.publishDeviceData(deviceId, mqttResource, policy);
                    break;
            }
            return Response.ok().build();
        } catch (TransportHandlerException e) {
            log.error("Failed to send update-policy request to device [" + deviceId + "] via " + protocolString);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        } catch (DeviceAccessAuthorizationException e) {
            log.error(e.getErrorMessage(), e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
    }


    @Path("device/stats/{deviceId}")
    @GET
    @Consumes("application/json")
    @Produces("application/json")
    public Response getVirtualFirealarmStats(@PathParam("deviceId") String deviceId, @QueryParam("from") long from,
                                             @QueryParam("to") long to) {
        String fromDate = String.valueOf(from);
        String toDate = String.valueOf(to);
        String query = "deviceId:" + deviceId + " AND deviceType:" +
                VirtualFireAlarmConstants.DEVICE_TYPE + " AND time : [" + fromDate + " TO " + toDate + "]";
        String sensorTableName = VirtualFireAlarmConstants.TEMPERATURE_EVENT_TABLE;
        try {
            if (!APIUtil.getDeviceAccessAuthorizationService().isUserAuthorized(
                    new DeviceIdentifier(deviceId, VirtualFireAlarmConstants.DEVICE_TYPE),
                    DeviceGroupConstants.Permissions.DEFAULT_STATS_MONITOR_PERMISSIONS)) {
                return Response.status(Response.Status.UNAUTHORIZED.getStatusCode()).build();
            }
            if (sensorTableName != null) {
                List<SortByField> sortByFields = new ArrayList<>();
                SortByField sortByField = new SortByField("time", SORT.ASC, false);
                sortByFields.add(sortByField);
                List<SensorRecord> sensorRecords = APIUtil.getAllEventsForDevice(sensorTableName, query, sortByFields);
                return Response.status(Response.Status.OK.getStatusCode()).entity(sensorRecords).build();
            }
        } catch (AnalyticsException e) {
            String errorMsg = "Error on retrieving stats on table " + sensorTableName + " with query " + query;
            log.error(errorMsg);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode()).entity(errorMsg).build();
        } catch (DeviceAccessAuthorizationException e) {
            log.error(e.getErrorMessage(), e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
        return Response.status(Response.Status.BAD_REQUEST).build();
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

    /**
     * Fetches the `SecurityManager` specific to this VirtualFirealarm controller service.
     *
     * @return the 'SecurityManager' instance bound to the 'securityManager' variable of this service.
     */
    @SuppressWarnings("Unused")
    public SecurityManager getSecurityManager() {
        return securityManager;
    }

    /**
     * Sets the `securityManager` variable of this VirtualFirealarm controller service.
     *
     * @param securityManager a 'SecurityManager' object that handles the encryption, decryption, signing and validation
     *                        of incoming messages from VirtualFirealarm device-types.
     */
    @SuppressWarnings("Unused")
    public void setSecurityManager(SecurityManager securityManager) {
        this.securityManager = securityManager;
        securityManager.initVerificationManager();
    }

    /**
     * Fetches the `VirtualFireAlarmXMPPConnector` specific to this VirtualFirealarm controller service.
     *
     * @return the 'VirtualFireAlarmXMPPConnector' instance bound to the 'virtualFireAlarmXMPPConnector' variable of
     * this service.
     */
    @SuppressWarnings("Unused")
    public VirtualFireAlarmXMPPConnector getVirtualFireAlarmXMPPConnector() {
        return virtualFireAlarmXMPPConnector;
    }

    /**
     * Sets the `virtualFireAlarmXMPPConnector` variable of this VirtualFirealarm controller service.
     *
     * @param virtualFireAlarmXMPPConnector a 'VirtualFireAlarmXMPPConnector' object that handles all XMPP related
     *                                      communications of any connected VirtualFirealarm device-type
     */
    @SuppressWarnings("Unused")
    public void setVirtualFireAlarmXMPPConnector(
            final VirtualFireAlarmXMPPConnector virtualFireAlarmXMPPConnector) {
        Runnable connector = new Runnable() {
            public void run() {
                if (waitForServerStartup()) {
                    return;
                }
                VirtualFireAlarmControllerServiceImpl.this.virtualFireAlarmXMPPConnector =
                        virtualFireAlarmXMPPConnector;

                if (XmppConfig.getInstance().isEnabled()) {
                    Runnable xmppStarter = new Runnable() {
                        @Override
                        public void run() {
                            virtualFireAlarmXMPPConnector.initConnector();
                            virtualFireAlarmXMPPConnector.connect();
                        }
                    };

                    Thread xmppStarterThread = new Thread(xmppStarter);
                    xmppStarterThread.setDaemon(true);
                    xmppStarterThread.start();
                } else {
                    log.warn(
                            "XMPP disabled in 'devicemgt-config.xml'. Hence, VirtualFireAlarmXMPPConnector not " +
                                    "started.");
                }
            }
        };
        Thread connectorThread = new Thread(connector);
        connectorThread.start();
    }

    /**
     * Fetches the `VirtualFireAlarmMQTTConnector` specific to this VirtualFirealarm controller service.
     *
     * @return the 'VirtualFireAlarmMQTTConnector' instance bound to the 'virtualFireAlarmMQTTConnector' variable of
     * this service.
     */
    @SuppressWarnings("Unused")
    public VirtualFireAlarmMQTTConnector getVirtualFireAlarmMQTTConnector() {
        return virtualFireAlarmMQTTConnector;
    }

    /**
     * Sets the `virtualFireAlarmMQTTConnector` variable of this VirtualFirealarm controller service.
     *
     * @param virtualFireAlarmMQTTConnector a 'VirtualFireAlarmMQTTConnector' object that handles all MQTT related
     *                                      communications of any connected VirtualFirealarm device-type
     */
    @SuppressWarnings("Unused")
    public void setVirtualFireAlarmMQTTConnector(
            final VirtualFireAlarmMQTTConnector virtualFireAlarmMQTTConnector) {
        Runnable connector = new Runnable() {
            public void run() {
                if (waitForServerStartup()) {
                    return;
                }
                //The delay is added for the server to starts up.
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                VirtualFireAlarmControllerServiceImpl.this.virtualFireAlarmMQTTConnector =
                        virtualFireAlarmMQTTConnector;
                if (MqttConfig.getInstance().isEnabled()) {
                    virtualFireAlarmMQTTConnector.connect();
                } else {
                    log.warn(
                            "MQTT disabled in 'devicemgt-config.xml'. Hence, VirtualFireAlarmMQTTConnector not started.");
                }
            }
        };
        Thread connectorThread = new Thread(connector);
        connectorThread.start();
    }
}
