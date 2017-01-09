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

import org.wso2.carbon.event.input.adapter.core.InputEventAdapterConfiguration;
import org.wso2.carbon.event.input.adapter.core.exception.InputEventAdapterException;

import java.util.Map;

/**
 * This holds the configurations related to MQTT Broker.
 */
public class MQTTBrokerConnectionConfiguration {

    private String username = null;
    private String password = null;
    private String brokerScopes = null;
    private boolean cleanSession = true;
    private int keepAlive;
    private String brokerUrl;
    private String dcrUrl;
    private String tokenUrl;
    private String contentValidatorClassName;
    private String contentTransformerClassName;
    private String adapterName;

    public String getBrokerScopes() {
        return brokerScopes;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public boolean isCleanSession() {
        return cleanSession;
    }

    public String getBrokerUrl() {
        return brokerUrl;
    }

    public String getDcrUrl() {
        return dcrUrl;
    }

    public int getKeepAlive() {
        return keepAlive;
    }

    public String getContentValidatorClassName() {
        return contentValidatorClassName;
    }

    public String getContentTransformerClassName() {
        return contentTransformerClassName;
    }

    public String getTokenUrl() {
        return tokenUrl;
    }

    public String getAdapterName() {
        return adapterName;
    }

    public MQTTBrokerConnectionConfiguration(InputEventAdapterConfiguration eventAdapterConfiguration,
                                             Map<String, String> globalProperties) throws InputEventAdapterException {

        adapterName = eventAdapterConfiguration.getName();
        this.username = eventAdapterConfiguration.getProperties().get(MQTTEventAdapterConstants.ADAPTER_CONF_USERNAME);
        this.password = eventAdapterConfiguration.getProperties().get(MQTTEventAdapterConstants.ADAPTER_CONF_PASSWORD);
        this.brokerScopes = eventAdapterConfiguration.getProperties().get(MQTTEventAdapterConstants.ADAPTER_CONF_SCOPES);
        if (brokerScopes == null) {
            this.brokerScopes = MQTTEventAdapterConstants.EMPTY_STRING;
        }
        String url = eventAdapterConfiguration .getProperties().get(MQTTEventAdapterConstants.ADAPTER_CONF_URL);
        if (url == null || url.isEmpty()) {
            url = globalProperties.get(MQTTEventAdapterConstants.ADAPTER_CONF_URL);
        }
        this.brokerUrl = PropertyUtils.replaceMqttProperty(url);
        this.dcrUrl = PropertyUtils
                .replaceMqttProperty(globalProperties.get(MQTTEventAdapterConstants.ADAPTER_CONF_DCR_URL));
        this.tokenUrl = PropertyUtils
                .replaceMqttProperty(globalProperties.get(MQTTEventAdapterConstants.ADAPTER_CONF_TOKEN_URL));
        this.contentValidatorClassName = eventAdapterConfiguration.getProperties()
                .get(MQTTEventAdapterConstants.ADAPTER_CONF_CONTENT_VALIDATOR_CLASSNAME);
        this.cleanSession = Boolean.parseBoolean(eventAdapterConfiguration.getProperties()
                                                         .get(MQTTEventAdapterConstants.ADAPTER_CONF_CLEAN_SESSION));
        //If global properties are available those will be assigned else constant values will be assigned
        if (globalProperties.get(MQTTEventAdapterConstants.ADAPTER_CONF_KEEP_ALIVE) != null) {
            keepAlive = Integer.parseInt((globalProperties.get(MQTTEventAdapterConstants.ADAPTER_CONF_KEEP_ALIVE)));
        } else {
            keepAlive = MQTTEventAdapterConstants.ADAPTER_CONF_DEFAULT_KEEP_ALIVE;
        }
        this.contentTransformerClassName = eventAdapterConfiguration.getProperties()
                .get(MQTTEventAdapterConstants.ADAPTER_CONF_CONTENT_TRANSFORMER_CLASSNAME);
    }
}
