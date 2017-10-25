/*
 *  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.wso2.carbon.device.mgt.iot.arduino.service.impl.listener;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.CarbonConstants;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.device.mgt.iot.arduino.service.impl.constants.ArduinoConstants;
import org.wso2.carbon.user.api.AuthorizationManager;
import org.wso2.carbon.user.api.Permission;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.user.api.UserStoreManager;
import org.wso2.carbon.user.core.service.RealmService;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

public class ArduinoPermissionUpdateListener implements ServletContextListener {

    private static Log log = LogFactory.getLog(ArduinoPermissionUpdateListener.class);
    private static PrivilegedCarbonContext threadLocalCarbonContext;
    private static RealmService realmService;

    @Override
    public void contextInitialized(ServletContextEvent servletContextEvent) {
        threadLocalCarbonContext = PrivilegedCarbonContext.getThreadLocalCarbonContext();
        realmService = (RealmService) threadLocalCarbonContext.getOSGiService(RealmService.class, null);
        UserStoreManager userStoreManager = getUserStoreManager();
        try {
            if (userStoreManager != null) {
                if (!userStoreManager.isExistingRole(ArduinoConstants.ROLE_NAME)) {
                    userStoreManager.addRole(ArduinoConstants.ROLE_NAME, null, getPermissions());
                } else {
                    getAuthorizationManager().authorizeRole(ArduinoConstants.ROLE_NAME,
                            ArduinoConstants.PERM_ENROLL_ARDUINO, CarbonConstants.UI_PERMISSION_ACTION);
                    getAuthorizationManager().authorizeRole(ArduinoConstants.ROLE_NAME,
                            ArduinoConstants.PERM_OWNING_DEVICE_VIEW, CarbonConstants.UI_PERMISSION_ACTION);
                }
            }
        } catch (UserStoreException e) {
            log.error("Error while creating a role and adding a user for Arduino.", e);
        }
    }

    @Override
    public void contextDestroyed(ServletContextEvent servletContextEvent) {

    }

    private UserStoreManager getUserStoreManager() {
        UserStoreManager userStoreManager;
        try {
            if (realmService == null) {
                String msg = "Realm service has not initialized.";
                throw new IllegalStateException(msg);
            }
            int tenantId = threadLocalCarbonContext.getTenantId();
            userStoreManager = realmService.getTenantUserRealm(tenantId).getUserStoreManager();
            realmService.getTenantUserRealm(tenantId).getAuthorizationManager();
        } catch (UserStoreException e) {
            String msg = "Error occurred while retrieving current user store manager";
            throw new IllegalStateException(msg);
        }
        return userStoreManager;
    }

    private AuthorizationManager getAuthorizationManager() {
        AuthorizationManager authorizationManager;
        try {
            if (realmService == null) {
                String msg = "Realm service has not initialized.";
                throw new IllegalStateException(msg);
            }
            int tenantId = threadLocalCarbonContext.getTenantId();
            authorizationManager = realmService.getTenantUserRealm(tenantId).getAuthorizationManager();
        } catch (UserStoreException e) {
            String msg = "Error occurred while retrieving current user store manager";
            throw new IllegalStateException(msg);
        }
        return authorizationManager;
    }

    private Permission[] getPermissions() {
        Permission androidSense = new Permission(ArduinoConstants.PERM_ENROLL_ARDUINO,
                CarbonConstants.UI_PERMISSION_ACTION);
        Permission view = new Permission(ArduinoConstants.PERM_OWNING_DEVICE_VIEW,
                CarbonConstants.UI_PERMISSION_ACTION);
        return new Permission[]{androidSense, view};
    }
}
