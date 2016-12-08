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
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.device.mgt.iot.virtualfirealarm.agent.core;

public class AgentConstants {
    public static final String DEVICE_TYPE = "virtual_firealarm";
    public static final String LOG_APPENDER = "AGENT_LOG:: ";
    public static final String PROPERTIES_FILE_PATH = "";
    public static final int DEFAULT_RETRY_THREAD_INTERVAL = 5000;        // time in millis
    public static final String TOKEN_AUTHENTICATION_METHOD = "token";
    /*	---------------------------------------------------------------------------------------
                                IoT-Server specific information
         ---------------------------------------------------------------------------------------	*/
    public static final String DEVICE_ENROLLMENT_API_EP = "/scep";
    public static final String DEVICE_REGISTER_API_EP = "/register";
    public static final String DEVICE_PUSH_TEMPERATURE_API_EP = "/temperature";
    public static final String PUSH_DATA_PAYLOAD =
            "{\"owner\":\"%s\",\"deviceId\":\"%s\",\"reply\":\"%s\",\"value\":\"%s\"}";

    public static final String PUSH_SIMULATION_DATA_PAYLOAD =
            "{\"owner\":\"%s\",\"deviceId\":\"%s\",\"reply\":\"%s\",\"value\":\"%s\",\"isSimulated\":\"%s\"," +
                    "\"duration\":\"%s\",\"frequency\":\"%s\"}";

    public static final String DEVICE_DETAILS_PAGE_EP = "/devicemgt/device/%s?id=%s";
    public static final String DEVICE_ANALYTICS_PAGE_URL =
            "/devicemgt/device/virtual_firealarm/analytics?deviceId=%s&deviceName=%s";

    /*	---------------------------------------------------------------------------------------
                HTTP Connection specific information for communicating with IoT-Server
         ---------------------------------------------------------------------------------------	*/
    public static final String HTTP_POST = "POST";
    public static final String HTTP_GET = "GET";
    public static final String AUTHORIZATION_HEADER = "Authorization";
    public static final String CONTENT_TYPE_HEADER = "Content-Type";
    public static final String APPLICATION_JSON = "application/json";
    public static final String X_WWW_FORM_URLENCODED = "application/x-www-form-urlencoded";
    public static final String REGISTERED = "Registered";
    public static final String NOT_REGISTERED = "Not-Registered";
    public static final String REGISTRATION_FAILED = "Registration Failed";
    public static final String RETRYING_TO_REGISTER = "Registration Failed. Re-trying..";
    public static final String SERVER_NOT_RESPONDING = "Server not responding..";

    /*	---------------------------------------------------------------------------------------
                                MQTT Connection specific information
         ---------------------------------------------------------------------------------------	*/
    public static final int DEFAULT_MQTT_RECONNECTION_INTERVAL = 2;        // time in seconds
    public static final int DEFAULT_MQTT_QUALITY_OF_SERVICE = 0;
    public static final String MQTT_SUBSCRIBE_TOPIC = "%s/" + DEVICE_TYPE + "/%s";
    public static final String MQTT_PUBLISH_TOPIC = "%s/" + DEVICE_TYPE + "/%s/publisher";

    /*	---------------------------------------------------------------------------------------
          Device/Agent specific properties to be read from the 'deviceConfig.properties' file
         ---------------------------------------------------------------------------------------	*/
    public static final String AGENT_PROPERTIES_FILE_NAME = "deviceConfig.properties";
    public static final String TENANT_DOMAIN = "tenantDomain";
    public static final String DEVICE_OWNER_PROPERTY = "owner";
    public static final String DEVICE_ID_PROPERTY = "deviceId";
    public static final String SERVER_JID_PROPERTY = "server-jid";
    public static final String DEVICE_NAME_PROPERTY = "device-name";
    public static final String DEVICE_CONTROLLER_CONTEXT_PROPERTY = "controller-context";
    public static final String DEVICE_SCEP_CONTEXT_PROPERTY = "scep-context";
    public static final String SERVER_HTTPS_EP_PROPERTY = "https-ep";
    public static final String SERVER_HTTP_EP_PROPERTY = "http-ep";
    public static final String APIM_GATEWAY_EP_PROPERTY = "apim-ep";
    public static final String MQTT_BROKER_EP_PROPERTY = "mqtt-ep";
    public static final String XMPP_SERVER_EP_PROPERTY = "xmpp-ep";
    public static final String XMPP_SERVER_NAME_PROPERTY = "xmpp-server-name";
    public static final String API_APPLICATION_KEY = "application-key";
    public static final String AUTH_METHOD_PROPERTY = "auth-method";
    public static final String AUTH_TOKEN_PROPERTY = "auth-token";
    public static final String REFRESH_TOKEN_PROPERTY = "refresh-token";
    public static final String NETWORK_INTERFACE_PROPERTY = "network-interface";
    public static final String PUSH_INTERVAL_PROPERTY = "push-interval";
    /*	---------------------------------------------------------------------------------------
                Default values for the Device/Agent specific configurations listed above
         ---------------------------------------------------------------------------------------	*/
    public static final String DEFAULT_NETWORK_INTERFACE = "en0";
    public static final int DEFAULT_DATA_PUBLISH_INTERVAL = 15;                  // seconds
    public static final String DEFAULT_PROTOCOL = "MQTT";
    /*	---------------------------------------------------------------------------------------
                    Control Signal specific constants to match the request context
         ---------------------------------------------------------------------------------------	*/
    public static final String BULB_CONTROL = "BULB";
    public static final String TEMPERATURE_CONTROL = "TEMPERATURE";
    public static final String POLICY_SIGNAL = "POLICY";
    public static final String HUMIDITY_CONTROL = "HUMIDITY";
    public static final String CONTROL_ON = "ON";
    public static final String CONTROL_OFF = "OFF";
    public static final String AUDIO_FILE_NAME = "fireAlarmSound.mid";
    /*	---------------------------------------------------------------------------------------
                    Communication protocol specific Strings
        ---------------------------------------------------------------------------------------	*/
    public static final String TCP_PREFIX = "tcp://";
    public static final String HTTP_PREFIX = "http://";
    public static final String HTTPS_PREFIX = "https://";
    public static final String HTTP_PROTOCOL = "HTTP";
    public static final String MQTT_PROTOCOL = "MQTT";
    public static final String XMPP_PROTOCOL = "XMPP";
    public static final String PROTOCOL_PROPERTY = "Protocol";
    public static final String HOST_PROPERTY = "Host";
    public static final String PORT_PROPERTY = "Port";

    /*	---------------------------------------------------------------------------------------
                    Keystore specific strings for the device trustStore
        ---------------------------------------------------------------------------------------	*/
    public static final String DEVICE_KEYSTORE_TYPE = "JKS";
    public static final String DEVICE_KEYSTORE = "virtual_firealarm.jks";
    public static final String DEVICE_KEYSTORE_PASSWORD = "wso2@virtual_firealarm";
    public static final String DEVICE_PRIVATE_KEY_ALIAS = "virtual_firealarm_key";
    public static final String DEVICE_CERT_ALIAS = "virtual_firealarm_cert";
    public static final String SERVER_CA_CERT_ALIAS = "ca_iotServer";
}
