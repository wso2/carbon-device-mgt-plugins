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
import org.wso2.carbon.device.mgt.common.DeviceIdentifier;
import org.wso2.carbon.device.mgt.common.authorization.DeviceAccessAuthorizationException;
import org.wso2.carbon.device.mgt.common.group.mgt.DeviceGroupConstants;
import org.wso2.carbon.device.mgt.iot.virtualfirealarm.plugin.constants.VirtualFireAlarmConstants;
import org.wso2.carbon.device.mgt.iot.virtualfirealarm.plugin.impl.util.VirtualFirealarmSecurityManager;
import org.wso2.carbon.device.mgt.iot.virtualfirealarm.plugin.xmpp.XmppConfig;
import org.wso2.carbon.device.mgt.iot.virtualfirealarm.service.impl.dto.SensorRecord;
import org.wso2.carbon.device.mgt.iot.virtualfirealarm.service.impl.exception.VirtualFireAlarmException;
import org.wso2.carbon.device.mgt.iot.virtualfirealarm.service.impl.util.APIUtil;
import org.wso2.carbon.device.mgt.iot.virtualfirealarm.service.impl.util.VirtualFireAlarmServiceUtils;

import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;
import java.security.PrivateKey;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class VirtualFireAlarmControllerServiceImpl implements VirtualFireAlarmControllerService {

    private static final String XMPP_PROTOCOL = "XMPP";
    private static Log log = LogFactory.getLog(VirtualFireAlarmControllerServiceImpl.class);

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
            String resource = VirtualFireAlarmConstants.BULB_CONTEXT.replace("/", "");
            PrivateKey serverPrivateKey = VirtualFirealarmSecurityManager.getServerPrivateKey();
            String actualMessage = resource + ":" + switchToState;
            String encryptedMsg = VirtualFireAlarmServiceUtils.prepareSecurePayLoad(actualMessage,
                                                                                    serverPrivateKey);
            Map<String, String> dynamicProperties = new HashMap<>();
            switch (protocolString) {
                case XMPP_PROTOCOL:
                    dynamicProperties.put(VirtualFireAlarmConstants.JID_PROPERTY_KEY,
                                          deviceId + "@" + XmppConfig.getInstance().getXmppServerName());
                    dynamicProperties.put(VirtualFireAlarmConstants.SUBJECT_PROPERTY_KEY, "CONTROL-REQUEST");
                    dynamicProperties.put(VirtualFireAlarmConstants.MESSAGE_TYPE_PROPERTY_KEY,
                                          VirtualFireAlarmConstants.CHAT_PROPERTY_KEY);
                    APIUtil.getOutputEventAdapterService().publish(VirtualFireAlarmConstants.XMPP_ADAPTER_NAME,
                                                                   dynamicProperties, encryptedMsg);
                    break;
                default:
                    String publishTopic = APIUtil.getTenantDomainOftheUser() + "/"
                            + VirtualFireAlarmConstants.DEVICE_TYPE + "/" + deviceId;
                    dynamicProperties.put(VirtualFireAlarmConstants.ADAPTER_TOPIC_PROPERTY, publishTopic);
                    APIUtil.getOutputEventAdapterService().publish(VirtualFireAlarmConstants.MQTT_ADAPTER_NAME,
                                                                   dynamicProperties, encryptedMsg);
                    break;
            }
            return Response.ok().build();
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
            log.debug("Sending request to update-policy of device [" + deviceId + "] via " + protocolString);
        }
        try {
            if (!APIUtil.getDeviceAccessAuthorizationService().isUserAuthorized(
                    new DeviceIdentifier(deviceId, VirtualFireAlarmConstants.DEVICE_TYPE),
                    DeviceGroupConstants.Permissions.DEFAULT_MANAGE_POLICIES_PERMISSIONS)) {
                return Response.status(Response.Status.UNAUTHORIZED.getStatusCode()).build();
            }
            PrivateKey serverPrivateKey = VirtualFirealarmSecurityManager.getServerPrivateKey();
            String actualMessage = VirtualFireAlarmConstants.POLICY_CONTEXT + ":" + policy;
            String encryptedMsg = VirtualFireAlarmServiceUtils.prepareSecurePayLoad(actualMessage,
                                                                                    serverPrivateKey);
            Map<String, String> dynamicProperties = new HashMap<>();
            switch (protocolString) {
                case XMPP_PROTOCOL:
                    dynamicProperties.put(VirtualFireAlarmConstants.JID_PROPERTY_KEY,
                                          deviceId + "@" + XmppConfig.getInstance().getXmppServerName());
                    dynamicProperties.put(VirtualFireAlarmConstants.SUBJECT_PROPERTY_KEY, "POLICTY-REQUEST");
                    dynamicProperties.put(VirtualFireAlarmConstants.MESSAGE_TYPE_PROPERTY_KEY,
                                          VirtualFireAlarmConstants.CHAT_PROPERTY_KEY);
                    APIUtil.getOutputEventAdapterService().publish(VirtualFireAlarmConstants.XMPP_ADAPTER_NAME,
                                                                   dynamicProperties, encryptedMsg);
                    break;
                default:

                    String publishTopic = APIUtil.getTenantDomainOftheUser() + "/"
                            + VirtualFireAlarmConstants.DEVICE_TYPE + "/" + deviceId;
                    dynamicProperties.put(VirtualFireAlarmConstants.ADAPTER_TOPIC_PROPERTY, publishTopic);
                    APIUtil.getOutputEventAdapterService().publish(VirtualFireAlarmConstants.MQTT_ADAPTER_NAME,
                                                                   dynamicProperties, encryptedMsg);
                    break;
            }
            return Response.ok().build();
        } catch (DeviceAccessAuthorizationException e) {
            log.error(e.getErrorMessage(), e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        } catch (VirtualFireAlarmException e) {
            log.error(e);
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
