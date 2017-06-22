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
package org.wso2.carbon.device.mgt.mqtt.notification.listener.internal;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.framework.BundleContext;
import org.osgi.service.component.ComponentContext;
import org.wso2.carbon.core.ServerStartupObserver;
import org.wso2.carbon.device.mgt.core.service.DeviceManagementProviderService;
import org.wso2.carbon.device.mgt.input.adapter.extension.ContentTransformer;
import org.wso2.carbon.device.mgt.mqtt.notification.listener.PullNotificationMqttContentTransformer;
import org.wso2.carbon.device.mgt.mqtt.notification.listener.PullNotificationStartupListener;
import org.wso2.carbon.device.mgt.mqtt.notification.listener.util.MqttNotificationListener;
import org.wso2.carbon.event.input.adapter.core.InputEventAdapterService;

/**
 * @scr.component name="org.wso2.carbon.device.mgt.mqtt.notification.listener.internal.PullNotificationListenerServiceComponent" immediate="true"
 * @scr.reference name="carbon.device.mgt.provider"
 * interface="org.wso2.carbon.device.mgt.core.service.DeviceManagementProviderService"
 * cardinality="1..1"
 * policy="dynamic"
 * bind="setDeviceManagementProviderService"
 * unbind="unsetDeviceManagementProviderService"
 * @scr.reference name="event.input.adapter.service"
 * interface="org.wso2.carbon.event.input.adapter.core.InputEventAdapterService"
 * cardinality="1..1"
 * policy="dynamic"
 * bind="setInputEventAdapterService"
 * unbind="unsetInputEventAdapterService"
 */
public class PullNotificationListenerServiceComponent {

    private static final Log log = LogFactory.getLog(PullNotificationListenerServiceComponent.class);

    @SuppressWarnings("unused")
    protected void activate(ComponentContext componentContext) {
        try {
            //Do nothing
            if (log.isDebugEnabled()) {
                log.debug("Pull notification provider implementation bundle has been successfully " +
                        "initialized");
            }
            BundleContext bundleContext = componentContext.getBundleContext();
            bundleContext.registerService(ServerStartupObserver.class.getName(), new PullNotificationStartupListener(),
                                          null);
            bundleContext.registerService(ContentTransformer.class, new PullNotificationMqttContentTransformer(), null);
        } catch (Throwable e) {
            log.error("Error occurred while initializing pull notification provider implementation bundle", e);
        }
    }

    protected void deactivate(ComponentContext componentContext) {
        //Do nothing
    }

    protected void setDeviceManagementProviderService(DeviceManagementProviderService deviceManagementProviderService) {
        MqttNotificationDataHolder.getInstance().setDeviceManagementProviderService(deviceManagementProviderService);
    }

    protected void unsetDeviceManagementProviderService(DeviceManagementProviderService deviceManagementProviderService) {
        MqttNotificationDataHolder.getInstance().setDeviceManagementProviderService(deviceManagementProviderService);
    }

    protected void setInputEventAdapterService(InputEventAdapterService inputEventAdapterService) {
        MqttNotificationDataHolder.getInstance().setInputEventAdapterService(inputEventAdapterService);
    }

    protected void unsetInputEventAdapterService(InputEventAdapterService inputEventAdapterService) {
        MqttNotificationDataHolder.getInstance().setInputEventAdapterService(null);
    }

}
