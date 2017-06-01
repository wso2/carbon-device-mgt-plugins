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

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.device.mgt.common.DeviceIdentifier;
import org.wso2.carbon.device.mgt.common.pull.notification.NotificationContext;
import org.wso2.carbon.device.mgt.common.pull.notification.NotificationPayload;
import org.wso2.carbon.device.mgt.input.adapter.extension.ContentTransformer;

import java.util.Map;

/**
 * This transforms the incomming message payload to notification message, inorder to pass this
 * information before its passed to the input adapter subscriber.
 */
public class PullNotificationMqttContentTransformer implements ContentTransformer {

    public static final String MQTT_NOTIFICATION_MESSAGE_TRANSFORMER = "mqtt-notification-transformer";

    @Override
    public String getType() {
        return MQTT_NOTIFICATION_MESSAGE_TRANSFORMER;
    }

    @Override
    public Object transform(Object message, Map<String, Object> dynamicProperties) {
        String topic = (String) dynamicProperties.get("topic");
        String[] topicParams = topic.split("/");
        String tenantDomain = topicParams[0];
        String deviceType = topicParams[1];
        String deviceId = topicParams[2];
        Gson gson = new Gson();
        try {
            NotificationPayload notificationPayload = gson.fromJson((String) message, NotificationPayload.class);
            NotificationContext notificationContext =  new NotificationContext
                    (new DeviceIdentifier(deviceId, deviceType), notificationPayload);
            return new NotificationMessage(tenantDomain, notificationContext);
        } catch (Exception e) {
            //Avoid notification listener to fail.
            return new Object();
        } finally {
            PrivilegedCarbonContext.endTenantFlow();
        }
    }

}
