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

import org.apache.commons.io.FileUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.analytics.dataservice.commons.SortByField;
import org.wso2.carbon.analytics.dataservice.commons.SortType;
import org.wso2.carbon.analytics.datasource.commons.exception.AnalyticsException;
import org.wso2.carbon.apimgt.application.extension.APIManagementProviderService;
import org.wso2.carbon.apimgt.application.extension.dto.ApiApplicationKey;
import org.wso2.carbon.apimgt.application.extension.exception.APIManagerException;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.device.mgt.common.*;
import org.wso2.carbon.device.mgt.common.authorization.DeviceAccessAuthorizationException;
import org.wso2.carbon.device.mgt.common.group.mgt.DeviceGroupConstants;
import org.wso2.carbon.device.mgt.common.operation.mgt.Operation;
import org.wso2.carbon.device.mgt.common.operation.mgt.OperationManagementException;
import org.wso2.carbon.device.mgt.core.operation.mgt.CommandOperation;
import org.wso2.carbon.device.mgt.core.operation.mgt.ConfigOperation;
import org.wso2.carbon.device.mgt.iot.virtualfirealarm.service.impl.constants.VirtualFireAlarmConstants;
import org.wso2.carbon.device.mgt.iot.virtualfirealarm.service.impl.dto.SensorRecord;
import org.wso2.carbon.device.mgt.iot.virtualfirealarm.service.impl.util.APIUtil;
import org.wso2.carbon.device.mgt.iot.virtualfirealarm.service.impl.util.ZipArchive;
import org.wso2.carbon.device.mgt.iot.virtualfirealarm.service.impl.util.ZipUtil;
import org.wso2.carbon.device.mgt.iot.virtualfirealarm.service.impl.xmpp.VirtualFirealarmXMPPException;
import org.wso2.carbon.device.mgt.iot.virtualfirealarm.service.impl.xmpp.XmppAccount;
import org.wso2.carbon.device.mgt.iot.virtualfirealarm.service.impl.xmpp.XmppConfig;
import org.wso2.carbon.device.mgt.iot.virtualfirealarm.service.impl.xmpp.XmppServerClient;
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
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.UUID;

public class VirtualFireAlarmServiceImpl implements VirtualFireAlarmService {

    private static final String KEY_TYPE = "PRODUCTION";
    private static ApiApplicationKey apiApplicationKey;
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
            String actualMessage = resource + ":" + switchToState;
            String publishTopic = APIUtil.getTenantDomainOftheUser() + "/"
                    + VirtualFireAlarmConstants.DEVICE_TYPE + "/" + deviceId;

            ConfigOperation commandOp = new ConfigOperation();
            commandOp.setCode("buzz");
            commandOp.setEnabled(true);
            commandOp.setPayLoad(actualMessage);

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
        }  catch (InvalidDeviceException e) {
            String msg = "Error occurred while executing command operation to send keywords";
            log.error(msg, e);
            return Response.status(Response.Status.BAD_REQUEST).build();
        } catch (DeviceAccessAuthorizationException e) {
            log.error(e.getErrorMessage(), e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        } catch (OperationManagementException e) {
            String msg = "Error occurred while executing command operation upon ringing the buzzer";
            log.error(msg, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
    }

    @Path("device/stats/{deviceId}")
    @GET
    @Consumes("application/json")
    @Produces("application/json")
    public Response getVirtualFirealarmStats(@PathParam("deviceId") String deviceId, @QueryParam("from") long from,
                                             @QueryParam("to") long to) {
        String fromDate = String.valueOf(from*1000); // converting time to ms
        String toDate = String.valueOf(to*1000); // converting time to ms
        String query = "meta_deviceId:" + deviceId + " AND meta_deviceType:" +
                VirtualFireAlarmConstants.DEVICE_TYPE + " AND meta_time : [" + fromDate + " TO " + toDate + "]";
        String sensorTableName = VirtualFireAlarmConstants.TEMPERATURE_EVENT_TABLE;
        try {
            if (!APIUtil.getDeviceAccessAuthorizationService().isUserAuthorized(
                    new DeviceIdentifier(deviceId, VirtualFireAlarmConstants.DEVICE_TYPE),
                    DeviceGroupConstants.Permissions.DEFAULT_STATS_MONITOR_PERMISSIONS)) {
                return Response.status(Response.Status.UNAUTHORIZED.getStatusCode()).build();
            }
            List<SortByField> sortByFields = new ArrayList<>();
            SortByField sortByField = new SortByField("meta_time", SortType.ASC);
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
            String user = APIUtil.getAuthenticatedUser() + "@" + PrivilegedCarbonContext.getThreadLocalCarbonContext()
                    .getTenantDomain();
            ZipArchive zipFile = createDownloadFile(user, deviceName, sketchType);
            Response.ResponseBuilder response = Response.ok(zipFile.getZipFileContent());
            response.status(Response.Status.OK);
            response.type("application/zip");
            response.header("Content-Disposition", "attachment; filename=\"" + zipFile.getFileName() + "\"");
            Response resp = response.build();
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
        } catch (UserStoreException ex) {
            log.error(ex.getMessage(), ex);
            return Response.status(500).entity(ex.getMessage()).build();
        } catch (VirtualFirealarmXMPPException ex) {
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
                   UserStoreException, VirtualFirealarmXMPPException {
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
                            .getAdminUserName() + "@" + PrivilegedCarbonContext.getThreadLocalCarbonContext()
                            .getTenantDomain();
            APIManagementProviderService apiManagementProviderService = APIUtil.getAPIManagementProviderService();
            String[] tags = {VirtualFireAlarmConstants.DEVICE_TYPE};
            apiApplicationKey = apiManagementProviderService.generateAndRetrieveApplicationKeys(
                    VirtualFireAlarmConstants.DEVICE_TYPE, tags, KEY_TYPE, applicationUsername, true,
                    VirtualFireAlarmConstants.APIM_APPLICATION_TOKEN_VALIDITY_PERIOD);
        }
        JWTClient jwtClient = APIUtil.getJWTClientManagerService().getJWTClient();
        String scopes = " device_" + deviceId;
        AccessTokenInfo accessTokenInfo = jwtClient.getAccessToken(apiApplicationKey.getConsumerKey(),
                                                                   apiApplicationKey.getConsumerSecret(), owner,
                                                                   scopes);
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
