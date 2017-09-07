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

package org.wso2.carbon.andes.extensions.device.mgt.jaxrs.util;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.andes.extensions.device.mgt.jaxrs.beans.ErrorResponse;
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.context.PrivilegedCarbonContext;
//import org.wso2.carbon.device.mgt.analytics.dashboard.GadgetDataService;
import org.wso2.carbon.device.mgt.common.DeviceManagementException;
import org.wso2.carbon.device.mgt.common.authorization.DeviceAccessAuthorizationService;
import org.wso2.carbon.device.mgt.common.configuration.mgt.ConfigurationEntry;
import org.wso2.carbon.device.mgt.common.configuration.mgt.PlatformConfiguration;
import org.wso2.carbon.device.mgt.common.configuration.mgt.PlatformConfigurationManagementService;
import org.wso2.carbon.device.mgt.common.notification.mgt.NotificationManagementService;
import org.wso2.carbon.device.mgt.core.app.mgt.ApplicationManagementProviderService;
import org.wso2.carbon.device.mgt.core.device.details.mgt.DeviceInformationManager;
import org.wso2.carbon.device.mgt.core.search.mgt.SearchManagerService;
import org.wso2.carbon.device.mgt.core.service.DeviceManagementProviderService;
import org.wso2.carbon.device.mgt.core.service.GroupManagementProviderService;
import org.wso2.carbon.andes.extensions.device.mgt.jaxrs.service.impl.util.InputValidationException;
import org.wso2.carbon.policy.mgt.core.PolicyManagerService;
import org.wso2.carbon.user.api.AuthorizationManager;
import org.wso2.carbon.user.api.UserRealm;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.user.api.UserStoreManager;
import org.wso2.carbon.user.core.service.RealmService;

import javax.ws.rs.core.MediaType;
import java.util.List;

/**
 * MDMAPIUtils class provides utility function used by CDM REST-API classes.
 */
public class MQTTMgtAPIUtils {

    public static final MediaType DEFAULT_CONTENT_TYPE = MediaType.APPLICATION_JSON_TYPE;
    private static final String NOTIFIER_FREQUENCY = "notifierFrequency";
    private static Log log = LogFactory.getLog(MQTTMgtAPIUtils.class);

    public static RealmService getRealmService() throws UserStoreException {
        RealmService realmService;
        PrivilegedCarbonContext ctx = PrivilegedCarbonContext.getThreadLocalCarbonContext();
        realmService = (RealmService) ctx.getOSGiService(RealmService.class, null);
        if (realmService == null) {
            String msg = "Realm service has not initialized.";
            log.error(msg);
            throw new IllegalStateException(msg);
        }
        return realmService;
    }

    /**
     * Getting the current tenant's user realm
     */
    public static UserRealm getUserRealm() throws UserStoreException {
        RealmService realmService;
        UserRealm realm;
        PrivilegedCarbonContext ctx = PrivilegedCarbonContext.getThreadLocalCarbonContext();
        realmService = (RealmService) ctx.getOSGiService(RealmService.class, null);

        if (realmService == null) {
            throw new IllegalStateException("Realm service not initialized");
        }
        int tenantId = CarbonContext.getThreadLocalCarbonContext().getTenantId();
        realm = realmService.getTenantUserRealm(tenantId);
        return realm;
    }

    public static AuthorizationManager getAuthorizationManager() throws UserStoreException {
        RealmService realmService;
        AuthorizationManager authorizationManager;
        PrivilegedCarbonContext ctx = PrivilegedCarbonContext.getThreadLocalCarbonContext();
        realmService = (RealmService) ctx.getOSGiService(RealmService.class, null);
        if (realmService == null) {
            throw new IllegalStateException("Realm service is not initialized.");
        }
        int tenantId = ctx.getTenantId();
        authorizationManager = realmService.getTenantUserRealm(tenantId).getAuthorizationManager();

        return authorizationManager;
    }

    public static int getTenantId(String tenantDomain) throws DeviceManagementException {
        RealmService realmService =
                (RealmService) PrivilegedCarbonContext.getThreadLocalCarbonContext().getOSGiService(RealmService.class, null);
        if (realmService == null) {
            throw new IllegalStateException("Realm service has not been initialized.");
        }
        try {
            return realmService.getTenantManager().getTenantId(tenantDomain);
        } catch (UserStoreException e) {
            throw new DeviceManagementException("Error occured while trying to " +
                "obtain tenant id of currently logged in user");
        }
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

}
