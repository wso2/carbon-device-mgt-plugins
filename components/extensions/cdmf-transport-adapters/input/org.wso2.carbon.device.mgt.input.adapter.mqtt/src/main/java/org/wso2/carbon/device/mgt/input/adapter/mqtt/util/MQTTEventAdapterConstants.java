/*
*  Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/
package org.wso2.carbon.device.mgt.input.adapter.mqtt.util;


/**
 * This holds the constants related to mqtt event adapter.
 */
public class MQTTEventAdapterConstants {

    public static final String ADAPTER_TYPE_MQTT = "oauth-mqtt";
    public static final String ADAPTER_CONF_URL = "url";
    public static final String ADAPTER_CONF_USERNAME = "username";
    public static final String ADAPTER_CONF_USERNAME_HINT = "username.hint";
    public static final String ADAPTER_CONF_PASSWORD = "password";
    public static final String ADAPTER_CONF_PASSWORD_HINT = "password.hint";
    public static final String ADAPTER_CONF_SCOPES = "scopes";
    public static final String ADAPTER_CONF_SCOPES_HINT = "scopes.hint";
    public static final String ADAPTER_CONF_URL_HINT = "url.hint";
    public static final String ADAPTER_CONF_DCR_URL = "dcrUrl";
    public static final String ADAPTER_CONF_TOKEN_URL = "tokenUrl";
    public static final String ADAPTER_CONF_DCR_URL_HINT = "dcrUrl.hint";
    public static final String ADAPTER_CONF_CONTENT_VALIDATOR_CLASSNAME = "contentValidator";
    public static final String ADAPTER_CONF_CONTENT_VALIDATOR_CLASSNAME_HINT = "contentValidator.hint";
    public static final String ADAPTER_CONF_CONTENT_TRANSFORMER_CLASSNAME = "contentTransformer";
    public static final String ADAPTER_CONF_CONTENT_TRANSFORMER_CLASSNAME_HINT = "contentTransformer.hint";
    public static final String ADAPTER_MESSAGE_TOPIC = "topic";
    public static final String ADAPTER_MESSAGE_TOPIC_HINT = "topic.hint";
    public static final String ADAPTER_CONF_CLIENTID = "clientId";
    public static final String ADAPTER_CONF_CLIENTID_HINT = "clientId.hint";
    public static final String ADAPTER_CONF_CLEAN_SESSION = "cleanSession";
    public static final String ADAPTER_CONF_CLEAN_SESSION_HINT = "cleanSession.hint";
    public static final String ADAPTER_CONF_KEEP_ALIVE = "keepAlive";
    public static final int ADAPTER_CONF_DEFAULT_KEEP_ALIVE = 60000;

    public static final int INITIAL_RECONNECTION_DURATION = 4000;
    public static final int RECONNECTION_PROGRESS_FACTOR = 2;

    public static final String EMPTY_STRING = "";
    public static final String GRANT_TYPE_PARAM_NAME = "grant_type";
    public static final String GRANT_TYPE = "password refresh_token";
    public static final String TOKEN_SCOPE = "production";
    public static final String APPLICATION_NAME_PREFIX = "InputAdapter_";
    public static final String CLIENT_ID = "clientId";
    public static final String CLIENT_SECRET = "clientSecret";
    public static final String CLIENT_NAME = "client_name";
    public static final String DEFAULT = "default";
    public static final String MQTT_CONTENT_VALIDATION_DEFAULT_PARAMETERS = "";
    public static final String TOPIC = "topic";
    public static final String PAYLOAD = "payload";
    public static final String DEVICE_ID_JSON_PATH = "event.metaData.deviceId";
    public static final String DEVICE_TYPE_JSON_PATH = "event.metaData.deviceId";
    public static final int DEVICE_ID_TOPIC_HIERARCHY_INDEX = 2;
    public static final String AUTHORIZATION_HEADER_NAME = "Authorization";
    public static final String AUTHORIZATION_HEADER_VALUE_PREFIX = "Basic ";
    public static final String PASSWORD_GRANT_TYPE = "password";
    public static final String PASSWORD_GRANT_TYPE_USERNAME = "username";
    public static final String PASSWORD_GRANT_TYPE_PASSWORD = "password";
    public static final String PASSWORD_GRANT_TYPE_SCOPES = "scopes";
    public static final String ACCESS_TOKEN_GRANT_TYPE_PARAM_NAME = "access_token";

}
