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

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.FileUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.analytics.dataservice.commons.SORT;
import org.wso2.carbon.analytics.dataservice.commons.SortByField;
import org.wso2.carbon.analytics.datasource.commons.exception.AnalyticsException;
import org.wso2.carbon.apimgt.application.extension.APIManagementProviderService;
import org.wso2.carbon.apimgt.application.extension.dto.ApiApplicationKey;
import org.wso2.carbon.apimgt.application.extension.exception.APIManagerException;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.device.mgt.common.Device;
import org.wso2.carbon.device.mgt.common.DeviceIdentifier;
import org.wso2.carbon.device.mgt.common.DeviceManagementException;
import org.wso2.carbon.device.mgt.common.EnrolmentInfo;
import org.wso2.carbon.device.mgt.common.authorization.DeviceAccessAuthorizationException;
import org.wso2.carbon.device.mgt.common.group.mgt.DeviceGroupConstants;
import org.wso2.carbon.device.mgt.common.operation.mgt.Operation;
import org.wso2.carbon.device.mgt.common.operation.mgt.OperationManagementException;
import org.wso2.carbon.device.mgt.core.operation.mgt.CommandOperation;
import org.wso2.carbon.device.mgt.iot.util.ZipArchive;
import org.wso2.carbon.device.mgt.iot.virtualfirealarm.plugin.constants.VirtualFireAlarmConstants;
import org.wso2.carbon.device.mgt.iot.virtualfirealarm.plugin.exception.VirtualFirealarmDeviceMgtPluginException;
import org.wso2.carbon.device.mgt.iot.virtualfirealarm.plugin.impl.util.VirtualFirealarmSecurityManager;
import org.wso2.carbon.device.mgt.iot.virtualfirealarm.plugin.xmpp.XmppAccount;
import org.wso2.carbon.device.mgt.iot.virtualfirealarm.plugin.xmpp.XmppConfig;
import org.wso2.carbon.device.mgt.iot.virtualfirealarm.plugin.xmpp.XmppServerClient;
import org.wso2.carbon.device.mgt.iot.virtualfirealarm.service.impl.dto.SensorRecord;
import org.wso2.carbon.device.mgt.iot.virtualfirealarm.service.impl.exception.VirtualFireAlarmException;
import org.wso2.carbon.device.mgt.iot.virtualfirealarm.service.impl.util.APIUtil;
import org.wso2.carbon.device.mgt.iot.virtualfirealarm.service.impl.util.VirtualFireAlarmServiceUtils;
import org.wso2.carbon.device.mgt.iot.virtualfirealarm.service.impl.util.ZipUtil;
import org.wso2.carbon.identity.jwt.client.extension.JWTClient;
import org.wso2.carbon.identity.jwt.client.extension.dto.AccessTokenInfo;
import org.wso2.carbon.identity.jwt.client.extension.exception.JWTClientException;
import org.wso2.carbon.user.api.UserStoreException;

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
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.PrivateKey;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.UUID;

public class VirtualFireAlarmServiceImpl implements VirtualFireAlarmService {

    private static final String XMPP_PROTOCOL = "XMPP";
    private static final String MQTT_PROTOCOL = "MQTT";
    private static final String KEY_TYPE = "PRODUCTION";
    private static ApiApplicationKey apiApplicationKey;
    private static final String DEVICE_MGT_SCOPE_IDENTIFIER = "device-mgt";
    private static Log log = LogFactory.getLog(VirtualFireAlarmServiceImpl.class);

    @POST
    @Path("device/{deviceId}/buzz")
    public Response switchBuzzer(@PathParam("deviceId") String deviceId, @FormParam("state") String state) {
        if (state == null || state.isEmpty()) {
            log.error("State is not defined for the buzzer operation");
            return Response.status(Response.Status.BAD_REQUEST).build();
        }
        String switchToState = state.toUpperCase();
        if (!switchToState.equals(VirtualFireAlarmConstants.STATE_ON) && !switchToState.equals(
                VirtualFireAlarmConstants.STATE_OFF)) {
            log.error("The requested state change shoud be either - 'ON' or 'OFF'");
            return Response.status(Response.Status.BAD_REQUEST).build();
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
            String publishTopic = APIUtil.getTenantDomainOftheUser() + "/"
                    + VirtualFireAlarmConstants.DEVICE_TYPE + "/" + deviceId;

            Operation commandOp = new CommandOperation();
            commandOp.setCode("buzz");
            commandOp.setType(Operation.Type.COMMAND);
            commandOp.setEnabled(true);
            commandOp.setPayLoad(encryptedMsg);

            Properties props = new Properties();
            props.setProperty(VirtualFireAlarmConstants.MQTT_ADAPTER_TOPIC_PROPERTY_NAME, publishTopic);
            props.setProperty(VirtualFireAlarmConstants.CLIENT_JID_PROPERTY_KEY, deviceId + "@" + XmppConfig
                    .getInstance().getServerName());
            props.setProperty(VirtualFireAlarmConstants.SUBJECT_PROPERTY_KEY, "CONTROL-REQUEST");
            props.setProperty(VirtualFireAlarmConstants.MESSAGE_TYPE_PROPERTY_KEY,
                              VirtualFireAlarmConstants.CHAT_PROPERTY_KEY);
            commandOp.setProperties(props);

            List<DeviceIdentifier> deviceIdentifiers = new ArrayList<>();
            deviceIdentifiers.add(new DeviceIdentifier(deviceId, VirtualFireAlarmConstants.DEVICE_TYPE));
            APIUtil.getDeviceManagementService().addOperation(VirtualFireAlarmConstants.DEVICE_TYPE, commandOp,
                                                              deviceIdentifiers);
            return Response.ok().build();
        } catch (DeviceAccessAuthorizationException e) {
            log.error(e.getErrorMessage(), e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        } catch (VirtualFireAlarmException e) {
            String errorMsg = "Preparing Secure payload failed for device - [" + deviceId + "]";
            log.error(errorMsg);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        } catch (OperationManagementException e) {
            String msg = "Error occurred while executing command operation upon ringing the buzzer";
            log.error(msg, e);
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
                                          deviceId + "@" + XmppConfig.getInstance().getServerName());
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

    @Path("device/download")
    @GET
    @Produces("application/zip")
    public Response downloadSketch(@QueryParam("deviceName") String deviceName,
                                   @QueryParam("sketchType") String sketchType) {
        try {
            ZipArchive zipFile = createDownloadFile(APIUtil.getAuthenticatedUser(), deviceName, sketchType);
            Response.ResponseBuilder response = Response.ok(FileUtils.readFileToByteArray(zipFile.getZipFile()));
            response.status(Response.Status.OK);
            response.type("application/zip");
            response.header("Content-Disposition", "attachment; filename=\"" + zipFile.getFileName() + "\"");
            Response resp = response.build();
            zipFile.getZipFile().delete();
            return resp;
        } catch (IllegalArgumentException ex) {
            return Response.status(400).entity(ex.getMessage()).build();//bad request
        } catch (DeviceManagementException ex) {
            log.error(ex.getMessage(), ex);
            return Response.status(500).entity(ex.getMessage()).build();
        } catch (JWTClientException ex) {
            log.error(ex.getMessage(), ex);
            return Response.status(500).entity(ex.getMessage()).build();
        } catch (APIManagerException ex) {
            log.error(ex.getMessage(), ex);
            return Response.status(500).entity(ex.getMessage()).build();
        } catch (IOException ex) {
            log.error(ex.getMessage(), ex);
            return Response.status(500).entity(ex.getMessage()).build();
        } catch (UserStoreException ex) {
            log.error(ex.getMessage(), ex);
            return Response.status(500).entity(ex.getMessage()).build();
        } catch (VirtualFirealarmDeviceMgtPluginException ex) {
            log.error(ex.getMessage(), ex);
            return Response.status(500).entity(ex.getMessage()).build();
        }
    }

    private boolean register(String deviceId, String name) {
        try {
            DeviceIdentifier deviceIdentifier = new DeviceIdentifier();
            deviceIdentifier.setId(deviceId);
            deviceIdentifier.setType(VirtualFireAlarmConstants.DEVICE_TYPE);
            if (APIUtil.getDeviceManagementService().isEnrolled(deviceIdentifier)) {
                return false;
            }
            Device device = new Device();
            device.setDeviceIdentifier(deviceId);
            EnrolmentInfo enrolmentInfo = new EnrolmentInfo();
            enrolmentInfo.setDateOfEnrolment(new Date().getTime());
            enrolmentInfo.setDateOfLastUpdate(new Date().getTime());
            enrolmentInfo.setStatus(EnrolmentInfo.Status.ACTIVE);
            enrolmentInfo.setOwnership(EnrolmentInfo.OwnerShip.BYOD);
            device.setName(name);
            device.setType(VirtualFireAlarmConstants.DEVICE_TYPE);
            enrolmentInfo.setOwner(APIUtil.getAuthenticatedUser());
            device.setEnrolmentInfo(enrolmentInfo);
            return APIUtil.getDeviceManagementService().enrollDevice(device);
        } catch (DeviceManagementException e) {
            log.error(e.getMessage(), e);
            return false;
        }
    }

    private ZipArchive createDownloadFile(String owner, String deviceName, String sketchType)
            throws DeviceManagementException, APIManagerException, JWTClientException,
                   UserStoreException, VirtualFirealarmDeviceMgtPluginException {
        //create new device id
        String deviceId = shortUUID();
        boolean status = register(deviceId, deviceName);
        if (!status) {
            String msg = "Error occurred while registering the device with " + "id: " + deviceId + " owner:" + owner;
            throw new DeviceManagementException(msg);
        }
        if (apiApplicationKey == null) {
            String applicationUsername =
                    PrivilegedCarbonContext.getThreadLocalCarbonContext().getUserRealm().getRealmConfiguration()
                            .getAdminUserName();
            APIManagementProviderService apiManagementProviderService = APIUtil.getAPIManagementProviderService();
            String[] tags = {VirtualFireAlarmConstants.DEVICE_TYPE};
            apiApplicationKey = apiManagementProviderService.generateAndRetrieveApplicationKeys(
                    VirtualFireAlarmConstants.DEVICE_TYPE, tags, KEY_TYPE, applicationUsername, true);
        }
        JWTClient jwtClient = APIUtil.getJWTClientManagerService().getJWTClient();
        String device = "{ \"scope\":\"mqtt-publisher mqtt-subscriber\", \"deviceIdentifiers\":[{\"id\":\""+deviceId+"\", " +
                "\"type\":\""+VirtualFireAlarmConstants.DEVICE_TYPE+"\"}]}";
        Map<String, String> params = new HashMap<String, String>();
        params.put("device", Base64.encodeBase64String(device.getBytes()));
        AccessTokenInfo accessTokenInfo = jwtClient.getAccessToken(apiApplicationKey.getConsumerKey(),
                                                                   apiApplicationKey.getConsumerSecret(), owner,
                                                                   null, params);
        String accessToken = accessTokenInfo.getAccessToken();
        String refreshToken = accessTokenInfo.getRefreshToken();
        XmppAccount newXmppAccount = new XmppAccount();
        newXmppAccount.setAccountName(deviceId);
        newXmppAccount.setUsername(deviceId);
        newXmppAccount.setPassword(accessToken);
        newXmppAccount.setEmail(deviceId + "@" + APIUtil.getTenantDomainOftheUser());

        status = XmppServerClient.createAccount(newXmppAccount);
        if (!status) {
            String msg = "XMPP Account was not created for device - " + deviceId + " of owner - " + owner +
                    ".XMPP might have been disabled in org.wso2.carbon.device.mgt.iot" +
                    ".common.config.server.configs";
            throw new DeviceManagementException(msg);
        }
        ZipUtil ziputil = new ZipUtil();
        return ziputil.createZipFile(owner, sketchType, deviceId, deviceName, apiApplicationKey.toString(),
                                     accessToken, refreshToken);
    }

    private static String shortUUID() {
        UUID uuid = UUID.randomUUID();
        long l = ByteBuffer.wrap(uuid.toString().getBytes(StandardCharsets.UTF_8)).getLong();
        return Long.toString(l, Character.MAX_RADIX);
    }
}
