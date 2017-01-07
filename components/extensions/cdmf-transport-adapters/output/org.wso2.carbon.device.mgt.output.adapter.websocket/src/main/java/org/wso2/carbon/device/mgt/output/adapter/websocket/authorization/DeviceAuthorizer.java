/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * you may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.carbon.device.mgt.output.adapter.websocket.authorization;

import feign.Feign;
import feign.FeignException;
import feign.gson.GsonDecoder;
import feign.gson.GsonEncoder;
import feign.jaxrs.JAXRSContract;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.device.mgt.output.adapter.websocket.authentication.AuthenticationInfo;
import org.wso2.carbon.device.mgt.output.adapter.websocket.authorization.client.OAuthRequestInterceptor;
import org.wso2.carbon.device.mgt.output.adapter.websocket.authorization.client.dto.AuthorizationRequest;
import org.wso2.carbon.device.mgt.output.adapter.websocket.authorization.client.dto
        .DeviceAccessAuthorizationAdminService;
import org.wso2.carbon.device.mgt.output.adapter.websocket.authorization.client.dto.DeviceAuthorizationResult;
import org.wso2.carbon.device.mgt.output.adapter.websocket.authorization.client.dto.DeviceIdentifier;
import org.wso2.carbon.device.mgt.output.adapter.websocket.util.PropertyUtils;
import org.wso2.carbon.device.mgt.output.adapter.websocket.util.WebSocketSessionRequest;
import org.wso2.carbon.event.output.adapter.core.exception.OutputEventAdapterException;

import javax.websocket.Session;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * This authorizer crossvalidates the request with device id and device type.
 */
public class DeviceAuthorizer implements Authorizer {

    private static DeviceAccessAuthorizationAdminService deviceAccessAuthorizationAdminService;
    private static final String CDMF_SERVER_BASE_CONTEXT = "/api/device-mgt/v1.0";
    private static final String DEVICE_MGT_SERVER_URL = "deviceMgtServerUrl";
    private static final String STAT_PERMISSION = "statsPermission";
    private static final String DEVICE_ID = "deviceId";
    private static final String DEVICE_TYPE = "deviceType";
    private static Log logger = LogFactory.getLog(DeviceAuthorizer.class);
    private static List<String> statPermissions;

    public DeviceAuthorizer() {
    }

    @Override
    public void init(Map<String, String> globalProperties) {
        statPermissions = getPermissions(globalProperties);
        try {
            deviceAccessAuthorizationAdminService = Feign.builder()
                    .requestInterceptor(new OAuthRequestInterceptor(globalProperties))
                    .contract(new JAXRSContract()).encoder(new GsonEncoder()).decoder(new GsonDecoder())
                    .target(DeviceAccessAuthorizationAdminService.class, getDeviceMgtServerUrl(globalProperties)
                            + CDMF_SERVER_BASE_CONTEXT);
        } catch (OutputEventAdapterException e) {
            logger.error("Invalid value for deviceMgtServerUrl in globalProperties.");
        }
    }

    @Override
    public boolean isAuthorized(AuthenticationInfo authenticationInfo, Session session, String stream) {
        WebSocketSessionRequest webSocketSessionRequest = new WebSocketSessionRequest(session);
        Map<String, String> queryParams = webSocketSessionRequest.getQueryParamValuePairs();
        String deviceId = queryParams.get(DEVICE_ID);
        String deviceType = queryParams.get(DEVICE_TYPE);

        if (deviceId != null && !deviceId.isEmpty() && deviceType != null && !deviceType.isEmpty()) {

            AuthorizationRequest authorizationRequest = new AuthorizationRequest();
            authorizationRequest.setTenantDomain(authenticationInfo.getTenantDomain());
            if (statPermissions != null && !statPermissions.isEmpty()) {
                authorizationRequest.setPermissions(statPermissions);
            }
            authorizationRequest.setUsername(authenticationInfo.getUsername());
            DeviceIdentifier deviceIdentifier = new DeviceIdentifier();
            deviceIdentifier.setId(deviceId);
            deviceIdentifier.setType(deviceType);
            List<DeviceIdentifier> deviceIdentifiers = new ArrayList<>();
            deviceIdentifiers.add(deviceIdentifier);
            authorizationRequest.setDeviceIdentifiers(deviceIdentifiers);
            try {
                DeviceAuthorizationResult deviceAuthorizationResult =
                        deviceAccessAuthorizationAdminService.isAuthorized(authorizationRequest);
                List<DeviceIdentifier> devices = deviceAuthorizationResult.getAuthorizedDevices();
                if (devices != null && devices.size() > 0) {
                    DeviceIdentifier authorizedDevice = devices.get(0);
                    if (authorizedDevice.getId().equals(deviceId) && authorizedDevice.getType().equals(deviceType)) {
                        return true;
                    }
                }
            } catch (FeignException e) {
                logger.error(e.getMessage(), e);
            }
        }
        return false;
    }

    private String getDeviceMgtServerUrl(Map<String, String> properties) throws OutputEventAdapterException {
        String deviceMgtServerUrl = PropertyUtils.replaceMqttProperty(properties.get(DEVICE_MGT_SERVER_URL));
        if (deviceMgtServerUrl == null || deviceMgtServerUrl.isEmpty()) {
            logger.error("deviceMgtServerUrl can't be empty ");
        }
        return deviceMgtServerUrl;
    }

    private List<String> getPermissions(Map<String, String> properties) {
        String stats =  properties.get(STAT_PERMISSION);
        if (stats != null && !stats.isEmpty()) {
            return Arrays.asList(stats.replace("\n", "").split(" "));
        }
        return null;
    }
}