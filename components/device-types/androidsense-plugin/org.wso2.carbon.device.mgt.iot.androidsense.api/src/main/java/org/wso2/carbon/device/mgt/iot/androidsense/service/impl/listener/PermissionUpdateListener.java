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
package org.wso2.carbon.device.mgt.iot.androidsense.service.impl.listener;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.device.mgt.iot.androidsense.service.impl.AndroidSenseServiceImpl;
import org.wso2.carbon.device.mgt.iot.androidsense.service.impl.constants.AndroidSenseConstants;
import org.wso2.carbon.user.api.AuthorizationManager;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.user.api.UserStoreManager;
import org.wso2.carbon.user.core.service.RealmService;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

public class PermissionUpdateListener implements ServletContextListener {

    private static Log log = LogFactory.getLog(AndroidSenseServiceImpl.class);

    private static final String ROLE_NAME = "internal/devicemgt-user";

    @Override
    public void contextInitialized(ServletContextEvent servletContextEvent) {

        UserStoreManager userStoreManager = getUserStoreManager();
        try {
            if (userStoreManager != null) {
                if (!userStoreManager.isExistingRole(ROLE_NAME)) {
                    userStoreManager.addRole(ROLE_NAME, null, AndroidSenseConstants.permissions);
                } else {
                    getAuthorizationManager().authorizeRole(ROLE_NAME,
                            "/permission/admin/device-mgt/devices/enroll/android-sense", "ui.execute");
                    getAuthorizationManager().authorizeRole(ROLE_NAME,
                            "/permission/admin/device-mgt/devices/owning-device/view", "ui.execute");
                }
            } } catch (UserStoreException e) {
            //
        }
    }

    @Override
    public void contextDestroyed(ServletContextEvent servletContextEvent) {

    }

    public static UserStoreManager getUserStoreManager() {
        RealmService realmService;
        UserStoreManager userStoreManager;
        try {
            PrivilegedCarbonContext ctx = PrivilegedCarbonContext.getThreadLocalCarbonContext();
            realmService = (RealmService) ctx.getOSGiService(RealmService.class, null);
            if (realmService == null) {
                String msg = "Realm service has not initialized.";
                log.error(msg);
                throw new IllegalStateException(msg);
            }
            int tenantId = ctx.getTenantId();
            userStoreManager = realmService.getTenantUserRealm(tenantId).getUserStoreManager();
            realmService.getTenantUserRealm(tenantId).getAuthorizationManager();
        } catch (UserStoreException e) {
            String msg = "Error occurred while retrieving current user store manager";
            log.error(msg, e);
            throw new IllegalStateException(msg);
        }
        return userStoreManager;
    }

    public static AuthorizationManager getAuthorizationManager() {
        RealmService realmService;
        AuthorizationManager authorizationManager;
        try {
            PrivilegedCarbonContext ctx = PrivilegedCarbonContext.getThreadLocalCarbonContext();
            realmService = (RealmService) ctx.getOSGiService(RealmService.class, null);
            if (realmService == null) {
                String msg = "Realm service has not initialized.";
                log.error(msg);
                throw new IllegalStateException(msg);
            }
            int tenantId = ctx.getTenantId();
            authorizationManager = realmService.getTenantUserRealm(tenantId).getAuthorizationManager();
        } catch (UserStoreException e) {
            String msg = "Error occurred while retrieving current user store manager";
            log.error(msg, e);
            throw new IllegalStateException(msg);
        }
        return authorizationManager;
    }


//    public static void registerApiAccessRoles(String user) {
//        UserStoreManager userStoreManager = null;
//        try {
//            userStoreManager = getUserStoreManager();
//            String[] userList = new String[]{user};
//            if (userStoreManager != null) {
//                String rolesOfUser[] = userStoreManager.getRoleListOfUser(user);
//                if (!userStoreManager.isExistingRole(Constants.DEFAULT_ROLE_NAME)) {
//                    userStoreManager.addRole(Constants.DEFAULT_ROLE_NAME, userList, Constants.DEFAULT_PERMISSION);
//                } else if (rolesOfUser != null && Arrays.asList(rolesOfUser).contains(Constants.DEFAULT_ROLE_NAME)) {
//                    return;
//                } else {
//                    userStoreManager.updateUserListOfRole(Constants.DEFAULT_ROLE_NAME, new String[0], userList);
//                }
//            }
//        } catch (UserStoreException e) {
//            log.error("Error while creating a role and adding a user for virtual_firealarm.", e);
//        }
//    }

}
