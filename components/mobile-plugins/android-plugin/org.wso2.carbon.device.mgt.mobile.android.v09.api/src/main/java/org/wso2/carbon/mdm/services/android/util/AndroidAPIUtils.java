/*
 * Copyright (c) 2017, WSO2 Inc. (http:www.wso2.org) All Rights Reserved.
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
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.mdm.services.android.util;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.analytics.api.AnalyticsDataAPI;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.device.mgt.analytics.data.publisher.service.EventsPublisherService;
import org.wso2.carbon.device.mgt.common.notification.mgt.NotificationManagementService;
import org.wso2.carbon.device.mgt.core.app.mgt.ApplicationManagementProviderService;
import org.wso2.carbon.device.mgt.core.device.details.mgt.DeviceInformationManager;
import org.wso2.carbon.device.mgt.core.service.DeviceManagementProviderService;
import org.wso2.carbon.policy.mgt.core.PolicyManagerService;

/**
 * AndroidAPIUtil class provides utility functions used by Android REST-API classes.
 */
public class AndroidAPIUtils {

    private static Log log = LogFactory.getLog(AndroidAPIUtils.class);

    private AndroidAPIUtils(){
        throw new IllegalStateException("Utility class");
    }

    public static String getAuthenticatedUser() {
        PrivilegedCarbonContext threadLocalCarbonContext = PrivilegedCarbonContext.getThreadLocalCarbonContext();
        String username = threadLocalCarbonContext.getUsername();
        String tenantDomain = threadLocalCarbonContext.getTenantDomain();
        if (username != null && username.endsWith(tenantDomain)) {
            return username.substring(0, username.lastIndexOf("@"));
        }
        return username;
    }

    public static DeviceManagementProviderService getDeviceManagementService() {
        PrivilegedCarbonContext ctx = PrivilegedCarbonContext.getThreadLocalCarbonContext();
        DeviceManagementProviderService deviceManagementProviderService =
                (DeviceManagementProviderService) ctx.getOSGiService(DeviceManagementProviderService.class, null);
        if (deviceManagementProviderService == null) {
            String msg = "Device Management service has not initialized.";
            log.error(msg);
            throw new IllegalStateException(msg);
        }
        return deviceManagementProviderService;
    }

    public static DeviceInformationManager getDeviceInformationManagerService() {
        PrivilegedCarbonContext ctx = PrivilegedCarbonContext.getThreadLocalCarbonContext();
        DeviceInformationManager informationManager =
                (DeviceInformationManager) ctx.getOSGiService(DeviceInformationManager.class, null);
        if (informationManager == null) {
            String msg = "Information Manager service not initialized.";
            log.error(msg);
            throw new IllegalStateException(msg);
        }
        return informationManager;
    }

    public static PolicyManagerService getPolicyManagerService() {
        PrivilegedCarbonContext ctx = PrivilegedCarbonContext.getThreadLocalCarbonContext();
        PolicyManagerService policyManagerService = (PolicyManagerService) ctx.getOSGiService(
                PolicyManagerService.class, null);
        if (policyManagerService == null) {
            String msg = "Policy Manager service has not initialized";
            log.error(msg);
            throw new IllegalStateException(msg);
        }
        return policyManagerService;
    }

    public static ApplicationManagementProviderService getApplicationManagerService() {
        PrivilegedCarbonContext ctx = PrivilegedCarbonContext.getThreadLocalCarbonContext();
        ApplicationManagementProviderService applicationManagementProviderService =
                (ApplicationManagementProviderService) ctx.getOSGiService(ApplicationManagementProviderService.class, null);
        if (applicationManagementProviderService == null) {
            String msg = "Application Management provider service has not initialized";
            log.error(msg);
            throw new IllegalStateException(msg);
        }
        return applicationManagementProviderService;
    }

    public static NotificationManagementService getNotificationManagementService() {
        NotificationManagementService notificationManagementService;
        PrivilegedCarbonContext ctx = PrivilegedCarbonContext.getThreadLocalCarbonContext();
        notificationManagementService = (NotificationManagementService) ctx.getOSGiService(
                NotificationManagementService.class, null);
        if (notificationManagementService == null) {
            String msg = "Notification Management service not initialized.";
            log.error(msg);
            throw new IllegalStateException(msg);
        }
        return notificationManagementService;
    }

    public static EventsPublisherService getEventPublisherService() {
        PrivilegedCarbonContext ctx = PrivilegedCarbonContext.getThreadLocalCarbonContext();
        EventsPublisherService eventsPublisherService =
                (EventsPublisherService) ctx.getOSGiService(EventsPublisherService.class, null);
        if (eventsPublisherService == null) {
            String msg = "Event Publisher service has not initialized.";
            log.error(msg);
            throw new IllegalStateException(msg);
        }
        return eventsPublisherService;
    }

    public static AnalyticsDataAPI getAnalyticsDataAPI() {
        PrivilegedCarbonContext ctx = PrivilegedCarbonContext.getThreadLocalCarbonContext();
        AnalyticsDataAPI analyticsDataAPI =
                (AnalyticsDataAPI) ctx.getOSGiService(AnalyticsDataAPI.class, null);
        if (analyticsDataAPI == null) {
            String msg = "Analytics api service has not initialized.";
            log.error(msg);
            throw new IllegalStateException(msg);
        }
        return analyticsDataAPI;
    }

}
