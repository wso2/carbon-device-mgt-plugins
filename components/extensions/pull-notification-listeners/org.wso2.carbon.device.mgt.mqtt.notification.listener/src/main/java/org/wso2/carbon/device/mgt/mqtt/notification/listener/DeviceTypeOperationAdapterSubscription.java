/*
 *   Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *   WSO2 Inc. licenses this file to you under the Apache License,
 *   Version 2.0 (the "License"); you may not use this file except
 *   in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing,
 *   software distributed under the License is distributed on an
 *   "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *   KIND, either express or implied.  See the License for the
 *   specific language governing permissions and limitations
 *   under the License.
 *
 */
package org.wso2.carbon.device.mgt.mqtt.notification.listener;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.device.mgt.common.pull.notification.PullNotificationExecutionFailedException;
import org.wso2.carbon.device.mgt.mqtt.notification.listener.internal.MqttNotificationDataHolder;
import org.wso2.carbon.event.input.adapter.core.InputEventAdapterSubscription;
import org.wso2.carbon.user.api.UserStoreException;

/**
 * Creates a event subscription for the input adapter.
 */
public class DeviceTypeOperationAdapterSubscription implements InputEventAdapterSubscription {
    private static final Log log = LogFactory.getLog(DeviceTypeOperationAdapterSubscription.class);

    @Override
    public void onEvent(Object o) {

        if (o == null || !(o instanceof  NotificationMessage)) {
            return;
        }

        NotificationMessage notificationMessage = (NotificationMessage) o;
        PrivilegedCarbonContext.startTenantFlow();
        PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantDomain(notificationMessage.getTenantDomain(),
                                                                              true);
        String deviceType = "";
        try {
            PrivilegedCarbonContext.getThreadLocalCarbonContext().setUsername(PrivilegedCarbonContext.
                    getThreadLocalCarbonContext().getUserRealm().getRealmConfiguration().getAdminUserName());
            deviceType = notificationMessage.getDeviceIdentifier().getType();
            MqttNotificationDataHolder.getInstance().getDeviceManagementProviderService().
                    updatePullNotificationOperation(notificationMessage.getDeviceIdentifier(),
                                                    notificationMessage.getOperation());
        } catch (UserStoreException e) {
            log.error("Failed to retrieve tenant username", e);
        } catch (PullNotificationExecutionFailedException e) {
            log.error("Failed to execute device type pull notification subscriber execution for device type" + deviceType,
                    e);
        } finally {
            PrivilegedCarbonContext.endTenantFlow();
        }

    }
}
