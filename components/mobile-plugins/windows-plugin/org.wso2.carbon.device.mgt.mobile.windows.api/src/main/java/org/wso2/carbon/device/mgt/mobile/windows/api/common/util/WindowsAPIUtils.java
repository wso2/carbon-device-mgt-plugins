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

package org.wso2.carbon.device.mgt.mobile.windows.api.common.util;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.base.MultitenantConstants;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.device.mgt.common.DeviceIdentifier;
import org.wso2.carbon.device.mgt.common.DeviceManagementConstants;
import org.wso2.carbon.device.mgt.common.DeviceManagementException;
import org.wso2.carbon.device.mgt.common.InvalidDeviceException;
import org.wso2.carbon.device.mgt.common.configuration.mgt.ConfigurationEntry;
import org.wso2.carbon.device.mgt.common.configuration.mgt.PlatformConfiguration;
import org.wso2.carbon.device.mgt.common.device.details.DeviceInfo;
import org.wso2.carbon.device.mgt.common.device.details.DeviceLocation;
import org.wso2.carbon.device.mgt.common.notification.mgt.NotificationManagementService;
import org.wso2.carbon.device.mgt.common.operation.mgt.Activity;
import org.wso2.carbon.device.mgt.common.operation.mgt.Operation;
import org.wso2.carbon.device.mgt.common.operation.mgt.OperationManagementException;
import org.wso2.carbon.device.mgt.core.device.details.mgt.DeviceDetailsMgtException;
import org.wso2.carbon.device.mgt.core.device.details.mgt.DeviceInformationManager;
import org.wso2.carbon.device.mgt.core.service.DeviceManagementProviderService;
import org.wso2.carbon.device.mgt.mobile.windows.api.common.PluginConstants;
import org.wso2.carbon.device.mgt.mobile.windows.api.common.beans.ErrorResponse;
import org.wso2.carbon.device.mgt.mobile.windows.api.common.exceptions.BadRequestException;
import org.wso2.carbon.identity.core.util.IdentityTenantUtil;
import org.wso2.carbon.identity.oauth2.OAuth2TokenValidationService;
import org.wso2.carbon.policy.mgt.core.PolicyManagerService;
import org.wso2.carbon.user.api.TenantManager;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.user.core.service.RealmService;
import org.wso2.carbon.utils.multitenancy.MultitenantUtils;
import org.wso2.carbon.webapp.authenticator.framework.config.AuthenticatorConfig;
import org.wso2.carbon.webapp.authenticator.framework.config.AuthenticatorConfigService;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.List;

/**
 * Class for get Windows API utilities.
 */
public class WindowsAPIUtils {

    private static Log log = LogFactory.getLog(WindowsAPIUtils.class);

    public static DeviceIdentifier convertToDeviceIdentifierObject(String deviceId) {
        DeviceIdentifier identifier = new DeviceIdentifier();
        identifier.setId(deviceId);
        identifier.setType(DeviceManagementConstants.MobileDeviceTypes.MOBILE_DEVICE_TYPE_WINDOWS);
        return identifier;
    }

    public static DeviceManagementProviderService getDeviceManagementService() {
        PrivilegedCarbonContext ctx = PrivilegedCarbonContext.getThreadLocalCarbonContext();
        DeviceManagementProviderService deviceManagementProviderService =
                (DeviceManagementProviderService) ctx.getOSGiService(DeviceManagementProviderService.class, null);
        if (deviceManagementProviderService == null) {
            throw new IllegalStateException("Device Management service has not initialized.");
        }
        return deviceManagementProviderService;
    }

    public static NotificationManagementService getNotificationManagementService() {
        NotificationManagementService notificationManagementService;
        PrivilegedCarbonContext ctx = PrivilegedCarbonContext.getThreadLocalCarbonContext();
        notificationManagementService =
                (NotificationManagementService) ctx.getOSGiService(NotificationManagementService.class, null);
        if (notificationManagementService == null) {
            throw new IllegalStateException("Notification Management service not initialized.");
        }
        return notificationManagementService;
    }

    public static MediaType getResponseMediaType(String acceptHeader) {
        MediaType responseMediaType;
        if (MediaType.WILDCARD.equals(acceptHeader)) {
            responseMediaType = MediaType.APPLICATION_JSON_TYPE;
        } else {
            responseMediaType = MediaType.valueOf(acceptHeader);
        }
        return responseMediaType;
    }

    public static Response getOperationResponse(List<String> deviceIDs, Operation operation)
            throws DeviceManagementException, OperationManagementException, InvalidDeviceException {
        if (deviceIDs == null || deviceIDs.size() == 0) {
            String errorMessage = "Device identifier list is empty";
            log.error(errorMessage);
            throw new BadRequestException(
                    new ErrorResponse.ErrorResponseBuilder().setCode(400l).setMessage(errorMessage).build());
        }
        DeviceIdentifier deviceIdentifier;
        List<DeviceIdentifier> deviceIdentifiers = new ArrayList<>();
        for (String deviceId : deviceIDs) {
            deviceIdentifier = new DeviceIdentifier();
            deviceIdentifier.setId(deviceId);
            deviceIdentifier.setType(PluginConstants.WindowsConstant.DEVICE_TYPE_WINDOWS);
            deviceIdentifiers.add(deviceIdentifier);
        }
        Activity activity = getDeviceManagementService().addOperation(
                DeviceManagementConstants.MobileDeviceTypes.MOBILE_DEVICE_TYPE_WINDOWS, operation, deviceIdentifiers);
        return Response.status(Response.Status.CREATED).entity(activity).build();
    }

    public static PolicyManagerService getPolicyManagerService() {
        PrivilegedCarbonContext ctx = PrivilegedCarbonContext.getThreadLocalCarbonContext();
        PolicyManagerService policyManagerService =
                (PolicyManagerService) ctx.getOSGiService(PolicyManagerService.class, null);
        if (policyManagerService == null) {
            throw new IllegalStateException("Policy Manager service has not initialized");
        }
        return policyManagerService;
    }

    public static void updateOperation(String deviceId, Operation operation)
            throws OperationManagementException {
        DeviceIdentifier deviceIdentifier = new DeviceIdentifier();
        deviceIdentifier.setId(deviceId);
        deviceIdentifier.setType(DeviceManagementConstants.MobileDeviceTypes.MOBILE_DEVICE_TYPE_WINDOWS);
        getDeviceManagementService().updateOperation(deviceIdentifier, operation);
    }

    public static List<? extends Operation> getPendingOperations(DeviceIdentifier deviceIdentifier)
            throws OperationManagementException, DeviceManagementException {
        List<? extends Operation> pendingDataOperations;
        pendingDataOperations = org.wso2.carbon.device.mgt.mobile.windows.api.common.util.WindowsAPIUtils.
                getDeviceManagementService().getOperationsByDeviceAndStatus(
                deviceIdentifier, Operation.Status.PENDING);
        return pendingDataOperations;
    }

    public static PlatformConfiguration getTenantConfiguration() throws DeviceManagementException {
        return getDeviceManagementService().getConfiguration(
                DeviceManagementConstants.MobileDeviceTypes.MOBILE_DEVICE_TYPE_WINDOWS);
    }

    public static int getTenantIdOFUser(String username) throws DeviceManagementException {
        int tenantId = 0;
        RealmService realmService;
        String domainName = MultitenantUtils.getTenantDomain(username);
        if (domainName != null) {
            try {
                if ((realmService = IdentityTenantUtil.getRealmService()) != null) {
                    TenantManager tenantManager = realmService.getTenantManager();
                    tenantId = tenantManager.getTenantId(domainName);
                }
                if (realmService == null) {
                    throw new IllegalStateException("Realm service has not initialized.");
                }
            } catch (UserStoreException e) {
                throw new DeviceManagementException("Error when getting the tenant id from the tenant domain : "
                        + domainName, e);
            }
        }
        return tenantId;
    }

    public static OAuth2TokenValidationService getOAuth2TokenValidationService() {
        PrivilegedCarbonContext ctx = PrivilegedCarbonContext.getThreadLocalCarbonContext();
        OAuth2TokenValidationService oAuth2TokenValidationService =
                (OAuth2TokenValidationService) ctx.getOSGiService(OAuth2TokenValidationService.class, null);
        if (oAuth2TokenValidationService == null) {
            throw new IllegalStateException("OAuth2TokenValidation service has not initialized.");
        }
        return oAuth2TokenValidationService;
    }

    public static AuthenticatorConfig getBSTAuthenticatorConfig() {
        PrivilegedCarbonContext ctx = PrivilegedCarbonContext.getThreadLocalCarbonContext();
        AuthenticatorConfigService authenticatorConfigService =
                (AuthenticatorConfigService) ctx.getOSGiService(AuthenticatorConfigService.class, null);
        if (authenticatorConfigService == null) {
            throw new IllegalStateException("AuthenticatorConfiguration service has not initialized.");
        }
        AuthenticatorConfig authenticatorConfig = authenticatorConfigService.getAuthenticatorConfig("BST");
        if (authenticatorConfig == null) {
            throw new IllegalStateException("BST authenticatorConfig has not initialized.");
        }
        return authenticatorConfig;
    }

    public static void startTenantFlow(AuthenticationInfo authenticationInfo) {
        PrivilegedCarbonContext.startTenantFlow();
        PrivilegedCarbonContext privilegedCarbonContext = PrivilegedCarbonContext.getThreadLocalCarbonContext();
        if (authenticationInfo.getTenantDomain() == null) {
            privilegedCarbonContext.setTenantId(MultitenantConstants.SUPER_TENANT_ID);
            privilegedCarbonContext.setTenantDomain(MultitenantConstants.SUPER_TENANT_DOMAIN_NAME);
        } else {
            privilegedCarbonContext.setTenantId(authenticationInfo.getTenantId());
            privilegedCarbonContext.setTenantDomain(authenticationInfo.getTenantDomain());
        }
        privilegedCarbonContext.setUsername(authenticationInfo.getUsername());
    }

    /**
     * This method is used to get tenant configurations.
     *
     * @return List of Configurations entries.
     * @throws DeviceManagementException
     */
    public static List<ConfigurationEntry> getTenantConfigurationData() throws DeviceManagementException {
        PlatformConfiguration tenantConfiguration;
        if ((tenantConfiguration = org.wso2.carbon.device.mgt.mobile.windows.api.common.util.WindowsAPIUtils
                .getTenantConfiguration()) != null) {
            return tenantConfiguration.getConfiguration();
        } else {
            return null;
        }
    }

    /**
     * This method is used to update Device Information.
     * @param deviceId DeviceID to need to update.
     * @param deviceInfo Device Info to be update/
     * @throws DeviceDetailsMgtException Error occurs while updating Device Info.
     */
    public static void updateDeviceInfo(DeviceIdentifier deviceId, DeviceInfo deviceInfo)
            throws DeviceDetailsMgtException {
        PrivilegedCarbonContext ctx = PrivilegedCarbonContext.getThreadLocalCarbonContext();
        DeviceInformationManager informationManager =
                (DeviceInformationManager) ctx.getOSGiService(DeviceInformationManager.class, null);
        informationManager.addDeviceInfo(deviceId, deviceInfo);
    }

    /**
     * This method is used to update device location.
     * @param deviceLocation Device coordination related information.
     * @throws DeviceDetailsMgtException Error occurs while updating Device location.
     */
    public static void updateDeviceLocation(DeviceLocation deviceLocation) throws DeviceDetailsMgtException {
        PrivilegedCarbonContext ctx = PrivilegedCarbonContext.getThreadLocalCarbonContext();
        DeviceInformationManager informationManager =
                (DeviceInformationManager) ctx.getOSGiService(DeviceInformationManager.class, null);
        informationManager.addDeviceLocation(deviceLocation);
    }
}
