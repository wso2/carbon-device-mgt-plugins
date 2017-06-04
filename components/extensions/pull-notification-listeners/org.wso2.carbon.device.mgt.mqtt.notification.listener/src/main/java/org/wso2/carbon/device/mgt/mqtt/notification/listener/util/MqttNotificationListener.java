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
package org.wso2.carbon.device.mgt.mqtt.notification.listener.util;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.device.mgt.mqtt.notification.listener.DeviceTypeOperationAdapterSubscription;
import org.wso2.carbon.device.mgt.mqtt.notification.listener.PullNotificationMqttContentTransformer;
import org.wso2.carbon.device.mgt.mqtt.notification.listener.internal.MqttNotificationDataHolder;
import org.wso2.carbon.event.input.adapter.core.InputEventAdapterConfiguration;
import org.wso2.carbon.event.input.adapter.core.exception.InputEventAdapterException;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;

import java.util.HashMap;
import java.util.Map;

/**
 * This creates a link between input adapter and the subscription of the input adpater.
 */
public class MqttNotificationListener {
    private static final Log log = LogFactory.getLog(MqttNotificationListener.class);

    private static final String TOPIC = "topic";
    private static final String SUBSCRIBED_TOPIC = "+/+/+/update/operation";
    private static final String TYPE = "oauth-mqtt";
    private static final String JSON = "json";
    private static final String NAME = "iot_core_server_adapter";
    private static final String CONTENT_TRANSFORMER_TYPE = "contentTransformer";
    private static final String MQTT_CONTENT_VALIDATOR_TYPE = "contentValidator";
    private static final String MQTT_CONTENT_VALIDATOR = "default";


    public static void setupMqttInputAdapter() {
        InputEventAdapterConfiguration inputEventAdapterConfiguration = new InputEventAdapterConfiguration();
        inputEventAdapterConfiguration.setName(NAME);
        inputEventAdapterConfiguration.setType(TYPE);
        inputEventAdapterConfiguration.setMessageFormat(JSON);
        Map<String, String> mqttAdapterProperties = new HashMap<>();
        mqttAdapterProperties.put(TOPIC, SUBSCRIBED_TOPIC);
        mqttAdapterProperties.put(CONTENT_TRANSFORMER_TYPE,
                                  PullNotificationMqttContentTransformer.MQTT_NOTIFICATION_MESSAGE_TRANSFORMER);
        mqttAdapterProperties.put(MQTT_CONTENT_VALIDATOR_TYPE, MQTT_CONTENT_VALIDATOR);
        inputEventAdapterConfiguration.setProperties(mqttAdapterProperties);
        try {
            PrivilegedCarbonContext.startTenantFlow();
            PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantDomain(MultitenantConstants
                                                                                          .SUPER_TENANT_DOMAIN_NAME, true);
            MqttNotificationDataHolder.getInstance().getInputEventAdapterService()
                    .create(inputEventAdapterConfiguration, new DeviceTypeOperationAdapterSubscription());
        } catch (InputEventAdapterException e) {
            log.error("Unable to create Input Event Adapter for pull notification.", e);
        } finally {
            PrivilegedCarbonContext.endTenantFlow();
        }
    }

}
