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
import org.wso2.carbon.core.ServerStartupObserver;
import org.wso2.carbon.device.mgt.core.config.DeviceConfigurationManager;
import org.wso2.carbon.device.mgt.mqtt.notification.listener.internal.MqttNotificationDataHolder;
import org.wso2.carbon.device.mgt.mqtt.notification.listener.util.MqttNotificationListener;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;

/**
 * Startup listener is been used to make sure the reciever gets activated after the server start up to avoid
 * Bundle not loading issues.
 */
public class PullNotificationStartupListener implements ServerStartupObserver {

    private static final Log log = LogFactory.getLog(PullNotificationStartupListener.class);

    @Override
    public void completingServerStartup() {
    }

    @Override
    public void completedServerStartup() {
        PrivilegedCarbonContext.startTenantFlow();
        PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantDomain(
                MultitenantConstants.SUPER_TENANT_DOMAIN_NAME, true);
        try {
            //TODO DeviceConfiguration Either need to be a osgi service or need to add those variable to system variables.
            boolean isEnabled = DeviceConfigurationManager.getInstance().getDeviceManagementConfig()
                    .getPullNotificationConfiguration().isEnabled();
            if (isEnabled) {
                MqttNotificationListener.setupMqttInputAdapter();
                MqttNotificationDataHolder.getInstance().getInputEventAdapterService().start();
                log.info("Mqtt operation listener activated");
            }
        } finally {
            PrivilegedCarbonContext.endTenantFlow();
        }
    }

}
