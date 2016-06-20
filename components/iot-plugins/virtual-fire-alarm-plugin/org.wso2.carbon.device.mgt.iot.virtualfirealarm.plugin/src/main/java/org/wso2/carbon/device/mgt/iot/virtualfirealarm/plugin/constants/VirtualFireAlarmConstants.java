/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.device.mgt.iot.virtualfirealarm.plugin.constants;

import org.wso2.carbon.utils.CarbonUtils;

import java.io.File;

public class VirtualFireAlarmConstants {
    public final static String DEVICE_TYPE = "virtual_firealarm";
    public final static String DEVICE_PLUGIN_DEVICE_NAME = "DEVICE_NAME";
    public final static String DEVICE_PLUGIN_DEVICE_ID = "VIRTUAL_FIREALARM_DEVICE_ID";
    public final static String STATE_ON = "ON";
    public final static String STATE_OFF = "OFF";

	public static final String URL_PREFIX = "http://";
	public static final String BULB_CONTEXT = "BULB";
	public static final String POLICY_CONTEXT = "POLICY";

    //sensor events sumerized table name for temperature
    public static final String TEMPERATURE_EVENT_TABLE = "DEVICE_TEMPERATURE_SUMMARY";
    public final static String DEVICE_TYPE_PROVIDER_DOMAIN = "carbon.super";

    //mqtt tranport related constants
    public static final String MQTT_ADAPTER_NAME = "virtual_firealarm_mqtt";
    public static final String MQTT_ADAPTER_TYPE = "oauth-mqtt";
    public static final String ADAPTER_TOPIC_PROPERTY = "topic";
    public static final String MQTT_PORT = "\\$\\{mqtt.broker.port\\}";
    public static final String MQTT_BROKER_HOST = "\\$\\{mqtt.broker.host\\}";
    public static final String CARBON_CONFIG_PORT_OFFSET = "Ports.Offset";
    public static final String DEFAULT_CARBON_LOCAL_IP_PROPERTY = "carbon.local.ip";
    public static final int CARBON_DEFAULT_PORT_OFFSET = 0;
    public static final int DEFAULT_MQTT_PORT = 1883;

    //xmpp transport related constants
    public static final String XMPP_ADAPTER_NAME = "virtual_firealarm_xmpp";
    public static final String XMPP_ADAPTER_TYPE = "xmpp";
    public static final String PASSWORD_PROPERTY_KEY = "password";
    public static final String JID_PROPERTY_KEY = "jid";
    public static final String CLIENT_JID_PROPERTY_KEY = "xmpp.client.jid";
    public static final String SUBJECT_PROPERTY_KEY = "xmpp.client.subject";
    public static final String MESSAGE_TYPE_PROPERTY_KEY = "xmpp.client.messageType";
    public static final String CHAT_PROPERTY_KEY = "chat";

    public static final String USERNAME_PROPERTY_KEY = "username";
    public static final String DCR_PROPERTY_KEY = "dcrUrl";
    public static final String BROKER_URL_PROPERTY_KEY = "url";
    public static final String SCOPES_PROPERTY_KEY = "scopes";
    public static final String QOS_PROPERTY_KEY = "qos";
    public static final String CLIENT_ID_PROPERTY_KEY = "qos";
    public static final String CLEAR_SESSION_PROPERTY_KEY = "clearSession";
    public static final String TOPIC = "topic";
    public static final String SUBSCRIBED_TOPIC = "carbon.super/virtual_firealarm/+/publisher";

    public static final String CONTENT_VALIDATION = "contentValidator";
    public static final String CONTENT_TRANSFORMATION = "contentTransformer";
    public static final String RESOURCE = "resource";

    public static final String JSON_SERIAL_KEY = "SerialNumber";
    public static final String TEMPERATURE_STREAM_DEFINITION = "org.wso2.iot.devices.temperature";
    public static final String JSON_MESSAGE_KEY = "Msg";
    public static final String JSON_SIGNATURE_KEY = "Sig";

    public static final String HOST_KEY = "host";
    public static final String PORT_KEY = "port";

    public static final String SERVER_NAME = "serverName";

    public static final String MQTT_ADAPTER_TOPIC_PROPERTY_NAME = "mqtt.adapter.topic";
}
