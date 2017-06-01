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

import org.wso2.carbon.device.mgt.common.pull.notification.NotificationContext;

public class NotificationMessage {

    private String tenantDomain;
    private NotificationContext notificationContext;

    public NotificationMessage(String tenantDomain, NotificationContext notificationContext) {
        this.tenantDomain = tenantDomain;
        this.notificationContext = notificationContext;
    }

    public String getTenantDomain() {
        return tenantDomain;
    }

    public void setTenantDomain(String tenantDomain) {
        this.tenantDomain = tenantDomain;
    }

    public NotificationContext getNotificationContext() {
        return notificationContext;
    }

    public void setNotificationContext(
            NotificationContext notificationContext) {
        this.notificationContext = notificationContext;
    }
}
