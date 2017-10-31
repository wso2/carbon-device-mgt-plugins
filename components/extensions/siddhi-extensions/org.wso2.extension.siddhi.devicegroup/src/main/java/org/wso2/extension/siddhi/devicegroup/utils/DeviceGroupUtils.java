/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.extension.siddhi.devicegroup.utils;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.device.mgt.core.service.GroupManagementProviderService;

/**
 * This class holds utility methods to retrieve data.
 */
public class DeviceGroupUtils {

    private static Log log = LogFactory.getLog(DeviceGroupUtils.class);
    private static GroupManagementProviderService groupManagementProviderServiceForTest;

    private DeviceGroupUtils(){

    }

    public static GroupManagementProviderService getGroupManagementProviderService() {
        if (groupManagementProviderServiceForTest != null) {
            return groupManagementProviderServiceForTest;
        }
        PrivilegedCarbonContext ctx = PrivilegedCarbonContext.getThreadLocalCarbonContext();
        GroupManagementProviderService groupManagementProviderService =
                (GroupManagementProviderService) ctx.getOSGiService(GroupManagementProviderService.class, null);
        if (groupManagementProviderService == null) {
            String msg = "GroupImpl Management service has not initialized.";
            log.error(msg);
            throw new IllegalStateException(msg);
        }
        return groupManagementProviderService;
    }

    /**
     * This method is only to set groupManagementProviderService locally for testing as OSGi framework cannot start
     * with testng to register the groupManagementProviderService. Hence setting groupManagementProviderService from
     * CheckDeviceInGroupExtensionTestCase
     * @param groupManagementProviderServiceForTest to be set.
     */
    public static void setGroupManagementProviderServiceForTest(
            GroupManagementProviderService groupManagementProviderServiceForTest) {
        DeviceGroupUtils.groupManagementProviderServiceForTest = groupManagementProviderServiceForTest;
    }
}
